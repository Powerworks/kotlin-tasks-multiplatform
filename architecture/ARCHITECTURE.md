# Architecture extract — kotlin-tasks-multiplatform

**Level:** Principal  
**Purpose:** Extract architecture-relevant bits that compound into the next projects in this series.  
**Format:** Obsidian-friendly markdown with `[[wikilinks]]`.

## Signal (carry forward)

| Pattern | Observation | Why it matters |
|---------|-------------|----------------|
| **Structured concurrency** | `CoroutineScope` + `SupervisorJob`; cancellation is hierarchical (parent→child). | Work is scoped to lifecycles, not fire-and-forget threads. |
| **Repository protocol** | `TaskRepository` interface; callers depend on the interface. | Same boundary as SOA service contracts (repo #3) and Swift's actor model (repo #5). |
| **Flow** | `StateFlow` for UI state; cold `flow { }` for one-shot operations. | Unified stream model replaces callbacks and observables. |
| **Local state canonical** | Writes to local store immediately; background sync reconciles. | Same offline-first principle as Swift (repo #5). |

## Noise (deliberately skipped at this layer)

- **Android-specific code** — shared Kotlin is platform-agnostic; Android UI layer is a thin adapter.
- **Room / SQLDelight** — persistence detail; repository interface is unchanged.
- **Retrofit / Ktor** — network client detail; `SyncService` is the abstraction boundary.

## Principal takeaway

> Structured concurrency means cancellation is a property of the scope, not of individual tasks. If a scope dies, its work dies with it — no orphaned threads, no leaked network calls.

Kotlin coroutines give the JVM what Erlang/OTP gave the BEAM: structured, scoped concurrency with hierarchical cancellation — without process isolation (that's the actor's job, handled by the repository's single-threaded access).

## Compounds into

- [[../../elixir-supervision-pool/architecture/ARCHITECTURE|Elixir supervision]] — `SupervisorJob` is the JVM equivalent of OTP supervision (lighter, no process boundary)
- [[../../swift-notes-offline-first/architecture/ARCHITECTURE|Swift offline-first]] — same model; Swift actors are language-built-in, Kotlin achieves it via dispatcher + interface
- [[../../soa-contract-first/architecture/ARCHITECTURE|SOA services]] — repository protocol is the service contract; `SyncService` is the network boundary
- [[../wasm-actor-runtime/docs/COMPOUNDING|WASM (planned)]] — structured concurrency at bytecode level; Wasm modules as isolated units
