package com.canopobd.data.model

import com.canopobd.bluetooth.Mode22TurboData
import org.junit.Assert.*
import org.junit.Test

class Mode22TurboDataTest {

    // --- Mode22TurboData construction tests ---

    @Test
    fun `Mode22TurboData can be created with default values`() {
        val data = Mode22TurboData()
        assertEquals(0.0, data.turboBoostActual, 0.001)
        assertEquals(0.0, data.turboBoostTarget, 0.001)
        assertEquals(0.0, data.wastegateDuty, 0.001)
        assertEquals(0.0, data.turboSpeed, 0.001)
        assertEquals(0.0, data.chargeAirTemp, 0.001)
        assertEquals(0.0, data.turboInletTemp, 0.001)
        assertEquals(0.0, data.turboOutletTemp, 0.001)
        assertEquals(0.0, data.engineTorque, 0.001)
        assertEquals(0.0, data.vgtPosition, 0.001)
        assertTrue(data.timestamp > 0)
    }

    @Test
    fun `Mode22TurboData can be created with values`() {
        val timestamp = System.currentTimeMillis()
        val data = Mode22TurboData(
            turboBoostActual = 170.0,
            turboBoostTarget = 165.0,
            wastegateDuty = 35.0,
            turboSpeed = 120000.0,
            chargeAirTemp = 45.0,
            turboInletTemp = 80.0,
            turboOutletTemp = 180.0,
            engineTorque = 75.0,
            vgtPosition = 60.0,
            timestamp = timestamp
        )
        assertEquals(170.0, data.turboBoostActual, 0.001)
        assertEquals(165.0, data.turboBoostTarget, 0.001)
        assertEquals(35.0, data.wastegateDuty, 0.001)
        assertEquals(120000.0, data.turboSpeed, 0.001)
        assertEquals(45.0, data.chargeAirTemp, 0.001)
        assertEquals(80.0, data.turboInletTemp, 0.001)
        assertEquals(180.0, data.turboOutletTemp, 0.001)
        assertEquals(75.0, data.engineTorque, 0.001)
        assertEquals(60.0, data.vgtPosition, 0.001)
        assertEquals(timestamp, data.timestamp)
    }

    // --- boostDeviation tests ---

    @Test
    fun `boostDeviation calculates positive deviation correctly`() {
        val data = Mode22TurboData(turboBoostActual = 180.0, turboBoostTarget = 170.0)
        val deviation = data.boostDeviation
        // (180 - 170) / 170 * 100 = 5.88%
        assertEquals(5.88, deviation, 0.01)
    }

    @Test
    fun `boostDeviation calculates negative deviation correctly`() {
        val data = Mode22TurboData(turboBoostActual = 160.0, turboBoostTarget = 170.0)
        val deviation = data.boostDeviation
        // (160 - 170) / 170 * 100 = -5.88%
        assertEquals(-5.88, deviation, 0.01)
    }

    @Test
    fun `boostDeviation returns zero when target is zero`() {
        val data = Mode22TurboData(turboBoostActual = 100.0, turboBoostTarget = 0.0)
        assertEquals(0.0, data.boostDeviation, 0.001)
    }

    @Test
    fun `boostDeviation returns zero when target is negative`() {
        val data = Mode22TurboData(turboBoostActual = 100.0, turboBoostTarget = -10.0)
        assertEquals(0.0, data.boostDeviation, 0.001)
    }

    @Test
    fun `boostDeviation returns zero when values are equal`() {
        val data = Mode22TurboData(turboBoostActual = 170.0, turboBoostTarget = 170.0)
        assertEquals(0.0, data.boostDeviation, 0.001)
    }

    // --- relativeBoost tests ---

    @Test
    fun `relativeBoost returns zero at atmospheric pressure`() {
        val data = Mode22TurboData(turboBoostActual = 100.0)
        assertEquals(0.0, data.relativeBoost, 0.001)
    }

    @Test
    fun `relativeBoost returns positive value above atmospheric`() {
        val data = Mode22TurboData(turboBoostActual = 170.0)
        assertEquals(70.0, data.relativeBoost, 0.001)
    }

    @Test
    fun `relativeBoost never returns negative (coerceAtLeast)`() {
        val data = Mode22TurboData(turboBoostActual = 90.0)
        assertEquals(0.0, data.relativeBoost, 0.001)
    }

    // --- boostBar tests ---

    @Test
    fun `boostBar returns zero at atmospheric pressure`() {
        val data = Mode22TurboData(turboBoostActual = 100.0)
        assertEquals(0.0, data.boostBar, 0.001)
    }

