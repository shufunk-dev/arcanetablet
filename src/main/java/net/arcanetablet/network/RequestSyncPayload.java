package net.arcanetablet.network;

import net.arcanetablet.ArcaneTabletMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestSyncPayload() implements CustomPayload {
    public static final CustomPayload.Id<RequestSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ArcaneTabletMod.MOD_ID, "request_sync"));

    public static final PacketCodec<RegistryByteBuf, RequestSyncPayload> CODEC =
            PacketCodec.unit(new RequestSyncPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
