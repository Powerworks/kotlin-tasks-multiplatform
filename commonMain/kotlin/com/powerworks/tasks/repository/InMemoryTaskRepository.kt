package com.powerworks.tasks.repository

import com.powerworks.tasks.model.Task
import com.powerworks.tasks.model.TaskEvent
import com.powerworks.tasks.model.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

class InMemoryTaskRepository(
    private val syncService: SyncService? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : TaskRepository, CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    private val tasks = ConcurrentHashMap<String, Task>()
    private val _tasksState = MutableStateFlow<List<Task>>(emptyList())
    private val _taskEvents = MutableSharedFlow<TaskEvent>(replay = 1, extraBufferCapacity = 64)

    // Channel for external sync requests — decouples caller from sync execution
    private val syncChannel = Channel<SyncCommand>(Channel.UNLIMITED)

    private sealed class SyncCommand {
        object Pull : SyncCommand()
        data class Push(val task: Task) : SyncCommand()
    }

    init {
        launch {
            for (cmd in syncChannel) {
                when (cmd) {
                    is SyncCommand.Pull -> doPull()
                    is SyncCommand.Push -> syncService?.push(cmd.task)
                }
            }
        }
    }

    override val tasksState: StateFlow<List<Task>> = _tasksState.asStateFlow()
    override fun observeTasks(): Flow<TaskEvent> = _taskEvents.asSharedFlow()

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
        _taskEvents.emit(TaskEvent.Success(listTasksLocked()))
        syncChannel.send(SyncCommand.Push(stored)) // non-blocking, structured
        stored
    }

    override suspend fun update(task: Task): Task = withContext(ioDispatcher) {
        val now = Instant.now()
        val stored = task.copy(modifiedAt = now)
        tasks[stored.id] = stored
        publish()
        _taskEvents.emit(TaskEvent.Success(listTasksLocked()))
        syncChannel.send(SyncCommand.Push(stored))
        stored
    }

    override suspend fun delete(id: String) = withContext(ioDispatcher) {
        tasks[id]?.let {
            tasks[id] = it.copy(isDeleted = true, modifiedAt = Instant.now())
            publish()
            _taskEvents.emit(TaskEvent.Success(listTasksLocked()))
        }
    }

    override suspend fun sync() = withContext(ioDispatcher) {
        syncChannel.send(SyncCommand.Pull)
        doPull()
    }

    private suspend fun doPull() {
        _taskEvents.emit(TaskEvent.Loading)
        syncService?.pull()?.forEach { remote ->
            val local = tasks[remote.id]
            if (local == null || remote.modifiedAt.isAfter(local.modifiedAt)) {
                tasks[remote.id] = remote
            }
        }
        publish()
        _taskEvents.emit(TaskEvent.Success(listTasksLocked()))
    }

    private fun publish() {
        _tasksState.value = listTasksLocked()
    }

    private fun listTasksLocked(): List<Task> =
        tasks.values.filter { !it.isDeleted }.sortedByDescending { it.modifiedAt }.toList()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + SupervisorJob()
}

import java.time.Instant
