package com.smf.core.machines.multiblock

import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Deferred restore of the blocks hidden by an assembled SMF multiblock.
 *
 * [SMFMultiblockBlockEntity.setRemoved] also runs while a chunk unloads, and touching the level
 * there is fatal: `getBlockState` on a not-yet-loaded neighbour synchronously loads that chunk
 * while the chunk system is mid-unload (deadlock), and `setBlockAndUpdate` would additionally
 * mutate the block-entity map the unload is iterating (`removeBlockEntity` from `setRemoved`).
 *
 * `setRemoved` therefore only *records* the removal here, and the world writes happen on the next
 * server tick — a normal, unlocked context. An entry is dropped when the controller is still
 * present (a plain unload, nothing to restore) or when its chunk is gone, and every target
 * position is checked with [ServerLevel.hasChunkAt] so this code never loads a chunk itself.
 */
object SMFHiddenBlockRestore {

    private class Pending(
        val level: WeakReference<ServerLevel>,
        val pos: BlockPos,
        val facing: Direction,
        val shape: SMFShape,
    )

    private val pending = ConcurrentLinkedQueue<Pending>()

    /** Records a controller removal. Never touches the level. */
    fun enqueue(level: ServerLevel, pos: BlockPos, facing: Direction, shape: SMFShape) {
        pending.add(Pending(WeakReference(level), pos.immutable(), facing, shape))
    }

    /** Applies the pending restores that are safe to apply. Called once per server tick. */
    fun tick(server: MinecraftServer) {
        if (pending.isEmpty()) return
        val iterator = pending.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val level = entry.level.get()
            // Level gone (shutdown / dimension unload): nothing to do.
            if (level == null || level.server !== server) {
                iterator.remove()
                continue
            }
            // Chunk already unloaded, or the controller is still there (this was a chunk unload
            // rather than a real block removal): the structure is still assembled, keep the blocks
            // hidden and never load the chunk back.
            if (!level.isLoaded(entry.pos) || level.getBlockEntity(entry.pos) is SMFMultiblockBlockEntity) {
                iterator.remove()
                continue
            }
            restore(level, entry)
            iterator.remove()
        }
    }

    private fun restore(level: ServerLevel, entry: Pending) {
        for ((templatePos, flags) in entry.shape.keyFlags) {
            if (!flags.hidden) {
                continue
            }
            val pos = ShapeMatcher.toWorldPos(entry.pos, entry.facing, templatePos)
            // Only ever touch chunks that are already loaded.
            if (!level.isLoaded(pos)) {
                continue
            }
            val state = level.getBlockState(pos)
            val block = state.block as? HideableBlock ?: continue
            val target = state.setValue(block.hiddenProperty, false)
            if (state !== target) {
                // UPDATE_CLIENTS only: the hidden property is purely visual, and skipping neighbour
                // updates guarantees no neighbouring chunk can be loaded by this write.
                level.setBlock(pos, target, Block.UPDATE_CLIENTS)
            }
        }
    }
}
