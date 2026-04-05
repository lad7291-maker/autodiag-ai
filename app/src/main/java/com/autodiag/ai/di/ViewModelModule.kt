package com.autodiag.ai.di

import com.autodiag.ai.presentation.screens.onboarding.OnboardingViewModel
import com.autodiag.ai.ui.viewmodel.BluetoothViewModel
import com.autodiag.ai.ui.viewmodel.DiagnosisViewModel
import com.autodiag.ai.ui.viewmodel.HomeViewModel
import com.autodiag.ai.ui.viewmodel.LiveDataViewModel
import com.autodiag.ai.ui.viewmodel.SettingsViewModel
import com.autodiag.ai.ui.viewmodel.VehicleViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { BluetoothViewModel(get()) }
    viewModel { DiagnosisViewModel(get(), get()) }
    viewModel { LiveDataViewModel(get()) }
    viewModel { VehicleViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }
}
