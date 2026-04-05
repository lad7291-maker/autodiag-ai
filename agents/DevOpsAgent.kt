package com.autodiag.ai.agents

/**
 * Агент анализа пути до продакшена
 * Оценивает CI/CD, тестирование, мониторинг, масштабируемость
 */
class DevOpsAgent : ProjectAgent {
    
    override val name = "DevOpsAgent"
    override val expertise = listOf("CI/CD", "DevOps", "Mobile Deployment", "Testing", "Monitoring")
    
    data class DevOpsAnalysis(
        val overallScore: Int,              // 0-100
        val ciCdScore: Int,                 // 0-100
        val testingScore: Int,              // 0-100
        val monitoringScore: Int,           // 0-100
        val deploymentReadiness: DeploymentReadiness,
        val missingComponents: List<MissingComponent>,
        val roadmap: List<RoadmapItem>,
        val effortEstimate: EffortEstimate,
        val recommendations: List<String>
    )
    
    data class DeploymentReadiness(
        val isReady: Boolean,
        val blockers: List<String>,
        val warnings: List<String>
    )
    
    data class MissingComponent(
        val name: String,
        val priority: Priority,
        val estimatedEffort: String,
        val impact: String
    )
    
    enum class Priority { CRITICAL, HIGH, MEDIUM, LOW }
    
    data class RoadmapItem(
        val phase: String,
        val duration: String,
        val tasks: List<String>,
        val deliverables: List<String>
    )
    
    data class EffortEstimate(
        val totalWeeks: Int,
        val developers: Int,
        val phases: Map<String, Int> // phase -> weeks
    )
    
    fun analyze(project: AutoDiagProject): DevOpsAnalysis {
        val missingComponents = identifyMissingComponents()
        val blockers = identifyBlockers(missingComponents)
        val warnings = identifyWarnings()
        
        val ciCdScore = calculateCiCdScore()
        val testingScore = calculateTestingScore()
        val monitoringScore = calculateMonitoringScore()
        
        val overallScore = (ciCdScore + testingScore + monitoringScore) / 3
        
        return DevOpsAnalysis(
            overallScore = overallScore,
            ciCdScore = ciCdScore,
            testingScore = testingScore,
            monitoringScore = monitoringScore,
            deploymentReadiness = DeploymentReadiness(
                isReady = blockers.isEmpty(),
                blockers = blockers,
                warnings = warnings
            ),
            missingComponents = missingComponents,
            roadmap = generateRoadmap(missingComponents),
            effortEstimate = estimateEffort(missingComponents),
            recommendations = generateDevOpsRecommendations(missingComponents)
        )
    }
    
    private fun identifyMissingComponents(): List<MissingComponent> {
        return listOf(
            MissingComponent(
                name = "CI/CD Pipeline (GitHub Actions/GitLab CI)",
                priority = Priority.CRITICAL,
                estimatedEffort = "2-3 дня",
                impact = "Автоматическая сборка и тестирование"
            ),
            
            MissingComponent(
                name = "Unit Tests (минимум 70% покрытие)",
                priority = Priority.CRITICAL,
                estimatedEffort = "1-2 недели",
                impact = "Уверенность в работе кода"
            ),
            
            MissingComponent(
                name = "Integration Tests",
                priority = Priority.HIGH,
                estimatedEffort = "3-5 дней",
                impact = "Проверка работы с OBD2"
            ),
            
            MissingComponent(
                name = "UI Tests (Compose Testing)",
                priority = Priority.HIGH,
                estimatedEffort = "3-5 дней",
                impact = "Проверка пользовательских сценариев"
            ),
            
            MissingComponent(
                name = "Google Play Console настройка",
                priority = Priority.CRITICAL,
                estimatedEffort = "1 день",
                impact = "Возможность публикации"
            ),
            
            MissingComponent(
                name = "Firebase Crashlytics",
                priority = Priority.HIGH,
                estimatedEffort = "1 день",
                impact = "Мониторинг крашей"
            ),
            
            MissingComponent(
                name = "Firebase Analytics",
                priority = Priority.MEDIUM,
                estimatedEffort = "1-2 дня",
                impact = "Аналитика использования"
            ),
            
            MissingComponent(
                name = "ProGuard/R8 конфигурация",
                priority = Priority.MEDIUM,
                estimatedEffort = "1-2 дня",
                impact = "Оптимизация размера APK"
            ),
            
            MissingComponent(
                name = "Signing конфигурация",
                priority = Priority.CRITICAL,
                estimatedEffort = "1 день",
                impact = "Подпись релизных сборок"
            ),
            
            MissingComponent(
                name = "Privacy Policy и Terms of Service",
                priority = Priority.HIGH,
                estimatedEffort = "2-3 дня (с юристом)",
                impact = "Требование Google Play"
            ),
            
            MissingComponent(
                name = "App Store скриншоты и описание",
                priority = Priority.MEDIUM,
                estimatedEffort = "2-3 дня",
                impact = "Презентация в магазине"
            ),
            
            MissingComponent(
                name = "Beta Testing (Firebase App Distribution)",
                priority = Priority.MEDIUM,
                estimatedEffort = "1 день",
                impact = "Тестирование перед релизом"
            ),
            
            MissingComponent(
                name = "Versioning strategy",
                priority = Priority.MEDIUM,
                estimatedEffort = "1 день",
                impact = "Управление версиями"
            ),
            
            MissingComponent(
                name = "Changelog maintenance",
                priority = Priority.LOW,
                estimatedEffort = "ongoing",
                impact = "Информирование пользователей"
            ),
            
            MissingComponent(
                name = "In-app update mechanism",
                priority = Priority.LOW,
                estimatedEffort = "1-2 дня",
                impact = "Автоматические обновления"
            )
        )
    }
    
