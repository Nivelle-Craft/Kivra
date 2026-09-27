package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.nazhida.kivra.Kivra;
import dev.nazhida.kivra.moderation.ModerationService.Record;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public final class ModerationCommands {
    private ModerationCommands(){}
    public static void register(CommandDispatcher<CommandSourceStack>d){
        d.register(Commands.literal("kick").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");p.connection.disconnect(Component.literal("Kicked: "+r));msg(c.getSource(),"Player kicked.");return 1;}))));
        d.register(Commands.literal("warn").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");Kivra.moderation().warn(p.getUUID(),p.getGameProfile().getName(),staff(c.getSource()),r);p.sendSystemMessage(Component.literal("Warning: "+r));msg(c.getSource(),"Warning saved.");return 1;}))));
        d.register(Commands.literal("ban").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");Kivra.moderation().ban(p.getUUID(),p.getGameProfile().getName(),staff(c.getSource()),r,0);p.connection.disconnect(Component.literal("Banned: "+r));msg(c.getSource(),"Player banned.");return 1;}))));
        d.register(Commands.literal("mute").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");Kivra.moderation().mute(p.getUUID(),p.getGameProfile().getName(),staff(c.getSource()),r,0);msg(c.getSource(),"Player muted.");return 1;}))));
        d.register(Commands.literal("unmute").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.moderation().unmute(p.getUUID(),staff(c.getSource()));msg(c.getSource(),ok?"Player unmuted.":"Player is not muted.");return ok?1:0;})));
        d.register(Commands.literal("warnings").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");List<Record> all=Kivra.moderation().history(p.getUUID());long warns=all.stream().filter(x->x.type.equals("WARN")).count();msg(c.getSource(),p.getGameProfile().getName()+" warnings: "+warns+" | history entries: "+all.size());return 1;})));
    }
    private static UUID staff(CommandSourceStack s){try{return s.getPlayerOrException().getUUID();}catch(Exception e){return new UUID(0,0);}}
    private static void msg(CommandSourceStack s,String m){s.sendSuccess(()->Component.literal(m),false);}
}
