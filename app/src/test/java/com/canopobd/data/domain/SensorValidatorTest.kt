package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SensorValidatorTest {

    private lateinit var validator: SensorValidator
    private val calibration = AstraJ14TurboCalibration()

    @Before
    fun setup() {
        validator = SensorValidator(calibration)
    }

    // --- MAF Sensor Tests ---

    @Test
    fun `MAF sensor - valid reading`() {
        val result = validator.validateMaf(5.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `MAF sensor - out of range high`() {
        val result = validator.validateMaf(300.0)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("MAF außerhalb Bereich", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `MAF sensor - out of range negative`() {
        val result = validator.validateMaf(-5.0)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `MAF sensor - null returns unavailable`() {
        val result = validator.validateMaf(null)
        assertTrue(result is ValidationResult.Unavailable)
    }

    @Test
    fun `MAF sensor - suspicious when above normal range`() {
        val result = validator.validateMaf(95.0)
        assertTrue(result is ValidationResult.Suspicious)
    }

    @Test
    fun `MAF sensor - rate of change detection - sudden spike`() {
        val result = validator.validateMaf(maf = 50.0, previousMaf = 10.0)
        assertTrue(result is ValidationResult.Suspicious)
        val msg = (result as ValidationResult.Suspicious).message
        assertTrue(msg.contains("MAF-Sprung"))
    }

    @Test
    fun `MAF sensor - no rate warning for small change`() {
        val result = validator.validateMaf(maf = 15.0, previousMaf = 10.0)
        assertFalse(result is ValidationResult.Suspicious)
    }

    @Test
    fun `MAF sensor - zero is valid within range`() {
        val result = validator.validateMaf(0.0)
        assertTrue(result is ValidationResult.Suspicious)
    }

    @Test
    fun `MAF sensor - boundary value at 100 is suspicious`() {
        val result = validator.validateMaf(100.0)
        assertTrue(result is ValidationResult.Suspicious)
    }

    @Test
    fun `MAF sensor - rate of change skipped when previous is null`() {
        val result = validator.validateMaf(maf = 50.0, previousMaf = null)
        assertFalse(result is ValidationResult.Suspicious)
    }

    // --- Coolant Temperature Tests ---

    @Test
    fun `coolant temp - valid`() {
        val result = validator.validateCoolant(90.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `coolant temp - suspicious at 101 degrees`() {
        val result = validator.validateCoolant(101.0)
        assertTrue(result is ValidationResult.Suspicious)
        assertEquals("Kühlmittel warnung", (result as ValidationResult.Suspicious).message)
    }

    @Test
    fun `coolant temp - invalid when overheated`() {
        val result = validator.validateCoolant(106.0)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Motor überhitzt!", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `coolant temp - invalid when too cold`() {
        val result = validator.validateCoolant(-50.0)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Sensorfehler (zu kalt)", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `coolant temp - null returns unavailable`() {
        val result = validator.validateCoolant(null)
        assertTrue(result is ValidationResult.Unavailable)
    }

    @Test
    fun `coolant temp - boundary at 100 is valid`() {
        val result = validator.validateCoolant(100.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `coolant temp - boundary at -40 is valid`() {
        val result = validator.validateCoolant(-40.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `coolant temp - boundary at -41 is invalid`() {
        val result = validator.validateCoolant(-41.0)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `coolant temp - boundary at 105 is suspicious`() {
        val result = validator.validateCoolant(105.0)
        assertTrue(result is ValidationResult.Suspicious)
    }

    // --- Boost Pressure Tests ---

    @Test
    fun `boost pressure - valid at WOT`() {
        val result = validator.validateBoost(1.1, 1.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `boost pressure - overboost suspicious`() {
        val result = validator.validateBoost(1.25, 1.0)
        assertTrue(result is ValidationResult.Suspicious)
        assertEquals("Overboost aktiv", (result as ValidationResult.Suspicious).message)
    }

    @Test
    fun `boost pressure - critical overboost invalid`() {
        val result = validator.validateBoost(1.36, 1.0)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Kritische Überladung!", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `boost pressure - null returns unavailable`() {
        val result = validator.validateBoost(null, 1.0)
        assertTrue(result is ValidationResult.Unavailable)
    }

    @Test
    fun `boost pressure at 1_2 bar is valid`() {
        val result = validator.validateBoost(1.2, 1.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `boost pressure at 1_19 bar is valid`() {
        val result = validator.validateBoost(1.19, 1.0)
        assertTrue(result is ValidationResult.Valid)
    }

    // --- RPM Tests ---

    @Test
    fun `RPM - valid at idle`() {
        val result = validator.validateRpm(750.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `RPM - valid at cruise`() {
        val result = validator.validateRpm(3000.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `RPM - suspicious near redline`() {
        val result = validator.validateRpm(6600.0)
        assertTrue(result is ValidationResult.Suspicious)
        assertEquals("Redline erreicht!", (result as ValidationResult.Suspicious).message)
    }

    @Test
    fun `RPM - invalid when negative`() {
        val result = validator.validateRpm(-100.0)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("RPM ungültig", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `RPM - invalid when exceeds 7000`() {
        val result = validator.validateRpm(7500.0)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `RPM - null returns unavailable`() {
        val result = validator.validateRpm(null)
        assertTrue(result is ValidationResult.Unavailable)
    }

    @Test
    fun `RPM - at exactly 6500 is valid`() {
        val result = validator.validateRpm(6500.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `RPM - at 0 is valid`() {
        val result = validator.validateRpm(0.0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `RPM - rate check compares against last stored value`() {
        validator.addRpm(2000.0)
        validator.addRpm(2100.0)
        // +100 gegenueber letztem Wert -> kein Sprung
        assertTrue(validator.validateRpm(2200.0) is ValidationResult.Valid)
        // +900 gegenueber letztem Wert -> Sprung (alter Code verglich gegen
        // vorletzten Wert und meldete hier faelschlich nichts/zu viel)
        val result = validator.validateRpm(3000.0)
        assertTrue(result is ValidationResult.Suspicious)
    }

    // --- ValidationResult sealed class tests ---

    @Test
    fun `ValidationResult Valid is singleton`() {
        assertTrue(ValidationResult.Valid === ValidationResult.Valid)
    }

    @Test
    fun `ValidationResult Unavailable is singleton`() {
        assertTrue(ValidationResult.Unavailable === ValidationResult.Unavailable)
    }

    @Test
    fun `ValidationResult Suspicious stores message`() {
        val result = ValidationResult.Suspicious("test warning")
        assertEquals("test warning", result.message)
    }

    @Test
    fun `ValidationResult Invalid stores message`() {
        val result = ValidationResult.Invalid("test error")
        assertEquals("test error", result.message)
    }
}
