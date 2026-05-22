package com.canopobd.data.model

enum class DTCFrequency(val label: String, val sortWeight: Int) {
    VERY_COMMON("Sehr haeufig", 5),
    COMMON("Haeufig", 4),
    OCCASIONAL("Gelegentlich", 3),
    RARE("Selten", 2),
    VERY_RARE("Sehr selten", 1)
}

enum class DTCCostRange(val label: String) {
    VERY_LOW("Unter 50 EUR"),
    LOW("50 - 150 EUR"),
    MEDIUM("150 - 500 EUR"),
    HIGH("500 - 1500 EUR"),
    VERY_HIGH("Ueber 1500 EUR")
}

data class AstraJDTCDetails(
    val code: String,
    val description: String,
    val severity: DTCSeverity,
    val system: String,
    val frequency: DTCFrequency,
    val typicalCause: String,
    val recommendedSolution: String,
    val estimatedCostWorkshop: DTCCostRange,
    val estimatedCostDIY: DTCCostRange,
    val isAstraJCommon: Boolean = false,
    val relatedCodes: List<String> = emptyList(),
    val technicalNotes: String = "",
    val udsDTCType: String = "P",
    val freezeFrameRelevant: Boolean = true
) {
    fun toDTCDetails(): DTCDetails = DTCDetails(
        code = code,
        description = description,
        system = system,
        severity = severity,
        possibleCauses = listOf(typicalCause),
        recommendedActions = listOf(recommendedSolution),
        isAstraJCommon = isAstraJCommon,
        relatedCodes = relatedCodes
    )

    fun toProcessedDTC(): ProcessedDTC = ProcessedDTC(
        code = code,
        description = description,
        severity = severity,
        category = system,
        recommendation = recommendedSolution
    )
}

object AstraJDTCCodes {

    private val database: Map<String, AstraJDTCDetails> by lazy {
        buildDatabase()
    }

    fun getDTCDetails(code: String): AstraJDTCDetails? =
        database[code.trim().uppercase()]

    fun getDTCsBySystem(system: String): List<AstraJDTCDetails> =
        database.values.filter { it.system.equals(system, ignoreCase = true) }
            .sortedByDescending { it.frequency.sortWeight }

    fun getCommonDTCs(): List<AstraJDTCDetails> =
        database.values.filter { it.isAstraJCommon }
            .sortedByDescending { it.frequency.sortWeight }

    fun getDTCsBySeverity(severity: DTCSeverity): List<AstraJDTCDetails> =
        database.values.filter { it.severity == severity }
            .sortedByDescending { it.frequency.sortWeight }

    fun getAllDTCs(): List<AstraJDTCDetails> =
        database.values.sortedByDescending { it.frequency.sortWeight }

    fun searchDTCs(query: String): List<AstraJDTCDetails> {
        val q = query.trim().uppercase()
        return database.values.filter {
            it.code.contains(q) || it.description.uppercase().contains(q)
        }.sortedByDescending { it.frequency.sortWeight }
    }

    fun getSystems(): List<String> =
        database.values.map { it.system }.distinct().sorted()

    fun isKnown(code: String): Boolean =
        database.containsKey(code.trim().uppercase())

    private fun buildDatabase(): Map<String, AstraJDTCDetails> {
        val entries = mutableListOf<AstraJDTCDetails>()

        // =====================================================================
        // P0016-P0017: Nockenwellen/Kurbelwellen-Korrelation
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0016",
            description = "Nockenwellen-Kurbelwellen-Korrelation Bank 1 Sensor A (Ansaugseite)",
            severity = DTCSeverity.CRITICAL,
            system = "Steuerkette",
            frequency = DTCFrequency.VERY_COMMON,
            typicalCause = "Kettenverlängerung durch Verschleiss, defekter Kettenspanner, verschlissene Nockenwellenrad-Verzahnung",
            recommendedSolution = "Steuerkette, Kettenspanner und Nockenwellen-Sensor prüfen; bei Verschleiss: Steuerkettensatz mit Spanner und Schienen erneuern",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            isAstraJCommon = true,
            relatedCodes = listOf("P0017", "P0340", "P0341", "P1345"),
            technicalNotes = "Sehr haeufig ab 80.000+ km beim A14NET. Kaltstart-Rattern ist erstes Symptom.",
            udsDTCType = "C"
        )
        entries += AstraJDTCDetails(
            code = "P0017",
            description = "Nockenwellen-Kurbelwellen-Korrelation Bank 1 Sensor B (Auslassseite)",
            severity = DTCSeverity.CRITICAL,
            system = "Steuerkette",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Kettenverlängerung, falsche Zahnstellung, verschlissene Auslass-Nockenwellenverstellung (DCVCP)",
            recommendedSolution = "Steuerkette und Phasenversteller prüfen; Auslassnockenwellen-Variator prüfen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            isAstraJCommon = true,
            relatedCodes = listOf("P0016", "P1345"),
            technicalNotes = "Oft in Kombination mit P0016. DCVCP-System bei A14NET dual phasenverstellbar."
        )

