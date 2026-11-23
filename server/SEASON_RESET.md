# New season world: finite water, 2k border

## What the terrain datapack does (`datapacks/CCH-Season-Terrain`)
| File | Change |
|---|---|
| `worldgen/density_function/overworld/continents.json` | Replaces the Continents mod and Skylands' ocean shapes. Almost everything becomes inland, and a separate low-frequency "lakes" noise (`cch:lakes`) cuts a few round basins that generate as small ocean biomes with beaches. With the threshold at -0.69, about 0.5% of the area is lake, which works out to about 6 lakes inside ±1000. |
| `worldgen/density_function/overworld/ridges_folded.json` | Rivers keep their paths but their valleys are about a third as wide (terrain only; biome placement is unchanged) |
| `worldgen/density_function/overworld/offset.json` | All terrain sits ~2.5 blocks higher, so shallow swamp and mangrove water mostly becomes land and mud. Swamps and mangroves still exist (slime chunks, mangrove saplings), with only small puddles. |
| `worldgen/noise_settings/overworld.json` | Vanilla settings with aquifer floodedness ×0.6: fewer and smaller flooded caves (not removed) |

Note: this overrides the overworld shape from the **Continents** and **Skylands Over The Sea** mods. Their own blocks and biomes stay, but the world is no longer "continents + oceans" or "sky islands over an endless sea".

`ZZ-CCH-Reforged-Balance` is named so it loads after `CCH4.2` in a fresh world. Datapacks found in a new world are enabled in alphabetical order, and the fixes must load after the packs they fix.

## Border
- Locked final size: **2000 × 2000** (±1000 around 0,0).
- Opens at **200 × 200** and grows to 2000 over **one week** of server uptime. The border only moves while the server is running; restarts pause it.

```
/worldborder center 0 0
/worldborder set 200
# when the season opens:
/worldborder set 2000 604800
```

## Reset steps (done on wings1)
1. Stop the server through Wings.
2. Move the old world to `/home/celeste/backups/` (kept, not deleted).
3. Create `world/datapacks/` with all datapacks from this folder, and copy the old `world/serverconfig/` (claims settings).
4. Set `level-seed` in `server.properties` to the tested seed.
5. Start the server, carry over the old world's gamerules:
   - `waterSourceConversion false`
   - `playersSleepingPercentage 30`
   - `spawnRadius 0`
   - `spawnChunkRadius 0`
   - `doSeasonCycle true`
6. Set the 200 border, then pregenerate ±1000 with Chunky so nobody hits world generation lag.
