package com.canopobd.data.model

enum class ProblemCategory(val displayName: String) {
    ENGINE("Motor"),
    TURBO("Turbo-Probleme"),
    SENSOR("Sensor-Probleme"),
    EXHAUST("Abgassystem-Probleme"),
    TIMING_CHAIN("Steuerkette"),
    FUEL_SYSTEM("Kraftstoffsystem"),
    IGNITION("Zündung"),
    ELECTRICAL("Elektrik"),
    COMMUNICATION("Kommunikation")
}

enum class DiagnosticStepType {
    VISUAL_INSPECTION,
    MEASUREMENT,
    TEST,
    REPLACEMENT,
    RESET
}

data class DiagnosticStep(
    val type: DiagnosticStepType,
    val title: String,
    val description: String,
    val expectedValue: String = "",
    val warningNote: String = ""
)

data class ProblemCase(
    val id: String,
    val title: String,
    val category: ProblemCategory,
    val dtcCodes: List<String>,
    val summary: String,
    val symptoms: List<String>,
    val possibleCauses: List<String>,
    val diagnosticSteps: List<DiagnosticStep>,
    val estimatedCostDIY: String,
    val estimatedCostWorkshop: String,
    val relatedDtcCodes: List<String>,
    val technicalNotes: String,
    val severity: DTCSeverity,
    val frequency: DTCFrequency,
    val isAstraJCommon: Boolean = false
)

object DiagnosticProblemCases {

    private val problemCases: List<ProblemCase> by lazy { buildProblemCases() }

    fun getAllCases(): List<ProblemCase> = problemCases

    fun getCasesByCategory(category: ProblemCategory): List<ProblemCase> =
        problemCases.filter { it.category == category }

    fun getCasesByDTC(code: String): List<ProblemCase> =
        problemCases.filter { code.uppercase() in it.dtcCodes.map { c -> c.uppercase() } }

    fun getCaseById(id: String): ProblemCase? =
        problemCases.find { it.id == id }

    fun getCommonCases(): List<ProblemCase> =
        problemCases.filter { it.isAstraJCommon }
            .sortedByDescending { it.frequency.sortWeight }

