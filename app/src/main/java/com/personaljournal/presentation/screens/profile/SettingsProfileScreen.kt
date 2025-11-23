package com.personaljournal.presentation.screens.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsProfileScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prelock_flags", Context.MODE_PRIVATE) }

    val nameState = rememberSaveable { mutableStateOf(prefs.getString("profile_name", "").orEmpty()) }
    val emailState = rememberSaveable { mutableStateOf(prefs.getString("profile_email", "").orEmpty()) }
    val reminderState = rememberSaveable {
        mutableStateOf(
            prefs.getString(
                "profile_reminder",
                prefs.getString("reminder_pref", "")
            ).orEmpty()
        )
    }
    val createdAt = remember { prefs.getLong("profile_created_at", 0L) }
    val memberSince = remember(createdAt) {
        if (createdAt == 0L) "Member since today"
        else SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(createdAt))
    }
    val showDialog = remember { mutableStateOf(false) }
    val saveMessage = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)
        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initials = remember(nameState.value) {
                    nameState.value
                        .trim()
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .joinToString("") { it.first().uppercase() }
                        .ifBlank { "PJ" }
                }
                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(16.dp)
                ) {
                    Text(initials, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(
                        if (nameState.value.isNotBlank()) nameState.value else "No name set",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (emailState.value.isNotBlank()) emailState.value else "Email not set",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(memberSince, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Edit profile", style = MaterialTheme.typography.titleMedium)
                TextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors()
                )
                TextField(
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = TextFieldDefaults.colors()
                )
                TextField(
                    value = reminderState.value,
                    onValueChange = { reminderState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reminder preference") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors()
                )
                Button(
                    onClick = {
                        prefs.edit()
                            .putString("profile_name", nameState.value.trim())
                            .putString("profile_email", emailState.value.trim())
                            .putString("profile_reminder", reminderState.value.trim())
                            .putString("reminder_pref", reminderState.value.trim())
                            .putLong("profile_created_at", if (createdAt == 0L) System.currentTimeMillis() else createdAt)
                            .apply()
                        saveMessage.value = "Profile saved"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save changes")
                }
                if (saveMessage.value != null) {
                    Text(
                        text = saveMessage.value!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Security", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Unlock with Biometrics")
                    Switch(checked = true, onCheckedChange = {})
                }
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text("Exports and backups live on this device. Use the Export Center to share or back up securely.", modifier = Modifier.padding(top = 4.dp))
            }
        }
        Button(
            onClick = { showDialog.value = true },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B6B)
            )
        ) {
            Text("Delete Account")
        }
    }
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("Yes, Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Account?") },
            text = {
                Text("This is permanent. All your journal entries, mood data, and personal settings will be erased forever.")
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
