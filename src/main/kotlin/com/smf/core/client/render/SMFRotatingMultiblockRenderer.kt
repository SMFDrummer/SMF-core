package com.smf.core.client.render

import aztech.modern_industrialization.client.machines.multiblocks.MultiblockMachineBER
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis

import com.smf.core.machines.multiblock.CapturedMember
import com.smf.core.machines.multiblock.SMFMultiblockBlockEntity
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.level.LightLayer
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.abs

/**
 * Create-style rotating multiblock renderer for every SMF machine.
 *
 * - Extends MI's `MultiblockMachineBER`, so the wrench shape-preview, hatch-placement overlays
 *   and the machine's active overlay are preserved untouched.
 * - The hidden members (keys flagged [com.smf.core.machines.multiblock.SMFKeyFlag.HIDDEN]) have
 *   their *geometry* tessellated ONCE per assembly and replayed every frame with only a rotation
 *   matrix (Create's SuperByteBuffer pattern).
 * - Lighting is deliberately NOT baked. A baked per-vertex light/AO is frozen to the assembled
 *   orientation, so every face that used to be occluded (rotated away from a neighbour, e.g. the
 *   one sitting on the ground) would replay pitch black. Instead the packed light is looked up
 *   from each face's *rotated* world position and the shading from its *rotated* normal, so the
 *   animated structure follows the dimension's real, dynamic lighting while it spins.
 * - The angle is integrated per frame (`renderAngle += renderSpeed * dt`, Create's
 *   MechanicalBearing pattern): idle = still, run = smooth acceleration, recipe end = natural
 *   deceleration that stops in place. Never reverses, cascaded machines are independent.
 */
class SMFRotatingMultiblockRenderer(context: BlockEntityRendererProvider.Context) : MultiblockMachineBER(context) {

    override fun render(be: MultiblockMachineBlockEntity, tickDelta: Float, poseStack: PoseStack, bufferSource: MultiBufferSource, light: Int, overlay: Int) {
        super.render(be, tickDelta, poseStack, bufferSource, light, overlay)
        if (be is SMFMultiblockBlockEntity) {
            renderRotatingStructure(be, tickDelta, poseStack, bufferSource, light, overlay)
        }
    }

