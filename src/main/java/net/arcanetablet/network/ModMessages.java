package net.arcanetablet.network;

import com.mojang.datafixers.util.Pair;
import net.arcanetablet.ArcaneTabletMod;
import net.arcanetablet.data.ArcaneAction;
import net.arcanetablet.data.ArcaneWorldData;
import net.arcanetablet.data.PlayerArcaneData;
import net.arcanetablet.item.ArcaneTabletItem;
import net.arcanetablet.item.ModItems;
import net.arcanetablet.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ModMessages {

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                BankActionPayload.TYPE,
                BankActionPayload.STREAM_CODEC,
                ModMessages::handleBankActionPayload
        );

        registrar.playToServer(
                ExecuteActionPayload.TYPE,
                ExecuteActionPayload.STREAM_CODEC,
                ModMessages::handleExecuteActionPayload
        );

        registrar.playToServer(
                RequestSyncPayload.TYPE,
                RequestSyncPayload.STREAM_CODEC,
                ModMessages::handleRequestSyncPayload
        );

        registrar.playToClient(
                SyncPlayerDataPayload.TYPE,
                SyncPlayerDataPayload.STREAM_CODEC,
                ModMessages::handleSyncPlayerDataPayload
        );
    }

    public static void sendToServer(CustomPacketPayload payload) {
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    // Payload Definitions
    public record BankActionPayload(int actionType) implements CustomPacketPayload {
        public static final Type<BankActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ArcaneTabletMod.MOD_ID, "bank_action"));
        public static final StreamCodec<FriendlyByteBuf, BankActionPayload> STREAM_CODEC = StreamCodec.ofMember(
                BankActionPayload::encode,
                BankActionPayload::decode
        );

        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(actionType);
        }

        public static BankActionPayload decode(FriendlyByteBuf buf) {
            return new BankActionPayload(buf.readInt());
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ExecuteActionPayload(String actionId, boolean preferBankXp) implements CustomPacketPayload {
        public static final Type<ExecuteActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ArcaneTabletMod.MOD_ID, "execute_action"));
        public static final StreamCodec<FriendlyByteBuf, ExecuteActionPayload> STREAM_CODEC = StreamCodec.ofMember(
                ExecuteActionPayload::encode,
                ExecuteActionPayload::decode
        );

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(actionId);
            buf.writeBoolean(preferBankXp);
        }

        public static ExecuteActionPayload decode(FriendlyByteBuf buf) {
            return new ExecuteActionPayload(buf.readUtf(), buf.readBoolean());
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestSyncPayload() implements CustomPacketPayload {
        public static final Type<RequestSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ArcaneTabletMod.MOD_ID, "request_sync"));
        public static final StreamCodec<FriendlyByteBuf, RequestSyncPayload> STREAM_CODEC = StreamCodec.ofMember(
                RequestSyncPayload::encode,
                RequestSyncPayload::decode
        );

        public void encode(FriendlyByteBuf buf) {}

        public static RequestSyncPayload decode(FriendlyByteBuf buf) {
            return new RequestSyncPayload();
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SyncPlayerDataPayload(
            List<String> unlockedActionIds,
            int deathX, int deathY, int deathZ, String deathDimension,
            String scannedStructure, int scanX, int scanY, int scanZ, int scanDistance,
            int bankLevels, long spiritTetherRemainingSec, long cooldownRemainingSec,
            boolean isOp, String statusMessage, int statusCode
    ) implements CustomPacketPayload {
        public static final Type<SyncPlayerDataPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ArcaneTabletMod.MOD_ID, "sync_player_data"));
        public static final StreamCodec<FriendlyByteBuf, SyncPlayerDataPayload> STREAM_CODEC = StreamCodec.ofMember(
                SyncPlayerDataPayload::encode,
                SyncPlayerDataPayload::decode
        );

        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(unlockedActionIds.size());
            for (String id : unlockedActionIds) {
                buf.writeUtf(id);
            }
            buf.writeInt(deathX);
            buf.writeInt(deathY);
            buf.writeInt(deathZ);
            buf.writeUtf(deathDimension);
            buf.writeUtf(scannedStructure);
            buf.writeInt(scanX);
            buf.writeInt(scanY);
            buf.writeInt(scanZ);
            buf.writeInt(scanDistance);
            buf.writeInt(bankLevels);
            buf.writeLong(spiritTetherRemainingSec);
            buf.writeLong(cooldownRemainingSec);
            buf.writeBoolean(isOp);
            buf.writeUtf(statusMessage);
            buf.writeInt(statusCode);
        }

        public static SyncPlayerDataPayload decode(FriendlyByteBuf buf) {
            int count = buf.readInt();
            List<String> unlocked = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                unlocked.add(buf.readUtf());
            }
            int dX = buf.readInt();
            int dY = buf.readInt();
            int dZ = buf.readInt();
            String dDim = buf.readUtf();
            String sStruct = buf.readUtf();
            int sX = buf.readInt();
            int sY = buf.readInt();
            int sZ = buf.readInt();
            int sDist = buf.readInt();
            int bank = buf.readInt();
            long tSec = buf.readLong();
            long cdSec = buf.readLong();
            boolean isOp = buf.readBoolean();
            String status = buf.readUtf();
            int code = buf.readInt();

            return new SyncPlayerDataPayload(unlocked, dX, dY, dZ, dDim, sStruct, sX, sY, sZ, sDist, bank, tSec, cdSec, isOp, status, code);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet Handlers
    private static void handleBankActionPayload(BankActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                handleBankAction(player, payload.actionType());
            }
        });
    }

    private static void handleExecuteActionPayload(ExecuteActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                handleExecuteAction(player, payload.actionId(), payload.preferBankXp());
            }
        });
    }

    private static void handleRequestSyncPayload(RequestSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.level() instanceof ServerLevel serverLevel) {
                ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverLevel);
                PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUUID());
                ItemStack tablet = findTablet(player);
                sendSyncPacket(player, playerData, tablet, "Telemetry Synchronized", 0);
            }
        });
    }

    private static void handleSyncPlayerDataPayload(SyncPlayerDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.arcanetablet.client.ArcaneTabletClient.receiveSyncPacket(payload);
        });
    }

    // Server Action Handlers
    private static void handleBankAction(ServerPlayer player, int actionType) {
        if (player.level() instanceof ServerLevel serverLevel) {
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverLevel);
            PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUUID());
            ItemStack tablet = findTablet(player);

            if (tablet.isEmpty()) {
                sendSyncPacket(player, playerData, tablet, "Arcane Tablet not found in inventory!", 2);
                return;
            }

            int currentBank = ArcaneTabletItem.getBankLevels(tablet);

            if (actionType == 0) { // Deposit 1 level
                if (player.experienceLevel >= 1) {
                    player.setExperienceLevels(player.experienceLevel - 1);
                    ArcaneTabletItem.setBankLevels(tablet, currentBank + 1);
                    playBankSound(player);
                    sendSyncPacket(player, playerData, tablet, "Deposited 1 Level (Bank: " + (currentBank + 1) + " Lvl)", 1);
                } else {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
                    sendSyncPacket(player, playerData, tablet, "Insufficient XP level to deposit!", 2);
                }
            } else if (actionType == 1) { // Deposit 5 levels
                int deposit = Math.min(5, player.experienceLevel);
                if (deposit > 0) {
                    player.setExperienceLevels(player.experienceLevel - deposit);
                    ArcaneTabletItem.setBankLevels(tablet, currentBank + deposit);
                    playBankSound(player);
                    sendSyncPacket(player, playerData, tablet, "Deposited " + deposit + " Levels (Bank: " + (currentBank + deposit) + " Lvl)", 1);
                } else {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
                    sendSyncPacket(player, playerData, tablet, "Insufficient XP levels to deposit!", 2);
                }
            } else if (actionType == 2) { // Withdraw 1 level
                if (currentBank >= 1) {
                    ArcaneTabletItem.setBankLevels(tablet, currentBank - 1);
                    player.giveExperienceLevels(1);
                    playBankSound(player);
                    sendSyncPacket(player, playerData, tablet, "Withdrew 1 Level (Bank: " + (currentBank - 1) + " Lvl)", 1);
                } else {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
                    sendSyncPacket(player, playerData, tablet, "Energy Bank is empty!", 2);
                }
            } else if (actionType == 3) { // Withdraw 5 levels
                int withdraw = Math.min(5, currentBank);
                if (withdraw > 0) {
                    ArcaneTabletItem.setBankLevels(tablet, currentBank - withdraw);
                    player.giveExperienceLevels(withdraw);
                    playBankSound(player);
                    sendSyncPacket(player, playerData, tablet, "Withdrew " + withdraw + " Levels (Bank: " + (currentBank - withdraw) + " Lvl)", 1);
                } else {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
                    sendSyncPacket(player, playerData, tablet, "Energy Bank is empty!", 2);
                }
            }
        }
    }

    private static void handleExecuteAction(ServerPlayer player, String actionId, boolean preferBankXp) {
        if (player.level() instanceof ServerLevel serverLevel) {
            ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverLevel);
            PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUUID());
            ItemStack tablet = findTablet(player);

            ArcaneAction action = ArcaneAction.byId(actionId);
            if (action == null) {
                sendSyncPacket(player, playerData, tablet, "Unknown command matrix operation!", 2);
                return;
            }

            // Cooldown check (2.5 seconds matrix refresh cooldown)
            if (!playerData.canExecuteAction()) {
                long remaining = playerData.getRemainingCooldownSeconds();
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
                sendSyncPacket(player, playerData, tablet, "Matrix Cooldown: Please wait " + remaining + "s", 2);
                return;
            }

            // Progression Unlock check
            if (!action.isUnlockedFor(player, playerData)) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
                sendSyncPacket(player, playerData, tablet, "Locked! Requires advancement: " + action.getAdvancementName(), 2);
                return;
            }

            int cost = action.getLevelCost();
            int currentBank = tablet.isEmpty() ? 0 : ArcaneTabletItem.getBankLevels(tablet);

            // Deduct XP Cost
            boolean paid = false;
            if (preferBankXp && currentBank >= cost) {
                ArcaneTabletItem.setBankLevels(tablet, currentBank - cost);
                paid = true;
            } else if (player.experienceLevel >= cost) {
                player.setExperienceLevels(player.experienceLevel - cost);
                paid = true;
            } else if (currentBank >= cost) {
                ArcaneTabletItem.setBankLevels(tablet, currentBank - cost);
                paid = true;
            }

            if (!paid) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
                sendSyncPacket(player, playerData, tablet, "Insufficient XP! Requires " + cost + " Levels (Player: " + player.experienceLevel + " Lvl | Bank: " + currentBank + " Lvl)", 2);
                return;
            }

            // Perform Action
            boolean success = executeCommandAction(serverLevel, player, playerData, action, tablet);

            if (success) {
                playerData.setCooldown(2500L); // 2.5s matrix pulse cooldown
                worldData.setDirty();
                sendSyncPacket(player, playerData, tablet, "Executed: " + action.getTitle() + " (-" + cost + " Levels)", 1);
            } else {
                // Refund if failed
                if (preferBankXp && !tablet.isEmpty()) {
                    ArcaneTabletItem.setBankLevels(tablet, ArcaneTabletItem.getBankLevels(tablet) + cost);
                } else {
                    player.giveExperienceLevels(cost);
                }
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
                sendSyncPacket(player, playerData, tablet, "Operation failed or target unavailable. XP Refunded.", 2);
            }
        }
    }

    private static boolean executeCommandAction(ServerLevel world, ServerPlayer player, PlayerArcaneData data, ArcaneAction action, ItemStack tablet) {
        switch (action) {
            // 1. Environmental Control
            case CALL_THE_DAWN -> {
                long current = world.getDayTime();
                long nextDawn = ((current / 24000L) + 1L) * 24000L + 1000L;
                world.setDayTime(nextDawn);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.FLAME, 30);
                player.sendSystemMessage(Component.literal("§e☀ [Call the Dawn] Time accelerated to morning daylight (1000 ticks)."));
                return true;
            }
            case PART_THE_STORM -> {
                world.setWeatherParameters(60000, 0, false, false);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.CLOUD, 40);
                player.sendSystemMessage(Component.literal("§b🌦 [Part the Storm] Skies cleared for 1 in-game day."));
                return true;
            }
            case GATHER_THE_GALE -> {
                world.setWeatherParameters(0, 12000, true, true);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 0.8f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.ELECTRIC_SPARK, 50);
                player.sendSystemMessage(Component.literal("§9⚡ [Gather the Gale] Atmospheric surge triggered: Thunderstorm summoned!"));
                return true;
            }
            case LUNAR_HALT -> {
                long current = world.getDayTime();
                long midnight = (current / 24000L) * 24000L + 18000L;
                if (current % 24000L >= 18000L) {
                    midnight += 24000L;
                }
                world.setDayTime(midnight);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 0.9f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.SCULK_SOUL, 30);
                player.sendSystemMessage(Component.literal("§1🌙 [Lunar Halt] Midnight darkness locked (18000 ticks). Phantom & mob hunt active."));
                return true;
            }

            // 2. Navigation & Exploration
            case BEACON_TETHER -> {
                ItemStack compass = null;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack it = player.getInventory().getItem(i);
                    if (it.is(Items.COMPASS) && it.get(DataComponents.LODESTONE_TRACKER) != null) {
                        compass = it;
                        break;
                    }
                }

                if (compass == null) {
                    player.sendSystemMessage(Component.literal("§c[Beacon Tether] No Lodestone Compass detected in your inventory!"));
                    return false;
                }

                LodestoneTracker tracker = compass.get(DataComponents.LODESTONE_TRACKER);
                if (tracker == null || tracker.target().isEmpty()) {
                    player.sendSystemMessage(Component.literal("§c[Beacon Tether] Lodestone Compass is unlinked or compass target lost!"));
                    return false;
                }

                var targetGlobal = tracker.target().get();
                BlockPos targetPos = targetGlobal.pos();
                ServerLevel targetWorld = world.getServer().getLevel(targetGlobal.dimension());

                if (targetWorld == null) {
                    player.sendSystemMessage(Component.literal("§c[Beacon Tether] Lodestone dimension is currently unloaded!"));
                    return false;
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_WARP.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                player.teleportTo(targetWorld, targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, Collections.emptySet(), player.getYRot(), player.getXRot(), false);
                targetWorld.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_WARP.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                spawnParticles(targetWorld, targetPos, ParticleTypes.PORTAL, 40);
                player.sendSystemMessage(Component.literal("§6🌌 [Beacon Tether] Relocated to Lodestone Anchor @ [" + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ() + "]"));
                return true;
            }
            case SUBSPACE_GATEWAY -> {
                ServerPlayer.RespawnConfig respawnConfig = player.getRespawnConfig();
                ServerLevel targetWorld;
                Vec3 targetPos;
                float yaw;
                float pitch;
                boolean isBed = false;

                if (respawnConfig != null) {
                    targetWorld = world.getServer().getLevel(respawnConfig.respawnData().dimension());
                    if (targetWorld == null) targetWorld = world.getServer().overworld();
                    BlockPos sp = respawnConfig.respawnData().pos();
                    targetPos = new Vec3(sp.getX() + 0.5, sp.getY() + 0.5, sp.getZ() + 0.5);
                    yaw = respawnConfig.respawnData().yaw();
                    pitch = respawnConfig.respawnData().pitch();
                    isBed = true;
                } else {
                    targetWorld = world.getServer().overworld();
                    BlockPos sp = targetWorld.getRespawnData().pos();
                    targetPos = new Vec3(sp.getX() + 0.5, sp.getY() + 0.5, sp.getZ() + 0.5);
                    yaw = player.getYRot();
                    pitch = player.getXRot();
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_WARP.get(), SoundSource.PLAYERS, 1.0f, 1.1f);
                player.teleportTo(targetWorld, targetPos.x(), targetPos.y(), targetPos.z(), Collections.emptySet(), yaw, pitch, false);
                targetWorld.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_WARP.get(), SoundSource.PLAYERS, 1.0f, 1.1f);
                spawnParticles(targetWorld, BlockPos.containing(targetPos), ParticleTypes.REVERSE_PORTAL, 50);

                String dest = isBed ? "Bed / Respawn Anchor" : "World Spawn";
                player.sendSystemMessage(Component.literal("§d🌀 [Subspace Gateway] Recalled through cross-dimensional gateway to " + dest + " @ [" + (int) targetPos.x() + ", " + (int) targetPos.y() + ", " + (int) targetPos.z() + "]"));
                return true;
            }
            case LOCATE_VILLAGE -> {
                BlockPos foundPos = locateStructure(world, StructureTags.VILLAGE, player.blockPosition());
                return handleLocate(world, player, data, "Village", foundPos);
            }
            case LOCATE_ANCIENT_CITY -> {
                BlockPos foundPos = locateStructureKey(world, BuiltinStructures.ANCIENT_CITY, player.blockPosition());
                return handleLocate(world, player, data, "Ancient City", foundPos);
            }
            case LOCATE_TRIAL_CHAMBERS -> {
                BlockPos foundPos = locateStructureKey(world, BuiltinStructures.TRIAL_CHAMBERS, player.blockPosition());
                return handleLocate(world, player, data, "Trial Chamber", foundPos);
            }
            case LOCATE_FORTRESS -> {
                BlockPos foundPos = locateStructureKey(world, BuiltinStructures.FORTRESS, player.blockPosition());
                return handleLocate(world, player, data, "Nether Fortress", foundPos);
            }
            case LOCATE_STRONGHOLD -> {
                BlockPos foundPos = locateStructureKey(world, BuiltinStructures.STRONGHOLD, player.blockPosition());
                return handleLocate(world, player, data, "Stronghold", foundPos);
            }
            case LOCATE_MINESHAFT -> {
                BlockPos foundPos = locateStructure(world, StructureTags.MINESHAFT, player.blockPosition());
                return handleLocate(world, player, data, "Mineshaft", foundPos);
            }
            case LOCATE_MONUMENT -> {
                BlockPos foundPos = locateStructureKey(world, BuiltinStructures.OCEAN_MONUMENT, player.blockPosition());
                return handleLocate(world, player, data, "Ocean Monument", foundPos);
            }
            case LOCATE_OUTPOST -> {
                BlockPos foundPos = locateStructureKey(world, BuiltinStructures.PILLAGER_OUTPOST, player.blockPosition());
                return handleLocate(world, player, data, "Pillager Outpost", foundPos);
            }
            case LOCATE_END_CITY -> {
                BlockPos foundPos = locateStructureKey(world, BuiltinStructures.END_CITY, player.blockPosition());
                return handleLocate(world, player, data, "End City", foundPos);
            }
            case WAYFARERS_SURVEY -> {
                Pair<BlockPos, Holder<Biome>> biomeResult = world.findClosestBiome3d(
                        holder -> holder.is(Biomes.CHERRY_GROVE) || holder.is(Biomes.MUSHROOM_FIELDS) || holder.is(Biomes.BADLANDS) || holder.is(Biomes.JUNGLE) || holder.is(Biomes.DEEP_DARK),
                        player.blockPosition(),
                        6400,
                        32,
                        64
                );
                if (biomeResult != null) {
                    BlockPos bPos = biomeResult.getFirst();
                    int dist = (int) Math.sqrt(player.blockPosition().distSqr(bPos));
                    String bName = biomeResult.getSecond().unwrapKey().map(k -> k.identifier().getPath()).orElse("Special Biome");
                    data.setScanResult(bName, bPos, dist);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_SCAN.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.literal("§a🧭 [Wayfarer's Survey] " + bName.toUpperCase() + " located @ [" + bPos.getX() + ", " + bPos.getZ() + "] (" + dist + "m away)"));
                    return true;
                }
                return false;
            }

            // 3. Sanctuary & Defense
            case TURN_UNDEAD -> {
                AABB box = new AABB(player.blockPosition()).inflate(32.0);
                List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, box, e -> e instanceof Monster || e instanceof Enemy);
                for (LivingEntity e : entities) {
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1200, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 1200, 3));
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 1.4f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.GLOW, 50);
                player.sendSystemMessage(Component.literal("§a✨ [Turn Undead] " + entities.size() + " hostiles illuminated and suppressed for 60s."));
                return true;
            }
            case WARDING_WARD -> {
                AABB box = new AABB(player.blockPosition()).inflate(64.0);
                List<Phantom> phantoms = world.getEntitiesOfClass(Phantom.class, box, e -> true);
                for (Phantom phantom : phantoms) {
                    spawnParticles(world, phantom.blockPosition(), ParticleTypes.SMOKE, 15);
                    phantom.discard();
                }
                player.getStats().setValue(player, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 0);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 1.2f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.NOTE, 30);
                player.sendSystemMessage(Component.literal("§b🎵 [Warding Ward] Banished " + phantoms.size() + " phantoms. Rest restored for 3 in-game days."));
                return true;
            }
            case REPEL_INVADERS -> {
                player.removeEffect(MobEffects.BAD_OMEN);
                player.removeEffect(MobEffects.RAID_OMEN);
                player.removeEffect(MobEffects.TRIAL_OMEN);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 1.3f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.HEART, 20);
                player.sendSystemMessage(Component.literal("§a🛡 [Repel Invaders] All Raid & Bad Omen curses purged safely."));
                return true;
            }
            case PURGE_THE_FALLEN -> {
                AABB box = new AABB(player.blockPosition()).inflate(48.0);
                List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, box, e -> (e instanceof Monster || e instanceof Enemy) && !(e instanceof EnderDragon) && !(e instanceof WitherBoss) && !(e instanceof ElderGuardian));
                for (LivingEntity e : entities) {
                    spawnParticles(world, e.blockPosition(), ParticleTypes.SOUL_FIRE_FLAME, 20);
                    e.discard(); // Zero drops, zero XP
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 0.6f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.EXPLOSION_EMITTER, 1);
                player.sendSystemMessage(Component.literal("§5💥 [Purge the Fallen] " + entities.size() + " hostile anomalies atomized."));
                return true;
            }

            // 4. Preservation
            case SPIRIT_TETHER -> {
                data.setSpiritTether(600000L); // 10 minutes (600,000 ms)
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 1.5f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.TOTEM_OF_UNDYING, 60);
                player.sendSystemMessage(Component.literal("§d🔮 [Spirit Tether] Soul Anchor locked for 10 minutes: inventory will persist across death."));
                return true;
            }
            case GRAVE_COMPASS -> {
                BlockPos dPos = data.getLastDeathPos();
                if (dPos == null) {
                    player.sendSystemMessage(Component.literal("§c[Grave Compass] No death coordinates found in memory banks!"));
                    return false;
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_SCAN.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                String dim = data.getLastDeathDimension().replace("minecraft:", "").toUpperCase();
                player.sendSystemMessage(Component.literal("§d⚰ [Grave Compass] Last Death: [" + dPos.getX() + ", " + dPos.getY() + ", " + dPos.getZ() + "] in " + dim));
                return true;
            }
            case STASIS_SHELL -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 900, 4, false, true, true)); // Resistance V (45s)
                player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 900, 4, false, true, true)); // Mining Fatigue V (45s)
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                spawnParticles(world, player.blockPosition(), ParticleTypes.ENCHANTED_HIT, 50);
                player.sendSystemMessage(Component.literal("§b🛡 [Stasis Shell] Invulnerability barrier active for 45s (Resistance V + Mining Fatigue V)."));
                return true;
            }
            case RESTORE_ANVIL -> {
                BlockPos playerPos = player.blockPosition();
                BlockPos foundAnvil = null;
                BlockState anvilState = null;

                for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-5, -3, -5), playerPos.offset(5, 3, 5))) {
                    BlockState st = world.getBlockState(pos);
                    if (st.is(Blocks.CHIPPED_ANVIL) || st.is(Blocks.DAMAGED_ANVIL)) {
                        foundAnvil = pos.immutable();
                        anvilState = st;
                        break;
                    }
                }

                if (foundAnvil == null) {
                    player.sendSystemMessage(Component.literal("§c[Restore Anvil] No chipped or damaged anvil detected within 5 blocks!"));
                    return false;
                }

                // Restore anvil preserving horizontal facing
                BlockState pristine = Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, anvilState.getValue(AnvilBlock.FACING));
                world.setBlockAndUpdate(foundAnvil, pristine);
                world.playSound(null, foundAnvil.getX(), foundAnvil.getY(), foundAnvil.getZ(), net.minecraft.sounds.SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                spawnParticles(world, foundAnvil, ParticleTypes.WAX_ON, 25);
                player.sendSystemMessage(Component.literal("§6🔨 [Restore Anvil] Damaged anvil successfully reforged to pristine condition!"));
                return true;
            }
        }
        return false;
    }

    private static boolean handleLocate(ServerLevel world, ServerPlayer player, PlayerArcaneData data, String displayName, BlockPos foundPos) {
        if (foundPos != null) {
            int dist = (int) Math.sqrt(player.blockPosition().distSqr(foundPos));
            data.setScanResult(displayName, foundPos, dist);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_SCAN.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            spawnParticles(world, player.blockPosition(), ParticleTypes.SONIC_BOOM, 1);
            player.sendSystemMessage(Component.literal("§e📡 [" + displayName + "] Located @ [" + foundPos.getX() + ", " + foundPos.getY() + ", " + foundPos.getZ() + "] (" + dist + "m away)"));
            return true;
        } else {
            player.sendSystemMessage(Component.literal("§c📡 [" + displayName + "] No structure located within search radius in this dimension!"));
            return false;
        }
    }

    private static BlockPos locateStructure(ServerLevel world, net.minecraft.tags.TagKey<Structure> tagKey, BlockPos center) {
        Registry<Structure> registry = world.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Optional<HolderSet.Named<Structure>> list = registry.get(tagKey);
        if (list.isEmpty()) return null;
        Pair<BlockPos, Holder<Structure>> res = world.getChunkSource().getGenerator().findNearestMapStructure(world, list.get(), center, 100, false);
        return res != null ? res.getFirst() : null;
    }

    private static BlockPos locateStructureKey(ServerLevel world, ResourceKey<Structure> key, BlockPos center) {
        Registry<Structure> registry = world.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> entry = registry.get(key);
        if (entry.isEmpty()) return null;
        Pair<BlockPos, Holder<Structure>> res = world.getChunkSource().getGenerator().findNearestMapStructure(world, HolderSet.direct(entry.get()), center, 100, false);
        return res != null ? res.getFirst() : null;
    }

    private static void spawnParticles(ServerLevel world, BlockPos pos, ParticleOptions particle, int count) {
        world.sendParticles(particle, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, count, 0.5, 0.5, 0.5, 0.05);
    }

    private static void playBankSound(ServerPlayer player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_BANK.get(), SoundSource.PLAYERS, 0.7f, 1.2f);
    }

    public static ItemStack findTablet(ServerPlayer player) {
        if (player.getMainHandItem().is(ModItems.ARCANE_TABLET.get())) return player.getMainHandItem();
        if (player.getOffhandItem().is(ModItems.ARCANE_TABLET.get())) return player.getOffhandItem();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.ARCANE_TABLET.get())) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static void sendSyncPacket(ServerPlayer player, PlayerArcaneData data, ItemStack tablet, String statusMessage, int statusCode) {
        List<String> unlocks = new ArrayList<>();
        for (ArcaneAction action : ArcaneAction.values()) {
            if (action.isUnlockedFor(player, data)) {
                unlocks.add(action.getId());
            }
        }

        BlockPos dPos = data.getLastDeathPos() != null ? data.getLastDeathPos() : BlockPos.ZERO;
        String dDim = data.getLastDeathDimension() != null ? data.getLastDeathDimension() : "";
        BlockPos sPos = data.getLastScannedPos() != null ? data.getLastScannedPos() : BlockPos.ZERO;
        String sStruct = data.getLastScannedStructure() != null ? data.getLastScannedStructure() : "";
        int sDist = data.getLastScannedDistance();
        int bank = tablet.isEmpty() ? 0 : ArcaneTabletItem.getBankLevels(tablet);
        long tSec = data.isSpiritTetherActive() ? (data.getSpiritTetherExpiry() - System.currentTimeMillis()) / 1000L : 0;
        long cdSec = data.getRemainingCooldownSeconds();
        boolean isOp = player.level().getServer() != null && player.level().getServer().getPlayerList().isOp(new net.minecraft.server.players.NameAndId(player.getGameProfile()));

        SyncPlayerDataPayload payload = new SyncPlayerDataPayload(
                unlocks,
                dPos.getX(), dPos.getY(), dPos.getZ(), dDim,
                sStruct, sPos.getX(), sPos.getY(), sPos.getZ(), sDist,
                bank, tSec, cdSec, isOp, statusMessage, statusCode
        );

        sendToPlayer(player, payload);
    }
}
