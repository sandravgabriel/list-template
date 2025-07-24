package de.gabriel.listtemplate.data

import kotlinx.coroutines.flow.Flow
import java.io.File

interface ItemsRepository{

    fun getItemStream(id: Int): Flow<ItemEntry?>

    suspend fun getItemWithFile(id: Int, file: File): Flow<Item?>

    suspend fun getAllItemsStream(file: File): Flow<List<Item>>

    suspend fun insertItem(item: ItemEntry)

    suspend fun updateItem(item: ItemEntry)

    suspend fun deleteItem(item: ItemEntry)
}
