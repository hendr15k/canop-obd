package com.canopobd.data.model

object AstraJCodingModels {

    enum class Module(val displayName: String, val address: String) {
        UEC("UEC - Underhood Electrical Center", "0x09"),
        REC("REC - Rear Electrical Center", "0x2E"),
        BCM("BCM - Body Control Module", "0xFF"),
        IPC("IPC - Instrument Panel Cluster", "0x83"),
        CIM("CIM - Column Integration Module", "0x7E"),
        ECU("ECU - Engine Control Unit", "0x01"),
        TCM("TCM - Transmission Control Module", " "),
    }

    data class CodingOption(
        val id: String,
        val module: Module,
        val channel: String,
        val displayName: String,
        val description: String,
        val values: List<CodingValue>,
        val currentValue: CodingValue? = null,
        val requiresCarPass: Boolean = true,
        val hardwareRequired: String? = null
    )

    data class CodingValue(
        val value: String,
        val displayName: String,
        val description: String = ""
    )

    data class CodingCategory(
        val id: String,
        val displayName: String,
        val icon: String,
        val options: List<CodingOption>
    )

    data class CodingProfile(
        val id: String,
        val name: String,
        val description: String,
        val options: Map<String, String>
    )

    data class CodingResult(
        val success: Boolean,
        val option: CodingOption,
        val newValue: CodingValue,
        val timestamp: Long = System.currentTimeMillis(),
        val error: String? = null
    )
}

object AstraJBleuchtungCoding {

    val tagfahrlichtOptions = listOf(
        AstraJCodingModels.CodingOption(
            id = "drl_disable",
            module = AstraJCodingModels.Module.UEC,
            channel = "Daytime Running Light",
            displayName = "Tagfahrlicht",
            description = "Tagfahrlicht aktivieren oder deaktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Variante 1 - EU Standard"),
                AstraJCodingModels.CodingValue("2", "Variante 2 - Dimmung"),
                AstraJCodingModels.CodingValue("3", "Variante 3 - Skandinavien"),
                AstraJCodingModels.CodingValue("4", "Variante 4 - LED"),
                AstraJCodingModels.CodingValue("5", "Variante 5 - Voll")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "drl_with_parking",
            module = AstraJCodingModels.Module.UEC,
            channel = "DRL with Parking Light",
            displayName = "DRL mit Standlicht",
            description = "Tagfahrlicht zusammen mit Standlicht",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "coming_home",
            module = AstraJCodingModels.Module.UEC,
            channel = "Coming Home",
            displayName = "Coming Home",
            description = "Licht nach dem Abschließen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "10 Sekunden"),
                AstraJCodingModels.CodingValue("2", "20 Sekunden"),
                AstraJCodingModels.CodingValue("3", "30 Sekunden"),
                AstraJCodingModels.CodingValue("4", "60 Sekunden"),
                AstraJCodingModels.CodingValue("5", "90 Sekunden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "leaving_home",
            module = AstraJCodingModels.Module.UEC,
            channel = "Leaving Home",
            displayName = "Leaving Home",
            description = "Licht beim Entriegeln",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "emergency_brake",
            module = AstraJCodingModels.Module.REC,
            channel = "Emergency Brake Light",
            displayName = "Adaptives Bremslicht",
            description = "Bremslicht blinkt bei Notbremsung",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "fog_lights",
            module = AstraJCodingModels.Module.UEC,
            channel = "Fog Lamps Front",
            displayName = "Nebelscheinwerfer",
            description = "Nebelscheinwerfer aktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "fog_as_drl",
            module = AstraJCodingModels.Module.UEC,
            channel = "Fog as DRL",
            displayName = "Nebelscheinwerfer als TFL",
            description = "Nebelscheinwerfer als Tagfahrlicht nutzen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nein"),
                AstraJCodingModels.CodingValue("1", "Ja")
            ),
            hardwareRequired = "Nebelscheinwerfer muss verbaut sein"
        ),
        AstraJCodingModels.CodingOption(
            id = "check_control",
            module = AstraJCodingModels.Module.UEC,
            channel = "Check Control",
            displayName = "Check Control Lampen",
            description = "Fehlerüberwachung für Lampen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "ambient_color",
            module = AstraJCodingModels.Module.REC,
            channel = "Ambient Light Color",
            displayName = "Ambientebeleuchtung Farbe",
            description = "Farbe der Ambientebeleuchtung",
            values = listOf(
                AstraJCodingModels.CodingValue("1", "Rot"),
                AstraJCodingModels.CodingValue("2", "Blau"),
                AstraJCodingModels.CodingValue("3", "Grün"),
                AstraJCodingModels.CodingValue("4", "Lila"),
                AstraJCodingModels.CodingValue("5", "Cyan"),
                AstraJCodingModels.CodingValue("6", "Gelb")
            ),
            hardwareRequired = "Ambientebeleuchtung muss verbaut sein"
        ),
        AstraJCodingModels.CodingOption(
            id = "interior_light_timeout",
            module = AstraJCodingModels.Module.BCM,
            channel = "Interior Light Timeout",
            displayName = "Innenbeleuchtung Timeout",
            description = "Zeit bis Innenbeleuchtung erlischt",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Sofort aus"),
                AstraJCodingModels.CodingValue("1", "10 Sekunden"),
                AstraJCodingModels.CodingValue("2", "20 Sekunden"),
                AstraJCodingModels.CodingValue("3", "30 Sekunden"),
                AstraJCodingModels.CodingValue("4", "60 Sekunden"),
                AstraJCodingModels.CodingValue("5", "90 Sekunden"),
                AstraJCodingModels.CodingValue("6", "120 Sekunden"),
                AstraJCodingModels.CodingValue("7", "150 Sekunden")
            )
        )
    )

    fun getCategory() = AstraJCodingModels.CodingCategory(
        id = "beleuchtung",
        displayName = "Beleuchtung",
        icon = "Lightbulb",
        options = tagfahrlichtOptions
    )
}

