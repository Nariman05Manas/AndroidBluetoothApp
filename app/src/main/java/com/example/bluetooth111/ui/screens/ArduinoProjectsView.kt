package com.example.bluetooth111.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bluetooth111.bluetooth.BluetoothManager

data class Project(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArduinoProjectsView(
    bluetoothManager: BluetoothManager,
    onMotorControlClick: () -> Unit,
    onSensorDataClick: () -> Unit,
    onArduinoProjectsClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var showArduinoCode by remember { mutableStateOf(false) }
    var showRainSensorCode by remember { mutableStateOf(false) }
    
    val projects = remember(primaryColor) {
        listOf(
            Project(
                id = "motor",
                name = "Джойстик управления машинкой",
                icon = Icons.Default.PlayArrow,
                color = primaryColor
            ),
            Project(
                id = "sensor",
                name = "Датчик влаги и температуры",
                icon = Icons.Default.Star,
                color = primaryColor
            ),
            Project(
                id = "code",
                name = "Скетч для ESP32-C6 RC Car",
                icon = Icons.Default.Info,
                color = primaryColor
            ),
            Project(
                id = "rain",
                name = "Скетч для датчика дождя",
                icon = Icons.Default.Star,
                color = primaryColor
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Проекты для Arduino") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(projects) { project ->
                ProjectButton(
                    project = project,
                    onClick = {
                        when (project.id) {
                            "motor" -> onMotorControlClick()
                            "sensor" -> onSensorDataClick()
                            "code" -> showArduinoCode = true
                            "rain" -> showRainSensorCode = true
                        }
                    }
                )
            }
        }
    }
    
    if (showArduinoCode) {
        Dialog(
            onDismissRequest = { showArduinoCode = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                ArduinoCodeView(onDismiss = { showArduinoCode = false })
            }
        }
    }
    
    if (showRainSensorCode) {
        Dialog(
            onDismissRequest = { showRainSensorCode = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                RainSensorCodeView(onDismiss = { showRainSensorCode = false })
            }
        }
    }
}

@Composable
fun ProjectButton(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = project.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = project.color,
                modifier = Modifier.size(44.dp)
            )
            
            Icon(
                imageVector = project.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(60.dp)
            )
            
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

