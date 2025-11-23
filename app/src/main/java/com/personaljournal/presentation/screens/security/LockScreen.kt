package com.personaljournal.presentation.screens.security

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LockScreen(
    error: String?,
    onPinCompleted: (String) -> Unit,
    onRequestBiometric: () -> Unit,
    onForgotPin: () -> Unit
) {
    val digits = remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040608))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = Color(0xFF112018)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = "Journal lock",
                        tint = Color(0xFF6EE7B7)
                    )
                }
            }
            Text(
                text = "Unlock Your Journal",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = "Enter your PIN to continue",
                color = Color(0xFF8DA3B8),
                modifier = Modifier.padding(top = 8.dp)
            )
            PinDots(count = 4, filled = digits.value.length)
            error?.let {
                Text(
                    text = it,
                    color = Color(0xFFFF8F8F),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NumericPad(
                onNumber = { number ->
                    if (digits.value.length < 4) {
                        digits.value += number
                        if (digits.value.length == 4) {
                            onPinCompleted(digits.value)
                            digits.value = ""
                        }
                    }
                },
                onDelete = {
                    if (digits.value.isNotEmpty()) {
                        digits.value = digits.value.dropLast(1)
                    }
                }
            )
            Icon(
                Icons.Outlined.Fingerprint,
                contentDescription = "Use biometric",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRequestBiometric() },
                tint = Color(0xFF6EE7B7)
            )
            Text(
                text = "Forgot PIN?",
                color = Color(0xFF6EE7B7),
                modifier = Modifier.clickable { onForgotPin() }
            )
        }
    }
}

@Composable
private fun PinDots(
    count: Int,
    filled: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 24.dp)
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < filled) Color(0xFF75F3D4) else Color(0xFF1F2A37)
                    )
            )
        }
    }
}

@Composable
private fun NumericPad(
    onNumber: (String) -> Unit,
    onDelete: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("bio", "0", "del")
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { label ->
                    when (label) {
                        "bio" -> Spacer(modifier = Modifier.size(72.dp))
                        "del" -> KeypadButton("⌫", contentDescription = "Delete") { onDelete() }
                        else -> KeypadButton(label, contentDescription = "Digit $label") { onNumber(label) }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFF0C1318))
            .clickable { onClick() }
            .semantics { contentDescription?.let { this.contentDescription = it } }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}

