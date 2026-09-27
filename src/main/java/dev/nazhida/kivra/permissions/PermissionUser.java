package dev.nazhida.kivra.permissions;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PermissionUser {
    private final UUID uuid;
    private final Set<String> groups = new LinkedHashSet<>();
    private final Set<String> permissions = new LinkedHashSet<>();
    private final Map<String, Long> temporaryGroups = new LinkedHashMap<>();

    public PermissionUser(UUID uuid) {
        this.uuid = uuid;
        this.groups.add("default");
    }

    public UUID uuid() { return uuid; }
    public Set<String> groups() { return groups; }
    public Set<String> permissions() { return permissions; }
    public Map<String, Long> temporaryGroups() { return temporaryGroups; }
}
