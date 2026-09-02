package com.smf.core.machines

import aztech.modern_industrialization.MI
import aztech.modern_industrialization.machines.BEP
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricCraftingMultiblockBlockEntity
import aztech.modern_industrialization.machines.models.MachineCasings
import aztech.modern_industrialization.machines.multiblocks.HatchFlags
import aztech.modern_industrialization.machines.multiblocks.HatchTypes
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate
import aztech.modern_industrialization.machines.multiblocks.SimpleMember
import com.smf.core.SMFCore
import com.smf.core.blocks.SMFBlocks
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class ElectricFrittingFurnaceBlockEntity(bep: BEP) :
    ElectricCraftingMultiblockBlockEntity(
        bep,
        ResourceLocation.fromNamespaceAndPath(SMFCore.ID, ID),
        SHAPE,
        SMFMachines.ELECTRIC_FRITTING_FURNACE_RECIPE_TYPE
    ) {

    companion object {
        const val ID = "electric_fritting_furnace"

        private val itemIoEnergyFlags: HatchFlags = HatchFlags.Builder()
            .with(HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT, HatchTypes.ENERGY_INPUT)
            .build()

        val SHAPE: ShapeTemplate = ShapeTemplate.LayeredBuilder(
            MachineCasings.HEATPROOF,
            arrayOf(
                arrayOf("a b a", "accca", "bdecb", " ddd ", "  b  "),
                arrayOf("  g  ", " ghg ", "ghhhg", " ghg ", "  g  "),
                arrayOf("  g  ", " ghg ", "gh hg", " ghg ", "  g  "),
                arrayOf("  g  ", " ghg ", "ghhhg", " ghg ", "  g  "),
                arrayOf("a b a", "abbba", "bb#bb", " bbb ", "  b  ")
            )
        )
            .key('a', SimpleMember.forBlock(SMFBlocks.STEEL_SCAFFOLDING), HatchFlags.NO_HATCH)
            .key('b', SimpleMember.forBlockId(MI.id("heatproof_machine_casing")), HatchFlags.NO_HATCH)
            .key('c', SimpleMember.forBlockId(MI.id("heatproof_machine_casing")), itemIoEnergyFlags)
            .key('d', SimpleMember.forBlockId(MI.id("heatproof_machine_casing")), itemIoEnergyFlags)
            .key('e', SimpleMember.forBlockId(MI.id("heatproof_machine_casing")), itemIoEnergyFlags)
            .key('g', SimpleMember.forBlockId(MI.id("steel_machine_casing")), HatchFlags.NO_HATCH)
            .key('h', SimpleMember.forBlockId(MI.id("fire_clay_bricks")), HatchFlags.NO_HATCH)
            .build()

        fun create(pos: BlockPos, state: BlockState): ElectricFrittingFurnaceBlockEntity =
            ElectricFrittingFurnaceBlockEntity(
                BEP(
                    SMFBlocks.ELECTRIC_FRITTING_FURNACE_BLOCK_ENTITY.get() as BlockEntityType<*>,
                    pos,
                    state
                )
            )
    }
}
