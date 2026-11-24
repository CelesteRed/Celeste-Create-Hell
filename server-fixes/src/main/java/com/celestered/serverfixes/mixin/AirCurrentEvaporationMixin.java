package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.WaterRules;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Water in a running fan's air current slowly evaporates. */
@Mixin(targets = "com.simibubi.create.content.kinetics.fan.AirCurrent", remap = false)
public abstract class AirCurrentEvaporationMixin {
    @Shadow @Final public IAirCurrentSource source;
    @Shadow public Direction direction;
    @Shadow public float maxDistance;

    @Inject(method = "tick", at = @At("HEAD"))
    private void celeste$evaporate(CallbackInfo ci) {
        if (source == null || direction == null) return;
        Level level = source.getAirCurrentWorld();
        if (level == null || level.isClientSide || source.isSourceRemoved()) return;
        BlockPos origin = source.getAirCurrentPos();
        if (origin == null || source.getSpeed() == 0 || maxDistance <= 0) return;
        if ((level.getGameTime() + (origin.asLong() & 0xF)) % 20 != 0) return;
        WaterRules.exposeToFan(level, origin, direction, (int) Math.ceil(maxDistance));
    }
}
