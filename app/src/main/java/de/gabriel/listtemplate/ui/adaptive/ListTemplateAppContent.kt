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
import de.gabriel.listtemplate.ui.navigation.ListTemplateNavHost

@Composable
fun ListTemplateAppContent(
    navController: NavHostController,
    currentScreenWidthClass: WindowWidthSizeClass,
    selectedItemId: Int?,
    onItemSelected: (itemId: Int) -> Unit,
    onBackFromDetail: () -> Unit,
    modifier: Modifier = Modifier // Modifier für den gesamten Content-Bereich
) {
    // Die when-Anweisung wird das Herzstück deiner adaptiven Logik.
    // In Commit 4 ist sie noch einfach, wird aber in späteren Commits gefüllt.
    when (currentScreenWidthClass) {
        WindowWidthSizeClass.Compact -> {
            // Für Compact: Bestehenden NavHost verwenden.
            // Die Screens innerhalb des NavHost verwenden ihren eigenen Scaffold (provideScaffold = true).
            ListTemplateNavHost(
                navController = navController,
                modifier = modifier, // modifier hier an NavHost weitergeben
                // Du musst Callbacks an ListTemplateNavHost oder direkt an die Screens
                // im NavHost weitergeben, damit sie onItemSelected aufrufen können.
                // Beispiel: onItemClickInListScreen = onItemSelected
            )
        }
        WindowWidthSizeClass.Medium -> {
            // Für Medium: Vorerst wie Compact behandeln oder einen Platzhalter.
            // Später kannst du hier auch ListDetailLayout verwenden.
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Medium Screen Layout - Placeholder")
            }
            // Alternativ für den Anfang auch einfach den NavHost:
            // ListTemplateNavHost(navController = navController, modifier = modifier, ...)
        }
        WindowWidthSizeClass.Expanded -> {
            // Für Expanded: Hier wird ListDetailLayout verwendet.
            // In Commit 4 ist es nur ein Platzhalter.
            // Später wird hier ListDetailLayout(...) aufgerufen.
            ListDetailLayout(
                modifier = modifier, // modifier hier an ListDetailLayout weitergeben
                selectedItemId = selectedItemId,
                onItemSelected = onItemSelected,
                onBack = onBackFromDetail,
                listPaneContent = {
                    // Platzhalter für ListScreen (wird später HomeScreen sein)
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("List Pane (z.B. HomeScreen)")
                    }
                    // Später: HomeScreen(provideScaffold = false, onItemClick = onItemSelected, ...)
                },
                detailPaneContent = { itemId, receivedPaddingValues -> // Lambda nimmt itemId und PaddingValues entgegen
                    // Platzhalter für DetailScreen
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(receivedPaddingValues), // Padding hier anwenden
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Detail Pane für Item ID: $itemId")
                    }
                    // Später: ItemDetailsScreen(provideScaffold = false, itemId = itemId, modifier = Modifier.padding(receivedPaddingValues), ...)
                }
            )
        }
    }
}