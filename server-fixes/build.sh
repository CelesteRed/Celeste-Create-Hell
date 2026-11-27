#!/usr/bin/env bash
# Builds celeste-server-fixes.jar (server-only NeoForge 1.21.1 mod). Run from WSL; uses the Windows JDK.
# Needs a NeoForge server install for the compile classpath (Minecraft + Mixin jars).
set -euo pipefail
VERSION=1.5.0
ROOT="$(cd "$(dirname "$0")" && pwd)"
SERVER="${SERVER:-/mnt/c/CelesteTest/server}"
JDK="${JDK:-/mnt/c/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot/bin}"
MC="$SERVER/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar"
MIXIN="$(ls "$SERVER"/libraries/net/fabricmc/sponge-mixin/*/sponge-mixin-*.jar | head -1)"
CREATE="$(ls "$SERVER"/mods/create-1.21.1-*.jar | head -1)"
GSON="$(ls "$SERVER"/libraries/com/google/code/gson/gson/*/gson-*.jar | head -1)"
NFSRV="$(ls "$SERVER"/libraries/net/neoforged/neoforge/*/neoforge-*-server.jar | head -1)"
NFUNI="$(ls "$SERVER"/libraries/net/neoforged/neoforge/*/neoforge-*-universal.jar | head -1)"
BRIG="$(ls "$SERVER"/libraries/com/mojang/brigadier/*/brigadier-*.jar | head -1)"
DFU="$(ls "$SERVER"/libraries/com/mojang/datafixerupper/*/datafixerupper-*.jar | head -1)"
CEI="$(ls "$SERVER"/mods/create-enchantment-industry-*.jar | head -1)"
HANDSON="$(ls "$SERVER"/mods/handson_delight-*.jar | head -1)"
OUT="$ROOT/build"; rm -rf "$OUT"; mkdir -p "$OUT/classes"
w() { wslpath -w "$1"; }
mapfile -t SRC < <(find "$ROOT/src/main/java" -name '*.java')
WSRC=(); for s in "${SRC[@]}"; do WSRC+=("$(w "$s")"); done
"$JDK/javac.exe" --release 21 -encoding UTF-8 -proc:none -cp "$(w "$NFSRV");$(w "$NFUNI");$(w "$BRIG");$(w "$MC");$(w "$MIXIN");$(w "$CREATE");$(w "$GSON");$(w "$DFU");$(w "$CEI");$(w "$HANDSON")" -d "$(w "$OUT/classes")" "${WSRC[@]}"
cp -r "$ROOT/src/main/resources/." "$OUT/classes/"
"$JDK/jar.exe" --create --file "$(w "$OUT/celeste-server-fixes-$VERSION.jar")" -C "$(w "$OUT/classes")" .
echo "built $OUT/celeste-server-fixes-$VERSION.jar"
