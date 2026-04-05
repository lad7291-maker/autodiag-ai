package com.autodiag.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.ai.data.local.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _autoConnect = MutableStateFlow(true)
    val autoConnect: StateFlow<Boolean> = _autoConnect.asStateFlow()
    
    private val _monitoringInterval = MutableStateFlow(1000)
    val monitoringInterval: StateFlow<Int> = _monitoringInterval.asStateFlow()
    
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _isDarkTheme.value = userPreferences.isDarkTheme()
                _notificationsEnabled.value = userPreferences.areNotificationsEnabled()
                _autoConnect.value = userPreferences.isAutoConnectEnabled()
                _monitoringInterval.value = userPreferences.getMonitoringInterval()
                _themeMode.value = userPreferences.getThemeMode()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkTheme(enabled)
            _isDarkTheme.value = enabled
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
            _notificationsEnabled.value = enabled
        }
    }
    
    fun setAutoConnect(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoConnect(enabled)
            _autoConnect.value = enabled
        }
    }
    
    fun setMonitoringInterval(intervalMs: Int) {
        viewModelScope.launch {
            userPreferences.setMonitoringInterval(intervalMs)
            _monitoringInterval.value = intervalMs
        }
    }
    
    /**
     * Установить режим темы
     * @param mode "system", "light" или "dark"
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
            _themeMode.value = mode
            _isDarkTheme.value = mode == "dark"
        }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            userPreferences.clearAll()
            // Перезагружаем настройки
            loadSettings()
        }
    }
}
