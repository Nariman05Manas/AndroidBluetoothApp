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
fun RainSensorCodeView(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showCopiedMessage by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Скетч для датчика дождя") },
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
                        text = "Этот скетч считывает данные с датчика дождя и DHT11 (температура и влажность) " +
                                "и передает их через Bluetooth Low Energy (BLE) на Android приложение.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Компоненты:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "• ESP32 (любая модель)\n" +
                                "• Датчик дождя (Rain sensor module)\n" +
                                "• Датчик DHT11 (температура и влажность)\n" +
                                "• Соединительные провода\n" +
                                "• Питание 3.3V-5V",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Подключение:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "• Датчик дождя (аналоговый): пин 2 (ADC)\n" +
                                "• DHT11 (данные): пин 10\n" +
                                "• Питание: 3.3V/5V и GND для обоих датчиков",
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
                                val clip = ClipData.newPlainText("Arduino Code", getRainSensorCode())
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
                                text = getRainSensorCode(),
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
                        text = "1. Установите библиотеку DHT через Arduino IDE:\n" +
                                "   Sketch → Include Library → Manage Libraries → DHT sensor library\n" +
                                "2. Загрузите скетч на ESP32-C6 через Arduino IDE\n" +
                                "3. Подключите датчик дождя к пину 2 (аналоговый)\n" +
                                "4. Подключите DHT11 к пину 10\n" +
                                "5. Откройте приложение на телефоне\n" +
                                "6. Перейдите в раздел 'Поиск'\n" +
                                "7. Найдите 'ESP32 Weather Station' и подключитесь\n" +
                                "8. Откройте 'Проекты' → 'Датчик влаги и температуры'\n" +
                                "9. Смотрите данные в реальном времени",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun getRainSensorCode(): String {
    return """#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <DHT.h>

// Конфигурация пинов для ESP32-C6
#define RAIN_SENSOR_PIN 2   // Аналоговый пин (ADC1_CH2)
#define DHT_PIN 10          // Цифровой пин для DHT11
#define DHT_TYPE DHT11      // Тип датчика DHT

DHT dht(DHT_PIN, DHT_TYPE);

// BLE настройки
#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

BLEServer *pServer = NULL;
BLECharacteristic *pCharacteristic = NULL;
bool deviceConnected = false;

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

void setup() {
    Serial.begin(115200);
    Serial.println("Инициализация метеостанции...");
    
    // Инициализация датчиков
    dht.begin();
    pinMode(RAIN_SENSOR_PIN, INPUT);
    
    // Инициализация BLE
    BLEDevice::init("ESP32 Weather Station");
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks());
    
    BLEService *pService = pServer->createService(SERVICE_UUID);
    pCharacteristic = pService->createCharacteristic(
        CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ |
        BLECharacteristic::PROPERTY_WRITE |
        BLECharacteristic::PROPERTY_NOTIFY
    );
    
    pCharacteristic->setValue("Weather Station Ready");
    pService->start();
    
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);
    pAdvertising->setMinPreferred(0x12);
    BLEDevice::startAdvertising();
    
    Serial.println("Метеостанция готова к работе");
}

void loop() {
    if (deviceConnected) {
        // Чтение температуры и влажности
        float temperature = dht.readTemperature();
        float humidity = dht.readHumidity();
        
        // Чтение датчика дождя (0-4095)
        int rainValue = analogRead(RAIN_SENSOR_PIN);
        // Преобразуем в проценты (инвертированно: 0 = сухо, 100 = мокро)
        int rainPercentage = map(rainValue, 4095, 0, 0, 100);
        rainPercentage = constrain(rainPercentage, 0, 100);
        
        // Проверка на ошибки чтения
        if (isnan(temperature) || isnan(humidity)) {
            Serial.println("Ошибка чтения DHT11!");
            temperature = 0.0;
            humidity = 0.0;
        }
        
        // Отправка данных через BLE
        String data = "TEMP:" + String(temperature, 1) + "\n";
        data += "HUMID:" + String(humidity, 1) + "\n";
        data += "RAIN:" + String(rainPercentage);
        
        pCharacteristic->setValue(data.c_str());
        pCharacteristic->notify();
        
        // Вывод в Serial Monitor
        Serial.println("=== Данные метеостанции ===");
        Serial.print("Температура: ");
        Serial.print(temperature);
        Serial.println(" °C");
        Serial.print("Влажность: ");
        Serial.print(humidity);
        Serial.println(" %");
        Serial.print("Дождь: ");
        Serial.print(rainPercentage);
        Serial.print("% (значение: ");
        Serial.print(rainValue);
        Serial.println(")");
        
        if (rainPercentage > 70) {
            Serial.println("⛈ СИЛЬНЫЙ ДОЖДЬ!");
        } else if (rainPercentage > 40) {
            Serial.println("🌧 Идет дождь");
        } else if (rainPercentage > 20) {
            Serial.println("💧 Небольшая влага");
        } else {
            Serial.println("☀️ Сухо");
        }
        Serial.println("==========================");
    }
    
    delay(2000); // Обновление каждые 2 секунды
}"""
}

