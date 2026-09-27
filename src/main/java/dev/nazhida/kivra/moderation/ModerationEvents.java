package dev.nazhida.kivra.moderation;

import dev.nazhida.kivra.Kivra;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ModerationEvents {
    @SubscribeEvent public void onLogin(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){ModerationService.Punishment ban=Kivra.moderation().ban(p.getUUID());if(ban!=null)p.connection.disconnect(Component.literal("You are banned: "+ban.reason));}}
    @SubscribeEvent public void onChat(ServerChatEvent e){ModerationService.Punishment mute=Kivra.moderation().mute(e.getPlayer().getUUID());if(mute!=null){e.setCanceled(true);e.getPlayer().sendSystemMessage(Component.literal("You are muted: "+mute.reason));}}
}
