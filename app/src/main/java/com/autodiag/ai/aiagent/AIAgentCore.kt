package com.autodiag.ai.aiagent

import android.content.Context
import com.autodiag.ai.data.model.DetectedIssue
import com.autodiag.ai.data.model.DiagnosisHistory
import com.autodiag.ai.data.model.DtcCode
import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.data.model.Vehicle
import com.autodiag.ai.data.repository.DiagnosisRepository
import com.autodiag.ai.data.repository.VehicleRepository
import com.autodiag.ai.services.ObdConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Core AI Agent - центральный контроллер интеллектуального помощника
 * 
 * ПРИНЦИП: AI только АНАЛИЗИРУЕТ, РЕКОМЕНДУЕТ и ПРЕДУПРЕЖДАЕТ
 * Водитель сам решает что делать!
 * 
 * Что AI делает:
 * ✓ Собирает данные по запросу водителя (на выбранном километраже)
 * ✓ Анализирует стиль вождения
 * ✓ Дает рекомендации по настройкам
 * ✓ Предупреждает голосом о проблемах
 * 
 * Что AI НЕ делает:
 * ✗ Не снимает нагрузку автоматически
 * ✗ Не меняет настройки без разрешения
 * ✗ Не управляет педалями/тормозами
 * ✗ Не влияет на системы безопасности
 */
