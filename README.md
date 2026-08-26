# QoL Toolbox

Fabric mod (Minecraft 1.21.1) — lima fitur Quality of Life dalam satu jar.

## Fitur

| Fitur | Tipe | Hotkey | Keterangan |
|---|---|---|---|
| Vein Miner | Server+Client | Shift+Break | Hancurkan satu blok ore → blok sejenis terhubung ikut hancur |
| Death Marker | Client | `J` | Beam merah + HUD koordinat di lokasi kematian |
| Fullbright | Client | `B` | Lihat terang di malam tanpa potion |
| Recipe Viewer | Client | `R` | Resep crafting — dekat crafting table (radius 4) |
| Arrow Trajectory | Client | — | Garis alur panah saat menarik busur |

## Build

```bash
./gradlew build
```

Jar output: `build/libs/qoltoolbox-1.0.0.jar`

**Prasyarat:** JDK 21+, internet (Gradle download dependency otomatis).

## Install

1. Install [Fabric Loader](https://fabricmc.net/) untuk Minecraft 1.21.1
2. Copy `qoltoolbox-1.0.0.jar` ke folder `mods/`
3. Jalankan Minecraft via Fabric

## Commands (server-side)

```
/qol veinminer toggle              — on/off
/qol veinminer add <block>         — tambah ke whitelist
/qol veinminer remove <block>      — hapus dari whitelist
/qol veinminer list                — lihat whitelist
/qol veinminer max <1-128>         — set max blok per vein
```

## Config

File: `config/qoltoolbox.json`

```json
{
  "fullbright": { "enabled": false },
  "trajectory": { "enabled": true },
  "deathmarker": { "enabled": true, "x": null, "y": null, "z": null, "dimension": null },
  "recipeviewer": { "enabled": true },
  "veinminer": { "enabled": true, "maxBlocks": 32, "whitelist": ["minecraft:iron_ore", "..."] }
}
```

## Catatan

- Vein Miner perlu mod terinstall di sisi **server** (singleplayer otomatis OK)
- Fortune/Silk Touch bekerja pada blok pertama (yang di-break vanilla); blok tambahan pakai loot table vanilla standar
