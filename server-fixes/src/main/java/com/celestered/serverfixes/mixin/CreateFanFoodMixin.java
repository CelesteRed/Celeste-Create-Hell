package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.ClassRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Food smoked or blasted by an encased fan is "Factory-made". */
@Mixin(targets = {
        "com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes$SmokingType",
        "com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes$BlastingType"}, remap = false)
public abstract class CreateFanFoodMixin {
    @Inject(method = "process", at = @At("RETURN"))
    private void celeste$factoryFood(ItemStack stack, Level level, CallbackInfoReturnable<List<ItemStack>> cir) {
        ClassRules.stampAll(cir.getReturnValue(), "machine");
    }
}
