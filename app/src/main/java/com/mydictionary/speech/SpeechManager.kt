package com.mydictionary.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Speaks dictionary entries aloud so the pronunciation can be heard.
 *
 * A single [TextToSpeech] engine is shared by the whole app; requests made before the
 * engine finishes starting up are held in [pending] and spoken as soon as it is ready.
 */
class SpeechManager(context: Context) {

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private var accent: Locale = Locale.UK
    private var ready = false
    private var pending: String? = null

    private val engine = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            applyAccent(accent)
            pending?.let { text ->
                pending = null
                speak(text)
            }
        }
    }

    init {
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Kept for API levels below 21", ReplaceWith(""))
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
            }
        })
    }

    /** Switches between "en-GB" and "en-US" pronunciation. */
    fun setAccent(languageTag: String) {
        val locale = when (languageTag) {
            ACCENT_AMERICAN -> Locale.US
            else -> Locale.UK
        }
        accent = locale
        if (ready) applyAccent(locale)
    }

    fun speak(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        if (!ready) {
            pending = trimmed
            return
        }
        engine.speak(trimmed, TextToSpeech.QUEUE_FLUSH, null, trimmed.hashCode().toString())
    }

    fun stop() {
        if (ready) engine.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        pending = null
        ready = false
        engine.shutdown()
    }

    private fun applyAccent(locale: Locale) {
        val result = engine.setLanguage(locale)
        val missing = result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED
        if (missing) {
            val fallback = engine.setLanguage(Locale.ENGLISH)
            _isAvailable.value = fallback != TextToSpeech.LANG_MISSING_DATA &&
                fallback != TextToSpeech.LANG_NOT_SUPPORTED
        } else {
            _isAvailable.value = true
        }
    }

    companion object {
        const val ACCENT_BRITISH = "en-GB"
        const val ACCENT_AMERICAN = "en-US"
    }
}
