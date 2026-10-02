package net.arcanetablet.network;

import net.arcanetablet.ArcaneTabletMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ExecuteActionPayload(String actionId, boolean preferBankXp) implements CustomPayload {
    public static final CustomPayload.Id<ExecuteActionPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ArcaneTabletMod.MOD_ID, "execute_action"));

    public static final PacketCodec<RegistryByteBuf, ExecuteActionPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING,
                    ExecuteActionPayload::actionId,
                    PacketCodecs.BOOLEAN,
                    ExecuteActionPayload::preferBankXp,
                    ExecuteActionPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
