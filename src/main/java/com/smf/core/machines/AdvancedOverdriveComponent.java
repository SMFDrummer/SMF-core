package com.smf.core.machines;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.OverdriveComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Overdrive slot that also accepts the advanced overdrive module
 * (smfcore:advanced_overdrive_module). The advanced module keeps the machine's
 * efficiency accumulation when switching recipes (handled by the crafter mixin);
 * the vanilla module keeps its vanilla behavior.
 */
public class AdvancedOverdriveComponent extends OverdriveComponent {

    private static final ResourceLocation ADVANCED_MODULE_ID =
            ResourceLocation.fromNamespaceAndPath("smfcore", "advanced_overdrive_module");

    public static boolean isAdvancedModule(ItemStack stack) {
        return stack.getItem().builtInRegistryHolder().is(ADVANCED_MODULE_ID);
    }

    public boolean isAdvanced() {
        return isAdvancedModule(getDrop());
    }

    @Override
    public ItemInteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (MIItem.OVERDRIVE_MODULE.is(stackInHand) || isAdvancedModule(stackInHand)) {
            if (getDrop().isEmpty()) {
                setStackServer(be, stackInHand.copyWithCount(1));
                stackInHand.consume(1, player);
                return ItemInteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}