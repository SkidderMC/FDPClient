# FDPClient parity coverage

This matrix is the release gate for the 1.8.9 parity program. `Implemented` means the code path is
present and covered by compilation, static analysis, or deterministic verification. `External`
means the implementation is complete but still needs a controlled live-server session.

| Area | Status | Evidence |
|---|---|---|
| Value types and persistence | Implemented | Double/Long ranges, vectors, mutable lists, registry multi-selects, key actions and round-trip verification |
| Module state/config lifecycle | Implemented | Enable-before-callback ordering, re-entrant loading scope, versioned flat-to-nested migration |
| Combat selection and prediction | Implemented | ComparatorChain, TargetTracker, extrapolation, item/armor scoring, CombatManager and simulation snapshots |
| Rotation engine | Implemented | Adaptive smoothers, embedded neural model, GCD, visual interpolation, projectile strategies and movement correction |
| Movement foundations | Implemented | Exact 1.8 physics simulation, edge planning, dodge planning, hazard avoidance and speed safety helpers |
| Network observation | Implemented | Plugin/channel/brand observation, transaction fingerprinting, lag-back telemetry and ordered spawn handling |
| Anticheat mode guidance | Implemented | NCP, AAC, Grim, Vulcan, Watchdog, Verus, Matrix, Intave, Spartan and Polar risk profiles |
| Native ClickGUI parity | Implemented | Search, every value kind, nested collapsible groups and deterministic grouping for remaining legacy modules |
| Web ClickGUI and HUD | Implemented | Live event socket, nested settings, search/descriptions, web HUD bridge and persistent MCEF browser |
| Virtual screens | Implemented | Title, multiplayer, singleplayer, alt manager, disconnected, inventory, ClickGUI and HUD with native fallback |
| AutoConfig/catalog | Implemented | HTTPS catalog, bounded downloads, pinned catalog SHA-256, mandatory per-preset SHA-256 and unsigned opt-in only |
| Build and release hygiene | Implemented | Machine-local Java config, Detekt, unit/foundation tests, library provenance/checksums and tag release workflow |
| Localization | Implemented | Seven locale bundles, English fallback, PT-BR coverage and a localized fallback tooltip for every value |
| 1.9+ mechanics | N/A | Offhand, elytra, mace, wind charge, pose and data components do not exist in protocol 47 |
| Server-dependent dupes | N/A | No universal deterministic client implementation exists; adding a known patched exploit would be misleading |
| Live anticheat sessions | External | Run the final validation matrix on authorized NCP/AAC/Grim/Vulcan/Watchdog/Verus/Matrix/Intave/Spartan/Polar targets |

The matrix is code-complete only when every non-External row is green. A release is validated only
after the final External row is recorded with server build, anticheat build, mode, duration and result.
