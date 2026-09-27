package dev.nazhida.kivra.permissions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class PermissionService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path directory;
    private final Map<String, PermissionGroup> groups = new LinkedHashMap<>();
    private final Map<UUID, PermissionUser> users = new LinkedHashMap<>();

    public PermissionService(MinecraftServer server) {
        this.directory = server.getServerDirectory().toPath().resolve("config").resolve("kivra");
    }

    public void load() {
        try {
            Files.createDirectories(directory);
            loadGroups();
            loadUsers();
            if (!groups.containsKey("default")) {
                groups.put("default", new PermissionGroup("default"));
                save();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load Kivra permission data", e);
        }
    }

    private void loadGroups() throws IOException {
        Path file = directory.resolve("groups.json");
        if (!Files.exists(file)) return;
        Type type = new TypeToken<Map<String, PermissionGroup>>() {}.getType();
        try (Reader reader = Files.newBufferedReader(file)) {
            Map<String, PermissionGroup> loaded = GSON.fromJson(reader, type);
            if (loaded != null) groups.putAll(loaded);
        }
    }

    private void loadUsers() throws IOException {
        Path file = directory.resolve("users.json");
        if (!Files.exists(file)) return;
        Type type = new TypeToken<Map<UUID, PermissionUser>>() {}.getType();
        try (Reader reader = Files.newBufferedReader(file)) {
            Map<UUID, PermissionUser> loaded = GSON.fromJson(reader, type);
            if (loaded != null) users.putAll(loaded);
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(directory);
            try (Writer writer = Files.newBufferedWriter(directory.resolve("groups.json"))) {
                GSON.toJson(groups, writer);
            }
            try (Writer writer = Files.newBufferedWriter(directory.resolve("users.json"))) {
                GSON.toJson(users, writer);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save Kivra permission data", e);
        }
    }

    public PermissionUser user(UUID uuid) {
        return users.computeIfAbsent(uuid, PermissionUser::new);
    }

    public PermissionGroup group(String name) {
        return groups.get(name.toLowerCase(Locale.ROOT));
    }

    public Collection<PermissionGroup> groups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public boolean createGroup(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        if (groups.containsKey(key)) return false;
        groups.put(key, new PermissionGroup(key));
        save();
        return true;
    }

    public boolean addGroup(UUID uuid, String group) {
        if (!groups.containsKey(group.toLowerCase(Locale.ROOT))) return false;
        boolean changed = user(uuid).groups().add(group.toLowerCase(Locale.ROOT));
        if (changed) save();
        return changed;
    }

    public boolean addPermissionToGroup(String groupName, String permission) {
        PermissionGroup group = group(groupName);
        if (group == null) return false;
        boolean changed = group.permissions().add(permission.toLowerCase(Locale.ROOT));
        if (changed) save();
        return changed;
    }

    public boolean hasPermission(ServerPlayer player, String permission) {
        PermissionUser user = user(player.getUUID());
        String node = permission.toLowerCase(Locale.ROOT);
        if (matches(user.permissions(), node)) return true;
        for (String group : user.groups()) {
            if (hasGroupPermission(group, node, new HashSet<>())) return true;
        }
        return false;
    }

    private boolean hasGroupPermission(String groupName, String node, Set<String> visited) {
        String key = groupName.toLowerCase(Locale.ROOT);
        if (!visited.add(key)) return false;
        PermissionGroup group = groups.get(key);
        if (group == null) return false;
        if (matches(group.permissions(), node)) return true;
        for (String parent : group.parents()) {
            if (hasGroupPermission(parent, node, visited)) return true;
        }
        return false;
    }

    private boolean matches(Set<String> nodes, String requested) {
        if (nodes.contains("*") || nodes.contains(requested)) return true;
        int dot = requested.length();
        while ((dot = requested.lastIndexOf('.', dot - 1)) >= 0) {
            if (nodes.contains(requested.substring(0, dot) + ".*")) return true;
        }
        return false;
    }
}
