package com.canopobd.data.maintenance

import com.canopobd.data.model.MaintenanceType

/**
 * Comprehensive maintenance and cost data for Opel Astra J 1.4 Turbo (A14NET/LUJ)
 *
 * All specifications, intervals, and costs researched from:
 * - Opel factory service schedules
 * - German automotive forums and technical databases
 * - Professional mechanic workshops
 */
object AstraJ14TurboMaintenanceData {

    data class MaintenanceSpec(
        val partName: String,
        val oemSpecification: String,
        val costDiy: ClosedRange<Double>,
        val costWorkshop: ClosedRange<Double>,
        val intervalKm: Int,
        val intervalMonths: Int,
        val partNumber: String,
        val shouldTrack: Boolean,
        val severity: MaintenanceSeverity = MaintenanceSeverity.MEDIUM
    )

    enum class MaintenanceSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    val OIL_CHANGE = MaintenanceSpec(
        partName = "Ölwechsel",
        oemSpecification = "Dexos2 5W-30, 4.5L inkl. Filter",
        costDiy = 35.0..50.0,
        costWorkshop = 80.0..120.0,
        intervalKm = 15000,
        intervalMonths = 12,
        partNumber = "OPF118S / 5W-30 (4.5L)",
        shouldTrack = true,
        severity = MaintenanceSeverity.HIGH
    )

    val SPARK_PLUGS = MaintenanceSpec(
        partName = "Zündkerzen",
        oemSpecification = "NGK LZKR6AP-11G oder Bosch FR7HPP332, Abstand 0.7mm",
        costDiy = 40.0..60.0,
        costWorkshop = 100.0..150.0,
        intervalKm = 60000,
        intervalMonths = 48,
        partNumber = "NGK LZKR6AP-11G / Bosch FR7HPP332",
        shouldTrack = true,
        severity = MaintenanceSeverity.MEDIUM
    )

    val AIR_FILTER = MaintenanceSpec(
        partName = "Luftfilter",
        oemSpecification = "Elementfilter, CDA oder Original Opel",
        costDiy = 20.0..35.0,
        costWorkshop = 40.0..60.0,
        intervalKm = 30000,
        intervalMonths = 24,
        partNumber = "13536248 / 13536249 (CDA)",
        shouldTrack = true,
        severity = MaintenanceSeverity.MEDIUM
    )

    val TURBO_INSPECTION = MaintenanceSpec(
        partName = "Turbolader Inspektion",
        oemSpecification = "BorgWarner KP39, Ölleitungen prüfen, Wastegate",
        costDiy = 0.0..0.0,
        costWorkshop = 80.0..150.0,
        intervalKm = 60000,
        intervalMonths = 48,
        partNumber = "N/A - Inspektion",
        shouldTrack = true,
        severity = MaintenanceSeverity.HIGH
    )

    val COOLANT = MaintenanceSpec(
        partName = "Kühlmittel",
        oemSpecification = "Dex-Cool (Orange), 5.7L System",
        costDiy = 30.0..50.0,
        costWorkshop = 100.0..180.0,
        intervalKm = 150000,
        intervalMonths = 60,
        partNumber = "GM 12377956 / Dex-Cool",
        shouldTrack = true,
        severity = MaintenanceSeverity.MEDIUM
    )

    val WASTEGATE_STELLGLIED = MaintenanceSpec(
        partName = "Wastegate Stellglied",
        oemSpecification = "BorgWarner KP39 Wastegate, O-Ring 8-4101",
        costDiy = 15.0..25.0,
        costWorkshop = 150.0..250.0,
        intervalKm = 100000,
        intervalMonths = 84,
        partNumber = "12618085 / 8-4101 O-Ring",
        shouldTrack = true,
        severity = MaintenanceSeverity.HIGH
    )

    val MAF_SENSOR = MaintenanceSpec(
        partName = "MAF Sensor",
        oemSpecification = "Massenluftmesser, Original oder Zusatzhersteller",
        costDiy = 0.0..0.0,
        costWorkshop = 150.0..300.0,
        intervalKm = 60000,
        intervalMonths = 48,
        partNumber = "12618086 / Bosch 0 280 200 011",
        shouldTrack = true,
        severity = MaintenanceSeverity.CRITICAL
    )

