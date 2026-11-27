#!/usr/bin/env python3
"""Small packwiz-compatible helper (no Go/packwiz needed).

  python3 tools/pack_tool.py refresh                      rebuild pack/index.toml and the pack.toml index hash
  python3 tools/pack_tool.py add <slug> <version-id> [side] [subdir]
                                                          write a Modrinth .pw.toml (side: both|client|server, subdir: mods)
"""
import hashlib
import json
import pathlib
import sys
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
PACK = ROOT / "pack"
UA = {"User-Agent": "CelesteRed/Celeste-Create-Hell pack_tool"}
IGNORE_TOP = {"index.toml", "pack.toml", ".packwizignore"}


def sha256(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()


def toml_str(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def refresh():
    ignore = set()
    pwi = PACK / ".packwizignore"
    if pwi.exists():
        ignore = {l.strip() for l in pwi.read_text().splitlines() if l.strip() and not l.startswith("#")}
    entries = []
    for p in sorted(PACK.rglob("*")):
        if not p.is_file():
            continue
        rel = p.relative_to(PACK).as_posix()
        if rel in IGNORE_TOP or any(rel == i or rel.startswith(i.rstrip("/") + "/") for i in ignore):
            continue
        entries.append((rel, sha256(p), rel.endswith(".pw.toml")))
    out = ['hash-format = "sha256"', ""]
    for rel, h, meta in entries:
        out += ["[[files]]", f"file = {toml_str(rel)}", f"hash = {toml_str(h)}"]
        if meta:
            out.append("metafile = true")
        out.append("")
    (PACK / "index.toml").write_text("\n".join(out))
    idx_hash = sha256(PACK / "index.toml")
    pt = (PACK / "pack.toml").read_text().splitlines()
    in_index = False
    for i, line in enumerate(pt):
        if line.strip() == "[index]":
            in_index = True
        elif line.startswith("["):
            in_index = False
        elif in_index and line.strip().startswith("hash ="):
            pt[i] = f"hash = {toml_str(idx_hash)}"
    (PACK / "pack.toml").write_text("\n".join(pt) + "\n")
    print(f"Index refreshed: {len(entries)} files")


def add(slug, version_id, side="both", subdir="mods"):
    req = urllib.request.Request(f"https://api.modrinth.com/v2/version/{version_id}", headers=UA)
    v = json.loads(urllib.request.urlopen(req, timeout=60).read())
    f = next((x for x in v["files"] if x.get("primary")), v["files"][0])
    proj = json.loads(urllib.request.urlopen(urllib.request.Request(
        f"https://api.modrinth.com/v2/project/{v['project_id']}", headers=UA), timeout=60).read())
    text = (f"name = {toml_str(proj['title'])}\nfilename = {toml_str(f['filename'])}\nside = {toml_str(side)}\n\n"
            f"[download]\nurl = {toml_str(f['url'])}\nhash-format = \"sha512\"\nhash = {toml_str(f['hashes']['sha512'])}\n\n"
            f"[update]\n[update.modrinth]\nmod-id = {toml_str(v['project_id'])}\nversion = {toml_str(version_id)}\n")
    p = PACK / subdir / f"{slug}.pw.toml"
    p.write_text(text)
    print(f"Wrote {p.relative_to(ROOT)} -> {f['filename']}")


if __name__ == "__main__":
    a = sys.argv[1:]
    if not a or a[0] == "refresh":
        refresh()
    elif a[0] == "add":
        add(*a[1:])
    else:
        sys.exit(__doc__)
