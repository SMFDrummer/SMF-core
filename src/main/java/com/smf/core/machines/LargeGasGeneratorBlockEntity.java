package com.smf.core.machines;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.ActiveShapeComponent;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import aztech.modern_industrialization.machines.components.FluidItemConsumerComponent;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.MultiblockInventoryComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.RedstoneControlComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.GeneratorMultiblockGui;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.Tickable;
import com.smf.core.SMFCore;
import com.smf.core.blocks.SMFBlocks;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * Multiblock generator logic copied from MI's GeneratorMultiblockBlockEntity, but with a
 * correct smfcore namespace ResourceLocation for the GUI title (the MI helper hard-codes
 * the MI namespace), plus custom tooltips for the "Liquefied Gas Fuels" generator.
 */
public class LargeGasGeneratorBlockEntity extends MultiblockMachineBlockEntity implements Tickable,
        EnergyListComponentHolder, MultiblockInventoryComponentHolder {

    public static final String ID = "large_gas_generator";
    public static final long MAX_EU = 8192L;

    // EU per mB, scaled from real volumetric heat values to MI's diesel = 400 EU/mB
    // (36.3 MJ/L), rounded UP to multiples of 16. Not based on GTCEu values.
    public static final long METHANE_EU_PER_MB = 240L;        // LNG 21.0 MJ/L
    public static final long ACETYLENE_EU_PER_MB = 336L;      // 29.9 MJ/L
    public static final long ETHYLENE_EU_PER_MB = 320L;       // 28.7 MJ/L
    public static final long PROPENE_EU_PER_MB = 336L;        // 29.8 MJ/L
    public static final long BUTADIENE_EU_PER_MB = 320L;      // 28.2 MJ/L
    public static final long BENZENE_EU_PER_MB = 416L;        // 36.8 MJ/L
    public static final long TOLUENE_EU_PER_MB = 416L;        // 36.9 MJ/L
    public static final long ETHYLBENZENE_EU_PER_MB = 416L;   // 37.2 MJ/L
    public static final long STYRENE_EU_PER_MB = 432L;        // 38.0 MJ/L
    public static final long DIETHYL_ETHER_EU_PER_MB = 272L;  // 24.0 MJ/L
    public static final long ETHANOL_EU_PER_MB = 272L;        // 23.5 MJ/L
    public static final long HELIUM_NATURAL_GAS_EU_PER_MB = METHANE_EU_PER_MB; // ~99% methane

    /** Fluid id -> EU per mB for the liquefied gas fuels table (insertion order preserved). */
    public static final Map<ResourceLocation, Long> FUEL_EU = buildFuelMap();

    public static final FluidItemConsumerComponent.EUProductionMap<Fluid> FUEL_MAP = buildFuelMapFrom(FUEL_EU);

    private static Map<ResourceLocation, Long> buildFuelMap() {
        Map<ResourceLocation, Long> map = new LinkedHashMap<>();
        map.put(MIFluids.METHANE.getId(), METHANE_EU_PER_MB);
        map.put(MIFluids.ACETYLENE.getId(), ACETYLENE_EU_PER_MB);
        map.put(MIFluids.ETHYLENE.getId(), ETHYLENE_EU_PER_MB);
        map.put(MIFluids.PROPENE.getId(), PROPENE_EU_PER_MB);
        map.put(MIFluids.BUTADIENE.getId(), BUTADIENE_EU_PER_MB);
        map.put(MIFluids.BENZENE.getId(), BENZENE_EU_PER_MB);
        map.put(MIFluids.TOLUENE.getId(), TOLUENE_EU_PER_MB);
        map.put(MIFluids.ETHYLBENZENE.getId(), ETHYLBENZENE_EU_PER_MB);
        map.put(MIFluids.STYRENE.getId(), STYRENE_EU_PER_MB);
        map.put(MIFluids.DIETHYL_ETHER.getId(), DIETHYL_ETHER_EU_PER_MB);
        map.put(MIFluids.ETHANOL.getId(), ETHANOL_EU_PER_MB);
        map.put(ResourceLocation.fromNamespaceAndPath("smfcore", "helium_natural_gas"), HELIUM_NATURAL_GAS_EU_PER_MB);
        return map;
    }

    private static FluidItemConsumerComponent.EUProductionMap<Fluid> buildFuelMapFrom(Map<ResourceLocation, Long> map) {
        FluidItemConsumerComponent.EuProductionMapBuilder<Fluid> builder =
                new FluidItemConsumerComponent.EuProductionMapBuilder<>(BuiltInRegistries.FLUID);
        map.forEach(builder::add);
        return builder.build();
    }

    /** EU per mB for a given fluid, 0 if it is not a liquefied gas fuel. Used by JEI. */
    public static long euPerMb(Fluid fluid) {
        return FUEL_EU.getOrDefault(BuiltInRegistries.FLUID.getKey(fluid), 0L);
    }

    // Hatch positions follow MI's large diesel generator: each position only accepts its
    // specific hatch type (fluid input hatches on the side columns, energy output hatch
    // on the back center) instead of allowing any hatch.
    private static final HatchFlags FLUID_INPUT_FLAG = new HatchFlags.Builder()
            .with(HatchTypes.FLUID_INPUT)
            .build();
    private static final HatchFlags ENERGY_OUTPUT_FLAG = new HatchFlags.Builder()
            .with(HatchTypes.ENERGY_OUTPUT)
            .build();

    private static final SimpleMember CLEAN_CASING =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "clean_stainless_steel_machine_casing"));
    private static final SimpleMember PIPE_CASING =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "stainless_steel_machine_casing_pipe"));

    public static final ShapeTemplate SHAPE = new ShapeTemplate.LayeredBuilder(
            MachineCasings.CLEAN_STAINLESS_STEEL,
            new String[][] {
                    { "aaa", "aba", "aaa" },
                    { "aaa", "cdc", "aaa" },
                    { "aaa", "cdc", "aaa" },
                    { "ddd", "d#d", "ddd" }
            })
            .key('a', CLEAN_CASING, HatchFlags.NO_HATCH)
            .key('b', CLEAN_CASING, ENERGY_OUTPUT_FLAG)
            .key('c', CLEAN_CASING, FLUID_INPUT_FLAG)
            .key('d', PIPE_CASING, HatchFlags.NO_HATCH)
            .build();

    public LargeGasGeneratorBlockEntity(BEP bep,
            ShapeTemplate shapeTemplate,
            FluidItemConsumerComponent fluidConsumer) {
        super(bep, new MachineGuiParameters.Builder(ResourceLocation.fromNamespaceAndPath(SMFCore.ID, ID), false)
                .backgroundHeight(200).build(),
                new OrientationComponent.Params(false, false, false));

        this.activeShape = new ActiveShapeComponent(new ShapeTemplate[] { shapeTemplate });
        this.inventory = new MultiblockInventoryComponent();
        this.isActiveComponent = new IsActiveComponent();
        this.fluidConsumer = fluidConsumer;
        this.redstoneControl = new RedstoneControlComponent();

        this.registerComponents(activeShape, isActiveComponent, fluidConsumer, redstoneControl);
        registerGuiComponent(new SlotPanel(this).withRedstoneControl(redstoneControl));
        registerGuiComponent(new GeneratorMultiblockGui(() -> shapeValid.shapeValid, () -> lastEuProduction, fluidConsumer.maxEuProduction));
    }

    private boolean allowNormalOperation = false;

    private final ActiveShapeComponent activeShape;
    private final MultiblockInventoryComponent inventory;
    private final IsActiveComponent isActiveComponent;
    private final RedstoneControlComponent redstoneControl;
    private final List<EnergyComponent> energyOutputs = new ArrayList<>();
    private final FluidItemConsumerComponent fluidConsumer;
    private long lastEuProduction;

    @Override
    public ShapeTemplate getActiveShape() {
        return activeShape.getActiveShape();
    }

    @Override
    public List<EnergyComponent> getEnergyComponents() {
        return energyOutputs;
    }

    @Override
    public MultiblockInventoryComponent getMultiblockInventoryComponent() {
        return inventory;
    }

    @Override
    public final MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    public final MachineModelClientData getMachineModelData() {
        return new MachineModelClientData(null, orientation.facingDirection).active(isActiveComponent.isActive);
    }

    @Override
    public final void tick() {
        if (!level.isClientSide) {
            link();
            lastEuProduction = 0;
            if (allowNormalOperation) {
                if (this.redstoneControl.doAllowNormalOperation(this)) {
                    long euProduced = fluidConsumer.getEuProduction(inventory.getFluidInputs(),
                            inventory.getItemInputs(),
                            insertEnergy(Long.MAX_VALUE, Simulation.SIMULATE));
                    lastEuProduction = euProduced;
                    insertEnergy(euProduced, Simulation.ACT);
                    isActiveComponent.updateActive(euProduced != 0, this);
                } else {
                    isActiveComponent.updateActive(false, this);
                }
            } else {
                isActiveComponent.updateActive(false, this);
            }

            setChanged();
        }
    }

    public long insertEnergy(long value, Simulation simulation) {
        long rem = value;
        long inserted = 0;
        for (EnergyComponent e : energyOutputs) {
            if (rem > 0) {
                inserted += e.insertEu(rem, simulation);
                rem -= inserted;
            }
        }
        return inserted;
    }

    @Override
    protected void onRematch(ShapeMatcher shapeMatcher) {
        allowNormalOperation = false;
        if (shapeMatcher.isMatchSuccessful()) {
            inventory.rebuild(shapeMatcher);
            allowNormalOperation = true;

            energyOutputs.clear();
            for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
                hatch.appendEnergyOutputs(energyOutputs);
            }
        }
    }

    @Override
    public List<Component> getTooltips() {
        // Same presentation as MI's large diesel generator: default gray text with
        // gold-highlighted numbers (EU_PER_TICK_PARSER renders "8192 EU/t").
        return List.of(
                new MITooltips.Line(MIText.MaxEuProduction).arg(MAX_EU, MITooltips.EU_PER_TICK_PARSER).build(),
                Component.translatable("smfcore.generator.accept_any_liquefied_gas_fuels")
                        .setStyle(MITooltips.DEFAULT_STYLE));
    }

    public static LargeGasGeneratorBlockEntity create(BlockPos pos, BlockState state) {
        return new LargeGasGeneratorBlockEntity(
                new BEP(SMFBlocks.INSTANCE.getLARGE_GAS_GENERATOR_BLOCK_ENTITY().get(), pos, state),
                SHAPE,
                FluidItemConsumerComponent.ofFluid(MAX_EU, FUEL_MAP));
    }
}