package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.nazhida.kivra.Kivra;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class EssentialsCommands {
    private EssentialsCommands() {}
    public static void register(CommandDispatcher<CommandSourceStack> d){
        d.register(Commands.literal("setspawn").requires(s->allowed(s,"kivra.essentials.setspawn")).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();Kivra.essentials().setSpawn(p);msg(c.getSource(),"Spawn set.");return 1;}));
        d.register(Commands.literal("spawn").requires(s->allowed(s,"kivra.essentials.spawn")).executes(c->{boolean ok=Kivra.essentials().spawn(c.getSource().getPlayerOrException());msg(c.getSource(),ok?"Teleported to spawn.":"Spawn is not set.");return ok?1:0;}));
        d.register(Commands.literal("sethome").requires(s->allowed(s,"kivra.essentials.home")).then(Commands.argument("name",StringArgumentType.word()).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();Kivra.essentials().setHome(p,StringArgumentType.getString(c,"name"));msg(c.getSource(),"Home saved.");return 1;})));
        d.register(Commands.literal("home").requires(s->allowed(s,"kivra.essentials.home")).then(Commands.argument("name",StringArgumentType.word()).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();boolean ok=Kivra.essentials().home(p,StringArgumentType.getString(c,"name"));msg(c.getSource(),ok?"Teleported home.":"Home not found.");return ok?1:0;})));
        d.register(Commands.literal("delhome").requires(s->allowed(s,"kivra.essentials.home")).then(Commands.argument("name",StringArgumentType.word()).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();boolean ok=Kivra.essentials().delHome(p,StringArgumentType.getString(c,"name"));msg(c.getSource(),ok?"Home deleted.":"Home not found.");return ok?1:0;})));
        d.register(Commands.literal("homes").requires(s->allowed(s,"kivra.essentials.home")).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();msg(c.getSource(),"Homes: "+String.join(", ",Kivra.essentials().homes(p.getUUID())));return 1;}));
        d.register(Commands.literal("setwarp").requires(s->allowed(s,"kivra.essentials.warp.admin")).then(Commands.argument("name",StringArgumentType.word()).executes(c->{Kivra.essentials().setWarp(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"));msg(c.getSource(),"Warp saved.");return 1;})));
        d.register(Commands.literal("warp").requires(s->allowed(s,"kivra.essentials.warp")).then(Commands.argument("name",StringArgumentType.word()).executes(c->{boolean ok=Kivra.essentials().warp(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"));msg(c.getSource(),ok?"Teleported to warp.":"Warp not found.");return ok?1:0;})));
        d.register(Commands.literal("delwarp").requires(s->allowed(s,"kivra.essentials.warp.admin")).then(Commands.argument("name",StringArgumentType.word()).executes(c->{boolean ok=Kivra.essentials().delWarp(StringArgumentType.getString(c,"name"));msg(c.getSource(),ok?"Warp deleted.":"Warp not found.");return ok?1:0;})));
        d.register(Commands.literal("warps").requires(s->allowed(s,"kivra.essentials.warp")).executes(c->{msg(c.getSource(),"Warps: "+String.join(", ",Kivra.essentials().warps()));return 1;}));
        d.register(Commands.literal("tpa").requires(s->allowed(s,"kivra.essentials.tpa")).then(Commands.argument("player",EntityArgument.player()).executes(c->{ServerPlayer from=c.getSource().getPlayerOrException(),to=EntityArgument.getPlayer(c,"player");if(from.getUUID().equals(to.getUUID())){msg(c.getSource(),"You cannot TPA to yourself.");return 0;}Kivra.essentials().request(from,to);msg(c.getSource(),"TPA request sent.");to.sendSystemMessage(Component.literal(from.getGameProfile().getName()+" sent you a TPA request. Use /tpaccept or /tpdeny."));return 1;})));
        d.register(Commands.literal("tpaccept").requires(s->allowed(s,"kivra.essentials.tpa")).executes(c->{ServerPlayer target=c.getSource().getPlayerOrException();ServerPlayer from=Kivra.essentials().accept(target);boolean ok=from!=null;msg(c.getSource(),ok?"TPA accepted.":"No pending TPA request.");if(from!=null)from.sendSystemMessage(Component.literal("TPA accepted."));return ok?1:0;}));
        d.register(Commands.literal("tpdeny").requires(s->allowed(s,"kivra.essentials.tpa")).executes(c->{boolean ok=Kivra.essentials().deny(c.getSource().getPlayerOrException());msg(c.getSource(),ok?"TPA denied.":"No pending TPA request.");return ok?1:0;}));
    }
    private static boolean allowed(CommandSourceStack s,String node){if(s.hasPermission(4))return true;try{return Kivra.permissions().hasPermission(s.getPlayerOrException(),node);}catch(Exception e){return false;}}
    private static void msg(CommandSourceStack s,String m){s.sendSuccess(()->Component.literal(m),false);}
}
