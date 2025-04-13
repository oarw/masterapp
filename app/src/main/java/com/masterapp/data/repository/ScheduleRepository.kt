package com.masterapp.data.repository

import com.masterapp.data.local.dao.ScheduleDao
import com.masterapp.data.local.entity.Schedule
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao
) {
    fun getSchedulesForDateRange(startDate: Date, endDate: Date): Flow<List<Schedule>> =
        scheduleDao.getSchedulesForDateRange(startDate, endDate)
    
    fun getUpcomingSchedules(today: Date = Date()): Flow<List<Schedule>> =
        scheduleDao.getUpcomingSchedules(today)
    
    fun getAllSchedules(): Flow<List<Schedule>> =
        scheduleDao.getAllSchedules()
    
    fun getSchedulesByCompletionStatus(isCompleted: Boolean): Flow<List<Schedule>> =
        scheduleDao.getSchedulesByCompletionStatus(isCompleted)
    
    fun getSchedulesByCategory(categoryId: Long): Flow<List<Schedule>> =
        scheduleDao.getSchedulesByCategory(categoryId)
    
    suspend fun getScheduleById(scheduleId: Long): Schedule? =
        scheduleDao.getScheduleById(scheduleId)
    
    suspend fun insertSchedule(schedule: Schedule): Long =
        scheduleDao.insert(schedule)
    
    suspend fun updateSchedule(schedule: Schedule) =
        scheduleDao.update(schedule)
    
    suspend fun deleteSchedule(schedule: Schedule) =
        scheduleDao.delete(schedule)
    
    suspend fun deleteAllSchedules() =
        scheduleDao.deleteAllSchedules()
}
