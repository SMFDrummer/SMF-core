package com.smf.core.client.jei;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.client.machines.gui.MachineScreen;
import com.smf.core.SMFCore;
import com.smf.core.machines.LargeGasGeneratorBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * "Liquefied Gas Fuels" JEI category, modeled after MI's FluidFuelsCategory.
 * Shows every fluid the Large Gas Generator can burn, with a methane bucket icon.
 */
public class LiquefiedGasFuelsCategory extends AbstractRecipeCategory<Fluid> {
    public static final RecipeType<Fluid> TYPE = RecipeType.create(SMFCore.ID, "liquefied_gas_fuels", Fluid.class);

    private final IDrawable fluidSlot;

    public LiquefiedGasFuelsCategory(IJeiHelpers helpers) {
        super(
                TYPE,
                Component.translatable("jei.smfcore.liquefied_gas_fuels"),
                helpers.getGuiHelper().createDrawableItemStack(new ItemStack(MIFluids.METHANE.getBucket())),
                150 - 8,
                35 - 8);
        this.fluidSlot = helpers.getGuiHelper().createDrawable(MachineScreen.SLOT_ATLAS, 18, 0, 18, 18);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Fluid recipe, IFocusGroup focuses) {
        // Same rendering as MI's own FluidFuelsCategory (via ViewerCategoryJei.variant):
        // add the full-bucket amount with the default fluid renderer and a slot background,
        // so the sprite shows full without a small "1 mB" amount label on hover.
        builder.addSlot(RecipeIngredientRole.INPUT, 15 - 4, 10 - 4)
                .addFluidStack(recipe, FluidType.BUCKET_VOLUME, DataComponentPatch.EMPTY)
                .setBackground(fluidSlot, -1, -1);
    }

    @Override
    public void draw(Fluid recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(-4, -4, 0);
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei.smfcore.eu_per_mb", LargeGasGeneratorBlockEntity.euPerMb(recipe)),
                40,
                14,
                0xFF404040,
                false);
        guiGraphics.pose().popPose();
    }

    @Override
    public ResourceLocation getRegistryName(Fluid recipe) {
        return BuiltInRegistries.FLUID.getKey(recipe);
    }
}