        // =====================================================================
        // P0100-P0104: MAF-Sensor
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0100",
            description = "Luftmassenmesser (MAF) - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Defekter MAF-Sensor, unterbrochene Verkabelung, Korrosion am Stecker",
            recommendedSolution = "MAF-Sensor-Stecker prüfen, Verkabelung auf Unterbrechung prüfen, MAF-Sensor reinigen oder ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0101", "P0102", "P0103"),
            technicalNotes = "Bei A14NET oft durch Ölfeuchte am Stecker oder Verschmutzung des Heizdrahtes."
        )
        entries += AstraJDTCDetails(
            code = "P0101",
            description = "Luftmassenmesser (MAF) - Leistungsbereich / Plausibilität",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "MAF-Sensor verschmutzt, veralteter Sensor, Luftfilter verstopft, Ansaugluftleck",
            recommendedSolution = "MAF-Sensor mit Kontaktreiniger reinigen, Luftfilter wechseln, Ansaugstrecke auf Dichtheit prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0100", "P0102", "P0171"),
            technicalNotes = "MAF-Reinigung mit Isopropanol oder MAF-Reiniger. Kein Druckluft verwenden!"
        )
        entries += AstraJDTCDetails(
            code = "P0102",
            description = "Luftmassenmesser (MAF) - Signaleingang niedrig",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "MAF-Sensor verschmutzt oder defekt, zu wenig Luftstrom, verstopfter Luftfilter",
            recommendedSolution = "Luftfilter prüfen/tauschen, MAF-Sensor reinigen, bei anhaltendem Fehler: MAF ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0100", "P0101")
        )
        entries += AstraJDTCDetails(
            code = "P0103",
            description = "Luftmassenmesser (MAF) - Signaleingang hoch",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "MAF-Sensor Kurzschluss, defekte Verkabelung, Kurzschluss gegen Versorgungsspannung",
            recommendedSolution = "Verkabelung prüfen, MAF-Sensor auf Kurzschluss testen, ggf. ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0100", "P0101")
        )
        entries += AstraJDTCDetails(
            code = "P0104",
            description = "Luftmassenmesser (MAF) - Intermittierendes Signal",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Lockere Verkabelung, Korrosion am Stecker, intermittierender Sensorfehler",
            recommendedSolution = "Stecker und Verkabelung auf Festigkeit prüfen, Kontakte reinigen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0100", "P0102")
        )

        // =====================================================================
        // P0110-P0115: Temperatursensoren
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0110",
            description = "Ansauglufttemperatur-Sensor 1 (IAT) - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defekter IAT-Sensor, Unterbrechung in der Verkabelung, defekter Stecker",
            recommendedSolution = "IAT-Sensor und Verkabelung prüfen, Sensor bei Bedarf ersetzen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0112", "P0113")
        )
        entries += AstraJDTCDetails(
            code = "P0111",
            description = "Ansauglufttemperatur-Sensor 1 (IAT) - Plausibilitätsfehler",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "IAT-Sensor langsam reagierend, Verkokung, interner Sensorfehler",
            recommendedSolution = "IAT-Sensor prüfen und reinigen, bei Bedarf ersetzen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0110", "P0112", "P0113")
        )
        entries += AstraJDTCDetails(
            code = "P0112",
            description = "Ansauglufttemperatur-Sensor 1 (IAT) - Signaleingang niedrig",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "IAT-Sensor Kurzschluss gegen Masse, defekter Sensor, Steckerverbindung",
            recommendedSolution = "IAT-Sensor auf Kurzschluss prüfen, Verkabelung testen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0110")
        )
        entries += AstraJDTCDetails(
            code = "P0113",
            description = "Ansauglufttemperatur-Sensor 1 (IAT) - Signaleingang hoch",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "IAT-Sensor offen, Unterbrechung der Verkabelung, korrodierte Kontakte",
            recommendedSolution = "Verkabelung auf Unterbrechung prüfen, Stecker reinigen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0110")
        )
        entries += AstraJDTCDetails(
            code = "P0114",
            description = "Ansauglufttemperatur-Sensor 1 (IAT) - Intermittierend",
            severity = DTCSeverity.INFO,
            system = "Sensor",
            frequency = DTCFrequency.RARE,
            typicalCause = "Lockere Verkabelung, Temperaturbedingte Unterbrechung",
            recommendedSolution = "Verkabelung bei verschwundenem Fehler warm/altern testen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0110", "P0113")
        )
        entries += AstraJDTCDetails(
            code = "P0115",
            description = "Kühlmitteltemperatur-Sensor 1 (ECT) - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Defekter ECT-Sensor (im Thermostatgehäuse), Verkabelungsschaden, Steckerverbindung",
            recommendedSolution = "ECT-Sensor im Thermostat prüfen, Verkabelung testen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0116", "P0117", "P0118"),
            technicalNotes = "Beim A14NET ist der ECT-Sensor im Thermostat integriert. Oft Ursache fuer Kaltstartprobleme."
        )
        entries += AstraJDTCDetails(
            code = "P0116",
            description = "Kühlmitteltemperatur-Sensor 1 (ECT) - Plausibilitätsfehler",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Sensor langsam reagierend, Thermostat klemmt offen, Luft im Kühlsystem",
            recommendedSolution = "Thermostat-Öffnungsverhalten prüfen, ECT-Sensor testen, Kühlsystem entlüften",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0115", "P0117", "P0118"),
            technicalNotes = "Plausibilitätsprüfung: ECT muss innerhalb definierter Zeit eine bestimmte Änderungsrate erreichen."
        )
        entries += AstraJDTCDetails(
            code = "P0117",
            description = "Kühlmitteltemperatur-Sensor 1 (ECT) - Signaleingang niedrig",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "ECT-Sensor Kurzschluss gegen Masse, defekter Sensor, fehlerhafte Verkabelung",
            recommendedSolution = "ECT-Sensor und Verkabelung prüfen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0115", "P0116")
        )
        entries += AstraJDTCDetails(
            code = "P0118",
            description = "Kühlmitteltemperatur-Sensor 1 (ECT) - Signaleingang hoch",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "ECT-Sensor offen, Unterbrechung in der Verkabelung, korrodierte Kontakte",
            recommendedSolution = "Verkabelung auf Unterbrechung prüfen, Stecker reinigen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0115", "P0117")
        )

        // =====================================================================
        // P0171/P0172: Kraftstoffgemisch (mager/fett)
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0171",
            description = "System zu mager (Bank 1) - Kraftstofftrimmung zu hoch",
            severity = DTCSeverity.WARNING,
            system = "Kraftstoff",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Luftleck (Saugrohr, Dichtung), verschmutzter MAF-Sensor, defekter Kraftstoffdruckregler, undichte Kraftstoffeinspritzventile",
            recommendedSolution = "Vakuumtest der Ansaugstrecke, MAF-Sensor prüfen, Kraftstoffdruck messen, Kraftstofftrimmungswerte (STFT/LTFT) auswerten",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0172", "P0101", "P0174"),
            technicalNotes = "Prüfe STFT und LTFT: Werte über +15% total deuten auf Luftleck oder mangelnde Kraftstoffzufuhr hin. Beim A14NET oft durch poröse PCV-Schläuche."
        )
        entries += AstraJDTCDetails(
            code = "P0172",
            description = "System zu fett (Bank 1) - Kraftstofftrimmung zu niedrig",
            severity = DTCSeverity.WARNING,
            system = "Kraftstoff",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defekter O2-Sensor, undichte Kraftstoffeinspritzventile, hoher Kraftstoffdruck, verstopfter Luftfilter",
            recommendedSolution = "O2-Sensor (Lambda) prüfen, Kraftstoffdruck und Undichtigkeit der Einspritzventile testen, Luftfilter wechseln",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0171", "P0130", "P0131"),
            technicalNotes = "STFT/LTFT unter -15% total: zu viel Kraftstoff oder zu wenig Luft. Bei A14NET prüfe ob Kraftstoffdruckregler Vakuumleitung undicht ist."
        )

        // =====================================================================
        // P0234/P0235: Turbolader-Überladung
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0234",
            description = "Turbolader-Überladung (Overboost) - Ladedruck zu hoch",
            severity = DTCSeverity.CRITICAL,
            system = "Turbo",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Wastegate-Stellglied klemmt geschlossen, defekter Ladedrucksensor, Vakuumleitung zum Wastegate undicht",
            recommendedSolution = "Wastegate-Stellglied auf Freigang prüfen, Vakuumleitungen kontrollieren, Ladedrucksensor testen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            isAstraJCommon = true,
            relatedCodes = listOf("P0235", "P0299"),
            technicalNotes = "Beim A14NET: BorgWarner KP39. Max. Boost ca. 1.0 bar (normal), Overboost 1.2 bar für max. 10s. Bei Dauer-Overboost: Wastegate-Feder oder Stellglied prüfen."
        )
        entries += AstraJDTCDetails(
            code = "P0235",
            description = "Turbolader-Überladungs-Sensor A - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Turbo",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Defekter Ladedrucksensor (Boost-Sensor), fehlerhafte Verkabelung, Vakuumleck",
            recommendedSolution = "Ladedrucksensor prüfen, Verkabelung testen, Vakuumleitungen auf Dichtheit prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0234"),
            technicalNotes = "Boost-Sensor am Ansaugkrümmer montiert. Bei diesem DTC kann der Ladedruck nicht korrekt geregelt werden."
        )

        // =====================================================================
        // P0299: Turbolader-Unterdruck
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0299",
            description = "Turbolader/Abgaslader - Unterdruck (Underboost)",
            severity = DTCSeverity.WARNING,
            system = "Turbo",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Wastegate undicht, undichte Ladeluftkühler-Verbindungen, Riss im Ansaugschlauch, Verschleiss des Turbo-Laufrads",
            recommendedSolution = "Drucktest des gesamten Ansaug- und Ladedrucksystems, Wastegate prüfen, Ladeluftkühler auf Dichtheit testen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            isAstraJCommon = true,
            relatedCodes = listOf("P0234", "P0235"),
            technicalNotes = "Beim A14NET prüfe: Ladedruck-Soll vs. Ist. Bei >30% Abweichung: Wastegate-Blase/Dichtung, O-Ring am Stellglied. Oft ab 80k km."
        )

        // =====================================================================
        // P0300-P0304: Zündaussetzer
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0300",
            description = "Zufällige / mehrere Zündaussetzer erkannt",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Verschlissene Zündkerzen, defekte Zündspulen, Kraftstoffmangel, Kompressionsverlust",
            recommendedSolution = "Zündkerzen-Zustand prüfen, Zündspulen testen, Kompressionsdruck messen, Kraftstoffsystem prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0301", "P0302", "P0303", "P0304"),
            technicalNotes = "Beim A14NET: Zündkerzen-Intervall 30.000-60.000 km (NGK LZKR6AP-11G). Prüfe auch Kraftstoffqualität (95 RON min)."
        )
        entries += AstraJDTCDetails(
            code = "P0301",
            description = "Zündaussetzer Zylinder 1 erkannt",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Verschlissene Zündkerze Zyl. 1, defekte Zündspule Zyl. 1, Einspritzventil Zyl. 1 undicht",
            recommendedSolution = "Zündkerze und Zündspule Zyl. 1 prüfen, Spulen mit anderem Zylinder tauschen zur Fehlersuche, Kompression messen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0300", "P0302", "P0303", "P0304"),
            technicalNotes = "Spulentausch zwischen Zylindern hilft bei Fehlersuche: wandert der Fehler mit der Spule, ist diese defekt."
        )
        entries += AstraJDTCDetails(
            code = "P0302",
            description = "Zündaussetzer Zylinder 2 erkannt",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Verschlissene Zündkerze Zyl. 2, defekte Zündspule Zyl. 2, Einspritzventil Zyl. 2 undicht",
            recommendedSolution = "Zündkerze und Zündspule Zyl. 2 prüfen, Spulen mit anderem Zylinder tauschen zur Fehlersuche, Kompression messen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0300", "P0301", "P0303", "P0304")
        )
        entries += AstraJDTCDetails(
            code = "P0303",
            description = "Zündaussetzer Zylinder 3 erkannt",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Verschlissene Zündkerze Zyl. 3, defekte Zündspule Zyl. 3, Einspritzventil Zyl. 3 undicht",
            recommendedSolution = "Zündkerze und Zündspule Zyl. 3 prüfen, Spulen mit anderem Zylinder tauschen zur Fehlersuche, Kompression messen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0300", "P0301", "P0302", "P0304")
        )
        entries += AstraJDTCDetails(
            code = "P0304",
            description = "Zündaussetzer Zylinder 4 erkannt",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Verschlissene Zündkerze Zyl. 4, defekte Zündspule Zyl. 4, Einspritzventil Zyl. 4 undicht",
            recommendedSolution = "Zündkerze und Zündspule Zyl. 4 prüfen, Spulen mit anderem Zylinder tauschen zur Fehlersuche, Kompression messen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0300", "P0301", "P0302", "P0303")
        )

        // =====================================================================
        // P0340/P0341: Nockenwellenposition
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0340",
            description = "Nockenwellenpositionssensor - Stromkreisfehler",
            severity = DTCSeverity.CRITICAL,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Defekter Nockenwellenpositionssensor, Unterbrechung der Verkabelung, Kurzschluss, Steuerkettenverschleiss",
            recommendedSolution = "Nockenwellenpositionssensor prüfen, Verkabelung testen, Steuerkettenspannung kontrollieren",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0341", "P0016", "P0017", "P1345"),
            technicalNotes = "Beim A14NET: Sensor sitzt am Zylinderkopf. Häufig kombiniert mit Steuerkettentausch erforderlich."
        )
        entries += AstraJDTCDetails(
            code = "P0341",
            description = "Nockenwellenpositionssensor - Leistungsbereich / Plausibilität",
            severity = DTCSeverity.CRITICAL,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Steuerkette verzögert, Nockenwellenverstellung defekt (DCVCP), Sensor verschmutzt, Magnetabstand zu groß",
            recommendedSolution = "Sensor auf Abstand und Verschmutzung prüfen, DCVCP-Phasenversteller testen, Steuerkettenspannung messen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            isAstraJCommon = true,
            relatedCodes = listOf("P0340", "P0016", "P0017", "P1345"),
            technicalNotes = "Typischer A14NET-Fehler ab 80k-150k km. Prüfe Zahnradspiel mit der Handkurbel."
        )

        // =====================================================================
        // P0420: Katalysator
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0420",
            description = "Katalysator-Wirkung unter Schwellenwert (Bank 1)",
            severity = DTCSeverity.WARNING,
            system = "Abgas",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Katalysator verschlissen oder beschädigt, defekter Lambdasonde (Hinterkat), Kraftstoffverbrauch",
            recommendedSolution = "Lambdasonden-Signale (Vorkat und Nachkat) vergleichen, Katalysator-Effizienz prüfen, ggf. Katalysator ersetzen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.HIGH,
            isAstraJCommon = true,
            relatedCodes = listOf("P0130", "P0131", "P0136"),
            technicalNotes = "Beim A14NET: Nachkat-O2-Sensor Spannung sollte sich langsam ändern. Bei schnellem Wechsel: Kat-Schaden. Kat-Lebensdauer >150k km bei guter Pflege."
        )

        // =====================================================================
        // P0562: Systemspannung
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0562",
            description = "Systemspannung niedrig",
            severity = DTCSeverity.INFO,
            system = "Elektrik",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Schwache Batterie, defekte Lichtmaschine (Alternator), Korrosion an Klemmen, schlechte Masseverbindung",
            recommendedSolution = "Batteriespannung bei laufendem Motor messen (soll >13.5V), Batterie-Zustandscheck, Lichtmaschine prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0563"),
            technicalNotes = "Beim A14NET: Batterie 70 Ah, Alternator 14V. Niedrige Spannung kann diverse Fehlfunktionen verursachen."
        )

        // =====================================================================
        // P1100/P1101: PCV-System
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P1100",
            description = "PCV-System (Crankcase Ventilation) Störung",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Verstopftes oder defektes PCV-Ventil im Zylinderkopfdeckel, Überdruck im Kurbelgehäuse, verstopfte Rückführleitung",
            recommendedSolution = "PCV-Ventil im Zylinderkopfdeckel prüfen, Kurbelgehäusedruck messen, Rückführleitung auf Verstopfung prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P1101"),
            technicalNotes = "Beim A14NET: PCV-Ventil ist im Zylinderkopfdeckel integriert. Kompletter Zylinderkopfdeckeltausch erforderlich. Typisch ab 60k-100k km."
        )
        entries += AstraJDTCDetails(
            code = "P1101",
            description = "Ansaugluftsystem - Luftleck erkannt (Plausibilität)",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Luftleck im Ansaugsystem, poröse PCV-Schläuche, undichte Dichtungen am Saugrohr, defekter Bremskraftverstärker",
            recommendedSolution = "Ansaugstrecke mit Rauchprüfung auf Luftleck testen, alle Schläuche und Dichtungen prüfen, Vakuumtest",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P1100", "P0171"),
            technicalNotes = "Oft in Kombination mit P0171 (mager). Beim A14NET: prüfe besonders die PCV-Schläuche und die Verbindung zum Ansaugkrümmer."
        )

        // =====================================================================
        // P1345: Nockenwellen-Kurbelwellen-Phasenabweichung
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P1345",
            description = "Nockenwellen-Kurbelwellen-Phasenabweichung (GM-spezifisch)",
            severity = DTCSeverity.CRITICAL,
            system = "Steuerkette",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Kettenverlängerung, defekter Kettenspanner, DCVCP-Variator verschlissen, Zahnradspiel zu groß",
            recommendedSolution = "Steuerkette, Kettenspanner, Führungschiene und DCVCP-Phasenversteller prüfen und bei Bedarf erneuern",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.HIGH,
            isAstraJCommon = true,
            relatedCodes = listOf("P0016", "P0017", "P0340", "P0341"),
            technicalNotes = "GM-spezifischer Code (P1xxx). Sehr häufig beim A14NET ab 80k km. Kettenkit mit allen Teilen (Kette, Spanner, Schienen, ggf. Ritzel) erneuern."
        )

        // =====================================================================
        // Weitere haeufige Astra J 1.4T DTCs
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0130",
            description = "O2-Sensor Stromkreisfehler (Bank 1 Sensor 1)",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defekter Vorkat-Lambdasonde, fehlerhafte Verkabelung, Kurzschluss",
            recommendedSolution = "O2-Sensor-Spannung messen (soll 0.1-0.9V wechselnd), Verkabelung prüfen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0131", "P0132", "P0420"),
            technicalNotes = "Beim A14NET: Vorkat-Breitband-Lambda 4-Draht. Heizwiderstand ca. 2-5 Ohm."
        )
        entries += AstraJDTCDetails(
            code = "P0131",
            description = "O2-Sensor Spannung niedrig (Bank 1 Sensor 1)",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "O2-Sensor defekt, Kraftstoffmangel, Luftleck vor dem Sensor",
            recommendedSolution = "Kraftstoffdruck prüfen, Luftleck suchen, O2-Sensor-Spannung analysieren",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0130", "P0171")
        )
        entries += AstraJDTCDetails(
            code = "P0132",
            description = "O2-Sensor Spannung hoch (Bank 1 Sensor 1)",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "O2-Sensor defekt, Kraftstoffdruck zu hoch, undichte Einspritzventile",
            recommendedSolution = "Kraftstoffdruck messen, Einspritzventile prüfen, O2-Sensor-Signalmuster analysieren",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0130", "P0172")
        )
        entries += AstraJDTCDetails(
            code = "P0133",
            description = "O2-Sensor Langsam (Bank 1 Sensor 1)",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "O2-Sensor verschlissen, Verschmutzung der Sensorpitze, Alterung",
            recommendedSolution = "O2-Sensor-Antwortzeit prüfen, bei Langsamkeit: Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0130", "P0420")
        )
        entries += AstraJDTCDetails(
            code = "P0136",
            description = "O2-Sensor Stromkreisfehler (Bank 1 Sensor 2 - Nachkat)",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defekter Nachkat-Lambdasonde, Verkabelungsschaden, Korrosion",
            recommendedSolution = "Nachkat-Sensor prüfen, Verkabelung testen, Sensor bei Bedarf ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0420")
        )

        // Weiterer häufiger Code: P0010/P0011 VVT
        entries += AstraJDTCDetails(
            code = "P0010",
            description = "Nockenwellenverstellung (VVT) Stellglied Bank 1 - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "DCVCP-Ventil (Variable Nockenwellenverstellung) defekt, Verkabelung, Ölverschmutzung im Ventil",
            recommendedSolution = "DCVCP-Stellventil prüfen, Verkabelung testen, Motoröl-Qualität prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0011", "P0013", "P0014"),
            technicalNotes = "Beim A14NET: DCVCP (Dual Continuous Variable Cam Phasing). Benötigt sauberes Dexos2 5W-30 Öl."
        )
        entries += AstraJDTCDetails(
            code = "P0011",
            description = "Nockenwellenverstellung (VVT) Ansaugseite - Positionsabweichung",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "DCVCP-Ölversorgung verstopft, verschlissener Phasenversteller, falsche Ölviskosität",
            recommendedSolution = "Ölwechsel durchführen (Dexos2 5W-30), DCVCP-Ölleitung prüfen, Phasenversteller testen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0010", "P0013", "P0014"),
            technicalNotes = "Erst Ölwechsel mit korrektem Öl und Filter durchführen, bevor teure Bauteile getauscht werden!"
        )
        entries += AstraJDTCDetails(
            code = "P0013",
            description = "Nockenwellenverstellung (VVT) Auslassseite - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "DCVCP-Auslassventil defekt, Verkabelung, Ölverschmutzung",
            recommendedSolution = "DCVCP-Auslassventil prüfen, Verkabelung testen, Ölwechsel durchführen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0014", "P0010", "P0011")
        )
        entries += AstraJDTCDetails(
            code = "P0014",
            description = "Nockenwellenverstellung (VVT) Auslassseite - Positionsabweichung",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "DCVCP-Auslassventil verschlissen, Ölverunreinigung, falsche Ölviskosität",
            recommendedSolution = "Ölwechsel (Dexos2 5W-30), DCVCP-Ventil reinigen oder ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0013", "P0010", "P0011")
        )

        // Kraftstoffsystem
        entries += AstraJDTCDetails(
            code = "P0087",
            description = "Kraftstoffschienendruck zu niedrig",
            severity = DTCSeverity.WARNING,
            system = "Kraftstoff",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defekte Kraftstoffpumpe, verstopftes Kraftstofffilter, undichte Kraftstoffleitung, defekter Druckregler",
            recommendedSolution = "Kraftstoffdruck messen (idle: 350-450 kPa, WOT: 400-550 kPa), Kraftstoffpumpe und Filter prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P0088", "P0171"),
            technicalNotes = "Kraftstoffpumpe im Tank (Pumpe-Einheit). Kraftstofffilter integriert."
        )
        entries += AstraJDTCDetails(
            code = "P0088",
            description = "Kraftstoffschienendruck zu hoch",
            severity = DTCSeverity.WARNING,
            system = "Kraftstoff",
            frequency = DTCFrequency.RARE,
            typicalCause = "Kraftstoffdruckregler klemmt geschlossen, verstopfte Rücklaufleitung, defekte Kraftstoffpumpe",
            recommendedSolution = "Kraftstoffdruckregler prüfen, Rücklaufleitung auf Verstopfung testen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P0087")
        )

        // Turbolader-bezogene weitere Codes
        entries += AstraJDTCDetails(
            code = "P2261",
            description = "Turbolader Wastegate - Mechanische Störung",
            severity = DTCSeverity.WARNING,
            system = "Turbo",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Wastegate-Stellglied mechanischer Defekt, Federermüdung, Korrosion am Wastegate-Hebel",
            recommendedSolution = "Wastegate-Stellglied auf mechanischen Freigang prüfen, Hebel und Lagerung inspizieren",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.HIGH,
            isAstraJCommon = true,
            relatedCodes = listOf("P0234", "P0299"),
            technicalNotes = "Beim A14NET: Wastegate-Stellglied mit Vakuum angesteuert. Prüfe Vakuum-Leitung, Membran und Stellglied-Hebel mechanisch."
        )

        // Lambdasonde Heizung
        entries += AstraJDTCDetails(
            code = "P0030",
            description = "Heizung Lambdasonde Bank 1 Sensor 1 - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defekte Heizung im O2-Sensor, Unterbrechung der Verkabelung",
            recommendedSolution = "Heizwiderstand des O2-Sensors messen (2-5 Ohm), Verkabelung prüfen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0130")
        )
        entries += AstraJDTCDetails(
            code = "P0031",
            description = "Heizung Lambdasonde Bank 1 Sensor 1 - Niedrig",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.RARE,
            typicalCause = "Heizung O2-Sensor Kurzschluss gegen Masse",
            recommendedSolution = "Verkabelung und O2-Sensor prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0030")
        )

        // Leerlaufregelung
        entries += AstraJDTCDetails(
            code = "P0505",
            description = "Leerlaufregelung (IAC) - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defekter Leerlaufregler, verstopfter Regler, Drosselklappenverschmutzung",
            recommendedSolution = "Drosselklappenventil und Leerlaufregler prüfen, Drosselklappe reinigen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0506", "P0507")
        )
        entries += AstraJDTCDetails(
            code = "P0506",
            description = "Leerlaufdrehzahl zu niedrig",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Verstopfte Drosselklappe, Luftleck, Leerlaufregler klemmt",
            recommendedSolution = "Drosselklappenventil reinigen, Leerlaufdrehzahl-Erlernung durchführen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0505", "P0507")
        )
        entries += AstraJDTCDetails(
            code = "P0507",
            description = "Leerlaufdrehzahl zu hoch",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Luftleck im Ansaugsystem, Leerlaufregler klemmt offen, Drosselklappenventil defekt",
            recommendedSolution = "Ansaugstrecke auf Luftleck prüfen, Leerlaufregler und Drosselklappe testen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0505", "P0506", "P1101")
        )

        // Getriebebezogene (U-Codes für Getriebesteuerung)
        entries += AstraJDTCDetails(
            code = "P0700",
            description = "Getriebesteuerung (TCM) - Fehlercodes vorhanden",
            severity = DTCSeverity.WARNING,
            system = "Getriebe",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Fehler im Getriebesteuergerät, Sensorfehler, Solenoid defekt",
            recommendedSolution = "Getriebe-Spezialdiagnose mit Hersteller-Tool (GDS2/Opel Tech2) durchführen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.HIGH,
            relatedCodes = listOf("P0730"),
            technicalNotes = "P0700 ist ein Sammelcode. Für Details: Getriebespezifische Codes mit Herstellerdiagnose auslesen."
        )
        entries += AstraJDTCDetails(
            code = "P0730",
            description = "Falsches Getriebeübersetzungsverhältnis",
            severity = DTCSeverity.WARNING,
            system = "Getriebe",
            frequency = DTCFrequency.RARE,
            typicalCause = "Verschlissene Getriebe, defektes Getriebeöl, Solenoid defekt, mechanischer Verschleiss",
            recommendedSolution = "Getriebeöl-Zustand prüfen, Getriebe-Spezialdiagnose, ggf. Getriebe öffnen",
            estimatedCostWorkshop = DTCCostRange.VERY_HIGH,
            estimatedCostDIY = DTCCostRange.VERY_HIGH,
            relatedCodes = listOf("P0700")
        )

        // EVAP System
        entries += AstraJDTCDetails(
            code = "P0441",
            description = "EVAP-System - Falsche Purge-Strommenge",
            severity = DTCSeverity.INFO,
            system = "Abgas",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defektes EVAP-Purgeventil, verstopfte Leitung, Tankdeckel undicht",
            recommendedSolution = "Tankdeckel prüfen, Purgeventil testen, Leitungen auf Dichtheit prüfen",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0442", "P0455")
        )
        entries += AstraJDTCDetails(
            code = "P0442",
            description = "EVAP-System - Kleines Drossellochleck erkannt",
            severity = DTCSeverity.INFO,
            system = "Abgas",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Undichter Tankdeckel, kleine Undichtigkeit im EVAP-System",
            recommendedSolution = "Tankdeckel-Verschluss prüfen, Tankdeckel-Dichtung erneuern",
            estimatedCostWorkshop = DTCCostRange.LOW,
            estimatedCostDIY = DTCCostRange.VERY_LOW,
            relatedCodes = listOf("P0441", "P0455")
        )
        entries += AstraJDTCDetails(
            code = "P0455",
            description = "EVAP-System - Großes Drossellochleck erkannt",
            severity = DTCSeverity.WARNING,
            system = "Abgas",
            frequency = DTCFrequency.RARE,
            typicalCause = "Fehlender oder lockerer Tankdeckel, Riss in EVAP-Leitung, defektes Canister-Purgeventil",
            recommendedSolution = "Tankdeckel prüfen und festziehen, EVAP-System mit Rauchprüfung auf Lecks testen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0441", "P0442")
        )

        // Überhitzung / Kühlsystem
        entries += AstraJDTCDetails(
            code = "P0128",
            description = "Kühlmitteltemperatur unter Thermostat-Solltemperatur",
            severity = DTCSeverity.INFO,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Thermostat klemmt offen, Kühlmittelstand zu niedrig, falscher ECT-Sensor",
            recommendedSolution = "Thermostat-Öffnungsverhalten prüfen, Kühlmittelstand und -qualität kontrollieren",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0116", "P0117"),
            technicalNotes = "Motor erreicht Betriebstemperatur nicht. Erhöhter Kraftstoffverbrauch."
        )

        // Drosselklappe
        entries += AstraJDTCDetails(
            code = "P0220",
            description = "Drosselklappenpositionssensor B - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.RARE,
            typicalCause = "Defekter Drosselklappensensor, Verkabelungsfehler, Elektronik defekt",
            recommendedSolution = "Drosselklappeneinheit prüfen, ggf. komplette Drosselklappe ersetzen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P0221", "P0120")
        )
        entries += AstraJDTCDetails(
            code = "P2100",
            description = "Drosselklappen-Stellglied - Stromkreisfehler",
            severity = DTCSeverity.CRITICAL,
            system = "Motor",
            frequency = DTCFrequency.RARE,
            typicalCause = "Defektes Drosselklappen-Stellglied, interner Kurzschluss, Verkabelung",
            recommendedSolution = "Drosselklappeneinheit komplett prüfen und ersetzen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P2101", "P2102"),
            technicalNotes = "Beim A14NET: Elektronische Drosselklappe. Bei Defekt: komplette Einheit tauschen und adaption durchführen."
        )

        // Kraftstoffdruck
        entries += AstraJDTCDetails(
            code = "P0190",
            description = "Kraftstoffraildrucksensor - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Kraftstoff",
            frequency = DTCFrequency.RARE,
            typicalCause = "Defekter Kraftstoffdrucksensor, Verkabelungsfehler",
            recommendedSolution = "Kraftstoffdrucksensor und Verkabelung prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0191", "P0087")
        )

        // EGR
        entries += AstraJDTCDetails(
            code = "P0401",
            description = "EGR - Unzureichende Durchflussmenge",
            severity = DTCSeverity.WARNING,
            system = "Abgas",
            frequency = DTCFrequency.RARE,
            typicalCause = "Verstopfter EGR-Ventil, Ablagerungen im Ansaugkrümmer, defektes Stellglied",
            recommendedSolution = "EGR-Ventil reinigen, Ansaugkrümmer auf Verkokung prüfen, EGR-Systemtest",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0402", "P0403"),
            technicalNotes = "Beim A14NET: EGR ist elektrisch geregelt. Ablagerungen im Ansaugkrümmer sind typisch."
        )

        // Unbekannte / UDS-Codes
        entries += AstraJDTCDetails(
            code = "U0100",
            description = "Kommunikationsverlust mit Steuergerät A (ECM/PCM)",
            severity = DTCSeverity.WARNING,
            system = "Kommunikation",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "CAN-Bus-Unterbrechung, defektes Steuergerät, Verkabelungsfehler, Masseschluss",
            recommendedSolution = "CAN-Bus-Verbindungen prüfen, Steuergeräte-Kommunikation testen, Steckverbindungen kontrollieren",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.HIGH,
            udsDTCType = "U",
            technicalNotes = "U-Code (Network). Bei diesem Fehler können verschiedene Systeme Ausfälle zeigen."
        )
        entries += AstraJDTCDetails(
            code = "U0121",
            description = "Kommunikationsverlust mit ABS-Steuergerät",
            severity = DTCSeverity.WARNING,
            system = "Kommunikation",
            frequency = DTCFrequency.RARE,
            typicalCause = "CAN-Bus-Unterbrechung, ABS-Steuergerät-Defekt, Verkabelungsschaden",
            recommendedSolution = "ABS-Steuergerät und CAN-Bus-Verbindungen prüfen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.HIGH,
            udsDTCType = "U"
        )
        entries += AstraJDTCDetails(
            code = "U0140",
            description = "Kommunikationsverlust mit Body Control Module (BCM)",
            severity = DTCSeverity.WARNING,
            system = "Kommunikation",
            frequency = DTCFrequency.RARE,
            typicalCause = "CAN-Bus-Fehler, BCM-Defekt, Verkabelungsschaden",
            recommendedSolution = "BCM und CAN-Bus-Verbindungen prüfen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.HIGH,
            udsDTCType = "U"
        )

        // B-Codes (Body)
        entries += AstraJDTCDetails(
            code = "B0015",
            description = "Lenksäulen-Schaltercluster - PCM/ECM Kommunikationsfehler",
            severity = DTCSeverity.INFO,
            system = "Karosserie",
            frequency = DTCFrequency.RARE,
            typicalCause = "CIM-Modul (Column Integration Module) Kommunikationsfehler",
            recommendedSolution = "CIM-Modul prüfen, CAN-Bus-Verbindung testen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.HIGH,
            udsDTCType = "B"
        )

        // =====================================================================
        // Z14XEL-spezifische DTCs (N/A 1.4L - Bosch ME17.9.2)
        // =====================================================================
        entries += AstraJDTCDetails(
            code = "P0401",
            description = "EGR - Unzureichende Durchflussmenge",
            severity = DTCSeverity.WARNING,
            system = "Abgas",
            frequency = DTCFrequency.COMMON,
            typicalCause = "Verstopftes EGR-Ventil, Ablagerungen im Ansaugkrümmer, defektes Stellglied",
            recommendedSolution = "EGR-Ventil reinigen, Ansaugkrümmer auf Verkokung prüfen, EGR-Systemtest",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0402", "P0403"),
            technicalNotes = "Beim Z14XEL: EGR ist elektrisch geregelt. Ablagerungen im Ansaugkrümmer sind typisch. Kein Turbo, daher andere Luftdynamik als A14NET."
        )
        entries += AstraJDTCDetails(
            code = "P0402",
            description = "EGR - Überschreitende Durchflussmenge",
            severity = DTCSeverity.WARNING,
            system = "Abgas",
            frequency = DTCFrequency.RARE,
            typicalCause = "EGR-Ventil klemmt offen, Dichtungsleck, Vakuumleitung defekt",
            recommendedSolution = "EGR-Ventil auf Freigang prüfen, Vakuumleitungen kontrollieren",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0401", "P0403")
        )
        entries += AstraJDTCDetails(
            code = "P0403",
            description = "EGR - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Abgas",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defektes EGR-Stellglied, Verkabelungsfehler, Steuergeräteausgang defekt",
            recommendedSolution = "EGR-Stellglied-Widerstand messen, Verkabelung prüfen, Ventil ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0401", "P0402")
        )
        entries += AstraJDTCDetails(
            code = "P0106",
            description = "MAP-Sensor - Bereich/Leistung (Plausibilität)",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "MAP-Sensor verschmutzt, Vakuumleck, Sensor defekt, Dichtungsleck",
            recommendedSolution = "MAP-Sensor prüfen, Vakuumleitung auf Dichtheit testen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0105", "P0107", "P0108"),
            technicalNotes = "Beim Z14XEL: MAP-Sensor misst Ansaugdruck. Im Leerlauf Vakuum (30-50 kPa), bei Last Atmospheric (100+ kPa)."
        )
        entries += AstraJDTCDetails(
            code = "P0105",
            description = "MAP-Sensor - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Defekter MAP-Sensor, Verkabelungsfehler, Steckerverbindung",
            recommendedSolution = "MAP-Sensor und Verkabelung prüfen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0106", "P0107", "P0108")
        )
        entries += AstraJDTCDetails(
            code = "P0107",
            description = "MAP-Sensor - Signaleingang niedrig",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "MAP-Sensor Kurzschluss gegen Masse, defekter Sensor, Verkabelung",
            recommendedSolution = "MAP-Sensor-Spannung messen ( Leerlauf: 1.0-1.5V), Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0105", "P0106")
        )
        entries += AstraJDTCDetails(
            code = "P0108",
            description = "MAP-Sensor - Signaleingang hoch",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "MAP-Sensor offen, Unterbrechung der Verkabelung, Vakuumleck",
            recommendedSolution = "Verkabelung prüfen, Vakuumleitung auf Dichtheit testen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0105", "P0106")
        )
        entries += AstraJDTCDetails(
            code = "P2138",
            description = "Drosselklappenpositionssensor - Spannung unplausibel",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Drosselklappensensor defekt, Verkabelungsfehler, Kurzschluss zwischen Sensoren",
            recommendedSolution = "Drosselklappensensoren prüfen, Verkabelung testen, ggf. Drosselklappeneinheit ersetzen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P0220", "P0120", "P2101"),
            technicalNotes = "Z14XEL: Elektronische Drosselklappe mit zwei Sensoren. Bei Differenz >0.5V: Fehler."
        )
        entries += AstraJDTCDetails(
            code = "P2101",
            description = "Drosselklappensteller - Unplausibles Signal",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Drosselklappensteller defekt, Verkabelungsfehler, mechanischer Defekt",
            recommendedSolution = "Drosselklappensteller prüfen, Drosselklappeneinheit reinigen oder ersetzen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P2138", "P0220", "P2100"),
            technicalNotes = "Z14XEL: Drosselklappenadaption nach Reinigung/Ersetzung durchführen."
        )
        entries += AstraJDTCDetails(
            code = "P0606",
            description = "Steuergerätfehler (ECM/PCM Prozessorfehler)",
            severity = DTCSeverity.CRITICAL,
            system = "Elektronik",
            frequency = DTCFrequency.RARE,
            typicalCause = "Interner Steuergerätefehler, Softwarefehler, Überspannung",
            recommendedSolution = "Steuergerät neu initialisieren, Software-Update prüfen, ggf. Steuergerät ersetzen",
            estimatedCostWorkshop = DTCCostRange.VERY_HIGH,
            estimatedCostDIY = DTCCostRange.VERY_HIGH,
            relatedCodes = listOf("P0607"),
            technicalNotes = "Z14XEL: Bei anhaltendem Fehler: Steuergerät-Batterie trennen (30 Min.), bei Fortsetzung: Werkstatt."
        )
        entries += AstraJDTCDetails(
            code = "P0102",
            description = "Luftmassenmesser (MAF) - Signaleingang niedrig",
            severity = DTCSeverity.WARNING,
            system = "Sensor",
            frequency = DTCFrequency.COMMON,
            typicalCause = "MAF-Sensor verschmutzt oder defekt, zu wenig Luftstrom, verstopfter Luftfilter",
            recommendedSolution = "Luftfilter prüfen/tauschen, MAF-Sensor reinigen, bei anhaltendem Fehler: MAF ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            isAstraJCommon = true,
            relatedCodes = listOf("P0100", "P0101"),
            technicalNotes = "Z14XEL: MAF-Wert im Leerlauf ca. 2-4 g/s, bei Last 20-60 g/s. Zu niedrig = Verschmutzung oder Luftleck."
        )
        entries += AstraJDTCDetails(
            code = "P2135",
            description = "Drosselklappenpositionssensor 1/2 - Unplausibel",
            severity = DTCSeverity.WARNING,
            system = "Motor",
            frequency = DTCFrequency.OCCASIONAL,
            typicalCause = "Drosselklappensensor defekt, Verkabelung, mechanischer Verschleiss",
            recommendedSolution = "Drosselklappeneinheit prüfen, Sensoren testen, ggf. Einheit ersetzen",
            estimatedCostWorkshop = DTCCostRange.HIGH,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P2138", "P0220"),
            technicalNotes = "Z14XEL: Zwei Drosselklappensensoren müssen innerhalb von 0.5V übereinstimmen."
        )

        // Weitere P-Codes fuer Vollstaendigkeit
        entries += AstraJDTCDetails(
            code = "P0335",
            description = "Kurbelwellenpositionssensor - Stromkreisfehler",
            severity = DTCSeverity.CRITICAL,
            system = "Sensor",
            frequency = DTCFrequency.RARE,
            typicalCause = "Defekter Kurbelwellenpositionssensor, Unterbrechung der Verkabelung, Reluktanzrad verschmutzt",
            recommendedSolution = "CKP-Sensor prüfen, Verkabelung testen, Sensor ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0336", "P0016"),
            technicalNotes = "Bei CKP-Ausfall: Motor startet nicht oder läuft unrund."
        )
        entries += AstraJDTCDetails(
            code = "P0336",
            description = "Kurbelwellenpositionssensor - Signalfehler / Plausibilität",
            severity = DTCSeverity.CRITICAL,
            system = "Sensor",
            frequency = DTCFrequency.RARE,
            typicalCause = "Reluktanzrad verschmutzt, Sensorabstand zu groß, Steuerkettenspiel",
            recommendedSolution = "CKP-Sensor und Reluktanzrad prüfen, Steuerkettenspannung kontrollieren",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.LOW,
            relatedCodes = listOf("P0335", "P0016")
        )

        // Einspritzsystem
        entries += AstraJDTCDetails(
            code = "P0201",
            description = "Einspritzventil Zylinder 1 - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Kraftstoff",
            frequency = DTCFrequency.RARE,
            typicalCause = "Defektes Einspritzventil, Verkabelungsfehler, Steuergerät-Ausgang defekt",
            recommendedSolution = "Einspritzventil-Widerstand messen, Verkabelung prüfen, Ventil ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P0202", "P0203", "P0204")
        )
        entries += AstraJDTCDetails(
            code = "P0202",
            description = "Einspritzventil Zylinder 2 - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Kraftstoff",
            frequency = DTCFrequency.RARE,
            typicalCause = "Defektes Einspritzventil, Verkabelungsfehler",
            recommendedSolution = "Einspritzventil-Widerstand messen, Verkabelung prüfen, Ventil ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P0201", "P0203", "P0204")
        )
        entries += AstraJDTCDetails(
            code = "P0203",
            description = "Einspritzventil Zylinder 3 - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Kraftstoff",
            frequency = DTCFrequency.RARE,
            typicalCause = "Defektes Einspritzventil, Verkabelungsfehler",
            recommendedSolution = "Einspritzventil-Widerstand messen, Verkabelung prüfen, Ventil ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P0201", "P0202", "P0204")
        )
        entries += AstraJDTCDetails(
            code = "P0204",
            description = "Einspritzventil Zylinder 4 - Stromkreisfehler",
            severity = DTCSeverity.WARNING,
            system = "Kraftstoff",
            frequency = DTCFrequency.RARE,
            typicalCause = "Defektes Einspritzventil, Verkabelungsfehler",
            recommendedSolution = "Einspritzventil-Widerstand messen, Verkabelung prüfen, Ventil ersetzen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P0201", "P0202", "P0203")
        )

        // Batterie / Lichtmaschine
        entries += AstraJDTCDetails(
            code = "P0563",
            description = "Systemspannung hoch",
            severity = DTCSeverity.WARNING,
            system = "Elektrik",
            frequency = DTCFrequency.RARE,
            typicalCause = "Überspannung durch defekten Spannungsregler der Lichtmaschine",
            recommendedSolution = "Lichtmaschine-Spannung bei verschiedenen Drehzahlen messen, Spannungsregler prüfen",
            estimatedCostWorkshop = DTCCostRange.MEDIUM,
            estimatedCostDIY = DTCCostRange.MEDIUM,
            relatedCodes = listOf("P0562")
        )

        return entries.associateBy { it.code }
    }
}
