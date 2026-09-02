package com.smf.core.machines.ui

import aztech.modern_industrialization.api.machine.holder.CrafterComponentHolder
import aztech.modern_industrialization.machines.MachineBlockEntity
import aztech.modern_industrialization.machines.MachineComponent
import aztech.modern_industrialization.machines.components.ActiveShapeComponent
import aztech.modern_industrialization.machines.components.CrafterComponent
import aztech.modern_industrialization.machines.components.OverdriveComponent
import aztech.modern_industrialization.machines.components.RedstoneControlComponent
import aztech.modern_industrialization.machines.components.UpgradeComponent
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate
import aztech.modern_industrialization.machines.recipe.MachineRecipe
import aztech.modern_industrialization.util.TextHelper
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UI
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Unified LDLib2 UI framework for smfcore multiblocks.
 *
 * Each multiblock uses the editor-authored multiblock_gui.ui.nbt template.
 * MachineUIs only injects runtime data and behavior into that template.
 */
object MultiblockUI {

    /**
     * Builds the ModularUI for a machine from the single shared NBT template.
     */
    fun <T : MultiblockMachineBlockEntity> of(be: T?, player: Player): ModularUI {
        val ctx = MultiblockUIContext(be, player)
        // Apply the built-in "ore" theme globally, then the smfcore overrides that
        // pin every container/panel to the ore preset textures (see
        // assets/smfcore/lss/multiblock.lss).
        return ModularUI(
            UI.of(
                MachineUIs.multiblock(ctx),
                StylesheetManager.ORE_MERGED,
            ),
            player,
        )
    }
}

/**
 * Context handed to the DSL layout: machine block entity, crafter, player and
 * convenience accessors (shape validity, current recipe, per-recipe efficiency).
 */
