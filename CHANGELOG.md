# Changelog

## [0.1.0] — 2026-07-20

### Added
- **Transport Pipe** — routes items through the network (no capability interaction)
- **Provider Pipe** — advertises adjacent inventory contents to the network
- **Requester Pipe** — continuously requests configured items from available providers
- **Supplier Pipe** — maintains buffer levels in adjacent inventory by requesting from the network
- **Off-thread routing** — Dijkstra path resolution on ForkJoinPool (`availableProcessors/2`, min 1, max 4)
- **4-tick coalescing window** — batches inventory-change events to avoid routing-table thrash
- **Zero-config capability compat** — any block exposing `IItemHandler` is automatically a valid network endpoint via `CapabilityEndpointAdapter` + `BlockCapabilityCache`
- **Multipart pipe models** — connector geometry adapted from Mekanism's Logistical Transporter (MIT-licensed)
- **Per-type Mekanism textures** — Transport (basic), Provider (advanced/red), Requester (elite/blue), Supplier (ultimate/purple)
- **In-pipe item rendering** — server-authoritative positions via `ItemPositionPayload`, client-interpolated
- **FE energy budget** — network scans for `IEnergyStorage` contributors; zero energy = graceful degradation (longer queue times)
- **Manifold Wrench** — right-click to dismantle pipes, sneak+right-click for config info
- **Network graph** — merge on connect, flood-fill split on disconnect
- **Crafting Table** (per-network, storage only) — recipe registration with cycle detection
- **Crafting Resolver** (engine, not wired) — stock-then-craft recursion with dependency ordering
- **Persistent network data** — `SavedData` per dimension, adjacency + metadata saved to disk
- **`RequestFulfillmentEvent`** (Pre/Post) — fired on NeoForge event bus for addon hooking
- **Full datagen** — blockstates, models, recipes, loot tables, `en_us` lang (all generated via `runData`)
- **Unit tests** — `NetworkGraph` (merge/split), `DijkstraPathResolver` (pathfinding), `CraftingTable` (cycle detection)
- **GameTest stubs** — structure template + test class for capability discovery
- **MIT-licensed `manifold-api`** — public API surface for third-party pipe/module authors
- **MPL-2.0 licensed `manifold`** — core mod with file-level weak copyleft
- **Compat module scaffolding** — AE2, Refined Storage, Create, Mekanism (empty stubs for v0.2+)

### Not in v0.1
- Crafting Pipe behavior (resolver present but not wired to pipe tick)
- Chassis Pipe + PipeModules
- Relay Node (cross-network bridge)
- Compat module adapter implementations
- Dedicated Crafting Monitor UI
- JEI/EMI crafting recipe display
- Fluid or energy pipe types
