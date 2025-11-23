package com.personaljournal.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.biometric.BiometricManager
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.ReminderType
import com.personaljournal.domain.repository.ThemeMode
import com.personaljournal.presentation.viewmodel.SecurityViewModel
import com.personaljournal.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    securityViewModel: SecurityViewModel = hiltViewModel(),
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenExportCenter: () -> Unit
) {
    val state by settingsViewModel.state.collectAsStateWithLifecycle()
    val biometrics by securityViewModel.biometricsEnabled.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        biometricsEnabled = biometrics,
        onThemeSelected = settingsViewModel::setTheme,
        onLanguageSelected = settingsViewModel::setLanguage,
        onReminderToggle = { enabled ->
            if (enabled) settingsViewModel.scheduleReminder(ReminderType.DAILY, 20, 0)
            else settingsViewModel.disableReminder()
        },
        onBackup = settingsViewModel::backup,
        onSavePin = securityViewModel::savePin,
        onBiometricToggle = securityViewModel::enableBiometrics,
        onOpenProfile = onOpenProfile,
        onOpenNotifications = onOpenNotifications,
        onOpenExportCenter = onOpenExportCenter
    )
}

@Composable
fun SettingsScreen(
    state: com.personaljournal.presentation.viewmodel.SettingsUiState,
    biometricsEnabled: Boolean,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onBackup: () -> Unit,
    onSavePin: (String) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenExportCenter: () -> Unit
) {
    val context = LocalContext.current
    val background = Color(0xFF050B16)
    var pinText by remember { mutableStateOf("") }
    var biometricError by rememberSaveable { mutableStateOf<String?>(null) }
    val biometricStatus = BiometricManager.from(context)
        .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        SettingsSection(title = "Account") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Alex Doe", color = Color.White)
                    Text("Member since Aug 2023", color = Color(0xFF6B7BA0))
                }
                androidx.compose.material3.TextButton(onClick = onOpenProfile) {
                    Text("View profile")
                }
            }
            Text(
                text = "Email Address",
                color = Color(0xFF6B7BA0),
                modifier = Modifier.padding(top = 12.dp)
            )
            Text("alex.doe@example.com", color = Color.White)
            Text("Subscription  •  Premium", color = Color(0xFF6B7BA0))
        }
        SettingsSection(title = "Preferences") {
            Text("Theme", color = Color.White)
            OptionRow(
                options = ThemeMode.values().map { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                onSelect = { label ->
                    val mode = ThemeMode.values().first { it.name.startsWith(label.uppercase()) }
                    onThemeSelected(mode)
                }
            )
            Text("Language", color = Color.White, modifier = Modifier.padding(top = 12.dp))
            OptionRow(
                options = listOf("EN", "FR"),
                onSelect = { onLanguageSelected(it.lowercase()) }
            )
        }
        SettingsSection(title = "Reminders") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Reminder", color = Color.White)
                    Text("8:00 PM", color = Color(0xFF6B7BA0))
                }
                Switch(checked = state.reminder?.enabled == true, onCheckedChange = onReminderToggle)
            }
            androidx.compose.material3.TextButton(
                onClick = onOpenNotifications,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Open reminder center")
            }
        }
        SettingsSection(title = "Security") {
            TextField(
                value = pinText,
                onValueChange = { pinText = it.take(6) },
                label = { Text("PIN", color = Color.White) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSavePin(pinText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Save PIN")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Biometric Lock", color = Color.White)
                Switch(
                    checked = biometricsEnabled,
                    enabled = true,
                    onCheckedChange = { enabled ->
                        when {
                            enabled && biometricStatus == BiometricManager.BIOMETRIC_SUCCESS -> {
                                biometricError = null
                                onBiometricToggle(true)
                            }
                            enabled -> {
                                biometricError = when (biometricStatus) {
                                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                                        "This device has no biometric hardware."
                                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                                        "Biometric hardware is temporarily unavailable. Try again later."
                                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                                        "No biometrics enrolled. Please add a fingerprint/face in system settings."
                                    else -> "Biometric authentication is unavailable on this device."
                                }
                                onBiometricToggle(false)
                            }
                            else -> {
                                biometricError = null
                                onBiometricToggle(false)
                            }
                        }
                    }
                )
            }
            biometricError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Button(
                onClick = onBackup,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Export & Backup")
            }
            androidx.compose.material3.TextButton(
                onClick = onOpenExportCenter,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Open export center")
            }
        }
        state.message?.let {
            Text(text = it, color = Color(0xFF5ED4A2))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun OptionRow(
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { label ->
            Button(onClick = { onSelect(label) }) {
                Text(label)
            }
        }
    }
}
