package de.gabriel.template.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gabriel.template.data.ItemsRepository
import de.gabriel.template.data.PhotoSaverRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
    private val photoSaver: PhotoSaverRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ItemDetailsUiState> =
        savedStateHandle.getStateFlow(ItemDetailsDestination.ITEM_ID_ARG, 0)
            .flatMapLatest { itemId ->
                if (itemId > 0) { // Nur laden, wenn eine gültige ID vorhanden ist
                    itemsRepository.getItemWithFile(itemId, photoSaver.photoFolder)
                        .map { item ->
                            ItemDetailsUiState(itemDetails = item?.toItemDetails())
                        }
                } else {
                    flowOf(ItemDetailsUiState()) // Leerer Zustand für ungültige ID
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ItemDetailsUiState()
            )

    suspend fun deleteItem() {
        val itemDetails = uiState.value.itemDetails
        if (itemDetails != null) {
            itemsRepository.deleteItem(itemDetails.toItem().toItemEntry())
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ItemDetailsUiState(
    val itemDetails: ItemDetails? = null
)
