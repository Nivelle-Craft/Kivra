<div align="center">

# ✦ Kivra

### One mod. Your whole server.

**Kivra is an all-in-one, modular server core for Minecraft Forge.**  
Permissions, economy, claims, shops, kits, moderation and everyday server tools — designed to work together instead of feeling like a pile of unrelated mods.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=for-the-badge)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.10-E88C43?style=for-the-badge)](https://files.minecraftforge.net/)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Build](https://img.shields.io/github/actions/workflow/status/nazhida/Kivra/build.yml?branch=main&style=for-the-badge&label=Build)](https://github.com/nazhida/Kivra/actions)
[![Latest](https://img.shields.io/github/v/release/nazhida/Kivra?include_prereleases&style=for-the-badge&label=Latest)](https://github.com/nazhida/Kivra/releases)

**[⬇ Download Kivra](https://github.com/nazhida/Kivra/releases) · [⚙ Builds](https://github.com/nazhida/Kivra/actions) · [🐛 Issues](https://github.com/nazhida/Kivra/issues)**

</div>

---

## 🌿 What is Kivra?

Running a Minecraft server usually means installing one thing for permissions, another for economy, another for claims, another for moderation… and then trying to make all of them behave nicely together.

Kivra takes a different approach: **one server mod with a shared core and independent modules**.

You can manage the important parts of your server from the same system, with consistent permissions, persistent data and an in-game admin interface.

> **The goal:** less time wiring server tools together, more time actually building your server.

---

## ✨ What's inside?

| Module | What it does | Status |
| :--- | :--- | :---: |
| 🔐 **Permissions** | Groups, permission nodes, inheritance, prefixes and weights | ✅ |
| 🖥️ **Admin GUI** | In-game control panel with module management | ✅ |
| 💰 **Economy** | Player balances, payments and admin controls | ✅ |
| 🏠 **Essentials** | Homes, spawn, warps and TPA | ✅ |
| 🛡️ **Claims** | Protect areas and trust other players | ✅ |
| 🛒 **Shops** | Player and admin shops | ✅ |
| 🎁 **Kits** | Reusable kits with cooldowns | ✅ |
| 🔨 **Moderation** | Warns, kicks, bans, mutes and history | ✅ |
| 💬 **Chat** | Rank-aware chat foundation | ✅ |

Kivra is still under active development, so these systems will continue getting deeper configuration and nicer interfaces.

---

## 🖥️ Admin GUI

Don't want to remember dozens of administration commands?

Run:

```text
/kivra admin
```

The Kivra panel gives administrators a central place for:

- 👤 Players
- 🏷️ Ranks & permissions
- 💰 Economy
- 🛡️ Claims
- 🛒 Shops
- 🎁 Kits
- 🔨 Moderation

Destructive actions such as deleting claims, shops or kits use a **confirmation screen** to help prevent accidental changes.

---

## 🔐 Ranks that make sense

Kivra ships with a simple rank hierarchy:

```text
Default → VIP → Helper → Moderator → Admin → Owner
```

Higher groups can inherit from lower groups, while permission nodes keep access granular.

Examples:

```text
kivra.essentials.home
kivra.claim.create
kivra.shop.buy
kivra.moderation.warn
kivra.admin
```

Wildcard nodes such as `kivra.moderation.*` and `*` are supported too.

---

## 🚀 Installation

### Requirements

- **Minecraft:** 1.20.1
- **Forge:** 47.4.10
- **Java:** 17

### Install

1. Download **`Kivra-latest.jar`** from [Releases](https://github.com/nazhida/Kivra/releases).
2. Put the JAR inside your server's `mods/` folder.
3. Start the server.
4. Kivra will create its data/configuration files automatically.
5. Join the server and try `/kivra admin` as an operator.

That's it. 🌱

---

## 🧩 A few useful commands

```text
/kivra admin
/kivra groups

/spawn
/setspawn
/sethome <name>
/home <name>
/tpa <player>

/claim pos1
/claim pos2
/claim create <name>

/shops
/kits
```

Administration and module-specific commands are permission controlled.

---

## 💾 Data

Kivra keeps its module data persistent between restarts. Permission groups and users are stored in readable JSON files under the Kivra configuration directory, making the setup easier to inspect and back up.

---

## 🗺️ Where Kivra is going

The current foundation is already usable, but this is only the beginning. Upcoming work is focused on polishing the experience rather than turning Kivra into a collection of random features:

- richer module GUIs
- better player management
- deeper rank and permission editing from the GUI
- more flexible economy controls
- claim and shop management improvements
- configurable messages and module settings
- better admin history and audit information
- quality-of-life improvements based on real server testing

---

## 🧑‍💻 Building from source

Clone the repository and run:

```bash
./gradlew build
```

On Windows:

```powershell
gradlew.bat build
```

The compiled mod will be generated under:

```text
build/libs/
```

Every push to `main` is also compiled automatically by GitHub Actions.

---

## 🐛 Found something broken?

Kivra is being actively developed and tested. If you find a bug, weird behavior or something that could feel better, open an [issue](https://github.com/nazhida/Kivra/issues).

When reporting a problem, including the Minecraft/Forge version and the relevant server log makes debugging much easier.

---

<div align="center">

### ✦ Kivra

**Build the server. Kivra handles the boring parts.**

Made for Minecraft servers that want one clean foundation instead of ten disconnected systems.

</div>
