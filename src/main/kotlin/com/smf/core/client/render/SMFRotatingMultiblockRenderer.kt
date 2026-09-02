package com.smf.core.client.render

import aztech.modern_industrialization.client.machines.multiblocks.MultiblockMachineBER
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis

import com.smf.core.machines.multiblock.CapturedMember
import com.smf.core.machines.multiblock.SMFMultiblockBlockEntity
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import org.joml.Matrix3f
import org.joml.Vector3f

/**
 * Create-style rotating multiblock renderer for every SMF machine.
 *
 * - Extends MI's `MultiblockMachineBER`, so the wrench shape-preview, hatch-placement overlays
 *   and the machine's active overlay are preserved untouched.
 * - The hidden members (keys flagged [com.smf.core.machines.multiblock.SMFKeyFlag.HIDDEN]) are
 *   tessellated ONCE per assembly (capturing per-vertex AO and lighting) and replayed every frame
 *   with only a rotation matrix — no AO/lookup work per frame (Create's SuperByteBuffer pattern).
 *   Per-vertex AO and lighting therefore match the real blocks exactly.
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

        poseStack.pushPose()
        // Rotate around the machine's front-back axis through the controller's center.
        poseStack.translate(0.5, 0.5, 0.5)
        if (facing.axis == Direction.Axis.X) {
            poseStack.mulPose(Axis.XP.rotationDegrees(angle))
        } else {
            poseStack.mulPose(Axis.ZP.rotationDegrees(angle))
        }
        poseStack.translate(-0.5, -0.5, -0.5)

        val pose = poseStack.last()
        val poseMatrix = pose.pose()
        val normalMatrix = Matrix3f(pose.normal())
        val normal = Vector3f()

        // Replay the baked vertices (bake once, transform every frame).
        for ((renderType, group) in byRenderType) {
            val buffer = bufferSource.getBuffer(renderType)
            for (member in group) {
                for (v in member.data) {
                    normal.set(v[10], v[11], v[12])
                    normalMatrix.transform(normal)
                    buffer.addVertex(poseMatrix, v[0], v[1], v[2])
                        .setColor(v[3], v[4], v[5], v[6])
                        .setUv(v[7], v[8])
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(v[9].toInt())
                        .setNormal(normal.x, normal.y, normal.z)
                }
            }
        }
        poseStack.popPose()
    }
}