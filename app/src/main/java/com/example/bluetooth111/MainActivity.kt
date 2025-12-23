package com.example.bluetooth111

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.bluetooth111.ads.YandexAdsManager
import com.example.bluetooth111.bluetooth.BluetoothManager
import com.example.bluetooth111.ui.MainScreen
import com.example.bluetooth111.ui.theme.Bluetooth111Theme
import com.yandex.mobile.ads.common.MobileAds

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var adsManager: YandexAdsManager
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Разрешения обработаны
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализация менеджера рекламы
        adsManager = YandexAdsManager(this)
        
        // Инициализация Yandex Mobile Ads SDK
        MobileAds.initialize(this) {
            android.util.Log.d("MainActivity", "✅ Yandex Mobile Ads SDK инициализирован")
            // Загружаем и показываем рекламу ТОЛЬКО при старте приложения
            adsManager.loadAd(autoShow = true)
        }
        
        bluetoothManager = BluetoothManager(this)
        
        // Запрашиваем разрешения
        requestBluetoothPermissions()
        
        setContent {
            Bluetooth111Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(bluetoothManager = bluetoothManager)
                }
            }
        }
    }
    
    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            } else {
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
            }
            // Для Huawei и других устройств обязательно нужно местоположение
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            android.util.Log.d("MainActivity", "Requesting permissions: ${permissionsToRequest.joinToString()}")
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            android.util.Log.d("MainActivity", "All permissions granted")
            // Проверяем, включен ли Bluetooth
            checkBluetoothEnabled()
        }
    }
    
    private fun checkBluetoothEnabled() {
        val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled != true) {
            android.util.Log.w("MainActivity", "Bluetooth is disabled")
            // Можно показать диалог с просьбой включить Bluetooth
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Освобождаем ресурсы рекламы
        adsManager.destroy()
        // Отключаемся от Bluetooth при закрытии приложения
        // bluetoothManager.disconnect() // Раскомментируйте если нужно
    }
}