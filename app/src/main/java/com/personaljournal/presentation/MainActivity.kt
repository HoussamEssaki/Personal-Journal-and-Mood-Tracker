package com.personaljournal.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.personaljournal.infrastructure.security.BiometricAuthenticator
import com.personaljournal.infrastructure.security.BiometricAvailability
import com.personaljournal.presentation.screens.onboarding.SplashOnboardingScreen
import com.personaljournal.presentation.screens.security.LockScreen
import com.personaljournal.presentation.ui.theme.PersonalJournalTheme
import com.personaljournal.presentation.ui.theme.colorFromHex
import com.personaljournal.presentation.viewmodel.AppViewModel
import com.personaljournal.presentation.viewmodel.SecurityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var biometricAuthenticator: BiometricAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val appViewModel: AppViewModel = hiltViewModel()
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val theme by appViewModel.theme.collectAsStateWithLifecycle()
            val language by appViewModel.language.collectAsStateWithLifecycle()
            val accentHex by appViewModel.accentColor.collectAsStateWithLifecycle()
            val accessibility by appViewModel.accessibility.collectAsStateWithLifecycle()
            val isLocked by securityViewModel.isLocked.collectAsStateWithLifecycle()
            val pinError by securityViewModel.pinError.collectAsStateWithLifecycle()
            val onboardingPrefs = remember { applicationContext.getSharedPreferences("onboarding_flags", MODE_PRIVATE) }
            val preLockPrefs = remember { applicationContext.getSharedPreferences("prelock_flags", MODE_PRIVATE) }
            val showForgotPinDialog = rememberSaveable { mutableStateOf(false) }
            val preLockDone = rememberSaveable { mutableStateOf(preLockPrefs.getBoolean("prelock_done", false)) }
            val preLockStep = rememberSaveable { mutableStateOf(0) }

            val darkTheme = when (theme) {
                com.personaljournal.domain.repository.ThemeMode.DARK -> true
                com.personaljournal.domain.repository.ThemeMode.LIGHT -> false
                com.personaljournal.domain.repository.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            val accentColor = remember(accentHex) { colorFromHex(accentHex) }
            val showOnboarding = rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(isLocked) {
                if (!isLocked) {
                    val seen = onboardingPrefs.getBoolean("onboarding_seen", false)
                    showOnboarding.value = !seen
                } else {
                    showOnboarding.value = false
                }
            }

            LaunchedEffect(language) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(language)
                )
            }

            PersonalJournalTheme(
                darkTheme = darkTheme,
                accentColor = accentColor,
                fontScale = accessibility.fontScale,
                highContrast = accessibility.highContrast,
                reduceMotion = accessibility.reduceMotion
            ) {
                when {
                    !preLockDone.value -> {
                        when (preLockStep.value) {
                            0 -> GetStartedScreen(onContinue = { preLockStep.value = 1 })
                            else -> CreateAccountScreen(
                                onContinue = { name, email, reminder ->
                                    preLockPrefs.edit()
                                        .putBoolean("prelock_done", true)
                                        .putString("profile_name", name)
                                        .putString("profile_email", email)
                                        .putString("profile_reminder", reminder)
                                        .putString("reminder_pref", reminder)
                                        .putLong("profile_created_at", System.currentTimeMillis())
                                        .apply()
                                    preLockDone.value = true
                                },
                                onSkip = {
                                    preLockPrefs.edit().putBoolean("prelock_done", true).apply()
                                    preLockDone.value = true
                                }
                            )
                        }
                    }
                    isLocked -> {
                        LockScreen(
                            error = pinError,
                            onPinCompleted = { pin ->
                                securityViewModel.unlock(pin) {}
                            },
                            onRequestBiometric = {
                                lifecycleScope.launch {
                                    when (val availability = biometricAuthenticator.availability()) {
                                        BiometricAvailability.Available -> {
                                            val result = biometricAuthenticator.authenticate(this@MainActivity)
                                            if (result) {
                                                securityViewModel.unlockWithBiometric()
                                            } else {
                                                securityViewModel.setError("Biometric check failed. Try again or use PIN.")
                                            }
                                        }
                                        is BiometricAvailability.Unavailable -> {
                                            securityViewModel.setError("${availability.reason} Use your PIN instead.")
                                        }
                                    }
                                }
                            },
                            onForgotPin = {
                                showForgotPinDialog.value = true
                            }
                        )
                        if (showForgotPinDialog.value) {
                            AlertDialog(
                                onDismissRequest = { showForgotPinDialog.value = false },
                                title = { Text("Reset PIN?") },
                                text = {
                                    Text("This will clear your current PIN. You will need to set a new PIN in Settings after unlocking. Your journal data stays on-device.")
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showForgotPinDialog.value = false
                                            lifecycleScope.launch {
                                                securityViewModel.clearPinAndLock()
                                                securityViewModel.setError("PIN cleared. Please set a new PIN in Settings.")
                                            }
                                        }
                                    ) { Text("Reset PIN") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showForgotPinDialog.value = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                    showOnboarding.value -> {
                        SplashOnboardingScreen(
                            onGetStarted = {
                                onboardingPrefs.edit().putBoolean("onboarding_seen", true).apply()
                                showOnboarding.value = false
                            }
                        )
                    }
                    else -> {
                        PersonalJournalApp(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun GetStartedScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF161C2A),
                        Color(0xFF0F1725)
                    )
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Personal Journal",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Secure journaling, mood tracking, and insights tailored to you.",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFE8ECF5))
            )
            val highlights = listOf(
                "End-to-end local security with PIN/biometric",
                "Mood timeline, heatmaps, and guided prompts",
                "Exports (PDF/CSV/JSON) and private backups"
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                highlights.forEach { bullet ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF5BE6B0)
                        )
                        Text(
                            text = bullet,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFD7DEEB))
                        )
                    }
                }
            }
        }
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text("Get started")
        }
    }
}

@Composable
private fun CreateAccountScreen(
    onContinue: (name: String, email: String, reminder: String) -> Unit,
    onSkip: () -> Unit
) {
    val name = rememberSaveable { mutableStateOf("") }
    val email = rememberSaveable { mutableStateOf("") }
    val reminder = rememberSaveable { mutableStateOf("Evening") }
    val error = rememberSaveable { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1725))
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create your account",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Set up a profile to keep preferences, reminders, and exports organized. You can still stay fully offline.",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFE8ECF5))
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFF182235),
                    contentColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    OutlinedTextField(
                        value = reminder.value,
                        onValueChange = { reminder.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Reminder preference") },
                        singleLine = true
                    )
                    if (error.value != null) {
                        Text(
                            text = error.value.orEmpty(),
                            color = Color(0xFFFF8A80),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (name.value.isBlank()) {
                        error.value = "Name is required."
                    } else {
                        error.value = null
                        onContinue(name.value.trim(), email.value.trim(), reminder.value.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Create account") }
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Skip for now", color = Color(0xFFBFC7D7)) }
        }
    }
}
