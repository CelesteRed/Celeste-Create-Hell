package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.ClassRules;
import com.pockyl.handson_delight.station.KitchenPot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Meals a Cook serves out of a Hands-On cooking pot are "Cooked by a Cook". */
@Mixin(targets = "com.pockyl.handson_delight.station.PotServing", remap = false)
public abstract class HandsOnCookServeMixin {
    @Shadow @Final private static ThreadLocal<ItemStack> MEAL_BEFORE;

    @Inject(method = "after", at = @At("HEAD"), require = 0)
    private static void celeste$stampServed(Level level, BlockPos pos, Player player, CallbackInfo ci) {
        if (level == null || level.isClientSide || player == null) return;
        if (!ClassRules.COOK.equals(ClassRules.classOf(player))) return;
        ItemStack before = MEAL_BEFORE.get();
        if (before == null || before.isEmpty()) return;
        KitchenPot pot = KitchenPot.of(level.getBlockEntity(pos));
        if (pot == null) return;
        ItemStack now = pot.handson$meal();
        int served = before.getCount() - (ItemStack.isSameItem(before, now) ? now.getCount() : 0);
        if (served <= 0) return;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && served > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !ItemStack.isSameItem(s, before) || ClassRules.isStamped(s)) continue;
            int n = Math.min(served, s.getCount());
            ItemStack stamped = ClassRules.stampFood(s.split(n), ClassRules.COOK);
            served -= n;
            if (!inv.add(stamped)) player.drop(stamped, false);
        }
    }
}
