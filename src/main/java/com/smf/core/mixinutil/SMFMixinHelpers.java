package com.smf.core.mixinutil;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class SMFMixinHelpers {
    public static final TagKey<Item> DRILLS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("smfcore", "tools/drills"));
    public static final ResourceKey<Enchantment> ADAMANTINE =
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("smfcore", "adamantine"));

    private SMFMixinHelpers() {}

    /**
     * Returns the maximum enchantment level found in the given input stacks,
     * without needing a registry access.
     */
    public static int maxEnchantmentLevel(List<ConfigurableItemStack> stacks, ResourceKey<Enchantment> key) {
        int max = 0;
        for (ConfigurableItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemEnchantments enchantments = stack.toStack().get(DataComponents.ENCHANTMENTS);
            if (enchantments == null) {
                continue;
            }
            for (Holder<Enchantment> holder : enchantments.keySet()) {
                if (holder.is(key)) {
                    max = Math.max(max, enchantments.getLevel(holder));
                }
            }
        }
        return max;
    }
}
