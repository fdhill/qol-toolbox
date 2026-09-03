# QoL Toolbox

Fabric mod untuk Minecraft 1.21.1 — kumpulan fitur Quality of Life dalam satu jar. Dirancang ringan, modular, dan bisa di-toggle per fitur.

## Fitur

| Fitur | Tipe | Hotkey | Keterangan |
|---|---|---|---|
| **Vein Miner** | Server + Client | Shift + Break | Hancurkan satu blok ore → blok sejenis terhubung ikut hancur (BFS) |
| **Death Marker** | Client | `J` | Beam merah + HUD koordinat di lokasi kematian terakhir |
| **Fullbright** | Client | `B` | Lihat terang di malam hari tanpa potion |
| **Mini Recipe Viewer** | Client | `R` | Cari & lihat resep crafting — dekat crafting table (radius 4 blok) |
| **Arrow Trajectory** | Client | — | Garis alur panah saat menarik busur/crossbow |
| **Dynamic Lights** | Client | — | Senter/lantern yang dipegang memberikan cahaya dinamis |

## Screenshots

<!-- TODO: tambah screenshot di sini -->

## Persiapan

- **JDK 21+** (JDK 23 sudah terinstall, kompatibel)
- **Internet** — Gradle akan download dependency otomatis

## Build

```bash
./gradlew build
```

Output: `build/libs/qoltoolbox-1.0.0.jar`

## Install

1. Install [Fabric Loader](https://fabricmc.net/) untuk Minecraft 1.21.1
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Copy `qoltoolbox-1.0.0.jar` ke folder `mods/` (sama untuk client & server)
4. Jalankan Minecraft via Fabric

## Commands (server-side)

```
/qol veinminer toggle              — on/off Vein Miner
/qol veinminer add <block>         — tambah blok ke whitelist
/qol veinminer remove <block>      — hapus blok dari whitelist
/qol veinminer list                — lihat semua blok di whitelist
/qol veinminer max <1-128>         — set jumlah maks blok per vein
```

## Configuration

File: `config/qoltoolbox.json`

```json
{
  "fullbright": { "enabled": false },
  "trajectory": { "enabled": true },
  "deathmarker": { "enabled": true, "x": null, "y": null, "z": null, "dimension": null },
  "recipeviewer": { "enabled": true },
  "dynamiclights": { "enabled": true },
  "veinminer": {
    "enabled": true,
    "maxBlocks": 32,
    "whitelist": [
      "minecraft:iron_ore",
      "minecraft:deepslate_iron_ore",
      "minecraft:gold_ore",
      "minecraft:deepslate_gold_ore",
      "minecraft:diamond_ore",
      "minecraft:deepslate_diamond_ore"
    ]
  }
}
```

Fitur yang nonaktif (`enabled: false`) tidak register event/render apa pun — efeknya identik dengan tidak dipasang.

## Project Structure

```
src/main/java/com/fdhill/qoltoolbox/
├── QolMod.java                 # Entry point (common/server)
├── QolClient.java              # Entry point (client)
├── config/
│   └── QolConfig.java          # Gson JSON config loader/saver
├── veinminer/
│   └── VeinMiner.java          # BFS logika + command Brigadier
├── deathmarker/
│   └── DeathMarker.java        # Beam + HUD rendering
├── fullbright/
│   └── Fullbright.java         # Gamma/brightness bypass
├── recipeviewer/
│   └── RecipeViewerScreen.java # Custom screen search + grid resep
├── trajectory/
│   └── TrajectoryRenderer.java # Simulasi fisika + render garis
├── dynamiclights/
│   └── DynamicLights.java      # Cahaya dinamis dari item yang dipegang
├── settings/
│   ├── SettingsScreen.java     # Menu utama settings
│   ├── FullbrightSettingsScreen.java
│   ├── VeinMinerSettingsScreen.java
│   ├── DeathMarkerSettingsScreen.java
│   ├── RecipeViewerSettingsScreen.java
│   ├── TrajectorySettingsScreen.java
│   └── DynamicLightsSettingsScreen.java
└── mixin/
    ├── BlockBreakMixin.java    # Hook block break untuk Vein Miner
    └── SimpleOptionAccessor.java # Akses gamma untuk Fullbright

src/main/resources/
├── fabric.mod.json             # Mod metadata + entrypoints
├── qoltoolbox.mixins.json      # Mixin config
└── assets/qoltoolbox/
    ├── lang/en_us.json         # English
    ├── lang/id_id.json         # Indonesia
    └── textures/death_skull.png
```

## Tech Stack

| Komponen | Versi |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.115.3+1.21.1 |
| Yarn Mappings | 1.21.1+build.3 |
| Java Target | 21 |
| Build System | Gradle + Fabric Loom 1.17.19 |

## Catatan

- **Vein Miner** perlu mod terinstall di sisi **server** agar jalan di multiplayer. Singleplayer otomatis OK.
- Fortune/Silk Touch bekerja pada blok pertama (yang di-break vanilla); blok tambahan menggunakan loot table vanilla standar.
- **Dynamic Lights** hanya menempatkan blok cahaya di posisi pemain — hanya berfungsi di area yang bisa dimodifikasi (bukan di area adventure mode).

## License

MIT
