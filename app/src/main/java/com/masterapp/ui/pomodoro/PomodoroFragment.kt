package com.masterapp.ui.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.masterapp.R
import com.masterapp.databinding.FragmentPomodoroBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PomodoroViewModel by viewModels()
    
    private var countDownTimer: CountDownTimer? = null
    private var selectedTaskId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTaskSpinner()
        setupButtons()
        observeViewModel()
    }

    private fun setupTaskSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingTasks.collectLatest { tasks ->
                val taskNames = mutableListOf("无关联任务")
                taskNames.addAll(tasks.map { it.title })
                
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    taskNames
                )
                
                binding.spinnerTask.adapter = adapter
                binding.spinnerTask.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedTaskId = if (position == 0) null else tasks[position - 1].id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedTaskId = null
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        binding.buttonStart.setOnClickListener {
            viewModel.startTimer(selectedTaskId)
        }
        
        binding.buttonPause.setOnClickListener {
            viewModel.pauseTimer()
        }
        
        binding.buttonResume.setOnClickListener {
            viewModel.resumeTimer()
        }
        
        binding.buttonStop.setOnClickListener {
            viewModel.stopTimer()
        }
        
        updateButtonStates(PomodoroState.IDLE)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.remainingTime.collectLatest { timeInMillis ->
                updateTimerDisplay(timeInMillis)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timerState.collectLatest { state ->
                updateButtonStates(state)
                updateProgressBar(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isWorkSession.collectLatest { isWork ->
                binding.textViewSessionType.text = if (isWork) 
                    getString(R.string.work_time) 
                else 
                    getString(R.string.break_time)
                
                binding.progressCircular.setIndicatorColor(
                    requireContext().getColor(
                        if (isWork) R.color.color_work else R.color.color_break
                    )
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sessionCount.collectLatest { count ->
                binding.textViewSessionCount.text = getString(R.string.pomodoro_count) + ": $count"
            }
        }
    }

    private fun updateTimerDisplay(timeInMillis: Long) {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        binding.textViewTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateButtonStates(state: PomodoroState) {
        when (state) {
            PomodoroState.IDLE -> {
                binding.buttonStart.visibility = View.VISIBLE
                binding.buttonPause.visibility = View.GONE
                binding.buttonResume.visibility = View.GONE
                binding.buttonStop.visibility = View.GONE
                binding.spinnerTask.isEnabled = true
            }
            PomodoroState.RUNNING -> {
                binding.buttonStart.visibility = View.GONE
                binding.buttonPause.visibility = View.VISIBLE
                binding.buttonResume.visibility = View.GONE
                binding.buttonStop.visibility = View.VISIBLE
                binding.spinnerTask.isEnabled = false
            }
            PomodoroState.PAUSED -> {
                binding.buttonStart.visibility = View.GONE
                binding.buttonPause.visibility = View.GONE
                binding.buttonResume.visibility = View.VISIBLE
                binding.buttonStop.visibility = View.VISIBLE
                binding.spinnerTask.isEnabled = false
            }
            PomodoroState.FINISHED -> {
                binding.buttonStart.visibility = View.VISIBLE
                binding.buttonPause.visibility = View.GONE
                binding.buttonResume.visibility = View.GONE
                binding.buttonStop.visibility = View.GONE
                binding.spinnerTask.isEnabled = true
            }
        }
    }

    private fun updateProgressBar(state: PomodoroState) {
        binding.progressCircular.progress = when (state) {
            PomodoroState.IDLE -> 0
            PomodoroState.FINISHED -> 100
            else -> {
                val totalTime = if (viewModel.isWorkSession.value) 
                    viewModel.workDurationMinutes * 60 * 1000L 
                else 
                    viewModel.breakDurationMinutes * 60 * 1000L
                
                val remainingTime = viewModel.remainingTime.value
                val elapsedTime = totalTime - remainingTime
                
                (elapsedTime * 100 / totalTime).toInt()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pomodoro, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pomodoro_settings -> {
                findNavController().navigate(R.id.action_pomodoro_to_pomodoroSettings)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}