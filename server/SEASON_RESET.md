# New season world: one lake, no rivers, rare sky islands, 2k border

## Terrain (`datapacks/CCH-Season-Terrain`)
| File | Change |
|---|---|
| `cch:spawn_lake_distance` + `minecraft:overworld/continents` | The world is all land, except **one small ocean at 0,0** (radius ~40 blocks with a natural shoreline, about 4,500 blocks²). It's an exact circle from Lithostitched's `axis`/`sqrt` density functions, so it doesn't depend on the seed. |
| `minecraft:overworld/ridges` | **No rivers.** Weirdness never enters the river band, so there are no river valleys and no river biomes. |
| `minecraft:overworld/offset` | Land sits ~6 blocks higher, so nothing but the lake is below sea level: no swamp or lowland standing water. Swamps and mangroves still generate (slime chunks, mangrove saplings), on dry ground. |
| `minecraft:noise_settings/overworld` | Vanilla surface terrain plus Skylands' sky islands above y=120. Aquifers never flood: caves are dry (deep lava stays). |
| `skylands_over_the_sea:islands/layer1..3/noise` | The low island layer is off (it would hit hills). The two high layers are rarer: about 2.4% of the sky each instead of 10%. |
| `minecraft:placed_feature/spring_water` | Water springs only generate at y 130–300, on sky-island cliffs. |
| `minecraft:placed_feature/lush_caves_clay` | Off: lush caves no longer get clay pools full of water. |
| `minecraft:placed_feature/sulfur_pool` | Sulfur Caves (Vanilla Backport) water pools cut from 256 to 32 placement tries per chunk, so potent sulfur still shows up, rarely. |

**Ocean blocks on land fixed:** biomes now follow the same land/lake shape as the terrain. Ocean biomes exist only in the spawn lake, so kelp, seagrass, shipwrecks and ocean ruins can't appear on land.

Tested on a 2000×2000 pregenerated test world (whole world, every height):
- The spawn lake holds ~78,000 water blocks.
- Everything else together holds ~21,000. That's fewer than 1 block per 190 columns, almost all of it buried in caves: dripstone caves, Alex's Caves' Abyssal Chasm, the few remaining sulfur pools, and structure water like wells. Before the cave fixes it was ~25,000.
- No ocean or river biome anywhere but the lake.
- `season-terrain-preview.png` is a top-down map of that world (1 px = 2 blocks). Blue is any fluid at the surface: the lake in the middle, and small dots that are mostly lava pools and sky-island springs. Surface fluid covers 0.18% of the area.

`ZZ-CCH-Reforged-Balance` is named so it loads after `CCH4.2` in a fresh world. Datapacks found in a new world are enabled in alphabetical order, and the fixes must load after the packs they fix.

## Other dimensions
| Dimension | How you get in | Water | Border |
|---|---|---|---|
| **Bumblezone** (`the_bumblezone:the_bumblezone`) | A **Bumble Portal Pearl** thrown at an Overworld bee nest/hive; throw another anywhere inside to leave | No sugar-water sea (sea level 0); only small sugar-water falls. Honey is the resource. | ChunkyBorder ±250 (1:4 scale = overworld ±1000) |
| **Otherside** (`deeperdarker:otherside`) | Ancient City portal lit with a Heart of the Deep, which now needs Shulker Shells + Dragon's Breath (after the Ender Dragon) | No water pools; the dimension's fluid is sculk grime | ChunkyBorder ±1000 |
| Nether, End | unchanged | — | follows the overworld border |

`season-reset.sh` adds both ChunkyBorders and pregenerates both dimensions.

Optional lock: `/function bumble_portal:lock` closes the Bumblezone (thrown pearls are returned) and `/function bumble_portal:unlock` opens it. For the Otherside, don't hand out Hearts of the Deep; there's no other way in.

## Border
- Locked final size: **2000 × 2000** (±1000 around 0,0).
- Opens at **200 × 200** around the lake.
- Run this when the season opens; it grows to 2000 over **one week** of server uptime (restarts pause it): `/worldborder set 2000 604800`

## What `season-reset.sh` does on wings1
Everything is staged in `/home/celeste/` (`season-reset.sh`, `season-datapacks.tar`, `season-staging/` with the Bumblezone and Deeper and Darker jars and the tuned Bumblezone config). Nothing touches the live world until the script runs.
1. Stops the server through Wings.
2. Moves the old world to `/home/celeste/backups/` (not deleted).
3. Creates `world/datapacks/` with all datapacks from this folder, and copies the old `world/serverconfig/` (claims settings).
4. Installs the staged mods and config.
5. Starts the server and carries over the old world's gamerules:
   - `waterSourceConversion false`
   - `playersSleepingPercentage 30`
   - `spawnRadius 0`
   - `spawnChunkRadius 0`
   - `doSeasonCycle true`
6. Sets the 200 border at 0,0 and pregenerates ±1000 with Chunky (about 15 minutes at 20–40 chunks/s).
7. Adds the Bumblezone (±250) and Otherside (±1000) ChunkyBorders and pregenerates both. That takes about 3 minutes at 70–100 chunks/s.

Run it: `sudo bash /home/celeste/season-reset.sh`. Push pack 1.0.9 at the same time, so players' clients get the two new mods.
