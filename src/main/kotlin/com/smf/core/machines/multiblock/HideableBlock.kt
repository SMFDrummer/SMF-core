package com.smf.core.machines.multiblock

import net.minecraft.world.level.block.state.properties.BooleanProperty

/**
 * A block that can be visually hidden by an assembled multiblock (see [SMFKeyFlag.HIDDEN]).
 *
 * The block physically stays in place (shape validation keeps working); only its rendering is
 * disabled while the property is `true`. `getRenderShape` should return `RenderShape.INVISIBLE`
 * when the property is set.
 */
interface HideableBlock {
    val hiddenProperty: BooleanProperty
}