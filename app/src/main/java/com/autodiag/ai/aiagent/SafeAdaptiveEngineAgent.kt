package com.autodiag.ai.aiagent

import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.data.model.EngineParametersSnapshot
import com.autodiag.ai.data.model.EngineProfile
import com.autodiag.ai.services.ObdConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Import types from same package (explicit for compilation order)
import com.autodiag.ai.aiagent.EngineTuneRecommendation
import com.autodiag.ai.aiagent.DriverProfile

// Re-export data model classes for easier access from UI layer
typealias EngineParametersSnapshot = com.autodiag.ai.data.model.EngineParametersSnapshot
typealias EngineProfile = com.autodiag.ai.data.model.EngineProfile

/**
 * БЕЗОПАСНЫЙ адаптивный агент двигателя
 * 
 * ПРИНЦИП: AI только АНАЛИЗИРУЕТ и РЕКОМЕНДУЕТ
 * Водитель сам решает применять настройки или нет!
 * 
 * Что AI делает:
 * ✓ Собирает данные по запросу водителя
 * ✓ Анализирует стиль вождения
 * ✓ Дает рекомендации по настройкам
 * ✓ Предупреждает голосом о проблемах
 * ✓ Создает резервные копии перед изменениями
 * 
 * Что AI НЕ делает:
 * ✗ Не снимает нагрузку автоматически
 * ✗ Не меняет настройки без разрешения
 * ✗ Не управляет педалями/тормозами
 * ✗ Не влияет на системы безопасности
 */
