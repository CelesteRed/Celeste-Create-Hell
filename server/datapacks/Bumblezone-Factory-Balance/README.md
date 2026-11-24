# Bumblezone Factory Balance

A datapack for **The Bumblezone** (NeoForge 1.21.1) + **Create 6**. It makes the Bumblezone a destination you reach with a working Create factory, keeps sugar water from standing in for water, and stops the dimension's early loot from handing out endgame items.

Made for Celeste Create Hell; it doesn't depend on anything else from that pack.

## Getting in and out: the Bumble Portal Pearl
The Bumble Portal Pearl is the **only** way into and out of the Bumblezone. The Bumblezone's own Ender-Pearl-at-a-hive entry and its fall-out-of-the-world exit are switched off (see Install).

- **Make it** in a Create sequenced assembly that turns a beehive or bee nest into **2 pearls**: deploy an Ender Pearl → fill with 500 mB honey → deploy a Brass Ingot → press.
- **Enter:** throw a pearl so it hits a **bee nest or beehive in the Overworld**. You land in the Bumblezone at the same spot ÷ 4, on a safe floor.
- **Leave:** throw a pearl **anywhere in the Bumblezone**. You land on the Overworld surface at your position × 4, kept inside the world border.
- Each throw uses up one pearl, so a round trip costs both pearls from one hive. A pearl that lands anywhere else works as a normal Ender Pearl. If no safe landing spot is found, you get the pearl back.
- The pearl is an Ender Pearl with a name, lore, glint and a hidden `bumble_portal` tag. Renaming a normal pearl in an anvil doesn't make one.
- Lock or unlock travel (as an operator): `/function bumble_portal:lock` and `/function bumble_portal:unlock`. While locked, thrown pearls are returned. `/function bumble_portal:give` gives you 2 pearls.

## Sugar water is not water
| Tag | Before | After |
|---|---|---|
| `#minecraft:water` | Includes sugar water: it hydrates farmland and passes vanilla and Create water checks. Create pipes even make cobblestone or obsidian from it and lava. | Sugar water removed |
| `#create:fan_processing_catalysts/splashing` | Sugar water washes items in an encased fan | Removed |
| `#create:bottomless/allow` | Sugar water and honey count as bottomless sources for Create pumps | Removed |

These use NeoForge's tag `remove` list, so other mods' additions to the same tags stay. Create water wheels don't read any of these tags (they spin for any fluid except lava). The companion server mod **Celeste Server Fixes** stops sugar water from turning them.

Side effect: you can't fish in sugar water.

## Other changes
| What | Before | After |
|---|---|---|
| **Super Candle** | Crafting table: 1 string + 2 carvable wax | **Create heated compacting** (Blaze Burner): 2 carvable wax + 1 string + 250 mB honey |
| **Incense Candle** (from Super Candles) | Super Candle + Royal Jelly Bottle + up to 5 potions, effects up to level V | Super Candle + Royal Jelly Bottle + **Precision Mechanism** + up to **3** potions, effects up to level **II** |
| **Incense Candle** (from string + wax) | Crafting table, skipping the Super Candle | **Removed**, so every Incense Candle goes through the factory |
| Incense Candle **Resistance** | Up to Resistance V (immune to damage) | Up to Resistance II |
| **Bee Queen: Royal Jelly Bottle trade** | Could give a Nether Star, Totems, Enchanted Golden Apples, End Crystals, Dragon's Breath, Dragon Head, Beacon, Heart of the Sea, Budding Amethyst, Wither Roses | Golden Apples, Bottles o' Enchanting, Glow Ink, Name Tags, Turtle Eggs, Golden Carrots, an Ender Chest, 2 Sculk Catalysts (was 10) |
| **Bee Queen: Royal Jelly Bucket/Block trade** | Could give the Dragon Egg, Nether Stars, Totems, Enchanted Golden Apples, 24 End Crystals, Beacons, Dragon Heads, Dragon's Breath, Hearts of the Sea | Golden Apples, Bottles o' Enchanting, Golden Carrots, Glow Ink, Ender Chests, Name Tags |
| **Brushing suspicious blocks in Pollinated Fields** | 3.4% chance of an Enchanted Golden Apple per block | Golden Apple instead |

Not changed: Sempiternal Sanctum loot (diamonds, netherite scrap) and the Essence rewards. Those are behind the dimension's hardest challenges.

## Requirements
- The Bumblezone 7.x for NeoForge 1.21.1
- Create 6.x. Without Create, the pearl, Super Candle and Incense Candle recipes switch off.

## Install
1. Put the folder (or a zip of it) in `<world>/datapacks/` and run `/reload`.
2. In `config/the_bumblezone/dimension.toml`, set `enableEntranceTeleportation = false` and `enableExitTeleportation = false`, then restart. Otherwise the Bumblezone's own pearl-at-a-hive entry and fall-out exit keep working alongside the pearl.
3. Optional: install **Celeste Server Fixes** on the server so sugar water can't turn Create water wheels.

Leaving by pearl assumes the Overworld world border is centred on 0,0.
