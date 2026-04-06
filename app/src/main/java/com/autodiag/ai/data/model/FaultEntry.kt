package com.autodiag.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Запись о неисправности для конкретных моделей авто
 */
@Entity(tableName = "fault_database")
@Serializable
data class FaultEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleBrand: VehicleBrand,
    val vehicleModel: String,
    val engineType: String? = null,
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
    
    // Симптомы
    val symptoms: List<String>,
    val symptomsRu: List<String>,
    
    // Возможные причины
    val possibleCauses: List<FaultCause>,
    
    // Диагностика
    val diagnosticSteps: List<DiagnosticStep>,
    
    // Решение
    val solution: String,
    val solutionRu: String,
    
    // Сложность ремонта
    val repairDifficulty: RepairDifficulty,
    
    // Стоимость
    val estimatedCost: RepairCost? = null,
    
    // Время ремонта
    val estimatedTime: String? = null,
    
    // Можно ли сделать самому
    val canDoItYourself: Boolean = false,
    
    // Рейтинг полезности
    val helpfulRating: Float = 0f,
    val votesCount: Int = 0,
    
    // Источник
    val source: String? = null,
    
    // Дата добавления
    val addedDate: Long = System.currentTimeMillis()
)

@Serializable
data class FaultCause(
    val name: String,
    val nameRu: String,
    val probability: Float, // 0.0 - 1.0
    val partNumber: String? = null,
    val partName: String? = null,
    val partNameRu: String? = null,
    val whereToBuy: List<String>? = null,
    val priceRange: PriceRange? = null
)

@Serializable
data class DiagnosticStep(
    val step: Int,
    val description: String,
    val descriptionRu: String,
    val requiredTools: List<String>? = null,
    val expectedResult: String? = null,
    val expectedResultRu: String? = null
)

@Serializable
data class PriceRange(
    val min: Int,
    val max: Int,
    val currency: String = "RUB"
)
