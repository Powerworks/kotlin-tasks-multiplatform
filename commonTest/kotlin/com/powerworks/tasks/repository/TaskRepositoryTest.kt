package com.powerworks.tasks.model

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.Test

class TaskRepositoryTest {

    @Test
    fun create_and_fetch() = runCurrent {
        val repo = InMemoryTaskRepository()
        val task = repo.create(Task(id = "t1", title = "Buy milk"))
        assertEquals("Buy milk", task.title)
        assertEquals(1, repo.getAll().size)
    }

    fun `delete marks isDeleted`() = runCurrent {
        val repo = InMemoryTaskRepository()
        repo.create(Task(id = "t1", title = "Temp"))
        repo.delete("t1")
        val fetched = repo.getById("t1")
        assertTrue(fetched?.isDeleted == true)
        // getAll filters deleted
        assertEquals(0, repo.getAll().size)
    }

    fun `sync merges remote changes`() = runCurrent {
        val sync = FakeSyncService()
        val repo = InMemoryTaskRepository(sync)
        repo.create(Task(id = "local", title = "Local"))
        sync.remoteTasks.add(Task(id = "remote", title = "Remote", modifiedAt = java.time.Instant.now().plusSeconds(60)))
        repo.sync()
        val all = repo.getAll()
        assertEquals(2, all.size)
    }

    fun `sync keeps newer local over stale remote`() = runCurrent {
        val sync = FakeSyncService()
        val repo = InMemoryTaskRepository(sync)
        val local = Task(id = "t1", title = "Local v2", modifiedAt = java.time.Instant.now().plusSeconds(120))
        repo.create(local)
        sync.remoteTasks.add(Task(id = "t1", title = "Remote v1", modifiedAt = java.time.Instant.now().minusSeconds(60)))
        repo.sync()
        assertEquals("Local v2", repo.getById("t1")?.title)
    }

    fun `fake sync failure propagates`() = runCurrent {
        val sync = FakeSyncService(shouldFail = true)
        val repo = InMemoryTaskRepository(sync)
        assertFailsWith<IllegalStateException> {
            repo.sync()
        }
    }
}
