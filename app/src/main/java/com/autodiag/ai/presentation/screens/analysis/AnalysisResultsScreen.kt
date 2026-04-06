package com.autodiag.ai.presentation.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autodiag.ai.aiagent.DrivingAnalysis
import com.autodiag.ai.aiagent.DrivingStyle
import com.autodiag.ai.aiagent.SafeAdaptiveEngineAgent
import com.autodiag.ai.aiagent.EngineProfile
import com.autodiag.ai.aiagent.EngineTuneRecommendation
import com.autodiag.ai.ui.theme.RussianAutoRed
import com.autodiag.ai.ui.theme.RussianAutoDark
import com.autodiag.ai.ui.theme.RussianAutoGray
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultsScreen(
    viewModel: AnalysisViewModel = get(),
    onBackClick: () -> Unit,
    onSettingsApplied: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showApplyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    
    val analysis = (uiState as? AnalysisUiState.ResultsReady)?.analysis
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Результаты анализа") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RussianAutoGray
                )
            )
        }
    ) { padding ->
        analysis?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Driving style card
                DrivingStyleCard(data.drivingStyle)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Engine profile
                EngineProfileCard(data.engineProfile)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recommendations
                RecommendationsCard(data.recommendations)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Safety notes
                SafetyNotesCard(data.safetyNotes)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                ActionButtons(
                    onApply = { showApplyDialog = true },
                    onReset = { showResetDialog = true }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        } ?: run {
            // No data state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет данных для отображения",
                    fontSize = 18.sp,
                    color = RussianAutoDark.copy(alpha = 0.7f)
                )
            }
        }
    }
    
    if (showApplyDialog) {
        ApplyConfirmationDialog(
            onDismiss = { showApplyDialog = false },
            onConfirm = {
                showApplyDialog = false
                viewModel.applySettings()
                onSettingsApplied()
            }
        )
    }
    
    if (showResetDialog) {
        ResetConfirmationDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                showResetDialog = false
                viewModel.resetToFactory()
                onSettingsApplied()
            }
        )
    }
}

@Composable
private fun DrivingStyleCard(style: DrivingStyle) {
    val (icon, title, description, color) = when (style) {
        DrivingStyle.ECONOMICAL -> Quad(
            Icons.Default.Eco,
            "Экономичный",
            "Плавное ускорение, низкие обороты",
            Color(0xFF4CAF50)
        )
        DrivingStyle.BALANCED -> Quad(
            Icons.Default.Balance,
            "Сбалансированный",
            "Оптимальный баланс расхода и динамики",
            Color(0xFF2196F3)
        )
        DrivingStyle.SPORTY -> Quad(
            Icons.Default.Speed,
            "Спортивный",
            "Высокие обороты, активное ускорение",
            Color(0xFFFF9800)
        )
        DrivingStyle.AGGRESSIVE -> Quad(
            Icons.Default.Warning,
            "Агрессивный",
            "Резкие ускорения и торможения",
            RussianAutoRed
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(56.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Стиль вождения",
                    fontSize = 14.sp,
                    color = RussianAutoDark.copy(alpha = 0.7f)
                )
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = RussianAutoDark.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EngineProfileCard(profile: EngineProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RussianAutoGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Профиль двигателя",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RussianAutoDark
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ProfileBar("Средние обороты", profile.avgRpm, 6000f, "%.0f об/мин".format(profile.avgRpm))
            ProfileBar("Средняя нагрузка", profile.avgLoad, 100f, "%.1f%%".format(profile.avgLoad))
            ProfileBar("Средняя температура", profile.avgTemp, 120f, "%.1f°C".format(profile.avgTemp))
            ProfileBar("Средний расход", profile.avgConsumption, 20f, "%.1f л/100км".format(profile.avgConsumption))
        }
    }
}

@Composable
private fun ProfileBar(
    label: String,
    value: Float,
    maxValue: Float,
    valueText: String
) {
    val progress = (value / maxValue).coerceIn(0f, 1f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = RussianAutoDark.copy(alpha = 0.7f)
            )
            Text(
                text = valueText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = RussianAutoDark
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = RussianAutoRed,
            trackColor = Color.White
        )
    }
}

@Composable
private fun RecommendationsCard(recommendations: SafeAdaptiveEngineAgent.EngineTuneRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RussianAutoGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Рекомендуемые настройки",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RussianAutoDark
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ignition timing
            RecommendationRow(
                icon = Icons.Default.Timer,
                title = "Угол опережения зажигания",
                currentValue = "Заводской",
                recommendedValue = if (recommendations.ignitionTimingOffset >= 0) 
                    "+${recommendations.ignitionTimingOffset}°" 
                else 
                    "${recommendations.ignitionTimingOffset}°",
                description = if (recommendations.ignitionTimingOffset > 0) 
                    "Увеличить для лучшей динамики" 
                else if (recommendations.ignitionTimingOffset < 0) 
                    "Уменьшить для экономии топлива" 
                else 
                    "Оставить без изменений"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Fuel mixture
            RecommendationRow(
                icon = Icons.Default.LocalGasStation,
                title = "Топливная смесь",
                currentValue = "Заводская",
                recommendedValue = if (recommendations.fuelMixtureBias >= 0) 
                    "+${recommendations.fuelMixtureBias}%" 
                else 
                    "${recommendations.fuelMixtureBias}%",
                description = if (recommendations.fuelMixtureBias > 0) 
                    "Обогатить для мощности" 
                else if (recommendations.fuelMixtureBias < 0) 
                    "Обеднить для экономии" 
                else 
                    "Оставить без изменений"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reasoning
            Text(
                text = "Обоснование:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = RussianAutoDark
            )
            
            for (reason: String in recommendations.reasoning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = RussianAutoRed,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = reason,
                        fontSize = 14.sp,
                        color = RussianAutoDark.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    currentValue: String,
    recommendedValue: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RussianAutoRed,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = RussianAutoDark
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row {
                Text(
                    text = "Сейчас: ",
                    fontSize = 14.sp,
                    color = RussianAutoDark.copy(alpha = 0.7f)
                )
                Text(
                    text = currentValue,
                    fontSize = 14.sp,
                    color = RussianAutoDark
                )
            }
            
            Row {
                Text(
                    text = "Рекомендуется: ",
                    fontSize = 14.sp,
                    color = RussianAutoDark.copy(alpha = 0.7f)
                )
                Text(
                    text = recommendedValue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RussianAutoRed
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = RussianAutoDark.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SafetyNotesCard(notes: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Примечания безопасности",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            notes.forEach { note ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "⚠",
                        fontSize = 14.sp,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = note,
                        fontSize = 14.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RussianAutoRed
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Применить настройки",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RussianAutoDark
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Restore, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Сбросить к заводским",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ApplyConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = RussianAutoRed,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text("Применить настройки?") },
        text = {
            Column {
                Text(
                    "Вы собираетесь изменить параметры работы двигателя:",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• Угол зажигания будет скорректирован",
                    fontSize = 14.sp,
                    color = RussianAutoDark.copy(alpha = 0.8f)
                )
                Text(
                    "• Топливная смесь будет откорректирована",
                    fontSize = 14.sp,
                    color = RussianAutoDark.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Изменения можно отменить, вернув заводские настройки.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = RussianAutoRed
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RussianAutoRed
                )
            ) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun ResetConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Restore,
                contentDescription = null,
                tint = RussianAutoRed,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text("Сбросить настройки?") },
        text = {
            Text(
                "Все изменения будут отменены и двигатель вернётся к заводским настройкам.",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RussianAutoRed
                )
            ) {
                Text("Сбросить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

// Helper data class
private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
