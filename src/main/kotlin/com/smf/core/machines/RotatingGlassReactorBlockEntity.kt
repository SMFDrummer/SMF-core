package com.smf.core.machines

import aztech.modern_industrialization.machines.BEP
import aztech.modern_industrialization.machines.models.MachineCasings
import aztech.modern_industrialization.machines.multiblocks.HatchFlags
import aztech.modern_industrialization.machines.multiblocks.HatchTypes
import aztech.modern_industrialization.machines.multiblocks.SimpleMember
import com.smf.core.SMFCore
import com.smf.core.blocks.SMFBlocks
import com.smf.core.machines.multiblock.SMFKeyFlag
import com.smf.core.machines.multiblock.SMFKeyFlags
import com.smf.core.machines.multiblock.SMFMultiblockBlockEntity
import com.smf.core.machines.multiblock.SMFShape
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

/**
 * Example multiblock whose assembled tempered-glass structure is rendered rotating by the
 * Create-style renderer (`SMFRotatingMultiblockRenderer`): the glass members are hidden by the
 * base class while the structure is assembled, and the renderer rotates their baked models
 * around the machine's front-back axis while a recipe runs.
 */
class RotatingGlassReactorBlockEntity(bep: BEP) :
    SMFMultiblockBlockEntity(
        bep,
        SMFCore.id(ID),
        SHAPE,
        SMFMachines.ROTATING_GLASS_REACTOR_RECIPE_TYPE
    ) {

    /** Fast spin while running: 6 deg/tick = one revolution per second. */
    override fun getRunSpeedDegPerTick(): Float = 6.0f

    companion object {
        const val ID = "rotating_glass_reactor"

        // b = clean stainless steel casing: hosts the item/fluid/energy hatches.
        private val mainHatchFlags: HatchFlags = HatchFlags.Builder()
            .with(
                HatchTypes.ITEM_INPUT,
                HatchTypes.ITEM_OUTPUT,
                HatchTypes.FLUID_INPUT,
                HatchTypes.FLUID_OUTPUT,
                HatchTypes.ENERGY_INPUT
            )
            .build()

        /** The raw layered shape, shared with REI registration. */
        val LAYERS: Array<Array<String>> = arrayOf(
            arrayOf("   a   ", "       ", "  bbb  ", "a bbb a", "  bbb  ", "       ", "   a   "),
            arrayOf("   a   ", "       ", "       ", "a     a", "       ", "       ", "   a   "),
            arrayOf("   a   ", "   a   ", "   a   ", "aaaaaaa", "   a   ", "   a   ", "   a   "),
            arrayOf("       ", "       ", "       ", "       ", "       ", "       ", "       "),
            arrayOf("       ", "       ", "  bbb  ", "  b#b  ", "  bbb  ", "       ", "       "),
        )

        val SHAPE: SMFShape = SMFShape.Builder(
            MachineCasings.CLEAN_STAINLESS_STEEL,
            LAYERS
        )
            // Glass: hidden while assembled, rendered as the rotating structure by the renderer.
            .key(
                'a',
                SimpleMember.forBlockId(SMFCore.id("tempered_glass")),
                HatchFlags.NO_HATCH,
                SMFKeyFlags.of(SMFKeyFlag.HIDDEN)
            )
            .key(
                'b',
                SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "clean_stainless_steel_machine_casing")),
                mainHatchFlags
            )
            .build()

        fun create(pos: BlockPos, state: BlockState): RotatingGlassReactorBlockEntity =
            RotatingGlassReactorBlockEntity(
                BEP(
                    SMFBlocks.ROTATING_GLASS_REACTOR_BLOCK_ENTITY.get() as BlockEntityType<*>,
                    pos,
                    state
                )
            )
    }
}