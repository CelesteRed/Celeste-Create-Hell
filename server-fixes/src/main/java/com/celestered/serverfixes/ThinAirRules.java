package com.celestered.serverfixes;

import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

/**
 * Thin air (our own take on the Thin Air mod, which has no 1.21.1 build): below Y 0 in the Overworld,
 * everywhere in the Nether and in Deeper and Darker's Otherside there is no breathable air.
 * Players need a Create diving helmet and a backtank with air, which drains 1 air per second.
 * Without them they run out of breath like underwater (about 15 s), then take drowning damage.
 * The Bumblezone is fine.
 */
public final class ThinAirRules {
    private static final Set<ResourceLocation> NO_AIR_DIMENSIONS = Set.of(
            ResourceLocation.withDefaultNamespace("the_nether"),
            ResourceLocation.fromNamespaceAndPath("deeperdarker", "otherside"));
    private static final int OVERWORLD_MIN_Y = 0;

    private ThinAirRules() {}

    public static boolean inThinAir(ServerPlayer p) {
        if (p.isCreative() || p.isSpectator()) return false;
        Level level = p.level();
        if (NO_AIR_DIMENSIONS.contains(level.dimension().location())) return true;
        return level.dimension() == Level.OVERWORLD && p.getEyeY() < OVERWORLD_MIN_Y;
    }

    /** Called every tick for players in thin air. */
    public static void tick(ServerPlayer p) {
        if (p.isUnderWater()) return; // vanilla/Create underwater breathing already applies
        if (DivingHelmetItem.isWornBy(p)) {
            List<ItemStack> tanks = BacktankUtil.getAllWithAir(p);
            if (!tanks.isEmpty()) {
                if (p.tickCount % 20 == 0) BacktankUtil.consumeAir(p, tanks.get(0), 1);
                p.setAirSupply(Math.min(p.getMaxAirSupply(), p.getAirSupply() + 4));
                return;
            }
        }
        // Vanilla refills 4 air per tick out of water; take 5 so it drains 1 per tick, like underwater.
        int air = p.getAirSupply() - 5;
        if (air <= -20) {
            air = 0;
            p.hurt(p.damageSources().drown(), 2.0F);
        }
        p.setAirSupply(air);
        if (p.tickCount % 200 == 0) {
            p.displayClientMessage(Component.literal("The air here is too thin to breathe. Wear a Create diving helmet and a filled backtank."), true);
        }
    }
}
