package com.autodiag.ai.agents

/**
 * Агент консенсуса
 * Собирает мнения всех агентов и формирует итоговый вердикт
 */
class ConsensusAgent : ProjectAgent {
    
    override val name = "ConsensusAgent"
    override val expertise = listOf("Project Management", "Strategic Planning", "Risk Assessment")
    
    data class ConsensusReport(
        val overallProjectScore: Int,           // 0-100
        val agentScores: Map<String, Int>,      // Agent name -> score
        val finalVerdict: FinalVerdict,
        val priorityActions: List<PriorityAction>,
        val strengths: List<String>,
        val criticalIssues: List<String>,
        val marketPosition: MarketAssessment,
        val releaseReadiness: ReleaseReadiness,
        val longTermOutlook: LongTermOutlook
    )
    
    data class FinalVerdict(
        val decision: Decision,
        val confidence: Int,                    // 0-100
        val summary: String,
        val detailedReasoning: List<String>
    )
    
    enum class Decision {
        READY_FOR_RELEASE,          // Готов к релизу
        READY_WITH_RESERVATIONS,    // Готов с оговорками
        NEEDS_IMPROVEMENT,          // Требует доработки
        NOT_READY                   // Не готов
    }
    
    data class PriorityAction(
        val priority: ActionPriority,
        val category: String,
        val action: String,
        val estimatedEffort: String,
        val impact: String
    )
    
    enum class ActionPriority { P0_CRITICAL, P1_HIGH, P2_MEDIUM, P3_LOW }
    
    data class MarketAssessment(
        val competitivePosition: String,
        val uniqueValue: String,
        val targetMarketFit: Int,               // 0-100
        val growthPotential: Int                // 0-100
    )
    
    data class ReleaseReadiness(
        val isReady: Boolean,
        val blockers: List<String>,
        val estimatedTimeToRelease: String,
        val riskLevel: RiskLevel
    )
    
    enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }
    
    data class LongTermOutlook(
        val sustainability: Int,                // 0-100
        val scalability: Int,                   // 0-100
        val maintenanceEffort: String,
        val keyChallenges: List<String>
    )
    
    fun reachConsensus(
        codeAnalysis: CodeReviewAgent.CodeAnalysis,
        securityAnalysis: SecurityAgent.SecurityAnalysis,
        uxAnalysis: UXAgent.UXAnalysis,
        marketAnalysis: MarketResearchAgent.MarketAnalysis,
        devOpsAnalysis: DevOpsAgent.DevOpsAnalysis
    ): ConsensusReport {
        
        // Собираем оценки от всех агентов
        val agentScores = mapOf(
            "CodeReview" to codeAnalysis.overallScore,
            "Security" to securityAnalysis.overallScore,
            "UX" to uxAnalysis.overallScore,
            "Market" to marketAnalysis.overallCompetitiveScore,
            "DevOps" to devOpsAnalysis.overallScore
        )
        
        // Взвешенная общая оценка
        val weightedScores = mapOf(
            "CodeReview" to codeAnalysis.overallScore * 0.20,
            "Security" to securityAnalysis.overallScore * 0.25,  // Критично для OBD2
            "UX" to uxAnalysis.overallScore * 0.20,
            "Market" to marketAnalysis.overallCompetitiveScore * 0.15,
            "DevOps" to devOpsAnalysis.overallScore * 0.20
        )
        
        val overallScore = weightedScores.values.sum().toInt()
        
        // Формируем вердикт
        val verdict = determineVerdict(
            overallScore,
            securityAnalysis,
            devOpsAnalysis,
            codeAnalysis
        )
        
        // Приоритетные действия
        val priorityActions = compilePriorityActions(
            codeAnalysis,
            securityAnalysis,
            uxAnalysis,
            devOpsAnalysis
        )
        
        // Собираем сильные стороны
        val strengths = compileStrengths(
            codeAnalysis,
            securityAnalysis,
            uxAnalysis,
            marketAnalysis
        )
        
        // Критические проблемы
        val criticalIssues = compileCriticalIssues(
            codeAnalysis,
            securityAnalysis,
            devOpsAnalysis
        )
        
        // Оценка рынка
        val marketAssessment = MarketAssessment(
            competitivePosition = determineCompetitivePosition(marketAnalysis),
            uniqueValue = marketAnalysis.uniqueSellingPoints.firstOrNull() ?: "",
            targetMarketFit = 85,  // Хорошо подходит для целевой аудитории
            growthPotential = 75
        )
        
        // Готовность к релизу
        val releaseReadiness = ReleaseReadiness(
            isReady = devOpsAnalysis.deploymentReadiness.isReady && 
                     securityAnalysis.riskLevel != SecurityAgent.RiskLevel.CRITICAL,
            blockers = devOpsAnalysis.deploymentReadiness.blockers,
            estimatedTimeToRelease = devOpsAnalysis.effortEstimate.let { 
                "${it.totalWeeks} недель с ${it.developers} разработчиками" 
            },
            riskLevel = determineOverallRisk(securityAnalysis, devOpsAnalysis)
        )
        
        // Долгосрочный прогноз
        val longTermOutlook = LongTermOutlook(
            sustainability = calculateSustainability(codeAnalysis, securityAnalysis),
            scalability = calculateScalability(codeAnalysis),
            maintenanceEffort = "Средняя (2-4 часа в неделю)",
            keyChallenges = listOf(
                "Поддержка новых моделей автомобилей",
                "Обновление базы DTC кодов",
                "Конкуренция с крупными игроками",
                "Обеспечение безопасности при росте пользователей"
            )
        )
        
        return ConsensusReport(
            overallProjectScore = overallScore,
            agentScores = agentScores,
            finalVerdict = verdict,
            priorityActions = priorityActions,
            strengths = strengths,
            criticalIssues = criticalIssues,
            marketPosition = marketAssessment,
            releaseReadiness = releaseReadiness,
            longTermOutlook = longTermOutlook
        )
    }
    
    private fun determineVerdict(
        overallScore: Int,
        security: SecurityAgent.SecurityAnalysis,
        devOps: DevOpsAgent.DevOpsAnalysis,
        code: CodeReviewAgent.CodeAnalysis
    ): FinalVerdict {
        val reasoning = mutableListOf<String>()
        
        // Проверяем критические факторы
        val hasCriticalSecurityRisks = security.criticalRisks.any { 
            it.level == SecurityAgent.RiskLevel.CRITICAL 
        }
        val hasReleaseBlockers = devOps.deploymentReadiness.blockers.isNotEmpty()
        val hasCriticalCodeIssues = code.findings.any { 
            it.severity == CodeReviewAgent.Severity.CRITICAL 
        }
        
        return when {
            hasCriticalSecurityRisks -> {
                reasoning.add("Обнаружены критические риски безопасности")
                reasoning.add("Требуется доработка архитектуры безопасности")
                FinalVerdict(
                    decision = Decision.NOT_READY,
                    confidence = 90,
                    summary = "❌ НЕ ГОТОВ К РЕЛИЗУ: Критические риски безопасности",
                    detailedReasoning = reasoning
                )
            }
            
            hasReleaseBlockers -> {
                reasoning.add("Отсутствуют критические компоненты для релиза")
                reasoning.add("Нет CI/CD, тестов, мониторинга")
                FinalVerdict(
                    decision = Decision.NEEDS_IMPROVEMENT,
                    confidence = 85,
                    summary = "⚠️ ТРЕБУЕТ ДОРАБОТКИ: Нет инфраструктуры для продакшена",
                    detailedReasoning = reasoning
                )
            }
            
            overallScore >= 80 -> {
                reasoning.add("Высокое качество кода и архитектуры")
                reasoning.add("Безопасная архитектура для OBD2")
                reasoning.add("Уникальное позиционирование на рынке")
                FinalVerdict(
                    decision = Decision.READY_FOR_RELEASE,
                    confidence = 85,
                    summary = "✅ ГОТОВ К РЕЛИЗУ: Высокое качество, безопасность, уникальность",
                    detailedReasoning = reasoning
                )
            }
            
            overallScore >= 60 -> {
                reasoning.add("Хорошая база, но есть области для улучшения")
                reasoning.add("Требуется добавить тесты и CI/CD")
                FinalVerdict(
                    decision = Decision.READY_WITH_RESERVATIONS,
                    confidence = 75,
                    summary = "✓ ГОТОВ С ОГОВОРКАМИ: Нужны тесты и инфраструктура",
                    detailedReasoning = reasoning
                )
            }
            
            else -> {
                reasoning.add("Низкое качество кода (${code.overallScore}/100)")
                reasoning.add("Недостаточно тестов (${code.testCoverageScore}/100)")
                FinalVerdict(
                    decision = Decision.NEEDS_IMPROVEMENT,
                    confidence = 80,
                    summary = "⚠️ ТРЕБУЕТ ДОРАБОТКИ: Низкое качество кода и тестирования",
                    detailedReasoning = reasoning
                )
            }
        }
    }
    
    private fun compilePriorityActions(
        code: CodeReviewAgent.CodeAnalysis,
        security: SecurityAgent.SecurityAnalysis,
        ux: UXAgent.UXAnalysis,
        devOps: DevOpsAgent.DevOpsAnalysis
    ): List<PriorityAction> {
        val actions = mutableListOf<PriorityAction>()
        
        // P0 - Критические
        devOps.missingComponents
            .filter { it.priority == DevOpsAgent.Priority.CRITICAL }
            .forEach {
                actions.add(PriorityAction(
                    priority = ActionPriority.P0_CRITICAL,
                    category = "DevOps",
                    action = it.name,
                    estimatedEffort = it.estimatedEffort,
                    impact = it.impact
                ))
            }
        
        security.criticalRisks.take(2).forEach { risk ->
            actions.add(PriorityAction(
                priority = ActionPriority.P0_CRITICAL,
                category = "Security",
                action = risk.mitigation,
                estimatedEffort = "3-5 дней",
                impact = risk.impact
            ))
        }
        
        // P1 - Высокие
        code.findings
            .filter { it.severity == CodeReviewAgent.Severity.HIGH }
            .take(3)
            .forEach {
                actions.add(PriorityAction(
                    priority = ActionPriority.P1_HIGH,
                    category = "Code Quality",
                    action = it.suggestion,
                    estimatedEffort = "2-3 дня",
                    impact = "Улучшение качества кода"
                ))
            }
        
        ux.usabilityIssues
            .filter { it.severity == UXAgent.Severity.HIGH }
            .take(2)
            .forEach {
                actions.add(PriorityAction(
                    priority = ActionPriority.P1_HIGH,
                    category = "UX",
                    action = it.solution,
                    estimatedEffort = "2-4 дня",
                    impact = it.impact
                ))
            }
        
        return actions.distinctBy { it.action }.take(10)
    }
    
    private fun compileStrengths(
        code: CodeReviewAgent.CodeAnalysis,
        security: SecurityAgent.SecurityAnalysis,
        ux: UXAgent.UXAnalysis,
        market: MarketResearchAgent.MarketAnalysis
    ): List<String> {
        return mutableListOf<String>().apply {
            addAll(code.strengths.take(3))
            addAll(security.safetyMeasures.filter { it.isImplemented }.map { it.name })
            addAll(ux.strengths.take(2))
            addAll(market.uniqueSellingPoints.take(2))
        }.distinct()
    }
    
    private fun compileCriticalIssues(
        code: CodeReviewAgent.CodeAnalysis,
        security: SecurityAgent.SecurityAnalysis,
        devOps: DevOpsAgent.DevOpsAnalysis
    ): List<String> {
        return mutableListOf<String>().apply {
            addAll(security.criticalRisks.map { "[Security] ${it.description}" })
            addAll(devOps.deploymentReadiness.blockers)
            addAll(code.findings
                .filter { it.severity == CodeReviewAgent.Severity.CRITICAL }
                .map { "[Code] ${it.description}" }
            )
        }
    }
    
    private fun determineCompetitivePosition(market: MarketResearchAgent.MarketAnalysis): String {
        return when {
            market.overallCompetitiveScore >= 80 -> "Лидер ниши"
            market.overallCompetitiveScore >= 70 -> "Сильный конкурент"
            market.overallCompetitiveScore >= 60 -> "Средний игрок"
            else -> "Новичок"
        }
    }
    
    private fun determineOverallRisk(
        security: SecurityAgent.SecurityAnalysis,
        devOps: DevOpsAgent.DevOpsAnalysis
    ): RiskLevel {
        return when {
            security.riskLevel == SecurityAgent.RiskLevel.CRITICAL -> RiskLevel.CRITICAL
            security.riskLevel == SecurityAgent.RiskLevel.HIGH -> RiskLevel.HIGH
            devOps.deploymentReadiness.blockers.isNotEmpty() -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
    
    private fun calculateSustainability(
        code: CodeReviewAgent.CodeAnalysis,
        security: SecurityAgent.SecurityAnalysis
    ): Int {
        val codeScore = code.architectureScore
        val securityScore = security.safetyScore
        return ((codeScore + securityScore) / 2).coerceIn(50, 95)
    }
    
    private fun calculateScalability(code: CodeReviewAgent.CodeAnalysis): Int {
        return code.architectureScore.coerceIn(60, 90)
    }
}
