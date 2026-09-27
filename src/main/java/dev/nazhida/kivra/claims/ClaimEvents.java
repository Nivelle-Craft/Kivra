package dev.nazhida.kivra.claims;

import dev.nazhida.kivra.Kivra;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClaimEvents {
    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event){if(event.getPlayer() instanceof ServerPlayer p&&!Kivra.claims().canBuild(p,event.getPos()))event.setCanceled(true);}
    @SubscribeEvent
    public void onPlace(BlockEvent.EntityPlaceEvent event){if(event.getEntity() instanceof ServerPlayer p&&!Kivra.claims().canBuild(p,event.getPos()))event.setCanceled(true);}
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){if(event.getEntity() instanceof ServerPlayer p&&!Kivra.claims().canInteract(p,event.getPos()))event.setCanceled(true);}
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event){if(event.getEntity() instanceof ServerPlayer p&&!Kivra.claims().canBuild(p,event.getPos()))event.setCanceled(true);}
}
