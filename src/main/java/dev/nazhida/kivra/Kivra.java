package dev.nazhida.kivra;

import com.mojang.logging.LogUtils;
import dev.nazhida.kivra.chat.ChatModule;
import dev.nazhida.kivra.command.EconomyCommands;
import dev.nazhida.kivra.command.EssentialsCommands;
import dev.nazhida.kivra.command.KitCommands;
import dev.nazhida.kivra.command.KivraCommands;
import dev.nazhida.kivra.economy.EconomyService;
import dev.nazhida.kivra.essentials.EssentialsService;
import dev.nazhida.kivra.kits.KitService;
import dev.nazhida.kivra.permissions.PermissionService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Kivra.MOD_ID)
public final class Kivra {
    public static final String MOD_ID="kivra";
    public static final Logger LOGGER=LogUtils.getLogger();
    private static PermissionService permissions;
    private static EconomyService economy;
    private static EssentialsService essentials;
    private static KitService kits;

    public Kivra(){MinecraftForge.EVENT_BUS.register(this);MinecraftForge.EVENT_BUS.register(new ChatModule());}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event){
        permissions=new PermissionService(event.getServer());permissions.load();
        economy=new EconomyService(event.getServer());economy.load();
        essentials=new EssentialsService(event.getServer());essentials.load();
        kits=new KitService(event.getServer());kits.load();
        LOGGER.info("Kivra permissions, chat, economy, essentials and kits modules loaded.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event){
        KivraCommands.register(event.getDispatcher());
        EconomyCommands.register(event.getDispatcher());
        EssentialsCommands.register(event.getDispatcher());
        KitCommands.register(event.getDispatcher());
    }

    public static PermissionService permissions(){if(permissions==null)throw new IllegalStateException("Kivra permissions are not initialized yet");return permissions;}
    public static EconomyService economy(){if(economy==null)throw new IllegalStateException("Kivra economy is not initialized yet");return economy;}
    public static EssentialsService essentials(){if(essentials==null)throw new IllegalStateException("Kivra essentials are not initialized yet");return essentials;}
    public static KitService kits(){if(kits==null)throw new IllegalStateException("Kivra kits are not initialized yet");return kits;}
}
