package com.example.bluetooth111.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluetooth111.bluetooth.BluetoothManager
import kotlin.math.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorControlView(
    bluetoothManager: BluetoothManager,
    onDismiss: () -> Unit
) {
    // Определяем ориентацию экрана
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    var throttlePosition by remember { mutableStateOf(Offset(0f, 0f)) }
    var steeringPosition by remember { mutableStateOf(Offset(0f, 0f)) }
    var isThrottleActive by remember { mutableStateOf(false) }
    var isSteeringActive by remember { mutableStateOf(false) }
    
    var maxThrottle by remember { mutableStateOf(255.0f) }
    var maxSteeringAngle by remember { mutableStateOf(28.0f) }
    var throttleSensitivity by remember { mutableStateOf(1.0f) }
    var steeringSensitivity by remember { mutableStateOf(1.0f) }
    
    var showThrottleSettings by remember { mutableStateOf(false) }
    var showSteeringSettings by remember { mutableStateOf(false) }
    
    val isConnected by bluetoothManager.isConnected.collectAsState()
    val connectionStatus by bluetoothManager.connectionStatus.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Адаптивные размеры для портрета и альбома
    val joystickRadius = if (isLandscape) 50f else 55f  // Уменьшено: 70f → 55f
    val handleRadius = if (isLandscape) 20f else 24f    // Уменьшено: 28f → 24f
    
    val minServoAngle = 62
    val maxServoAngle = 118
    val centerServoAngle = 90
    
    var lastSentSpeed by remember { mutableStateOf(0) }
    var lastSentAngle by remember { mutableStateOf(90) }
    
    // Таймер для постоянной отправки команд (как в iOS версии - каждые 50ms)
    LaunchedEffect(isConnected) {
        if (isConnected) {
            // Используем snapshotFlow для отслеживания изменений позиций
            snapshotFlow { 
                Pair(throttlePosition.y, steeringPosition.x) 
            }.collect { (throttleY, steeringX) ->
                if (!bluetoothManager.isConnected.value) return@collect
                
                val currentSpeed = (-throttleY * maxThrottle * throttleSensitivity).toInt()
                if (currentSpeed != lastSentSpeed) {
                    bluetoothManager.sendMotorSpeed(currentSpeed)
                    lastSentSpeed = currentSpeed
                    android.util.Log.d("MotorControl", "Sent MOTOR: $currentSpeed")
                }
                
                val rawAngle = centerServoAngle + (steeringX * maxSteeringAngle * steeringSensitivity).toInt()
                val currentAngle = maxOf(minServoAngle, minOf(maxServoAngle, rawAngle))
                if (currentAngle != lastSentAngle) {
                    bluetoothManager.sendServoAngle(currentAngle)
                    lastSentAngle = currentAngle
                    android.util.Log.d("MotorControl", "Sent SERVO: $currentAngle")
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLandscape) 8.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 12.dp)
        ) {
            // Верхний ряд с кнопками навигации
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isLandscape) 2.dp else 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка "Назад"
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(if (isLandscape) 24.dp else 32.dp)
                    )
                }
                
                Text(
                    text = "Управление",
                    style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                
                // Кнопка закрытия
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.Red,
                        modifier = Modifier.size(if (isLandscape) 24.dp else 32.dp)
                    )
                }
            }
            // Статус подключения
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        connectionStatus == "Ready" -> Color(0xFF4CAF50)
                        isConnected -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(if (isLandscape) 6.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 8.dp)
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(if (isLandscape) 12.dp else 16.dp)
                    )
                    Text(
                        text = connectionStatus,
                        style = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (connectionStatus != "Ready" && isConnected && !isLandscape) {
                Text(
                    text = "Ожидание готовности...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800)
                )
            }
            
            // Настройки сверху с адаптивными размерами
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isLandscape) 4.dp else 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { showThrottleSettings = true },
                    modifier = Modifier.height(if (isLandscape) 40.dp else 50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1976D2)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = if (isLandscape) 8.dp else 12.dp, 
                        vertical = if (isLandscape) 2.dp else 2.dp
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Газ", 
                            style = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${maxThrottle.toInt()}", 
                            style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                        if (!isLandscape) {
                            Text(
                                "×${String.format("%.1f", throttleSensitivity)}", 
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black
                            )
                        }
                    }
                }
                
                Button(
                    onClick = { showSteeringSettings = true },
                    modifier = Modifier.height(if (isLandscape) 40.dp else 50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1976D2)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = if (isLandscape) 8.dp else 12.dp, 
                        vertical = if (isLandscape) 2.dp else 2.dp
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Угол", 
                            style = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${maxSteeringAngle.toInt()}°", 
                            style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Bold
                        )
                        if (!isLandscape) {
                            Text(
                                "×${String.format("%.1f", steeringSensitivity)}", 
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
            
            // Джойстики с адаптивными отступами
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(
                        top = if (isLandscape) 4.dp else 16.dp, 
                        bottom = if (isLandscape) 4.dp else 32.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Джойстик газа (слева)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (isLandscape) 2.dp else 8.dp)
                ) {
                    if (!isLandscape) {
                        Text("Вперёд\nНазад", style = MaterialTheme.typography.bodySmall)
                    }
                    JoystickView(
                        position = throttlePosition,
                        isActive = isThrottleActive,
                        radius = joystickRadius * 1.5f,
                        handleRadius = handleRadius * 1.2f,
                        color = Color(0xFF4CAF50),
                        axis = JoystickAxis.Vertical,
                        onPositionChange = { throttlePosition = it; isThrottleActive = true },
                        onRelease = { throttlePosition = Offset(0f, 0f); isThrottleActive = false }
                    )
                    Text(
                        "${abs(throttlePosition.y * maxThrottle * throttleSensitivity).toInt()}",
                        style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Джойстик руля (справа)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (isLandscape) 2.dp else 8.dp)
                ) {
                    if (!isLandscape) {
                        Text("Влево\nВправо", style = MaterialTheme.typography.bodySmall)
                    }
                    JoystickView(
                        position = steeringPosition,
                        isActive = isSteeringActive,
                        radius = joystickRadius * 1.5f,
                        handleRadius = handleRadius * 1.2f,
                        color = Color(0xFF2196F3),
                        axis = JoystickAxis.Horizontal,
                        onPositionChange = { steeringPosition = it; isSteeringActive = true },
                        onRelease = { steeringPosition = Offset(0f, 0f); isSteeringActive = false }
                    )
                    Text(
                        "${(steeringPosition.x * maxSteeringAngle * steeringSensitivity).toInt()}°",
                        style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Дополнительный отступ между джойстиками и кнопками
            Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 24.dp))
            
            // Кнопки управления с адаптивными размерами
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isLandscape) 4.dp else 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { bluetoothManager.sendMotorSpeed(-maxThrottle.toInt()) },
                    modifier = if (isLandscape) Modifier.height(36.dp) else Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    contentPadding = if (isLandscape) PaddingValues(horizontal = 12.dp, vertical = 4.dp) else ButtonDefaults.ContentPadding
                ) {
                    Text(
                        "Назад",
                        style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = {
                        throttlePosition = Offset(0f, 0f)
                        bluetoothManager.sendMotorSpeed(0)
                    },
                    modifier = if (isLandscape) Modifier.height(36.dp) else Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    contentPadding = if (isLandscape) PaddingValues(horizontal = 12.dp, vertical = 4.dp) else ButtonDefaults.ContentPadding
                ) {
                    Text(
                        "Стоп",
                        style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = { bluetoothManager.sendMotorSpeed(maxThrottle.toInt()) },
                    modifier = if (isLandscape) Modifier.height(36.dp) else Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    contentPadding = if (isLandscape) PaddingValues(horizontal = 12.dp, vertical = 4.dp) else ButtonDefaults.ContentPadding
                ) {
                    Text(
                        "Вперёд",
                        style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Модальные окна настроек
        if (showThrottleSettings) {
            ThrottleSettingsDialog(
                maxThrottle = maxThrottle,
                sensitivity = throttleSensitivity,
                onMaxThrottleChange = { maxThrottle = it },
                onSensitivityChange = { throttleSensitivity = it },
                onDismiss = { showThrottleSettings = false }
            )
        }
        
        if (showSteeringSettings) {
            SteeringSettingsDialog(
                maxAngle = maxSteeringAngle,
                sensitivity = steeringSensitivity,
                onMaxAngleChange = { maxSteeringAngle = it },
                onSensitivityChange = { steeringSensitivity = it },
                onDismiss = { showSteeringSettings = false }
            )
        }
    }
}

enum class JoystickAxis {
    Vertical, Horizontal
}

@Composable
fun JoystickView(
    position: Offset,
    isActive: Boolean,
    radius: Float,
    handleRadius: Float,
    color: Color,
    axis: JoystickAxis,
    onPositionChange: (Offset) -> Unit,
    onRelease: () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val radiusPx = with(density) { radius.dp.toPx() }
    
    Box(
        modifier = Modifier
            .size((radius * 2 + 40).dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { onRelease() }
                ) { change, _ ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val location = change.position
                    var deltaX = location.x - centerX
                    var deltaY = location.y - centerY
                    
                    // Ограничиваем по одной оси
                    when (axis) {
                        JoystickAxis.Vertical -> deltaX = 0f
                        JoystickAxis.Horizontal -> deltaY = 0f
                    }
                    
                    val distance = minOf(sqrt(deltaX * deltaX + deltaY * deltaY), radiusPx)
                    val angle = atan2(deltaY, deltaX)
                    
                    val normalizedDistance = distance / radiusPx
                    
                    val newPosition = Offset(
                        x = if (axis == JoystickAxis.Horizontal) cos(angle) * normalizedDistance else 0f,
                        y = if (axis == JoystickAxis.Vertical) sin(angle) * normalizedDistance else 0f
                    )
                    
                    onPositionChange(newPosition)
                }
            }
    ) {
        val handleRadiusPx = with(density) { handleRadius.dp.toPx() }
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Фон джойстика (серый круг)
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = radiusPx,
                center = center
            )
            
            // Ось
            if (axis == JoystickAxis.Vertical) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(center.x, center.y - radiusPx),
                    end = Offset(center.x, center.y + radiusPx),
                    strokeWidth = 3f
                )
            } else {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(center.x - radiusPx, center.y),
                    end = Offset(center.x + radiusPx, center.y),
                    strokeWidth = 3f
                )
            }
            
            // Ручка (ползунок)
            val handleCenter = Offset(
                center.x + (if (axis == JoystickAxis.Horizontal) position.x * radiusPx else 0f),
                center.y + (if (axis == JoystickAxis.Vertical) position.y * radiusPx else 0f)
            )
            
            drawCircle(
                color = color.copy(alpha = 0.7f),
                radius = handleRadiusPx,
                center = handleCenter
            )
            
            // Обводка ручки
            drawCircle(
                color = color,
                radius = handleRadiusPx,
                center = handleCenter,
                style = Stroke(width = 4f)
            )
            
            // Полоска внутри ручки
            if (axis == JoystickAxis.Vertical) {
                // Вертикальная полоска для джойстика "Вперёд/Назад"
                drawLine(
                    color = Color.White,
                    start = Offset(handleCenter.x, handleCenter.y - handleRadiusPx * 0.6f),
                    end = Offset(handleCenter.x, handleCenter.y + handleRadiusPx * 0.6f),
                    strokeWidth = 4f
                )
            } else {
                // Горизонтальная полоска для джойстика "Влево/Вправо"
                drawLine(
                    color = Color.White,
                    start = Offset(handleCenter.x - handleRadiusPx * 0.6f, handleCenter.y),
                    end = Offset(handleCenter.x + handleRadiusPx * 0.6f, handleCenter.y),
                    strokeWidth = 4f
                )
            }
        }
    }
}

@Composable
fun ThrottleSettingsDialog(
    maxThrottle: Float,
    sensitivity: Float,
    onMaxThrottleChange: (Float) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки газа") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Максимальный газ: ${maxThrottle.toInt()}")
                Slider(
                    value = maxThrottle,
                    onValueChange = onMaxThrottleChange,
                    valueRange = 100f..255f,
                    steps = 30
                )
                Text("Чувствительность: ${sensitivity}")
                Slider(
                    value = sensitivity,
                    onValueChange = onSensitivityChange,
                    valueRange = 0.1f..2.0f,
                    steps = 18
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Готово")
            }
        }
    )
}

@Composable
fun SteeringSettingsDialog(
    maxAngle: Float,
    sensitivity: Float,
    onMaxAngleChange: (Float) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки поворота") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Максимальный угол: ${maxAngle.toInt()}°")
                Slider(
                    value = maxAngle,
                    onValueChange = onMaxAngleChange,
                    valueRange = 15f..45f,
                    steps = 29
                )
                Text("Чувствительность: ${sensitivity}")
                Slider(
                    value = sensitivity,
                    onValueChange = onSensitivityChange,
                    valueRange = 0.1f..2.0f,
                    steps = 18
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Готово")
            }
        }
    )
}

