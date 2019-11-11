package com.streamliner.base_architecture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.streamliner.base_architecture.data.source.TasksRepository
import com.streamliner.base_architecture.tasks.TasksViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory constructor(
    private val tasksRepository: TasksRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return with(modelClass) {
            when {
                isAssignableFrom(TasksViewModel::class.java) -> TasksViewModel(tasksRepository)
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
    }
}