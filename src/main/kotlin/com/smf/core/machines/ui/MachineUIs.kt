package com.smf.core.machines.ui

import aztech.modern_industrialization.MIItem
import aztech.modern_industrialization.MIText
import aztech.modern_industrialization.inventory.HackySlot
import aztech.modern_industrialization.items.RedstoneControlModuleItem
import aztech.modern_industrialization.machines.MachineBlockEntity
import aztech.modern_industrialization.machines.components.UpgradeComponent
import aztech.modern_industrialization.machines.multiblocks.HatchFlags
import aztech.modern_industrialization.machines.multiblocks.HatchType
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate
import aztech.modern_industrialization.machines.recipe.MachineRecipe
import com.lowdragmc.lowdraglib2.editor.resource.FilePath
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.bindingsS2C
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView
import com.lowdragmc.lowdraglib2.gui.ui.elements.Tab
import com.lowdragmc.lowdraglib2.gui.ui.elements.TabView
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement
import com.lowdragmc.lowdraglib2.gui.ui.elements.UITemplateElement
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin
import com.lowdragmc.lowdraglib2.utils.FluidHelper
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld
import com.smf.core.items.SMFItems
import com.smf.core.machines.AdvancedOverdriveComponent
import aztech.modern_industrialization.util.TextHelper
import dev.vfyjxf.taffy.style.FlexDirection
import dev.vfyjxf.taffy.style.FlexWrap
import dev.vfyjxf.taffy.style.TaffyDisplay
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.neoforged.neoforge.fluids.FluidStack
import java.util.LinkedHashMap
import kotlin.math.max
import mezz.jei.api.recipe.RecipeType

/** Runtime bindings for the single editor-authored multiblock UI. */
object MachineUIs {
    private const val TEMPLATE_PATH = "./ldlib2/assets/ldlib2/resources/global/multiblock_gui.ui.nbt"

    // Recipe slot rows: max 4 slots per row so long recipes wrap instead of
    // stretching the overclock card horizontally. ItemSlot is 18x18.
    private const val SLOT_SIZE = 18f
    private const val SLOT_ROW_GAP = 2f
    private const val SLOTS_PER_ROW = 4
    private val SLOT_ROW_MAX_WIDTH = SLOTS_PER_ROW * SLOT_SIZE + (SLOTS_PER_ROW - 1) * SLOT_ROW_GAP

    // Mirrors MI's ViewerUtil.PROBABILITY_FORMAT ("#.#" drops trailing zeros).
    private val PROBABILITY_FORMAT = java.text.DecimalFormat("#.#")

    /** Kept as the public entry point used by the current gray-test menu. */
    fun vacuumFreezer(ctx: MultiblockUIContext): UIElement = multiblock(ctx)

    /** Every MI multiblock uses this same editor-authored template. */
    fun multiblock(ctx: MultiblockUIContext): UIElement {
        val root = UITemplateElement(FilePath(TEMPLATE_PATH))
        check(root.template != null) {
            "Unable to load LDLib2 multiblock template: $TEMPLATE_PATH"
        }

        // The root itself is only a layout host. Page/card backgrounds remain
        // owned by the editor-authored NBT and its GDP stylesheet.
        root.removeClass("panel_bg")
        root.style.backgroundTexture(com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture.EMPTY)

        val runtimeState = RuntimeState(ctx)
        bindTitle(root, ctx)
        bindInfoPage(root, runtimeState)
        bindInfoPanelRecipeLookup(root, ctx)
        bindInventory(root, ctx)
        bindModuleRail(root, ctx)
        bindTabs(root, ctx)
        bindPreviewPage(root, ctx)
        bindOverclockPage(root, ctx, runtimeState)
        bindRuntimeSnapshot(root, ctx, runtimeState)
        return root
    }

    private class RuntimeState(ctx: MultiblockUIContext) {
        var snapshot: RuntimeSnapshot = RuntimeSnapshot.from(ctx)
    }

    private data class RecipeRuntime(
        val id: ResourceLocation,
        val efficiency: Float,
        val execution: Float,
        val efficiencyTicks: Int,
        val maxEfficiencyTicks: Int,
        val currentEu: Long,
        val effectiveDurationTicks: Int,
    )

