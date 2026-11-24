package com.celestered.serverfixes;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class-based crafting rules. Players carry an entity tag for their class (added by the class's
 * Apoli marker power: cch_cook, cch_cleric). Furnaces, smokers, blast furnaces and brewing stands
 * belong to the last player who opened them; campfires to the last entity that put food on them.
 */
public final class ClassRules {
    public static final String OWNER_KEY = "cch_owner_class";
    public static final String COOK = "cook";
    public static final String CLERIC = "cleric";
    public static final String OTHER = "other";

    private ClassRules() {}

    public static String classOf(Entity e) {
        if (e == null) return OTHER;
        if (e.getTags().contains("cch_cook")) return COOK;
        if (e.getTags().contains("cch_cleric")) return CLERIC;
        return OTHER;
    }

    public static void setOwner(BlockEntity be, Entity e) {
        be.getPersistentData().putString(OWNER_KEY, classOf(e));
        be.setChanged();
    }

    public static String ownerOf(BlockEntity be) {
        String s = be.getPersistentData().getString(OWNER_KEY);
        return s.isEmpty() ? OTHER : s;
    }

    /** kind = "cook" (made by a Cook) or "machine" (made by a Create machine). Only food is stamped. */
    public static ItemStack stampFood(ItemStack stack, String kind) {
        if (stack == null || stack.isEmpty() || !stack.has(DataComponents.FOOD)) return stack;
        CompoundTag existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (existing.contains("cch_food")) return stack;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("cch_food", kind));
        Component line = COOK.equals(kind)
                ? Component.literal("Cooked by a Cook").withStyle(s -> s.withColor(ChatFormatting.GOLD).withItalic(false))
                : Component.literal("Factory-made").withStyle(s -> s.withColor(ChatFormatting.GRAY).withItalic(false));
        stack.set(DataComponents.LORE, stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).withLineAdded(line));
        return stack;
    }

    public static void stampAll(List<ItemStack> stacks, String kind) {
        if (stacks == null) return;
        for (ItemStack s : stacks) stampFood(s, kind);
    }

    /** Amateur potion: every effect at level I and half duration; no potion id, so it can't be brewed further. */
    public static void weakenPotion(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        PotionContents pc = stack.get(DataComponents.POTION_CONTENTS);
        if (pc == null) return;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.getBoolean("cch_amateur")) return;
        List<MobEffectInstance> effects = new ArrayList<>();
        for (MobEffectInstance e : pc.getAllEffects()) {
            int duration = e.getEffect().value().isInstantenous() ? e.getDuration() : Math.max(20, e.getDuration() / 2);
            effects.add(new MobEffectInstance(e.getEffect(), duration, 0, e.isAmbient(), e.isVisible(), e.showIcon()));
        }
        if (effects.isEmpty()) return; // water, awkward, mundane, thick: nothing to weaken
        MutableComponent name = stack.getHoverName().copy().append(" (Amateur)").withStyle(s -> s.withItalic(false));
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(pc.getColor()), effects));
        stack.set(DataComponents.ITEM_NAME, name);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("cch_amateur", true));
    }
}
