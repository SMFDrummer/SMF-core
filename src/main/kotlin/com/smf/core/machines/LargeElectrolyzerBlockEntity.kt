package com.smf.core.machines

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

class LargeElectrolyzerBlockEntity(bep: BEP) :
    ElectricCraftingMultiblockBlockEntity(
        bep,
        ResourceLocation.fromNamespaceAndPath(SMFCore.ID, ID),
        SHAPE,
        SMFMachines.LARGE_ELECTROLYZER_RECIPE_TYPE
    ) {

    companion object {
        const val ID = "large_electrolyzer"

        // All hatch types are allowed: item in/out, fluid in/out, energy in
        private val allHatchFlags: HatchFlags = HatchFlags.Builder()
            .with(
                HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT,
                HatchTypes.FLUID_INPUT, HatchTypes.FLUID_OUTPUT,
                HatchTypes.ENERGY_INPUT
            )
            .build()

        val SHAPE: ShapeTemplate = ShapeTemplate.LayeredBuilder(
            MachineCasings.get(SMFCore.id("nonconducting_casing")),
            arrayOf(
                arrayOf("aaaaa", "aaaaa", "aaaaa"),
                arrayOf("aaaaa", "abbba", "abbba"),
                arrayOf("aaaaa", "abbba", "abbba"),
                arrayOf("aaaaa", "aa#aa", "aaaaa")
            )
        )
            .key('a', SimpleMember.forBlockId(SMFCore.id("nonconducting_casing")), allHatchFlags)
            .key('b', SimpleMember.forBlockId(SMFCore.id("electrolytic_cell")), HatchFlags.NO_HATCH)
            .build()

        fun create(pos: BlockPos, state: BlockState): LargeElectrolyzerBlockEntity =
            LargeElectrolyzerBlockEntity(
                BEP(
                    SMFBlocks.LARGE_ELECTROLYZER_BLOCK_ENTITY.get() as BlockEntityType<*>,
                    pos,
                    state
                )
            )
    }
}
