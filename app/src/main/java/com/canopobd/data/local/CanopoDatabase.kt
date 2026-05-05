package com.canopobd.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MaintenanceEntity::class,
        AlertConfigEntity::class,
        ShiftLightConfigEntity::class,
        TripEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CanopoDatabase : RoomDatabase() {
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun alertConfigDao(): AlertConfigDao
    abstract fun shiftLightConfigDao(): ShiftLightConfigDao
    abstract fun tripDao(): TripDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: CanopoDatabase? = null

        fun getInstance(context: Context): CanopoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CanopoDatabase::class.java,
                    "canopo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