object AstraJKomfortCoding {

    val komfortOptions = listOf(
        AstraJCodingModels.CodingOption(
            id = "auto_lock_12",
            module = AstraJCodingModels.Module.UEC,
            channel = "Speed Dependent Locking",
            displayName = "Auto-Verriegelung 12 km/h",
            description = "Türen verriegeln automatisch bei 12 km/h",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "single_unlock",
            module = AstraJCodingModels.Module.BCM,
            channel = "Selective Door Unlock",
            displayName = "Einzelentriegelung",
            description = "1x drücken = Fahrertür, 2x = alle Türen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Alle Türen auf einmal"),
                AstraJCodingModels.CodingValue("1", "Einzelentriegelung")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "comfort_close",
            module = AstraJCodingModels.Module.REC,
            channel = "Windows Comfort Closing",
            displayName = "Komfortschließen",
            description = "Fenster mit Fernbedienung schließen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "comfort_open",
            module = AstraJCodingModels.Module.REC,
            channel = "Windows Comfort Opening",
            displayName = "Komfortöffnen",
            description = "Fenster mit Fernbedienung öffnen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "crash_unlock",
            module = AstraJCodingModels.Module.UEC,
            channel = "Crash Unlock Relay",
            displayName = "Crash-Entriegelung",
            description = "Türen öffnen bei Airbag-Auslösung",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "mirror_fold",
            module = AstraJCodingModels.Module.BCM,
            channel = "Power Folding Mirrors",
            displayName = "Spiegelanklappung",
            description = "Spiegel klappen bei Verriegelung ein",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Elektrisch anklappbare Spiegel"
        ),
        AstraJCodingModels.CodingOption(
            id = "mirror_unfold",
            module = AstraJCodingModels.Module.BCM,
            channel = "Power Unfolding Mirrors",
            displayName = "Spiegelausklappung",
            description = "Spiegel klappen bei Entriegelung aus",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Elektrisch anklappbare Spiegel"
        ),
        AstraJCodingModels.CodingOption(
            id = "rear_wiper_reverse",
            module = AstraJCodingModels.Module.REC,
            channel = "Rear Wiper Reverse",
            displayName = "Heckwischer Rückwärtsgang",
            description = "Heckwischer aktiv bei Rückwärtsgang",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Heckscheibenwischer muss verbaut sein"
        ),
        AstraJCodingModels.CodingOption(
            id = "acoustic_lock",
            module = AstraJCodingModels.Module.CIM,
            channel = "Acoustic Lock Confirmation",
            displayName = "Akustische Quittung",
            description = "Piepen bei Ver-/Entriegelung",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Bei Verriegelung"),
                AstraJCodingModels.CodingValue("2", "Bei Entriegelung"),
                AstraJCodingModels.CodingValue("3", "Beides")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "auto_relock",
            module = AstraJCodingModels.Module.BCM,
            channel = "Auto Relock",
            displayName = "Auto-Wiederverriegelung",
            description = "Auto verriegelt nach 3 Min wenn nicht geöffnet",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        )
    )

    fun getCategory() = AstraJCodingModels.CodingCategory(
        id = "komfort",
        displayName = "Komfort & ZV",
        icon = "Car",
        options = komfortOptions
    )
}

