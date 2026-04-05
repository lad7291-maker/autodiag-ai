package com.autodiag.ai.presentation.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Экран онбординга
 * Показывается при первом запуске приложения
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val pages = listOf(
        OnboardingPage(
            title = "Добро пожаловать в AutoDiagAI",
            description = "Ваш интеллектуальный помощник для диагностики автомобиля. Подключайтесь через OBD2 и получайте профессиональный анализ прямо на смартфоне.",
            icon = Icons.Default.DirectionsCar,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        ),
        OnboardingPage(
            title = "Диагностика DTC кодов",
            description = "Считывайте и расшифровывайте коды ошибок. AI поможет понять причину и предложит варианты решения проблемы.",
            icon = Icons.Default.Warning,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        OnboardingPage(
            title = "Анализ параметров двигателя",
            description = "Отслеживайте температуру, обороты, расход топлива и другие параметры в реальном времени. Получайте рекомендации по стилю вождения.",
            icon = Icons.Default.Speed,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        OnboardingPage(
            title = "Безопасность прежде всего",
            description = "Приложение только анализирует и рекомендует. Все изменения настроек требуют вашего подтверждения. Резервные копии создаются автоматически.",
            icon = Icons.Default.Security,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding()
                            onOnboardingComplete()
                        }
                    ) {
                        Text("Пропустить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Индикаторы страниц
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                    )
                }
            }
            
            // Кнопки навигации
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка "Назад"
                AnimatedVisibility(
                    visible = pagerState.currentPage > 0,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Назад")
                    }
                }
                
                // Кнопка "Далее" или "Начать"
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.completeOnboarding()
                            onOnboardingComplete()
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .then(
                            if (pagerState.currentPage == 0) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Text(
                        if (pagerState.currentPage < pages.size - 1) "Далее" else "Начать использование"
                    )
                    if (pagerState.currentPage < pages.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Иконка
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(page.backgroundColor)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Заголовок
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Описание
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

private data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color
)