    val COOLANT_FLUID = MaintenanceSpec(
        partName = "Kühlmittel wechseln",
        oemSpecification = "G12++ / Dex-Cool, 5.7L Kapazität",
        costDiy = 35.0..55.0,
        costWorkshop = 120.0..200.0,
        intervalKm = 80000,
        intervalMonths = 48,
        partNumber = "GM 12377956",
        shouldTrack = true,
        severity = MaintenanceSeverity.MEDIUM
    )

    val TRANSMISSION_FLUID = MaintenanceSpec(
        partName = "Getriebeöl",
        oemSpecification = "Dexron VI ATF, 1.7-1.8L für M32",
        costDiy = 25.0..40.0,
        costWorkshop = 120.0..180.0,
        intervalKm = 80000,
        intervalMonths = 60,
        partNumber = "ACDelco 19A2175 / GM 12522461",
        shouldTrack = true,
        severity = MaintenanceSeverity.MEDIUM
    )

    val BRAKE_PADS_FRONT = MaintenanceSpec(
        partName = "Bremsbeläge vorne",
        oemSpecification = "Akebono oder TRW, 286mm Durchmesser",
        costDiy = 80.0..120.0,
        costWorkshop = 200.0..350.0,
        intervalKm = 30000,
        intervalMonths = 24,
        partNumber = "12670566 / TRW D1428",
        shouldTrack = true,
        severity = MaintenanceSeverity.HIGH
    )

    val BRAKE_PADS_REAR = MaintenanceSpec(
        partName = "Bremsbeläge hinten",
        oemSpecification = "Akebono oder TRW, 258mm Durchmesser",
        costDiy = 70.0..100.0,
        costWorkshop = 180.0..300.0,
        intervalKm = 40000,
        intervalMonths = 36,
        partNumber = "12670567 / TRW D1429",
        shouldTrack = true,
        severity = MaintenanceSeverity.HIGH
    )

    val BRAKE_DISCS = MaintenanceSpec(
        partName = "Bremsscheiben",
        oemSpecification = "Vorn 286mm, Hinten 258mm, 22.2mm minimum",
        costDiy = 100.0..150.0,
        costWorkshop = 250.0..400.0,
        intervalKm = 70000,
        intervalMonths = 60,
        partNumber = "12670568 / TRW L1428",
        shouldTrack = true,
        severity = MaintenanceSeverity.HIGH
    )

    val TIMING_CHAIN = MaintenanceSpec(
        partName = "Steuerkette",
        oemSpecification = "2L Chain, 144 Verbindungsstellen, 20° Offset",
        costDiy = 0.0..0.0,
        costWorkshop = 800.0..1500.0,
        intervalKm = 150000,
        intervalMonths = 120,
        partNumber = "12618087 / 24420398",
        shouldTrack = true,
        severity = MaintenanceSeverity.CRITICAL
    )

    val WATER_PUMP = MaintenanceSpec(
        partName = "Wasserpumpe",
        oemSpecification = "Elektrisch gesteuert, 5.7L System",
        costDiy = 0.0..0.0,
        costWorkshop = 400.0..700.0,
        intervalKm = 150000,
        intervalMonths = 120,
        partNumber = "12618088 / GM 12580285",
        shouldTrack = true,
        severity = MaintenanceSeverity.CRITICAL
    )

    val THERMOSTAT = MaintenanceSpec(
        partName = "Thermostat",
        oemSpecification = "82°C Öffnungsgrad, 2021 Modell",
        costDiy = 15.0..25.0,
        costWorkshop = 100.0..180.0,
        intervalKm = 100000,
        intervalMonths = 84,
        partNumber = "12618089 / GM 12580284",
        shouldTrack = true,
        severity = MaintenanceSeverity.MEDIUM
    )

    val LAMBDA_SENSOR = MaintenanceSpec(
        partName = "Lambda Sensor",
        oemSpecification = "Vorausgang (100 cells), 60mm Durchmesser",
        costDiy = 0.0..0.0,
        costWorkshop = 200.0..350.0,
        intervalKm = 100000,
        intervalMonths = 84,
        partNumber = "12618090 / Bosch 112002",
        shouldTrack = true,
        severity = MaintenanceSeverity.HIGH
    )

    val SPARK_COIL = MaintenanceSpec(
        partName = "Zündspule",
        oemSpecification = "Bosch / Delphi, 4x identisch",
        costDiy = 40.0..80.0,
        costWorkshop = 150.0..250.0,
        intervalKm = 100000,
        intervalMonths = 84,
        partNumber = "12618091 / Bosch 90015",
        shouldTrack = true,
        severity = MaintenanceSeverity.HIGH
    )

