package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.WaterRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Waterlogged blocks (and kelp, seagrass, bubble columns) never act as water for fan washing. */
@Mixin(targets = "com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes$SplashingType", remap = false)
public abstract class FanSplashingNoWaterloggedMixin {
    @Inject(method = "isValidAt", at = @At("HEAD"), cancellable = true)
    private void celeste$onlyRealWater(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Boolean override = WaterRules.splashingOverride(level, pos);
        if (override != null) cir.setReturnValue(override);
    }
}
