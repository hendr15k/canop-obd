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
    version = 4,
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE maintenance_items ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alert_config ADD COLUMN boostWarning REAL NOT NULL DEFAULT 0.85")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN boostWarningEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN boostCritical REAL NOT NULL DEFAULT 1.35")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN boostCriticalEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN egtWarning REAL NOT NULL DEFAULT 850")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN egtWarningEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN egtCritical REAL NOT NULL DEFAULT 950")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN egtCriticalEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN oilTempWarning REAL NOT NULL DEFAULT 120")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN oilTempWarningEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN oilTempCritical REAL NOT NULL DEFAULT 135")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN oilTempCriticalEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN turboSpeedWarning REAL NOT NULL DEFAULT 180000")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN turboSpeedWarningEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN chargeAirTempWarning REAL NOT NULL DEFAULT 65")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN chargeAirTempWarningEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN fuelTrimWarning REAL NOT NULL DEFAULT 15")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN fuelTrimWarningEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN soundEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN vibrationEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN hysteresisSeconds INTEGER NOT NULL DEFAULT 10")
                db.execSQL("ALTER TABLE alert_config ADD COLUMN cooldownSeconds INTEGER NOT NULL DEFAULT 60")
            }
        }

        fun getInstance(context: Context): CanopoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CanopoDatabase::class.java,
                    "canopo_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
