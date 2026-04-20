package com.canopobd.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OBDModelsTest {

    @Test
    fun `SPEED formula with empty byte array returns 0`() {
        val result = OBDPID.SPEED.formula(byteArrayOf())
        assertEquals(0.0, result, 0.0)
    }

    @Test
    fun `SPEED formula with valid positive speed values returns correctly`() {
        assertEquals(0.0, OBDPID.SPEED.formula(byteArrayOf(0)), 0.0)
        assertEquals(50.0, OBDPID.SPEED.formula(byteArrayOf(50)), 0.0)
        assertEquals(100.0, OBDPID.SPEED.formula(byteArrayOf(100)), 0.0)
    }

    @Test
    fun `SPEED formula with maximum unsigned 1-byte value returns 255`() {
        // -1 byte is 11111111 in binary, which is 255 when unsigned.
        assertEquals(255.0, OBDPID.SPEED.formula(byteArrayOf(-1)), 0.0)
        assertEquals(255.0, OBDPID.SPEED.formula(byteArrayOf(255.toByte())), 0.0)
    }

    @Test
    fun `SPEED formula with multi-byte array reads only the first byte`() {
        assertEquals(50.0, OBDPID.SPEED.formula(byteArrayOf(50, 100, 20)), 0.0)
    }
}
