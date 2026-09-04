package com.mydictionary

import android.app.Application
import com.mydictionary.data.DictionaryRepository
import com.mydictionary.data.db.AppDatabase
import com.mydictionary.speech.SpeechManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyDictionaryApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy {
        DictionaryRepository(
            database.wordDao(),
            database.favoriteDao(),
            database.learningDao(),
            database.settingsDao()
        )
    }

    val speech by lazy { SpeechManager(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { speech.setAccent(repository.getSpeechAccent()) }
    }

    override fun onTerminate() {
        speech.shutdown()
        super.onTerminate()
    }
}
