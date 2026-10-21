# Compounding series

This is **repo #6** in the sequence where each project extracts architecture bits and feeds the next.

## Sequence

| # | Repo | Language/Stack | Core idea extracted |
|---|------|----------------|---------------------|
| 1 | [`c-message-passing`](https://github.com/Powerworks/c-message-passing) | C99 | Actor message passing, mailbox isolation |
| 2 | [`ada-contract-state-machine`](https://github.com/Powerworks/ada-contract-state-machine) | Ada 2012 | Design-by-contract, protected objects |
| 3 | [`soa-contract-first`](https://github.com/Powerworks/soa-contract-first) | C99 / HTTP/JSON | Service contracts as bounded boundaries |
| 4 | [`elixir-supervision-pool`](https://github.com/Powerworks/elixir-supervision-pool) | Elixir / OTP | Supervision trees, let-it-crash |
| 5 | [`swift-notes-offline-first`](https://github.com/Powerworks/swift-notes-offline-first) | Swift / SwiftUI | Value types, actors, offline-first UI |
| 6 | `kotlin-tasks-multiplatform` | Kotlin Multiplatform | Structured concurrency, repository protocol |
| 7 | `wasm-actor-runtime` | C → WASM | Sandboxed message passing at the bytecode level |
| 8 | `ebpf-observability` | C + eBPF | Programmable kernelspace tracing & networking |
| 9 | `http3-message-gateway` | C / HTTP/3 | QUIC-based message transport, datagram-first semantics |

## What this repo contributes downstream

- **From Swift:** Offline-first model → Kotlin coroutines + Flow (JVM equivalent of Swift actors + async).
- **From SOA:** Repository protocol → service contract; `SyncService` → network boundary.
- **From Elixir:** `SupervisorJob` → OTP supervision (lighter weight, no process isolation).
- **To WASM:** Structured concurrency at bytecode level; Wasm modules as isolated units with scoped lifetimes.
- **To eBPF:** Coroutine cancellation → kernelspace observable lifecycle events.

## Obsidian links

[[Programming/C|c-message-passing]]  
[[Programming/Ada|ada-contract-state-machine]]  
[[Programming/SOA|soa-contract-first]]  
[[Programming/Elixir|elixir-supervision-pool]]  
[[Programming/Swift|swift-notes-offline-first]]  
[[Programming/Kotlin|kotlin-tasks-multiplatform]]
