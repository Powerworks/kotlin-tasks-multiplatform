# kotlin-tasks-multiplatform

A task manager in Kotlin Multiplatform demonstrating **structured concurrency** and a **repository protocol**.

**Thesis:** Kotlin coroutines brought structured concurrency to the JVM — cancellation is hierarchical, scopes form trees, and work is scoped to lifecycles rather than fire-and-forget thread pools. This is Ada's tasking and OTP's supervision, reborn for the JVM. Paired with a repository protocol, it mirrors the SOA service boundary (repo #3) and Swift's actor-isolated model (repo #5).

This is **repo #6** in the compounding series on `github.com/Powerworks`:

```
C message passing  ──►  Ada contracts  ──►  SOA services  ──►  Elixir actors  ──►  Swift mobile  ──►  Kotlin mobile  ──►  WASM / eBPF / HTTP3
                                                     └── (healthcare track: hl7-mirth-2019)
```

## What's inside

| Path | Purpose |
|------|---------|
| `commonMain/kotlin/` | Shared Kotlin code: models, repository interface/impl, coroutines, Flow |
| `commonTest/kotlin/` | Tests using kotlinx.coroutines.test |
| `build.gradle.kts` | Gradle build (Kotlin DSL) |
| `settings.gradle.kts` | Project settings |
| `design/` | System design narrative, ADRs, Mermaid diagrams |
| `architecture/` | Principal-level architecture extract (Obsidian-friendly) |
| `docs/` | Compounding links to previous/next projects |

## Build & run

Requires **JDK 17+** and **Gradle**.

```bash
./gradlew build
./gradlew jvmRun   # if a JVM application target is configured
./gradlew commonTest
```

## Design summary

- **Structured concurrency** — `CoroutineScope` + `Job` hierarchy; cancellation propagates parent→child
- **Flow** — cold streams (`flowOf`, `flow { }`), hot streams (`StateFlow`, `SharedFlow`) for UI state
- **Repository protocol** — `TaskRepository` interface abstracts local vs remote; callers depend on the interface, not the implementation
- **Offline-first** — local in-memory store is canonical; remote sync is asynchronous and may fail
- **Structured concurrency** — `supervisorScope`, `coroutineScope`, `launch`, `async` with proper cancellation

See `design/README.md` for the full system design and `architecture/ARCHITECTURE.md` for the principal-level extract.
