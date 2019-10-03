package com.streamliner.base_architecture

import com.streamliner.base_architecture.data.source.local.ToDoDatabase

/**
 * A Service Locator for the [TasksRepository]. This is the mock version, with a
 * [FakeTasksRemoteDataSource].
 */
object ServiceLocator {

    private val lock = Any()
    private var database: ToDoDatabase? = null

}