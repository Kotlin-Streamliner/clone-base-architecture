package com.streamliner.base_architecture.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.streamliner.base_architecture.data.Task

class TasksViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {
    private val items = MutableLiveData<List<Task>>.apply {
        value =  emptyList()
    }
}