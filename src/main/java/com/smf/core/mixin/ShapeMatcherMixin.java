package com.smf.core.mixin;

import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import com.smf.core.machines.LargeVacuumFreezerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla MI links every hatch of a multiblock to the shape template's core
 * casing (hatch.link(template.hatchCasing)), so hatches always render with the
 * machine's base texture. For the large vacuum freezer the energy input hatches
 * sit on clean stainless steel casing (shape key 'b'), so they are relinked to
 * MachineCasings.CLEAN_STAINLESS_STEEL instead of the frostproof core.
 */
@Mixin(ShapeMatcher.class)
public class ShapeMatcherMixin {
    @Shadow
    protected BlockPos controllerPos;

    @Redirect(
            method = "rematch",
            at = @At(value = "INVOKE", target = "Laztech/modern_industrialization/machines/multiblocks/HatchBlockEntity;link(Laztech/modern_industrialization/machines/models/MachineCasing;)V"))
    private void smfcore$linkHatchWithCasing(HatchBlockEntity hatch, MachineCasing casing) {
        Level level = hatch.getLevel();
        MachineCasing resolved = casing;
        if (level != null
                && level.getBlockEntity(controllerPos) instanceof LargeVacuumFreezerBlockEntity
                && hatch.getHatchType() == HatchTypes.ENERGY_INPUT) {
            resolved = MachineCasings.CLEAN_STAINLESS_STEEL;
        }
        hatch.link(resolved);
    }
}