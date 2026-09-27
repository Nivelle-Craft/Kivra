package dev.nazhida.kivra.essentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class EssentialsService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final MinecraftServer server;
    private final Path file;
    private Data data = new Data();
    private final Map<UUID, UUID> tpa = new HashMap<>();

    public EssentialsService(MinecraftServer server) {
        this.server = server;
        this.file = server.getServerDirectory().toPath().resolve("config/kivra/essentials.json");
    }

    public void load() {
        try {
            Files.createDirectories(file.getParent());
            if (Files.exists(file)) try (Reader r=Files.newBufferedReader(file)) { Data d=GSON.fromJson(r,Data.class); if(d!=null)data=d; }
            save();
        } catch(IOException e){throw new IllegalStateException("Could not load Essentials",e);}
    }
    public synchronized void save(){try{Files.createDirectories(file.getParent());try(Writer w=Files.newBufferedWriter(file)){GSON.toJson(data,w);}}catch(IOException e){throw new IllegalStateException("Could not save Essentials",e);}}
    public LocationData capture(ServerPlayer p){return new LocationData(p.level().dimension().location().toString(),p.getX(),p.getY(),p.getZ(),p.getYRot(),p.getXRot());}
    public boolean teleport(ServerPlayer p,LocationData l){if(l==null)return false;ResourceLocation id=ResourceLocation.tryParse(l.dimension);if(id==null)return false;ServerLevel level=server.getLevel(ResourceKey.create(Registries.DIMENSION,id));if(level==null)return false;p.teleportTo(level,l.x,l.y,l.z,l.yaw,l.pitch);return true;}
    public void setSpawn(ServerPlayer p){data.spawn=capture(p);save();}
    public boolean spawn(ServerPlayer p){return teleport(p,data.spawn);}
    public void setHome(ServerPlayer p,String n){data.homes.computeIfAbsent(p.getUUID(),x->new LinkedHashMap<>()).put(key(n),capture(p));save();}
    public boolean home(ServerPlayer p,String n){Map<String,LocationData> h=data.homes.get(p.getUUID());return h!=null&&teleport(p,h.get(key(n)));}
    public boolean delHome(ServerPlayer p,String n){Map<String,LocationData> h=data.homes.get(p.getUUID());boolean ok=h!=null&&h.remove(key(n))!=null;if(ok)save();return ok;}
    public Set<String> homes(UUID id){Map<String,LocationData> h=data.homes.get(id);return h==null?Collections.emptySet():h.keySet();}
    public void setWarp(ServerPlayer p,String n){data.warps.put(key(n),capture(p));save();}
    public boolean warp(ServerPlayer p,String n){return teleport(p,data.warps.get(key(n)));}
    public boolean delWarp(String n){boolean ok=data.warps.remove(key(n))!=null;if(ok)save();return ok;}
    public Set<String> warps(){return data.warps.keySet();}
    public void request(ServerPlayer from,ServerPlayer to){tpa.put(to.getUUID(),from.getUUID());}
    public ServerPlayer accept(ServerPlayer target){UUID id=tpa.remove(target.getUUID());if(id==null)return null;ServerPlayer p=server.getPlayerList().getPlayer(id);if(p!=null)p.teleportTo(target.serverLevel(),target.getX(),target.getY(),target.getZ(),target.getYRot(),target.getXRot());return p;}
    public boolean deny(ServerPlayer target){return tpa.remove(target.getUUID())!=null;}
    private String key(String s){return s.toLowerCase(Locale.ROOT);}
    private static final class Data{LocationData spawn;Map<UUID,Map<String,LocationData>> homes=new LinkedHashMap<>();Map<String,LocationData> warps=new LinkedHashMap<>();}
}
