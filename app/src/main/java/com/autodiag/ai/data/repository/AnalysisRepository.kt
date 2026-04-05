package com.autodiag.ai.data.repository

import com.autodiag.ai.aiagent.DrivingAnalysis
import com.autodiag.ai.data.local.AnalysisDao
import com.autodiag.ai.data.local.AnalysisEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnalysisRepository(
    private val analysisDao: AnalysisDao
) {
    
    suspend fun saveAnalysis(analysis: DrivingAnalysis) {
        val entity = AnalysisEntity(
            timestamp = System.currentTimeMillis(),
            profile = analysis.profile.name,
            drivingStyle = analysis.drivingStyle.name,
            samplesCount = analysis.samplesCount,
            averageSpeed = analysis.averageSpeed,
            averageRpm = analysis.averageRpm,
            ignitionTimingOffset = analysis.recommendations?.ignitionTimingOffset ?: 0f,
            fuelMixtureBias = analysis.recommendations?.fuelMixtureBias ?: 0f,
            reasoning = analysis.recommendations?.reasoning?.joinToString("\n") ?: "",
            safetyNotes = analysis.recommendations?.safetyNotes?.joinToString("\n") ?: "",
            isApplied = false
        )
        analysisDao.insert(entity)
    }
    
    fun getAllAnalyses(): Flow<List<AnalysisEntity>> {
        return analysisDao.getAllAnalyses()
    }
    
    suspend fun getLatestAnalysis(): AnalysisEntity? {
        return analysisDao.getLatestAnalysis()
    }
    
    suspend fun markAsApplied(analysis: DrivingAnalysis) {
        // Mark the latest analysis as applied
        val latest = analysisDao.getLatestAnalysis()
        latest?.let {
            analysisDao.update(it.copy(isApplied = true))
        }
    }
    
    suspend fun clearAppliedSettings() {
        analysisDao.clearAppliedStatus()
    }
    
    suspend fun deleteAnalysis(id: Long) {
        analysisDao.deleteById(id)
    }
    
    suspend fun clearAll() {
        analysisDao.deleteAll()
    }
}
