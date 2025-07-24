package de.gabriel.listtemplate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item")
class ItemEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val imageName: String
)
