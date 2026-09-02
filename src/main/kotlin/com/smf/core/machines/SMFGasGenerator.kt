package com.smf.core.machines

import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes
import com.smf.core.SMFCore
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.material.Fluid

/**
 * Initialization for the Large Gas Generator: registers the REI/JEI multiblock shape
 * preview. The controller casing is MI's clean stainless steel machine casing (same as
 * the distillation tower), so no custom casing block is needed.
 */
object SMFGasGenerator {
    // Keep this a plain string (not referencing the Java class) so that touching this
    // object does not trigger LargeGasGeneratorBlockEntity's static SHAPE build too early.
    const val ID = "large_gas_generator"

    private var initialized = false

    fun init() {
        if (initialized) {
            return
        }
        initialized = true

        ReiMachineRecipes.registerMultiblockShape(SMFCore.id(ID), LargeGasGeneratorBlockEntity.SHAPE)
    }

    /** Fluids the large gas generator can burn, for the JEI "Liquefied Gas Fuels" page. */
    fun fuelFluids(): List<Fluid> {
        return LargeGasGeneratorBlockEntity.FUEL_EU.keys.map { BuiltInRegistries.FLUID.get(it) }
    }
}