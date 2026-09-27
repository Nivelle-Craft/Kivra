package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import dev.nazhida.kivra.Kivra;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigDecimal;

public final class EconomyCommands {
    private EconomyCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("balance")
            .executes(c -> showBalance(c.getSource(), c.getSource().getPlayerOrException()))
            .then(Commands.argument("player", EntityArgument.player())
                .requires(s -> s.hasPermission(2))
                .executes(c -> showBalance(c.getSource(), EntityArgument.getPlayer(c, "player")))));

        dispatcher.register(Commands.literal("bal")
            .executes(c -> showBalance(c.getSource(), c.getSource().getPlayerOrException())));

        dispatcher.register(Commands.literal("pay")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                    .executes(c -> {
                        ServerPlayer from = c.getSource().getPlayerOrException();
                        ServerPlayer to = EntityArgument.getPlayer(c, "player");
                        BigDecimal amount = BigDecimal.valueOf(DoubleArgumentType.getDouble(c, "amount"));
                        boolean ok = Kivra.economy().transfer(from.getUUID(), to.getUUID(), amount);
                        if (!ok) { msg(c.getSource(), "Payment failed. Check your balance and amount."); return 0; }
                        msg(c.getSource(), "Paid " + Kivra.economy().format(amount) + " to " + to.getGameProfile().getName() + ".");
                        to.sendSystemMessage(Component.literal("You received " + Kivra.economy().format(amount) + " from " + from.getGameProfile().getName() + "."));
                        return 1;
                    }))));

        dispatcher.register(Commands.literal("kivraeco").requires(s -> s.hasPermission(4))
            .then(adminAmount("give"))
            .then(adminAmount("take"))
            .then(adminAmount("set")));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> adminAmount(String action) {
        return Commands.literal(action).then(Commands.argument("player", EntityArgument.player())
            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0)).executes(c -> {
                ServerPlayer player = EntityArgument.getPlayer(c, "player");
                BigDecimal amount = BigDecimal.valueOf(DoubleArgumentType.getDouble(c, "amount"));
                boolean ok = switch (action) {
                    case "give" -> Kivra.economy().deposit(player.getUUID(), amount);
                    case "take" -> Kivra.economy().withdraw(player.getUUID(), amount);
                    case "set" -> Kivra.economy().set(player.getUUID(), amount);
                    default -> false;
                };
                msg(c.getSource(), ok ? "Economy updated. New balance: " + Kivra.economy().format(Kivra.economy().balance(player.getUUID())) : "Could not update balance.");
                return ok ? 1 : 0;
            })));
    }

    private static int showBalance(CommandSourceStack source, ServerPlayer player) {
        msg(source, player.getGameProfile().getName() + " balance: " + Kivra.economy().format(Kivra.economy().balance(player.getUUID())));
        return 1;
    }

    private static void msg(CommandSourceStack source, String text) { source.sendSuccess(() -> Component.literal(text), false); }
}
