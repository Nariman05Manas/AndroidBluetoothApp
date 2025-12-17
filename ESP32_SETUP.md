# Настройка ESP32 для работы с приложением

## Поддерживаемые модули
- ESP32 (встроенный BLE)
- HM-10 / HM-11
- Arduino с BLE модулем

## Пример кода для ESP32

### Для управления моторами (MotorControlView)

```cpp
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;
bool deviceConnected = false;

// Пины для моторов и серво
const int MOTOR_PIN1 = 16;
const int MOTOR_PIN2 = 17;
const int SERVO_PIN = 18;

// UUID для сервиса (используйте любой уникальный)
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      Serial.println("Устройство подключено");
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("Устройство отключено");
    }
};

class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      String value = pCharacteristic->getValue();
      
      if (value.length() > 0) {
        Serial.print("Получено: ");
        Serial.println(value.c_str());
        
        // Парсим команды
        if (value.startsWith("MOTOR:")) {
          int speed = value.substring(6).toInt();
          setMotorSpeed(speed);
        }
        else if (value.startsWith("SERVO:")) {
          int angle = value.substring(6).toInt();
          setServoAngle(angle);
        }
      }
    }
};

void setup() {
  Serial.begin(115200);
  
  // Настройка пинов
  pinMode(MOTOR_PIN1, OUTPUT);
  pinMode(MOTOR_PIN2, OUTPUT);
  pinMode(SERVO_PIN, OUTPUT);
  
  // Создание BLE устройства
  BLEDevice::init("ESP32_Arduino");
  
  // Создание BLE сервера
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  
  // Создание BLE сервиса
  BLEService *pService = pServer->createService(SERVICE_UUID);
  
  // Создание BLE характеристики
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );
  
  pCharacteristic->setCallbacks(new MyCallbacks());
  pCharacteristic->addDescriptor(new BLE2902());
  
  // Запуск сервиса
  pService->start();
  
  // Запуск рекламы
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);
  BLEDevice::startAdvertising();
  
  Serial.println("BLE устройство готово к подключению");
}

void loop() {
  // Если отключились, перезапускаем рекламу
  if (!deviceConnected) {
    delay(500);
    pServer->startAdvertising();
  }
  delay(10);
}

void setMotorSpeed(int speed) {
  // Управление мотором
  // speed: -255 до 255
  Serial.print("Скорость мотора: ");
  Serial.println(speed);
  
  if (speed > 0) {
    analogWrite(MOTOR_PIN1, speed);
    analogWrite(MOTOR_PIN2, 0);
  } else if (speed < 0) {
    analogWrite(MOTOR_PIN1, 0);
    analogWrite(MOTOR_PIN2, abs(speed));
  } else {
    analogWrite(MOTOR_PIN1, 0);
    analogWrite(MOTOR_PIN2, 0);
  }
}

void setServoAngle(int angle) {
  // Управление сервоприводом
  // angle: 0 до 180 градусов
  Serial.print("Угол серво: ");
  Serial.println(angle);
  
  // Здесь добавьте код для управления серво
  // Можно использовать библиотеку Servo.h или ESP32Servo
}
```

### Для датчика температуры и влажности (SensorDataView)

```cpp
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <DHT.h>

#define DHTPIN 4
#define DHTTYPE DHT22

DHT dht(DHTPIN, DHTTYPE);

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;
bool deviceConnected = false;

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};

void setup() {
  Serial.begin(115200);
  dht.begin();
  
  BLEDevice::init("ESP32_DHT22");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  
  BLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );
  
  pCharacteristic->addDescriptor(new BLE2902());
  pService->start();
  
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);
  BLEDevice::startAdvertising();
  
  Serial.println("BLE готов");
}

void loop() {
  if (deviceConnected) {
    // Читаем данные с датчика
    float temp = dht.readTemperature();
    float humid = dht.readHumidity();
    
    if (!isnan(temp) && !isnan(humid)) {
      // Отправляем температуру
      String tempData = "TEMP:" + String(temp, 1);
      pCharacteristic->setValue(tempData.c_str());
      pCharacteristic->notify();
      delay(100);
      
      // Отправляем влажность
      String humidData = "HUMID:" + String(humid, 1);
      pCharacteristic->setValue(humidData.c_str());
      pCharacteristic->notify();
      
      Serial.printf("Отправлено - T: %.1f°C, H: %.1f%%\n", temp, humid);
    }
  } else {
    pServer->startAdvertising();
  }
  
  delay(2000); // Отправка каждые 2 секунды
}
```

## Формат команд

### От приложения к ESP32:
- `MOTOR:<speed>\n` - управление мотором (speed: -255 до 255)
- `SERVO:<angle>\n` - управление сервоприводом (angle: 0 до 180)
- Любой текст с командной строки

### От ESP32 к приложению:
- `TEMP:<value>` - температура в градусах Цельсия
- `HUMID:<value>` - влажность в процентах

## Библиотеки для ESP32
- `BLEDevice.h` - встроенная в ESP32
- `DHT.h` - для датчика DHT22/DHT11
- `ESP32Servo.h` - для серво (опционально)

## Отладка
1. Откройте Serial Monitor в Arduino IDE (115200 baud)
2. После загрузки кода на ESP32, вы увидите:
   - "BLE устройство готово к подключению"
   - При подключении: "Устройство подключено"
   - Все получаемые команды

## Проблемы и решения

### Приложение не видит ESP32
- Убедитесь, что BLE включен на ESP32
- Проверьте, что ESP32 не подключен к другому устройству
- Перезагрузите ESP32
- Убедитесь, что разрешения Bluetooth предоставлены приложению

### Подключается, но не работает
- Проверьте Serial Monitor - там будут видны все получаемые команды
- Убедитесь, что в коде ESP32 правильно обрабатываются команды
- Проверьте формат команд (должны заканчиваться `\n`)

### Устройство отключается
- Увеличьте интервал отправки данных на ESP32
- Проверьте питание ESP32 (требуется стабильное питание)


