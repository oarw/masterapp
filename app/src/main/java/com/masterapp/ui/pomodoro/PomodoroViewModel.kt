package com.masterapp.ui.pomodoro

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterapp.data.local.entity.PomodoroSession
import com.masterapp.data.repository.PomodoroRepository
import com.masterapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    // 番茄钟默认参数
    var workDurationMinutes: Int = 25   // 工作时间（分钟）
    var breakDurationMinutes: Int = 5   // 休息时间（分钟）
    var longBreakDurationMinutes: Int = 15  // 长休息时间（分钟）
    var sessionsBeforeLongBreak: Int = 4    // 长休息前的工作周期数

    // 番茄钟状态
    private val _timerState = MutableStateFlow(PomodoroState.IDLE)
    val timerState = _timerState.asStateFlow()

    // 当前是否是工作时段
    private val _isWorkSession = MutableStateFlow(true)
    val isWorkSession = _isWorkSession.asStateFlow()

    // 剩余时间（毫秒）
    private val _remainingTime = MutableStateFlow(workDurationMinutes * 60 * 1000L)
    val remainingTime = _remainingTime.asStateFlow()

    // 完成的番茄钟计数
    private val _sessionCount = MutableStateFlow(0)
    val sessionCount = _sessionCount.asStateFlow()

    // 倒计时器
    private var countDownTimer: CountDownTimer? = null

    // 番茄钟统计数据
    private val _todayStats = MutableStateFlow(0)
    val todayStats = _todayStats.asStateFlow()

    // 当前会话
    private var currentSession: PomodoroSession? = null
    private var pauseTimeStamp: Long = 0

    // 待处理任务列表
    val pendingTasks = taskRepository.getTasksByCompletionStatus(false).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadSettings()
        loadTodayStats()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            pomodoroRepository.getPomodoroSettings().collect { settings ->
                settings?.let {
                    workDurationMinutes = it.workDurationMinutes
                    breakDurationMinutes = it.breakDurationMinutes
                    longBreakDurationMinutes = it.longBreakDurationMinutes
                    sessionsBeforeLongBreak = it.sessionsBeforeLongBreak

                    // 如果当前是空闲状态，更新剩余时间
                    if (_timerState.value == PomodoroState.IDLE) {
                        _remainingTime.value = if (_isWorkSession.value) {
                            workDurationMinutes * 60 * 1000L
                        } else {
                            breakDurationMinutes * 60 * 1000L
                        }
                    }
                }
            }
        }
    }

    private fun loadTodayStats() {
        viewModelScope.launch {
            pomodoroRepository.getTodaySessionsCount().collect {
                _sessionCount.value = it
            }
        }
    }

    fun startTimer(taskId: Long?) {
        // 如果倒计时已经运行，先取消
        countDownTimer?.cancel()

        // 设定剩余时间
        val duration = if (_isWorkSession.value) {
            workDurationMinutes * 60 * 1000L
        } else {
            if (_sessionCount.value > 0 && _sessionCount.value % sessionsBeforeLongBreak == 0) {
                longBreakDurationMinutes * 60 * 1000L
            } else {
                breakDurationMinutes * 60 * 1000L
            }
        }
        _remainingTime.value = duration

        // 创建新的番茄钟会话
        val now = Date()
        currentSession = PomodoroSession(
            taskId = taskId,
            startTime = now,
            duration = if (_isWorkSession.value) workDurationMinutes else breakDurationMinutes,
            isWorkSession = _isWorkSession.value
        )

        // 保存会话到数据库
        viewModelScope.launch {
            currentSession?.let { session ->
                val sessionId = pomodoroRepository.insertPomodoroSession(session)
                currentSession = session.copy(id = sessionId)
            }
        }

        // 创建倒计时器
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished
            }

            override fun onFinish() {
                completeSession()
            }
        }.start()

        _timerState.value = PomodoroState.RUNNING
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        pauseTimeStamp = System.currentTimeMillis()
        _timerState.value = PomodoroState.PAUSED
    }

    fun resumeTimer() {
        countDownTimer?.cancel()
        
        // 创建新的倒计时器
        countDownTimer = object : CountDownTimer(_remainingTime.value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished
            }

            override fun onFinish() {
                completeSession()
            }
        }.start()

        _timerState.value = PomodoroState.RUNNING
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        
        // 如果有当前会话，标记为未完成
        viewModelScope.launch {
            currentSession?.let { session ->
                val endTime = Date()
                val updated = session.copy(
                    endTime = endTime,
                    completed = false
                )
                pomodoroRepository.updatePomodoroSession(updated)
                currentSession = null
            }
        }
        
        // 重置为初始状态
        _timerState.value = PomodoroState.IDLE
        _remainingTime.value = if (_isWorkSession.value) {
            workDurationMinutes * 60 * 1000L
        } else {
            breakDurationMinutes * 60 * 1000L
        }
    }

    private fun completeSession() {
        viewModelScope.launch {
            // 更新会话状态为已完成
            currentSession?.let { session ->
                val endTime = Date()
                val updated = session.copy(
                    endTime = endTime,
                    completed = true
                )
                pomodoroRepository.updatePomodoroSession(updated)
                currentSession = null
                
                // 如果是工作时段，增加计数
                if (session.isWorkSession) {
                    _sessionCount.value += 1
                }
            }
            
            // 切换工作/休息状态
            _isWorkSession.value = !_isWorkSession.value
            
            // 更新状态为已完成
            _timerState.value = PomodoroState.FINISHED
            
            // 更新剩余时间为下一个时段的时间
            _remainingTime.value = if (_isWorkSession.value) {
                workDurationMinutes * 60 * 1000L
            } else {
                if (_sessionCount.value > 0 && _sessionCount.value % sessionsBeforeLongBreak == 0) {
                    longBreakDurationMinutes * 60 * 1000L
                } else {
                    breakDurationMinutes * 60 * 1000L
                }
            }
        }
    }

    fun updateSettings(
        workMinutes: Int,
        breakMinutes: Int,
        longBreakMinutes: Int,
        sessionsCount: Int
    ) {
        viewModelScope.launch {
            pomodoroRepository.updateSettings(
                workMinutes,
                breakMinutes,
                longBreakMinutes,
                sessionsCount
            )
        }
    }

    override fun onCleared() {
        countDownTimer?.cancel()
        super.onCleared()
    }
}