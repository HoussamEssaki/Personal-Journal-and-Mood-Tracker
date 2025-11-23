package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.EntryFilter
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.model.Tag
import com.personaljournal.domain.usecase.journal.SearchEntriesUseCase
import com.personaljournal.domain.usecase.mood.GetAvailableMoodsUseCase
import com.personaljournal.domain.usecase.tag.GetAvailableTagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val moods: List<Mood> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedMoods: Set<MoodLevel> = emptySet(),
    val selectedTags: Set<String> = emptySet(),
    val includeMediaOnly: Boolean = false,
    val results: List<JournalEntry> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchEntriesUseCase: SearchEntriesUseCase,
    private val getAvailableTagsUseCase: GetAvailableTagsUseCase,
    private val getAvailableMoodsUseCase: GetAvailableMoodsUseCase
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val selectedMoodsFlow = MutableStateFlow(emptySet<MoodLevel>())
    private val selectedTagsFlow = MutableStateFlow(emptySet<String>())
    private val mediaOnlyFlow = MutableStateFlow(false)

    private val moodsFlow = MutableStateFlow<List<Mood>>(emptyList())
    private val tagsFlow = MutableStateFlow<List<Tag>>(emptyList())

    private val filterFlow = combine(
        queryFlow,
        selectedMoodsFlow,
        selectedTagsFlow,
        mediaOnlyFlow
    ) { query, moods, tags, mediaOnly ->
        EntryFilter(
            query = query.takeIf { it.isNotBlank() },
            moodLevels = moods,
            tagLabels = tags,
            hasMedia = if (mediaOnly) true else null
        )
    }

    private val resultsFlow = filterFlow.flatMapLatest { filter ->
        searchEntriesUseCase(filter)
    }

    private val baseState = combine(
        resultsFlow,
        queryFlow,
        moodsFlow,
        tagsFlow
    ) { results, query, moods, tags ->
        SearchBase(results, query, moods, tags)
    }

    val state: StateFlow<SearchUiState> = combine(
        baseState,
        selectedMoodsFlow,
        selectedTagsFlow,
        mediaOnlyFlow
    ) { base, selectedMoods, selectedTags, mediaOnly ->
        SearchUiState(
            query = base.query,
            moods = base.moods,
            tags = base.tags,
            selectedMoods = selectedMoods,
            selectedTags = selectedTags,
            includeMediaOnly = mediaOnly,
            results = base.results
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchUiState())

    init {
        viewModelScope.launch {
            moodsFlow.value = getAvailableMoodsUseCase()
            tagsFlow.value = getAvailableTagsUseCase()
        }
    }

    fun updateQuery(query: String) {
        queryFlow.value = query
    }

    fun toggleMood(mood: MoodLevel) {
        selectedMoodsFlow.value =
            selectedMoodsFlow.value.toMutableSet().also { set ->
                if (!set.add(mood)) set.remove(mood)
            }
    }

    fun toggleTag(tag: String) {
        selectedTagsFlow.value =
            selectedTagsFlow.value.toMutableSet().also { set ->
                if (!set.add(tag)) set.remove(tag)
            }
    }

    fun toggleMediaFilter() {
        mediaOnlyFlow.value = !mediaOnlyFlow.value
    }

    fun clearFilters() {
        selectedMoodsFlow.value = emptySet()
        selectedTagsFlow.value = emptySet()
        mediaOnlyFlow.value = false
    }
}

private data class SearchBase(
    val results: List<JournalEntry>,
    val query: String,
    val moods: List<Mood>,
    val tags: List<Tag>
)