    private data class RuntimeSnapshot(
        val generator: Boolean,
        val shapeValid: Boolean,
        val matchesMultipleRecipes: Boolean,
        val hasActiveRecipe: Boolean,
        val progress: Float,
        val efficiencyTicks: Int,
        val maxEfficiencyTicks: Int,
        val baseEu: Long,
        val currentEu: Long,
        val currentGeneration: Long,
        val maxGeneration: Long,
        val recipes: List<RecipeRuntime>,
    ) {
        companion object {
            fun from(ctx: MultiblockUIContext): RuntimeSnapshot {
                val crafter = ctx.crafter
                val active = ctx.hasActiveRecipe()
                return RuntimeSnapshot(
                    generator = ctx.isGenerator(),
                    shapeValid = ctx.shapeValid,
                    matchesMultipleRecipes = ctx.matchesMultipleRecipes(),
                    hasActiveRecipe = active,
                    progress = ctx.activeRecipeProgress(),
                    efficiencyTicks = crafter?.efficiencyTicks ?: 0,
                    maxEfficiencyTicks = crafter?.maxEfficiencyTicks ?: 0,
                    baseEu = if (active) crafter?.baseRecipeEu ?: 0L else 0L,
                    currentEu = if (active) crafter?.currentRecipeEu ?: 0L else 0L,
                    currentGeneration = ctx.currentEuGeneration(),
                    maxGeneration = ctx.maxEuGeneration(),
                    recipes = ctx.lockedRecipeIds().map { id ->
                        val ticks = ctx.recipeEfficiencyTicks(id)
                        RecipeRuntime(
                            id,
                            ctx.overclockProgress(id),
                            ctx.recipeProgress(id),
                            ticks.first,
                            ticks.second,
                            ctx.recipeCurrentEu(id),
                            ctx.recipeEffectiveDurationTicks(id),
                        )
                    },
                )
            }

            fun from(tag: Tag): RuntimeSnapshot {
                val root = tag as? CompoundTag ?: return empty()
                val recipeList = root.getList("recipes", Tag.TAG_COMPOUND.toInt())
                val recipes = (0 until recipeList.size).map { index ->
                    val entry = recipeList.getCompound(index)
                    RecipeRuntime(
                        id = ResourceLocation.parse(entry.getString("id")),
                        efficiency = entry.getFloat("efficiency"),
                        execution = entry.getFloat("execution"),
                        efficiencyTicks = entry.getInt("efficiency_ticks"),
                        maxEfficiencyTicks = entry.getInt("max_efficiency_ticks"),
                        currentEu = entry.getLong("current_eu"),
                        effectiveDurationTicks = entry.getInt("effective_duration_ticks"),
                    )
                }
                return RuntimeSnapshot(
                    generator = root.getBoolean("generator"),
                    shapeValid = root.getBoolean("shape_valid"),
                    matchesMultipleRecipes = root.getBoolean("matches_multiple"),
                    hasActiveRecipe = root.getBoolean("active_recipe"),
                    progress = root.getFloat("progress"),
                    efficiencyTicks = root.getInt("efficiency_ticks"),
                    maxEfficiencyTicks = root.getInt("max_efficiency_ticks"),
                    baseEu = root.getLong("base_eu"),
                    currentEu = root.getLong("current_eu"),
                    currentGeneration = root.getLong("current_generation"),
                    maxGeneration = root.getLong("max_generation"),
                    recipes = recipes,
                )
            }

            fun empty() = RuntimeSnapshot(false, false, false, false, 0f, 0, 0, 0L, 0L, 0L, 0L, emptyList())
        }
    }

    private fun runtimeSnapshotTag(ctx: MultiblockUIContext): CompoundTag {
        val snapshot = RuntimeSnapshot.from(ctx)
        return CompoundTag().apply {
            putBoolean("generator", snapshot.generator)
            putBoolean("shape_valid", snapshot.shapeValid)
            putBoolean("matches_multiple", snapshot.matchesMultipleRecipes)
            putBoolean("active_recipe", snapshot.hasActiveRecipe)
            putFloat("progress", snapshot.progress)
            putInt("efficiency_ticks", snapshot.efficiencyTicks)
            putInt("max_efficiency_ticks", snapshot.maxEfficiencyTicks)
            putLong("base_eu", snapshot.baseEu)
            putLong("current_eu", snapshot.currentEu)
            putLong("current_generation", snapshot.currentGeneration)
            putLong("max_generation", snapshot.maxGeneration)
            val recipes = ListTag()
            snapshot.recipes.forEach { recipe ->
                recipes.add(CompoundTag().apply {
                    putString("id", recipe.id.toString())
                    putFloat("efficiency", recipe.efficiency)
                    putFloat("execution", recipe.execution)
                    putInt("efficiency_ticks", recipe.efficiencyTicks)
                    putInt("max_efficiency_ticks", recipe.maxEfficiencyTicks)
                    putLong("current_eu", recipe.currentEu)
                    putInt("effective_duration_ticks", recipe.effectiveDurationTicks)
                })
            }
            put("recipes", recipes)
        }
    }

    private fun bindRuntimeSnapshot(root: UITemplateElement, ctx: MultiblockUIContext, state: RuntimeState) {
        val initial: Tag = if (ctx.isClientSide) CompoundTag() else runtimeSnapshotTag(ctx)
        val binding = DataBindingBuilder.tagS2C {
            if (ctx.isClientSide) CompoundTag() else runtimeSnapshotTag(ctx)
        }.initialValue(initial).build()
        root.addSyncValue(binding.syncValue)
        binding.syncValue.addListener { tag ->
            if (ctx.isClientSide) state.snapshot = RuntimeSnapshot.from(tag)
        }
    }

    private fun bindTitle(root: UITemplateElement, ctx: MultiblockUIContext) {
        val machineStack = ctx.machineStack()
        root.find<TextElement>("title_machine_name")?.setText(machineStack.hoverName)
        root.find<UIElement>("title_machine_icon")?.setIcon(machineStack)
        root.find<UIElement>("info_tab_machine_icon_placeholder")?.setIcon(machineStack)
    }

