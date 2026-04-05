package com.autodiag.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.ai.data.model.DetectedIssue
import com.autodiag.ai.data.model.DiagnosisHistory
import com.autodiag.ai.data.model.DtcCode
import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.data.model.OperatingTip
import com.autodiag.ai.data.model.Vehicle
import com.autodiag.ai.data.repository.DiagnosisRepository
import com.autodiag.ai.data.repository.VehicleRepository
import com.autodiag.ai.utils.ai.DiagnosisAI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiagnosisViewModel(
    private val diagnosisRepository: DiagnosisRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Idle)
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()
    
    private val _currentParameters = MutableStateFlow<EngineParameters?>(null)
    val currentParameters: StateFlow<EngineParameters?> = _currentParameters.asStateFlow()
    
    private val _detectedIssues = MutableStateFlow<List<DetectedIssue>>(emptyList())
    val detectedIssues: StateFlow<List<DetectedIssue>> = _detectedIssues.asStateFlow()
    
    private val _operatingTips = MutableStateFlow<List<OperatingTip>>(emptyList())
    val operatingTips: StateFlow<List<OperatingTip>> = _operatingTips.asStateFlow()
    
    private val _healthScore = MutableStateFlow(100f)
    val healthScore: StateFlow<Float> = _healthScore.asStateFlow()
    
    private val _dtcCodes = MutableStateFlow<List<DtcCode>>(emptyList())
    val dtcCodes: StateFlow<List<DtcCode>> = _dtcCodes.asStateFlow()
    
    private val _dtcAnalysis = MutableStateFlow<DiagnosisAI.DtcAnalysisResult?>(null)
    val dtcAnalysis: StateFlow<DiagnosisAI.DtcAnalysisResult?> = _dtcAnalysis.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Анализ параметров двигателя
     */
    fun analyzeParameters(params: EngineParameters) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val vehicle = vehicleRepository.getSelectedVehicle()
            val result = diagnosisRepository.analyzeEngineParameters(params, vehicle)
            
            _currentParameters.value = params
            _detectedIssues.value = result.issues
            _operatingTips.value = result.operatingTips
            _healthScore.value = result.healthScore
            
            _uiState.value = if (result.issues.isNotEmpty()) {
                DiagnosisUiState.IssuesDetected(result.issues)
            } else {
                DiagnosisUiState.Healthy
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Анализ DTC кодов
     */
    fun analyzeDtcCodes(codes: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val dtcInfos = diagnosisRepository.getDtcInfos(codes)
            _dtcCodes.value = dtcInfos
            
            val vehicle = vehicleRepository.getSelectedVehicle()
            val analysis = diagnosisRepository.analyzeDtcCodes(dtcInfos, vehicle)
            _dtcAnalysis.value = analysis
            
            _uiState.value = DiagnosisUiState.DtcAnalyzed(analysis)
            
            _isLoading.value = false
        }
    }
    
    /**
     * Поиск DTC кода
     */
    fun searchDtc(query: String, onResult: (List<DtcCode>) -> Unit) {
        viewModelScope.launch {
            val results = diagnosisRepository.searchDtc(query)
            onResult(results)
        }
    }
    
    /**
     * Сохранить диагностику в историю
     */
    fun saveDiagnosis() {
        viewModelScope.launch {
            val vehicle = vehicleRepository.getSelectedVehicle() ?: return@launch
            
            val history = DiagnosisHistory(
                vehicleVin = vehicle.vin,
                dtcCodes = _dtcCodes.value.map { it.code },
                engineHealthScore = _healthScore.value,
                detectedIssues = _detectedIssues.value,
                recommendations = _detectedIssues.value.map { it.recommendedAction },
                operatingTips = _operatingTips.value,
                isSaved = true
            )
            
            diagnosisRepository.saveDiagnosis(history)
        }
    }
    
    /**
     * Очистить состояние
     */
    fun clearState() {
        _uiState.value = DiagnosisUiState.Idle
        _currentParameters.value = null
        _detectedIssues.value = emptyList()
        _operatingTips.value = emptyList()
        _healthScore.value = 100f
        _dtcCodes.value = emptyList()
        _dtcAnalysis.value = null
    }
    
    sealed class DiagnosisUiState {
        object Idle : DiagnosisUiState()
        object Loading : DiagnosisUiState()
        object Healthy : DiagnosisUiState()
        data class IssuesDetected(val issues: List<DetectedIssue>) : DiagnosisUiState()
        data class DtcAnalyzed(val analysis: DiagnosisAI.DtcAnalysisResult) : DiagnosisUiState()
        data class Error(val message: String) : DiagnosisUiState()
    }
}
