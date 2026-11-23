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
