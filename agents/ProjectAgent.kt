package com.autodiag.ai.agents

/**
 * Базовый интерфейс для всех агентов анализа
 */
interface ProjectAgent {
    val name: String
    val expertise: List<String>
}

/**
 * Заглушка для проекта (в реальности здесь был бы анализ файлов)
 */
class AutoDiagProject {
    val name = "AutoDiagAI"
    val version = "1.0.0"
    val language = "Kotlin"
    val platform = "Android"
    val totalFiles = 49
    val totalLinesOfCode = 8500  // Примерная оценка
}
