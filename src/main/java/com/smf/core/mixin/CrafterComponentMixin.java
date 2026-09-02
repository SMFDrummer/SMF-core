package com.smf.core.mixin;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.stats.PlayerStatistics;
import aztech.modern_industrialization.util.Simulation;
import com.smf.core.machines.AdvancedOverdriveMachine;
import com.smf.core.mixinutil.SMFMixinHelpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.smf.core.mixinutil.SMFMixinHelpers.ADAMANTINE;

@Mixin(CrafterComponent.class)
public class CrafterComponentMixin {
    @Shadow
    private CrafterComponent.Inventory inventory;

    // ---------- Advanced overdrive: persistent efficiency across recipe switches ----------

    @Shadow
    private CrafterComponent.Behavior behavior;
    @Shadow
    private MachineProcessCondition.Context conditionContext;
    @Shadow
    private RecipeHolder<MachineRecipe> activeRecipe;
    @Shadow
    private long usedEnergy;
    @Shadow
    private long recipeEnergy;
    @Shadow
    private long recipeMaxEu;
    @Shadow
    private int efficiencyTicks;
    @Shadow
    private int maxEfficiencyTicks;
    @Shadow
    private long previousBaseEu;
    @Shadow
    private long previousMaxEu;
    @Shadow
    private int lastForcedTick;

    @Shadow
    private void loadDelayedActiveRecipe() {
    }

    @Shadow
    private boolean updateActiveRecipe() {
        return false;
    }

    @Shadow
    protected void clearLocks() {
    }

