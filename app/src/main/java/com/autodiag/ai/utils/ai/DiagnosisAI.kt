package com.autodiag.ai.utils.ai

import android.content.Context
import com.autodiag.ai.data.model.DetectedIssue
import com.autodiag.ai.data.model.DtcCategory
import com.autodiag.ai.data.model.DtcCode
import com.autodiag.ai.data.model.DtcSeverity
import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.data.model.FuelType
import com.autodiag.ai.data.model.OperatingTip
import com.autodiag.ai.data.model.RepairCost
import com.autodiag.ai.data.model.RepairDifficulty
import com.autodiag.ai.data.model.TipCategory
import com.autodiag.ai.data.model.TipPriority
import com.autodiag.ai.data.model.Vehicle
import com.autodiag.ai.data.model.VehicleBrand
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * AI модуль для диагностики и анализа
 * 
 * Этот модуль анализирует состояние двигателя и дает рекомендации,
 * но НЕ управляет автомобилем напрямую!
 */
class DiagnosisAI(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    
    // Пороговые значения для диагностики
    companion object {
        // Температуры
        const val MIN_COOLANT_TEMP = 85f
        const val MAX_COOLANT_TEMP = 105f
        const val CRITICAL_COOLANT_TEMP = 115f
        
        // Обороты
        const val MIN_IDLE_RPM = 700
        const val MAX_IDLE_RPM = 1000
        const val CRITICAL_RPM = 7000
        
        // Нагрузка
        const val MAX_ENGINE_LOAD = 90f
        
        // Fuel Trim
        const val MAX_FUEL_TRIM = 15f
        const val CRITICAL_FUEL_TRIM = 25f
        
        // Дроссель
        const val MIN_THROTTLE_AT_IDLE = 0f
        const val MAX_THROTTLE_AT_IDLE = 5f
    }
    
    init {
        loadModel()
    }
    
    private fun loadModel() {
        try {
            val model = loadModelFile("ml-models/diagnosis_model.tflite")
            interpreter = Interpreter(model)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    /**
     * Анализ параметров двигателя
     * AI анализирует и дает рекомендации, но НЕ управляет!
     */
    fun analyzeEngineParameters(
        params: EngineParameters,
        vehicle: Vehicle? = null
    ): AnalysisResult {
        val issues = mutableListOf<DetectedIssue>()
        val tips = mutableListOf<OperatingTip>()
        var healthScore = 100f
        
        // Анализ температуры ОЖ
        params.coolantTemperature?.let { temp ->
            when {
                temp > CRITICAL_COOLANT_TEMP -> {
                    issues.add(
                        DetectedIssue(
                            system = "Охлаждение",
                            severity = DtcSeverity.CRITICAL,
                            description = "Критическая температура двигателя: ${temp}°C",
                            recommendedAction = "Немедленно остановите двигатель! Проверьте уровень охлаждающей жидкости."
                        )
                    )
                    healthScore -= 30f
                    
                    tips.add(
                        OperatingTip(
                            category = TipCategory.SAFETY,
                            title = "Перегрев двигателя",
                            description = "При перегреве нельзя сразу открывать крышку радиатора. Дождитесь остывания.",
                            priority = TipPriority.CRITICAL
                        )
                    )
                }
                temp > MAX_COOLANT_TEMP -> {
                    issues.add(
                        DetectedIssue(
                            system = "Охлаждение",
                            severity = DtcSeverity.HIGH,
                            description = "Повышенная температура двигателя: ${temp}°C",
                            recommendedAction = "Проверьте уровень антифриза, состояние термостата и радиатора."
                        )
                    )
                    healthScore -= 15f
                }
                temp < MIN_COOLANT_TEMP && params.rpm != null && params.rpm > 1500 -> {
                    issues.add(
                        DetectedIssue(
                            system = "Охлаждение",
                            severity = DtcSeverity.MEDIUM,
                            description = "Двигатель не прогрет: ${temp}°C",
                            recommendedAction = "Дайте двигателю прогреться до рабочей температуры перед нагрузкой."
                        )
                    )
                    healthScore -= 5f
                }
            }
        }
        
        // Анализ оборотов
        params.rpm?.let { rpm ->
            when {
                rpm > CRITICAL_RPM -> {
                    issues.add(
                        DetectedIssue(
                            system = "Двигатель",
                            severity = DtcSeverity.CRITICAL,
                            description = "Критические обороты: $rpm RPM",
                            recommendedAction = "Снизьте обороты! Риск повреждения двигателя."
                        )
                    )
                    healthScore -= 25f
                }
                params.speed == 0 && (rpm < MIN_IDLE_RPM || rpm > MAX_IDLE_RPM) -> {
                    issues.add(
                        DetectedIssue(
                            system = "Холостой ход",
                            severity = DtcSeverity.MEDIUM,
                            description = "Нестабильный холостой ход: $rpm RPM",
                            recommendedAction = "Проверьте дроссельную заслонку, ДПДЗ, датчик холостого хода."
                        )
                    )
                    healthScore -= 10f
                }
            }
        }
        
        // Анализ нагрузки
        params.engineLoad?.let { load ->
            if (load > MAX_ENGINE_LOAD && params.rpm != null && params.rpm < 2000) {
                issues.add(
                    DetectedIssue(
                        system = "Двигатель",
                        severity = DtcSeverity.HIGH,
                        description = "Высокая нагрузка на низких оборотах: ${load}%",
                        recommendedAction = "Переключитесь на пониженную передачу. Риск детонации."
                    )
                )
                healthScore -= 15f
                
                tips.add(
                    OperatingTip(
                        category = TipCategory.DRIVING_STYLE,
                        title = "Нагрузка на двигатель",
                        description = "Избегайте высокой нагрузки на низких оборотах - это вызывает детонацию.",
                        priority = TipPriority.HIGH
                    )
                )
            }
        }
        
        // Анализ Fuel Trim
        params.shortTermFuelTrim?.let { trim ->
            when {
                kotlin.math.abs(trim) > CRITICAL_FUEL_TRIM -> {
                    issues.add(
                        DetectedIssue(
                            system = "Топливная система",
                            severity = DtcSeverity.CRITICAL,
                            description = "Критическое отклонение топливной смеси: ${trim}%",
                            recommendedAction = "Проверьте лямбда-зонд, форсунки, топливный насос, давление в рампе."
                        )
                    )
                    healthScore -= 20f
                }
                kotlin.math.abs(trim) > MAX_FUEL_TRIM -> {
                    issues.add(
                        DetectedIssue(
                            system = "Топливная система",
                            severity = DtcSeverity.HIGH,
                            description = "Отклонение топливной смеси: ${trim}%",
                            recommendedAction = "Проверьте воздушный фильтр, лямбда-зонд, подсос воздуха."
                        )
                    )
                    healthScore -= 10f
                }
            }
        }
        
        // Анализ дросселя
        params.throttlePosition?.let { throttle ->
            if (params.speed == 0 && params.rpm != null && params.rpm > 1000) {
                if (throttle > MAX_THROTTLE_AT_IDLE) {
                    issues.add(
                        DetectedIssue(
                            system = "Дроссель",
                            severity = DtcSeverity.MEDIUM,
                            description = "Заслонка открыта на холостом ходу: ${throttle}%",
                            recommendedAction = "Требуется чистка дроссельной заслонки или адаптация."
                        )
                    )
                    healthScore -= 8f
                }
            }
        }
        
        // Анализ температуры впуска
        params.intakeAirTemperature?.let { temp ->
            if (temp > 60f) {
                issues.add(
                    DetectedIssue(
                        system = "Впуск",
                        severity = DtcSeverity.MEDIUM,
                        description = "Высокая температура впускаемого воздуха: ${temp}°C",
                        recommendedAction = "Проверьте систему охлаждения наддувочного воздуха (интеркулер)."
                    )
                )
                healthScore -= 5f
            }
        }
        
        // Анализ уровня топлива
        params.fuelLevel?.let { level ->
            if (level < 10f) {
                tips.add(
                    OperatingTip(
                        category = TipCategory.FUEL,
                        title = "Низкий уровень топлива",
                        description = "При низком уровне топлива бензонасос может перегреваться. Заправьтесь.",
                        priority = TipPriority.MEDIUM
                    )
                )
            }
        }
        
        // Рекомендации по эксплуатации на основе данных
        if (params.rpm != null && params.rpm > 5000) {
            tips.add(
                OperatingTip(
                    category = TipCategory.ENGINE_CARE,
                    title = "Высокие обороты",
                    description = "Частая езда на высоких оборотах сокращает ресурс двигателя. Давайте двигателю остывать перед выключением.",
                    priority = TipPriority.MEDIUM
                )
            )
        }
        
        // Специфичные рекомендации для русских авто
        vehicle?.let { addVehicleSpecificTips(it, tips) }
        
        return AnalysisResult(
            healthScore = kotlin.math.max(0f, healthScore),
            issues = issues,
            operatingTips = tips
        )
    }
    
    /**
     * Специфичные рекомендации для конкретных марок
     */
    private fun addVehicleSpecificTips(vehicle: Vehicle, tips: MutableList<OperatingTip>) {
        when (vehicle.brand) {
            VehicleBrand.VAZ -> {
                tips.add(
                    OperatingTip(
                        category = TipCategory.MAINTENANCE,
                        title = "Особенности ВАЗ",
                        description = "Регулярно проверяйте уровень масла - двигатели ВАЗ склонны к повышенному расходу. Рекомендуется менять масло каждые 7500 км.",
                        priority = TipPriority.MEDIUM
                    )
                )
                
                if (vehicle.engineType.contains("8 кл", ignoreCase = true)) {
                    tips.add(
                        OperatingTip(
                            category = TipCategory.MAINTENANCE,
                            title = "Регулировка клапанов",
                            description = "На 8-клапанных двигателях ВАЗ требуется регулировка зазоров клапанов каждые 30 000 км.",
                            priority = TipPriority.HIGH
                        )
                    )
                }
            }
            
            VehicleBrand.UAZ -> {
                tips.add(
                    OperatingTip(
                        category = TipCategory.MAINTENANCE,
                        title = "Особенности УАЗ",
                        description = "Проверяйте состояние карданных валов и крестовин. Используйте качественное масло в мостах и раздатке.",
                        priority = TipPriority.MEDIUM
                    )
                )
            }
            
            VehicleBrand.GAZ -> {
                tips.add(
                    OperatingTip(
                        category = TipCategory.MAINTENANCE,
                        title = "Газель/Соболь",
                        description = "Следите за состоянием цепи ГРМ. При шуме цепи немедленно обратитесь на СТО.",
                        priority = TipPriority.HIGH
                    )
                )
            }
            
            else -> {}
        }
        
        // Рекомендации по топливу
        when (vehicle.fuelType) {
            FuelType.PETROL_92 -> {
                tips.add(
                    OperatingTip(
                        category = TipCategory.FUEL,
                        title = "Качество топлива",
                        description = "Заправляйтесь только на проверенных АЗС. Некачественный бензин - главная причина проблем с катализатором.",
                        priority = TipPriority.HIGH
                    )
                )
            }
            FuelType.LPG -> {
                tips.add(
                    OperatingTip(
                        category = TipCategory.FUEL,
                        title = "ГБО",
                        description = "Регулярно проверяйте фильтры ГБО и редуктор. Меняйте свечи чаще - газ сушит их.",
                        priority = TipPriority.MEDIUM
                    )
                )
            }
            else -> {}
        }
    }
    
    /**
     * Анализ DTC кодов с помощью AI
     */
    fun analyzeDtcCodes(codes: List<DtcCode>, vehicle: Vehicle? = null): DtcAnalysisResult {
        val criticalCodes = codes.filter { it.severity == DtcSeverity.CRITICAL }
        val highCodes = codes.filter { it.severity == DtcSeverity.HIGH }
        val mediumCodes = codes.filter { it.severity == DtcSeverity.MEDIUM }
        
        val allActions = codes.flatMap { it.recommendedActions }
        val totalCost = codes.mapNotNull { it.estimatedRepairCost }
            .fold(RepairCost(0, 0, "RUB", "")) { acc, cost ->
                RepairCost(
                    acc.minCost + cost.minCost,
                    acc.maxCost + cost.maxCost,
                    "RUB",
                    ""
                )
            }
        
        // Определение приоритетности ремонта
        val priorityOrder = when {
            criticalCodes.isNotEmpty() -> "Немедленный ремонт требуется!"
            highCodes.isNotEmpty() -> "Ремонт в ближайшее время"
            mediumCodes.isNotEmpty() -> "Плановый ремонт"
            else -> "Проблемы не критичны"
        }
        
        return DtcAnalysisResult(
            totalCodes = codes.size,
            criticalCount = criticalCodes.size,
            highCount = highCodes.size,
            mediumCount = mediumCodes.size,
            lowCount = codes.filter { it.severity == DtcSeverity.LOW }.size,
            priorityOrder = priorityOrder,
            estimatedCost = if (totalCost.minCost > 0) totalCost else null,
            canDrive = criticalCodes.isEmpty(),
            recommendedActions = allActions.distinct()
        )
    }
    
    data class AnalysisResult(
        val healthScore: Float,
        val issues: List<DetectedIssue>,
        val operatingTips: List<OperatingTip>
    )
    
    data class DtcAnalysisResult(
        val totalCodes: Int,
        val criticalCount: Int,
        val highCount: Int,
        val mediumCount: Int,
        val lowCount: Int,
        val priorityOrder: String,
        val estimatedCost: RepairCost?,
        val canDrive: Boolean,
        val recommendedActions: List<String>
    )
}