    private fun bindInfoPage(root: UITemplateElement, state: RuntimeState) {
        val ids = listOf(
            "multiblock_integrity_status",
            "multiblock_running_status",
            "recipe_running_progress",
            "recipe_running_efficiency",
            "recipe_running_eu_cost_base",
            "recipe_running_eu_cost_current",
        )

        fun value(index: Int): Component {
            val snapshot = state.snapshot
            if (snapshot.generator) {
                return when (index) {
                    0 -> if (snapshot.shapeValid) MIText.MultiblockShapeValid.text() else MIText.MultiblockShapeInvalid.text()
                    1 -> MIText.GeneratorCurrentEu.text(TextHelper.getEuTextTick(snapshot.currentGeneration))
                    2 -> MIText.GeneratorMaxEu.text(TextHelper.getEuTextTick(snapshot.maxGeneration))
                    else -> Component.empty()
                }
            }
            return when (index) {
                0 -> if (snapshot.shapeValid) MIText.MultiblockShapeValid.text() else MIText.MultiblockShapeInvalid.text()
                1 -> {
                    val status = if (snapshot.shapeValid) MIText.MultiblockStatusActive.text() else Component.empty()
                    if (snapshot.matchesMultipleRecipes) {
                        if (!snapshot.shapeValid) MIText.MachineMultipleRecipes1.text()
                        else status.append("\n").append(MIText.MachineMultipleRecipes1.text())
                    } else status
                }
                2 -> MIText.Progress.text("%.1f".format(snapshot.progress * 100f) + " %")
                3 -> MIText.EfficiencyTicks.text(snapshot.efficiencyTicks, snapshot.maxEfficiencyTicks)
                4 -> MIText.BaseEuRecipe.text(TextHelper.getEuTextTick(snapshot.baseEu))
                5 -> MIText.CurrentEuRecipe.text(TextHelper.getEuTextTick(snapshot.currentEu))
                else -> Component.empty()
            }
        }

        fun update() {
            val snapshot = state.snapshot
            val active = snapshot.hasActiveRecipe
            ids.forEachIndexed { index, id ->
                root.find<Label>(id)?.let { label ->
                    val visible = if (snapshot.generator) {
                        index <= 2 && (index == 0 || snapshot.shapeValid)
                    } else when (index) {
                        0 -> true
                        1 -> snapshot.shapeValid || snapshot.matchesMultipleRecipes
                        2, 4, 5 -> snapshot.shapeValid && active
                        3 -> snapshot.shapeValid && active && (snapshot.efficiencyTicks != 0 || snapshot.maxEfficiencyTicks != 0)
                        else -> false
                    }
                    label.setAvailable(visible)
                    if (visible) label.setText(value(index))
                }
            }
        }

        root.addEventListener(UIEvents.TICK) { update() }
        update()
    }

    /**
     * Vanilla-MI style recipe lookup: hovering the multiblock info panel shows a
     * "view recipes" tooltip, and clicking it opens the machine's JEI recipe
     * category (all recipes of this machine).
     */
    private fun bindInfoPanelRecipeLookup(root: UITemplateElement, ctx: MultiblockUIContext) {
        val infoPanel = root.find<UIElement>("multiblock_info") ?: return
        infoPanel.style.tooltips(Component.translatable("smfcore.ui.view_recipes"))
        infoPanel.addEventListener(UIEvents.CLICK) { event ->
            if (event.button == 0) {
                openMachineRecipes(ctx)
            }
        }
    }

    private fun openMachineRecipes(ctx: MultiblockUIContext) {
        val block = ctx.be?.blockState?.block ?: return
        val blockId = BuiltInRegistries.BLOCK.getKey(block)
        val jeiRuntime = LDLibJEIPlugin.jeiRuntime ?: return
        val recipeType = RecipeType.create(blockId.namespace, blockId.path, RecipeHolder::class.java)
        jeiRuntime.recipesGui.showTypes(listOf(recipeType))
    }

    private fun bindInventory(root: UITemplateElement, ctx: MultiblockUIContext) {
        for (index in 0 until 36) {
            root.find<ItemSlot>("inventory_$index")?.apply {
                bind(Slot(ctx.player.inventory, index, 0, 0))
            }
        }
    }

    private fun bindModuleRail(root: UITemplateElement, ctx: MultiblockUIContext) {
        val redstoneSlot = root.find<ItemSlot>("mi_origin_redstone_module_placeholder")?.apply {
            setAvailable(ctx.redstoneControl() != null)
            if (ctx.redstoneControl() != null) {
                bind(redstoneModuleSlot(ctx))
                addModuleSlotOverlay(
                    "smfcore:textures/gui/redstone_module_layer.png",
                    MIText.AcceptsRedstoneControlModule.text(),
                )
            }
        }
        root.find<ItemSlot>("mi_origin_upgrade_module_placeholder")?.apply {
            setAvailable(ctx.upgrades() != null)
            if (ctx.upgrades() != null) {
                bind(upgradeModuleSlot(ctx))
                addModuleSlotOverlay(
                    "smfcore:textures/gui/upgrade_module_layer.png",
                    MIText.AcceptsUpgrades.text(),
                )
            }
        }
        root.find<ItemSlot>("mi_origin_overdrive_module_placeholder")?.apply {
            setAvailable(ctx.overdrive() != null)
            if (ctx.overdrive() != null) {
                bind(overdriveModuleSlot(ctx))
                addModuleSlotOverlay(
                    "smfcore:textures/gui/overdrive_module_layer.png",
                    MIText.AcceptsOverdriveModule.text(),
                )
            }
        }

        val button = root.find<Button>("toggle_redstone_module_mode_button")
        val icon = root.find<UIElement>("toggle_redstone_module_mode_icon_placeholder")
        button?.setOnServerClick { _ ->
            if (!ctx.isClientSide) ctx.toggleRedstoneMode()
        }

        fun update() {
            val module = redstoneSlot?.getValue()?.takeUnless { it.isEmpty } ?: ctx.redstoneStack()
            val available = module != null
            button?.setAvailable(available)
            icon?.setAvailable(available)
            if (available) {
                // Use the actual module stack as the fallback icon. Vanilla
                // sprite lookup is not guaranteed to contain the requested
                // block sprite in every LDLib2 atlas configuration.
                icon?.style?.backgroundTexture(ItemStackTexture(module!!))
            }
        }

        root.addEventListener(UIEvents.TICK) { update() }
        update()
    }

    /**
     * Adds the vanilla-MI module slot look: an empty-slot overlay texture plus
     * the "accepts ..." hover tooltip shown only while the slot is empty.
     */
    private fun ItemSlot.addModuleSlotOverlay(texture: String, tooltip: Component) {
        slotStyle.slotOverlay(SpriteTexture.of(texture))
        addEventListener(UIEvents.HOVER_TOOLTIPS) { event ->
            if (getValue().isEmpty) {
                event.hoverTooltips = com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips(
                    listOf(tooltip),
                    null,
                    null,
                    null,
                )
            }
        }
    }

