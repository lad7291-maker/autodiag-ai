package com.autodiag.ai.data.model

/**
 * Снимок параметров двигателя для отображения в UI
 * Используется при сборе данных для анализа стиля вождения
 */
data class EngineParametersSnapshot(
    val rpm: Float,                     // Обороты двигателя
    val engineLoad: Float,              // Нагрузка на двигатель (%)
    val coolantTemp: Float,             // Температура охлаждающей жидкости (°C)
    val speed: Float,                   // Скорость (км/ч)
    val throttlePosition: Float         // Положение дроссельной заслонки (%)
)
