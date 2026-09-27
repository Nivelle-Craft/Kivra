package dev.nazhida.kivra.kits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class KitService {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private Data data=new Data();

    public KitService(MinecraftServer server){file=server.getServerDirectory().toPath().resolve("config/kivra/kits.json");}
    public void load(){try{Files.createDirectories(file.getParent());if(Files.exists(file))try(Reader r=Files.newBufferedReader(file)){Data d=GSON.fromJson(r,Data.class);if(d!=null)data=d;}save();}catch(IOException e){throw new IllegalStateException("Could not load kits",e);}}
    public synchronized void save(){try{Files.createDirectories(file.getParent());try(Writer w=Files.newBufferedWriter(file)){GSON.toJson(data,w);}}catch(IOException e){throw new IllegalStateException("Could not save kits",e);}}

    public boolean create(String name,ServerPlayer player){String k=key(name);if(data.kits.containsKey(k))return false;Kit kit=new Kit();kit.cooldownSeconds=86400;for(ItemStack stack:player.getInventory().items){if(!stack.isEmpty())kit.items.add(stack.save(new CompoundTag()).toString());}data.kits.put(k,kit);save();return true;}
    public boolean delete(String name){String k=key(name);boolean ok=data.kits.remove(k)!=null;if(ok){data.claims.values().forEach(m->m.remove(k));save();}return ok;}
    public boolean setCooldown(String name,long seconds){Kit kit=data.kits.get(key(name));if(kit==null||seconds<0)return false;kit.cooldownSeconds=seconds;save();return true;}
    public Set<String> names(){return Collections.unmodifiableSet(data.kits.keySet());}
    public long remaining(UUID player,String name){Kit kit=data.kits.get(key(name));if(kit==null)return -1;long last=data.claims.getOrDefault(player,Collections.emptyMap()).getOrDefault(key(name),0L);return Math.max(0,(last+kit.cooldownSeconds*1000L-System.currentTimeMillis()+999)/1000);}
    public boolean give(ServerPlayer player,String name,boolean bypass){Kit kit=data.kits.get(key(name));if(kit==null)return false;if(!bypass&&remaining(player.getUUID(),name)>0)return false;for(String raw:kit.items){try{CompoundTag tag=NbtUtils.snbtToStructure(raw);ItemStack stack=ItemStack.of(tag);if(!stack.isEmpty()&&!player.getInventory().add(stack))player.drop(stack,false);}catch(Exception ignored){}}data.claims.computeIfAbsent(player.getUUID(),x->new LinkedHashMap<>()).put(key(name),System.currentTimeMillis());save();return true;}
    private String key(String s){return s.toLowerCase(Locale.ROOT);}
    private static final class Data{Map<String,Kit> kits=new LinkedHashMap<>();Map<UUID,Map<String,Long>> claims=new LinkedHashMap<>();}
    private static final class Kit{List<String> items=new ArrayList<>();long cooldownSeconds;}
}
