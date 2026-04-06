package com.autodiag.ai.data.model

/**
 * Профиль двигателя на основе анализа данных
 * Содержит средние значения параметров за период сбора
 */
data class EngineProfile(
    val avgRpm: Float,                  // Средние обороты
    val avgLoad: Float,                 // Средняя нагрузка
    val avgTemp: Float,                 // Средняя температура
    val avgConsumption: Float           // Средний расход топлива
)
