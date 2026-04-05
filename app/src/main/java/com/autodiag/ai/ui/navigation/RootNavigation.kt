package com.autodiag.ai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.autodiag.ai.presentation.screens.onboarding.OnboardingScreen
import com.autodiag.ai.presentation.screens.onboarding.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Корневая навигация приложения
 * Управляет показом онбординга перед основным приложением
 */
@Composable
fun RootNavigation() {
    val onboardingViewModel: OnboardingViewModel = koinViewModel()
    val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsState()
    val isLoading by onboardingViewModel.isLoading.collectAsState()
    
    // Показываем онбординг или основное приложение
    when {
        isLoading -> {
            // Можно показать Splash Screen или Progress
        }
        !isOnboardingCompleted -> {
            OnboardingScreen(
                onOnboardingComplete = {
                    onboardingViewModel.completeOnboarding()
                },
                viewModel = onboardingViewModel
            )
        }
        else -> {
            AutoDiagNavHost()
        }
    }
}
