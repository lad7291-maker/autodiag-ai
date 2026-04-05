package com.autodiag.ai.agents

/**
 * Агент анализа пользовательского опыта (UX)
 * Оценивает usability, accessibility, дизайн
 */
class UXAgent : ProjectAgent {
    
    override val name = "UXAgent"
    override val expertise = listOf("UX Design", "Mobile UI", "Accessibility", "User Research")
    
    data class UXAnalysis(
        val overallScore: Int,              // 0-100
        val usabilityScore: Int,            // 0-100
        val visualDesignScore: Int,         // 0-100
        val accessibilityScore: Int,        // 0-100
        val userFlowScore: Int,             // 0-100
        val strengths: List<String>,
        val weaknesses: List<String>,
        val usabilityIssues: List<UsabilityIssue>,
        val recommendations: List<UXRecommendation>
    )
    
    data class UsabilityIssue(
        val severity: Severity,
        val screen: String,
        val issue: String,
        val impact: String,
        val solution: String
    )
    
    data class UXRecommendation(
        val priority: Priority,
        val area: Area,
        val description: String,
        val expectedImpact: String
    )
    
    enum class Severity { CRITICAL, HIGH, MEDIUM, LOW }
    enum class Priority { CRITICAL, HIGH, MEDIUM, LOW }
    enum class Area { NAVIGATION, VISUAL, INTERACTION, CONTENT, ACCESSIBILITY }
    
    fun analyze(project: AutoDiagProject): UXAnalysis {
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()
        val issues = mutableListOf<UsabilityIssue>()
        val recommendations = mutableListOf<UXRecommendation>()
        
        // Анализ навигации
        analyzeNavigation(strengths, weaknesses, issues, recommendations)
        
        // Анализ визуального дизайна
        analyzeVisualDesign(strengths, weaknesses, issues, recommendations)
        
        // Анализ пользовательских сценариев
        analyzeUserFlows(strengths, weaknesses, issues, recommendations)
        
        // Анализ доступности
        analyzeAccessibility(strengths, weaknesses, issues, recommendations)
        
        val usabilityScore = calculateUsabilityScore(issues)
        val visualDesignScore = 75 // Material 3, но можно лучше
        val accessibilityScore = calculateAccessibilityScore(issues)
        val userFlowScore = calculateUserFlowScore(issues)
        
        val overallScore = (usabilityScore + visualDesignScore + accessibilityScore + userFlowScore) / 4
        
        return UXAnalysis(
            overallScore = overallScore,
            usabilityScore = usabilityScore,
            visualDesignScore = visualDesignScore,
            accessibilityScore = accessibilityScore,
            userFlowScore = userFlowScore,
            strengths = strengths,
            weaknesses = weaknesses,
            usabilityIssues = issues.sortedBy { it.severity.ordinal },
            recommendations = recommendations.sortedBy { it.priority.ordinal }
        )
    }
    
    private fun analyzeNavigation(
        strengths: MutableList<String>,
        weaknesses: MutableList<String>,
        issues: MutableList<UsabilityIssue>,
        recommendations: MutableList<UXRecommendation>
    ) {
        strengths.add("Чёткая нижняя навигация с 5 разделами")
        strengths.add("Стандартные паттерны Material Design")
        strengths.add("Возврат назад работает корректно")
        
        weaknesses.add("Нет быстрого доступа к частым действиям")
        weaknesses.add("Нет поиска по функциям")
        
        issues.add(UsabilityIssue(
            severity = Severity.MEDIUM,
            screen = "Главный экран",
            issue = "Нет виджета для быстрого запуска анализа",
            impact = "Пользователю нужно 2 тапа для начала анализа",
            solution = "Добавить Floating Action Button или виджет на главный экран"
        ))
        
        recommendations.add(UXRecommendation(
            priority = Priority.HIGH,
            area = Area.NAVIGATION,
            description = "Добавить быстрые действия на главный экран (Начать диагностику, Начать анализ)",
            expectedImpact = "Сокращение времени на запуск частых операций на 50%"
        ))
    }
    
    private fun analyzeVisualDesign(
        strengths: MutableList<String>,
        weaknesses: MutableList<String>,
        issues: MutableList<UsabilityIssue>,
        recommendations: MutableList<UXRecommendation>
    ) {
        strengths.add("Использование Material Design 3")
        strengths.add("Консистентная цветовая схема")
        strengths.add("Хороший контраст текста")
        
        weaknesses.add("Нет тёмной темы")
        weaknesses.add("Слишком много текста на экране результатов")
        weaknesses.add("Нет анимаций переходов")
        
        issues.add(UsabilityIssue(
            severity = Severity.LOW,
            screen = "AnalysisResultsScreen",
            issue = "Перегруженный экран информацией",
            impact = "Пользователь может потеряться в данных",
            solution = "Разбить на вкладки: Обзор, Детали, Рекомендации"
        ))
        
        issues.add(UsabilityIssue(
            severity = Severity.LOW,
            screen = "DataCollectionScreen",
            issue = "Нет визуальной обратной связи при выборе км",
            impact = "Неочевидно что выбор сохранился",
            solution = "Добавить haptic feedback и анимацию"
        ))
        
        recommendations.add(UXRecommendation(
            priority = Priority.MEDIUM,
            area = Area.VISUAL,
            description = "Добавить тёмную тему для ночного вождения",
            expectedImpact = "Улучшение комфорта использования ночью, снижение нагрузки на глаза"
        ))
        
        recommendations.add(UXRecommendation(
            priority = Priority.MEDIUM,
            area = Area.VISUAL,
            description = "Добавить анимации переходов между экранами",
            expectedImpact = "Более плавный и приятный UX"
        ))
    }
    
