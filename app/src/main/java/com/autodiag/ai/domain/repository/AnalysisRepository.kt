package com.autodiag.ai.domain.repository

import com.autodiag.ai.domain.model.DrivingAnalysisDomainModel
import com.autodiag.ai.domain.model.SettingsBackupDomain
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с анализом вождения
 * Определяет контракт для Domain слоя
 */
interface AnalysisRepository {
    
    /**
     * Сохранить анализ вождения
     */
    suspend fun saveAnalysis(analysis: DrivingAnalysisDomainModel): Result<Unit>
    
    /**
     * Получить все анализы
     */
    fun getAllAnalyses(): Flow<List<DrivingAnalysisDomainModel>>
    
    /**
     * Получить последний анализ
     */
    suspend fun getLatestAnalysis(): DrivingAnalysisDomainModel?
    
    /**
     * Отметить анализ как примененный
     */
    suspend fun markAsApplied(analysis: DrivingAnalysisDomainModel): Result<Unit>
    
    /**
     * Сбросить статус применения настроек
     */
    suspend fun clearAppliedSettings(): Result<Unit>
    
    /**
     * Удалить анализ по ID
     */
    suspend fun deleteAnalysis(id: Long): Result<Unit>
    
    /**
     * Очистить все анализы
     */
    suspend fun clearAll(): Result<Unit>
    
    /**
     * Создать резервную копию настроек
     */
    suspend fun createSettingsBackup(backup: SettingsBackupDomain): Result<Long>
    
    /**
     * Получить последнюю резервную копию для автомобиля
     */
    suspend fun getLatestBackup(vehicleVin: String): SettingsBackupDomain?
    
    /**
     * Получить все резервные копии для автомобиля
     */
    fun getBackupsForVehicle(vehicleVin: String): Flow<List<SettingsBackupDomain>>
    
    /**
     * Удалить резервную копию
     */
    suspend fun deleteBackup(id: Long): Result<Unit>
    
    /**
     * Восстановить настройки из резервной копии
     */
    suspend fun restoreFromBackup(backupId: Long): Result<SettingsBackupDomain>
}
