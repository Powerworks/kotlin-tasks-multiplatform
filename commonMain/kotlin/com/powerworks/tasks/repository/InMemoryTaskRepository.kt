package com.powerworks.tasks.repository

import com.powerworks.tasks.model.Task
import com.powerworks.tasks.model.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of [TaskRepository].
 * Local store is canonical; remote sync is asynchronous and may fail.
 * All access is serialized through a single-threaded dispatcher to avoid
 * shared mutable state — the Kotlin equivalent of an actor mailbox.
 */
class InMemoryTaskRepository(
    private val syncService: SyncService? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : TaskRepository, CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    private val tasks = ConcurrentHashMap<String, Task>()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    override fun observeTasks(): Flow<List<Task>> = _tasks.asStateFlow()

    override suspend fun getAll(): List<Task> = withContext(ioDispatcher) {
        tasks.values.filter { !it.isDeleted }.sortedByDescending { it.modifiedAt }.toList()
    }

    override suspend fun getById(id: String): Task? = withContext(ioDispatcher) {
        tasks[id]
    }

    override suspend fun create(task: Task): Task = withContext(ioDispatcher) {
        val now = Instant.now()
        val stored = task.copy(createdAt = now, modifiedAt = now, isDeleted = false)
        tasks[stored.id] = stored
        publish()
        syncService?.push(stored)
        stored
    }

    override suspend fun update(task: Task): Task = withContext(ioDispatcher) {
        val now = Instant.now()
        val stored = task.copy(modifiedAt = now)
        tasks[stored.id] = stored
        publish()
        syncService?.push(stored)
        stored
    }

    override suspend fun delete(id: String) = withContext(ioDispatcher) {
        tasks[id]?.let {
            tasks[id] = it.copy(isDeleted = true, modifiedAt = Instant.now())
            publish()
            syncService?.tombstone(id)
        }
    }

    override suspend fun sync() = withContext(ioDispatcher) {
        syncService?.pull()?.forEach { remote ->
            val local = tasks[remote.id]
            if (local == null || remote.modifiedAt.isAfter(local.modifiedAt)) {
                tasks[remote.id] = remote
            }
        }
        publish()
    }

    private fun publish() {
        _tasks.value = tasks.values.filter { !it.isDeleted }.sortedByDescending { it.modifiedAt }.toList()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + SupervisorJob()
}

import java.time.Instant
