package com.personaljournal.presentation.screens.support

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.presentation.viewmodel.HelpCenterUiState
import com.personaljournal.presentation.viewmodel.HelpCenterViewModel

@Composable
fun SupportHelpRoute(
    viewModel: HelpCenterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SupportHelpScreen(
        state = state,
        onQueryChange = viewModel::updateQuery
    )
}

@Composable
fun SupportHelpScreen(
    state: HelpCenterUiState,
    onQueryChange: (String) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .background(Color(0xFFFFF8F3))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Help & Support", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search help articles...") }
        )
        CrisisBanner()
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@personaljournal.app")
                    putExtra(Intent.EXTRA_SUBJECT, "Support request")
                }
                runCatching { context.startActivity(intent) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Contact support")
        }
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.personaljournal.app/help"))
                runCatching { context.startActivity(intent) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open help center")
        }
        if (state.query.isNotBlank()) {
            SearchResults(state)
        }
        state.sections.forEach { section ->
            SectionCard(
                title = section.title,
                items = section.entries.map { it.title }
            )
        }
    }
}

@Composable
private fun CrisisBanner() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0xFFFFE1E1)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Need Immediate Support?", color = Color(0xFFB42318))
            Text(
                "If you're in crisis, please reach out. You are not alone. Help is available 24/7.",
                modifier = Modifier.padding(top = 4.dp),
                color = Color(0xFFB42318)
            )
            Button(
                onClick = {},
                modifier = Modifier.padding(top = 12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB42318)
                )
            ) {
                Text("View Crisis Hotlines")
            }
        }
    }
}

@Composable
private fun SearchResults(
    state: HelpCenterUiState
) {
    if (state.filteredEntries.isEmpty()) {
        Text(
            text = "No articles found for \"${state.query}\"",
            color = Color(0xFF9A6B73),
            fontWeight = FontWeight.SemiBold
        )
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Quick answers",
            style = MaterialTheme.typography.titleMedium
        )
        state.filteredEntries.forEach { entry ->
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(entry.title, fontWeight = FontWeight.SemiBold)
                    Text(
                        entry.description,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, items: List<String>) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            items.forEach {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
