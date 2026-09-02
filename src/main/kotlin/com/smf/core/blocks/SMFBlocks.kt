package com.smf.core.blocks

import com.smf.core.SMFCore
import aztech.modern_industrialization.machines.MachineBlock
import com.smf.core.machines.ElectricFrittingFurnaceBlockEntity
import com.smf.core.machines.HyperPressureReactorBlockEntity
import com.smf.core.machines.LargeElectrolyzerBlockEntity
import com.smf.core.machines.LargeGasGeneratorBlockEntity
import com.smf.core.machines.CryogenicFractionationTowerBlockEntity
import com.smf.core.machines.LargeChemicalBathBlockEntity
import com.smf.core.machines.LargeAutoclaveBlockEntity
import com.smf.core.machines.LargeVacuumFreezerBlockEntity
import com.smf.core.machines.RotatingGlassReactorBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object SMFBlocks {
    val REGISTRY: DeferredRegister.Blocks = DeferredRegister.createBlocks(SMFCore.ID)
    val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SMFCore.ID)

    val STEEL_SCAFFOLDING: DeferredBlock<Block> = REGISTRY.register("steel_scaffolding", Supplier {
        Block(
            BlockBehaviour.Properties.of()
                .strength(2.0f, 3.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    })

    val STAINLESS_STEEL_SCAFFOLDING: DeferredBlock<SMFHideableBlock> = REGISTRY.register("stainless_steel_scaffolding", Supplier {
        SMFHideableBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f, 3.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    })

    val TITANIUM_SCAFFOLDING: DeferredBlock<Block> = REGISTRY.register("titanium_scaffolding", Supplier {
        Block(
            BlockBehaviour.Properties.of()
                .strength(2.0f, 3.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    })

    val STAINLESS_STEEL_MACHINE_PIPE_CASING: DeferredBlock<Block> = REGISTRY.register("stainless_steel_machine_pipe_casing", Supplier {
        Block(
            BlockBehaviour.Properties.of()
                .strength(5.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
        )
    })

    val TITANIUM_MACHINE_PIPE_CASING: DeferredBlock<Block> = REGISTRY.register("titanium_machine_pipe_casing", Supplier {
        Block(
            BlockBehaviour.Properties.of()
                .strength(5.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
        )
    })

    val STEEL_MACHINE_PIPE_CASING: DeferredBlock<Block> = REGISTRY.register("steel_machine_pipe_casing", Supplier {
        Block(
            BlockBehaviour.Properties.of()
                .strength(5.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
        )
    })

    val TEMPERED_GLASS: DeferredBlock<TemperedGlassBlock> = REGISTRY.register("tempered_glass", Supplier {
        TemperedGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).requiresCorrectToolForDrops())
    })

    val NONCONDUCTING_CASING: DeferredBlock<Block> = REGISTRY.register("nonconducting_casing", Supplier {
        Block(
            BlockBehaviour.Properties.of()
                .strength(3.0f, 4.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
        )
    })

    val ELECTROLYTIC_CELL: DeferredBlock<Block> = REGISTRY.register("electrolytic_cell", Supplier {
        Block(
            BlockBehaviour.Properties.of()
                .strength(3.0f, 4.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
        )
    })

    val LARGE_ELECTROLYZER: DeferredBlock<MachineBlock> = REGISTRY.register(
        "large_electrolyzer",
        Supplier {
            MachineBlock(
                { pos: BlockPos, state -> LargeElectrolyzerBlockEntity.create(pos, state) },
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(4.0f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
            )
        }
    )

    val LARGE_ELECTROLYZER_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<LargeElectrolyzerBlockEntity>> =
        BLOCK_ENTITIES.register("large_electrolyzer", Supplier {
            BlockEntityType.Builder.of(
                { pos, state -> LargeElectrolyzerBlockEntity.create(pos, state) },
                LARGE_ELECTROLYZER.get()
            ).build(null)
        })

    val LARGE_GAS_GENERATOR: DeferredBlock<MachineBlock> = REGISTRY.register(
        "large_gas_generator",
        Supplier {
            MachineBlock(
                { pos: BlockPos, state -> LargeGasGeneratorBlockEntity.create(pos, state) },
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(4.0f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
            )
        }
    )

    val LARGE_GAS_GENERATOR_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<LargeGasGeneratorBlockEntity>> =
        BLOCK_ENTITIES.register("large_gas_generator", Supplier {
            BlockEntityType.Builder.of(
                { pos, state -> LargeGasGeneratorBlockEntity.create(pos, state) },
                LARGE_GAS_GENERATOR.get()
            ).build(null)
        })

    val CRYOGENIC_FRACTIONATION_TOWER: DeferredBlock<MachineBlock> = REGISTRY.register(
        "cryogenic_fractionation_tower",
        Supplier {
            MachineBlock(
                { pos: BlockPos, state -> CryogenicFractionationTowerBlockEntity.create(pos, state) },
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(4.0f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
            )
        }
    )

    val CRYOGENIC_FRACTIONATION_TOWER_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<CryogenicFractionationTowerBlockEntity>> =
        BLOCK_ENTITIES.register("cryogenic_fractionation_tower", Supplier {
            BlockEntityType.Builder.of(
                { pos, state -> CryogenicFractionationTowerBlockEntity.create(pos, state) },
                CRYOGENIC_FRACTIONATION_TOWER.get()
            ).build(null)
        })

    val LARGE_CHEMICAL_BATH: DeferredBlock<MachineBlock> = REGISTRY.register(
        "large_chemical_bath",
        Supplier {
            MachineBlock(
                { pos: BlockPos, state -> LargeChemicalBathBlockEntity.create(pos, state) },
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(4.0f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
            )
        }
    )

    val LARGE_CHEMICAL_BATH_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<LargeChemicalBathBlockEntity>> =
        BLOCK_ENTITIES.register("large_chemical_bath", Supplier {
            BlockEntityType.Builder.of(
                { pos, state -> LargeChemicalBathBlockEntity.create(pos, state) },
                LARGE_CHEMICAL_BATH.get()
            ).build(null)
        })

    val LARGE_AUTOCLAVE: DeferredBlock<MachineBlock> = REGISTRY.register(
        "large_autoclave",
        Supplier {
            MachineBlock(
                { pos: BlockPos, state -> LargeAutoclaveBlockEntity.create(pos, state) },
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(4.0f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
            )
        }
    )

    val LARGE_AUTOCLAVE_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<LargeAutoclaveBlockEntity>> =
        BLOCK_ENTITIES.register("large_autoclave", Supplier {
            BlockEntityType.Builder.of(
                { pos, state -> LargeAutoclaveBlockEntity.create(pos, state) },
                LARGE_AUTOCLAVE.get()
            ).build(null)
        })

    val LARGE_VACUUM_FREEZER: DeferredBlock<MachineBlock> = REGISTRY.register(
        "large_vacuum_freezer",
        Supplier {
            MachineBlock(
                { pos: BlockPos, state -> LargeVacuumFreezerBlockEntity.create(pos, state) },
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(4.0f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
            )
        }
    )

    val LARGE_VACUUM_FREEZER_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<LargeVacuumFreezerBlockEntity>> =
        BLOCK_ENTITIES.register("large_vacuum_freezer", Supplier {
            BlockEntityType.Builder.of(
                { pos, state -> LargeVacuumFreezerBlockEntity.create(pos, state) },
                LARGE_VACUUM_FREEZER.get()
            ).build(null)
        })

    val COBALT_ORE: DeferredBlock<Block> = REGISTRY.register("cobalt_ore", Supplier {
        Block(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .destroyTime(3.0f)
                .requiresCorrectToolForDrops()
        )
    })

    val DEEPSLATE_COBALT_ORE: DeferredBlock<Block> = REGISTRY.register("deepslate_cobalt_ore", Supplier {
        Block(
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                .destroyTime(4.5f)
                .requiresCorrectToolForDrops()
        )
    })

    val SILVER_ORE: DeferredBlock<Block> = REGISTRY.register("silver_ore", Supplier {
        Block(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .destroyTime(3.0f)
                .requiresCorrectToolForDrops()
        )
    })

    val FLUORITE_ORE: DeferredBlock<Block> = REGISTRY.register("fluorite_ore", Supplier {
        Block(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .destroyTime(3.0f)
                .requiresCorrectToolForDrops()
        )
    })

    val DEEPSLATE_FLUORITE_ORE: DeferredBlock<Block> = REGISTRY.register("deepslate_fluorite_ore", Supplier {
        Block(
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                .destroyTime(4.5f)
                .requiresCorrectToolForDrops()
        )
    })

    val CARNALLITE_ORE: DeferredBlock<Block> = REGISTRY.register("carnallite_ore", Supplier {
        Block(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .destroyTime(2.0f)
                .requiresCorrectToolForDrops()
        )
    })

    val COBALT_BLOCK: DeferredBlock<Block> = REGISTRY.register("cobalt_block", Supplier {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .destroyTime(5.0f)
                .explosionResistance(6.0f)
                .requiresCorrectToolForDrops()
        )
    })

    val RAW_COBALT_BLOCK: DeferredBlock<Block> = REGISTRY.register("raw_cobalt_block", Supplier {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .destroyTime(5.0f)
                .explosionResistance(6.0f)
                .requiresCorrectToolForDrops()
        )
    })

    val ADAMANTINE_DEBRIS: DeferredBlock<Block> = REGISTRY.register("adamantine_debris", Supplier {
        Block(BlockBehaviour.Properties.ofFullCopy(Blocks.ANCIENT_DEBRIS))
    })

    val ADAMANTINE_BLOCK: DeferredBlock<Block> = REGISTRY.register("adamantine_block", Supplier {
        Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK))
    })

    val ELECTRIC_FRITTING_FURNACE: DeferredBlock<MachineBlock> = REGISTRY.register(
        "electric_fritting_furnace",
        Supplier {
            MachineBlock(
                { pos: BlockPos, state -> ElectricFrittingFurnaceBlockEntity.create(pos, state) },
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(4.0f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
            )
        }
    )

    val HYPER_PRESSURE_REACTOR: DeferredBlock<MachineBlock> = REGISTRY.register(
        "hyper_pressure_reactor",
        Supplier {
            MachineBlock(
                { pos: BlockPos, state -> HyperPressureReactorBlockEntity.create(pos, state) },
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(4.0f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
            )
        }
    )

    val ELECTRIC_FRITTING_FURNACE_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<ElectricFrittingFurnaceBlockEntity>> =
        BLOCK_ENTITIES.register("electric_fritting_furnace", Supplier {
            BlockEntityType.Builder.of(
                { pos, state -> ElectricFrittingFurnaceBlockEntity.create(pos, state) },
                ELECTRIC_FRITTING_FURNACE.get()
            ).build(null)
        })

    val HYPER_PRESSURE_REACTOR_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<HyperPressureReactorBlockEntity>> =
        BLOCK_ENTITIES.register("hyper_pressure_reactor", Supplier {
            BlockEntityType.Builder.of(
                { pos, state -> HyperPressureReactorBlockEntity.create(pos, state) },
                HYPER_PRESSURE_REACTOR.get()
            ).build(null)
        })

    val ROTATING_GLASS_REACTOR: DeferredBlock<MachineBlock> = REGISTRY.register(
        "rotating_glass_reactor",
        Supplier {
            MachineBlock(
                { pos: BlockPos, state -> RotatingGlassReactorBlockEntity.create(pos, state) },
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(4.0f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
            )
        }
    )

    val ROTATING_GLASS_REACTOR_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<RotatingGlassReactorBlockEntity>> =
        BLOCK_ENTITIES.register("rotating_glass_reactor", Supplier {
            BlockEntityType.Builder.of(
                { pos, state -> RotatingGlassReactorBlockEntity.create(pos, state) },
                ROTATING_GLASS_REACTOR.get()
            ).build(null)
        })

    fun init() = Unit
}
