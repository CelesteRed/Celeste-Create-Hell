package com.celestered.serverfixes.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerInventory;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerMode;

/** Create Enchantment Industry's Blaze Forger can't apply enchanted books to tools either (armor is fine). */
@Mixin(value = BlazeForgerInventory.class, remap = false)
public abstract class BlazeForgerArmorOnlyMixin {
    @Inject(method = "calculateApply", at = @At("HEAD"), cancellable = true, require = 0)
    private void celeste$booksOnArmorOnly(ItemStack target, ItemStack addition, CallbackInfoReturnable<BlazeForgerInventory.Result> cir) {
        if (target.isEmpty() || addition.isEmpty() || target.is(Items.ENCHANTED_BOOK) || target.is(Items.BOOK)) return;
        if (target.is(ItemTags.ARMOR_ENCHANTABLE)) return;
        if (addition.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()) return;
        cir.setReturnValue(BlazeForgerInventory.Result.invalid(BlazeForgerMode.APPLY,
                Component.literal("Enchanted books can only be applied to armor. Tools level up by being used.")));
    }
}
