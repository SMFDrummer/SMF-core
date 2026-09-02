package com.smf.core.machines

import aztech.modern_industrialization.machines.BEP
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity
import aztech.modern_industrialization.machines.components.OrientationComponent
import aztech.modern_industrialization.machines.components.OverdriveComponent
import aztech.modern_industrialization.machines.components.UpgradeComponent
import aztech.modern_industrialization.machines.guicomponents.SlotPanel
import aztech.modern_industrialization.machines.init.MachineTier
import aztech.modern_industrialization.machines.models.MachineCasings
import aztech.modern_industrialization.machines.multiblocks.HatchFlags
import aztech.modern_industrialization.machines.multiblocks.HatchTypes
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate
import aztech.modern_industrialization.machines.multiblocks.SimpleMember
import aztech.modern_industrialization.machines.recipe.MachineRecipeType
import com.smf.core.SMFCore
import com.smf.core.blocks.SMFBlocks
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

/**
 * Large Vacuum Freezer: a big freezing chamber built from frostproof casing with a
 * stainless steel pipe spine and tempered glass inspection windows. The controller
 * sits on top. Layout mirrors the kubejs reference shape:
 * - 'a' frostproof casing: item/fluid input-output hatches
 * - 'b' clean stainless casing: energy input hatch
 * - 'c' MI stainless steel casing pipe, 'd' smfcore stainless pipe, 'e' tempered glass: no hatch
 *
 * Supports upgrades and the advanced overdrive module (persistent efficiency across
 * recipe switches, see AdvancedOverdriveComponent / CrafterComponentMixin).
 */
class LargeVacuumFreezerBlockEntity(bep: BEP) :
    AbstractElectricCraftingMultiblockBlockEntity(
        bep,
        ResourceLocation.fromNamespaceAndPath(SMFCore.ID, ID),
        OrientationComponent.Params(false, false, false),
        arrayOf(SHAPE)
    ), AdvancedOverdriveMachine {

    private val upgrades = UpgradeComponent()
    private val overdrive = AdvancedOverdriveComponent()

    init {
        registerComponents(upgrades, overdrive)
        registerGuiComponent(
            SlotPanel(this)
                .withRedstoneControl(redstoneControl)
                .withUpgrades(upgrades)
                .withOverdrive(overdrive)
        )
    }

    override fun recipeType(): MachineRecipeType = SMFMachines.LARGE_VACUUM_FREEZER_RECIPE_TYPE

    override fun getBaseRecipeEu(): Long = MachineTier.MULTIBLOCK.getBaseEu().toLong()

    override fun getMaxRecipeEu(): Long =
        MachineTier.MULTIBLOCK.getMaxEu() + upgrades.getAddMaxEUPerTick()

    override fun isOverdriving(): Boolean = overdrive.shouldOverdrive()

    override fun isAdvancedOverdrive(): Boolean = overdrive.isAdvanced()

    companion object {
        const val ID = "large_vacuum_freezer"

        private val frostproofFlags: HatchFlags = HatchFlags.Builder()
            .with(
                HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT,
                HatchTypes.FLUID_INPUT, HatchTypes.FLUID_OUTPUT
            )
            .build()
        private val cleanFlags: HatchFlags = HatchFlags.Builder()
            .with(HatchTypes.ENERGY_INPUT)
            .build()

        private val frostproof: SimpleMember =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "frostproof_machine_casing"))
        private val cleanStainless: SimpleMember =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "clean_stainless_steel_machine_casing"))
        private val miStainlessPipe: SimpleMember =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "stainless_steel_machine_casing_pipe"))
        private val temperedGlass: SimpleMember =
            SimpleMember.forBlockId(SMFCore.id("tempered_glass"))
        private val smfStainlessPipe: SimpleMember =
            SimpleMember.forBlockId(SMFCore.id("stainless_steel_machine_pipe_casing"))

        // 6 layers (y0..y5), 7 rows of z (z0..z6), 10 chars per row (x0..x9);
        // controller '#' at layer 5, row 1, column 3. Mirrors the kubejs reference.
        private val LAYERS: Array<Array<String>> = arrayOf(
            arrayOf(
                "aaaaaaa bbb",
                "aaaaaaa bcb",
                "aaaaaaa bcb",
                "aaaaaaa bcb",
                "aaaaaaa bbb",
                "aaaaaaa    ",
                "aaaaaaa    "
            ),
            arrayOf(
                "aaaaaaa bcb",
                "adddddddddc",
                "ad d da cdc",
                "adddddddddc",
                "ad d da bcb",
                "addddda    ",
                "aaaaaaa    "
            ),
            arrayOf(
                "aaaaaaa bcb",
                "ad d da cdc",
                "a     a cdc",
                "ad   da cdc",
                "a     a bcb",
                "ad d da    ",
                "aaaaaaa    "
            ),
            arrayOf(
                "aaaaaaa bcb",
                "ad d dddddc",
                "a     a cdc",
                "ad   dddddc",
                "a     a bcb",
                "ad d da    ",
                "aaaaaaa    "
            ),
            arrayOf(
                "aaaaaaa bbb",
                "addddda bcb",
                "ad   da bcb",
                "ad   da bcb",
                "ad   da bbb",
                "addddda    ",
                "aaaaaaa    "
            ),
            arrayOf(
                " aaaaa     ",
                " aa#aa     ",
                " aaaaa     ",
                " aeeea     ",
                " aeeea     ",
                " aaaaa     ",
                "           "
            )
        )

        val SHAPE: ShapeTemplate = ShapeTemplate.LayeredBuilder(
            MachineCasings.FROSTPROOF,
            LAYERS
        )
            .key('a', frostproof, frostproofFlags)
            .key('b', cleanStainless, cleanFlags)
            .key('c', miStainlessPipe, HatchFlags.NO_HATCH)
            .key('d', smfStainlessPipe, HatchFlags.NO_HATCH)
            .key('e', temperedGlass, HatchFlags.NO_HATCH)
            .build()

        fun create(pos: BlockPos, state: BlockState): LargeVacuumFreezerBlockEntity =
            LargeVacuumFreezerBlockEntity(
                BEP(
                    SMFBlocks.LARGE_VACUUM_FREEZER_BLOCK_ENTITY.get() as BlockEntityType<*>,
                    pos,
                    state
                )
            )
    }
}