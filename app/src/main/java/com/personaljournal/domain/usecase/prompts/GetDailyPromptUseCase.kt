package com.personaljournal.domain.usecase.prompts

import com.personaljournal.domain.model.Prompt
import com.personaljournal.util.DailyPromptProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetDailyPromptUseCase @Inject constructor(
    private val promptProvider: DailyPromptProvider
) {
    operator fun invoke(locale: String): Flow<Prompt> = flow {
        emit(promptProvider.promptFor(locale))
    }
}
