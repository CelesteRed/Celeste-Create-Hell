package com.celestered.serverfixes.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Enchanted books can only be applied to armor (or combined with other books) in an anvil. Tools level up through Penchant. */
@Mixin(value = AnvilMenu.class, remap = false)
public abstract class AnvilBooksArmorOnlyMixin {
    @Shadow @Final private DataSlot cost;

    @Inject(method = "createResult", at = @At("TAIL"))
    private void celeste$booksOnArmorOnly(CallbackInfo ci) {
        AnvilMenu self = (AnvilMenu) (Object) this;
        ItemStack left = self.getSlot(0).getItem();
        ItemStack right = self.getSlot(1).getItem();
        if (!celeste$isBookOntoTool(left, right)) return;
        self.getSlot(2).set(ItemStack.EMPTY);
        cost.set(0);
    }

    private static boolean celeste$isBookOntoTool(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) return false;
        ItemEnchantments stored = right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (stored.isEmpty()) return false;
        if (left.is(Items.ENCHANTED_BOOK) || left.is(Items.BOOK)) return false;
        return !left.is(ItemTags.ARMOR_ENCHANTABLE);
    }
}
