package com.mydictionary.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mydictionary.ui.browse.BrowseScreen
import com.mydictionary.ui.browse.WordListScreen
import com.mydictionary.ui.browse.WordListViewModel
import com.mydictionary.ui.detail.WordDetailScreen
import com.mydictionary.ui.favorites.FavoritesScreen
import com.mydictionary.ui.home.HomeScreen
import com.mydictionary.ui.learnings.AddLearningScreen
import com.mydictionary.ui.learnings.LearningsScreen
import com.mydictionary.ui.search.SearchScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomRoutes = bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == item.route) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onWordClick = { navController.navigate(Routes.wordDetail(it)) },
                    onCategoryClick = { navController.navigate(Routes.wordsByCategory(it)) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onWordClick = { navController.navigate(Routes.wordDetail(it)) },
                    onAddLearning = { navController.navigate(Routes.addLearning(it)) }
                )
            }
            composable(Routes.BROWSE) {
                BrowseScreen(
                    onCategoryClick = { navController.navigate(Routes.wordsByCategory(it)) },
                    onGroupClick = { navController.navigate(Routes.wordsByGroup(it)) },
                    onPartOfSpeechClick = { navController.navigate(Routes.wordsByPartOfSpeech(it)) }
                )
            }
            composable(Routes.LEARNINGS) {
                LearningsScreen(
                    onAdd = { navController.navigate(Routes.addLearning()) },
                    onEdit = { id -> navController.navigate(Routes.addLearning(id = id)) }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(onWordClick = { navController.navigate(Routes.wordDetail(it)) })
            }
            composable(
                route = Routes.WORD_DETAIL,
                arguments = listOf(navArgument("word") { type = NavType.StringType })
            ) { entry ->
                val word = entry.arguments?.getString("word")?.let { Uri.decode(it) }.orEmpty()
                WordDetailScreen(
                    word = word,
                    onBack = { navController.popBackStack() },
                    onAddLearning = { navController.navigate(Routes.addLearning(word)) },
                    onCategoryClick = { navController.navigate(Routes.wordsByCategory(it)) }
                )
            }
            composable(
                route = Routes.WORD_LIST,
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType; defaultValue = "" },
                    navArgument("group") { type = NavType.StringType; defaultValue = "" },
                    navArgument("pos") { type = NavType.StringType; defaultValue = "" }
                )
            ) { entry ->
                val args = entry.arguments
                val category = args?.getString("category")?.let { Uri.decode(it) }.orEmpty()
                val group = args?.getString("group")?.let { Uri.decode(it) }.orEmpty()
                val pos = args?.getString("pos")?.let { Uri.decode(it) }.orEmpty()
                val title = when {
                    category.isNotEmpty() -> category
                    group.isNotEmpty() -> group
                    pos.isNotEmpty() -> pos.replaceFirstChar { it.uppercase() }
                    else -> "All words"
                }
                WordListScreen(
                    title = title,
                    filter = WordListViewModel.Filter(
                        category = category.ifEmpty { null },
                        categoryGroup = group.ifEmpty { null },
                        partOfSpeech = pos.ifEmpty { null }
                    ),
                    onBack = { navController.popBackStack() },
                    onWordClick = { navController.navigate(Routes.wordDetail(it)) },
                    onAddLearning = { navController.navigate(Routes.addLearning(it)) }
                )
            }
            composable(
                route = Routes.ADD_LEARNING,
                arguments = listOf(
                    navArgument("word") { type = NavType.StringType; defaultValue = "" },
                    navArgument("id") { type = NavType.LongType; defaultValue = 0L }
                )
            ) { entry ->
                val word = entry.arguments?.getString("word")?.let { Uri.decode(it) }.orEmpty()
                val id = entry.arguments?.getLong("id") ?: 0L
                AddLearningScreen(
                    initialWord = word,
                    learningId = id,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}
