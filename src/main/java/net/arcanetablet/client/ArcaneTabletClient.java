package net.arcanetablet.client;

import net.arcanetablet.ArcaneTabletMod;
import net.arcanetablet.client.gui.ArcaneTabletScreen;
import net.arcanetablet.network.ModMessages;
import net.arcanetablet.network.RequestSyncPayload;
import net.arcanetablet.network.SyncPlayerDataPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

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

        ClientPlayNetworking.registerGlobalReceiver(SyncPlayerDataPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                UNLOCKED_ACTIONS.clear();
                UNLOCKED_ACTIONS.addAll(payload.unlockedIds());

                if (payload.deathX() != 0 || payload.deathY() != 0 || payload.deathZ() != 0) {
                    LAST_DEATH_POS = new BlockPos(payload.deathX(), payload.deathY(), payload.deathZ());
                    LAST_DEATH_DIM = payload.deathDim();
                }

                if (!payload.scannedStructure().isEmpty()) {
                    LAST_SCAN_STRUCT = payload.scannedStructure();
                    LAST_SCAN_POS = new BlockPos(payload.scanX(), payload.scanY(), payload.scanZ());
                    LAST_SCAN_DIST = payload.scanDist();
                }

                BANK_LEVELS = payload.bankLevels();
                TETHER_SEC_REMAINING = payload.tetherSecRemaining();
                COOLDOWN_SEC_REMAINING = payload.cooldownSecRemaining();
                IS_OP = payload.isOp();
                STATUS_MESSAGE = payload.statusMessage();
                STATUS_CODE = payload.statusCode();
                STATUS_TIME = System.currentTimeMillis();

                // If screen is open, refresh widgets
                if (MinecraftClient.getInstance().currentScreen instanceof ArcaneTabletScreen screen) {
                    screen.refreshStatus();
                }
            });
        });
    }

    public static void openScreen(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // Request fresh telemetry from server
            ClientPlayNetworking.send(new RequestSyncPayload());
            client.setScreen(new ArcaneTabletScreen(Text.literal("Quantum Command Matrix"), stack));
        }
    }
}
