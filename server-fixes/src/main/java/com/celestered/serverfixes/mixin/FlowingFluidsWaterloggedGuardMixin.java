package com.celestered.serverfixes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Some modded blocks (e.g. Alex's Caves sulfur buds) implement SimpleWaterloggedBlock without having the
 * "waterlogged" property. Flowing Fluids then calls pickupBlock on them and vanilla throws, crashing the server.
 * Skip those blocks: there's no water in them to remove.
 */
@Mixin(targets = "traben.flowing_fluids.FFFluidUtils", remap = false)
public abstract class FlowingFluidsWaterloggedGuardMixin {
    @Inject(method = "removeAllFluidAtPos", at = @At("HEAD"), cancellable = true, require = 0)
    private static void celeste$skipBrokenWaterloggables(LevelAccessor level, BlockPos pos, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof SimpleWaterloggedBlock && !state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            cir.setReturnValue(false);
        }
    }
}
