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

    @Query("SELECT id, name, imageName FROM item ORDER BY name ASC")
    fun getAllItems(): Flow<List<ItemEntry>>

    @Update
    suspend fun update(item: ItemEntry)

    @Delete
    suspend fun delete(item: ItemEntry)
}