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

        event.setMessage(
            Component.literal(prefix + " " + player.getGameProfile().getName() + ": ")
                .append(event.getMessage())
        );
    }

    @SubscribeEvent
    public void onNameFormat(PlayerEvent.NameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String prefix = Kivra.permissions().prefix(player.getUUID());
        if (prefix == null || prefix.isBlank()) return;

        event.setDisplayname(
            Component.literal(prefix + " ").append(event.getUsername())
        );
    }

    @SubscribeEvent
    public void onTabFormat(PlayerEvent.TabListNameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String prefix = Kivra.permissions().prefix(player.getUUID());
        if (prefix == null || prefix.isBlank()) return;

        event.setDisplayName(
            Component.literal(prefix + " " + player.getGameProfile().getName())
        );
    }
}
