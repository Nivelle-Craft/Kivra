package dev.nazhida.kivra.shops;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.nazhida.kivra.economy.EconomyService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

public final class ShopService {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private final EconomyService economy;
    private Data data=new Data();

    public ShopService(MinecraftServer server,EconomyService economy){this.economy=economy;file=server.getServerDirectory().toPath().resolve("config/kivra/shops.json");}
    public void load(){try{Files.createDirectories(file.getParent());if(Files.exists(file))try(Reader r=Files.newBufferedReader(file)){Data d=GSON.fromJson(r,Data.class);if(d!=null)data=d;}save();}catch(IOException e){throw new IllegalStateException("Could not load shops",e);}}
    public synchronized void save(){try{Files.createDirectories(file.getParent());try(Writer w=Files.newBufferedWriter(file)){GSON.toJson(data,w);}}catch(IOException e){throw new IllegalStateException("Could not save shops",e);}}

    public boolean create(ServerPlayer owner,String name,BigDecimal price,int stock,boolean admin){if(price.signum()<=0||stock<0)return false;String k=key(name);if(data.shops.containsKey(k))return false;ItemStack held=owner.getMainHandItem();if(held.isEmpty())return false;Shop s=new Shop();s.name=k;s.owner=owner.getUUID();s.ownerName=owner.getGameProfile().getName();s.price=price.setScale(2,java.math.RoundingMode.HALF_UP);s.stock=stock;s.admin=admin;s.item=held.copyWithCount(1).save(new CompoundTag()).toString();data.shops.put(k,s);save();return true;}
    public boolean delete(ServerPlayer actor,String name,boolean adminBypass){Shop s=data.shops.get(key(name));if(s==null||(!adminBypass&&!s.owner.equals(actor.getUUID())))return false;data.shops.remove(key(name));save();return true;}
    public boolean restock(ServerPlayer actor,String name,int amount){Shop s=data.shops.get(key(name));if(s==null||s.admin||amount<=0||!s.owner.equals(actor.getUUID()))return false;s.stock+=amount;save();return true;}
    public Result buy(ServerPlayer buyer,String name,int amount){Shop s=data.shops.get(key(name));if(s==null)return Result.NOT_FOUND;if(amount<=0)return Result.INVALID;if(!s.admin&&s.stock<amount)return Result.NO_STOCK;BigDecimal total=s.price.multiply(BigDecimal.valueOf(amount));if(!economy.has(buyer.getUUID(),total))return Result.NO_MONEY;ItemStack unit=item(s);if(unit.isEmpty())return Result.INVALID;if(!economy.withdraw(buyer.getUUID(),total))return Result.NO_MONEY;if(!s.admin)economy.deposit(s.owner,total);if(!s.admin)s.stock-=amount;give(buyer,unit,amount);save();return Result.OK;}
    public Result sell(ServerPlayer seller,String name,int amount){Shop s=data.shops.get(key(name));if(s==null)return Result.NOT_FOUND;if(amount<=0)return Result.INVALID;ItemStack unit=item(s);if(unit.isEmpty()||countMatching(seller,unit)<amount)return Result.NO_ITEMS;BigDecimal total=s.price.multiply(BigDecimal.valueOf(amount));if(!s.admin&&!economy.has(s.owner,total))return Result.SHOP_NO_MONEY;removeMatching(seller,unit,amount);economy.deposit(seller.getUUID(),total);if(!s.admin){economy.withdraw(s.owner,total);s.stock+=amount;}save();return Result.OK;}
    public Set<String> names(){return Collections.unmodifiableSet(data.shops.keySet());}
    public String describe(String name){Shop s=data.shops.get(key(name));if(s==null)return null;return s.name+" | "+s.price.toPlainString()+" coins | stock: "+(s.admin?"infinite":s.stock)+" | owner: "+s.ownerName;}
    private ItemStack item(Shop s){try{return ItemStack.of(NbtUtils.snbtToStructure(s.item));}catch(Exception e){return ItemStack.EMPTY;}}
    private void give(ServerPlayer p,ItemStack unit,int amount){int left=amount;while(left>0){int n=Math.min(unit.getMaxStackSize(),left);ItemStack stack=unit.copyWithCount(n);if(!p.getInventory().add(stack))p.drop(stack,false);left-=n;}}
    private int countMatching(ServerPlayer p,ItemStack unit){int n=0;for(ItemStack s:p.getInventory().items)if(ItemStack.isSameItemSameTags(s,unit))n+=s.getCount();return n;}
    private void removeMatching(ServerPlayer p,ItemStack unit,int amount){int left=amount;for(ItemStack s:p.getInventory().items){if(left<=0)break;if(ItemStack.isSameItemSameTags(s,unit)){int n=Math.min(left,s.getCount());s.shrink(n);left-=n;}}}
    private String key(String s){return s.toLowerCase(Locale.ROOT);}
    public enum Result{OK,NOT_FOUND,INVALID,NO_STOCK,NO_MONEY,NO_ITEMS,SHOP_NO_MONEY}
    private static final class Data{Map<String,Shop> shops=new LinkedHashMap<>();}
    private static final class Shop{String name;UUID owner;String ownerName;String item;BigDecimal price;int stock;boolean admin;}
}
