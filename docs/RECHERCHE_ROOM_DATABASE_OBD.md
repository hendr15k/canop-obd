# Room Database für persistente OBD-Daten in Android

## Inhaltsverzeichnis

1. [Einführung](#einfuehrung)
2. [Room vs. SQLite direkt](#room-vs-sqlite-direkt)
3. [Datenbankschema für OBD-Trip-Daten](#datenbankschema-fuer-obd-trip-daten)
4. [Entity-Klassen](#entity-klassen)
5. [TypeConverter für komplexe Datentypen](#typeconverter)
6. [DAO-Interfaces](#dao-interfaces)
7. [Datenbank-Klasse](#datenbank-klasse)
8. [Room mit Kotlin Coroutines/Flow](#room-mit-coroutines)
9. [Migration-Strategien](#migration-strategien)
10. [Performance bei vielen Datensätzen](#performance)
11. [Backup und Export](#backup-export)
12. [DAO Pattern für OBD-Repository](#repository-pattern)
13. [Trip-Tracking und Statistiken](#trip-tracking)
14. [Encrypted SharedPreferences vs. Room](#sharedpreferences-vs-room)
15. [Best Practices](#best-practices)
16. [Quellen und Links](#quellen)

---

## 1. Einfuehrung

Room ist eine Abstraktionsschicht über SQLite, die von Google als Teil der Android Architecture Components entwickelt wurde. Room bietet erhebliche Vorteile gegenüber direktem SQLite-Zugriff:

- **Compile-time SQL verification**: SQL-Abfragen werden zur Kompilierzeit geprüft
- **Annotation-basiert**: Minimale Boilerplate-Code durch Annotationen
- **Kotlin Coroutines Support**: Nativer Support für asynchrone Datenbankoperationen
- **LiveData/Flow Integration**: Automatische UI-Updates bei Datenänderungen
- **Migration Support**: Eingebaute Migrationsunterstützung

Für OBD-Tracking-Apps ist Room besonders geeignet, da typische OBD-Daten strukturiert vorliegen (Trips, Fahrzeugdaten, Sensor-Readings) und häufig statistische Auswertungen benötigt werden.

---

## 2. Room vs. SQLite direkt

| Kriterium | Room | SQLite direkt |
|-----------|------|---------------|
| **Code-Aufwand** | Minimal (Annotationen) | Hoch (manuelles SQL) |
| **Type-Safety** | Stark (Kotlin-Typen) | Schwach (Raw Strings) |
| **SQL-Validierung** | Compile-time | Runtime nur |
| **Lebenszyklus-Integration** | LiveData/Flow | Manuell |
| **Migration** | Automatisiert | Manuell |
| **Performance** | Minimaler Overhead | Schnellster Zugriff |
| **Testbarkeit** | Einfach (in-memory DB) | Komplex |
| **Lernkurve** | Moderat | Steil |

**Empfehlung für OBD-Apps**: Room ist die klare Wahl, da:
- OBD-Daten strukturiert und vorhersagbar sind
- Statistiken und Aggregationen häufig benötigt werden
- Die App wartbar bleiben muss
- Der Performance-Unterschied für OBD-Datenmengen irrelevant ist

---

## 3. Datenbankschema für OBD-Trip-Daten

### Konzeptionelles Schema

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│    Vehicle      │       │      Trip       │       │  OBDReading     │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │──┐    │ id (PK)         │──┐    │ id (PK)         │
│ make            │  │    │ vehicleId (FK)  │←─┘    │ tripId (FK)     │
│ model           │  │    │ startTime       │       │ timestamp       │
│ year            │  │    │ endTime         │       │ rpm             │
│ vin             │  │    │ startOdometer   │       │ speed           │
│ licensePlate    │  └───►│ endOdometer     │       │ fuelLevel       │
│ fuelType        │       │ avgSpeed        │       │ coolantTemp     │
│ tankCapacity    │       │ maxSpeed        │       │ throttlePos     │
└─────────────────┘       │ fuelConsumed    │       │ mafRate         │
                          │ avgFuelConsump  │       │ latitude        │
                          │ isCompleted     │       │ longitude       │
                          └─────────────────┘       └─────────────────┘
```

---

## 4. Entity-Klassen

### 4.1 Vehicle Entity

```kotlin
@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "make")
    val make: String,
    
    @ColumnInfo(name = "model")
    val model: String,
    
    @ColumnInfo(name = "year")
    val year: Int?,
    
    @ColumnInfo(name = "vin")
    @Unique
    val vin: String?,
    
    @ColumnInfo(name = "license_plate")
    val licensePlate: String?,
    
    @ColumnInfo(name = "fuel_type")
    val fuelType: FuelType,
    
    @ColumnInfo(name = "tank_capacity_liters")
    val tankCapacityLiters: Float?,
    
    @ColumnInfo(name = "engine_displacement_liters")
    val engineDisplacementLiters: Float?,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

enum class FuelType {
    GASOLINE, DIESEL, ELECTRIC, HYBRID, LPG, CNG, OTHER
}
```

### 4.2 Trip Entity

```kotlin
@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicle_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["vehicle_id"]),
        Index(value = ["start_time"]),
        Index(value = ["is_completed"])
    ]
)
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    
    @ColumnInfo(name = "end_time")
    val endTime: Long?,
    
    @ColumnInfo(name = "start_latitude")
    val startLatitude: Double?,
    
    @ColumnInfo(name = "start_longitude")
    val startLongitude: Double?,
    
    @ColumnInfo(name = "end_latitude")
    val endLatitude: Double?,
    
    @ColumnInfo(name = "end_longitude")
    val endLongitude: Double?,
    
    @ColumnInfo(name = "start_odometer_km")
    val startOdometerKm: Float?,
    
    @ColumnInfo(name = "end_odometer_km")
    val endOdometerKm: Float?,
    
    @ColumnInfo(name = "distance_km")
    val distanceKm: Float = 0f,
    
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long = 0,
    
    @ColumnInfo(name = "avg_speed_kmh")
    val avgSpeedKmh: Float = 0f,
    
    @ColumnInfo(name = "max_speed_kmh")
    val maxSpeedKmh: Float = 0f,
    
    @ColumnInfo(name = "avg_rpm")
    val avgRpm: Float = 0f,
    
    @ColumnInfo(name = "max_rpm")
    val maxRpm: Int = 0,
    
    @ColumnInfo(name = "fuel_consumed_liters")
    val fuelConsumedLiters: Float = 0f,
    
    @ColumnInfo(name = "avg_fuel_consumption_l100km")
    val avgFuelConsumptionL100km: Float = 0f,
    
    @ColumnInfo(name = "fuel_level_start_percent")
    val fuelLevelStartPercent: Float?,
    
    @ColumnInfo(name = "fuel_level_end_percent")
    val fuelLevelEndPercent: Float?,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    
    @ColumnInfo(name = "notes")
    val notes: String?,
    
    @ColumnInfo(name = "tags")
    val tags: String?, // Komma-getrennte Tags
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

### 4.3 OBDReading Entity

```kotlin
@Entity(
    tableName = "obd_readings",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["trip_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["trip_id", "timestamp"])
    ]
)
data class OBDReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "trip_id")
    val tripId: Long,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    // OBD PIDs
    @ColumnInfo(name = "rpm")
    val rpm: Int?,
    
    @ColumnInfo(name = "speed_kmh")
    val speedKmh: Float?,
    
    @ColumnInfo(name = "throttle_position_percent")
    val throttlePositionPercent: Float?,
    
    @ColumnInfo(name = "engine_load_percent")
    val engineLoadPercent: Float?,
    
    @ColumnInfo(name = "coolant_temp_celsius")
    val coolantTempCelsius: Float?,
    
    @ColumnInfo(name = "intake_temp_celsius")
    val intakeTempCelsius: Float?,
    
    @ColumnInfo(name = "maf_rate_gps")
    val mafRateGps: Float?,
    
    @ColumnInfo(name = "fuel_pressure_kpa")
    val fuelPressureKpa: Float?,
    
    @ColumnInfo(name = "fuel_level_percent")
    val fuelLevelPercent: Float?,
    
    // GPS-Daten
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double?,
    
    @ColumnInfo(name = "altitude_meters")
    val altitudeMeters: Float?,
    
    @ColumnInfo(name = "gps_speed_kmh")
    val gpsSpeedKmh: Float?,
    
    @ColumnInfo(name = "data_source")
    val dataSource: DataSource = DataSource.OBD
)

enum class DataSource {
    OBD, GPS, OBD_GPS_FUSED, SIMULATED
}
```

### 4.4 DiagnosticDTC Entity

```kotlin
@Entity(
    tableName = "diagnostic_dtcs",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["trip_id"]),
        Index(value = ["dtc_code"]),
        Index(value = ["is_resolved"])
    ]
)
data class DiagnosticDTC(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "trip_id")
    val tripId: Long,
    
    @ColumnInfo(name = "dtc_code")
    val dtcCode: String,
    
    @ColumnInfo(name = "description")
    val description: String?,
    
    @ColumnInfo(name = "severity")
    val severity: DTCSeverity,
    
    @ColumnInfo(name = "detected_at")
    val detectedAt: Long,
    
    @ColumnInfo(name = "resolved_at")
    val resolvedAt: Long?,
    
    @ColumnInfo(name = "freeze_frame_data")
    val freezeFrameData: String? // JSON
)

enum class DTCSeverity {
    CRITICAL, HIGH, MEDIUM, LOW, INFO
}
```

### 4.5 TripEvent Entity

```kotlin
@Entity(
    tableName = "trip_events",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["trip_id"]),
        Index(value = ["event_type"]),
        Index(value = ["timestamp"])
    ]
)
data class TripEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "trip_id")
    val tripId: Long,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "event_type")
    val eventType: TripEventType,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double?,
    
    @ColumnInfo(name = "speed_kmh")
    val speedKmh: Float?,
    
    @ColumnInfo(name = "severity")
    val severity: EventSeverity = EventSeverity.INFO,
    
    @ColumnInfo(name = "description")
    val description: String?,
    
    @ColumnInfo(name = "metadata")
    val metadata: String? // JSON
)

enum class TripEventType {
    TRIP_START, TRIP_END, HARD_BRAKE, HARD_ACCELERATION, HARSH_CORNERING,
    SPEED_LIMIT_EXCEEDED, HIGH_RPM, DTC_DETECTED, DTC_CLEARED,
    FUEL_STOP, IDLE_START, IDLE_END, GPS_SIGNAL_LOST, GPS_SIGNAL_RESTORED
}

enum class EventSeverity {
    DEBUG, INFO, WARNING, ALERT, CRITICAL
}
```

---

## 5. TypeConverter <a name="typeconverter"></a>

```kotlin
class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromFuelType(fuelType: FuelType): String {
        return fuelType.name
    }
    
    @TypeConverter
    fun toFuelType(value: String): FuelType {
        return FuelType.valueOf(value)
    }
    
    @TypeConverter
    fun fromDataSource(dataSource: DataSource): String {
        return dataSource.name
    }
    
    @TypeConverter
    fun toDataSource(value: String): DataSource {
        return DataSource.valueOf(value)
    }
    
    @TypeConverter
    fun fromDTCSeverity(severity: DTCSeverity): String {
        return severity.name
    }
    
    @TypeConverter
    fun toDTCSeverity(value: String): DTCSeverity {
        return DTCSeverity.valueOf(value)
    }
    
    @TypeConverter
    fun fromEventType(eventType: TripEventType): String {
        return eventType.name
    }
    
    @TypeConverter
    fun toEventType(value: String): TripEventType {
        return TripEventType.valueOf(value)
    }
    
    @TypeConverter
    fun fromEventSeverity(severity: EventSeverity): String {
        return severity.name
    }
    
    @TypeConverter
    fun toEventSeverity(value: String): EventSeverity {
        return EventSeverity.valueOf(value)
    }
    
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString("|||")
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split("|||")?.filter { it.isNotEmpty() }
    }
    
    @TypeConverter
    fun fromJsonObject(value: String?): Map<String, Any>? {
        return value?.let {
            Gson().fromJson(it, object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type)
        }
    }
    
    @TypeConverter
    fun toJsonObject(map: Map<String, Any>?): String? {
        return map?.let { Gson().toJson(it) }
    }
}
```

---

## 6. DAO-Interfaces <a name="dao-interfaces"></a>

### 6.1 VehicleDao

```kotlin
@Dao
interface VehicleDao {
    
    @Query("SELECT * FROM vehicles WHERE is_active = 1 ORDER BY updated_at DESC")
    fun getAllActiveVehicles(): Flow<List<Vehicle>>
    
    @Query("SELECT * FROM vehicles ORDER BY updated_at DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>
    
    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun getVehicleById(id: Long): Flow<Vehicle?>
    
    @Query("SELECT * FROM vehicles WHERE vin = :vin")
    fun getVehicleByVin(vin: String): Flow<Vehicle?>
    
    @Query("SELECT * FROM vehicles WHERE is_active = 1 LIMIT 1")
    fun getActiveVehicle(): Flow<Vehicle?>
    
    @Query("SELECT * FROM vehicles WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveVehicleOnce(): Vehicle?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: Vehicle): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehicles: List<Vehicle>)
    
    @Update
    suspend fun update(vehicle: Vehicle)
    
    @Delete
    suspend fun delete(vehicle: Vehicle)
    
    @Query("UPDATE vehicles SET is_active = 0, updated_at = :timestamp WHERE id = :id")
    suspend fun deactivate(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE vehicles SET is_active = 1, updated_at = :timestamp WHERE id = :id")
    suspend fun activate(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getVehicleCount(): Int
}
```

### 6.2 TripDao

```kotlin
@Dao
interface TripDao {
    
    @Query("SELECT * FROM trips ORDER BY start_time DESC")
    fun getAllTrips(): Flow<List<Trip>>
    
    @Query("SELECT * FROM trips WHERE id = :id")
    fun getTripById(id: Long): Flow<Trip?>
    
    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripByIdOnce(id: Long): Trip?
    
    @Query("SELECT * FROM trips WHERE vehicle_id = :vehicleId ORDER BY start_time DESC")
    fun getTripsByVehicle(vehicleId: Long): Flow<List<Trip>>
    
    @Query("SELECT * FROM trips WHERE is_completed = 1 ORDER BY start_time DESC")
    fun getCompletedTrips(): Flow<List<Trip>>
    
    @Query("SELECT * FROM trips WHERE is_completed = 0 ORDER BY start_time DESC LIMIT 1")
    fun getActiveTrip(): Flow<Trip?>
    
    @Query("SELECT * FROM trips WHERE is_completed = 0 ORDER BY start_time DESC LIMIT 1")
    suspend fun getActiveTripOnce(): Trip?
    
    @Query("SELECT * FROM trips WHERE start_time BETWEEN :startTime AND :endTime ORDER BY start_time DESC")
    fun getTripsInTimeRange(startTime: Long, endTime: Long): Flow<List<Trip>>
    
    @Query("""
        SELECT SUM(distance_km) 
        FROM trips 
        WHERE vehicle_id = :vehicleId AND is_completed = 1
    """)
    fun getTotalDistanceByVehicle(vehicleId: Long): Flow<Float?>
    
    @Query("""
        SELECT SUM(fuel_consumed_liters) 
        FROM trips 
        WHERE vehicle_id = :vehicleId AND is_completed = 1
    """)
    fun getTotalFuelConsumedByVehicle(vehicleId: Long): Flow<Float?>
    
    @Query("""
        SELECT strftime('%Y-%m', datetime(start_time/1000, 'unixepoch')) as month,
               SUM(distance_km) as total_distance,
               SUM(fuel_consumed_liters) as total_fuel,
               AVG(avg_fuel_consumption_l100km) as avg_consumption,
               COUNT(*) as trip_count
        FROM trips
        WHERE vehicle_id = :vehicleId AND is_completed = 1
        GROUP BY month
        ORDER BY month DESC
        LIMIT :limit
    """)
    fun getMonthlyStats(vehicleId: Long, limit: Int = 12): Flow<List<MonthlyStats>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: Trip): Long
    
    @Update
    suspend fun update(trip: Trip)
    
    @Delete
    suspend fun delete(trip: Trip)
    
    @Query("UPDATE trips SET is_completed = 1 WHERE id = :tripId")
    suspend fun completeTrip(tripId: Long)
    
    @Query("""
        UPDATE trips SET 
            end_time = :endTime,
            end_latitude = :endLat,
            end_longitude = :endLng,
            end_odometer_km = :endOdometer,
            distance_km = :distance,
            duration_seconds = :duration,
            avg_speed_kmh = :avgSpeed,
            max_speed_kmh = :maxSpeed,
            fuel_consumed_liters = :fuelConsumed,
            avg_fuel_consumption_l100km = :avgFuelConsumption,
            is_completed = :isCompleted
        WHERE id = :tripId
    """)
    suspend fun updateTripStats(
        tripId: Long, endTime: Long, endLat: Double?, endLng: Double?,
        endOdometer: Float?, distance: Float, duration: Long,
        avgSpeed: Float, maxSpeed: Float, fuelConsumed: Float,
        avgFuelConsumption: Float, isCompleted: Boolean
    )
}

data class MonthlyStats(
    val month: String,
    val totalDistance: Float,
    val totalFuel: Float,
    val avgConsumption: Float,
    val tripCount: Int
)
```

### 6.3 OBDReadingDao

```kotlin
@Dao
interface OBDReadingDao {
    
    @Query("SELECT * FROM obd_readings WHERE trip_id = :tripId ORDER BY timestamp ASC")
    fun getReadingsByTrip(tripId: Long): Flow<List<OBDReading>>
    
    @Query("SELECT * FROM obd_readings WHERE trip_id = :tripId ORDER BY timestamp ASC")
    suspend fun getReadingsByTripOnce(tripId: Long): List<OBDReading>
    
    @Query("SELECT * FROM obd_readings WHERE trip_id = :tripId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReadingByTrip(tripId: Long): Flow<OBDReading?>
    
    @Query("SELECT * FROM obd_readings WHERE trip_id = :tripId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReadingByTripOnce(tripId: Long): OBDReading?
    
    @Query("""
        SELECT * FROM obd_readings 
        WHERE trip_id = :tripId 
          AND timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp ASC
    """)
    fun getReadingsInTimeRange(
        tripId: Long, startTime: Long, endTime: Long
    ): Flow<List<OBDReading>>
    
    @Query("SELECT COUNT(*) FROM obd_readings WHERE trip_id = :tripId")
    fun getReadingCountByTrip(tripId: Long): Flow<Int>
    
    @Query("SELECT AVG(speed_kmh) FROM obd_readings WHERE trip_id = :tripId AND speed_kmh IS NOT NULL")
    fun getAverageSpeedByTrip(tripId: Long): Flow<Float?>
    
    @Query("SELECT MAX(speed_kmh) FROM obd_readings WHERE trip_id = :tripId AND speed_kmh IS NOT NULL")
    fun getMaxSpeedByTrip(tripId: Long): Flow<Float?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: OBDReading): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<OBDReading>)
    
    @Update
    suspend fun update(reading: OBDReading)
    
    @Delete
    suspend fun delete(reading: OBDReading)
    
    @Query("DELETE FROM obd_readings WHERE trip_id = :tripId")
    suspend fun deleteByTrip(tripId: Long)
    
    @Transaction
    suspend fun insertAllWithProgress(
        readings: List<OBDReading>,
        onProgress: (suspend (Int, Int) -> Unit)? = null
    ) {
        val batchSize = 500
        readings.chunked(batchSize).forEachIndexed { index, batch ->
            insertAll(batch)
            onProgress?.invoke(index * batchSize + batch.size, readings.size)
        }
    }
    
    @Query("SELECT * FROM obd_readings WHERE trip_id = :tripId ORDER BY timestamp DESC LIMIT :limit")
    fun getLastNReadings(tripId: Long, limit: Int): Flow<List<OBDReading>>
}
```

### 6.4 DiagnosticDTCDao

```kotlin
@Dao
interface DiagnosticDTCDao {
    
    @Query("SELECT * FROM diagnostic_dtcs ORDER BY detected_at DESC")
    fun getAllDTCs(): Flow<List<DiagnosticDTC>>
    
    @Query("SELECT * FROM diagnostic_dtcs WHERE trip_id = :tripId ORDER BY detected_at DESC")
    fun getDTCsByTrip(tripId: Long): Flow<List<DiagnosticDTC>>
    
    @Query("SELECT * FROM diagnostic_dtcs WHERE resolved_at IS NULL ORDER BY detected_at DESC")
    fun getActiveDTCs(): Flow<List<DiagnosticDTC>>
    
    @Query("SELECT * FROM diagnostic_dtcs WHERE resolved_at IS NULL ORDER BY detected_at DESC")
    suspend fun getActiveDTCsOnce(): List<DiagnosticDTC>
    
    @Query("SELECT * FROM diagnostic_dtcs WHERE dtc_code = :code ORDER BY detected_at DESC LIMIT 1")
    fun getDTCByCode(code: String): Flow<DiagnosticDTC?>
    
    @Query("SELECT COUNT(*) FROM diagnostic_dtcs WHERE resolved_at IS NULL")
    fun getActiveDTCCount(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dtc: DiagnosticDTC): Long
    
    @Update
    suspend fun update(dtc: DiagnosticDTC)
    
    @Query("UPDATE diagnostic_dtcs SET resolved_at = :timestamp WHERE id = :id")
    suspend fun resolve(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun delete(dtc: DiagnosticDTC)
}
```

### 6.5 TripEventDao

```kotlin
@Dao
interface TripEventDao {
    
    @Query("SELECT * FROM trip_events WHERE trip_id = :tripId ORDER BY timestamp ASC")
    fun getEventsByTrip(tripId: Long): Flow<List<TripEvent>>
    
    @Query("SELECT * FROM trip_events WHERE trip_id = :tripId ORDER BY timestamp ASC")
    suspend fun getEventsByTripOnce(tripId: Long): List<TripEvent>
    
    @Query("SELECT * FROM trip_events WHERE trip_id = :tripId AND event_type = :eventType ORDER BY timestamp ASC")
    fun getEventsByTripAndType(tripId: Long, eventType: TripEventType): Flow<List<TripEvent>>
    
    @Query("SELECT COUNT(*) FROM trip_events WHERE trip_id = :tripId AND event_type = :eventType")
    fun getEventCountByTripAndType(tripId: Long, eventType: TripEventType): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: TripEvent): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<TripEvent>)
    
    @Update
    suspend fun update(event: TripEvent)
    
    @Delete
    suspend fun delete(event: TripEvent)
    
    @Query("DELETE FROM trip_events WHERE trip_id = :tripId")
    suspend fun deleteByTrip(tripId: Long)
    
    @Query("""
        SELECT * FROM trip_events 
        WHERE trip_id = :tripId 
          AND event_type IN ('HARD_BRAKE', 'HARD_ACCELERATION', 'HARSH_CORNERING')
        ORDER BY timestamp ASC
    """)
    fun getDrivingEvents(tripId: Long): Flow<List<TripEvent>>
}
```

---

## 7. Datenbank-Klasse <a name="datenbank-klasse"></a>

```kotlin
@Database(
    entities = [
        Vehicle::class,
        Trip::class,
        OBDReading::class,
        DiagnosticDTC::class,
        TripEvent::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class OBDDatabase : RoomDatabase() {
    
    abstract fun vehicleDao(): VehicleDao
    abstract fun tripDao(): TripDao
    abstract fun obdReadingDao(): OBDReadingDao
    abstract fun diagnosticDTCDao(): DiagnosticDTCDao
    abstract fun tripEventDao(): TripEventDao
    
    companion object {
        const val DATABASE_NAME = "obd_database"
        
        @Volatile
        private var INSTANCE: OBDDatabase? = null
        
        fun getInstance(context: Context): OBDDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): OBDDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                OBDDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(DatabaseCallback())
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .build()
        }
        
        fun createInMemoryDatabase(context: Context): OBDDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                OBDDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
    
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
        
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            db.execSQL("PRAGMA journal_mode = WAL")
        }
    }
    
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            clearAllTables()
        }
    }
}
```

---

## 8. Room mit Kotlin Coroutines/Flow <a name="room-mit-coroutines"></a>

### 8.1 Repository

```kotlin
@Singleton
class OBDRepository @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val tripDao: TripDao,
    private val obdReadingDao: OBDReadingDao,
    private val dtcDao: DiagnosticDTCDao,
    private val tripEventDao: TripEventDao
) {
    
    // Vehicle
    fun getAllVehicles(): Flow<List<Vehicle>> = vehicleDao.getAllActiveVehicles()
    fun getVehicleById(id: Long): Flow<Vehicle?> = vehicleDao.getVehicleById(id)
    fun getActiveVehicle(): Flow<Vehicle?> = vehicleDao.getActiveVehicle()
    suspend fun getActiveVehicleOnce(): Vehicle? = vehicleDao.getActiveVehicleOnce()
    
    suspend fun insertVehicle(vehicle: Vehicle): Long = vehicleDao.insert(vehicle)
    suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.update(vehicle)
    suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.delete(vehicle)
    suspend fun setActiveVehicle(id: Long) {
        vehicleDao.getAllVehicles().first().forEach { v ->
            if (v.id == id) vehicleDao.activate(v.id)
            else vehicleDao.deactivate(v.id)
        }
    }
    
    // Trip
    fun getAllTrips(): Flow<List<Trip>> = tripDao.getAllTrips()
    fun getTripsByVehicle(vehicleId: Long): Flow<List<Trip>> = tripDao.getTripsByVehicle(vehicleId)
    fun getActiveTrip(): Flow<Trip?> = tripDao.getActiveTrip()
    fun getTripById(id: Long): Flow<Trip?> = tripDao.getTripById(id)
    suspend fun getTripByIdOnce(id: Long): Trip? = tripDao.getTripByIdOnce(id)
    fun getTripsInTimeRange(startTime: Long, endTime: Long): Flow<List<Trip>> =
        tripDao.getTripsInTimeRange(startTime, endTime)
    
    suspend fun createTrip(vehicleId: Long): Long {
        val trip = Trip(
            vehicleId = vehicleId,
            startTime = System.currentTimeMillis()
        )
        return tripDao.insert(trip)
    }
    
    suspend fun completeTrip(tripId: Long) {
        tripDao.getTripByIdOnce(tripId)?.let { trip ->
            val readings = obdReadingDao.getReadingsByTripOnce(tripId)
            val stats = calculateTripStatistics(readings)
            
            tripDao.updateTripStats(
                tripId = tripId,
                endTime = System.currentTimeMillis(),
                endLat = stats.lastLatitude,
                endLng = stats.lastLongitude,
                endOdometer = stats.lastOdometer,
                distance = stats.distance,
                duration = stats.duration,
                avgSpeed = stats.avgSpeed,
                maxSpeed = stats.maxSpeed,
                fuelConsumed = stats.fuelConsumed,
                avgFuelConsumption = stats.avgFuelConsumption,
                isCompleted = true
            )
        }
    }
    
    // OBD Readings
    fun getReadingsByTrip(tripId: Long): Flow<List<OBDReading>> =
        obdReadingDao.getReadingsByTrip(tripId)
    fun getLatestReading(tripId: Long): Flow<OBDReading?> =
        obdReadingDao.getLatestReadingByTrip(tripId)
    
    suspend fun addReading(reading: OBDReading): Long =
        obdReadingDao.insert(reading)
    
    suspend fun addReadingsBatch(readings: List<OBDReading>) {
        obdReadingDao.insertAll(readings)
    }
    
    // Statistics
    fun getTotalDistance(vehicleId: Long): Flow<Float?> =
        tripDao.getTotalDistanceByVehicle(vehicleId)
    fun getTotalFuelConsumed(vehicleId: Long): Flow<Float?> =
        tripDao.getTotalFuelConsumedByVehicle(vehicleId)
    fun getMonthlyStats(vehicleId: Long, limit: Int = 12): Flow<List<MonthlyStats>> =
        tripDao.getMonthlyStats(vehicleId, limit)
    
    // DTC
    fun getActiveDTCs(): Flow<List<DiagnosticDTC>> = dtcDao.getActiveDTCs()
    fun getDTCsByTrip(tripId: Long): Flow<List<DiagnosticDTC>> = dtcDao.getDTCsByTrip(tripId)
    suspend fun addDTC(dtc: DiagnosticDTC): Long = dtcDao.insert(dtc)
    suspend fun clearDTC(dtcCode: String) = dtcDao.resolveByCode(dtcCode)
    
    // Events
    fun getEventsByTrip(tripId: Long): Flow<List<TripEvent>> =
        tripEventDao.getEventsByTrip(tripId)
    fun getDrivingEvents(tripId: Long): Flow<List<TripEvent>> =
        tripEventDao.getDrivingEvents(tripId)
    suspend fun logEvent(event: TripEvent): Long = tripEventDao.insert(event)
    
    // Helper
    private data class TripStats(
        val distance: Float, val duration: Long, val avgSpeed: Float, val maxSpeed: Float,
        val fuelConsumed: Float, val avgFuelConsumption: Float,
        val lastLatitude: Double?, val lastLongitude: Double?, val lastOdometer: Float?
    )
    
    private fun calculateTripStatistics(readings: List<OBDReading>): TripStats {
        if (readings.isEmpty()) {
            return TripStats(0f, 0, 0f, 0f, 0f, 0f, null, null, null)
        }
        
        val speeds = readings.mapNotNull { it.speedKmh }.filter { it > 0 }
        val lastReading = readings.last()
        val fuelLevels = readings.mapNotNull { it.fuelLevelPercent }
        
        return TripStats(
            distance = calculateDistanceFromGPS(readings),
            duration = readings.last().timestamp - readings.first().timestamp,
            avgSpeed = speeds.average().toFloat(),
            maxSpeed = speeds.maxOrNull() ?: 0f,
            fuelConsumed = calculateFuelConsumed(fuelLevels),
            avgFuelConsumption = 0f,
            lastLatitude = lastReading.latitude,
            lastLongitude = lastReading.longitude,
            lastOdometer = null
        )
    }
    
    private fun calculateDistanceFromGPS(readings: List<OBDReading>): Float {
        if (readings.size < 2) return 0f
        var totalDistance = 0f
        for (i in 1 until readings.size) {
            val prev = readings[i - 1]
            val curr = readings[i]
            if (prev.latitude != null && prev.longitude != null &&
                curr.latitude != null && curr.longitude != null) {
                totalDistance += haversine(
                    prev.latitude, prev.longitude, curr.latitude, curr.longitude
                ).toFloat()
            }
        }
        return totalDistance
    }
    
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
    
    private fun calculateFuelConsumed(fuelLevels: List<Float>): Float {
        if (fuelLevels.size < 2) return 0f
        return (fuelLevels.first() - fuelLevels.last()).coerceAtLeast(0f)
    }
}
```

### 8.2 ViewModel

```kotlin
@HiltViewModel
class OBDViewModel @Inject constructor(
    private val repository: OBDRepository
) : ViewModel() {
    
    val vehicles: StateFlow<List<Vehicle>> = repository.getAllVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val activeVehicle: StateFlow<Vehicle?> = repository.getActiveVehicle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    private val _activeTrip = MutableStateFlow<Trip?>(null)
    val activeTrip: StateFlow<Trip?> = _activeTrip.asStateFlow()
    
    val allTrips: StateFlow<List<Trip>> = repository.getAllTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _liveReading = MutableStateFlow<OBDReading?>(null)
    val liveReading: StateFlow<OBDReading?> = _liveReading.asStateFlow()
    
    private val _monthlyStats = MutableStateFlow<List<MonthlyStats>>(emptyList())
    val monthlyStats: StateFlow<List<MonthlyStats>> = _monthlyStats.asStateFlow()
    
    private val _events = MutableSharedFlow<OBDUiEvent>()
    val events: SharedFlow<OBDUiEvent> = _events.asSharedFlow()
    
    init {
        observeActiveTrip()
    }
    
    private fun observeActiveTrip() {
        viewModelScope.launch {
            repository.getActiveTrip().collect { trip ->
                _activeTrip.value = trip
                trip?.let { observeLatestReading(it.id) }
            }
        }
    }
    
    private fun observeLatestReading(tripId: Long) {
        viewModelScope.launch {
            repository.getLatestReading(tripId).collect { reading ->
                _liveReading.value = reading
            }
        }
    }
    
    fun startNewTrip() {
        viewModelScope.launch {
            try {
                activeVehicle.value?.let { vehicle ->
                    val tripId = repository.createTrip(vehicle.id)
                    _events.emit(OBDUiEvent.TripStarted(tripId))
                } ?: _events.emit(OBDUiEvent.Error("Kein aktives Fahrzeug"))
            } catch (e: Exception) {
                _events.emit(OBDUiEvent.Error(e.message ?: "Fehler"))
            }
        }
    }
    
    fun endCurrentTrip() {
        viewModelScope.launch {
            try {
                _activeTrip.value?.let { trip ->
                    repository.completeTrip(trip.id)
                    _events.emit(OBDUiEvent.TripEnded(trip.id))
                }
            } catch (e: Exception) {
                _events.emit(OBDUiEvent.Error(e.message ?: "Fehler"))
            }
        }
    }
    
    fun addOBDReading(reading: OBDReading) {
        viewModelScope.launch {
            _activeTrip.value?.id?.let { tripId ->
                repository.addReading(reading.copy(tripId = tripId))
            }
        }
    }
    
    fun loadMonthlyStats(vehicleId: Long) {
        viewModelScope.launch {
            repository.getMonthlyStats(vehicleId).collect { stats ->
                _monthlyStats.value = stats
            }
        }
    }
}

sealed class OBDUiEvent {
    data class TripStarted(val tripId: Long) : OBDUiEvent()
    data class TripEnded(val tripId: Long) : OBDUiEvent()
    data class Error(val message: String) : OBDUiEvent()
    data object DTCDetected : OBDUiEvent()
}
```

---

## 9. Migration-Strategien <a name="migration-strategien"></a>

### 9.1 Grundlegendes Muster

```kotlin
@Database(
    entities = [Vehicle::class, Trip::class, ...],
    version = 2, // Erhoehen bei Aenderungen
    exportSchema = true
)
abstract class OBDDatabase : RoomDatabase()
```

### 9.2 Migration 1 -> 2 (Spalte hinzufuegen)

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            ALTER TABLE vehicles 
            ADD COLUMN engine_displacement_liters REAL
        """)
    }
}
```

### 9.3 Komplexe Migration (Tabelle umbenennen)

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE vehicles_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                make TEXT NOT NULL,
                model TEXT NOT NULL,
                year INTEGER,
                vin TEXT UNIQUE,
                license_plate TEXT,
                fuel_type TEXT NOT NULL,
                tank_capacity_liters REAL,
                engine_displacement_liters REAL,
                is_active INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """)
        
        database.execSQL("""
            INSERT INTO vehicles_new 
            SELECT id, make, model, year, vin, license_plate, 
                   fuel_type, tank_capacity_liters, engine_displacement_liters,
                   is_active, created_at, updated_at 
            FROM vehicles
        """)
        
        database.execSQL("DROP TABLE vehicles")
        database.execSQL("ALTER TABLE vehicles_new RENAME TO vehicles")
    }
}
```

### 9.4 Migrationen im Builder registrieren

```kotlin
val db = Room.databaseBuilder(
    context.applicationContext,
    OBDDatabase::class.java,
    DATABASE_NAME
)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .fallbackToDestructiveMigration() // Nur fuer Entwicklung!
    .build()
```

### 9.5 Selektive destruktive Migration

```kotlin
val db = Room.databaseBuilder(
    context.applicationContext,
    OBDDatabase::class.java,
    DATABASE_NAME
)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .fallbackToDestructiveMigrationFrom(4) // Ab Version 4: destruktiv
    .build()
```

### 9.6 JSON-Export vor Migration

```kotlin
class DatabaseMigrationManager(
    private val context: Context,
    private val database: OBDDatabase
) {
    
    suspend fun exportBeforeMigration(exportDir: File): File {
        return withContext(Dispatchers.IO) {
            val backupFile = File(exportDir, "backup_v${database.openHelper.readableDatabase.version}.json")
            
            val exportData = mapOf(
                "version" to database.openHelper.readableDatabase.version,
                "exportTime" to System.currentTimeMillis(),
                "vehicles" to database.vehicleDao().getAllVehicles().first(),
                "trips" to database.tripDao().getAllTrips().first()
            )
            
            backupFile.writeText(Gson().toJson(exportData))
            backupFile
        }
    }
}
```

---

## 10. Performance bei vielen Datensaetzen <a name="performance"></a>

### 10.1 Indizes definieren

```kotlin
@Entity(
    tableName = "obd_readings",
    indices = [
        Index(value = ["trip_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["trip_id", "timestamp"]), // Komposit-Index
        Index(value = ["speed_kmh"]),
        Index(value = ["rpm"])
    ]
)
class OBDReading { ... }
```

### 10.2 Batch-Operationen

```kotlin
@Dao
interface OBDReadingDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<OBDReading>)
    
    @Transaction
    suspend fun insertAllOptimized(readings: List<OBDReading>) {
        val batchSize = 500
        readings.chunked(batchSize).forEach { batch ->
            insertAll(batch)
        }
    }
}
```

### 10.3 Paging mit Room Paging 3

```kotlin
@Dao
interface TripDao {
    
    @Query("SELECT * FROM trips ORDER BY start_time DESC")
    fun getTripsPaged(): PagingSource<Int, Trip>
    
    @Query("SELECT * FROM trips WHERE vehicle_id = :vehicleId ORDER BY start_time DESC")
    fun getTripsByVehiclePaged(vehicleId: Long): PagingSource<Int, Trip>
}

@Dao
interface OBDReadingDao {
    
    @Query("SELECT * FROM obd_readings WHERE trip_id = :tripId ORDER BY timestamp ASC")
    fun getReadingsByTripPaged(tripId: Long): PagingSource<Int, OBDReading>
}
```

### 10.4 Paging-Integration im Repository

```kotlin
fun getTripsPaged(vehicleId: Long): Flow<PagingData<Trip>> {
    return Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            prefetchDistance = 5
        ),
        pagingSourceFactory = { tripDao.getTripsByVehiclePaged(vehicleId) }
    ).flow
}
```

### 10.5 Datenbank-Optimierungen (PRAGMA)

```kotlin
override fun onConfigure(db: SupportSQLiteDatabase) {
    super.onConfigure(db)
    db.execSQL("PRAGMA journal_mode = WAL")
    db.execSQL("PRAGMA synchronous = NORMAL")
    db.execSQL("PRAGMA cache_size = -64000") // 64MB Cache
    db.execSQL("PRAGMA temp_store = MEMORY")
}
```

### 10.6 SQL-Aggregation statt Kotlin

```kotlin
@Dao
interface TripDao {
    
    // GUT: Aggregation in SQL
    @Query("""
        SELECT 
            COUNT(*) as trip_count,
            SUM(distance_km) as total_distance,
            SUM(fuel_consumed_liters) as total_fuel,
            AVG(avg_speed_kmh) as avg_speed
        FROM trips 
        WHERE vehicle_id = :vehicleId AND is_completed = 1
    """)
    fun getTripSummary(vehicleId: Long): Flow<TripSummary>
    
    // SCHLECHT: Alles laden und in Kotlin aggregieren
    @Query("SELECT * FROM trips WHERE vehicle_id = :vehicleId AND is_completed = 1")
    suspend fun getAllCompletedTrips(vehicleId: Long): List<Trip>
}

data class TripSummary(
    val tripCount: Int,
    val totalDistance: Float,
    val totalFuel: Float,
    val avgSpeed: Float
)
```

---

## 11. Backup und Export <a name="backup-export"></a>

### 11.1 Export-Service

```kotlin
class ExportService(
    private val context: Context,
    private val database: OBDDatabase
) {
    
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()
    
    suspend fun exportToJson(exportDir: File): ExportResult {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                val exportFile = File(exportDir, "obd_export_$timestamp.json")
                
                val exportData = ExportData(
                    exportVersion = 1,
                    exportedAt = System.currentTimeMillis(),
                    vehicles = database.vehicleDao().getAllVehicles().first(),
                    trips = database.tripDao().getAllTrips().first(),
                    dtcs = database.diagnosticDTCDao().getAllDTCs().first()
                )
                
                exportFile.writeText(gson.toJson(exportData))
                ExportResult.Success(exportFile)
            } catch (e: Exception) {
                ExportResult.Error(e.message ?: "Fehler")
            }
        }
    }
    
    suspend fun exportToCSV(exportDir: File, tripId: Long? = null): List<File> {
        return withContext(Dispatchers.IO) {
            val files = mutableListOf<File>()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            
            // Vehicles CSV
            val vehicles = database.vehicleDao().getAllVehicles().first()
            files.add(createVehiclesCSV(vehicles, exportDir, timestamp))
            
            // Trips CSV
            val trips = if (tripId != null) {
                listOfNotNull(database.tripDao().getTripByIdOnce(tripId))
            } else {
                database.tripDao().getAllTrips().first()
            }
            files.add(createTripsCSV(trips, exportDir, timestamp))
            
            // Readings CSV (nur bei spezifischem Trip)
            if (tripId != null) {
                val readings = database.obdReadingDao().getReadingsByTripOnce(tripId)
                if (readings.isNotEmpty()) {
                    files.add(createReadingsCSV(readings, exportDir, timestamp))
                }
            }
            
            files
        }
    }
    
    private fun createVehiclesCSV(vehicles: List<Vehicle>, dir: File, ts: String): File {
        val file = File(dir, "vehicles_$ts.csv")
        file.bufferedWriter().use { writer ->
            writer.write("id,make,model,year,vin,license_plate,fuel_type,tank_capacity,is_active")
            writer.newLine()
            vehicles.forEach { v ->
                writer.write("${v.id},${v.make},${v.model},${v.year ?: ""},${v.vin ?: ""},${v.licensePlate ?: ""},${v.fuelType.name},${v.tankCapacityLiters ?: ""},${v.isActive}")
                writer.newLine()
            }
        }
        return file
    }
    
    private fun createTripsCSV(trips: List<Trip>, dir: File, ts: String): File {
        val file = File(dir, "trips_$ts.csv")
        file.bufferedWriter().use { writer ->
            writer.write("id,vehicle_id,start_time,end_time,distance_km,duration_seconds,avg_speed_kmh,max_speed_kmh,fuel_consumed,is_completed")
            writer.newLine()
            trips.forEach { t ->
                writer.write("${t.id},${t.vehicleId},${t.startTime},${t.endTime ?: ""},${t.distanceKm},${t.durationSeconds},${t.avgSpeedKmh},${t.maxSpeedKmh},${t.fuelConsumedLiters},${t.isCompleted}")
                writer.newLine()
            }
        }
        return file
    }
    
    private fun createReadingsCSV(readings: List<OBDReading>, dir: File, ts: String): File {
        val file = File(dir, "readings_$ts.csv")
        file.bufferedWriter().use { writer ->
            writer.write("id,trip_id,timestamp,rpm,speed_kmh,throttle,coolant_temp,fuel_level,latitude,longitude")
            writer.newLine()
            readings.forEach { r ->
                writer.write("${r.id},${r.tripId},${r.timestamp},${r.rpm ?: ""},${r.speedKmh ?: ""},${r.throttlePositionPercent ?: ""},${r.coolantTempCelsius ?: ""},${r.fuelLevelPercent ?: ""},${r.latitude ?: ""},${r.longitude ?: ""}")
                writer.newLine()
            }
        }
        return file
    }
}

data class ExportData(
    val exportVersion: Int,
    val exportedAt: Long,
    val vehicles: List<Vehicle>,
    val trips: List<Trip>,
    val dtcs: List<DiagnosticDTC>
)

sealed class ExportResult {
    data class Success(val file: File) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
```

### 11.2 Import-Service

```kotlin
class ImportService(private val database: OBDDatabase) {
    
    suspend fun importFromJson(file: File): ImportResult {
        return withContext(Dispatchers.IO) {
            try {
                val data = Gson().fromJson(file.readText(), ExportData::class.java)
                
                data.vehicles.forEach { database.vehicleDao().insert(it) }
                data.trips.forEach { database.tripDao().insert(it) }
                data.dtcs.forEach { database.diagnosticDTCDao().insert(it) }
                
                ImportResult.Success(data.vehicles.size, data.trips.size)
            } catch (e: Exception) {
                ImportResult.Error(e.message ?: "Import fehlgeschlagen")
            }
        }
    }
}

sealed class ImportResult {
    data class Success(val vehicleCount: Int, val tripCount: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}
```

---

## 12. DAO Pattern fuer OBD-Repository <a name="repository-pattern"></a>

### 12.1 Vollstaendiges Repository

```kotlin
@Singleton
class OBDRepository @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val tripDao: TripDao,
    private val obdReadingDao: OBDReadingDao,
    private val dtcDao: DiagnosticDTCDao,
    private val tripEventDao: TripEventDao
) {
    // Kapselt alle Datenbank-Operationen
    // Bietet saubere API fuer ViewModels
    // Handhabt Flow-basierte reactive Daten
    
    // Siehe Abschnitt 8 fuer vollstaendigen Code
}
```

### 12.2 Dependency Injection mit Hilt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OBDDatabase {
        return OBDDatabase.getInstance(context)
    }
    
    @Provides
    fun provideVehicleDao(database: OBDDatabase): VehicleDao = database.vehicleDao()
    
    @Provides
    fun provideTripDao(database: OBDDatabase): TripDao = database.tripDao()
    
    @Provides
    fun provideOBDReadingDao(database: OBDDatabase): OBDReadingDao = database.obdReadingDao()
    
    @Provides
    fun provideDiagnosticDTCDao(database: OBDDatabase): DiagnosticDTCDao = database.diagnosticDTCDao()
    
    @Provides
    fun provideTripEventDao(database: OBDDatabase): TripEventDao = database.tripEventDao()
}
```

---

## 13. Trip-Tracking und Statistiken <a name="trip-tracking"></a>

### 13.1 Statistik-Berechnungen

```kotlin
class TripStatisticsCalculator {
    
    data class TripStats(
        val totalDistance: Float,
        val totalDuration: Long,
        val avgSpeed: Float,
        val maxSpeed: Float,
        val avgRpm: Float,
        val maxRpm: Int,
        val fuelConsumed: Float,
        val avgFuelConsumption: Float,
        val idleTime: Long,
        val drivingTime: Long
    )
    
    fun calculate(readings: List<OBDReading>): TripStats {
        if (readings.isEmpty()) return emptyStats()
        
        val speeds = readings.mapNotNull { it.speedKmh }
        val rpms = readings.mapNotNull { it.rpm }
        val fuelLevels = readings.mapNotNull { it.fuelLevelPercent }
        
        val totalDuration = readings.last().timestamp - readings.first().timestamp
        val idleTime = calculateIdleTime(readings)
        
        return TripStats(
            totalDistance = calculateGPSDistance(readings),
            totalDuration = totalDuration,
            avgSpeed = speeds.average().toFloat(),
            maxSpeed = speeds.maxOrNull() ?: 0f,
            avgRpm = rpms.average().toFloat(),
            maxRpm = rpms.maxOrNull() ?: 0,
            fuelConsumed = calculateFuelUsed(fuelLevels),
            avgFuelConsumption = calculateAvgFuelConsumption(readings),
            idleTime = idleTime,
            drivingTime = totalDuration - idleTime
        )
    }
    
    private fun calculateIdleTime(readings: List<OBDReading>): Long {
        var idleTime = 0L
        var idleStart = 0L
        var wasIdle = false
        
        for (reading in readings) {
            val isIdle = (reading.speedKmh ?: 0f) < 2f
            when {
                isIdle && !wasIdle -> { idleStart = reading.timestamp; wasIdle = true }
                !isIdle && wasIdle -> { idleTime += reading.timestamp - idleStart; wasIdle = false }
            }
        }
        return idleTime
    }
    
    private fun calculateGPSDistance(readings: List<OBDReading>): Float {
        if (readings.size < 2) return 0f
        var distance = 0f
        for (i in 1 until readings.size) {
            val p1 = readings[i - 1]
            val p2 = readings[i]
            if (p1.latitude != null && p1.longitude != null &&
                p2.latitude != null && p2.longitude != null) {
                distance += haversine(p1.latitude, p1.longitude, p2.latitude, p2.longitude).toFloat()
            }
        }
        return distance
    }
    
    private fun calculateFuelUsed(fuelLevels: List<Float>): Float {
        if (fuelLevels.size < 2) return 0f
        return (fuelLevels.first() - fuelLevels.last()).coerceAtLeast(0f)
    }
    
    private fun calculateAvgFuelConsumption(readings: List<OBDReading>): Float {
        val distance = calculateGPSDistance(readings)
        if (distance <= 0) return 0f
        
        val fuelLevels = readings.mapNotNull { it.fuelLevelPercent }
        val fuelUsed = calculateFuelUsed(fuelLevels)
        
        return if (distance > 0) (fuelUsed / distance) * 100 else 0f
    }
    
    private fun emptyStats() = TripStats(0f, 0, 0f, 0f, 0f, 0, 0f, 0f, 0, 0)
}
```

### 13.2 Fahrverhalten-Analyse

```kotlin
class DrivingBehaviorAnalyzer {
    
    data class DrivingScore(
        val overallScore: Int, // 0-100
        val accelerationScore: Int,
        val brakingScore: Int,
        val corneringScore: Int,
        val speedComplianceScore: Int
    )
    
    fun analyze(tripId: Long, events: List<TripEvent>): DrivingScore {
        val hardBrakes = events.count { it.eventType == TripEventType.HARD_BRAKE }
        val hardAccels = events.count { it.eventType == TripEventType.HARD_ACCELERATION }
        val harshCornerings = events.count { it.eventType == TripEventType.HARSH_CORNERING }
        val speedViolations = events.count { it.eventType == TripEventType.SPEED_LIMIT_EXCEEDED }
        
        return DrivingScore(
            overallScore = calculateOverallScore(hardBrakes, hardAccels, harshCornerings, speedViolations),
            accelerationScore = scoreAcceleration(hardAccels),
            brakingScore = scoreBraking(hardBrakes),
            corneringScore = scoreCornering(harshCornerings),
            speedComplianceScore = scoreSpeedCompliance(speedViolations)
        )
    }
    
    private fun calculateOverallScore(brakes: Int, accels: Int, corners: Int, speeds: Int): Int {
        val baseScore = 100
        val deductions = (brakes * 5) + (accels * 3) + (corners * 4) + (speeds * 2)
        return (baseScore - deductions).coerceIn(0, 100)
    }
    
    private fun scoreAcceleration(count: Int): Int = (100 - (count * 5)).coerceIn(0, 100)
    private fun scoreBraking(count: Int): Int = (100 - (count * 5)).coerceIn(0, 100)
    private fun scoreCornering(count: Int): Int = (100 - (count * 5)).coerceIn(0, 100)
    private fun scoreSpeedCompliance(count: Int): Int = (100 - (count * 2)).coerceIn(0, 100)
}
```

---

## 14. Encrypted SharedPreferences vs. Room fuer Einstellungen <a name="sharedpreferences-vs-room"></a>

| Kriterium | EncryptedSharedPreferences | Room |
|-----------|---------------------------|------|
| **Datentypen** | Primitive (String, Int, etc.) | Alle Typen via TypeConverter |
| **Struktur** | Key-Value Paare | Relationale Tabellen |
| **Verschluesselung** | AES-256 out-of-the-box | Manuell (SQLCipher) |
| **Performance (kleine Daten)** | Schneller | Etwas langsamer |
| **Performance (grosse Daten)** | Ungeeignet | Ideal |
| **Query-Möglichkeiten** | Nein | Ja (SQL) |
| **Migration** | Manuell | Automatisch via Room |
| **Type-Safety** | Nein | Ja |

### 14.1 EncryptedSharedPreferences fuer App-Einstellungen

```kotlin
class SecurePreferencesManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // OBD-Einstellungen
    var samplingIntervalMs: Long
        get() = sharedPreferences.getLong("sampling_interval", 1000L)
        set(value) = sharedPreferences.edit().putLong("sampling_interval", value).apply()
    
    var maxSpeedAlert: Float
        get() = sharedPreferences.getFloat("max_speed_alert", 130f)
        set(value) = sharedPreferences.edit().putFloat("max_speed_alert", value).apply()
    
    var enableGpsTracking: Boolean
        get() = sharedPreferences.getBoolean("enable_gps", true)
        set(value) = sharedPreferences.edit().putBoolean("enable_gps", value).apply()
    
    var lastConnectedDevice: String?
        get() = sharedPreferences.getString("last_device", null)
        set(value) = sharedPreferences.edit().putString("last_device", value).apply()
    
    var userApiKey: String?
        get() = sharedPreferences.getString("api_key", null)
        set(value) = sharedPreferences.edit().putString("api_key", value).apply()
    
    fun clearAll() = sharedPreferences.edit().clear().apply()
}
```

### 14.2 Room fuer komplexe Einstellungen

```kotlin
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<String?>
    
    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    suspend fun getSettingOnce(key: String): String?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettings)
    
    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)
}

class SettingsRepository(private val settingsDao: AppSettingsDao) {
    
    companion object {
        private const val KEY_OBD_PROTOCOL = "obd_protocol"
        private const val KEY_ALERT_THRESHOLDS = "alert_thresholds"
    }
    
    suspend fun setOBDProtocol(protocol: String) {
        settingsDao.setSetting(AppSettings(KEY_OBD_PROTOCOL, protocol))
    }
    
    suspend fun getOBDProtocol(): String {
        return settingsDao.getSettingOnce(KEY_OBD_PROTOCOL) ?: "AUTO"
    }
    
    suspend fun setAlertThresholds(thresholds: AlertThresholds) {
        val json = Gson().toJson(thresholds)
        settingsDao.setSetting(AppSettings(KEY_ALERT_THRESHOLDS, json))
    }
    
    suspend fun getAlertThresholds(): AlertThresholds {
        val json = settingsDao.getSettingOnce(KEY_ALERT_THRESHOLDS)
        return json?.let { Gson().fromJson(it, AlertThresholds::class.java) } 
            ?: AlertThresholds()
    }
}

data class AlertThresholds(
    val maxSpeedKmh: Float = 130f,
    val maxRpm: Int = 6500,
    val highRpmThreshold: Int = 5500,
    val hardBrakeThreshold: Float = -3.5f,
    val hardAccelThreshold: Float = 3.0f
)
```

### 14.3 Empfehlung

```
Fuer OBD-Apps:

1. EncryptedSharedPreferences:
   - API-Schluessel / Tokens
   - Bluetooth-Geraete-MAC-Adressen
   - Benutzer-Login-Daten (Token)
   - Sensible Fahrzeug-IDs

2. Room:
   - Alert-Konfigurationen (komplexe Objekte)
   - Benutzerdefinierte PID-Listen
   - Fahrzeugprofile mit vielen Feldern
   - Einstellungen die durchsucht/gefiltert werden muessen
```

---

## 15. Best Practices <a name="best-practices"></a>

### 15.1 Architektur

1. **Single Source of Truth**: Room-Datenbank als zentrale Datenquelle
2. **Repository Pattern**: Kapselt alle DB-Operationen
3. **Unidirectional Data Flow**: ViewModel -> Repository -> Room -> Flow -> UI
4. **Separation of Concerns**: Entities, DAOs, Repository, ViewModel sauber trennen

### 15.2 Performance

1. **Indizes setzen**: Fremdschluessel und haeufige Query-Felder
2. **Batch-Operationen**: Bulk-Inserts statt einzelne Insert-Aufrufe
3. **Paging**: Paging 3 fuer lange Listen
4. **SQL-Aggregation**: Berechnungen in SQL statt Kotlin
5. **Write-Ahead Logging**: JournalMode.WRITE_AHEAD_LOGGING aktivieren

### 15.3 Migration

1. **Schema exportieren**: exportSchema = true fuer automatische Dokumentation
2. **Fallback definieren**: Immer fallbackToDestructiveMigration() als Backup
3. **Daten sichern**: JSON-Backup vor destruktiven Migrationen
4. **Testen**: Migrationen mit instrumentierten Tests verifizieren

### 15.4 Testing

```kotlin
@Before
fun setup() {
    database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        OBDDatabase::class.java
    )
        .allowMainThreadQueries()
        .build()
}

@Test
fun testInsertAndRetrieveVehicle() = runBlocking {
    val vehicle = Vehicle(
        make = "Toyota",
        model = "Corolla",
        fuelType = FuelType.GASOLINE
    )
    
    val id = database.vehicleDao().insert(vehicle)
    val retrieved = database.vehicleDao().getVehicleById(id).first()
    
    assertEquals(vehicle.make, retrieved?.make)
    assertEquals(vehicle.model, retrieved?.model)
}

@After
fun tearDown() {
    database.close()
}
```

### 15.5 Sicherheit

1. **SQLCipher**: Fuer verschluesselte Datenbank
2. **EncryptedSharedPreferences**: Fuer sensible Einstellungen
3. **Input Validation**: Niemals unbeguetigte Daten speichern
4. **Export/Import**: Warnung bei Export sensibler Daten

---

## 16. Quellen und Links <a name="quellen"></a>

### Offizielle Dokumentation

- [Room Persistence Library - Android Developers](https://developer.android.com/training/data-storage/room)
- [Room Database Guide](https://developer.android.com/topic/libraries/architecture/room)
- [Room Codelab](https://developer.android.com/codelabs/android-room-with-a-view-kotlin)
- [Room with Coroutines](https://developer.android.com/codelabs/kotlin-coroutines)
- [Paging 3 with Room](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)

### Externe Ressourcen

- [Android Architecture Blueprints - Room Beispiele](https://github.com/android/architecture-components-samples)
- [Room Migration Guide - Medium](https://medium.com/androiddevelopers/room-migration-2b7d0c08f9e3)
- [Room Database Best Practices - ProAndroidDev](https://proandroiddev.com/android-room-database-best-practices-f6f6ac18c2a9)
- [Encrypted SharedPreferences - Android Developers](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)

### OBD-Spezifische Referenzen

- [OBD-II PIDs - Wikipedia](https://en.wikipedia.org/wiki/OBD-II_PIDs)
- [Mode 01 - Current Data PIDs](https://www.cs.ubc.ca/~nicell/Papers/SAE%20papers/05AN01-02%20Khan%20Nehmadi.pdf)
- [SAE J1979 - OBD-II Standard](https://en.wikipedia.org/wiki/On-board_diagnostics)

### Code-Beispiele

- [Architecture Components Samples - TodoMVP](https://github.com/android/architecture-components-samples/tree/main/TodoMVP)
- [Room Database Tutorial - Mindorks](https://mindorks.com/course/learn-android-room-database)
- [Room with Kotlin Coroutines](https://github.com/googlesamples/android-architecture-components/tree/master/BasicRxJavaSample)

---

## Lizenz

Dieses Dokument ist Teil des canop-obd Projekts und unterliegt der MIT-Lizenz.

---

Letzte Aktualisierung: Mai 2026
