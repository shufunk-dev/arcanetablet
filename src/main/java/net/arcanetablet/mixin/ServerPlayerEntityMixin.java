package net.arcanetablet.mixin;

import net.arcanetablet.data.ArcaneWorldData;
import net.arcanetablet.data.PlayerArcaneData;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void arcaneTablet$onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverWorld);
            PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUuid());
            playerData.setLastDeath(player.getBlockPos(), serverWorld.getRegistryKey().getValue().toString());

            if (playerData.isSpiritTetherActive()) {
                player.sendMessage(Text.literal("§d⚡ [Spirit Tether] Your inventory was preserved across the void!"), false);
                playerData.clearSpiritTether();
            }

            worldData.markDirty();
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void arcaneTablet$copyInventoryOnRespawn(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity newPlayer = (ServerPlayerEntity) (Object) this;
        if (!alive && newPlayer.getEntityWorld() instanceof ServerWorld serverWorld) {
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverWorld);
            PlayerArcaneData playerData = worldData.getOrCreatePlayerData(oldPlayer.getUuid());

            // If player died with keepInventory-like tether, clone inventory
            if (playerData.isSpiritTetherActive()) {
                newPlayer.getInventory().clone(oldPlayer.getInventory());
            }
        }
    }
}
