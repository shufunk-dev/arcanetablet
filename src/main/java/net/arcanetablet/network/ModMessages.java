package net.arcanetablet.network;

import com.mojang.datafixers.util.Pair;
import net.arcanetablet.ArcaneTabletMod;
import net.arcanetablet.data.ArcaneAction;
import net.arcanetablet.data.ArcaneWorldData;
import net.arcanetablet.data.PlayerArcaneData;
import net.arcanetablet.item.ArcaneTabletItem;
import net.arcanetablet.item.ModItems;
import net.arcanetablet.sound.ModSounds;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModMessages {

    public static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(ExecuteActionPayload.ID, ExecuteActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BankActionPayload.ID, BankActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestSyncPayload.ID, RequestSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncPlayerDataPayload.ID, SyncPlayerDataPayload.CODEC);

        // Handle sync request
        ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
                    ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverWorld);
                    PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUuid());
                    ItemStack tablet = findTablet(player);
                    sendSyncPacket(player, playerData, tablet, "Telemetry Synchronized", 0);
                }
            });
        });

        // Handle Bank Action
        ServerPlayNetworking.registerGlobalReceiver(BankActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                ItemStack tablet = findTablet(player);
                if (tablet.isEmpty()) return;

                int bank = ArcaneTabletItem.getBankLevels(tablet);
                int pLevels = player.experienceLevel;

                switch (payload.actionType()) {
                    case 0 -> { // Deposit 1
                        if (pLevels >= 1) {
                            player.addExperienceLevels(-1);
                            ArcaneTabletItem.setBankLevels(tablet, bank + 1);
                            playBankSound(player);
                        }
                    }
                    case 1 -> { // Deposit 5
                        if (pLevels >= 5) {
                            player.addExperienceLevels(-5);
                            ArcaneTabletItem.setBankLevels(tablet, bank + 5);
                            playBankSound(player);
                        } else if (pLevels > 0) {
                            player.addExperienceLevels(-pLevels);
                            ArcaneTabletItem.setBankLevels(tablet, bank + pLevels);
                            playBankSound(player);
                        }
                    }
                    case 2 -> { // Withdraw 1
                        if (bank >= 1) {
                            ArcaneTabletItem.setBankLevels(tablet, bank - 1);
                            player.addExperienceLevels(1);
                            playBankSound(player);
                        }
                    }
                    case 3 -> { // Withdraw 5
                        if (bank >= 5) {
                            ArcaneTabletItem.setBankLevels(tablet, bank - 5);
                            player.addExperienceLevels(5);
                            playBankSound(player);
                        } else if (bank > 0) {
                            ArcaneTabletItem.setBankLevels(tablet, 0);
                            player.addExperienceLevels(bank);
                            playBankSound(player);
                        }
                    }
                    case 4 -> { // Deposit All
                        if (pLevels > 0) {
                            player.addExperienceLevels(-pLevels);
                            ArcaneTabletItem.setBankLevels(tablet, bank + pLevels);
                            playBankSound(player);
                        }
                    }
                    case 5 -> { // Withdraw All
                        if (bank > 0) {
                            ArcaneTabletItem.setBankLevels(tablet, 0);
                            player.addExperienceLevels(bank);
                            playBankSound(player);
                        }
                    }
                }

                if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
                    ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverWorld);
                    PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUuid());
                    sendSyncPacket(player, playerData, tablet, "Energy Bank Updated: " + ArcaneTabletItem.getBankLevels(tablet) + " Lvl", 0);
                }
            });
        });

        // Handle Execute Action
        ServerPlayNetworking.registerGlobalReceiver(ExecuteActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

                ItemStack tablet = findTablet(player);
                if (tablet.isEmpty()) {
                    player.sendMessage(Text.literal("§c[Arcane Tablet] No attuned tablet found in inventory!"), false);
                    return;
                }

                ArcaneAction action = ArcaneAction.byId(payload.actionId());
                if (action == null) {
                    player.sendMessage(Text.literal("§c[Arcane Tablet] Unknown rite sequence!"), false);
                    return;
                }

                ArcaneWorldData worldData = ArcaneWorldData.getServerState(serverWorld);
                PlayerArcaneData playerData = worldData.getOrCreatePlayerData(player.getUuid());

                // 1. Check Unlock Criteria
                if (!action.isUnlockedFor(player, playerData)) {
                    serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED, SoundCategory.PLAYERS, 1.0f, 0.8f);
                    sendSyncPacket(player, playerData, tablet, "LOCKED: Requires [" + action.getAdvancementName() + "]", 2);
                    return;
                }

                // 2. Check Action-Specific Restrictions
                if (action == ArcaneAction.CALL_THE_DAWN) {
                    long time = serverWorld.getTimeOfDay() % 24000L;
                    boolean isNight = time >= 12542L && time <= 23458L;
                    boolean isStorm = serverWorld.isRaining() || serverWorld.isThundering();
                    if (!isNight && !isStorm) {
                        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED, SoundCategory.PLAYERS, 1.0f, 0.8f);
                        sendSyncPacket(player, playerData, tablet, "RESTRICTION: Dawn can only be called during Night or Storms.", 2);
                        return;
                    }
                }

                // 3. Check Cost & Pay
                int cost = action.getLevelCost();
                int playerLvl = player.experienceLevel;
                int bankLvl = ArcaneTabletItem.getBankLevels(tablet);

                boolean paid = false;
                if (payload.preferBankXp() && bankLvl >= cost) {
                    ArcaneTabletItem.setBankLevels(tablet, bankLvl - cost);
                    paid = true;
                } else if (playerLvl >= cost) {
                    player.addExperienceLevels(-cost);
                    paid = true;
                } else if (bankLvl >= cost) {
                    ArcaneTabletItem.setBankLevels(tablet, bankLvl - cost);
                    paid = true;
                } else if ((playerLvl + bankLvl) >= cost) {
                    int fromPlayer = playerLvl;
                    int fromBank = cost - fromPlayer;
                    player.addExperienceLevels(-fromPlayer);
                    ArcaneTabletItem.setBankLevels(tablet, bankLvl - fromBank);
                    paid = true;
                }

                if (!paid) {
                    serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_DENIED, SoundCategory.PLAYERS, 1.0f, 0.8f);
                    sendSyncPacket(player, playerData, tablet, "INSUFFICIENT ENERGY: Requires " + cost + " Levels (Player: " + playerLvl + ", Bank: " + bankLvl + ")", 2);
                    return;
                }

                // 4. Execute Effect
                boolean success = executeCommandEffect(action, player, serverWorld, playerData);
                worldData.markDirty();

                if (success) {
                    sendSyncPacket(player, playerData, tablet, "RITE ENGAGED: " + action.getTitle(), 1);
                } else {
                    // Refund cost if failed
                    if (payload.preferBankXp()) {
                        ArcaneTabletItem.setBankLevels(tablet, ArcaneTabletItem.getBankLevels(tablet) + cost);
                    } else {
                        player.addExperienceLevels(cost);
                    }
                    sendSyncPacket(player, playerData, tablet, "RITE CANCELLED: Target conditions not met. (XP Refunded)", 2);
                }
            });
        });
    }

    private static boolean executeCommandEffect(ArcaneAction action, ServerPlayerEntity player, ServerWorld world, PlayerArcaneData data) {
        switch (action) {
            // 1. Environmental
            case CALL_THE_DAWN -> {
                long day = world.getTimeOfDay() / 24000L;
                world.setTimeOfDay(day * 24000L + 1000L);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 1.2f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.END_ROD, 35);
                player.sendMessage(Text.literal("§e☀ [Call the Dawn] The celestial sun ascends into the morning sky.").formatted(Formatting.YELLOW), false);
                return true;
            }
            case PART_THE_STORM -> {
                world.setWeather(24000, 0, false, false); // 1 in-game day clear
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 1.1f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.TOTEM_OF_UNDYING, 40);
                player.sendMessage(Text.literal("§b☁ [Part the Storm] Storm clouds dissolve across the horizon for 1 day.").formatted(Formatting.AQUA), false);
                return true;
            }
            case GATHER_THE_GALE -> {
                world.setWeather(0, 24000, true, true);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 0.7f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.ELECTRIC_SPARK, 40);
                player.sendMessage(Text.literal("§9⚡ [Gather the Gale] An electric gale charges the atmosphere.").formatted(Formatting.BLUE), false);
                return true;
            }
            case LUNAR_HALT -> {
                long day = world.getTimeOfDay() / 24000L;
                world.setTimeOfDay(day * 24000L + 18000L);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 0.8f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.PORTAL, 35);
                player.sendMessage(Text.literal("§5🌙 [Lunar Halt] Midnight darkness blankets the world.").formatted(Formatting.DARK_PURPLE), false);
                return true;
            }

            // 2. Navigation
            case BEACON_TETHER -> {
                // Find linked lodestone compass in inventory
                BlockPos targetPos = null;
                ServerWorld targetWorld = world;

                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack st = player.getInventory().getStack(i);
                    if (st.isOf(Items.COMPASS) && st.contains(DataComponentTypes.LODESTONE_TRACKER)) {
                        LodestoneTrackerComponent tracker = st.get(DataComponentTypes.LODESTONE_TRACKER);
                        if (tracker != null && tracker.target().isPresent()) {
                            var globalPos = tracker.target().get();
                            targetPos = globalPos.pos();
                            for (ServerWorld sw : world.getServer().getWorlds()) {
                                if (sw.getRegistryKey().equals(globalPos.dimension())) {
                                    targetWorld = sw;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

                if (targetPos == null) {
                    player.sendMessage(Text.literal("§c[Beacon Tether] No Lodestone-linked Compass detected in inventory! Attune a compass to a Lodestone first.").formatted(Formatting.RED), false);
                    return false;
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_WARP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                player.teleport(targetWorld, targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, java.util.Collections.emptySet(), player.getYaw(), player.getPitch(), false);
                targetWorld.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_WARP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                spawnParticles(targetWorld, targetPos, ParticleTypes.PORTAL, 40);
                player.sendMessage(Text.literal("§6🌌 [Beacon Tether] Relocated to Lodestone Anchor @ [" + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ() + "]").formatted(Formatting.GOLD), false);
                return true;
            }
            case SUBSPACE_GATEWAY -> {
                TeleportTarget respawnTarget = player.getRespawnTarget(false, TeleportTarget.NO_OP);
                ServerWorld targetWorld;
                Vec3d targetPos;
                float yaw;
                float pitch;
                boolean isBed = false;

                if (respawnTarget != null) {
                    targetWorld = respawnTarget.world();
                    targetPos = respawnTarget.position();
                    yaw = respawnTarget.yaw();
                    pitch = respawnTarget.pitch();
                    isBed = !respawnTarget.missingRespawnBlock() && player.getRespawn() != null;
                } else {
                    targetWorld = world.getServer().getOverworld();
                    BlockPos sp = targetWorld.getSpawnPoint().getPos();
                    targetPos = new Vec3d(sp.getX() + 0.5, sp.getY() + 0.5, sp.getZ() + 0.5);
                    yaw = player.getYaw();
                    pitch = player.getPitch();
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_WARP, SoundCategory.PLAYERS, 1.0f, 1.1f);
                player.teleport(targetWorld, targetPos.getX(), targetPos.getY(), targetPos.getZ(), java.util.Collections.emptySet(), yaw, pitch, false);
                targetWorld.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_WARP, SoundCategory.PLAYERS, 1.0f, 1.1f);
                spawnParticles(targetWorld, BlockPos.ofFloored(targetPos), ParticleTypes.REVERSE_PORTAL, 50);

                String dest = isBed ? "Bed / Respawn Anchor" : "World Spawn";
                player.sendMessage(Text.literal("§d🌀 [Subspace Gateway] Recalled through cross-dimensional gateway to " + dest + " @ [" + (int) targetPos.getX() + ", " + (int) targetPos.getY() + ", " + (int) targetPos.getZ() + "]").formatted(Formatting.LIGHT_PURPLE), false);
                return true;
            }
            case LOCATE_VILLAGE -> {
                BlockPos foundPos = locateStructure(world, StructureTags.VILLAGE, player.getBlockPos());
                return handleLocate(world, player, data, "Village", foundPos);
            }
            case LOCATE_ANCIENT_CITY -> {
                BlockPos foundPos = locateStructureKey(world, StructureKeys.ANCIENT_CITY, player.getBlockPos());
                return handleLocate(world, player, data, "Ancient City", foundPos);
            }
            case LOCATE_TRIAL_CHAMBERS -> {
                BlockPos foundPos = locateStructureKey(world, StructureKeys.TRIAL_CHAMBERS, player.getBlockPos());
                return handleLocate(world, player, data, "Trial Chamber", foundPos);
            }
            case LOCATE_FORTRESS -> {
                BlockPos foundPos = locateStructureKey(world, StructureKeys.FORTRESS, player.getBlockPos());
                return handleLocate(world, player, data, "Nether Fortress", foundPos);
            }
            case LOCATE_STRONGHOLD -> {
                BlockPos foundPos = locateStructureKey(world, StructureKeys.STRONGHOLD, player.getBlockPos());
                return handleLocate(world, player, data, "Stronghold", foundPos);
            }
            case LOCATE_MINESHAFT -> {
                BlockPos foundPos = locateStructure(world, StructureTags.MINESHAFT, player.getBlockPos());
                return handleLocate(world, player, data, "Mineshaft", foundPos);
            }
            case LOCATE_MONUMENT -> {
                BlockPos foundPos = locateStructureKey(world, StructureKeys.MONUMENT, player.getBlockPos());
                return handleLocate(world, player, data, "Ocean Monument", foundPos);
            }
            case LOCATE_OUTPOST -> {
                BlockPos foundPos = locateStructureKey(world, StructureKeys.PILLAGER_OUTPOST, player.getBlockPos());
                return handleLocate(world, player, data, "Pillager Outpost", foundPos);
            }
            case LOCATE_END_CITY -> {
                BlockPos foundPos = locateStructureKey(world, StructureKeys.END_CITY, player.getBlockPos());
                return handleLocate(world, player, data, "End City", foundPos);
            }
            case WAYFARERS_SURVEY -> {
                // Find nearest rare biome
                Pair<BlockPos, RegistryEntry<Biome>> biomeResult = world.locateBiome(
                        entry -> entry.matchesKey(BiomeKeys.CHERRY_GROVE) || entry.matchesKey(BiomeKeys.MUSHROOM_FIELDS) || entry.matchesKey(BiomeKeys.BADLANDS) || entry.matchesKey(BiomeKeys.JUNGLE) || entry.matchesKey(BiomeKeys.DEEP_DARK),
                        player.getBlockPos(),
                        6400,
                        32,
                        64
                );
                if (biomeResult != null) {
                    BlockPos bPos = biomeResult.getFirst();
                    int dist = (int) Math.sqrt(player.getBlockPos().getSquaredDistance(bPos));
                    String bName = biomeResult.getSecond().getKey().map(k -> k.getValue().getPath()).orElse("Special Biome");
                    data.setScanResult(bName, bPos, dist);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_SCAN, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    player.sendMessage(Text.literal("§a🧭 [Wayfarer's Survey] " + bName.toUpperCase() + " located @ [" + bPos.getX() + ", " + bPos.getZ() + "] (" + dist + "m away)").formatted(Formatting.GREEN), false);
                    return true;
                }
                return false;
            }

            // 3. Sanctuary & Defense
            case TURN_UNDEAD -> {
                Box box = new Box(player.getBlockPos()).expand(32.0);
                List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, e -> e instanceof Monster || e instanceof HostileEntity);
                for (LivingEntity e : entities) {
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 1200, 0));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 1200, 3));
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 1.4f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.GLOW, 50);
                player.sendMessage(Text.literal("§a✨ [Turn Undead] " + entities.size() + " hostiles illuminated and suppressed for 60s.").formatted(Formatting.GREEN), false);
                return true;
            }
            case WARDING_WARD -> {
                Box box = new Box(player.getBlockPos()).expand(64.0);
                List<PhantomEntity> phantoms = world.getEntitiesByClass(PhantomEntity.class, box, e -> true);
                for (PhantomEntity phantom : phantoms) {
                    spawnParticles(world, phantom.getBlockPos(), ParticleTypes.SMOKE, 15);
                    phantom.discard();
                }
                player.getStatHandler().setStat(player, Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST), 0);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 1.2f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.NOTE, 30);
                player.sendMessage(Text.literal("§b🎵 [Warding Ward] Banished " + phantoms.size() + " phantoms. Rest restored for 3 in-game days.").formatted(Formatting.AQUA), false);
                return true;
            }
            case REPEL_INVADERS -> {
                boolean hadOmen = false;
                if (player.hasStatusEffect(StatusEffects.BAD_OMEN)) {
                    player.removeStatusEffect(StatusEffects.BAD_OMEN);
                    hadOmen = true;
                }
                if (player.hasStatusEffect(StatusEffects.RAID_OMEN)) {
                    player.removeStatusEffect(StatusEffects.RAID_OMEN);
                    hadOmen = true;
                }
                if (player.hasStatusEffect(StatusEffects.TRIAL_OMEN)) {
                    player.removeStatusEffect(StatusEffects.TRIAL_OMEN);
                    hadOmen = true;
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 1.3f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.HEART, 20);
                player.sendMessage(Text.literal("§a🛡 [Repel Invaders] All Raid & Bad Omen curses purged safely.").formatted(Formatting.GREEN), false);
                return true;
            }
            case PURGE_THE_FALLEN -> {
                Box box = new Box(player.getBlockPos()).expand(48.0);
                List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, e -> (e instanceof Monster || e instanceof HostileEntity) && !(e instanceof EnderDragonEntity) && !(e instanceof WitherEntity) && !(e instanceof ElderGuardianEntity));
                for (LivingEntity e : entities) {
                    spawnParticles(world, e.getBlockPos(), ParticleTypes.SOUL_FIRE_FLAME, 20);
                    e.discard(); // Zero drops, zero XP
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 0.6f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.EXPLOSION_EMITTER, 1);
                player.sendMessage(Text.literal("§5💥 [Purge the Fallen] " + entities.size() + " hostile anomalies atomized.").formatted(Formatting.DARK_PURPLE), false);
                return true;
            }

            // 4. Preservation
            case SPIRIT_TETHER -> {
                data.setSpiritTether(600000L); // 10 minutes (600,000 ms)
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 1.5f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.TOTEM_OF_UNDYING, 60);
                player.sendMessage(Text.literal("§d🔮 [Spirit Tether] Soul Anchor locked for 10 minutes: inventory will persist across death.").formatted(Formatting.LIGHT_PURPLE), false);
                return true;
            }
            case GRAVE_COMPASS -> {
                BlockPos dPos = data.getLastDeathPos();
                if (dPos == null) {
                    player.sendMessage(Text.literal("§c[Grave Compass] No death coordinates found in memory banks!").formatted(Formatting.RED), false);
                    return false;
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_SCAN, SoundCategory.PLAYERS, 1.0f, 1.0f);
                String dim = data.getLastDeathDimension().replace("minecraft:", "").toUpperCase();
                player.sendMessage(Text.literal("§d⚰ [Grave Compass] Last Death: [" + dPos.getX() + ", " + dPos.getY() + ", " + dPos.getZ() + "] in " + dim).formatted(Formatting.LIGHT_PURPLE), false);
                return true;
            }
            case STASIS_SHELL -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 900, 4, false, true, true)); // Resistance V (45s)
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 900, 4, false, true, true)); // Mining Fatigue V (45s)
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_EXECUTE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                spawnParticles(world, player.getBlockPos(), ParticleTypes.ENCHANTED_HIT, 50);
                player.sendMessage(Text.literal("§b🛡 [Stasis Shell] Invulnerability barrier active for 45s (Resistance V + Mining Fatigue V).").formatted(Formatting.AQUA), false);
                return true;
            }
            case RESTORE_ANVIL -> {
                BlockPos playerPos = player.getBlockPos();
                BlockPos foundAnvil = null;
                BlockState anvilState = null;

                for (BlockPos pos : BlockPos.iterate(playerPos.add(-5, -3, -5), playerPos.add(5, 3, 5))) {
                    BlockState st = world.getBlockState(pos);
                    if (st.isOf(Blocks.CHIPPED_ANVIL) || st.isOf(Blocks.DAMAGED_ANVIL)) {
                        foundAnvil = pos.toImmutable();
                        anvilState = st;
                        break;
                    }
                }

                if (foundAnvil == null) {
                    player.sendMessage(Text.literal("§c[Restore Anvil] No chipped or damaged anvil detected within 5 blocks!").formatted(Formatting.RED), false);
                    return false;
                }

                // Restore anvil preserving horizontal facing
                BlockState pristine = Blocks.ANVIL.getDefaultState().with(AnvilBlock.FACING, anvilState.get(AnvilBlock.FACING));
                world.setBlockState(foundAnvil, pristine);
                world.playSound(null, foundAnvil.getX(), foundAnvil.getY(), foundAnvil.getZ(), net.minecraft.sound.SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                spawnParticles(world, foundAnvil, ParticleTypes.WAX_ON, 25);
                player.sendMessage(Text.literal("§6🔨 [Restore Anvil] Damaged anvil successfully reforged to pristine condition!").formatted(Formatting.GOLD), false);
                return true;
            }
        }
        return false;
    }

    private static boolean handleLocate(ServerWorld world, ServerPlayerEntity player, PlayerArcaneData data, String displayName, BlockPos foundPos) {
        if (foundPos != null) {
            int dist = (int) Math.sqrt(player.getBlockPos().getSquaredDistance(foundPos));
            data.setScanResult(displayName, foundPos, dist);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_SCAN, SoundCategory.PLAYERS, 1.0f, 1.0f);
            spawnParticles(world, player.getBlockPos(), ParticleTypes.SONIC_BOOM, 1);
            player.sendMessage(Text.literal("§e📡 [" + displayName + "] Located @ [" + foundPos.getX() + ", " + foundPos.getY() + ", " + foundPos.getZ() + "] (" + dist + "m away)").formatted(Formatting.YELLOW), false);
            return true;
        } else {
            player.sendMessage(Text.literal("§c📡 [" + displayName + "] No structure located within search radius in this dimension!").formatted(Formatting.RED), false);
            return false;
        }
    }

    private static BlockPos locateStructure(ServerWorld world, net.minecraft.registry.tag.TagKey<Structure> tagKey, BlockPos center) {
        Registry<Structure> registry = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
        RegistryEntryList<Structure> list = registry.getOptional(tagKey).orElse(null);
        if (list == null) return null;
        Pair<BlockPos, RegistryEntry<Structure>> res = world.getChunkManager().getChunkGenerator().locateStructure(world, list, center, 100, false);
        return res != null ? res.getFirst() : null;
    }

    private static BlockPos locateStructureKey(ServerWorld world, net.minecraft.registry.RegistryKey<Structure> key, BlockPos center) {
        Registry<Structure> registry = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
        Optional<RegistryEntry.Reference<Structure>> entry = registry.getOptional(key);
        if (entry.isEmpty()) return null;
        Pair<BlockPos, RegistryEntry<Structure>> res = world.getChunkManager().getChunkGenerator().locateStructure(world, RegistryEntryList.of(entry.get()), center, 100, false);
        return res != null ? res.getFirst() : null;
    }

    private static void spawnParticles(ServerWorld world, BlockPos pos, net.minecraft.particle.ParticleEffect particle, int count) {
        world.spawnParticles(particle, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, count, 0.5, 0.5, 0.5, 0.05);
    }

    private static void playBankSound(ServerPlayerEntity player) {
        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TABLET_BANK, SoundCategory.PLAYERS, 0.7f, 1.2f);
    }

    public static ItemStack findTablet(ServerPlayerEntity player) {
        if (player.getMainHandStack().isOf(ModItems.ARCANE_TABLET)) return player.getMainHandStack();
        if (player.getOffHandStack().isOf(ModItems.ARCANE_TABLET)) return player.getOffHandStack();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(ModItems.ARCANE_TABLET)) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static void sendSyncPacket(ServerPlayerEntity player, PlayerArcaneData data, ItemStack tablet, String statusMessage, int statusCode) {
        List<String> unlocks = new ArrayList<>();
        for (ArcaneAction action : ArcaneAction.values()) {
            if (action.isUnlockedFor(player, data)) {
                unlocks.add(action.getId());
            }
        }

        BlockPos dPos = data.getLastDeathPos() != null ? data.getLastDeathPos() : BlockPos.ORIGIN;
        String dDim = data.getLastDeathDimension() != null ? data.getLastDeathDimension() : "";
        BlockPos sPos = data.getLastScannedPos() != null ? data.getLastScannedPos() : BlockPos.ORIGIN;
        String sStruct = data.getLastScannedStructure() != null ? data.getLastScannedStructure() : "";
        int sDist = data.getLastScannedDistance();
        int bank = tablet.isEmpty() ? 0 : ArcaneTabletItem.getBankLevels(tablet);
        long tSec = data.isSpiritTetherActive() ? (data.getSpiritTetherExpiry() - System.currentTimeMillis()) / 1000L : 0;
        long cdSec = data.getRemainingCooldownSeconds();
        boolean isOp = player.getEntityWorld().getServer() != null && player.getEntityWorld().getServer().getPlayerManager().isOperator(new net.minecraft.server.PlayerConfigEntry(player.getGameProfile()));

        SyncPlayerDataPayload payload = new SyncPlayerDataPayload(
                unlocks,
                dPos.getX(), dPos.getY(), dPos.getZ(), dDim,
                sStruct, sPos.getX(), sPos.getY(), sPos.getZ(), sDist,
                bank, tSec, cdSec, isOp, statusMessage, statusCode
        );

        ServerPlayNetworking.send(player, payload);
    }
}
