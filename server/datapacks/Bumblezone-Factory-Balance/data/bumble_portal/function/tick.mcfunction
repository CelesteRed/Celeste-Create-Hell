# Spot newly thrown Bumble Portal Pearls (ender pearls carrying custom_data {bumble_portal:1b}).
execute as @e[type=minecraft:ender_pearl,tag=!bumble_portal.checked] run function bumble_portal:classify
# A player whose pearl existed last tick but is gone now has just landed (vanilla already moved them to the impact point).
scoreboard players set @a bumble_portal.now 0
execute as @e[type=minecraft:ender_pearl,tag=bumble_portal.pearl] on origin run scoreboard players set @s bumble_portal.now 1
execute as @a[scores={bumble_portal.was=1,bumble_portal.now=0}] at @s run function bumble_portal:landed
execute as @a run scoreboard players operation @s bumble_portal.was = @s bumble_portal.now
