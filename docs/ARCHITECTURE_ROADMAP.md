# FDP architecture roadmap

This document tracks the incremental modernization work for the Minecraft 1.8.9 client. Each
block must compile independently, preserve existing configuration files, and avoid work on the
render thread that can be bounded or scheduled.

## Initial inventory

| Area | Existing base | Main gap |
| --- | --- | --- |
| Values and config | Recursive `Configurable`, range/file/curve/group values | Action-scoped random ranges and explicit serialization policies |
| Commands | Typed command builder and dispatcher | Kotlin DSL surface and broader adoption |
| Events | Priority hooks, coroutine scope and tick scheduler | Explicit cancellation/discard behavior and reusable triggers |
| Geometry | Scattered `Vec3` helpers and Minecraft intercept calls | Reusable line, ray, plane and face primitives |
| World | Legacy path utility and module-specific scans | Incremental chunk scanner, block tracker and bounded graph search |
| Simulation | Player and arrow simulation | Bounded N-tick prediction facade and trajectory consumers |
| Rotation | Request arbitration, point tracking and post-move executor | Unified action timing modes and optional vector diagnostics |
| Player filters | Existing bot filter, buff and tool modules | Composable transparent predicates and durability-aware scoring |
| Render | Several font/render paths and damage particle module | Bounded atlas allocation with safe legacy fallback |
| Browser UI | Existing local browser bridge/server | Non-blocking event stream and reconnect protocol |

The middle-click modules overlap. They will be consolidated only after their registration and
configuration migration paths are verified, so existing user configs keep loading.

## Current status

- Block A completed: geometry, packed color, refreshable range and deterministic verification.
- Block B completed: JSON profiles, export policy, command DSL, suspend handler policies and triggers.
- Block C in progress: bounded graph search, incremental chunk/block tracking, support planning,
  face targeting and the separated placement planner are completed. Tool scoring remains.

## Delivery blocks and affected files

### Block A: deterministic foundations

- `utils/math/geometry/VecGeometry.kt`
- `utils/math/geometry/Line.kt`
- `utils/math/geometry/Ray.kt`
- `utils/math/geometry/Plane.kt`
- `utils/math/geometry/Face.kt`
- `utils/render/Color4b.kt`
- `config/Values.kt`
- `config/RefreshableRangeValue.kt`
- `config/Configurable.kt`
- `src/test/.../FoundationVerification.kt`
- `build.gradle`

Acceptance: finite-input validation, deterministic verification, no config schema change, and a
successful `check` task.

### Block B: configuration, command and event foundations

- Add serialization profiles and field annotations under `file/gson`.
- Add a concise Kotlin command DSL over the existing typed command tree.
- Add explicit coroutine handler behavior under `event/async`.
- Add tick, movement and world triggers under `event`.

Acceptance: legacy JSON remains readable; event cancellation cannot leak a job; DSL invariants are
verified without changing current commands.

### Block C: world services

- Add an incremental chunk work queue and abstract block tracker under `utils/block`.
- Add bounded Dijkstra and A* implementations under `utils/pathfinding`.
- Add support evaluation, face targeting and placement planning under `utils/block`.
- Refactor tool selection into a pure scorer before wiring it to the player module.

Acceptance: explicit per-tick budgets, maximum cost/node limits, immutable snapshots, and safe
fallback results.

### Block D: prediction and rotation

- Add a drag-aware projectile solver under `utils/simulation`.
- Add an N-tick prediction facade over the existing simulators.
- Add action timing modes to the rotation request API.
- Add disabled-by-default vector diagnostics and local trajectory assistance.

Acceptance: solvers remain independent from modules and all loops have configurable hard limits.

### Blocks E-G: player filters, rendering and browser UI

These blocks follow only after the reusable services are stable. They cover transparent entity
predicates, unified buff logic, atlas/font caches, particles, and a reconnecting event stream for
the existing browser UI.

## Deferred or incompatible work

Modern-version-only items (equipment slots, flight equipment, new combat items and modern GPU
pipelines) are not compatible with 1.8.9 and will not be added. Network-evasion behavior and
public-server exploitation are outside this roadmap. Browser bootstrap replacement is unnecessary
because the client already has a local browser runtime.

## Technical risks

- Minecraft 1.8.9 classes are mutable or nullable in places; foundation APIs validate inputs and
  expose immutable results where practical.
- Full game startup depends on native graphics/browser libraries; pure verification tasks must
  cover math and scheduling logic separately.
- Configuration migrations require aliases rather than destructive renames.
- Chunk work, glyph upload and browser delivery must never block the render thread.
