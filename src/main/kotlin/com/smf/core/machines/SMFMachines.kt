package com.smf.core.machines

import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes
import aztech.modern_industrialization.compat.rei.machines.SteamMode
import aztech.modern_industrialization.inventory.SlotPositions
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGui
import aztech.modern_industrialization.machines.guicomponents.ProgressBar
import aztech.modern_industrialization.machines.models.MachineCasings
import aztech.modern_industrialization.machines.recipe.MachineRecipeType
import aztech.modern_industrialization.util.Rectangle
import com.smf.core.SMFCore
import com.smf.core.blocks.SMFBlocks
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Predicate
import java.util.function.Supplier

object SMFMachines {
    val RECIPE_SERIALIZERS: DeferredRegister<RecipeSerializer<*>> =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, SMFCore.ID)
    val RECIPE_TYPES: DeferredRegister<RecipeType<*>> =
        DeferredRegister.create(Registries.RECIPE_TYPE, SMFCore.ID)

    val ELECTRIC_FRITTING_FURNACE_RECIPE_TYPE: MachineRecipeType =
        MachineRecipeType(id(ElectricFrittingFurnaceBlockEntity.ID))
            .withItemInputs()
            .withItemOutputs()

    val HYPER_PRESSURE_REACTOR_RECIPE_TYPE: MachineRecipeType =
        MachineRecipeType(id(HyperPressureReactorBlockEntity.ID))
            .withItemInputs()
            .withFluidInputs()
            .withItemOutputs()
            .withFluidOutputs()

    val LARGE_ELECTROLYZER_RECIPE_TYPE: MachineRecipeType =
        MachineRecipeType(id(LargeElectrolyzerBlockEntity.ID))
            .withItemInputs()
            .withFluidInputs()
            .withItemOutputs()
            .withFluidOutputs()

    val CRYOGENIC_FRACTIONATION_TOWER_RECIPE_TYPE: MachineRecipeType =
        MachineRecipeType(id(CryogenicFractionationTowerBlockEntity.ID))
            .withFluidInputs()
            .withFluidOutputs()

    val LARGE_CHEMICAL_BATH_RECIPE_TYPE: MachineRecipeType =
        MachineRecipeType(id(LargeChemicalBathBlockEntity.ID))
            .withItemInputs()
            .withFluidInputs()
            .withItemOutputs()
            .withFluidOutputs()

    val LARGE_AUTOCLAVE_RECIPE_TYPE: MachineRecipeType =
        MachineRecipeType(id(LargeAutoclaveBlockEntity.ID))
            .withFluidInputs()
            .withItemOutputs()
            .withFluidOutputs()

    val LARGE_VACUUM_FREEZER_RECIPE_TYPE: MachineRecipeType =
        MachineRecipeType(id(LargeVacuumFreezerBlockEntity.ID))
            .withItemInputs()
            .withFluidInputs()
            .withItemOutputs()
            .withFluidOutputs()

    val ROTATING_GLASS_REACTOR_RECIPE_TYPE: MachineRecipeType =
        MachineRecipeType(id(RotatingGlassReactorBlockEntity.ID))
            .withItemInputs()
            .withItemOutputs()
            .withFluidInputs()
            .withFluidOutputs()

    @Suppress("unused")
    val ELECTRIC_FRITTING_FURNACE_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, MachineRecipeType> =
        RECIPE_SERIALIZERS.register(ElectricFrittingFurnaceBlockEntity.ID, Supplier {
            ELECTRIC_FRITTING_FURNACE_RECIPE_TYPE
        })

    @Suppress("unused")
    val ELECTRIC_FRITTING_FURNACE_RECIPE_TYPE_HOLDER: DeferredHolder<RecipeType<*>, MachineRecipeType> =
        RECIPE_TYPES.register(ElectricFrittingFurnaceBlockEntity.ID, Supplier {
            ELECTRIC_FRITTING_FURNACE_RECIPE_TYPE
        })

    @Suppress("unused")
    val HYPER_PRESSURE_REACTOR_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, MachineRecipeType> =
        RECIPE_SERIALIZERS.register(HyperPressureReactorBlockEntity.ID, Supplier {
            HYPER_PRESSURE_REACTOR_RECIPE_TYPE
        })

    @Suppress("unused")
    val HYPER_PRESSURE_REACTOR_RECIPE_TYPE_HOLDER: DeferredHolder<RecipeType<*>, MachineRecipeType> =
        RECIPE_TYPES.register(HyperPressureReactorBlockEntity.ID, Supplier {
            HYPER_PRESSURE_REACTOR_RECIPE_TYPE
        })

    @Suppress("unused")
    val LARGE_ELECTROLYZER_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, MachineRecipeType> =
        RECIPE_SERIALIZERS.register(LargeElectrolyzerBlockEntity.ID, Supplier {
            LARGE_ELECTROLYZER_RECIPE_TYPE
        })

    @Suppress("unused")
    val LARGE_ELECTROLYZER_RECIPE_TYPE_HOLDER: DeferredHolder<RecipeType<*>, MachineRecipeType> =
        RECIPE_TYPES.register(LargeElectrolyzerBlockEntity.ID, Supplier {
            LARGE_ELECTROLYZER_RECIPE_TYPE
        })

    @Suppress("unused")
    val CRYOGENIC_FRACTIONATION_TOWER_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, MachineRecipeType> =
        RECIPE_SERIALIZERS.register(CryogenicFractionationTowerBlockEntity.ID, Supplier {
            CRYOGENIC_FRACTIONATION_TOWER_RECIPE_TYPE
        })

    @Suppress("unused")
    val CRYOGENIC_FRACTIONATION_TOWER_RECIPE_TYPE_HOLDER: DeferredHolder<RecipeType<*>, MachineRecipeType> =
        RECIPE_TYPES.register(CryogenicFractionationTowerBlockEntity.ID, Supplier {
            CRYOGENIC_FRACTIONATION_TOWER_RECIPE_TYPE
        })

    @Suppress("unused")
    val LARGE_CHEMICAL_BATH_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, MachineRecipeType> =
        RECIPE_SERIALIZERS.register(LargeChemicalBathBlockEntity.ID, Supplier {
            LARGE_CHEMICAL_BATH_RECIPE_TYPE
        })

    @Suppress("unused")
    val LARGE_CHEMICAL_BATH_RECIPE_TYPE_HOLDER: DeferredHolder<RecipeType<*>, MachineRecipeType> =
        RECIPE_TYPES.register(LargeChemicalBathBlockEntity.ID, Supplier {
            LARGE_CHEMICAL_BATH_RECIPE_TYPE
        })

    @Suppress("unused")
    val LARGE_AUTOCLAVE_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, MachineRecipeType> =
        RECIPE_SERIALIZERS.register(LargeAutoclaveBlockEntity.ID, Supplier {
            LARGE_AUTOCLAVE_RECIPE_TYPE
        })

