package de.gabriel.template.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
