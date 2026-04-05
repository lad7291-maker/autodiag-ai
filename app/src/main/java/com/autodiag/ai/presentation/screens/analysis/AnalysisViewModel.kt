package com.autodiag.ai.presentation.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.ai.aiagent.SafeAdaptiveEngineAgent
import com.autodiag.ai.aiagent.DrivingAnalysis
import com.autodiag.ai.data.repository.AnalysisRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AnalysisUiState {
    object Idle : AnalysisUiState()
    data class Collecting(
        val progress: Float,
        val kmCompleted: Float,
        val kmTotal: Int,
        val currentParameters: SafeAdaptiveEngineAgent.EngineParametersSnapshot?
    ) : AnalysisUiState()
    object Analyzing : AnalysisUiState()
    data class ResultsReady(val analysis: DrivingAnalysis) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

class AnalysisViewModel(
    private val engineAgent: SafeAdaptiveEngineAgent,
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val _collectionProgress = MutableStateFlow(0f)
    val collectionProgress: StateFlow<Float> = _collectionProgress.asStateFlow()

    private var collectionJob: kotlinx.coroutines.Job? = null

    fun startCollection(kilometers: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = AnalysisUiState.Collecting(
                    progress = 0f,
                    kmCompleted = 0f,
                    kmTotal = kilometers,
                    currentParameters = null
                )

                // Start data collection
                engineAgent.startDataCollection(kilometers)

                // Simulate progress updates (in real app, this would come from OBD2)
                collectionJob = launch {
                    var progress = 0f
                    while (progress < 1f) {
                        delay(500) // Update every 500ms
                        progress += 0.02f // Simulate progress
                        
                        val kmCompleted = progress * kilometers
                        val currentParams = engineAgent.getCurrentParameters()
                        
                        _uiState.value = AnalysisUiState.Collecting(
                            progress = progress.coerceIn(0f, 1f),
                            kmCompleted = kmCompleted,
                            kmTotal = kilometers,
                            currentParameters = currentParams
                        )
                        _collectionProgress.value = progress
                    }
                    
                    // Collection complete, move to analysis
                    stopCollection()
                }
            } catch (e: Exception) {
                _uiState.value = AnalysisUiState.Error(
                    "Ошибка запуска сбора: ${e.message}"
                )
            }
        }
    }

    fun stopCollection() {
        collectionJob?.cancel()
        
        viewModelScope.launch {
            try {
                _uiState.value = AnalysisUiState.Analyzing
                
                // Stop collection and get analysis
                val analysis = engineAgent.stopAndAnalyze()
                
                // Save analysis to repository
                analysisRepository.saveAnalysis(analysis)
                
                _uiState.value = AnalysisUiState.ResultsReady(analysis)
            } catch (e: Exception) {
                _uiState.value = AnalysisUiState.Error(
                    "Ошибка анализа: ${e.message}"
                )
            }
        }
    }

    fun reset() {
        collectionJob?.cancel()
        _uiState.value = AnalysisUiState.Idle
        _collectionProgress.value = 0f
    }

    fun applySettings() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is AnalysisUiState.ResultsReady) {
                    engineAgent.applyRecommendations(currentState.analysis.recommendations)
                    analysisRepository.markAsApplied(currentState.analysis)
                }
            } catch (e: Exception) {
                _uiState.value = AnalysisUiState.Error(
                    "Ошибка применения настроек: ${e.message}"
                )
            }
        }
    }

    fun resetToFactory() {
        viewModelScope.launch {
            try {
                engineAgent.resetToFactorySettings()
                analysisRepository.clearAppliedSettings()
            } catch (e: Exception) {
                _uiState.value = AnalysisUiState.Error(
                    "Ошибка сброса настроек: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        collectionJob?.cancel()
    }
}
