package de.gabriel.listtemplate.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import kotlin.collections.map

class OfflineItemsRepository(private val itemDao: ItemDao) : ItemsRepository {

    override suspend fun getItemWithFile(id: Int, file: File): Flow<Item?> {
        return itemDao.getItem(id).map { itemEntry ->
            itemEntry?.let {
                Item.fromItemEntry(it, file)
            }
        }
    }

    /**
     * This function first fetches all `ItemEntry` objects from the database. Then, for each
     * `ItemEntry`, it converts it into an `Item` object using `Item.fromItemEntry`,
     * passing the provided `file` to establish the association.
     */
    override suspend fun getAllItemsWithFiles(file: File): Flow<List<Item>> {
        return itemDao.getAllItems().map { itemEntries ->
            itemEntries.map { itemEntry -> Item.fromItemEntry(itemEntry, file) }
        }
    }

    override suspend fun insertItem(item: ItemEntry) = itemDao.insert(item)

    override suspend fun deleteItem(item: ItemEntry) = itemDao.delete(item)

    override suspend fun updateItem(item: ItemEntry) = itemDao.update(item)
}
