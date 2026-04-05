package com.autodiag.ai.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.ai.data.local.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для управления онбордингом
 * Отвечает за отслеживание состояния и сохранение прогресса
 */
class OnboardingViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()
    
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        checkOnboardingStatus()
    }
    
    /**
     * Проверить, завершен ли онбординг
     */
    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val completed = userPreferences.isOnboardingCompleted()
                _isOnboardingCompleted.value = completed
            } catch (e: Exception) {
                // В случае ошибки считаем, что онбординг не завершен
                _isOnboardingCompleted.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Отметить онбординг как завершенный
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            try {
                userPreferences.setOnboardingCompleted(true)
                _isOnboardingCompleted.value = true
            } catch (e: Exception) {
                // Логирование ошибки
            }
        }
    }
    
    /**
     * Перейти к указанной странице
     */
    fun navigateToPage(page: Int) {
        if (page >= 0) {
            _currentPage.value = page
        }
    }
    
    /**
     * Перейти на следующую страницу
     */
    fun nextPage() {
        _currentPage.value = _currentPage.value + 1
    }
    
    /**
     * Перейти на предыдущую страницу
     */
    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value = _currentPage.value - 1
        }
    }
    
    /**
     * Сбросить состояние онбординга (для тестирования)
     */
    fun resetOnboarding() {
        viewModelScope.launch {
            try {
                userPreferences.setOnboardingCompleted(false)
                _isOnboardingCompleted.value = false
                _currentPage.value = 0
            } catch (e: Exception) {
                // Логирование ошибки
            }
        }
    }
}