class MultiblockUIContext(
    val be: MultiblockMachineBlockEntity?,
    val player: Player,
) {
    /**
     * LDLib2 constructs the container UI on both logical sides.  Container
     * slots therefore need an explicit server-real/client-mirror split, just
     * like MI's MachineMenuServer and MachineMenuClient.
     */
    /** The concrete player type is the stable logical-side marker during menu bootstrap. */
    val isClientSide: Boolean
        get() = player !is ServerPlayer

    /** The common MI block-entity base needed by SlotPanel-equivalent slots. */
    val machine: MachineBlockEntity?
        get() = be as? MachineBlockEntity

    val crafter: CrafterComponent?
        get() = (be as? CrafterComponentHolder)?.getCrafterComponent() as? CrafterComponent

    val shapeValid: Boolean get() = be != null && be.isShapeValid()

    /** Current recipe id (from the crafter), or null when idle. */
    fun activeRecipeId(): ResourceLocation? = crafter?.smfActiveRecipeId()

    /** Per-recipe efficiency map (recipe id -> efficiency EU), or null when the
     *  advanced overdrive mixin is not present on the crafter. */
    fun efficiency(): Map<ResourceLocation, Long> = crafter?.smfEfficiency().orEmpty()

    /**
     * The recipe ids represented by an installed overdrive module.  The mixin
     * records every advanced-module recipe with accumulated efficiency; the
     * active recipe is included even before it has accumulated any efficiency.
     */
    fun lockedRecipeIds(): List<ResourceLocation> {
        if (!hasOverdriveModule()) return emptyList()
        return linkedSetOf<ResourceLocation>().apply {
        addAll(efficiency().keys)
        activeRecipeId()?.let(::add)
        }.toList()
    }

    fun matchesMultipleRecipes(): Boolean = crafter?.matchesMultipleRecipes() == true

    fun hasActiveRecipe(): Boolean = crafter?.hasActiveRecipe() == true

    fun isGenerator(): Boolean = be?.javaClass?.simpleName?.contains("Generator", ignoreCase = true) == true

    fun currentEuGeneration(): Long = readLongField("lastEuProduction") ?: 0L

    fun maxEuGeneration(): Long = readLongField("MAX_EU")
        ?: readNestedLongField("fluidConsumer", "maxEuProduction")
        ?: 0L

    fun currentEuGenerationText(): net.minecraft.network.chat.Component =
        TextHelper.getEuTextTick(currentEuGeneration())

    fun maxEuGenerationText(): net.minecraft.network.chat.Component =
        TextHelper.getEuTextTick(maxEuGeneration())

    fun baseRecipeEuText(): net.minecraft.network.chat.Component =
        if (hasActiveRecipe()) TextHelper.getEuTextTick(crafter?.baseRecipeEu ?: 0L)
        else TextHelper.getEuTextTick(0L)

    fun currentRecipeEuText(): net.minecraft.network.chat.Component =
        if (hasActiveRecipe()) TextHelper.getEuTextTick(crafter?.currentRecipeEu ?: 0L)
        else TextHelper.getEuTextTick(0L)

    data class ShapeSelectionPresentation(
        val title: net.minecraft.network.chat.Component,
        val useArrows: Boolean,
        val canDecrease: Boolean,
        val canIncrease: Boolean,
    )

    fun shapeSelectionPresentation(): ShapeSelectionPresentation {
        val selection = (be as? MachineBlockEntity)?.guiComponents?.getNullable(ShapeSelection::class.java)
        val lines = selection?.params.orEmpty()
        if (selection == null || lines.isEmpty()) {
            return ShapeSelectionPresentation(
                net.minecraft.network.chat.Component.literal("多方块结构"),
                true,
                false,
                false,
            )
        }

        val current = lines.indices.map { line ->
            selection.behavior.getCurrentIndex(line).coerceIn(0, lines[line].translations().lastIndex)
        }
        val title = current.mapIndexed { index, value ->
            lines[index].translations()[value]
        }.reduce { left, right -> left.copy().append(" / ").append(right) }
        val first = current.first()
        return ShapeSelectionPresentation(
            title,
            lines.first().useArrows(),
            first > 0,
            first < lines.first().numValues() - 1,
        )
    }

    fun activeRecipeProgress(): Float = crafter?.progress?.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f

    fun recipeProgress(recipeId: ResourceLocation): Float =
        if (recipeId == activeRecipeId()) activeRecipeProgress() else 0f

    fun recipeCurrentEu(recipeId: ResourceLocation): Long {
        val component = crafter ?: return 0L
        if (recipeId == activeRecipeId() && component.hasActiveRecipe()) {
            return component.currentRecipeEu
        }

        val recipe = findRecipe(recipeId) ?: return 0L
        val totalEu = recipe.totalEu.toLong()
        if (totalEu <= 0L) return 0L
        val (ticks, _) = recipeEfficiencyTicks(recipeId)
        val baseEu = max(component.behavior.baseRecipeEu, recipe.eu.toLong())
        val overclockedEu = baseEu + ticks * totalEu / 600L
        return min(totalEu, min(overclockedEu, component.behavior.maxRecipeEu))
    }

    fun recipeEffectiveDurationTicks(recipeId: ResourceLocation): Int {
        val recipe = findRecipe(recipeId) ?: return 0
        val currentEu = recipeCurrentEu(recipeId)
        if (currentEu <= 0L) return 0
        return ceil(recipe.totalEu.toDouble() / currentEu.toDouble()).toInt().coerceAtLeast(1)
    }

    /** Efficiency ticks shown beside one retained recipe's overclock bar. */
    fun recipeEfficiencyTicks(recipeId: ResourceLocation): Pair<Int, Int> {
        val component = crafter ?: return 0 to 0
        if (recipeId == activeRecipeId()) {
            return component.efficiencyTicks to component.maxEfficiencyTicks
        }

        val recipe = findRecipe(recipeId) ?: return 0 to 0
        val totalEu = recipe.totalEu
        if (totalEu <= 0) return 0 to 0
        val baseEu = max(component.behavior.baseRecipeEu, recipe.eu.toLong())
        val targetEu = min(totalEu, component.behavior.maxRecipeEu)
        if (targetEu <= baseEu) return 0 to 0

        val maxTicks = ceil((targetEu - baseEu).toDouble() * 600.0 / totalEu.toDouble())
            .toInt()
            .coerceAtLeast(0)
        val accumulatedTicks = ((efficiency()[recipeId] ?: 0L) / totalEu.toFloat()).toInt()
        return accumulatedTicks.coerceIn(0, maxTicks) to maxTicks
    }

    /**
     * Converts the persistent efficiency-EU accumulator back into MI's 0..1
     * efficiency bar.  For the active recipe we use the authoritative vanilla
     * values; for retained advanced recipes we reproduce MI's max-tick formula
     * from their recipe data and the machine's current EU cap.
     */
    fun overclockProgress(recipeId: ResourceLocation): Float {
        val component = crafter ?: return 0f
        if (recipeId == activeRecipeId()) {
            val maximum = component.maxEfficiencyTicks
            return if (maximum > 0) component.efficiencyTicks.toFloat() / maximum else 0f
        }

        val recipe = findRecipe(recipeId) ?: return 0f
        val totalEu = recipe.totalEu
        if (totalEu <= 0) return 0f
        val behavior = component.behavior
        val baseEu = max(behavior.baseRecipeEu, recipe.eu.toLong())
        val targetEu = min(totalEu, behavior.maxRecipeEu)
        if (targetEu <= baseEu) return 1f

        val maxTicks = ceil((targetEu - baseEu).toDouble() * 600.0 / totalEu.toDouble()).toLong()
        if (maxTicks <= 0) return 0f
        val accumulatedTicks = (efficiency()[recipeId] ?: 0L) / totalEu.toFloat()
        return (accumulatedTicks / maxTicks.toFloat()).coerceIn(0f, 1f)
    }

    /** Client and server both have the synced recipe manager, so this is safe in UI construction. */
    fun findRecipe(recipeId: ResourceLocation): MachineRecipe? =
        be?.level?.recipeManager?.byKey(recipeId)?.orElse(null)?.value() as? MachineRecipe

    // ---------- multiblock shape selection ----------

    private val activeShapeComponent: ActiveShapeComponent?
        get() = runCatching {
            var type: Class<*>? = be?.javaClass
            while (type != null) {
                type.declaredFields.firstOrNull {
                    ActiveShapeComponent::class.java.isAssignableFrom(it.type)
                }?.let { field ->
                    field.isAccessible = true
                    return@runCatching field.get(be) as? ActiveShapeComponent
                }
                type = type.superclass
            }
            null
        }.getOrNull()

    /** All shape templates of the machine (1 for single-shape machines). */
    fun shapeTemplates(): List<ShapeTemplate> =
        activeShapeComponent?.shapeTemplates?.toList()
            ?: be?.let { listOf(it.activeShape) }
            ?: emptyList()

    /** Currently selected shape index. */
    fun shapeIndex(): Int = activeShapeComponent?.getActiveShapeIndex() ?: 0

    /** Switch the active shape on the server. */
    fun incrementShape(delta: Int) {
        val multiblock = be ?: return
        val selection = (be as? MachineBlockEntity)?.guiComponents?.getNullable(ShapeSelection::class.java)
        if (selection != null) {
            selection.behavior.handleClick(0, delta)
        } else {
            activeShapeComponent?.incrementShape(multiblock, delta)
        }
    }

    private fun readLongField(name: String): Long? {
        var type: Class<*>? = be?.javaClass
        while (type != null) {
            runCatching {
                type.getDeclaredField(name).let { field ->
                    field.isAccessible = true
                    val value = field.get(if (java.lang.reflect.Modifier.isStatic(field.modifiers)) null else be)
                    return (value as? Number)?.toLong()
                }
            }
            type = type.superclass
        }
        return null
    }

    private fun readNestedLongField(parentName: String, fieldName: String): Long? {
        var type: Class<*>? = be?.javaClass
        while (type != null) {
            runCatching {
                type.getDeclaredField(parentName).let { parentField ->
                    parentField.isAccessible = true
                    val parent = parentField.get(be) ?: return null
                    var nestedType: Class<*>? = parent.javaClass
                    while (nestedType != null) {
                        runCatching {
                            nestedType.getDeclaredField(fieldName).let { field ->
                                field.isAccessible = true
                                return (field.get(parent) as? Number)?.toLong()
                            }
                        }
                        nestedType = nestedType.superclass
                    }
                }
            }
            type = type.superclass
        }
        return null
    }

    // ---------- MI components (slot panel equivalents) ----------

    private fun <T : MachineComponent> component(cls: Class<T>): T? =
        (be as? MachineBlockEntity)?.components?.getNullable(cls)

    fun redstoneControl(): RedstoneControlComponent? = component(RedstoneControlComponent::class.java)
    fun upgrades(): UpgradeComponent? = component(UpgradeComponent::class.java)
    fun overdrive(): OverdriveComponent? = component(OverdriveComponent::class.java)

    fun redstoneStack(): ItemStack? = redstoneControl()?.getDrop()?.takeIf { !it.isEmpty }
    fun upgradeStack(): ItemStack? = upgrades()?.getDrop()?.takeIf { !it.isEmpty }
    fun overdriveStack(): ItemStack? = overdrive()?.getDrop()?.takeIf { !it.isEmpty }

    /** True if an overdrive module (vanilla or advanced) is installed. */
    fun hasOverdriveModule(): Boolean = overdriveStack() != null

    /** True when the installed overdrive module is the advanced one. */
    val isAdvancedOverdrive: Boolean
        get() = (be as? com.smf.core.machines.AdvancedOverdriveMachine)?.isAdvancedOverdrive() ?: false
}

// ---------------------------------------------------------------------------
// Access to the mixin-injected crafter methods (the interface cannot be attached
// with @Implements due to mixin class-loading visibility, so we call the
// mixed-in methods through cached reflection).
// ---------------------------------------------------------------------------

private val efficiencyMethod: java.lang.reflect.Method by lazy {
    CrafterComponent::class.java.getMethod("smfcore\$getRecipeEfficiency")
}
private val activeRecipeIdMethod: java.lang.reflect.Method by lazy {
    CrafterComponent::class.java.getMethod("smfcore\$getActiveRecipeId")
}

fun CrafterComponent.smfEfficiency(): Map<ResourceLocation, Long> {
    @Suppress("UNCHECKED_CAST")
    return runCatching {
        efficiencyMethod.invoke(this) as Map<ResourceLocation, Long>
    }.getOrDefault(emptyMap())
}

fun CrafterComponent.smfActiveRecipeId(): ResourceLocation? =
    runCatching { activeRecipeIdMethod.invoke(this) as ResourceLocation? }.getOrNull()
