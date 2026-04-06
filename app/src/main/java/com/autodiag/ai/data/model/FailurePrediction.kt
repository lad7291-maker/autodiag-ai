package com.autodiag.ai.data.model

/**
 * Предсказание возможной поломки компонента
 */
data class FailurePrediction(
    val component: String,              // Название компонента
    val probability: Float,             // Вероятность поломки (0.0 - 1.0)
    val estimatedMileage: Int,          // Предполагаемый пробег до поломки
    val recommendedAction: String,      // Рекомендуемое действие
    val urgency: Urgency                // Уровень срочности
)
