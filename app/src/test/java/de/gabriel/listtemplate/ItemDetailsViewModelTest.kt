package de.gabriel.listtemplate

import app.cash.turbine.test
import de.gabriel.listtemplate.data.Item
import de.gabriel.listtemplate.data.ItemEntry
import de.gabriel.listtemplate.data.ItemsRepository
import de.gabriel.listtemplate.data.PhotoSaverRepository
import de.gabriel.listtemplate.ui.item.ItemDetailsUiState
import de.gabriel.listtemplate.ui.item.ItemDetailsViewModel
import de.gabriel.listtemplate.ui.item.toItemDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ItemDetailsViewModelTest {

    private lateinit var viewModel: ItemDetailsViewModel
    private lateinit var itemsRepository: ItemsRepository
    private lateinit var photoSaverRepository: PhotoSaverRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        itemsRepository = mock()
        photoSaverRepository = mock()
        viewModel = ItemDetailsViewModel(itemsRepository, photoSaverRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should be initialized with null itemDetails`() = runTest {
        assertNull(viewModel.uiState.value.itemDetails)
    }

    @Test
    fun `uiState should react to data from repository`() = runTest {
        val item = Item.fromItemEntry(ItemEntry(1, "Item 1", "Description 1"), null)
        whenever(itemsRepository.getItemWithFile(1, photoSaverRepository.photoFolder)).doReturn(
            flowOf(
                item
            )
        )

        viewModel.uiState.test {
            //Überspringe den initialen Zustand, bei dem itemDetails noch null ist.
            skipItems(1)

            viewModel.loadItemDetailsForId(1)

            val loadedState = awaitItem()
            assertEquals(item.toItemDetails(), loadedState.itemDetails)

            ensureAllEventsConsumed()
        }
    }


    @Test
    fun `deleteItem should return true when item and file are deleted`() = runTest {
        val item = Item.fromItemEntry(ItemEntry(1, "Item 1", "Description 1"), null)
        whenever(photoSaverRepository.removeFile()).doReturn(true)
        viewModel.uiState.value.itemDetails
        val itemDetails = item.toItemDetails()
        val uiStateWithItem = ItemDetailsUiState(itemDetails)
        val viewModel = ItemDetailsViewModel(itemsRepository, photoSaverRepository)
        val uiStateField = viewModel.javaClass.getDeclaredField("uiState")
        uiStateField.isAccessible = true
        val mockStateFlow = MutableStateFlow(uiStateWithItem)
        uiStateField.set(viewModel, mockStateFlow)


        val result = viewModel.deleteItem()

        assertTrue(result)
    }


    @Test
    fun `deleteItem should return false when file deletion fails`() = runTest {
        val item = Item.fromItemEntry(ItemEntry(1, "Item 1", "Description 1"), null)
        whenever(photoSaverRepository.removeFile()).doReturn(false)
        val itemDetails = item.toItemDetails()
        val uiStateWithItem = ItemDetailsUiState(itemDetails)

        val viewModel = ItemDetailsViewModel(itemsRepository, photoSaverRepository)
        val uiStateField = viewModel.javaClass.getDeclaredField("uiState")
        uiStateField.isAccessible = true
        val mockStateFlow = MutableStateFlow(uiStateWithItem)
        uiStateField.set(viewModel, mockStateFlow)
        val result = viewModel.deleteItem()

        assertFalse(result)
    }
}

