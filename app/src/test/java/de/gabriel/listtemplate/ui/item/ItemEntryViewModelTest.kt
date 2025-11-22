package de.gabriel.listtemplate.ui.item

import de.gabriel.listtemplate.data.ItemsRepository
import de.gabriel.listtemplate.data.PhotoSaverRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ItemEntryViewModelTest {

    private lateinit var viewModel: ItemEntryViewModel
    private lateinit var itemsRepository: ItemsRepository
    private lateinit var photoSaverRepository: PhotoSaverRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        itemsRepository = mock()
        photoSaverRepository = mock()
        viewModel = ItemEntryViewModel(itemsRepository, photoSaverRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should be initialized with empty itemDetails`() {
        val initialState = viewModel.itemUiState
        assertEquals(ItemDetails(), initialState.itemDetails)
        assertFalse(initialState.isEntryValid)
    }

    @Test
    fun `updateUiState should update itemDetails and validation`() {
        val newItemDetails = ItemDetails(name = "New Item")
        viewModel.updateUiState(newItemDetails)

        val updatedState = viewModel.itemUiState
        assertEquals(newItemDetails, updatedState.itemDetails)
        assertTrue(updatedState.isEntryValid)
    }

    @Test
    fun `updateUiState should handle invalid input`() {
        val invalidItemDetails = ItemDetails(name = "")
        viewModel.updateUiState(invalidItemDetails)

        val updatedState = viewModel.itemUiState
        assertEquals(invalidItemDetails, updatedState.itemDetails)
        assertFalse(updatedState.isEntryValid)
    }

    @Test
    fun `saveItem should call repository when input is valid`() = runTest {
        val validItemDetails = ItemDetails(name = "Valid Item")
        viewModel.updateUiState(validItemDetails)
        whenever(photoSaverRepository.savePhoto()).thenReturn(null)

        viewModel.saveItem()

        verify(itemsRepository).insertItem(validItemDetails.toItem().toItemEntry())
    }

    @Test
    fun `saveItem should not call repository when input is invalid`() = runTest {
        val invalidItemDetails = ItemDetails(name = "")
        viewModel.updateUiState(invalidItemDetails)

        viewModel.saveItem()

        verify(itemsRepository, org.mockito.kotlin.never()).insertItem(invalidItemDetails.toItem().toItemEntry())
    }
}
