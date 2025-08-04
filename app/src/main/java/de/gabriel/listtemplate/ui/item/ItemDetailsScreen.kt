package de.gabriel.listtemplate.ui.item

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.gabriel.listtemplate.ui.common.TopAppBar
import de.gabriel.listtemplate.R
import de.gabriel.listtemplate.data.Item
import de.gabriel.listtemplate.ui.AppViewModelProvider
import de.gabriel.listtemplate.ui.navigation.NavigationDestination
import de.gabriel.listtemplate.ui.theme.ListTemplateTheme
import kotlinx.coroutines.launch

object ItemDetailsDestination : NavigationDestination {
    override val route = "item_details"
    override val titleRes = R.string.item_detail_title
    const val ITEM_ID_ARG = "itemId"
    val routeWithArgs = "$route/{$ITEM_ID_ARG}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    navigateToEditItem: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    provideScaffold: Boolean = true,
    topAppBarTitle: String = stringResource(ItemDetailsDestination.titleRes)
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showDeleteFailedDialog by rememberSaveable { mutableStateOf(false) }

    val screenContent = @Composable { paddingValuesFromParentScaffold: PaddingValues ->
        Box(modifier = modifier.fillMaxSize()) {
            ItemDetailsBody(
                itemDetailsUiState = uiState.value,
                onDelete = {
                    coroutineScope.launch {
                        if (viewModel.deleteItem()) {
                            navigateBack()
                        } else {
                            showDeleteFailedDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = paddingValuesFromParentScaffold.calculateStartPadding(
                            LocalLayoutDirection.current
                        ),
                        end = paddingValuesFromParentScaffold.calculateEndPadding(
                            LocalLayoutDirection.current
                        ),
                        top = paddingValuesFromParentScaffold.calculateTopPadding()
                    )
                    .verticalScroll(rememberScrollState())
            )
            FloatingActionButton(
                onClick = { navigateToEditItem(uiState.value.itemDetails?.id ?: 0) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimensionResource(id = R.dimen.padding_large))
                    .padding(
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(LocalLayoutDirection.current),
                        bottom = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateBottomPadding()
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_item_title),
                )
            }
            if (showDeleteFailedDialog) {
                DeleteFailedDialog(
                    onDismiss = { showDeleteFailedDialog = false }
                )
            }
        }
    }

    if (provideScaffold) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = topAppBarTitle,
                    canNavigateBack = true,
                    navigateUp = navigateBack
                )
            }
        ) { innerPadding ->
            screenContent(innerPadding)
        }
    } else {
        screenContent(PaddingValues(0.dp))
    }
}

@Composable
private fun ItemDetailsBody(
    itemDetailsUiState: ItemDetailsUiState,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }

        val itemDetails = itemDetailsUiState.itemDetails
        if (itemDetails != null) {
            ItemDetails(
                item = itemDetails.toItem(),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // TODO:
            // Optional: Zeige einen Ladeindikator oder nichts, da die Navigation bald stattfinden sollte.
            // Text("Item wird geladen oder ist nicht verfügbar...")
        }
        OutlinedButton(
            onClick = { deleteConfirmationRequired = true },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete))
        }
        if (deleteConfirmationRequired) {
            DeleteConfirmationDialog(
                onDeleteConfirm = {
                    deleteConfirmationRequired = false
                    onDelete()
                },
                onDeleteCancel = { deleteConfirmationRequired = false },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            )
        }
    }
}

@Composable
fun ItemDetails(
    item: Item, modifier: Modifier = Modifier
) {
    val userImageAvailable = item.image != null
    val painter = painterResource(id = R.drawable.default_image)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.padding_medium)
            )
        ) {
            ItemDetailsRow(
                itemDetail = item.name,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(
                        id = R.dimen
                            .padding_medium
                    )
                ),
            )
            if (userImageAvailable) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.image)
                        .placeholder(R.drawable.default_image)
                        //.error(R.drawable.ic_error_image) // TODO: Ein Fehler-Drawable
                        .listener(onError = { request, result ->
                            Log.e("Coil", "Fehler beim Laden von ${request.data}: ${result.throwable}") //TODO
                        })
                        .build(),
                    contentDescription = "selected image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(vertical = dimensionResource(id = R.dimen.padding_small))
                        .aspectRatio(1f),
                )
            } else {
                Image(
                    painter = painter,
                    contentDescription = "default image",
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.4f)
                        .aspectRatio(1f)
                        .padding(
                            horizontal = dimensionResource(
                                id = R.dimen
                                    .padding_medium
                            )
                        )
                        .align(Alignment.CenterHorizontally),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                )
            }
        }
    }
}

@Composable
private fun ItemDetailsRow(
    itemDetail: String, modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(text = itemDetail, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(stringResource(R.string.yes))
            }
        })
}

@Composable
private fun DeleteFailedDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_failed_title)) },
        text = { Text(stringResource(R.string.delete_failed_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun ItemDetailsScreenPreview() {
    ListTemplateTheme {
        ItemDetailsBody(
            ItemDetailsUiState(
                itemDetails = ItemDetails(1, "Item")
            ),
            onDelete = {}
        )
    }
}