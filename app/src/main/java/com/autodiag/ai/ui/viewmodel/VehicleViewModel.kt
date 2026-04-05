package com.autodiag.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.ai.data.model.Vehicle
import com.autodiag.ai.data.model.VehicleBrand
import com.autodiag.ai.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {
    
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()
    
    private val _selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedVehicle: StateFlow<Vehicle?> = _selectedVehicle.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadVehicles()
        loadSelectedVehicle()
    }
    
    private fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getAllVehicles().collect {
                _vehicles.value = it
            }
        }
    }
    
    private fun loadSelectedVehicle() {
        viewModelScope.launch {
            _selectedVehicle.value = vehicleRepository.getSelectedVehicle()
        }
    }
    
    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            vehicleRepository.addVehicle(vehicle)
            if (_vehicles.value.isEmpty()) {
                vehicleRepository.setSelectedVehicle(vehicle.vin)
            }
        }
    }
    
    fun selectVehicle(vin: String) {
        viewModelScope.launch {
            vehicleRepository.setSelectedVehicle(vin)
            _selectedVehicle.value = vehicleRepository.getVehicleByVin(vin)
        }
    }
    
    fun updateMileage(vin: String, mileage: Int) {
        viewModelScope.launch {
            vehicleRepository.updateMileage(vin, mileage)
        }
    }
    
    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            vehicleRepository.deleteVehicle(vehicle)
        }
    }
}
