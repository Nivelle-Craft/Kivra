package dev.nazhida.kivra.claims;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class ClaimService {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private Data data=new Data();
    private final Map<UUID,BlockPos> pos1=new HashMap<>();
    private final Map<UUID,BlockPos> pos2=new HashMap<>();

    public ClaimService(MinecraftServer server){file=server.getServerDirectory().toPath().resolve("config/kivra/claims.json");}
    public void load(){try{Files.createDirectories(file.getParent());if(Files.exists(file))try(Reader r=Files.newBufferedReader(file)){Data d=GSON.fromJson(r,Data.class);if(d!=null)data=d;}save();}catch(IOException e){throw new IllegalStateException("Could not load claims",e);}}
    public synchronized void save(){try{Files.createDirectories(file.getParent());try(Writer w=Files.newBufferedWriter(file)){GSON.toJson(data,w);}}catch(IOException e){throw new IllegalStateException("Could not save claims",e);}}

    public void setPos1(ServerPlayer p){pos1.put(p.getUUID(),p.blockPosition());}
    public void setPos2(ServerPlayer p){pos2.put(p.getUUID(),p.blockPosition());}
    public boolean create(ServerPlayer p,String name){String k=key(name);if(data.claims.containsKey(k))return false;BlockPos a=pos1.get(p.getUUID()),b=pos2.get(p.getUUID());if(a==null||b==null)return false;String dim=p.level().dimension().location().toString();Claim c=new Claim();c.name=k;c.owner=p.getUUID();c.dimension=dim;c.minX=Math.min(a.getX(),b.getX());c.maxX=Math.max(a.getX(),b.getX());c.minY=Math.min(a.getY(),b.getY());c.maxY=Math.max(a.getY(),b.getY());c.minZ=Math.min(a.getZ(),b.getZ());c.maxZ=Math.max(a.getZ(),b.getZ());for(Claim other:data.claims.values())if(overlaps(c,other))return false;data.claims.put(k,c);save();return true;}
    public boolean delete(ServerPlayer p,String name,boolean admin){Claim c=data.claims.get(key(name));if(c==null||(!admin&&!c.owner.equals(p.getUUID())))return false;data.claims.remove(key(name));save();return true;}
    public boolean trust(ServerPlayer owner,String name,UUID user,Access access){Claim c=data.claims.get(key(name));if(c==null||!c.owner.equals(owner.getUUID()))return false;c.trusted.put(user,access);save();return true;}
    public boolean untrust(ServerPlayer owner,String name,UUID user){Claim c=data.claims.get(key(name));if(c==null||!c.owner.equals(owner.getUUID()))return false;boolean ok=c.trusted.remove(user)!=null;if(ok)save();return ok;}
    public Claim at(ServerPlayer p,BlockPos pos){String dim=p.level().dimension().location().toString();for(Claim c:data.claims.values())if(c.dimension.equals(dim)&&inside(c,pos))return c;return null;}
    public boolean canBuild(ServerPlayer p,BlockPos pos){Claim c=at(p,pos);if(c==null||c.owner.equals(p.getUUID()))return true;return c.trusted.getOrDefault(p.getUUID(),Access.NONE)==Access.FULL;}
    public boolean canInteract(ServerPlayer p,BlockPos pos){Claim c=at(p,pos);if(c==null||c.owner.equals(p.getUUID()))return true;Access a=c.trusted.getOrDefault(p.getUUID(),Access.NONE);return a==Access.FULL||a==Access.INTERACT;}
    public Set<String> owned(UUID id){Set<String> out=new LinkedHashSet<>();for(Claim c:data.claims.values())if(c.owner.equals(id))out.add(c.name);return out;}
    private boolean inside(Claim c,BlockPos p){return p.getX()>=c.minX&&p.getX()<=c.maxX&&p.getY()>=c.minY&&p.getY()<=c.maxY&&p.getZ()>=c.minZ&&p.getZ()<=c.maxZ;}
    private boolean overlaps(Claim a,Claim b){return a.dimension.equals(b.dimension)&&a.minX<=b.maxX&&a.maxX>=b.minX&&a.minY<=b.maxY&&a.maxY>=b.minY&&a.minZ<=b.maxZ&&a.maxZ>=b.minZ;}
    private String key(String s){return s.toLowerCase(Locale.ROOT);}
    public enum Access{NONE,INTERACT,FULL}
    public static final class Claim{String name;UUID owner;String dimension;int minX,maxX,minY,maxY,minZ,maxZ;Map<UUID,Access> trusted=new LinkedHashMap<>();public String name(){return name;}public UUID owner(){return owner;}}
    private static final class Data{Map<String,Claim> claims=new LinkedHashMap<>();}
}
