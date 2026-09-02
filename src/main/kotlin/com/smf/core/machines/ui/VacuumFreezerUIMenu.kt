package com.smf.core.machines.ui

import com.lowdragmc.lowdraglib2.gui.factory.IContainerUIHolder
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity
import com.smf.core.SMFCore
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

/**
 * Shared LDLib2 menu for every MI multiblock. The large vacuum freezer remains
 * the gray-test machine, but the holder deliberately resolves the block entity
 * by position so the same editor-authored UI can be used by all MI shapes.
 */
object VacuumFreezerUIMenu {

    val MENUS: DeferredRegister<MenuType<*>> =
        DeferredRegister.create(BuiltInRegistries.MENU, SMFCore.ID)

    val MENU_TYPE: Supplier<MenuType<ModularUIContainerMenu>> = MENUS.register(
        "multiblock_ui",
        Supplier { IMenuTypeExtension.create { id: Int, inv: Inventory, data: RegistryFriendlyByteBuf ->
            createFromNetwork(id, inv, data)
        } }
    )

    private fun createFromNetwork(
        id: Int,
        inv: Inventory,
        data: RegistryFriendlyByteBuf
    ): ModularUIContainerMenu {
        val pos = data.readBlockPos()
        val holder = MultiblockUIHolder(pos)
        return ModularUIContainerMenu(MENU_TYPE.get(), id, inv, holder)
    }

    fun open(player: ServerPlayer, pos: BlockPos) {
        player.openMenu(object : MenuProvider {
            override fun getDisplayName(): Component =
                (player.level().getBlockEntity(pos) as? MultiblockMachineBlockEntity)
                    ?.blockState?.block?.name
                    ?: Component.translatable("container.smfcore.multiblock")

            override fun createMenu(id: Int, inventory: Inventory, p: Player): AbstractContainerMenu {
                return ModularUIContainerMenu(MENU_TYPE.get(), id, inventory, MultiblockUIHolder(pos))
            }

            override fun writeClientSideData(menu: AbstractContainerMenu, buf: RegistryFriendlyByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
    }

    fun registerMenuType(eventBus: IEventBus) {
        MENUS.register(eventBus)
    }

    fun registerScreen(event: RegisterMenuScreensEvent) {
        event.register(MENU_TYPE.get()) { menu, inv, title ->
            com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen(menu, inv, title)
        }
    }
}

/**
 * UI holder carrying the machine position. Builds the LDLib2 UI from the
 * block entity on both sides.
 */
class MultiblockUIHolder(private val pos: BlockPos) : IContainerUIHolder {
    override fun createUI(player: Player): com.lowdragmc.lowdraglib2.gui.ui.ModularUI {
        val be = player.level().getBlockEntity(pos) as? MultiblockMachineBlockEntity
        return MultiblockUI.of(be, player)
    }

    override fun isStillValid(player: Player): Boolean =
        player.level().getBlockEntity(pos) is MultiblockMachineBlockEntity
}
