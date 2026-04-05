package com.autodiag.ai.agents

/**
 * Агент исследования рынка
 * Сравнивает приложение с лучшими мировыми конкурентами
 */
class MarketResearchAgent : ProjectAgent {
    
    override val name = "MarketResearchAgent"
    override val expertise = listOf("Market Analysis", "Competitive Intelligence", "Automotive Apps", "OBD2 Market")
    
    data class MarketAnalysis(
        val overallCompetitiveScore: Int,   // 0-100
        val featureComparison: FeatureComparison,
        val marketPosition: MarketPosition,
        val topCompetitors: List<Competitor>,
        val uniqueSellingPoints: List<String>,
        val gaps: List<MarketGap>,
        val opportunities: List<Opportunity>,
        val recommendations: List<String>
    )
    
    data class FeatureComparison(
        val ourFeatures: List<Feature>,
        val competitorFeatures: Map<String, List<Feature>>
    )
    
    data class Feature(
        val name: String,
        val isImplemented: Boolean,
        val quality: FeatureQuality
    )
    
    enum class FeatureQuality { NONE, BASIC, GOOD, EXCELLENT }
    
    data class MarketPosition(
        val segment: String,
        val targetAudience: String,
        val differentiation: String,
        val pricePosition: PricePosition
    )
    
    enum class PricePosition { FREE, FREEMIUM, PAID, PREMIUM }
    
    data class Competitor(
        val name: String,
        val platform: String,
        val rating: Float,
        val downloads: String,
        val strengths: List<String>,
        val weaknesses: List<String>,
        val features: List<String>,
        val score: Int  // 0-100
    )
    
    data class MarketGap(
        val feature: String,
        val demand: Demand,
        val ourStatus: ImplementationStatus
    )
    
    enum class Demand { LOW, MEDIUM, HIGH, CRITICAL }
    enum class ImplementationStatus { NOT_PLANNED, PLANNED, IN_PROGRESS, IMPLEMENTED }
    
    data class Opportunity(
        val description: String,
        val potential: Potential,
        val effort: Effort
    )
    
    enum class Potential { LOW, MEDIUM, HIGH }
    enum class Effort { LOW, MEDIUM, HIGH }
    
    fun analyze(project: AutoDiagProject): MarketAnalysis {
        // Анализ конкурентов
        val competitors = analyzeCompetitors()
        
        // Сравнение функций
        val featureComparison = compareFeatures(competitors)
        
        // Определение позиции на рынке
        val marketPosition = determineMarketPosition()
        
        // Поиск пробелов на рынке
        val gaps = identifyMarketGaps(competitors)
        
        // Возможности
        val opportunities = identifyOpportunities(gaps)
        
        // Уникальные преимущества
        val usps = identifyUSPs(competitors)
        
        // Итоговая оценка
        val competitiveScore = calculateCompetitiveScore(featureComparison, competitors)
        
        return MarketAnalysis(
            overallCompetitiveScore = competitiveScore,
            featureComparison = featureComparison,
            marketPosition = marketPosition,
            topCompetitors = competitors.sortedByDescending { it.score }.take(5),
            uniqueSellingPoints = usps,
            gaps = gaps,
            opportunities = opportunities,
            recommendations = generateMarketRecommendations(gaps, opportunities)
        )
    }
    
