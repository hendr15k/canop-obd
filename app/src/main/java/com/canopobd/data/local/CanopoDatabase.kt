package com.canopobd.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MaintenanceEntity::class,
        AlertConfigEntity::class,
        ShiftLightConfigEntity::class,
        TripEntity::class,
        AppSettingsEntity::class,
        TripLocationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CanopoDatabase : RoomDatabase() {
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun alertConfigDao(): AlertConfigDao
    abstract fun shiftLightConfigDao(): ShiftLightConfigDao
    abstract fun tripDao(): TripDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun tripLocationDao(): TripLocationDao

    companion object {
        @Volatile
        private var INSTANCE: CanopoDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS trip_locations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        altitude REAL NOT NULL,
                        speed REAL NOT NULL,
                        bearing REAL NOT NULL,
                        accuracy REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY (tripId) REFERENCES trips(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_trip_locations_tripId ON trip_locations(tripId)")
            }
        }

        fun getInstance(context: Context): CanopoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CanopoDatabase::class.java,
                    "canopo_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
