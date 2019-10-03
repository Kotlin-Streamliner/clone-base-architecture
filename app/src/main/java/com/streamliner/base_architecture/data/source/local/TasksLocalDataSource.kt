package com.streamliner.base_architecture.data.source.local

import com.streamliner.base_architecture.data.Result.Success
import com.streamliner.base_architecture.data.Result.Error
import com.streamliner.base_architecture.data.Result
import com.streamliner.base_architecture.data.Task
import com.streamliner.base_architecture.data.source.TasksDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception


/**
 * Concrete implementation of a data source as a db.
 */
class TasksLocalDataSource internal constructor(
    private val tasksDao: TasksDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): TasksDataSource {

    override suspend fun getTasks(): Result<List<Task>> {
        return withContext(ioDispatcher) {
            return@withContext try {
                Success(tasksDao.getTasks())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        return withContext(ioDispatcher) {
            try {
                val task = tasksDao.getTaskById(taskId)
                if (task != null) {
                    return@withContext Success(task)
                } else {
                    return@withContext Error(Exception("Task not found!"))
                }
            } catch (e : Exception) {
                return@withContext Error(e)
            }
        }
    }

    override suspend fun saveTask(task: Task) {
        withContext(ioDispatcher) {
            tasksDao.insertTask(task)
        }
    }

    override suspend fun completeTask(task: Task) {
        withContext(ioDispatcher) {
            tasksDao.updateCompleted(task.id, true)
        }
    }

    override suspend fun completeTask(taskId: String) {
        withContext(ioDispatcher) {
            tasksDao.updateCompleted(taskId, true)
        }
    }

    override suspend fun activateTask(task: Task) {
        withContext(ioDispatcher) {
            tasksDao.updateCompleted(task.id, false)
        }
    }

    override suspend fun activateTask(taskId: String) {
        withContext(ioDispatcher) {
            tasksDao.updateCompleted(taskId, false)
        }
    }

    override suspend fun clearCompletedTasks() {
        withContext(ioDispatcher) {
            tasksDao.deleteCompletedTasks()
        }
    }

    override suspend fun deleteAllTasks() {
        withContext(ioDispatcher) {
            tasksDao.deleteTasks()
        }
    }

    override suspend fun deleteTask(taskId: String) {
        withContext(ioDispatcher) {
            tasksDao.deleteTaskById(taskId)
        }
    }
}