    private fun identifyBlockers(missing: List<MissingComponent>): List<String> {
        return missing
            .filter { it.priority == Priority.CRITICAL }
            .map { "❌ ${it.name}: ${it.impact}" }
    }
    
    private fun identifyWarnings(): List<String> {
        return listOf(
            "⚠️ Нет тестов - риск критических багов в продакшене",
            "⚠️ Нет мониторинга - не узнаем о проблемах у пользователей",
            "⚠️ Нет CI/CD - ручная сборка подвержена ошибкам",
            "⚠️ Нет beta testing - первые пользователи станут тестировщиками"
        )
    }
    
    private fun calculateCiCdScore(): Int {
        // Нет CI/CD вообще
        return 15
    }
    
    private fun calculateTestingScore(): Int {
        // Нет тестов
        return 5
    }
    
    private fun calculateMonitoringScore(): Int {
        // Нет мониторинга
        return 10
    }
    
    private fun generateRoadmap(missing: List<MissingComponent>): List<RoadmapItem> {
        return listOf(
            RoadmapItem(
                phase = "Фаза 1: Foundation (2-3 недели)",
                duration = "2-3 недели",
                tasks = listOf(
                    "Настроить CI/CD pipeline",
                    "Добавить unit tests (70% покрытие)",
                    "Настроить signing конфигурацию",
                    "Настроить Google Play Console"
                ),
                deliverables = listOf(
                    "Автоматическая сборка APK",
                    "Unit test suite",
                    "Подписанный релизный APK"
                )
            ),
            
            RoadmapItem(
                phase = "Фаза 2: Quality (2-3 недели)",
                duration = "2-3 недели",
                tasks = listOf(
                    "Добавить integration tests",
                    "Добавить UI tests",
                    "Настроить Firebase Crashlytics",
                    "Провести security audit"
                ),
                deliverables = listOf(
                    "Test suite с >80% покрытием",
                    "Мониторинг крашей",
                    "Security report"
                )
            ),
            
            RoadmapItem(
                phase = "Фаза 3: Beta (1-2 недели)",
                duration = "1-2 недели",
                tasks = listOf(
                    "Настроить Firebase App Distribution",
                    "Пригласить beta testers (20-50 человек)",
                    "Собрать feedback",
                    "Исправить критические баги"
                ),
                deliverables = listOf(
                    "Beta версия протестирована",
                    "Список исправленных багов",
                    "Подтверждение готовности к релизу"
                )
            ),
            
            RoadmapItem(
                phase = "Фаза 4: Launch (1 неделя)",
                duration = "1 неделя",
                tasks = listOf(
                    "Подготовить store listing",
                    "Создать скриншоты и видео",
                    "Написать описание",
                    "Опубликовать в Google Play"
                ),
                deliverables = listOf(
                    "Приложение в Google Play",
                    "Первая установка из магазина"
                )
            ),
            
            RoadmapItem(
                phase = "Фаза 5: Post-Launch (ongoing)",
                duration = "Постоянно",
                tasks = listOf(
                    "Мониторинг крашей и аналитики",
                    "Сбор отзывов пользователей",
                    "Планирование новых фич",
                    "Регулярные обновления"
                ),
                deliverables = listOf(
                    "Стабильное приложение",
                    "Довольные пользователи",
                    "Растущая база установок"
                )
            )
        )
    }
    
    private fun estimateEffort(missing: List<MissingComponent>): EffortEstimate {
        val phases = mapOf(
            "Foundation" to 3,
            "Quality" to 3,
            "Beta" to 2,
            "Launch" to 1
        )
        
        return EffortEstimate(
            totalWeeks = phases.values.sum(),
            developers = 2,
            phases = phases
        )
    }
    
    private fun generateDevOpsRecommendations(missing: List<MissingComponent>): List<String> {
        val critical = missing.filter { it.priority == Priority.CRITICAL }
        
        return listOf(
            "🚨 КРИТИЧНО: Начать с настройки CI/CD и unit tests",
            "🚨 КРИТИЧНО: Настроить signing до первого релиза",
            "⚡ ВЫСОКО: Добавить Firebase Crashlytics для мониторинга",
            "⚡ ВЫСОКО: Подготовить Privacy Policy для Google Play",
            "📋 СРЕДНЕ: Настроить beta distribution через Firebase",
            "📋 СРЕДНЕ: Оптимизировать размер APK с ProGuard",
            "💡 НИЗКО: Добавить in-app updates",
            "💡 НИЗКО: Автоматизировать changelog"
        )
    }
}
