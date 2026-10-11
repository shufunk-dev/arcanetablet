# 📜 Arcane Tablet — Changelog

## [1.0.2] — Minecraft 26.3 Release

### 🚀 Highlights & Platform Support
- **Full Port to Minecraft 26.3:** Native compatibility implemented across all 3 major mod loaders:
  - **Fabric:** Loader `0.19.5` + Fabric API `0.162.0+26.3`
  - **Forge:** `66.0.10`
  - **NeoForge:** `26.3.0.69-beta`
- **Java 25 Runtime:** Configured toolchains and build wrappers to target **Java 25** (`JavaLanguageVersion.of(25)`).
- **Native Mojang Deobfuscation:** Adapted all network payloads, screens, mixins, and entity events to Mojang's native 26.3 signatures (`ServerPlayer`, `ServerLevel`, `GuiGraphicsExtractor`, `CustomData`, `FabricCreativeModeTab`).

---

### ✨ Features & Balance Updates
- **💎 Multi-Gem Tablet Crafting:**
  - Expanded the top-middle slot recipe requirement: now crafts using either an **Amethyst Shard**, **Lapis Lazuli**, **Diamond**, or **Emerald**.
- **🏕️ Abandoned Camp Radar Rite (26.3 Exclusive):**
  - Added a dedicated radar scan in Navigation targeting all 18 biome variants of the new 26.3 **Abandoned Camp** structure.
- **🔮 Amethyst Geode Radar Rite:**
  - Added subterranean acoustic radar to the Navigation category that scans chunks within an 18-chunk radius for `budding_amethyst` / `amethyst_block`.
  - Outputs live X/Y/Z coordinates and distance directly into chat and stores targets in the HUD telemetry screen.
- **⚡ Dual Energy Core & XP Banking:**
  - Deposit, withdraw (+1, +5, ALL), or drain stored XP levels directly inside the tablet.
  - Automatically draws from the tablet's energy bank or player level when casting rites.
- **🌀 Subspace Gateway & Beacon Tether:**
  - Teleport across dimensions to your active Bed / Respawn Anchor.
  - Quantum-warp to any Lodestone-attuned compass held in your inventory.
- **🛡️ Spirit Tether (Death Insurance):**
  - High-stakes ritual providing a 10-minute temporary soul anchor that preserves your entire inventory across death.
- **🧭 Holographic UI & Live Telemetry:**
  - Neon sci-fi matrix UI featuring category tabs, paged action cards, live bank display, radar readout, and world cheat/survival legitimacy detection.

---

### 📦 Artifact Distribution
- `arcanetablet-fabric-26.3-1.0.2.jar`
- `arcanetablet-forge-26.3-1.0.2.jar`
- `arcanetablet-neoforge-26.3-1.0.2.jar`
