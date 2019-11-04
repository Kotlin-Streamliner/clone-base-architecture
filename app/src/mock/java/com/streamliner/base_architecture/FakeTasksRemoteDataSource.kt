package com.streamliner.base_architecture

import com.streamliner.base_architecture.data.Result
import com.streamliner.base_architecture.data.Task
import com.streamliner.base_architecture.data.source.TasksDataSource

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
object FakeTasksRemoteDataSource : TasksDataSource {

    private var TASKS_SERVICE_DATA: LinkedHashMap<String, Task> = LinkedHashMap()



    override suspend fun getTasks(): Result<List<Task>> {
        return Result.Success(TASKS_SERVICE_DATA.values.toList())
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        TASKS_SERVICE_DATA[taskId]?.let {
            return Result.Success(it)
        }
        return Result.Error(Exception("Could not find task"))
    }

    override suspend fun saveTask(task: Task) {
        TASKS_SERVICE_DATA[task.id] = task
    }

    override suspend fun completeTask(task: Task) {
        TASKS_SERVICE_DATA[task.id] = Task(task.title, task.description, true, task.id)
    }

    override suspend fun completeTask(taskId: String) {
        // Not required for the remote data source.
    }

    override suspend fun activateTask(task: Task) {
        TASKS_SERVICE_DATA[task.id] = Task(task.title, task.description, false, task.id)
    }

    override suspend fun activateTask(taskId: String) {
        // Not required for the remote data source.
    }

    override suspend fun clearCompletedTasks() {
        TASKS_SERVICE_DATA.filterValues { it.isActive } as LinkedHashMap<String, Task>
    }

    override suspend fun deleteAllTasks() {
        TASKS_SERVICE_DATA.clear()
    }

    override suspend fun deleteTask(taskId: String) {
        TASKS_SERVICE_DATA.remove(taskId)
    }
}