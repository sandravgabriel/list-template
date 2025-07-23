package de.gabriel.template.data

import kotlinx.coroutines.flow.Flow
import java.io.File

class OfflineItemsRepository(private val itemDao: ItemDao) : ItemsRepository {

    override fun getItemStream(id: Int): Flow<ItemEntry?> = itemDao.getItem(id)

    override suspend fun getItemWithFile(id: Int, file: File): Flow<Item?> = itemDao.getItemWithFile(id, file)

    override suspend fun getAllItemsStream(file: File): Flow<List<Item>> = itemDao.getAllItemsWithFiles(file)

    override suspend fun insertItem(item: ItemEntry) = itemDao.insert(item)

    override suspend fun deleteItem(item: ItemEntry) = itemDao.delete(item)

    override suspend fun updateItem(item: ItemEntry) = itemDao.update(item)
}
