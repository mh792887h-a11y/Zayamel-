package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CategoryEntity::class, ZamilEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ZawamilDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun zamilDao(): ZamilDao

    companion object {
        @Volatile
        private var INSTANCE: ZawamilDatabase? = null

        fun getDatabase(context: Context): ZawamilDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZawamilDatabase::class.java,
                    "zawamil_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
