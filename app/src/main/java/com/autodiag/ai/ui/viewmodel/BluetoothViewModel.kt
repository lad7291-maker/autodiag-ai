package com.autodiag.ai.ui.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.data.repository.BluetoothRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BluetoothViewModel(
    private val bluetoothRepository: BluetoothRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _pairedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val pairedDevices: StateFlow<Set<BluetoothDevice>> = _pairedDevices.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()
    
    private val _currentParameters = MutableStateFlow<EngineParameters?>(null)
    val currentParameters: StateFlow<EngineParameters?> = _currentParameters.asStateFlow()
    
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    private var monitoringJob: Job? = null
    
    init {
        refreshDevices()
    }
    
    fun refreshDevices() {
        viewModelScope.launch {
            _pairedDevices.value = bluetoothRepository.getPairedDevices()
        }
    }
    
    fun connect(device: BluetoothDevice) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            bluetoothRepository.connect(device)
                .onSuccess {
                    _isConnected.value = true
                    _connectedDeviceName.value = device.name
                    _uiState.value = UiState.Connected
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Ошибка подключения")
                }
        }
    }
    
    fun disconnect() {
        viewModelScope.launch {
            stopMonitoring()
            bluetoothRepository.disconnect()
            _isConnected.value = false
            _connectedDeviceName.value = null
            _uiState.value = UiState.Idle
        }
    }
    
    fun startMonitoring() {
        if (!_isConnected.value) return
        
        monitoringJob = bluetoothRepository.startMonitoring(1000)
            .onEach { params ->
                _currentParameters.value = params
                _isMonitoring.value = true
            }
            .catch { error ->
                _uiState.value = UiState.Error(error.message ?: "Ошибка мониторинга")
                _isMonitoring.value = false
            }
            .launchIn(viewModelScope)
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _isMonitoring.value = false
    }
    
    fun readTroubleCodes(onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            val codes = bluetoothRepository.readTroubleCodes()
            onResult(codes)
        }
    }
    
    fun clearTroubleCodes(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = bluetoothRepository.clearTroubleCodes()
            onResult(result.isSuccess)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
        disconnect()
    }
    
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Connected : UiState()
        data class Error(val message: String) : UiState()
    }
}
