package dev.nazhida.kivra.command;

import com.mojang.authlib.GameProfile;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ModerationCommands {
    private static final Pattern DURATION=Pattern.compile("^(\\d+)(s|m|h|d|w)$",Pattern.CASE_INSENSITIVE);
    private ModerationCommands(){}
    public static void register(CommandDispatcher<CommandSourceStack>d){
        d.register(Commands.literal("kick").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");p.connection.disconnect(Component.literal("Kicked: "+r));msg(c.getSource(),"Player kicked.");return 1;}))));
        d.register(Commands.literal("warn").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");Kivra.moderation().warn(p.getUUID(),p.getGameProfile().getName(),staff(c.getSource()),r);p.sendSystemMessage(Component.literal("Warning: "+r));msg(c.getSource(),"Warning saved.");return 1;}))));
        d.register(Commands.literal("ban").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");Kivra.moderation().ban(p.getUUID(),p.getGameProfile().getName(),staff(c.getSource()),r,0);p.connection.disconnect(Component.literal("Banned: "+r));msg(c.getSource(),"Player permanently banned.");return 1;}))));
        d.register(Commands.literal("tempban").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("duration",StringArgumentType.word()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{long ms=parseDuration(StringArgumentType.getString(c,"duration"));if(ms<=0){msg(c.getSource(),"Invalid duration. Examples: 30m, 12h, 7d, 2w.");return 0;}ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");Kivra.moderation().ban(p.getUUID(),p.getGameProfile().getName(),staff(c.getSource()),r,ms);p.connection.disconnect(Component.literal("Temporarily banned ("+StringArgumentType.getString(c,"duration")+"): "+r));msg(c.getSource(),"Temporary ban applied.");return 1;})))));
        d.register(Commands.literal("mute").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");Kivra.moderation().mute(p.getUUID(),p.getGameProfile().getName(),staff(c.getSource()),r,0);msg(c.getSource(),"Player permanently muted.");return 1;}))));
        d.register(Commands.literal("tempmute").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("duration",StringArgumentType.word()).then(Commands.argument("reason",StringArgumentType.greedyString()).executes(c->{long ms=parseDuration(StringArgumentType.getString(c,"duration"));if(ms<=0){msg(c.getSource(),"Invalid duration. Examples: 30m, 12h, 7d, 2w.");return 0;}ServerPlayer p=EntityArgument.getPlayer(c,"player");String r=StringArgumentType.getString(c,"reason");Kivra.moderation().mute(p.getUUID(),p.getGameProfile().getName(),staff(c.getSource()),r,ms);msg(c.getSource(),"Temporary mute applied.");return 1;})))));
        d.register(Commands.literal("unmute").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.moderation().unmute(p.getUUID(),staff(c.getSource()));msg(c.getSource(),ok?"Player unmuted.":"Player is not muted.");return ok?1:0;})));
        d.register(Commands.literal("unban").requires(s->s.hasPermission(3)).then(Commands.argument("player",StringArgumentType.word()).executes(c->{String name=StringArgumentType.getString(c,"player");GameProfile profile=c.getSource().getServer().getProfileCache().get(name).orElse(null);if(profile==null){msg(c.getSource(),"Player profile not found. They must have joined this server before.");return 0;}boolean ok=Kivra.moderation().unban(profile.getId(),staff(c.getSource()));msg(c.getSource(),ok?"Player unbanned.":"Player is not banned.");return ok?1:0;})));
        d.register(Commands.literal("warnings").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");List<Record> all=Kivra.moderation().history(p.getUUID());long warns=all.stream().filter(x->x.type.equals("WARN")).count();msg(c.getSource(),p.getGameProfile().getName()+" warnings: "+warns+" | history entries: "+all.size());return 1;})));
        d.register(Commands.literal("history").requires(s->s.hasPermission(3)).then(Commands.argument("player",EntityArgument.player()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");List<Record> all=Kivra.moderation().history(p.getUUID());if(all.isEmpty()){msg(c.getSource(),"No moderation history for "+p.getGameProfile().getName()+".");return 1;}msg(c.getSource(),"History for "+p.getGameProfile().getName()+" ("+all.size()+" entries):");int start=Math.max(0,all.size()-10);for(int i=start;i<all.size();i++){Record r=all.get(i);String expiry=r.until==0?"permanent":("until "+new java.util.Date(r.until));msg(c.getSource(),"#"+(i+1)+" "+r.type+" | "+r.reason+" | "+expiry);}return 1;})));
    }
    private static long parseDuration(String value){Matcher m=DURATION.matcher(value);if(!m.matches())return -1;try{long n=Long.parseLong(m.group(1));long multiplier=switch(m.group(2).toLowerCase()){case "s"->1000L;case "m"->60_000L;case "h"->3_600_000L;case "d"->86_400_000L;case "w"->604_800_000L;default->0L;};return Math.multiplyExact(n,multiplier);}catch(Exception e){return -1;}}
    private static UUID staff(CommandSourceStack s){try{return s.getPlayerOrException().getUUID();}catch(Exception e){return new UUID(0,0);}}
    private static void msg(CommandSourceStack s,String m){s.sendSuccess(()->Component.literal(m),false);}
}
