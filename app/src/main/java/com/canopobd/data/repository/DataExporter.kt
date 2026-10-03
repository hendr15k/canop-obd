package com.canopobd.data.repository

import com.canopobd.data.model.DataRecord
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
    CSV("csv", "text/csv", "CSV (Excel)"),
    JSON("json", "application/json", "JSON"),
    GPX_WITH_OBD("gpx", "application/gpx+xml", "GPX mit OBD-Telemetrie"),
    TORQUE_CSV("csv", "text/csv", "Torque Pro CSV")
}

object DataExporter {

    // Grenzen fuer gueltige GPS-Koordinaten. Bewusst als benannte Konstanten,
    // damit die Pruefung in hasValidGps nicht als Zahlenmystik dasteht.
    private const val MAX_LATITUDE_DEG = 90.0
    private const val MAX_LONGITUDE_DEG = 180.0

    // Umrechnungsfaktoren der Torque-Pro-Ausgabe (metrisch -> imperial).
    private const val KMH_TO_MPH = 0.621371
    private const val METER_TO_FEET = 3.28084
    private const val GRAMS_TO_POUNDS = 0.13228
    private const val KPA_TO_BAR = 100.0
    private const val FAHRENHEIT_OFFSET = 32.0
    private const val CELSIUS_SCALE = 9.0 / 5.0

    // Exportdateien gehen an Fremdsysteme (Excel, Torque Pro, GPX-Viewer,
    // JSON-Parser), die einen Punkt als Dezimaltrenner erwarten. Auf deutschen
    // Geraeten setzt LocaleManager.setDefault auf Deutsch, wodurch
    // "%.2f".format(...) ein Komma erzeugt und die Datei unbrauchbar macht.
    // Deshalb formatieren alle Exportformate mit Locale.ROOT.
    private fun fmt(pattern: String, vararg args: Any?): String =
        String.format(Locale.ROOT, pattern, *args)

    private val isoFmt: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .withZone(ZoneOffset.UTC)

    private val isoOffsetFmt: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
        .withZone(ZoneOffset.UTC)

    private val gpxTimeFmt: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .withZone(ZoneOffset.UTC)

