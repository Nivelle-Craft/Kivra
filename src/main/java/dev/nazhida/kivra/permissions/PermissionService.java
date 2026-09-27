package dev.nazhida.kivra.permissions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

public final class PermissionService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path directory;
    private final Map<String, PermissionGroup> groups = new LinkedHashMap<>();
    private final Map<UUID, PermissionUser> users = new LinkedHashMap<>();

    public PermissionService(MinecraftServer server) {
        directory = server.getServerDirectory().toPath().resolve("config").resolve("kivra");
    }

    public void load() {
        try {
            Files.createDirectories(directory);
            loadGroups(); loadUsers();
            if (!groups.containsKey("default")) groups.put("default", new PermissionGroup("default"));
            save();
        } catch (IOException e) { throw new IllegalStateException("Could not load Kivra permission data", e); }
    }

    private void loadGroups() throws IOException {
        Path file = directory.resolve("groups.json"); if (!Files.exists(file)) return;
        Type type = new TypeToken<Map<String, PermissionGroup>>(){}.getType();
        try (Reader r = Files.newBufferedReader(file)) { Map<String, PermissionGroup> data = GSON.fromJson(r,type); if(data!=null) groups.putAll(data); }
    }
    private void loadUsers() throws IOException {
        Path file = directory.resolve("users.json"); if (!Files.exists(file)) return;
        Type type = new TypeToken<Map<UUID, PermissionUser>>(){}.getType();
        try (Reader r = Files.newBufferedReader(file)) { Map<UUID, PermissionUser> data = GSON.fromJson(r,type); if(data!=null) users.putAll(data); }
    }
    public synchronized void save() {
        try {
            Files.createDirectories(directory);
            try(Writer w=Files.newBufferedWriter(directory.resolve("groups.json"))){GSON.toJson(groups,w);}
            try(Writer w=Files.newBufferedWriter(directory.resolve("users.json"))){GSON.toJson(users,w);}
        } catch(IOException e){throw new IllegalStateException("Could not save Kivra permission data",e);}
    }

    private String key(String value){ return value.toLowerCase(Locale.ROOT); }
    public PermissionUser user(UUID uuid){return users.computeIfAbsent(uuid,PermissionUser::new);}
    public PermissionGroup group(String name){return groups.get(key(name));}
    public Collection<PermissionGroup> groups(){return Collections.unmodifiableCollection(groups.values());}

    public boolean createGroup(String name){String k=key(name); if(k.isBlank()||groups.containsKey(k))return false; groups.put(k,new PermissionGroup(k)); save(); return true;}
    public boolean deleteGroup(String name){String k=key(name); if(k.equals("default")||groups.remove(k)==null)return false; users.values().forEach(u->u.groups().remove(k)); groups.values().forEach(g->g.parents().remove(k)); save(); return true;}
    public boolean addGroup(UUID uuid,String name){String k=key(name); if(!groups.containsKey(k))return false; boolean c=user(uuid).groups().add(k); if(c)save(); return c;}
    public boolean removeGroup(UUID uuid,String name){String k=key(name); if(k.equals("default"))return false; boolean c=user(uuid).groups().remove(k); if(c)save(); return c;}
    public boolean addPermissionToGroup(String group,String node){PermissionGroup g=group(group); if(g==null)return false; boolean c=g.permissions().add(key(node)); if(c)save(); return c;}
    public boolean removePermissionFromGroup(String group,String node){PermissionGroup g=group(group); if(g==null)return false; boolean c=g.permissions().remove(key(node)); if(c)save(); return c;}
    public boolean addUserPermission(UUID uuid,String node){boolean c=user(uuid).permissions().add(key(node)); if(c)save(); return c;}
    public boolean removeUserPermission(UUID uuid,String node){boolean c=user(uuid).permissions().remove(key(node)); if(c)save(); return c;}
    public boolean addParent(String child,String parent){PermissionGroup c=group(child),p=group(parent); if(c==null||p==null||key(child).equals(key(parent)))return false; boolean changed=c.parents().add(key(parent)); if(changed)save(); return changed;}
    public boolean removeParent(String child,String parent){PermissionGroup c=group(child); if(c==null)return false; boolean changed=c.parents().remove(key(parent)); if(changed)save(); return changed;}
    public boolean setPrefix(String group,String prefix){PermissionGroup g=group(group); if(g==null)return false; g.setPrefix(prefix); save(); return true;}
    public boolean setWeight(String group,int weight){PermissionGroup g=group(group); if(g==null)return false; g.setWeight(weight); save(); return true;}

    public boolean hasPermission(ServerPlayer player,String permission){return hasPermission(player.getUUID(),permission);}
    public boolean hasPermission(UUID uuid,String permission){PermissionUser u=user(uuid); String node=key(permission); if(matches(u.permissions(),node))return true; for(String g:u.groups())if(hasGroupPermission(g,node,new HashSet<>()))return true; return false;}
    private boolean hasGroupPermission(String name,String node,Set<String> visited){String k=key(name); if(!visited.add(k))return false; PermissionGroup g=groups.get(k); if(g==null)return false; if(matches(g.permissions(),node))return true; for(String p:g.parents())if(hasGroupPermission(p,node,visited))return true; return false;}
    private boolean matches(Set<String> nodes,String requested){if(nodes.contains("*")||nodes.contains(requested))return true; int dot=requested.length(); while((dot=requested.lastIndexOf('.',dot-1))>=0)if(nodes.contains(requested.substring(0,dot)+".*"))return true; return false;}
}
