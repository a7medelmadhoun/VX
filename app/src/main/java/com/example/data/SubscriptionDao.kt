package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription_keys LIMIT 1")
    fun getKey(): Flow<SubscriptionKey?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: SubscriptionKey)

    @Query("DELETE FROM subscription_keys")
    suspend fun clearKey()
}
