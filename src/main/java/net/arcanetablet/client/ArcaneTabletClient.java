package net.arcanetablet.client;

import net.arcanetablet.ArcaneTabletMod;
import net.arcanetablet.client.gui.ArcaneTabletScreen;
import net.arcanetablet.network.ModMessages;
import net.arcanetablet.network.ModMessages.RequestSyncPayload;
import net.arcanetablet.network.ModMessages.SyncPlayerDataPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ArcaneTabletClient implements ClientModInitializer {

    // Local client cache
    public static final Set<String> UNLOCKED_ACTIONS = new HashSet<>();
    public static BlockPos LAST_DEATH_POS = null;
    public static String LAST_DEATH_DIM = "";
    public static String LAST_SCAN_STRUCT = "";
    public static BlockPos LAST_SCAN_POS = null;
    public static int LAST_SCAN_DIST = -1;
    public static int BANK_LEVELS = 0;
    public static long TETHER_SEC_REMAINING = 0;
    public static long COOLDOWN_SEC_REMAINING = 0;
    public static boolean IS_OP = false;
    public static String STATUS_MESSAGE = "Matrix Standby";
    public static int STATUS_CODE = 0; // 0=Info, 1=Success, 2=Error
    public static long STATUS_TIME = 0;

    @Override
    public void onInitializeClient() {
        ArcaneTabletMod.LOGGER.info("Initializing Arcane Tablet Client!");

        ClientPlayNetworking.registerGlobalReceiver(SyncPlayerDataPayload.TYPE, (payload, context) -> {
            receiveSyncPacket(payload);
        });
    }

    public static void receiveSyncPacket(SyncPlayerDataPayload payload) {
        handleSyncPacket(payload);
    }

    public static void handleSyncPacket(SyncPlayerDataPayload payload) {
        Minecraft.getInstance().execute(() -> {
            UNLOCKED_ACTIONS.clear();
            UNLOCKED_ACTIONS.addAll(payload.unlockedActionIds());

            if (payload.deathX() != 0 || payload.deathY() != 0 || payload.deathZ() != 0) {
                LAST_DEATH_POS = new BlockPos(payload.deathX(), payload.deathY(), payload.deathZ());
                LAST_DEATH_DIM = payload.deathDimension();
            }

            if (!payload.scannedStructure().isEmpty()) {
                LAST_SCAN_STRUCT = payload.scannedStructure();
                LAST_SCAN_POS = new BlockPos(payload.scanX(), payload.scanY(), payload.scanZ());
                LAST_SCAN_DIST = payload.scanDistance();
            }

            BANK_LEVELS = payload.bankLevels();
            TETHER_SEC_REMAINING = payload.spiritTetherRemainingSec();
            COOLDOWN_SEC_REMAINING = payload.cooldownRemainingSec();
            IS_OP = payload.isOp();
            STATUS_MESSAGE = payload.statusMessage();
            STATUS_CODE = payload.statusCode();
            STATUS_TIME = System.currentTimeMillis();

            // If screen is open, refresh widgets
            if (Minecraft.getInstance().gui.screen() instanceof ArcaneTabletScreen screen) {
                screen.refreshStatus();
            }
        });
    }

    public static void openScreen(ItemStack stack) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            // Request fresh telemetry from server
            ModMessages.sendToServer(new RequestSyncPayload());
            client.gui.setScreen(new ArcaneTabletScreen(Component.literal("Quantum Command Matrix"), stack));
        }
    }
}
