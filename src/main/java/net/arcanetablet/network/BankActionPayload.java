package net.arcanetablet.network;

import net.arcanetablet.ArcaneTabletMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BankActionPayload(int actionType) implements CustomPayload {
    public static final CustomPayload.Id<BankActionPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ArcaneTabletMod.MOD_ID, "bank_action"));

    public static final PacketCodec<RegistryByteBuf, BankActionPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,
                    BankActionPayload::actionType,
                    BankActionPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
