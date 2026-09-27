package dev.nazhida.kivra.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class EconomyService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private final Map<UUID, BigDecimal> balances = new LinkedHashMap<>();

    public EconomyService(MinecraftServer server) {
        this.file = server.getServerDirectory().toPath().resolve("config").resolve("kivra").resolve("economy.json");
    }

    public synchronized void load() {
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) { save(); return; }
            Type type = new TypeToken<Map<UUID, BigDecimal>>() {}.getType();
            try (Reader reader = Files.newBufferedReader(file)) {
                Map<UUID, BigDecimal> data = GSON.fromJson(reader, type);
                if (data != null) balances.putAll(data);
            }
        } catch (IOException e) { throw new IllegalStateException("Could not load Kivra economy", e); }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) { GSON.toJson(balances, writer); }
        } catch (IOException e) { throw new IllegalStateException("Could not save Kivra economy", e); }
    }

    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public synchronized BigDecimal balance(UUID player) {
        return balances.getOrDefault(player, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public synchronized boolean has(UUID player, BigDecimal amount) {
        amount = normalize(amount);
        return amount.signum() >= 0 && balance(player).compareTo(amount) >= 0;
    }

    public synchronized boolean set(UUID player, BigDecimal amount) {
        amount = normalize(amount);
        if (amount.signum() < 0) return false;
        balances.put(player, amount); save(); return true;
    }

    public synchronized boolean deposit(UUID player, BigDecimal amount) {
        amount = normalize(amount);
        if (amount.signum() <= 0) return false;
        balances.put(player, balance(player).add(amount)); save(); return true;
    }

    public synchronized boolean withdraw(UUID player, BigDecimal amount) {
        amount = normalize(amount);
        if (amount.signum() <= 0 || !has(player, amount)) return false;
        balances.put(player, balance(player).subtract(amount)); save(); return true;
    }

    public synchronized boolean transfer(UUID from, UUID to, BigDecimal amount) {
        amount = normalize(amount);
        if (from.equals(to) || amount.signum() <= 0 || !has(from, amount)) return false;
        balances.put(from, balance(from).subtract(amount));
        balances.put(to, balance(to).add(amount));
        save(); return true;
    }

    public String format(BigDecimal amount) { return normalize(amount).toPlainString() + " coins"; }
}
