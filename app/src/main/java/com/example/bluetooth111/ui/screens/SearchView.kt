package com.example.bluetooth111.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluetooth111.bluetooth.BluetoothManager
import kotlinx.coroutines.launch

@Composable
fun SearchView(bluetoothManager: BluetoothManager) {
    val peripherals by bluetoothManager.peripherals.collectAsState()
    val isConnected by bluetoothManager.isConnected.collectAsState()
    val connectionStatus by bluetoothManager.connectionStatus.collectAsState()
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isConnected) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = "Успешно подключено!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Text(
            text = connectionStatus,
            style = MaterialTheme.typography.titleMedium,
            color = getStatusColor(connectionStatus)
        )
        
        Text(
            text = "Нажмите для поиска устройства",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Button(
            onClick = { bluetoothManager.startScanning() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Начать поиск", style = MaterialTheme.typography.titleMedium)
        }
        
        if (peripherals.isNotEmpty()) {
            Text(
                text = "Найденные устройства:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(peripherals) { peripheral ->
                PeripheralRow(
                    peripheral = peripheral,
                    bluetoothManager = bluetoothManager,
                    onTap = {
                        scope.launch {
                            bluetoothManager.connect(peripheral)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PeripheralRow(
    peripheral: android.bluetooth.BluetoothDevice,
    bluetoothManager: BluetoothManager,
    onTap: () -> Unit
) {
    val isConnected = remember(peripheral.address) {
        bluetoothManager.isConnected(peripheral)
    }
    
    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.Done else Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = peripheral.name ?: "Unknown Device",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isConnected) "Подключено ✓" else "Нажмите для подключения",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = peripheral.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isConnected) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Connected",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun getStatusColor(status: String): Color {
    return when (status) {
        "Connected", "Bluetooth Ready" -> Color(0xFF4CAF50)
        "Scanning...", "Connecting...", "Resetting..." -> Color(0xFFFF9800)
        "Disconnected", "Scan Stopped" -> Color.Gray
        "Connection Failed", "Bluetooth Off", "Unauthorized", "Unsupported" -> Color(0xFFF44336)
        else -> Color.Black
    }
}