    private fun bindTabs(root: UITemplateElement, ctx: MultiblockUIContext) {
        val tabView = root.find<TabView>("tab_view") ?: return
        val infoTab = root.find<Tab>("info_tab") ?: return
        val previewTab = root.find<Tab>("preview_tab") ?: return
        val overclockTab = root.find<Tab>("overclock_tab")
        val overclockIcon = root.find<UIElement>("overclock_tab_module_icon_placeholder")

        root.find<UIElement>("preview_tab_structure_block_icon_placeholder")
            ?.setIcon(Items.STRUCTURE_BLOCK.defaultInstance)
        tabView.selectTab(infoTab)

        fun update() {
            infoTab.setAvailable(true)
            previewTab.setAvailable(true)
            val module = root.find<ItemSlot>("mi_origin_overdrive_module_placeholder")
                ?.getValue()?.takeUnless { it.isEmpty }
                ?: ctx.overdriveStack()
            val hasOverdrive = module != null
            overclockTab?.setAvailable(hasOverdrive)
            overclockIcon?.setAvailable(hasOverdrive)
            if (hasOverdrive) overclockIcon?.setIcon(module!!)
            if (!hasOverdrive && tabView.selectedTab == overclockTab) tabView.selectTab(infoTab)
        }

        root.addEventListener(UIEvents.TICK) { update() }
        update()
    }

    private fun bindPreviewPage(root: UITemplateElement, ctx: MultiblockUIContext) {
        val previous = root.find<Button>("multiblock_structure_selector_back")
        val next = root.find<Button>("multiblock_structure_selector_front")
        val title = root.find<TextElement>("multiblock_structure_level")
        val materialsView = root.find<ScrollerView>("multiblock_structure_material_list")
        val scene = root.find<Scene>("multiblock_preview_scene")
        val fluidPrototype = root.find<FluidSlot>("fluid_material_template")?.copy() as? FluidSlot

        var observedShape = ctx.shapeIndex()

        title?.let {
            bindDynamicText(it) { ctx.shapeSelectionPresentation().title }
        }
        previous?.let {
            bindDynamicText(it.text) {
                if (ctx.shapeSelectionPresentation().useArrows) Component.literal("<") else Component.literal("-")
            }
        }
        next?.let {
            bindDynamicText(it.text) {
                if (ctx.shapeSelectionPresentation().useArrows) Component.literal(">") else Component.literal("+")
            }
        }

        fun updatePreview(index: Int) {
            val shapes = ctx.shapeTemplates()
            val selected = index.coerceIn(0, max(0, shapes.lastIndex))
            observedShape = selected
            // The client may not have MI's ShapeSelection GUI component. The
            // actual selected index is still synchronized through the machine
            // state, so use it as the authoritative visual state and avoid a
            // local optimistic update being overwritten by an old value.
            previous?.setEnabled(selected > 0)
            next?.setEnabled(selected < shapes.lastIndex)
            materialsView?.let {
                populateMaterials(it, fluidPrototype, ctx, shapes.getOrNull(selected))
            }
            if (scene != null && shapes.getOrNull(selected) != null) {
                configureScene(scene, ctx, shapes[selected])
            }
        }

        previous?.setOnServerClick { _ -> ctx.incrementShape(-1) }
        next?.setOnServerClick { _ -> ctx.incrementShape(1) }
        updatePreview(observedShape)

        root.addEventListener(UIEvents.TICK) {
            val current = ctx.shapeIndex()
            if (current != observedShape) updatePreview(current)
        }
    }

    private fun bindOverclockPage(root: UITemplateElement, ctx: MultiblockUIContext, state: RuntimeState) {
        val list = root.find<ScrollerView>("overclock_recipe_list") ?: return
        val prototype = root.find<UIElement>("overclock_recipe_card_template")?.copy()
        var rendered: List<ResourceLocation>? = null

        fun rebuild() {
            // Dynamic cards are a client presentation. Building them on the
            // server creates a different slot/sync tree while the client is
            // still waiting for active-recipe state.
            if (!ctx.isClientSide) return
            val recipes = state.snapshot.recipes
            val ids = recipes.map { it.id }
            if (ids == rendered) return
            rendered = ids
            list.clearAllScrollViewChildren()

            if (ids.isEmpty()) {
                val empty = Label().setText("暂无锁定配方", false)
                empty.layout.widthPercent(100f).heightPercent(100f)
                empty.textStyle { style ->
                    style.textAlignHorizontal(com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal.CENTER)
                        .textAlignVertical(com.lowdragmc.lowdraglib2.gui.ui.data.Vertical.CENTER)
                }
                list.addScrollViewChild(empty)
                return
            }

            recipes.forEachIndexed { index, recipeState ->
                val recipeId = recipeState.id
                val card = prototype?.copy() ?: UIElement()
                card.setId("overclock_recipe_card_$index")
                val placeholder = card.find<UIElement>("overclock_recipe_placeholder")
                placeholder?.clearAllChildren()
                placeholder?.addChild(
                    recipeUi(
                        ctx.findRecipe(recipeId),
                        recipeId,
                        { state.snapshot.recipes.getOrNull(index) ?: recipeState },
                    )
                )

                val efficiency = card.find<ProgressBar>("overclock_recipe_efficiency_progress_bar")
                efficiency?.setProgress(recipeState.efficiency)
                efficiency?.label?.setText(
                    MIText.EfficiencyTicks.text(
                        recipeState.efficiencyTicks,
                        recipeState.maxEfficiencyTicks,
                    )
                )
                card.find<TextElement>("recipe_current_eu")?.setText(
                    TextHelper.getEuTextTick(recipeState.currentEu)
                )
                card.find<TextElement>("recipe_duration")?.setText(
                    MIText.BaseDurationSeconds.text(recipeState.effectiveDurationTicks / 20.0)
                )
                card.find<RecipeArrowElement>("recipe_arrow")?.setProgress(recipeState.execution)
                ctx.findRecipe(recipeId)?.let { resolvedRecipe ->
                    card.addEventListener(UIEvents.HOVER_TOOLTIPS) { event ->
                        event.hoverTooltips = com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips(
                            listOf(MIText.BaseEuTotal.text(TextHelper.getEuText(resolvedRecipe.totalEu.toLong()))),
                            null,
                            null,
                            ItemStack.EMPTY,
                        )
                    }
                }
                list.addScrollViewChild(card)
            }
        }

        root.addEventListener(UIEvents.TICK) {
            rebuild()
            list.select("progress-bar", ProgressBar::class.java).toList().forEachIndexed { index, bar ->
                val recipe = state.snapshot.recipes.getOrNull(index) ?: return@forEachIndexed
                bar.setProgress(recipe.efficiency)
                bar.label.setText(MIText.EfficiencyTicks.text(recipe.efficiencyTicks, recipe.maxEfficiencyTicks))
                val card = list.viewContainer.getChildren().getOrNull(index)
                val current = state.snapshot.recipes.getOrNull(index)
                if (card != null && current != null) {
                    card.find<TextElement>("recipe_current_eu")?.setText(TextHelper.getEuTextTick(current.currentEu))
                    card.find<TextElement>("recipe_duration")?.setText(
                        MIText.BaseDurationSeconds.text(current.effectiveDurationTicks / 20.0)
                    )
                    card.find<RecipeArrowElement>("recipe_arrow")?.setProgress(current.execution)
                }
            }
        }
        rebuild()
    }

