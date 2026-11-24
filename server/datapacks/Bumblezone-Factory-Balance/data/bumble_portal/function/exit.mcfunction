# Bumblezone -> Overworld: x4 scale, kept inside the Overworld world border (assumed centred on 0,0).
execute store result score #x bumble_portal.tmp run data get entity @s Pos[0] 4
execute store result score #z bumble_portal.tmp run data get entity @s Pos[2] 4
execute in minecraft:overworld store result score #half bumble_portal.tmp run worldborder get
scoreboard players operation #half bumble_portal.tmp /= #2 bumble_portal.tmp
scoreboard players remove #half bumble_portal.tmp 16
scoreboard players operation #neg bumble_portal.tmp = #half bumble_portal.tmp
scoreboard players operation #neg bumble_portal.tmp *= #-1 bumble_portal.tmp
scoreboard players operation #x bumble_portal.tmp < #half bumble_portal.tmp
scoreboard players operation #x bumble_portal.tmp > #neg bumble_portal.tmp
scoreboard players operation #z bumble_portal.tmp < #half bumble_portal.tmp
scoreboard players operation #z bumble_portal.tmp > #neg bumble_portal.tmp
execute store result storage bumble_portal:tp x int 1 run scoreboard players get #x bumble_portal.tmp
execute store result storage bumble_portal:tp z int 1 run scoreboard players get #z bumble_portal.tmp
function bumble_portal:exit_at with storage bumble_portal:tp
execute at @s if dimension the_bumblezone:the_bumblezone run return run function bumble_portal:failed
function bumble_portal:arrived
