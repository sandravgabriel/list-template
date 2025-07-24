package de.gabriel.listtemplate.data

import java.io.File

data class Item(
    val id: Int,
    val name: String,
    val image: File? = null
){
    fun toItemEntry(): ItemEntry {
        return ItemEntry(
            id = id,
            name = name,
            imageName = image?.name ?: ""
        )
    }

    companion object {
        fun fromItemEntry(itemEntry: ItemEntry, folderPath:File?): Item {
            return Item(
                id = itemEntry.id,
                name = itemEntry.name,
                image = if (itemEntry.imageName.isNotEmpty()) File(folderPath, itemEntry.imageName) else null
            )
        }
    }
}