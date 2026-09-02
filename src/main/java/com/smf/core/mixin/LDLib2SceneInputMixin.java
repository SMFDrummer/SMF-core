package com.smf.core.mixin;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Makes Scene's pan gesture use the right mouse button instead of middle. */
@Mixin(Scene.class)
public class LDLib2SceneInputMixin {
    @Inject(method = "onMouseDown", at = @At("HEAD"))
    private void smfcore$useRightButtonForPan(UIEvent event, CallbackInfo ci) {
        if (!"multiblock_preview_scene".equals(((Scene) (Object) this).getId())) {
            return;
        }
        if (event.button == 1) {
            event.button = 2;
        } else if (event.button == 2) {
            // Keep the original middle-button branch unreachable; right drag
            // is now the only pan gesture for the multiblock preview scene.
            event.button = -1;
        }
    }
}
