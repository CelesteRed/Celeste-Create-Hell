package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.ClassRules;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Furnaces, smokers, blast furnaces and brewing stands belong to whoever last opened them. */
@Mixin(value = BaseContainerBlockEntity.class, remap = false)
public abstract class ContainerOwnerMixin {
    @Inject(method = "createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;", at = @At("HEAD"))
    private void celeste$rememberOwner(int id, Inventory inv, Player player, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        Object self = this;
        if ((self instanceof AbstractFurnaceBlockEntity || self instanceof BrewingStandBlockEntity) && player != null && !player.level().isClientSide) {
            ClassRules.setOwner((BaseContainerBlockEntity) self, player);
        }
    }
}
