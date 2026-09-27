package dev.nazhida.kivra.claims;

import dev.nazhida.kivra.Kivra;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClaimEvents {
    @SubscribeEvent public void onBreak(BlockEvent.BreakEvent e){if(e.getPlayer() instanceof ServerPlayer p&&!Kivra.claims().canBuild(p,e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void onPlace(BlockEvent.EntityPlaceEvent e){if(e.getEntity() instanceof ServerPlayer p&&!Kivra.claims().canBuild(p,e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void onRightClickBlock(PlayerInteractEvent.RightClickBlock e){if(e.getEntity() instanceof ServerPlayer p&&!Kivra.claims().canInteract(p,e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e){if(e.getEntity() instanceof ServerPlayer p&&!Kivra.claims().canBuild(p,e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void onRightClickEntity(PlayerInteractEvent.EntityInteract e){if(e.getEntity() instanceof ServerPlayer p&&!Kivra.claims().canInteract(p,e.getTarget().blockPosition()))e.setCanceled(true);}
    @SubscribeEvent public void onAttackEntity(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&!Kivra.claims().canBuild(p,e.getTarget().blockPosition()))e.setCanceled(true);}
    @SubscribeEvent public void onExplosion(ExplosionEvent.Detonate e){e.getAffectedBlocks().removeIf(pos->Kivra.claims().isProtected(e.getLevel(),pos));}
    @SubscribeEvent public void onFluid(BlockEvent.FluidPlaceBlockEvent e){BlockPos source=e.getLiquidPos();if(Kivra.claims().isProtected(e.getLevel(),e.getPos())&&!Kivra.claims().isProtected(e.getLevel(),source))e.setCanceled(true);}
}
