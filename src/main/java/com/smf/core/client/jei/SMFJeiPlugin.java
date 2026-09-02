package com.smf.core.client.jei;

import com.smf.core.SMFCore;
import com.smf.core.blocks.SMFBlocks;
import com.smf.core.machines.SMFGasGenerator;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class SMFJeiPlugin implements IModPlugin {
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new LiquefiedGasFuelsCategory(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(SMFBlocks.INSTANCE.getLARGE_GAS_GENERATOR().get()),
                LiquefiedGasFuelsCategory.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(LiquefiedGasFuelsCategory.TYPE, SMFGasGenerator.INSTANCE.fuelFluids());
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(SMFCore.ID, "viewer");
    }
}