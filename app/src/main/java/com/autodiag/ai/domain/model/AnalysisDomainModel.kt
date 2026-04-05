package com.autodiag.ai.domain.model

/**
 * Domain модель анализа стиля вождения
 */
data class DrivingAnalysisDomainModel(
    val status: AnalysisStatusDomain = AnalysisStatusDomain.IDLE,
    val profile: DriverProfileDomain = DriverProfileDomain.UNKNOWN,
    val drivingStyle: DrivingStyleDomain = DrivingStyleDomain.BALANCED,
    val engineProfile: EngineProfileDomain = EngineProfileDomain(),
    val samplesCount: Int = 0,
    val averageSpeed: Int = 0,
    val averageRpm: Int = 0,
    val progressPercent: Int = 0,
    val targetDistanceKm: Int = 10,
    val message: String = "",
    val recommendations: EngineTuneRecommendationDomain? = null,
    val safetyNotes: List<String> = emptyList()
) {
    /**
     * Проверка, активен ли сбор данных
     */
    val isCollecting: Boolean
        get() = status == AnalysisStatusDomain.COLLECTING
    
    /**
     * Проверка завершенности анализа
     */
    val isCompleted: Boolean
        get() = status == AnalysisStatusDomain.COMPLETED
    
    /**
     * Проверка наличия достаточных данных
     */
    val hasSufficientData: Boolean
        get() = status != AnalysisStatusDomain.INSUFFICIENT_DATA
}

enum class AnalysisStatusDomain {
    IDLE,               // Ожидание
    COLLECTING,         // Сбор данных
    INSUFFICIENT_DATA,  // Недостаточно данных
    COMPLETED           // Анализ завершен
}

enum class DriverProfileDomain {
    UNKNOWN,
    ECONOMICAL,
    DYNAMIC,
    HIGHWAY,
    URBAN,
    BALANCED
}

enum class DrivingStyleDomain {
    ECONOMICAL,
    BALANCED,
    SPORTY,
    AGGRESSIVE
}

data class EngineProfileDomain(
    val avgRpm: Float = 0f,
    val avgLoad: Float = 0f,
    val avgTemp: Float = 0f,
    val avgConsumption: Float = 0f
)

/**
 * Domain модель рекомендации по настройке двигателя
 */
data class EngineTuneRecommendationDomain(
    val profile: DriverProfileDomain,
    val description: String,
    val ignitionTimingOffset: Float,
    val fuelMixtureBias: Float,
    val reasoning: List<String>,
    val safetyNotes: List<String>
) {
    companion object {
        // Безопасные диапазоны корректировок
        const val MAX_IGNITION_TIMING_OFFSET = 2f    // ±2° УОЗ
        const val MAX_FUEL_MIXTURE_BIAS = 5f          // ±5% смесь
    }
    
    /**
     * Проверка, находятся ли корректировки в безопасных пределах
     */
    fun isWithinSafeLimits(): Boolean {
        return kotlin.math.abs(ignitionTimingOffset) <= MAX_IGNITION_TIMING_OFFSET &&
               kotlin.math.abs(fuelMixtureBias) <= MAX_FUEL_MIXTURE_BIAS
    }
    
    /**
     * Получение нормализованных значений (в пределах безопасности)
     */
    fun normalized(): EngineTuneRecommendationDomain {
        return copy(
            ignitionTimingOffset = ignitionTimingOffset.coerceIn(
                -MAX_IGNITION_TIMING_OFFSET, 
                MAX_IGNITION_TIMING_OFFSET
            ),
            fuelMixtureBias = fuelMixtureBias.coerceIn(
                -MAX_FUEL_MIXTURE_BIAS, 
                MAX_FUEL_MIXTURE_BIAS
            )
        )
    }
}

/**
 * Domain модель голосового предупреждения
 */
data class VoiceAlertDomain(
    val priority: AlertPriorityDomain,
    val message: String,
    val type: AlertTypeDomain,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AlertPriorityDomain {
    INFO,
    WARNING,
    CRITICAL
}

enum class AlertTypeDomain {
    TEMPERATURE,
    KNOCK,
    FUEL_MIXTURE,
    OIL_PRESSURE
}

/**
 * Domain модель резервной копии настроек
 */
data class SettingsBackupDomain(
    val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val vehicleVin: String,
    val originalIgnitionTiming: Float,
    val originalFuelMixture: Float,
    val notes: String = ""
) {
    /**
     * Форматированная дата создания
     */
    val formattedDate: String
        get() = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(createdAt))
}
