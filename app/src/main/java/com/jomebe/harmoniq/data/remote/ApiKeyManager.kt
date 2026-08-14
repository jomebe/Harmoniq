package com.jomebe.harmoniq.data.remote

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger
import retrofit2.HttpException

class ApiKeyManager(
    initialKeys: List<String> = emptyList()
) {
    private val keys: List<String> = (initialKeys.filter { it.isNotBlank() } + DEFAULT_KEYS).distinct()
    private val currentIndex = AtomicInteger(0)

    val currentKey: String
        get() = keys[currentIndex.get() % keys.size]

    fun rotateKey(): String {
        val next = currentIndex.incrementAndGet() % keys.size
        Log.d("ApiKeyManager", "Rotating YouTube API key to index $next")
        return keys[next]
    }

    suspend fun <T> executeWithRetry(block: suspend (apiKey: String) -> T): T {
        check(keys.isNotEmpty()) { "사용 가능한 YouTube API 키가 없습니다." }
        var lastException: Throwable? = null
        val totalKeys = keys.size

        for (attempt in 0 until totalKeys) {
            val key = currentKey
            try {
                return block(key)
            } catch (e: HttpException) {
                lastException = e
                val code = e.code()
                // 403 (Quota Exceeded / Forbidden), 400 (Bad Request / Invalid Key), 429 (Too Many Requests)
                if (code == 403 || code == 400 || code == 429) {
                    Log.w("ApiKeyManager", "API Key failed with HTTP $code, rotating to next key (attempt ${attempt + 1}/$totalKeys)")
                    rotateKey()
                } else {
                    throw e
                }
            } catch (e: Exception) {
                lastException = e
                val message = e.message.orEmpty()
                if (message.contains("quota", ignoreCase = true) || message.contains("key", ignoreCase = true)) {
                    Log.w("ApiKeyManager", "API Key error: $message, rotating to next key (attempt ${attempt + 1}/$totalKeys)")
                    rotateKey()
                } else {
                    throw e
                }
            }
        }
        throw lastException ?: IllegalStateException("모든 YouTube API 키가 소진되었습니다.")
    }

    companion object {
        val DEFAULT_KEYS = listOf(
            "AIzaSyDrYBj6j1B1a9uMWDmUm-sC1eG_uyy5yno",
            "AIzaSyDr06sLiwHpu9OUA8Q9FCMSnKgh-C-hCtA",
            "AIzaSyBBQsDRauQiAaHvhLkt_dNrfo62raBntzc",
            "AIzaSyDSw32q6TJ2JEKBW8VCR9dv6L61R6cJgtU",
            "AIzaSyDJit4L27z7B3q80ZZzce7AWS0Um0WZpwU"
        )
    }
}
