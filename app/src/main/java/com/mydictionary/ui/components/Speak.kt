package com.mydictionary.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mydictionary.MyDictionaryApplication
import com.mydictionary.speech.SpeechManager

@Composable
fun rememberSpeechManager(): SpeechManager {
    val context = LocalContext.current
    return remember(context) {
        (context.applicationContext as MyDictionaryApplication).speech
    }
}

/** A speaker button that reads [text] aloud in the currently selected accent. */
@Composable
fun SpeakButton(
    text: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val speech = rememberSpeechManager()
    IconButton(onClick = { speech.speak(text) }, modifier = modifier) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Play pronunciation of $text",
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
