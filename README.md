# ⚡ Arcane Tablet (Quantum Command Matrix) — NeoForge Edition

[![Minecraft 1.21.4 / 1.21.11](https://img.shields.io/badge/Minecraft-1.21.4%20%2F%201.21.11-brightgreen.svg)](https://neoforged.net/)
[![NeoForge Loader](https://img.shields.io/badge/NeoForge-21.11.45%2B-orange.svg)](https://neoforged.net/)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A standalone **NeoForge** utility & magic mod for **Minecraft 1.21.4 / 1.21.11+**.

The **Arcane Tablet** enables pure survival players to execute balanced, lore-friendly world manipulation rites **without enabling cheats or operator permissions**. Every command is strictly gated by **Experience Levels (XP)** and unlocked through **Vanilla Advancement Progression**.

---

## 🌐 Other Loader Versions
- **Fabric Edition (1.21.4 / 1.21.11):** Available on branch [`main`](https://github.com/shufunk-dev/arcanetablet/tree/main)
- **Forge Edition (1.21.4 / 1.21.11):** Available on branch [`forge-1.21.4`](https://github.com/shufunk-dev/arcanetablet/tree/forge-1.21.4)

---

## 🌟 Key Features

- **🛡️ 100% Legit Survival Compatibility:** Works out of the box in worlds with cheats/OP disabled.
- **⚡ Dual Energy Core:** Cast directly using your character's XP or store surplus levels in the Tablet's internal XP bank.
- **🧭 Discrete Structure Radar:** Search and pinpoint coordinates and distances for Villages, Ancient Cities, Trial Chambers, Nether Fortresses, Strongholds, Mineshafts, Ocean Monuments, Outposts, and End Cities.
- **🌀 Cross-Dimensional Recall:** Return directly to your active Bed or Respawn Anchor across dimensions with Subspace Gateway.
- **🌌 Lodestone Waypoint Teleportation:** Quantum-warp to any attuned Lodestone Compass across vanilla and modded dimensions.
- **🔮 Soul Anchor (Death Insurance):** Activate Spirit Tether for a 10-minute temporary keep-inventory buff.
- **🖥️ Futuristic Hologram HUD:** Neon cyan sci-fi interface with live telemetry, dimension tracking, pagination (`[◀] [▶]`), and status feedback.

---

## ⚡ Command Rites & Categories

### 1. ☁️ Environmental Control (Weather & Time)
| Rite | Effect | Cost | Unlocked By Advancement / Fallback | Restriction |
| :--- | :--- | :---: | :--- | :--- |
| **Call the Dawn** | `/time set day` | **7 LVL** | *Sweet Dreams* (`adventure/sleep_in_bed`) | Usable only during night or thunderstorms. |
| **Part the Storm** | `/weather clear` | **5 LVL** | *A Seedy Place* (`husbandry/plant_seed`) | Clears rain/snow for 1 full in-game day (24,000 ticks). |
| **Gather the Gale** | `/weather thunder` | **15 LVL** | *Surge Protector* (`adventure/lightning_rod_with_villager_no_fire`) | Charges the atmosphere with thunder and lightning. |
| **Lunar Halt** | Set time to midnight | **12 LVL** | *Two Birds, One Arrow* (`adventure/two_birds_one_arrow`) | Advances celestial clock to full midnight (18,000 ticks). |

---

### 2. 🧭 Navigation & Exploration (Paging `[◀] [▶]`)
| Rite | Effect | Cost | Unlocked By Advancement / Fallback |
| :--- | :--- | :---: | :--- |
| **Beacon Tether** | Warp to Lodestone Compass | **15 LVL** | *Country Lode, Take Me Home* (`nether/use_lodestone`) |
| **Subspace Gateway** | Recall to Bed / Respawn Anchor | **25 LVL** | *Subspace Bubble* (`nether/fast_travel`) |
| **Village** | Seismic scan for nearest village | **10 LVL** | *Monster Hunter* (`adventure/kill_a_mob`) |
| **Mineshaft** | Locate subterranean timber lines | **8 LVL** | *Isn't It Iron Pick* (`story/iron_tools`) |
| **Pillager Outpost** | Detect illager watchtowers | **10 LVL** | *Voluntary Exile* (`adventure/voluntary_exile`) |
| **Ocean Monument** | Ping ocean guardian megaliths | **12 LVL** | *Subspace Bubble* (`nether/fast_travel`) |
| **Nether Fortress** | Lock onto Nether bridge structures | **15 LVL** | *A Terrible Fortress* (`nether/find_fortress`) |
| **Trial Chamber** | Detect copper vaults & spawners | **20 LVL** | *Acquire Hardware* (`story/smelt_iron`) |
| **Ancient City** | Subterranean sculk acoustic scan | **22 LVL** | *Sneak 100* (`adventure/sneak_past_sculk_sensor`) |
| **Stronghold** | Scan for Ender portal frame | **25 LVL** | *Eye Spy* (`story/enter_the_stronghold`) |
| **End City** | Scan void islands for purpur towers | **25 LVL** | *The City at the End* (`end/find_end_city`) |
| **Amethyst Geode** | Scans strata for budding amethyst crystals | **12 LVL** | *Stone Age* (`story/mine_stone`) / Unlocked |
| **Wayfarer's Survey** | Biome scanner (Cherry, Jungle, etc.) | **10 LVL** | *Adventuring Time* (`adventure/adventuring_time`) |

---

### 3. 🛡️ Sanctuary & Defense (Mob Control)
| Rite | Effect | Cost | Unlocked By Advancement / Fallback |
| :--- | :--- | :---: | :--- |
| **Turn Undead** | Glowing + Slowness IV for 60s | **8 LVL** | *Monster Hunter* (`adventure/kill_a_mob`) |
| **Warding Ward** | Purge Phantoms + reset insomnia 3 days | **10 LVL** | *Sound of Music* (`adventure/play_jukebox_in_meadows`) |
| **Repel Invaders** | Cleanse Raid Omen & Bad Omen | **14 LVL** | *Hero of the Village* (`adventure/hero_of_the_village`) |
| **Purge the Fallen** | Despawn hostiles in 48-block sphere | **30 LVL** | *Free the End* (`end/kill_dragon`) (0 loot, 0 XP) |

---

### 4. 🔮 Preservation & Recovery
| Rite | Effect | Cost | Unlocked By Advancement / Fallback |
| :--- | :--- | :---: | :--- |
| **Spirit Tether** | 10-minute Soul Anchor (Keep-Inventory) | **35 LVL** | *Postmortal* (`adventure/totem_of_undying`) |
| **Grave Compass** | Locates death coords & dimension | **6 LVL** | *Not Today, Thank You* (`adventure/shield_block`) |
| **Stasis Shell** | Resistance V + Mining Fatigue V (45s) | **20 LVL** | *Beaconator* (`nether/create_full_beacon`) |
| **Restore Anvil** | Repairs damaged anvil to pristine | **15 LVL** | *Acquire Hardware* (`story/smelt_iron`) |

---

## 🛠️ Crafting Recipe

Crafted at a standard Crafting Table:

```text
[ Gold Ingot ]   [ Amethyst Shard ]  [ Gold Ingot ]
[  Redstone  ]   [    Compass     ]  [  Redstone  ]
[ Gold Ingot ]   [    Obsidian    ]  [ Gold Ingot ]
```

---

## 📥 Installation (NeoForge)

1. Install **NeoForge** (21.11.45 or newer) for **Minecraft 1.21.4 / 1.21.11**.
2. Download **Arcane Tablet (NeoForge)** (`arcanetablet-neoforge-1.21.11-1.0.0.jar`) and place it into your `.minecraft/mods` folder.
3. *No additional library or API dependencies required!*
4. Launch the game, craft your tablet, and right-click to open the matrix!

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
