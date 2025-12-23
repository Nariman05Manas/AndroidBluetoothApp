package com.example.bluetooth111.ads

import android.app.Activity
import android.util.Log
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

class YandexAdsManager(private val activity: Activity) {
    
    // Для тестирования используйте: "demo-interstitial-yandex"
    // Для продакшена: "R-M-18065057-1"
    private val adUnitId = "R-M-18065057-1"  // Ваш ID рекламы
    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdLoader: InterstitialAdLoader? = null
    private var shouldShowOnLoad = false  // Флаг для контроля показа
    
    companion object {
        private const val TAG = "YandexAds"
    }
    
    init {
        Log.d(TAG, "🎬 Инициализация YandexAdsManager")
        Log.d(TAG, "📺 Ad Unit ID: $adUnitId")
        
        // Инициализация загрузчика рекламы
        interstitialAdLoader = InterstitialAdLoader(activity).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "✅ Реклама загружена успешно!")
                    interstitialAd = ad
                    
                    // Устанавливаем слушатель событий
                    interstitialAd?.setAdEventListener(object : InterstitialAdEventListener {
                        override fun onAdShown() {
                            Log.d(TAG, "👀 Реклама показана на экране")
                        }
                        
                        override fun onAdFailedToShow(error: com.yandex.mobile.ads.common.AdError) {
                            Log.e(TAG, "❌ Ошибка показа рекламы: ${error.description}")
                            interstitialAd = null
                        }
                        
                        override fun onAdDismissed() {
                            Log.d(TAG, "✋ Реклама закрыта пользователем")
                            interstitialAd = null
                            // Загружаем следующую рекламу, но НЕ показываем автоматически
                            Log.d(TAG, "🔄 Загружаем следующую рекламу (не показывать сразу)...")
                            loadAd(autoShow = false)
                        }
                        
                        override fun onAdClicked() {
                            Log.d(TAG, "👆 Клик по рекламе")
                        }
                        
                        override fun onAdImpression(impressionData: ImpressionData?) {
                            Log.d(TAG, "📊 Показ рекламы зарегистрирован")
                        }
                    })
                    
                    // Показываем рекламу только если флаг установлен
                    if (shouldShowOnLoad) {
                        Log.d(TAG, "   Автопоказ включен, показываем рекламу")
                        showAd()
                        shouldShowOnLoad = false
                    } else {
                        Log.d(TAG, "   Реклама загружена, но автопоказ отключен")
                    }
                }
                
                override fun onAdFailedToLoad(error: AdRequestError) {
                    Log.e(TAG, "❌ ОШИБКА загрузки рекламы!")
                    Log.e(TAG, "   Код ошибки: ${error.code}")
                    Log.e(TAG, "   Описание: ${error.description}")
                    interstitialAd = null
                }
            })
        }
    }
    
    /**
     * Загрузка рекламы
     * @param autoShow - показывать ли рекламу автоматически после загрузки
     */
    fun loadAd(autoShow: Boolean = true) {
        shouldShowOnLoad = autoShow
        
        Log.d(TAG, "⏳ Начинаем загрузку рекламы...")
        Log.d(TAG, "   Ad Unit ID: $adUnitId")
        Log.d(TAG, "   Автопоказ: ${if (autoShow) "ДА" else "НЕТ"}")
        
        try {
            val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()
            interstitialAdLoader?.loadAd(adRequestConfiguration)
            Log.d(TAG, "   Запрос отправлен")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Исключение при загрузке: ${e.message}", e)
        }
    }
    
    /**
     * Показ рекламы
     */
    fun showAd() {
        Log.d(TAG, "🎬 Попытка показать рекламу...")
        interstitialAd?.let { ad ->
            Log.d(TAG, "   Реклама готова, показываем!")
            ad.show(activity)
        } ?: run {
            Log.w(TAG, "   ⚠️ Реклама еще не загружена, ждите...")
        }
    }
    
    /**
     * Проверка, загружена ли реклама
     */
    fun isAdLoaded(): Boolean {
        return interstitialAd != null
    }
    
    /**
     * Освобождение ресурсов
     */
    fun destroy() {
        interstitialAd?.setAdEventListener(null)
        interstitialAd = null
        interstitialAdLoader?.setAdLoadListener(null)
        interstitialAdLoader = null
    }
}

