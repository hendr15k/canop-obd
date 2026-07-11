package com.canopobd.data.repository

import com.canopobd.data.model.DataRecord
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
    CSV("csv", "text/csv", "CSV (Excel)"),
    JSON("json", "application/json", "JSON"),
    GPX_WITH_OBD("gpx", "application/gpx+xml", "GPX mit OBD-Telemetrie"),
    TORQUE_CSV("csv", "text/csv", "Torque Pro CSV")
}

object DataExporter {

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
        if (enhanced) {
            sb.appendLine(
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
            )
        } else {
            sb.appendLine("Timestamp,RPM,Speed,Coolant,Throttle,Fuel,Battery")
        }
        for (r in records) {
            if (enhanced) {
                val boostBar = calcBoostBar(r)
                val time = isoFmt.format(Instant.ofEpochMilli(r.timestamp))
                sb.append(
                    "${r.timestamp},${time}," +
                        "${r.rpm.toInt()},${r.speed.toInt()},${r.coolantTemp.toInt()},${r.throttle.toInt()}," +
                        "${r.fuelLevel.toInt()},${"%.2f".format(r.batteryVoltage)}," +
                        "${r.intakeTemp.toInt()},${r.oilTemp.toInt()}," +
                        "${r.boostPressure.toInt()},${r.barometricPressure.toInt()},${"%.2f".format(boostBar)}," +
                        "${r.wastegateDuty.toInt()},${r.turboRpm.toInt()}," +
                        "${r.egtBank1.toInt()},${r.egtBank2.toInt()}," +
                        "${r.chargeAirTemp.toInt()},${"%.2f".format(r.mafRate)}," +
                        "${r.engineLoad.toInt()},${"%.1f".format(r.shortTermFuelTrimB1)},${"%.1f".format(r.longTermFuelTrimB1)}," +
                        "${r.timingAdvance.toInt()}"
                )
                if (r.latitude != null && r.longitude != null) {
                    sb.appendLine(",${"%.6f".format(r.latitude)},${"%.6f".format(r.longitude)},${r.altitude?.let { "%.1f".format(it) } ?: ""}")
                } else {
                    sb.appendLine(",,,")
                }
            } else {
                sb.appendLine(
                    "${r.timestamp},${r.rpm.toInt()},${r.speed.toInt()}," +
                        "${r.coolantTemp.toInt()},${r.throttle.toInt()},${r.fuelLevel.toInt()},${r.batteryVoltage}"
                )
            }
        }
        return sb.toString()
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
                put("boostBar", "%.3f".format(calcBoostBar(r)))
                put("wastegate", r.wastegateDuty.toInt())
                put("turboRpm", r.turboRpm.toInt())
                put("egt1", r.egtBank1.toInt())
                put("egt2", r.egtBank2.toInt())
                put("chargeAir", r.chargeAirTemp.toInt())
                put("maf", "%.2f".format(r.mafRate))
                put("load", r.engineLoad.toInt())
                put("stft", "%.1f".format(r.shortTermFuelTrimB1))
                put("ltft", "%.1f".format(r.longTermFuelTrimB1))
                put("timingAdv", r.timingAdvance.toInt())
                if (r.latitude != null && r.longitude != null) {
                    put("lat", "%.6f".format(r.latitude))
                    put("lon", "%.6f".format(r.longitude))
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
        val withGps = records.filter { it.latitude != null && it.longitude != null }
        if (withGps.isEmpty()) {
            sb.appendLine("""      <!-- No GPS data available - generating time-only track -->""")
            records.forEach { r ->
                val time = gpxTimeFmt.format(Instant.ofEpochMilli(r.timestamp))
                sb.appendLine("""      <trkpt lat="0.0" lon="0.0"><time>$time</time></trkpt>""")
            }
        } else {
            for (r in withGps) {
                val lat = r.latitude ?: continue
                val lon = r.longitude ?: continue
                val alt = r.altitude ?: 0.0
                val time = gpxTimeFmt.format(Instant.ofEpochMilli(r.timestamp))
                val boostBar = calcBoostBar(r)
                sb.appendLine(
                    """      <trkpt lat="${"%.6f".format(lat)}" lon="${"%.6f".format(lon)}">""" +
                        """<ele>${"%.1f".format(alt)}</ele><time>$time</time>""" +
                        """<extensions><obd:track xmlns:obd="http://canopobd.com/gpx">""" +
                        """<obd:speed unit="kmh">${r.speed.toInt()}</obd:speed>""" +
                        """<obd:rpm>${r.rpm.toInt()}</obd:rpm>""" +
                        """<obd:coolant unit="c">${r.coolantTemp.toInt()}</obd:coolant>""" +
                        """<obd:throttle unit="pct">${r.throttle.toInt()}</obd:throttle>""" +
                        """<obd:boost unit="bar">${"%.2f".format(boostBar)}</obd:boost>""" +
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
            val altFt = (r.altitude ?: 0.0) * 3.28084
            val speedMph = r.speed * 0.621371
            val coolantF = r.coolantTemp * 9.0 / 5.0 + 32.0
            val intakeF = r.intakeTemp * 9.0 / 5.0 + 32.0
            val mafLbMin = r.mafRate * 0.13228
            sb.appendLine(
                "${r.timestamp},$time,${"%.6f".format(lon)},${"%.6f".format(lat)},${"%.1f".format(altFt)}," +
                    "${r.rpm.toInt()},${"%.1f".format(speedMph)},${"%.1f".format(coolantF)},${r.throttle.toInt()}," +
                    "${r.fuelLevel.toInt()},${"%.2f".format(r.batteryVoltage)}," +
                    "${"%.1f".format(intakeF)},${r.engineLoad.toInt()}," +
                    "${"%.1f".format(r.shortTermFuelTrimB1)},${"%.1f".format(r.longTermFuelTrimB1)}," +
                    "${r.timingAdvance.toInt()},${"%.2f".format(mafLbMin)}"
            )
        }
        return sb.toString()
    }

    private fun calcBoostBar(r: DataRecord): Double {
        return if (r.barometricPressure > 0) {
            ((r.boostPressure - r.barometricPressure).coerceAtLeast(0.0) / 100.0)
        } else 0.0
    }
}
