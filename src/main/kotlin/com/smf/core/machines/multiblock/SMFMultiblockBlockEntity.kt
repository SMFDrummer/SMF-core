package com.smf.core.machines.multiblock

import aztech.modern_industrialization.machines.BEP
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricCraftingMultiblockBlockEntity
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher
import aztech.modern_industrialization.machines.recipe.MachineRecipeType
import com.mojang.blaze3d.vertex.PoseStack
import com.smf.core.SMFCore
import com.smf.core.client.render.CapturingVertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.client.model.data.ModelData
import java.util.ArrayList

/**
 * Base class for every SMF multiblock machine.
 *
 * Handles the SMF-specific block-visibility behaviour declared through [SMFShape] key flags:
 * when the structure is fully assembled, all members whose key has [SMFKeyFlag.HIDDEN] are
 * visually hidden (they must be [HideableBlock]s); when the shape breaks or the controller is
 * removed, the hidden blocks are restored immediately.
 *
 * Also provides the Create-style rotating-animation state used by
 * `SMFRotatingMultiblockRenderer`: the rotation angle is a stateless function of time
 * (`(renderTime - rotationStartTime) * renderSpeed`), so it never reverses, never needs
 * "homing", is immune to first-render/reconnect state bugs, and multiple cascaded machines
 * animate independently.
 */
