package dev.nazhida.kivra.admin;

import dev.nazhida.kivra.Kivra;
import dev.nazhida.kivra.permissions.PermissionGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AdminMenuEvents {
    @SubscribeEvent
    public void onOpen(PlayerContainerEvent.Open event) {
        // Reserved for future per-menu session tracking.
    }

    public static void openPlayers(ServerPlayer viewer) {
        viewer.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> {
            ChestMenu menu = ChestMenu.sixRows(id, inventory);
            int slot = 0;
            for (ServerPlayer target : viewer.getServer().getPlayerList().getPlayers()) {
                if (slot >= 45) break;
                ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                PermissionGroup rank = Kivra.permissions().primaryGroup(target.getUUID());
                head.setHoverName(Component.literal(target.getGameProfile().getName()));
                head.getOrCreateTag().putString("KivraAdminAction", "player:" + target.getUUID());
                head.getOrCreateTag().putString("KivraAdminDescription", "Rank: " + (rank == null ? "default" : rank.name()));
                menu.getContainer().setItem(slot++, head);
            }
            button(menu, 49, Items.ARROW, "Back", "Back to Kivra Admin", "back");
            return menu;
        }, Component.literal("Kivra Admin - Players")));
    }

    public static void openPlayer(ServerPlayer viewer, ServerPlayer target) {
        viewer.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> {
            ChestMenu menu = ChestMenu.threeRows(id, inventory);
            PermissionGroup rank = Kivra.permissions().primaryGroup(target.getUUID());
            button(menu, 10, Items.PLAYER_HEAD, target.getGameProfile().getName(), "Primary rank: " + (rank == null ? "default" : rank.name()), "noop");
            button(menu, 12, Items.NAME_TAG, "Change Rank", "Open rank selector", "ranks:" + target.getUUID());
            button(menu, 14, Items.PAPER, "Permissions", "Groups: " + String.join(", ", Kivra.permissions().effectiveGroups(target.getUUID())), "noop");
            button(menu, 16, Items.ARROW, "Back", "Back to players", "players");
            return menu;
        }, Component.literal("Kivra - " + target.getGameProfile().getName())));
    }

    public static void openRanks(ServerPlayer viewer, ServerPlayer target) {
        viewer.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> {
            ChestMenu menu = ChestMenu.sixRows(id, inventory);
            List<PermissionGroup> groups = new ArrayList<>(Kivra.permissions().groups());
            groups.sort(Comparator.comparingInt(PermissionGroup::weight).reversed());
            int slot = 0;
            for (PermissionGroup group : groups) {
                if (slot >= 45) break;
                boolean active = Kivra.permissions().effectiveGroups(target.getUUID()).contains(group.name());
                button(menu, slot++, active ? Items.LIME_DYE : Items.GRAY_DYE, group.name(), (active ? "Assigned" : "Not assigned") + " | weight " + group.weight(), "rank:" + target.getUUID() + ":" + group.name());
            }
            button(menu, 49, Items.ARROW, "Back", "Back to player", "player:" + target.getUUID());
            return menu;
        }, Component.literal("Kivra Ranks - " + target.getGameProfile().getName())));
    }

    static void button(ChestMenu menu, int slot, net.minecraft.world.item.Item item, String name, String description, String action) {
        ItemStack stack = new ItemStack(item);
        stack.setHoverName(Component.literal(name));
        stack.getOrCreateTag().putString("KivraAdminAction", action);
        stack.getOrCreateTag().putString("KivraAdminDescription", description);
        menu.getContainer().setItem(slot, stack);
    }
}