class AIAgentCore(
    private val context: Context,
    private val obdManager: ObdConnectionManager,
    private val diagnosisRepository: DiagnosisRepository,
    private val vehicleRepository: VehicleRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    
    // Суб-агенты
    private val adaptiveEngineAgent = SafeAdaptiveEngineAgent(obdManager)
    private val diagnosticAgent = DiagnosticAgent(diagnosisRepository)
    private val predictiveAgent = PredictiveMaintenanceAgent(diagnosisRepository)
    private val voiceCommandProcessor = VoiceCommandProcessor(this)
    
    // Состояние агента
    private val _agentState = MutableStateFlow(AgentState.IDLE)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()
    
    // Текущий анализ
    private val _currentAnalysis = MutableStateFlow<DrivingAnalysis?>(null)
    val currentAnalysis: StateFlow<DrivingAnalysis?> = _currentAnalysis.asStateFlow()
    
    // Рекомендуемые настройки
    private val _recommendedSettings = MutableStateFlow<EngineTuneRecommendation?>(null)
    val recommendedSettings: StateFlow<EngineTuneRecommendation?> = _recommendedSettings.asStateFlow()
    
    // Голосовые предупреждения
    private val _voiceAlerts = MutableStateFlow<List<VoiceAlert>>(emptyList())
    val voiceAlerts: StateFlow<List<VoiceAlert>> = _voiceAlerts.asStateFlow()
    
    // Последняя команда
    private val _lastCommand = MutableStateFlow<ProcessedCommand?>(null)
    val lastCommand: StateFlow<ProcessedCommand?> = _lastCommand.asStateFlow()
    
    private var monitoringJob: Job? = null
    private var isCollectingData = false
    
    /**
     * Обработка голосовой команды
     */
    suspend fun processVoiceCommand(command: String): CommandResult {
        _agentState.value = AgentState.PROCESSING
        
        return try {
            val processedCommand = voiceCommandProcessor.process(command)
            _lastCommand.value = processedCommand
            
            val result = executeCommand(processedCommand)
            
            _agentState.value = AgentState.RESPONDING
            delay(500)
            _agentState.value = AgentState.IDLE
            
            result
        } catch (e: Exception) {
            _agentState.value = AgentState.ERROR
            CommandResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }
    
    /**
     * Выполнение команды
     */
    private suspend fun executeCommand(command: ProcessedCommand): CommandResult {
        return when (command) {
            is ProcessedCommand.StartDataCollection -> {
                val km = command.kilometers
                adaptiveEngineAgent.startDataCollection(km)
                isCollectingData = true
                startMonitoring()
                CommandResult.Success(
                    "Начат сбор данных. Проедьте $km километров в обычном режиме вождения. " +
                    "Я проанализирую ваш стиль и дам рекомендации по настройке двигателя."
                )
            }
            
            is ProcessedCommand.StopAndAnalyze -> {
                isCollectingData = false
                val analysis = adaptiveEngineAgent.stopAndAnalyze()
                _currentAnalysis.value = analysis
                
                when (analysis.status) {
                    AnalysisStatus.INSUFFICIENT_DATA -> {
                        CommandResult.Error(analysis.message)
                    }
                    AnalysisStatus.COMPLETED -> {
                        _recommendedSettings.value = analysis.recommendations
                        val rec = analysis.recommendations
                        if (rec != null && (rec.ignitionTimingOffset != 0f || rec.fuelMixtureBias != 0f)) {
                            CommandResult.Success(
                                "Анализ завершен! Ваш профиль: ${getProfileName(analysis.profile)}. " +
                                "Рекомендую: УОЗ ${if (rec.ignitionTimingOffset > 0) "+" else ""}${rec.ignitionTimingOffset}°, " +
                                "смесь ${if (rec.fuelMixtureBias > 0) "+" else ""}${rec.fuelMixtureBias}%. " +
                                "Скажите 'примени настройки' чтобы применить или 'покажи детали' для подробностей."
                            )
                        } else {
                            CommandResult.Success(
                                "Анализ завершен! Ваш профиль: ${getProfileName(analysis.profile)}. " +
                                "Специальных настроек не требуется - ваш стиль уже оптимален."
                            )
                        }
                    }
                    else -> CommandResult.Success("Сбор данных остановлен.")
                }
            }
            
            is ProcessedCommand.ShowRecommendations -> {
                val rec = _recommendedSettings.value
                if (rec != null) {
                    val reasoning = rec.reasoning.joinToString("\n• ", "• ")
                    val safety = rec.safetyNotes.joinToString("\n⚠ ", "⚠ ")
                    CommandResult.Success(
                        "${rec.description}\n\n" +
                        "Рекомендуемые настройки:\n" +
                        "• УОЗ: ${if (rec.ignitionTimingOffset > 0) "+" else ""}${rec.ignitionTimingOffset}°\n" +
                        "• Смесь: ${if (rec.fuelMixtureBias > 0) "+" else ""}${rec.fuelMixtureBias}%\n\n" +
                        "Обоснование:\n$reasoning\n\n" +
                        "Безопасность:\n$safety\n\n" +
                        "Скажите 'примени настройки' для применения."
                    )
                } else {
                    CommandResult.Error("Сначала проведите анализ. Скажите 'начни сбор данных на 10 километров'")
                }
            }
            
            is ProcessedCommand.ApplySettings -> {
                val success = adaptiveEngineAgent.applyRecommendedSettings()
                if (success) {
                    CommandResult.Success(
                        "Настройки применены! Двигатель теперь настроен под ваш стиль вождения. " +
                        "Если почувствуете детонацию или другие проблемы - скажите 'сбрось настройки'."
                    )
                } else {
                    CommandResult.Error("Нет рекомендуемых настроек. Проведите анализ сначала.")
                }
            }
            
            is ProcessedCommand.ResetSettings -> {
                adaptiveEngineAgent.resetToFactory()
                _recommendedSettings.value = null
                CommandResult.Success("Настройки сброшены к заводским значениям.")
            }
            
            is ProcessedCommand.RunDiagnostics -> {
                val issues = diagnosticAgent.runFullDiagnostics()
                if (issues.isEmpty()) {
                    CommandResult.Success("Диагностика завершена. Проблем не обнаружено! Двигатель работает нормально.")
                } else {
                    val critical = issues.filter { it.severity == com.autodiag.ai.data.model.DtcSeverity.CRITICAL }
                    if (critical.isNotEmpty()) {
                        CommandResult.Success("Обнаружены КРИТИЧЕСКИЕ проблемы! Проверьте раздел диагностики немедленно!")
                    } else {
                        CommandResult.Success("Обнаружено ${issues.size} проблем. Проверьте раздел диагностики.")
                    }
                }
            }
            
            is ProcessedCommand.GetEngineStatus -> {
                val status = getQuickStatus()
                CommandResult.Success(status)
            }
            
            is ProcessedCommand.GetPredictions -> {
                val vehicle = vehicleRepository.getSelectedVehicle()
                if (vehicle != null) {
                    val predictions = predictiveAgent.predictFailures(vehicle)
                    if (predictions.isEmpty()) {
                        CommandResult.Success("Предсказаний поломок нет. Всё в порядке! Продолжайте регулярное обслуживание.")
                    } else {
                        val critical = predictions.filter { it.urgency == Urgency.CRITICAL }
                        if (critical.isNotEmpty()) {
                            CommandResult.Success(
                                "⚠️ Обнаружены критические предупреждения! ${critical.first().component} - " +
                                critical.first().recommendedAction
                            )
                        } else {
                            CommandResult.Success(
                                "Есть рекомендации по обслуживанию. " +
                                predictions.first().component + " - " + predictions.first().recommendedAction
                            )
                        }
                    }
                } else {
                    CommandResult.Error("Сначала добавьте автомобиль в настройках")
                }
            }
            
            is ProcessedCommand.ClearAlerts -> {
                adaptiveEngineAgent.clearAlerts()
                _voiceAlerts.value = emptyList()
                CommandResult.Success("Предупреждения очищены")
            }
            
            is ProcessedCommand.Unknown -> {
                CommandResult.Error(
                    "Не понял команду. Попробуйте:\n" +
                    "• 'Начни сбор данных на 10 километров'\n" +
                    "• 'Останови сбор и проанализируй'\n" +
                    "• 'Покажи рекомендации'\n" +
                    "• 'Примени настройки'\n" +
                    "• 'Проверь машину'\n" +
                    "• 'Какие поломки ожидаются'"
                )
            }
        }
    }
    
    /**
     * Мониторинг параметров (только для предупреждений!)
     */
    private fun startMonitoring() {
        monitoringJob = scope.launch {
            while (isActive && isCollectingData) {
                if (obdManager.isConnected()) {
                    // Получаем текущие параметры
                    val params = getCurrentParameters()
                    
                    // Добавляем в анализ
                    adaptiveEngineAgent.addSample(params)
                    
                    // Обновляем текущий анализ
                    _currentAnalysis.value = adaptiveEngineAgent.currentAnalysis.value
                    
                    // Обновляем предупреждения
                    _voiceAlerts.value = adaptiveEngineAgent.voiceAlerts.value
                }
                delay(1000) // Каждую секунду
            }
        }
    }
    
    /**
     * Получить текущие параметры двигателя
     */
    private suspend fun getCurrentParameters(): EngineParameters {
        // Здесь чтение через OBD2
        return EngineParameters()
    }
    
    /**
     * Быстрый статус
     */
    private fun getQuickStatus(): String {
        val analysis = _currentAnalysis.value
        val rec = _recommendedSettings.value
        
        return buildString {
            append("Состояние двигателя: Норма\n")
            
            if (analysis != null && analysis.status == AnalysisStatus.COMPLETED) {
                append("Ваш профиль: ${getProfileName(analysis.profile)}\n")
            }
            
            if (rec != null) {
                append("Текущие настройки:\n")
                if (rec.ignitionTimingOffset != 0f) {
                    append("• УОЗ: ${if (rec.ignitionTimingOffset > 0) "+" else ""}${rec.ignitionTimingOffset}°\n")
                }
                if (rec.fuelMixtureBias != 0f) {
                    append("• Смесь: ${if (rec.fuelMixtureBias > 0) "+" else ""}${rec.fuelMixtureBias}%\n")
                }
            } else {
                append("Настройки: заводские\n")
            }
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
    
    /**
     * Получить предсказания о поломках
     */
    suspend fun getPredictions(): List<FailurePrediction> {
        val vehicle = vehicleRepository.getSelectedVehicle() ?: return emptyList()
        return predictiveAgent.predictFailures(vehicle)
    }
    
    fun stop() {
        monitoringJob?.cancel()
        isCollectingData = false
    }
    
    enum class AgentState {
        IDLE,
        LISTENING,
        PROCESSING,
        RESPONDING,
        ERROR
    }
}

/**
 * Результат выполнения команды
 */
sealed class CommandResult {
    data class Success(val message: String) : CommandResult()
    data class Error(val message: String) : CommandResult()
}

/**
 * Обработанная команда
 */
sealed class ProcessedCommand {
    data class StartDataCollection(val kilometers: Int) : ProcessedCommand()
    object StopAndAnalyze : ProcessedCommand()
    object ShowRecommendations : ProcessedCommand()
    object ApplySettings : ProcessedCommand()
    object ResetSettings : ProcessedCommand()
    object RunDiagnostics : ProcessedCommand()
    object GetEngineStatus : ProcessedCommand()
    object GetPredictions : ProcessedCommand()
    object ClearAlerts : ProcessedCommand()
    object Unknown : ProcessedCommand()
}
