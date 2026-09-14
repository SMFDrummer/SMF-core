package com.smf.core.client.render

import com.mojang.blaze3d.vertex.VertexConsumer

/**
 * Captures the geometry emitted by `ModelBlockRenderer.tesselateBlock` as raw data, so the
 * renderer can replay it every frame with only a matrix transform.
 *
 * Only geometry is captured — positions (controller-relative, i.e. after the pose passed to
 * `tesselateBlock`, whose normals are untransformed because that pose only translates), UVs and
 * normals. The per-vertex colour (ambient occlusion + face shade) and packed light that vanilla
 * computes during tessellation are *deliberately dropped*: they are baked against the structure's
 * assembled orientation, so replaying them after a rotation leaves every face that used to be
 * occluded (e.g. the one against the ground) pitch black. The renderer recomputes both from the
 * world every frame instead.
 *
 * Layout: 8 floats per vertex: x,y,z, u,v, nx,ny,nz.
 */
class CapturingVertexConsumer : VertexConsumer {
    val data = ArrayList<FloatArray>(1024)

    private var px = 0.0f
    private var py = 0.0f
    private var pz = 0.0f
    private var tu = 0.0f
    private var tv = 0.0f
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

    /** Ignored: the renderer lights and shades every frame instead of replaying baked values. */
    override fun setColor(red: Int, green: Int, blue: Int, alpha: Int): VertexConsumer = this

    override fun setUv(u: Float, v: Float): VertexConsumer {
        tu = u
        tv = v
        return this
    }

    override fun setUv1(u: Int, v: Int): VertexConsumer = this

    /** Ignored: packed light is recomputed per frame from the rotated world position. */
    override fun setUv2(u: Int, v: Int): VertexConsumer = this

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
            data.add(floatArrayOf(px, py, pz, tu, tv, nx, ny, nz))
            pending = false
        }
    }
}
