package com.canopobd.data.model

/**
 * Erweiterte GM Mode 22 DIDs für Opel Astra J 2012 1.4 Turbo (A14NET / LUJ)
 *
 * Mode 22 (SAE J2190) – Herstellerspezifische Data Identifiers
 * ECU: Bosch ME17.9.22 / Delco E78
 *
 * Befehlsformat: 22XXXX (XXXX = 4-stelliger erweiterter PID)
 * Antwortformat: 62XXXX + Datenbytes
 *
 * Referenzen:
 * - SAE J2190 (Enhanced OBD-II)
 * - Bosch ME17.x ECU-Dokumentation
 * - GM/Opel OBD-II Implementierung
 */
object ExtendedGMMode22 {

    data class Mode22PIDDefinition(
        val code: String,
        val displayName: String,
        val unit: String,
        val byteCount: Int,
        val formula: (ByteArray) -> Double,
        val normalRangeMin: Double,
        val normalRangeMax: Double,
        val description: String
    ) {
        fun decode(data: ByteArray): Double = formula(data)

        fun isNormal(value: Double): Boolean = value in normalRangeMin..normalRangeMax

        fun statusText(value: Double): String = when {
            value < normalRangeMin -> "Zu niedrig"
            value > normalRangeMax -> "Zu hoch"
            else -> "Normal"
        }
    }

    // =========================================================================
    // GM Mode 22 PIDs – Astra J 1.4 Turbo (A14NET)
    // =========================================================================

