package com.smf.core.blocks

import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Collision box inset by 0.05 on both horizontal axes, matching GregTech's frame boxes.
 *
 * The inset is what makes climbing possible at all. `entityInside` is only called for blocks whose
 * *position* the entity's bounding box overlaps, and `Entity` shrinks that box by 1.0E-7 before
 * converting it to block coordinates. With a full cube the entity stops exactly on the block
 * boundary, the shrunk coordinate falls into the neighbouring block, and the callback never fires
 * while pressing against the block. Insetting the box by 0.05 leaves the entity overlapping the
 * scaffolding by a hair, so the callback does fire — the same trick GTCEu uses for
 * `TagPrefix.frameGt`.
 */
private val SCAFFOLDING_COLLISION_BOX: VoxelShape = Shapes.box(0.05, 0.0, 0.05, 0.95, 1.0, 0.95)

/**
 * Climbing physics for scaffolding, ported from GTCEu's frame boxes
 * (`MaterialBlock#entityInside` for `TagPrefix.frameGt`).
 *
 * This deliberately does not rely on the vanilla `#minecraft:climbable` tag: that only applies
 * while the entity is *inside* the block, which a solid structural block never allows. Instead the
 * behaviour is driven by the block's own volume overlap:
 *
 * - pressing into the scaffolding (`horizontalCollision`) lifts the entity, so it climbs;
 * - sneaking holds it in place instead of sliding down;
 * - otherwise it slides down slowly (0.15/tick) like a ladder, and fall distance is reset so
 *   climbing scaffolding never causes fall damage.
 */
private fun applyScaffoldingClimb(entity: Entity) {
    val living = entity as? LivingEntity ?: return
    val delta = living.deltaMovement
    living.resetFallDistance()

    val limit = 0.15
    var vertical = maxOf(delta.y, -limit)

    // Sneaking holds in place (vanilla ladder behaviour), except on vanilla scaffolding, which
    // handles its own descending.
    if (vertical < 0.0 && !living.blockStateOn.isScaffolding(living) &&
        living.isSuppressingSlidingDownLadder && living is Player
    ) {
        val acceleration = 0.15 * (if (delta.y < 0.3) 2.5 else 1.0)
        vertical = minOf(delta.y + acceleration, 0.0)
    }

    // Pushing into the scaffolding is what climbs it.
    if (living.horizontalCollision) {
        vertical = 0.3
    }

    living.deltaMovement = Vec3(
        Mth.clamp(delta.x, -limit, limit),
        vertical,
        Mth.clamp(delta.z, -limit, limit),
    )
}

/**
 * Scaffolding that can be climbed like a GregTech frame box (see [applyScaffoldingClimb]).
 *
 * It stays a solid structural block: only the collision box is inset (imperceptibly so), which
 * keeps it usable in multiblock shapes and as a floor while making it climbable.
 */
open class ScaffoldingBlock(properties: BlockBehaviour.Properties) : Block(properties) {
    override fun getCollisionShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape = SCAFFOLDING_COLLISION_BOX

    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        applyScaffoldingClimb(entity)
    }
}

/**
 * Stainless steel scaffolding: climbable exactly like [ScaffoldingBlock], but also hideable because
 * it is used as a multiblock member block.
 */
class HideableScaffoldingBlock(properties: BlockBehaviour.Properties) : SMFHideableBlock(properties) {
    override fun getCollisionShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape = SCAFFOLDING_COLLISION_BOX

    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        applyScaffoldingClimb(entity)
    }
}
