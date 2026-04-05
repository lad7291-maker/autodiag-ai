package com.autodiag.ai.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {
    
    @Query("SELECT * FROM analyses ORDER BY timestamp DESC")
    fun getAllAnalyses(): Flow<List<AnalysisEntity>>
    
    @Query("SELECT * FROM analyses ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAnalysis(): AnalysisEntity?
    
    @Insert
    suspend fun insert(analysis: AnalysisEntity): Long
    
    @Update
    suspend fun update(analysis: AnalysisEntity)
    
    @Query("DELETE FROM analyses WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM analyses")
    suspend fun deleteAll()
    
    @Query("UPDATE analyses SET isApplied = 0")
    suspend fun clearAppliedStatus()
    
    @Query("SELECT * FROM analyses WHERE isApplied = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastAppliedAnalysis(): AnalysisEntity?
}
