package com.autodiag.ai.aiagent

/**
 * Рекомендация по настройке двигателя
 */
data class EngineTuneRecommendation(
    val profile: DriverProfile,
    val description: String,
    val ignitionTimingOffset: Float,    // Коррекция УОЗ (±2°)
    val fuelMixtureBias: Float,         // Смещение смеси (±5%)
    val reasoning: List<String>,        // Обоснование
    val safetyNotes: List<String>       // Примечания по безопасности
)
