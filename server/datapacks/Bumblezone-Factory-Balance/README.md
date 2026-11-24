# Bumblezone Factory Balance

A datapack for **The Bumblezone** (NeoForge 1.21.1) + **Create 6**. It makes the Bumblezone a mid-game destination you reach with a working Create factory, and stops its early loot from handing out endgame items.

Made for Celeste Create Hell; it doesn't depend on anything else from that pack.

## Changes

| What | Before | After |
|---|---|---|
| **Entering the Bumblezone** | Throw an Ender Pearl at any bee nest or beehive | The nest/hive must sit on a **Brass Casing**. Players who try without one get a message saying which block is needed. |
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
- Create 6.x. Without Create, the Super Candle and Incense Candle recipes switch off and the Brass Casing requirement is dropped.

## Install
Put the folder (or a zip of it) in `<world>/datapacks/`, then run `/reload`. Changing the entry block takes effect immediately. To use a different block, edit `data/the_bumblezone/tags/block/dimension_teleportation/required_blocks_under_beehive_to_teleport.json`.
