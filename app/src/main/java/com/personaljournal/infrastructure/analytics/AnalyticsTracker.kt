package com.personaljournal.infrastructure.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.personaljournal.util.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsTracker @Inject constructor(
    private val analytics: FirebaseAnalytics?,
    private val logger: AppLogger
) {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap()) {
        val instance = analytics ?: run {
            logger.d("analytics", "Event '$name' skipped. Firebase Analytics disabled.")
            return
        }
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putString(key, value.toString())
                }
            }
        }
        instance.logEvent(name, bundle)
    }
}
