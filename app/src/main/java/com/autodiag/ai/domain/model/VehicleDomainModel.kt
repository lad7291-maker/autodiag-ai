package com.autodiag.ai.domain.model

/**
 * Domain модель автомобиля
 * Используется в Domain слое для бизнес-логики
 */
data class VehicleDomainModel(
    val vin: String,
    val brand: VehicleBrandDomain,
    val model: String,
    val year: Int,
    val engineType: String,
    val engineCode: String? = null,
    val engineVolume: Float? = null,
    val fuelType: FuelTypeDomain,
    val transmission: TransmissionTypeDomain,
    val mileage: Int = 0,
    val lastServiceDate: Long? = null,
    val nextServiceMileage: Int? = null,
    val ecuType: String? = null,
    val isSelected: Boolean = false,
    val addedDate: Long = System.currentTimeMillis(),
    val notes: String? = null
) {
    /**
     * Полное название автомобиля для отображения
     */
    val displayName: String
        get() = "${brand.displayName} $model ($year)"
    
    /**
     * Проверка, требуется ли техническое обслуживание
     */
    fun isServiceRequired(): Boolean {
        return nextServiceMileage?.let { mileage >= it } ?: false
    }
    
    /**
     * Получение информации о двигателе
     */
    val engineInfo: String
        get() = buildString {
            append(engineType)
            engineVolume?.let { append(" ${it}L") }
            engineCode?.let { append(" ($it)") }
        }
}

enum class VehicleBrandDomain(val displayName: String) {
    VAZ("ВАЗ/LADA"),
    UAZ("УАЗ"),
    GAZ("ГАЗ"),
    KAMAZ("КАМАЗ"),
    MOSCOW("Москвич"),
    FOREIGN("Иномарка"),
    OTHER("Другая")
}

enum class FuelTypeDomain {
    PETROL_92,
    PETROL_95,
    PETROL_98,
    DIESEL,
    LPG,
    CNG,
    HYBRID,
    ELECTRIC
}

enum class TransmissionTypeDomain {
    MANUAL_4,
    MANUAL_5,
    MANUAL_6,
    AUTOMATIC_4,
    AUTOMATIC_6,
    CVT,
    ROBOT
}
