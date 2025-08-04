package de.gabriel.listtemplate

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import de.gabriel.listtemplate.ui.adaptive.ListTemplateAppContent
import de.gabriel.listtemplate.ui.item.ItemDetailsDestination
import de.gabriel.listtemplate.ui.item.ItemEntryDestination

enum class DetailScreenMode {
    VIEW,
    EDIT
}

const val ENTRY_ITEM_ID = -1

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ListTemplateApp(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val activity = context as Activity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val currentScreenWidthClass = windowSizeClass.widthSizeClass

    var selectedItemId by rememberSaveable { mutableStateOf<Int?>(null) }
    var detailScreenMode by rememberSaveable { mutableStateOf(DetailScreenMode.VIEW) }

    ListTemplateAppContent(
        navController = navController,
        currentScreenWidthClass = currentScreenWidthClass,
        selectedItemId = selectedItemId,
        detailScreenMode = detailScreenMode,
        onItemSelected = { itemId ->
            selectedItemId = itemId
            // Wenn ein neues Item ausgewählt wird (oder der Entry-Modus gestartet wird),
            // immer zur VIEW-Ansicht des Details zurückkehren.
            detailScreenMode = DetailScreenMode.VIEW
            if (currentScreenWidthClass != WindowWidthSizeClass.Expanded && itemId != ENTRY_ITEM_ID) {
                navController.navigate("${ItemDetailsDestination.route}/$itemId")
            } else if (currentScreenWidthClass != WindowWidthSizeClass.Expanded && itemId == ENTRY_ITEM_ID) {
                navController.navigate(ItemEntryDestination.route)
            }
        },
        onNavigateToEditInDetailPane = {
            detailScreenMode = DetailScreenMode.EDIT
        },
        onBackFromDetail = {
            if (detailScreenMode == DetailScreenMode.EDIT && selectedItemId != null && selectedItemId != ENTRY_ITEM_ID) {
                // Wenn im Edit-Modus und "zurück", dann zur Detail-View zurückkehren
                detailScreenMode = DetailScreenMode.VIEW
            } else {
                // Sonst: Detailbereich leeren (selectedItemId auf null setzen)
                // und sicherstellen, dass der Modus für das nächste Mal auf VIEW steht.
                selectedItemId = null
                detailScreenMode = DetailScreenMode.VIEW
            }
            if (currentScreenWidthClass != WindowWidthSizeClass.Expanded) {
                navController.popBackStack()
            }
        }
    )
}