#!/usr/bin/env python3
"""Build the bootstrap .mrpack players import into the Modrinth App.

The .mrpack holds the whole pack as it is right now (so the first launch doesn't have to
download everything through unsup) plus the three files that make auto-updating work:
unsup.jar, unsup.ini and the Relauncher config. After that, unsup keeps the instance in
sync with pack/ on GitHub, so this file only needs rebuilding for brand-new players, and
even an old copy works (unsup catches it up on first launch).

Usage:  python3 tools/build_mrpack.py              -> dist/Celeste-Create-Hell-<version>.mrpack
        python3 tools/build_mrpack.py --modrinth    -> dist/Celeste-Create-Hell-<version>-modrinth.mrpack
                For publishing on Modrinth: no jars in overrides (our own jars such as
                celeste-early-fixes are left out; unsup adds them on first launch from GitHub),
                every download comes from Modrinth's CDN.
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
    modrinth = "--modrinth" in sys.argv[1:]
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

    # unsup.jar sits in the instance root. If this exact file is on Modrinth (it should be),
    # reference it as a download instead of bundling it.
    unsup_bytes = (LAUNCHER / "unsup.jar").read_bytes()
    unsup_sha512 = hashlib.sha512(unsup_bytes).hexdigest()
    req = urllib.request.Request("https://api.modrinth.com/v2/version_files",
                                 data=json.dumps({"hashes": [unsup_sha512], "algorithm": "sha512"}).encode(),
                                 headers={**UA, "Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=60) as r:
        unsup_version = json.loads(r.read()).get(unsup_sha512)
    bundle_unsup = unsup_version is None
    if unsup_version:
        uf = next(f for f in unsup_version["files"] if f["hashes"]["sha512"] == unsup_sha512)
        files.append({
            "path": "unsup.jar",
            "hashes": {"sha1": uf["hashes"]["sha1"], "sha512": unsup_sha512},
            "env": {"client": "required", "server": "required"},
            "downloads": [uf["url"]],
            "fileSize": uf["size"],
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
    out = DIST / f"Celeste-Create-Hell-{version}{'-modrinth' if modrinth else ''}.mrpack"
    left_out = []
    if modrinth:
        if bundle_unsup:
            sys.exit("unsup.jar is not on Modrinth, so it can't be referenced; refusing to bundle a jar")
        bad = [f["path"] for f in files if not all(urllib.parse.urlparse(u).hostname == "cdn.modrinth.com" for u in f["downloads"])]
        if bad:
            sys.exit("non-Modrinth downloads: " + ", ".join(bad))
        left_out = [r for r in raw if r.lower().endswith((".jar", ".jar.disabled"))]
        raw = [r for r in raw if r not in left_out]
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("modrinth.index.json", json.dumps(index_json, indent=2))
        for rel in raw:
            z.write(PACK / rel, f"overrides/{rel}")
        if bundle_unsup:
            z.write(LAUNCHER / "unsup.jar", "overrides/unsup.jar")
        z.write(LAUNCHER / "unsup.ini", "overrides/unsup.ini")
        # Starting server list (Survival + Creative). Only in the .mrpack, so unsup never overwrites a player's own list.
        z.write(LAUNCHER / "servers.dat", "overrides/servers.dat")
    print(f"Wrote {out} ({len(files)} downloads, {len(raw) + 1 + bundle_unsup} override files"
          f"{', unsup.jar bundled (not on Modrinth)' if bundle_unsup else ''})")
    if left_out:
        print("Left out of the Modrinth .mrpack (unsup adds them on first launch): " + ", ".join(left_out))
    if skipped:
        print("Not in the .mrpack (unsup fetches them on first launch): " + ", ".join(sorted(skipped)))


if __name__ == "__main__":
    sys.exit(main())
