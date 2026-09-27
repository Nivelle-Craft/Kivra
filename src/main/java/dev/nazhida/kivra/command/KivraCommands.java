package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.nazhida.kivra.Kivra;
import dev.nazhida.kivra.permissions.DurationParser;
import dev.nazhida.kivra.permissions.PermissionGroup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.stream.Collectors;

public final class KivraCommands {
    private KivraCommands() {}
    private static void msg(CommandSourceStack s,String m){s.sendSuccess(()->Component.literal(m),false);}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("kivra")
            .executes(c->{msg(c.getSource(),"Kivra v0.1.0 — Permissions");return 1;})
            .then(Commands.literal("groups").executes(c->{String list=Kivra.permissions().groups().stream().map(PermissionGroup::name).collect(Collectors.joining(", "));msg(c.getSource(),"Groups: "+list);return 1;}))
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
                .then(Commands.literal("info").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");PermissionGroup primary=Kivra.permissions().primaryGroup(p.getUUID());msg(c.getSource(),p.getGameProfile().getName()+" | primary="+(primary==null?"default":primary.name())+" | groups="+String.join(", ",Kivra.permissions().effectiveGroups(p.getUUID()))+" | prefix="+Kivra.permissions().prefix(p.getUUID()));return 1;}))
                .then(Commands.literal("group")
                    .then(Commands.literal("add").then(word("group").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.permissions().addGroup(p.getUUID(),str(c,"group"));msg(c.getSource(),ok?"Group added.":"Could not add group.");return ok?1:0;})))
                    .then(Commands.literal("remove").then(word("group").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.permissions().removeGroup(p.getUUID(),str(c,"group"));msg(c.getSource(),ok?"Group removed.":"Could not remove group.");return ok?1:0;})))
                    .then(Commands.literal("tempadd").then(word("group").then(word("duration").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");try{long ms=DurationParser.parseMillis(str(c,"duration"));boolean ok=Kivra.permissions().addTemporaryGroup(p.getUUID(),str(c,"group"),ms);msg(c.getSource(),ok?"Temporary group added for "+str(c,"duration")+".":"Could not add temporary group.");return ok?1:0;}catch(Exception e){msg(c.getSource(),"Invalid duration. Examples: 30m, 12h, 7d, 2w");return 0;}})))
                    .then(Commands.literal("tempremove").then(word("group").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.permissions().removeTemporaryGroup(p.getUUID(),str(c,"group"));msg(c.getSource(),ok?"Temporary group removed.":"Temporary group not found.");return ok?1:0;}))))
                .then(Commands.literal("permission")
                    .then(Commands.literal("add").then(word("node").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.permissions().addUserPermission(p.getUUID(),str(c,"node"));msg(c.getSource(),ok?"User permission added.":"Could not add permission.");return ok?1:0;})))
                    .then(Commands.literal("remove").then(word("node").executes(c->{ServerPlayer p=EntityArgument.getPlayer(c,"player");boolean ok=Kivra.permissions().removeUserPermission(p.getUUID(),str(c,"node"));msg(c.getSource(),ok?"User permission removed.":"Could not remove permission.");return ok?1:0;}))))));
    }
    private static RequiredArgumentBuilder<CommandSourceStack,String> word(String n){return Commands.argument(n,StringArgumentType.word());}
    private static String str(CommandContext<CommandSourceStack> c,String n){return StringArgumentType.getString(c,n);}
}
