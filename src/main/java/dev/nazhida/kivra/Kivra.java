package dev.nazhida.kivra;

import dev.nazhida.kivra.command.KivraCommands;
import dev.nazhida.kivra.permissions.PermissionService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(Kivra.MOD_ID)
public final class Kivra {
    public static final String MOD_ID = "kivra";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static PermissionService permissions;

    public Kivra() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        permissions = new PermissionService(event.getServer());
        permissions.load();
        LOGGER.info("Kivra permissions module loaded.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        KivraCommands.register(event.getDispatcher());
    }

    public static PermissionService permissions() {
        if (permissions == null) {
            throw new IllegalStateException("Kivra permissions are not initialized yet");
        }
        return permissions;
    }
}
