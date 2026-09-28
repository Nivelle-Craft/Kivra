package dev.nazhida.kivra.integration;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.UUID;

/** Optional runtime bridge. Kivra does not require Wanderlog to be installed. */
public final class WanderlogIntegration {
    private WanderlogIntegration() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("wanderlog");
    }

    public static Stats stats(MinecraftServer server, UUID playerId) {
        if (!isLoaded()) return null;
        try {
            Class<?> api = Class.forName("dev.nazhida.wanderlog.api.WanderlogApi");
            Method getStats = api.getMethod("getStats", MinecraftServer.class, UUID.class);
            Object value = getStats.invoke(null, server, playerId);
            Class<?> type = value.getClass();
            int level = (int) type.getMethod("level").invoke(value);
            int xp = (int) type.getMethod("xp").invoke(value);
            int next = (int) type.getMethod("xpForNextLevel").invoke(value);
            int biomes = (int) type.getMethod("discoveredBiomes").invoke(value);
            return new Stats(level, xp, next, biomes);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    public record Stats(int level, int xp, int xpForNextLevel, int discoveredBiomes) {}
}
