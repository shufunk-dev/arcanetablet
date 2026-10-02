package net.arcanetablet.mixin;

import net.arcanetablet.data.ArcaneWorldData;
import net.arcanetablet.data.PlayerArcaneData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    private void arcaneTablet$preserveInventoryIfTethered(ServerWorld world, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ArcaneWorldData worldData = ArcaneWorldData.getServerState(world);
        PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUuid());

        if (playerData.isSpiritTetherActive()) {
            ci.cancel(); // Prevent items from dropping on the ground!
        }
    }
}
