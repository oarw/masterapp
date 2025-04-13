package com.masterapp.ui.task

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.masterapp.R
import com.masterapp.data.local.entity.Category
import com.masterapp.data.local.entity.SubTask
import com.masterapp.data.local.entity.Task
import com.masterapp.databinding.FragmentTaskDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskDetailViewModel by viewModels()
    private lateinit var subtaskAdapter: SubTaskAdapter
    private var selectedDueDate: Date? = null
    private var selectedCategoryId: Long? = null

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupSubtaskRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupSubtaskRecyclerView() {
        subtaskAdapter = SubTaskAdapter(
            onCompletedChanged = { subtask ->
                viewModel.toggleSubtaskCompleted(subtask)
            },
            onDeleteClick = { subtask ->
                viewModel.deleteSubtask(subtask)
            }
        )

        binding.recyclerViewSubtasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = subtaskAdapter
        }
    }

    private fun setupListeners() {
        binding.layoutDueDate.setEndIconOnClickListener {
            showDatePicker()
        }

        binding.editTextDueDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonAddSubtask.setOnClickListener {
            val subtaskTitle = binding.editTextSubtask.text.toString().trim()
            if (subtaskTitle.isNotEmpty()) {
                viewModel.addSubtask(subtaskTitle)
                binding.editTextSubtask.text?.clear()
            }
        }

        binding.buttonSave.setOnClickListener {
            saveTask()
        }

        binding.buttonDelete.setOnClickListener {
            viewModel.deleteTask()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.taskState.collectLatest { state ->
                when (state) {
                    is TaskDetailState.Loading -> showLoading(true)
                    
                    is TaskDetailState.Success -> {
                        showLoading(false)
                        populateTaskDetails(state.task)
                    }
                    
                    is TaskDetailState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    
                    is TaskDetailState.Saved -> {
                        findNavController().popBackStack()
                    }
                    
                    is TaskDetailState.Deleted -> {
                        findNavController().popBackStack()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collectLatest { categories ->
                setupCategoryDropdown(categories)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.subtasks.collectLatest { subtasks ->
                subtaskAdapter.submitList(subtasks)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.layoutTitle.isEnabled = !isLoading
        binding.layoutDescription.isEnabled = !isLoading
        binding.layoutDueDate.isEnabled = !isLoading
        binding.radioGroupPriority.isEnabled = !isLoading
        binding.layoutCategory.isEnabled = !isLoading
        binding.layoutEstimatedTime.isEnabled = !isLoading
        binding.buttonSave.isEnabled = !isLoading
        binding.buttonDelete.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun populateTaskDetails(task: Task) {
        binding.toolbar.title = if (task.id == 0L) getString(R.string.add_task) else getString(R.string.task_details)
        
        binding.editTextTitle.setText(task.title)
        binding.editTextDescription.setText(task.description)
        
        task.dueDate?.let {
            selectedDueDate = it
            binding.editTextDueDate.setText(dateFormatter.format(it))
        }
        
        when (task.priority) {
            3 -> binding.radioPriorityHigh.isChecked = true
            2 -> binding.radioPriorityMedium.isChecked = true
            1 -> binding.radioPriorityLow.isChecked = true
        }
        
        selectedCategoryId = task.categoryId
        
        binding.editTextEstimatedTime.setText(task.estimatedTime?.toString() ?: "")
        
        // 仅当编辑现有任务时显示删除按钮
        binding.buttonDelete.visibility = if (task.id == 0L) View.GONE else View.VISIBLE
    }

    private fun setupCategoryDropdown(categories: List<Category>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories.map { it.name }
        )
        
        (binding.dropdownCategory as? AutoCompleteTextView)?.let { autoComplete ->
            autoComplete.setAdapter(adapter)
            autoComplete.setOnItemClickListener { _, _, position, _ ->
                selectedCategoryId = categories[position].id
            }
            
            // 如果有选中的类别，显示相应的名称
            val selectedCategory = categories.find { it.id == selectedCategoryId }
            if (selectedCategory != null) {
                autoComplete.setText(selectedCategory.name, false)
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDueDate != null) {
            calendar.time = selectedDueDate!!
        }
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                selectedDueDate = calendar.time
                binding.editTextDueDate.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTask() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        
        val priority = when {
            binding.radioPriorityHigh.isChecked -> 3
            binding.radioPriorityMedium.isChecked -> 2
            binding.radioPriorityLow.isChecked -> 1
            else -> 0
        }
        
        val estimatedTime = binding.editTextEstimatedTime.text.toString().trim().toIntOrNull()
        
        viewModel.saveTask(
            title = title,
            description = description,
            dueDate = selectedDueDate,
            priority = priority,
            categoryId = selectedCategoryId,
            estimatedTime = estimatedTime
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}