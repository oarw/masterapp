package com.masterapp.data.local.dao

import androidx.room.*
import com.masterapp.data.local.entity.StatisticsEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface StatisticsDao {
    @Insert
    suspend fun insert(statisticsEntry: StatisticsEntry): Long
    
    @Update
    suspend fun update(statisticsEntry: StatisticsEntry)
    
    @Delete
    suspend fun delete(statisticsEntry: StatisticsEntry)
    
    @Query("SELECT * FROM statistics WHERE id = :entryId")
    suspend fun getStatisticsEntryById(entryId: Long): StatisticsEntry?
    
    @Query("SELECT * FROM statistics WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getStatisticsForDateRange(startDate: Date, endDate: Date): Flow<List<StatisticsEntry>>
    
    @Query("SELECT * FROM statistics ORDER BY date DESC LIMIT :limit")
    fun getRecentStatistics(limit: Int): Flow<List<StatisticsEntry>>
    
    @Query("SELECT AVG(completedTasksCount) FROM statistics WHERE date BETWEEN :startDate AND :endDate")
    fun getAverageCompletedTasksForDateRange(startDate: Date, endDate: Date): Flow<Float?>
    
    @Query("SELECT AVG(productivityScore) FROM statistics WHERE date BETWEEN :startDate AND :endDate")
    fun getAverageProductivityScoreForDateRange(startDate: Date, endDate: Date): Flow<Float?>
    
    @Query("DELETE FROM statistics")
    suspend fun deleteAllStatistics()
}