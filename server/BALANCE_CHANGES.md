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
| Entering and leaving | Ender Pearl at any bee nest/hive; fall out of the world to leave | Only with a **Bumble Portal Pearl** (see below) |
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

## 9. Bumble Portal Pearl and "sugar water is not water" (pack 1.0.11, released as 8.0.0)

### Getting in and out of the Bumblezone
| What | Before (1.0.9) | After |
|---|---|---|
| Entering | Ender Pearl at a hive sitting on Brass Casing | **Bumble Portal Pearl** thrown at any Overworld bee nest or beehive. Normal pearls do nothing special. |
| Leaving | Fall out of the bottom or top of the dimension | **Bumble Portal Pearl** thrown anywhere in the Bumblezone. You land on the Overworld surface at ×4, inside the world border. |
| Bumble Portal Pearl recipe | — | Create sequenced assembly: beehive or bee nest → deploy Ender Pearl → fill 500 mB honey → deploy Brass Ingot → press = **2 pearls** (one round trip) |
| `config/the_bumblezone/dimension.toml` `enableEntranceTeleportation` | true | **false** |
| `config/the_bumblezone/dimension.toml` `enableExitTeleportation` | true | **false** |

How it works: the datapack watches for thrown ender pearls carrying `custom_data {bumble_portal:1b}`. When one lands, vanilla has already moved the thrower to the impact point. The datapack then checks for a hive within 1 block (Overworld) or that the thrower is in the Bumblezone, and moves them with `spreadplayers`, which only picks safe floors. If no safe spot is found, the pearl is returned.

The Brass Casing requirement from 1.0.9 is gone; brass is now part of the pearl recipe instead.

Tested on the local server:
- A normal pearl on a hive does nothing.
- A Bumble pearl on a beehive or bee nest enters.
- A Bumble pearl thrown in the Bumblezone exits at ×4.
- With a 200-wide Overworld border, exiting from Bumblezone x=200 landed at x=81 (clamped inside the border).
- A Bumble pearl on plain ground is just a normal pearl.
- While locked, the pearl is returned.

