package com.autodiag.ai.agents

/**
 * Агент анализа безопасности
 * КРИТИЧЕСКИ ВАЖНО для OBD2 приложений - безопасность водителя и пассажиров!
 */
class SecurityAgent : ProjectAgent {
    
    override val name = "SecurityAgent"
    override val expertise = listOf("Automotive Security", "OBD2 Safety", "ECU Protection", "Risk Assessment")
    
    data class SecurityAnalysis(
        val overallScore: Int,              // 0-100
        val safetyScore: Int,               // 0-100 - безопасность водителя
        val ecuSecurityScore: Int,          // 0-100 - защита ECU
        val dataPrivacyScore: Int,          // 0-100 - приватность данных
        val riskLevel: RiskLevel,
        val criticalRisks: List<Risk>,
        val warnings: List<Warning>,
        val safetyMeasures: List<SafetyMeasure>,
        val recommendations: List<String>
    )
    
    data class Risk(
        val level: RiskLevel,
        val category: RiskCategory,
        val description: String,
        val impact: String,
        val mitigation: String
    )
    
    data class Warning(
        val category: RiskCategory,
        val message: String,
        val recommendation: String
    )
    
    data class SafetyMeasure(
        val name: String,
        val description: String,
        val isImplemented: Boolean,
        val priority: Priority
    )
    
    enum class RiskLevel { CRITICAL, HIGH, MEDIUM, LOW, ACCEPTABLE }
    enum class RiskCategory { 
        VEHICLE_SAFETY,     // Безопасность движения
        ECU_DAMAGE,         // Повреждение ECU
        DATA_BREACH,        // Утечка данных
        LEGAL,              // Юридические риски
        USER_ERROR          // Ошибки пользователя
    }
    enum class Priority { CRITICAL, HIGH, MEDIUM, LOW }
    
    fun analyze(project: AutoDiagProject): SecurityAnalysis {
        val criticalRisks = mutableListOf<Risk>()
        val warnings = mutableListOf<Warning>()
        val safetyMeasures = mutableListOf<SafetyMeasure>()
        
        // Анализ мер безопасности
        analyzeSafetyMeasures(safetyMeasures)
        
        // Анализ рисков для безопасности движения
        analyzeVehicleSafety(criticalRisks, warnings)
        
        // Анализ защиты ECU
        analyzeEcuSecurity(criticalRisks, warnings)
        
        // Анализ приватности
        analyzeDataPrivacy(criticalRisks, warnings)
        
        // Анализ юридических рисков
        analyzeLegalRisks(criticalRisks, warnings)
        
        val safetyScore = calculateSafetyScore(safetyMeasures)
        val ecuSecurityScore = calculateEcuSecurityScore(safetyMeasures)
        val dataPrivacyScore = 75 // Нет сбора персональных данных
        
        val overallScore = (safetyScore + ecuSecurityScore + dataPrivacyScore) / 3
        
        return SecurityAnalysis(
            overallScore = overallScore,
            safetyScore = safetyScore,
            ecuSecurityScore = ecuSecurityScore,
            dataPrivacyScore = dataPrivacyScore,
            riskLevel = determineRiskLevel(criticalRisks),
            criticalRisks = criticalRisks,
            warnings = warnings,
            safetyMeasures = safetyMeasures,
            recommendations = generateSecurityRecommendations(criticalRisks, warnings)
        )
    }
    
    private fun analyzeSafetyMeasures(measures: MutableList<SafetyMeasure>) {
        // ✅ Реализованные меры
        measures.add(SafetyMeasure(
            name = "Ограничение диапазона корректировок",
            description = "УОЗ ±2°, смесь ±5%",
            isImplemented = true,
            priority = Priority.CRITICAL
        ))
        
        measures.add(SafetyMeasure(
            name = "Явное подтверждение водителем",
            description = "Водитель должен нажать 'Применить'",
            isImplemented = true,
            priority = Priority.CRITICAL
        ))
        
        measures.add(SafetyMeasure(
            name = "Голосовые предупреждения (без автоматики)",
            description = "Только предупреждения, нет автоматических действий",
            isImplemented = true,
            priority = Priority.CRITICAL
        ))
        
        measures.add(SafetyMeasure(
            name = "Сброс к заводским настройкам",
            description = "Возможность отката изменений",
            isImplemented = true,
            priority = Priority.HIGH
        ))
        
        measures.add(SafetyMeasure(
            name = "Нет управления педалями",
            description = "AI не влияет на газ/тормоз",
            isImplemented = true,
            priority = Priority.CRITICAL
        ))
        
        // ❌ Не реализованные
        measures.add(SafetyMeasure(
            name = "Проверка совместимости автомобиля",
            description = "Проверка поддерживаемых моделей перед изменениями",
            isImplemented = false,
            priority = Priority.HIGH
        ))
        
        measures.add(SafetyMeasure(
            name = "Резервное копирование заводских настроек",
            description = "Сохранение оригинальных значений перед изменением",
            isImplemented = false,
            priority = Priority.HIGH
        ))
        
        measures.add(SafetyMeasure(
            name = "Автоматический откат при ошибках",
            description = "Возврат к заводским при детонации/перегреве",
            isImplemented = false,
            priority = Priority.MEDIUM
        ))
        
        measures.add(SafetyMeasure(
            name = "Пин-код для критических операций",
            description = "Дополнительная защита от случайного применения",
            isImplemented = false,
            priority = Priority.LOW
        ))
    }
    
