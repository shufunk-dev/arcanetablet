package net.arcanetablet.event;

import net.arcanetablet.ArcaneTabletMod;
import net.arcanetablet.data.ArcaneWorldData;
import net.arcanetablet.data.PlayerArcaneData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArcaneTabletMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArcaneForgeEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel serverLevel) {
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverLevel);
            PlayerArcaneData data = worldData.getOrCreatePlayerData(player.getUUID());

            BlockPos dPos = player.blockPosition();
            String dim = player.level().dimension().identifier().toString();
            data.setLastDeath(dPos, dim);
            worldData.setDirty();
        }
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel serverLevel) {
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverLevel);
            PlayerArcaneData data = worldData.getOrCreatePlayerData(player.getUUID());

            if (data.isSpiritTetherActive()) {
                // Clear drops on death so inventory items aren't spilled
                event.getDrops().clear();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            if (newPlayer.level() instanceof ServerLevel serverLevel) {
                ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverLevel);
                PlayerArcaneData data = worldData.getOrCreatePlayerData(newPlayer.getUUID());

                if (data.isSpiritTetherActive()) {
                    // Restore entire inventory from old player to new player
                    newPlayer.getInventory().replaceWith(oldPlayer.getInventory());
                    data.clearSpiritTether();
                    worldData.setDirty();
                }
            }
        }
    }
}
