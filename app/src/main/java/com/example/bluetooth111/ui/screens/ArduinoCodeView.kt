package com.example.bluetooth111.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArduinoCodeView(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showCopiedMessage by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Скетч для ESP32-C6 RC Car") },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Описание
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Описание проекта",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Этот скетч управляет машинкой на ESP32-C6 через Bluetooth Low Energy (BLE). " +
                                "Используются два сервопривода: один для движения (вперед/назад), другой для поворотов (влево/вправо).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Компоненты:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "• ESP32-C6\n" +
                                "• 2x Сервопривод (SG90 или аналогичный)\n" +
                                "• Питание для сервоприводов (5V)\n" +
                                "• Соединительные провода",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Подключение:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "• Сервопривод направления: пин 3\n" +
                                "• Сервопривод движения: пин 6\n" +
                                "• Питание сервоприводов: 5V и GND",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            
            // Код
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Код скетча:",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Arduino Code", getArduinoCode())
                                clipboard.setPrimaryClip(clip)
                                showCopiedMessage = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = "Копировать",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Копировать")
                        }
                    }
                    
                    if (showCopiedMessage) {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(2000)
                            showCopiedMessage = false
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "✅ Код скопирован в буфер обмена",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = getArduinoCode(),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            // Инструкция по использованию
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Как использовать:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "1. Загрузите скетч на ESP32-C6 через Arduino IDE\n" +
                                "2. Откройте приложение на телефоне\n" +
                                "3. Перейдите в раздел 'Поиск'\n" +
                                "4. Найдите 'ESP32-C6 RC Car' и подключитесь\n" +
                                "5. Откройте 'Проекты' → 'Джойстик управления машинкой'\n" +
                                "6. Используйте джойстики для управления",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun getArduinoCode(): String {
    return """#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <ESP32Servo.h>

// Пиновая конфигурация для ESP32-C6
const int SERVO_DIRECTION_PIN = 3;
const int SERVO_MOTOR_PIN = 6;

Servo directionServo;
Servo motorServo;

// BLE настройки
#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

bool deviceConnected = false;

void processCommand(String command);

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
        deviceConnected = true;
        Serial.println("Устройство подключено");
    }
    void onDisconnect(BLEServer* pServer) {
        deviceConnected = false;
        Serial.println("Устройство отключено");
        BLEDevice::startAdvertising();
    }
};

class MotorCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
        String value = String(pCharacteristic->getValue().c_str());
        if (value.length() > 0) {
            String command = value;
            command.trim();
            Serial.print("Получена команда: ");
            Serial.println(command);
            processCommand(command);
        }
    }
};

void setup() {
    Serial.begin(115200);
    Serial.println("Инициализация системы...");
    
    directionServo.attach(SERVO_DIRECTION_PIN);
    motorServo.attach(SERVO_MOTOR_PIN);
    
    directionServo.write(90);
    motorServo.write(90);
    delay(1000);
    
    Serial.println("Оба сервопривода в центральном положении (90°)");
    
    BLEDevice::init("ESP32-C6 RC Car");
    BLEServer *pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks());
    
    BLEService *pService = pServer->createService(SERVICE_UUID);
    BLECharacteristic *pCharacteristic = pService->createCharacteristic(
        CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ |
        BLECharacteristic::PROPERTY_WRITE
    );
    
    pCharacteristic->setCallbacks(new MotorCallbacks());
    pCharacteristic->setValue("Hello World");
    pService->start();
    
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);
    pAdvertising->setMinPreferred(0x12);
    BLEDevice::startAdvertising();
    
    Serial.println("Система готова к работе");
}

void loop() {
    delay(1000);
}

void processCommand(String command) {
    if (command.startsWith("MOTOR:")) {
        int speed = command.substring(6).toInt();
        speed = constrain(speed, -1275, 1275);
        
        int angle;
        if (speed == 0) {
            angle = 90;
        } else if (speed > 0) {
            angle = map(speed, 0, 1275, 90, 180);
        } else {
            angle = map(speed, -1275, 0, 0, 90);
        }
        
        angle = constrain(angle, 0, 180);
        motorServo.write(angle);
        
        Serial.print("ДВИЖЕНИЕ: Скорость=");
        Serial.print(speed);
        Serial.print(", Угол=");
        Serial.print(angle);
        Serial.println("°");
    }
    else if (command.startsWith("SERVO:")) {
        int angle = command.substring(6).toInt();
        angle = constrain(angle, 0, 180);
        int invertedAngle = 180 - angle;
        directionServo.write(invertedAngle);
        
        Serial.print("НАПРАВЛЕНИЕ: ");
        Serial.print(angle);
        Serial.println("°");
    }
    else if (command == "STOP" || command == "CENTER") {
        directionServo.write(90);
        motorServo.write(90);
        Serial.println("ПОЛНАЯ ОСТАНОВКА");
    }
}"""
}