    @Test
    fun `boostBar returns correct value above atmospheric`() {
        val data = Mode22TurboData(turboBoostActual = 170.0)
        assertEquals(0.7, data.boostBar, 0.01)
    }

    @Test
    fun `boostBar converts kPa to bar correctly`() {
        val data = Mode22TurboData(turboBoostActual = 230.0)
        assertEquals(1.3, data.boostBar, 0.01)
    }

    // --- isOverboost tests ---

    @Test
    fun `isOverboost returns true when boost exceeds 1_3 bar`() {
        val data = Mode22TurboData(turboBoostActual = 235.0)
        assertTrue(data.isOverboost)
    }

    @Test
    fun `isOverboost returns false when boost is at 1_3 bar`() {
        val data = Mode22TurboData(turboBoostActual = 230.0) // exactly 1.3 bar
        // isOverboost: boostBar > 1.3, so 1.3 is NOT overboost
        assertFalse(data.isOverboost)
    }

    @Test
    fun `isOverboost returns false when boost is below 1_3 bar`() {
        val data = Mode22TurboData(turboBoostActual = 220.0) // 1.2 bar
        assertFalse(data.isOverboost)
    }

    // --- isUnderboost tests ---

    @Test
    fun `isUnderboost returns true when actual is zero and target is positive`() {
        val data = Mode22TurboData(turboBoostActual = 0.0, turboBoostTarget = 170.0)
        assertTrue(data.isUnderboost)
    }

    @Test
    fun `isUnderboost returns true when actual is negative and target is positive`() {
        val data = Mode22TurboData(turboBoostActual = -10.0, turboBoostTarget = 170.0)
        assertTrue(data.isUnderboost)
    }

    @Test
    fun `isUnderboost returns false when actual is zero and target is zero`() {
        val data = Mode22TurboData(turboBoostActual = 0.0, turboBoostTarget = 0.0)
        assertFalse(data.isUnderboost)
    }

    @Test
    fun `isUnderboost returns false when actual is positive`() {
        val data = Mode22TurboData(turboBoostActual = 50.0, turboBoostTarget = 170.0)
        assertFalse(data.isUnderboost)
    }

    // --- Real-world scenario tests ---

    @Test
    fun `Mode22TurboData models idle condition`() {
        val data = Mode22TurboData(
            turboBoostActual = 30.0,
            turboBoostTarget = 30.0,
            wastegateDuty = 85.0,
            turboSpeed = 8000.0,
            chargeAirTemp = 25.0,
            turboInletTemp = 40.0,
            turboOutletTemp = 120.0,
            engineTorque = 0.0,
            vgtPosition = 95.0
        )
        assertEquals(0.0, data.relativeBoost, 0.001)
        assertEquals(0.0, data.boostBar, 0.001)
        assertFalse(data.isOverboost)
        assertFalse(data.isUnderboost)
    }

    @Test
    fun `Mode22TurboData models cruise condition`() {
        val data = Mode22TurboData(
            turboBoostActual = 100.0,
            turboBoostTarget = 100.0,
            wastegateDuty = 55.0,
            turboSpeed = 80000.0,
            chargeAirTemp = 35.0,
            turboInletTemp = 60.0,
            turboOutletTemp = 150.0,
            engineTorque = 40.0,
            vgtPosition = 70.0
        )
        assertEquals(0.0, data.boostDeviation, 0.001)
        assertEquals(0.0, data.relativeBoost, 0.001) // 100 - 100 = 0
        assertFalse(data.isOverboost)
        assertFalse(data.isUnderboost)
    }

    @Test
    fun `Mode22TurboData models WOT overboost condition`() {
        val data = Mode22TurboData(
            turboBoostActual = 310.0,
            turboBoostTarget = 270.0,
            wastegateDuty = 25.0,
            turboSpeed = 150000.0,
            chargeAirTemp = 50.0,
            turboInletTemp = 80.0,
            turboOutletTemp = 200.0,
            engineTorque = 95.0,
            vgtPosition = 30.0
        )
        assertTrue(data.boostDeviation > 0)
        assertEquals(2.1, data.boostBar, 0.01)
        assertTrue(data.isOverboost)
        assertFalse(data.isUnderboost)
    }

    @Test
    fun `Mode22TurboData models limp-home mode`() {
        val data = Mode22TurboData(
            turboBoostActual = 0.0,
            turboBoostTarget = 120.0,
            wastegateDuty = 100.0,
            turboSpeed = 0.0,
            chargeAirTemp = 30.0,
            turboInletTemp = 50.0,
            turboOutletTemp = 100.0,
            engineTorque = 10.0,
            vgtPosition = 0.0
        )
        assertTrue(data.isUnderboost)
        assertEquals(0.0, data.relativeBoost, 0.001)
    }
}
