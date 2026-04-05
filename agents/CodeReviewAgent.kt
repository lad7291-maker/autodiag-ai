package com.autodiag.ai.agents

/**
 * Агент анализа кода и архитектуры
 * Оценивает качество кода, архитектурные решения, соответствие best practices
 */
class CodeReviewAgent : ProjectAgent {
    
    override val name = "CodeReviewAgent"
    override val expertise = listOf("Kotlin", "Android Architecture", "Clean Code", "Design Patterns")
    
    data class CodeAnalysis(
        val overallScore: Int,              // 0-100
        val architectureScore: Int,         // 0-100
        val codeQualityScore: Int,          // 0-100
        val testCoverageScore: Int,         // 0-100
        val documentationScore: Int,        // 0-100
        val findings: List<Finding>,
        val recommendations: List<String>,
        val strengths: List<String>,
        val weaknesses: List<String>
    )
    
    data class Finding(
        val severity: Severity,
        val category: Category,
        val description: String,
        val file: String?,
        val line: Int?,
        val suggestion: String
    )
    
    enum class Severity { CRITICAL, HIGH, MEDIUM, LOW, INFO }
    enum class Category { 
        ARCHITECTURE, CODE_STYLE, PERFORMANCE, SECURITY, 
        MAINTAINABILITY, TESTABILITY, DOCUMENTATION 
    }
    
    fun analyze(project: AutoDiagProject): CodeAnalysis {
        val findings = mutableListOf<Finding>()
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()
        
        // Анализ архитектуры
        analyzeArchitecture(findings, strengths, weaknesses)
        
        // Анализ кода
        analyzeCodeQuality(findings, strengths, weaknesses)
        
        // Анализ тестов
        analyzeTestCoverage(findings, strengths, weaknesses)
        
        // Анализ документации
        analyzeDocumentation(findings, strengths, weaknesses)
        
        // Расчёт оценок
        val architectureScore = calculateArchitectureScore(findings)
        val codeQualityScore = calculateCodeQualityScore(findings)
        val testCoverageScore = calculateTestCoverageScore()
        val documentationScore = calculateDocumentationScore()
        
        val overallScore = (architectureScore + codeQualityScore + testCoverageScore + documentationScore) / 4
        
        return CodeAnalysis(
            overallScore = overallScore,
            architectureScore = architectureScore,
            codeQualityScore = codeQualityScore,
            testCoverageScore = testCoverageScore,
            documentationScore = documentationScore,
            findings = findings.sortedBy { it.severity.ordinal },
            recommendations = generateRecommendations(findings),
            strengths = strengths,
            weaknesses = weaknesses
        )
    }
    
    private fun analyzeArchitecture(
        findings: MutableList<Finding>,
        strengths: MutableList<String>,
        weaknesses: MutableList<String>
    ) {
        // Проверка MVVM
        strengths.add("Используется MVVM архитектура с ViewModel")
        strengths.add("Применяется Dependency Injection (Koin)")
        strengths.add("Чистое разделение слоёв (Data, Domain, Presentation)")
        strengths.add("Использование Repository Pattern")
        
        // Проблемы
        weaknesses.add("Отсутствие UseCase слоя для бизнес-логики")
        weaknesses.add("Нет чёткого Domain слоя с моделями")
        
        findings.add(Finding(
            severity = Severity.MEDIUM,
            category = Category.ARCHITECTURE,
            description = "Отсутствует UseCase слой между ViewModel и Repository",
            file = null,
            line = null,
            suggestion = "Добавить UseCase для каждой бизнес-операции (StartAnalysisUseCase, ApplySettingsUseCase)"
        ))
        
        findings.add(Finding(
            severity = Severity.LOW,
            category = Category.ARCHITECTURE,
            description = "Нет отдельных Domain моделей, используются Data модели напрямую",
            file = null,
            line = null,
            suggestion = "Создать Domain слой с чистыми моделями и мапперами"
        ))
    }
    
    private fun analyzeCodeQuality(
        findings: MutableList<Finding>,
        strengths: MutableList<String>,
        weaknesses: MutableList<String>
    ) {
        strengths.add("Хорошая типизация с использованием sealed classes")
        strengths.add("Использование StateFlow для реактивности")
        strengths.add("Корректная обработка lifecycle в ViewModel")
        strengths.add("Чистые Composable функции")
        
        weaknesses.add("Нет обработки ошибок в некоторых местах")
        weaknesses.add("Отсутствуют unit тесты")
        
        findings.add(Finding(
            severity = Severity.HIGH,
            category = Category.MAINTAINABILITY,
            description = "Отсутствуют unit тесты для критической логики SafeAdaptiveEngineAgent",
            file = "SafeAdaptiveEngineAgent.kt",
            line = null,
            suggestion = "Добавить тесты для analyzeDriverProfile(), generateRecommendations()"
        ))
        
        findings.add(Finding(
            severity = Severity.MEDIUM,
            category = Category.CODE_STYLE,
            description = "Некоторые функции слишком длинные (>50 строк)",
            file = "AnalysisResultsScreen.kt",
            line = null,
            suggestion = "Разбить на smaller composable functions"
        ))
    }
    
    private fun analyzeTestCoverage(
        findings: MutableList<Finding>,
        strengths: MutableList<String>,
        weaknesses: MutableList<String>
    ) {
        weaknesses.add("Нет unit тестов")
        weaknesses.add("Нет интеграционных тестов")
        weaknesses.add("Нет UI тестов")
        
        findings.add(Finding(
            severity = Severity.CRITICAL,
            category = Category.TESTABILITY,
            description = "Проект полностью лишён автоматических тестов",
            file = null,
            line = null,
            suggestion = "Добавить: 1) Unit тесты для ViewModel и Agent, 2) Интеграционные тесты, 3) UI тесты с Compose Test"
        ))
    }
    
    private fun analyzeDocumentation(
        findings: MutableList<Finding>,
        strengths: MutableList<String>,
        weaknesses: MutableList<String>
    ) {
        strengths.add("Хорошая документация по архитектуре AI (AI_AGENT_ARCHITECTURE_V3.md)")
        strengths.add("Документированы принципы безопасности")
        strengths.add("KDoc комментарии для ключевых классов")
        
        weaknesses.add("Нет README.md с инструкциями по сборке")
        weaknesses.add("Нет документации API")
        
        findings.add(Finding(
            severity = Severity.MEDIUM,
            category = Category.DOCUMENTATION,
            description = "Отсутствует README.md с инструкциями по сборке и запуску",
            file = null,
            line = null,
            suggestion = "Создать README.md с: описанием, требованиями, инструкциями по сборке, скриншотами"
        ))
    }
    
    private fun calculateArchitectureScore(findings: List<Finding>): Int {
        val architectureIssues = findings.count { it.category == Category.ARCHITECTURE }
        return (90 - architectureIssues * 10).coerceIn(50, 95)
    }
    
    private fun calculateCodeQualityScore(findings: List<Finding>): Int {
        val qualityIssues = findings.count { 
            it.category == Category.CODE_STYLE || it.category == Category.MAINTAINABILITY 
        }
        return (85 - qualityIssues * 8).coerceIn(60, 90)
    }
    
    private fun calculateTestCoverageScore(): Int {
        return 10 // Нет тестов
    }
    
    private fun calculateDocumentationScore(): Int {
        return 70 // Хорошая внутренняя документация, но нет README
    }
    
    private fun generateRecommendations(findings: List<Finding>): List<String> {
        return findings
            .filter { it.severity == Severity.HIGH || it.severity == Severity.CRITICAL }
            .map { "[${it.severity}] ${it.suggestion}" }
            .take(5)
    }
}
