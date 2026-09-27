package dev.nazhida.kivra.chat;

import dev.nazhida.kivra.Kivra;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ChatModule {

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String prefix = Kivra.permissions().prefix(player.getUUID());
        if (prefix == null || prefix.isBlank()) return;

        event.setPlayerChatMessage(event.getPlayerChatMessage().withUnsignedContent(
            Component.literal(prefix + " " + player.getGameProfile().getName() + ": ")
                .append(event.getMessage())
        ));
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) updateTabName(player);
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) updateTabName(player);
    }

    public static void updateTabName(ServerPlayer player) {
        String prefix = Kivra.permissions().prefix(player.getUUID());
        if (prefix == null || prefix.isBlank()) {
            player.setTabListDisplayName(null);
        } else {
            player.setTabListDisplayName(Component.literal(prefix + " " + player.getGameProfile().getName()));
        }
    }
}
