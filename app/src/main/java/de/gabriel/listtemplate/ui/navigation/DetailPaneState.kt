package de.gabriel.listtemplate.ui.navigation

sealed class DetailPaneState {
    data object Hidden : DetailPaneState() // Für den Platzhalter-Zustand
    data class ViewItem(val itemId: Int) : DetailPaneState()
    data class EditItem(val itemId: Int) : DetailPaneState()
}
