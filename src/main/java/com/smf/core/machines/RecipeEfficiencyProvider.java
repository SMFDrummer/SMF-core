package com.smf.core.machines;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Exposed by the crafter of machines that track per-recipe efficiency
 * (advanced overdrive). Implemented via mixin on CrafterComponent.
 */
public interface RecipeEfficiencyProvider {
    /**
     * Snapshot of the per-recipe efficiency map (recipe id -> efficiency EU).
     */
    Map<ResourceLocation, Long> getRecipeEfficiency();

    /**
     * Id of the currently active recipe, or null when idle.
     */
    ResourceLocation getActiveRecipeId();
}