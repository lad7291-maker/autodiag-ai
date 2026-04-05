package com.autodiag.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autodiag.ai.data.model.DtcCategory
import com.autodiag.ai.data.model.DtcCode
import com.autodiag.ai.data.model.DtcSeverity
import kotlinx.coroutines.flow.Flow

@Dao
interface DtcCodeDao {
    
    @Query("SELECT * FROM dtc_codes WHERE code = :code")
    suspend fun getByCode(code: String): DtcCode?
    
    @Query("SELECT * FROM dtc_codes WHERE code IN (:codes)")
    suspend fun getByCodes(codes: List<String>): List<DtcCode>
    
    @Query("SELECT * FROM dtc_codes WHERE category = :category")
    fun getByCategory(category: DtcCategory): Flow<List<DtcCode>>
    
    @Query("SELECT * FROM dtc_codes WHERE severity = :severity")
    fun getBySeverity(severity: DtcSeverity): Flow<List<DtcCode>>
    
    @Query("SELECT * FROM dtc_codes WHERE isCritical = 1")
    fun getCriticalCodes(): Flow<List<DtcCode>>
    
    @Query("SELECT * FROM dtc_codes WHERE descriptionRu LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<DtcCode>
    
    @Query("SELECT * FROM dtc_codes")
    fun getAll(): Flow<List<DtcCode>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(code: DtcCode)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(codes: List<DtcCode>)
    
    @Query("SELECT COUNT(*) FROM dtc_codes")
    suspend fun getCount(): Int
}
