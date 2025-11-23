package com.personaljournal.presentation.screens.export

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.BuildConfig
import com.personaljournal.domain.model.ExportFormat
import com.personaljournal.presentation.viewmodel.AnalyticsUiState
import com.personaljournal.presentation.viewmodel.AnalyticsViewModel
import com.personaljournal.presentation.viewmodel.ExportCenterEvent
import com.personaljournal.presentation.viewmodel.ExportCenterUiState
import com.personaljournal.presentation.viewmodel.ExportCenterViewModel
import com.personaljournal.presentation.viewmodel.ShareTargetUi
import java.io.File

@Composable
fun ExportShareRoute(
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    centerViewModel: ExportCenterViewModel = hiltViewModel()
) {
    val analyticsState by analyticsViewModel.state.collectAsStateWithLifecycle()
    val centerState by centerViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingShareTarget by rememberSaveable { mutableStateOf<ShareTargetUi?>(null) }
    var pendingSave by rememberSaveable { mutableStateOf<Boolean>(false) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            centerViewModel.backupTo(uri, uri.lastPathSegment)
        } else {
            centerViewModel.postStatus("Backup cancelled")
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            centerViewModel.restoreFrom(uri, uri.lastPathSegment)
        } else {
            centerViewModel.postStatus("Restore cancelled")
        }
    }
    val pdfSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            analyticsState.exportFile?.let { file ->
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { it.copyTo(out) }
                }
                centerViewModel.postStatus("Export saved to device.")
            }
        }
        pendingSave = false
        analyticsViewModel.clearExportStatus()
    }
    val csvSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            analyticsState.exportFile?.let { file ->
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { it.copyTo(out) }
                }
                centerViewModel.postStatus("Export saved to device.")
            }
        }
        pendingSave = false
        analyticsViewModel.clearExportStatus()
    }
    val jsonSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            analyticsState.exportFile?.let { file ->
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { it.copyTo(out) }
                }
                centerViewModel.postStatus("Export saved to device.")
            }
        }
        pendingSave = false
        analyticsViewModel.clearExportStatus()
    }

    LaunchedEffect(centerViewModel) {
        centerViewModel.events.collect { event ->
            when (event) {
                is ExportCenterEvent.RequestBackupDestination -> backupLauncher.launch(event.suggestedName)
                ExportCenterEvent.RequestRestoreSource -> restoreLauncher.launch(arrayOf("*/*"))
            }
        }
    }

    LaunchedEffect(analyticsState.exportFile, pendingShareTarget, pendingSave, centerState.selectedFormat) {
        val file = analyticsState.exportFile
        if (file != null) {
            pendingShareTarget?.let { target ->
                shareExportedFile(
                    context = context,
                    file = file,
                    format = centerState.selectedFormat,
                    target = target,
                    onStatus = centerViewModel::postStatus
                )
                analyticsViewModel.clearExportStatus()
                pendingShareTarget = null
            }
            if (pendingSave) {
                when (centerState.selectedFormat) {
                    ExportFormat.PDF -> pdfSaveLauncher.launch("journal-export.pdf")
                    ExportFormat.CSV -> csvSaveLauncher.launch("journal-export.csv")
                    ExportFormat.JSON -> jsonSaveLauncher.launch("journal-export.json")
                }
            }
        }
    }

    ExportShareScreen(
        analyticsState = analyticsState,
        centerState = centerState,
        onSelectTab = centerViewModel::selectTab,
        onSelectFormat = centerViewModel::selectFormat,
        onToggleIncludeMedia = centerViewModel::setIncludeMedia,
        onExport = {
            analyticsViewModel.export(centerState.selectedFormat, includeMedia = centerState.includeMedia)
        },
        onShareTarget = { target ->
            pendingShareTarget = target
            if (analyticsState.exportFile == null) {
                analyticsViewModel.export(centerState.selectedFormat, includeMedia = centerState.includeMedia)
            }
        },
        onSaveToDevice = {
            pendingSave = true
            if (analyticsState.exportFile == null) {
                analyticsViewModel.export(centerState.selectedFormat, includeMedia = centerState.includeMedia)
            }
        },
        onBackup = centerViewModel::requestBackupDestination,
        onRestore = centerViewModel::requestRestoreSource,
        onDismissStatus = centerViewModel::dismissStatusMessage
    )
}

@Composable
private fun ExportShareScreen(
    analyticsState: AnalyticsUiState,
    centerState: ExportCenterUiState,
    onSelectTab: (ExportTab) -> Unit,
    onSelectFormat: (ExportFormat) -> Unit,
    onToggleIncludeMedia: (Boolean) -> Unit,
    onExport: () -> Unit,
    onShareTarget: (ShareTargetUi) -> Unit,
    onSaveToDevice: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onDismissStatus: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Export & Backup Center", style = MaterialTheme.typography.headlineSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExportTab.values().forEach { tab ->
                OutlinedButton(
                    onClick = { onSelectTab(tab) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (centerState.selectedTab == tab) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(tab.label)
                }
            }
        }

        when (centerState.selectedTab) {
            ExportTab.Export -> ExportTabContent(
                selectedFormat = centerState.selectedFormat,
                includeMedia = centerState.includeMedia,
                isExporting = analyticsState.isExporting,
                onSelectFormat = onSelectFormat,
                onToggleIncludeMedia = onToggleIncludeMedia,
                onExport = onExport,
                onSaveToDevice = onSaveToDevice
            )
            ExportTab.Share -> ShareTabContent(
                selectedFormat = centerState.selectedFormat,
                includeMedia = centerState.includeMedia,
                shareTargets = centerState.shareTargets,
                isExporting = analyticsState.isExporting,
                onSelectFormat = onSelectFormat,
                onToggleIncludeMedia = onToggleIncludeMedia,
                onExportAndShare = onShareTarget
            )
            ExportTab.Backup -> BackupTabContent(
                state = centerState,
                onBackup = onBackup,
                onRestore = onRestore
            )
        }

        centerState.statusMessage?.let { message ->
            StatusBanner(text = message, onDismiss = onDismissStatus)
        }
    }
}

