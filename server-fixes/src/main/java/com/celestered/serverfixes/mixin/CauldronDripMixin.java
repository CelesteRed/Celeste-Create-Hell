package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.WaterRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** An empty cauldron filled by dripstone uses up the source above the dripstone. */
@Mixin(value = CauldronBlock.class, remap = false)
public abstract class CauldronDripMixin {
    @Inject(method = "receiveStalactiteDrip", at = @At("HEAD"))
    private void celeste$consumeSource(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci) {
        WaterRules.consumeDripSource(level, pos, fluid);
    }
}
