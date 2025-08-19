package de.gabriel.listtemplate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.gabriel.listtemplate.ui.HomeDestination
import de.gabriel.listtemplate.ui.HomeScreen
import de.gabriel.listtemplate.ui.item.ItemDetailsDestination
import de.gabriel.listtemplate.ui.item.ItemDetailsScreen
import de.gabriel.listtemplate.ui.item.ItemEditDestination
import de.gabriel.listtemplate.ui.item.ItemEditScreen
import de.gabriel.listtemplate.ui.item.ItemEntryDestination
import de.gabriel.listtemplate.ui.item.ItemEntryScreen

@Composable
fun ListTemplateNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onItemSelectedInListScreen: (Int) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToItemEntry = { navController.navigate(ItemEntryDestination.route) },
                onItemClick = { itemId ->
                    onItemSelectedInListScreen(itemId)
                },
            )
        }
        composable(route = ItemEntryDestination.route) {
            ItemEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
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
                navigateToEditItem = { currentItemId ->
                    navController.navigate("${ItemEditDestination.route}/$currentItemId")
                },
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = ItemEditDestination.routeWithArgs,
            arguments = listOf(navArgument(ItemEditDestination.ITEM_ID_ARG) {
                type = NavType.IntType
            })
        ) {
            // Das ViewModel im ItemEditScreen wird die itemId aus den NavArgs beziehen.
            ItemEditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
