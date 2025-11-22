package de.gabriel.listtemplate.ui.item

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import de.gabriel.listtemplate.data.Item
import de.gabriel.listtemplate.data.ItemEntry
import de.gabriel.listtemplate.data.ItemsRepository
import de.gabriel.listtemplate.data.PhotoSaverRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ItemEditViewModelTest {

    private lateinit var viewModel: ItemEditViewModel
    private lateinit var itemsRepository: ItemsRepository
    private lateinit var photoSaverRepository: PhotoSaverRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var logMock: MockedStatic<Log>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        itemsRepository = mock()
        photoSaverRepository = mock()
        savedStateHandle = SavedStateHandle()
        logMock = Mockito.mockStatic(Log::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        logMock.close()
    }

    private fun initializeViewModel() {
        viewModel = ItemEditViewModel(savedStateHandle, itemsRepository, photoSaverRepository)
    }

    @Test
    fun `uiState should be initialized correctly when item is loaded`() = runTest {
        val item = Item.fromItemEntry(ItemEntry(1, "Item 1", ""), null)
        savedStateHandle[ItemEditDestination.ITEM_ID_ARG] = 1
        whenever(itemsRepository.getItemWithFile(1, photoSaverRepository.photoFolder)).thenReturn(flowOf(item))

        initializeViewModel()
        advanceUntilIdle() // Wait for the init coroutine to finish

        val uiState = viewModel.itemUiState
        assertEquals(item.toItemDetails(), uiState.itemDetails)
        assertTrue(uiState.isEntryValid)
    }

    @Test
    fun `updateUiState should update itemDetails and validation`() {
        initializeViewModel()
        val newItemDetails = ItemDetails(id = 1, name = "Updated Item")
        viewModel.updateUiState(newItemDetails)

        val updatedState = viewModel.itemUiState
        assertEquals(newItemDetails, updatedState.itemDetails)
        assertTrue(updatedState.isEntryValid)
    }

    @Test
    fun `updateUiState should handle invalid input`() {
        initializeViewModel()
        val invalidItemDetails = ItemDetails(id = 1, name = "")
        viewModel.updateUiState(invalidItemDetails)

        val updatedState = viewModel.itemUiState
        assertEquals(invalidItemDetails, updatedState.itemDetails)
        assertFalse(updatedState.isEntryValid)
    }

    @Test
    fun `updateItem should call repository when input is valid`() = runTest {
        val item = Item.fromItemEntry(ItemEntry(1, "Item 1", ""), null)
        savedStateHandle[ItemEditDestination.ITEM_ID_ARG] = 1
        whenever(itemsRepository.getItemWithFile(1, photoSaverRepository.photoFolder)).thenReturn(flowOf(item))
        initializeViewModel()
        advanceUntilIdle() // Wait for the init coroutine to finish

        val updatedDetails = viewModel.itemUiState.itemDetails.copy(name = "Updated Name")
        viewModel.updateUiState(updatedDetails)

        val result = viewModel.updateItem()

        assertTrue(result)
        verify(itemsRepository).updateItem(updatedDetails.toItem().toItemEntry())
    }

    @Test
    fun `updateItem should not call repository when input is invalid`() = runTest {
        val item = Item.fromItemEntry(ItemEntry(1, "Item 1", ""), null)
        savedStateHandle[ItemEditDestination.ITEM_ID_ARG] = 1
        whenever(itemsRepository.getItemWithFile(1, photoSaverRepository.photoFolder)).thenReturn(flowOf(item))
        initializeViewModel()
        advanceUntilIdle() // Wait for the init coroutine to finish

        val invalidDetails = viewModel.itemUiState.itemDetails.copy(name = "")
        viewModel.updateUiState(invalidDetails)

        val result = viewModel.updateItem()

        assertFalse(result)
        verify(itemsRepository, never()).updateItem(invalidDetails.toItem().toItemEntry())
    }
}