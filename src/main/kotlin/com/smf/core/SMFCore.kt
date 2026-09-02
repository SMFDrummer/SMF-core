package com.smf.core

import aztech.modern_industrialization.MIText
import aztech.modern_industrialization.MITooltips
import aztech.modern_industrialization.client.machines.multiblocks.MultiblockMachineBER
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity
import com.smf.core.blocks.SMFBlocks
import com.smf.core.client.render.SMFRotatingMultiblockRenderer
import com.smf.core.enchantments.SMFEnchantments
import com.smf.core.fluids.SMFFluids
import com.smf.core.items.SMFItems
import com.smf.core.machines.SMFMachines
import com.smf.core.machines.multiblock.SMFMultiblockBlockEntity
import com.mojang.datafixers.util.Either
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.client.event.RenderTooltipEvent
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
import net.neoforged.neoforge.common.NeoForge
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * SMF Core - A Modern Industrialization addon focused on multiblock machines
 *
 * Main mod class for SMF Core. This class is responsible for initializing the mod
 * and registering all components (machines, items, blocks, etc.)
 */
@Mod(SMFCore.ID)
class SMFCore(modEventBus: IEventBus, modContainer: ModContainer) {

    init {
        LOGGER.info("Initializing SMF Core")

        // Register blocks, items, and creative tabs to the mod event bus
        SMFBlocks.REGISTRY.register(modEventBus)
        SMFBlocks.BLOCK_ENTITIES.register(modEventBus)
        SMFItems.REGISTRY.register(modEventBus)
        SMFItems.CREATIVE_TABS.register(modEventBus)
        SMFMachines.RECIPE_SERIALIZERS.register(modEventBus)
        SMFMachines.RECIPE_TYPES.register(modEventBus)
        SMFFluids.init(modEventBus)
        com.smf.core.machines.ui.VacuumFreezerUIMenu.registerMenuType(modEventBus)

        // Initialize blocks and items
        SMFBlocks.init()
        SMFItems.init()

        SMFMachines.init()

        // Register client-side rendering setup
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener { event: RegisterMenuScreensEvent ->
                com.smf.core.machines.ui.VacuumFreezerUIMenu.registerScreen(event)
            }

            // Bump the baked-vertex cache version on resource reload (atlas UVs change).
            modEventBus.addListener { event: RegisterClientReloadListenersEvent ->
                event.registerReloadListener(object : SimplePreparableReloadListener<Void>() {
                    override fun prepare(resourceManager: ResourceManager, profiler: ProfilerFiller): Void? {
                        return null
                    }

                    override fun apply(data: Void?, resourceManager: ResourceManager, profiler: ProfilerFiller) {
                        SMFMultiblockBlockEntity.clientResourceVersion++
                    }
                })
            }

            modEventBus.addListener { event: FMLClientSetupEvent ->
                // Set render layer for transparent blocks
                ItemBlockRenderTypes.setRenderLayer(SMFBlocks.STEEL_SCAFFOLDING.get(), RenderType.cutout())
                ItemBlockRenderTypes.setRenderLayer(SMFBlocks.STAINLESS_STEEL_SCAFFOLDING.get(), RenderType.cutout())
                ItemBlockRenderTypes.setRenderLayer(SMFBlocks.TITANIUM_SCAFFOLDING.get(), RenderType.cutout())
                ItemBlockRenderTypes.setRenderLayer(SMFBlocks.TEMPERED_GLASS.get(), RenderType.translucent())

                // Register the multiblock renderer so that the wrench shape preview and
                // hatch placement overlays are rendered for the electric fritting furnace.
                BlockEntityRenderers.register(
                    SMFBlocks.ELECTRIC_FRITTING_FURNACE_BLOCK_ENTITY.get() as BlockEntityType<MultiblockMachineBlockEntity>,
                    BlockEntityRendererProvider { MultiblockMachineBER(it) }
                )
                LOGGER.info("Registered MultiblockMachineBER for {}", SMFBlocks.ELECTRIC_FRITTING_FURNACE_BLOCK_ENTITY.getId())

                // Multiblock renderer for the hyper pressure reactor (wrench preview etc.)
                BlockEntityRenderers.register(
                    SMFBlocks.HYPER_PRESSURE_REACTOR_BLOCK_ENTITY.get() as BlockEntityType<MultiblockMachineBlockEntity>,
                    BlockEntityRendererProvider { MultiblockMachineBER(it) }
                )

                // Multiblock renderer for the large electrolyzer (wrench preview etc.)
                BlockEntityRenderers.register(
                    SMFBlocks.LARGE_ELECTROLYZER_BLOCK_ENTITY.get() as BlockEntityType<MultiblockMachineBlockEntity>,
                    BlockEntityRendererProvider { MultiblockMachineBER(it) }
                )

                // Multiblock renderer for the large gas generator (wrench preview etc.)
                BlockEntityRenderers.register(
                    SMFBlocks.LARGE_GAS_GENERATOR_BLOCK_ENTITY.get() as BlockEntityType<MultiblockMachineBlockEntity>,
                    BlockEntityRendererProvider { MultiblockMachineBER(it) }
                )

                // Multiblock renderer for the cryogenic fractionation tower (wrench preview etc.)
                BlockEntityRenderers.register(
                    SMFBlocks.CRYOGENIC_FRACTIONATION_TOWER_BLOCK_ENTITY.get() as BlockEntityType<MultiblockMachineBlockEntity>,
                    BlockEntityRendererProvider { MultiblockMachineBER(it) }
                )

                // Multiblock renderer for the large chemical bath (wrench preview etc.)
                BlockEntityRenderers.register(
                    SMFBlocks.LARGE_CHEMICAL_BATH_BLOCK_ENTITY.get() as BlockEntityType<MultiblockMachineBlockEntity>,
                    BlockEntityRendererProvider { MultiblockMachineBER(it) }
                )

                // Multiblock renderer for the large autoclave (wrench preview etc.)
                BlockEntityRenderers.register(
                    SMFBlocks.LARGE_AUTOCLAVE_BLOCK_ENTITY.get() as BlockEntityType<MultiblockMachineBlockEntity>,
                    BlockEntityRendererProvider { MultiblockMachineBER(it) }
                )

                // Multiblock renderer for the large vacuum freezer (wrench preview etc.)
                BlockEntityRenderers.register(
                    SMFBlocks.LARGE_VACUUM_FREEZER_BLOCK_ENTITY.get() as BlockEntityType<MultiblockMachineBlockEntity>,
                    BlockEntityRendererProvider { MultiblockMachineBER(it) }
                )

                // Rotating glass reactor: Create-style renderer rotates the baked hidden glass structure
                // (base class hides the real glass while the multiblock is assembled).
                BlockEntityRenderers.register(
                    SMFBlocks.ROTATING_GLASS_REACTOR_BLOCK_ENTITY.get() as BlockEntityType<MultiblockMachineBlockEntity>,
                    BlockEntityRendererProvider { SMFRotatingMultiblockRenderer(it) }
                )

                // Ore generation shift-tooltips, matching MI's ore tooltips
                // (Y level, veins per chunk, vein size)
                val oreAttachment = MITooltips.TooltipAttachment.ofMultilines { _, item ->
                    val block = (item as? BlockItem)?.block
                    when (block) {
                        SMFBlocks.COBALT_ORE.get(), SMFBlocks.DEEPSLATE_COBALT_ORE.get() -> listOf(
                            MITooltips.Line(MIText.OreGenerationTooltipY).arg(-64).arg(20).build(),
                            MITooltips.Line(MIText.OreGenerationTooltipVeinFrequency).arg(6).build(),
                            MITooltips.Line(MIText.OreGenerationTooltipVeinSize).arg(5).build()
                        )
                        SMFBlocks.SILVER_ORE.get() -> listOf(
                            MITooltips.Line(MIText.OreNotGenerated).build()
                        )
                        else -> emptyList()
                    }
                }
                MITooltips.TOOLTIPS.add(oreAttachment)
            }

            // Tooltip fixes: dedupe identical text lines and show the adamantine
            // enchantment description.
            // The same lines can be added twice when the tooltip is collected
            // multiple times (e.g. with ModernUI rendering), so we remove duplicates.
            NeoForge.EVENT_BUS.addListener { event: RenderTooltipEvent.GatherComponents ->
                val seen = HashSet<String>()
                val iterator = event.tooltipElements.listIterator()
                while (iterator.hasNext()) {
                    val element = iterator.next()
                    val text = element.left().orElse(null)?.string
                    if (text != null && !seen.add(text)) {
                        iterator.remove()
                    }
                }

                val enchantments = event.itemStack.get(DataComponents.ENCHANTMENTS)
                if (enchantments != null && enchantments.keySet().any { it.`is`(SMFEnchantments.ADAMANTINE) }) {
                    event.tooltipElements.add(
                        Either.left(
                            Component.translatable("enchantment.smfcore.adamantine.desc")
                                .setStyle(Style.EMPTY.withColor(0x8a8a8a))
                        )
                    )
                }
            }
        }

        // Register event listeners - only if you have @SubscribeEvent methods
        // NeoForge.EVENT_BUS.register(this)

        LOGGER.info("SMF Core initialized successfully")
    }

    companion object {
        const val ID = "smfcore"
        val LOGGER: Logger = LogManager.getLogger(ID)

        fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(ID, path)
    }
}
