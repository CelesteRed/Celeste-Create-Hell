package com.celestered.serverfixes.mixin;

import com.celestered.serverfixes.ClassRules;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Food smelted in a furnace/smoker/blast furnace owned by a Cook is "Cooked by a Cook". */
@Mixin(value = AbstractFurnaceBlockEntity.class, remap = false)
public abstract class FurnaceCookStampMixin {
    private static final String ASSEMBLE = "Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;assemble(Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;";

    @Redirect(method = "canBurn", at = @At(value = "INVOKE", target = ASSEMBLE))
    private static ItemStack celeste$stampCanBurn(AbstractCookingRecipe recipe, SingleRecipeInput input, HolderLookup.Provider provider,
                                                  RegistryAccess access, RecipeHolder<?> holder, NonNullList<ItemStack> items, int max, AbstractFurnaceBlockEntity furnace) {
        return celeste$stamp(recipe.assemble(input, provider), furnace);
    }

    @Redirect(method = "burn", at = @At(value = "INVOKE", target = ASSEMBLE))
    private static ItemStack celeste$stampBurn(AbstractCookingRecipe recipe, SingleRecipeInput input, HolderLookup.Provider provider,
                                               RegistryAccess access, RecipeHolder<?> holder, NonNullList<ItemStack> items, int max, AbstractFurnaceBlockEntity furnace) {
        return celeste$stamp(recipe.assemble(input, provider), furnace);
    }

    private static ItemStack celeste$stamp(ItemStack result, AbstractFurnaceBlockEntity furnace) {
        if (furnace != null && ClassRules.COOK.equals(ClassRules.ownerOf(furnace))) ClassRules.stampFood(result, ClassRules.COOK);
        return result;
    }
}
