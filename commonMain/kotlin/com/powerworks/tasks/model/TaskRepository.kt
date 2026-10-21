package com.powerworks.tasks.model

import kotlinx.coroutines.flow.Flow
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
 * Mirrors the SOA service boundary (repo #3) and Swift's actor-isolated model (repo #5).
 */
interface TaskRepository {
    suspend fun getAll(): List<Task>
    suspend fun getById(id: String): Task?
    suspend fun create(task: Task): Task
    suspend fun update(task: Task): Task
    suspend fun delete(id: String)
    suspend fun sync() // pull remote changes, merge with local

    // Reactive stream of task events — cold Flow, collected by UI
    fun observeTasks(): Flow<List<Task>>
}