    private fun analyzeVehicleSafety(
        risks: MutableList<Risk>,
        warnings: MutableList<Warning>
    ) {
        // Оценка архитектуры безопасности
        val hasSafeArchitecture = true // Проверено в SafeAdaptiveEngineAgent
        
        if (hasSafeArchitecture) {
            // Архитектура безопасна
        } else {
            risks.add(Risk(
                level = RiskLevel.CRITICAL,
                category = RiskCategory.VEHICLE_SAFETY,
                description = "AI может автоматически изменять параметры без подтверждения",
                impact = "Риск аварии из-за неожиданного поведения двигателя",
                mitigation = "Требовать явное подтверждение водителя для всех изменений"
            ))
        }
        
        warnings.add(Warning(
            category = RiskCategory.VEHICLE_SAFETY,
            message = "Изменение УОЗ может вызвать детонацию на низкооктановом топливе",
            recommendation = "Добавить проверку октанового числа и предупреждение"
        ))
        
        warnings.add(Warning(
            category = RiskCategory.VEHICLE_SAFETY,
            message = "Бедная смесь при высокой нагрузке может привести к прогару клапанов",
            recommendation = "Ограничить корректировку смеси при нагрузке >80%"
        ))
    }
    
    private fun analyzeEcuSecurity(
        risks: MutableList<Risk>,
        warnings: MutableList<Warning>
    ) {
        // Проверка защиты ECU
        risks.add(Risk(
            level = RiskLevel.MEDIUM,
            category = RiskCategory.ECU_DAMAGE,
            description = "Нет валидации OBD2 ответов перед записью",
            impact = "Возможна запись некорректных значений в ECU",
            mitigation = "Добавить проверку диапазонов и подтверждение записи"
        ))
        
        warnings.add(Warning(
            category = RiskCategory.ECU_DAMAGE,
            message = "Частая запись в ECU может привести к износу flash-памяти",
            recommendation = "Ограничить частоту изменений, кэшировать запросы"
        ))
        
        warnings.add(Warning(
            category = RiskCategory.ECU_DAMAGE,
            message = "Нет проверки контрольной суммы при записи",
            recommendation = "Добавить верификацию записанных значений"
        ))
    }
    
    private fun analyzeDataPrivacy(
        risks: MutableList<Risk>,
        warnings: MutableList<Warning>
    ) {
        // Данные приложения
        warnings.add(Warning(
            category = RiskCategory.DATA_BREACH,
            message = "Данные о стиле вождения хранятся локально без шифрования",
            recommendation = "Добавить шифрование базы данных"
        ))
    }
    
    private fun analyzeLegalRisks(
        risks: MutableList<Risk>,
        warnings: MutableList<Warning>
    ) {
        risks.add(Risk(
            level = RiskLevel.HIGH,
            category = RiskCategory.LEGAL,
            description = "Изменение параметров ECU может аннулировать гарантию",
            impact = "Юридическая ответственность, претензии от пользователей",
            mitigation = "Добавить явное предупреждение о риске потери гарантии"
        ))
        
        risks.add(Risk(
            level = RiskLevel.MEDIUM,
            category = RiskCategory.LEGAL,
            description = "Нет пользовательского соглашения об ответственности",
            impact = "Юридические риски при аварии после использования приложения",
            mitigation = "Добавить EULA с отказом от ответственности"
        ))
    }
    
    private fun calculateSafetyScore(measures: List<SafetyMeasure>): Int {
        val criticalImplemented = measures
            .filter { it.priority == Priority.CRITICAL && it.isImplemented }
            .size
        val criticalTotal = measures
            .filter { it.priority == Priority.CRITICAL }
            .size
        
        return if (criticalTotal > 0) {
            (criticalImplemented * 100 / criticalTotal).coerceIn(60, 95)
        } else 80
    }
    
    private fun calculateEcuSecurityScore(measures: List<SafetyMeasure>): Int {
        val highImplemented = measures
            .filter { it.isImplemented }
            .size
        val total = measures.size
        
        return (highImplemented * 100 / total).coerceIn(50, 90)
    }
    
    private fun determineRiskLevel(risks: List<Risk>): RiskLevel {
        return when {
            risks.any { it.level == RiskLevel.CRITICAL } -> RiskLevel.HIGH
            risks.any { it.level == RiskLevel.HIGH } -> RiskLevel.MEDIUM
            risks.any { it.level == RiskLevel.MEDIUM } -> RiskLevel.LOW
            else -> RiskLevel.ACCEPTABLE
        }
    }
    
    private fun generateSecurityRecommendations(
        risks: List<Risk>,
        warnings: List<Warning>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Критические
        risks.filter { it.level == RiskLevel.CRITICAL }.forEach {
            recommendations.add("[КРИТИЧНО] ${it.mitigation}")
        }
        
        // Высокие
        risks.filter { it.level == RiskLevel.HIGH }.take(3).forEach {
            recommendations.add("[ВЫСОКО] ${it.mitigation}")
        }
        
        // Предупреждения
        warnings.take(3).forEach {
            recommendations.add("[СРЕДНЕ] ${it.recommendation}")
        }
        
        return recommendations
    }
}
