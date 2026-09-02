package com.smf.core.client.render

import com.mojang.blaze3d.vertex.VertexConsumer

/**
 * Captures vertices emitted by `ModelBlockRenderer.tesselateBlock` (which computes per-vertex
 * ambient occlusion and lighting) as raw data, so the renderer can replay them every frame with
 * only a matrix transform — the same bake-once/transform-every-frame pattern Create's
 * SuperByteBuffer uses. Positions are captured after the pose passed to `tesselateBlock`
 * (controller-relative); normals are captured untransformed (the bake pose only translates).
 *
 * Layout: 13 floats per vertex: x,y,z, r,g,b,a, u,v, light, nx,ny,nz (light stored as a float).
 */
class CapturingVertexConsumer : VertexConsumer {
    val data = ArrayList<FloatArray>(1024)

    private var px = 0.0f
    private var py = 0.0f
    private var pz = 0.0f
    private var cr = 1.0f
    private var cg = 1.0f
    private var cb = 1.0f
    private var ca = 1.0f
    private var tu = 0.0f
    private var tv = 0.0f
    private var light = 0
    private var nx = 0.0f
    private var ny = 0.0f
    private var nz = 0.0f
    private var pending = false

    override fun addVertex(x: Float, y: Float, z: Float): VertexConsumer {
        // A new vertex begins: flush the previous one, which was completed by its trailing setNormal.
        flush()
        px = x
        py = y
        pz = z
        pending = true
        return this
    }

    override fun setColor(red: Int, green: Int, blue: Int, alpha: Int): VertexConsumer {
        cr = red / 255.0f
        cg = green / 255.0f
        cb = blue / 255.0f
        ca = alpha / 255.0f
        return this
    }

    override fun setUv(u: Float, v: Float): VertexConsumer {
        tu = u
        tv = v
        return this
    }

    override fun setUv1(u: Int, v: Int): VertexConsumer = this

    override fun setUv2(u: Int, v: Int): VertexConsumer {
        // Packed light: setLight(packed) calls setUv2(packed & 0xFFFF, packed >> 16 & 0xFFFF).
        light = u or (v shl 16)
        return this
    }

    override fun setNormal(x: Float, y: Float, z: Float): VertexConsumer {
        nx = x
        ny = y
        nz = z
        // setNormal is the final element of each vertex in the block pipeline: commit it now.
        flush()
        return this
    }

    private fun flush() {
        if (pending) {
            data.add(floatArrayOf(px, py, pz, cr, cg, cb, ca, tu, tv, light.toFloat(), nx, ny, nz))
            pending = false
        }
    }
}