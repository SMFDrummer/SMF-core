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

class HyperPressureReactorBlockEntity(bep: BEP) :
    ElectricCraftingMultiblockBlockEntity(
        bep,
        ResourceLocation.fromNamespaceAndPath(SMFCore.ID, ID),
        SHAPE,
        SMFMachines.HYPER_PRESSURE_REACTOR_RECIPE_TYPE
    ) {

    companion object {
        const val ID = "hyper_pressure_reactor"

        // Item in/out, fluid in and energy in hatches are allowed (no fluid output).
        private val mainHatchFlags: HatchFlags = HatchFlags.Builder()
            .with(HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT, HatchTypes.FLUID_INPUT, HatchTypes.ENERGY_INPUT)
            .build()

        // Only fluid output hatches are allowed here.
        private val fluidOutputHatchFlags: HatchFlags = HatchFlags.Builder()
            .with(HatchTypes.FLUID_OUTPUT)
            .build()

        val SHAPE: ShapeTemplate = ShapeTemplate.LayeredBuilder(
            MachineCasings.CLEAN_STAINLESS_STEEL,
            arrayOf(
                arrayOf(" aaa ", " aaa ", "  b  ", "     "),
                arrayOf("aaaaa", "addda", " ddd ", " ddd "),
                arrayOf("aaaaa", "adcda", "bdcdb", " dcd "),
                arrayOf("aaaaa", "addda", " ddd ", " ddd "),
                arrayOf(" aaa ", " a#a ", "  b  ", "     ")
            )
        )
            .key('a', SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "clean_stainless_steel_machine_casing")), mainHatchFlags)
            .key('b', SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "clean_stainless_steel_machine_casing")), fluidOutputHatchFlags)
            .key('c', SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath(SMFCore.ID, "stainless_steel_machine_pipe_casing")), HatchFlags.NO_HATCH)
            .key('d', SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "stainless_steel_machine_casing_pipe")), HatchFlags.NO_HATCH)
            .build()

        fun create(pos: BlockPos, state: BlockState): HyperPressureReactorBlockEntity =
            HyperPressureReactorBlockEntity(
                BEP(
                    SMFBlocks.HYPER_PRESSURE_REACTOR_BLOCK_ENTITY.get() as BlockEntityType<*>,
                    pos,
                    state
                )
            )
    }
}
