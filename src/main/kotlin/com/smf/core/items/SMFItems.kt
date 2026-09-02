package com.smf.core.items

import com.smf.core.SMFCore
import com.smf.core.blocks.SMFBlocks
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SmithingTemplateItem
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object SMFItems {
    val REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(SMFCore.ID)

    val CREATIVE_TABS: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SMFCore.ID)

    val STEEL_SCAFFOLDING: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "steel_scaffolding",
        SMFBlocks.STEEL_SCAFFOLDING,
        Item.Properties()
    )

    val STAINLESS_STEEL_SCAFFOLDING: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "stainless_steel_scaffolding",
        SMFBlocks.STAINLESS_STEEL_SCAFFOLDING,
        Item.Properties()
    )

    val TITANIUM_SCAFFOLDING: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "titanium_scaffolding",
        SMFBlocks.TITANIUM_SCAFFOLDING,
        Item.Properties()
    )

    val STAINLESS_STEEL_MACHINE_PIPE_CASING: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "stainless_steel_machine_pipe_casing",
        SMFBlocks.STAINLESS_STEEL_MACHINE_PIPE_CASING,
        Item.Properties()
    )

    val TITANIUM_MACHINE_PIPE_CASING: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "titanium_machine_pipe_casing",
        SMFBlocks.TITANIUM_MACHINE_PIPE_CASING,
        Item.Properties()
    )

    val STEEL_MACHINE_PIPE_CASING: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "steel_machine_pipe_casing",
        SMFBlocks.STEEL_MACHINE_PIPE_CASING,
        Item.Properties()
    )

    val TEMPERED_GLASS: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "tempered_glass",
        SMFBlocks.TEMPERED_GLASS,
        Item.Properties()
    )

    val NONCONDUCTING_CASING: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "nonconducting_casing",
        SMFBlocks.NONCONDUCTING_CASING,
        Item.Properties()
    )

    val ELECTROLYTIC_CELL: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "electrolytic_cell",
        SMFBlocks.ELECTROLYTIC_CELL,
        Item.Properties()
    )

    val LARGE_ELECTROLYZER: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "large_electrolyzer",
        SMFBlocks.LARGE_ELECTROLYZER,
        Item.Properties()
    )

    val LARGE_GAS_GENERATOR: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "large_gas_generator",
        SMFBlocks.LARGE_GAS_GENERATOR,
        Item.Properties()
    )

    val CRYOGENIC_FRACTIONATION_TOWER: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "cryogenic_fractionation_tower",
        SMFBlocks.CRYOGENIC_FRACTIONATION_TOWER,
        Item.Properties()
    )

    val LARGE_CHEMICAL_BATH: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "large_chemical_bath",
        SMFBlocks.LARGE_CHEMICAL_BATH,
        Item.Properties()
    )

    val LARGE_AUTOCLAVE: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "large_autoclave",
        SMFBlocks.LARGE_AUTOCLAVE,
        Item.Properties()
    )

    val LARGE_VACUUM_FREEZER: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "large_vacuum_freezer",
        SMFBlocks.LARGE_VACUUM_FREEZER,
        Item.Properties()
    )

    val ALUMINIUM_HYDROXIDE_DUST: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("aluminium_hydroxide_dust", Item.Properties())
    val ALUMINA_DUST: DeferredItem<Item> = REGISTRY.registerSimpleItem("alumina_dust", Item.Properties())

    val COBALT_ORE: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "cobalt_ore",
        SMFBlocks.COBALT_ORE,
        Item.Properties()
    )

    val DEEPSLATE_COBALT_ORE: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "deepslate_cobalt_ore",
        SMFBlocks.DEEPSLATE_COBALT_ORE,
        Item.Properties()
    )

    val SILVER_ORE: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "silver_ore",
        SMFBlocks.SILVER_ORE,
        Item.Properties()
    )

    val FLUORITE_ORE: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "fluorite_ore",
        SMFBlocks.FLUORITE_ORE,
        Item.Properties()
    )

    val DEEPSLATE_FLUORITE_ORE: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "deepslate_fluorite_ore",
        SMFBlocks.DEEPSLATE_FLUORITE_ORE,
        Item.Properties()
    )

    val FLUORITE_GEM: DeferredItem<Item> = REGISTRY.registerSimpleItem("fluorite_gem", Item.Properties())
    val FLUORITE_DUST: DeferredItem<Item> = REGISTRY.registerSimpleItem("fluorite_dust", Item.Properties())
    val SODIUM_HYDROXIDE_DUST: DeferredItem<Item> = REGISTRY.registerSimpleItem("sodium_hydroxide_dust", Item.Properties())
    val CALCIUM_SULFATE_DUST: DeferredItem<Item> = REGISTRY.registerSimpleItem("calcium_sulfate_dust", Item.Properties())
    val CRYOLITE_DUST: DeferredItem<Item> = REGISTRY.registerSimpleItem("cryolite_dust", Item.Properties())

    val COBALT_BLOCK: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "cobalt_block",
        SMFBlocks.COBALT_BLOCK,
        Item.Properties()
    )

    val RAW_COBALT_BLOCK: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "raw_cobalt_block",
        SMFBlocks.RAW_COBALT_BLOCK,
        Item.Properties()
    )

    val ADAMANTINE_DEBRIS: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "adamantine_debris",
        SMFBlocks.ADAMANTINE_DEBRIS,
        Item.Properties()
    )

    val ADAMANTINE_BLOCK: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "adamantine_block",
        SMFBlocks.ADAMANTINE_BLOCK,
        Item.Properties()
    )

    val ADAMANTINE_SCRAP: DeferredItem<Item> = REGISTRY.registerSimpleItem("adamantine_scrap", Item.Properties())
    val ADAMANTINE_INGOT: DeferredItem<Item> = REGISTRY.registerSimpleItem("adamantine_ingot", Item.Properties())

    val ADAMANTINE_UPGRADE_SMITHING_TEMPLATE: DeferredItem<SmithingTemplateItem> = REGISTRY.register(
        "adamantine_upgrade_smithing_template",
        Supplier {
            SmithingTemplateItem(
                Component.translatable("item.smfcore.smithing_template.adamantine_upgrade.applies_to"),
                Component.translatable("item.smfcore.smithing_template.adamantine_upgrade.ingredients"),
                Component.translatable("upgrade.smfcore.adamantine_upgrade"),
                Component.translatable("item.smfcore.smithing_template.adamantine_upgrade.base_slot_description"),
                Component.translatable("item.smfcore.smithing_template.adamantine_upgrade.additions_slot_description"),
                listOf(ResourceLocation.withDefaultNamespace("item/empty_slot_ingot")),
                listOf(ResourceLocation.withDefaultNamespace("item/empty_slot_ingot"))
            )
        }
    )

    val TUNGSTEN_CARBIDE_DUST: DeferredItem<Item> = REGISTRY.registerSimpleItem(
        "tungsten_carbide_dust",
        Item.Properties()
    )

    val RAW_COBALT: DeferredItem<Item> = REGISTRY.registerSimpleItem("raw_cobalt", Item.Properties())
    val COBALT_DUST: DeferredItem<Item> = REGISTRY.registerSimpleItem("cobalt_dust", Item.Properties())
    val COBALT_TINY_DUST: DeferredItem<Item> = REGISTRY.registerSimpleItem("cobalt_tiny_dust", Item.Properties())
    val TALONITE_DUST: DeferredItem<Item> = REGISTRY.registerSimpleItem("talonite_dust", Item.Properties())
    val TALONITE_PLATE: DeferredItem<Item> = REGISTRY.registerSimpleItem("talonite_plate", Item.Properties())
    val TALONITE_DRILL_HEAD: DeferredItem<Item> = REGISTRY.registerSimpleItem("talonite_drill_head", Item.Properties())
    val TALONITE_DRILL: DeferredItem<Item> = REGISTRY.registerSimpleItem("talonite_drill", Item.Properties())
    val COBALT_INGOT: DeferredItem<Item> = REGISTRY.registerSimpleItem("cobalt_ingot", Item.Properties())
    val COBALT_DOUBLE_INGOT: DeferredItem<Item> = REGISTRY.registerSimpleItem("cobalt_double_ingot", Item.Properties())
    val COBALT_NUGGET: DeferredItem<Item> = REGISTRY.registerSimpleItem("cobalt_nugget", Item.Properties())
    val COBALT_PLATE: DeferredItem<Item> = REGISTRY.registerSimpleItem("cobalt_plate", Item.Properties())
    val COBALT_LARGE_PLATE: DeferredItem<Item> = REGISTRY.registerSimpleItem("cobalt_large_plate", Item.Properties())

    val ELECTRIC_FRITTING_FURNACE: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "electric_fritting_furnace",
        SMFBlocks.ELECTRIC_FRITTING_FURNACE,
        Item.Properties()
    )

    val HYPER_PRESSURE_REACTOR: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "hyper_pressure_reactor",
        SMFBlocks.HYPER_PRESSURE_REACTOR,
        Item.Properties()
    )

    val ROTATING_GLASS_REACTOR: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "rotating_glass_reactor",
        SMFBlocks.ROTATING_GLASS_REACTOR,
        Item.Properties()
    )

    val RED_MUD: DeferredItem<Item> = REGISTRY.registerSimpleItem("red_mud", Item.Properties())

    val RARE_EARTH_HYDROXIDE_MIXTURE: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("rare_earth_hydroxide_mixture", Item.Properties())

    val HYDRATED_RARE_EARTH_CHLORIDE: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("hydrated_rare_earth_chloride", Item.Properties())

    val RARE_EARTH_DUST: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("rare_earth_dust", Item.Properties())

    val TITANIUM_RESIDUE: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("titanium_residue", Item.Properties())

    val METAL_HYDROXIDE_MIXTURE: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("metal_hydroxide_mixture", Item.Properties())

    val METAL_DIOXIDE_MIXTURE: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("metal_dioxide_mixture", Item.Properties())

    val ADVANCED_OVERDRIVE_MODULE: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("advanced_overdrive_module", Item.Properties())

    val RADIOACTIVE_RESIDUE: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("radioactive_residue", Item.Properties())

    val CARNALLITE_ORE: DeferredItem<BlockItem> = REGISTRY.registerSimpleBlockItem(
        "carnallite_ore",
        SMFBlocks.CARNALLITE_ORE,
        Item.Properties()
    )

    val CARNALLITE_DUST: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("carnallite_dust", Item.Properties())

    val POTASSIUM_CHLORIDE_DUST: DeferredItem<Item> =
        REGISTRY.registerSimpleItem("potassium_chloride_dust", Item.Properties())

    val SMF_TAB = CREATIVE_TABS.register("smfcore_tab", Supplier {
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.smfcore"))
            .icon { ItemStack(COBALT_INGOT.get()) }
            .displayItems { _, output ->
                output.accept(STEEL_SCAFFOLDING.get())
                output.accept(STAINLESS_STEEL_SCAFFOLDING.get())
                output.accept(TITANIUM_SCAFFOLDING.get())
                output.accept(STAINLESS_STEEL_MACHINE_PIPE_CASING.get())
                output.accept(STEEL_MACHINE_PIPE_CASING.get())
                output.accept(TITANIUM_MACHINE_PIPE_CASING.get())
                output.accept(TEMPERED_GLASS.get())
                output.accept(NONCONDUCTING_CASING.get())
                output.accept(ELECTROLYTIC_CELL.get())
                output.accept(LARGE_ELECTROLYZER.get())
                output.accept(ROTATING_GLASS_REACTOR.get())
                output.accept(ALUMINIUM_HYDROXIDE_DUST.get())
                output.accept(ALUMINA_DUST.get())
                output.accept(COBALT_ORE.get())
                output.accept(DEEPSLATE_COBALT_ORE.get())
                output.accept(SILVER_ORE.get())
                output.accept(FLUORITE_ORE.get())
                output.accept(DEEPSLATE_FLUORITE_ORE.get())
                output.accept(FLUORITE_GEM.get())
                output.accept(FLUORITE_DUST.get())
                output.accept(SODIUM_HYDROXIDE_DUST.get())
                output.accept(CALCIUM_SULFATE_DUST.get())
                output.accept(CRYOLITE_DUST.get())
                output.accept(RAW_COBALT_BLOCK.get())
                output.accept(COBALT_BLOCK.get())
                output.accept(RAW_COBALT.get())
                output.accept(COBALT_DUST.get())
                output.accept(COBALT_TINY_DUST.get())
                output.accept(COBALT_INGOT.get())
                output.accept(COBALT_DOUBLE_INGOT.get())
                output.accept(COBALT_NUGGET.get())
                output.accept(COBALT_PLATE.get())
                output.accept(COBALT_LARGE_PLATE.get())
                output.accept(TUNGSTEN_CARBIDE_DUST.get())
                output.accept(TALONITE_DUST.get())
                output.accept(TALONITE_PLATE.get())
                output.accept(TALONITE_DRILL_HEAD.get())
                output.accept(TALONITE_DRILL.get())
                output.accept(ADAMANTINE_DEBRIS.get())
                output.accept(ADAMANTINE_SCRAP.get())
                output.accept(ADAMANTINE_INGOT.get())
                output.accept(ADAMANTINE_BLOCK.get())
                output.accept(ADAMANTINE_UPGRADE_SMITHING_TEMPLATE.get())
                output.accept(ELECTRIC_FRITTING_FURNACE.get())
                output.accept(HYPER_PRESSURE_REACTOR.get())
                output.accept(LARGE_GAS_GENERATOR.get())
                output.accept(CRYOGENIC_FRACTIONATION_TOWER.get())
                output.accept(LARGE_CHEMICAL_BATH.get())
                output.accept(LARGE_AUTOCLAVE.get())
                output.accept(LARGE_VACUUM_FREEZER.get())
                output.accept(RED_MUD.get())
                output.accept(RARE_EARTH_HYDROXIDE_MIXTURE.get())
                output.accept(HYDRATED_RARE_EARTH_CHLORIDE.get())
                output.accept(RARE_EARTH_DUST.get())
                output.accept(TITANIUM_RESIDUE.get())
                output.accept(METAL_HYDROXIDE_MIXTURE.get())
                output.accept(METAL_DIOXIDE_MIXTURE.get())
                output.accept(ADVANCED_OVERDRIVE_MODULE.get())
                output.accept(RADIOACTIVE_RESIDUE.get())
                output.accept(CARNALLITE_ORE.get())
                output.accept(CARNALLITE_DUST.get())
                output.accept(POTASSIUM_CHLORIDE_DUST.get())
            }
            .build()
    })

    fun init() = Unit
}
