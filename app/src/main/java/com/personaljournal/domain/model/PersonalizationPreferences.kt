package com.personaljournal.domain.model

enum class LayoutPreset { BALANCED, JOURNAL_FIRST, INSIGHTS_FOCUS }

enum class DensityMode { COMFORTABLE, COMPACT }

enum class QuickCaptureTarget { REFLECTION, GRATITUDE, AUDIO }

data class LayoutPreferences(
    val preset: LayoutPreset = LayoutPreset.BALANCED,
    val densityMode: DensityMode = DensityMode.COMFORTABLE
)

data class PersonalPreferences(
    val quickCapture: QuickCaptureTarget = QuickCaptureTarget.REFLECTION,
    val showPrompts: Boolean = true,
    val autoTagFromMood: Boolean = true
)

data class AccessibilityPreferences(
    val fontScale: Float = 1f,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false
)
