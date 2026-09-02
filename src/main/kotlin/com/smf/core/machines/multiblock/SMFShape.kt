package com.smf.core.machines.multiblock

import aztech.modern_industrialization.machines.models.MachineCasing
import aztech.modern_industrialization.machines.multiblocks.HatchFlags
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate
import aztech.modern_industrialization.machines.multiblocks.SimpleMember
import net.minecraft.core.BlockPos

/**
 * An MI [ShapeTemplate] plus SMF-specific per-key [SMFKeyFlags], mapped to template coordinates.
 *
 * Use [Builder] exactly like MI's `ShapeTemplate.LayeredBuilder` (same layer/row/column layout and
 * same controller `#` marker), but you can also attach SMF flags to each key. The flags map is in
 * template space (relative to the controller); convert to world space with
 * `ShapeMatcher.toWorldPos(controllerPos, facing, templatePos)`.
 */
class SMFShape(
    val template: ShapeTemplate,
    val keyFlags: Map<BlockPos, SMFKeyFlags>
) {
    /**
     * Builder that mirrors MI's layered-shape syntax and additionally records SMF key flags.
     *
     * Layers are `String[][]`; each layer is a row-major grid, layer 0 is the top and the last layer
     * holds the controller `#`. Member coordinates map to template space exactly like MI:
     * `x = column - controllerColumn`, `y = row - controllerRow`, `z = -(layer - controllerLayer)`.
     */
    class Builder(
        hatchCasing: MachineCasing,
        private val layers: Array<Array<String>>
    ) {
        private val innerBuilder = ShapeTemplate.Builder(hatchCasing)
        private val missingKeys = HashSet<Char>()
        private val keyDefinitions = HashMap<Char, KeyDefinition>()
        private var iController = 0
        private var jController = 0
        private var kController = 0

        private data class KeyDefinition(
            val member: SimpleMember,
            val hatchFlags: HatchFlags?,
            val flags: SMFKeyFlags
        )

        init {
            if (layers.isEmpty()) {
                throw IllegalArgumentException("No layers provided")
            }
            val dim1 = layers.size
            val dim2 = layers[0].size
            val dim3 = layers[0][0].length
            if (dim2 == 0 || dim3 == 0) {
                throw IllegalArgumentException("Layer 0 cannot have size 0")
            }

            var foundController = false
            for (i in 0 until dim1) {
                if (layers[i].size != dim2) {
                    throw IllegalArgumentException("Layer $i has invalid size, expected $dim2")
                }
                for (j in 0 until dim2) {
                    if (layers[i][j].length != dim3) {
                        throw IllegalArgumentException("Layer $i entry $j has invalid size, expected $dim3")
                    }
                    for (k in 0 until dim3) {
                        when (val c = layers[i][j][k]) {
                            '#' -> {
                                if (foundController) {
                                    throw IllegalArgumentException("Multiple controllers found (character #)")
                                }
                                foundController = true
                                iController = i
                                jController = j
                                kController = k
                            }
                            ' ' -> Unit
                            else -> missingKeys.add(c)
                        }
                    }
                }
            }
        }

        /**
         * Registers a member for the given key character, with MI hatch flags and optional SMF flags.
         */
        fun key(char: Char, member: SimpleMember, hatchFlags: HatchFlags?, flags: SMFKeyFlags = SMFKeyFlags.NONE): Builder {
            if (keyDefinitions.containsKey(char)) {
                throw IllegalArgumentException("Key '$char' was already defined")
            }
            if (!missingKeys.contains(char)) {
                throw IllegalArgumentException("Key '$char' is not part of the shape layers")
            }
            missingKeys.remove(char)
            keyDefinitions[char] = KeyDefinition(member, hatchFlags, flags)
            return this
        }

        /** Convenience overload without hatch flags (equivalent to `HatchFlags.NO_HATCH`). */
        fun key(char: Char, member: SimpleMember, flags: SMFKeyFlags): Builder = key(char, member, null, flags)

        fun build(): SMFShape {
            if (missingKeys.isNotEmpty()) {
                throw IllegalArgumentException("Missing keys: $missingKeys")
            }

            val flagsByPos = HashMap<BlockPos, SMFKeyFlags>()
            for (i in layers.indices) {
                for (j in layers[i].indices) {
                    for (k in layers[i][j].indices) {
                        val c = layers[i][j][k]
                        if (c != ' ' && c != '#') {
                            val def = keyDefinitions[c] ?: continue
                            val x = k - kController
                            val y = j - jController
                            val z = -(i - iController)
                            innerBuilder.add(x, y, z, def.member, def.hatchFlags)
                            if (!def.flags.isEmpty) {
                                flagsByPos[BlockPos(x, y, z)] = def.flags
                            }
                        }
                    }
                }
            }
            return SMFShape(innerBuilder.build(), flagsByPos)
        }
    }
}