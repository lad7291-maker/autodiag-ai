package com.autodiag.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.data.repository.BluetoothRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveDataViewModel(
    private val bluetoothRepository: BluetoothRepository
) : ViewModel() {
    
    private val _parameters = MutableStateFlow<EngineParameters?>(null)
    val parameters: StateFlow<EngineParameters?> = _parameters.asStateFlow()
    
    private val _rpmHistory = MutableStateFlow<List<Int>>(emptyList())
    val rpmHistory: StateFlow<List<Int>> = _rpmHistory.asStateFlow()
    
    private val _tempHistory = MutableStateFlow<List<Float>>(emptyList())
    val tempHistory: StateFlow<List<Float>> = _tempHistory.asStateFlow()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    fun updateParameters(params: EngineParameters) {
        _parameters.value = params
        
        // Update histories
        params.rpm?.let { rpm ->
            val newList = _rpmHistory.value + rpm
            if (newList.size > 100) {
                _rpmHistory.value = newList.drop(newList.size - 100)
            } else {
                _rpmHistory.value = newList
            }
        }
        
        params.coolantTemperature?.let { temp ->
            val newList = _tempHistory.value + temp
            if (newList.size > 100) {
                _tempHistory.value = newList.drop(newList.size - 100)
            } else {
                _tempHistory.value = newList
            }
        }
    }
    
    fun clearHistory() {
        _rpmHistory.value = emptyList()
        _tempHistory.value = emptyList()
    }
    
    fun startRecording() {
        _isRecording.value = true
    }
    
    fun stopRecording() {
        _isRecording.value = false
    }
}