    private val torqueFmt: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneOffset.UTC)

    // Ein Datensatz gilt nur dann als GPS-gueltig, wenn Breite und Laenge
    // vorhanden, endlich und im Wertebereich sind. Eine einzelne ungueltige
    // Koordinate (NaN, Unendlich, 91 Grad) darf weder eine CSV-Zeile mit
    // unlesbarer Zahl noch einen erfundenen Trackpunkt erzeugen. (0.0, 0.0)
    // bleibt gueltig, weil das eine echte Position im Golf von Guinea ist.
    fun hasValidGps(record: DataRecord): Boolean {
        val lat = record.latitude
        val lon = record.longitude
        return lat != null && lon != null &&
            lat.isFinite() && lon.isFinite() &&
            lat in -MAX_LATITUDE_DEG..MAX_LATITUDE_DEG &&
            lon in -MAX_LONGITUDE_DEG..MAX_LONGITUDE_DEG
    }

    fun export(records: List<DataRecord>, format: ExportFormat): String {
        return when (format) {
            ExportFormat.CSV -> exportCsv(records, enhanced = true)
            ExportFormat.JSON -> exportJson(records)
            ExportFormat.GPX_WITH_OBD -> exportGpxWithObd(records)
            ExportFormat.TORQUE_CSV -> exportTorqueCsv(records)
        }
    }

    fun exportCsv(records: List<DataRecord>, enhanced: Boolean = true): String {
        val sb = StringBuilder()
        sb.appendLine(if (enhanced) CSV_HEADER_ENHANCED else CSV_HEADER_BASIC)
        for (r in records) {
            sb.appendLine(if (enhanced) enhancedCsvRow(r) else basicCsvRow(r))
        }
        return sb.toString()
    }

    private const val CSV_HEADER_BASIC = "Timestamp,RPM,Speed,Coolant,Throttle,Fuel,Battery"

    private const val CSV_HEADER_ENHANCED =
        "Timestamp (ms),Time (ISO)," +
            "RPM,Speed (km/h),Coolant (°C),Throttle (%),Fuel (%),Battery (V)," +
            "Intake Temp (°C),Oil Temp (°C)," +
            "Boost (kPa),Baro (kPa),Boost (bar)," +
            "Wastegate (%),Turbo RPM," +
            "EGT B1 (°C),EGT B2 (°C)," +
            "Charge Air (°C),MAF (g/s)," +
            "Engine Load (%),STFT B1 (%),LTFT B1 (%)," +
            "Timing Adv (°)," +
            "Latitude,Longitude,Altitude (m)"

    private fun basicCsvRow(r: DataRecord): String =
        "${r.timestamp},${r.rpm.toInt()},${r.speed.toInt()}," +
            "${r.coolantTemp.toInt()},${r.throttle.toInt()},${r.fuelLevel.toInt()},${r.batteryVoltage}"

    private fun enhancedCsvRow(r: DataRecord): String {
        val time = isoFmt.format(Instant.ofEpochMilli(r.timestamp))
        val gps = if (hasValidGps(r)) {
            val alt = r.altitude?.let { fmt("%.1f", it) } ?: ""
            "${fmt("%.6f", r.latitude)},${fmt("%.6f", r.longitude)},$alt"
        } else {
            ",,"
        }
        return "${r.timestamp},$time," +
            "${r.rpm.toInt()},${r.speed.toInt()},${r.coolantTemp.toInt()},${r.throttle.toInt()}," +
            "${r.fuelLevel.toInt()},${fmt("%.2f", r.batteryVoltage)}," +
            "${r.intakeTemp.toInt()},${r.oilTemp.toInt()}," +
            "${r.boostPressure.toInt()},${r.barometricPressure.toInt()}," +
            "${fmt("%.2f", calcBoostBar(r))}," +
            "${r.wastegateDuty.toInt()},${r.turboRpm.toInt()}," +
            "${r.egtBank1.toInt()},${r.egtBank2.toInt()}," +
            "${r.chargeAirTemp.toInt()},${fmt("%.2f", r.mafRate)}," +
            "${r.engineLoad.toInt()},${fmt("%.1f", r.shortTermFuelTrimB1)}," +
            "${fmt("%.1f", r.longTermFuelTrimB1)}," +
            "${r.timingAdvance.toInt()},$gps"
    }

    fun exportJson(records: List<DataRecord>): String {
        val root = JSONObject()
        root.put("format", "Canopo OBD-II DataLog")
        root.put("version", 1)
        root.put("recordCount", records.size)
        root.put("exportedAt", isoOffsetFmt.format(Instant.now()))

        val arr = JSONArray()
        for (r in records) {
            val obj = JSONObject().apply {
                put("timestamp", r.timestamp)
                put("timestampIso", isoOffsetFmt.format(Instant.ofEpochMilli(r.timestamp)))
                put("rpm", r.rpm.toInt())
                put("speed", r.speed.toInt())
                put("coolant", r.coolantTemp.toInt())
                put("throttle", r.throttle.toInt())
                put("fuel", r.fuelLevel.toInt())
                put("battery", r.batteryVoltage)
                put("intake", r.intakeTemp.toInt())
                put("oil", r.oilTemp.toInt())
                put("boost", r.boostPressure.toInt())
                put("baro", r.barometricPressure.toInt())
                put("boostBar", fmt("%.3f", calcBoostBar(r)))
                put("wastegate", r.wastegateDuty.toInt())
                put("turboRpm", r.turboRpm.toInt())
                put("egt1", r.egtBank1.toInt())
                put("egt2", r.egtBank2.toInt())
                put("chargeAir", r.chargeAirTemp.toInt())
                put("maf", fmt("%.2f", r.mafRate))
                put("load", r.engineLoad.toInt())
                put("stft", fmt("%.1f", r.shortTermFuelTrimB1))
                put("ltft", fmt("%.1f", r.longTermFuelTrimB1))
                put("timingAdv", r.timingAdvance.toInt())
                if (r.latitude != null && r.longitude != null) {
                    put("lat", fmt("%.6f", r.latitude))
                    put("lon", fmt("%.6f", r.longitude))
                    put("alt", r.altitude ?: 0.0)
                }
            }
            arr.put(obj)
        }
        root.put("records", arr)
        return root.toString(2)
    }

    fun exportGpxWithObd(records: List<DataRecord>): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<gpx version="1.1" creator="Canopo OBD" xmlns="http://www.topografix.com/GPX/1/1">""")
        sb.appendLine("""  <metadata>""")
        sb.appendLine("""    <name>Canopo OBD Track</name>""")
        sb.appendLine("""    <time>${gpxTimeFmt.format(Instant.now())}</time>""")
        sb.appendLine("""    <desc>OBD-II Telemetry Track (${records.size} points)</desc>""")
        sb.appendLine("""  </metadata>""")
        sb.appendLine("""  <trk>""")
        sb.appendLine("""    <name>OBD-II Track</name>""")
        sb.appendLine("""    <trkseg>""")
        val withGps = records.filter { hasValidGps(it) }
        if (withGps.isEmpty()) {
            // Ohne GPS-Position darf kein Track erfunden werden: ein Pfad aus
            // lauter (0.0, 0.0)-Punkten legt in jedem GPX-Betrachter eine
            // Strecke vor der Kueste Afrikas an. Stattdessen bleibt der Track
            // leer und die Datei nennt den Grund.
            sb.appendLine("""      <!-- Keine GPS-Daten vorhanden: kein Track erzeugt -->""")
        } else {
            for (r in withGps) {
                val lat = r.latitude ?: continue
                val lon = r.longitude ?: continue
                val alt = r.altitude ?: 0.0
                val time = gpxTimeFmt.format(Instant.ofEpochMilli(r.timestamp))
                val boostBar = calcBoostBar(r)
                sb.appendLine(
                    """      <trkpt lat="${fmt("%.6f", lat)}" lon="${fmt("%.6f", lon)}">""" +
                        """<ele>${fmt("%.1f", alt)}</ele><time>$time</time>""" +
                        """<extensions><obd:track xmlns:obd="http://canopobd.com/gpx">""" +
                        """<obd:speed unit="kmh">${r.speed.toInt()}</obd:speed>""" +
                        """<obd:rpm>${r.rpm.toInt()}</obd:rpm>""" +
                        """<obd:coolant unit="c">${r.coolantTemp.toInt()}</obd:coolant>""" +
                        """<obd:throttle unit="pct">${r.throttle.toInt()}</obd:throttle>""" +
                        """<obd:boost unit="bar">${fmt("%.2f", boostBar)}</obd:boost>""" +
                        """<obd:turbo_rpm>${r.turboRpm.toInt()}</obd:turbo_rpm>""" +
                        """<obd:egt unit="c">${r.egtBank1.toInt()}</obd:egt>""" +
                        """<obd:load unit="pct">${r.engineLoad.toInt()}</obd:load>""" +
                        """</obd:track></extensions></trkpt>"""
                )
            }
        }
        sb.appendLine("""    </trkseg>""")
        sb.appendLine("""  </trk>""")
        sb.appendLine("""</gpx>""")
        return sb.toString()
    }

    fun exportTorqueCsv(records: List<DataRecord>): String {
        val sb = StringBuilder()
        sb.appendLine(
            "Device Time,GPS Time,Longitude,Latitude,Altitude (ft)," +
                "RPM (RPM),Speed (MPH),Coolant (°F),Throttle Position (%)," +
                "Fuel Level (%),Battery Voltage (V)," +
                "Intake Air Temp (°F),Engine Load (%)," +
                "Short Term Fuel Trim (%),Long Term Fuel Trim (%)," +
                "Timing Advance (°),MAF (lb/min)"
        )
        for (r in records) {
            val time = torqueFmt.format(Instant.ofEpochMilli(r.timestamp))
            val lat = r.latitude ?: 0.0
            val lon = r.longitude ?: 0.0
            val altFt = (r.altitude ?: 0.0) * METER_TO_FEET
            val speedMph = r.speed * KMH_TO_MPH
            val coolantF = r.coolantTemp * CELSIUS_SCALE + FAHRENHEIT_OFFSET
            val intakeF = r.intakeTemp * CELSIUS_SCALE + FAHRENHEIT_OFFSET
            val mafLbMin = r.mafRate * GRAMS_TO_POUNDS
            sb.appendLine(
                "${r.timestamp},$time,${fmt("%.6f", lon)},${fmt("%.6f", lat)}," +
                    "${fmt("%.1f", altFt)}," +
                    "${r.rpm.toInt()},${fmt("%.1f", speedMph)},${fmt("%.1f", coolantF)}," +
                    "${r.throttle.toInt()}," +
                    "${r.fuelLevel.toInt()},${fmt("%.2f", r.batteryVoltage)}," +
                    "${fmt("%.1f", intakeF)},${r.engineLoad.toInt()}," +
                    "${fmt("%.1f", r.shortTermFuelTrimB1)},${fmt("%.1f", r.longTermFuelTrimB1)}," +
                    "${r.timingAdvance.toInt()},${fmt("%.2f", mafLbMin)}"
            )
        }
        return sb.toString()
    }

    private fun calcBoostBar(r: DataRecord): Double {
        return if (r.barometricPressure > 0) {
            ((r.boostPressure - r.barometricPressure).coerceAtLeast(0.0) / KPA_TO_BAR)
        } else {
            0.0
        }
    }
}
