package com.personaljournal.util

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor() {
    fun d(tag: String, message: String) = Log.d("PJ_$tag", message)
    fun e(tag: String, throwable: Throwable) =
        Log.e("PJ_$tag", throwable.message ?: "error", throwable)

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w("PJ_$tag", message, throwable)
        } else {
            Log.w("PJ_$tag", message)
        }
    }
}
