package com.smf.core.mixin;

import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import com.smf.core.machines.ui.VacuumFreezerUIMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives every MI multiblock the shared LDLib2-based GUI: right-clicking with an
 * empty hand opens the editor-authored multiblock template instead of the
 * vanilla MI machine screen. Interactions with an item in hand (hatch
 * placement, module insertion) keep their vanilla behavior. Hatch GUIs are
 * unaffected because they are not MachineBlock instances.
 */
@Mixin(MachineBlock.class)
public class MachineBlockMixin {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void smfcore$openLDLib2UI(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof MultiblockMachineBlockEntity) {
                VacuumFreezerUIMenu.INSTANCE.open((ServerPlayer) player, pos);
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