    private fun bindProgress(bar: ProgressBar?, value: () -> Float) {
        bar ?: return
        val initial = value().coerceIn(0f, 1f)
        bar.setRange(0f, 1f)
        bar.setProgress(initial)
        bar.bind(bindingsS2C({ value().coerceIn(0f, 1f) }, initial).build())
    }

    private fun populateMaterials(
        view: ScrollerView,
        fluidPrototype: FluidSlot?,
        ctx: MultiblockUIContext,
        shape: ShapeTemplate?,
    ) {
        view.clearAllScrollViewChildren()
        val materials = collectMaterials(ctx, shape)
        materials.items.forEach { (item, amount) ->
            val displayAmount = amount.coerceAtLeast(1)
            val stack = ItemStack(item, 1)
            // Construct the ItemSlot with the unlimited Slot from the start,
            // matching miviewer's implementation. Binding a copied editor
            // ItemSlot after construction can leave its serialized LocalSlot
            // as the value source and clamp the visible count to 64.
            val slot = UnlimitedMaterialItemSlot(stack, displayAmount)
                .setItem(stack, false)
                .xeiRecipeIngredient(IngredientIO.INPUT)
                .xeiRecipeSlot(IngredientIO.INPUT, 1f)
            view.addScrollViewChild(slot)
        }
        materials.fluids.forEach { (fluid, amount) ->
            val amountInt = amount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            val slot = (fluidPrototype?.copy() as? FluidSlot ?: FluidSlot())
                .setCapacity(amountInt)
                .setFluid(FluidStack(fluid, amountInt), false)
                .apply {
                    setAllowClickFilled(false)
                    setAllowClickDrained(false)
                }
                .xeiRecipeIngredient(IngredientIO.INPUT)
                .xeiRecipeSlot(IngredientIO.INPUT, 1f)
            view.addScrollViewChild(slot)
        }
    }

    private data class MaterialSet(
        val items: LinkedHashMap<Item, Int> = LinkedHashMap(),
        val fluids: LinkedHashMap<Fluid, Long> = LinkedHashMap(),
    )

    private fun collectMaterials(ctx: MultiblockUIContext, shape: ShapeTemplate?): MaterialSet {
        val result = MaterialSet()
        ctx.machineStack().item.takeIf { it != Items.AIR }?.let { result.items[it] = 1 }
        shape?.simpleMembers?.entries
            ?.sortedWith(compareBy({ it.key.x }, { it.key.y }, { it.key.z }))
            ?.forEach { (_, member) ->
                val state = member.getPreviewState()
                val item = state.block.asItem()
                if (item != Items.AIR) {
                    result.items[item] = (result.items[item] ?: 0) + 1
                } else {
                    addFluidState(result.fluids, state.fluidState)
                }
            }
        return result
    }

    private fun addFluidState(target: LinkedHashMap<Fluid, Long>, state: FluidState) {
        if (state.isEmpty) return
        val fluid = state.type
        if (fluid == Fluids.EMPTY) return
        target[fluid] = (target[fluid] ?: 0L) + 1000L
    }

    private fun configureScene(scene: Scene, ctx: MultiblockUIContext, shape: ShapeTemplate) {
        if (!ctx.isClientSide) return
        val controller = ctx.be?.blockState ?: return
        val world = TrackedDummyWorld()
        shape.simpleMembers.forEach { (pos, member) ->
            world.setBlockAndUpdate(BlockPos(-pos.x, pos.y, pos.z), member.getPreviewState())
        }
        world.setBlockAndUpdate(BlockPos.ZERO, controller)
        val blocks = world.getFilledBlocks().toList().map(BlockPos::of)

        scene.setAfterWorldRender { renderedScene ->
            val hover = renderedScene.getLastHoverPosFace() ?: return@setAfterWorldRender
            val modularUi = renderedScene.getModularUI() ?: return@setAfterWorldRender
            val position = hover.pos()
            val state = world.getBlockState(position)
            val tooltip = ArrayList<Component>()

            val fluid = state.fluidState
            if (!fluid.isEmpty) {
                tooltip += FluidHelper.getDisplayName(FluidStack(fluid.type, 1000))
            } else {
                tooltip += state.block.name
            }

            // miviewer mirrors the x coordinate when it builds the preview
            // world, so its hatch flags are read back with the inverse x.
            val hatchFlags: HatchFlags? = shape.hatchFlags[BlockPos(-position.x, position.y, position.z)]
            if (hatchFlags != null && hatchFlags.values().isNotEmpty()) {
                tooltip += MIText.AcceptsHatches.text()
                hatchFlags.values().forEach { hatchType: HatchType ->
                    tooltip += Component.literal("- ").append(hatchType.description())
                }
            }
            modularUi.setHoverTooltip(tooltip, ItemStack.EMPTY, null, null)
        }
        scene.createScene(world)
            .useOrtho(false)
            .useCacheBuffer(true)
            .syncCompile(false)
            .setTickWorld(false)
            .setRenderedCore(blocks)
            .setRenderFacing(false)
            .setRenderSelect(true)
            .setDraggable(true)
            .setScalable(true)
            .setIntractable(true)
            // Scene applies its built-in item tooltip after the world-render
            // callback. Disable it so the callback can append hatch support
            // information without being overwritten in the same frame.
            .setShowHoverBlockTips(false)

        val automaticZoom = scene.camZoom()
        scene.setZoom((3.6 * Math.pow((automaticZoom / 3.5f).toDouble(), 1.4)).toFloat())
    }

