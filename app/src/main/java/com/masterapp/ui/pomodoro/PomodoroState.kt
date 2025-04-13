package com.masterapp.ui.pomodoro

enum class PomodoroState {
    IDLE,      // 空闲状态，尚未开始
    RUNNING,   // 运行中
    PAUSED,    // 已暂停
    FINISHED   // 已完成
}