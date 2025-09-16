package de.gabriel.listtemplate.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gabriel.listtemplate.data.ItemsRepository
import de.gabriel.listtemplate.data.PhotoSaverRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ItemDetailsViewModel(
    private val itemsRepository: ItemsRepository,
    private val photoSaver: PhotoSaverRepository
) : ViewModel() {

    private val _currentDisplayItemId = MutableStateFlow<Int?>(null)

    fun loadItemDetailsForId(itemId: Int?) {
        _currentDisplayItemId.value = itemId
    }

    fun clearItemDetails() {
        _currentDisplayItemId.value = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ItemDetailsUiState> =
        _currentDisplayItemId.flatMapLatest { itemId ->
            if (itemId != null && itemId > 0) {
                itemsRepository.getItemWithFile(itemId, photoSaver.photoFolder)
                    .map { item -> ItemDetailsUiState(itemDetails = item?.toItemDetails()) }
            } else {
                flowOf(ItemDetailsUiState())
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
