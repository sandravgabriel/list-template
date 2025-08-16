package de.gabriel.listtemplate.data

import kotlinx.coroutines.flow.Flow
import java.io.File

interface ItemsRepository{

    suspend fun getItemWithFile(id: Int, file: File): Flow<Item?>

    suspend fun getAllItemsWithFiles(file: File): Flow<List<Item>>

    suspend fun insertItem(item: ItemEntry)

    suspend fun updateItem(item: ItemEntry)

    suspend fun deleteItem(item: ItemEntry)
}
