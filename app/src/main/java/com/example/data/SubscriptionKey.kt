package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscription_keys")
data class SubscriptionKey(
    @PrimaryKey val key: String,
    val isValid: Boolean = true,
    val lastValidated: Long = System.currentTimeMillis()
)
