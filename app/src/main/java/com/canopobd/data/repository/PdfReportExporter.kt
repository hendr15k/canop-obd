package com.canopobd.data.repository

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.canopobd.data.local.TripEntity
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PdfReportSummary(
    val title: String,
    val generatedAt: String,
    val file: File,
    val uri: android.net.Uri
)

data class DiagnosticSnapshotPoint(
    val label: String,
    val value: String,
    val unit: String
)

object PdfReportExporter {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 36f

    private val dateFmt: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm", Locale.GERMAN)
        .withZone(ZoneId.systemDefault())

    fun exportTripReport(
        context: Context,
        trips: List<TripEntity>,
        fileNamePrefix: String = "trip-report"
    ): PdfReportSummary? = try {
        val timestamp = Instant.now().epochSecond
        val file = File(context.cacheDir, "$fileNamePrefix-$timestamp.pdf")
        val doc = PdfDocument()
        renderTripDocument(doc, trips)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        PdfReportSummary(
            title = "Trip Report",
            generatedAt = dateFmt.format(Instant.now()),
            file = file,
            uri = uri
        )
    } catch (e: Exception) {
        android.util.Log.e("PdfReportExporter", "Failed to build trip PDF", e)
        null
    }

    fun exportDiagnosticReport(
        context: Context,
        dtcCodes: List<String>,
        dataPoints: List<DiagnosticSnapshotPoint>,
        fileNamePrefix: String = "diag-report"
    ): PdfReportSummary? = try {
        val timestamp = Instant.now().epochSecond
        val file = File(context.cacheDir, "$fileNamePrefix-$timestamp.pdf")
        val doc = PdfDocument()
        renderDiagnosticDocument(doc, dtcCodes, dataPoints)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        PdfReportSummary(
            title = "Diagnostic Report",
            generatedAt = dateFmt.format(Instant.now()),
            file = file,
            uri = uri
        )
    } catch (e: Exception) {
        android.util.Log.e("PdfReportExporter", "Failed to build diagnostic PDF", e)
        null
    }