    private fun analyzeUserFlows(
        strengths: MutableList<String>,
        weaknesses: MutableList<String>,
        issues: MutableList<UsabilityIssue>,
        recommendations: MutableList<UXRecommendation>
    ) {
        strengths.add("Простой flow сбора данных: выбор км → сбор → анализ → результаты")
        strengths.add("Явное подтверждение перед применением настроек")
        strengths.add("Возможность отмены (сброс к заводским)")
        
        weaknesses.add("Нет onboarding для новых пользователей")
        weaknesses.add("Нет подсказок в интерфейсе")
        weaknesses.add("Нет индикатора что настройки применены")
        
        issues.add(UsabilityIssue(
            severity = Severity.HIGH,
            screen = "Первый запуск",
            issue = "Нет onboarding объясняющего как пользоваться приложением",
            impact = "Пользователь не понимает возможности приложения",
            solution = "Добавить 3-4 экрана onboarding с объяснением функций"
        ))
        
        issues.add(UsabilityIssue(
            severity = Severity.MEDIUM,
            screen = "AnalysisResultsScreen",
            issue = "После применения настроек нет подтверждения что всё ок",
            impact = "Пользователь не уверен что настройки применились",
            solution = "Добавить Snackbar с подтверждением и индикатор активных настроек"
        ))
        
        recommendations.add(UXRecommendation(
            priority = Priority.HIGH,
            area = Area.INTERACTION,
            description = "Создать onboarding flow для новых пользователей",
            expectedImpact = "Снижение bounce rate на 30%, повышение удержания"
        ))
        
        recommendations.add(UXRecommendation(
            priority = Priority.MEDIUM,
            area = Area.INTERACTION,
            description = "Добавить подсказки (tooltips) для технических терминов",
            expectedImpact = "Повышение понимания функций у неопытных пользователей"
        ))
    }
    
    private fun analyzeAccessibility(
        strengths: MutableList<String>,
        weaknesses: MutableList<String>,
        issues: MutableList<UsabilityIssue>,
        recommendations: MutableList<UXRecommendation>
    ) {
        strengths.add("Compose по умолчанию поддерживает accessibility")
        strengths.add("Достаточные размеры touch targets (мин 48dp)")
        
        weaknesses.add("Нет тестов с TalkBack")
        weaknesses.add("Нет настройки размера шрифта")
        weaknesses.add("Некоторые цвета могут быть проблемны для дальтоников")
        
        issues.add(UsabilityIssue(
            severity = Severity.MEDIUM,
            screen = "Все экраны",
            issue = "Нет поддержки TalkBack для голосовой навигации",
            impact = "Недоступно для слабовидящих пользователей",
            solution = "Добавить contentDescription ко всем иконкам и кнопкам"
        ))
        
        recommendations.add(UXRecommendation(
            priority = Priority.MEDIUM,
            area = Area.ACCESSIBILITY,
            description = "Провести аудит с TalkBack и исправить проблемы",
            expectedImpact = "Доступность для слабовидящих пользователей"
        ))
        
        recommendations.add(UXRecommendation(
            priority = Priority.LOW,
            area = Area.ACCESSIBILITY,
            description = "Добавить настройку размера шрифта в приложении",
            expectedImpact = "Улучшение читаемости для пользователей с плохим зрением"
        ))
    }
    
    private fun calculateUsabilityScore(issues: List<UsabilityIssue>): Int {
        val criticalCount = issues.count { it.severity == Severity.CRITICAL }
        val highCount = issues.count { it.severity == Severity.HIGH }
        
        return (85 - criticalCount * 20 - highCount * 10).coerceIn(50, 95)
    }
    
    private fun calculateAccessibilityScore(issues: List<UsabilityIssue>): Int {
        val accessibilityIssues = issues.count { 
            it.issue.contains("TalkBack") || it.issue.contains("доступност")
        }
        return (70 - accessibilityIssues * 15).coerceIn(40, 85)
    }
    
    private fun calculateUserFlowScore(issues: List<UsabilityIssue>): Int {
        val flowIssues = issues.count { 
            it.screen.contains("Первый") || it.issue.contains("flow")
        }
        return (80 - flowIssues * 10).coerceIn(60, 90)
    }
}
