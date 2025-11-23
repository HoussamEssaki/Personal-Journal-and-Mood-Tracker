@file:OptIn(ExperimentalFoundationApi::class)

package com.personaljournal.presentation.screens.editor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.personaljournal.BuildConfig
import com.personaljournal.domain.model.AttachmentType
import com.personaljournal.domain.model.MediaAttachment
import com.personaljournal.domain.model.QuickCaptureTarget
import com.personaljournal.presentation.ui.components.MoodSelector
import com.personaljournal.presentation.ui.components.RichTextEditor
import com.personaljournal.presentation.viewmodel.EditorUiState
import com.personaljournal.presentation.viewmodel.EditorViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorRoute(
    entryId: Long?,
    onSaved: () -> Unit,
    onOpenMoodDetails: () -> Unit,
    captureTarget: QuickCaptureTarget? = null,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(entryId) {
        if (entryId != null) viewModel.loadEntry(entryId)
    }
    LaunchedEffect(captureTarget) {
        viewModel.applyQuickCaptureTemplate(captureTarget)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    var showMediaDialog by rememberSaveable { mutableStateOf(false) }
    var requestedPermissionsFor by rememberSaveable { mutableStateOf<MediaAction?>(null) }
    var autoAudioRequested by rememberSaveable { mutableStateOf(false) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
            viewModel.addMedia(stream.toByteArray(), AttachmentType.PHOTO)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    withContext(Dispatchers.Main) {
                        viewModel.addMedia(bytes, AttachmentType.PHOTO)
                    }
                }
            }
        }
    }
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@rememberLauncherForActivityResult
            scope.launch(Dispatchers.IO) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    withContext(Dispatchers.Main) {
                        viewModel.addMedia(bytes, AttachmentType.AUDIO)
                    }
                }
            }
        }
    }

    fun ensurePermissions(action: MediaAction): Boolean {
        val required = when (action) {
            MediaAction.PHOTO -> listOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
            MediaAction.GALLERY -> listOf(Manifest.permission.READ_MEDIA_IMAGES)
            MediaAction.AUDIO -> listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO)
        }
        val missing = required.filter {
            ContextCompat.checkSelfPermission(context, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        return if (missing.isEmpty()) {
            true
        } else {
            requestedPermissionsFor = action
            permissionLauncher.launch(missing.toTypedArray())
            false
        }
    }

    fun launchMediaAction(
        action: MediaAction,
        camera: ActivityResultLauncher<Void?>,
        gallery: ActivityResultLauncher<String>,
        audio: ActivityResultLauncher<Intent>
    ) {
        if (!ensurePermissions(action)) return
        when (action) {
            MediaAction.PHOTO -> camera.launch(null)
            MediaAction.GALLERY -> gallery.launch("image/*")
            MediaAction.AUDIO -> {
                val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                if (intent.resolveActivity(context.packageManager) != null) {
                    audio.launch(intent)
                } else {
                    Toast.makeText(context, "No audio recorder available on this device", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        val target = requestedPermissionsFor
        requestedPermissionsFor = null
        if (granted && target != null) {
            launchMediaAction(target, cameraLauncher, galleryLauncher, audioLauncher)
        } else if (!granted) {
            Toast.makeText(context, "Permission required to attach media", Toast.LENGTH_SHORT).show()
        }
    }

    val shareScope = rememberCoroutineScope()
    val shareAttachment: (MediaAttachment) -> Unit = { attachment ->
        shareScope.launch {
            runCatching {
                val file = viewModel.getAttachmentFile(attachment)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )
                val mime = when (attachment.type) {
                    AttachmentType.PHOTO -> "image/jpeg"
                    AttachmentType.AUDIO -> "audio/*"
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share attachment"))
            }.onFailure {
                Toast.makeText(context, "Unable to share attachment", Toast.LENGTH_SHORT).show()
            }
        }
    }
    LaunchedEffect(captureTarget, state.entryId) {
        if (captureTarget != QuickCaptureTarget.AUDIO || state.entryId != null) {
            autoAudioRequested = false
        }
    }
    LaunchedEffect(captureTarget, state.entryId, state.media) {
        if (
            captureTarget == QuickCaptureTarget.AUDIO &&
            state.entryId == null &&
            !autoAudioRequested &&
            state.media.none { it.type == AttachmentType.AUDIO }
        ) {
            autoAudioRequested = true
            runCatching {
                if (ensurePermissions(MediaAction.AUDIO)) {
                    val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        audioLauncher.launch(intent)
                    } else {
                        Toast.makeText(context, "No audio recorder available on this device", Toast.LENGTH_SHORT).show()
                    }
                }
            }.onFailure {
                autoAudioRequested = false
            }
        }
    }
    EditorScreen(
        state = state,
        onTitleChanged = viewModel::updateTitle,
        onContentChanged = viewModel::updateContent,
        onRichTextChanged = viewModel::updateRichText,
        onMoodSelected = viewModel::selectMood,
        onAddTag = viewModel::addTag,
        onTagDraftChanged = viewModel::updateTagDraft,
        onRemoveTag = viewModel::removeTag,
        onEditTag = viewModel::updateTag,
        onAddMedia = { showMediaDialog = true },
        onSave = { viewModel.save(onSaved) },
        onOpenMoodDetails = onOpenMoodDetails,
        resolveAttachmentFile = viewModel::getAttachmentFile,
        onShareAttachment = shareAttachment,
        onRemoveAttachment = { viewModel.removeMedia(it) }
    )

    if (showMediaDialog) {
        AlertDialog(
            onDismissRequest = { showMediaDialog = false },
            title = { Text("Add media") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Take photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                launchMediaAction(
                                    MediaAction.PHOTO,
                                    cameraLauncher,
                                    galleryLauncher,
                                    audioLauncher
                                )
                                showMediaDialog = false
                            }
                    )
                    Text(
                        text = "Choose from gallery",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                launchMediaAction(
                                    MediaAction.GALLERY,
                                    cameraLauncher,
                                    galleryLauncher,
                                    audioLauncher
                                )
                                showMediaDialog = false
                            }
                    )
                    Text(
                        text = "Record audio",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                launchMediaAction(
                                    MediaAction.AUDIO,
                                    cameraLauncher,
                                    galleryLauncher,
                                    audioLauncher
                                )
                                showMediaDialog = false
                            }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showMediaDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private enum class MediaAction { PHOTO, GALLERY, AUDIO }

@Composable
fun EditorScreen(
    state: EditorUiState,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onRichTextChanged: (List<com.personaljournal.domain.model.RichTextBlock>) -> Unit,
    onMoodSelected: (com.personaljournal.domain.model.Mood) -> Unit,
    onAddTag: (String) -> Unit,
    onTagDraftChanged: (String) -> Unit,
    onRemoveTag: (com.personaljournal.domain.model.Tag) -> Unit,
    onEditTag: (com.personaljournal.domain.model.Tag, String) -> Unit,
    onAddMedia: () -> Unit,
    onSave: () -> Unit,
    onOpenMoodDetails: () -> Unit,
    resolveAttachmentFile: suspend (MediaAttachment) -> java.io.File,
    onShareAttachment: (MediaAttachment) -> Unit,
    onRemoveAttachment: (MediaAttachment) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val wordCount = state.content.split(" ").filter { it.isNotBlank() }.size
        Text("October 26, 2023", style = MaterialTheme.typography.titleLarge)
        Text("$wordCount words", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = state.title,
                    onValueChange = onTitleChanged,
                    placeholder = { Text("Entry title") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.content,
                    onValueChange = onContentChanged,
                    placeholder = { Text("Start writing about your day...") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 8.dp),
                    minLines = 6
                )
            }
        }
        RichTextEditor(
            blocks = state.richText.blocks,
            onChange = onRichTextChanged
        )
        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.titleMedium
        )
        MoodSelector(
            moods = state.moods,
            selected = state.selectedMood,
            onMoodSelected = onMoodSelected,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Need more nuance?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onOpenMoodDetails) {
                Text("Detailed selector")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalButton(onClick = onAddMedia) {
                Icon(
                    Icons.Outlined.AttachFile,
                    contentDescription = "Attach photo or audio"
                )
                Text(text = "Add media", modifier = Modifier.padding(start = 8.dp))
            }
            Button(onClick = onSave) {
                Text("Save")
            }
        }
        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.tagDraft,
                onValueChange = onTagDraftChanged,
                modifier = Modifier.weight(1f),
                label = { Text("#tag") }
            )
            IconButton(onClick = {
                if (state.tagDraft.isNotBlank()) {
                    onAddTag(state.tagDraft)
                    onTagDraftChanged("")
                }
            }) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Add tag"
                )
            }
        }
        var tagToEdit by rememberSaveable { mutableStateOf<com.personaljournal.domain.model.Tag?>(null) }
        var editTagText by rememberSaveable { mutableStateOf("") }
        if (state.tags.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.tags, key = { it.id }) { tag ->
                    Surface(
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = { onRemoveTag(tag) },
                                onLongClick = {
                                    tagToEdit = tag
                                    editTagText = tag.label
                                }
                            )
                            .semantics { contentDescription = "Remove or edit tag ${tag.label}" }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#${tag.label}")
                            Text("x", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        tagToEdit?.let { tag ->
            AlertDialog(
                onDismissRequest = { tagToEdit = null },
                title = { Text("Edit tag") },
                text = {
                    OutlinedTextField(
                        value = editTagText,
                        onValueChange = { editTagText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tag label") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onEditTag(tag, editTagText)
                        tagToEdit = null
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { tagToEdit = null }) { Text("Cancel") }
                }
            )
        }
        if (state.media.isNotEmpty()) {
            Text(
                text = "Attachments",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.media, key = { it.id }) { media ->
                    when (media.type) {
                        AttachmentType.AUDIO -> AudioAttachmentCard(
                            attachment = media,
                            resolveAttachmentFile = resolveAttachmentFile,
                            onShare = { onShareAttachment(media) },
                            onRemove = { onRemoveAttachment(media) }
                        )
                        AttachmentType.PHOTO -> PhotoAttachmentCard(
                            attachment = media,
                            onShare = { onShareAttachment(media) },
                            onRemove = { onRemoveAttachment(media) },
                            resolveAttachmentFile = resolveAttachmentFile
                        )
                    }
                }
            }
        }
        state.prompt?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun PhotoAttachmentCard(
    attachment: MediaAttachment,
    onShare: () -> Unit,
    onRemove: () -> Unit,
    resolveAttachmentFile: suspend (MediaAttachment) -> java.io.File
) {
    var previewPath by remember(attachment.id) { mutableStateOf<String?>(null) }
    var isLoading by remember(attachment.id) { mutableStateOf(true) }
    var loadError by remember(attachment.id) { mutableStateOf(false) }

    LaunchedEffect(attachment.id) {
        isLoading = true
        loadError = false
        runCatching {
            withContext(Dispatchers.IO) { resolveAttachmentFile(attachment).absolutePath }
        }.onSuccess {
            previewPath = it
            isLoading = false
        }.onFailure {
            isLoading = false
            loadError = true
        }
    }

    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Photo", style = MaterialTheme.typography.titleSmall)
                Row {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share photo")
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Remove photo")
                    }
                }
            }
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                }
                loadError -> {
                    Text(
                        text = "Preview unavailable",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                else -> {
                    previewPath?.let { path ->
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Photo attachment",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }
            Text(
                text = attachment.filePath.substringAfterLast('/'),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AudioAttachmentCard(
    attachment: MediaAttachment,
    resolveAttachmentFile: suspend (MediaAttachment) -> java.io.File,
    onShare: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var cachePath by remember { mutableStateOf<String?>(null) }
    val mediaPlayer = remember { MediaPlayer() }
    DisposableEffect(attachment.id) {
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
        }
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            mediaPlayer.release()
        }
    }

    fun requestPlayback() {
        scope.launch {
            isLoading = true
            val path = cachePath ?: runCatching {
                withContext(Dispatchers.IO) {
                    resolveAttachmentFile(attachment).absolutePath
                }
            }.onFailure {
                isLoading = false
                Toast.makeText(context, "Unable to load audio", Toast.LENGTH_SHORT).show()
            }.getOrNull() ?: return@launch
            cachePath = path
            val prepared = runCatching {
                withContext(Dispatchers.IO) {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(path)
                    mediaPlayer.prepare()
                }
            }
            if (prepared.isFailure) {
                isLoading = false
                Toast.makeText(context, "Unable to play audio", Toast.LENGTH_SHORT).show()
                return@launch
            }
            mediaPlayer.start()
            isPlaying = true
            isLoading = false
        }
    }

    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Voice note", style = MaterialTheme.typography.titleSmall)
                Row {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share audio")
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Remove audio")
                    }
                }
            }
            Text(
                text = cachePath?.substringAfterLast('/') ?: "Tap play to preview",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                } else {
                    Button(
                        onClick = {
                            if (isPlaying) {
                                mediaPlayer.stop()
                                isPlaying = false
                            } else {
                                requestPlayback()
                            }
                        }
                    ) {
                        Text(if (isPlaying) "Stop" else "Play")
                    }
                }
                attachment.durationSeconds?.let { seconds ->
                    Text(
                        text = "${seconds}s",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