object AstraJMotorCoding {

    val motorOptions = listOf(
        AstraJCodingModels.CodingOption(
            id = "start_stop",
            module = AstraJCodingModels.Module.BCM,
            channel = "Start-Stop System",
            displayName = "Start-Stopp Automatik",
            description = "Start-Stopp System aktivieren/deaktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "eco_mode",
            module = AstraJCodingModels.Module.ECU,
            channel = "Eco Mode",
            displayName = "Eco-Modus",
            description = "ECO-Fahrmodus Parameter",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Standard"),
                AstraJCodingModels.CodingValue("1", "Eco Aktiviert"),
                AstraJCodingModels.CodingValue("2", "Sport Aktiviert")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "cruise_control",
            module = AstraJCodingModels.Module.CIM,
            channel = "Cruise Control",
            displayName = "Tempomat",
            description = "Tempomat aktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "overspeed_warning",
            module = AstraJCodingModels.Module.IPC,
            channel = "Overspeed Warning",
            displayName = "Geschwindigkeitswarnung",
            description = "Warnung bei Überschreitung 120 km/h",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        )
    )

    fun getCategory() = AstraJCodingModels.CodingCategory(
        id = "motor",
        displayName = "Motor & Antrieb",
        icon = "Engine",
        options = motorOptions
    )
}

object AstraJIPCCoding {

    val ipcOptions = listOf(
        AstraJCodingModels.CodingOption(
            id = "board_computer",
            module = AstraJCodingModels.Module.IPC,
            channel = "Board Computer",
            displayName = "Bordcomputer",
            description = "Bordcomputer-Anzeigen freischalten",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "dic_present",
            module = AstraJCodingModels.Module.IPC,
            channel = "Driver Information Center",
            displayName = "Fahrerinfo-Center",
            description = "DIC Menüs freischalten",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "instant_mpg",
            module = AstraJCodingModels.Module.IPC,
            channel = "Instant MPG Display",
            displayName = "Momentanverbrauch",
            description = "Momentanverbrauch im Display",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "avg_mpg",
            module = AstraJCodingModels.Module.IPC,
            channel = "Average MPG Display",
            displayName = "Durchschnittsverbrauch",
            description = "Durchschnittsverbrauch anzeigen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "range_display",
            module = AstraJCodingModels.Module.IPC,
            channel = "Fuel Range Display",
            displayName = "Reichweite",
            description = "Restreichweite anzeigen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "outside_temp",
            module = AstraJCodingModels.Module.IPC,
            channel = "Outside Temperature",
            displayName = "Außentemperatur",
            description = "Außentemperatur anzeigen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "oil_temp",
            module = AstraJCodingModels.Module.IPC,
            channel = "Oil Temperature Display",
            displayName = "Öltemperatur",
            description = "Öltemperatur im Display (Mode 22)",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "eco_index",
            module = AstraJCodingModels.Module.IPC,
            channel = "ECO Index Display",
            displayName = "ECO-Index",
            description = "ECO-Fahrindex anzeigen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "boost_gauge",
            module = AstraJCodingModels.Module.IPC,
            channel = "Turbo Boost Gauge",
            displayName = "Ladedruck-Anzeige",
            description = "Turbo-Ladedruck im Display",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "compass",
            module = AstraJCodingModels.Module.IPC,
            channel = "Compass Display",
            displayName = "Kompass",
            description = "Kompass im Display (versteckte Funktion)",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "language",
            module = AstraJCodingModels.Module.IPC,
            channel = "Display Language",
            displayName = "Sprache",
            description = "Display-Sprache ändern",
            values = listOf(
                AstraJCodingModels.CodingValue("DE", "Deutsch"),
                AstraJCodingModels.CodingValue("EN", "Englisch"),
                AstraJCodingModels.CodingValue("FR", "Französisch"),
                AstraJCodingModels.CodingValue("IT", "Italienisch"),
                AstraJCodingModels.CodingValue("ES", "Spanisch")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "units",
            module = AstraJCodingModels.Module.IPC,
            channel = "Units",
            displayName = "Maßeinheiten",
            description = "km/h oder mph anzeigen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "km/h, °C, L/100km"),
                AstraJCodingModels.CodingValue("1", "mph, °F, MPG")
            )
        )
    )

