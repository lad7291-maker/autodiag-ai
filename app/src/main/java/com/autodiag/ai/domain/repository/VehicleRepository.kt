package com.autodiag.ai.domain.repository

import com.autodiag.ai.domain.model.VehicleBrandDomain
import com.autodiag.ai.domain.model.VehicleDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с автомобилями
 * Определяет контракт для Domain слоя
 */
interface VehicleRepository {
    
    /**
     * Получить все автомобили как Flow
     */
    fun getAllVehicles(): Flow<List<VehicleDomainModel>>
    
    /**
     * Получить автомобиль по VIN
     */
    suspend fun getVehicleByVin(vin: String): VehicleDomainModel?
    
    /**
     * Получить выбранный автомобиль
     */
    suspend fun getSelectedVehicle(): VehicleDomainModel?
    
    /**
     * Добавить новый автомобиль
     */
    suspend fun addVehicle(vehicle: VehicleDomainModel): Result<Unit>
    
    /**
     * Обновить данные автомобиля
     */
    suspend fun updateVehicle(vehicle: VehicleDomainModel): Result<Unit>
    
    /**
     * Удалить автомобиль
     */
    suspend fun deleteVehicle(vehicle: VehicleDomainModel): Result<Unit>
    
    /**
     * Установить выбранный автомобиль
     */
    suspend fun setSelectedVehicle(vin: String): Result<Unit>
    
    /**
     * Обновить пробег автомобиля
     */
    suspend fun updateMileage(vin: String, mileage: Int): Result<Unit>
    
    /**
     * Получить автомобили по марке
     */
    fun getVehiclesByBrand(brand: VehicleBrandDomain): Flow<List<VehicleDomainModel>>
    
    /**
     * Проверить существование автомобиля с данным VIN
     */
    suspend fun vehicleExists(vin: String): Boolean
}