abstract class SMFMultiblockBlockEntity(
    bep: BEP,
    blockId: ResourceLocation,
    protected val smfShape: SMFShape,
    recipeType: MachineRecipeType
) : ElectricCraftingMultiblockBlockEntity(bep, blockId, smfShape.template, recipeType) {

    companion object {
        /**
         * Bumped on every client resource reload (atlas changes invalidate baked UV data).
         * Machine caches compare against this integer, so cache invalidation is a cheap int check.
         */
        @Volatile
        var clientResourceVersion = 0
    }

    // ---------------------------------------------------------------------------------------
    // Create-style rotating animation state (client-only, never persisted).
    //
    // Angle integration (like Create's MechanicalBearing): `renderAngle += renderSpeed * dt`
    // every rendered frame. The angle is monotonic (renderSpeed >= 0), so it never reverses and
    // never "unwinds" accumulated revolutions when the speed eases down. Idle = still at the last
    // angle, run = smooth acceleration, recipe end = natural deceleration stopping in place.
    // ---------------------------------------------------------------------------------------

    /** Current smoothed rotation speed in degrees per tick. */
    var renderSpeed = 0.0f

    /**
     * Accumulated rotation angle in degrees (0..360), integrated every rendered frame.
     * Kept as double: game ticks grow unboundedly, and float would swallow the sub-tick
     * frame deltas (angle updates would step instead of being smooth).
     */
    var renderAngle = 0.0

    /** Render-time of the previous frame (double for the same precision reason). */
    var lastRenderTime = -1.0

    /** True while the multiblock shape is valid on the client (re-assembly detection). */
    var wasShapeValid = false

    /** Render-time at which the shape became invalid, or -1 if never observed invalid. */
    var shapeInvalidSinceTick = -1.0f

    /** Rotation speed while a recipe is running, in degrees per tick (default: 6 deg/t = 3 s/rev). */
    open fun getRunSpeedDegPerTick(): Float = 6.0f

    /** Speed easing factor per frame (higher = snappier acceleration/deceleration). */
    open fun getSpeedSmoothing(): Float = 0.12f

    /** Minimum time (ticks) the shape must stay invalid before re-valid counts as a real re-assembly. */
    open fun getReassemblyResetThresholdTicks(): Float = 10.0f

    /**
     * Maximum number of other controllers of the same machine type allowed inside
     * [getCascadeCheckRadius] blocks, or -1 for unlimited cascading.
     *
     * Cascaded machines are supported out of the box (the stateless angle function makes them
     * independent); override this and use [countNearbySameMachines] if a machine must not cascade.
     */
    open fun getCascadeLimit(): Int = -1

    /** Radius (blocks) used by [countNearbySameMachines]. */
    open fun getCascadeCheckRadius(): Int = 3

    /** Counts controllers of the same machine type within [getCascadeCheckRadius] blocks (server side). */
    fun countNearbySameMachines(): Int {
        val world = level ?: return 0
        if (world.isClientSide) {
            return 0
        }
        val radius = getCascadeCheckRadius()
        val minPos = worldPosition.offset(-radius, -radius, -radius)
        val maxPos = worldPosition.offset(radius, radius, radius)
        val mine = getBlockState().block
        var count = 0
        for (pos in BlockPos.betweenClosed(minPos, maxPos)) {
            if (pos == worldPosition) {
                continue
            }
            val other = world.getBlockEntity(pos)
            if (other != null && other !== this && other.blockState.block === mine && other::class == this::class) {
                count++
            }
        }
        return count
    }

    // ---------------------------------------------------------------------------------------
    // Hidden-member baking (Create-style: tessellate once, replay every frame).
    // ---------------------------------------------------------------------------------------

    private var cachedHiddenMembers: List<HiddenMember>? = null
    private var cachedHiddenVertices: List<CapturedMember>? = null
    private var cachedResourceVersion = -1

    /** Drops the baked caches (e.g. when the structure is re-assembled). */
    fun invalidateHiddenMembers() {
        cachedHiddenMembers = null
        cachedHiddenVertices = null
        cachedResourceVersion = -1
    }

    /**
     * Collects the hidden member blocks (keys flagged [SMFKeyFlag.HIDDEN] whose block implements
     * [HideableBlock]) with their world positions and material render layers.
     */
    fun getOrComputeHiddenMembers(level: Level): List<HiddenMember>? {
        if (cachedHiddenMembers != null) {
            return cachedHiddenMembers
        }
        val members = computeHiddenMembers(level)
        cachedHiddenMembers = members
        return members
    }

    private fun computeHiddenMembers(level: Level): List<HiddenMember>? {
        val facing = getOrientation().facingDirection
        val result = ArrayList<HiddenMember>()
        val random = RandomSource.create()

        for ((templatePos, flags) in smfShape.keyFlags) {
            if (!flags.hidden) {
                continue
            }
            val worldPos = ShapeMatcher.toWorldPos(worldPosition, facing, templatePos)
            val state = level.getBlockState(worldPos)
            val block = state.block
            if (block !is HideableBlock) {
                continue
            }
            val model = Minecraft.getInstance().blockRenderer.getBlockModel(state)
            // Use the layer the material actually renders in (translucent for glass, cutout for
            // opaque-but-non-occluding blocks) so brightness matches the real block.
            val renderTypes = model.getRenderTypes(state, random, ModelData.EMPTY)
            val renderType = if (renderTypes.contains(RenderType.translucent())) {
                RenderType.translucent()
            } else {
                RenderType.cutout()
            }
            result.add(
                HiddenMember(
                    intArrayOf(worldPos.x - worldPosition.x, worldPos.y - worldPosition.y, worldPos.z - worldPosition.z),
                    worldPos,
                    state,
                    renderType
                )
            )
        }
        return if (result.isEmpty()) null else result
    }

    /**
     * Tesselates the hidden members once through the vanilla block renderer and captures the
     * resulting per-vertex data (positions in controller-relative space, per-vertex AO color,
     * UVs, per-vertex light, normals). The renderer replays this data every frame with only a
     * rotation matrix — no AO/lookup work per frame.
     */
    fun getOrBakeHiddenVertices(level: Level): List<CapturedMember>? {
        if (cachedHiddenVertices != null && cachedResourceVersion == clientResourceVersion) {
            return cachedHiddenVertices
        }

        val members = getOrComputeHiddenMembers(level) ?: return null
        val blockRenderer = Minecraft.getInstance().blockRenderer
        val random = RandomSource.create()
        val result = ArrayList<CapturedMember>(members.size)

        for (member in members) {
            val model = blockRenderer.getBlockModel(member.state)
            val capturing = CapturingVertexConsumer()
            val seed = member.state.getSeed(member.worldPos)
            random.setSeed(seed)
            blockRenderer.modelRenderer.tesselateBlock(
                level,
                model,
                member.state,
                member.worldPos,
                PoseStack().apply { translate(member.offset[0].toDouble(), member.offset[1].toDouble(), member.offset[2].toDouble()) },
                capturing,
                // checkSides = false: bake every face. Faces culled against solid neighbours
                // (e.g. ground blocks) would be missing once the structure rotates them into view.
                false,
                random,
                seed,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                member.renderType
            )
            result.add(CapturedMember(member.renderType, capturing.data))
        }

        cachedHiddenVertices = if (result.isEmpty()) null else result
        cachedResourceVersion = clientResourceVersion
        val vertexCount = result.sumOf { it.data.size }
        SMFCore.LOGGER.info("[SMF] Baked hidden vertices for {}: {} members, {} vertices (res v{})", this::class.java.simpleName, result.size, vertexCount, clientResourceVersion)
        return cachedHiddenVertices
    }

    override fun onRematch(shapeMatcher: ShapeMatcher) {
        super.onRematch(shapeMatcher)
        val world = level
        if (world != null && !world.isClientSide) {
            // isMatchSuccessful() is computed before shapeValid flips to true, so it already
            // reflects the rematch that just ran.
            updateHiddenBlocks(shapeMatcher.isMatchSuccessful())
        }
    }

    override fun setRemoved() {
        super.setRemoved()
        val world = level
        if (world != null && !world.isClientSide) {
            updateHiddenBlocks(false)
        }
    }

    private fun updateHiddenBlocks(hidden: Boolean) {
        val world = level ?: return
        val facing = getOrientation().facingDirection
        for ((templatePos, flags) in smfShape.keyFlags) {
            if (!flags.hidden) {
                continue
            }
            val worldPos = ShapeMatcher.toWorldPos(worldPosition, facing, templatePos)
            val state = world.getBlockState(worldPos)
            val block = state.block
            if (block is HideableBlock) {
                val target = state.setValue(block.hiddenProperty, hidden)
                if (state !== target) {
                    world.setBlockAndUpdate(worldPos, target)
                }
            }
        }
        cachedHiddenMembers = null
        cachedHiddenVertices = null
        cachedResourceVersion = -1
    }
}

/**
 * One hidden member block: controller-relative offset, world position (for per-vertex AO/light),
 * block state, and the render layer its material uses.
 */
data class HiddenMember(
    val offset: IntArray,
    val worldPos: BlockPos,
    val state: BlockState,
    val renderType: RenderType
)

/**
 * One captured member: its material render layer and the baked per-vertex data
 * (13 floats per vertex: x,y,z, r,g,b,a, u,v, light, nx,ny,nz).
 */
data class CapturedMember(
    val renderType: RenderType,
    val data: List<FloatArray>
)