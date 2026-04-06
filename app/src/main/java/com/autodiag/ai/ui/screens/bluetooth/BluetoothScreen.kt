package com.autodiag.ai.ui.screens.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.ui.theme.StatusGood
import com.autodiag.ai.ui.theme.StatusInfo
import com.autodiag.ai.ui.viewmodel.BluetoothViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BluetoothScreen(
    viewModel: BluetoothViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val currentParams by viewModel.currentParameters.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "OBD2 Подключение",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshDevices() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isConnected) {
                FloatingActionButton(
                    onClick = {
                        if (isMonitoring) {
                            viewModel.stopMonitoring()
                        } else {
                            viewModel.startMonitoring()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isMonitoring) {
                            Icons.Default.BluetoothDisabled
                        } else {
                            Icons.Default.BluetoothConnected
                        },
                        contentDescription = if (isMonitoring) "Остановить" else "Старт"
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection Status
            item {
                ConnectionStatusCard(
                    isConnected = isConnected,
                    deviceName = viewModel.connectedDeviceName.collectAsState().value
                )
            }
            
            // Live Data
            if (isMonitoring && currentParams != null) {
                item {
                    Text(
                        text = "Данные в реальном времени",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    LiveDataCard(params = currentParams!!)
                }
            }
            
            // Paired Devices
            if (!isConnected) {
                item {
                    Text(
                        text = "Сопряженные устройства",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (pairedDevices.isEmpty()) {
                    item {
                        EmptyDevicesCard()
                    }
                } else {
                    items(pairedDevices.toList()) { device ->
                        DeviceCard(
                            device = device,
                            onClick = { viewModel.connect(device) }
                        )
                    }
                }
            }
            
            // Loading
            if (uiState is BluetoothViewModel.UiState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(isConnected: Boolean, deviceName: String?) {
    val backgroundColor = if (isConnected) StatusGood.copy(alpha = 0.1f) else StatusInfo.copy(alpha = 0.1f)
    val icon = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled
    val text = if (isConnected) "Подключено" else "Не подключено"
    val subtext = if (isConnected) deviceName ?: "OBD2 адаптер" else "Выберите устройство для подключения"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) StatusGood else StatusInfo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.size(16.dp))
            
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) StatusGood else StatusInfo
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DeviceCard(device: BluetoothDevice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.size(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Неизвестное устройство",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = "Подключиться",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyDevicesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothDisabled,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Устройства не найдены",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Убедитесь, что Bluetooth включен и OBD2 адаптер сопряжен",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun LiveDataCard(params: EngineParameters) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // RPM
            DataRow(
                label = "Обороты",
                value = params.rpm?.let { "$it RPM" } ?: "--",
                icon = Icons.Default.Refresh
            )
            
            // Speed
            DataRow(
                label = "Скорость",
                value = params.speed?.let { "$it км/ч" } ?: "--",
                icon = Icons.Default.BluetoothSearching
            )
            
            // Temperature
            DataRow(
                label = "Температура ОЖ",
                value = params.coolantTemperature?.let { "${it.toInt()}°C" } ?: "--",
                icon = Icons.Default.Bluetooth
            )
            
            // Load
            DataRow(
                label = "Нагрузка",
                value = params.engineLoad?.let { "${it.toInt()}%" } ?: "--",
                icon = Icons.Default.BluetoothConnected
            )
            
            // Throttle
            DataRow(
                label = "Дроссель",
                value = params.throttlePosition?.let { "${it.toInt()}%" } ?: "--",
                icon = Icons.Default.BluetoothDisabled
            )
            
            // Fuel
            DataRow(
                label = "Уровень топлива",
                value = params.fuelLevel?.let { "${it.toInt()}%" } ?: "--",
                icon = Icons.Default.Refresh
            )
        }
    }
}

@Composable
fun DataRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
