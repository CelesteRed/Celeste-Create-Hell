# New season world: one lake, no rivers, rare sky islands, 2k border

## Terrain (`datapacks/CCH-Season-Terrain`)
| File | Change |
|---|---|
| `cch:spawn_lake_distance` + `minecraft:overworld/continents` | The world is all land, except **one small ocean at 0,0** (radius ~40 blocks with a natural shoreline, about 4,500 blocks²). It's an exact circle from Lithostitched's `axis`/`sqrt` density functions, so it doesn't depend on the seed. |
| `minecraft:overworld/ridges` | **No rivers.** Weirdness never enters the river band, so there are no river valleys and no river biomes. |
| `minecraft:overworld/offset` | Land sits ~6 blocks higher, so nothing but the lake is below sea level: no swamp or lowland standing water. Swamps and mangroves still generate (slime chunks, mangrove saplings), on dry ground. |
| `minecraft:noise_settings/overworld` | Vanilla surface terrain plus Skylands' sky islands above y=120. Aquifers never flood: caves are dry (deep lava stays). |
| `skylands_over_the_sea:islands/layer1..3/noise` | The low island layer is off (it would hit hills). The two high layers are rarer: about 2.4% of the sky each instead of 10%. |
| `minecraft:placed_feature/spring_water` | Water springs only generate at y 130–300, on sky-island cliffs. They're the only water besides the lake. |

**Ocean blocks on land fixed:** biomes now follow the same land/lake shape as the terrain. Ocean biomes exist only in the spawn lake, so kelp, seagrass, shipwrecks and ocean ruins can't appear on land.

Tested on a 2000×2000 pregenerated test world:
- 0.21% of the surface is water: the 4,500 blocks² spawn lake, plus a little water on sky islands.
- No ocean biome anywhere but the lake.
- `season-terrain-preview.png` shows the result (1 px = 4 blocks; blue = water, pink = sky islands, green→white = height).

`ZZ-CCH-Reforged-Balance` is named so it loads after `CCH4.2` in a fresh world. Datapacks found in a new world are enabled in alphabetical order, and the fixes must load after the packs they fix.

## Border
- Locked final size: **2000 × 2000** (±1000 around 0,0).
- Opens at **200 × 200** around the lake.
- Run this when the season opens; it grows to 2000 over **one week** of server uptime (restarts pause it): `/worldborder set 2000 604800`

## What was done on wings1
1. Stopped the server through Wings.
2. Moved the old world to `/home/celeste/backups/` (not deleted).
3. Created `world/datapacks/` with all datapacks from this folder, and copied the old `world/serverconfig/` (claims settings).
4. Started the server, carried over the old world's gamerules:
   - `waterSourceConversion false`
   - `playersSleepingPercentage 30`
   - `spawnRadius 0`
   - `spawnChunkRadius 0`
   - `doSeasonCycle true`
5. Set the 200 border at 0,0 and pregenerated ±1000 with Chunky (about 11 minutes at 25–40 chunks/s).
