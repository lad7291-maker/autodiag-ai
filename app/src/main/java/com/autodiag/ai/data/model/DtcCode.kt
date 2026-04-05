package com.autodiag.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * DTC (Diagnostic Trouble Code) - код ошибки OBD2
 */
@Entity(tableName = "dtc_codes")
@Serializable
data class DtcCode(
    @PrimaryKey
    val code: String,
    val description: String,
    val descriptionRu: String,
    val severity: DtcSeverity,
    val category: DtcCategory,
    val possibleCauses: List<String>,
    val symptoms: List<String>,
    val recommendedActions: List<String>,
    val affectedSystems: List<String>,
    val estimatedRepairCost: RepairCost? = null,
    val isCritical: Boolean = false,
    val canClear: Boolean = true
)

enum class DtcSeverity {
    INFO,       // Информационный
    LOW,        // Низкая важность
    MEDIUM,     // Средняя важность
    HIGH,       // Высокая важность
    CRITICAL    // Критическая - требует немедленного ремонта
}

enum class DtcCategory {
    ENGINE,         // Двигатель
    TRANSMISSION,   // Трансмиссия
    ABS,            // ABS
    AIRBAG,         // Подушки безопасности
    CLIMATE,        // Климат-контроль
    ELECTRICAL,     // Электрика
    FUEL,           // Топливная система
    EMISSIONS,      // Выхлопная система
    SENSOR,         // Датчики
    NETWORK,        // CAN-шина
    UNKNOWN         // Неизвестно
}

@Serializable
data class RepairCost(
    val minCost: Int,
    val maxCost: Int,
    val currency: String = "RUB",
    val description: String
)

enum class RepairDifficulty {
    EASY,       // Можно сделать самому
    MEDIUM,     // Требует опыта
    HARD,       // Лучше в сервис
    PROFESSIONAL // Только СТО
}
