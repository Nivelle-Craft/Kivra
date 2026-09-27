package dev.nazhida.kivra.api;

import dev.nazhida.kivra.Kivra;
import dev.nazhida.kivra.permissions.PermissionService;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/** Public entry point for integrations with Kivra. */
public final class KivraAPI {
    private KivraAPI() {}

    public static PermissionService permissions() {
        return Kivra.permissions();
    }

    public static boolean hasPermission(ServerPlayer player, String node) {
        return permissions().hasPermission(player, node);
    }

    public static boolean hasPermission(UUID playerId, String node) {
        return permissions().hasPermission(playerId, node);
    }
}
