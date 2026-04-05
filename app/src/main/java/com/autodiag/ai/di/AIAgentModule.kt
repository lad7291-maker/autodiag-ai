package com.autodiag.ai.di

import com.autodiag.ai.aiagent.SafeAdaptiveEngineAgent
import org.koin.dsl.module

/**
 * DI модуль для AI агентов
 */
val aiAgentModule = module {
    single { SafeAdaptiveEngineAgent(get()) }
}
