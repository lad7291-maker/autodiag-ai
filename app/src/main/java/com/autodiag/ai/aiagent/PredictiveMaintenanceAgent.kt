package com.autodiag.ai.aiagent

import com.autodiag.ai.data.model.DiagnosisHistory
import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.data.model.FailurePrediction
import com.autodiag.ai.data.model.Urgency
import com.autodiag.ai.data.model.Vehicle
import com.autodiag.ai.data.repository.DiagnosisRepository

/**
 * Agent для предиктивного обслуживания
 * Предсказывает поломки до их возникновения
 */
class PredictiveMaintenanceAgent(
    private val diagnosisRepository: DiagnosisRepository
) {
    
    /**
     * Предсказание возможных поломок
     */
    suspend fun predictFailures(vehicle: Vehicle): List<FailurePrediction> {
        val predictions = mutableListOf<FailurePrediction>()
        
        // Получаем историю диагностик
        val history = getVehicleHistory(vehicle.vin)
        
        // Анализ свечей зажигания
        analyzeSparkPlugs(history, vehicle)?.let { predictions.add(it) }
        
        // Анализ катализатора
        analyzeCatalyst(history, vehicle)?.let { predictions.add(it) }
        
        // Анализ тормозных колодок
        analyzeBrakePads(history, vehicle)?.let { predictions.add(it) }
        
        // Анализ аккумулятора
        analyzeBattery(history, vehicle)?.let { predictions.add(it) }
        
        // Анализ масла
        analyzeOilCondition(history, vehicle)?.let { predictions.add(it) }
        
        // Анализ ремня ГРМ
        analyzeTimingBelt(vehicle)?.let { predictions.add(it) }
        
        return predictions.sortedByDescending { it.probability }
    }
    
    /**
     * Анализ состояния свечей
     */
    private fun analyzeSparkPlugs(
        history: List<DiagnosisHistory>,
        vehicle: Vehicle
    ): FailurePrediction? {
        // Анализируем историю на признаки пропусков зажигания
        val misfireCount = history.flatMap { it.detectedIssues }
            .count { it.description.contains("пропуск") || it.description.contains("misfire") }
        
        val mileage = vehicle.mileage
        val lastChange = history.lastOrNull { 
            it.recommendations.any { rec -> rec.contains("свечи") }
        }?.diagnosisDate ?: 0
        
        val kmSinceLastChange = if (lastChange > 0) {
            mileage - (mileage - 30000) // Примерная логика
        } else mileage
        
        val probability = when {
            misfireCount > 3 -> 0.9f
            kmSinceLastChange > 40000 -> 0.7f
            kmSinceLastChange > 30000 -> 0.5f
            else -> 0f
        }
        
        return if (probability > 0.3f) {
            FailurePrediction(
                component = "Свечи зажигания",
                probability = probability,
                estimatedMileage = mileage + when {
                    probability > 0.8f -> 2000
                    probability > 0.6f -> 5000
                    else -> 10000
                },
                recommendedAction = "Заменить свечи зажигания. " +
                    "Рекомендуемые: NGK BKR6E или аналоги",
                urgency = if (probability > 0.8f) Urgency.HIGH else Urgency.MEDIUM
            )
        } else null
    }
    
    /**
     * Анализ катализатора
     */
    private fun analyzeCatalyst(
        history: List<DiagnosisHistory>,
        vehicle: Vehicle
    ): FailurePrediction? {
        // Анализ эффективности катализатора по данным O2 сенсоров
        val catalystIssues = history.flatMap { it.detectedIssues }
            .count { it.description.contains("катализатор") || it.description.contains("P0420") }
        
        val probability = when {
            catalystIssues > 2 -> 0.85f
            vehicle.mileage > 150000 -> 0.6f
            vehicle.mileage > 100000 -> 0.4f
            else -> 0f
        }
        
        return if (probability > 0.3f) {
            FailurePrediction(
                component = "Каталитический нейтрализатор",
                probability = probability,
                estimatedMileage = vehicle.mileage + 20000,
                recommendedAction = "Проверить эффективность катализатора. " +
                    "При необходимости заменить. Стоимость: 15000-50000 руб.",
                urgency = Urgency.LOW
            )
        } else null
    }
    
    /**
     * Анализ тормозных колодок
     */
    private fun analyzeBrakePads(
        history: List<DiagnosisHistory>,
        vehicle: Vehicle
    ): FailurePrediction? {
        // Оценка износа на основе стиля вождения и пробега
        val aggressiveBraking = history.size > 10 // Упрощенная логика
        
        val probability = when {
            aggressiveBraking && vehicle.mileage > 30000 -> 0.8f
            vehicle.mileage > 50000 -> 0.6f
            vehicle.mileage > 40000 -> 0.4f
            else -> 0f
        }
        
        return if (probability > 0.3f) {
            FailurePrediction(
                component = "Тормозные колодки",
                probability = probability,
                estimatedMileage = vehicle.mileage + 5000,
                recommendedAction = "Проверить толщину тормозных колодок. " +
                    "При износе более 70% - заменить. Стоимость: 2000-5000 руб.",
                urgency = Urgency.HIGH
            )
        } else null
    }
    
    /**
     * Анализ аккумулятора
     */
    private fun analyzeBattery(
        history: List<DiagnosisHistory>,
        vehicle: Vehicle
    ): FailurePrediction? {
        // Оценка состояния АКБ по напряжению и истории
        val probability = when {
            vehicle.mileage > 80000 -> 0.5f
            vehicle.mileage > 60000 -> 0.3f
            else -> 0f
        }
        
        return if (probability > 0.3f) {
            FailurePrediction(
                component = "Аккумулятор",
                probability = probability,
                estimatedMileage = vehicle.mileage + 15000,
                recommendedAction = "Проверить напряжение и плотность электролита. " +
                    "Рекомендуемая емкость: 60-75 Ач",
                urgency = Urgency.MEDIUM
            )
        } else null
    }
    
    /**
     * Анализ состояния масла
     */
    private fun analyzeOilCondition(
        history: List<DiagnosisHistory>,
        vehicle: Vehicle
    ): FailurePrediction? {
        val lastService = vehicle.lastServiceDate ?: 0
        val monthsSinceService = (System.currentTimeMillis() - lastService) / (1000 * 60 * 60 * 24 * 30)
        
        val probability = when {
            monthsSinceService > 12 -> 0.7f
            monthsSinceService > 9 -> 0.5f
            vehicle.mileage - (vehicle.nextServiceMileage ?: 0) > 2000 -> 0.6f
            else -> 0f
        }
        
        return if (probability > 0.3f) {
            FailurePrediction(
                component = "Моторное масло",
                probability = probability,
                estimatedMileage = vehicle.mileage + 1000,
                recommendedAction = "Заменить моторное масло и фильтр. " +
                    "Рекомендуемое: 5W-40 синтетика",
                urgency = Urgency.MEDIUM
            )
        } else null
    }
    
    /**
     * Анализ ремня ГРМ
     */
    private fun analyzeTimingBelt(vehicle: Vehicle): FailurePrediction? {
        // Ремень ГРМ - критически важный узел
        val probability = when {
            vehicle.mileage > 90000 -> 0.9f
            vehicle.mileage > 80000 -> 0.7f
            vehicle.mileage > 70000 -> 0.5f
            else -> 0f
        }
        
        return if (probability > 0.3f) {
            FailurePrediction(
                component = "Ремень ГРМ",
                probability = probability,
                estimatedMileage = vehicle.mileage + 5000,
                recommendedAction = "СРОЧНО заменить ремень ГРМ! " +
                    "Обрыв ремня приведет к капитальному ремонту двигателя. " +
                    "Стоимость замены: 8000-15000 руб.",
                urgency = Urgency.CRITICAL
            )
        } else null
    }
    
    /**
     * Получить историю по VIN
     */
    private suspend fun getVehicleHistory(vin: String): List<DiagnosisHistory> {
        var history: List<DiagnosisHistory> = emptyList()
        diagnosisRepository.getDiagnosisHistoryByVehicle(vin).collect {
            history = it
        }
        return history
    }
    
    /**
     * Получить план обслуживания
     */
    fun getMaintenanceSchedule(vehicle: Vehicle): MaintenanceSchedule {
        return MaintenanceSchedule(
            oilChange = vehicle.mileage + 10000,
            filterChange = vehicle.mileage + 10000,
            sparkPlugChange = vehicle.mileage + 30000,
            timingBeltChange = vehicle.mileage + 90000,
            brakePadCheck = vehicle.mileage + 20000
        )
    }
}

/**
 * План обслуживания
 */
data class MaintenanceSchedule(
    val oilChange: Int,
    val filterChange: Int,
    val sparkPlugChange: Int,
    val timingBeltChange: Int,
    val brakePadCheck: Int
)
