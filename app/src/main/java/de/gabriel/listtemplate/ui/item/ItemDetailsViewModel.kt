package de.gabriel.listtemplate.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gabriel.listtemplate.data.ItemsRepository
import de.gabriel.listtemplate.data.PhotoSaverRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File

class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
    private val photoSaver: PhotoSaverRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ItemDetailsUiState> =
        savedStateHandle.getStateFlow(ItemDetailsDestination.ITEM_ID_ARG, 0)
            .flatMapLatest { itemId ->
                if (itemId > 0) {
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

    suspend fun deleteItem(): Boolean {
        val itemDetails = uiState.value.itemDetails
        val deletionSuccessful = photoSaver.removeFile()
        if (deletionSuccessful && itemDetails != null) {
            itemsRepository.deleteItem(itemDetails.toItem().toItemEntry())
            return true
        }
        else if (deletionSuccessful) {
            return true
        }
        return false
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ItemDetailsUiState(
    val itemDetails: ItemDetails? = null
)
