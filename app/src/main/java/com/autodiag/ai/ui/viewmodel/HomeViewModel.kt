package com.autodiag.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.ai.data.repository.DiagnosisRepository
import com.autodiag.ai.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val diagnosisRepository: DiagnosisRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {
    
    private val _recentDiagnoses = MutableStateFlow(0)
    val recentDiagnoses: StateFlow<Int> = _recentDiagnoses.asStateFlow()
    
    private val _averageHealth = MutableStateFlow(100f)
    val averageHealth: StateFlow<Float> = _averageHealth.asStateFlow()
    
    private val _hasVehicle = MutableStateFlow(false)
    val hasVehicle: StateFlow<Boolean> = _hasVehicle.asStateFlow()
    
    init {
        loadStats()
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            val vehicle = vehicleRepository.getSelectedVehicle()
            _hasVehicle.value = vehicle != null
            
            vehicle?.let {
                // Получаем средний health score за последние 30 дней
                val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
                val avgHealth = diagnosisRepository.getAverageHealthScore(it.vin, thirtyDaysAgo)
                avgHealth?.let { score ->
                    _averageHealth.value = score
                }
            }
        }
    }
}