    private fun recipeUi(
        recipe: MachineRecipe?,
        recipeId: ResourceLocation,
        runtime: () -> RecipeRuntime,
    ): UIElement {
        val container = UIElement().setId("recipe_$recipeId")
        container.layout.flexDirection(FlexDirection.COLUMN)
        container.layout.widthPercent(100f)
        container.layout.gapAll(2f)

        if (recipe == null) {
            container.addChild(Label().setText("配方详情正在同步…", false))
            return container
        }

        val initialRuntime = runtime()
        // Mirrors MI's MachineCategory header, but uses the live overclocked
        // EU/t and the duration derived from that EU/t.
        val header = UIElement().setId("recipe_header_$recipeId")
        header.layout.flexDirection(FlexDirection.ROW)
        header.layout.widthPercent(100f)
        header.layout.justifyContent(dev.vfyjxf.taffy.style.AlignContent.SPACE_BETWEEN)
        header.addChild(
            FullEnergyIconElement().setId("recipe_energy_icon")
        )
        header.addChild(Label().also {
            it.setId("recipe_current_eu")
            it.setText(TextHelper.getEuTextTick(initialRuntime.currentEu))
            it.layout.flexGrow(1f)
        })
        header.addChild(Label().also {
            it.setId("recipe_duration")
            it.setText(MIText.BaseDurationSeconds.text(initialRuntime.effectiveDurationTicks / 20.0))
            it.textStyle { style ->
                style.textAlignHorizontal(com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal.RIGHT)
            }
            it.layout.flexGrow(1f)
        })
        container.addChild(header)

        val recipeBody = UIElement().setId("recipe_body_$recipeId")
        recipeBody.layout.flexDirection(FlexDirection.ROW)
        recipeBody.layout.widthPercent(100f)
        recipeBody.layout.alignItems(dev.vfyjxf.taffy.style.AlignItems.CENTER)
        recipeBody.layout.justifyContent(dev.vfyjxf.taffy.style.AlignContent.SPACE_BETWEEN)
        recipeBody.layout.gapAll(3f)

        val inputs = UIElement().setId("recipe_inputs_$recipeId")
        inputs.layout.flexDirection(FlexDirection.COLUMN)
        inputs.layout.gapAll(2f)
        inputs.layout.alignItems(dev.vfyjxf.taffy.style.AlignItems.FLEX_START)
        // Items first, fluids on their own rows below; each row caps at 4 slots.
        if (recipe.itemInputs.isNotEmpty()) {
            inputs.addChild(
                slotRow(
                    "recipe_item_inputs_$recipeId",
                    recipe.itemInputs.mapNotNull { input ->
                        input.ingredient().items.firstOrNull()?.let { item ->
                            recipeItemSlot(item, input.amount, input.probability, IngredientIO.INPUT)
                        }
                    },
                )
            )
        }
        if (recipe.fluidInputs.isNotEmpty()) {
            inputs.addChild(
                slotRow(
                    "recipe_fluid_inputs_$recipeId",
                    recipe.fluidInputs.mapNotNull { input ->
                        input.fluid().stacks.firstOrNull()?.let { fluid ->
                            recipeFluidSlot(fluid.fluid, input.amount, input.probability, IngredientIO.INPUT)
                        }
                    },
                )
            )
        }

        val operation = UIElement().setId("recipe_operation_$recipeId")
        operation.layout.flexDirection(FlexDirection.COLUMN)
        operation.layout.alignItems(dev.vfyjxf.taffy.style.AlignItems.CENTER)
        operation.layout.justifyContent(dev.vfyjxf.taffy.style.AlignContent.CENTER)
        operation.addChild(
            RecipeArrowElement()
                .setProgress(initialRuntime.execution)
                .setId("recipe_arrow")
        )

        val outputs = UIElement().setId("recipe_outputs_$recipeId")
        outputs.layout.flexDirection(FlexDirection.COLUMN)
        outputs.layout.gapAll(2f)
        outputs.layout.alignItems(dev.vfyjxf.taffy.style.AlignItems.FLEX_START)
        // Items first, fluids on their own rows below; each row caps at 4 slots.
        if (recipe.itemOutputs.isNotEmpty()) {
            outputs.addChild(
                slotRow(
                    "recipe_item_outputs_$recipeId",
                    recipe.itemOutputs.map { output ->
                        recipeItemSlot(output.stack, output.amount, output.probability, IngredientIO.OUTPUT)
                    },
                )
            )
        }
        if (recipe.fluidOutputs.isNotEmpty()) {
            outputs.addChild(
                slotRow(
                    "recipe_fluid_outputs_$recipeId",
                    recipe.fluidOutputs.map { output ->
                        recipeFluidSlot(output.fluid, output.amount, output.probability, IngredientIO.OUTPUT)
                    },
                )
            )
        }

        recipeBody.addChildren(inputs, operation, outputs)
        container.addChild(recipeBody)

        container.addEventListener(UIEvents.TICK) {
            val current = runtime()
            header.find<TextElement>("recipe_current_eu")?.setText(TextHelper.getEuTextTick(current.currentEu))
            header.find<TextElement>("recipe_duration")?.setText(
                MIText.BaseDurationSeconds.text(current.effectiveDurationTicks / 20.0)
            )
            operation.find<RecipeArrowElement>("recipe_arrow")?.setProgress(current.execution)
        }
        return container
    }

