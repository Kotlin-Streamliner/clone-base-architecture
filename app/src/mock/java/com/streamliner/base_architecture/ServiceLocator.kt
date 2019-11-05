package com.streamliner.base_architecture

import android.content.Context
import androidx.room.Room
import com.streamliner.base_architecture.data.FakeTasksRemoteDataSource
import com.streamliner.base_architecture.data.source.DefaultTasksRepository
import com.streamliner.base_architecture.data.source.TasksDataSource
import com.streamliner.base_architecture.data.source.TasksRepository
import com.streamliner.base_architecture.data.source.local.TasksLocalDataSource
import com.streamliner.base_architecture.data.source.local.ToDoDatabase
import kotlinx.coroutines.runBlocking

/**
 * A Service Locator for the [TasksRepository]. This is the mock version, with a
 * [FakeTasksRemoteDataSource].
 */
object ServiceLocator {

    private val lock = Any()
    private var database: ToDoDatabase? = null

    @Volatile
    var tasksRepository: TasksRepository? = null

    fun provideTasksRepository(context: Context): TasksRepository {
        synchronized(this) {
            return tasksRepository ?: tasksRepository ?: createTasksRepository(context)
        }
    }

    private fun createTasksRepository(context: Context): TasksRepository {
        return DefaultTasksRepository(FakeTasksRemoteDataSource, createTasksLocalDataSource(context))
    }

    private fun createTasksLocalDataSource(context: Context) :TasksDataSource {
        val database = database ?: createDataBase(context)
        return TasksLocalDataSource(database.taskDao())
    }

    private fun createDataBase(context: Context): ToDoDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java, "Tasks.db"
        ).build()
        database = result
        return result
    }

    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                FakeTasksRemoteDataSource.deleteAllTasks()
            }
            // clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            tasksRepository = null
        }
    }
}