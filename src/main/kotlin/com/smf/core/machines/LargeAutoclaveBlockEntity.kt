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

/**
 * Large Autoclave: a tall pressure-vessel tower. All casing positions accept
 * item/fluid input-output and energy input hatches; a stainless steel pipe
 * casing spine runs through the middle and the controller sits on top.
 */
class LargeAutoclaveBlockEntity(bep: BEP) :
    ElectricCraftingMultiblockBlockEntity(
        bep,
        ResourceLocation.fromNamespaceAndPath(SMFCore.ID, ID),
        SHAPE,
        SMFMachines.LARGE_AUTOCLAVE_RECIPE_TYPE
    ) {

    companion object {
        const val ID = "large_autoclave"

        private val allHatchFlags: HatchFlags = HatchFlags.Builder()
            .with(
                HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT,
                HatchTypes.FLUID_INPUT, HatchTypes.FLUID_OUTPUT,
                HatchTypes.ENERGY_INPUT
            )
            .build()

        private val cleanCasing: SimpleMember =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "clean_stainless_steel_machine_casing"))
        private val stainlessPipe: SimpleMember =
            SimpleMember.forBlockId(SMFCore.id("stainless_steel_machine_pipe_casing"))

        val SHAPE: ShapeTemplate = ShapeTemplate.LayeredBuilder(
            MachineCasings.CLEAN_STAINLESS_STEEL,
            arrayOf(
                arrayOf("aaa", "aaa", "aaa"),
                arrayOf("aaa", "aba", "aaa"),
                arrayOf("aaa", "aba", "aaa"),
                arrayOf("aaa", "aba", "aaa"),
                arrayOf("aaa", "a#a", "aaa")
            )
        )
            .key('a', cleanCasing, allHatchFlags)
            .key('b', stainlessPipe, HatchFlags.NO_HATCH)
            .build()

        fun create(pos: BlockPos, state: BlockState): LargeAutoclaveBlockEntity =
            LargeAutoclaveBlockEntity(
                BEP(
                    SMFBlocks.LARGE_AUTOCLAVE_BLOCK_ENTITY.get() as BlockEntityType<*>,
                    pos,
                    state
                )
            )
    }
}