    fun getCategory() = AstraJCodingModels.CodingCategory(
        id = "ipc",
        displayName = "Bordcomputer & Display",
        icon = "Dashboard",
        options = ipcOptions
    )
}

object AstraJInfotainmentCoding {

    val infotainmentOptions = listOf(
        AstraJCodingModels.CodingOption(
            id = "bluetooth_present",
            module = AstraJCodingModels.Module.ECU,
            channel = "Bluetooth Module",
            displayName = "Bluetooth",
            description = "Bluetooth-Freisprecheinrichtung",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "usb_present",
            module = AstraJCodingModels.Module.ECU,
            channel = "USB Module",
            displayName = "USB-Anschluss",
            description = "USB-Eingang aktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "aux_present",
            module = AstraJCodingModels.Module.ECU,
            channel = "AUX Input",
            displayName = "AUX-Eingang",
            description = "AUX-Eingang aktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "video_motion",
            module = AstraJCodingModels.Module.ECU,
            channel = "Video in Motion",
            displayName = "Video während Fahrt",
            description = "Video-Wiedergabe während der Fahrt",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            ),
            hardwareRequired = "Navi900/IntelliLink erforderlich"
        ),
        AstraJCodingModels.CodingOption(
            id = "reverse_camera",
            module = AstraJCodingModels.Module.ECU,
            channel = "Rear View Camera",
            displayName = "Rückfahrkamera",
            description = "Rückfahrkamera aktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "navi_unlock",
            module = AstraJCodingModels.Module.ECU,
            channel = "Navigation Unlock",
            displayName = "Navigation während Fahrt",
            description = "Navi-Eingabe während der Fahrt",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Gesperrt"),
                AstraJCodingModels.CodingValue("1", "Freigegeben")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "rds_ta",
            module = AstraJCodingModels.Module.ECU,
            channel = "RDS TA",
            displayName = "Verkehrsdurchsagen",
            description = "TP/TA Funktion aktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Aus"),
                AstraJCodingModels.CodingValue("1", "Ein")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "onstar_status",
            module = AstraJCodingModels.Module.ECU,
            channel = "OnStar Status",
            displayName = "OnStar Status",
            description = "OnStar-Statusanzeige",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        )
    )

    fun getCategory() = AstraJCodingModels.CodingCategory(
        id = "infotainment",
        displayName = "Infotainment & Navi",
        icon = "Radio",
        options = infotainmentOptions
    )
}

object AstraJFahrdynamikCoding {

    val fahrdynamikOptions = listOf(
        AstraJCodingModels.CodingOption(
            id = "esp_sport",
            module = AstraJCodingModels.Module.UEC,
            channel = "Sport Mode ESP",
            displayName = "Sport-Modus ESP",
            description = "ESP im Sport-Modus deaktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Immer aktiv"),
                AstraJCodingModels.CodingValue("1", "Im Sport-Modus aus")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "tc_off",
            module = AstraJCodingModels.Module.UEC,
            channel = "Traction Control",
            displayName = "Traktionskontrolle",
            description = "TC kann deaktiviert werden",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Immer aktiv"),
                AstraJCodingModels.CodingValue("1", "Deaktivierbar")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "hill_assist",
            module = AstraJCodingModels.Module.UEC,
            channel = "Hill Start Assist",
            displayName = "Berganfahrassistent",
            description = "Berganfahrhilfe aktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "flexride_sport",
            module = AstraJCodingModels.Module.BCM,
            channel = "FlexRide Sport Mode",
            displayName = "FlexRide Sport-Modus",
            description = "Sport-Fahrwerk aktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "FlexRide Fahrwerk erforderlich"
        ),
        AstraJCodingModels.CodingOption(
            id = "steering_weight",
            module = AstraJCodingModels.Module.BCM,
            channel = "Steering Weight",
            displayName = "Lenkungsgewicht",
            description = "Lenkungsunterstützung anpassen",
            values = listOf(
                AstraJCodingModels.CodingValue("1", "Leicht"),
                AstraJCodingModels.CodingValue("2", "Normal"),
                AstraJCodingModels.CodingValue("3", "Sport")
            )
        )
    )

    fun getCategory() = AstraJCodingModels.CodingCategory(
        id = "fahrdynamik",
        displayName = "Fahrdynamik & ESP",
        icon = "Speed",
        options = fahrdynamikOptions
    )
}

