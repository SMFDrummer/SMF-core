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
 * Large Chemical Bath: a long dipping tank with a hollow bath inside. All casing
 * positions accept item/fluid input-output and energy input hatches; the bath ends
 * use titanium pipes.
 */
class LargeChemicalBathBlockEntity(bep: BEP) :
    ElectricCraftingMultiblockBlockEntity(
        bep,
        ResourceLocation.fromNamespaceAndPath(SMFCore.ID, ID),
        SHAPE,
        SMFMachines.LARGE_CHEMICAL_BATH_RECIPE_TYPE
    ) {

    companion object {
        const val ID = "large_chemical_bath"

        private val allHatchFlags: HatchFlags = HatchFlags.Builder()
            .with(
                HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT,
                HatchTypes.FLUID_INPUT, HatchTypes.FLUID_OUTPUT,
                HatchTypes.ENERGY_INPUT
            )
            .build()

        private val cleanCasing: SimpleMember =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "clean_stainless_steel_machine_casing"))
        private val titaniumPipe: SimpleMember =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "titanium_machine_casing_pipe"))
        private val smfTitaniumPipe: SimpleMember =
            SimpleMember.forBlockId(SMFCore.id("titanium_machine_pipe_casing"))

        val SHAPE: ShapeTemplate = ShapeTemplate.LayeredBuilder(
            MachineCasings.CLEAN_STAINLESS_STEEL,
            arrayOf(
                arrayOf("aaaaa", "aaaaa", "aaaaa"),
                arrayOf("aaaaa", "bcccb", "a   a"),
                arrayOf("aaaaa", "a   a", "a   a"),
                arrayOf("aaaaa", "a   a", "a   a"),
                arrayOf("aaaaa", "a   a", "a   a"),
                arrayOf("aaaaa", "bcccb", "a   a"),
                arrayOf("aaaaa", "aa#aa", "aaaaa")
            )
        )
            .key('a', cleanCasing, allHatchFlags)
            .key('b', titaniumPipe, HatchFlags.NO_HATCH)
            .key('c', smfTitaniumPipe, HatchFlags.NO_HATCH)
            .build()

        fun create(pos: BlockPos, state: BlockState): LargeChemicalBathBlockEntity =
            LargeChemicalBathBlockEntity(
                BEP(
                    SMFBlocks.LARGE_CHEMICAL_BATH_BLOCK_ENTITY.get() as BlockEntityType<*>,
                    pos,
                    state
                )
            )
    }
}