    @Shadow
    protected boolean putItemOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        return false;
    }

    @Shadow
    protected boolean putFluidOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        return false;
    }

    @Shadow
    private int getRecipeMaxEfficiencyTicks(MachineRecipe recipe) {
        return 0;
    }

    @Shadow
    private boolean shouldUpdateActiveRecipe() {
        return false;
    }

    @Shadow
    private boolean canStartRecipe(MachineRecipe recipe) {
        return false;
    }

    @Shadow
    private boolean tryStartRecipe(MachineRecipe recipe) {
        return false;
    }

    @Shadow
    protected boolean takeItemInputs(MachineRecipe recipe, boolean simulate) {
        return false;
    }

    private static final org.slf4j.Logger SMF_LOGGER =
            org.slf4j.LoggerFactory.getLogger("smfcore");

    @Shadow
    private static Collection<RecipeHolder<MachineRecipe>> getRecipes(
            net.minecraft.server.level.ServerLevel level,
            aztech.modern_industrialization.machines.recipe.MachineRecipeType recipeType,
            List<ConfigurableItemStack> itemInputs) {
        return List.of();
    }

    /**
     * Per-recipe efficiency accumulation ("multi-threaded" overclock): each recipe
     * keeps its own independent efficiency amount in EU units, so switching recipes
     * never resets another recipe's progress and the machine may run different
     * recipes back to back. The GUI shows the efficiency of the currently running
     * recipe. recipeMaxEu = baseEu + efficiency / 600 (same math as vanilla
     * efficiencyTicks * totalEu / 600, just decoupled from the locked recipe).
     */
    @Unique
    private final Map<ResourceLocation, Long> smfcore$efficiencyByRecipe = new HashMap<>();

    @Unique
    private boolean smfcore$isPersistent() {
        return behavior instanceof AdvancedOverdriveMachine advanced && advanced.isAdvancedOverdrive();
    }

    @Unique
    public Map<ResourceLocation, Long> smfcore$getRecipeEfficiency() {
        return new HashMap<>(smfcore$efficiencyByRecipe);
    }

    @Unique
    public ResourceLocation smfcore$getActiveRecipeId() {
        return activeRecipe == null ? null : activeRecipe.id();
    }

    @Unique
    private long smfcore$efficiencyFor(ResourceLocation recipeId) {
        return smfcore$efficiencyByRecipe.getOrDefault(recipeId, 0L);
    }

    @Unique
    private long smfcore$maxEuForPersistent() {
        long baseEu = Math.max(behavior.getBaseRecipeEu(), activeRecipe.value().eu);
        long overclocked = baseEu + smfcore$efficiencyFor(activeRecipe.id()) / 600L;
        return Math.min(recipeEnergy, Math.min(overclocked, behavior.getMaxRecipeEu()));
    }

    /**
     * Like the vanilla updateActiveRecipe but without the output-slot overlap
     * restriction and without the vanilla "locked recipe" enumeration: recipes are
     * enumerated from the machine recipe type directly (the vanilla getRecipes()
     * only returns the current recipe while efficiency ticks are non-zero, which
     * would prevent switching). When {@code force} is true the 100-tick cooldown
     * and the inventory hash check are bypassed entirely: this is used while the
     * machine is overdriving-locked, so switching inputs switches the recipe on
     * the very next tick ("free" recipe switching).
     */
    @Unique
    private boolean smfcore$updateActiveRecipePersistent(boolean force) {
        if (!force && !shouldUpdateActiveRecipe()) {
            return false;
        }
        RecipeHolder<MachineRecipe> newActiveRecipe = null;
        for (RecipeHolder<MachineRecipe> recipe : getRecipes(
                behavior.getCrafterWorld(), behavior.recipeType(), inventory.getItemInputs())) {
            if (canStartRecipe(recipe.value())) {
                newActiveRecipe = recipe;
                break;
            }
        }
        if (newActiveRecipe == null) {
            return false;
        }
        // Consume the inputs and lock the outputs, exactly like the vanilla
        // updateActiveRecipe does via tryStartRecipe. Without this the inputs are
        // never actually consumed, so the finished recipe stays matchable forever
        // and the machine never switches to another recipe.
        if (!tryStartRecipe(newActiveRecipe.value())) {
            return false;
        }
        if (activeRecipe != newActiveRecipe || efficiencyTicks == 0) {
            maxEfficiencyTicks = getRecipeMaxEfficiencyTicks(newActiveRecipe.value());
        }
        activeRecipe = newActiveRecipe;
        usedEnergy = 0;
        recipeEnergy = newActiveRecipe.value().getTotalEu();
        return true;
    }

    @Inject(method = "tickRecipe", at = @At("HEAD"), cancellable = true)
    private void smfcore$tickRecipePersistent(CallbackInfoReturnable<Boolean> cir) {
        if (!smfcore$isPersistent()) {
            // The advanced overdrive module was removed (or replaced with a vanilla
            // module): drop the per-recipe efficiency accumulation so the overclock
            // recipe list is cleared, matching the vanilla overdrive behavior.
            if (!smfcore$efficiencyByRecipe.isEmpty()) {
                smfcore$efficiencyByRecipe.clear();
            }
            return;
        }
        cir.setReturnValue(smfcore$tickPersistentImpl());
    }

    /**
     * Copy of the vanilla tickRecipe scheduling, but each recipe has its own
     * independent efficiency accumulation and the machine may switch recipes
     * immediately (like the vanilla overdrive module, the finished recipe stays
     * locked at full EU draw while waiting for inputs, but the input changes switch
     * to the new recipe right away without resetting its efficiency).
     */
    @Unique
    private boolean smfcore$tickPersistentImpl() {
        boolean isActive;
        boolean isEnabled = behavior.isEnabled();

        loadDelayedActiveRecipe();

        boolean recipeStarted = false;
        if (usedEnergy == 0 && isEnabled) {
            if (behavior.consumeEu(1, Simulation.SIMULATE) == 1) {
                // While overdriving-locked (finished recipe waiting for inputs) we
                // re-match every tick with force=true so that switching inputs
                // switches the recipe immediately.
                boolean locked = activeRecipe != null && behavior.isOverdriving();
                recipeStarted = smfcore$updateActiveRecipePersistent(locked);
            }
        }

        if (activeRecipe != null) {
            lastForcedTick = 0;
        }

        long eu = 0;
        boolean finishedRecipe = false;
        if (activeRecipe != null && isEnabled) {
            if (usedEnergy > 0 || recipeStarted) {
                recipeMaxEu = smfcore$maxEuForPersistent();
                eu = activeRecipe.value().conditionsMatch(conditionContext)
                        ? behavior.consumeEu(Math.min(recipeMaxEu, recipeEnergy - usedEnergy), Simulation.ACT)
                        : 0;
                isActive = eu > 0;
                usedEnergy += eu;

                if (usedEnergy == recipeEnergy) {
                    putItemOutputs(activeRecipe.value(), false, false);
                    putFluidOutputs(activeRecipe.value(), false, false);
                    clearLocks();
                    usedEnergy = 0;
                    finishedRecipe = true;
                    behavior.onCraft();
                }
            } else if (behavior.isOverdriving()) {
                eu = activeRecipe.value().conditionsMatch(conditionContext) ? behavior.consumeEu(recipeMaxEu, Simulation.ACT) : 0;
                isActive = eu > 0;
            } else {
                isActive = false;
            }
        } else {
            isActive = false;
        }

        if (activeRecipe != null) {
            if (previousBaseEu != behavior.getBaseRecipeEu() || previousMaxEu != behavior.getMaxRecipeEu()) {
                previousBaseEu = behavior.getBaseRecipeEu();
                previousMaxEu = behavior.getMaxRecipeEu();
                maxEfficiencyTicks = getRecipeMaxEfficiencyTicks(activeRecipe.value());
            }
        }

        // Per-recipe efficiency accumulation: only the currently running recipe's
        // progress is touched (each recipe keeps its own "thread" of overclock).
        if (finishedRecipe && activeRecipe != null) {
            smfcore$efficiencyByRecipe.merge(activeRecipe.id(), recipeEnergy, Long::sum);
        } else if (eu < recipeMaxEu && activeRecipe != null) {
            long current = smfcore$efficiencyFor(activeRecipe.id());
            if (current > 0) {
                smfcore$efficiencyByRecipe.put(activeRecipe.id(), Math.max(0, current - recipeEnergy));
            }
        }

        // Keep the vanilla display ticks in sync (the GUI reads efficiencyTicks /
        // maxEfficiencyTicks): one vanilla tick corresponds to totalEu/600 extra EU
        // per tick, i.e. efficiency / totalEu ticks for the current recipe.
        if (activeRecipe != null && recipeEnergy > 0) {
            efficiencyTicks = (int) Math.min(
                    maxEfficiencyTicks,
                    Math.min(Integer.MAX_VALUE, smfcore$efficiencyFor(activeRecipe.id()) / recipeEnergy));
        }

        // Persistent mode: like the vanilla overdrive module, the finished recipe
        // stays locked at full EU draw while waiting for inputs (efficiency never
        // drains). Unlike vanilla, when the inputs change the persistent matcher
        // switches to the new recipe right away (no output-overlap restriction),
        // and every recipe keeps its own independent efficiency accumulation.

        return isActive;
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void smfcore$writeEfficiencyByRecipe(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        CompoundTag map = new CompoundTag();
        for (Map.Entry<ResourceLocation, Long> entry : smfcore$efficiencyByRecipe.entrySet()) {
            map.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("smfcoreEfficiencyByRecipe", map);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void smfcore$readEfficiencyByRecipe(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine, CallbackInfo ci) {
        smfcore$efficiencyByRecipe.clear();
        CompoundTag map = tag.getCompound("smfcoreEfficiencyByRecipe");
        for (String key : map.getAllKeys()) {
            smfcore$efficiencyByRecipe.put(ResourceLocation.parse(key), map.getLong(key));
        }
    }

    /**
     * Efficiency: reduce the recipe's total EU (= duration) following the vanilla
     * efficiency formula (mining speed f -> f + level^2 + 1, base speed 4).
     * Works for both item and fluid recipes.
     */
    @Redirect(
            method = "updateActiveRecipe",
            at = @At(value = "INVOKE", target = "Laztech/modern_industrialization/machines/recipe/MachineRecipe;getTotalEu()J"))
    private long smfcore$scaledTotalEu(MachineRecipe recipe) {
        int level = SMFMixinHelpers.maxEnchantmentLevel(inventory.getItemInputs(), Enchantments.EFFICIENCY);
        if (level > 0) {
            long totalEu = recipe.getTotalEu();
            return Math.max(1L, (long) Math.ceil(totalEu * 4.0 / (4.0 + level * level + 1.0)));
        }
        return recipe.getTotalEu();
    }

    /**
     * Adamantine: never damage the drill, i.e. force item input probabilities to 0.
     */
    @Redirect(
            method = "takeItemInputs(Ljava/util/List;Laztech/modern_industrialization/stats/PlayerStatistics;Laztech/modern_industrialization/machines/recipe/MachineRecipe;Z)Z",
            at = @At(value = "INVOKE", target = "Laztech/modern_industrialization/machines/recipe/MachineRecipe$ItemInput;probability()F"))
    private static float smfcore$adamantineProbability(
            MachineRecipe.ItemInput input,
            List<ConfigurableItemStack> baseList, PlayerStatistics stats, MachineRecipe recipe, boolean simulate) {
        if (input.probability() < 1.0f && SMFMixinHelpers.maxEnchantmentLevel(baseList, ADAMANTINE) > 0) {
            return 0.0f;
        }
        return input.probability();
    }

    /**
     * Fortune: boost item output probabilities. Fluid outputs are untouched.
     */
    @Redirect(
            method = "putItemOutputs",
            at = @At(value = "INVOKE", target = "Laztech/modern_industrialization/machines/recipe/MachineRecipe$ItemOutput;probability()F"))
    private float smfcore$fortuneProbability(
            MachineRecipe.ItemOutput output,
            MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        float prob = output.probability();
        int fortune = SMFMixinHelpers.maxEnchantmentLevel(inventory.getItemInputs(), Enchantments.FORTUNE);
        if (prob < 1.0f && fortune > 0) {
            return Math.min(1.0f, prob * (1.0f + fortune * 0.5f));
        }
        return prob;
    }
}
