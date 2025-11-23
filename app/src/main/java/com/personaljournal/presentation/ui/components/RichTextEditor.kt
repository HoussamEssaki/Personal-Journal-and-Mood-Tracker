package com.personaljournal.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personaljournal.domain.model.RichTextBlock

@Composable
fun RichTextEditor(
    blocks: List<RichTextBlock>,
    onChange: (List<RichTextBlock>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        BasicTextField(
            value = blocks.joinToString("\n") { it.text },
            onValueChange = { text ->
                val newBlocks = text.lines().map { RichTextBlock(it) }
                onChange(newBlocks)
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { toggleFormatting(blocks, onChange) }) {
                Icon(
                    imageVector = Icons.Outlined.FormatBold,
                    contentDescription = "Bold"
                )
            }
            IconButton(onClick = { toggleFormatting(blocks, onChange, italic = true) }) {
                Icon(
                    imageVector = Icons.Outlined.FormatItalic,
                    contentDescription = "Italic"
                )
            }
            IconButton(onClick = { toggleFormatting(blocks, onChange, bullet = true) }) {
                Icon(
                    imageVector = Icons.Outlined.FormatListBulleted,
                    contentDescription = "Bulleted list"
                )
            }
        }
    }
}

private fun toggleFormatting(
    blocks: List<RichTextBlock>,
    onChange: (List<RichTextBlock>) -> Unit,
    italic: Boolean = false,
    bullet: Boolean = false
) {
    if (blocks.isEmpty()) return
    val updated = blocks.toMutableList()
    val lastIndex = updated.lastIndex
    val block = updated[lastIndex]
    updated[lastIndex] = block.copy(
        bold = if (!italic && !bullet) !block.bold else block.bold,
        italic = if (italic) !block.italic else block.italic,
        bullet = if (bullet) !block.bullet else block.bullet
    )
    onChange(updated)
}
