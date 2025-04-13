package com.masterapp.data.repository

import com.masterapp.data.local.dao.PomodoroSessionDao
import com.masterapp.data.local.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class PomodoroRepository @Inject constructor(
    private val pomodoroSessionDao: PomodoroSessionDao
) {
    fun getSessionsForDateRange(startDate: Date, endDate: Date): Flow<List<PomodoroSession>> =
        pomodoroSessionDao.getSessionsForDateRange(startDate, endDate)
    
    fun getSessionsByTaskId(taskId: Long): Flow<List<PomodoroSession>> =
        pomodoroSessionDao.getSessionsByTaskId(taskId)
    
    fun getCompletedWorkSessions(): Flow<List<PomodoroSession>> =
        pomodoroSessionDao.getCompletedWorkSessions()
    
    fun getCompletedSessionsCountForDateRange(startDate: Date, endDate: Date): Flow<Int> =
        pomodoroSessionDao.getCompletedSessionsCountForDateRange(startDate, endDate)
    
    fun getTotalWorkTimeForDateRange(startDate: Date, endDate: Date): Flow<Int?> =
        pomodoroSessionDao.getTotalWorkTimeForDateRange(startDate, endDate)
    
    suspend fun getSessionById(sessionId: Long): PomodoroSession? =
        pomodoroSessionDao.getSessionById(sessionId)
    
    suspend fun insertSession(session: PomodoroSession): Long =
        pomodoroSessionDao.insert(session)
    
    suspend fun updateSession(session: PomodoroSession) =
        pomodoroSessionDao.update(session)
    
    suspend fun deleteSession(session: PomodoroSession) =
        pomodoroSessionDao.delete(session)
    
    suspend fun deleteAllSessions() =
        pomodoroSessionDao.deleteAllSessions()
}
