package com.masterapp.ui.pomodoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.masterapp.databinding.FragmentPomodoroSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PomodoroSettingsFragment : Fragment() {

    private var _binding: FragmentPomodoroSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PomodoroViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPomodoroSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        populateSettings()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun populateSettings() {
        binding.editTextWorkTime.setText(viewModel.workDurationMinutes.toString())
        binding.editTextBreakTime.setText(viewModel.breakDurationMinutes.toString())
        binding.editTextLongBreak.setText(viewModel.longBreakDurationMinutes.toString())
        binding.editTextSessionsCount.setText(viewModel.sessionsBeforeLongBreak.toString())
    }

    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            saveSettings()
        }
        
        binding.buttonReset.setOnClickListener {
            resetToDefault()
        }
    }

    private fun saveSettings() {
        val workMinutes = binding.editTextWorkTime.text.toString().toIntOrNull()
        val breakMinutes = binding.editTextBreakTime.text.toString().toIntOrNull()
        val longBreakMinutes = binding.editTextLongBreak.text.toString().toIntOrNull()
        val sessionsCount = binding.editTextSessionsCount.text.toString().toIntOrNull()
        
        if (workMinutes == null || breakMinutes == null || longBreakMinutes == null || sessionsCount == null) {
            Toast.makeText(context, "请确保所有字段都填写了有效的数字", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (workMinutes < 1 || breakMinutes < 1 || longBreakMinutes < 1 || sessionsCount < 1) {
            Toast.makeText(context, "所有值必须大于0", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.updateSettings(workMinutes, breakMinutes, longBreakMinutes, sessionsCount)
        findNavController().popBackStack()
    }

    private fun resetToDefault() {
        binding.editTextWorkTime.setText("25")
        binding.editTextBreakTime.setText("5")
        binding.editTextLongBreak.setText("15")
        binding.editTextSessionsCount.setText("4")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}