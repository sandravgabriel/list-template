package de.gabriel.template.data

import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Database class with a singleton Instance object.
 */
@Database(entities = [ItemEntry::class], version = 1, exportSchema = false)
abstract class TemplateDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {

        @Volatile
        private var Instance: TemplateDatabase? = null

        fun getDatabase(context: Context): TemplateDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TemplateDatabase::class.java, "item_database")
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}