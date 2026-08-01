package com.canopobd.viewmodel

import com.canopobd.data.model.DTCSeverity
import com.canopobd.data.model.DTCResponse
import com.canopobd.data.model.ProcessedDTC
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DTCProcessor {

    private val _processedDTCs = MutableStateFlow<List<ProcessedDTC>>(emptyList())
    val processedDTCs: StateFlow<List<ProcessedDTC>> = _processedDTCs

    private val _criticalDTCs = MutableStateFlow<List<ProcessedDTC>>(emptyList())
    val criticalDTCs: StateFlow<List<ProcessedDTC>> = _criticalDTCs

    private val _warningDTCs = MutableStateFlow<List<ProcessedDTC>>(emptyList())
    val warningDTCs: StateFlow<List<ProcessedDTC>> = _warningDTCs

    private val _infoDTCs = MutableStateFlow<List<ProcessedDTC>>(emptyList())
    val infoDTCs: StateFlow<List<ProcessedDTC>> = _infoDTCs

    private val knownDTCs = mapOf(
        "P0016" to ProcessedDTC("P0016", "Nockenwellen-Kurbelwellen-Korrelation Bank 1 Sensor A", DTCSeverity.CRITICAL, "Steuerkette", "Steuerkette, Kettenspanner und Sensoren prüfen"),
        "P0017" to ProcessedDTC("P0017", "Nockenwellen-Kurbelwellen-Korrelation Bank 1 Sensor B", DTCSeverity.CRITICAL, "Steuerkette", "Steuerkette und Nockenwellenposition prüfen"),
        "P0100" to ProcessedDTC("P0100", "Luftmassenmesser (MAF) - Stromkreisfehler", DTCSeverity.WARNING, "Sensor", "MAF-Sensor prüfen und reinigen"),
        "P0101" to ProcessedDTC("P0101", "Luftmassenmesser (MAF) - Leistungsbereich", DTCSeverity.WARNING, "Sensor", "MAF-Sensor prüfen, Luftfilter wechseln"),
        "P0102" to ProcessedDTC("P0102", "Luftmassenmesser (MAF) - Signaleingang niedrig", DTCSeverity.WARNING, "Sensor", "MAF-Sensor reinigen oder ersetzen"),
        "P0103" to ProcessedDTC("P0103", "Luftmassenmesser (MAF) - Signaleingang hoch", DTCSeverity.WARNING, "Sensor", "MAF-Sensor prüfen"),
        "P0116" to ProcessedDTC("P0116", "Kühlmitteltemperatur-Sensor - Plausibilitätsfehler", DTCSeverity.WARNING, "Sensor", "Temperatursensor prüfen"),
        "P0117" to ProcessedDTC("P0117", "Kühlmitteltemperatur-Sensor - Signaleingang niedrig", DTCSeverity.WARNING, "Sensor", "Kühlmitteltemperatursensor ersetzen"),
        "P0234" to ProcessedDTC("P0234", "Turbolader-Überladung (Overboost)", DTCSeverity.CRITICAL, "Turbo", "Wastegate und Ladedruckregelung prüfen"),
        "P0235" to ProcessedDTC("P0235", "Turbolader-Überladungs-Sensor A", DTCSeverity.WARNING, "Turbo", "Ladedrucksensor prüfen"),
        "P0340" to ProcessedDTC("P0340", "Nockenwellenpositionssensor - Stromkreisfehler", DTCSeverity.CRITICAL, "Sensor", "Sensor und Verkabelung prüfen"),
        "P0341" to ProcessedDTC("P0341", "Nockenwellenpositionssensor - Leistungsbereich", DTCSeverity.CRITICAL, "Sensor", "Sensor prüfen, Steuerkette inspizieren"),
        "P1100" to ProcessedDTC("P1100", "PCV-System (Crankcase Ventilation) Störung", DTCSeverity.WARNING, "PCV", "PCV-Ventil und Zylinderkopfhaube prüfen"),
        "P1101" to ProcessedDTC("P1101", "Ansaugluftsystem - Luftleck erkannt", DTCSeverity.WARNING, "Ansaugung", "Saugrohr und Dichtungen auf Luftleck prüfen"),
        "P1345" to ProcessedDTC("P1345", "Nockenwellen-Kurbelwellen-Phasenabweichung", DTCSeverity.CRITICAL, "Steuerkette", "Steuerkette und Kettenspanner ersetzen"),
        "P0171" to ProcessedDTC("P0171", "System zu mager (Bank 1)", DTCSeverity.WARNING, "Kraftstoff", "MAF, O2-Sensor, Kraftstoffdruck und Luftleck prüfen"),
        "P0172" to ProcessedDTC("P0172", "System zu fett (Bank 1)", DTCSeverity.WARNING, "Kraftstoff", "Einspritzventile, Kraftstoffdruck und O2-Sensor prüfen"),
        "P0420" to ProcessedDTC("P0420", "Katalysator-Wirkung unter Schwellenwert (Bank 1)", DTCSeverity.WARNING, "Abgas", "Katalysator prüfen, O2-Sensoren messen"),
        "P0562" to ProcessedDTC("P0562", "Systemspannung niedrig", DTCSeverity.INFO, "Elektrik", "Batterie und Lichtmaschine prüfen"),
        "P0130" to ProcessedDTC("P0130", "O2-Sensor Stromkreis (Bank 1 Sensor 1)", DTCSeverity.WARNING, "Sensor", "O2-Sensor prüfen und ggf. ersetzen")
    )

    fun processDTC(dtc: String): ProcessedDTC {
        val upperCode = dtc.trim().uppercase()
        return knownDTCs[upperCode] ?: ProcessedDTC(
            code = upperCode,
            description = "Astra J 1.4T DTC: $upperCode",
            severity = DTCSeverity.INFO,
            category = "Sonstige",
            recommendation = "Herstellerspezifischen Diagnose-Code nachschlagen"
        )
    }

    fun processAllDTCs(response: DTCResponse?) {
        val allCodes = ((response?.codes ?: emptyList()) + (response?.pendingCodes ?: emptyList()))
            .distinctBy { it.code.trim().uppercase() }
        val processed = allCodes.map { processDTC(it.code) }
        _processedDTCs.value = processed
        _criticalDTCs.value = processed.filter { it.severity == DTCSeverity.CRITICAL }
        _warningDTCs.value = processed.filter { it.severity == DTCSeverity.WARNING }
        _infoDTCs.value = processed.filter { it.severity == DTCSeverity.INFO || it.severity == DTCSeverity.PERFORMANCE }
    }
}
