package com.autodiag.ai.di

import android.content.Context
import com.autodiag.ai.data.local.preferences.UserPreferences
import com.autodiag.ai.data.repository.BluetoothRepository
import com.autodiag.ai.data.repository.DiagnosisRepository
import com.autodiag.ai.data.repository.VehicleRepository
import com.autodiag.ai.services.ObdConnectionManager
import com.autodiag.ai.utils.ai.DiagnosisAI
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { provideUserPreferences(androidContext()) }
    single { ObdConnectionManager(androidContext()) }
    single { DiagnosisAI(androidContext()) }
    single { BluetoothRepository(androidContext(), get()) }
    single { DiagnosisRepository(get(), get(), get()) }
    single { VehicleRepository(get()) }
}

private fun provideUserPreferences(context: Context): UserPreferences {
    return UserPreferences(context)
}
