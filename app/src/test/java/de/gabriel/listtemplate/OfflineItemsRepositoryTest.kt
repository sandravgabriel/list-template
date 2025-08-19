package de.gabriel.listtemplate

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.gabriel.listtemplate.data.Item
import de.gabriel.listtemplate.data.ItemDao
import de.gabriel.listtemplate.data.ItemEntry
import de.gabriel.listtemplate.data.OfflineItemsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import io.mockk.*
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File
import kotlin.intArrayOf

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class OfflineItemsRepositoryTest {

    private lateinit var itemDao: ItemDao
    private lateinit var repository: OfflineItemsRepository
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockFile: File

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        itemDao = mockk()
        repository = OfflineItemsRepository(itemDao)
        mockFile = mockk<File>(relaxed = true) // relaxed, damit z.B. file.name nicht gemockt werden muss, wenn nicht direkt gebraucht
        // every { mockFile.name } returns "test.txt" // Wenn der Name gebraucht wird
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    // --- Tests für getItemWithFile ---

    @Test
    fun `getItemWithFile returns item when dao provides entry`() = runTest(testDispatcher) {
        // Given
        val testId = 1
        val dummyEntry = ItemEntry(id = testId, name = "Test Item", "")
        val expectedItem = Item.fromItemEntry(dummyEntry, mockFile) // Berechne das erwartete Item

        every { itemDao.getItem(testId) } returns flowOf(dummyEntry)

        // When
        val resultFlow = repository.getItemWithFile(testId, mockFile)
        val resultItem = resultFlow.first()

        // Then
        assertNotNull(resultItem)
        assertEquals(expectedItem.id, resultItem!!.id)
        assertEquals(expectedItem.name, resultItem.name)
        Assert.assertEquals(expectedItem.image, resultItem.image)
        // ... weitere Assertions für die Eigenschaften des Items ...

        verify(exactly = 1) { itemDao.getItem(testId) }
    }

    @Test
    fun `getItemWithFile returns null when dao provides null`() = runTest(testDispatcher) {
        // Given
        val testId = 1
        every { itemDao.getItem(testId) } returns flowOf(null)

        // When
        val resultFlow = repository.getItemWithFile(testId, mockFile)
        val resultItem = resultFlow.first()

        // Then
        assertNull(resultItem)
        verify(exactly = 1) { itemDao.getItem(testId) }
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getItemWithFile   File Read Permission Denied`() {
        // Test getItemWithFile with a valid ID and a File object for which read permissions are denied.
        // Verify Item.fromItemEntry's behavior in this scenario.
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getItemWithFile   itemDao getItem throws exception`() {
        // Mock itemDao.getItem to throw an exception.
        // Verify that getItemWithFile propagates the exception or handles it as expected (e.g., emits an error Flow).
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getItemWithFile   Item fromItemEntry throws exception`() {
        // Mock Item.fromItemEntry to throw an exception when called.
        // Verify that the Flow pipeline handles this (e.g., emits an error or null if the exception is caught and handled within the map).
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getItemWithFile   Concurrent calls`() {
        // Test getItemWithFile with multiple concurrent calls for different or same IDs and files.
        // Ensure thread safety and correct data retrieval for each call.
    }

    // --- Tests für getAllItemsWithFiles ---

    @Test
    fun `getAllItemsWithFiles returns list of items when dao provides entries`() = runTest(testDispatcher) {
        // Given
        val entry1 = ItemEntry(id = 1, name = "Item One", "")
        val entry2 = ItemEntry(id = 2, name = "Item Two", "")
        val daoEntries = listOf(entry1, entry2)

        val expectedItem1 = Item.fromItemEntry(entry1, mockFile)
        val expectedItem2 = Item.fromItemEntry(entry2, mockFile)

        every { itemDao.getAllItems() } returns flowOf(daoEntries)
        // Optional: Mocking für Item.fromItemEntry, wenn nötig

        // When
        val resultFlow = repository.getAllItemsWithFiles(mockFile)
        val resultList = resultFlow.first()

        // Then
        assertNotNull(resultList)
        assertEquals(2, resultList.size)
        // Vergleiche die Elemente genauer, z.B. über IDs oder Inhalte
        assertTrue(resultList.any { it.id == expectedItem1.id && it.name == expectedItem1.name })
        assertTrue(resultList.any { it.id == expectedItem2.id && it.name == expectedItem2.name })
        // Oder, wenn die Reihenfolge garantiert ist:
        // assertEquals(expectedItem1, resultList[0]) // Benötigt korrekte equals/hashCode in Item
        // assertEquals(expectedItem2, resultList[1])

        verify(exactly = 1) { itemDao.getAllItems() }
        // Optional: Verifiziere Aufrufe von Item.fromItemEntry für jedes Element
        // verify(exactly = 1) { Item.fromItemEntry(entry1, mockFile) }
        // verify(exactly = 1) { Item.fromItemEntry(entry2, mockFile) }
    }

    @Test
    fun `getAllItemsWithFiles returns empty list when dao provides empty list`() = runTest(testDispatcher) {
        // Given
        every { itemDao.getAllItems() } returns flowOf(emptyList())

        // When
        val resultFlow = repository.getAllItemsWithFiles(mockFile)
        val resultList = resultFlow.first()

        // Then
        assertNotNull(resultList)
        assertTrue(resultList.isEmpty())
        verify(exactly = 1) { itemDao.getAllItems() }
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getAllItemsWithFiles   File Read Permission Denied`() {
        // Test getAllItemsWithFiles with a File object for which read permissions are denied.
        // Verify Item.fromItemEntry's behavior for each item.
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getAllItemsWithFiles   itemDao getAllItems throws exception`() {
        // Mock itemDao.getAllItems to throw an exception.
        // Verify that getAllItemsWithFiles propagates the exception or handles it (e.g., emits an error Flow).
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getAllItemsWithFiles   Item fromItemEntry throws exception for one item`() {
        // Mock Item.fromItemEntry to throw an exception for one of the items during mapping.
        // Verify how the overall Flow behaves (e.g., does it emit partial results, an error, or filter out the problematic item?).
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getAllItemsWithFiles   Item fromItemEntry throws exception for all items`() {
        // Mock Item.fromItemEntry to throw an exception for all items during mapping.
        // Verify the behavior, expecting an error Flow or an empty list if exceptions are caught and items skipped.
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getAllItemsWithFiles   Concurrent calls`() {
        // Test getAllItemsWithFiles with multiple concurrent calls using different or same files.
        // Ensure thread safety and correct data retrieval.
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getItemWithFile   Flow cancellation`() {
        // Test that if the collector cancels the Flow returned by getItemWithFile, underlying operations (like database access) are properly cancelled if possible.
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getAllItemsWithFiles   Flow cancellation`() {
        // Test that if the collector cancels the Flow returned by getAllItemsWithFiles, underlying operations are properly cancelled.
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getItemWithFile   Large File Object  Metaphorical `() {
        // While the File object itself isn't large, test with a file path that might be unusually long or contain special characters, if Item.fromItemEntry interacts with the path string itself.
    }

    @Ignore("TODO: to be optionally implemented")
    @Test
    fun `getAllItemsWithFiles   Large Number of Items`() {
        // Test getAllItemsWithFiles with a very large number of items in the database to check for performance implications of mapping all items.
    }

}