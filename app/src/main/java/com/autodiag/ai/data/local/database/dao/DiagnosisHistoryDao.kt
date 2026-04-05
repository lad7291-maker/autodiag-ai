package com.autodiag.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.autodiag.ai.data.model.DiagnosisHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosisHistoryDao {
    
    @Query("SELECT * FROM diagnosis_history ORDER BY diagnosisDate DESC")
    fun getAll(): Flow<List<DiagnosisHistory>>
    
    @Query("SELECT * FROM diagnosis_history WHERE vehicleVin = :vin ORDER BY diagnosisDate DESC")
    fun getByVehicle(vin: String): Flow<List<DiagnosisHistory>>
    
    @Query("SELECT * FROM diagnosis_history WHERE id = :id")
    suspend fun getById(id: Long): DiagnosisHistory?
    
    @Query("SELECT * FROM diagnosis_history WHERE isSaved = 1 ORDER BY diagnosisDate DESC")
    fun getSaved(): Flow<List<DiagnosisHistory>>
    
    @Query("SELECT * FROM diagnosis_history WHERE diagnosisDate > :fromDate ORDER BY diagnosisDate DESC")
    fun getRecent(fromDate: Long): Flow<List<DiagnosisHistory>>
    
    @Query("""
        SELECT AVG(engineHealthScore) FROM diagnosis_history 
        WHERE vehicleVin = :vin 
        AND diagnosisDate > :fromDate
    """)
    suspend fun getAverageHealthScore(vin: String, fromDate: Long): Float?
    
    @Query("""
        SELECT COUNT(DISTINCT dtcCodes) FROM diagnosis_history 
        WHERE vehicleVin = :vin
    """)
    suspend fun getUniqueDtcCount(vin: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: DiagnosisHistory): Long
    
    @Update
    suspend fun update(history: DiagnosisHistory)
    
    @Delete
    suspend fun delete(history: DiagnosisHistory)
    
    @Query("DELETE FROM diagnosis_history WHERE vehicleVin = :vin")
    suspend fun deleteByVehicle(vin: String)
    
    @Query("SELECT COUNT(*) FROM diagnosis_history")
    suspend fun getCount(): Int
}