    private fun analyzeCompetitors(): List<Competitor> {
        return listOf(
            Competitor(
                name = "Torque Pro",
                platform = "Android",
                rating = 4.4f,
                downloads = "10M+",
                strengths = listOf(
                    "Поддержка огромного количества PID",
                    "Кастомизируемые дашборды",
                    "Графики в реальном времени",
                    "Экспорт данных"
                ),
                weaknesses = listOf(
                    "Устаревший UI",
                    "Нет AI анализа",
                    "Сложная настройка",
                    "Нет адаптации под стиль вождения"
                ),
                features = listOf(
                    "OBD2 диагностика", "Графики", "Дашборды", 
                    "Логирование", "DTC коды", "Экспорт"
                ),
                score = 85
            ),
            
            Competitor(
                name = "Car Scanner ELM OBD2",
                platform = "Android/iOS",
                rating = 4.6f,
                downloads = "5M+",
                strengths = listOf(
                    "Современный Material Design UI",
                    "Поддержка множества автомобилей",
                    "Детальная информация о датчиках",
                    "Хорошая локализация"
                ),
                weaknesses = listOf(
                    "Нет AI рекомендаций",
                    "Нет адаптации двигателя",
                    "Ограниченная кастомизация"
                ),
                features = listOf(
                    "OBD2 диагностика", "Дашборды", "DTC коды",
                    "Тесты автомобиля", "Логирование"
                ),
                score = 80
            ),
            
            Competitor(
                name = "OBD Auto Doctor",
                platform = "Android/iOS/Desktop",
                rating = 4.2f,
                downloads = "1M+",
                strengths = listOf(
                    "Кросс-платформенность",
                    "Профессиональный уровень",
                    "Подробные описания DTC"
                ),
                weaknesses = listOf(
                    "Платное (дорогое)",
                    "Нет AI",
                    "Сложный для обычных пользователей"
                ),
                features = listOf(
                    "OBD2 диагностика", "DTC коды", "Freeze frame",
                    "Oxygen sensors", "Emissions"
                ),
                score = 75
            ),
            
            Competitor(
                name = "DashCommand",
                platform = "Android/iOS",
                rating = 3.9f,
                downloads = "500K+",
                strengths = listOf(
                    "Красивые визуализации",
                    "Drag-and-drop дашборд",
                    "Performance тесты"
                ),
                weaknesses = listOf(
                    "Платное",
                    "Устаревший UI",
                    "Нет AI анализа"
                ),
                features = listOf(
                    "OBD2 диагностика", "Дашборды", "Performance тесты",
                    "Логирование", "Skins"
                ),
                score = 70
            ),
            
            Competitor(
                name = "inCarDoc",
                platform = "Android",
                rating = 4.3f,
                downloads = "1M+",
                strengths = listOf(
                    "Простой интерфейс",
                    "Хорош для начинающих",
                    "Трип компьютер"
                ),
                weaknesses = listOf(
                    "Ограниченная функциональность",
                    "Нет глубокой диагностики",
                    "Нет AI"
                ),
                features = listOf(
                    "OBD2 диагностика", "Трип компьютер", "DTC коды"
                ),
                score = 65
            ),
            
            Competitor(
                name = "HobDrive",
                platform = "Android",
                rating = 4.5f,
                downloads = "100K+",
                strengths = listOf(
                    "Российская разработка",
                    "Поддержка отечественных авто",
                    "Хорошая локализация",
                    "Адаптация под Lada"
                ),
                weaknesses = listOf(
                    "Устаревший UI",
                    "Мало пользователей",
                    "Нет AI анализа"
                ),
                features = listOf(
                    "OBD2 диагностика", "Поддержка ВАЗ/УАЗ/ГАЗ",
                    "Трип компьютер", "DTC коды"
                ),
                score = 72
            )
        )
    }
    
    private fun compareFeatures(competitors: List<Competitor>): FeatureComparison {
        val ourFeatures = listOf(
            Feature("OBD2 Диагностика", true, FeatureQuality.GOOD),
            Feature("AI Анализ стиля вождения", true, FeatureQuality.EXCELLENT),
            Feature("Адаптация двигателя", true, FeatureQuality.GOOD),
            Feature("Голосовые команды", true, FeatureQuality.BASIC),
            Feature("Дашборды", false, FeatureQuality.NONE),
            Feature("Графики параметров", false, FeatureQuality.NONE),
            Feature("DTC База кодов", true, FeatureQuality.GOOD),
            Feature("История анализов", true, FeatureQuality.GOOD),
            Feature("Экспорт данных", false, FeatureQuality.NONE),
            Feature("Тёмная тема", false, FeatureQuality.NONE),
            Feature("Поддержка русских авто", true, FeatureQuality.EXCELLENT),
            Feature("Безопасная архитектура", true, FeatureQuality.EXCELLENT)
        )
        
        val competitorFeatures = competitors.associate { competitor ->
            competitor.name to competitor.features.map { 
                Feature(it, true, FeatureQuality.GOOD) 
            }
        }
        
        return FeatureComparison(ourFeatures, competitorFeatures)
    }
    
