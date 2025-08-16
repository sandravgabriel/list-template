package de.gabriel.listtemplate

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.gabriel.listtemplate.data.ItemDao
import de.gabriel.listtemplate.data.ItemEntry
import de.gabriel.listtemplate.data.TemplateDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {

    private lateinit var itemDao: ItemDao
    private lateinit var templateDatabase: TemplateDatabase
    private val item1 = ItemEntry(1, "Apple", "")
    private val item2 = ItemEntry(2, "Banana", "")

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        templateDatabase = Room.inMemoryDatabaseBuilder(context, TemplateDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        itemDao = templateDatabase.itemDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        templateDatabase.close()
    }

    @Test
    @Throws(Exception::class)
    fun insert_insertsItemIntoDB() = runBlocking {
        addOneItemToDb()
        val allItems = itemDao.getAllItems().first()
        Assert.assertEquals(allItems[0], item1)
    }

    @Test
    @Throws(Exception::class)
    fun getItem_returnsItemFromDB() = runBlocking {
        addOneItemToDb()
        val itemEntry = itemDao.getItem(1)
        Assert.assertEquals(itemEntry.first(), item1)
    }

    @Test
    fun getItem_nonExistingItem() = runBlocking {
        // Verify that attempting to retrieve a non-existent ItemEntry by ID returns a Flow emitting null.
        val itemEntry = itemDao.getItem(3).first() // ID 3 does not exist
        Assert.assertNull(itemEntry)
    }

    @Test
    @Throws(Exception::class)
    fun getAllItems_returnsAllItemsFromDB() = runBlocking {
        addTwoItemsToDb()
        val allItems = itemDao.getAllItems().first()
        Assert.assertEquals(allItems[0], item1)
        Assert.assertEquals(allItems[1], item2)
    }

    @Test
    @Throws(Exception::class)
    fun update_updatesItemsInDB() = runBlocking {
        addTwoItemsToDb()
        itemDao.update(ItemEntry(1, "Apple123", ""))
        itemDao.update(ItemEntry(2, "Banana123", ""))

        val allItems = itemDao.getAllItems().first()
        Assert.assertEquals(allItems[0], ItemEntry(1, "Apple123", ""))
        Assert.assertEquals(allItems[1], ItemEntry(2, "Banana123", ""))
    }

    @Test
    fun update_nonExistingItem() = runBlocking {
        // Verify that attempting to update an ItemEntry that does not exist in the database has no effect (Room's default behavior for @Update).
        addOneItemToDb()
        val initialItems = itemDao.getAllItems().first()

        val nonExistingItemEntry = ItemEntry(99, "NonExistent", "")
        itemDao.update(nonExistingItemEntry)

        val itemsAfterUpdateAttempt = itemDao.getAllItems().first()
        Assert.assertEquals(initialItems, itemsAfterUpdateAttempt)
    }

    @Test
    @Throws(Exception::class)
    fun delete_deletesAllItemsFromDB() = runBlocking {
        addTwoItemsToDb()
        itemDao.delete(item1)
        itemDao.delete(item2)
        val allItems = itemDao.getAllItems().first()
        Assert.assertTrue(allItems.isEmpty())
    }

    @Test
    fun delete_nonExistingItem_noChangeInDb() = runBlocking {
        // Verify that attempting to delete an ItemEntry that does not exist has no effect (Room's default behavior for @Delete).
        addOneItemToDb()
        val initialItems = itemDao.getAllItems().first()

        val nonExistingItemEntry = ItemEntry(99, "NonExistent", "")
        itemDao.delete(nonExistingItemEntry)

        val itemsAfterDeleteAttempt = itemDao.getAllItems().first()

        Assert.assertEquals(initialItems, itemsAfterDeleteAttempt)
    }

    private suspend fun addOneItemToDb() {
        itemDao.insert(item1)
    }

    private suspend fun addTwoItemsToDb() {
        itemDao.insert(item1)
        itemDao.insert(item2)
    }
}