@Composable
private fun ExportTabContent(
    selectedFormat: ExportFormat,
    includeMedia: Boolean,
    isExporting: Boolean,
    onSelectFormat: (ExportFormat) -> Unit,
    onToggleIncludeMedia: (Boolean) -> Unit,
    onExport: () -> Unit,
    onSaveToDevice: () -> Unit
) {
        Text("Choose format", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormatOption.values().forEach { option ->
                OutlinedButton(
                    onClick = { option.format?.let(onSelectFormat) },
                    enabled = option.format != null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            option.format?.let {
                                contentDescription = "Select ${option.label} format${if (it == selectedFormat) " selected" else ""}"
                            }
                        },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (option.format == selectedFormat) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(option.label, textAlign = TextAlign.Center)
                }
            }
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Include media files", style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = includeMedia,
            onCheckedChange = onToggleIncludeMedia,
            modifier = Modifier.semantics {
                contentDescription = "Include media files ${if (includeMedia) "on" else "off"}"
            }
        )
    }
    Button(
        onClick = onExport,
        enabled = !isExporting,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Generate export ${selectedFormat.name.lowercase()}" }
    ) {
        Text(if (isExporting) "Exporting..." else "Generate export")
    }
    OutlinedButton(
        onClick = onSaveToDevice,
        enabled = !isExporting,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Save export ${selectedFormat.name.lowercase()} to device" }
    ) {
        Text("Save to device")
    }
}

@Composable
private fun ShareTabContent(
    selectedFormat: ExportFormat,
    includeMedia: Boolean,
    shareTargets: List<ShareTargetUi>,
    isExporting: Boolean,
    onSelectFormat: (ExportFormat) -> Unit,
    onToggleIncludeMedia: (Boolean) -> Unit,
    onExportAndShare: (ShareTargetUi) -> Unit
) {
    Text("Choose format", style = MaterialTheme.typography.titleMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FormatOption.values().forEach { option ->
            OutlinedButton(
                onClick = { option.format?.let(onSelectFormat) },
                enabled = option.format != null,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        option.format?.let {
                            contentDescription = "Select ${option.label} format for sharing${if (it == selectedFormat) " selected" else ""}"
                        }
                    },
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = if (option.format == selectedFormat) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Text(option.label, textAlign = TextAlign.Center)
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Include media files", style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = includeMedia,
            onCheckedChange = onToggleIncludeMedia,
            modifier = Modifier.semantics {
                contentDescription = "Include media files ${if (includeMedia) "on" else "off"}"
            }
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    shareTargets.forEach { target ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isExporting) { onExportAndShare(target) }
                    .semantics {
                        contentDescription = "Share ${selectedFormat.name.lowercase()} via ${target.title}"
                    }
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(target.title, fontWeight = FontWeight.SemiBold)
                Text(target.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isExporting) {
                    Text("Generating export...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun BackupTabContent(
    state: ExportCenterUiState,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Backup status", style = MaterialTheme.typography.titleMedium)
            Text("Last backup: ${state.backupStatus.lastBackup}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Location: ${state.backupStatus.location}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Last restore: ${state.backupStatus.lastRestore}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = onBackup,
                enabled = !state.isBackingUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Create backup ${if (state.isBackingUp) "in progress" else ""}" }
            ) {
                Text(if (state.isBackingUp) "Backing up..." else "Create backup")
            }
            OutlinedButton(
                onClick = onRestore,
                enabled = !state.isRestoring,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Restore from backup ${if (state.isRestoring) "in progress" else ""}" }
            ) {
                Text(if (state.isRestoring) "Restoring..." else "Restore from backup")
            }
        }
    }
}

@Composable
private fun StatusBanner(text: String, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .semantics { contentDescription = "Status: $text" },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(
                text = "Dismiss",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onDismiss() }
            )
        }
    }
}

private enum class FormatOption(val label: String, val format: ExportFormat?) {
    PDF("PDF", ExportFormat.PDF),
    JSON("JSON", ExportFormat.JSON),
    CSV("CSV", ExportFormat.CSV)
}

private fun shareExportedFile(
    context: Context,
    file: File,
    format: ExportFormat,
    target: ShareTargetUi,
    onStatus: (String) -> Unit
) {
    if (!file.exists()) {
        onStatus("Generate an export before sharing.")
        return
    }
    val mimeType = when (format) {
        ExportFormat.PDF -> "application/pdf"
        ExportFormat.CSV -> "text/csv"
        ExportFormat.JSON -> "application/json"
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        file
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_TEXT, "Shared from Personal Journal")
    }
    val chooser = Intent.createChooser(shareIntent, "Share via ${target.title}")
    try {
        context.startActivity(chooser)
        onStatus("Sharing via ${target.title}")
    } catch (e: ActivityNotFoundException) {
        onStatus("No compatible app available to share.")
    }
}
