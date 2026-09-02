package com.smf.core.enchantments

import com.smf.core.SMFCore
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment

object SMFEnchantments {
    val ADAMANTINE: ResourceKey<Enchantment> =
        ResourceKey.create(Registries.ENCHANTMENT, SMFCore.id("adamantine"))
}
