package com.smf.core.blocks

import com.smf.core.machines.multiblock.HideableBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty

/**
 * A block that can be visually hidden by an assembled multiblock ([HideableBlock]).
 *
 * The block physically stays in place (shape validation keeps working); only its rendering is
 * disabled while the `hidden` property is set (`RenderShape.INVISIBLE`).
 */
open class SMFHideableBlock(properties: BlockBehaviour.Properties) : Block(properties), HideableBlock {
    init {
        registerDefaultState(stateDefinition.any().setValue(HIDDEN, false))
    }

    override val hiddenProperty: BooleanProperty
        get() = HIDDEN

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(HIDDEN)
    }

    override fun getRenderShape(state: BlockState): RenderShape =
        if (state.getValue(HIDDEN)) RenderShape.INVISIBLE else RenderShape.MODEL

    companion object {
        val HIDDEN: BooleanProperty = BooleanProperty.create("hidden")
    }
}