    private fun renderRotatingStructure(be: SMFMultiblockBlockEntity, tickDelta: Float, poseStack: PoseStack, bufferSource: MultiBufferSource, light: Int, overlay: Int) {
        val level = be.level ?: return
        // Double precision: game tick counts grow unboundedly and float would lose the sub-tick
        // frame deltas, making the integrated angle step instead of advancing smoothly.
        val renderTime = level.gameTime.toDouble() + tickDelta
        val valid = be.isShapeValid

        // Real re-assembly detection (debounced): only reset the rotation when the shape was
        // actually observed invalid long enough, so MI's shapeValid flapping and the first render
        // of an already-assembled machine never snap the angle.
        if (valid) {
            if (!be.wasShapeValid) {
                if (be.shapeInvalidSinceTick >= 0.0f
                    && renderTime - be.shapeInvalidSinceTick >= be.getReassemblyResetThresholdTicks()
                ) {
                    be.renderAngle = 0.0
                    be.renderSpeed = 0.0f
                    be.invalidateHiddenMembers()
                }
            }
            be.shapeInvalidSinceTick = -1.0f
        } else {
            if (be.shapeInvalidSinceTick < 0.0f) {
                be.shapeInvalidSinceTick = renderTime.toFloat()
            }
        }
        be.wasShapeValid = valid

        // The animated model replaces the hidden real blocks, so it is only shown once the
        // multiblock is fully assembled.
        if (!valid) {
            return
        }

        val captured = be.getOrBakeHiddenVertices(level)
        if (captured == null || captured.isEmpty()) {
            return
        }

        // Speed easing: run -> accelerate to target, idle -> decelerate to 0.
        val target = if (be.getMachineModelData().isActive) be.getRunSpeedDegPerTick() else 0.0f
        be.renderSpeed += (target - be.renderSpeed) * be.getSpeedSmoothing()

        // Angle integration (Create's MechanicalBearing pattern): the angle only ever increases,
        // so easing the speed down to 0 stops the structure in place instead of unwinding the
        // accumulated revolutions.
        val dt = if (be.lastRenderTime < 0.0) 0.0 else (renderTime - be.lastRenderTime).coerceIn(0.0, 2.0)
        be.lastRenderTime = renderTime
        be.renderAngle = (be.renderAngle + be.renderSpeed * dt) % 360.0
        val angle = be.renderAngle.toFloat()

        val facing = be.getOrientation().facingDirection

        // Group captured members by their material's render layer.
        val byRenderType = LinkedHashMap<RenderType, MutableList<CapturedMember>>()
        for (member in captured) {
            byRenderType.getOrPut(member.renderType) { ArrayList() }.add(member)
        }

        // Rotation around the machine's front-back axis through the controller's center. The same
        // rotation is pushed as the pose and kept as a matrix, so that a vertex can also be mapped
        // to its rotated world position for the per-frame light lookup below.
        val rotationQuat = if (facing.axis == Direction.Axis.X) {
            Axis.XP.rotationDegrees(angle)
        } else {
            Axis.ZP.rotationDegrees(angle)
        }
        val rotation = Matrix4f()
            .translate(0.5f, 0.5f, 0.5f)
            .rotate(rotationQuat)
            .translate(-0.5f, -0.5f, -0.5f)

        poseStack.pushPose()
        poseStack.translate(0.5, 0.5, 0.5)
        poseStack.mulPose(rotationQuat)
        poseStack.translate(-0.5, -0.5, -0.5)

        val pose = poseStack.last()
        val poseMatrix = pose.pose()
        val normalMatrix = Matrix3f(pose.normal())
        val normal = Vector3f()
        val center = Vector3f()
        val scratch = Vector3f()
        val origin = be.blockPos

        // Replay the captured geometry, lighting and shading it from the world as it rotates.
        for ((renderType, group) in byRenderType) {
            val buffer = bufferSource.getBuffer(renderType)
            for (member in group) {
                val data = member.data
                var index = 0
                while (index + 3 < data.size) {
                    // One quad: 4 consecutive vertices written by tesselateBlock. Vanilla uses a
                    // single light value per face, so one lookup per quad is enough.
                    center.set(0.0f, 0.0f, 0.0f)
                    for (k in 0 until 4) {
                        val v = data[index + k]
                        scratch.set(v[0], v[1], v[2])
                        rotation.transformPosition(scratch)
                        center.add(scratch)
                    }
                    center.mul(0.25f)
                    val lightPos = BlockPos(
                        origin.x + Mth.floor(center.x),
                        origin.y + Mth.floor(center.y),
                        origin.z + Mth.floor(center.z),
                    )
                    val quadLight = if (level.isLoaded(lightPos)) {
                        LightTexture.pack(
                            level.getBrightness(LightLayer.BLOCK, lightPos),
                            level.getBrightness(LightLayer.SKY, lightPos),
                        )
                    } else {
                        // Rotated outside the loaded area: fall back to the controller's own light.
                        light
                    }

                    for (k in 0 until 4) {
                        val v = data[index + k]
                        normal.set(v[5], v[6], v[7])
                        normalMatrix.transform(normal)
                        val shade = diffuseShade(normal.x, normal.y, normal.z)
                        buffer.addVertex(poseMatrix, v[0], v[1], v[2])
                            .setColor(shade, shade, shade, 1.0f)
                            .setUv(v[3], v[4])
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(quadLight)
                            .setNormal(normal.x, normal.y, normal.z)
                    }
                    index += 4
                }
            }
        }
        poseStack.popPose()
    }

    /**
     * Vanilla block face shading (`DiffuseLighting`), driven by the *rotated* normal so the
     * shading follows the structure while it spins instead of staying frozen to its assembled
     * orientation.
     */
    private fun diffuseShade(nx: Float, ny: Float, nz: Float): Float {
        val ax = abs(nx)
        val ay = abs(ny)
        val az = abs(nz)
        return when {
            ay >= ax && ay >= az -> if (ny > 0.0f) 1.0f else 0.5f
            ax >= az -> 0.6f
            else -> 0.8f
        }
    }
}