class SafeAdaptiveEngineAgent(
    private val obdManager: ObdConnectionManager
) {
    
    // Текущий анализ (только для информации водителя)
    private val _currentAnalysis = MutableStateFlow(DrivingAnalysis())
    val currentAnalysis: StateFlow<DrivingAnalysis> = _currentAnalysis.asStateFlow()
    
    // Рекомендуемые настройки (водитель сам решает применять или нет)
    private val _recommendedSettings = MutableStateFlow<EngineTuneRecommendation?>(null)
    val recommendedSettings: StateFlow<EngineTuneRecommendation?> = _recommendedSettings.asStateFlow()
    
    // Предупреждения (только голосом, никаких автоматических действий)
    private val _voiceAlerts = MutableStateFlow<List<VoiceAlert>>(emptyList())
    val voiceAlerts: StateFlow<List<VoiceAlert>> = _voiceAlerts.asStateFlow()
    
    // История вождения для анализа
    private val drivingSamples = mutableListOf<DrivingSample>()
    
    // Резервные копии настроек
    private val settingsBackups = mutableListOf<SettingsBackup>()
    
    // Пороги для предупреждений (только информирование!)
    companion object AlertThresholds {
        const val TEMP_WARNING = 100f      // Предупреждение о температуре
        const val TEMP_CRITICAL = 110f     // Критическое предупреждение
        const val KNOCK_WARNING = 2.0f     // Предупреждение о детонации
        const val LEAN_MIXTURE = -15f      // Бедная смесь
        const val RICH_MIXTURE = 15f       // Богатая смесь
        
        // Безопасные диапазоны корректировок
        const val MAX_IGNITION_TIMING_OFFSET = 2.0f   // Максимальное отклонение УОЗ (±2°)
        const val MAX_FUEL_MIXTURE_BIAS = 5.0f        // Максимальное отклонение смеси (±5%)
        
        // Валидационные константы
        const val MIN_VALID_RPM = 0
        const val MAX_VALID_RPM = 15000
        const val MIN_VALID_TEMP = -40f
        const val MAX_VALID_TEMP = 150f
        const val MIN_VALID_SPEED = 0
        const val MAX_VALID_SPEED = 300
    }
    
    /**
     * Начать сбор данных для анализа
     * Водитель сам запускает и останавливает сбор!
     */
    fun startDataCollection(durationKm: Int = 10) {
        drivingSamples.clear()
        _currentAnalysis.value = DrivingAnalysis(
            status = AnalysisStatus.COLLECTING,
            targetDistanceKm = durationKm,
            message = "Сбор данных начат. Проедьте $durationKm км в обычном режиме."
        )
    }
    
    /**
     * Добавить образец вождения
     */
    fun addSample(params: EngineParameters) {
        if (_currentAnalysis.value.status != AnalysisStatus.COLLECTING) return
        
        val sample = DrivingSample(
            timestamp = System.currentTimeMillis(),
            rpm = params.rpm ?: 0,
            throttlePosition = params.throttlePosition ?: 0f,
            throttleChangeRate = calculateThrottleChange(params),
            engineLoad = params.engineLoad ?: 0f,
            speed = params.speed ?: 0,
            coolantTemp = params.coolantTemperature ?: 0f,
            intakeTemp = params.intakeAirTemperature ?: 0f,
            knockSensor = params.knockSensorValue ?: 0f,
            shortTermFuelTrim = params.shortTermFuelTrim ?: 0f,
            longTermFuelTrim = params.longTermFuelTrim ?: 0f
        )
        
        drivingSamples.add(sample)
        
        // Проверяем предупреждения (только голосом!)
        checkAlerts(params)
        
        // Обновляем прогресс
        updateProgress()
    }
    
    /**
     * Остановить сбор и проанализировать
     */
    fun stopAndAnalyze(): DrivingAnalysis {
        if (drivingSamples.size < 10) {
            return DrivingAnalysis(
                status = AnalysisStatus.INSUFFICIENT_DATA,
                message = "Недостаточно данных. Проедьте минимум 5-10 км."
            )
        }

        val profile = analyzeDriverProfile()
        val recommendations = generateRecommendations(profile)
        val drivingStyle = mapProfileToDrivingStyle(profile)
        val engineProfile = calculateEngineProfile()

        _recommendedSettings.value = recommendations

        val analysis = DrivingAnalysis(
            status = AnalysisStatus.COMPLETED,
            profile = profile,
            drivingStyle = drivingStyle,
            engineProfile = engineProfile,
            samplesCount = drivingSamples.size,
            averageSpeed = drivingSamples.map { it.speed }.average().toInt(),
            averageRpm = drivingSamples.map { it.rpm }.average().toInt(),
            message = "Анализ завершен. Ваш профиль: ${getProfileName(profile)}",
            recommendations = recommendations,
            safetyNotes = recommendations?.safetyNotes ?: emptyList()
        )

        _currentAnalysis.value = analysis
        return analysis
    }

    private fun mapProfileToDrivingStyle(profile: DriverProfile): DrivingStyle {
        return when (profile) {
            DriverProfile.ECONOMICAL -> DrivingStyle.ECONOMICAL
            DriverProfile.DYNAMIC -> DrivingStyle.SPORTY
            DriverProfile.HIGHWAY -> DrivingStyle.BALANCED
            DriverProfile.URBAN -> DrivingStyle.BALANCED
            DriverProfile.BALANCED -> DrivingStyle.BALANCED
            DriverProfile.UNKNOWN -> DrivingStyle.BALANCED
        }
    }

    private fun calculateEngineProfile(): EngineProfile {
        return EngineProfile(
            avgRpm = drivingSamples.map { it.rpm }.average().toFloat(),
            avgLoad = drivingSamples.map { it.engineLoad }.average().toFloat(),
            avgTemp = drivingSamples.map { it.coolantTemp }.average().toFloat(),
            avgConsumption = calculateEstimatedConsumption()
        )
    }

    private fun calculateEstimatedConsumption(): Float {
        // Упрощенная оценка расхода на основе нагрузки и оборотов
        val avgLoad = drivingSamples.map { it.engineLoad }.average()
        val avgRpm = drivingSamples.map { it.rpm }.average()
        return (avgLoad * 0.15 + avgRpm * 0.001).toFloat()
    }
    
    /**
     * Анализ профиля водителя
     */
    private fun analyzeDriverProfile(): DriverProfile {
        if (drivingSamples.size < 10) return DriverProfile.UNKNOWN
        
        val recent = drivingSamples.takeLast(50)
        
        val avgThrottleChange = recent.map { it.throttleChangeRate }.average()
        val avgRpm = recent.map { it.rpm }.average()
        val avgLoad = recent.map { it.engineLoad }.average()
        val highRpmTime = recent.count { it.rpm > 4000 } / recent.size.toFloat()
        val lowRpmTime = recent.count { it.rpm < 2000 } / recent.size.toFloat()
        val highwayTime = recent.count { it.speed > 80 } / recent.size.toFloat()
        val urbanTime = recent.count { it.speed < 50 } / recent.size.toFloat()
        
        return when {
            // Спокойный водитель
            avgThrottleChange < 20 && avgRpm < 2500 && lowRpmTime > 0.6f -> 
                DriverProfile.ECONOMICAL
            
            // Динамичный водитель
            avgThrottleChange > 50 && highRpmTime > 0.3f -> 
                DriverProfile.DYNAMIC
            
            // Трасса
            highwayTime > 0.6f && avgThrottleChange < 30 -> 
                DriverProfile.HIGHWAY
            
            // Город
            urbanTime > 0.5f -> 
                DriverProfile.URBAN
            
            else -> DriverProfile.BALANCED
        }
    }
    
    /**
     * Генерация рекомендаций по настройке
     */
    private fun generateRecommendations(profile: DriverProfile): EngineTuneRecommendation {
        return when (profile) {
            DriverProfile.ECONOMICAL -> EngineTuneRecommendation(
                profile = profile,
                description = "Экономичный стиль вождения обнаружен",
                ignitionTimingOffset = 1f,           // +1° УОЗ
                fuelMixtureBias = -3f,               // Чуть беднее
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
            
            DriverProfile.DYNAMIC -> EngineTuneRecommendation(
                profile = profile,
                description = "Динамичный стиль вождения обнаружен",
                ignitionTimingOffset = -1f,          // -1° УОЗ
                fuelMixtureBias = 3f,                // Чуть богаче
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
            
            DriverProfile.HIGHWAY -> EngineTuneRecommendation(
                profile = profile,
                description = "Трасса - стабильная скорость",
                ignitionTimingOffset = 2f,           // +2° УОЗ
                fuelMixtureBias = -5f,               // Беднее
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
            
            DriverProfile.URBAN -> EngineTuneRecommendation(
                profile = profile,
                description = "Городской цикл",
                ignitionTimingOffset = 0f,           // Без изменений
                fuelMixtureBias = 2f,                // Чуть богаче
                reasoning = listOf(
                    "Частые остановки и старты",
                    "Богатая смесь (+2%) для лучшего отклика на низких",
                    "УОЗ без изменений для стабильности"
                ),
                safetyNotes = listOf(
                    "В пробках следите за температурой"
                )
            )
            
            else -> EngineTuneRecommendation(
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
    }
    
    /**
     * Проверка предупреждений - только голосом!
     * Никаких автоматических действий!
     */
    private fun checkAlerts(params: EngineParameters) {
        val alerts = mutableListOf<VoiceAlert>()
        
        // Температура - только предупреждение!
        params.coolantTemperature?.let { temp ->
            when {
                temp > AlertThresholds.TEMP_CRITICAL -> {
                    alerts.add(VoiceAlert(
                        priority = AlertPriority.CRITICAL,
                        message = "ВНИМАНИЕ! Критическая температура двигателя: ${temp.toInt()} градусов! Остановите автомобиль!",
                        type = AlertType.TEMPERATURE
                    ))
                }
                temp > AlertThresholds.TEMP_WARNING -> {
                    alerts.add(VoiceAlert(
                        priority = AlertPriority.WARNING,
                        message = "Температура двигателя повышена: ${temp.toInt()} градусов. Рекомендую снизить скорость.",
                        type = AlertType.TEMPERATURE
                    ))
                }
                else -> {}
            }
        }
        
        // Детонация - только предупреждение!
        params.knockSensorValue?.let { knock ->
            if (knock > AlertThresholds.KNOCK_WARNING) {
                alerts.add(VoiceAlert(
                    priority = AlertPriority.WARNING,
                    message = "Обнаружена детонация. Рекомендую заправиться качественным топливом или снизить УОЗ.",
                    type = AlertType.KNOCK
                ))
            }
        }
        
        // Смесь - только информация
        params.shortTermFuelTrim?.let { trim ->
            when {
                trim < AlertThresholds.LEAN_MIXTURE -> {
                    alerts.add(VoiceAlert(
                        priority = AlertPriority.INFO,
                        message = "Топливная смесь бедная. Возможен подсос воздуха.",
                        type = AlertType.FUEL_MIXTURE
                    ))
                }
                trim > AlertThresholds.RICH_MIXTURE -> {
                    alerts.add(VoiceAlert(
                        priority = AlertPriority.INFO,
                        message = "Топливная смесь богатая. Возможна проблема с лямбда-зондом.",
                        type = AlertType.FUEL_MIXTURE
                    ))
                }
                else -> {}
            }
        }
        
        if (alerts.isNotEmpty()) {
            _voiceAlerts.value = alerts
        }
    }
    
    /**
     * Применить рекомендуемые настройки
     * Только по явному запросу водителя!
     * Создает резервную копию перед изменениями
     */
    fun applyRecommendedSettings(vehicleVin: String = "unknown"): Boolean {
        val recommendation = _recommendedSettings.value ?: return false
        
        // Валидация настроек перед применением
        if (!validateRecommendations(recommendation)) {
            return false
        }
        
        // Создаем резервную копию перед изменениями
        val backup = createSettingsBackup(
            vehicleVin = vehicleVin,
            ignitionTiming = 0f, // Текущие значения (в реальности читаем из ECU)
            fuelMixture = 0f
        )
        settingsBackups.add(backup)
        
        // Нормализуем значения до безопасных пределов
        val safeRecommendation = normalizeToSafeLimits(recommendation)
        
        // Здесь отправка команд в ECU через OBD2
        // Но только если водитель явно согласился!
        
        // Пример:
        // obdManager.setIgnitionTiming(safeRecommendation.ignitionTimingOffset)
        // obdManager.setFuelTrim(safeRecommendation.fuelMixtureBias)
        
        return true
    }
    
    /**
     * Сбросить настройки к заводским
     */
    fun resetToFactory(): Boolean {
        // obdManager.setIgnitionTiming(0f)
        // obdManager.setFuelTrim(0f)
        
        _recommendedSettings.value = null
        return true
    }
    
    /**
     * Валидация входных параметров двигателя
     * @return Результат валидации с описанием ошибок
     */
    fun validateEngineParameters(params: EngineParameters): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Валидация оборотов
        params.rpm?.let { rpm ->
            if (rpm < AlertThresholds.MIN_VALID_RPM || rpm > AlertThresholds.MAX_VALID_RPM) {
                errors.add("Некорректные обороты двигателя: $rpm RPM (допустимо: ${AlertThresholds.MIN_VALID_RPM}-${AlertThresholds.MAX_VALID_RPM})")
            }
        }
        
        // Валидация температуры
        params.coolantTemperature?.let { temp ->
            if (temp < AlertThresholds.MIN_VALID_TEMP || temp > AlertThresholds.MAX_VALID_TEMP) {
                errors.add("Некорректная температура ОЖ: $temp°C (допустимо: ${AlertThresholds.MIN_VALID_TEMP}-${AlertThresholds.MAX_VALID_TEMP})")
            }
        }
        
        // Валидация скорости
        params.speed?.let { speed ->
            if (speed < AlertThresholds.MIN_VALID_SPEED || speed > AlertThresholds.MAX_VALID_SPEED) {
                errors.add("Некорректная скорость: $speed км/ч (допустимо: ${AlertThresholds.MIN_VALID_SPEED}-${AlertThresholds.MAX_VALID_SPEED})")
            }
        }
        
        // Валидация напряжения батареи
        params.batteryVoltage?.let { voltage ->
            if (voltage < 6f || voltage > 18f) {
                errors.add("Некорректное напряжение батареи: $voltage V (допустимо: 6-18V)")
            }
        }
        
        // Валидация нагрузки
        params.engineLoad?.let { load ->
            if (load < 0f || load > 100f) {
                errors.add("Некорректная нагрузка двигателя: $load% (допустимо: 0-100%)")
            }
        }
        
        // Валидация положения дросселя
        params.throttlePosition?.let { throttle ->
            if (throttle < 0f || throttle > 100f) {
                errors.add("Некорректное положение дросселя: $throttle% (допустимо: 0-100%)")
            }
        }
        
        // Валидация Fuel Trim
        params.shortTermFuelTrim?.let { trim ->
            if (trim < -50f || trim > 50f) {
                errors.add("Некорректный Short Term Fuel Trim: $trim% (допустимо: -50% до +50%)")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Проверка, находятся ли рекомендации в безопасных пределах
     */
    fun validateRecommendations(recommendation: EngineTuneRecommendation): Boolean {
        return kotlin.math.abs(recommendation.ignitionTimingOffset) <= AlertThresholds.MAX_IGNITION_TIMING_OFFSET &&
               kotlin.math.abs(recommendation.fuelMixtureBias) <= AlertThresholds.MAX_FUEL_MIXTURE_BIAS
    }
    
    /**
     * Нормализация рекомендаций до безопасных пределов
     */
    fun normalizeToSafeLimits(recommendation: EngineTuneRecommendation): EngineTuneRecommendation {
        return recommendation.copy(
            ignitionTimingOffset = recommendation.ignitionTimingOffset.coerceIn(
                -AlertThresholds.MAX_IGNITION_TIMING_OFFSET,
                AlertThresholds.MAX_IGNITION_TIMING_OFFSET
            ),
            fuelMixtureBias = recommendation.fuelMixtureBias.coerceIn(
                -AlertThresholds.MAX_FUEL_MIXTURE_BIAS,
                AlertThresholds.MAX_FUEL_MIXTURE_BIAS
            )
        )
    }
    
    /**
     * Создание резервной копии настроек
     */
    private fun createSettingsBackup(
        vehicleVin: String,
        ignitionTiming: Float,
        fuelMixture: Float
    ): SettingsBackup {
        return SettingsBackup(
            id = System.currentTimeMillis(),
            vehicleVin = vehicleVin,
            originalIgnitionTiming = ignitionTiming,
            originalFuelMixture = fuelMixture,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Получить список резервных копий
     */
    fun getSettingsBackups(): List<SettingsBackup> {
        return settingsBackups.toList()
    }
    
    /**
     * Получить последнюю резервную копию
     */
    fun getLastBackup(): SettingsBackup? {
        return settingsBackups.lastOrNull()
    }
    
    /**
     * Восстановить настройки из резервной копии
     */
    fun restoreFromBackup(backupId: Long): Boolean {
        val backup = settingsBackups.find { it.id == backupId } ?: return false
        
        // Восстановление настроек в ECU
        // obdManager.setIgnitionTiming(backup.originalIgnitionTiming)
        // obdManager.setFuelTrim(backup.originalFuelMixture)
        
        return true
    }
    
    /**
     * Удалить резервную копию
     */
    fun deleteBackup(backupId: Long): Boolean {
        return settingsBackups.removeIf { it.id == backupId }
    }
    
    private fun calculateThrottleChange(params: EngineParameters): Float {
        if (drivingSamples.isEmpty()) return 0f
        val lastThrottle = drivingSamples.last().throttlePosition
        val currentThrottle = params.throttlePosition ?: 0f
        return kotlin.math.abs(currentThrottle - lastThrottle)
    }
    
    private fun updateProgress() {
        val current = _currentAnalysis.value
        if (current.status == AnalysisStatus.COLLECTING) {
            val progress = (drivingSamples.size / 100.0 * 100).toInt() // Упрощенно
            _currentAnalysis.value = current.copy(
                progressPercent = progress.coerceAtMost(99),
                message = "Сбор данных... ${drivingSamples.size} образцов"
            )
        }
    }
    
    private fun getProfileName(profile: DriverProfile): String {
        return when (profile) {
            DriverProfile.ECONOMICAL -> "Экономичный"
            DriverProfile.DYNAMIC -> "Динамичный"
            DriverProfile.HIGHWAY -> "Трасса"
            DriverProfile.URBAN -> "Город"
            DriverProfile.BALANCED -> "Сбалансированный"
            DriverProfile.UNKNOWN -> "Не определен"
        }
    }
    
    fun clearAlerts() {
        _voiceAlerts.value = emptyList()
    }

    /**
     * Получить текущие параметры двигателя
     */
    fun getCurrentParameters(): EngineParametersSnapshot? {
        return if (drivingSamples.isNotEmpty()) {
            val last = drivingSamples.last()
            EngineParametersSnapshot(
                rpm = last.rpm.toFloat(),
                engineLoad = last.engineLoad,
                coolantTemp = last.coolantTemp,
                speed = last.speed.toFloat(),
                throttlePosition = last.throttlePosition
            )
        } else null
    }

    /**
     * Apply recommendations (alias for applyRecommendedSettings)
     * This method is called from AnalysisViewModel
     */
    fun applyRecommendations(recommendation: EngineTuneRecommendation?): Boolean {
        return recommendation?.let { applyRecommendedSettings() } ?: false
    }

    /**
     * Reset to factory settings (alias for resetToFactory)
     * This method is called from AnalysisViewModel
     */
    fun resetToFactorySettings(): Boolean {
        return resetToFactory()
    }
}

/**
 * Стиль вождения для UI
 */
enum class DrivingStyle {
    ECONOMICAL,   // Экономичный
    BALANCED,     // Сбалансированный
    SPORTY,       // Спортивный
    AGGRESSIVE    // Агрессивный
}

/**
 * Анализ вождения
 */
data class DrivingAnalysis(
    val status: AnalysisStatus = AnalysisStatus.IDLE,
    val profile: DriverProfile = DriverProfile.UNKNOWN,
    val drivingStyle: DrivingStyle = DrivingStyle.BALANCED,
    val engineProfile: EngineProfile = EngineProfile(0f, 0f, 0f, 0f),
    val samplesCount: Int = 0,
    val averageSpeed: Int = 0,
    val averageRpm: Int = 0,
    val progressPercent: Int = 0,
    val targetDistanceKm: Int = 10,
    val message: String = "",
    val recommendations: EngineTuneRecommendation? = null,
    val safetyNotes: List<String> = emptyList()
)

enum class AnalysisStatus {
    IDLE,               // Ожидание
    COLLECTING,         // Сбор данных
    INSUFFICIENT_DATA,  // Недостаточно данных
    COMPLETED           // Анализ завершен
}

// EngineTuneRecommendation, VoiceAlert, DriverProfile are now in separate files

enum class AlertPriority {
    INFO,       // Информация
    WARNING,    // Предупреждение
    CRITICAL    // Критическое
}

enum class AlertType {
    TEMPERATURE,
    KNOCK,
    FUEL_MIXTURE,
    OIL_PRESSURE
}

/**
 * Образец вождения
 */
data class DrivingSample(
    val timestamp: Long,
    val rpm: Int,
    val throttlePosition: Float,
    val throttleChangeRate: Float,
    val engineLoad: Float,
    val speed: Int,
    val coolantTemp: Float,
    val intakeTemp: Float,
    val knockSensor: Float,
    val shortTermFuelTrim: Float,
    val longTermFuelTrim: Float
)

/**
 * Резервная копия настроек
 */
data class SettingsBackup(
    val id: Long,
    val vehicleVin: String,
    val originalIgnitionTiming: Float,
    val originalFuelMixture: Float,
    val createdAt: Long
)

/**
 * Результат валидации
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: List<String>) : ValidationResult()
    
    fun isValid(): Boolean = this is Success
}
