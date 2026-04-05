package com.autodiag.ai.di

import android.content.Context
import androidx.room.Room
import com.autodiag.ai.data.local.AppDatabase
import com.autodiag.ai.data.local.AnalysisDao
import com.autodiag.ai.data.local.database.AutoDiagDatabase
import com.autodiag.ai.data.local.database.dao.DtcCodeDao
import com.autodiag.ai.data.local.database.dao.FaultDatabaseDao
import com.autodiag.ai.data.local.database.dao.VehicleDao
import com.autodiag.ai.data.local.database.dao.DiagnosisHistoryDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { provideDatabase(androidContext()) }
    single { provideAppDatabase(androidContext()) }
    single { provideDtcCodeDao(get()) }
    single { provideFaultDatabaseDao(get()) }
    single { provideVehicleDao(get()) }
    single { provideDiagnosisHistoryDao(get()) }
    single { provideAnalysisDao(get()) }
}

private fun provideDatabase(context: Context): AutoDiagDatabase {
    return Room.databaseBuilder(
        context,
        AutoDiagDatabase::class.java,
        "autodiag_database"
    )
        .createFromAsset("database/autodiag_db.db")
        .fallbackToDestructiveMigration()
        .build()
}

private fun provideAppDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "analysis_database"
    )
        .fallbackToDestructiveMigration()
        .build()
}

private fun provideDtcCodeDao(database: AutoDiagDatabase): DtcCodeDao {
    return database.dtcCodeDao()
}

private fun provideFaultDatabaseDao(database: AutoDiagDatabase): FaultDatabaseDao {
    return database.faultDatabaseDao()
}

private fun provideVehicleDao(database: AutoDiagDatabase): VehicleDao {
    return database.vehicleDao()
}

private fun provideDiagnosisHistoryDao(database: AutoDiagDatabase): DiagnosisHistoryDao {
    return database.diagnosisHistoryDao()
}

private fun provideAnalysisDao(database: AppDatabase): AnalysisDao {
    return database.analysisDao()
}
