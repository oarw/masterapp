package com.masterapp.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.masterapp.R
import com.masterapp.data.local.entity.Task
import com.masterapp.databinding.FragmentTaskListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onItemClick = { task ->
                val action = TaskListFragmentDirections.actionTaskListToTaskDetail(task.id)
                findNavController().navigate(action)
            },
            onCheckboxClick = { task ->
                viewModel.toggleTaskCompleted(task)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TaskListFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListToTaskDetail(0L)
            findNavController().navigate(action)
        }

        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chip_all -> viewModel.setTaskFilter(TaskFilter.ALL)
                R.id.chip_pending -> viewModel.setTaskFilter(TaskFilter.PENDING)
                R.id.chip_completed -> viewModel.setTaskFilter(TaskFilter.COMPLETED)
                R.id.chip_today -> viewModel.setTaskFilter(TaskFilter.TODAY)
                R.id.chip_important -> viewModel.setTaskFilter(TaskFilter.IMPORTANT)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collectLatest { tasks ->
                adapter.submitList(tasks)
                updateEmptyStateVisibility(tasks)
            }
        }
    }

    private fun updateEmptyStateVisibility(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            binding.recyclerViewTasks.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewTasks.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_task_list, menu)
        
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
        
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_date -> {
                viewModel.setSortOrder(TaskSortOrder.DATE)
                true
            }
            R.id.action_sort_priority -> {
                viewModel.setSortOrder(TaskSortOrder.PRIORITY)
                true
            }
            R.id.action_sort_alphabetical -> {
                viewModel.setSortOrder(TaskSortOrder.ALPHABETICAL)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}