package com.autodiag.ai.domain.usecase

import com.autodiag.ai.domain.model.VehicleBrandDomain
import com.autodiag.ai.domain.model.VehicleDomainModel
import com.autodiag.ai.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * UseCase для работы с автомобилями
 * Реализует бизнес-логику управления автопарком
 */
class VehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {
    
    /**
     * Добавить новый автомобиль с валидацией данных
     * @param vehicle Данные автомобиля
     * @return Результат операции с ID или ошибкой
     */
    suspend fun addVehicle(vehicle: VehicleDomainModel): Result<String> {
        // Валидация VIN
        if (vehicle.vin.isBlank()) {
            return Result.failure(IllegalArgumentException("VIN не может быть пустым"))
        }
        
        if (vehicle.vin.length != 17) {
            return Result.failure(IllegalArgumentException("VIN должен содержать 17 символов"))
        }
        
        // Проверка на дубликат
        if (vehicleRepository.vehicleExists(vehicle.vin)) {
            return Result.failure(IllegalStateException("Автомобиль с таким VIN уже существует"))
        }
        
        // Валидация года
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        if (vehicle.year < 1950 || vehicle.year > currentYear + 1) {
            return Result.failure(IllegalArgumentException("Некорректный год выпуска"))
        }
        
        // Валидация модели
        if (vehicle.model.isBlank()) {
            return Result.failure(IllegalArgumentException("Модель не может быть пустой"))
        }
        
        // Валидация пробега
        if (vehicle.mileage < 0) {
            return Result.failure(IllegalArgumentException("Пробег не может быть отрицательным"))
        }
        
        return vehicleRepository.addVehicle(vehicle).map { vehicle.vin }
    }
    
    /**
     * Получить список всех автомобилей
     */
    fun getVehicles(): Flow<List<VehicleDomainModel>> {
        return vehicleRepository.getAllVehicles()
    }
    
    /**
     * Получить выбранный автомобиль
     */
    suspend fun getSelectedVehicle(): VehicleDomainModel? {
        return vehicleRepository.getSelectedVehicle()
    }
    
    /**
     * Выбрать автомобиль по VIN
     */
    suspend fun selectVehicle(vin: String): Result<Unit> {
        if (vin.isBlank()) {
            return Result.failure(IllegalArgumentException("VIN не может быть пустым"))
        }
        
        if (!vehicleRepository.vehicleExists(vin)) {
            return Result.failure(IllegalStateException("Автомобиль с VIN $vin не найден"))
        }
        
        return vehicleRepository.setSelectedVehicle(vin)
    }
    
    /**
     * Обновить данные автомобиля
     */
    suspend fun updateVehicle(vehicle: VehicleDomainModel): Result<Unit> {
        if (!vehicleRepository.vehicleExists(vehicle.vin)) {
            return Result.failure(IllegalStateException("Автомобиль не найден"))
        }
        
        // Валидация пробега
        if (vehicle.mileage < 0) {
            return Result.failure(IllegalArgumentException("Пробег не может быть отрицательным"))
        }
        
        return vehicleRepository.updateVehicle(vehicle)
    }
    
    /**
     * Удалить автомобиль
     */
    suspend fun deleteVehicle(vin: String): Result<Unit> {
        val vehicle = vehicleRepository.getVehicleByVin(vin)
            ?: return Result.failure(IllegalStateException("Автомобиль не найден"))
        
        return vehicleRepository.deleteVehicle(vehicle)
    }
    
    /**
     * Обновить пробег автомобиля
     */
    suspend fun updateMileage(vin: String, mileage: Int): Result<Unit> {
        if (mileage < 0) {
            return Result.failure(IllegalArgumentException("Пробег не может быть отрицательным"))
        }
        
        return vehicleRepository.updateMileage(vin, mileage)
    }
    
    /**
     * Получить автомобили по марке
     */
    fun getVehiclesByBrand(brand: VehicleBrandDomain): Flow<List<VehicleDomainModel>> {
        return vehicleRepository.getVehiclesByBrand(brand)
    }
    
    /**
     * Проверить необходимость ТО
     */
    suspend fun checkServiceRequired(): List<Pair<VehicleDomainModel, Boolean>> {
        return vehicleRepository.getAllVehicles().first().map { vehicle ->
            vehicle to vehicle.isServiceRequired()
        }
    }
}
