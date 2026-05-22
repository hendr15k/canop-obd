package com.canopobd.data.model

data class VehicleProfile(
    val id: String,
    val displayName: String,
    val manufacturer: String,
    val model: String,
    val year: Int,
    val engineCode: String,
    val displacement: String,
    val powerKw: Int,
    val powerHp: Int,
    val torqueNm: Int,
    val redlineRpm: Int,
    val maxBoostBar: Float,
    val fuelType: String,
    val recommendedFuelOctane: Int,
    val ecuType: String,
    val transmission: String,
    val calibration: AstraJ14TurboCalibration?,
    val gaugePreset: DashboardPreset,
    val alertConfig: AlertConfig,
    val maintenanceItems: List<MaintenanceItem>,
    val knownIssues: List<KnownIssue>
) {
    val displayNameFull: String get() = "$manufacturer $model $year ($powerHp PS)"
    val powerLabel: String get() = "$powerKw kW ($powerHp PS)"
    val torqueLabel: String get() = "${torqueNm} Nm"
}

object VehicleProfiles {
    val ASTRA_J_14T = VehicleProfile(
        id = "astra_j_2012_14t",
        displayName = "Opel Astra J 1.4 Turbo",
        manufacturer = "Opel",
        model = "Astra J",
        year = 2012,
        engineCode = "A14NET / B14NET / LUJ",
        displacement = "1364 ccm (1.4L)",
        powerKw = 103,
        powerHp = 140,
        torqueNm = 200,
        redlineRpm = 6500,
        maxBoostBar = 1.0f,
        fuelType = "Benzin (Super 95/98)",
        recommendedFuelOctane = 98,
        ecuType = "Bosch ME17.9.24 / Delco E78",
        transmission = "6-Gang Manuell / 6-Gang Auto",
        calibration = AstraJ14TurboCalibration.INSTANCE,
        gaugePreset = AstraJ14TurboCalibration.DASHBOARD_PRESET,
        alertConfig = AstraJ14TurboCalibration.ALERT_CONFIG,
        maintenanceItems = AstraJ14TurboCalibration.MAINTENANCE_ITEMS,
        knownIssues = AstraJ14TurboCalibration.KNOWN_ISSUES
    )

    val profiles = listOf(ASTRA_J_14T)

    fun fromId(id: String): VehicleProfile? = profiles.find { it.id == id }
}
