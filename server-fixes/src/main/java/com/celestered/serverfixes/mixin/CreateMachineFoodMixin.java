package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.ClassRules;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Food from Create processing (basins, presses, deployers, spouts, sequenced assembly...) is "Factory-made". */
@Mixin(targets = "com.simibubi.create.content.processing.recipe.ProcessingRecipe", remap = false)
public abstract class CreateMachineFoodMixin {
    @Inject(method = "rollResults(Ljava/util/List;Lnet/minecraft/util/RandomSource;)Ljava/util/List;", at = @At("RETURN"))
    private void celeste$factoryFood(List<?> outputs, RandomSource random, CallbackInfoReturnable<List<ItemStack>> cir) {
        ClassRules.stampAll(cir.getReturnValue(), "machine");
    }
}
