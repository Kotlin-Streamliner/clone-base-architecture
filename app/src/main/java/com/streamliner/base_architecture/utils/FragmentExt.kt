package com.streamliner.base_architecture.utils

import androidx.fragment.app.Fragment
import com.streamliner.base_architecture.TodoApplication
import com.streamliner.base_architecture.ViewModelFactory

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val repository = (requireContext().applicationContext as TodoApplication).taskRepository
    return ViewModelFactory(repository)
}