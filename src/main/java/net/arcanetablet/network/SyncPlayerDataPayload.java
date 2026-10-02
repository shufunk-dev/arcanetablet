package net.arcanetablet.network;

import net.arcanetablet.ArcaneTabletMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record SyncPlayerDataPayload(
        List<String> unlockedIds,
        int deathX,
        int deathY,
        int deathZ,
        String deathDim,
        String scannedStructure,
        int scanX,
        int scanY,
        int scanZ,
        int scanDist,
        int bankLevels,
        long tetherSecRemaining,
        long cooldownSecRemaining,
        boolean isOp,
        String statusMessage,
        int statusCode
) implements CustomPayload {
    public static final CustomPayload.Id<SyncPlayerDataPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ArcaneTabletMod.MOD_ID, "sync_player_data"));

    public static final PacketCodec<RegistryByteBuf, SyncPlayerDataPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.unlockedIds().size());
                for (String id : value.unlockedIds()) {
                    buf.writeString(id);
                }
                buf.writeInt(value.deathX());
                buf.writeInt(value.deathY());
                buf.writeInt(value.deathZ());
                buf.writeString(value.deathDim());
                buf.writeString(value.scannedStructure());
                buf.writeInt(value.scanX());
                buf.writeInt(value.scanY());
                buf.writeInt(value.scanZ());
                buf.writeInt(value.scanDist());
                buf.writeInt(value.bankLevels());
                buf.writeLong(value.tetherSecRemaining());
                buf.writeLong(value.cooldownSecRemaining());
                buf.writeBoolean(value.isOp());
                buf.writeString(value.statusMessage());
                buf.writeInt(value.statusCode());
            },
            buf -> {
                int size = buf.readInt();
                List<String> unlocks = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    unlocks.add(buf.readString());
                }
                int dx = buf.readInt();
                int dy = buf.readInt();
                int dz = buf.readInt();
                String dDim = buf.readString();
                String sStruct = buf.readString();
                int sx = buf.readInt();
                int sy = buf.readInt();
                int sz = buf.readInt();
                int sDist = buf.readInt();
                int bLevels = buf.readInt();
                long tSec = buf.readLong();
                long cdSec = buf.readLong();
                boolean op = buf.readBoolean();
                String msg = buf.readString();
                int code = buf.readInt();
                return new SyncPlayerDataPayload(unlocks, dx, dy, dz, dDim, sStruct, sx, sy, sz, sDist, bLevels, tSec, cdSec, op, msg, code);
            }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
