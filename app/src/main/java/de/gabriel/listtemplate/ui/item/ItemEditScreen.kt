package de.gabriel.listtemplate.ui.item

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.gabriel.listtemplate.ui.common.TopAppBar
import de.gabriel.listtemplate.R
import de.gabriel.listtemplate.ui.AppViewModelProvider
import de.gabriel.listtemplate.ui.navigation.NavigationDestination

object ItemEditDestination : NavigationDestination {
    override val route = "item_edit"
    override val titleRes = R.string.edit_item_title
    const val ITEM_ID_ARG = "itemId"
    val routeWithArgs = "$route/{$ITEM_ID_ARG}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
    provideScaffold: Boolean = true,
    topAppBarTitle: String = stringResource(ItemEditDestination.titleRes)
) {
    val coroutineScope = rememberCoroutineScope()

    val screenContent = @Composable { paddingValuesFromParentScaffold: PaddingValues ->
        ItemEntryBody(
            itemUiState = viewModel.itemUiState,
            onItemValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.updateItem()
                    navigateBack()
                }
            },
            onPhotoPickerSelect = viewModel::onPhotoPickerSelect,
            modifier = Modifier
                .padding(
                    start = paddingValuesFromParentScaffold.calculateStartPadding(LocalLayoutDirection.current),
                    end = paddingValuesFromParentScaffold.calculateEndPadding(LocalLayoutDirection.current),
                    top = paddingValuesFromParentScaffold.calculateTopPadding()
                )
                .verticalScroll(rememberScrollState())
        )
    }

    if (provideScaffold) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = topAppBarTitle,
                    canNavigateBack = true,
                    navigateUp = onNavigateUp
                )
            }
        ) { innerPadding ->
            screenContent(innerPadding)
        }
    } else {
        screenContent(PaddingValues(0.dp))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = topAppBarTitle,
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        modifier = modifier
    ) { innerPadding ->
        ItemEntryBody(
            itemUiState = viewModel.itemUiState,
            onItemValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.updateItem()
                    navigateBack()
                }
            },
            onPhotoPickerSelect = viewModel::onPhotoPickerSelect,
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding()
                )
                .verticalScroll(rememberScrollState())
        )
    }
}