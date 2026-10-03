package net.arcanetablet.item;

import net.arcanetablet.data.ArcaneWorldData;
import net.arcanetablet.data.PlayerArcaneData;
import net.arcanetablet.network.ModMessages;
import net.arcanetablet.sound.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ArcaneTabletItem extends Item {

    public ArcaneTabletItem(Properties properties) {
        super(properties);
    }

    public static int getBankLevels(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            return nbt.getIntOr("BankLevels", 0);
        }
        return 0;
    }

    public static void setBankLevels(ItemStack stack, int levels) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbt = customData.copyTag();
        nbt.putInt("BankLevels", Math.max(0, levels));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.TABLET_OPEN.get(), SoundSource.PLAYERS, 0.8f, 1.0f);

        if (!world.isClientSide() && user instanceof ServerPlayer serverPlayer) {
            ServerLevel serverWorld = (ServerLevel) world;
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverWorld);
            PlayerArcaneData playerData = worldData.getOrCreatePlayerData(serverPlayer.getUUID());

            // Send sync packet to client
            ModMessages.sendSyncPacket(serverPlayer, playerData, stack, "Command Matrix Active", 0);
        } else if (world.isClientSide()) {
            net.arcanetablet.client.ArcaneTabletClient.openScreen(stack);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag tooltipFlag) {
        int bank = getBankLevels(stack);
        tooltipConsumer.accept(Component.literal("§b⚡ Stored Energy Bank: §e" + bank + " Levels"));
        tooltipConsumer.accept(Component.literal("§7Right-Click to access §fQuantum Command Matrix§7."));
        tooltipConsumer.accept(Component.literal("§8Commands are unlocked via survival progression & consume XP."));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipConsumer, tooltipFlag);
    }
}
