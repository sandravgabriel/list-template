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
    navigateBack: () -> Unit, 
    onNavigateUp: () -> Unit,   
    modifier: Modifier = Modifier, 
    // Für Hosting im Detail-Pane
    itemIdFromPane: Int? = null, 
    onDoneEditingInPane: ((editedItemId: Int) -> Unit)? = null, 
    onNavigateBackInPane: (() -> Unit)? = null,
    viewModel: ItemEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
    provideScaffold: Boolean = true, 
    topAppBarTitleText: String = stringResource(R.string.edit_item_title)
) {
    LaunchedEffect(itemIdFromPane) {
        if (itemIdFromPane != null && itemIdFromPane > 0) {
            viewModel.initializeWithItemId(itemIdFromPane)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val actualLoadedItemId = viewModel.itemUiState.itemDetails.id

    if (provideScaffold) {
        Scaffold(
            modifier = modifier, 
            topBar = {
                TopAppBar(
                    title = topAppBarTitleText,
                    canNavigateBack = true,
                    navigateUp = onNavigateUp // Für Standalone-Modus
                )
            }
        ) { innerPadding -> 
            ItemEntryBody(
                itemUiState = viewModel.itemUiState,
                onItemValueChange = viewModel::updateUiState,
                onSaveClick = {
                    coroutineScope.launch {
                        val success = viewModel.updateItem()
                        if (success) {
                            navigateBack() 
                        }
                    }
                },
                onPhotoPickerSelect = viewModel::onPhotoPickerSelect,
                modifier = Modifier 
                    .padding(innerPadding) 
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }
    } else {
        // Im Detail-Pane: Eigene Column mit TopAppBar
        Column(modifier = modifier.fillMaxSize()) { 
            TopAppBar(
                title = topAppBarTitleText,
                canNavigateBack = true,
                navigateUp = {
                    if (onNavigateBackInPane != null) {
                        onNavigateBackInPane() //Bevorzugte Aktion für Pane
                    } else {
                        // Fallback (sollte nicht eintreten, wenn von ListTemplateApp korrekt versorgt)
                        if (itemIdFromPane != null && itemIdFromPane > 0) {
                            onDoneEditingInPane?.invoke(itemIdFromPane) 
                        } else {
                           // Im unwahrscheinlichen Fall, dass keine der Aktionen möglich ist.
                           // Hier könnte man auch onNavigateUp() aus dem Standalone-Modus rufen, aber das
                           // würde den globalen navController beeinflussen, was im Pane evtl. unerwünscht ist.
                        }
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
                            if (actualLoadedItemId > 0) {
                                onDoneEditingInPane?.invoke(actualLoadedItemId)
                            } else {
                                Log.e("ItemEditScreen", "Konnte nach dem Speichern im Pane nicht zurücknavigieren: Ungültige actualLoadedItemId.")
                                if (itemIdFromPane != null && itemIdFromPane > 0) {
                                    onDoneEditingInPane?.invoke(itemIdFromPane)
                                }
                            }
                        }
                    }
                },
                onPhotoPickerSelect = viewModel::onPhotoPickerSelect,
                modifier = Modifier 
                    .padding(dimensionResource(id = R.dimen.padding_medium))
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}