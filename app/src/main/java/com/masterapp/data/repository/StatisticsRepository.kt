package com.masterapp.data.repository

import com.masterapp.data.local.dao.StatisticsDao
import com.masterapp.data.local.entity.StatisticsEntry
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class StatisticsRepository @Inject constructor(
    private val statisticsDao: StatisticsDao
) {
    fun getStatisticsForDateRange(startDate: Date, endDate: Date): Flow<List<StatisticsEntry>> =
        statisticsDao.getStatisticsForDateRange(startDate, endDate)
    
    fun getRecentStatistics(limit: Int): Flow<List<StatisticsEntry>> =
        statisticsDao.getRecentStatistics(limit)
    
    fun getWeeklyStatistics(): Flow<List<StatisticsEntry>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time
        
        return statisticsDao.getStatisticsForDateRange(startDate, endDate)
    }
    
    fun getMonthlyStatistics(): Flow<List<StatisticsEntry>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        calendar.add(Calendar.MONTH, -1)
        val startDate = calendar.time
        
        return statisticsDao.getStatisticsForDateRange(startDate, endDate)
    }
    
    fun getAverageCompletedTasksForWeek(): Flow<Float?> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time
        
        return statisticsDao.getAverageCompletedTasksForDateRange(startDate, endDate)
    }
    
    fun getAverageProductivityScoreForWeek(): Flow<Float?> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time
        
        return statisticsDao.getAverageProductivityScoreForDateRange(startDate, endDate)
    }
    
    suspend fun getStatisticsEntryById(entryId: Long): StatisticsEntry? =
        statisticsDao.getStatisticsEntryById(entryId)
    
    suspend fun insertStatisticsEntry(statisticsEntry: StatisticsEntry): Long =
        statisticsDao.insert(statisticsEntry)
    
    suspend fun updateStatisticsEntry(statisticsEntry: StatisticsEntry) =
        statisticsDao.update(statisticsEntry)
    
    suspend fun deleteStatisticsEntry(statisticsEntry: StatisticsEntry) =
        statisticsDao.delete(statisticsEntry)
    
    suspend fun deleteAllStatistics() =
        statisticsDao.deleteAllStatistics()
}