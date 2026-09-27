package dev.nazhida.kivra.permissions;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PermissionGroup {
    private String name;
    private String prefix;
    private int weight;
    private final Set<String> permissions = new LinkedHashSet<>();
    private final Set<String> parents = new LinkedHashSet<>();

    public PermissionGroup(String name) {
        this.name = name.toLowerCase();
        this.prefix = "";
    }

    public String name() { return name; }
    public String prefix() { return prefix; }
    public int weight() { return weight; }
    public Set<String> permissions() { return permissions; }
    public Set<String> parents() { return parents; }

    public void setPrefix(String prefix) { this.prefix = prefix == null ? "" : prefix; }
    public void setWeight(int weight) { this.weight = weight; }
}
