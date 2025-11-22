package de.gabriel.listtemplate.ui.item

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gabriel.listtemplate.data.ItemsRepository
import de.gabriel.listtemplate.data.PhotoSaverRepository
import kotlinx.coroutines.flow.filterNotNull
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
        val navArgItemId: Int? = savedStateHandle[ItemEditDestination.ITEM_ID_ARG]
        if (navArgItemId != null && navArgItemId > 0) {
            initializeWithItemId(navArgItemId)
        } else {
            // Wenn keine gültige ID aus NavArgs kommt, wird das ViewModel möglicherweise
            // später durch einen direkten Aufruf von initializeWithItemId() (z.B. vom Pane)
            // initialisiert. Wenn nicht, bleibt der itemUiState leer/ungültig.
            Log.d("ItemEditViewModel", "Keine gültige itemId in NavArgs beim Start.")
        }
    }

    fun initializeWithItemId(itemId: Int) {
        // Verhindere Neuladen, wenn bereits mit derselben ID initialisiert und Daten scheinbar geladen sind.
        if (this.currentItemId == itemId && itemUiState.itemDetails.id == itemId && itemUiState.itemDetails.name.isNotBlank()) {
            Log.d("ItemEditViewModel", "Bereits mit itemId $itemId initialisiert und Daten vorhanden.")
            return
        }
        this.currentItemId = itemId
        Log.d("ItemEditViewModel", "Initialisiere mit itemId: $itemId")

        viewModelScope.launch {
            // Lade das Item mit der neuen ID
            val loadedItemEntity = itemsRepository.getItemWithFile(itemId, photoSaver.photoFolder)
                .filterNotNull()
                .firstOrNull()

            if (loadedItemEntity != null) {
                val initialItemDetails = loadedItemEntity.toItemDetails()
                itemUiState = ItemUiState(
                    itemDetails = initialItemDetails,
                    isEntryValid = validateInput(initialItemDetails),
                    localPickerPhoto = null // Für ein bestehendes Item explizit zurücksetzen
                )
                Log.d("ItemEditViewModel", "Item $itemId geladen: ${initialItemDetails.name}")
            } else {
                Log.e("ItemEditViewModel", "Konnte Item mit ID $itemId nicht laden.")
                itemUiState = ItemUiState(isEntryValid = false) // Setze auf leeren/ungültigen Zustand
                currentItemId = null // Setze currentItemId zurück, da das Laden fehlschlug
            }
        }
    }

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

    fun onPhotoPickerSelect(photo: Uri?) {
        if (photo != null) {
            viewModelScope.launch {
                photoSaver.cacheFromUri(photo)
                itemUiState = itemUiState.copy(localPickerPhoto = photo)
            }
        } else {
            itemUiState = itemUiState.copy(localPickerPhoto = null)
        }
    }

    // Gibt true zurück, wenn die Aktualisierung erfolgreich angestoßen wurde, sonst false.
    suspend fun updateItem(): Boolean {
        if (!validateInput(itemUiState.itemDetails)) {
            Log.w("ItemEditViewModel", "Validierung für Update fehlgeschlagen.")
            return false
        }
        val currentId = this.currentItemId // Lokale Kopie für Null-Sicherheit
        if (currentId == null) {
            Log.e("ItemEditViewModel", "Update fehlgeschlagen: currentItemId ist null.")
            return false
        }

        val fileToSaveWithItem: File?
        if (itemUiState.localPickerPhoto != null) {
            val newSavedFile = photoSaver.savePhoto()
            fileToSaveWithItem = newSavedFile ?: itemUiState.itemDetails.savedPhoto
            if (newSavedFile == null) {
                Log.w("ItemEditViewModel", "Neues Foto konnte nicht gespeichert werden, verwende altes Foto.")
            }
        } else {
            fileToSaveWithItem = itemUiState.itemDetails.savedPhoto
        }

        val detailsForUpdate = itemUiState.itemDetails.copy(
            id = currentId, // Stelle sicher, dass die korrekte ID verwendet wird
            savedPhoto = fileToSaveWithItem
        )

        // Aktualisiere itemUiState, um localPickerPhoto ggf. zurückzusetzen
        itemUiState = itemUiState.copy(
            itemDetails = detailsForUpdate,
            localPickerPhoto = if (itemUiState.localPickerPhoto != null && fileToSaveWithItem != itemUiState.itemDetails.savedPhoto) null else itemUiState.localPickerPhoto
        )

        itemsRepository.updateItem(detailsForUpdate.toItem().toItemEntry())
        Log.d("ItemEditViewModel", "Item ${detailsForUpdate.id} Aktualisierung angestoßen.")
        return true
    }
}