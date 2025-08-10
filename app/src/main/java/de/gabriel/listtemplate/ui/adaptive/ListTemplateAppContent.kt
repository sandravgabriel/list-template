package de.gabriel.listtemplate.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import de.gabriel.listtemplate.DetailScreenMode
import de.gabriel.listtemplate.ENTRY_ITEM_ID
import de.gabriel.listtemplate.R
import de.gabriel.listtemplate.ui.HomeScreen
import de.gabriel.listtemplate.ui.item.ItemDetailsScreen
import de.gabriel.listtemplate.ui.item.ItemEditScreen
import de.gabriel.listtemplate.ui.item.ItemEntryScreen
import de.gabriel.listtemplate.ui.navigation.ListTemplateNavHost

@Composable
fun ListTemplateAppContent(
    navController: NavHostController,
    currentScreenWidthClass: WindowWidthSizeClass,
    selectedItemId: Int?,
    detailScreenMode: DetailScreenMode,
    onItemSelected: (itemId: Int) -> Unit,
    onNavigateToEditInDetailPane: () -> Unit,
    onBackFromDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (currentScreenWidthClass) {
        WindowWidthSizeClass.Compact -> {
            ListTemplateNavHost(
                navController = navController,
                modifier = modifier,
                onItemSelectedInListScreen = { itemId ->
                    onItemSelected(itemId)
                }
            )
        }
        WindowWidthSizeClass.Medium -> {
            ListTemplateNavHost(
                navController = navController,
                modifier = modifier,
                onItemSelectedInListScreen = { itemId ->
                    onItemSelected(itemId)
                }
            )
        }
        WindowWidthSizeClass.Expanded -> {
            ListDetailLayout(
                modifier = modifier,
                selectedItemId = selectedItemId,
                detailScreenMode = detailScreenMode,
                onItemSelected = onItemSelected,
                onBack = onBackFromDetail,
                listPaneContent = { receivedPaddingValues ->
                    HomeScreen(
                        navigateToItemEntry = { onItemSelected(ENTRY_ITEM_ID) },
                        onItemClick = onItemSelected,
                        modifier = Modifier.padding(receivedPaddingValues),
                        provideScaffold = false
                    )
                },
                detailPaneContent = { itemId, currentDetailMode, receivedPaddingValues ->
                    when {
                        // Fall 1: Eingabemodus
                        itemId == ENTRY_ITEM_ID -> {
                            ItemEntryScreen(
                                provideScaffold = false,
                                modifier = Modifier.padding(receivedPaddingValues),
                                navigateBack = onBackFromDetail,
                                onNavigateUp = onBackFromDetail
                            )
                        }
                        // Fall 2: Bearbeitungsmodus (nur wenn ein gültiges Item ausgewählt ist)
                        detailScreenMode == DetailScreenMode.EDIT && itemId != null -> {
                            ItemEditScreen(
                                selectedItemId = itemId,
                                provideScaffold = false,
                                modifier = Modifier.padding(receivedPaddingValues),
                                // Wenn Bearbeitung fertig oder abgebrochen -> zurück zur Detailansicht
                                onNavigateUp = onBackFromDetail, // Ruft Logik in ListTemplateApp auf, die Modus auf VIEW setzt
                                navigateBack = onBackFromDetail
                            )
                        }
                        // Fall 3: Ansichtsmodus (nur wenn ein gültiges Item ausgewählt ist)
                        detailScreenMode == DetailScreenMode.VIEW && itemId != null -> {
                            ItemDetailsScreen(
                                itemIdFromNavArgs = null,
                                selectedItemIdFromParent = itemId,
                                provideScaffold = false,
                                modifier = Modifier.padding(receivedPaddingValues),
                                navigateToEditItem = {
                                    onNavigateToEditInDetailPane()
                                },
                                navigateBack = onBackFromDetail
                            )
                        }
                        // Fall 4: Kein Item ausgewählt oder ungültiger Zustand (Platzhalter)
                        else -> {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(receivedPaddingValues),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.placeholder_no_item_selected))
                            }
                        }
                    }
                }
            )
        }
    }
}