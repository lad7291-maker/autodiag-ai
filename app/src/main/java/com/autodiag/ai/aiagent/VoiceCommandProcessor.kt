package com.autodiag.ai.aiagent

import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Процессор голосовых команд на русском языке
 * 
 * Команды для безопасного управления:
 * - Водитель сам решает когда и что делать
 * - AI только анализирует и рекомендует
 */
class VoiceCommandProcessor(
    private val agentCore: AIAgentCore
) {
    private var textToSpeech: TextToSpeech? = null
    
    // Словарь команд
    private val commandPatterns = listOf(
        // Сбор данных - водитель выбирает километраж
        CommandPattern(
            keywords = listOf(
                "начни сбор", "собирай данные", "начать сбор", 
                "проанализируй", "проанализировать", "исследуй"
            ),
            action = { text -> 
                val km = extractKilometers(text) ?: 10
                ProcessedCommand.StartDataCollection(km)
            },
            description = "Начать сбор данных для анализа"
        ),
        
        // Остановить сбор и проанализировать
        CommandPattern(
            keywords = listOf(
                "останови сбор", "останови", "хватит собирать",
                "проанализируй", "анализ", "результат", "готово"
            ),
            action = { ProcessedCommand.StopAndAnalyze },
            description = "Остановить сбор и проанализировать данные"
        ),
        
        // Показать рекомендации
        CommandPattern(
            keywords = listOf(
                "покажи рекомендации", "рекомендации", "что рекомендуешь",
                "подробности", "детали", "обоснование", "почему"
            ),
            action = { ProcessedCommand.ShowRecommendations },
            description = "Показать рекомендации по настройке"
        ),
        
        // Применить настройки
        CommandPattern(
            keywords = listOf(
                "примени", "примени настройки", "включи", "активируй",
                "сделай", "настрой", "установи"
            ),
            action = { ProcessedCommand.ApplySettings },
            description = "Применить рекомендуемые настройки"
        ),
        
        // Сбросить настройки
        CommandPattern(
            keywords = listOf(
                "сбрось", "сбрось настройки", "верни заводские",
                "отмени", "назад", "заводские"
            ),
            action = { ProcessedCommand.ResetSettings },
            description = "Сбросить настройки к заводским"
        ),
        
        // Диагностика
        CommandPattern(
            keywords = listOf(
                "диагностика", "проверь", "проверка", "сканируй", 
                "ошибки", "коды", "что с машиной", "как дела",
                "проверь машину", "проверь двигатель", "диагностику"
            ),
            action = { ProcessedCommand.RunDiagnostics },
            description = "Запустить диагностику"
        ),
        
        // Статус двигателя
        CommandPattern(
            keywords = listOf(
                "состояние", "статус", "здоровье", "нормально",
                "как работает", "что показывает", "параметры"
            ),
            action = { ProcessedCommand.GetEngineStatus },
            description = "Получить статус двигателя"
        ),
        
        // Предсказания
        CommandPattern(
            keywords = listOf(
                "предсказание", "поломки", "что сломается", 
                "что ждет", "когда менять", "предупреди",
                "какие поломки", "что ожидается", "обслуживание"
            ),
            action = { ProcessedCommand.GetPredictions },
            description = "Получить предсказания о поломках"
        ),
        
        // Очистить предупреждения
        CommandPattern(
            keywords = listOf(
                "очисти", "убери", "сбрось предупреждения",
                "понял", "ясно", "хорошо"
            ),
            action = { ProcessedCommand.ClearAlerts },
            description = "Очистить голосовые предупреждения"
        )
    )
    
    /**
     * Обработка голосовой команды
     */
    fun process(command: String): ProcessedCommand {
        val normalizedCommand = normalizeCommand(command)
        
        // Поиск по ключевым словам
        for (pattern in commandPatterns) {
            if (pattern.matches(normalizedCommand)) {
                return pattern.action(normalizedCommand)
            }
        }
        
        // Fuzzy matching
        val bestMatch = findBestMatch(normalizedCommand)
        if (bestMatch != null) {
            return bestMatch
        }
        
        return ProcessedCommand.Unknown
    }
    
    /**
     * Извлечь километраж из команды
     */
    private fun extractKilometers(text: String): Int? {
        // Ищем числа перед "километр" или "км"
        val regex = "(\\d+)\\s*(километр|км|километров|км)".toRegex()
        val match = regex.find(text.lowercase())
        return match?.groupValues?.get(1)?.toIntOrNull()
    }
    
    /**
     * Нормализация команды
     */
    private fun normalizeCommand(command: String): String {
        return command
            .lowercase()
            .replace("[,.!?]".toRegex(), "")
            .replace("ё", "е")
            .trim()
    }
    
    /**
     * Поиск наилучшего совпадения
     */
    private fun findBestMatch(command: String): ProcessedCommand? {
        var bestScore = 0.0
        var bestAction: ProcessedCommand? = null
        
        for (pattern in commandPatterns) {
            for (keyword in pattern.keywords) {
                val score = calculateSimilarity(command, keyword)
                if (score > bestScore && score > 0.7) {
                    bestScore = score
                    bestAction = pattern.action(command)
                }
            }
        }
        
        return bestAction
    }
    
    /**
     * Расчет схожести строк
     */
    private fun calculateSimilarity(s1: String, s2: String): Double {
        val maxLength = maxOf(s1.length, s2.length)
        if (maxLength == 0) return 1.0
        
        val distance = levenshteinDistance(s1, s2)
        return 1.0 - (distance.toDouble() / maxLength)
    }
    
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        
        if (m == 0) return n
        if (n == 0) return m
        
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        
        return dp[m][n]
    }
    
    /**
     * Голосовой ответ
     */
    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    /**
     * Инициализация TTS
     */
    fun initTTS(context: android.content.Context, onInit: (Boolean) -> Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("ru", "RU"))
                onInit(result != TextToSpeech.LANG_MISSING_DATA && 
                       result != TextToSpeech.LANG_NOT_SUPPORTED)
            } else {
                onInit(false)
            }
        }
    }
    
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
    
    /**
     * Получить список доступных команд
     */
    fun getAvailableCommands(): List<CommandInfo> {
        return commandPatterns.map { 
            CommandInfo(
                examples = it.keywords.take(3),
                description = it.description
            )
        }
    }
}

/**
 * Паттерн команды
 */
data class CommandPattern(
    val keywords: List<String>,
    val action: (String) -> ProcessedCommand,
    val description: String
) {
    fun matches(command: String): Boolean {
        return keywords.any { command.contains(it) }
    }
}

/**
 * Информация о команде
 */
data class CommandInfo(
    val examples: List<String>,
    val description: String
)

/**
 * Примеры команд:
 * 
 * "Начни сбор данных на 10 километров" → StartDataCollection(10)
 * "Проанализируй" → StopAndAnalyze
 * "Покажи рекомендации" → ShowRecommendations
 * "Примени настройки" → ApplySettings
 * "Сбрось настройки" → ResetSettings
 * "Проверь машину" → RunDiagnostics
 * "Какие поломки ожидаются" → GetPredictions
 */
