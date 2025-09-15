package de.gabriel.listtemplate

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gabriel.listtemplate.ui.HomeDestination
import de.gabriel.listtemplate.ui.HomeScreen
import de.gabriel.listtemplate.ui.item.ItemEntryDestination
import de.gabriel.listtemplate.ui.item.ItemEntryScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListTemplateApp(
    modifier: Modifier = Modifier
) {
    val navController: NavHostController = rememberNavController()
    val navigator = rememberListDetailPaneScaffoldNavigator<Int?>()
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = HomeDestination.route) {
        composable(HomeDestination.route) {
            NavigableListDetailPaneScaffold(
                modifier = modifier,
                navigator = navigator,
                listPane = {
                    AnimatedPane(modifier = Modifier) {
                        HomeScreen(
                            navigateToItemEntry = {
                                navController.navigate(ItemEntryDestination.route)
                            },
                            onItemClick = { itemId ->
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, itemId)
                                }
                            },
                            provideScaffold = false
                        )
                    }
                },
                detailPane = {
                    AnimatedPane(modifier = Modifier) {
                        AnimatedContent(
                            targetState = navigator.currentDestination?.contentKey,
                            label = "DetailPaneAnimation"
                        ) { selectedItemId: Int? ->
                            if (selectedItemId != null) {
                                Text(text = "Displaying details for item ID: $selectedItemId")
                            } else {
                                Text(text = "Select an item to view its details.")
                            }
                        }
                    }
                }
            )
            // BackHandler für den listDetailPaneNavigator, spezifisch für diese Route
            BackHandler(enabled = navigator.canNavigateBack()) {
                scope.launch {
                    navigator.navigateBack()
                }
            }
        }
            composable(ItemEntryDestination.route) {
                ItemEntryScreen(
                    navigateBack = { navController.popBackStack() },
                    onNavigateUp = { navController.popBackStack() }
                )
            }

            // Hier könnten weitere globale Routen definiert werden (z.B. Settings, etc.)
            // composable("settings") { SettingsScreen(appNavController) }
    }
}
