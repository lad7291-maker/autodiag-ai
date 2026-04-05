package com.autodiag.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.autodiag.ai.data.local.database.converter.Converters
import com.autodiag.ai.data.local.database.dao.DiagnosisHistoryDao
import com.autodiag.ai.data.local.database.dao.DtcCodeDao
import com.autodiag.ai.data.local.database.dao.FaultDatabaseDao
import com.autodiag.ai.data.local.database.dao.VehicleDao
import com.autodiag.ai.data.model.DiagnosisHistory
import com.autodiag.ai.data.model.DtcCode
import com.autodiag.ai.data.model.FaultEntry
import com.autodiag.ai.data.model.Vehicle

@Database(
    entities = [
        DtcCode::class,
        FaultEntry::class,
        Vehicle::class,
        DiagnosisHistory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AutoDiagDatabase : RoomDatabase() {
    abstract fun dtcCodeDao(): DtcCodeDao
    abstract fun faultDatabaseDao(): FaultDatabaseDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun diagnosisHistoryDao(): DiagnosisHistoryDao
}
