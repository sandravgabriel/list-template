package de.gabriel.listtemplate.ui.item

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import de.gabriel.listtemplate.R
import de.gabriel.listtemplate.ui.AppViewModelProvider
import de.gabriel.listtemplate.ui.common.TopAppBar
import de.gabriel.listtemplate.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object ItemEditDestination : NavigationDestination {
    override val route = "item_edit"
    override val titleRes = R.string.edit_item_title
    const val ITEM_ID_ARG = "itemId"
    val routeWithArgs = "$route/{$ITEM_ID_ARG}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditScreen(
    // Für Standalone-Navigation
    navigateBack: () -> Unit, // Nach erfolgreichem Speichern im Standalone-Modus
    onNavigateUp: () -> Unit,   // Für den Zurück-Pfeil in der TopAppBar im Standalone-Modus
    modifier: Modifier = Modifier, // Modifier für die Wurzel-Composable dieses Screens
    // Für Hosting im Detail-Pane
    itemIdFromPane: Int? = null, // ID des Items, das im Pane bearbeitet wird
    onDoneEditingInPane: ((editedItemId: Int) -> Unit)? = null, // Callback für Pane: navigiert zu ViewItem(id)
    viewModel: ItemEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
    provideScaffold: Boolean = true, // True für standalone, false für Detail-Pane
    topAppBarTitleText: String = stringResource(R.string.edit_item_title)
) {
    LaunchedEffect(itemIdFromPane) {
        if (itemIdFromPane != null && itemIdFromPane > 0) {
            // ViewModel mit der ID aus dem Pane initialisieren.
            // Das ViewModel sollte intern sicherstellen, dass es nicht unnötig neu lädt.
            viewModel.initializeWithItemId(itemIdFromPane)
        }
        // Wenn itemIdFromPane null ist, wird erwartet, dass das ViewModel
        // die itemId aus dem SavedStateHandle (NavArgs des Haupt-NavControllers) holt.
    }

    val coroutineScope = rememberCoroutineScope()

    // Die ID des Items, das tatsächlich geladen und bearbeitet wird.
    // Wichtig für die Rücknavigation nach dem Speichern im Pane-Modus.
    val actualLoadedItemId = viewModel.itemUiState.itemDetails.id

    if (provideScaffold) {
        Scaffold(
            modifier = modifier, // ItemEditScreen-Modifier auf Scaffold anwenden
            topBar = {
                TopAppBar(
                    title = topAppBarTitleText,
                    canNavigateBack = true,
                    navigateUp = onNavigateUp // Für Standalone-Modus
                )
            }
        ) { innerPadding -> // Padding vom Scaffold
            ItemEntryBody(
                itemUiState = viewModel.itemUiState,
                onItemValueChange = viewModel::updateUiState,
                onSaveClick = {
                    coroutineScope.launch {
                        val success = viewModel.updateItem() // ViewModel sollte Erfolg zurückgeben
                        if (success) {
                            navigateBack() // Standalone-Modus: Zurück-Navigation
                        }
                    }
                },
                onPhotoPickerSelect = viewModel::onPhotoPickerSelect,
                modifier = Modifier // Eigener Modifier für ItemEntryBody
                    .padding(innerPadding) // Padding vom Scaffold übernehmen
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }
    } else {
        // Im Detail-Pane: Eigene Column mit TopAppBar
        Column(modifier = modifier.fillMaxSize()) { // ItemEditScreen-Modifier auf Column anwenden
            TopAppBar(
                title = topAppBarTitleText,
                canNavigateBack = true,
                navigateUp = {
                    // Im Pane-Modus: Zurück zum ViewItem-Zustand des ursprünglich geöffneten Items
                    if (itemIdFromPane != null && itemIdFromPane > 0) {
                        onDoneEditingInPane?.invoke(itemIdFromPane)
                    }
                }
            )
            ItemEntryBody(
                itemUiState = viewModel.itemUiState,
                onItemValueChange = viewModel::updateUiState,
                onSaveClick = {
                    coroutineScope.launch {
                        val success = viewModel.updateItem()
                        if (success) {
                            // Im Pane-Modus: Zurück zum ViewItem-Zustand des *bearbeiteten* Items
                            if (actualLoadedItemId > 0) {
                                onDoneEditingInPane?.invoke(actualLoadedItemId)
                            } else {
                                Log.e("ItemEditScreen", "Konnte nach dem Speichern im Pane nicht zurücknavigieren: Ungültige actualLoadedItemId.")
                                // Fallback: Vielleicht zum ursprünglichen itemIdFromPane, wenn actualLoadedItemId ungültig
                                if (itemIdFromPane != null && itemIdFromPane > 0) {
                                    onDoneEditingInPane?.invoke(itemIdFromPane)
                                } else {
                                     // Letzter Ausweg: Versuche, das Pane generell zurückzunavigieren (könnte zu Hidden führen)
                                     // Diese Logik gehört aber eher in ListTemplateApp, wenn onDoneEditingInPane null ist oder fehlschlägt.
                                }
                            }
                        }
                    }
                },
                onPhotoPickerSelect = viewModel::onPhotoPickerSelect,
                modifier = Modifier // Eigener Modifier für ItemEntryBody
                    .padding(dimensionResource(id = R.dimen.padding_medium))
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}