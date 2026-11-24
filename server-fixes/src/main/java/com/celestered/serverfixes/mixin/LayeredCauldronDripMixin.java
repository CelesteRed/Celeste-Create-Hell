package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.WaterRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A partly filled water cauldron topped up by dripstone uses up the source above the dripstone. */
@Mixin(value = LayeredCauldronBlock.class, remap = false)
public abstract class LayeredCauldronDripMixin {
    @Inject(method = "receiveStalactiteDrip", at = @At("HEAD"))
    private void celeste$consumeSource(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci) {
        if (state.hasProperty(LayeredCauldronBlock.LEVEL) && state.getValue(LayeredCauldronBlock.LEVEL) < 3) {
            WaterRules.consumeDripSource(level, pos, fluid);
        }
    }
}
