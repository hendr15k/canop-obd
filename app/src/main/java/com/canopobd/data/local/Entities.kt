package com.canopobd.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_items")
data class MaintenanceEntity(
    @PrimaryKey val type: String,
    val lastServiceKm: Int,
    val intervalKm: Int,
    val lastServiceDate: Long,
    val notes: String = ""
)

@Entity(tableName = "alert_config")
data class AlertConfigEntity(
    @PrimaryKey val id: Int = 1,
    val speedWarning: Float = 130f,
    val speedWarningEnabled: Boolean = false,
    val coolantWarning: Float = 105f,
    val coolantWarningEnabled: Boolean = true,
    val fuelWarning: Float = 15f,
    val fuelWarningEnabled: Boolean = false,
    val rpmWarning: Float = 6000f,
    val rpmWarningEnabled: Boolean = false,
    val batteryLowWarning: Float = 11.5f,
    val batteryLowWarningEnabled: Boolean = true,
    val boostWarning: Float = 0.85f,
    val boostWarningEnabled: Boolean = false,
    val boostCritical: Float = 1.35f,
    val boostCriticalEnabled: Boolean = true,
    val egtWarning: Float = 850f,
    val egtWarningEnabled: Boolean = true,
    val egtCritical: Float = 950f,
    val egtCriticalEnabled: Boolean = true,
    val oilTempWarning: Float = 120f,
    val oilTempWarningEnabled: Boolean = true,
    val oilTempCritical: Float = 135f,
    val oilTempCriticalEnabled: Boolean = true,
    val turboSpeedWarning: Float = 180000f,
    val turboSpeedWarningEnabled: Boolean = false,
    val chargeAirTempWarning: Float = 65f,
    val chargeAirTempWarningEnabled: Boolean = false,
    val fuelTrimWarning: Float = 15f,
    val fuelTrimWarningEnabled: Boolean = false,
    val soundEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val hysteresisSeconds: Int = 10,
    val cooldownSeconds: Int = 60
)

@Entity(tableName = "shift_light_config")
data class ShiftLightConfigEntity(
    @PrimaryKey val id: Int = 1,
    val enabled: Boolean = false,
    val redlineRpm: Int = 6500,
    val warningRpm: Int = 5500,
    val flashEnabled: Boolean = true,
    val soundEnabled: Boolean = false
)

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val distanceKm: Float,
    val avgSpeedKmh: Float,
    val maxSpeedKmh: Float,
    val avgRpm: Double,
    val maxRpm: Double,
    val fuelUsedLiters: Float,
    val vin: String
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val pollRate: Long = 500L,
    val autoReconnect: Boolean = false,
    val colorTheme: String = "CANOPO",
    val pollMode: String = "NORMAL",
    val primaryGaugeIds: String = "",
    val storedVin: String = "",
    val carProfileId: String = "",
    val currentKm: Int = 0
)

@Entity(tableName = "trip_locations", foreignKeys = [
    ForeignKey(entity = TripEntity::class, parentColumns = ["id"], childColumns = ["tripId"], onDelete = ForeignKey.CASCADE)
], indices = [Index("tripId")])
data class TripLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val timestamp: Long
)
