package com.canopobd.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression tests for audit batch 3 fixes.
 *
 * - PollMode.FAST was 50 ms, contradicting setPollRate's coerceIn(100..2000)
 *   and SettingsDialog's fast option (250 ms). A 50 ms cycle is physically
 *   impossible with ~58 serial ELM327 PID reads (~5-15 s per cycle).
 * - UpdateChecker.parseVersionCode never matched the real release-body
 *   format ("**versionCode:** 9": markdown chars between key and digits),
 *   so the remoteCode > currentCode comparison leg was dead code.
 */
class AuditBatch3RegressionTest {

    @Test
    fun `FAST poll mode is reachable via setPollRate clamp`() {
        val fast = PollMode.FAST.pollInterval
        assertEquals(250L, fast)
        // setPollRate coerces to 100..2000 — FAST must survive that clamp.
        assertEquals(fast, fast.coerceIn(100L, 2000L))
    }

    @Test
    fun `FAST poll mode matches settings dialog fast option`() {
        // SettingsDialog.PollRateFastMs = 250L.
        assertEquals(250L, PollMode.FAST.pollInterval)
    }

    @Test
    fun `parseVersionCode reads markdown body format`() {
        val body = "## canop-obd v1.7.0\n\n**versionCode:** 9\n\n### Installation"
        assertEquals(9, UpdateChecker.parseVersionCode(body))
    }

    @Test
    fun `parseVersionCode reads plain body format`() {
        assertEquals(10, UpdateChecker.parseVersionCode("versionCode: 10"))
        assertEquals(11, UpdateChecker.parseVersionCode("VersionCode 11"))
    }

    @Test
    fun `parseVersionCode returns 0 without metadata`() {
        assertEquals(0, UpdateChecker.parseVersionCode("See the commit history for details."))
    }
}
