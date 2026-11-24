# Celeste Create Hell: balance changes (2026-11-23)

Your original "hellish" setup is still in place and unchanged:
- `config/create-server.toml`: water wheel 2 SU, steam engine 512 SU, shafts 4 SU, cogwheels 6 SU, minimum 16 windmill sails, and so on.
- `CCH4.2`, `CAR3.0`, `CGunsmithCCHRebalance`, `LetsDoVineryRebalanced`, `NoMoreTemplateCrafting`, `QuickFixes`.
- `reducedores` (ores half size, half as often) and `reducedloot` (70% loot chance).

New datapack `world/datapacks/CCH-Reforged-Balance` loads after all of them. Each change below lists the old value and the new value.

## 1. Repairs to your existing datapacks
These recipes were silently failing to load on 1.21.1. Each one was already broken before today; the server logged the errors on every start.

| Recipe | Problem | Fix |
|---|---|---|
| CCH4.2 `create:large_cogwheel`, `large_cogwheel_from_little` | Old result format (`"item"` instead of `"id"`). The expensive 8-plank recipes never loaded, so large cogwheels had **no recipe at all**. | Same recipes (shaft or cogwheel + 8 planks), 1.21.1 format |
| LetsDoVineryRebalanced: apple wine, solaris wine, stal wine | Typo: `minecraft:golden_apples` (the item is `golden_apple`) | Typo fixed; recipes unchanged otherwise |
| CGunsmithCCHRebalance: rocket | One deploying step had 4 TNT, but deployers take 1 item per step | 4 deploying steps with 1 TNT each (same total cost) |
| NoMoreTemplateCrafting: 19 smithing template recipes | Old key format | Same recipes (bedrock = still uncraftable in survival), 1.21.1 format |

## 2. Power sources from the new mods, scaled to CCH's power levels
CCH halves the steam engine (1024 → 512) and makes water wheels nearly useless. The new mods' engines were still at full Create strength, which would let players skip the steam stage.

| Mod | Source | Before | After |
|---|---|---|---|
| Create: Diesel Generators | Diesel engine (normal / modular / huge) | 6144 / 8192 / 16384 SU | **1536 / 2048 / 4096** |
| | Biodiesel and gasoline engines | 4096 / 6144 / 12288 | **1024 / 1536 / 3072** |
| | Ethanol engines | 2048 / 3072 / 6144 | **512 / 768 / 1536** |
| | Plant oil engines | 2048 / 2560 / 4096 | **512 / 640 / 1024** |
| Create: Dreams n' Desires | Stirling engine | 1024 SU | **512** (same as the steam engine) |
| | Cog crank / large cog crank | 8 SU | **6** (same as the hand crank) |
| Create: Connected | Crank wheel / large crank wheel | 8 SU | **6** |

Diesel stays the strongest power source, since it needs a full oil drilling, distilling and fuel chain, but no longer at four times a steam engine. (Datapack: `data/createdieselgenerators/createdieselgenerators/fuel_type/*.json`)

## 3. Closing stress loopholes
CCH charges for every shaft (4), cogwheel (6), gearbox (1) and gearshift (4). The new mods added versions of these parts that cost 0 stress, so players could route power through them to avoid CCH's costs.

| Mod | Part | Before | After |
|---|---|---|---|
| Create: Connected | Brass gearbox, six-way gearbox | 0 | **1** (same as gearbox) |
| | Parallel gearbox, encased chain cogwheel | 0 | **2** (same as encased chain drive) |
| | Inverted gearshift | 0 | **4** (same as gearshift) |
| | Shear pin | 0 | **4** (same as shaft) |
| Create: Dreams n' Desires | Omni gearbox | 0 | **1** |
| | Inverse gearshift | 0 | **4** |
| | Omni speed controller | 0 | **8** (same as rotation speed controller) |

