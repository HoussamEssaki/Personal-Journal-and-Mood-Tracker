package com.personaljournal.presentation.screens.personalization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.AccessibilityPreferences
import com.personaljournal.domain.model.DensityMode
import com.personaljournal.domain.model.LayoutPreferences
import com.personaljournal.domain.model.LayoutPreset
import com.personaljournal.domain.model.PersonalPreferences
import com.personaljournal.domain.model.QuickCaptureTarget
import com.personaljournal.domain.repository.ThemeMode
import com.personaljournal.presentation.ui.theme.colorFromHex
import com.personaljournal.presentation.viewmodel.PersonalizationUiState
import com.personaljournal.presentation.viewmodel.PersonalizationViewModel
import kotlin.math.roundToInt

private val accentPalette = listOf(
    "#4C7DFF",
    "#2EC972",
    "#B367F3",
    "#FF6F91",
    "#FF9F45",
    "#FDE047"
)

@Composable
fun PersonalizationRoute(
    viewModel: PersonalizationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PersonalizationThemesScreen(
        state = state,
        onThemeSelected = viewModel::selectTheme,
        onAccentSelected = viewModel::selectAccent,
        onLayoutSelected = viewModel::selectLayoutPreset,
        onDensitySelected = viewModel::selectDensityMode,
        onQuickCaptureSelected = viewModel::selectQuickCapture,
        onShowPromptsChange = viewModel::setShowPrompts,
        onAutoTagChange = viewModel::setAutoTagFromMood,
        onFontScaleChange = viewModel::updateFontScale,
        onReduceMotionChange = viewModel::setReduceMotion,
        onHighContrastChange = viewModel::setHighContrast
    )
}

@Composable
fun PersonalizationThemesScreen(
    state: PersonalizationUiState,
    onThemeSelected: (ThemeMode) -> Unit,
    onAccentSelected: (String) -> Unit,
    onLayoutSelected: (LayoutPreset) -> Unit,
    onDensitySelected: (DensityMode) -> Unit,
    onQuickCaptureSelected: (QuickCaptureTarget) -> Unit,
    onShowPromptsChange: (Boolean) -> Unit,
    onAutoTagChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit
) {
    val tabs = PersonalizationTab.values().toList()
    var selectedTab by rememberSaveable { mutableStateOf(PersonalizationTab.THEME) }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Customization", style = MaterialTheme.typography.headlineSmall)
        TabRow(tabs = tabs, selected = selectedTab, onSelect = { selectedTab = it })
        when (selectedTab) {
            PersonalizationTab.THEME -> {
                PreviewCard()
                ThemeModeSection(
                    currentMode = state.themeMode,
                    onThemeSelected = onThemeSelected
                )
                Spacer(modifier = Modifier.height(12.dp))
                AccentSection(
                    currentAccent = state.accentHex,
                    onAccentSelected = onAccentSelected
                )
            }
            PersonalizationTab.LAYOUT -> {
                LayoutPresetSection(
                    currentPreset = state.layout.preset,
                    onPresetSelected = onLayoutSelected
                )
                Spacer(modifier = Modifier.height(12.dp))
                DensitySection(
                    currentDensity = state.layout.densityMode,
                    onDensitySelected = onDensitySelected
                )
            }
            PersonalizationTab.PERSONAL -> {
                PersonalShortcutsSection(
                    preferences = state.personal,
                    onQuickCaptureSelected = onQuickCaptureSelected,
                    onShowPromptsChange = onShowPromptsChange,
                    onAutoTagChange = onAutoTagChange
                )
            }
            PersonalizationTab.ACCESSIBILITY -> {
                AccessibilitySection(
                    preferences = state.accessibility,
                    onFontScaleChange = onFontScaleChange,
                    onReduceMotionChange = onReduceMotionChange,
                    onHighContrastChange = onHighContrastChange
                )
            }
        }
    }
}

@Composable
private fun TabRow(
    tabs: List<PersonalizationTab>,
    selected: PersonalizationTab,
    onSelect: (PersonalizationTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selected
            Text(
                text = tab.label,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else Color.Transparent
                    )
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun PreviewCard() {
    Card(shape = RoundedCornerShape(28.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Good morning, Alex.", style = MaterialTheme.typography.titleMedium)
            Text(
                "How are you feeling?",
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE0ECFF))
                    .padding(12.dp)
            )
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFECEFF9))
                    .height(80.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ThemeModeSection(
    currentMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Text("Mode", style = MaterialTheme.typography.titleMedium)
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ThemeMode.values().forEach { mode ->
            val selected = mode == currentMode
            Surface(
                tonalElevation = if (selected) 6.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else Color.Transparent
                        )
                        .clickable { onThemeSelected(mode) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(mode.name.lowercase().replaceFirstChar { it.titlecase() })
                }
            }
        }
    }
}

