package de.gabriel.template.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update
import androidx.room.Delete

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Query("SELECT * from item WHERE id = :id")
    fun getItem(id: Int): Flow<Item?>

    @Query("SELECT * from item ORDER BY name ASC")
    fun getAllItems(): Flow<List<Item>>

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)
}