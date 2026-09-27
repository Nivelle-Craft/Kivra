package dev.nazhida.kivra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.nazhida.kivra.Kivra;
import dev.nazhida.kivra.shops.ShopService.Result;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.math.BigDecimal;

public final class ShopCommands {
    private ShopCommands(){}
    public static void register(CommandDispatcher<CommandSourceStack>d){
        d.register(Commands.literal("shops").requires(s->allowed(s,"kivra.shop.use")).executes(c->{msg(c.getSource(),"Shops: "+String.join(", ",Kivra.shops().names()));return 1;}));
        d.register(Commands.literal("shop").requires(s->allowed(s,"kivra.shop.use"))
            .then(Commands.literal("info").then(Commands.argument("name",StringArgumentType.word()).executes(c->{String v=Kivra.shops().describe(StringArgumentType.getString(c,"name"));msg(c.getSource(),v==null?"Shop not found.":v);return v==null?0:1;})))
            .then(Commands.literal("buy").requires(s->allowed(s,"kivra.shop.buy")).then(Commands.argument("name",StringArgumentType.word()).then(Commands.argument("amount",IntegerArgumentType.integer(1)).executes(c->{Result r=Kivra.shops().buy(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"),IntegerArgumentType.getInteger(c,"amount"));msg(c.getSource(),message(r,true));return r==Result.OK?1:0;}))))
            .then(Commands.literal("sell").requires(s->allowed(s,"kivra.shop.sell")).then(Commands.argument("name",StringArgumentType.word()).then(Commands.argument("amount",IntegerArgumentType.integer(1)).executes(c->{Result r=Kivra.shops().sell(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"),IntegerArgumentType.getInteger(c,"amount"));msg(c.getSource(),message(r,false));return r==Result.OK?1:0;}))))
            .then(Commands.literal("create").requires(s->allowed(s,"kivra.shop.create")).then(Commands.argument("name",StringArgumentType.word()).then(Commands.argument("price",DoubleArgumentType.doubleArg(0.01)).then(Commands.argument("stock",IntegerArgumentType.integer(0)).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();boolean ok=Kivra.shops().create(p,StringArgumentType.getString(c,"name"),BigDecimal.valueOf(DoubleArgumentType.getDouble(c,"price")),IntegerArgumentType.getInteger(c,"stock"),false);msg(c.getSource(),ok?"Shop created using the item in your main hand.":"Could not create shop.");return ok?1:0;})))))
            .then(Commands.literal("restock").requires(s->allowed(s,"kivra.shop.create")).then(Commands.argument("name",StringArgumentType.word()).then(Commands.argument("amount",IntegerArgumentType.integer(1)).executes(c->{boolean ok=Kivra.shops().restock(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"),IntegerArgumentType.getInteger(c,"amount"));msg(c.getSource(),ok?"Shop restocked.":"Could not restock shop.");return ok?1:0;}))))
            .then(Commands.literal("delete").requires(s->allowed(s,"kivra.shop.create")).then(Commands.argument("name",StringArgumentType.word()).executes(c->{boolean ok=Kivra.shops().delete(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"),false);msg(c.getSource(),ok?"Shop deleted.":"Shop not found or not yours.");return ok?1:0;}))));
        d.register(Commands.literal("kivrashop").requires(s->allowed(s,"kivra.shop.admin"))
            .then(Commands.literal("createadmin").then(Commands.argument("name",StringArgumentType.word()).then(Commands.argument("price",DoubleArgumentType.doubleArg(0.01)).executes(c->{boolean ok=Kivra.shops().create(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"),BigDecimal.valueOf(DoubleArgumentType.getDouble(c,"price")),0,true);msg(c.getSource(),ok?"Admin shop created with infinite stock.":"Could not create admin shop.");return ok?1:0;}))))
            .then(Commands.literal("delete").then(Commands.argument("name",StringArgumentType.word()).executes(c->{boolean ok=Kivra.shops().delete(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"),true);msg(c.getSource(),ok?"Shop deleted.":"Shop not found.");return ok?1:0;}))));
    }
    private static boolean allowed(CommandSourceStack s,String node){if(s.hasPermission(4))return true;try{return Kivra.permissions().hasPermission(s.getPlayerOrException(),node);}catch(Exception e){return false;}}
    private static String message(Result r,boolean buy){return switch(r){case OK->buy?"Purchase completed.":"Sale completed.";case NOT_FOUND->"Shop not found.";case INVALID->"Invalid shop or amount.";case NO_STOCK->"Not enough stock.";case NO_MONEY->"You do not have enough money.";case NO_ITEMS->"You do not have enough matching items.";case SHOP_NO_MONEY->"The shop owner cannot afford this purchase.";};}
    private static void msg(CommandSourceStack s,String m){s.sendSuccess(()->Component.literal(m),false);}
}
