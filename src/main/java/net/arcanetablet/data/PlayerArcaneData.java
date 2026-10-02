package net.arcanetablet.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PlayerArcaneData {
    private BlockPos lastDeathPos = null;
    private String lastDeathDimension = "";
    private final Set<String> explicitUnlocks = new HashSet<>();

    // Last located structure/biome cached telemetry
    private String lastScannedStructure = "";
    private BlockPos lastScannedPos = null;
    private int lastScannedDistance = -1;

    // High-Stakes & Balance flags
    private long spiritTetherExpiry = 0;
    private long channelingCooldownExpiry = 0;

    public static final Codec<PlayerArcaneData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.optionalFieldOf("DeathPos").forGetter(data -> Optional.ofNullable(data.getLastDeathPos())),
                    Codec.STRING.optionalFieldOf("DeathDim", "").forGetter(PlayerArcaneData::getLastDeathDimension),
                    Codec.list(Codec.STRING).optionalFieldOf("Unlocks", List.of()).forGetter(data -> new ArrayList<>(data.getExplicitUnlocks())),
                    Codec.STRING.optionalFieldOf("ScanStruct", "").forGetter(PlayerArcaneData::getLastScannedStructure),
                    BlockPos.CODEC.optionalFieldOf("ScanPos").forGetter(data -> Optional.ofNullable(data.getLastScannedPos())),
                    Codec.INT.optionalFieldOf("ScanDist", -1).forGetter(PlayerArcaneData::getLastScannedDistance),
                    Codec.LONG.optionalFieldOf("TetherExp", 0L).forGetter(PlayerArcaneData::getSpiritTetherExpiry),
                    Codec.LONG.optionalFieldOf("CooldownExp", 0L).forGetter(PlayerArcaneData::getChannelingCooldownExpiry)
            ).apply(instance, (deathPos, deathDim, unlocks, scanStruct, scanPos, scanDist, tetherExp, cdExp) -> {
                PlayerArcaneData d = new PlayerArcaneData();
                deathPos.ifPresent(p -> d.setLastDeath(p, deathDim));
                unlocks.forEach(d::addExplicitUnlock);
                scanPos.ifPresent(p -> d.setScanResult(scanStruct, p, scanDist));
                d.spiritTetherExpiry = tetherExp;
                d.channelingCooldownExpiry = cdExp;
                return d;
            })
    );

    public PlayerArcaneData() {
    }

    public BlockPos getLastDeathPos() {
        return lastDeathPos;
    }

    public void setLastDeath(BlockPos pos, String dimension) {
        this.lastDeathPos = pos;
        this.lastDeathDimension = dimension;
    }

    public String getLastDeathDimension() {
        return lastDeathDimension;
    }

    public boolean hasExplicitUnlock(String actionId) {
        return explicitUnlocks.contains(actionId);
    }

    public void addExplicitUnlock(String actionId) {
        explicitUnlocks.add(actionId);
    }

    public Set<String> getExplicitUnlocks() {
        return explicitUnlocks;
    }

    public String getLastScannedStructure() {
        return lastScannedStructure;
    }

    public BlockPos getLastScannedPos() {
        return lastScannedPos;
    }

    public int getLastScannedDistance() {
        return lastScannedDistance;
    }

    public void setScanResult(String structure, BlockPos pos, int distance) {
        this.lastScannedStructure = structure;
        this.lastScannedPos = pos;
        this.lastScannedDistance = distance;
    }

    public long getSpiritTetherExpiry() {
        return spiritTetherExpiry;
    }

    public void setSpiritTether(long durationMs) {
        this.spiritTetherExpiry = System.currentTimeMillis() + durationMs;
    }

    public boolean isSpiritTetherActive() {
        return System.currentTimeMillis() < spiritTetherExpiry;
    }

    public void clearSpiritTether() {
        this.spiritTetherExpiry = 0;
    }

    public long getChannelingCooldownExpiry() {
        return channelingCooldownExpiry;
    }

    public void setChannelingCooldown(long durationMs) {
        this.channelingCooldownExpiry = System.currentTimeMillis() + durationMs;
    }

    public boolean isChannelingOnCooldown() {
        return System.currentTimeMillis() < channelingCooldownExpiry;
    }

    public long getRemainingCooldownSeconds() {
        long remaining = (channelingCooldownExpiry - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }
}
