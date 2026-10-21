package com.powerworks.tasks.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

data class Task(
    val id: String,
    var title: String,
    var description: String = "",
    var completed: Boolean = false,
    val createdAt: Instant = Instant.now(),
    var modifiedAt: Instant = Instant.now(),
    var isDeleted: Boolean = false
)

sealed class TaskEvent {
    object Loading : TaskEvent()
    data class Success(val tasks: List<Task>) : TaskEvent()
    data class Failure(val exception: Throwable) : TaskEvent()
}

/**
 * Repository protocol — abstracts local vs remote.
 * Callers depend on this interface, not on a concrete implementation.
 */
interface TaskRepository {
    suspend fun getAll(): List<Task>
    suspend fun getById(id: String): Task?
    suspend fun create(task: Task): Task
    suspend fun update(task: Task): Task
    suspend fun delete(id: String)
    suspend fun sync()

    // Reactive streams — StateFlow for current state, Flow for event stream
    val tasksState: StateFlow<List<Task>>
    fun observeTasks(): Flow<TaskEvent>
}
