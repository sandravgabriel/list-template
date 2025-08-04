package de.gabriel.listtemplate.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gabriel.listtemplate.R
import de.gabriel.listtemplate.ui.common.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailLayout(
    selectedItemId: Int?,
    onItemSelected: (itemId: Int) -> Unit, // Wird von listPaneContent aufgerufen
    onBack: () -> Unit, // Für Back-Handling im Detailbereich oder global
    listPaneContent: @Composable (paddingValues: PaddingValues) -> Unit,
    detailPaneContent: @Composable (itemId: Int, paddingValues: PaddingValues) -> Unit,
    modifier: Modifier = Modifier
) {
    // Dieser Scaffold wird der Haupt-Scaffold im Dual-Pane-Modus sein.
    Scaffold(
        modifier = modifier,
        topBar = {
            // Eine globale TopAppBar für das Dual-Pane-Layout.
            // Der Titel und die Aktionen können dynamisch sein.
            TopAppBar(
                title = if (selectedItemId != null) stringResource(R.string.screen_title_detail) else stringResource(R.string.app_name),
                canNavigateBack = selectedItemId != null, // Zeige "Zurück", um Detail zu schließen
                navigateUp = onBack // onBack sollte selectedItemId auf null setzen
            )
        }
    ) { innerPadding -> // Dieses innerPadding ist vom Scaffold des ListDetailLayout
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.4f) // Beispielgewichtung, anpassbar
                // .padding(innerPadding) // Das Padding wird an listPaneContent übergeben
            ) {
                // listPaneContent erhält das Padding vom äußeren Scaffold
                listPaneContent(innerPadding)
            }
            Box(
                modifier = Modifier
                    .weight(0.6f) // Beispielgewichtung
            ) {
                if (selectedItemId != null) {
                    detailPaneContent(selectedItemId, innerPadding)
                } else {
                    // Platzhalter, wenn kein Item ausgewählt ist
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.placeholder_no_item_selected))
                    }
                }
            }
        }
        // Hier könnte später der BackHandler für den Detailbereich hinkommen
        // if (selectedItemId != null) {
        //     BackHandler { onBack() }
        // }
    }
}
