package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.ThinAirRules;
import com.celestered.serverfixes.WeatherRules;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, remap = false)
public abstract class PlayerWeatherMixin {
    @Unique private int celeste$exposure;
    @Unique private boolean celeste$thinAir;

    @Inject(method = "tick", at = @At("TAIL"))
    private void celeste$weather(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.tickCount % 10 == 0) celeste$exposure = WeatherRules.exposure(self);
        if (celeste$exposure > 0 && self.isAlive()) WeatherRules.freezeTick(self, celeste$exposure);
        if (self.tickCount % 10 == 0) celeste$thinAir = ThinAirRules.inThinAir(self);
        if (celeste$thinAir && self.isAlive()) ThinAirRules.tick(self);
    }
}
