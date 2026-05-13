package com.canopobd.data.model

enum class CarProfile(
    val id: String,
    val displayName: String,
    val engineCode: String,
    val displacement: String,
    val power: String,
    val torque: String,
    val redlineRpm: Int,
    val maxBoostGaugeBar: Float,
    val normalBoostBar: Float,
    val overboostBar: Float,
    val peakRpmPower: Int,
    val peakRpmTorque: Int,
    val fuelType: String,
    val transmissionType: String,
    val oilSpec: String,
    val oilCapacity: String,
    val fuelTankCapacity: String,
    val turboType: String,
    val ecuType: String
) {
    ASTRA_J_2012_14T(
        id = "astra_j_2012_14t",
        displayName = "Opel Astra J 1.4 Turbo (140 PS)",
        engineCode = "A14NET / B14NET / LUJ",
        displacement = "1364 ccm (1.4L)",
        power = "103 kW (140 PS) @ 4.900–6.000 rpm",
        torque = "200 Nm @ 1.850–4.900 rpm (Overboost: 220 Nm max. 10 Sek.)",
        redlineRpm = 6500,
        maxBoostGaugeBar = 1.3f,
        normalBoostBar = 0.7f,
        overboostBar = 1.2f,
        peakRpmPower = 5500,
        peakRpmTorque = 3000,
        fuelType = "Benzin (Super 95 min / 98 empfohlen)",
        transmissionType = "6-Gang Schaltgetriebe (Getrag M32) / 6-Gang Automatik",
        oilSpec = "dexos2 5W-30",
        oilCapacity = "4,5 Liter mit Filter",
        fuelTankCapacity = "56 Liter",
        turboType = "BorgWarner KP39 (Single-Scroll, Fixed-Geometry, Wastegate-geregelt)",
        ecuType = "Bosch ME17.9.22 / Delco E78"
    );

    companion object {
        fun fromId(id: String): CarProfile? = entries.find { it.id == id }
        fun default(): CarProfile = ASTRA_J_2012_14T

        fun fromVehicleProfile(vp: VehicleProfile): CarProfile? =
            entries.find { it.id == vp.id }

        fun allWithVehicleProfile(): List<Pair<CarProfile, VehicleProfile>> =
            entries.mapNotNull { cp ->
                VehicleProfiles.fromId(cp.id)?.let { vp -> cp to vp }
            }
    }

    fun toVehicleProfile(): VehicleProfile? = VehicleProfiles.fromId(id)
}

data class TurboData(
    val boostPressure: Double = 0.0,
    val boostTarget: Double = 0.0,
    val wastegateDutyCycle: Double = 0.0,
    val turboRpm: Double = 0.0,
    val turboInletTemp: Double = 0.0,
    val turboOutletTemp: Double = 0.0,
    val chargeAirCoolerTemp: Double = 0.0,
    val turboHealthScore: Int = 100,
    val overboostActive: Boolean = false,
    val overboostSecondsRemaining: Int = 0,
    val overboostMaxDuration: Int = 10,
    val underboostDetected: Boolean = false,
    val wastegateDutyAtIdle: Double = 95.0,
    val wastegateDutyMaxBoost: Double = 25.0,
    val currentTorqueNm: Double = 0.0,
    val maxTorqueNm: Double = 200.0,
    val overboostTorqueNm: Double = 220.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val boostBar: Double get() = boostPressure / 100.0
    val relativeBoostBar: Double get() = (boostBar - 1.0).coerceAtLeast(0.0)
    val isOverboost: Boolean get() = boostBar > 1.0
    val overboostPercentage: Double get() = if (isOverboost) ((boostBar - 1.0) / 0.3) * 100.0 else 0.0
}

data class OilData(
    val temperature: Double = 0.0,
    val pressure: Double = 0.0,
    val oilLifeRemaining: Int = 100,
    val consumptionWarning: Boolean = false,
    val consumptionRateL1000km: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class TimingChainState(
    val healthScore: Int = 100,
    val coldStartRattleDetected: Boolean = false,
    val rattleDurationMs: Long = 0L,
    val idleRpmVariation: Double = 0.0,
    val isWarmedUp: Boolean = false,
    val statusMessage: String = "Prüfung läuft…",
    val phase: TimingChainPhase = TimingChainPhase.UNKNOWN,
    val recordedSamples: Int = 0,
    val coldSampleCount: Int = 0,
    val warmSampleCount: Int = 0,
    val avgRpmCold: Double = 0.0,
    val avgRpmWarm: Double = 0.0,
    val rpmDeviationCold: Double = 0.0,
    val lastRpmReading: Double = 0.0,
    val coldStartTimestamp: Long = 0L,
    val warmupCompleteTimestamp: Long = 0L
)

enum class TimingChainPhase(val label: String, val description: String) {
    UNKNOWN("Unbekannt", "Motor aus"),
    CRANKING("Anlassen", "Anlasser dreht"),
    COLD_RATTLE("Kaltstart-Rattern", "Rattern erkannt — Steuerkette prüfen!"),
    WARMING_UP("Aufwärmen", "Motor erreicht Betriebstemperatur"),
    STABLE("Stabil", "Leerlauf stabil"),
    HEALTHY("Gesund", "Steuerkette in gutem Zustand"),
    MONITORING("Überwachung", "Kontinuierliche Überwachung"),
    WARNING("Warnung", "Steuerkette-Warnung aktiv"),
    CRITICAL("Kritisch", "Sofort prüfen lassen!")
}

data class TurboCoolDownState(
    val isActive: Boolean = false,
    val secondsRemaining: Int = 0,
    val totalSeconds: Int = 0,
    val triggeredByRpm: Boolean = false,
    val triggeredBySpeed: Boolean = false,
    val triggeredByBoost: Boolean = false,
    val progress: Float = 0f,
    val statusMessage: String = ""
)
