package com.example.bluetooth111.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluetooth111.bluetooth.BluetoothManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDataView(
    bluetoothManager: BluetoothManager,
    onDismiss: () -> Unit
) {
    var temperature by remember { mutableStateOf(0.0) }
    var humidity by remember { mutableStateOf(0.0) }
    var rainLevel by remember { mutableStateOf(0.0) }
    var lastUpdate by remember { mutableStateOf(System.currentTimeMillis()) }
    
    val isConnected by bluetoothManager.isConnected.collectAsState()
    val connectionStatus by bluetoothManager.connectionStatus.collectAsState()
    val receivedData by bluetoothManager.receivedData.collectAsState()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(receivedData) {
        if (receivedData.isNotEmpty()) {
            parseSensorData(receivedData) { temp, humid, rain ->
                temperature = temp
                humidity = humid
                rainLevel = rain
                lastUpdate = System.currentTimeMillis()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Метеостанция") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ConnectionStatusView(status = connectionStatus)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SensorCard(
                    title = "Температура",
                    value = temperature,
                    unit = "°C",
                    icon = Icons.Default.Star,
                    color = Color(0xFFFF9800)
                )
                
                SensorCard(
                    title = "Влажность",
                    value = humidity,
                    unit = "%",
                    icon = Icons.Default.Star,
                    color = Color(0xFF2196F3)
                )
            }
            
            // Карточка датчика дождя
            if (rainLevel > 0) {
                SensorCard(
                    title = "Уровень дождя",
                    value = rainLevel,
                    unit = "%",
                    icon = Icons.Default.Star,
                    color = Color(0xFF00BCD4)
                )
                
                // Индикатор состояния
                val rainStatus = when {
                    rainLevel > 70 -> "⛈ Сильный дождь"
                    rainLevel > 40 -> "🌧 Идет дождь"
                    rainLevel > 20 -> "💧 Небольшая влага"
                    else -> "☀️ Сухо"
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = rainStatus,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Text(
                text = "Последнее обновление: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(lastUpdate))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (receivedData.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Полученные данные: $receivedData",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            ControlButtonsView(
                bluetoothManager = bluetoothManager,
                isConnected = isConnected
            )
        }
    }
}

@Composable
fun ConnectionStatusView(status: String) {
    val statusColor = when (status) {
        "Connected", "Bluetooth Ready" -> Color(0xFF4CAF50)
        "Scanning...", "Connecting...", "Resetting..." -> Color(0xFFFF9800)
        "Disconnected", "Bluetooth Off", "Connection Failed" -> Color(0xFFF44336)
        else -> Color.Gray
    }
    
    Row(
        modifier = Modifier
            .background(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = statusColor
        )
    }
}

@Composable
fun SensorCard(
    title: String,
    value: Double,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(30.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${String.format("%.1f", value)}$unit",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ControlButtonsView(
    bluetoothManager: BluetoothManager,
    isConnected: Boolean
) {
    val scope = rememberCoroutineScope()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (isConnected) {
            Button(
                onClick = {
                    scope.launch {
                        bluetoothManager.disconnect()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Text("Отключиться")
            }
        } else {
            Button(
                onClick = { bluetoothManager.startScanning() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Сканировать")
            }
        }
    }
}

private fun parseSensorData(
    data: String,
    onParsed: (temperature: Double, humidity: Double, rain: Double) -> Unit
) {
    val lines = data.split("\n")
    var temp = 0.0
    var humid = 0.0
    var rain = 0.0
    
    lines.forEach { line ->
        val trimmed = line.trim()
        if (trimmed.contains(":")) {
            val parts = trimmed.split(":")
            if (parts.size == 2) {
                val type = parts[0].uppercase()
                val value = parts[1].toDoubleOrNull()
                
                when (type) {
                    "TEMP", "TEMPERATURE" -> value?.let { temp = it }
                    "HUMID", "HUMIDITY" -> value?.let { humid = it }
                    "RAIN" -> value?.let { rain = it }
                }
            }
        }
    }
    
    if (temp != 0.0 || humid != 0.0 || rain != 0.0) {
        onParsed(temp, humid, rain)
    }
}
