package com.personaljournal.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SplashOnboardingScreen(
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Start Your Journey",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF1F2A37),
            modifier = Modifier.padding(top = 64.dp)
        )
        Text(
            text = "Begin your path to mindfulness and self-discovery with us. A new, calmer you is just a few taps away.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF6B7280)
        )
        DotsIndicator(activeIndex = 2, total = 4)
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape
        ) {
            Text("Get Started")
        }
    }
}

@Composable
private fun DotsIndicator(activeIndex: Int, total: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(total) { index ->
            Surface(
                modifier = Modifier
                    .size(if (index == activeIndex) 16.dp else 8.dp)
                    .clip(CircleShape),
                color = if (index == activeIndex) Color(0xFF3B82F6) else Color(0xFFE5E7EB)
            ) {}
        }
    }
}
