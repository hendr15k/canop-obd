package com.canopobd.data.protocol

import com.canopobd.bluetooth.ELM327BTConnection
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class UDSClientTest {

    private val client = UDSClient(mock(ELM327BTConnection::class.java))

    @Test
    fun `DID value excludes service and two byte DID header`() {
        val vin = "W0L0AHL08A1234567"
        val response = byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte()) + vin.toByteArray()

        val result = client.parseDIDValue("F190", response)

        assertEquals(vin, result.parsedValue)
    }

    @Test
    fun `four byte DID value is decoded as unsigned`() {
        val response = byteArrayOf(
            0x62, 0xF4.toByte(), 0xF0.toByte(),
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
        )

        val result = client.parseDIDValue("F4F0", response)

        assertEquals(4_294_967_295.0, result.parsedValue)
    }
}
