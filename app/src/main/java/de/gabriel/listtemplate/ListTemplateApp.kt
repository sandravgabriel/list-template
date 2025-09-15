package de.gabriel.listtemplate

import android.util.Log
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
import de.gabriel.listtemplate.ui.item.ItemDetailsScreen // Import hinzugefügt
import de.gabriel.listtemplate.ui.item.ItemEntryDestination
import de.gabriel.listtemplate.ui.item.ItemEntryScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListTemplateApp(
    modifier: Modifier = Modifier
) {
    val navController: NavHostController = rememberNavController() // Dieser NavController wird aktuell nur für ItemEntry verwendet
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
                                ItemDetailsScreen(
                                    selectedItemIdFromParent = selectedItemId,
                                    onClosePane = {
                                        scope.launch {
                                            navigator.navigateBack()
                                        }
                                    },
                                    provideScaffold = false,
                                    // TODO: Definiere die Navigation zum Bearbeiten, wenn der NavGraph konsolidiert ist.
                                    // Momentan kennt der NavHost in ListTemplateApp die ItemEditDestination nicht.
                                    navigateToEditItem = { itemId -> Log.d("ListTemplateApp", "Navigate to edit item $itemId") },
                                    navigateBack = {
                                        scope.launch {
                                            navigator.navigateBack()
                                        }
                                    },
                                    modifier = modifier,
                                    itemIdFromNavArgs = null
                                )
                            } else {
                                Text(text = "Select an item to view its details.")
                            }
                        }
                    }
                }
            )
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
