# ADR 0001 — Structured concurrency over fire-and-forget threads

## Status

Accepted

## Context

JVM concurrency before coroutines meant thread pools, `ExecutorService`, and fire-and-forget `Runnable`s. Cancellation was ad-hoc, thread leaks were common, and reasoning about concurrent work was difficult.

We considered:
1. **Plain threads + ExecutorService** — no structured cancellation, easy to leak.
2. **RxJava** — powerful but steep learning curve; hot/cold stream confusion.
3. **Kotlin coroutines + Flow** — structured, scoped to lifecycles, cancellation is hierarchical.

## Decision

Use Kotlin coroutines with `CoroutineScope` + `SupervisorJob` + `Flow`. Cancellation is structural; work is scoped to a Job tree.

## Consequences

### Positive

- **Hierarchical cancellation** — cancelling a parent scope cancels all children.
- **Structured** — `coroutineScope` waits for all children; `supervisorScope` isolates siblings.
- **Flow** — unified cold/hot stream model; `StateFlow` for UI state.

### Negative

- **Learning curve** — coroutines require understanding of suspend, scope, job, dispatcher.
- **Debugging** — stack traces across suspension points are harder to read.

## Related

- [[../architecture/ARCHITECTURE]] — principal extract
- [[../../swift-notes-offline-first/architecture/ARCHITECTURE|Swift offline-first]] — same offline-first model; Kotlin coroutines + Flow are the JVM equivalent of Swift actors + async
- [[../../elixir-supervision-pool/architecture/ARCHITECTURE|Elixir supervision]] — supervisor job is the JVM equivalent of OTP supervision (lighter weight, no process isolation)
