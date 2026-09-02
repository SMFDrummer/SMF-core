package com.smf.core.mixin;

import com.smf.core.mixinutil.SMFMixinHelpers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void smfcore$makeDrillsEnchantable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(SMFMixinHelpers.DRILLS)) {
            cir.setReturnValue(true);
        }
    }
}
