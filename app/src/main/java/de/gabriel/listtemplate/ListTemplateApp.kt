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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.gabriel.listtemplate.ui.HomeDestination
import de.gabriel.listtemplate.ui.HomeScreen
import de.gabriel.listtemplate.ui.item.ItemDetailsDestination
import de.gabriel.listtemplate.ui.item.ItemDetailsScreen
import de.gabriel.listtemplate.ui.item.ItemEditDestination
import de.gabriel.listtemplate.ui.item.ItemEditScreen
import de.gabriel.listtemplate.ui.item.ItemEntryDestination
import de.gabriel.listtemplate.ui.item.ItemEntryScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListTemplateApp(
    modifier: Modifier = Modifier
) {
    val navController: NavHostController = rememberNavController()
    val listDetailPaneNavigator = rememberListDetailPaneScaffoldNavigator<Int?>()
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = HomeDestination.route) {
        composable(HomeDestination.route) {
            NavigableListDetailPaneScaffold<Int?>(
                modifier = modifier, // Modifier für den Scaffold selbst
                navigator = listDetailPaneNavigator,
                listPane = {
                    AnimatedPane(modifier = Modifier) {
                        HomeScreen(
                            navigateToItemEntry = {
                                navController.navigate(ItemEntryDestination.route)
                            },
                            onItemClick = { itemId ->
                                scope.launch {
                                    listDetailPaneNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, itemId)
                                }
                            },
                            provideScaffold = false
                        )
                    }
                },
                detailPane = {
                    AnimatedPane(modifier = Modifier) {
                        AnimatedContent(
                            targetState = listDetailPaneNavigator.currentDestination?.contentKey,
                            label = "DetailPaneAnimation"
                        ) { selectedItemId: Int? ->
                            if (selectedItemId != null) {
                                ItemDetailsScreen(
                                    selectedItemIdFromParent = selectedItemId,
                                    onClosePane = {
                                        scope.launch {
                                            listDetailPaneNavigator.navigateBack()
                                        }
                                    },
                                    navigateToEditItem = { itemId ->
                                        navController.navigate("${ItemEditDestination.route}/$itemId")
                                    },
                                    navigateBack = {
                                        scope.launch {
                                            listDetailPaneNavigator.navigateBack()
                                        }
                                    },
                                    provideScaffold = false,
                                    itemIdFromNavArgs = null
                                    // Der Modifier wird hier nicht benötigt, da AnimatedPane den Modifier bereits hat
                                )
                            } else {
                                Text(text = "Select an item to view its details.")
                            }
                        }
                    }
                }
            )
            BackHandler(enabled = listDetailPaneNavigator.canNavigateBack()) {
                scope.launch {
                    listDetailPaneNavigator.navigateBack()
                }
            }
        }

        composable(route = ItemEntryDestination.route) {
            ItemEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        // Standalone-Route für ItemDetailsScreen (z.B. für schmale Bildschirme oder direkte Links)
        composable(
            route = ItemDetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(ItemDetailsDestination.ITEM_ID_ARG) {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt(ItemDetailsDestination.ITEM_ID_ARG)
            ItemDetailsScreen(
                itemIdFromNavArgs = itemId,
                selectedItemIdFromParent = null,
                onClosePane = null, // Kein Pane zum Schließen im Standalone-Modus
                navigateToEditItem = { currentItemId ->
                    navController.navigate("${ItemEditDestination.route}/$currentItemId")
                },
                navigateBack = { navController.popBackStack() },
                provideScaffold = true
            )
        }

        composable(
            route = ItemEditDestination.routeWithArgs,
            arguments = listOf(navArgument(ItemEditDestination.ITEM_ID_ARG) {
                type = NavType.IntType
            })
        ) {
            ItemEditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
