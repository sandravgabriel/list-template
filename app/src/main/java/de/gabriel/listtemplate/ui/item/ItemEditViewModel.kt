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
        // Versuch, die ID aus NavArgs zu laden (typisch für Compact-Modus)
        val navArgItemId: Int? = savedStateHandle[ItemEditDestination.ITEM_ID_ARG]
        if (navArgItemId != null) {
            initializeWithItemId(navArgItemId)
        } else {
            // Wenn navArgItemId null ist, warten wir möglicherweise auf einen expliziten Aufruf
            // von initializeWithItemId() (z.B. aus dem Screen im Expanded-Modus).

            // Was passiert, wenn navArgItemId null oder 0 ist?
            // Hier könntest du einen Fehler loggen oder einen Default-Zustand setzen.
            // Wenn du hier nichts tust, wird initializeWithItemId vielleicht gar nicht
            // mit einer gültigen ID aufgerufen, oder mit 0, falls das der Default ist.
            Log.e("ItemEditViewModel", "Invalid itemId from NavArgs: $navArgItemId")
            // Ggf. itemUiState auf einen Fehler- oder Leerzustand setzen.
        }
    }

    fun initializeWithItemId(itemId: Int) {
        if (this.currentItemId == itemId && itemUiState.itemDetails.id == itemId && itemUiState.itemDetails.name.isNotBlank()) {
            // Bereits mit dieser ID initialisiert und Daten scheinen geladen
            return
        }
        this.currentItemId = itemId

        viewModelScope.launch {
            val loadedItemEntity = itemsRepository.getItemWithFile(itemId, photoSaver.photoFolder)
                .filterNotNull()
                .firstOrNull()

            if (loadedItemEntity != null) {
                val initialItemDetails = loadedItemEntity.toItemDetails()

                itemUiState = ItemUiState(
                    itemDetails = initialItemDetails,
                    isEntryValid = validateInput(initialItemDetails), // Validiere mit den geladenen Details
                    localPickerPhoto = null //WICHTIG: Explizit auf null setzen für ein bestehendes Item
                )
            } else {
                // Fehlerbehandlung: z.B. itemUiState auf einen Fehlerzustand setzen oder eine Meldung anzeigen
                // Fürs Erste könnten wir einen leeren Zustand setzen oder den alten beibehalten,
                // aber idealerweise sollte der Nutzer informiert werden.
                itemUiState = ItemUiState(isEntryValid = false) // Beispiel für einen leeren/ungültigen Zustand
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
            // Was passiert, wenn die Auswahl abgebrochen wird oder kein Foto gewählt wird?
            itemUiState = itemUiState.copy(localPickerPhoto = null)
        }
    }

    suspend fun updateItem() {
        if (validateInput(itemUiState.itemDetails) && currentItemId != null) {
            val fileToSaveWithItem: File?
            if (itemUiState.localPickerPhoto != null) {
                // ... (Logik für neues Bild speichern) ...
                val newSavedFile = photoSaver.savePhoto()
                fileToSaveWithItem =
                    newSavedFile
                        ?: itemUiState.itemDetails.savedPhoto // Fehler beim Speichern, altes Bild
            } else {
                fileToSaveWithItem =
                    itemUiState.itemDetails.savedPhoto // Kein neues Bild, altes Bild
            }

            val detailsForUpdate =
                itemUiState.itemDetails.copy(
                    id = currentItemId!!,
                    savedPhoto = fileToSaveWithItem
                )

            // Aktualisiere itemUiState nochmal, damit auch der Screen ggf. den finalen savedPhoto-Status reflektiert
            // und localPickerPhoto zurückgesetzt wird, FALLS ein neues Bild gespeichert wurde.
            itemUiState = itemUiState.copy(
                itemDetails = detailsForUpdate,
                localPickerPhoto = if (itemUiState.localPickerPhoto != null && fileToSaveWithItem != itemUiState.itemDetails.savedPhoto) null else itemUiState.localPickerPhoto
                // Setze localPickerPhoto nur zurück, wenn es gesetzt war UND ein neues Bild erfolgreich gespeichert wurde
                // (erkennbar daran, dass fileToSaveWithItem sich von dem alten itemUiState.itemDetails.savedPhoto unterscheidet)
            )

            // Jetzt das Repository mit den finalen `detailsForUpdate` aufrufen
            itemsRepository.updateItem(detailsForUpdate.toItem().toItemEntry())
        }
    }
}