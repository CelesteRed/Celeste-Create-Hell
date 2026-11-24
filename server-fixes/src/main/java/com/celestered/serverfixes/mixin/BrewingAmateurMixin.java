package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.ClassRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Potions brewed in a stand that a non-Cleric last opened come out Amateur (level I, half duration). */
@Mixin(value = BrewingStandBlockEntity.class, remap = false)
public abstract class BrewingAmateurMixin {
    @Inject(method = "serverTick", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;doBrew(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/NonNullList;)V"))
    private static void celeste$amateurPotions(Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity stand, CallbackInfo ci) {
        if (ClassRules.CLERIC.equals(ClassRules.ownerOf(stand))) return;
        for (int i = 0; i < 3; i++) ClassRules.weakenPotion(stand.getItem(i));
        stand.setChanged();
    }
}
