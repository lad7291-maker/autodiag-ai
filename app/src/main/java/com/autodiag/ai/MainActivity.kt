package com.autodiag.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.autodiag.ai.data.local.preferences.UserPreferences
import com.autodiag.ai.ui.navigation.RootNavigation
import com.autodiag.ai.ui.theme.AutoDiagTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val userPreferences: UserPreferences by inject()
    private val themeModeFlow = MutableStateFlow("system")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        splashScreen.setKeepOnScreenCondition { false }
        
        // Загружаем сохраненную тему
        lifecycleScope.launch {
            userPreferences.themeModeFlow().collect { mode ->
                themeModeFlow.value = mode
            }
        }
        
        enableEdgeToEdge()
        
        setContent {
            val themeMode by themeModeFlow.collectAsState()
            
            AutoDiagTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavigation()
                }
            }
        }
    }
}
