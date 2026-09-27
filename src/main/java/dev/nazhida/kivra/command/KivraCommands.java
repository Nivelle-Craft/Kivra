package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.nazhida.kivra.Kivra;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class KivraCommands {
    private KivraCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kivra")
            .executes(ctx -> {
                ctx.getSource().sendSuccess(() -> Component.literal("Kivra v0.1.0 — Permissions"), false);
                return 1;
            })
            .then(Commands.literal("group")
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("create")
                    .then(Commands.argument("name", MessageArgument.message())
                        .executes(ctx -> {
                            String name = MessageArgument.getMessage(ctx, "name").getString().trim();
                            boolean created = Kivra.permissions().createGroup(name);
                            ctx.getSource().sendSuccess(() -> Component.literal(created
                                ? "Created group: " + name
                                : "That group already exists."), false);
                            return created ? 1 : 0;
                        })))
                .then(Commands.literal("permission")
                    .then(Commands.argument("group", MessageArgument.message())
                        .then(Commands.argument("node", MessageArgument.message())
                            .executes(ctx -> {
                                String group = MessageArgument.getMessage(ctx, "group").getString().trim();
                                String node = MessageArgument.getMessage(ctx, "node").getString().trim();
                                boolean changed = Kivra.permissions().addPermissionToGroup(group, node);
                                ctx.getSource().sendSuccess(() -> Component.literal(changed
                                    ? "Added " + node + " to " + group
                                    : "Could not add permission."), false);
                                return changed ? 1 : 0;
                            })))))
            .then(Commands.literal("user")
                .requires(source -> source.hasPermission(4))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("group")
                        .then(Commands.literal("add")
                            .then(Commands.argument("group", MessageArgument.message())
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    String group = MessageArgument.getMessage(ctx, "group").getString().trim();
                                    boolean changed = Kivra.permissions().addGroup(player.getUUID(), group);
                                    ctx.getSource().sendSuccess(() -> Component.literal(changed
                                        ? "Added " + player.getGameProfile().getName() + " to " + group
                                        : "Could not add player to group."), false);
                                    return changed ? 1 : 0;
                                })))))));
    }
}
