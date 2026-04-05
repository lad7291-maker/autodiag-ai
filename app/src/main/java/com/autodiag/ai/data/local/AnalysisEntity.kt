package com.autodiag.ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analyses")
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val profile: String,
    val drivingStyle: String,
    val samplesCount: Int,
    val averageSpeed: Int,
    val averageRpm: Int,
    val ignitionTimingOffset: Float,
    val fuelMixtureBias: Float,
    val reasoning: String,
    val safetyNotes: String,
    val isApplied: Boolean
)
