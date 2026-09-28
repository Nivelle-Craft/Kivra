package dev.nazhida.kivra.admin;

import dev.nazhida.kivra.integration.WanderlogIntegration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AdminMenu {
    private AdminMenu() {}

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> {
            AdminChestMenu menu = AdminChestMenu.threeRows(id, inventory);
            set(menu, 10, Items.PLAYER_HEAD, "Players", "Manage players and their Kivra data");
            set(menu, 11, Items.NAME_TAG, "Ranks & Permissions", "Groups, inheritance and permission nodes");
            set(menu, 12, Items.GOLD_INGOT, "Economy", "Balances and economy administration");
            set(menu, 13, Items.GOLDEN_SHOVEL, "Claims", "Protected regions and trusted players");
            set(menu, 14, Items.EMERALD, "Shops", "Player shops and admin shops");
            set(menu, 15, Items.CHEST, "Kits", "Create and manage server kits");
            set(menu, 16, Items.IRON_SWORD, "Moderation", "Bans, mutes, warnings and history");
            if (WanderlogIntegration.isLoaded()) {
                set(menu, 20, Items.COMPASS, "Wanderlog", "Explorer levels, XP and discovered biomes");
            }
            set(menu, 22, Items.BARRIER, "Close", "Close Kivra Admin");
            return menu;
        }, Component.literal("Kivra Admin")));
    }

    private static void set(ChestMenu menu, int slot, net.minecraft.world.item.Item item, String name, String description) {
        ItemStack stack = new ItemStack(item);
        stack.setHoverName(Component.literal(name));
        stack.getOrCreateTag().putString("KivraAdminAction", name);
        stack.getOrCreateTag().putString("KivraAdminDescription", description);
        menu.getContainer().setItem(slot, stack);
    }
}
