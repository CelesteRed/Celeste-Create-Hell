execute if data storage bumble_portal:state {locked:1b} run return run function bumble_portal:locked
execute if dimension minecraft:overworld if predicate bumble_portal:near_hive run return run function bumble_portal:enter
execute if dimension the_bumblezone:the_bumblezone run return run function bumble_portal:exit
title @s actionbar {"text":"The Bumble Portal Pearl fizzles. Throw it at a bee nest or beehive in the Overworld.","color":"yellow"}