object AstraJSecurityCoding {

    val securityOptions = listOf(
        AstraJCodingModels.CodingOption(
            id = "immobilizer_present",
            module = AstraJCodingModels.Module.BCM,
            channel = "Immobilizer",
            displayName = "Wegfahrsperre",
            description = "Wegfahrsperren-Status",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "alarm_present",
            module = AstraJCodingModels.Module.BCM,
            channel = "Alarm System",
            displayName = "Alarmsystem",
            description = "Diebstahlwarnanlage",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "interior_monitor",
            module = AstraJCodingModels.Module.BCM,
            channel = "Interior Monitoring",
            displayName = "Innenraumüberwachung",
            description = "Innenraumsensor aktivieren",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Innenraumsensor muss verbaut sein"
        ),
        AstraJCodingModels.CodingOption(
            id = "tilt_sensor",
            module = AstraJCodingModels.Module.BCM,
            channel = "Tilt Sensor",
            displayName = "Neigungssensor",
            description = "Neigungssensor für Alarm",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Nicht vorhanden"),
                AstraJCodingModels.CodingValue("1", "Vorhanden")
            )
        )
    )

    fun getCategory() = AstraJCodingModels.CodingCategory(
        id = "security",
        displayName = "Sicherheit & Alarm",
        icon = "Security",
        options = securityOptions
    )
}

object AstraJVersteckteFeaturesCoding {

    private val hiddenOptions = listOf(
        AstraJCodingModels.CodingOption(
            id = "needle_sweep",
            module = AstraJCodingModels.Module.IPC,
            channel = "Gauge Sweep",
            displayName = "Nadelsweep (Baron Mode)",
            description = "Nadeln fahren beim Einschalten über den ganzen Bereich und wieder zurück",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "esp_sport_mode",
            module = AstraJCodingModels.Module.CIM,
            channel = "ESP Sport Mode",
            displayName = "ESP Sport-Modus",
            description = "ESP im Sport-Modus mit reduzierter Eingriffsschwelle",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Standard"),
                AstraJCodingModels.CodingValue("1", "Sport (reduziert)"),
                AstraJCodingModels.CodingValue("2", "ESC Off (nur für Profis)")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "speed_warning",
            module = AstraJCodingModels.Module.IPC,
            channel = "Speed Warning",
            displayName = "Geschwindigkeitswarnung",
            description = "Akustische Warnung bei Überschreitung einer Geschwindigkeit",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "120 km/h"),
                AstraJCodingModels.CodingValue("2", "140 km/h"),
                AstraJCodingModels.CodingValue("3", "160 km/h"),
                AstraJCodingModels.CodingValue("4", "180 km/h"),
                AstraJCodingModels.CodingValue("5", "200 km/h"),
                AstraJCodingModels.CodingValue("6", "220 km/h"),
                AstraJCodingModels.CodingValue("7", "250 km/h")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "ambient_color",
            module = AstraJCodingModels.Module.BCM,
            channel = "Ambient Lighting Color",
            displayName = "Ambientebeleuchtung Farbe",
            description = "Farbe der Innenbeleuchtung (Ambiente)",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Blau (Standard)"),
                AstraJCodingModels.CodingValue("1", "Weiß"),
                AstraJCodingModels.CodingValue("2", "Rot"),
                AstraJCodingModels.CodingValue("3", "Grün"),
                AstraJCodingModels.CodingValue("4", "Orange"),
                AstraJCodingModels.CodingValue("5", "Lila")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "rain_sensor_sensitivity",
            module = AstraJCodingModels.Module.BCM,
            channel = "Rain Sensor Sensitivity",
            displayName = "Regensensor Empfindlichkeit",
            description = "Empfindlichkeit des Regensensors für automatische Scheibenwischer",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Niedrig"),
                AstraJCodingModels.CodingValue("1", "Mittel"),
                AstraJCodingModels.CodingValue("2", "Hoch"),
                AstraJCodingModels.CodingValue("3", "Sehr hoch")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "rear_wiper_speed",
            module = AstraJCodingModels.Module.BCM,
            channel = "Rear Wiper Speed",
            displayName = "Heckwischer Geschwindigkeit",
            description = "Intervall/Tempo des Heckwischers anpassen",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Langsam"),
                AstraJCodingModels.CodingValue("1", "Mittel"),
                AstraJCodingModels.CodingValue("2", "Schnell"),
                AstraJCodingModels.CodingValue("3", "Intervall langsam"),
                AstraJCodingModels.CodingValue("4", "Intervall schnell")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "mirror_fold_lock",
            module = AstraJCodingModels.Module.BCM,
            channel = "Mirror Fold on Lock",
            displayName = "Spiegel einklappen bei Verriegelung",
            description = "Seitenspiegel automatisch einklappen bei Verriegelung",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Aktiviert")
            )
        ),
        AstraJCodingModels.CodingOption(
            id = "drl_mode",
            module = AstraJCodingModels.Module.UEC,
            channel = "DRL Mode",
            displayName = "Tagfahrlicht-Modus",
            description = "Modus des Tagfahrlichts (DRL)",
            values = listOf(
                AstraJCodingModels.CodingValue("0", "Deaktiviert"),
                AstraJCodingModels.CodingValue("1", "Scheinwerfer (niedrige Leistung)"),
                AstraJCodingModels.CodingValue("2", "Separate LED-Leiste"),
                AstraJCodingModels.CodingValue("3", "Nebelscheinwerfer")
            )
        )
    )

    fun getCategory() = AstraJCodingModels.CodingCategory(
        id = "hidden_features",
        displayName = "Versteckte Features",
        icon = "Build",
        options = hiddenOptions
    )
}