    private class FullEnergyIconElement : UIElement() {
        private val texture = SpriteTexture.of(
            ResourceLocation.fromNamespaceAndPath("smfcore", "textures/gui/efficiency_energy.png")
        ).setSprite(0, 0, 16, 16)

        init {
            layout.width(10f).height(10f)
        }

        override fun drawBackgroundAdditional(guiContext: GUIContext) {
            super.drawBackgroundAdditional(guiContext)
            texture.draw(
                guiContext.graphics,
                guiContext.localMouseX,
                guiContext.localMouseY,
                getContentX(),
                getContentY(),
                getContentWidth(),
                getContentHeight(),
                guiContext.partialTick,
            )
        }
    }

    private class RecipeArrowElement : UIElement() {
        private var progress: Float = 0f
        // Arrow atlas: the upper 24x17 frame is the filled arrow,
        // the lower 24x17 frame is the grey empty track.
        private val arrowBackground = SpriteTexture.of(
            ResourceLocation.fromNamespaceAndPath("smfcore", "textures/gui/arrow.png")
        ).setSprite(0, 17, 24, 17)
        private val arrowForeground = SpriteTexture.of(
            ResourceLocation.fromNamespaceAndPath("smfcore", "textures/gui/arrow.png")
        ).setSprite(0, 0, 24, 17)

        init {
            layout.width(24f).height(17f)
        }

        fun setProgress(progress: Float): RecipeArrowElement {
            this.progress = progress.coerceIn(0f, 1f)
            return this
        }

        override fun drawBackgroundAdditional(guiContext: GUIContext) {
            super.drawBackgroundAdditional(guiContext)
            val width = getContentWidth()
            val height = getContentHeight()
            if (width <= 0f || height <= 0f) return

            val x = getContentX()
            val y = getContentY()

            // Grey empty track, always visible.
            arrowBackground.draw(
                guiContext.graphics,
                guiContext.localMouseX,
                guiContext.localMouseY,
                x,
                y,
                width,
                height,
                guiContext.partialTick,
            )

            // White fill clipped to the real recipe progress. SpriteTexture
            // scales instead of clipping, so shrink the source sprite to the
            // filled width and draw it at that same width (1:1).
            val clippedWidth = (width * progress).toInt()
            if (clippedWidth <= 0) return
            arrowForeground.setSprite(0, 0, clippedWidth, 17)
            arrowForeground.draw(
                guiContext.graphics,
                guiContext.localMouseX,
                guiContext.localMouseY,
                x,
                y,
                clippedWidth.toFloat(),
                height,
                guiContext.partialTick,
            )
        }
    }

    /**
     * A wrapping slot row that caps at {@link #SLOTS_PER_ROW} slots per line.
     * The row width fits its content but never exceeds the 4-slot maximum, so
     * long slot lists wrap instead of stretching the recipe card horizontally.
     */
    private fun slotRow(id: String, slots: List<UIElement>): UIElement {
        val row = UIElement().setId(id)
        row.layout.flexDirection(FlexDirection.ROW)
        row.layout.flexWrap(FlexWrap.WRAP)
        row.layout.gapAll(SLOT_ROW_GAP)
        row.layout.widthFitContent()
        row.layout.maxWidth(SLOT_ROW_MAX_WIDTH)
        slots.forEach(row::addChild)
        return row
    }

    /**
     * Same tooltip MI shows in its JEI/REI categories: null for 100%, "Not
     * consumed" for 0%, otherwise "Consumption/Production Chance: xx %" in
     * yellow, depending on whether the slot is an input (damage chance) or an
     * output (yield chance).
     */
    private fun probabilityTooltip(probability: Float, input: Boolean): Component? {
        if (probability == 1f) return null
        val text = when {
            probability == 0f -> MIText.NotConsumed.text()
            input -> MIText.ChanceConsumption.text(PROBABILITY_FORMAT.format(probability * 100))
            else -> MIText.ChanceProduction.text(PROBABILITY_FORMAT.format(probability * 100))
        }
        return text.setStyle(TextHelper.YELLOW)
    }

    private fun recipeItemSlot(
        item: ItemStack,
        amount: Int,
        probability: Float,
        io: IngredientIO,
    ): ItemSlot {
        val icon = item.copyWithCount(1)
        val slot = UnlimitedMaterialItemSlot(icon, amount.coerceAtLeast(1))
            .setItem(icon, false)
            .xeiRecipeIngredient(io)
            .xeiRecipeSlot(io, probability)
        probabilityTooltip(probability, io == IngredientIO.INPUT)?.let { slot.style.tooltips(it) }
        return slot
    }

    private fun recipeFluidSlot(
        fluid: Fluid,
        amount: Long,
        probability: Float,
        io: IngredientIO,
    ): FluidSlot {
        val displayAmount = amount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val slot = FluidSlot()
            .setCapacity(displayAmount)
            .setFluid(FluidStack(fluid, displayAmount), false)
            .apply {
                setAllowClickFilled(false)
                setAllowClickDrained(false)
            }
            .xeiRecipeIngredient(io)
            .xeiRecipeSlot(io, probability)
        probabilityTooltip(probability, io == IngredientIO.INPUT)?.let { slot.style.tooltips(it) }
        return slot
    }

