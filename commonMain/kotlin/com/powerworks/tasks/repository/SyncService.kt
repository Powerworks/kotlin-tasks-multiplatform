package com.powerworks.tasks.repository

import com.powerworks.tasks.model.Task
import kotlinx.coroutines.flow.flow

/**
 * Network boundary — may fail (offline).
 * Returns empty list when unreachable.
 * The repository doesn't know whether sync succeeded; it just merges whatever arrived.
 */
class SyncService {

    suspend fun push(task: Task) {
        // Network call — may throw if offline
        delay(50) // simulate network latency
    }

    suspend fun pull(): List<Task> {
        // Network call — returns [] when offline
        delay(100)
        return emptyList()
    }

    suspend fun tombstone(id: String) {
        delay(50)
    }
}

/**
 * Mock implementation used in tests to simulate flaky network.
 */
class FakeSyncService(
    private val remoteTasks: MutableList<Task> = mutableListOf(),
    private val shouldFail: Boolean = false
) : SyncService() {

    override suspend fun push(task: Task) {
        if (shouldFail) throw IllegalStateException("network down")
        remoteTasks.addIfNotPresent(task)
    }

    override suspend fun pull(): List<Task> {
        if (shouldFail) throw IllegalStateException("network down")
        return remoteTasks.toList()
    }

    override suspend fun tombstone(id: String) {
        if (shouldFail) throw IllegalStateException("network down")
        remoteTasks.removeIf { it.id == id }
    }

    private fun MutableList<Task>.addIfNotPresent(task: Task) {
        if (!any { it.id == task.id }) add(task)
    }
}
