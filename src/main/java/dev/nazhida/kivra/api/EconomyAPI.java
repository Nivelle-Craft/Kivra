package dev.nazhida.kivra.api;

import dev.nazhida.kivra.Kivra;
import dev.nazhida.kivra.economy.EconomyService;

import java.math.BigDecimal;
import java.util.UUID;

public final class EconomyAPI {
    private EconomyAPI() {}

    public static EconomyService economy() { return Kivra.economy(); }
    public static BigDecimal balance(UUID player) { return economy().balance(player); }
    public static boolean has(UUID player, BigDecimal amount) { return economy().has(player, amount); }
    public static boolean deposit(UUID player, BigDecimal amount) { return economy().deposit(player, amount); }
    public static boolean withdraw(UUID player, BigDecimal amount) { return economy().withdraw(player, amount); }
    public static boolean transfer(UUID from, UUID to, BigDecimal amount) { return economy().transfer(from, to, amount); }
}
