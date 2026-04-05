package com.autodiag.ai.domain.model

/**
 * Domain модель истории диагностики
 */
data class DiagnosisHistoryDomainModel(
    val id: Long = 0,
    val vehicleVin: String,
    val diagnosisDate: Long = System.currentTimeMillis(),
    val dtcCodes: List<String>,
    val engineHealthScore: Float,
    val detectedIssues: List<DetectedIssueDomain>,
    val recommendations: List<String>,
    val operatingTips: List<OperatingTipDomain>,
    val isSaved: Boolean = false
) {
    /**
     * Форматированная дата диагностики
     */
    val formattedDate: String
        get() = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(diagnosisDate))
    
    /**
     * Общее количество найденных проблем
     */
    val totalIssues: Int
        get() = detectedIssues.size
    
    /**
     * Проверка наличия критических проблем
     */
    val hasCriticalIssues: Boolean
        get() = detectedIssues.any { it.severity == DtcSeverityDomain.CRITICAL }
    
    /**
     * Оценка состояния двигателя в виде текста
     */
    val healthStatus: String
        get() = when {
            engineHealthScore >= 90 -> "Отличное"
            engineHealthScore >= 70 -> "Хорошее"
            engineHealthScore >= 50 -> "Удовлетворительное"
            engineHealthScore >= 30 -> "Требует внимания"
            else -> "Критическое"
        }
}

data class DetectedIssueDomain(
    val system: String,
    val severity: DtcSeverityDomain,
    val description: String,
    val recommendedAction: String
)

data class OperatingTipDomain(
    val category: TipCategoryDomain,
    val title: String,
    val description: String,
    val priority: TipPriorityDomain
)

enum class DtcSeverityDomain {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class TipCategoryDomain {
    DRIVING_STYLE,
    MAINTENANCE,
    FUEL,
    ENGINE_CARE,
    SEASONAL,
    SAFETY
}

enum class TipPriorityDomain {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Domain модель DTC кода
 */
data class DtcCodeDomainModel(
    val code: String,
    val description: String,
    val category: DtcCategoryDomain,
    val severity: DtcSeverityDomain,
    val symptoms: List<String>,
    val possibleCauses: List<String>,
    val recommendedActions: List<String>,
    val estimatedRepairCost: RepairCostDomain? = null,
    val difficulty: RepairDifficultyDomain = RepairDifficultyDomain.MEDIUM
)

enum class DtcCategoryDomain {
    ENGINE,
    TRANSMISSION,
    ABS,
    AIRBAG,
    CLIMATE,
    ELECTRICAL,
    FUEL,
    EMISSIONS,
    COMMUNICATION
}

data class RepairCostDomain(
    val minCost: Int,
    val maxCost: Int,
    val currency: String,
    val notes: String
)

enum class RepairDifficultyDomain {
    EASY,       // Можно сделать самому
    MEDIUM,     // Требует опыта
    HARD,       // Лучше в сервис
    PROFESSIONAL // Только СТО
}
