package de.gabriel.template.ui.home

import androidx.lifecycle.ViewModel
import de.gabriel.template.data.Item

class HomeViewModel : ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUiState(val itemList: List<Item> = listOf())