## 4. Making cheap, overpowered items cost more
| Mod | Setting | Before | After | Why |
|---|---|---|---|---|
| Create Cobblestone | Output per second per RPM | 2.5 | **0.1** | At 256 RPM it made 640 stone per second for free, which ended the need for drills and quarries. Now about 26 per second at 256 RPM. |
| | Stress per RPM | 8 | **16** | A real power cost |
| Create: Enchantment Industry | Enchanted book printing cost | ×1 | **×3** | Copying max-level books was the cheapest way to get every enchant |
| | Super enchanting / super forging cost | ×1 | **×2** | Going past max enchantment levels should be an endgame project |
| | Ignore enchantment compatibility | true | **false** | Stops "god gear" with normally incompatible enchantments (all protections together, Mending + Infinity, and so on) |
| | XP from deployer kills | ×1 | **×0.5** | Automatic mob-grinder XP was too fast |

## 5. Mod changes on the server
- **Kept (so cannons, radars and radios in the world don't disappear):** Create Big Cannons, Ritchie's Projectile Lib, CBC Equally Opposite, Create Radars, Simple Radio (+ Lexiconfig), Create Aeronautics Compat. These are now in the client pack too, so players can join.
- **Kept server-only:** Anti-Xray, Reduced Ores, Reduced Loot, Xaero's Map Server Utils, Custom Player Models.
- **Removed:** Fast Noise (crashes with Anti-Xray), duplicate Sable/Aeronautics/etc. jars left over from this morning's update attempt, Sound Physics (client-only), the old Miku Plushie (replaced by the new Miku Plushies; old plushie blocks placed in the world are gone), Vivecraft Compat and VR Swing Sprint (Modrinth lists them as client-only), Distant Horizons (it's disabled in the client pack too).

## Backups
- Full server backup from before any change: `/home/celeste/backups/cch-before-update-2026-11-23.tar.zst` on wings1 (world, mods, configs; 7.5 GB).
- The old mods folder: `/home/celeste/backups/old-mods-2026-11-23/`.

To undo a balance change, edit `world/datapacks/CCH-Reforged-Balance` or the named config file, then run `/reload` (datapacks) or restart (configs).

## 6. Finite water (season change, 2026-11-23)
Flowing Fluids was already on, so water was finite *except* in Ocean, River and Swamp biomes, which acted as infinite sources and refilled instantly. Rivers also ran water wheels for free with no flow.

