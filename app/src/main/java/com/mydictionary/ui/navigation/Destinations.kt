package com.mydictionary.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val BROWSE = "browse"
    const val LEARNINGS = "learnings"
    const val FAVORITES = "favorites"
    const val ADD_LEARNING = "add_learning?word={word}&id={id}"
    const val WORD_DETAIL = "word/{word}"
    const val WORD_LIST = "words?category={category}&group={group}&pos={pos}"

    fun wordDetail(word: String) = "word/${Uri.encode(word)}"

    fun addLearning(word: String = "", id: Long = 0L) =
        "add_learning?word=${Uri.encode(word)}&id=$id"

    fun wordsByCategory(category: String) =
        "words?category=${Uri.encode(category)}&group=&pos="

    fun wordsByGroup(group: String) =
        "words?category=&group=${Uri.encode(group)}&pos="

    fun wordsByPartOfSpeech(partOfSpeech: String) =
        "words?category=&group=&pos=${Uri.encode(partOfSpeech)}"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.SEARCH, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem(Routes.BROWSE, "Browse", Icons.Filled.Category, Icons.Outlined.Category),
    BottomNavItem(Routes.LEARNINGS, "Learnings", Icons.Filled.School, Icons.Outlined.School),
    BottomNavItem(Routes.FAVORITES, "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
)
