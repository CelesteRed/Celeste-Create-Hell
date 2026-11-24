#!/usr/bin/env bash
# New season world for Celeste Create Hell. Run as root on wings1.
# Expects /home/celeste/season-datapacks.tar (contents of server/datapacks/).
# The old world is moved to /home/celeste/backups/, never deleted.
set -euo pipefail
UUID=235d0353-1189-48f6-9ce8-9ca949277a4a
V=/var/lib/pterodactyl/volumes/$UUID
SEED="${SEED:-CelesteCreateHellSeason}"
T=$(grep -E "^token:" /etc/pterodactyl/config.yml | awk '{print $2}' | tr -d '"')
U=https://127.0.0.1:8080/api/servers/$UUID
api() { curl -sk -H "Authorization: Bearer $T" -H "Content-Type: application/json" "$@"; }
state() { api "$U" | grep -o '"state":"[a-z]*"' | head -1 | cut -d'"' -f4; }
cmd() { api -o /dev/null -X POST "$U/commands" -d "{\"commands\":[$1]}"; }

echo "== stopping"
api -o /dev/null -X POST "$U/power" -d '{"action":"stop"}'
for i in $(seq 1 60); do [ "$(state)" = offline ] && break; sleep 5; done
[ "$(state)" = offline ] || { echo "server did not stop"; exit 1; }

STAMP=$(date +%Y-%m-%d_%H%M)
echo "== moving old world to /home/celeste/backups/world-season-end-$STAMP"
mv "$V/world" "/home/celeste/backups/world-season-end-$STAMP"

echo "== new world folder"
mkdir -p "$V/world/datapacks"
tar xf /home/celeste/season-datapacks.tar -C "$V/world/datapacks"
cp -r "/home/celeste/backups/world-season-end-$STAMP/serverconfig" "$V/world/" 2>/dev/null || true
rm -rf "$V/config/chunky/tasks/"*
# New-season mods/configs (Bumblezone, Deeper and Darker, tuned Bumblezone config), staged ahead of time.
S=/home/celeste/season-staging
if [ -d "$S/mods" ]; then
  echo "== installing staged mods: $(ls "$S/mods" | tr '\n' ' ')"
  cp -n "$S/mods/"*.jar "$V/mods/"
  [ -d "$S/config" ] && cp -r "$S/config/." "$V/config/"
  chown -R pterodactyl:pterodactyl "$V/mods" "$V/config"
fi
if grep -q '^level-seed=' "$V/server.properties"; then
  sed -i "s/^level-seed=.*/level-seed=$SEED/" "$V/server.properties"
else
  echo "level-seed=$SEED" >> "$V/server.properties"
fi
chown -R pterodactyl:pterodactyl "$V/world" "$V/server.properties" "$V/config/chunky"

echo "== starting"
api -o /dev/null -X POST "$U/power" -d '{"action":"start"}'
L="$V/logs/latest.log"
sleep 20
for i in $(seq 1 120); do grep -q "Done (" "$L" 2>/dev/null && break; sleep 5; done
grep -q "Done (" "$L" || { echo "server did not finish starting"; tail -20 "$L"; exit 1; }
sleep 10

echo "== gamerules, border, pregeneration"
cmd '"gamerule waterSourceConversion false","gamerule playersSleepingPercentage 30","gamerule spawnRadius 0","gamerule spawnChunkRadius 0","gamerule doSeasonCycle true"'
sleep 2
cmd '"worldborder center 0 0","worldborder set 200"'
sleep 2
cmd '"function chunky_offline:config/set {radius:1000,x:0,z:0}","chunky cancel","chunky confirm","chunky center 0 0","chunky shape square","chunky radius 1000","chunky start"'
sleep 2
# Per-dimension borders (ChunkyBorder; saved in config/chunkyborder/borders.json) + pregeneration.
# Bumblezone is 1:4 scale, so +-250 there covers the same ground as the overworld's +-1000.
cmd '"chunky world the_bumblezone:the_bumblezone","chunky center 0 0","chunky shape square","chunky radius 250","chunky border add","chunky start"'
sleep 2
cmd '"chunky world deeperdarker:otherside","chunky center 0 0","chunky shape square","chunky radius 1000","chunky border add","chunky start","chunky world minecraft:overworld"'
echo "== done; pregeneration of +-1000 is running (about 15-25 minutes; Bumblezone/Otherside take ~3 minutes)"
