package com.autodiag.ai.di

import com.autodiag.ai.domain.usecase.AnalysisUseCase
import com.autodiag.ai.domain.usecase.DiagnosisUseCase
import com.autodiag.ai.domain.usecase.VehicleUseCase
import org.koin.dsl.module

/**
 * DI модуль для Domain слоя
 * Регистрирует UseCase классы
 */
val domainModule = module {
    // UseCases
    single { VehicleUseCase(get()) }
    single { DiagnosisUseCase(get()) }
    single { AnalysisUseCase(get()) }
}
