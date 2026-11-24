package com.celestered.serverfixes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create water wheels spin for any flowing fluid except lava. Fluids in {@code #celeste:not_water}
 * (Bumblezone sugar water by default) must never stand in for water, so they give no flow here.
 * Priority 100 puts this ahead of Flowing Fluids' own HEAD hook on the same method.
 */
@Mixin(targets = "com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity", priority = 100, remap = false)
public abstract class WaterWheelNoFakeWaterMixin {
    private static final TagKey<Fluid> CELESTE$NOT_WATER =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("celeste", "not_water"));

    @Inject(method = "getFlowVectorAtPosition", at = @At("HEAD"), cancellable = true, require = 1)
    private void celeste$noFlowFromFakeWater(BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level != null && level.getFluidState(pos).is(CELESTE$NOT_WATER)) {
            cir.setReturnValue(Vec3.ZERO);
        }
    }

    /** Flowing Fluids (REQUIRE_FLOW mode) remembers recent flows; don't let fake water register one. */
    @Dynamic("Added to WaterWheelBlockEntity by Flowing Fluids' MixinWaterWheel")
    @Inject(method = "ff$acceptRecentFlow", at = @At("HEAD"), cancellable = true, require = 0)
    private void celeste$ignoreFakeWaterFlow(BlockPos pos, Direction dir, Fluid fluid, boolean flag, CallbackInfo ci) {
        if (fluid != null && fluid.is(CELESTE$NOT_WATER)) {
            ci.cancel();
        }
    }
}
