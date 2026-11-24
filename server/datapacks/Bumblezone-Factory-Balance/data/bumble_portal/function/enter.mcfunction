# Overworld -> Bumblezone keeps the Bumblezone's 1:4 scale.
execute store result storage bumble_portal:tp x int 0.25 run data get entity @s Pos[0]
execute store result storage bumble_portal:tp z int 0.25 run data get entity @s Pos[2]
function bumble_portal:enter_at with storage bumble_portal:tp
execute at @s if dimension minecraft:overworld run function bumble_portal:enter_wide with storage bumble_portal:tp
execute at @s if dimension minecraft:overworld run return run function bumble_portal:failed
function bumble_portal:arrived
