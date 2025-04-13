package com.masterapp.data.local.dao

import androidx.room.*
import com.masterapp.data.local.entity.PomodoroSession
import java.util.Date
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Insert
    suspend fun insert(session: PomodoroSession): Long
    
    @Update
    suspend fun update(session: PomodoroSession)
    
    @Delete
    suspend fun delete(session: PomodoroSession)
    
    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): PomodoroSession?
    
    @Query("SELECT * FROM pomodoro_sessions WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getSessionsForDateRange(startDate: Date, endDate: Date): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getSessionsByTaskId(taskId: Long): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE isCompleted = 1 AND isWorkSession = 1 ORDER BY startTime DESC")
    fun getCompletedWorkSessions(): Flow<List<PomodoroSession>>
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE startTime BETWEEN :startDate AND :endDate AND isCompleted = 1 AND isWorkSession = 1")
    fun getCompletedSessionsCountForDateRange(startDate: Date, endDate: Date): Flow<Int>
    
    @Query("SELECT SUM(duration) FROM pomodoro_sessions WHERE startTime BETWEEN :startDate AND :endDate AND isCompleted = 1 AND isWorkSession = 1")
    fun getTotalWorkTimeForDateRange(startDate: Date, endDate: Date): Flow<Int?>
    
    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAllSessions()
}
