package com.autodiag.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FaultCodeEntity::class,
        SensorDataEntity::class,
        AnalysisEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun faultCodeDao(): FaultCodeDao
    abstract fun sensorDataDao(): SensorDataDao
    abstract fun analysisDao(): AnalysisDao
}