    private fun determineMarketPosition(): MarketPosition {
        return MarketPosition(
            segment = "OBD2 Диагностика с AI",
            targetAudience = "Владельцы российских автомобилей (ВАЗ, УАЗ, ГАЗ), энтузиасты тюнинга",
            differentiation = "Единственное приложение с AI-адаптацией под стиль вождения для русских авто",
            pricePosition = PricePosition.FREE
        )
    }
    
    private fun identifyMarketGaps(competitors: List<Competitor>): List<MarketGap> {
        return listOf(
            MarketGap(
                feature = "AI Анализ и адаптация",
                demand = Demand.HIGH,
                ourStatus = ImplementationStatus.IMPLEMENTED
            ),
            MarketGap(
                feature = "Поддержка российских авто (ВАЗ/УАЗ/ГАЗ)",
                demand = Demand.HIGH,
                ourStatus = ImplementationStatus.IMPLEMENTED
            ),
            MarketGap(
                feature = "Голосовое управление",
                demand = Demand.MEDIUM,
                ourStatus = ImplementationStatus.IMPLEMENTED
            ),
            MarketGap(
                feature = "Предиктивное обслуживание",
                demand = Demand.HIGH,
                ourStatus = ImplementationStatus.PLANNED
            ),
            MarketGap(
                feature = "Сообщество/обмен настройками",
                demand = Demand.MEDIUM,
                ourStatus = ImplementationStatus.NOT_PLANNED
            ),
            MarketGap(
                feature = "Облачная аналитика",
                demand = Demand.LOW,
                ourStatus = ImplementationStatus.NOT_PLANNED
            ),
            MarketGap(
                feature = "Интеграция с умным домом",
                demand = Demand.LOW,
                ourStatus = ImplementationStatus.NOT_PLANNED
            )
        )
    }
    
    private fun identifyOpportunities(gaps: List<MarketGap>): List<Opportunity> {
        return listOf(
            Opportunity(
                description = "Добавить предиктивное обслуживание на основе AI анализа",
                potential = Potential.HIGH,
                effort = Effort.MEDIUM
            ),
            Opportunity(
                description = "Создать сообщество для обмена настройками",
                potential = Potential.MEDIUM,
                effort = Effort.HIGH
            ),
            Opportunity(
                description = "Добавить графики и визуализацию данных",
                potential = Potential.MEDIUM,
                effort = Effort.LOW
            ),
            Opportunity(
                description = "Интеграция с популярными OBD2 адаптерами",
                potential = Potential.HIGH,
                effort = Effort.MEDIUM
            ),
            Opportunity(
                description = "Виджет для главного экрана Android",
                potential = Potential.MEDIUM,
                effort = Effort.LOW
            )
        )
    }
    
    private fun identifyUSPs(competitors: List<Competitor>): List<String> {
        return listOf(
            "Единственное приложение с AI-адаптацией под стиль вождения",
            "Специализировано на российских автомобилях (ВАЗ, УАЗ, ГАЗ)",
            "Безопасная архитектура - водитель контролирует все изменения",
            "Голосовое управление на русском языке",
            "Бесплатное с открытым исходным кодом"
        )
    }
    
    private fun calculateCompetitiveScore(
        comparison: FeatureComparison,
        competitors: List<Competitor>
    ): Int {
        val ourUniqueFeatures = comparison.ourFeatures.count { 
            it.isImplemented && it.quality == FeatureQuality.EXCELLENT 
        }
        val avgCompetitorScore = competitors.map { it.score }.average()
        
        // У нас есть уникальные фичи, но меньше базовых
        return (ourUniqueFeatures * 15 + 50).coerceIn(60, 85)
    }
    
    private fun generateMarketRecommendations(
        gaps: List<MarketGap>,
        opportunities: List<Opportunity>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Приоритет на HIGH demand gaps
        gaps.filter { it.demand == Demand.HIGH && it.ourStatus != ImplementationStatus.IMPLEMENTED }
            .forEach {
                recommendations.add("[ВЫСОКИЙ ПРИОРИТЕТ] Реализовать: ${it.feature}")
            }
        
        // Возможности с высоким потенциалом и низкими усилиями
        opportunities.filter { it.potential == Potential.HIGH && it.effort == Effort.LOW }
            .forEach {
                recommendations.add("[БЫСТРАЯ ПОБЕДА] ${it.description}")
            }
        
        return recommendations
    }
}
