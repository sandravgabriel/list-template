package de.gabriel.listtemplate.ui.item

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gabriel.listtemplate.data.Item
import de.gabriel.listtemplate.data.ItemsRepository
import de.gabriel.listtemplate.data.PhotoSaverRepository
import kotlinx.coroutines.launch
import java.io.File

class ItemEntryViewModel(
    private val itemsRepository: ItemsRepository,
    private val photoSaver: PhotoSaverRepository,
) : ViewModel() {

    var itemUiState by mutableStateOf(ItemUiState())
        private set

    fun updateUiState(newItemDetails: ItemDetails) {
        itemUiState = itemUiState.copy(
            itemDetails = newItemDetails,
            isEntryValid = validateInput(newItemDetails)
        )
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
        } else {
            // Was passiert, wenn die Auswahl abgebrochen wird oder kein Foto gewählt wird?
            itemUiState = itemUiState.copy(localPickerPhoto = null)
        }
    }

    suspend fun saveItem() {
        if (validateInput()) {
            val savedFile: File? = photoSaver.savePhoto()
            refreshSavedPhoto(savedFile)
            itemsRepository.insertItem(itemUiState.itemDetails.toItem().toItemEntry())
        }
    }
}

data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false,
    val localPickerPhoto: Uri? = null
)

data class ItemDetails(
    val id: Int = 0,
    val name: String = "",
    val savedPhoto: File? = null,
)

/**
 * Extension function to convert [ItemUiState] to [Item]
 */
fun ItemDetails.toItem(): Item = Item(
    id = id,
    name = name,
    image = savedPhoto
)

/**
 * Extension function to convert [Item] to [ItemUiState]
 */
fun Item.toItemUiState(isEntryValid: Boolean = false): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    isEntryValid = isEntryValid,
    localPickerPhoto = this.toItemDetails().savedPhoto?.let { Uri.fromFile(it) }
)

/**
 * Extension function to convert [Item] to [ItemDetails]
 */
fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    name = name,
    savedPhoto = image
)
