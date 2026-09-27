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
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path directory;
    private final Map<String,PermissionGroup> groups=new LinkedHashMap<>();
    private final Map<UUID,PermissionUser> users=new LinkedHashMap<>();
    public PermissionService(MinecraftServer server){directory=server.getServerDirectory().toPath().resolve("config/kivra");}
    public void load(){try{Files.createDirectories(directory);loadGroups();loadUsers();installDefaultGroups();purgeExpired();save();}catch(IOException e){throw new IllegalStateException("Could not load Kivra permission data",e);}}
    private void loadGroups()throws IOException{Path f=directory.resolve("groups.json");if(!Files.exists(f))return;Type t=new TypeToken<Map<String,PermissionGroup>>(){}.getType();try(Reader r=Files.newBufferedReader(f)){Map<String,PermissionGroup>d=GSON.fromJson(r,t);if(d!=null)groups.putAll(d);}}
    private void loadUsers()throws IOException{Path f=directory.resolve("users.json");if(!Files.exists(f))return;Type t=new TypeToken<Map<UUID,PermissionUser>>(){}.getType();try(Reader r=Files.newBufferedReader(f)){Map<UUID,PermissionUser>d=GSON.fromJson(r,t);if(d!=null)users.putAll(d);}}
    private void installDefaultGroups(){
        ensure("default","[Player] ",0,null,"kivra.essentials.spawn","kivra.essentials.home","kivra.essentials.warp","kivra.essentials.tpa","kivra.kit.use","kivra.claim.use","kivra.claim.create","kivra.claim.delete","kivra.claim.trust","kivra.shop.use","kivra.shop.buy","kivra.shop.sell","kivra.shop.create");
        ensure("vip","[VIP] ",10,"default");
        ensure("helper","[Helper] ",30,"vip","kivra.moderation.warn","kivra.moderation.history");
        ensure("moderator","[Mod] ",50,"helper","kivra.moderation.kick","kivra.moderation.mute","kivra.moderation.tempmute","kivra.moderation.unmute","kivra.moderation.tempban");
        ensure("admin","[Admin] ",80,"moderator","kivra.moderation.*","kivra.claim.bypass","kivra.shop.admin","kivra.kit.admin","kivra.essentials.setspawn","kivra.essentials.warp.admin","kivra.admin");
        ensure("owner","[Owner] ",100,"admin","*");
    }
    private void ensure(String name,String prefix,int weight,String parent,String...permissions){PermissionGroup g=groups.computeIfAbsent(name,PermissionGroup::new);if(g.prefix().isBlank())g.setPrefix(prefix);if(g.weight()==0&&weight!=0)g.setWeight(weight);if(parent!=null)g.parents().add(parent);if(g.permissions().isEmpty())Collections.addAll(g.permissions(),permissions);}
    public synchronized void save(){try{Files.createDirectories(directory);try(Writer w=Files.newBufferedWriter(directory.resolve("groups.json"))){GSON.toJson(groups,w);}try(Writer w=Files.newBufferedWriter(directory.resolve("users.json"))){GSON.toJson(users,w);}}catch(IOException e){throw new IllegalStateException("Could not save Kivra permission data",e);}}
    private String key(String v){return v.toLowerCase(Locale.ROOT);}
    public PermissionUser user(UUID id){return users.computeIfAbsent(id,PermissionUser::new);}
    public PermissionGroup group(String name){return groups.get(key(name));}
    public Collection<PermissionGroup> groups(){return Collections.unmodifiableCollection(groups.values());}
    public boolean createGroup(String n){String k=key(n);if(k.isBlank()||groups.containsKey(k))return false;groups.put(k,new PermissionGroup(k));save();return true;}
    public boolean deleteGroup(String n){String k=key(n);if(k.equals("default")||groups.remove(k)==null)return false;users.values().forEach(u->{u.groups().remove(k);u.temporaryGroups().remove(k);});groups.values().forEach(g->g.parents().remove(k));save();return true;}
    public boolean addGroup(UUID id,String n){String k=key(n);if(!groups.containsKey(k))return false;boolean c=user(id).groups().add(k);if(c)save();return c;}
    public boolean removeGroup(UUID id,String n){String k=key(n);if(k.equals("default"))return false;boolean c=user(id).groups().remove(k);if(c)save();return c;}
    public boolean addTemporaryGroup(UUID id,String n,long durationMillis){String k=key(n);if(!groups.containsKey(k)||durationMillis<=0)return false;user(id).temporaryGroups().put(k,System.currentTimeMillis()+durationMillis);save();return true;}
    public boolean removeTemporaryGroup(UUID id,String n){boolean c=user(id).temporaryGroups().remove(key(n))!=null;if(c)save();return c;}
    public void purgeExpired(){long now=System.currentTimeMillis();users.values().forEach(u->u.temporaryGroups().entrySet().removeIf(e->e.getValue()<=now));}
    public boolean addPermissionToGroup(String g,String n){PermissionGroup x=group(g);if(x==null)return false;boolean c=x.permissions().add(key(n));if(c)save();return c;}
    public boolean removePermissionFromGroup(String g,String n){PermissionGroup x=group(g);if(x==null)return false;boolean c=x.permissions().remove(key(n));if(c)save();return c;}
    public boolean addUserPermission(UUID id,String n){boolean c=user(id).permissions().add(key(n));if(c)save();return c;}
    public boolean removeUserPermission(UUID id,String n){boolean c=user(id).permissions().remove(key(n));if(c)save();return c;}
    public boolean addParent(String c,String p){PermissionGroup cg=group(c),pg=group(p);if(cg==null||pg==null||key(c).equals(key(p)))return false;boolean x=cg.parents().add(key(p));if(x)save();return x;}
    public boolean removeParent(String c,String p){PermissionGroup g=group(c);if(g==null)return false;boolean x=g.parents().remove(key(p));if(x)save();return x;}
    public boolean setPrefix(String g,String p){PermissionGroup x=group(g);if(x==null)return false;x.setPrefix(p);save();return true;}
    public boolean setWeight(String g,int w){PermissionGroup x=group(g);if(x==null)return false;x.setWeight(w);save();return true;}
    public Set<String> effectiveGroups(UUID id){PermissionUser u=user(id);long now=System.currentTimeMillis();Set<String> result=new LinkedHashSet<>(u.groups());u.temporaryGroups().forEach((g,expires)->{if(expires>now)result.add(g);});result.add("default");return result;}
    public PermissionGroup primaryGroup(UUID id){return effectiveGroups(id).stream().map(groups::get).filter(Objects::nonNull).max(Comparator.comparingInt(PermissionGroup::weight)).orElse(groups.get("default"));}
    public String prefix(UUID id){PermissionGroup g=primaryGroup(id);return g==null?"":g.prefix();}
    public boolean hasPermission(ServerPlayer p,String n){return hasPermission(p.getUUID(),n);}
    public boolean hasPermission(UUID id,String n){PermissionUser u=user(id);String node=key(n);if(matches(u.permissions(),node))return true;for(String g:effectiveGroups(id))if(hasGroupPermission(g,node,new HashSet<>()))return true;return false;}
    private boolean hasGroupPermission(String n,String node,Set<String>visited){String k=key(n);if(!visited.add(k))return false;PermissionGroup g=groups.get(k);if(g==null)return false;if(matches(g.permissions(),node))return true;for(String p:g.parents())if(hasGroupPermission(p,node,visited))return true;return false;}
    private boolean matches(Set<String>nodes,String requested){if(nodes.contains("*")||nodes.contains(requested))return true;int dot=requested.length();while((dot=requested.lastIndexOf('.',dot-1))>=0)if(nodes.contains(requested.substring(0,dot)+".*"))return true;return false;}
}
