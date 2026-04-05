package com.autodiag.ai.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    companion object {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        val MONITORING_INTERVAL = intPreferencesKey("monitoring_interval")
        val LAST_CONNECTED_DEVICE = stringPreferencesKey("last_connected_device")
        val SELECTED_VEHICLE_VIN = stringPreferencesKey("selected_vehicle_vin")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"
    }
    
    suspend fun isDarkTheme(): Boolean {
        return context.dataStore.data.map { it[DARK_THEME] ?: false }.first()
    }
    
    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[DARK_THEME] = enabled }
    }
    
    suspend fun areNotificationsEnabled(): Boolean {
        return context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }.first()
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }
    
    suspend fun isAutoConnectEnabled(): Boolean {
        return context.dataStore.data.map { it[AUTO_CONNECT] ?: true }.first()
    }
    
    suspend fun setAutoConnect(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_CONNECT] = enabled }
    }
    
    suspend fun getMonitoringInterval(): Int {
        return context.dataStore.data.map { it[MONITORING_INTERVAL] ?: 1000 }.first()
    }
    
    suspend fun setMonitoringInterval(intervalMs: Int) {
        context.dataStore.edit { it[MONITORING_INTERVAL] = intervalMs }
    }
    
    suspend fun getLastConnectedDevice(): String? {
        return context.dataStore.data.map { it[LAST_CONNECTED_DEVICE] }.first()
    }
    
    suspend fun setLastConnectedDevice(address: String) {
        context.dataStore.edit { it[LAST_CONNECTED_DEVICE] = address }
    }
    
    suspend fun getSelectedVehicleVin(): String? {
        return context.dataStore.data.map { it[SELECTED_VEHICLE_VIN] }.first()
    }
    
    suspend fun setSelectedVehicleVin(vin: String) {
        context.dataStore.edit { it[SELECTED_VEHICLE_VIN] = vin }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
    
    // ==================== Onboarding ====================
    
    suspend fun isOnboardingCompleted(): Boolean {
        return context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }.first()
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }
    
    // ==================== Theme ====================
    
    /**
     * Получить Flow для отслеживания изменения темы
     */
    fun darkThemeFlow(): Flow<Boolean> {
        return context.dataStore.data.map { it[DARK_THEME] ?: false }
    }
    
    /**
     * Получить режим темы (system, light, dark)
     */
    suspend fun getThemeMode(): String {
        return context.dataStore.data.map { it[THEME_MODE] ?: "system" }.first()
    }
    
    fun themeModeFlow(): Flow<String> {
        return context.dataStore.data.map { it[THEME_MODE] ?: "system" }
    }
    
    /**
     * Установить режим темы
     * @param mode "system", "light" или "dark"
     */
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { 
            it[THEME_MODE] = mode
            // Для совместимости существующих проверок
            it[DARK_THEME] = mode == "dark"
        }
    }
}
