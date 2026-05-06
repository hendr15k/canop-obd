package com.canopobd.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_items")
    fun getAll(): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenance_items")
    suspend fun getAllOnce(): List<MaintenanceEntity>

    @Query("SELECT * FROM maintenance_items WHERE type = :type")
    suspend fun getByType(type: String): MaintenanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MaintenanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MaintenanceEntity>)

    @Query("DELETE FROM maintenance_items")
    suspend fun deleteAll()
}

@Dao
interface AlertConfigDao {
    @Query("SELECT * FROM alert_config WHERE id = 1")
    fun get(): Flow<AlertConfigEntity?>

    @Query("SELECT * FROM alert_config WHERE id = 1")
    suspend fun getOnce(): AlertConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: AlertConfigEntity)

    @Query("DELETE FROM alert_config")
    suspend fun deleteAll()
}

@Dao
interface ShiftLightConfigDao {
    @Query("SELECT * FROM shift_light_config WHERE id = 1")
    fun get(): Flow<ShiftLightConfigEntity?>

    @Query("SELECT * FROM shift_light_config WHERE id = 1")
    suspend fun getOnce(): ShiftLightConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ShiftLightConfigEntity)
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAll(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    suspend fun getAllOnce(): List<TripEntity>

    @Query("SELECT * FROM trips ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<TripEntity>

    @Insert
    suspend fun insert(trip: TripEntity): Long

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM trips")
    suspend fun deleteAll()
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun get(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getOnce(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettingsEntity)
}
