package com.autodiag.ai.presentation.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.autodiag.ai.aiagent.EngineParametersSnapshot
import com.autodiag.ai.ui.theme.RussianAutoRed
import com.autodiag.ai.ui.theme.RussianAutoDark
import com.autodiag.ai.ui.theme.RussianAutoGray
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataCollectionScreen(
    viewModel: AnalysisViewModel = get(),
    onNavigateToResults: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showKmDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сбор данных") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is AnalysisUiState.Idle -> {
                    IdleStateContent(
                        onStartCollection = { showKmDialog = true }
                    )
                }
                
                is AnalysisUiState.Collecting -> {
                    CollectingContent(
                        progress = state.progress,
                        kmCompleted = state.kmCompleted,
                        kmTotal = state.kmTotal,
                        parameters = state.currentParameters,
                        onStop = { viewModel.stopCollection() }
                    )
                }
                
                is AnalysisUiState.Analyzing -> {
                    AnalyzingContent()
                }
                
                is AnalysisUiState.ResultsReady -> {
                    ResultsReadyContent(
                        onViewResults = onNavigateToResults
                    )
                }
                
                is AnalysisUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.reset() }
                    )
                }
                
                else -> {}
            }
        }
    }
    
    if (showKmDialog) {
        KmSelectionDialog(
            onDismiss = { showKmDialog = false },
            onConfirm = { km ->
                showKmDialog = false
                viewModel.startCollection(km)
            }
        )
    }
}

@Composable
private fun IdleStateContent(
    onStartCollection: () -> Unit
) {
    Spacer(modifier = Modifier.height(32.dp))
    
    Icon(
        imageVector = Icons.Default.Analytics,
        contentDescription = null,
        modifier = Modifier.size(120.dp),
        tint = RussianAutoRed
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "Анализ стиля вождения",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = RussianAutoDark
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Приложение проанализирует параметры двигателя за выбранный пробег и даст рекомендации по оптимизации",
        fontSize = 16.sp,
        color = RussianAutoDark.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RussianAutoGray
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            InfoRow(
                icon = Icons.Default.Speed,
                text = "Анализируются: обороты, нагрузка, температура"
            )
            InfoRow(
                icon = Icons.Default.LocalGasStation,
                text = "Расход топлива и стиль ускорений"
            )
            InfoRow(
                icon = Icons.Default.Build,
                text = "Детонация и параметры зажигания"
            )
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onStartCollection,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RussianAutoRed
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Начать сбор данных",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CollectingContent(
    progress: Float,
    kmCompleted: Float,
    kmTotal: Int,
    parameters: EngineParametersSnapshot?
    onStop: () -> Unit
) {
    Spacer(modifier = Modifier.height(32.dp))
    
    Text(
        text = "Сбор данных...",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = RussianAutoDark
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "Пройдено: %.1f / %d км".format(kmCompleted, kmTotal),
        fontSize = 18.sp,
        color = RussianAutoDark.copy(alpha = 0.7f)
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    // Progress indicator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(180.dp),
            strokeWidth = 12.dp,
            color = RussianAutoRed,
            trackColor = RussianAutoGray
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(progress * 100).toInt()}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = RussianAutoRed
            )
            Text(
                text = "%",
                fontSize = 20.sp,
                color = RussianAutoDark.copy(alpha = 0.7f)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Current parameters display
    parameters?.let { params ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = RussianAutoGray
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Текущие параметры:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = RussianAutoDark
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ParameterRow("Обороты", "${params.rpm.toInt()} об/мин")
                ParameterRow("Нагрузка", "${params.engineLoad.toInt()}%")
                ParameterRow("Температура", "${params.coolantTemp.toInt()}°C")
                ParameterRow("Скорость", "${params.speed.toInt()} км/ч")
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        OutlinedButton(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RussianAutoRed
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Stop, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Остановить и проанализировать",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun AnalyzingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        CircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            color = RussianAutoRed,
            strokeWidth = 8.dp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Анализ данных...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RussianAutoDark
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Нейросеть анализирует стиль вождения\nи формирует рекомендации",
            fontSize = 16.sp,
            color = RussianAutoDark.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ResultsReadyContent(
    onViewResults: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFF4CAF50)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Анализ завершён!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RussianAutoDark
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Рекомендации готовы к просмотру",
            fontSize = 16.sp,
            color = RussianAutoDark.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onViewResults,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RussianAutoRed
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Visibility, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Посмотреть результаты",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = RussianAutoRed
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Ошибка",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RussianAutoDark
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            fontSize = 16.sp,
            color = RussianAutoDark.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RussianAutoRed
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Попробовать снова",
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun KmSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedKm by remember { mutableStateOf(10) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите пробег для анализа") },
        text = {
            Column {
                Text(
                    "Укажите, сколько километров проехать для сбора данных",
                    fontSize = 14.sp,
                    color = RussianAutoDark.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    KmPresetButton(5, selectedKm) { selectedKm = it }
                    KmPresetButton(10, selectedKm) { selectedKm = it }
                    KmPresetButton(20, selectedKm) { selectedKm = it }
                    KmPresetButton(50, selectedKm) { selectedKm = it }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Выбрано: $selectedKm км",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = RussianAutoRed,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedKm) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RussianAutoRed
                )
            ) {
                Text("Начать")
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
private fun KmPresetButton(
    km: Int,
    selectedKm: Int,
    onClick: (Int) -> Unit
) {
    val isSelected = km == selectedKm
    
    Button(
        onClick = { onClick(km) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) RussianAutoRed else RussianAutoGray,
            contentColor = if (isSelected) Color.White else RussianAutoDark
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(60.dp, 44.dp)
    ) {
        Text(
            "$km",
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RussianAutoRed,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = RussianAutoDark
        )
    }
}

@Composable
private fun ParameterRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = RussianAutoDark.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = RussianAutoDark
        )
    }
}
