package com.powerworks.tasks.usecase

import com.powerworks.tasks.model.Task
import com.powerworks.tasks.model.TaskRepository

/**
 * Use case: create a task.
 * Depends on the repository interface, not on a concrete implementation.
 */
class CreateTaskUseCase(private val repository: TaskRepository) {
    suspend fun invoke(title: String, description: String = ""): Task =
        repository.create(Task(id = java.util.UUID.randomUUID().toString(), title = title, description = description))
}

/**
 * Use case: mark a task complete.
 */
class CompleteTaskUseCase(private val repository: TaskRepository) {
    suspend fun invoke(id: String): Task? =
        repository.getById(id)?.let { task ->
            repository.update(task.copy(completed = true))
        }
}

/**
 * Use case: sync local state with remote.
 */
class SyncTasksUseCase(private val repository: TaskRepository) {
    suspend fun invoke() = repository.sync()
}
