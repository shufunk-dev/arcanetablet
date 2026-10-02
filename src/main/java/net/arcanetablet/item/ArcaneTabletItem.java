package net.arcanetablet.item;

import net.arcanetablet.data.ArcaneWorldData;
import net.arcanetablet.data.PlayerArcaneData;
import net.arcanetablet.network.ModMessages;
import net.arcanetablet.sound.ModSounds;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class ArcaneTabletItem extends Item {

    public ArcaneTabletItem(Settings settings) {
        super(settings);
    }

    public static int getBankLevels(ItemStack stack) {
        if (stack.contains(DataComponentTypes.CUSTOM_DATA)) {
            NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
            return nbt.getInt("BankLevels").orElse(0);
        }
        return 0;
    }

    public static void setBankLevels(ItemStack stack, int levels) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        nbt.putInt("BankLevels", Math.max(0, levels));
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.TABLET_OPEN, SoundCategory.PLAYERS, 0.8f, 1.0f);

        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            ServerWorld serverWorld = (ServerWorld) world;
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverWorld);
            PlayerArcaneData playerData = worldData.getOrCreatePlayerData(serverPlayer.getUuid());

            // Send sync packet to client
            ModMessages.sendSyncPacket(serverPlayer, playerData, stack, "Command Matrix Active", 0);
        } else if (world.isClient()) {
            net.arcanetablet.client.ArcaneTabletClient.openScreen(stack);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        int bank = getBankLevels(stack);
        textConsumer.accept(Text.literal("§b⚡ Stored Energy Bank: §e" + bank + " Levels"));
        textConsumer.accept(Text.literal("§7Right-Click to access §fQuantum Command Matrix§7."));
        textConsumer.accept(Text.literal("§8Commands are unlocked via survival progression & consume XP."));
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
