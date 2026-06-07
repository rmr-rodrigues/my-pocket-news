package com.mypocketnews.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mypocketnews.MyPocketNewsApp
import com.mypocketnews.ui.detail.ArticleDetailScreen
import com.mypocketnews.ui.detail.ArticleDetailViewModel
import com.mypocketnews.ui.list.ArticleListScreen
import com.mypocketnews.ui.list.ArticleListViewModel
import com.mypocketnews.ui.settings.SettingsScreen
import com.mypocketnews.ui.settings.SettingsViewModel

@Composable
fun AppNavHost(
    app: MyPocketNewsApp,
    startDestination: String? = null,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination ?: "list",
        modifier = modifier
    ) {
        composable("list") {
            val listViewModel = viewModel<ArticleListViewModel>(
                factory = ArticleListViewModel.factory(app.database)
            )
            ArticleListScreen(
                viewModel = listViewModel,
                onArticleClick = { articleId ->
                    navController.navigate("detail/$articleId")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable(
            route = "detail/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getLong("articleId") ?: return@composable
            val detailViewModel = viewModel<ArticleDetailViewModel>(
                factory = ArticleDetailViewModel.factory(app.database, articleId)
            )
            ArticleDetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.navigateUp() }
            )
        }

        composable("settings") {
            val settingsViewModel = viewModel<SettingsViewModel>(
                factory = SettingsViewModel.factory(app.settingsRepository)
            )
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}
