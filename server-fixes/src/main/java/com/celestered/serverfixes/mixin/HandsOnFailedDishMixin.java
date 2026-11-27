package com.celestered.serverfixes.mixin;

import com.pockyl.handson_delight.station.KitchenPot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hands-On Delight: a cook that earned zero stars (bad stirring) ruins the dish. The ingredients are used up
 * but no meal comes out, instead of the usual single serving.
 */
@Mixin(targets = "com.pockyl.handson_delight.station.PotCooking", remap = false)
public abstract class HandsOnFailedDishMixin {
    @Shadow @Final private KitchenPot pot;
    @Shadow private int grade;
    @Unique private int celeste$mealCountBefore;

    @Inject(method = "serveTheBatch", at = @At("HEAD"), require = 0)
    private void celeste$remember(CallbackInfo ci) {
        ItemStack meal = pot.handson$meal();
        celeste$mealCountBefore = meal.isEmpty() ? 0 : meal.getCount();
    }

    @Inject(method = "serveTheBatch", at = @At("RETURN"), require = 0)
    private void celeste$ruinIfNoStars(CallbackInfo ci) {
        if (grade > 0) return;
        ItemStack meal = pot.handson$meal();
        int made = meal.getCount() - celeste$mealCountBefore;
        if (made > 0) meal.shrink(made);
    }
}
