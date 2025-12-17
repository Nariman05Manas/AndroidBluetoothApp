package com.example.bluetooth111.bluetooth

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class BluetoothManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    
    private val _peripherals = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val peripherals: StateFlow<List<BluetoothDevice>> = _peripherals
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    private val _currentMotorSpeed = MutableStateFlow(0)
    val currentMotorSpeed: StateFlow<Int> = _currentMotorSpeed
    
    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus
    
    private val _receivedData = MutableStateFlow<String>("")
    val receivedData: StateFlow<String> = _receivedData
    
    private var targetPeripheral: BluetoothDevice? = null
    
    // UUID для ESP32-C6 (из вашего скетча)
    private val esp32ServiceUUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val esp32CharacteristicUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    
    // UUID для других устройств (HM-10 и т.д.)
    private val knownServiceUUIDs = listOf(
        esp32ServiceUUID,
        UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"), // HM-10
        UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"), // Nordic UART Service
        UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")  // Generic
    )
    
    private val knownCharacteristicUUIDs = listOf(
        esp32CharacteristicUUID,
        UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"), // HM-10
        UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"), // Nordic UART TX
        UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"), // Nordic UART RX
        UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")  // Generic
    )
    
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val currentList = _peripherals.value.toMutableList()
            
            if (!currentList.any { it.address == device.address }) {
                currentList.add(device)
                _peripherals.value = currentList
                Log.d("BluetoothManager", "Discovered device: ${device.name ?: "Unknown"} - ${device.address}")
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            Log.e("BluetoothManager", "Scan failed with error: $errorCode")
            _connectionStatus.value = "Scan Failed"
        }
    }
    
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BluetoothManager", "Connected to GATT server, status: $status")
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        _isConnected.value = true
                        _connectionStatus.value = "Connected"
                        // Небольшая задержка перед обнаружением сервисов
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            gatt.discoverServices()
                        }, 600)
                    } else {
                        Log.e("BluetoothManager", "Connection failed with status: $status")
                        _connectionStatus.value = "Connection Failed"
                        gatt.close()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BluetoothManager", "Disconnected from GATT server, status: $status")
                    _isConnected.value = false
                    _connectionStatus.value = if (status == BluetoothGatt.GATT_SUCCESS) {
                        "Disconnected"
                    } else {
                        "Connection Error"
                    }
                    targetPeripheral = null
                    writeCharacteristic = null
                }
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "Services discovered: ${gatt.services.size}")
                
                // Сначала ищем ESP32 сервис и характеристику
                val esp32Service = gatt.getService(esp32ServiceUUID)
                if (esp32Service != null) {
                    Log.d("BluetoothManager", "Found ESP32 service!")
                    val esp32Char = esp32Service.getCharacteristic(esp32CharacteristicUUID)
                    if (esp32Char != null) {
                        writeCharacteristic = esp32Char
                        Log.d("BluetoothManager", "Found ESP32 characteristic - Ready!")
                        _connectionStatus.value = "Ready"
                        return
                    }
                }
                
                // Если ESP32 не найден, ищем другие
                for (service in gatt.services) {
                    Log.d("BluetoothManager", "Service UUID: ${service.uuid}")
                    
                    for (characteristic in service.characteristics) {
                        Log.d("BluetoothManager", "  Characteristic UUID: ${characteristic.uuid}")
                        Log.d("BluetoothManager", "  Properties: ${characteristic.properties}")
                        
                        val isWritable = characteristic.properties and 
                            (BluetoothGattCharacteristic.PROPERTY_WRITE or 
                             BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
                        
                        if (isWritable && writeCharacteristic == null) {
                            writeCharacteristic = characteristic
                            Log.d("BluetoothManager", "Found writable characteristic: ${characteristic.uuid}")
                        }
                    }
                }
                
                if (writeCharacteristic != null) {
                    Log.d("BluetoothManager", "Ready to send data")
                    _connectionStatus.value = "Ready"
                } else {
                    Log.e("BluetoothManager", "No writable characteristic found")
                    _connectionStatus.value = "No suitable characteristic"
                }
            } else {
                Log.e("BluetoothManager", "Service discovery failed with status: $status")
            }
        }
        
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.value
            if (data != null) {
                val stringData = String(data, Charsets.UTF_8)
                _receivedData.value = stringData
                Log.d("BluetoothManager", "Received: $stringData")
            }
        }
        
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "Successfully wrote value to characteristic")
            } else {
                Log.e("BluetoothManager", "Failed to write value to characteristic")
            }
        }
    }
    
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    fun startScanning() {
        if (!hasBluetoothPermission()) {
            _connectionStatus.value = "Permission Denied"
            Log.e("BluetoothManager", "Bluetooth permission denied")
            return
        }
        
        if (bluetoothAdapter?.isEnabled != true) {
            _connectionStatus.value = "Bluetooth Off"
            Log.e("BluetoothManager", "Bluetooth is disabled")
            return
        }
        
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        _peripherals.value = emptyList()
        
        // Также добавляем сопряженные устройства
        try {
            val pairedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
            if (pairedDevices.isNotEmpty()) {
                _peripherals.value = pairedDevices.toList()
                Log.d("BluetoothManager", "Found ${pairedDevices.size} paired devices")
                pairedDevices.forEach { device ->
                    Log.d("BluetoothManager", "Paired: ${device.name} - ${device.address}")
                }
            }
        } catch (e: SecurityException) {
            Log.e("BluetoothManager", "Security exception getting paired devices: ${e.message}")
        }
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        try {
            bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
            _connectionStatus.value = "Scanning..."
            Log.d("BluetoothManager", "Started BLE scan")
        } catch (e: SecurityException) {
            Log.e("BluetoothManager", "Security exception starting scan: ${e.message}")
            _connectionStatus.value = "Permission Error"
        }
    }
    
    fun stopScanning() {
        bluetoothLeScanner?.stopScan(scanCallback)
        _connectionStatus.value = "Scan Stopped"
    }
    
    suspend fun connect(device: BluetoothDevice): Boolean = withContext(Dispatchers.Main) {
        try {
            if (!hasBluetoothPermission()) {
                _connectionStatus.value = "Permission Denied"
                Log.e("BluetoothManager", "Bluetooth permissions not granted")
                return@withContext false
            }
            
            stopScanning()
            
            // Отключаемся от текущего устройства, если подключены
            bluetoothGatt?.let {
                it.disconnect()
                it.close()
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({}, 300)
            }
            
            targetPeripheral = device
            _connectionStatus.value = "Connecting..."
            
            Log.d("BluetoothManager", "Connecting to ${device.name ?: "Unknown"} (${device.address})")
            
            bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                @Suppress("DEPRECATION")
                device.connectGatt(context, false, gattCallback)
            }
            
            true
        } catch (e: Exception) {
            Log.e("BluetoothManager", "Error connecting: ${e.message}", e)
            _connectionStatus.value = "Connection Failed: ${e.message}"
            false
        }
    }
    
    suspend fun disconnect() = withContext(Dispatchers.Main) {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        targetPeripheral = null
        writeCharacteristic = null
        _isConnected.value = false
        _connectionStatus.value = "Disconnected"
    }
    
    fun sendServoAngle(angle: Int) {
        val clampedAngle = maxOf(0, minOf(180, angle))
        val command = "SERVO:$clampedAngle\n"
        sendCommand(command)
    }
    
    fun sendMotorSpeed(speed: Int) {
        val clampedSpeed = maxOf(-255, minOf(255, speed))
        _currentMotorSpeed.value = clampedSpeed
        val command = "MOTOR:$clampedSpeed\n"
        sendCommand(command)
        Log.d("BluetoothManager", "Sent: $command")
    }
    
    fun sendCommand(command: String) {
        if (!_isConnected.value) {
            Log.e("BluetoothManager", "Not connected - cannot send: $command")
            return
        }
        
        if (writeCharacteristic == null) {
            Log.e("BluetoothManager", "Write characteristic is null - cannot send: $command")
            return
        }
        
        if (bluetoothGatt == null) {
            Log.e("BluetoothManager", "GATT is null - cannot send: $command")
            return
        }
        
        try {
            val data = command.toByteArray(Charsets.UTF_8)
            Log.d("BluetoothManager", "Sending command: '$command' (${data.size} bytes)")
            Log.d("BluetoothManager", "Characteristic UUID: ${writeCharacteristic?.uuid}")
            Log.d("BluetoothManager", "Characteristic properties: ${writeCharacteristic?.properties}")
            
            writeCharacteristic?.value = data
            
            // Для ESP32 используем WRITE_TYPE_DEFAULT (как в iOS - withResponse)
            writeCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            
            val success = bluetoothGatt?.writeCharacteristic(writeCharacteristic) ?: false
            if (success) {
                Log.d("BluetoothManager", "✅ Command queued for sending: $command")
            } else {
                Log.e("BluetoothManager", "❌ Failed to queue command: $command")
            }
        } catch (e: Exception) {
            Log.e("BluetoothManager", "❌ Error sending command: ${e.message}", e)
        }
    }
    
    fun isConnected(device: BluetoothDevice): Boolean {
        return _isConnected.value && targetPeripheral?.address == device.address
    }
    
    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    enum class ConnectionState {
        Disconnected,
        Connecting,
        Connected
    }
}
