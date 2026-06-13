package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "daily_stats")
data class DailyStats(
    @PrimaryKey val date: String, // format: YYYY-MM-DD
    val successCount: Int = 0
)

@Dao
interface StatsDao {
    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun getRecentStats(): Flow<List<DailyStats>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: DailyStats)

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getStatsByDate(date: String): DailyStats?

    @Query("UPDATE daily_stats SET successCount = successCount + 1 WHERE date = :date")
    suspend fun incrementSuccess(date: String)
}
