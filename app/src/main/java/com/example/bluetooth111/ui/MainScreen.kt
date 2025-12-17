package com.example.bluetooth111.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bluetooth111.bluetooth.BluetoothManager
import com.example.bluetooth111.ui.screens.ArduinoProjectsView
import com.example.bluetooth111.ui.screens.CommandLineScreen
import com.example.bluetooth111.ui.screens.MotorControlView
import com.example.bluetooth111.ui.screens.SearchView
import com.example.bluetooth111.ui.screens.SensorDataView
import com.example.bluetooth111.ui.screens.SocialNetworksScreen

@Composable
fun MainScreen(bluetoothManager: BluetoothManager) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showMotorControl by remember { mutableStateOf(false) }
    var showSensorData by remember { mutableStateOf(false) }
    var showArduinoProjects by remember { mutableStateOf(false) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                    label = { Text("Поиск") },
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Create, contentDescription = "Командная строка") },
                    label = { Text("Командная строка") },
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = "Проекты") },
                    label = { Text("Проекты") },
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "О приложении") },
                    label = { Text("О приложении") },
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> SearchView(bluetoothManager)
                1 -> CommandLineScreen(bluetoothManager)
                2 -> ArduinoProjectsView(
                    bluetoothManager = bluetoothManager,
                    onMotorControlClick = { showMotorControl = true },
                    onSensorDataClick = { showSensorData = true },
                    onArduinoProjectsClick = { }
                )
                3 -> SocialNetworksScreen()
            }
        }
    }
    
    // Полноэкранные диалоги
    if (showMotorControl) {
        Dialog(
            onDismissRequest = { showMotorControl = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                MotorControlView(
                    bluetoothManager = bluetoothManager,
                    onDismiss = { showMotorControl = false }
                )
            }
        }
    }
    
    if (showSensorData) {
        Dialog(
            onDismissRequest = { showSensorData = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                SensorDataView(
                    bluetoothManager = bluetoothManager,
                    onDismiss = { showSensorData = false }
                )
            }
        }
    }
    
    if (showArduinoProjects) {
        AlertDialog(
            onDismissRequest = { showArduinoProjects = false },
            title = { Text("Примеры кода Arduino") },
            text = { 
                Text("Откройте файл ESP32_SETUP.md в корне проекта для примеров кода и инструкций по настройке ESP32.")
            },
            confirmButton = {
                TextButton(onClick = { showArduinoProjects = false }) {
                    Text("OK")
                }
            }
        )
    }
}