| Setting (`config/flowing_fluids.json`) | Before | After | Effect |
|---|---|---|---|
| `oceanRiverSwampRefillChance` | 1.0 | **0.0** | Oceans, rivers and swamps no longer refill themselves. Water you take is gone; only rain refills it, slowly (`rainRefillChance` stays 0.01). |
| `infiniteWaterBiomeDrainSurfaceChance` | 0.5 | **1.0** | Taking water from those biomes always lowers the water level |
| `infiniteWaterBiomeNonConsumeChance` | 0.0 | 0.0 | (unchanged: taking water always uses it up) |
| `create_waterWheelMode` | REQUIRE_FLOW_OR_RIVER | **REQUIRE_FLOW** | Water wheels need actually flowing water everywhere; no free river power |
| `create_infinitePipes` | false | false | (unchanged: Create pipes move water, they don't create it) |

Also set on the world: `/gamerule waterSourceConversion false`, so vanilla's "two sources make a third" is off too (`lavaSourceConversion` was already false).

Create's own settings were already finite and are unchanged: hose pulleys never treat a lake as infinite (`hosePulleyBlockThreshold = -1`), and `bottomlessFluidMode = DENY_ALL`.

Note: a huge pump setup draining an ocean will lag the server while the water moves. This is a known Flowing Fluids limitation.

## 7. Removed Create Dupe Fix (server-only)
It calls `Thread.getStackTrace()` every time any block checks whether it can survive (flowers, grass, torches, ...). That made world generation crawl at under 1 chunk per second, and it slows every block update on the live server. Removed from the server and the pack.

## 8. New dimensions: The Bumblezone and the Otherside (pack 1.0.9)
Added from Modrinth (client and server): **The Bumblezone** 7.20.0 (needs Resourceful Lib, already in the pack) and **Deeper and Darker** 1.4.1 (adds the Otherside dimension).

Checked and not added:
- **Twilight Forest:** not on Modrinth for 1.21.1; only an unofficial port is.
- **The Undergarden:** its caves are flooded with normal water up to y=32.
- **The Aether:** fits, but kept to two dimensions for now.

### Bumblezone: `datapacks/Bumblezone-Factory-Balance` (standalone, publishable)
The full table is in that folder's README. In short:

| What | Before | After |
|---|---|---|
| Entering | Ender Pearl at any bee nest/hive | The hive must sit on **Brass Casing** (needs brass, so a heated Create mixer) |
| Super Candle | Crafting table | **Heated compacting**: 2 carvable wax + string + 250 mB honey |
| Incense Candle | Super Candle + Royal Jelly + ≤5 potions, effects ≤ level V; or crafted straight from string + wax | Super Candle + Royal Jelly + **Precision Mechanism** + ≤**3** potions, effects ≤ level **II**; the string + wax recipe is removed |
| Incense Candle Resistance cap | Resistance V | Resistance II |
| Bee Queen Royal Jelly trades | Nether Stars, Totems, Enchanted Golden Apples, Dragon Egg/Head/Breath, End Crystals, Beacons | Golden Apples, XP bottles, Golden Carrots, Glow Ink, Name Tags, Ender Chests, Turtle Eggs, 2 Sculk Catalysts |
| Pollinated Field suspicious blocks | 3.4% Enchanted Golden Apple | Golden Apple |

Config `config/the_bumblezone/general.toml` (in the pack, so clients match):

| Setting | Before | After | Effect |
|---|---|---|---|
| `crystallineFlowerExtraTierCost` | 0 | **1** | Every enchantment costs one more Crystalline Flower tier |
| `crystallineFlowerEnchantingPowerAllowedPerTier` | 8 | **6** | High-level and rare enchantments show up only at higher flower tiers |

### Otherside: Heart of the Deep needs the End (`ZZ-CCH-Reforged-Balance`)

| What | Before | After |
|---|---|---|
| Warden drop | Always drops a Heart of the Deep | **No longer drops it** (`deeperdarker:loot_modifiers/warden_heart_from_warden` gets a 0% condition). It still drops 1–3 Warden Carapace. |
| Heart of the Deep | Warden drop only | **Create Mechanical Crafting** (`cch:deeperdarker/heart_of_the_deep`): 1 Warden Carapace + 2 **Shulker Shells** + 1 **Dragon's Breath** + 6 Echo Shards |

The Heart is the only thing that lights the Otherside portal, and that portal only exists inside Ancient Cities. Shulker Shells come from End Cities, which you can only reach through the gateway that opens after the Ender Dragon dies. Dragon's Breath comes from the dragon fight.

### Water stays rare in both (`datapacks/CCH-Season-Terrain`)

| File | Before | After |
|---|---|---|
| `the_bumblezone:worldgen/noise_settings/bz_noise_settings` | `sea_level` 40, `default_fluid` sugar water | `sea_level` **0**, `default_fluid` **air** |
| `deeperdarker:worldgen/placed_feature/blooming_pool` | 2 water pools per layer per chunk (Blooming Caverns) | **0** |
| `deeperdarker:worldgen/placed_feature/blooming_water_edge` | 13 waterlogged patches per chunk | **0** |

Why `sea_level` 0 in the Bumblezone: its cave feature hardcodes sugar water below sea level. Sugar water is in the `#minecraft:water` tag, so it hydrates farmland, splashes in Create fans and counts as water everywhere. The Bumblezone's own honey (honey blocks, honey crystals, small honey fluid pools, brood) is untouched and is the dimension's resource.

Create can't create fluid from Bumblezone fluids either. Bumblezone adds them to Create's "bottomless" tag, but Create's `bottomlessFluidMode = DENY_ALL` ignores that tag.

### Borders (ChunkyBorder, saved in `config/chunkyborder/borders.json`)
The overworld border also applies to every other dimension at the same size, and vanilla can't save a separate border per dimension. So the dimensions get extra ChunkyBorder limits:

| Dimension | Border | Why |
|---|---|---|
| Bumblezone | square ±250 around 0,0 | Bumblezone is 1:4 scale, so this matches the overworld's ±1000 |
| Otherside | square ±1000 around 0,0 | 1:1 scale; it also grows with the overworld border from 200 |

Server operators bypass ChunkyBorder (permission `chunkyborder.bypass.move`). Tested: a normal player who walks or teleports past ±250 in the Bumblezone or ±1000 in the Otherside is put back just inside.