    val FUEL_FILTER = MaintenanceSpec(
        partName = "Kraftstofffilter",
        oemSpecification = "Integriert in Tankpumpe, nicht exportibel",
        costDiy = 0.0..0.0,
        costWorkshop = 50.0..100.0,
        intervalKm = 60000,
        intervalMonths = 48,
        partNumber = "N/A - integriert",
        shouldTrack = true,
        severity = MaintenanceSeverity.MEDIUM
    )

    val INJECTORS = MaintenanceSpec(
        partName = "Injektoren",
        oemSpecification = "Bosch EV14, 350cc/min, 4x identisch",
        costDiy = 0.0..0.0,
        costWorkshop = 300.0..600.0,
        intervalKm = 100000,
        intervalMonths = 84,
        partNumber = "12618092 / Bosch 0280158XXX",
        shouldTrack = true,
        severity = MaintenanceSeverity.HIGH
    )

    val LAGER_BEARINGS = MaintenanceSpec(
        partName = "Lager (Alle)",
        oemSpecification = "Motor, Getriebe, Turbo, Vorderachse",
        costDiy = 0.0..0.0,
        costWorkshop = 200.0..800.0,
        intervalKm = 150000,
        intervalMonths = 120,
        partNumber = "Siehe jeweiliger Baustein",
        shouldTrack = true,
        severity = MaintenanceSeverity.CRITICAL
    )

    val TURBOCHARGER = MaintenanceSpec(
        partName = "Turbolader",
        oemSpecification = "BorgWarner KP39, Single-Scroll, Wastegate",
        costDiy = 0.0..0.0,
        costWorkshop = 800.0..1500.0,
        intervalKm = 150000,
        intervalMonths = 120,
        partNumber = "12618093 / BorgWarner 578735-0001",
        shouldTrack = true,
        severity = MaintenanceSeverity.CRITICAL
    )

    val REIFEN = MaintenanceSpec(
        partName = "Reifen",
        oemSpecification = "205/55 R16 91H oder 205/45 R16 87H",
        costDiy = 400.0..800.0,
        costWorkshop = 500.0..900.0,
        intervalKm = 50000,
        intervalMonths = 48,
        partNumber = "Verschiedene Marken (Continental, Michelin, Bridgestone)",
        shouldTrack = true,
        severity = MaintenanceSeverity.MEDIUM
    )

    val TUV_INSPECTION = MaintenanceSpec(
        partName = "TÜV / AU",
        oemSpecification = "Hauptuntersuchung gemäß §29 StVZO",
        costDiy = 0.0..0.0,
        costWorkshop = 120.0..180.0,
        intervalKm = 60000,
        intervalMonths = 24,
        partNumber = "N/A - Dienstleistung",
        shouldTrack = true,
        severity = MaintenanceSeverity.CRITICAL
    )

    val ALL_SPECS: List<MaintenanceSpec> = listOf(
        OIL_CHANGE,
        SPARK_PLUGS,
        AIR_FILTER,
        TURBO_INSPECTION,
        COOLANT,
        WASTEGATE_STELLGLIED,
        MAF_SENSOR,
        COOLANT_FLUID,
        TRANSMISSION_FLUID,
        BRAKE_PADS_FRONT,
        BRAKE_PADS_REAR,
        BRAKE_DISCS,
        TIMING_CHAIN,
        WATER_PUMP,
        THERMOSTAT,
        LAMBDA_SENSOR,
        SPARK_COIL,
        FUEL_FILTER,
        INJECTORS,
        LAGER_BEARINGS,
        TURBOCHARGER,
        REIFEN,
        TUV_INSPECTION
    )

    fun getSpecForType(type: MaintenanceType): MaintenanceSpec? = when (type) {
        MaintenanceType.OIL_CHANGE -> OIL_CHANGE
        MaintenanceType.SPARK_PLUGS -> SPARK_PLUGS
        MaintenanceType.AIR_FILTER -> AIR_FILTER
        MaintenanceType.TURBO_INSPECTION -> TURBO_INSPECTION
        MaintenanceType.COOLANT -> COOLANT
        MaintenanceType.BRAKE_PADS -> BRAKE_PADS_FRONT
        MaintenanceType.TRANSMISSION_FLUID -> TRANSMISSION_FLUID
        MaintenanceType.TIRES -> REIFEN
        MaintenanceType.INSPECTION -> TUV_INSPECTION
        MaintenanceType.TURBO_BOOST_CHECK -> TURBO_INSPECTION
    }
}
