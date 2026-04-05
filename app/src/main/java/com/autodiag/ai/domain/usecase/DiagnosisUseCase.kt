package com.autodiag.ai.domain.usecase

import com.autodiag.ai.domain.model.DiagnosisHistoryDomainModel
import com.autodiag.ai.domain.model.DtcCodeDomainModel
import com.autodiag.ai.domain.model.DtcSeverityDomain
import com.autodiag.ai.domain.repository.DiagnosisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * UseCase для работы с диагностикой
 * Реализует бизнес-логику диагностики автомобиля
 */
class DiagnosisUseCase(
    private val diagnosisRepository: DiagnosisRepository
) {
    
    /**
     * Запустить полную диагностику автомобиля
     * @param vehicleVin VIN автомобиля
     * @param dtcCodes Список считанных DTC кодов
     * @return Результат операции с созданной записью истории
     */
    suspend fun runDiagnosis(
        vehicleVin: String,
        dtcCodes: List<String>
    ): Result<DiagnosisHistoryDomainModel> {
        if (vehicleVin.isBlank()) {
            return Result.failure(IllegalArgumentException("VIN не может быть пустым"))
        }
        
        // Получаем информацию о DTC кодах
        val dtcInfoList = diagnosisRepository.getDtcInfos(dtcCodes)
        
        // Оцениваем состояние двигателя
        val healthScore = calculateHealthScore(dtcInfoList)
        
        // Формируем список обнаруженных проблем
        val detectedIssues = dtcInfoList.map { dtc ->
            com.autodiag.ai.domain.model.DetectedIssueDomain(
                system = getSystemForCategory(dtc.category),
                severity = dtc.severity,
                description = "${dtc.code}: ${dtc.description}",
                recommendedAction = dtc.recommendedActions.firstOrNull() ?: "Требуется диагностика"
            )
        }
        
        // Генерируем рекомендации
        val recommendations = generateRecommendations(dtcInfoList, healthScore)
        
        // Создаем запись истории
        val history = DiagnosisHistoryDomainModel(
            vehicleVin = vehicleVin,
            dtcCodes = dtcCodes,
            engineHealthScore = healthScore,
            detectedIssues = detectedIssues,
            recommendations = recommendations,
            operatingTips = emptyList() // Добавляются отдельно если нужно
        )
        
        return diagnosisRepository.saveDiagnosis(history).map { id ->
            history.copy(id = id)
        }
    }
    
    /**
     * Получить историю диагностик
     */
    fun getHistory(): Flow<List<DiagnosisHistoryDomainModel>> {
        return diagnosisRepository.getDiagnosisHistory()
    }
    
    /**
     * Получить историю диагностик для конкретного автомобиля
     */
    fun getHistoryByVehicle(vin: String): Flow<List<DiagnosisHistoryDomainModel>> {
        return diagnosisRepository.getDiagnosisHistoryByVehicle(vin)
    }
    
    /**
     * Очистить коды ошибок
     */
    suspend fun clearCodes(): Result<Unit> {
        return diagnosisRepository.clearDtcCodes()
    }
    
    /**
     * Получить информацию о DTC коде
     */
    suspend fun getDtcInfo(code: String): DtcCodeDomainModel? {
        return diagnosisRepository.getDtcInfo(code)
    }
    
    /**
     * Поиск DTC кодов
     */
    suspend fun searchDtc(query: String): List<DtcCodeDomainModel> {
        if (query.length < 2) {
            return emptyList()
        }
        return diagnosisRepository.searchDtc(query)
    }
    
    /**
     * Получить последнюю диагностику для автомобиля
     */
    suspend fun getLatestDiagnosis(vin: String): DiagnosisHistoryDomainModel? {
        return diagnosisRepository.getLatestDiagnosis(vin)
    }
    
    /**
     * Проверить, можно ли эксплуатировать автомобиль
     */
    suspend fun canVehicleBeDriven(vin: String): Boolean {
        val latestDiagnosis = diagnosisRepository.getLatestDiagnosis(vin) ?: return true
        return !latestDiagnosis.hasCriticalIssues
    }
    
    /**
     * Получить статистику по диагностикам
     */
    suspend fun getDiagnosisStatistics(vin: String): DiagnosisStatistics {
        val history = diagnosisRepository.getDiagnosisHistoryByVehicle(vin).first()
        
        if (history.isEmpty()) {
            return DiagnosisStatistics.EMPTY
        }
        
        val avgHealthScore = history.map { it.engineHealthScore }.average().toFloat()
        val criticalCount = history.count { it.hasCriticalIssues }
        val totalIssues = history.sumOf { it.totalIssues.toLong() }
        
        return DiagnosisStatistics(
            totalDiagnoses = history.size,
            averageHealthScore = avgHealthScore,
            criticalIssuesCount = criticalCount,
            totalIssuesFound = totalIssues.toInt(),
            lastDiagnosisDate = history.maxOfOrNull { it.diagnosisDate }
        )
    }
    
    /**
     * Удалить запись диагностики
     */
    suspend fun deleteDiagnosis(id: Long): Result<Unit> {
        return diagnosisRepository.deleteDiagnosis(id)
    }
    
    // Приватные методы
    
    private fun calculateHealthScore(dtcCodes: List<DtcCodeDomainModel>): Float {
        var score = 100f
        
        dtcCodes.forEach { dtc ->
            score -= when (dtc.severity) {
                DtcSeverityDomain.CRITICAL -> 30f
                DtcSeverityDomain.HIGH -> 15f
                DtcSeverityDomain.MEDIUM -> 8f
                DtcSeverityDomain.LOW -> 3f
            }
        }
        
        return score.coerceAtLeast(0f)
    }
    
    private fun getSystemForCategory(category: com.autodiag.ai.domain.model.DtcCategoryDomain): String {
        return when (category) {
            com.autodiag.ai.domain.model.DtcCategoryDomain.ENGINE -> "Двигатель"
            com.autodiag.ai.domain.model.DtcCategoryDomain.TRANSMISSION -> "Трансмиссия"
            com.autodiag.ai.domain.model.DtcCategoryDomain.ABS -> "Система ABS"
            com.autodiag.ai.domain.model.DtcCategoryDomain.AIRBAG -> "Подушки безопасности"
            com.autodiag.ai.domain.model.DtcCategoryDomain.CLIMATE -> "Климат-контроль"
            com.autodiag.ai.domain.model.DtcCategoryDomain.ELECTRICAL -> "Электрика"
            com.autodiag.ai.domain.model.DtcCategoryDomain.FUEL -> "Топливная система"
            com.autodiag.ai.domain.model.DtcCategoryDomain.EMISSIONS -> "Выхлопная система"
            com.autodiag.ai.domain.model.DtcCategoryDomain.COMMUNICATION -> "Связь"
        }
    }
    
    private fun generateRecommendations(
        dtcCodes: List<DtcCodeDomainModel>,
        healthScore: Float
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (healthScore < 50f) {
            recommendations.add("Рекомендуется незамедлительное обращение в сервисный центр")
        }
        
        val criticalCodes = dtcCodes.filter { it.severity == DtcSeverityDomain.CRITICAL }
        if (criticalCodes.isNotEmpty()) {
            recommendations.add("Обнаружены критические ошибки: ${criticalCodes.joinToString { it.code }}")
        }
        
        dtcCodes.flatMap { it.recommendedActions }
            .distinct()
            .take(5)
            .let { recommendations.addAll(it) }
        
        return recommendations
    }
    
    /**
     * Статистика диагностик
     */
    data class DiagnosisStatistics(
        val totalDiagnoses: Int,
        val averageHealthScore: Float,
        val criticalIssuesCount: Int,
        val totalIssuesFound: Int,
        val lastDiagnosisDate: Long?
    ) {
        companion object {
            val EMPTY = DiagnosisStatistics(0, 100f, 0, 0, null)
        }
    }
}
