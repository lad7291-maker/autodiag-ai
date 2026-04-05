package com.autodiag.ai.domain.repository

import com.autodiag.ai.domain.model.DiagnosisHistoryDomainModel
import com.autodiag.ai.domain.model.DtcCodeDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с диагностикой
 * Определяет контракт для Domain слоя
 */
interface DiagnosisRepository {
    
    /**
     * Получить информацию о DTC коде
     */
    suspend fun getDtcInfo(code: String): DtcCodeDomainModel?
    
    /**
     * Получить информацию о нескольких DTC кодах
     */
    suspend fun getDtcInfos(codes: List<String>): List<DtcCodeDomainModel>
    
    /**
     * Поиск DTC кодов
     */
    suspend fun searchDtc(query: String): List<DtcCodeDomainModel>
    
    /**
     * Сохранить историю диагностики
     * @return ID сохраненной записи или -1 в случае ошибки
     */
    suspend fun saveDiagnosis(history: DiagnosisHistoryDomainModel): Result<Long>
    
    /**
     * Получить всю историю диагностик
     */
    fun getDiagnosisHistory(): Flow<List<DiagnosisHistoryDomainModel>>
    
    /**
     * Получить историю диагностик для конкретного автомобиля
     */
    fun getDiagnosisHistoryByVehicle(vin: String): Flow<List<DiagnosisHistoryDomainModel>>
    
    /**
     * Получить средний health score
     */
    suspend fun getAverageHealthScore(vin: String, fromDate: Long): Float?
    
    /**
     * Получить последнюю диагностику для автомобиля
     */
    suspend fun getLatestDiagnosis(vin: String): DiagnosisHistoryDomainModel?
    
    /**
     * Удалить историю диагностики
     */
    suspend fun deleteDiagnosis(id: Long): Result<Unit>
    
    /**
     * Очистить все коды ошибок (DTC)
     */
    suspend fun clearDtcCodes(): Result<Unit>
    
    /**
     * Получить все DTC коды
     */
    fun getAllDtcCodes(): Flow<List<DtcCodeDomainModel>>
}
