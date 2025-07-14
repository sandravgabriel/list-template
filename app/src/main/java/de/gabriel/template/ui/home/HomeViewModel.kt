package de.gabriel.template.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gabriel.template.data.Item
import de.gabriel.template.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(itemsRepository: ItemsRepository) : ViewModel() {

    val homeUiState: StateFlow<de.gabriel.template.ui.home.HomeUiState> =
        itemsRepository.getAllItemsStream().map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(de.gabriel.template.ui.home.HomeViewModel.Companion.TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUiState(val itemList: List<Item> = listOf())
