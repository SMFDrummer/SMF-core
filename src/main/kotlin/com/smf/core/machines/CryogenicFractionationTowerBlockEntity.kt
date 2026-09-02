package com.smf.core.machines

import aztech.modern_industrialization.MIText
import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder
import aztech.modern_industrialization.machines.BEP
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity
import aztech.modern_industrialization.machines.components.OrientationComponent
import aztech.modern_industrialization.machines.components.OverdriveComponent
import aztech.modern_industrialization.machines.components.UpgradeComponent
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection
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
import java.util.stream.IntStream
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

/**
 * Cryogenic Fractionation Tower: separates helium-rich natural gas into methane
 * (for the Large Gas Generator / plastic chain) and helium (for the He-3 chain).
 * Adjustable height (1-9 layers) like MI's distillation tower, same shape style:
 * each layer is a solid titanium row over a frostproof row with a titanium pipe spine.
 */
class CryogenicFractionationTowerBlockEntity(bep: BEP) :
    AbstractElectricCraftingMultiblockBlockEntity(
        bep,
        ResourceLocation.fromNamespaceAndPath(SMFCore.ID, ID),
        OrientationComponent.Params(false, false, false),
        SHAPE_TEMPLATES
    ), EnergyListComponentHolder {

    private val upgrades = UpgradeComponent()
    private val overdrive = OverdriveComponent()

    init {
        registerComponents(upgrades, overdrive)
        registerGuiComponent(
            SlotPanel(this)
                .withRedstoneControl(redstoneControl)
                .withUpgrades(upgrades)
                .withOverdrive(overdrive)
        )
        registerGuiComponent(
            ShapeSelection(
                object : ShapeSelection.Behavior {
                    override fun handleClick(clickedLine: Int, delta: Int) {
                        activeShape.incrementShape(this@CryogenicFractionationTowerBlockEntity, delta)
                    }

                    override fun getCurrentIndex(line: Int): Int = activeShape.getActiveShapeIndex()
                },
                ShapeSelection.LineInfo(
                    (1..MAX_HEIGHT).map { MIText.ShapeTextHeight.text(it) as Component },
                    false
                )
            )
        )
    }

    override fun recipeType(): MachineRecipeType = SMFMachines.CRYOGENIC_FRACTIONATION_TOWER_RECIPE_TYPE

    override fun getBaseRecipeEu(): Long = MachineTier.MULTIBLOCK.getBaseEu().toLong()

    override fun getMaxRecipeEu(): Long =
        MachineTier.MULTIBLOCK.getMaxEu() + upgrades.getAddMaxEUPerTick()

    override fun isOverdriving(): Boolean = overdrive.shouldOverdrive()

    override fun getMaxFluidOutputs(): Int = activeShape.getActiveShapeIndex() + 1

    companion object {
        const val ID = "cryogenic_fractionation_tower"
        const val MAX_HEIGHT = 9

        private val solidTitanium: SimpleMember =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "solid_titanium_machine_casing"))
        private val frostproof: SimpleMember =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "frostproof_machine_casing"))
        private val titaniumPipe: SimpleMember =
            SimpleMember.forBlockId(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "titanium_machine_casing_pipe"))

        private val bottomFlags: HatchFlags = HatchFlags.Builder()
            .with(HatchTypes.ENERGY_INPUT, HatchTypes.FLUID_INPUT)
            .build()
        private val layerFlags: HatchFlags = HatchFlags.Builder()
            .with(HatchTypes.FLUID_OUTPUT)
            .build()

        val SHAPE_TEMPLATES: Array<ShapeTemplate> = buildShapeTemplates()

        private fun buildShapeTemplates(): Array<ShapeTemplate> {
            return Array(MAX_HEIGHT) { h ->
                val n = h + 1  // number of frostproof layers (1..9)
                val builder = ShapeTemplate.Builder(MachineCasings.SOLID_TITANIUM)
                // Bottom row (y=0): solid titanium, holds the controller; accepts
                // energy + fluid input hatches.
                for (z in 0..2) {
                    for (x in -1..1) {
                        builder.add(x, 0, z, solidTitanium, bottomFlags)
                    }
                }
                // Upper rows (y=1..n): frostproof casing with a titanium pipe spine
                // on the middle z-plane; each row accepts fluid output hatches.
                for (y in 1..n) {
                    for (z in 0..2) {
                        for (x in -1..1) {
                            if (!(z == 1 && x == 0)) {
                                builder.add(x, y, z, frostproof, layerFlags)
                            }
                        }
                        if (z == 1) {
                            builder.add(0, y, 1, titaniumPipe, null)
                        }
                    }
                }
                builder.build()
            }
        }

        fun create(pos: BlockPos, state: BlockState): CryogenicFractionationTowerBlockEntity =
            CryogenicFractionationTowerBlockEntity(
                BEP(
                    SMFBlocks.CRYOGENIC_FRACTIONATION_TOWER_BLOCK_ENTITY.get() as BlockEntityType<*>,
                    pos,
                    state
                )
            )
    }
}