package com.streamliner.base_architecture.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.streamliner.base_architecture.databinding.TasksFragBinding

/**
 * Display a grid of [Task]s. User can choose to view all, active or completed tasks.
 */
class TasksFragment: Fragment() {

    private val viewModel by viewModel<TasksViewModel> { getViewModelFactory() }

    private val args: TasksFragmentArgs by navArgs()

    private lateinit var viewDataBinding : TasksFragBinding

    private lateinit var listAdapter: TasksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = TasksFragBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        setHasOptionsMenu(true)
        return viewDataBinding.root
    }

}