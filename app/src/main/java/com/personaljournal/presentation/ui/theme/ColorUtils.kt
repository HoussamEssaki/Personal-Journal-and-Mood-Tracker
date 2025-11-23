package com.personaljournal.presentation.ui.theme

import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String, fallback: Color = PrimaryBlue): Color {
    val prefixed = if (hex.startsWith("#")) hex else "#$hex"
    return runCatching {
        Color(android.graphics.Color.parseColor(prefixed))
    }.getOrElse { fallback }
}
