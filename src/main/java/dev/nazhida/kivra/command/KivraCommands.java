package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.nazhida.kivra.Kivra;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class KivraCommands {
    private KivraCommands() {}
    private static void msg(CommandSourceStack s,String m){s.sendSuccess(()->Component.literal(m),false);}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("kivra")
            .executes(c->{msg(c.getSource(),"Kivra v0.1.0 — Permissions");return 1;})
            .then(Commands.literal("group").requires(s->s.hasPermission(4))
                .then(Commands.literal("create").then(word("group").executes(c->{String g=str(c,"group");boolean ok=Kivra.permissions().createGroup(g);msg(c.getSource(),ok?"Created group: "+g:"Could not create group.");return ok?1:0;})))
                .then(Commands.literal("delete").then(word("group").executes(c->{String g=str(c,"group");boolean ok=Kivra.permissions().deleteGroup(g);msg(c.getSource(),ok?"Deleted group: "+g:"Could not delete group.");return ok?1:0;})))
                .then(Commands.literal("permission").then(word("group")
                    .then(Commands.literal("add").then(word("node").executes(c->{boolean ok=Kivra.permissions().addPermissionToGroup(str(c,"group"),str(c,"node"));msg(c.getSource(),ok?"Permission added.":"Could not add permission.");return ok?1:0;})))
                    .then(Commands.literal("remove").then(word("node").executes(c->{boolean ok=Kivra.permissions().removePermissionFromGroup(str(c,"group"),str(c,"node"));msg(c.getSource(),ok?"Permission removed.":"Could not remove permission.");return ok?1:0;})))))
                .then(Commands.literal("parent").then(word("group")
                    .then(Commands.literal("add").then(word("parent").executes(c->{boolean ok=Kivra.permissions().addParent(str(c,"group"),str(c,"parent"));msg(c.getSource(),ok?"Parent added.":"Could not add parent.");return ok?1:0;})))
                    .then(Commands.literal("remove").then(word("parent").executes(c->{boolean ok=Kivra.permissions().removeParent(str(c,"group"),str(c,"parent"));msg(c.getSource(),ok?"Parent removed.":"Could not remove parent.");return ok?1:0;})))))
                .then(Commands.literal("prefix").then(word("group").then(Commands.argument("prefix",StringArgumentType.greedyString()).executes(c->{boolean ok=Kivra.permissions().setPrefix(str(c,"group"),StringArgumentType.getString(c,"prefix"));msg(c.getSource(),ok?"Prefix updated.":"Unknown group.");return ok?1:0;}))))
                .then(Commands.literal("weight").then(word("group").then(Commands.argument("weight",IntegerArgumentType.integer()).executes(c->{boolean ok=Kivra.permissions().setWeight(str(c,"group"),IntegerArgumentType.getInteger(c,"weight"));msg(c.getSource(),ok?"Weight updated.":"Unknown group.");return ok?1:0;})))))
            .then(Commands.literal("user").requires(s->s.hasPermission(4)).then(Commands.argument("player",EntityArgument.player())
                .then(Commands.literal("group")
                    .then(Commands.literal("add").then(word("group").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.permissions().addGroup(p.getUUID(),str(c,"group"));msg(c.getSource(),ok?"Group added to "+p.getGameProfile().getName()+".":"Could not add group.");return ok?1:0;})))
                    .then(Commands.literal("remove").then(word("group").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.permissions().removeGroup(p.getUUID(),str(c,"group"));msg(c.getSource(),ok?"Group removed from "+p.getGameProfile().getName()+".":"Could not remove group.");return ok?1:0;}))))
                .then(Commands.literal("permission")
                    .then(Commands.literal("add").then(word("node").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.permissions().addUserPermission(p.getUUID(),str(c,"node"));msg(c.getSource(),ok?"User permission added.":"Could not add permission.");return ok?1:0;})))
                    .then(Commands.literal("remove").then(word("node").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.permissions().removeUserPermission(p.getUUID(),str(c,"node"));msg(c.getSource(),ok?"User permission removed.":"Could not remove permission.");return ok?1:0;}))))));
    }
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack,String> word(String name){return Commands.argument(name,StringArgumentType.word());}
    private static String str(com.mojang.brigadier.context.CommandContext<CommandSourceStack> c,String name){return StringArgumentType.getString(c,name);}
}
