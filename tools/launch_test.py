#!/usr/bin/env python3
"""Launch a NeoForge 1.21.1 client instance like the Modrinth App does, with an offline account.
Usage: launch_test.py <instance dir (WSL path)> <stdout log>   (env EXTRA_GAME_ARGS, NF_VER)"""
import json, os, subprocess, sys, uuid

META = "/mnt/c/Users/celes/AppData/Roaming/ModrinthApp/meta"
VER = os.environ.get("NF_VER", "1.21.1-21.1.267")
JAVA = f"{META}/java_versions/zulu21.46.19-ca-jre21.0.9-win_x64/bin/java.exe"


def win(p):
    return subprocess.check_output(["wslpath", "-w", p], text=True).strip()


def allowed(rules):
    if not rules:
        return True
    ok = False
    for r in rules:
        osr = r.get("os") or {}
        match = not (osr.get("name") and osr["name"] != "windows") and osr.get("arch") != "x86" and not r.get("features")
        if match:
            ok = r["action"] == "allow"
    return ok


def main():
    inst, outlog = sys.argv[1], sys.argv[2]
    v = json.load(open(f"{META}/versions/{VER}/{VER}.json"))
    cp, seen = [], set()
    for lib in v["libraries"]:
        parts = lib["name"].split(":")
        key = (parts[0], parts[1], parts[3] if len(parts) > 3 else "")
        if key in seen or not allowed(lib.get("rules")) or lib.get("include_in_classpath") is False:
            continue
        seen.add(key)
        g, a, ver = parts[0], parts[1], parts[2]
        cls = f"-{parts[3]}" if len(parts) > 3 else ""
        path = f"{META}/libraries/{g.replace('.', '/')}/{a}/{ver}/{a}-{ver}{cls}.jar"
        if os.path.exists(path):
            cp.append(win(path))
    cp.append(win(f"{META}/versions/{VER}/{VER}.jar"))
    subs = {
        "natives_directory": win(f"{META}/natives/{VER}"), "launcher_name": "celeste-test", "launcher_version": "1",
        "classpath": ";".join(cp), "classpath_separator": ";", "library_directory": win(f"{META}/libraries"),
        "version_name": VER, "auth_player_name": "CelesteTest", "game_directory": win(inst),
        "assets_root": win(f"{META}/assets"), "assets_index_name": v["assetIndex"]["id"],
        "auth_uuid": uuid.uuid4().hex, "auth_access_token": "0", "clientid": "0", "auth_xuid": "0",
        "user_type": "legacy", "version_type": "release",
    }

    def expand(arglist):
        out = []
        for a in arglist:
            if isinstance(a, dict):
                if not allowed(a.get("rules")):
                    continue
                vals = a["value"] if isinstance(a["value"], list) else [a["value"]]
            else:
                vals = [a]
            for s in vals:
                for k, val in subs.items():
                    s = s.replace("${" + k + "}", val)
                out.append(s)
        return out

    cmd = [JAVA, "-Xmx10G", "-Xms2G"] + expand(v["arguments"]["jvm"]) + [v["mainClass"]] + expand(v["arguments"]["game"])
    if os.environ.get("EXTRA_GAME_ARGS"):
        cmd += os.environ["EXTRA_GAME_ARGS"].split()
    with open(outlog, "w") as f:
        subprocess.Popen(cmd, cwd=inst, stdout=f, stderr=subprocess.STDOUT)


if __name__ == "__main__":
    main()