### Sugar water is never water
| Where | Before | After |
|---|---|---|
| `#minecraft:water` | Included sugar water (Bumblezone adds it): it hydrated farmland, counted in Create recipes that take water, and made cobblestone or obsidian with lava in Create pipes | Removed (NeoForge tag `remove`, in `Bumblezone-Factory-Balance`) |
| `#create:fan_processing_catalysts/splashing` | Sugar water washed items in fans | Removed |
| `#create:bottomless/allow` | Sugar water and honey listed | Removed (Create's `DENY_ALL` already ignored it) |
| Create water wheels | Spin for any flowing fluid except lava, including sugar water | Sugar water gives no flow. New server-only mod **Celeste Server Fixes** (`server-fixes/`): a mixin on `WaterWheelBlockEntity.getFlowVectorAtPosition`, plus Flowing Fluids' flow memory, for fluids in `#celeste:not_water` |

Tested in-game:
- `#minecraft:water` and the fan-splashing tag now match water but not sugar water.
- With Flowing Fluids switched off for the test, a wheel next to flowing water spun at 8 RPM while the same wheel next to flowing sugar water stayed at 0. Flowing Fluids was turned back on and its config is unchanged.

Side effect: you can't fish in sugar water.

## 10. Fan washing uses up water; no waterlogged catalysts; dripstone and faucets (Celeste Server Fixes 1.1.0)
All of this is in the server-only mod **Celeste Server Fixes** (`server-fixes/`); players don't need it. Settings are in `config/celeste_server_fixes.json` (restart to apply).

| What | Before | After |
|---|---|---|
| Waterlogged blocks as fan washing catalysts | A waterlogged block (slab, iron bars, trapdoor...), kelp, seagrass or a bubble column in a fan current washed items forever without using water | **Doesn't count.** Only a real water block washes. Other fluids held inside blocks don't count either. |
| Water in a running fan's air current | Never used up | **Evaporates.** `fanWaterLifetimeSeconds` = **300**: a full water block in the current loses 1/8 every 37.5 s and is gone after 5 minutes of the fan blowing on it. A pool loses water at each block the current passes through, and Flowing Fluids levels the rest out. |
| Create Connected *Fan Splashing Catalyst* | Filled once (1 bucket), washed forever | After 300 s in a running current it turns back into an **Empty Fan Catalyst**; refill it with a bucket or a spout. |
| Dreams & Desires *Splashing Sail* | Crafted once (1 water bucket per sail), washed forever | After 300 s in a running current it turns back into a **Sail Frame**. |
| Dripstone filling a cauldron | Never used up the water or lava above it: infinite water and lava | **Uses up the source.** Water: each drip has a 1 in 3 chance to remove the source (on average one source per full cauldron, the same as a bucket). Lava: every fill removes the source. `dripstoneConsumesSource` turns this off. |

How long washing takes for reference: this pack's Create config has `fanProcessingTime = 1500` (75 s per item group), so one water block washes about 4 groups before it's gone.

Faucets: Supplementaries' Faucet and Let's Do Bakery's kitchen-sink faucet (both could make infinite water) were in the old Fabric pack. **Neither mod is in this pack.** I also searched every server and pack mod for faucet, tap, sink or spigot blocks and found none.

Tested on the local server:
- Fan with a water block in front: the water dropped one level per step and was gone at the configured time.
- A blaze downstream of real water took splashing damage and died in 6 s. A blaze downstream of waterlogged iron bars took none.
- Dripstone under an enclosed water source filled a cauldron, and the source was used up within the first few drips (random 1-in-3).

## 11. Removed Figura, Cosmetica and Emotecraft (8.0.0)
These are being replaced by our own mod, Cosmiq.

| Removed | Where it was | Also removed |
|---|---|---|
| Figura | client only | — |
| Cosmetica | client only | `config/cosmetica/` |
| Emotecraft | client + server | Emote Tweaks (needs Emotecraft), the `emotes/` folder (31 emotes), `config/emotecraft*.json`, the stale `config/pop_up_emotes-*.toml` |

Players' installs drop these on their next launch, because unsup removes files that left the pack. The server jars are in `/home/celeste/backups/removed-emotecraft-2026-11-24_0309/`; the server stops loading them at its next restart.

## 12. Tough As Nails temperatures in the new dimensions
Tough As Nails sets each biome's temperature from its tags. A biome with no tag falls back to the biome's base temperature, and both new dimensions fell back badly:

| Dimension | Base temperature | Before | After (`ZZ-CCH-Reforged-Balance`) |
|---|---|---|---|
| Bumblezone (all 9 biomes, `#the_bumblezone:the_bumblezone`) | 1.85 | **Hot**: players overheat | **Warm** (`toughasnails:warm_biomes`): a warm hive, not dangerous alone |
| Otherside (Overcast Columns 0.6, Echoing Forest 0.3, Deeplands −0.5, Blooming Caverns −0.3) | mostly below 0.15 | **Icy** in Deeplands and Blooming Caverns: players freeze | **Cold** (`toughasnails:cold_biomes`), the same as vanilla's Deep Dark |

Checked on the local server with `execute if biome`: Bumblezone biomes are warm and not hot, and Otherside biomes are cold.

## 13. Origins (ported from the Fabric Celeste Create Hell)
The old Fabric pack ran Apace's Origins 1.10 (1.20.1) with six add-ons. Apace's Origins has no NeoForge 1.21.1 build, and the unofficial "Origins (Forge)" port stopped before 1.21. So the port uses **Overgrown's Origins** + **Overgrown's Apoli**. It's a NeoForge 1.21.1 reimplementation that reads the same `origins:` data-pack format, so old origin packs load unchanged.

| Old add-on (Fabric 1.20.1) | Status in 8.0.0 | How |
|---|---|---|
| Origins (base) | ✅ Ported | Overgrown's Origins. The old roster was Arachnid, Avian, Blazeborn, Elytrian, Feline, Merling and Shulk, with Human, Enderian and Phantom turned off (`ZZ-CCH-Origins` keeps it that way) |
| Origins: Classes (14 classes) | ✅ Ported | Overgrown's Origins: Classes: the same 14 classes with the same ids (`origins-classes:*`), as a second layer |
| Mob Origins (11 mob origins) | ⚠️ Partly | The mod is Fabric 1.20.x only with ~30 Java power types, so it can't be ported as-is. Overgrown's ships **Buzzborne** (bee) and **Slimekin** (slime), which cover Mob Origins' Bee and Slime |
| Origins: Rockeater | ❌ Removed (14) | Ported at first, then removed on request; Pehkui went with it |
| Origins: Dietary Delights | ✅ Ported | Its 1.21.x data pack (CC0), in `datapacks/`: modded foods count for carnivore/vegetarian diets |
| Create: Origins | ✅ Ported (data part) | `ZZ-CCH-Origins`: Merling's swim bonus is off while wearing diving boots; only **full netherite** diving gear protects Blazeborn and Enderian from water |
| Origins: Sapling | ❌ Not included | The 1.20 data pack loads, but 5 of its powers need the Apugli add-on (regeneration field, bee immunity, sounds) and its block/entity tags and function use pre-1.21 folder names. It's All Rights Reserved, so we can't ship a fixed copy |

Power tweaks carried over from the old `origins_server.json` (in `ZZ-CCH-Origins`):

| Origin | Removed power |
|---|---|
| Feline | Nine Lives |
| Shulk | Shulker Inventory, Strong Arms, Strong Arms break speed |
| Arachnid | Carnivore, Fragile |
| Blazeborn | Nether Spawn |

Tested on the local server:
- 29 origins and 2 layers loaded (12 base origins, 14 classes, Rockeater, plus the inactive Sapling test); the live server loads 28 without Sapling.
- `/origin set` worked on both layers, and Feline has no Nine Lives while Fall Immunity still applies.
- Rockeater shrinks the player to half height through Pehkui, and switching to another origin resets the size.
- Shulk has no Shulker Inventory and keeps Natural Armor.

Choosing an origin: Overgrown's Origins opens the origin and class screens for players who have none yet (not checked by eye in the test). Operators can use `/origin gui <player>` to reopen them, or `/origin set <player> <layer> <origin>`.

## 14. Classes come with defects: specialize and trade
Every class is still good at its own trade, but now it's **bad at everyone else's**. Each class carries the "outsider" defect of the other 12 classes, so a Miner chops wood badly, a Farmer mines slowly, and so on. Players have to specialize and trade with other classes to get things done. The defects appear in each class's power list on the class selection screen.

| Trade | Defect for everyone who is **not** this class | Power |
|---|---|---|
| Miner | **Unpracticed Miner**: breaks pickaxe blocks (stone, ores…) 50% slower | `cch:outsider/miner` |
| Lumberjack | **Clumsy with an Axe**: chops logs 50% slower; a log crafts into 2 planks (normally 4) | `cch:outsider/lumberjack` |
| Farmer | **Black Thumb**: bone meal does nothing on crops; wheat, carrots, potatoes and beetroots never drop bonus items (no Fortune, no random extras) | `cch:outsider/farmer` |
| Rancher | **No Way with Livestock**: can't breed cows, mooshrooms, sheep, pigs, chickens, rabbits or goats | `cch:outsider/rancher` |
| Beastmaster | **Wild Things Distrust You**: can't tame or break in wolves, cats, parrots, horses, donkeys, mules or llamas | `cch:outsider/beastmaster` |
| Blacksmith | **Amateur Smith**: anvil repairs need 50% more material; combining items repairs less | `cch:outsider/blacksmith` |
| Cook | **Bland Cooking**: food fills you up 30% less (saturation) | `cch:outsider/cook` |
| Cleric | **Untrained Alchemist**: can't use brewing stands; enchanting tables roll weaker (−5 enchantability) | `cch:outsider/cleric` |
| Merchant | **Poor Haggler**: Wandering Traders won't trade with you | `cch:outsider/merchant` |
| Archer | **Shaky Aim**: projectiles spread three times as much | `cch:outsider/archer` |
| Warrior | **Untrained Fighter**: 15% less melee damage | `cch:outsider/warrior` |
| Explorer | **Out of Shape**: sprinting makes you 50% hungrier | `cch:outsider/explorer` |
| Rogue | **Conspicuous**: mobs notice you from 25% farther away | `cch:outsider/rogue` |

Also changed:
- **Nitwit is gone** from the class list. It had no perks, so with these defects it would only be a trap.
- **Villager trading is untouched.** Only Wandering Traders are Merchant-only.
- **Create machines aren't affected.** Drills, saws, harvesters and deployers don't have a class, so automation works the same for everyone. Building a factory is the shared way around the defects.

Tuning lives in `datapacks/ZZ-CCH-Origins/data/cch/powers/outsider/*.json`. Change a number and run `/reload`.

Tested on the local server:
- All 234 powers and 27 origins loaded with no errors.
- On a test player, a Miner gets every outsider defect except the Miner one, and a Farmer has the Miner defect but not the Farmer one.
- Attack damage drops by 15% for non-Warriors.

## 15. Classes, round 2: 8 classes, food quality, amateur potions
Removed classes: **Beastmaster, Warrior, Explorer, Rogue, Merchant**, along with their perks and defects. Everyone can tame and ride animals again, deals normal melee damage, sprints normally, and can trade with Wandering Traders.

**Classes now:** Blacksmith, Cleric, Cook, Farmer, Rancher, Miner, Archer, Lumberjack. Each carries the defects of the other seven.

### Cook: food quality and ownership
Every food item carries a quality, and it applies to **whoever eats it** (the `cch:food_quality` power, which every class has):

| Food | Hunger and saturation when eaten | How it's marked |
|---|---|---|
| **Cooked by a Cook** | 100% | Gold "Cooked by a Cook" lore line |
| **Factory-made** (Create) | 85% | Gray "Factory-made" lore line |
| Everything else (made by a non-Cook, raw, found) | 70% | none |

How food becomes "Cooked by a Cook":
- **Crafting**: a Cook crafts it (crafting grid).
- **Furnace / Smoker / Blast Furnace**: the station belongs to the **last player who opened it**. If that's a Cook, everything it smelts is Cook food. That covers a Cook loading it by hand, and also a hopper-fed station that a Cook opened last.
- **Campfire**: the **last player who put food on it** decides.

"Factory-made" food comes from Create: basin mixing and compacting, presses, deployers, spouts, sequenced assembly (all Create processing recipes), encased-fan smoking and blasting, and Mechanical Crafters.

Marked food only stacks with food that has the same mark. The mark is stored in `custom_data.cch_food` (`cook` / `machine`).

### Cleric: amateur potions
Everyone can use brewing stands. A brewing stand belongs to the **last player who opened it**. If that's not a Cleric, the potions it brews come out **Amateur**: every effect at level I, half the normal duration, named "Potion of … (Amateur)". They have no base potion, so they can't be upgraded with glowstone or redstone. Clerics brew normal potions, and their own perk still extends them. Non-Clerics also keep the −5 enchantability defect.

### Archer
Non-Archers: projectiles spread 3× as much (as before), plus every bow shot costs 2 extra durability.

All of this is server-side: the food marks, potion weakening and station ownership are in **Celeste Server Fixes 1.2.0**, and the class powers are in `ZZ-CCH-Origins`.

Tested on the local server:
- A Cook-owned furnace gave "Cooked by a Cook" cooked beef; a furnace with no Cook owner gave plain cooked beef.
- A Cook-loaded campfire dropped stamped cooked chicken.
- Fan-smoked beef on a depot came out "Factory-made".
- A non-Cleric brewing stand turned awkward potion + sugar into "Potion of Swiftness (Amateur)" (Speed I, 1:30 instead of 3:00). A Cleric's stand made a normal Potion of Swiftness.
