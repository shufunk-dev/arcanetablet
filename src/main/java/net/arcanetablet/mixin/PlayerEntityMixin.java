package net.arcanetablet.mixin;

import net.arcanetablet.data.ArcaneWorldData;
import net.arcanetablet.data.PlayerArcaneData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void arcaneTablet$preserveInventoryIfTethered(ServerLevel level, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ArcaneWorldData worldData = ArcaneWorldData.getServerState(level);
        PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUUID());

        if (playerData.isSpiritTetherActive()) {
            ci.cancel(); // Prevent items from dropping on the ground!
        }
    }
}
