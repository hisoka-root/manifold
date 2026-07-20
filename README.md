# Manifold

**Intelligent item logistics for NeoForge 1.21.1**

Manifold is a spiritual successor to Logistics Pipes — a pipe network mod where items don't just flow in a straight line. Instead, blocks on the network **request** what they need from anywhere connected to the pipe grid, using smart routing to find the shortest path through the network.

[![Build](https://github.com/hisoka-root/manifold/actions/workflows/build.yml/badge.svg)](https://github.com/hisoka-root/manifold/actions)

## Features

- **Smart routing** — items take the shortest path through the network, resolved off-thread so your TPS stays smooth
- **No per-mod adapters** — any block from any mod that exposes `IItemHandler` (chests, furnaces, barrels, machines) works automatically via NeoForge capabilities
- **Four pipe types** — Transport, Provider, Requester, and Supplier cover all basic logistics workflows
- **Visual item motion** — items are rendered traveling through pipes in real time, interpolated client-side
- **Energy economy** — moving items costs FE, drawn from any power source on the network

## Pipes

| Pipe | Does |
|------|------|
| **Transport Pipe** | Routes items through the network. Doesn't interact with inventories. |
| **Provider Pipe** | Exposes an adjacent inventory to the network so Requesters can pull from it. |
| **Requester Pipe** | Pulls configured items from any Provider on the network. |
| **Supplier Pipe** | Keeps a set amount of items in an adjacent inventory by requesting from the network when below target. |

## Getting Started

1. **Build a network** — place Transport Pipes connecting your machines and storage
2. **Add Providers** — place a Provider Pipe next to any inventory you want the network to pull from
3. **Add Requesters** — place a Requester Pipe next to a machine that needs items, then sneak-right-click with the Wrench to configure which items to request
4. **Power it** — connect a power source (any block with `IEnergyStorage`) somewhere on the network

## Recipes

| Item | Recipe |
|------|--------|
| Transport Pipe | 4 iron ingots + 4 glass panes |
| Provider Pipe | 4 iron ingots + 3 glass panes + 1 chest |
| Requester Pipe | 4 gold ingots + 4 glass panes |
| Supplier Pipe | 4 iron ingots + 3 glass panes + 1 dispenser |
| Wrench | 3 iron ingots + 1 glass pane |

## Download

Available on Modrinth and CurseForge (links coming with first release).

## Addon Development

Manifold ships a separate `manifold-api` artifact (MIT-licensed) that addon mods can compile against:

```gradle
repositories {
    maven {
        name = 'GitHubPackages'
        url = 'https://maven.pkg.github.com/hisoka-root/manifold'
        credentials {
            username = System.getenv('GITHUB_ACTOR') ?: 'your-username'
            password = System.getenv('GITHUB_TOKEN') ?: 'your-token'
        }
    }
}
dependencies {
    implementation "lol.hisoka:manifold-api:0.1.0"
}
```

Publishing is automatic on tagged releases via CI. Only `manifold-api` and `manifold` core are published — compat stubs are not.

The API exposes `EndpointAdapter` for registering custom network endpoints, `RequestFulfillmentEvent` for hooking request lifecycle, and `PipeModuleType`/`PipeModule` for Chassis modules (v0.2+).

## Roadmap

- **v0.1** — Transport / Provider / Requester / Supplier pipes, off-thread routing, capability compat, in-pipe rendering
- **v0.2** — Crafting Pipe, Chassis Pipe + modules, AE2 & Mekanism compat
- **v0.3** — Relay Node (cross-network), Refined Storage compat, Create compat, Crafting Monitor
- **v1.0** — API freeze, JEI/EMI integration, 500+ node performance pass

## License

- `manifold-api` — MIT (zero-friction for addons)
- `manifold` (core mod) — MPL-2.0 (file-level copyleft, doesn't reach addon code)

## Credits

- Pipe geometry adapted from [Mekanism](https://github.com/mekanism/Mekanism)'s Logistical Transporter models (MIT license, Copyright Aidan C. Brady)
- Inspired by Logistics Pipes (RS485/zieger999/Krapht)

---

*Built for NeoForge 1.21.1. Java 21. Branch `1.21.x`. For other versions/loaders, see [Multi-Version](#multi-version-development).*

## Multi-Version Development

Each Minecraft version lives on its own branch (`1.21.x`, `1.22.x`, etc.). Future Fabric/Quilt ports will follow the same pattern on `fabric/1.21.x`, `quilt/1.21.x`, etc.

To start a new version branch:
```bash
git checkout -b 1.22.x master
```
Then update `gradle.properties` (`minecraft_version` + `neoforge_version`), bump `neoforge.mods.toml` version ranges, and run `./gradlew runData`.

## Building

```bash
./gradlew build        # compile, test, package
./gradlew runData      # regenerate assets (models, recipes, lang, loot tables)
```
