package net.arcanetablet.mixin;

import net.arcanetablet.data.ArcaneWorldData;
import net.arcanetablet.data.PlayerArcaneData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void arcaneTablet$onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.level() instanceof ServerLevel serverLevel) {
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverLevel);
            PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUUID());
            playerData.setLastDeath(player.blockPosition(), serverLevel.dimension().identifier().toString());

            if (playerData.isSpiritTetherActive()) {
                player.sendSystemMessage(Component.literal("§d⚡ [Spirit Tether] Your inventory was preserved across the void!"));
                playerData.clearSpiritTether();
            }

            worldData.setDirty();
        }
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void arcaneTablet$copyInventoryOnRespawn(ServerPlayer oldPlayer, boolean keepEverything, CallbackInfo ci) {
        ServerPlayer newPlayer = (ServerPlayer) (Object) this;
        if (!keepEverything && newPlayer.level() instanceof ServerLevel serverLevel) {
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverLevel);
            PlayerArcaneData playerData = worldData.getOrCreatePlayerData(oldPlayer.getUUID());

            // If player died with keepInventory-like tether, restore inventory
            if (playerData.isSpiritTetherActive()) {
                newPlayer.getInventory().replaceWith(oldPlayer.getInventory());
            }
        }
    }
}
