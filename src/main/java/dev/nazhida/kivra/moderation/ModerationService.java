package dev.nazhida.kivra.moderation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class ModerationService {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private Data data=new Data();

    public ModerationService(Path serverDirectory){file=serverDirectory.resolve("config/kivra/moderation.json");}
    public void load(){try{Files.createDirectories(file.getParent());if(Files.exists(file))try(Reader r=Files.newBufferedReader(file)){Data d=GSON.fromJson(r,Data.class);if(d!=null)data=d;}save();}catch(IOException e){throw new IllegalStateException("Could not load moderation",e);}}
    public synchronized void save(){try{Files.createDirectories(file.getParent());try(Writer w=Files.newBufferedWriter(file)){GSON.toJson(data,w);}}catch(IOException e){throw new IllegalStateException("Could not save moderation",e);}}
    public synchronized void warn(UUID target,String name,UUID staff,String reason){Record r=new Record("WARN",target,name,staff,reason,System.currentTimeMillis(),0);data.history.add(r);save();}
    public synchronized void ban(UUID target,String name,UUID staff,String reason,long durationMillis){long until=durationMillis<=0?0:System.currentTimeMillis()+durationMillis;data.bans.put(target,new Punishment(name,staff,reason,System.currentTimeMillis(),until));data.history.add(new Record("BAN",target,name,staff,reason,System.currentTimeMillis(),until));save();}
    public synchronized boolean unban(UUID target,UUID staff){Punishment p=data.bans.remove(target);if(p==null)return false;data.history.add(new Record("UNBAN",target,p.name,staff,"Unbanned",System.currentTimeMillis(),0));save();return true;}
    public synchronized void mute(UUID target,String name,UUID staff,String reason,long durationMillis){long until=durationMillis<=0?0:System.currentTimeMillis()+durationMillis;data.mutes.put(target,new Punishment(name,staff,reason,System.currentTimeMillis(),until));data.history.add(new Record("MUTE",target,name,staff,reason,System.currentTimeMillis(),until));save();}
    public synchronized boolean unmute(UUID target,UUID staff){Punishment p=data.mutes.remove(target);if(p==null)return false;data.history.add(new Record("UNMUTE",target,p.name,staff,"Unmuted",System.currentTimeMillis(),0));save();return true;}
    public synchronized Punishment ban(UUID id){return active(data.bans,id);}
    public synchronized Punishment mute(UUID id){return active(data.mutes,id);}
    private Punishment active(Map<UUID,Punishment> map,UUID id){Punishment p=map.get(id);if(p!=null&&p.until>0&&p.until<=System.currentTimeMillis()){map.remove(id);save();return null;}return p;}
    public synchronized List<Record> history(UUID id){List<Record> out=new ArrayList<>();for(Record r:data.history)if(r.target.equals(id))out.add(r);return out;}
    public static final class Punishment{public String name;public UUID staff;public String reason;public long created;public long until;Punishment(String n,UUID s,String r,long c,long u){name=n;staff=s;reason=r;created=c;until=u;}public boolean permanent(){return until==0;}}
    public static final class Record{public String type;public UUID target;public String name;public UUID staff;public String reason;public long created;public long until;Record(String t,UUID id,String n,UUID s,String r,long c,long u){type=t;target=id;name=n;staff=s;reason=r;created=c;until=u;}}
    private static final class Data{Map<UUID,Punishment>bans=new LinkedHashMap<>();Map<UUID,Punishment>mutes=new LinkedHashMap<>();List<Record>history=new ArrayList<>();}
}
