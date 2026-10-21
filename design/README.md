# System design — kotlin-tasks-multiplatform

## Scope

A task manager in Kotlin Multiplatform demonstrating **structured concurrency** and a **repository protocol**. Shared Kotlin code targets JVM (and can be extended to Android/iOS via Compose Multiplatform).

## Components

### Task model (`data class Task`)

Value class with value semantics. `isDeleted` flag supports tombstone pattern for offline-first sync.

### TaskRepository (interface)

The abstraction boundary. Callers depend on the interface, not a concrete implementation:
- `getAll()` / `getById()` — reads
- `create()` / `update()` / `delete()` — writes
- `sync()` — pull remote, merge local
- `observeTasks(): Flow<List<Task>>` — reactive stream of state changes

### InMemoryTaskRepository (implementation)

Single-threaded access via `Dispatchers.Default + SupervisorJob()`. All mutations are serialized, avoiding shared mutable state — the Kotlin equivalent of an actor mailbox.

Exposes `StateFlow<List<Task>>` for UI observation (hot stream).

### SyncService

Network boundary. May fail (offline). Returns empty list when unreachable. Repository merges whatever arrives via last-write-wins.

### Use cases

`CreateTaskUseCase`, `CompleteTaskUseCase`, `SyncTasksUseCase` — thin wrappers around the repository. Keep business rules out of the repository and out of the UI.

## Structured concurrency

- `CoroutineScope` + `SupervisorJob` — child failures don't cancel siblings
- `withContext(Dispatchers.Default)` — offload blocking work off the caller's thread
- `launch` / `async` — structured, scoped to a Job tree
- Cancellation propagates parent → child

## Decisions recorded

See `design/adr/0001-structured-concurrency.md`.

## Diagrams

See `design/diagrams/task-repository-architecture.mmd`.