    private fun renderTripDocument(doc: PdfDocument, trips: List<TripEntity>) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 11f
        }
        val headingPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            isFakeBoldText = true
        }
        val cellPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }
        val altRowPaint = Paint().apply { color = Color.rgb(245, 247, 250) }
        val hrPaint = Paint().apply { color = Color.rgb(220, 224, 230) }

        val builder = PdfPageBuilder(doc)
        var page = builder.newPage()
        var y = page.header(titlePaint, subtitlePaint, "Trip Report")

        if (trips.isEmpty()) {
            page.canvas.drawText("No trips recorded.", MARGIN, y + 14f, cellPaint)
            doc.finishPage(page.page)
            return
        }

        val totalKm = trips.sumOf { it.distanceKm.toDouble() }
        val totalFuel = trips.sumOf { it.fuelUsedLiters.toDouble() }
        val maxSpeed = trips.maxOfOrNull { it.maxSpeedKmh } ?: 0f
        val avgConsumption = if (totalKm > 0) totalFuel / totalKm * 100.0 else 0.0

        page.canvas.drawText("Overview", MARGIN, y, headingPaint); y += 6f
        page.canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, hrPaint); y += 14f
        page.canvas.drawText("Trips: ${trips.size}", MARGIN + 4f, y, cellPaint); y += 16f
        page.canvas.drawText("Distance: %.1f km".format(totalKm), MARGIN + 4f, y, cellPaint); y += 16f
        page.canvas.drawText("Fuel used: %.1f L".format(totalFuel), MARGIN + 4f, y, cellPaint); y += 16f
        page.canvas.drawText("Avg consumption: %.2f L/100km".format(avgConsumption), MARGIN + 4f, y, cellPaint); y += 16f
        page.canvas.drawText("Top speed: %.0f km/h".format(maxSpeed), MARGIN + 4f, y, cellPaint); y += 16f

        y += 12f
        page.canvas.drawText("Trips", MARGIN, y, headingPaint); y += 6f
        page.canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, hrPaint); y += 16f

        val colXs = floatArrayOf(MARGIN + 4f, MARGIN + 110f, MARGIN + 200f, MARGIN + 290f, MARGIN + 380f, MARGIN + 470f)
        val headers = arrayOf("Date", "Distance", "Fuel", "Avg", "Max", "Duration")
        headers.forEachIndexed { i, h ->
            page.canvas.drawText(h, colXs[i], y, headingPaint)
        }
        y += 14f
        page.canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, hrPaint); y += 10f

        var rowIndex = 0
        for (trip in trips.sortedByDescending { it.startTime }) {
            if (y > PAGE_H - MARGIN - 40f) {
                doc.finishPage(page.page)
                page = builder.newPage()
                y = page.header(titlePaint, subtitlePaint, "Trip Report (continued)")
            }
            if (rowIndex % 2 == 0) {
                page.canvas.drawRect(MARGIN, y - 12f, PAGE_W - MARGIN, y + 4f, altRowPaint)
            }
            rowIndex++
            page.canvas.drawText(dateFmt.format(Instant.ofEpochMilli(trip.startTime)), colXs[0], y, cellPaint)
            page.canvas.drawText("%.1f km".format(trip.distanceKm), colXs[1], y, cellPaint)
            page.canvas.drawText("%.1f L".format(trip.fuelUsedLiters), colXs[2], y, cellPaint)
            page.canvas.drawText("%.0f km/h".format(trip.avgSpeedKmh), colXs[3], y, cellPaint)
            page.canvas.drawText("%.0f km/h".format(trip.maxSpeedKmh), colXs[4], y, cellPaint)
            page.canvas.drawText(formatDurationMs(trip.endTime - trip.startTime), colXs[5], y, cellPaint)
            y += 18f
        }
        y += 18f
        page.canvas.drawText("Generated by canop-obd", MARGIN, y, subtitlePaint)

        doc.finishPage(page.page)
    }

    private fun renderDiagnosticDocument(
        doc: PdfDocument,
        dtcCodes: List<String>,
        dataPoints: List<DiagnosticSnapshotPoint>
    ) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 11f
        }
        val headingPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            isFakeBoldText = true
        }
        val cellPaint = Paint().apply { color = Color.BLACK; textSize = 11f }
        val errorPaint = Paint().apply { color = Color.rgb(211, 47, 47); textSize = 11f; isFakeBoldText = true }
        val okPaint = Paint().apply { color = Color.rgb(46, 125, 50); textSize = 11f; isFakeBoldText = true }
        val hrPaint = Paint().apply { color = Color.rgb(220, 224, 230) }

        val builder = PdfPageBuilder(doc)
        val page = builder.newPage()
        var y = page.header(titlePaint, subtitlePaint, "Diagnostic Report")

        page.canvas.drawText("Fault Codes", MARGIN, y, headingPaint); y += 6f
        page.canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, hrPaint); y += 16f

        if (dtcCodes.isEmpty()) {
            page.canvas.drawText("✓ No DTCs stored.", MARGIN + 4f, y, okPaint)
            y += 18f
        } else {
            dtcCodes.forEach { code ->
                page.canvas.drawText(code, MARGIN + 4f, y, errorPaint)
                y += 16f
                if (y > PAGE_H - 80f) return doc.finishPage(page.page)
            }
        }
        y += 14f

        page.canvas.drawText("Sensor Snapshot", MARGIN, y, headingPaint); y += 6f
        page.canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, hrPaint); y += 16f

        if (dataPoints.isEmpty()) {
            page.canvas.drawText("No sensor data captured.", MARGIN + 4f, y, cellPaint)
        } else {
            page.canvas.drawText("Label", MARGIN + 4f, y, headingPaint)
            page.canvas.drawText("Value", MARGIN + 220f, y, headingPaint)
            page.canvas.drawText("Unit", MARGIN + 360f, y, headingPaint)
            y += 16f
            page.canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, hrPaint); y += 10f
            for (p in dataPoints) {
                if (y > PAGE_H - 50f) break
                page.canvas.drawText(p.label, MARGIN + 4f, y, cellPaint)
                page.canvas.drawText(p.value, MARGIN + 220f, y, cellPaint)
                page.canvas.drawText(p.unit, MARGIN + 360f, y, cellPaint)
                y += 16f
            }
        }

        val footerY = PAGE_H - MARGIN - 10f
        page.canvas.drawText("Generated by canop-obd", MARGIN, footerY, subtitlePaint)

        doc.finishPage(page.page)
    }

    private fun formatDurationMs(ms: Long): String {
        if (ms <= 0L) return "—"
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return when {
            hours > 0 -> "%dh %02dm".format(hours, minutes)
            minutes > 0 -> "%dm".format(minutes)
            else -> "%ds".format(totalSeconds)
        }
    }

    private class PdfPageBuilder(val doc: PdfDocument) {
        private var pageNumber = 0
        fun newPage(): ActivePage {
            pageNumber++
            val info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create()
            return ActivePage(doc.startPage(info))
        }
    }

    private class ActivePage(val page: PdfDocument.Page) {
        val canvas: android.graphics.Canvas get() = page.canvas

        fun header(
            titlePaint: Paint,
            subtitlePaint: Paint,
            title: String
        ): Float {
            val titleBounds = Rect()
            titlePaint.getTextBounds(title, 0, title.length.coerceAtMost(10), titleBounds)
            canvas.drawText(title, MARGIN, MARGIN + titleBounds.height(), titlePaint)
            canvas.drawText(
                "canop-obd · generated ${dateFmt.format(Instant.now())}",
                MARGIN,
                MARGIN + titleBounds.height() + 18f,
                subtitlePaint
            )
            return MARGIN + titleBounds.height() + 36f
        }
    }
}
