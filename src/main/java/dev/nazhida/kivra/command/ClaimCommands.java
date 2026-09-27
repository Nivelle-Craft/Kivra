package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.nazhida.kivra.Kivra;
import dev.nazhida.kivra.claims.ClaimService.Access;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ClaimCommands {
    private ClaimCommands(){}
    public static void register(CommandDispatcher<CommandSourceStack>d){
        d.register(Commands.literal("claim").requires(s->allowed(s,"kivra.claim.use"))
            .then(Commands.literal("pos1").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();Kivra.claims().setPos1(p);msg(c.getSource(),"Claim position 1 set.");return 1;}))
            .then(Commands.literal("pos2").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();Kivra.claims().setPos2(p);msg(c.getSource(),"Claim position 2 set.");return 1;}))
            .then(Commands.literal("create").requires(s->allowed(s,"kivra.claim.create")).then(Commands.argument("name",StringArgumentType.word()).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();boolean ok=Kivra.claims().create(p,StringArgumentType.getString(c,"name"));msg(c.getSource(),ok?"Claim created.":"Could not create claim. Check selection, overlap, dimension or claim limit.");return ok?1:0;})))
            .then(Commands.literal("delete").requires(s->allowed(s,"kivra.claim.delete")).then(Commands.argument("name",StringArgumentType.word()).executes(c->{boolean ok=Kivra.claims().delete(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"),false);msg(c.getSource(),ok?"Claim deleted.":"Claim not found or not yours.");return ok?1:0;})))
            .then(Commands.literal("list").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();msg(c.getSource(),"Claims ("+Kivra.claims().owned(p.getUUID()).size()+"/"+(Kivra.claims().maxClaims(p)==Integer.MAX_VALUE?"unlimited":Kivra.claims().maxClaims(p))+"): "+String.join(", ",Kivra.claims().owned(p.getUUID())));return 1;}))
            .then(Commands.literal("trust").requires(s->allowed(s,"kivra.claim.trust")).then(Commands.argument("name",StringArgumentType.word()).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("access",StringArgumentType.word()).executes(c->{ServerPlayer owner=c.getSource().getPlayerOrException(),target=EntityArgument.getPlayer(c,"player");Access access;try{access=Access.valueOf(StringArgumentType.getString(c,"access").toUpperCase());}catch(Exception e){msg(c.getSource(),"Access must be interact or full.");return 0;}if(access==Access.NONE){msg(c.getSource(),"Use /claim untrust instead.");return 0;}boolean ok=Kivra.claims().trust(owner,StringArgumentType.getString(c,"name"),target.getUUID(),access);msg(c.getSource(),ok?"Player trusted with "+access.name().toLowerCase()+" access.":"Could not trust player.");return ok?1:0;})))))
            .then(Commands.literal("untrust").requires(s->allowed(s,"kivra.claim.trust")).then(Commands.argument("name",StringArgumentType.word()).then(Commands.argument("player",EntityArgument.player()).executes(c->{ServerPlayer owner=c.getSource().getPlayerOrException(),target=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.claims().untrust(owner,StringArgumentType.getString(c,"name"),target.getUUID());msg(c.getSource(),ok?"Player untrusted.":"Player was not trusted or claim is not yours.");return ok?1:0;}))))
            .then(Commands.literal("bypass").requires(s->allowed(s,"kivra.claim.bypass")).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();boolean enabled=Kivra.claims().toggleBypass(p);msg(c.getSource(),"Claim bypass "+(enabled?"enabled.":"disabled."));return 1;})));
    }
    private static boolean allowed(CommandSourceStack s,String node){if(s.hasPermission(4))return true;try{return Kivra.permissions().hasPermission(s.getPlayerOrException(),node);}catch(Exception e){return false;}}
    private static void msg(CommandSourceStack s,String m){s.sendSuccess(()->Component.literal(m),false);}
}
