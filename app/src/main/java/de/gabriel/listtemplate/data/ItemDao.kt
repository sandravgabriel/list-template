package de.gabriel.listtemplate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.map
import java.io.File

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ItemEntry)

    @Query("SELECT id, name, imageName FROM item WHERE id = :id")
    fun getItem(id: Int): Flow<ItemEntry?>

    suspend fun getItemWithFile(id: Int, file: File): Flow<Item?> {
        return getItem(id).map { itemEntry -> // 'itemEntry' ist hier vom Typ ItemEntry? (nullable)
            itemEntry?.let { // Nur fortfahren, wenn itemEntry nicht null ist
                Item.fromItemEntry(it, file) // 'it' ist hier vom Typ ItemEntry (non-nullable)
            }
            // Wenn itemEntry null war, wird der 'map'-Operator implizit 'null' für diese Emission weitergeben.
        }
    }

    @Query("SELECT id, name, imageName FROM item ORDER BY name ASC")
    fun getAllItems(): Flow<List<ItemEntry>>

    /**
     * This function first fetches all `ItemEntry` objects from the database. Then, for each
     * `ItemEntry`, it converts it into an `Item` object using `Item.fromItemEntry`,
     * passing the provided `file` to establish the association.
     */
    suspend fun getAllItemsWithFiles(file: File): Flow<List<Item>> {
        return getAllItems().map { itemEntries ->
            itemEntries.map { itemEntry -> Item.fromItemEntry(itemEntry, file) }
        }
    }

    @Update
    suspend fun update(item: ItemEntry) //TODO?

    @Delete
    suspend fun delete(item: ItemEntry)
}