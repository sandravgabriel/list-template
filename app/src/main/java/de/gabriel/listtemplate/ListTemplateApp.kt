package de.gabriel.listtemplate

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import de.gabriel.listtemplate.ui.HomeScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListTemplateApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Int?>()
    val scope = rememberCoroutineScope()

    NavigableListDetailPaneScaffold<Int?>(
        modifier = modifier,
        navigator = navigator,
        listPane = {
            HomeScreen(
                navigateToItemEntry = { /*TODO? */ },
                onItemClick = { itemId ->
                    scope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, itemId)
                    }
                },
                provideScaffold = false
            )
        },
        detailPane = {
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
    )
}
