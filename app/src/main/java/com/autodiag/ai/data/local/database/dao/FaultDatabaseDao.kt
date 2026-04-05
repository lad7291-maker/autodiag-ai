package com.autodiag.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autodiag.ai.data.model.FaultEntry
import com.autodiag.ai.data.model.RepairDifficulty
import com.autodiag.ai.data.model.VehicleBrand
import kotlinx.coroutines.flow.Flow

@Dao
interface FaultDatabaseDao {
    
    @Query("SELECT * FROM fault_database WHERE id = :id")
    suspend fun getById(id: Long): FaultEntry?
    
    @Query("SELECT * FROM fault_database WHERE vehicleBrand = :brand")
    fun getByBrand(brand: VehicleBrand): Flow<List<FaultEntry>>
    
    @Query("SELECT * FROM fault_database WHERE vehicleBrand = :brand AND vehicleModel = :model")
    fun getByBrandAndModel(brand: VehicleBrand, model: String): Flow<List<FaultEntry>>
    
    @Query("""
        SELECT * FROM fault_database 
        WHERE vehicleBrand = :brand 
        AND (:model IS NULL OR vehicleModel = :model)
        AND (:symptom IS NULL OR EXISTS (
            SELECT 1 FROM fault_database AS f 
            WHERE f.id = fault_database.id 
            AND f.symptomsRu LIKE '%' || :symptom || '%'
        ))
    """)
    suspend fun searchFaults(
        brand: VehicleBrand,
        model: String? = null,
        symptom: String? = null
    ): List<FaultEntry>
    
    @Query("SELECT * FROM fault_database WHERE repairDifficulty = :difficulty")
    fun getByDifficulty(difficulty: RepairDifficulty): Flow<List<FaultEntry>>
    
    @Query("SELECT * FROM fault_database WHERE canDoItYourself = 1")
    fun getDiyRepairs(): Flow<List<FaultEntry>>
    
    @Query("SELECT * FROM fault_database ORDER BY helpfulRating DESC LIMIT :limit")
    fun getTopRated(limit: Int = 10): Flow<List<FaultEntry>>
    
    @Query("UPDATE fault_database SET helpfulRating = :rating, votesCount = votesCount + 1 WHERE id = :id")
    suspend fun updateRating(id: Long, rating: Float)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fault: FaultEntry)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(faults: List<FaultEntry>)
    
    @Query("SELECT COUNT(*) FROM fault_database")
    suspend fun getCount(): Int
}
