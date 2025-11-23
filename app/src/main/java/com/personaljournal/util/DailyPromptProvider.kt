package com.personaljournal.util

import com.personaljournal.domain.model.Prompt
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DailyPromptProvider @Inject constructor() {
    private val prompts = mapOf(
        "en" to listOf(
            Prompt("gratitude", "3 things I'm grateful for", "List three wins from today."),
            Prompt("growth", "What challenged me?", "Reflect on a recent obstacle and your response."),
            Prompt("mindful", "Mindful moment", "Describe a calm or mindful moment you noticed.")
        ),
        "fr" to listOf(
            Prompt("gratitude", "3 gratitudes", "Écris trois choses positives d'aujourd'hui."),
            Prompt("progres", "Quel défi ?", "Décris un obstacle récent et ta réaction."),
            Prompt("calme", "Instant de calme", "Note un moment paisible de la journée.")
        )
    )

    fun promptFor(locale: String): Prompt {
        val key = locale.ifBlank { Locale.getDefault().language }
        val list = prompts[key] ?: prompts["en"].orEmpty()
        return list[Random.nextInt(list.size)]
    }
}
