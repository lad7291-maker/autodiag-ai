package com.autodiag.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Информация об автомобиле пользователя
 */
@Entity(tableName = "vehicles")
@Serializable
data class Vehicle(
    @PrimaryKey
    val vin: String,
    val brand: VehicleBrand,
    val model: String,
    val year: Int,
    val engineType: String,
    val engineCode: String? = null,
    val engineVolume: Float? = null, // в литрах
    val fuelType: FuelType,
    val transmission: TransmissionType,
    val mileage: Int = 0,
    val lastServiceDate: Long? = null,
    val nextServiceMileage: Int? = null,
    val ecuType: String? = null,
    val isSelected: Boolean = false,
    val addedDate: Long = System.currentTimeMillis(),
    val notes: String? = null
)

enum class VehicleBrand {
    VAZ,        // ВАЗ/LADA
    UAZ,        // УАЗ
    GAZ,        // ГАЗ
    KAMAZ,      // КАМАЗ
    MOSCOW,     // Москвич
    FOREIGN,    // Иномарка
    OTHER       // Другая
}

enum class FuelType {
    PETROL_92,
    PETROL_95,
    PETROL_98,
    DIESEL,
    LPG,        // Газ
    CNG,        // Метан
    HYBRID,
    ELECTRIC
}

enum class TransmissionType {
    MANUAL_4,
    MANUAL_5,
    MANUAL_6,
    AUTOMATIC_4,
    AUTOMATIC_6,
    CVT,
    ROBOT
}

/**
 * Параметры двигателя в реальном времени
 */
@Serializable
data class EngineParameters(
    val timestamp: Long = System.currentTimeMillis(),
    val rpm: Int? = null,
    val speed: Int? = null,
    val throttlePosition: Float? = null,
    val coolantTemperature: Float? = null,
    val intakeAirTemperature: Float? = null,
    val intakeManifoldPressure: Float? = null,
    val massAirFlow: Float? = null,
    val fuelLevel: Float? = null,
    val fuelConsumption: Float? = null,
    val engineLoad: Float? = null,
    val timingAdvance: Float? = null,
    val oxygenSensorVoltage: Float? = null,
    val shortTermFuelTrim: Float? = null,
    val longTermFuelTrim: Float? = null,
    val batteryVoltage: Float? = null,
    val calculatedLoad: Float? = null,
    val barometricPressure: Float? = null,
    val catalystTemperature: Float? = null,
    val knockSensorValue: Float? = null  // Значение датчика детонации
)

/**
 * История диагностик
 */
@Entity(tableName = "diagnosis_history")
@Serializable
data class DiagnosisHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleVin: String,
    val diagnosisDate: Long = System.currentTimeMillis(),
    val dtcCodes: List<String>,
    val engineHealthScore: Float,
    val detectedIssues: List<DetectedIssue>,
    val recommendations: List<String>,
    val operatingTips: List<OperatingTip>,
    val isSaved: Boolean = false
)

@Serializable
data class DetectedIssue(
    val system: String,
    val severity: DtcSeverity,
    val description: String,
    val recommendedAction: String
)

@Serializable
data class OperatingTip(
    val category: TipCategory,
    val title: String,
    val description: String,
    val priority: TipPriority
)

enum class TipCategory {
    DRIVING_STYLE,      // Стиль вождения
    MAINTENANCE,        // Обслуживание
    FUEL,               // Топливо
    ENGINE_CARE,        // Уход за двигателем
    SEASONAL,           // Сезонные советы
    SAFETY              // Безопасность
}

enum class TipPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
