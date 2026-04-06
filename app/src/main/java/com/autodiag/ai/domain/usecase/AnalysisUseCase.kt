package com.autodiag.ai.domain.usecase

import com.autodiag.ai.domain.model.*
import com.autodiag.ai.domain.repository.AnalysisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * UseCase для анализа двигателя и стиля вождения
 * Реализует бизнес-логику анализа и рекомендаций
 */
class AnalysisUseCase(
    private val analysisRepository: AnalysisRepository
) {
    
    /**
     * Проанализировать работу двигателя
     * @param vehicleVin VIN автомобиля
     * @param engineParams Параметры двигателя
     * @return Результат с рекомендациями
     */
    suspend fun analyzeEngine(
        vehicleVin: String,
        engineParams: EngineParametersInput
    ): Result<AnalysisResult> {
        if (vehicleVin.isBlank()) {
            return Result.failure(IllegalArgumentException("VIN не может быть пустым"))
        }
        
        // Валидация параметров
        val validationResult = validateEngineParameters(engineParams)
        if (validationResult != null) {
            return Result.failure(validationResult)
        }
        
        // Анализ параметров
        val issues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        // Проверка температуры
        engineParams.coolantTemp?.let { temp ->
            when {
                temp > 105 -> {
                    issues.add("Критическая температура двигателя: ${temp}°C")
                    recommendations.add("Немедленно остановите двигатель и проверьте уровень охлаждающей жидкости")
                }
                temp > 95 -> {
                    issues.add("Повышенная температура двигателя: ${temp}°C")
                    recommendations.add("Проверьте состояние термостата и радиатора")
                }
                temp < 70 -> {
                    issues.add("Двигатель не прогрет: ${temp}°C")
                    recommendations.add("Дайте двигателю прогреться до рабочей температуры")
                }
                else -> {}
            }
        }
        
        // Проверка оборотов
        engineParams.rpm?.let { rpm ->
            when {
                rpm > 6000 -> {
                    issues.add("Критические обороты: $rpm RPM")
                    recommendations.add("Снизьте обороты двигателя")
                }
                engineParams.speed == 0 && (rpm < 700 || rpm > 1000) -> {
                    issues.add("Нестабильный холостой ход: $rpm RPM")
                    recommendations.add("Проверьте дроссельную заслонку и датчик холостого хода")
                }
                else -> {}
            }
        }
        
        // Проверка Fuel Trim
        engineParams.shortTermFuelTrim?.let { trim ->
            when {
                kotlin.math.abs(trim) > 25 -> {
                    issues.add("Критическое отклонение топливной смеси: ${trim}%")
                    recommendations.add("Срочная диагностика топливной системы")
                }
                kotlin.math.abs(trim) > 15 -> {
                    issues.add("Отклонение топливной смеси: ${trim}%")
                    recommendations.add("Проверьте лямбда-зонд и воздушный фильтр")
                }
                else -> {}
            }
        }
        
        // Расчет health score
        val healthScore = calculateHealthScore(engineParams, issues.size)
        
        return Result.success(
            AnalysisResult(
                healthScore = healthScore,
                issues = issues,
                recommendations = recommendations,
                engineStatus = determineEngineStatus(healthScore, issues.size)
            )
        )
    }
    
    /**
     * Получить рекомендации по настройке двигателя
     * @param profile Профиль водителя
     * @return Рекомендации по настройке
     */
    suspend fun getRecommendations(profile: DriverProfileDomain): Result<EngineTuneRecommendationDomain> {
        val recommendation = when (profile) {
            DriverProfileDomain.ECONOMICAL -> EngineTuneRecommendationDomain(
                profile = profile,
                description = "Экономичный стиль вождения обнаружен",
                ignitionTimingOffset = 1f,
                fuelMixtureBias = -3f,
                reasoning = listOf(
                    "Ваш стиль вождения спокойный",
                    "Раннее зажигание (+1°) улучшит эффективность сгорания",
                    "Бедная смесь (-3%) снизит расход топлива",
                    "Ожидаемая экономия: 5-10%"
                ),
                safetyNotes = listOf(
                    "При детонации верните УОЗ к 0°",
                    "Следите за температурой двигателя"
                )
            )
            
            DriverProfileDomain.DYNAMIC -> EngineTuneRecommendationDomain(
                profile = profile,
                description = "Динамичный стиль вождения обнаружен",
                ignitionTimingOffset = -1f,
                fuelMixtureBias = 3f,
                reasoning = listOf(
                    "Вы предпочитаете динамичную езду",
                    "Позднее зажигание (-1°) даст больше мощности",
                    "Богатая смесь (+3%) улучшит отклик",
                    "Ожидаемый прирост отклика: 10-15%"
                ),
                safetyNotes = listOf(
                    "Следите за температурой на высоких оборотах",
                    "При детонации верните УОЗ к 0°"
                )
            )
            
            DriverProfileDomain.HIGHWAY -> EngineTuneRecommendationDomain(
                profile = profile,
                description = "Трасса - стабильная скорость",
                ignitionTimingOffset = 2f,
                fuelMixtureBias = -5f,
                reasoning = listOf(
                    "Стабильная скорость на трассе",
                    "Раннее зажигание (+2°) для максимальной эффективности",
                    "Бедная смесь (-5%) для экономии топлива",
                    "Ожидаемая экономия: 10-15%"
                ),
                safetyNotes = listOf(
                    "При обгоне верните смесь к 0%"
                )
            )
            
            DriverProfileDomain.URBAN -> EngineTuneRecommendationDomain(
                profile = profile,
                description = "Городской цикл",
                ignitionTimingOffset = 0f,
                fuelMixtureBias = 2f,
                reasoning = listOf(
                    "Частые остановки и старты",
                    "Богатая смесь (+2%) для лучшего отклика на низких",
                    "УОЗ без изменений для стабильности"
                ),
                safetyNotes = listOf(
                    "В пробках следите за температурой"
                )
            )
            
            else -> EngineTuneRecommendationDomain(
                profile = profile,
                description = "Сбалансированный стиль",
                ignitionTimingOffset = 0f,
                fuelMixtureBias = 0f,
                reasoning = listOf(
                    "Ваш стиль вождения сбалансирован",
                    "Специальных настроек не требуется"
                ),
                safetyNotes = emptyList()
            )
        }
        
        // Проверяем, что рекомендации в безопасных пределах
        return if (recommendation.isWithinSafeLimits()) {
            Result.success(recommendation)
        } else {
            Result.success(recommendation.normalized())
        }
    }
    
    /**
     * Сохранить анализ вождения
     */
    suspend fun saveAnalysis(analysis: DrivingAnalysisDomainModel): Result<Unit> {
        return analysisRepository.saveAnalysis(analysis)
    }
    
    /**
     * Получить все анализы
     */
    fun getAllAnalyses(): Flow<List<DrivingAnalysisDomainModel>> {
        return analysisRepository.getAllAnalyses()
    }
    
    /**
     * Получить последний анализ
     */
    suspend fun getLatestAnalysis(): DrivingAnalysisDomainModel? {
        return analysisRepository.getLatestAnalysis()
    }
    
    /**
     * Создать резервную копию настроек
     */
    suspend fun backupSettings(
        vehicleVin: String,
        currentIgnitionTiming: Float,
        currentFuelMixture: Float
    ): Result<Long> {
        val backup = SettingsBackupDomain(
            vehicleVin = vehicleVin,
            originalIgnitionTiming = currentIgnitionTiming,
            originalFuelMixture = currentFuelMixture,
            notes = "Автоматическое резервное копирование перед изменениями"
        )
        return analysisRepository.createSettingsBackup(backup)
    }
    
    /**
     * Восстановить настройки из последней резервной копии
     */
    suspend fun restoreLastSettings(vehicleVin: String): Result<SettingsBackupDomain> {
        val backup = analysisRepository.getLatestBackup(vehicleVin)
            ?: return Result.failure(IllegalStateException("Резервная копия не найдена"))
        
        return analysisRepository.restoreFromBackup(backup.id)
    }
    
    /**
     * Получить статистику анализов
     */
    suspend fun getAnalysisStatistics(): AnalysisStatistics {
        val analyses = analysisRepository.getAllAnalyses().first()
        
        if (analyses.isEmpty()) {
            return AnalysisStatistics.EMPTY
        }
        
        val profileCounts = analyses.groupingBy { it.profile }.eachCount()
        val mostCommonProfile = profileCounts.maxByOrNull { it.value }?.key ?: DriverProfileDomain.UNKNOWN
        
        return AnalysisStatistics(
            totalAnalyses = analyses.size,
            mostCommonProfile = mostCommonProfile,
            averageSpeed = analyses.map { it.averageSpeed }.average().toInt(),
            averageRpm = analyses.map { it.averageRpm }.average().toInt(),
            appliedSettingsCount = analyses.count { it.recommendations != null }
        )
    }
    
    // Приватные методы
    
    private fun validateEngineParameters(params: EngineParametersInput): Throwable? {
        params.rpm?.let {
            if (it < 0 || it > 15000) {
                return IllegalArgumentException("Некорректные обороты двигателя: $it")
            }
        }
        
        params.coolantTemp?.let {
            if (it < -40 || it > 150) {
                return IllegalArgumentException("Некорректная температура: $it")
            }
        }
        
        params.speed?.let {
            if (it < 0 || it > 300) {
                return IllegalArgumentException("Некорректная скорость: $it")
            }
        }
        
        return null
    }
    
    private fun calculateHealthScore(params: EngineParametersInput, issuesCount: Int): Float {
        var score = 100f
        
        // Штраф за обнаруженные проблемы
        score -= issuesCount * 10f
        
        // Дополнительные проверки
        params.coolantTemp?.let { temp ->
            if (temp > 100) score -= 15f
            else if (temp < 80) score -= 5f
        }
        
        params.rpm?.let { rpm ->
            if (rpm > 5000) score -= 10f
        }
        
        return score.coerceAtLeast(0f)
    }
    
    private fun determineEngineStatus(healthScore: Float, issuesCount: Int): EngineStatus {
        return when {
            healthScore >= 90 && issuesCount == 0 -> EngineStatus.EXCELLENT
            healthScore >= 70 -> EngineStatus.GOOD
            healthScore >= 50 -> EngineStatus.FAIR
            healthScore >= 30 -> EngineStatus.POOR
            else -> EngineStatus.CRITICAL
        }
    }
    
    /**
     * Входные параметры двигателя
     */
    data class EngineParametersInput(
        val rpm: Int? = null,
        val speed: Int? = null,
        val coolantTemp: Float? = null,
        val intakeTemp: Float? = null,
        val engineLoad: Float? = null,
        val throttlePosition: Float? = null,
        val shortTermFuelTrim: Float? = null,
        val longTermFuelTrim: Float? = null,
        val batteryVoltage: Float? = null
    )
    
    /**
     * Результат анализа
     */
    data class AnalysisResult(
        val healthScore: Float,
        val issues: List<String>,
        val recommendations: List<String>,
        val engineStatus: EngineStatus
    )
    
    enum class EngineStatus {
        EXCELLENT, GOOD, FAIR, POOR, CRITICAL
    }
    
    data class AnalysisStatistics(
        val totalAnalyses: Int,
        val mostCommonProfile: DriverProfileDomain,
        val averageSpeed: Int,
        val averageRpm: Int,
        val appliedSettingsCount: Int
    ) {
        companion object {
            val EMPTY = AnalysisStatistics(0, DriverProfileDomain.UNKNOWN, 0, 0, 0)
        }
    }
}
