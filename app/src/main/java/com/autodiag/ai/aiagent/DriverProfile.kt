package com.autodiag.ai.aiagent

/**
 * Профиль водителя для анализа стиля вождения
 */
enum class DriverProfile {
    UNKNOWN,        // Не определен
    ECONOMICAL,     // Спокойный, экономит
    DYNAMIC,        // Агрессивный, динамичный
    HIGHWAY,        // Трасса
    URBAN,          // Город
    BALANCED        // Сбалансированный
}
