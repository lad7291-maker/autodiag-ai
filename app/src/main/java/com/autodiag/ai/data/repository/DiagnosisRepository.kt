package com.autodiag.ai.data.repository

import com.autodiag.ai.data.local.database.dao.DiagnosisHistoryDao
import com.autodiag.ai.data.local.database.dao.DtcCodeDao
import com.autodiag.ai.data.model.DiagnosisHistory
import com.autodiag.ai.data.model.DtcCode
import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.utils.ai.DiagnosisAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DiagnosisRepository(
    private val dtcCodeDao: DtcCodeDao,
    private val diagnosisHistoryDao: DiagnosisHistoryDao,
    private val diagnosisAI: DiagnosisAI
) {
    
    /**
     * Получить информацию о DTC коде
     */
    suspend fun getDtcInfo(code: String): DtcCode? = withContext(Dispatchers.IO) {
        dtcCodeDao.getByCode(code)
    }
    
    /**
     * Получить информацию о нескольких DTC кодах
     */
    suspend fun getDtcInfos(codes: List<String>): List<DtcCode> = withContext(Dispatchers.IO) {
        dtcCodeDao.getByCodes(codes)
    }
    
    /**
     * Поиск DTC кодов
     */
    suspend fun searchDtc(query: String): List<DtcCode> = withContext(Dispatchers.IO) {
        dtcCodeDao.search(query)
    }
    
    /**
     * Анализ параметров двигателя
     */
    fun analyzeEngineParameters(
        params: EngineParameters,
        vehicle: com.autodiag.ai.data.model.Vehicle? = null
    ): DiagnosisAI.AnalysisResult {
        return diagnosisAI.analyzeEngineParameters(params, vehicle)
    }
    
    /**
     * Анализ DTC кодов
     */
    fun analyzeDtcCodes(
        codes: List<DtcCode>,
        vehicle: com.autodiag.ai.data.model.Vehicle? = null
    ): DiagnosisAI.DtcAnalysisResult {
        return diagnosisAI.analyzeDtcCodes(codes, vehicle)
    }
    
    /**
     * Сохранить историю диагностики
     */
    suspend fun saveDiagnosis(history: DiagnosisHistory): Long = withContext(Dispatchers.IO) {
        diagnosisHistoryDao.insert(history)
    }
    
    /**
     * Получить историю диагностик
     */
    fun getDiagnosisHistory(): Flow<List<DiagnosisHistory>> {
        return diagnosisHistoryDao.getAll()
    }
    
    /**
     * Получить историю по VIN
     */
    fun getDiagnosisHistoryByVehicle(vin: String): Flow<List<DiagnosisHistory>> {
        return diagnosisHistoryDao.getByVehicle(vin)
    }
    
    /**
     * Получить средний health score
     */
    suspend fun getAverageHealthScore(vin: String, fromDate: Long): Float? = withContext(Dispatchers.IO) {
        diagnosisHistoryDao.getAverageHealthScore(vin, fromDate)
    }
    
    /**
     * Получить все DTC коды
     */
    fun getAllDtcCodes(): Flow<List<DtcCode>> {
        return dtcCodeDao.getAll()
    }
}
