package de.gabriel.listtemplate.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gabriel.listtemplate.DetailScreenMode
import de.gabriel.listtemplate.ENTRY_ITEM_ID
import de.gabriel.listtemplate.R
import de.gabriel.listtemplate.ui.common.TopAppBar
import de.gabriel.listtemplate.ui.item.ItemDetailsDestination
import de.gabriel.listtemplate.ui.item.ItemEditDestination
import de.gabriel.listtemplate.ui.item.ItemEntryDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailLayout(
    selectedItemId: Int?,
    detailScreenMode: DetailScreenMode,
    onBack: () -> Unit,
    listPaneContent: @Composable (paddingValues: PaddingValues) -> Unit,
    detailPaneContent: @Composable (itemId: Int?, currentDetailMode: DetailScreenMode, paddingValues: PaddingValues) -> Unit,
    modifier: Modifier = Modifier
) {
    // Dieser Scaffold wird der Haupt-Scaffold im Dual-Pane-Modus sein.
    Scaffold(
        modifier = modifier,
        topBar = {
            val titleRes = when {
                selectedItemId == ENTRY_ITEM_ID -> ItemEntryDestination.titleRes
                detailScreenMode == DetailScreenMode.EDIT && selectedItemId != null -> ItemEditDestination.titleRes
                selectedItemId != null -> ItemDetailsDestination.titleRes
                else -> R.string.app_name
            }
            TopAppBar(
                title = stringResource(titleRes),
                canNavigateBack = false,
                navigateUp = onBack
            )
        }
    ) { innerPadding ->
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.4f)
            ) {
                listPaneContent(innerPadding)
            }
            Box(
                modifier = Modifier.weight(0.6f)
            ) {
                detailPaneContent(selectedItemId, detailScreenMode, innerPadding)
            }
        }
    }
}
