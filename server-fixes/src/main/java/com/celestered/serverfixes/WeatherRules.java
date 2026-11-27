package com.celestered.serverfixes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Snowfall and blizzards are dangerous outdoors. A player under open sky in falling snow freezes
 * (vanilla freeze overlay, then freeze damage) unless they wear a full set of warm armor
 * (#toughasnails:heating_armor). Thunder-blizzards freeze twice as fast. Leather armor does not help.
 * Shelter (no sky above) stops it.
 */
public final class WeatherRules {
    public static final TagKey<Item> WARM_ARMOR = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("toughasnails", "heating_armor"));
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private WeatherRules() {}

    /** 0 = safe, 1 = snowing on the player, 2 = blizzard (snow + thunder). Checked every 10 ticks. */
    public static int exposure(ServerPlayer p) {
        if (p.isCreative() || p.isSpectator()) return 0;
        Level level = p.level();
        if (!(level instanceof ServerLevel) || level.dimension() != Level.OVERWORLD || !level.isRaining()) return 0;
        BlockPos head = BlockPos.containing(p.getEyePosition());
        if (!level.canSeeSky(head)) return 0;
        if (level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, head).getY() > head.getY()) return 0;
        Biome biome = level.getBiome(head).value();
        if (biome.getPrecipitationAt(head) != Biome.Precipitation.SNOW) return 0;
        if (wearsWarmSet(p)) return 0;
        return level.isThundering() ? 2 : 1;
    }

    public static boolean wearsWarmSet(ServerPlayer p) {
        for (EquipmentSlot s : ARMOR) if (!p.getItemBySlot(s).is(WARM_ARMOR)) return false;
        return true;
    }

    /** Runs every tick for exposed players. Vanilla thaws 2 ticks per tick, so we add on top of that. */
    public static void freezeTick(ServerPlayer p, int exposure) {
        int need = p.getTicksRequiredToFreeze();
        int add = 2 + exposure; // net +1/tick in snow (frozen in ~7s), +2/tick in a blizzard
        p.setTicksFrozen(Math.min(need + 100, p.getTicksFrozen() + add));
        // Vanilla already deals 1 freeze damage every 2 s once fully frozen (unless wearing leather).
        // We add 1 more in a blizzard, and cover the leather case so only wool counts.
        if (p.getTicksFrozen() >= need && p.tickCount % 40 == 20) {
            boolean vanillaSkips = !p.canFreeze();
            float extra = (exposure == 2 ? 1.0F : 0.0F) + (vanillaSkips ? 1.0F : 0.0F);
            if (extra > 0) p.hurt(p.damageSources().freeze(), extra);
        }
        if (p.tickCount % 200 == 0) {
            p.displayClientMessage(Component.literal(exposure == 2
                    ? "The blizzard is freezing you. Get to shelter or wear a full set of wool armor."
                    : "The snow is freezing you. Get to shelter or wear a full set of wool armor."), true);
        }
    }
}
