package com.smf.core.machines.multiblock

/**
 * Flags that can be attached to a shape key, mirroring MI's [aztech.modern_industrialization.machines.multiblocks.HatchFlags]
 * concept but for SMF-specific machine behaviour.
 */
enum class SMFKeyFlag {
    /**
     * The blocks placed for this key are visually hidden while the multiblock is fully assembled
     * (the blocks stay in place and keep validating the shape). The hidden blocks must be
     * [HideableBlock]s. The machine's renderer is expected to draw their animated replacement.
     */
    HIDDEN
}

/**
 * Immutable set of [SMFKeyFlag]s for one shape key. Use [of] to build, [NONE] for no flags.
 */
class SMFKeyFlags internal constructor(private val bits: Int) {
    val isEmpty: Boolean get() = bits == 0

    /** Whether the blocks of this key should be hidden while the structure is assembled. */
    val hidden: Boolean get() = (bits and HIDDEN_BIT) != 0

    override fun equals(other: Any?): Boolean = other is SMFKeyFlags && other.bits == bits

    override fun hashCode(): Int = bits

    override fun toString(): String {
        val flags = mutableListOf<String>()
        if (hidden) flags.add("HIDDEN")
        return flags.joinToString("|", prefix = "SMFKeyFlags(", postfix = ")").ifEmpty { "SMFKeyFlags(NONE)" }
    }

    companion object {
        private const val HIDDEN_BIT = 1

        /** No flags: the key behaves like a plain MI member. */
        val NONE = SMFKeyFlags(0)

        /** Builds a flag set from the given flags (duplicates are ignored). */
        fun of(vararg flags: SMFKeyFlag): SMFKeyFlags {
            var bits = 0
            for (flag in flags) {
                bits = when (flag) {
                    SMFKeyFlag.HIDDEN -> bits or HIDDEN_BIT
                }
            }
            return SMFKeyFlags(bits)
        }
    }
}