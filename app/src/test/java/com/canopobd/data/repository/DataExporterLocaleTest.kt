package com.canopobd.data.repository

import com.canopobd.data.model.DataRecord
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Exportdateien werden von Fremdsystemen (Excel, Torque Pro, GPX-Viewer,
 * JSON-Parser) gelesen. Diese erwarten einen Punkt als Dezimaltrenner.
 * Auf deutschen Geraeten setzt LocaleManager.setDefault auf Deutsch, wodurch
 * "%.2f".format(...) ein Komma erzeugt und die Dateien unlesbar macht.
 */
class DataExporterLocaleTest {

    private lateinit var previousLocale: Locale

    private fun record(
        speed: Double = 15.0,
        batteryVoltage: Double = 12.4,
        boostPressure: Double = 150.0,
        barometricPressure: Double = 100.0,
        mafRate: Double = 12.5,
        latitude: Double? = null,
        longitude: Double? = null
    ) = DataRecord(
        timestamp = 1_700_000_000_000L,
        rpm = 2000.0,
        speed = speed,
        coolantTemp = 90.0,
        throttle = 20.0,
        fuelLevel = 50.0,
        batteryVoltage = batteryVoltage,
        boostPressure = boostPressure,
        barometricPressure = barometricPressure,
        mafRate = mafRate,
        latitude = latitude,
        longitude = longitude
    )

    @Before
    fun useGermanLocale() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.GERMANY)
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(previousLocale)
    }

    @Test
    fun `csv export uses dot as decimal separator`() {
        val csv = DataExporter.exportCsv(listOf(record()), enhanced = true)

        assertTrue("Batteriespannung muss 12.40 schreiben, war: $csv", csv.contains("12.40"))
        assertFalse("Dezimalkomma im CSV gefunden: $csv", csv.contains("12,40"))
    }

    @Test
    fun `torque csv export uses dot as decimal separator`() {
        val csv = DataExporter.exportTorqueCsv(listOf(record()))

        assertTrue("Speed muss 9.3 mph schreiben, war: $csv", csv.contains("9.3"))
        assertFalse("Dezimalkomma in Torque-CSV gefunden: $csv", csv.contains("9,3"))
    }

    @Test
    fun `gpx export uses dot as decimal separator`() {
        val gpx = DataExporter.exportGpxWithObd(
            listOf(record(latitude = 50.9499, longitude = 6.3359))
        )

        assertTrue("Boost muss 0.50 schreiben, war: $gpx", gpx.contains(">0.50<"))
        assertTrue("Breitengrad muss 50.949900 schreiben, war: $gpx", gpx.contains("lat=\"50.949900\""))
        assertFalse("Dezimalkomma in GPX gefunden: $gpx", gpx.contains("0,50"))
    }

    @Test
    fun `gpx does not invent positions when GPS is missing`() {
        val gpx = DataExporter.exportGpxWithObd(listOf(record()))

        assertFalse("Missing GPS must not become a track at Null Island", gpx.contains("<trkpt"))
    }

    @Test
    fun `gpx rejects invalid coordinates but preserves real zero coordinates`() {
        val records = listOf(
            record(latitude = Double.NaN, longitude = 6.0),
            record(latitude = 91.0, longitude = 6.0),
            record(latitude = 50.0, longitude = Double.POSITIVE_INFINITY),
            record(latitude = 50.0, longitude = -181.0),
            record(latitude = 0.0, longitude = 0.0)
        )
        val gpx = DataExporter.exportGpxWithObd(records)
        assertEquals(1, "<trkpt".toRegex().findAll(gpx).count())
        assertTrue(gpx.contains("lat=\"0.000000\" lon=\"0.000000\""))
    }

    @Test
    fun `json export uses dot as decimal separator`() {
        val json = DataExporter.exportJson(listOf(record()))

        assertTrue("boostBar muss 0.500 schreiben, war: $json", json.contains("\"0.500\""))
        assertFalse("Dezimalkomma im JSON gefunden: $json", json.contains("\"0,500\""))
    }
}
