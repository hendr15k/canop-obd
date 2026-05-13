package com.canopobd.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.canopobd.data.local.CanopoDatabase
import com.canopobd.data.local.TripEntity
import com.canopobd.data.local.TripLocationEntity
import com.canopobd.data.model.GPSLocation
import com.canopobd.data.model.GPSTrip
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
class GPSTracker(private val context: Context) {

    private val db = CanopoDatabase.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private val _currentLocation = MutableStateFlow<GPSLocation?>(null)
    val currentLocation: StateFlow<GPSLocation?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _currentTrip = MutableStateFlow<GPSTrip?>(null)
    val currentTrip: StateFlow<GPSTrip?> = _currentTrip.asStateFlow()

    private val _tripHistory = MutableStateFlow<List<GPSTrip>>(emptyList())
    val tripHistory: StateFlow<List<GPSTrip>> = _tripHistory.asStateFlow()

    private var tripId: String = ""
    private var tripStartTime: Long = 0L
    private var tripLocations: MutableList<GPSLocation> = mutableListOf()
    private var tripDistanceMeters: Double = 0.0
    private var lastLocation: GPSLocation? = null
    private var locationCallback: LocationCallback? = null

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setMinUpdateIntervalMillis(500L)
        .setMinUpdateDistanceMeters(5f)
        .build()

    init {
        loadTripHistoryFromDb()
    }

    private fun loadTripHistoryFromDb() {
        scope.launch {
            try {
                val trips = db.tripDao().getAllOnce().map { trip ->
                    val locations = db.tripLocationDao().getLocationsForTrip(trip.id).map { loc ->
                        GPSLocation(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            altitude = loc.altitude,
                            speed = loc.speed,
                            bearing = loc.bearing,
                            accuracy = loc.accuracy,
                            timestamp = loc.timestamp
                        )
                    }
                    GPSTrip(
                        id = trip.id.toString(),
                        startTime = trip.startTime,
                        endTime = trip.endTime,
                        locations = locations,
                        distanceKm = trip.distanceKm.toDouble(),
                        maxSpeedKmh = trip.maxSpeedKmh.toDouble(),
                        avgSpeedKmh = trip.avgSpeedKmh.toDouble()
                    )
                }
                _tripHistory.value = trips
            } catch (_: Exception) { }
        }
    }

    fun startTracking(): Boolean {
        if (_isTracking.value) return false
        if (!hasLocationPermission()) return false

        tripId = UUID.randomUUID().toString().take(8).uppercase()
        tripStartTime = System.currentTimeMillis()
        tripLocations = mutableListOf()
        tripDistanceMeters = 0.0
        lastLocation = null

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val gpsLoc = locationToGPS(loc)
                    _currentLocation.value = gpsLoc
                    processLocation(gpsLoc)
                }
            }
        }

        val callback = locationCallback ?: return false
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
        _isTracking.value = true
        return true
    }

    fun stopTracking() {
        if (!_isTracking.value) return

        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null

        if (tripLocations.isNotEmpty()) {
            val trip = GPSTrip(
                id = tripId,
                startTime = tripStartTime,
                endTime = System.currentTimeMillis(),
                locations = tripLocations.toList(),
                distanceKm = tripDistanceMeters / 1000.0,
                maxSpeedKmh = tripLocations.maxOfOrNull { it.speed * 3.6f }?.toDouble() ?: 0.0,
                avgSpeedKmh = if (tripLocations.size > 1) {
                    tripLocations.sumOf { (it.speed * 3.6).toDouble() } / tripLocations.size
                } else 0.0
            )
            _tripHistory.value = _tripHistory.value + trip
            _currentTrip.value = trip
            persistTrip(trip)
        }

        _isTracking.value = false
    }

    private fun persistTrip(trip: GPSTrip) {
        scope.launch {
            try {
                val tripEntity = TripEntity(
                    startTime = trip.startTime,
                    endTime = trip.endTime,
                    distanceKm = trip.distanceKm.toFloat(),
                    avgSpeedKmh = trip.avgSpeedKmh.toFloat(),
                    maxSpeedKmh = trip.maxSpeedKmh.toFloat(),
                    avgRpm = 0.0,
                    maxRpm = 0.0,
                    fuelUsedLiters = 0f,
                    vin = ""
                )
                val tripRowId = db.tripDao().insert(tripEntity)

                val locationEntities = trip.locations.map { loc ->
                    TripLocationEntity(
                        tripId = tripRowId,
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        altitude = loc.altitude,
                        speed = loc.speed,
                        bearing = loc.bearing,
                        accuracy = loc.accuracy,
                        timestamp = loc.timestamp
                    )
                }
                db.tripLocationDao().insertAll(locationEntities)
            } catch (_: Exception) { }
        }
    }

    private fun processLocation(loc: GPSLocation) {
        tripLocations.add(loc)

        lastLocation?.let { prev ->
            val results = FloatArray(1)
            Location.distanceBetween(
                prev.latitude, prev.longitude,
                loc.latitude, loc.longitude,
                results
            )
            tripDistanceMeters += results[0]
        }
        lastLocation = loc
    }

    private fun locationToGPS(loc: Location): GPSLocation = GPSLocation(
        latitude = loc.latitude,
        longitude = loc.longitude,
        altitude = loc.altitude,
        speed = loc.speed,
        bearing = loc.bearing,
        accuracy = loc.accuracy,
        timestamp = loc.time
    )

    fun exportToGPX(): String {
        val trip = _currentTrip.value ?: return ""
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<gpx version="1.1" creator="canop-obd">""")
            appendLine("  <trk>")
            appendLine("    <name>Trip ${trip.id} - ${dateFormat.format(Date(trip.startTime))}</name>")
            appendLine("    <trkseg>")
            for (loc in trip.locations) {
                appendLine("""      <trkpt lat="${loc.latitude}" lon="${loc.longitude}">""")
                appendLine("""        <ele>${loc.altitude}</ele>""")
                appendLine("""        <time>${dateFormat.format(Date(loc.timestamp))}</time>""")
                appendLine("      </trkpt>")
            }
            appendLine("    </trkseg>")
            appendLine("  </trk>")
            appendLine("</gpx>")
        }
    }

    fun exportToKML(): String {
        val trip = _currentTrip.value ?: return ""
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<kml xmlns="http://www.opengis.net/kml/2.2">""")
            appendLine("  <Document>")
            appendLine("    <name>Trip ${trip.id}</name>")
            appendLine("    <description>Distance: %.1f km, Max Speed: %.0f km/h</description>".format(trip.distanceKm, trip.maxSpeedKmh))
            appendLine("    <Placemark>")
            appendLine("      <name>Trip Track</name>")
            appendLine("      <LineString>")
            appendLine("        <coordinates>")
            for (loc in trip.locations) {
                appendLine("          ${loc.longitude},${loc.latitude},${loc.altitude}")
            }
            appendLine("        </coordinates>")
            appendLine("      </LineString>")
            appendLine("    </Placemark>")
            appendLine("  </Document>")
            appendLine("</kml>")
        }
    }

    fun clearTripHistory() {
        _tripHistory.value = emptyList()
        _currentTrip.value = null
        scope.launch {
            try {
                db.tripDao().deleteAll()
            } catch (_: Exception) { }
        }
    }

    fun getLastKnownLocation(callback: (GPSLocation?) -> Unit) {
        if (!hasLocationPermission()) {
            callback(null)
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->
                callback(if (loc != null) locationToGPS(loc) else null)
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}