@Suppress("unused")
    val LARGE_AUTOCLAVE_RECIPE_TYPE_HOLDER: DeferredHolder<RecipeType<*>, MachineRecipeType> =
        RECIPE_TYPES.register(LargeAutoclaveBlockEntity.ID, Supplier {
            LARGE_AUTOCLAVE_RECIPE_TYPE
        })

    @Suppress("unused")
    val LARGE_VACUUM_FREEZER_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, MachineRecipeType> =
        RECIPE_SERIALIZERS.register(LargeVacuumFreezerBlockEntity.ID, Supplier {
            LARGE_VACUUM_FREEZER_RECIPE_TYPE
        })

    @Suppress("unused")
    val LARGE_VACUUM_FREEZER_RECIPE_TYPE_HOLDER: DeferredHolder<RecipeType<*>, MachineRecipeType> =
        RECIPE_TYPES.register(LargeVacuumFreezerBlockEntity.ID, Supplier {
            LARGE_VACUUM_FREEZER_RECIPE_TYPE
        })

    @Suppress("unused")
    val ROTATING_GLASS_REACTOR_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, MachineRecipeType> =
        RECIPE_SERIALIZERS.register(RotatingGlassReactorBlockEntity.ID, Supplier {
            ROTATING_GLASS_REACTOR_RECIPE_TYPE
        })

    @Suppress("unused")
    val ROTATING_GLASS_REACTOR_RECIPE_TYPE_HOLDER: DeferredHolder<RecipeType<*>, MachineRecipeType> =
        RECIPE_TYPES.register(RotatingGlassReactorBlockEntity.ID, Supplier {
            ROTATING_GLASS_REACTOR_RECIPE_TYPE
        })

    private val craftingGuiArea = Rectangle(
        CraftingMultiblockGui.X,
        CraftingMultiblockGui.Y,
        CraftingMultiblockGui.W,
        CraftingMultiblockGui.H
    )
    private var initialized = false

    fun init() {
        if (initialized) {
            return
        }
        initialized = true

        val machineId = id(ElectricFrittingFurnaceBlockEntity.ID)
        ReiMachineRecipes.registerMultiblockShape(machineId, ElectricFrittingFurnaceBlockEntity.SHAPE)

        val categoryParams = MachineCategoryParams(
            "Electric Fritting Furnace",
            machineId,
            SlotPositions.Builder().addSlots(36, 35, 2, 2).build(),
            SlotPositions.Builder().addSlots(102, 35, 1, 2).build(),
            SlotPositions.empty(),
            SlotPositions.empty(),
            ProgressBar.Params(77, 42, "arrow"),
            ELECTRIC_FRITTING_FURNACE_RECIPE_TYPE,
            Predicate { true },
            true,
            SteamMode.ELECTRIC_ONLY
        )

        ReiMachineRecipes.registerCategory(machineId, categoryParams)
        ReiMachineRecipes.registerWorkstation(machineId, machineId)
        ReiMachineRecipes.registerRecipeCategoryForMachine(
            machineId,
            machineId,
            ReiMachineRecipes.MachineScreenPredicate.MULTIBLOCK
        )
        ReiMachineRecipes.registerMachineClickArea(machineId, craftingGuiArea)

        val reactorId = id(HyperPressureReactorBlockEntity.ID)
        ReiMachineRecipes.registerMultiblockShape(reactorId, HyperPressureReactorBlockEntity.SHAPE)

        val reactorCategoryParams = MachineCategoryParams(
            "Hyper Pressure Reactor",
            reactorId,
            SlotPositions.Builder().addSlots(30, 27, 3, 1).build(),
            SlotPositions.Builder().addSlots(116, 27, 3, 1).build(),
            SlotPositions.Builder().addSlots(30, 47, 3, 1).build(),
            SlotPositions.Builder().addSlots(116, 47, 3, 1).build(),
            ProgressBar.Params(88, 35, "triple_arrow"),
            HYPER_PRESSURE_REACTOR_RECIPE_TYPE,
            Predicate { true },
            true,
            SteamMode.ELECTRIC_ONLY
        )

        ReiMachineRecipes.registerCategory(reactorId, reactorCategoryParams)
        ReiMachineRecipes.registerWorkstation(reactorId, reactorId)
        ReiMachineRecipes.registerRecipeCategoryForMachine(
            reactorId,
            reactorId,
            ReiMachineRecipes.MachineScreenPredicate.MULTIBLOCK
        )
        ReiMachineRecipes.registerMachineClickArea(reactorId, craftingGuiArea)

        // Machine casing imitation so the large electrolyzer can use the nonconducting casing texture
        MachineCasings.createBlockImitation(
            SMFCore.id("nonconducting_casing"),
            SMFBlocks.NONCONDUCTING_CASING::get
        )

        val leId = id(LargeElectrolyzerBlockEntity.ID)
        ReiMachineRecipes.registerMultiblockShape(leId, LargeElectrolyzerBlockEntity.SHAPE)

        val leCategoryParams = MachineCategoryParams(
            "Large Electrolyzer",
            leId,
            SlotPositions.Builder().addSlots(36, 35, 1, 2).build(),
            SlotPositions.Builder().addSlots(102, 35, 2, 2).build(),
            SlotPositions.Builder().addSlots(56, 35, 1, 2).build(),
            SlotPositions.Builder().addSlots(140, 35, 2, 2).build(),
            ProgressBar.Params(77, 42, "arrow"),
            LARGE_ELECTROLYZER_RECIPE_TYPE,
            Predicate { true },
            true,
            SteamMode.ELECTRIC_ONLY
        )

        ReiMachineRecipes.registerCategory(leId, leCategoryParams)
        ReiMachineRecipes.registerWorkstation(leId, leId)
        ReiMachineRecipes.registerRecipeCategoryForMachine(
            leId,
            leId,
            ReiMachineRecipes.MachineScreenPredicate.MULTIBLOCK
        )
        ReiMachineRecipes.registerMachineClickArea(leId, craftingGuiArea)

        val cftId = id(CryogenicFractionationTowerBlockEntity.ID)
        CryogenicFractionationTowerBlockEntity.SHAPE_TEMPLATES.forEachIndexed { i, shape ->
            ReiMachineRecipes.registerMultiblockShape(cftId, shape, i.toString())
        }

        val cftCategoryParams = MachineCategoryParams(
            "Cryogenic Fractionation Tower",
            cftId,
            SlotPositions.empty(),
            SlotPositions.empty(),
            SlotPositions.Builder().addSlot(56, 35).build(),
            SlotPositions.Builder().addSlots(102, 35, 8, 1).build(),
            ProgressBar.Params(77, 33, "arrow"),
            CRYOGENIC_FRACTIONATION_TOWER_RECIPE_TYPE,
            Predicate { true },
            true,
            SteamMode.ELECTRIC_ONLY
        )

        ReiMachineRecipes.registerCategory(cftId, cftCategoryParams)
        ReiMachineRecipes.registerWorkstation(cftId, cftId)
        ReiMachineRecipes.registerRecipeCategoryForMachine(
            cftId,
            cftId,
            ReiMachineRecipes.MachineScreenPredicate.MULTIBLOCK
        )
        ReiMachineRecipes.registerMachineClickArea(cftId, craftingGuiArea)

        val lcbId = id(LargeChemicalBathBlockEntity.ID)
        ReiMachineRecipes.registerMultiblockShape(lcbId, LargeChemicalBathBlockEntity.SHAPE)

        // JEI layout mirrors MI's electrolyzer (1 item/fluid input, 2x2 outputs)
        val lcbCategoryParams = MachineCategoryParams(
            "Large Chemical Bath",
            lcbId,
            SlotPositions.Builder().addSlot(42, 27).build(),
            SlotPositions.Builder().addSlots(93, 27, 2, 2).build(),
            SlotPositions.Builder().addSlot(42, 47).build(),
            SlotPositions.Builder().addSlots(131, 27, 2, 2).build(),
            ProgressBar.Params(66, 35, "arrow"),
            LARGE_CHEMICAL_BATH_RECIPE_TYPE,
            Predicate { true },
            true,
            SteamMode.ELECTRIC_ONLY
        )

        ReiMachineRecipes.registerCategory(lcbId, lcbCategoryParams)
        ReiMachineRecipes.registerWorkstation(lcbId, lcbId)
        ReiMachineRecipes.registerRecipeCategoryForMachine(
            lcbId,
            lcbId,
            ReiMachineRecipes.MachineScreenPredicate.MULTIBLOCK
        )
        ReiMachineRecipes.registerMachineClickArea(lcbId, craftingGuiArea)

        val autoclaveId = id(LargeAutoclaveBlockEntity.ID)
        ReiMachineRecipes.registerMultiblockShape(autoclaveId, LargeAutoclaveBlockEntity.SHAPE)

        // JEI layout mirrors industrialization_overdrive:pyrolyse_oven
        val autoclaveCategoryParams = MachineCategoryParams(
            "Large Autoclave",
            autoclaveId,
            SlotPositions.Builder().addSlot(56, 35).build(),
            SlotPositions.Builder().addSlot(102, 35).build(),
            SlotPositions.Builder().addSlot(36, 35).build(),
            SlotPositions.Builder().addSlot(122, 35).build(),
            ProgressBar.Params(77, 33, "arrow"),
            LARGE_AUTOCLAVE_RECIPE_TYPE,
            Predicate { true },
            true,
            SteamMode.ELECTRIC_ONLY
        )

        ReiMachineRecipes.registerCategory(autoclaveId, autoclaveCategoryParams)
        ReiMachineRecipes.registerWorkstation(autoclaveId, autoclaveId)
        ReiMachineRecipes.registerRecipeCategoryForMachine(
            autoclaveId,
            autoclaveId,
            ReiMachineRecipes.MachineScreenPredicate.MULTIBLOCK
        )
        ReiMachineRecipes.registerMachineClickArea(autoclaveId, craftingGuiArea)

        val rotatingId = id(RotatingGlassReactorBlockEntity.ID)
        ReiMachineRecipes.registerMultiblockShape(rotatingId, RotatingGlassReactorBlockEntity.SHAPE.template)

        // JEI layout mirrors the large autoclave (1 item/fluid input, 1 item/fluid output)
        val rotatingCategoryParams = MachineCategoryParams(
            "Rotating Glass Reactor",
            rotatingId,
            SlotPositions.Builder().addSlot(42, 27).build(),
            SlotPositions.Builder().addSlot(93, 27).build(),
            SlotPositions.Builder().addSlot(42, 47).build(),
            SlotPositions.Builder().addSlot(93, 47).build(),
            ProgressBar.Params(66, 35, "arrow"),
            ROTATING_GLASS_REACTOR_RECIPE_TYPE,
            Predicate { true },
            true,
            SteamMode.ELECTRIC_ONLY
        )

        ReiMachineRecipes.registerCategory(rotatingId, rotatingCategoryParams)
        ReiMachineRecipes.registerWorkstation(rotatingId, rotatingId)
        ReiMachineRecipes.registerRecipeCategoryForMachine(
            rotatingId,
            rotatingId,
            ReiMachineRecipes.MachineScreenPredicate.MULTIBLOCK
        )
        ReiMachineRecipes.registerMachineClickArea(rotatingId, craftingGuiArea)

        val vfId = id(LargeVacuumFreezerBlockEntity.ID)
        ReiMachineRecipes.registerMultiblockShape(vfId, LargeVacuumFreezerBlockEntity.SHAPE)

        val vfCategoryParams = MachineCategoryParams(
            "Large Vacuum Freezer",
            vfId,
            SlotPositions.Builder().addSlot(56, 35).build(),
            SlotPositions.Builder().addSlot(102, 35).build(),
            SlotPositions.Builder().addSlot(36, 35).build(),
            SlotPositions.Builder().addSlot(122, 35).build(),
            ProgressBar.Params(77, 33, "arrow"),
            LARGE_VACUUM_FREEZER_RECIPE_TYPE,
            Predicate { true },
            true,
            SteamMode.ELECTRIC_ONLY
        )

        ReiMachineRecipes.registerCategory(vfId, vfCategoryParams)
        ReiMachineRecipes.registerWorkstation(vfId, vfId)
        ReiMachineRecipes.registerRecipeCategoryForMachine(
            vfId,
            vfId,
            ReiMachineRecipes.MachineScreenPredicate.MULTIBLOCK
        )
        ReiMachineRecipes.registerMachineClickArea(vfId, craftingGuiArea)

        SMFGasGenerator.init()
    }

    private fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(SMFCore.ID, path)
}
