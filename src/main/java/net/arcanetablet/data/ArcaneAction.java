package net.arcanetablet.data;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public enum ArcaneAction {
    // 1. Environmental Control (Weather & Time)
    CALL_THE_DAWN(
            "call_the_dawn",
            ArcaneCategory.ENVIRONMENT,
            "Call the Dawn",
            "Dispel the darkness and align the sun to morning daylight (/time set day).",
            7,
            "Sweet Dreams",
            "minecraft:adventure/sleep_in_bed"
    ),
    PART_THE_STORM(
            "part_the_storm",
            ArcaneCategory.ENVIRONMENT,
            "Part the Storm",
            "Dissipate clouds and clear rain/snow for 1 full in-game day (/weather clear).",
            5,
            "A Seedy Place",
            "minecraft:husbandry/plant_seed"
    ),
    GATHER_THE_GALE(
            "gather_the_gale",
            ArcaneCategory.ENVIRONMENT,
            "Gather the Gale",
            "Summon an atmospheric surge of lightning and thunder (/weather thunder).",
            15,
            "Surge Protector",
            "minecraft:adventure/lightning_rod_with_villager_no_fire"
    ),
    LUNAR_HALT(
            "lunar_halt",
            ArcaneCategory.ENVIRONMENT,
            "Lunar Halt",
            "Advance the world into full midnight darkness for mob and phantom hunting.",
            12,
            "Two Birds, One Arrow",
            "minecraft:adventure/two_birds_one_arrow"
    ),

    // 2. Navigation & Exploration
    BEACON_TETHER(
            "beacon_tether",
            ArcaneCategory.NAVIGATION,
            "Beacon Tether",
            "Quantum-warp directly to your attuned Lodestone beacon compass.",
            15,
            "Country Lode, Take Me Home",
            "minecraft:nether/use_lodestone"
    ),
    SUBSPACE_GATEWAY(
            "subspace_gateway",
            ArcaneCategory.NAVIGATION,
            "Subspace Gateway",
            "Cross-dimensional recall back to your active Bed / Respawn Anchor or Spawn.",
            25,
            "Subspace Bubble",
            "minecraft:nether/fast_travel"
    ),
    LOCATE_VILLAGE(
            "locate_village",
            ArcaneCategory.NAVIGATION,
            "Village",
            "Pings seismic frequencies to find the nearest civilization settlement.",
            10,
            "Monster Hunter",
            "minecraft:adventure/kill_a_mob"
    ),
    LOCATE_ANCIENT_CITY(
            "locate_ancient_city",
            ArcaneCategory.NAVIGATION,
            "Ancient City",
            "Detects deep subterranean sculk resonant acoustic chambers.",
            22,
            "Sneak 100",
            "minecraft:adventure/sneak_past_sculk_sensor"
    ),
    LOCATE_TRIAL_CHAMBERS(
            "locate_trial_chambers",
            ArcaneCategory.NAVIGATION,
            "Trial Chamber",
            "Detects copper alloy vaults and breeze combat spawner chambers.",
            20,
            "Acquire Hardware",
            "minecraft:story/smelt_iron"
    ),
    LOCATE_FORTRESS(
            "locate_fortress",
            ArcaneCategory.NAVIGATION,
            "Nether Fortress",
            "Locks onto blazeborn bridge megaliths across the Nether.",
            15,
            "A Terrible Fortress",
            "minecraft:nether/find_fortress"
    ),
    LOCATE_STRONGHOLD(
            "locate_stronghold",
            ArcaneCategory.NAVIGATION,
            "Stronghold",
            "Scans for the subterranean resonant Ender portal frame matrix.",
            25,
            "Eye Spy",
            "minecraft:story/enter_the_stronghold"
    ),
    LOCATE_MINESHAFT(
            "locate_mineshaft",
            ArcaneCategory.NAVIGATION,
            "Mineshaft",
            "Scans underground timber supports and subterranean rail lines.",
            8,
            "Isn't It Iron Pick",
            "minecraft:story/iron_tools"
    ),
    LOCATE_MONUMENT(
            "locate_monument",
            ArcaneCategory.NAVIGATION,
            "Ocean Monument",
            "Pings ocean depths for prismarine guardian megaliths.",
            12,
            "Subspace Bubble",
            "minecraft:nether/fast_travel"
    ),
    LOCATE_OUTPOST(
            "locate_outpost",
            ArcaneCategory.NAVIGATION,
            "Pillager Outpost",
            "Scans overland terrain for hostile illager watchtowers.",
            10,
            "Voluntary Exile",
            "minecraft:adventure/voluntary_exile"
    ),
    LOCATE_END_CITY(
            "locate_end_city",
            ArcaneCategory.NAVIGATION,
            "End City",
            "Scans outer void islands for purpur towers and flying ships.",
            25,
            "The City at the End",
            "minecraft:end/find_end_city"
    ),
    WAYFARERS_SURVEY(
            "wayfarers_survey",
            ArcaneCategory.NAVIGATION,
            "Wayfarer's Survey",
            "Scans the biome spectrum to detect rare biomes in the current dimension.",
            10,
            "Adventuring Time",
            "minecraft:adventure/adventuring_time"
    ),

    // 3. Sanctuary & Defense (Mob & Entity Utility)
    TURN_UNDEAD(
            "turn_undead",
            ArcaneCategory.SANCTUARY,
            "Turn Undead",
            "Highlights (Glowing) and slows all hostiles within 32 blocks for 60 seconds.",
            8,
            "Monster Hunter",
            "minecraft:adventure/kill_a_mob"
    ),
    WARDING_WARD(
            "warding_ward",
            ArcaneCategory.SANCTUARY,
            "Warding Ward",
            "Banishes all Phantoms within 64 blocks and resets phantom insomnia for 3 days.",
            10,
            "Sound of Music",
            "minecraft:adventure/play_jukebox_in_meadows"
    ),
    REPEL_INVADERS(
            "repel_invaders",
            ArcaneCategory.SANCTUARY,
            "Repel Invaders",
            "Purges Raid Omen and Bad Omen without triggering a raid or drinking milk.",
            14,
            "Hero of the Village",
            "minecraft:adventure/hero_of_the_village"
    ),
    PURGE_THE_FALLEN(
            "purge_the_fallen",
            ArcaneCategory.SANCTUARY,
            "Purge the Fallen",
            "Vaporizes all hostile monsters in a 48-block sphere (no loot or XP dropped).",
            30,
            "Free the End",
            "minecraft:end/kill_dragon"
    ),

    // 4. Preservation & Recovery (High-Stakes Magic)
    SPIRIT_TETHER(
            "spirit_tether",
            ArcaneCategory.PRESERVATION,
            "Spirit Tether",
            "Grants a 10-minute Soul Anchor: preserves full inventory upon your next death.",
            35,
            "Postmortal",
            "minecraft:adventure/totem_of_undying"
    ),
    GRAVE_COMPASS(
            "grave_compass",
            ArcaneCategory.PRESERVATION,
            "Grave Compass",
            "Pings memory banks to output the exact coordinates of your last recorded death.",
            6,
            "Not Today, Thank You",
            "minecraft:adventure/shield_block"
    ),
    STASIS_SHELL(
            "stasis_shell",
            ArcaneCategory.PRESERVATION,
            "Stasis Shell",
            "Envelops player in Resistance V & Mining Fatigue V for 45s (AFK / emergency stasis).",
            20,
            "Beaconator",
            "minecraft:nether/create_full_beacon"
    ),
    RESTORE_ANVIL(
            "restore_anvil",
            ArcaneCategory.PRESERVATION,
            "Restore Anvil",
            "Repairs a damaged or chipped anvil within 5 blocks back to pristine condition.",
            15,
            "Acquire Hardware",
            "minecraft:story/smelt_iron"
    );

    private final String id;
    private final ArcaneCategory category;
    private final String title;
    private final String description;
    private final int levelCost;
    private final String advancementName;
    private final String advancementId;

    ArcaneAction(String id, ArcaneCategory category, String title, String description, int levelCost, String advancementName, String advancementId) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.description = description;
        this.levelCost = levelCost;
        this.advancementName = advancementName;
        this.advancementId = advancementId;
    }

    public String getId() {
        return id;
    }

    public ArcaneCategory getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getLevelCost() {
        return levelCost;
    }

    public String getAdvancementName() {
        return advancementName;
    }

    public String getAdvancementId() {
        return advancementId;
    }

    public static ArcaneAction byId(String id) {
        for (ArcaneAction action : values()) {
            if (action.id.equalsIgnoreCase(id)) {
                return action;
            }
        }
        return null;
    }

    public boolean isUnlockedFor(ServerPlayerEntity player, PlayerArcaneData data) {
        if (data != null && data.hasExplicitUnlock(this.id)) {
            return true;
        }

        // 1. Check vanilla Advancement
        if (advancementId != null && !advancementId.isEmpty() && player.getEntityWorld() instanceof ServerWorld sw) {
            AdvancementEntry entry = sw.getServer().getAdvancementLoader().get(Identifier.of(advancementId));
            if (entry != null && player.getAdvancementTracker().getProgress(entry).isDone()) {
                return true;
            }
        }

        // 2. Fallback vanilla gameplay stat/inventory verification
        return switch (this) {
            case CALL_THE_DAWN -> player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.SLEEP_IN_BED)) > 0 || player.getInventory().contains(Items.CLOCK.getDefaultStack());
            case PART_THE_STORM -> player.getInventory().contains(Items.WHEAT_SEEDS.getDefaultStack()) || player.getInventory().contains(Items.WATER_BUCKET.getDefaultStack());
            case GATHER_THE_GALE -> player.getInventory().contains(Items.LIGHTNING_ROD.getDefaultStack()) || player.getInventory().contains(Items.TRIDENT.getDefaultStack());
            case LUNAR_HALT -> player.getStatHandler().getStat(Stats.KILLED.getOrCreateStat(net.minecraft.entity.EntityType.PHANTOM)) > 0 || player.getInventory().contains(Items.PHANTOM_MEMBRANE.getDefaultStack());
            case BEACON_TETHER -> player.getInventory().contains(Items.LODESTONE.getDefaultStack()) || player.getInventory().contains(Items.COMPASS.getDefaultStack());
            case SUBSPACE_GATEWAY -> player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM)) > 50000;
            case LOCATE_VILLAGE -> player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.TALKED_TO_VILLAGER)) > 0 || player.getInventory().contains(Items.EMERALD.getDefaultStack());
            case LOCATE_ANCIENT_CITY -> player.getInventory().contains(Items.ECHO_SHARD.getDefaultStack()) || player.getInventory().contains(Items.SCULK.getDefaultStack()) || player.getInventory().contains(Items.AMETHYST_SHARD.getDefaultStack());
            case LOCATE_TRIAL_CHAMBERS -> player.getInventory().contains(Items.TRIAL_KEY.getDefaultStack()) || player.getInventory().contains(Items.COPPER_INGOT.getDefaultStack()) || player.getInventory().contains(Items.VAULT.getDefaultStack());
            case LOCATE_FORTRESS -> player.getInventory().contains(Items.BLAZE_POWDER.getDefaultStack()) || player.getInventory().contains(Items.BLAZE_ROD.getDefaultStack()) || player.getInventory().contains(Items.NETHERRACK.getDefaultStack());
            case LOCATE_STRONGHOLD -> player.getInventory().contains(Items.ENDER_EYE.getDefaultStack()) || player.getInventory().contains(Items.ENDER_PEARL.getDefaultStack());
            case LOCATE_MINESHAFT -> player.getStatHandler().getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.STONE)) > 20 || player.getInventory().contains(Items.RAIL.getDefaultStack());
            case LOCATE_MONUMENT -> player.getInventory().contains(Items.PRISMARINE_SHARD.getDefaultStack()) || player.getInventory().contains(Items.PRISMARINE.getDefaultStack()) || player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.SWIM_ONE_CM)) > 10000;
            case LOCATE_OUTPOST -> player.getInventory().contains(Items.CROSSBOW.getDefaultStack()) || player.getInventory().contains(Items.OMINOUS_BOTTLE.getDefaultStack()) || player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.RAID_TRIGGER)) > 0;
            case LOCATE_END_CITY -> player.getInventory().contains(Items.PURPUR_BLOCK.getDefaultStack()) || player.getInventory().contains(Items.CHORUS_FRUIT.getDefaultStack()) || player.getInventory().contains(Items.SHULKER_SHELL.getDefaultStack());
            case WAYFARERS_SURVEY -> player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM)) > 10000 || player.getInventory().contains(Items.MAP.getDefaultStack()) || player.getInventory().contains(Items.FILLED_MAP.getDefaultStack());
            case TURN_UNDEAD -> player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS)) > 0;
            case WARDING_WARD -> player.getInventory().contains(Items.JUKEBOX.getDefaultStack()) || player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_RECORD)) > 0;
            case REPEL_INVADERS -> player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.RAID_WIN)) > 0 || player.getInventory().contains(Items.OMINOUS_BOTTLE.getDefaultStack());
            case PURGE_THE_FALLEN -> player.getInventory().contains(Items.DRAGON_EGG.getDefaultStack()) || player.getInventory().contains(Items.DRAGON_BREATH.getDefaultStack());
            case SPIRIT_TETHER -> player.getInventory().contains(Items.TOTEM_OF_UNDYING.getDefaultStack());
            case GRAVE_COMPASS -> data != null && data.getLastDeathPos() != null;
            case STASIS_SHELL -> player.getInventory().contains(Items.BEACON.getDefaultStack()) || player.getInventory().contains(Items.NETHER_STAR.getDefaultStack());
            case RESTORE_ANVIL -> player.getInventory().contains(Items.IRON_INGOT.getDefaultStack()) || player.getInventory().contains(Items.ANVIL.getDefaultStack());
        };
    }
}
