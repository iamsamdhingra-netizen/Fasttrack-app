package com.local.fasttrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FastSession::class, WeightEntry::class, WaterEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fastSessionDao(): FastSessionDao
    abstract fun weightDao(): WeightDao
    abstract fun waterDao(): WaterDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Everything lives in this on-device SQLite file at
        // /data/data/com.local.fasttrack/databases/fasttrack.db
        // Nothing is ever uploaded anywhere.
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fasttrack.db"
                ).build().also { INSTANCE = it }
            }
    }
}
