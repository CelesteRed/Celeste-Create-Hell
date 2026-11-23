#!/usr/bin/env python3
"""Build the bootstrap .mrpack players import into the Modrinth App.

The .mrpack holds the whole pack as it is right now (so the first launch doesn't have to
download everything through unsup) plus the three files that make auto-updating work:
unsup.jar, unsup.ini and the Relauncher config. After that, unsup keeps the instance in
sync with pack/ on GitHub, so this file only needs rebuilding for brand-new players, and
even an old copy works (unsup catches it up on first launch).

Usage:  python3 tools/build_mrpack.py            -> dist/Celeste-Create-Hell-<version>.mrpack
"""
import hashlib
import json
import pathlib
import sys
import tomllib
import urllib.parse
import urllib.request
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
PACK = ROOT / "pack"
LAUNCHER = ROOT / "launcher"
DIST = ROOT / "dist"
UA = {"User-Agent": "CelesteRed/Celeste-Create-Hell mrpack builder"}
# Hosts the Modrinth App accepts as download sources inside an .mrpack.
ALLOWED_HOSTS = {"cdn.modrinth.com", "github.com", "raw.githubusercontent.com", "gitlab.com"}


def api(url):
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.loads(r.read())


def main():
    pack = tomllib.loads((PACK / "pack.toml").read_text("utf-8"))
    index = tomllib.loads((PACK / pack["index"]["file"]).read_text("utf-8"))
    version = pack["version"]

    metafiles, raw = [], []
    for entry in index["files"]:
        (metafiles if entry.get("metafile") else raw).append(entry["file"])

    # Collect Modrinth version ids so sha1/size can be fetched in bulk.
    metas = []
    for rel in metafiles:
        meta = tomllib.loads((PACK / rel).read_text("utf-8"))
        meta["_dir"] = str(pathlib.PurePosixPath(rel).parent)
        metas.append(meta)
    version_ids = sorted({m["update"]["modrinth"]["version"] for m in metas
                          if "modrinth" in m.get("update", {})})
    versions = {}
    for i in range(0, len(version_ids), 100):
        chunk = version_ids[i:i + 100]
        for v in api("https://api.modrinth.com/v2/versions?ids=" + urllib.parse.quote(json.dumps(chunk))):
            versions[v["id"]] = v

    files, skipped = [], []
    for m in metas:
        side = m.get("side", "both")
        if side == "server":
            continue  # server-only mods never go to players
        url = m["download"].get("url")
        host = urllib.parse.urlparse(url).hostname if url else None
        mr = m.get("update", {}).get("modrinth")
        if not url or host not in ALLOWED_HOSTS or not mr or mr["version"] not in versions:
            skipped.append(m["filename"])  # unsup downloads these on first launch instead
            continue
        vf = next(f for f in versions[mr["version"]]["files"] if f["hashes"]["sha512"] == m["download"]["hash"])
        files.append({
            "path": f"{m['_dir']}/{m['filename']}",
            "hashes": {"sha1": vf["hashes"]["sha1"], "sha512": vf["hashes"]["sha512"]},
            "env": {"client": "required", "server": "required" if side == "both" else "unsupported"},
            "downloads": [url],
            "fileSize": vf["size"],
        })

    index_json = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": version,
        "name": pack["name"],
        "summary": "Celeste Create Hell - updates itself from GitHub on every launch (unsup).",
        "files": sorted(files, key=lambda f: f["path"]),
        "dependencies": {"minecraft": pack["versions"]["minecraft"], "neoforge": pack["versions"]["neoforge"]},
    }

    DIST.mkdir(exist_ok=True)
    out = DIST / f"Celeste-Create-Hell-{version}.mrpack"
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("modrinth.index.json", json.dumps(index_json, indent=2))
        for rel in raw:
            z.write(PACK / rel, f"overrides/{rel}")
        z.write(LAUNCHER / "unsup.jar", "overrides/unsup.jar")
        z.write(LAUNCHER / "unsup.ini", "overrides/unsup.ini")
    print(f"Wrote {out} ({len(files)} downloads, {len(raw) + 2} override files)")
    if skipped:
        print("Not in the .mrpack (unsup fetches them on first launch): " + ", ".join(sorted(skipped)))


if __name__ == "__main__":
    sys.exit(main())
