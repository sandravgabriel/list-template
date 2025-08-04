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
import de.gabriel.listtemplate.ui.navigation.ListTemplateNavHost

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ListTemplateApp(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val activity = context as Activity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val currentScreenWidthClass = windowSizeClass.widthSizeClass
    var selectedItemId by rememberSaveable { mutableStateOf<Int?>(null) }

    //vorher: ListTemplateNavHost(navController = navController)

    ListTemplateAppContent(
        navController = navController,
        currentScreenWidthClass = currentScreenWidthClass,
        selectedItemId = selectedItemId,
        onItemSelected = { itemId ->
            // Logik für Item-Auswahl kommt in späteren Commits
            selectedItemId = if (currentScreenWidthClass == WindowWidthSizeClass.Expanded) {
                itemId // Im Expanded-Modus nur den Zustand aktualisieren
            } else {
                // Im Compact/Medium-Modus zum Detail-Screen navigieren
                // Diese Navigation wird später verfeinert
                navController.navigate("item_details/$itemId") // Beispielroute
                itemId
            }
        },
        onBackFromDetail = {
            // Logik für Zurück vom Detail kommt später
            if (currentScreenWidthClass == WindowWidthSizeClass.Expanded) {
                selectedItemId = null // Im Expanded-Modus den Detailbereich leeren
            } else {
                navController.popBackStack()
                selectedItemId = null
            }
        }
    )
}