@Composable
private fun AccentSection(
    currentAccent: String,
    onAccentSelected: (String) -> Unit
) {
    Text("Accent Color", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        accentPalette.forEach { hex ->
            val selected = currentAccent.equals(hex, ignoreCase = true)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colorFromHex(hex), CircleShape)
                    .border(
                        width = if (selected) 3.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
                        shape = CircleShape
                    )
                    .clickable { onAccentSelected(hex) }
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.Transparent, CircleShape)
                .border(1.dp, Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("+")
        }
    }
}

@Composable
private fun LayoutPresetSection(
    currentPreset: LayoutPreset,
    onPresetSelected: (LayoutPreset) -> Unit
) {
    Text("Home layout", style = MaterialTheme.typography.titleMedium)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LayoutPreset.values().forEach { preset ->
            val (title, description) = layoutPresetCopy(preset)
            val selected = preset == currentPreset
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPresetSelected(preset) },
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(
                        description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DensitySection(
    currentDensity: DensityMode,
    onDensitySelected: (DensityMode) -> Unit
) {
    Text("Component density", style = MaterialTheme.typography.titleMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DensityMode.values().forEach { mode ->
            val selected = mode == currentDensity
            Surface(
                tonalElevation = if (selected) 4.dp else 0.dp,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDensitySelected(mode) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(mode.displayLabel())
                    Text(
                        text = modeDescription(mode),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalShortcutsSection(
    preferences: PersonalPreferences,
    onQuickCaptureSelected: (QuickCaptureTarget) -> Unit,
    onShowPromptsChange: (Boolean) -> Unit,
    onAutoTagChange: (Boolean) -> Unit
) {
    Text("Quick capture", style = MaterialTheme.typography.titleMedium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickCaptureTarget.values().forEach { target ->
            val selected = target == preferences.quickCapture
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onQuickCaptureSelected(target) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(target.displayLabel(), fontWeight = FontWeight.SemiBold)
                    Text(
                        targetDescription(target),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    ToggleRow(
        title = "Daily prompts",
        description = "Show curated prompts on the dashboard and editor.",
        checked = preferences.showPrompts,
        onCheckedChange = onShowPromptsChange
    )
    ToggleRow(
        title = "Auto-tag moods",
        description = "Add mood-based tags to new entries automatically.",
        checked = preferences.autoTagFromMood,
        onCheckedChange = onAutoTagChange
    )
}

@Composable
private fun AccessibilitySection(
    preferences: AccessibilityPreferences,
    onFontScaleChange: (Float) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit
) {
    Text("Font size", style = MaterialTheme.typography.titleMedium)
    Slider(
        value = preferences.fontScale,
        onValueChange = onFontScaleChange,
        valueRange = 0.8f..1.5f,
        steps = 0
    )
    Text(
        text = "${(preferences.fontScale * 100).roundToInt()}% of default size",
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
    ToggleRow(
        title = "Reduce motion",
        description = "Limit parallax and animated gradients throughout the app.",
        checked = preferences.reduceMotion,
        onCheckedChange = onReduceMotionChange
    )
    ToggleRow(
        title = "High contrast mode",
        description = "Increase color contrast for improved readability.",
        checked = preferences.highContrast,
        onCheckedChange = onHighContrastChange
    )
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun layoutPresetCopy(preset: LayoutPreset): Pair<String, String> = when (preset) {
    LayoutPreset.BALANCED -> "Balanced" to "Mix of stats, calendar, and prompts."
    LayoutPreset.JOURNAL_FIRST -> "Journal first" to "Prioritize entry lists and writing shortcuts."
    LayoutPreset.INSIGHTS_FOCUS -> "Insights focus" to "Highlight streaks, correlations, and recent wins."
}

private fun DensityMode.displayLabel(): String = when (this) {
    DensityMode.COMFORTABLE -> "Comfortable"
    DensityMode.COMPACT -> "Compact"
}

private fun modeDescription(mode: DensityMode): String = when (mode) {
    DensityMode.COMFORTABLE -> "Spacious cards, relaxed spacing."
    DensityMode.COMPACT -> "Dense lists to fit more data."
}

private fun QuickCaptureTarget.displayLabel(): String = when (this) {
    QuickCaptureTarget.REFLECTION -> "Reflection"
    QuickCaptureTarget.GRATITUDE -> "Gratitude"
    QuickCaptureTarget.AUDIO -> "Voice note"
}

private fun targetDescription(target: QuickCaptureTarget): String = when (target) {
    QuickCaptureTarget.REFLECTION -> "Blank entry focused on mood + thoughts."
    QuickCaptureTarget.GRATITUDE -> "Template with gratitude prompts."
    QuickCaptureTarget.AUDIO -> "Jump straight into audio capture."
}

private enum class PersonalizationTab(val label: String) {
    THEME("Theme"),
    LAYOUT("Layout"),
    PERSONAL("Personal"),
    ACCESSIBILITY("Accessibility")
}
