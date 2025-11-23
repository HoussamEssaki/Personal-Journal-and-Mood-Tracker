package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.AttachmentType
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.MediaAttachment
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.model.PersonalPreferences
import com.personaljournal.domain.model.QuickCaptureTarget
import com.personaljournal.domain.model.RichTextBlock
import com.personaljournal.domain.model.RichTextContent
import com.personaljournal.domain.model.Tag
import com.personaljournal.domain.usecase.journal.CreateJournalEntryUseCase
import com.personaljournal.domain.usecase.journal.GetJournalEntryUseCase
import com.personaljournal.domain.usecase.journal.UpdateJournalEntryUseCase
import com.personaljournal.domain.usecase.media.DeleteMediaAttachmentUseCase
import com.personaljournal.domain.usecase.media.GetMediaFileUseCase
import com.personaljournal.domain.usecase.media.SaveMediaAttachmentUseCase
import com.personaljournal.domain.usecase.mood.ObserveMoodsUseCase
import com.personaljournal.domain.usecase.prompts.GetDailyPromptUseCase
import com.personaljournal.domain.usecase.settings.PersonalizationSettingsUseCase
import com.personaljournal.domain.usecase.tag.UpsertTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class EditorUiState(
    val entryId: Long? = null,
    val title: String = "",
    val content: String = "",
    val richText: RichTextContent = RichTextContent(),
    val moods: List<Mood> = emptyList(),
    val selectedMood: Mood? = null,
    val tags: List<Tag> = emptyList(),
    val tagDraft: String = "",
    val media: List<MediaAttachment> = emptyList(),
    val prompt: String? = null,
    val isSaving: Boolean = false,
    val emotions: List<String> = emptyList(),
    val factors: List<String> = emptyList()
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
    private val updateJournalEntryUseCase: UpdateJournalEntryUseCase,
    private val getJournalEntryUseCase: GetJournalEntryUseCase,
    observeMoodsUseCase: ObserveMoodsUseCase,
    private val saveMediaAttachmentUseCase: SaveMediaAttachmentUseCase,
    private val deleteMediaAttachmentUseCase: DeleteMediaAttachmentUseCase,
    private val upsertTagUseCase: UpsertTagUseCase,
    getDailyPromptUseCase: GetDailyPromptUseCase,
    private val getMediaFileUseCase: GetMediaFileUseCase,
    private val personalizationSettingsUseCase: PersonalizationSettingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state

    val moods: StateFlow<List<Mood>> = observeMoodsUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val personalPreferences = MutableStateFlow(PersonalPreferences())
    private var cachedPrompt: String? = null

    init {
        viewModelScope.launch {
            personalizationSettingsUseCase.personal().collect { prefs ->
                personalPreferences.value = prefs
                _state.value = _state.value.copy(
                    prompt = if (prefs.showPrompts) cachedPrompt else null
                )
            }
        }
        viewModelScope.launch {
            getDailyPromptUseCase("en").collect { prompt ->
                cachedPrompt = prompt.title
                if (personalPreferences.value.showPrompts) {
                    _state.value = _state.value.copy(prompt = prompt.title)
                }
            }
        }
        viewModelScope.launch {
            moods.collect { moodList ->
                _state.value = _state.value.copy(moods = moodList)
                if (_state.value.selectedMood == null && moodList.isNotEmpty()) {
                    _state.value = _state.value.copy(selectedMood = moodList.first())
                }
            }
        }
    }

    fun loadEntry(id: Long) {
        viewModelScope.launch {
            val entry = getJournalEntryUseCase(id) ?: return@launch
            _state.value = _state.value.copy(
                entryId = entry.id,
                title = entry.title,
                content = entry.content,
                richText = entry.richText,
                selectedMood = entry.mood,
                tags = entry.tags,
                media = entry.media,
                emotions = entry.secondaryEmotions,
                factors = entry.factors.map { it.label }
            )
        }
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _state.value = _state.value.copy(content = content)
    }

    fun updateRichText(blocks: List<RichTextBlock>) {
        _state.value = _state.value.copy(richText = RichTextContent(blocks))
    }

    fun selectMood(mood: Mood) {
        _state.value = _state.value.copy(selectedMood = mood)
    }

    fun toggleEmotion(emotion: String) {
        val emotions = _state.value.emotions.toMutableList()
        if (emotions.contains(emotion)) emotions.remove(emotion) else emotions.add(emotion)
        _state.value = _state.value.copy(emotions = emotions)
    }

    fun toggleFactor(factor: String) {
        val factors = _state.value.factors.toMutableList()
        if (factors.contains(factor)) factors.remove(factor) else factors.add(factor)
        _state.value = _state.value.copy(factors = factors)
    }

    fun addTag(label: String) {
        viewModelScope.launch {
            val tag = Tag(label = label)
            val id = upsertTagUseCase(tag)
            val newTag = tag.copy(id = id)
            if (_state.value.tags.none { it.label.equals(label, ignoreCase = true) }) {
                _state.value = _state.value.copy(tags = _state.value.tags + newTag)
            }
        }
    }

    fun removeTag(tag: Tag) {
        _state.value = _state.value.copy(tags = _state.value.tags.filterNot { it.id == tag.id && it.label == tag.label })
    }

    fun updateTag(tag: Tag, newLabel: String) {
        viewModelScope.launch {
            val trimmed = newLabel.trim()
            if (trimmed.isEmpty()) return@launch
            val updated = tag.copy(label = trimmed)
            val id = upsertTagUseCase(updated)
            _state.value = _state.value.copy(
                tags = _state.value.tags.map {
                    if (it.id == tag.id) updated.copy(id = id) else it
                }
            )
        }
    }

    fun updateTagDraft(value: String) {
        _state.value = _state.value.copy(tagDraft = value)
    }

    fun addMedia(bytes: ByteArray, type: AttachmentType) {
        viewModelScope.launch {
            val attachment = saveMediaAttachmentUseCase(bytes, type)
            _state.value = _state.value.copy(media = _state.value.media + attachment)
        }
    }

    fun removeMedia(attachment: MediaAttachment) {
        viewModelScope.launch {
            deleteMediaAttachmentUseCase(attachment.id)
            _state.value = _state.value.copy(media = _state.value.media - attachment)
        }
    }

    fun applyQuickCaptureTemplate(target: QuickCaptureTarget?) {
        if (target == null) return
        val current = _state.value
        if (current.entryId != null) return
        if (current.title.isNotBlank() || current.content.isNotBlank()) return
        val template = when (target) {
            QuickCaptureTarget.REFLECTION -> Template(
                title = "Daily reflection",
                body = "Right now I feel...\nI want to remember...\nTomorrow I'll..."
            )
            QuickCaptureTarget.GRATITUDE -> Template(
                title = "Gratitude log",
                body = "- I'm grateful for...\n- A small win today...\n- Someone who helped me..."
            )
            QuickCaptureTarget.AUDIO -> Template(
                title = "Voice note",
                body = ""
            )
        }
        _state.value = current.copy(
            title = template.title ?: current.title,
            content = template.body ?: current.content
        )
    }

    suspend fun getAttachmentFile(attachment: MediaAttachment) =
        getMediaFileUseCase(attachment)

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val mood = _state.value.selectedMood ?: return@launch
            val tags = _state.value.tags.toMutableList()
            if (personalPreferences.value.autoTagFromMood &&
                tags.none { it.label.equals(mood.label, ignoreCase = true) }
            ) {
                val moodTag = Tag(label = mood.label)
                val tagId = upsertTagUseCase(moodTag)
                tags.add(moodTag.copy(id = tagId))
            }
            val entry = JournalEntry(
                id = _state.value.entryId ?: 0L,
                title = _state.value.title,
                content = _state.value.content,
                richText = _state.value.richText,
                createdAt = Clock.System.now(),
                mood = mood,
                tags = tags,
                media = _state.value.media,
                secondaryEmotions = _state.value.emotions,
                factors = _state.value.factors.map {
                    com.personaljournal.domain.model.MoodFactor(it, it, com.personaljournal.domain.model.FactorCategory.HABIT)
                }
            )
            if (entry.id == 0L) {
                createJournalEntryUseCase(entry)
            } else {
                updateJournalEntryUseCase(entry)
            }
            onSaved()
        }
    }
}

private data class Template(
    val title: String?,
    val body: String?
)
