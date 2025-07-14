package de.gabriel.template.data

import kotlinx.coroutines.flow.Flow

interface ItemsRepository{

    fun getItemStream(id: Int): Flow<Item?>

    fun getAllItemsStream(): Flow<List<Item>>

    suspend fun insertItem(item: Item)

    suspend fun updateItem(item: Item)

    suspend fun deleteItem(item: Item)
}
