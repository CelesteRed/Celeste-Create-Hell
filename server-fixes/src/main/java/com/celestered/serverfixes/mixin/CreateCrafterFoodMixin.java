package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.ClassRules;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Food from a Mechanical Crafter grid is "Factory-made". */
@Mixin(targets = "com.simibubi.create.content.kinetics.crafter.RecipeGridHandler", remap = false)
public abstract class CreateCrafterFoodMixin {
    @Inject(method = "tryToApplyRecipe", at = @At("RETURN"))
    private static void celeste$factoryFood(CallbackInfoReturnable<ItemStack> cir) {
        ClassRules.stampFood(cir.getReturnValue(), "machine");
    }
}
