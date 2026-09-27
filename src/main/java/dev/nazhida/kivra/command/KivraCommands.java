package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.nazhida.kivra.Kivra;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class KivraCommands {
    private KivraCommands() {}

    private static void msg(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kivra")
            .executes(context -> {
                msg(context.getSource(), "Kivra v0.1.0 — Permissions");
                return 1;
            })
            .then(Commands.literal("group")
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("create")
                    .then(word("group").executes(context -> {
                        String group = str(context, "group");
                        boolean ok = Kivra.permissions().createGroup(group);
                        msg(context.getSource(), ok ? "Created group: " + group : "Could not create group.");
                        return ok ? 1 : 0;
                    })))
                .then(Commands.literal("delete")
                    .then(word("group").executes(context -> {
                        String group = str(context, "group");
                        boolean ok = Kivra.permissions().deleteGroup(group);
                        msg(context.getSource(), ok ? "Deleted group: " + group : "Could not delete group.");
                        return ok ? 1 : 0;
                    })))
                .then(Commands.literal("permission")
                    .then(word("group")
                        .then(Commands.literal("add")
                            .then(word("node").executes(context -> {
                                boolean ok = Kivra.permissions().addPermissionToGroup(str(context, "group"), str(context, "node"));
                                msg(context.getSource(), ok ? "Permission added." : "Could not add permission.");
                                return ok ? 1 : 0;
                            })))
                        .then(Commands.literal("remove")
                            .then(word("node").executes(context -> {
                                boolean ok = Kivra.permissions().removePermissionFromGroup(str(context, "group"), str(context, "node"));
                                msg(context.getSource(), ok ? "Permission removed." : "Could not remove permission.");
                                return ok ? 1 : 0;
                            })))))
                .then(Commands.literal("parent")
                    .then(word("group")
                        .then(Commands.literal("add")
                            .then(word("parent").executes(context -> {
                                boolean ok = Kivra.permissions().addParent(str(context, "group"), str(context, "parent"));
                                msg(context.getSource(), ok ? "Parent added." : "Could not add parent.");
                                return ok ? 1 : 0;
                            })))
                        .then(Commands.literal("remove")
                            .then(word("parent").executes(context -> {
                                boolean ok = Kivra.permissions().removeParent(str(context, "group"), str(context, "parent"));
                                msg(context.getSource(), ok ? "Parent removed." : "Could not remove parent.");
                                return ok ? 1 : 0;
                            })))))
                .then(Commands.literal("prefix")
                    .then(word("group")
                        .then(Commands.argument("prefix", StringArgumentType.greedyString()).executes(context -> {
                            boolean ok = Kivra.permissions().setPrefix(str(context, "group"), StringArgumentType.getString(context, "prefix"));
                            msg(context.getSource(), ok ? "Prefix updated." : "Unknown group.");
                            return ok ? 1 : 0;
                        }))))
                .then(Commands.literal("weight")
                    .then(word("group")
                        .then(Commands.argument("weight", IntegerArgumentType.integer()).executes(context -> {
                            boolean ok = Kivra.permissions().setWeight(str(context, "group"), IntegerArgumentType.getInteger(context, "weight"));
                            msg(context.getSource(), ok ? "Weight updated." : "Unknown group.");
                            return ok ? 1 : 0;
                        })))))
            .then(Commands.literal("user")
                .requires(source -> source.hasPermission(4))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("group")
                        .then(Commands.literal("add")
                            .then(word("group").executes(context -> {
                                ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                boolean ok = Kivra.permissions().addGroup(player.getUUID(), str(context, "group"));
                                msg(context.getSource(), ok ? "Group added to " + player.getGameProfile().getName() + "." : "Could not add group.");
                                return ok ? 1 : 0;
                            })))
                        .then(Commands.literal("remove")
                            .then(word("group").executes(context -> {
                                ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                boolean ok = Kivra.permissions().removeGroup(player.getUUID(), str(context, "group"));
                                msg(context.getSource(), ok ? "Group removed from " + player.getGameProfile().getName() + "." : "Could not remove group.");
                                return ok ? 1 : 0;
                            }))))
                    .then(Commands.literal("permission")
                        .then(Commands.literal("add")
                            .then(word("node").executes(context -> {
                                ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                boolean ok = Kivra.permissions().addUserPermission(player.getUUID(), str(context, "node"));
                                msg(context.getSource(), ok ? "User permission added." : "Could not add permission.");
                                return ok ? 1 : 0;
                            })))
                        .then(Commands.literal("remove")
                            .then(word("node").executes(context -> {
                                ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                boolean ok = Kivra.permissions().removeUserPermission(player.getUUID(), str(context, "node"));
                                msg(context.getSource(), ok ? "User permission removed." : "Could not remove permission.");
                                return ok ? 1 : 0;
                            }))))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> word(String name) {
        return Commands.argument(name, StringArgumentType.word());
    }

    private static String str(CommandContext<CommandSourceStack> context, String name) {
        return StringArgumentType.getString(context, name);
    }
}
