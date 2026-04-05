package com.autodiag.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.autodiag.ai.data.model.Vehicle
import com.autodiag.ai.data.model.VehicleBrand
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    
    @Query("SELECT * FROM vehicles")
    fun getAll(): Flow<List<Vehicle>>
    
    @Query("SELECT * FROM vehicles WHERE vin = :vin")
    suspend fun getByVin(vin: String): Vehicle?
    
    @Query("SELECT * FROM vehicles WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedVehicle(): Vehicle?
    
    @Query("SELECT * FROM vehicles WHERE brand = :brand")
    fun getByBrand(brand: VehicleBrand): Flow<List<Vehicle>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: Vehicle)
    
    @Update
    suspend fun update(vehicle: Vehicle)
    
    @Delete
    suspend fun delete(vehicle: Vehicle)
    
    @Query("UPDATE vehicles SET isSelected = 0")
    suspend fun clearSelection()
    
    @Query("UPDATE vehicles SET isSelected = 1 WHERE vin = :vin")
    suspend fun setSelected(vin: String)
    
    @Query("UPDATE vehicles SET mileage = :mileage WHERE vin = :vin")
    suspend fun updateMileage(vin: String, mileage: Int)
    
    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getCount(): Int
}
