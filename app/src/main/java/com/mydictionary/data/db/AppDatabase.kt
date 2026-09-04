package com.mydictionary.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mydictionary.data.model.Favorite
import com.mydictionary.data.model.Learning
import com.mydictionary.data.model.Settings
import com.mydictionary.data.model.Word

@Database(
    entities = [Word::class, Favorite::class, Learning::class, Settings::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun learningDao(): LearningDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dictionary.db"
                )
                    .createFromAsset("databases/dictionary.db")
                    // The word list ships inside the asset, so an upgrade copies the
                    // new asset rather than migrating the old rows in place.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
