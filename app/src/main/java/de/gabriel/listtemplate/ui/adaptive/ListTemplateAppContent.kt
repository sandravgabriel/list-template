package de.gabriel.listtemplate.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import de.gabriel.listtemplate.ui.HomeScreen
import de.gabriel.listtemplate.ui.item.ItemDetailsScreen
import de.gabriel.listtemplate.ui.navigation.ListTemplateNavHost

@Composable
fun ListTemplateAppContent(
    navController: NavHostController,
    currentScreenWidthClass: WindowWidthSizeClass,
    selectedItemId: Int?,
    onItemSelected: (itemId: Int) -> Unit,
    onBackFromDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (currentScreenWidthClass) {
        WindowWidthSizeClass.Compact -> {
            ListTemplateNavHost(
                navController = navController,
                modifier = modifier,
                onItemSelectedInListScreen = onItemSelected
            )
        }
        WindowWidthSizeClass.Medium -> {
            // Für Medium: Vorerst ein Platzhalter.
            // Später hier auch ListDetailLayout verwenden.
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Medium Screen Layout - Placeholder")
            }
        }
        WindowWidthSizeClass.Expanded -> {
            ListDetailLayout(
                modifier = modifier,
                selectedItemId = selectedItemId,
                onItemSelected = onItemSelected,
                onBack = onBackFromDetail,
                listPaneContent = { receivedPaddingValues ->
                    HomeScreen(
                        navigateToItemEntry = { /* TODO: Logik für FAB-Klick im Dual Pane */ },
                        onItemClick = onItemSelected,
                        modifier = Modifier.padding(receivedPaddingValues),
                        provideScaffold = false
                    )
                },
                detailPaneContent = { itemId, receivedPaddingValues ->
                    ItemDetailsScreen(
                        navigateToEditItem = { /* TODO: Logik für Bearbeiten-Klick im Dual Pane */ },
                        navigateBack = onBackFromDetail,
                        modifier = Modifier.padding(receivedPaddingValues),
                        provideScaffold = false
                    )
                }
            )
        }
    }
}