package net.arcanetablet.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArcaneWorldData extends SavedData {
    private static final String DATA_NAME = "arcane_tablet_world_data";
    private final Map<UUID, PlayerArcaneData> playerDataMap;

    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<ArcaneWorldData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(UUID_CODEC, PlayerArcaneData.CODEC)
                            .optionalFieldOf("Players", new HashMap<>())
                            .forGetter(ArcaneWorldData::getPlayerDataMap)
            ).apply(instance, ArcaneWorldData::new)
    );

    public static final SavedDataType<ArcaneWorldData> TYPE = new SavedDataType<>(
            DATA_NAME,
            ArcaneWorldData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public ArcaneWorldData() {
        this(new HashMap<>());
    }

    public ArcaneWorldData(Map<UUID, PlayerArcaneData> map) {
        this.playerDataMap = new HashMap<>(map);
    }

    public Map<UUID, PlayerArcaneData> getPlayerDataMap() {
        return playerDataMap;
    }

    public static ArcaneWorldData getServerState(ServerLevel world) {
        ServerLevel overworld = world.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public PlayerArcaneData getOrCreatePlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, id -> {
            setDirty();
            return new PlayerArcaneData();
        });
    }
}
