package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.ClassRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Food cooked on a campfire last loaded by a Cook is "Cooked by a Cook". */
@Mixin(value = CampfireBlockEntity.class, remap = false)
public abstract class CampfireCookStampMixin {
    @Inject(method = "placeFood", at = @At("HEAD"))
    private void celeste$rememberCook(LivingEntity entity, ItemStack stack, int cookTime, CallbackInfoReturnable<Boolean> cir) {
        if (entity != null && !entity.level().isClientSide) ClassRules.setOwner((CampfireBlockEntity) (Object) this, entity);
    }

    @Redirect(method = "cookTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"))
    private static void celeste$stampDrop(Level level, double x, double y, double z, ItemStack stack,
                                          Level l2, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        if (campfire != null && ClassRules.COOK.equals(ClassRules.ownerOf(campfire))) ClassRules.stampFood(stack, ClassRules.COOK);
        Containers.dropItemStack(level, x, y, z, stack);
    }
}
