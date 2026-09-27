package dev.nazhida.kivra.admin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/** Server-side admin container. Admin slots are buttons, never movable items. */
public final class AdminChestMenu extends ChestMenu {
    private final int adminSlots;

    private AdminChestMenu(MenuType<?> type, int id, Inventory inventory, Container container, int rows) {
        super(type, id, inventory, container, rows);
        this.adminSlots = rows * 9;
    }

    public static AdminChestMenu threeRows(int id, Inventory inventory) {
        return new AdminChestMenu(MenuType.GENERIC_9x3, id, inventory, new SimpleContainer(27), 3);
    }

    public static AdminChestMenu sixRows(int id, Inventory inventory) {
        return new AdminChestMenu(MenuType.GENERIC_9x6, id, inventory, new SimpleContainer(54), 6);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < adminSlots) {
            ItemStack clicked = getSlot(slotId).getItem();
            if (player instanceof ServerPlayer serverPlayer) {
                AdminMenuEvents.handleClick(serverPlayer, clicked.copy());
            }
            return;
        }
        // Do not allow shift-clicks from the player inventory into the admin GUI.
        if (clickType == ClickType.QUICK_MOVE) return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