object AstraJCodingRepository {

    fun getAllCategories(): List<AstraJCodingModels.CodingCategory> = listOf(
        AstraJBleuchtungCoding.getCategory(),
        AstraJKomfortCoding.getCategory(),
        AstraJMotorCoding.getCategory(),
        AstraJIPCCoding.getCategory(),
        AstraJInfotainmentCoding.getCategory(),
        AstraJFahrdynamikCoding.getCategory(),
        AstraJSecurityCoding.getCategory(),
    AstraJVersteckteFeaturesCoding.getCategory()
    )

    fun getCategoryById(id: String): AstraJCodingModels.CodingCategory? =
        getAllCategories().find { it.id == id }

    fun getOptionById(id: String): AstraJCodingModels.CodingOption? =
        getAllCategories().flatMap { it.options }.find { it.id == id }

    fun getProfiles(): List<AstraJCodingModels.CodingProfile> = listOf(
        AstraJCodingModels.CodingProfile(
            id = "stock",
            name = "Werkseinstellung",
            description = "Alle Werte auf Werkseinstellung zurücksetzen",
            options = emptyMap()
        ),
        AstraJCodingModels.CodingProfile(
            id = "comfort",
            name = "Komfort",
            description = "Maximaler Komfort mit allen Helfern",
            options = mapOf(
                "auto_lock_12" to "1",
                "single_unlock" to "1",
                "comfort_close" to "1",
                "comfort_open" to "1",
                "mirror_fold" to "1",
                "mirror_unfold" to "1",
                "acoustic_lock" to "3",
                "coming_home" to "3"
            )
        ),
        AstraJCodingModels.CodingProfile(
            id = "sport",
            name = "Sport",
            description = "Sportliche Einstellung mit ESC-Off",
            options = mapOf(
                "esp_sport" to "1",
                "eco_mode" to "2",
                "single_unlock" to "0",
                "boost_gauge" to "1"
            )
        ),
        AstraJCodingModels.CodingProfile(
            id = "eco",
            name = "Eco",
            description = "Sparsame Einstellung mit Start-Stopp",
            options = mapOf(
                "start_stop" to "1",
                "eco_mode" to "1",
                "eco_index" to "1"
            )
        )
    )
}
