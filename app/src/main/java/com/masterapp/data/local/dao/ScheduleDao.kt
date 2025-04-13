package com.masterapp.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.masterapp.data.local.entity.Schedule
import java.util.Date
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insert(schedule: Schedule): Long
    
    @Update
    suspend fun update(schedule: Schedule)
    
    @Delete
    suspend fun delete(schedule: Schedule)
    
    @Query("SELECT * FROM schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Long): Schedule?
    
    @Query("SELECT * FROM schedules WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime")
    fun getSchedulesForDateRange(startDate: Date, endDate: Date): Flow<List<Schedule>>
    
    @Query("SELECT * FROM schedules WHERE startTime >= :today ORDER BY startTime")
    fun getUpcomingSchedules(today: Date): Flow<List<Schedule>>
    
    @Query("SELECT * FROM schedules ORDER BY startTime DESC")
    fun getAllSchedules(): Flow<List<Schedule>>
    
    @Query("SELECT * FROM schedules WHERE isCompleted = :isCompleted ORDER BY startTime")
    fun getSchedulesByCompletionStatus(isCompleted: Boolean): Flow<List<Schedule>>
    
    @Query("SELECT * FROM schedules WHERE categoryId = :categoryId ORDER BY startTime")
    fun getSchedulesByCategory(categoryId: Long): Flow<List<Schedule>>
    
    @Query("DELETE FROM schedules")
    suspend fun deleteAllSchedules()
}