    val ENGINE_TORQUE = Mode22PIDDefinition(
        code = "221001",
        displayName = "Motor-Drehmoment",
        unit = "%",
        byteCount = 1,
        formula = { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128).toDouble() else 0.0 },
        normalRangeMin = 0.0,
        normalRangeMax = 100.0,
        description = "Aktuelles Motor-Drehmoment in Prozent des Nenndrehmoments (200 Nm)"
    )

    val REQUESTED_TORQUE = Mode22PIDDefinition(
        code = "221002",
        displayName = "Angefordertes Drehmoment",
        unit = "%",
        byteCount = 1,
        formula = { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128).toDouble() else 0.0 },
        normalRangeMin = 0.0,
        normalRangeMax = 100.0,
        description = "Vom Fahrer angefordertes Drehmoment (Gaspedal-Anforderung)"
    )

    val BOOST_PRESSURE_ACTUAL = Mode22PIDDefinition(
        code = "221008",
        displayName = "Boost-Druck Ist",
        unit = "kPa",
        byteCount = 2,
        formula = { b ->
            if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
        },
        normalRangeMin = 30.0,
        normalRangeMax = 250.0,
        description = "Tatsächlicher Ladedruck absolut (atmosphärisch ~100 kPa, max ~180 kPa)"
    )

    val BOOST_PRESSURE_TARGET = Mode22PIDDefinition(
        code = "221009",
        displayName = "Boost-Druck Soll",
        unit = "kPa",
        byteCount = 2,
        formula = { b ->
            if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
        },
        normalRangeMin = 30.0,
        normalRangeMax = 250.0,
        description = "Vom Steuergerät angeforderter Ladedruck (Sollwert)"
    )

    val WASTEGATE_POSITION = Mode22PIDDefinition(
        code = "22100A",
        displayName = "Wastegate-Position",
        unit = "%",
        byteCount = 1,
        formula = { b -> if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0 },
        normalRangeMin = 0.0,
        normalRangeMax = 100.0,
        description = "Position des Wastegate-Ventils (0% = geschlossen, 100% = voll geöffnet)"
    )

    val TURBO_RPM = Mode22PIDDefinition(
        code = "22100B",
        displayName = "Turbo-Drehzahl",
        unit = "rpm",
        byteCount = 2,
        formula = { b ->
            if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
        },
        normalRangeMin = 0.0,
        normalRangeMax = 200000.0,
        description = "Drehzahl des Turboladers (BorgWarner KP39, max ~200.000 rpm)"
    )

    val OIL_TEMPERATURE = Mode22PIDDefinition(
        code = "22100C",
        displayName = "Motoröl-Temperatur",
        unit = "°C",
        byteCount = 1,
        formula = { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0 },
        normalRangeMin = 70.0,
        normalRangeMax = 120.0,
        description = "Motöröl-Temperatur (optimal 90–110°C, max 120°C)"
    )

    val COOLANT_TEMPERATURE = Mode22PIDDefinition(
        code = "22100D",
        displayName = "Kühlmittel-Temperatur",
        unit = "°C",
        byteCount = 1,
        formula = { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0 },
        normalRangeMin = 80.0,
        normalRangeMax = 105.0,
        description = "Kühlmittel-Temperatur (Thermostat öffnet ~95°C, max 105°C)"
    )

    val INTAKE_AIR_TEMPERATURE = Mode22PIDDefinition(
        code = "22100E",
        displayName = "Ansaugluft-Temperatur",
        unit = "°C",
        byteCount = 1,
        formula = { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0 },
        normalRangeMin = -20.0,
        normalRangeMax = 60.0,
        description = "Temperatur der Ansaugluft (vor oder nach Intercooler)"
    )

    val FUEL_RAIL_PRESSURE = Mode22PIDDefinition(
        code = "22100F",
        displayName = "Einspritzdruck",
        unit = "kPa",
        byteCount = 2,
        formula = { b ->
            if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) * 10.0 else 0.0
        },
        normalRangeMin = 3500.0,
        normalRangeMax = 5500.0,
        description = "Kraftstoffeinspritzdruck auf der Rail (Leerlauf ~3500–4500 kPa, Last ~4000–5500 kPa)"
    )

    val INJECTOR_PULSE_WIDTH = Mode22PIDDefinition(
        code = "221010",
        displayName = "Einspritzdauer",
        unit = "ms",
        byteCount = 2,
        formula = { b ->
            if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) / 100.0 else 0.0
        },
        normalRangeMin = 0.5,
        normalRangeMax = 15.0,
        description = "Einspritzdauer der Injektoren (Leerlauf ~1–3 ms, Volllast ~5–12 ms)"
    )

    val VVT_INTAKE = Mode22PIDDefinition(
        code = "221015",
        displayName = "VVT-Ansaugseite",
        unit = "°",
        byteCount = 1,
        formula = { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128).toDouble() else 0.0 },
        normalRangeMin = -30.0,
        normalRangeMax = 50.0,
        description = "Nockenwellenverstellung Ansaugseite (DCVCP, positiver Wert = früher)"
    )

    val VVT_EXHAUST = Mode22PIDDefinition(
        code = "221016",
        displayName = "VVT-Auslassseite",
        unit = "°",
        byteCount = 1,
        formula = { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128).toDouble() else 0.0 },
        normalRangeMin = -30.0,
        normalRangeMax = 30.0,
        description = "Nockenwellenverstellung Auslassseite (DCVCP, negativer Wert = später)"
    )

    val FUEL_CONSUMPTION_INSTANT = Mode22PIDDefinition(
        code = "221018",
        displayName = "Kraftstoffverbrauch aktuell",
        unit = "L/h",
        byteCount = 2,
        formula = { b ->
            if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
        },
        normalRangeMin = 0.0,
        normalRangeMax = 30.0,
        description = "Aktueller Kraftstoffverbrauch in Liter pro Stunde"
    )

    val FUEL_CONSUMPTION_AVERAGE = Mode22PIDDefinition(
        code = "22101A",
        displayName = "Kraftstoffverbrauch Ø",
        unit = "L/100km",
        byteCount = 2,
        formula = { b ->
            if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) / 10.0 else 0.0
        },
        normalRangeMin = 0.0,
        normalRangeMax = 20.0,
        description = "Durchschnittlicher Kraftstoffverbrauch (Werk: 6.0 L/100km kombi., 7.8 Stadt, 5.0 Land)"
    )

    val AFR_RATIO = Mode22PIDDefinition(
        code = "22101F",
        displayName = "Luft-Kraftstoff-Verhältnis",
        unit = "λ",
        byteCount = 2,
        formula = { b ->
            if (b.size >= 2) 2.0 * (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) / 65536.0 else 0.0
        },
        normalRangeMin = 0.85,
        normalRangeMax = 1.15,
        description = "Luft-Kraftstoff-Verhältnis (λ = 1.0 = stöchiometrisch, 14.7:1)"
    )

    // =========================================================================
    // Alle PIDs als Liste
    // =========================================================================

    val ALL_PIDS: List<Mode22PIDDefinition> = listOf(
        ENGINE_TORQUE,
        REQUESTED_TORQUE,
        BOOST_PRESSURE_ACTUAL,
        BOOST_PRESSURE_TARGET,
        WASTEGATE_POSITION,
        TURBO_RPM,
        OIL_TEMPERATURE,
        COOLANT_TEMPERATURE,
        INTAKE_AIR_TEMPERATURE,
        FUEL_RAIL_PRESSURE,
        INJECTOR_PULSE_WIDTH,
        VVT_INTAKE,
        VVT_EXHAUST,
        FUEL_CONSUMPTION_INSTANT,
        FUEL_CONSUMPTION_AVERAGE,
        AFR_RATIO
    )

    private val PID_MAP: Map<String, Mode22PIDDefinition> = ALL_PIDS.associateBy { it.code }

    // =========================================================================
    // Support-Check
    // =========================================================================

    /**
     * Prüft ob ein bestimmter Mode 22 PID vom Steuergerät unterstützt wird.
     * @param supportedPIDs Die vom ECU zurückgegebenen unterstützten PIDs (Antwort auf $22 00 00)
     * @param pidCode Der zu prüfende PID-Code (z.B. "221008")
     * @return true wenn der PID unterstützt wird
     */
    fun isPIDSupported(supportedPIDs: Set<String>, pidCode: String): Boolean {
        return pidCode in supportedPIDs
    }

    /**
     * Gibt alle unterstützten PIDs aus der gegebenen Liste zurück.
     */
    fun getSupportedPIDs(allSupportedPIDs: Set<String>): List<Mode22PIDDefinition> {
        return ALL_PIDS.filter { it.code in allSupportedPIDs }
    }

    /**
     * Einfacher Support-Check: Gibt die PIDs zurück, die in einer gegebenen
     * hexadezimalen Bitmaske als unterstützt markiert sind.
     *
     * Die Bitmaske beginnt bei PID 0x220000 und prüft die Bits für die
     * erweiterten PIDs (Bit 0 = erstes PID, Bit 1 = zweites, usw.).
     *
     * @param bitmask Byte-Array der Support-Antwort
     * @param baseCode Der Basiscode der Gruppe (z.B. "221000")
     * @return Liste der unterstützten PID-Codes
     */
    fun parseSupportedPIDs(bitmask: ByteArray, baseCode: String = "221000"): Set<String> {
        val supported = mutableSetOf<String>()
        val baseNum = baseCode.toLong(16)
        for (byteIndex in bitmask.indices) {
            val byteVal = bitmask[byteIndex].toInt() and 0xFF
            for (bit in 0..7) {
                if (byteVal and (1 shl (7 - bit)) != 0) {
                    val pidNum = baseNum + (byteIndex * 8) + bit + 1
                    val pidHex = "22%04X".format(pidNum)
                    if (pidHex in PID_MAP) {
                        supported.add(pidHex)
                    }
                }
            }
        }
        return supported
    }

    // =========================================================================
    // Hilfsfunktionen
    // =========================================================================

    /**
     * Lookup eines PIDs anhand des Codes.
     */
    fun fromCode(code: String): Mode22PIDDefinition? = PID_MAP[code]

    /**
     * Decodiert ein Byte-Array mit dem passenden Formula für den gegebenen PID.
     */
    fun decode(code: String, data: ByteArray): Double? {
        return PID_MAP[code]?.decode(data)
    }

    /**
     * Berechnet Boost-Delta (Ist - Soll) in kPa.
     * Positiver Wert = Überdruck, negativer Wert = Unterdruck gegenüber Soll.
     *
     * @param actual Tatsächlicher Boost-Druck in kPa (absolut)
     * @param target Angeforderter Boost-Druck in kPa (absolut)
     * @return Delta in kPa (relativ zum Soll)
     */
    fun calculateBoostDelta(actual: Double, target: Double): Double {
        return actual - target
    }

    /**
     * Berechnet Boost-Abweichung als Prozentwert.
     *
     * @param actual Tatsächlicher Boost-Druck in kPa
     * @param target Angeforderter Boost-Druck in kPa
     * @return Abweichung in Prozent (positiv = mehr Boost als Soll)
     */
    fun calculateBoostDeviationPercent(actual: Double, target: Double): Double {
        if (target <= 0.0) return 0.0
        return ((actual - target) / target) * 100.0
    }

    /**
     * Berechnet den relativen Boost über atmosphärischem Druck.
     * Annahme: ~100 kPa atmosphärisch.
     *
     * @param absolutePressure Absoluter Boost-Druck in kPa
     * @return Relativer Boost in kPa (0 = kein Überdruck)
     */
    fun relativeBoostKpa(absolutePressure: Double): Double {
        return (absolutePressure - 100.0).coerceAtLeast(0.0)
    }

    /**
     * Berechnet Boost in Bar (relativ).
     */
    fun relativeBoostBar(absolutePressure: Double): Double {
        return relativeBoostKpa(absolutePressure) / 100.0
    }

    /**
     * Normalwerte für den Astra J 1.4 Turbo (A14NET) als Schnellreferenz.
     */
    object NormalValues {
        const val ENGINE_TORQUE_MIN = 0.0
        const val ENGINE_TORQUE_MAX = 100.0

        const val BOOST_ACTUAL_IDLE_KPA = 30.0
        const val BOOST_ACTUAL_CRUISE_KPA = 100.0
        const val BOOST_ACTUAL_WOT_KPA = 170.0
        const val BOOST_ACTUAL_OVERBOOST_KPA = 220.0
        const val BOOST_ACTUAL_MAX_KPA = 250.0

        const val BOOST_TARGET_IDLE_KPA = 30.0
        const val BOOST_TARGET_NORMAL_KPA = 170.0
        const val BOOST_TARGET_OVERBOOST_KPA = 220.0

        const val WASTEGATE_IDLE_PERCENT = 85.0
        const val WASTEGATE_WOT_MIN_PERCENT = 25.0
        const val WASTEGATE_WOT_MAX_PERCENT = 60.0

        const val TURBO_RPM_IDLE = 8000.0
        const val TURBO_RPM_NORMAL_MIN = 80000.0
        const val TURBO_RPM_NORMAL_MAX = 150000.0
        const val TURBO_RPM_MAX = 200000.0

        const val OIL_TEMP_MIN_C = 70.0
        const val OIL_TEMP_OPTIMAL_MIN_C = 90.0
        const val OIL_TEMP_OPTIMAL_MAX_C = 110.0
        const val OIL_TEMP_MAX_C = 120.0

        const val COOLANT_TEMP_MIN_C = 80.0
        const val COOLANT_TEMP_OPTIMAL_MIN_C = 85.0
        const val COOLANT_TEMP_OPTIMAL_MAX_C = 100.0
        const val COOLANT_TEMP_MAX_C = 105.0

        const val INTAKE_AIR_TEMP_MIN_C = -20.0
        const val INTAKE_AIR_TEMP_MAX_C = 60.0
        const val INTAKE_AIR_TEMP_INTERCOOLER_MAX_C = 50.0

        const val FUEL_RAIL_PRESSURE_IDLE_KPA = 3500.0
        const val FUEL_RAIL_PRESSURE_WOT_MIN_KPA = 4000.0
        const val FUEL_RAIL_PRESSURE_WOT_MAX_KPA = 5500.0

        const val INJECTOR_PULSE_IDLE_MS = 2.0
        const val INJECTOR_PULSE_WOT_MAX_MS = 12.0

        const val VVT_INTAKE_MIN_DEG = -30.0
        const val VVT_INTAKE_MAX_DEG = 50.0

        const val VVT_EXHAUST_MIN_DEG = -30.0
        const val VVT_EXHAUST_MAX_DEG = 30.0

        const val FUEL_CONSUMPTION_COMBINED_L100 = 6.0
        const val FUEL_CONSUMPTION_URBAN_L100 = 7.8
        const val FUEL_CONSUMPTION_EXTRA_URBAN_L100 = 5.0

        const val AFR_STOICHIOMETRIC = 1.0
        const val AFR_RICH_MIN = 0.85
        const val AFR_LEAN_MAX = 1.15
    }
}
