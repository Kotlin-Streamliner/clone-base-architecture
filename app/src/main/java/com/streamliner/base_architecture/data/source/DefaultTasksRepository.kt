package com.streamliner.base_architecture.data.source

import com.streamliner.base_architecture.data.Result
import com.streamliner.base_architecture.data.Task
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Concrete implementation to load tasks from the data sources into  a cache.
 *
 * To simplify the sample, this repository only uses the local data source only if the remote
 * data sources fails. Remote is the source of truth.
 */
class DefaultTasksRepository(
    private val tasksRemoteDataSource: TasksDataSource,
    private val tasksLocalDataSource: TasksDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TasksRepository {

    private var cachedTasks: ConcurrentMap<String, Task>? = null

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> {
        return withContext(ioDispatcher) {
            // Respond immediately with cache if available and not dirty
            if (!forceUpdate) {
                cachedTasks?.let { cachedTasks ->
                    return@withContext Result.Success(cachedTasks.values.sortedBy { it.id })
                }
            }

            val newTasks = fetchTasksFromRemoteOrLocal(forceUpdate)

            // Refresh the cache with the new tasks
            (newTasks as? Result.Success)?.let { refreshCache(it.data)}

            cachedTasks?.values?.let { tasks ->
                return@withContext Result.Success(tasks.sortedBy { it.id })
            }

            (newTasks as? Result.Success<List<Task>>)?.let {
                if (it.data.isEmpty()) {
                    return@withContext Result.Success(it.data)
                }
            }

            return@withContext Result.Error(Exception("Illegal state"))
        }
    }

    private suspend fun fetchTasksFromRemoteOrLocal(forceUpdate: Boolean) : Result<List<Task>> {
        // Remote first
        val remoteTasks = tasksRemoteDataSource.getTasks()
        when(remoteTasks) {
            is Result.Error -> Timber.w("Remote data source fetch failed")
            is Result.Success -> {
                refreshLocalDataSource(remoteTasks.data)
                return remoteTasks
            }
            else -> throw IllegalStateException()
        }

        // Don't read from local if it's forced
        if (forceUpdate) {
            return Result.Error(Exception("Can't force refresh: remote data source is unavailable"))
        }

        // Local if remote fails
        val localTasks = tasksLocalDataSource.getTasks()
        if (localTasks is Result.Success) return localTasks
        return Result.Error(Exception("Error fetching from remote and local"))
    }

    /**
     * Relies on [getTasks] to fetch data and picks the task with the same ID.
     */
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Result<Task> {
        return withContext(ioDispatcher) {
            // Respond immediately with cache if available
            if (!forceUpdate) {
                getTaskWithId(taskId)?.let {
                    return@withContext Result.Success(it)
                }
            }

            val newTask = fetchTaskFromRemoteOrLocal(taskId, forceUpdate)

            // Refresh the cache with new tasks
            (newTask as? Result.Success)?.let { cacheTask(it.data) }

            return@withContext newTask
        }
    }

    private suspend fun fetchTaskFromRemoteOrLocal(taskId: String, forceUpdate: Boolean): Result<Task> {
        // Remote first
        val remoteTask = tasksRemoteDataSource.getTask(taskId)
        when(remoteTask) {
            is Result.Error -> Timber.w("Remote data source fetch failed")
            is Result.Success -> {
                refreshLocalDataSource(remoteTask.data as List<Task>)
                return remoteTask
            }
            else -> throw IllegalStateException()
        }

        // Don't read from local if it's forced
        if (forceUpdate) return Result.Error(Exception("Refresh Failed"))

        // Local if remote fails
        val localTasks = tasksLocalDataSource.getTask(taskId)
        if (localTasks is Result.Success) return localTasks
        return Result.Error(Exception("Error fetching from remote and local"))
    }

    override suspend fun saveTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        cacheAndPerform(task){
            coroutineScope {
                launch { tasksRemoteDataSource.saveTask(it) }
                launch { tasksLocalDataSource.saveTask(it) }
            }
        }
    }

    override suspend fun completeTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        cacheAndPerform(task) {
            it.isCompleted = true
            coroutineScope {
                launch { tasksRemoteDataSource.completeTask(it) }
                launch { tasksLocalDataSource.completeTask(it) }
            }
        }
    }

    override suspend fun completeTask(taskId: String) {
        withContext(ioDispatcher) {
            getTaskWithId(taskId)?.let {
                completeTask(it)
            }
        }
    }

    override suspend fun activateTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        cacheAndPerform(task) {
            it.isCompleted = false
            coroutineScope {
                launch { tasksRemoteDataSource.activateTask(it) }
                launch { tasksLocalDataSource.activateTask(it) }
            }
        }
    }

    override suspend fun activateTask(taskId: String) {
        withContext(ioDispatcher) {
            getTaskWithId(taskId)?.let {
                activateTask(it)
            }
        }
    }

    override suspend fun clearCompletedTasks() {
        coroutineScope {
            launch { tasksRemoteDataSource.clearCompletedTasks() }
            launch { tasksLocalDataSource.clearCompletedTasks() }
        }
        withContext(ioDispatcher) {
            cachedTasks?.entries?.removeAll { it.value.isCompleted }
        }
    }

    override suspend fun deleteAllTasks() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { tasksRemoteDataSource.deleteAllTasks() }
                launch { tasksLocalDataSource.deleteAllTasks() }
            }
        }
        cachedTasks?.clear()
    }

    override suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { tasksRemoteDataSource.deleteTask(taskId) }
            launch { tasksLocalDataSource.deleteTask(taskId) }
        }
        cachedTasks?.remove(taskId)
    }

    private fun refreshCache(tasks: List<Task>) {
        cachedTasks?.clear()
        tasks.sortedBy { it.id }.forEach {
            cacheAndPerform(it) {}
        }
    }

    private suspend fun refreshLocalDataSource(tasks: List<Task>) {
        tasksLocalDataSource.deleteAllTasks()
        for (task in tasks) {
            tasksLocalDataSource.saveTask(task)
        }
    }

    private suspend fun refreshLocalDataSource(task: Task) {
        tasksLocalDataSource.saveTask(task)
    }

    private fun getTaskWithId(id: String) = cachedTasks?.get(id)

    private fun cacheTask(task: Task) : Task {
        val cachedTask = Task(task.title, task.description, task.isCompleted, task.id)
        // Create if it doesn't exist.
        if (cachedTasks == null) {
            cachedTasks = ConcurrentHashMap()
        }
        cachedTasks?.put(cachedTask.id, cachedTask)
        return cachedTask
    }

    private inline fun cacheAndPerform(task: Task, perform: (Task) -> Unit) {
        val cachedTask = cacheTask(task)
        perform(cachedTask)
    }

}