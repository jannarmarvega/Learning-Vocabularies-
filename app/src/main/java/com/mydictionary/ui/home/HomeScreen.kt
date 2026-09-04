package com.mydictionary.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mydictionary.data.model.Word
import com.mydictionary.ui.components.rememberSpeechManager

@Composable
fun HomeScreen(
    onWordClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val wordOfTheDay by viewModel.wordOfTheDay.collectAsStateWithLifecycle()
    val surprise by viewModel.surpriseWord.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Word of the Day",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            wordOfTheDay?.let {
                WordCard(
                    word = it,
                    onClick = { onWordClick(it.word) },
                    onCategoryClick = onCategoryClick
                )
            } ?: Text(
                text = "Loading your daily word…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Random pick",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = viewModel::shuffle) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shuffle")
                }
            }
            surprise?.let {
                WordCard(
                    word = it,
                    onClick = { onWordClick(it.word) },
                    onCategoryClick = onCategoryClick,
                    highlighted = false
                )
            }
        }
    }
}

@Composable
private fun WordCard(
    word: Word,
    onClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    highlighted: Boolean = true
) {
    val speech = rememberSpeechManager()
    val container = if (highlighted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val onContainer = if (highlighted) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.headlineMedium,
                    color = onContainer,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalIconButton(onClick = { speech.speak(word.word) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Play pronunciation of ${word.word}"
                    )
                }
            }
            if (word.partOfSpeech.isNotEmpty()) {
                Text(
                    text = word.partOfSpeech,
                    style = MaterialTheme.typography.labelMedium,
                    color = onContainer
                )
            }
            if (word.tagalogWord.isNotEmpty()) {
                Text(
                    text = word.tagalogWord,
                    style = MaterialTheme.typography.titleMedium,
                    color = onContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = word.definition,
                style = MaterialTheme.typography.bodyLarge,
                color = onContainer
            )
            if (word.example.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "“${word.example}”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer
                )
            }
            if (word.tagalogDefinition.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = word.tagalogDefinition,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer
                )
            }
            if (word.category.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AssistChip(
                    onClick = { onCategoryClick(word.category) },
                    label = { Text(word.category) }
                )
            }
        }
    }
}
