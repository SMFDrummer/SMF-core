package com.smf.core.fluids

import aztech.modern_industrialization.definition.FluidDefinition
import aztech.modern_industrialization.definition.FluidTexture
import com.smf.core.SMFCore
import com.smf.core.blocks.SMFBlocks
import com.smf.core.items.SMFItems
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.material.Fluid
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import net.swedz.tesseract.neoforge.registry.MIFluidProperties
import net.swedz.tesseract.neoforge.registry.SortOrder
import net.swedz.tesseract.neoforge.registry.holder.MIFluidHolder

object SMFFluids {
    val FLUIDS: DeferredRegister<Fluid> = DeferredRegister.create(Registries.FLUID, SMFCore.ID)
    val FLUID_TYPES: DeferredRegister<FluidType> =
        DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, SMFCore.ID)

    // Helium (0xffe6e485) + Methane (0xffb740d9) blended color
    private const val HELIUM_NATURAL_GAS_COLOR: Int = 0xce92af

    // Pale yellow, like a real sodium aluminate solution
    private const val SODIUM_ALUMINATE_COLOR: Int = 0xe8dc8f

    val HELIUM_NATURAL_GAS: MIFluidHolder = create(
        "helium_natural_gas",
        "Helium Natural Gas",
        HELIUM_NATURAL_GAS_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        true
    ).register()

    val SODIUM_ALUMINATE: MIFluidHolder = create(
        "sodium_aluminate",
        "Sodium Aluminate",
        SODIUM_ALUMINATE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Salt water: slightly whiter than plain water
    private const val SALT_WATER_COLOR: Int = 0x9fbee8

    val SALT_WATER: MIFluidHolder = create(
        "salt_water",
        "Salt Water",
        SALT_WATER_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Hydrofluoric acid, GTCEu color 0x0088AA
    private const val HYDROFLUORIC_ACID_COLOR: Int = 0x0088aa

    val HYDROFLUORIC_ACID: MIFluidHolder = create(
        "hydrofluoric_acid",
        "Hydrofluoric Acid",
        HYDROFLUORIC_ACID_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Molten cryolite: lava-like, light grey-pink (0x978f8f)
    private const val MOLTEN_CRYOLITE_COLOR: Int = 0x978f8f

    val MOLTEN_CRYOLITE: MIFluidHolder = create(
        "molten_cryolite",
        "Molten Cryolite",
        MOLTEN_CRYOLITE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.LAVA_LIKE,
        false
    ).register()

    // Molten potassium chloride: lava-like, KCl dust palette (0x564133)
    private const val MOLTEN_POTASSIUM_CHLORIDE_COLOR: Int = 0x564133

    val MOLTEN_POTASSIUM_CHLORIDE: MIFluidHolder = create(
        "molten_potassium_chloride",
        "Molten Potassium Chloride",
        MOLTEN_POTASSIUM_CHLORIDE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.LAVA_LIKE,
        false
    ).register()

    // Trisodium phosphate solution: colorless in reality, so a pale near-white tint
    private const val TRISODIUM_PHOSPHATE_COLOR: Int = 0xeef3f5

    val TRISODIUM_PHOSPHATE: MIFluidHolder = create(
        "trisodium_phosphate",
        "Trisodium Phosphate",
        TRISODIUM_PHOSPHATE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Rare earth chloride solution, GTCEu RareEarth palette (0xffdc88)
    private const val RARE_EARTH_CHLORIDE_COLOR: Int = 0xffdc88

    val RARE_EARTH_CHLORIDE: MIFluidHolder = create(
        "rare_earth_chloride",
        "Rare Earth Chloride",
        RARE_EARTH_CHLORIDE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Magnesium chloride solution, near-colorless gray-white (reference 83922)
    private const val MAGNESIUM_CHLORIDE_COLOR: Int = 0xa8a8b3

    val MAGNESIUM_CHLORIDE: MIFluidHolder = create(
        "magnesium_chloride",
        "Magnesium Chloride",
        MAGNESIUM_CHLORIDE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Phenol, deep brown (GTCEu phenol bucket inner color)
    private const val PHENOL_COLOR: Int = 0x5d3820

    val PHENOL: MIFluidHolder = create(
        "phenol",
        "Phenol",
        PHENOL_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Acetone, colorless transparent grey-white (GTCEu acetone bucket inner color)
    private const val ACETONE_COLOR: Int = 0x898989

    val ACETONE: MIFluidHolder = create(
        "acetone",
        "Acetone",
        ACETONE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Sodium hypochlorite (bleach), bright green (reference 912331)
    private const val SODIUM_HYPOCHLORITE_COLOR: Int = 0x4aaf37

    val SODIUM_HYPOCHLORITE: MIFluidHolder = create(
        "sodium_hypochlorite",
        "Sodium Hypochlorite",
        SODIUM_HYPOCHLORITE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Chloroform, purple-red (GTCEu chloroform bucket inner color)
    private const val CHLOROFORM_COLOR: Int = 0x79338a

    val CHLOROFORM: MIFluidHolder = create(
        "chloroform",
        "Chloroform",
        CHLOROFORM_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Sodium acetate, bright green (reference 698366)
    private const val SODIUM_ACETATE_COLOR: Int = 0x4cea13

    val SODIUM_ACETATE: MIFluidHolder = create(
        "sodium_acetate",
        "Sodium Acetate",
        SODIUM_ACETATE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Tetrafluoroethylene, colorless transparent grey-white (GTCEu bucket inner color)
    private const val TETRAFLUOROETHYLENE_COLOR: Int = 0xa0a0a0

    val TETRAFLUOROETHYLENE: MIFluidHolder = create(
        "tetrafluoroethylene",
        "Tetrafluoroethylene",
        TETRAFLUOROETHYLENE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Mixed metal chlorides, off-white (GTCEu iron II chloride bucket inner color)
    private const val METAL_CHLORIDE_COLOR: Int = 0xbcb79d

    val METAL_CHLORIDE: MIFluidHolder = create(
        "metal_chloride",
        "Metal Chloride",
        METAL_CHLORIDE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Titanium tetrachloride, grey-blue (GTCEu bucket inner color)
    private const val TITANIUM_TETRACHLORIDE_COLOR: Int = 0x7e9393

    val TITANIUM_TETRACHLORIDE: MIFluidHolder = create(
        "titanium_tetrachloride",
        "Titanium Tetrachloride",
        TITANIUM_TETRACHLORIDE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Ammonia, near-colorless with a pale blue tint (GTCEu bucket inner color)
    private const val AMMONIA_COLOR: Int = 0x9ba8ae

    val AMMONIA: MIFluidHolder = create(
        "ammonia",
        "Ammonia",
        AMMONIA_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Ammonium sulfate, bright blue (reference 666015)
    private const val AMMONIUM_SULFATE_COLOR: Int = 0x4646f3

    val AMMONIUM_SULFATE: MIFluidHolder = create(
        "ammonium_sulfate",
        "Ammonium Sulfate",
        AMMONIUM_SULFATE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Ammonium persulfate, bright blue slightly deeper (reference 700737)
    private const val AMMONIUM_PERSULFATE_COLOR: Int = 0x4646f3

    val AMMONIUM_PERSULFATE: MIFluidHolder = create(
        "ammonium_persulfate",
        "Ammonium Persulfate",
        AMMONIUM_PERSULFATE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.WATER_LIKE,
        false
    ).register()

    // Polytetrafluoroethylene (PTFE), lava-like grey-white plastic (GTCEu 0x6e6e6e)
    private const val POLYTETRAFLUOROETHYLENE_COLOR: Int = 0x6e6e6e

    val POLYTETRAFLUOROETHYLENE: MIFluidHolder = create(
        "polytetrafluoroethylene",
        "Polytetrafluoroethylene",
        POLYTETRAFLUOROETHYLENE_COLOR,
        FluidDefinition.LOW_OPACITY,
        FluidTexture.LAVA_LIKE,
        false
    ).register()

    private fun create(
        id: String,
        englishName: String,
        color: Int,
        opacity: Int,
        texture: FluidTexture,
        isGas: Boolean
    ): MIFluidHolder {
        return MIFluidHolder(
            SMFCore.id(id),
            englishName,
            FLUIDS,
            FLUID_TYPES,
            SMFBlocks.REGISTRY,
            SMFItems.REGISTRY,
            SortOrder.UNSORTED,
            MIFluidProperties(color, opacity, texture, isGas)
        )
    }

    fun init(bus: IEventBus) {
        FLUIDS.register(bus)
        FLUID_TYPES.register(bus)
    }
}
