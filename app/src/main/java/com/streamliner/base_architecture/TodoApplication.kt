package com.streamliner.base_architecture

import android.app.Application
import com.streamliner.base_architecture.data.source.TasksRepository
import timber.log.Timber

/**
 * An application that lazily provides a repository. Note that this Service Locator pattern is
 * used to simplify the sample. Consider a Dependency Injection framework.
 *
 * Also, sets up Timber in the DEBUG BuildConfig. Read Timber's documentation for production setups.
 */
class TodoApplication : Application() {

    // Depends on the flavor,
    val taskRepository: TasksRepository
        get() = ServiceLocator.provideTaskRepository(this)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}