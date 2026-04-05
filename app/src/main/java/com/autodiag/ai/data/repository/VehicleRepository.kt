package com.autodiag.ai.data.repository

import com.autodiag.ai.data.local.database.dao.VehicleDao
import com.autodiag.ai.data.model.Vehicle
import com.autodiag.ai.data.model.VehicleBrand
import kotlinx.coroutines.flow.Flow

class VehicleRepository(
    private val vehicleDao: VehicleDao
) {
    fun getAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.getAll()
    }
    
    suspend fun getVehicleByVin(vin: String): Vehicle? {
        return vehicleDao.getByVin(vin)
    }
    
    suspend fun getSelectedVehicle(): Vehicle? {
        return vehicleDao.getSelectedVehicle()
    }
    
    suspend fun addVehicle(vehicle: Vehicle) {
        vehicleDao.insert(vehicle)
    }
    
    suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.update(vehicle)
    }
    
    suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.delete(vehicle)
    }
    
    suspend fun setSelectedVehicle(vin: String) {
        vehicleDao.clearSelection()
        vehicleDao.setSelected(vin)
    }
    
    suspend fun updateMileage(vin: String, mileage: Int) {
        vehicleDao.updateMileage(vin, mileage)
    }
    
    fun getVehiclesByBrand(brand: VehicleBrand): Flow<List<Vehicle>> {
        return vehicleDao.getByBrand(brand)
    }
}