    private inline fun <reified T : UIElement> UIElement.find(id: String): T? =
        selectId(id, UIElement::class.java).findFirst().orElse(null) as? T

    private fun UIElement.setAvailable(available: Boolean) {
        setVisible(available)
        setActive(available)
        layout.display(if (available) TaffyDisplay.FLEX else TaffyDisplay.NONE)
    }

    private fun UIElement.setEnabled(enabled: Boolean) {
        setVisible(true)
        setActive(enabled)
    }

    private fun UIElement.setIcon(stack: ItemStack) {
        style.backgroundTexture(ItemStackTexture(stack))
    }

    private fun bindDynamicText(element: TextElement, value: () -> Component) {
        val initial = value()
        element.setText(initial)
        val binding = bindingsS2C(value, initial).build()
        element.addSyncValue(binding.syncValue)
        binding.syncValue.addListener { element.setText(it) }
    }

    private class UnlimitedPreviewContainer : SimpleContainer(1) {
        override fun getMaxStackSize(): Int = Int.MAX_VALUE
    }

    private class UnlimitedPreviewSlot(stack: ItemStack) : Slot(UnlimitedPreviewContainer(), 0, 0, 0) {
        private var previewStack: ItemStack = stack.copy()

        init {
            super.set(stack)
        }

        override fun getItem(): ItemStack = previewStack

        override fun set(stack: ItemStack) {
            previewStack = stack.copy()
        }

        override fun hasItem(): Boolean = !previewStack.isEmpty

        override fun remove(amount: Int): ItemStack = ItemStack.EMPTY

        override fun mayPlace(stack: ItemStack): Boolean = false

        override fun mayPickup(player: Player): Boolean = false

        override fun getMaxStackSize(): Int = Int.MAX_VALUE

        override fun getMaxStackSize(stack: ItemStack): Int = Int.MAX_VALUE
    }

    /**
     * Material entries are legends, not real inventories. Minecraft's normal
     * item decoration path is tied to the item's vanilla max stack size and
     * therefore renders counts above 64 unreliably. Keep the icon stack at one
     * item and pass the real material total as GuiGraphics' alternate count
     * text instead.
     */
    private class UnlimitedMaterialItemSlot(
        stack: ItemStack,
        private val displayAmount: Int,
    ) : ItemSlot(UnlimitedPreviewSlot(stack)) {
        override fun drawItemStack(guiContext: GUIContext, itemStack: ItemStack) {
            DrawerHelper.drawItemStack(
                guiContext.graphics,
                itemStack,
                0,
                0,
                guiContext.elementColor,
                displayAmount.toString(),
            )
        }
    }

    private fun redstoneModuleSlot(ctx: MultiblockUIContext): Slot = mirroredModuleSlot(
        ctx = ctx,
        maxStackSize = 1,
        mayPlace = { MIItem.REDSTONE_CONTROL_MODULE.`is`(it) },
        getServerStack = { ctx.redstoneControl()?.getDrop()?.copy() ?: ItemStack.EMPTY },
        setServerStack = { machine, stack -> ctx.redstoneControl()?.setStackServer(machine, stack) },
    )

    private fun upgradeModuleSlot(ctx: MultiblockUIContext): Slot = mirroredModuleSlot(
        ctx = ctx,
        maxStackSize = 64,
        mayPlace = { UpgradeComponent.getExtraEu(it.item) > 0 },
        getServerStack = { ctx.upgrades()?.getDrop()?.copy() ?: ItemStack.EMPTY },
        setServerStack = { machine, stack -> ctx.upgrades()?.setStackServer(machine, stack) },
    )

    private fun overdriveModuleSlot(ctx: MultiblockUIContext): Slot = mirroredModuleSlot(
        ctx = ctx,
        maxStackSize = 1,
        mayPlace = { MIItem.OVERDRIVE_MODULE.`is`(it) || AdvancedOverdriveComponent.isAdvancedModule(it) },
        getServerStack = { ctx.overdrive()?.getDrop()?.copy() ?: ItemStack.EMPTY },
        setServerStack = { machine, stack -> ctx.overdrive()?.setStackServer(machine, stack) },
    )

    private fun mirroredModuleSlot(
        ctx: MultiblockUIContext,
        maxStackSize: Int,
        mayPlace: (ItemStack) -> Boolean,
        getServerStack: () -> ItemStack,
        setServerStack: (MachineBlockEntity, ItemStack) -> Unit,
    ): Slot = if (ctx.isClientSide) {
        object : Slot(SimpleContainer(1), 0, 0, 0) {
            override fun mayPlace(stack: ItemStack): Boolean = mayPlace(stack)
            override fun getMaxStackSize(): Int = maxStackSize
        }
    } else {
        object : HackySlot(0, 0) {
            override fun getRealStack(): ItemStack = getServerStack()

            override fun setRealStack(stack: ItemStack) {
                ctx.machine?.let { machine -> setServerStack(machine, stack) }
            }

            override fun mayPlace(stack: ItemStack): Boolean = mayPlace(stack)

            override fun getMaxStackSize(): Int = maxStackSize
        }
    }

    private fun MultiblockUIContext.toggleRedstoneMode() {
        val machine = machine ?: return
        val control = redstoneControl() ?: return
        val module = control.getDrop().copy()
        if (module.isEmpty) return
        RedstoneControlModuleItem.setRequiresLowSignal(module, !RedstoneControlModuleItem.isRequiresLowSignal(module))
        control.setStackServer(machine, module)
    }

    private fun MultiblockUIContext.machineStack(): ItemStack =
        be?.blockState?.block?.asItem()?.takeIf { it != Items.AIR }?.defaultInstance
            ?: SMFItems.LARGE_VACUUM_FREEZER.get().defaultInstance
}
