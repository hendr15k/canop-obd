package com.canopobd.ui.datalog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.canopobd.R
import com.canopobd.data.model.DataRecord
import com.canopobd.data.repository.ExportFormat
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs in the ui-test-manifest ComponentActivity, not the Bluetooth/dashboard activity.
 *
 * Picker contract: opening Export shows selectable format rows, with CSV selected by
 * default. Picking a row only changes selection; sharing requires explicit confirmation.
 * Rows expose selected/disabled semantics and tags "export_format_<enum name>".
 * The picker container has tag "export_format_picker". Tags keep these tests independent
 * of German/English translations. No test launches a system share chooser.
 */
@RunWith(AndroidJUnit4::class)
class DataLogExportTest {
    @get:Rule
    val compose = createComposeRule()

    private var legacyExportCalls = 0

    @Test
    fun emptyRecordsDisableExport() {
        showDialog(emptyList())

        exportButton().assertIsDisplayed().assertIsNotEnabled()
        assertEquals(0, legacyExportCalls)
    }

    @Test
    fun recordedDataEnablesExport() {
        showDialog(listOf(record()))

        exportButton().assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun openingExportShowsAllFormatsWithoutExporting() {
        showDialog(listOf(record(latitude = 50.7753, longitude = 6.0839)))

        exportButton().performClick()

        compose.onNodeWithTag("export_format_picker").assertIsDisplayed()
        ExportFormat.entries.forEach { format ->
            formatOption(format).assertIsDisplayed().assertHasClickAction().assertIsEnabled()
        }
        formatOption(ExportFormat.CSV).assertIsSelected()
        assertEquals("Opening the picker must not generate the old CSV export", 0, legacyExportCalls)
    }

    @Test
    fun everyFormatCanBeSelectedWithoutLaunchingShare() {
        showDialog(listOf(record(latitude = 50.7753, longitude = 6.0839)))
        exportButton().performClick()
        compose.onNodeWithTag("export_format_picker").assertIsDisplayed()

        listOf(
            ExportFormat.JSON,
            ExportFormat.GPX_WITH_OBD,
            ExportFormat.TORQUE_CSV,
            ExportFormat.CSV,
        ).forEach { selected ->
            formatOption(selected).performClick().assertIsSelected()
            ExportFormat.entries.filter { it != selected }.forEach { other ->
                formatOption(other).assertIsNotSelected()
            }
            compose.onNodeWithTag("export_format_picker").assertIsDisplayed()
        }
        assertEquals("Changing the selected format must not call the CSV callback", 0, legacyExportCalls)
    }

    @Test
    fun missingGpsDisablesOnlyGpx() {
        showDialog(listOf(record()))
        exportButton().performClick()

        assertGpxDisabledAndOtherFormatsEnabled()
    }

    @Test
    fun incompleteGpsPairsCannotEnableGpx() {
        showDialog(
            listOf(
                record(latitude = 50.7753),
                record(longitude = 6.0839),
            ),
        )
        exportButton().performClick()

        // Coordinates from different samples must never be joined into a fake point.
        assertGpxDisabledAndOtherFormatsEnabled()
    }

    @Test
    fun nonFiniteOrOutOfRangeGpsCannotEnableGpx() {
        showDialog(
            listOf(
                record(latitude = Double.NaN, longitude = 6.0839),
                record(latitude = 50.7753, longitude = Double.POSITIVE_INFINITY),
                record(latitude = Double.NEGATIVE_INFINITY, longitude = 6.0839),
                record(latitude = 90.01, longitude = 6.0839),
                record(latitude = -90.01, longitude = 6.0839),
                record(latitude = 50.7753, longitude = 180.01),
                record(latitude = 50.7753, longitude = -180.01),
            ),
        )
        exportButton().performClick()

        assertGpxDisabledAndOtherFormatsEnabled()
    }

    @Test
    fun oneValidGpsSampleAmongInvalidSamplesEnablesGpx() {
        showDialog(
            listOf(
                record(),
                record(latitude = Double.NaN, longitude = 6.0839),
                record(latitude = 50.7753, longitude = 6.0839),
            ),
        )
        exportButton().performClick()

        formatOption(ExportFormat.GPX_WITH_OBD).assertIsEnabled().performClick().assertIsSelected()
        assertEquals(0, legacyExportCalls)
    }

    @Test
    fun clearingRecordsDisablesExportAfterRecomposition() {
        val records = mutableStateOf(listOf(record()))
        compose.setContent {
            MaterialTheme {
                DataLogDialog(
                    recordedData = records.value,
                    isRecording = false,
                    onDismiss = {},
                    onStartRecording = {},
                    onStopRecording = {},
                    onClearData = { records.value = emptyList() },
                    onExportData = {
                        legacyExportCalls++
                        ""
                    },
                )
            }
        }
        exportButton().assertIsEnabled()

        compose.runOnIdle { records.value = emptyList() }

        exportButton().assertIsNotEnabled()
        assertEquals(0, legacyExportCalls)
    }

    private fun showDialog(records: List<DataRecord>) {
        compose.setContent {
            MaterialTheme {
                DataLogDialog(
                    recordedData = records,
                    isRecording = false,
                    onDismiss = {},
                    onStartRecording = {},
                    onStopRecording = {},
                    onClearData = {},
                    onExportData = {
                        legacyExportCalls++
                        // A one-line response keeps the pre-picker implementation from
                        // launching a chooser while the missing-feature test goes RED.
                        ""
                    },
                )
            }
        }
    }

    private fun exportButton() = compose.onNodeWithText(
        InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.datalog_export),
    )

    private fun formatOption(format: ExportFormat) =
        compose.onNodeWithTag("export_format_${format.name}")

    private fun assertGpxDisabledAndOtherFormatsEnabled() {
        formatOption(ExportFormat.GPX_WITH_OBD).assertIsDisplayed().assertIsNotEnabled()
        listOf(ExportFormat.CSV, ExportFormat.JSON, ExportFormat.TORQUE_CSV).forEach { format ->
            formatOption(format).assertIsEnabled()
        }
        formatOption(ExportFormat.CSV).assertIsSelected()
        assertEquals(0, legacyExportCalls)
    }

    private fun record(latitude: Double? = null, longitude: Double? = null) = DataRecord(
        timestamp = 1_700_000_000_000L,
        rpm = 1_234.0,
        speed = 42.0,
        coolantTemp = 87.0,
        throttle = 18.0,
        fuelLevel = 65.0,
        batteryVoltage = 13.75,
        latitude = latitude,
        longitude = longitude,
    )
}
