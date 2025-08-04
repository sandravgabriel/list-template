package de.gabriel.listtemplate.ui.item

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gabriel.listtemplate.data.ItemsRepository
import de.gabriel.listtemplate.data.PhotoSaverRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

class ItemEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
    private val photoSaver: PhotoSaverRepository
) : ViewModel() {

    var itemUiState by mutableStateOf(ItemUiState())
        private set

    private var currentItemId: Int? = null

    init {
        // Versuch, die ID aus NavArgs zu laden (typisch für Compact-Modus)
        val navArgItemId: Int? = savedStateHandle[ItemEditDestination.ITEM_ID_ARG]
        if (navArgItemId != null) {
            initializeWithItemId(navArgItemId)
        }
        // Wenn navArgItemId null ist, warten wir möglicherweise auf einen expliziten Aufruf
        // von initializeWithItemId() (z.B. aus dem Screen im Expanded-Modus).
    }

    fun initializeWithItemId(itemId: Int) {
        if (this.currentItemId == itemId && itemUiState.itemDetails.id == itemId) {
            // Bereits mit dieser ID initialisiert und Daten sind geladen
            return
        }
        this.currentItemId = itemId
        viewModelScope.launch {
            itemUiState = itemsRepository.getItemWithFile(itemId, photoSaver.photoFolder)
                .filterNotNull()
                .first()
                .toItemUiState(true)
        }
    }

    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(itemDetails = itemDetails, isEntryValid = validateInput(itemDetails))
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            name.isNotBlank()
        }
    }

    fun refreshSavedPhoto(photo: File?) {
        val itemDetails = itemUiState.itemDetails.copy(savedPhoto = photo)
        itemUiState = itemUiState.copy(itemDetails = itemDetails)
    }

    fun onPhotoPickerSelect(photo: Uri?) {
        if (photo != null) {
            viewModelScope.launch {
                photoSaver.cacheFromUri(photo)
                itemUiState = itemUiState.copy(localPickerPhoto = photo)
            }
        }
    }

    suspend fun updateItem() {
        if (validateInput(itemUiState.itemDetails) && currentItemId != null) {
            val savedFile: File? = photoSaver.savePhoto()
            refreshSavedPhoto(savedFile)
            val itemToUpdate = itemUiState.itemDetails.copy(id = currentItemId!!)
            itemsRepository.updateItem(itemToUpdate.toItem().toItemEntry())
        }
    }
}