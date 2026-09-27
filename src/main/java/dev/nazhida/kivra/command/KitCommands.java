package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.nazhida.kivra.Kivra;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class KitCommands {
    private KitCommands(){}
    public static void register(CommandDispatcher<CommandSourceStack>d){
        d.register(Commands.literal("kits").executes(c->{msg(c.getSource(),"Kits: "+String.join(", ",Kivra.kits().names()));return 1;}));
        d.register(Commands.literal("kit").then(Commands.argument("name",StringArgumentType.word()).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();String n=StringArgumentType.getString(c,"name");long remaining=Kivra.kits().remaining(p.getUUID(),n);if(remaining<0){msg(c.getSource(),"Kit not found.");return 0;}if(remaining>0){msg(c.getSource(),"Kit cooldown: "+remaining+"s remaining.");return 0;}boolean ok=Kivra.kits().give(p,n,false);msg(c.getSource(),ok?"Kit received.":"Could not receive kit.");return ok?1:0;})));
        d.register(Commands.literal("kivra").requires(s->s.hasPermission(4)).then(Commands.literal("kit")
            .then(Commands.literal("create").then(Commands.argument("name",StringArgumentType.word()).executes(c->{boolean ok=Kivra.kits().create(StringArgumentType.getString(c,"name"),c.getSource().getPlayerOrException());msg(c.getSource(),ok?"Kit created from your inventory.":"Kit already exists.");return ok?1:0;})))
            .then(Commands.literal("delete").then(Commands.argument("name",StringArgumentType.word()).executes(c->{boolean ok=Kivra.kits().delete(StringArgumentType.getString(c,"name"));msg(c.getSource(),ok?"Kit deleted.":"Kit not found.");return ok?1:0;})))
            .then(Commands.literal("cooldown").then(Commands.argument("name",StringArgumentType.word()).then(Commands.argument("seconds",LongArgumentType.longArg(0)).executes(c->{boolean ok=Kivra.kits().setCooldown(StringArgumentType.getString(c,"name"),LongArgumentType.getLong(c,"seconds"));msg(c.getSource(),ok?"Kit cooldown updated.":"Kit not found.");return ok?1:0;}))))
            .then(Commands.literal("give").then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("name",StringArgumentType.word()).executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.kits().give(p,StringArgumentType.getString(c,"name"),true);msg(c.getSource(),ok?"Kit given.":"Kit not found.");return ok?1:0;}))))));
    }
    private static void msg(CommandSourceStack s,String m){s.sendSuccess(()->Component.literal(m),false);}
}