    private fun buildProblemCases(): List<ProblemCase> {
        val cases = mutableListOf<ProblemCase>()

        // TURBO PROBLEM CASES
        cases += ProblemCase(
            id = "turbo-underboost",
            title = "Turbo Unterladung (Underboost)",
            category = ProblemCategory.TURBO,
            dtcCodes = listOf("P0299"),
            summary = "Der Turbolader liefert nicht den erwarteten Ladedruck. Das Fahrzeug zeigt Leistungsverlust und verzögertes Ansprechverhalten.",
            symptoms = listOf(
                "Leistungsverlust bei Last",
                "Verzögertes Turbo-Ansprechen",
                "Schwarzer Qualm bei Last",
                "Erhöhter Kraftstoffverbrauch"
            ),
            possibleCauses = listOf(
                "Wastegate undicht oder klemmt",
                "Undichte Ladeluftkühler-Verbindungen",
                "Riss im Ansaugschlauch",
                " Verschleiß des Turbo-Laufrads",
                "Undichte Vakuumleitungen"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Ansaugsystem prüfen",
                    "Alle Schläuche und Verbindungen auf Risse, Löcher oder Undichtigkeiten prüfen. Besonders den Ladeluftkühler und die Verbindung zum Ansaugkrümmer.",
                    warningNote = "Biege den Ladeluftkühler nicht — er ist empfindlich!"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Vakuumleitungen prüfen",
                    "Alle Vakuumleitungen zum Wastegate-Ventil auf Risse oder Löcher prüfen. Besonders die kurzen Verbindungsleitungen."),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "Wastegate-Funktion prüfen",
                    "Manuell das Wastegate betätigen (vorsichtig mit einem Schraubenzieher) und prüfen, ob es frei beweglich ist. Es sollte mit leichtem Widerstand schließen.",
                    expectedValue = "Leichtgängig, kein Schleifen"),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "Ladedruck messen",
                    "Mit einem Ladedruck-Manometer den Ist-Ladedruck bei 3000 rpm messen. Vergleich mit Sollwert (ca. 1.0 bar relativ).",
                    expectedValue = "0.9–1.1 bar relativ bei 3000 rpm"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Turbo-Laufrad prüfen",
                    "Schlauch vom Ladeluftkühler zum Ansaugkrümmer abnehmen und Laufrad auf Beschädigungen, Ölablagerungen oder Spiel prüfen.",
                    warningNote = "Nur bei Verdacht auf Turbo-Schaden öffnen!")
            ),
            estimatedCostDIY = "20–100 EUR (Dichtungen, Schläuche)",
            estimatedCostWorkshop = "300–1500 EUR (je nach Ursache)",
            relatedDtcCodes = listOf("P0234", "P0235", "P1241", "P1243"),
            technicalNotes = "Beim A14NET: Wastegate-Blase und O-Ring am Stellglied sind typische Schwachstellen. Die Ladedruck-Abweichung sollte nicht mehr als 20% vom Sollwert betragen.",
            severity = DTCSeverity.WARNING,
            frequency = DTCFrequency.COMMON,
            isAstraJCommon = true
        )

        cases += ProblemCase(
            id = "turbo-overboost",
            title = "Turbo Überladung (Overboost)",
            category = ProblemCategory.TURBO,
            dtcCodes = listOf("P0234", "P1242", "P1245"),
            summary = "Der Turbolader erzeugt übermäßig hohen Ladedruck, was den Motor in den Notlauf-Modus versetzen kann.",
            symptoms = listOf(
                "Ungewöhnlich hoher Ladedruck",
                "Motor-Leistung schwankt",
                "Zündaussetzer unter Last",
                "Notlauf-Modus möglich"
            ),
            possibleCauses = listOf(
                "Wastegate-Stellglied klemmt geschlossen",
                "Defekter Ladedrucksensor (MAP)",
                "Vakuumleitung zum Wastegate undicht",
                "ECU-Softwarefehler"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Vakuumleitungen prüfen",
                    "Vakuumleitung vom Wastegate zum Unterdruck-Regler auf Undichtigkeiten prüfen. Bei Undichtigkeit: Vakuum hält nicht, Wastegate bleibt geschlossen → Overboost.",
                    warningNote = "Bemerken Sie sofort: Gas weglassen und OBD-Adapter anschließen!"),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "Ladedrucksensor prüfen",
                    "MAP-Sensor-Spannung im Leerlauf messen (ca. 2.5V bei Atmosphärendruck). Bei Last sollte die Spannung ansteigen. Fehlerhafte Werte = Sensor defekt.",
                    expectedValue = "Leerlauf ~2.5V, Volllast ~4.5V"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Wastegate-Mechanik prüfen",
                    "Wastegate-Hebel auf Freigang prüfen. Korrosion oder Ölablagerungen können das Schließen verhindern.",
                    expectedValue = "Hebel bewegt sich frei"),
                DiagnosticStep(DiagnosticStepType.TEST, "OBD-Daten live beobachten",
                    "Im OBD-Monitor den Soll-Ladedruck und Ist-Ladedruck vergleichen. Große Abweichung (>20%) deutet auf Wastegate-Problem hin.")
            ),
            estimatedCostDIY = "50–150 EUR (Sensor, Vakuumschläuche)",
            estimatedCostWorkshop = "300–800 EUR",
            relatedDtcCodes = listOf("P0299", "P0235", "P0238"),
            technicalNotes = "Overboost ist gefährlich für den Motor! Beim A14NET sollte der Ladedruck 1.3 bar relativ nicht dauerhaft überschreiten. Bei Overboost sofort Gas weglassen.",
            severity = DTCSeverity.CRITICAL,
            frequency = DTCFrequency.COMMON,
            isAstraJCommon = true
        )

        cases += ProblemCase(
            id = "turbo-wastegate-actuator",
            title = "Wastegate-Stellglied defekt",
            category = ProblemCategory.TURBO,
            dtcCodes = listOf("P0243", "P0245", "P0246", "P1253", "P1254", "P1255"),
            summary = "Das Wastegate-Stellglied reagiert nicht korrekt, was zu Ladedruckproblemen führt.",
            symptoms = listOf(
                "Ladedruck nicht regelbar",
                "Leistungsschwankungen",
                "Ungewöhnliche Motordrehzahl",
                "Wastegate-Position weicht ab"
            ),
            possibleCauses = listOf(
                "Elektrisches Stellglied defekt",
                "Vakuum-Membran gerissen",
                "Mechanische Blockade des Wastegate-Hebels",
                "Steuerungselektronik fehlerhaft"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Hebel-Mechanik prüfen",
                    "Wastegate-Hebel manuell bewegen. Er sollte sich mit leichtem bis mittlerem Kraftaufwand bewegen lassen. Schleifen oder Klemmen deutet auf mechanisches Problem.",
                    expectedValue = "Leichtgängig, kein Schleifen"),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "Vakuumtest",
                    "Direkt am Wastegate-Ventil Vakuum anlegen. Der Hebel sollte sich bewegen. Keine Bewegung = Membran defekt.",
                    expectedValue = "Hebel bewegt sich bei Vakuum"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Anschlüsse prüfen",
                    "Elektrische und Vakuum-Anschlüsse auf Korrosion, Bruch oder Lose prüfen."),
                DiagnosticStep(DiagnosticStepType.TEST, "Ladedruck-Regelung prüfen",
                    "Im OBD-Monitor die Wastegate-Position beobachten und mit dem Ladedruck vergleichen. Die Regelung sollte proportional sein.")
            ),
            estimatedCostDIY = "Nicht empfehlenswert",
            estimatedCostWorkshop = "500–2000 EUR",
            relatedDtcCodes = listOf("P0299", "P0234", "P1247"),
            technicalNotes = "Beim A14NET ist das Wastegate-Stellglied oft porös und verliert Vakuum. Alternativ gibt es Umbau-Kits mit elektrischem Stellglied.",
            severity = DTCSeverity.WARNING,
            frequency = DTCFrequency.OCCASIONAL,
            isAstraJCommon = true
        )

        // SENSOR PROBLEM CASES
        cases += ProblemCase(
            id = "sensor-maf",
            title = "MAF-Sensor Probleme",
            category = ProblemCategory.SENSOR,
            dtcCodes = listOf("P0100", "P0101", "P0102", "P0103", "P1100", "P1101"),
            summary = "Der Luftmassenmesser (MAF) liefert falsche oder keine Daten, was zu Gemischproblemen führt.",
            symptoms = listOf(
                "Rauer Leerlauf",
                "Hoher Kraftstoffverbrauch",
                "Leistungsverlust",
                "Zündaussetzer",
                "Motor startet schlecht"
            ),
            possibleCauses = listOf(
                "MAF-Sensor verschmutzt (Öl, Staub)",
                "Defekter MAF-Sensor",
                "Kabelbruch oder Korrosion am Stecker",
                "Ölfeuchte am MAF-Stecker"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "MAF-Sensor prüfen",
                    "MAF-Sensor ausbauen und den Heizdraht/die Membrane auf Verschmutzung prüfen. Ölablagerungen, Ruß oder Staub deuten auf Verschmutzung hin.",
                    warningNote = "NIEMALS Druckluft auf den MAF-Sensor blasen — das zerstört den Sensor!"),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "MAF-Wert im Leerlauf prüfen",
                    "Im OBD-Monitor den MAF-Wert im Leerlauf beobachten. Der A14NET sollte im Leerlauf ca. 2–4 g/s anzeigen.",
                    expectedValue = "2–4 g/s im Leerlauf (Motor warm)"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Stecker und Verkabelung prüfen",
                    "MAF-Stecker abnehmen und auf Korrosion, Oxidation oder Feuchtigkeitsspuren prüfen. Die Kontakte mit Kontaktspray reinigen."),
                DiagnosticStep(DiagnosticStepType.TEST, "MAF-Reinigungstest",
                    "MAF-Sensor mit Isopropanol oder speziellem MAF-Reiniger reinigen. Nach dem Trocknen wieder einbauen und Probefahrt machen."),
                DiagnosticStep(DiagnosticStepType.REPLACEMENT, "Sensor ersetzen",
                    "Wenn Reinigung nicht hilft: MAF-Sensor ersetzen. Verwende nur einen Sensor mit passender Teilenummer (Bosch oder OEM).")
            ),
            estimatedCostDIY = "15–50 EUR (Reiniger)",
            estimatedCostWorkshop = "150–400 EUR (inkl. Diagnose)",
            relatedDtcCodes = listOf("P0171", "P0172", "P0300"),
            technicalNotes = "Beim A14NET ist der MAF-Sensor sehr anfällig für Ölfeuchte aus dem PCV-System. Wenn der Fehler häufig zurückkommt: PCV-Ventil und Schläuche prüfen!",
            severity = DTCSeverity.WARNING,
            frequency = DTCFrequency.VERY_COMMON,
            isAstraJCommon = true
        )

        cases += ProblemCase(
            id = "sensor-camshaft",
            title = "Nockenwellensensor Probleme",
            category = ProblemCategory.SENSOR,
            dtcCodes = listOf("P0340", "P0341"),
            summary = "Der Nockenwellensensor gibt fehlerhafte Signale aus, oft zusammen mit Steuerkettenschäden.",
            symptoms = listOf(
                "Startprobleme",
                "Rauer Leerlauf",
                "Leistungsverlust",
                "Notlauf-Modus",
                "Kaltstart-Rattern"
            ),
            possibleCauses = listOf(
                "Verschlissene Steuerkette",
                "Defekter Kettenspanner",
                "Nockenwellensensor verschmutzt",
                "Sensorabstand zu groß",
                "Kabelbruch"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Sensor prüfen",
                    "Nockenwellensensor am Zylinderkopf auf Verschmutzung, Ölablagerungen und mechanische Beschädigung prüfen. Sensor vorsichtig reinigen.",
                    warningNote = "Sensor nicht mit scharfen Gegenständen reinigen!"),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "Sensor-Widerstand messen",
                    "Widerstand zwischen den Sensor-Anschlüssen messen. Typischer Wert: 800–1200 Ohm. Unendlich oder 0 Ohm = Sensor defekt.",
                    expectedValue = "800–1200 Ohm"),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "Luftspalt prüfen",
                    "Mit einer Fühlerlehre den Luftspalt zwischen Sensor und Nockenwellenrad prüfen. Zu großer Abstand führt zu Signalverlust.",
                    expectedValue = "0.5–1.5 mm"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Steuerkette prüfen",
                    "Öldeckel abnehmen und Steuerkette auf übermäßiges Spiel prüfen. Leichtes Zahnradspiel ist normal, aber kein seitliches Spiel.",
                    warningNote = "Vorsicht: Kettenkit nur bei Bedarf erneuern — kostspielig!"),
                DiagnosticStep(DiagnosticStepType.TEST, "Steuerketten-Spanner prüfen",
                    "Kettenspanner auf Funktion prüfen. Er sollte sich beim Zurückdrücken mit Federdruck wieder ausfahren.")
            ),
            estimatedCostDIY = "30–100 EUR (Sensor, Dichtungen)",
            estimatedCostWorkshop = "600–2000 EUR (Kettenkit inkl.)",
            relatedDtcCodes = listOf("P0016", "P0017", "P1345"),
            technicalNotes = "P0340 + P0341 beim A14NET ist oft kein Sensorproblem, sondern ein Steuerkettenproblem! Sensorersatz behebt den Fehler nur kurzfristig. Kettenkit immer miterneuern.",
            severity = DTCSeverity.CRITICAL,
            frequency = DTCFrequency.COMMON,
            isAstraJCommon = true
        )

        cases += ProblemCase(
            id = "sensor-coolant",
            title = "Kühlmitteltemperatur-Sensor (ECT)",
            category = ProblemCategory.SENSOR,
            dtcCodes = listOf("P0115", "P0116", "P0117", "P0118"),
            summary = "Der Kühlmitteltemperatur-Sensor liefert falsche Werte, was zu Kühlproblemen und schlechtem Kaltstart führt.",
            symptoms = listOf(
                "Motor läuft dauerhaft auf Kühlbetrieb",
                "Schlechter Kaltstart",
                "Übermäßig hohe Kühlmitteltemperatur",
                "Chiptuning reagiert nicht korrekt"
            ),
            possibleCauses = listOf(
                "Defekter ECT-Sensor (im Thermostatgehäuse)",
                "Thermostat klemmt offen",
                "Luft im Kühlsystem",
                "Steckerverbindung korrodiert"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "ECT-Widerstand messen",
                    "ECT-Sensor im kalten Zustand ausbauen und Widerstand messen. Bei 20°C sollte der Widerstand ca. 2.5–3.5 kΩ sein.",
                    expectedValue = "2.5–3.5 kΩ bei 20°C"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Thermostat-Gehäuse prüfen",
                    "Thermostat auf Funktion prüfen. Bei A14NET ist der ECT-Sensor im Thermostat integriert. Wenn beide defekt sind: komplettes Thermostatgehäuse ersetzen.",
                    warningNote = "Thermostat niemals im warmen Zustand ausbauen! Kühlsystem erst abkühlen lassen."),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Stecker prüfen",
                    "Steckverbindung am Thermostatgehäuse auf Korrosion und Feuchtigkeit prüfen. Kontakte mit Kontaktspray reinigen."),
                DiagnosticStep(DiagnosticStepType.TEST, "Temperaturverlauf beobachten",
                    "Im OBD-Monitor die Kühlmitteltemperatur beobachten. Nach Motorstart sollte sie ansteigen und zwischen 85–105°C stabil bleiben."),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Kühlsystem entlüften",
                    "Kühlsystem auf Luftblasen prüfen und nach jedem Kühlmittelwechsel gründlich entlüften. Luft im System führt zu falschen Temperaturmessungen.")
            ),
            estimatedCostDIY = "30–80 EUR (Thermostat)",
            estimatedCostWorkshop = "150–350 EUR",
            relatedDtcCodes = listOf("P0128", "P1299"),
            technicalNotes = "Beim A14NET ist der ECT-Sensor im Thermostat integriert. Thermostat und Sensor werden zusammen ersetzt. Achtung: Nur Dex-Cool 50/50 Kühlmittel verwenden!",
            severity = DTCSeverity.WARNING,
            frequency = DTCFrequency.COMMON,
            isAstraJCommon = true
        )

        cases += ProblemCase(
            id = "sensor-oxygen",
            title = "Lambdasonde / O2-Sensor Probleme",
            category = ProblemCategory.SENSOR,
            dtcCodes = listOf("P0130", "P0131", "P0132", "P0133", "P0134", "P0030", "P0031"),
            summary = "Der O2-Sensor (Lambdasonde) funktioniert nicht korrekt, was zu erhöhtem Kraftstoffverbrauch und schlechten Emissionswerten führt.",
            symptoms = listOf(
                "Erhöhter Kraftstoffverbrauch",
                "Rauer Motorlauf",
                "Schlechte Emissionswerte",
                "Katalysator-Schaden möglich"
            ),
            possibleCauses = listOf(
                "Defekte Lambdasonde (Heizung oder Sensor)",
                "Verkabelung oder Stecker beschädigt",
                "Sensor verschlissen / vergiftet",
                "Zu mageres oder zu fettes Gemisch"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "Heizungswiderstand prüfen",
                    "Heizungswiderstand der Lambdasonde messen. Zwischen Heizungsanschlüssen sollte ein Widerstand von 2–5 Ohm gemessen werden.",
                    expectedValue = "2–5 Ohm (Heizung)"),
                DiagnosticStep(DiagnosticStepType.TEST, "O2-Spannung beobachten",
                    "Im OBD-Monitor die O2-Spannung beobachten. Sie sollte zwischen 0.1V (mager) und 0.9V (fett) wechseln. Langsamer Wechsel = Sensor verschlissen.",
                    expectedValue = "0.1–0.9V wechselnd, min. 8x pro Sekunde"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Lambda-Vorkat und Nachkat vergleichen",
                    "Spannung von Vorkat und Nachkat vergleichen. Nachkat sollte stabiler sein. Wenn beide gleich schnell wechseln: Katalysator-Schaden!"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Stecker und Verkabelung prüfen",
                    "Stecker auf Korrosion und Bruch prüfen. Die 4-Draht-Lambdasonde hat: 2x Heizung, 1x Signal, 1x Masse.")
            ),
            estimatedCostDIY = "100–200 EUR (Sensor)",
            estimatedCostWorkshop = "300–600 EUR (inkl. Diagnose)",
            relatedDtcCodes = listOf("P0171", "P0172", "P0420"),
            technicalNotes = "Beim A14NET: Breitband-Lambda (4-Draht). Nur BOSCH NTK oder OEM-Ersatz verwenden. Billige Nachbauten haben oft zu kurze Lebensdauer.",
            severity = DTCSeverity.WARNING,
            frequency = DTCFrequency.OCCASIONAL,
            isAstraJCommon = false
        )

        // EXHAUST SYSTEM PROBLEM CASES
        cases += ProblemCase(
            id = "exhaust-catalyst",
            title = "Katalysator-Probleme",
            category = ProblemCategory.EXHAUST,
            dtcCodes = listOf("P0420", "P0421", "P0422", "P0430"),
            summary = "Der Katalysator arbeitet nicht mehr effektiv, was zu schlechten Emissionswerten und Leistungsverlust führt.",
            symptoms = listOf(
                "Schlechte Abgaswerte",
                "Muffiger Geruch aus dem Auspuff",
                "Leistungsverlust",
                "TÜV/Abgasuntersuchung nicht bestanden"
            ),
            possibleCauses = listOf(
                "Katalysator verschlissen oder beschädigt",
                "Zündaussetzer (Benzin ins Kat)",
                "Motor zu mager oder zu fett betrieben",
                "Öl- oder Kühlmittelverbrennung",
                "Defekter Nachkat-Lambdasonde"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.TEST, "Vorkat vs. Nachkat vergleichen",
                    "Im OBD-Monitor Vorkat-Lambda und Nachkat-Lambda vergleichen. Der Nachkat-Sensor sollte stabil sein. Wenn er wie der Vorkat wechselt: Kat-Schaden.",
                    expectedValue = "Nachkat stabil bei ~0.45V (Äquilibrium)"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Auspufferichtung prüfen",
                    "Auslassrohr und Kat-Gehäuse auf Beschädigungen, Risse oder Verfärbungen prüfen. Blauschwarze Verfärbung = Öl- oder Kraftstoffverbrennung."),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Abgasfarben prüfen",
                    "Motor starten und Auspufffarbe beobachten: Schwarz = zu fett, Weiß = Kühlmittel, Blau = Öl. Jede Farbe außer transparent deutet auf ein Problem hin."),
                DiagnosticStep(DiagnosticStepType.TEST, "Kraftstoffsystem prüfen",
                    "STFT und LTFT im OBD-Monitor prüfen. Werte über ±15% können den Kat schädigen."),
                DiagnosticStep(DiagnosticStepType.TEST, "Zündung prüfen",
                    "Zündkerzen auf Ablagerungen und Farbe prüfen. Weiße Ablagerungen = Verbrennung von Kühlmittel/Öl. Schwarz = Kraftstoffüberschuss.")
            ),
            estimatedCostDIY = "Nicht empfehlenswert (Kat ist verschweißt)",
            estimatedCostWorkshop = "800–2500 EUR (Kat-Ersatz)",
            relatedDtcCodes = listOf("P0171", "P0172", "P0300", "P0133"),
            technicalNotes = "P0420 beim A14NET bedeutet nicht immer, dass der Kat defekt ist! Oft ist die Ursache ein problematisches Gemisch oder Zündaussetzer. Kat-Lebensdauer >200.000 km bei guter Pflege.",
            severity = DTCSeverity.WARNING,
            frequency = DTCFrequency.COMMON,
            isAstraJCommon = true
        )

        cases += ProblemCase(
            id = "exhaust-egr",
            title = "EGR-System Probleme",
            category = ProblemCategory.EXHAUST,
            dtcCodes = listOf("P0400", "P0401", "P0402", "P0403", "P0404"),
            summary = "Das Abgasrückführungsventil (EGR) funktioniert nicht richtig, was zu Leistungsproblemen und erhöhten Emissionen führt.",
            symptoms = listOf(
                "Rauer Leerlauf",
                "Leistungsverlust",
                "Schwarzer Qualm",
                "Erhöhte NOx-Emissionen"
            ),
            possibleCauses = listOf(
                "EGR-Ventil durch Ablagerungen verstopft",
                "EGR-Ventil undicht oder klemmt",
                "Vakuumsteuerung defekt",
                "EGR-Kühlmittelumlauf verstopft"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "EGR-Ventil prüfen",
                    "EGR-Ventil auf Kohleablagerungen und Verstopfung prüfen. Ablagerungen im Ventor und in den Kanälen sind typisch beim A14NET.",
                    warningNote = "EGR-Reinigung nur mit geeignetem Reiniger (GDI-Injektorreiniger oder EGR-Reiniger)!"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Ansaugkrümmer prüfen",
                    "Einlaskanäle im Ansaugkrümmer auf Kohleablagerungen prüfen. Die Ablagerungen stammen von der EGR-Rückführung und können die Kanäle verstopfen.",
                    warningNote = "Bei starken Ablagerungen: Ansaugkrümmer reinigen oder ersetzen. Werkstatt erforderlich!"),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "EGR-Position prüfen",
                    "Im OBD-Monitor die EGR-Position beobachten. Das Ventil sollte sich im Leerlauf schließen und bei Last öffnen.",
                    expectedValue = "0% im Leerlauf, 20–80% bei Last"),
                DiagnosticStep(DiagnosticStepType.TEST, "EGR-Funktionstest",
                    "Mit OBD-Tool einen EGR-Test durchführen (falls unterstützt). Das Ventil sollte hörbar arbeiten.")
            ),
            estimatedCostDIY = "20–60 EUR (Reiniger)",
            estimatedCostWorkshop = "300–800 EUR (Ansaugkrümmer-Reinigung)",
            relatedDtcCodes = listOf("P1101", "P0171"),
            technicalNotes = "Beim A14NET ist der Ansaugkrümmer besonders anfällig für EGR-Ablagerungen. Regelmäßige Reinigung (alle 30.000–50.000 km) beugt Problemen vor.",
            severity = DTCSeverity.WARNING,
            frequency = DTCFrequency.RARE,
            isAstraJCommon = false
        )

        cases += ProblemCase(
            id = "exhaust-evap",
            title = "EVAP-System Probleme",
            category = ProblemCategory.EXHAUST,
            dtcCodes = listOf("P0440", "P0441", "P0442", "P0443", "P0444", "P0445", "P0455", "P0456"),
            summary = "Das Verdunstungsemissionssystem (EVAP) hat ein Leck oder Funktionsstörung.",
            symptoms = listOf(
                "Motor riecht nach Benzin",
                "TÜV/Abgasuntersuchung nicht bestanden",
                "Tankdeckel-Warnung",
                "Leistungsverlust"
            ),
            possibleCauses = listOf(
                "Tankdeckel undicht oder locker",
                "Kleines Leck im Tanksystem",
                "EVAP-Purgeventil defekt",
                "Kraftstofftank-Entlüftungsventil defekt"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Tankdeckel prüfen",
                    "Tankdeckel auf Beschädigung, Riss oder Verschmutzung der Dichtung prüfen. Tankdeckel fest schließen bis es klickt.",
                    expectedValue = "Dichtung intakt, fest verschlossen"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Sichtprüfung Tanksystem",
                    "Tanksystem auf sichtbare Beschädigungen, Risse oder Undichtigkeiten prüfen. Besonders den Bereich um den Einfüllstutzen und das Canister."),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Purge-Ventil prüfen",
                    "EVAP-Purge-Ventil (zwischen Ansaugkrümmer und Canister) auf Funktion prüfen. Bei laufendem Motor sollte ein Klicken hörbar sein.",
                    warningNote = "Nur bei kaltem Motor prüfen!"),
                DiagnosticStep(DiagnosticStepType.TEST, "Rauchprüfung",
                    "Mit einer Rauchprüfung (Werkstatt) das komplette EVAP-System auf Lecks testen. Kleinste Undichtigkeiten werden sichtbar.")
            ),
            estimatedCostDIY = "10–30 EUR (Tankdeckel)",
            estimatedCostWorkshop = "100–500 EUR (je nach Leckort)",
            relatedDtcCodes = listOf("P0171", "P0420"),
            technicalNotes = "P0442 und P0456 sind oft harmlos: Tankdeckel nicht richtig geschlossen oder minimaler Druckverlust durch Temperaturschwankungen. P0455 (großes Leck) aber muss geprüft werden!",
            severity = DTCSeverity.INFO,
            frequency = DTCFrequency.OCCASIONAL,
            isAstraJCommon = false
        )

        // TIMING CHAIN PROBLEM CASE
        cases += ProblemCase(
            id = "timing-chain-wear",
            title = "Steuerkette Verschleiß / Kettenspanner",
            category = ProblemCategory.TIMING_CHAIN,
            dtcCodes = listOf("P0016", "P0017", "P1345", "P0340", "P0341"),
            summary = "Die Steuerkette ist verschlissen, der Kettenspanner funktioniert nicht mehr richtig. Dies ist die häufigste und kostspieligste Schwachstelle des A14NET.",
            symptoms = listOf(
                "Kaltstart-Rattern (lautes Klackern)",
                "Leistungsverlust",
                "Ölverbrauch",
                "Rauer Leerlauf",
                "Schlechter Motorstart"
            ),
            possibleCauses = listOf(
                "Steuerkette verlängert durch Verschleiß",
                "Kettenspanner funktioniert nicht mehr",
                "Spannschiene und/oder Führungsschiene verschlissen",
                "Nockenwellenrad-Verzahnung verschlissen"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.TEST, "Kaltstart-Abhören",
                    "Motor im kalten Zustand starten und auf Rattern, Klackern oder Schleifgeräusche vom Motorraum achten. Dauer: bis Betriebstemperatur erreicht.",
                    warningNote = "Langes Rattern (>30 Sekunden) = sofort in die Werkstatt! Motorschaden möglich!"),
                DiagnosticStep(DiagnosticStepType.TEST, "Drehzahlschwankungen beobachten",
                    "Im OBD-Monitor die Drehzahl bei warmem Motor im Leerlauf beobachten. Schwankungen >50 rpm deuten auf Timing-Problem hin.",
                    expectedValue = "<30 rpm Schwankung im warmen Leerlauf"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Ölfilterdeckel abnehmen",
                    "Ölfilterdeckel abnehmen und mit einer Taschenlampe die Steuerkette und den Spanner beleuchten. Übermäßiges Spiel ist sichtbar.",
                    warningNote = "Nur zur Sichtkontrolle — nicht weiter zerlegen!"),
                DiagnosticStep(DiagnosticStepType.TEST, "Timing-Check mit Diagnosegerät",
                    "Mit OP-COM oder GM-Diagnosegerät die Cam-Crank-Korrelation prüfen. Abweichung >5° = Steuerkette hat Spiel."),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Ölqualität prüfen",
                    "Öl auf Schwarzfärbung und Ablagerungen prüfen. Schlechtes Öl beschleunigt den Kettenverschleiß erheblich.")
            ),
            estimatedCostDIY = "Nicht empfehlenswert",
            estimatedCostWorkshop = "1200–3500 EUR (Kettenkit komplett)",
            relatedDtcCodes = listOf("P0340", "P0341", "P0335"),
            technicalNotes = "Dies ist die ERNSTHAFTESTE Schwachstelle des A14NET! Kettenverschleiß beginnt oft ab 80.000 km. Regelmäßige Ölwechsel mit Dexos2 5W-30 und sauberes Öl verzögern den Verschleiß. Bei ersten Rattern SOFORT handeln!",
            severity = DTCSeverity.CRITICAL,
            frequency = DTCFrequency.VERY_COMMON,
            isAstraJCommon = true
        )

        // FUEL SYSTEM PROBLEM CASE
        cases += ProblemCase(
            id = "fuel-system-mixture",
            title = "Kraftstoffgemisch zu mager / zu fett",
            category = ProblemCategory.FUEL_SYSTEM,
            dtcCodes = listOf("P0171", "P0172"),
            summary = "Das Kraftstoffgemisch ist nicht im richtigen Verhältnis — entweder zu mager (Luftüberschuss) oder zu fett (Kraftstoffüberschuss).",
            symptoms = listOf(
                "Schlechte Motorleistung",
                "Erhöhter Kraftstoffverbrauch",
                "Rauer Motorlauf",
                "Schwarzer oder weißer Qualm"
            ),
            possibleCauses = listOf(
                "Luftleck im Ansaugsystem (mager)",
                "Verschmutzter MAF-Sensor (mager)",
                "Undichte Einspritzventile (fett)",
                "Defekter Kraftstoffdruckregler (fett)",
                "Defekte Lambdasonde (beides)"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "STFT/LTFT prüfen",
                    "Im OBD-Monitor die Kraftstofftrimmwerte (STFT und LTFT) ablesen. Werte über +10% = mager, unter -10% = fett.",
                    expectedValue = "±5–8% im stabilen Betrieb"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Ansaugsystem auf Luftleck prüfen",
                    "Alle Saugschläuche, Dichtungen und das PCV-System auf Undichtigkeiten prüfen. Bei laufendem Motor Bremsenreiniger an die Saugschläuche sprühen — Drehzahl ändert sich = Luftleck!",
                    warningNote = "Vorsicht bei laufendem Motor!"),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "Kraftstoffdruck prüfen",
                    "Kraftstoffdruck im Leerlauf und bei Last messen. Zu niedriger Druck = Pumpe/Filter-Problem. Zu hoher Druck = Druckregler defekt.",
                    expectedValue = "Leerlauf: 3.5–4.5 bar, WOT: 4.0–5.5 bar"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Einspritzventile prüfen",
                    "Einspritzventile auf Undichtigkeit prüfen. Bei ausgebauten Ventilen: jede Düse einzeln auf Tropfen prüfen.",
                    warningNote = "Fachwerkstatt erforderlich für Einspritzventil-Test!")
            ),
            estimatedCostDIY = "20–100 EUR (PCV-Schläuche, MAF-Reinigung)",
            estimatedCostWorkshop = "200–800 EUR (je nach Ursache)",
            relatedDtcCodes = listOf("P0101", "P1101", "P0130"),
            technicalNotes = "P0171 (mager) beim A14NET ist oft durch poröse PCV-Schläuche verursacht! Bevor teure Teile getauscht werden: PCV-System und MAF prüfen.",
            severity = DTCSeverity.WARNING,
            frequency = DTCFrequency.COMMON,
            isAstraJCommon = true
        )

        // PCV PROBLEM CASE
        cases += ProblemCase(
            id = "pcv-system",
            title = "PCV-System (Kurbelgehäuseentlüftung)",
            category = ProblemCategory.ENGINE,
            dtcCodes = listOf("P1100", "P1101"),
            summary = "Das Positive Crankcase Ventilation System (PCV) ist verstopft oder defekt, was zu Druckanstieg im Kurbelgehäuse und Ölverbrauch führt.",
            symptoms = listOf(
                "Ölverbrauch erhöht",
                "Öl im Ansaugsystem",
                "Weißer/blauer Qualm",
                "MAF-Probleme durch Ölfeuchte"
            ),
            possibleCauses = listOf(
                "PCV-Ventil im Zylinderkopfdeckel verstopft",
                "PCV-Rückführungsleitung verstopft",
                "Überdruck im Kurbelgehäuse",
                "Defekter Zylinderkopfdeckel"
            ),
            diagnosticSteps = listOf(
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "PCV-Ventil prüfen",
                    "PCV-Ventil (im Zylinderkopfdeckel) auf Verstopfung prüfen. Bei laufendem Motor sollte ein Sauggeräusch hörbar sein, wenn man den Deckel leicht anhebt.",
                    warningNote = "Beim A14NET ist das PCV-Ventil im Zylinderkopfdeckel integriert — kein separater Austausch möglich!"),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Rückführungsleitung prüfen",
                    "PCV-Rückführungsleitung vom Ventil zum Ansaugkrümmer auf Verstopfung und Risse prüfen."),
                DiagnosticStep(DiagnosticStepType.VISUAL_INSPECTION, "Ansaugkrümmer auf Ölablagerungen",
                    "Einlasskanäle im Ansaugkrümmer auf Ölablagerungen prüfen. Starke Ablagerungen = PCV-Problem, das den MAF-Sensor verschmutzt.",
                    warningNote = "Bei starken Ablagerungen: Zylinderkopfdeckel und Ansaugkrümmer reinigen. Werkstatt erforderlich!"),
                DiagnosticStep(DiagnosticStepType.MEASUREMENT, "Kurbelgehäusedruck messen",
                    "Ölmessstab-Öffnung mit einem Manometer abdecken und Unterdruck messen. Zu hoher Unterdruck = PCV verstopft oder Blow-by zu hoch.",
                    expectedValue = "Leichter Unterdruck normal")
            ),
            estimatedCostDIY = "30–80 EUR (Schläuche)",
            estimatedCostWorkshop = "400–1200 EUR (Zylinderkopfdeckel)",
            relatedDtcCodes = listOf("P0101", "P1101", "P0171"),
            technicalNotes = "Beim A14NET ist der Zylinderkopfdeckel mit integriertem PCV-Ventil eine bekannte Schwachstelle. Ablagerungen verstopfen das Ventil und Öl wird in den Ansaugtrakt gesaugt. Bei A14NET ab 60.000 km auf Öl im Ansaugtrakt achten!",
            severity = DTCSeverity.WARNING,
            frequency = DTCFrequency.COMMON,
            isAstraJCommon = true
        )

        return cases
    }
}
