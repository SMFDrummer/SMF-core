package com.smf.core.mixin;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.inventory.SlotGroup;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import com.smf.core.machines.AdvancedOverdriveComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The vanilla overdrive slot only accepts modern_industrialization:overdrive_module
 * (SlotType.OVERDRIVE_MODULE insertion checker). Extend it so the smfcore advanced
 * overdrive module can also be inserted from the machine GUI.
 */
@Mixin(SlotPanel.SlotType.class)
public class SlotTypeMixin {
    @Shadow
    @Final
    public SlotGroup group;

    @Inject(method = "mayPlace(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void smfcore$mayPlaceAdvancedOverdrive(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.group == SlotGroup.OVERDRIVE_MODULE) {
            cir.setReturnValue(
                    MIItem.OVERDRIVE_MODULE.is(stack) || AdvancedOverdriveComponent.isAdvancedModule(stack));
        }
    }
}