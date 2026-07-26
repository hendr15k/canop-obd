package com.canopobd.data.model

object AstraJCodingModels {

    enum class Module(val displayName: String, val address: String) {
        UEC("UEC - Underhood Electrical Center", "0x09"),
        REC("REC - Rear Electrical Center", "0x2E"),
        BCM("BCM - Body Control Module", "0xFF"),
        IPC("IPC - Instrument Panel Cluster", "0x83"),
        CIM("CIM - Column Integration Module", "0x7E"),
        ECU("ECU - Engine Control Unit", "0x01"),
        TCM("TCM - Transmission Control Module", "0x18"),
        HCM("HCM - Heat & Climate Module", "0x24"),
        PAM("PAM - Parking Assist Module", "0x30"),
        ABS("ABS - Anti-lock Brake System", "0x4B"),
        TRC("TRC - Trailer Control Module", "0x52"),
        AFL("AFL - Adaptive Forward Lighting", "0x40"),
        EPB("EPB - Electronic Parking Brake", "0x3B"),
        TPM("TPM - Tire Pressure Monitor", "0x55"),
        DSP("DSP - Digital Sound Processor", "0x62"),
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
        val hardwareRequired: String? = null,
        val subcategory: String = "",
        val riskLevel: Int = 1,
        val tags: List<String> = emptyList(),
        val defaultValue: String? = null
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

    data class CodingHistoryEntry(
        val optionId: String,
        val oldValue: CodingValue,
        val newValue: CodingValue,
        val timestamp: Long,
        val success: Boolean
    )
}

object AstraJBleuchtungCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("drl_disable", AstraJCodingModels.Module.UEC, "Daytime Running Light", "Tagfahrlicht", "Tagfahrlicht aktivieren oder deaktivieren", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Variante 1 - EU Standard"), AstraJCodingModels.CodingValue("2", "Variante 2 - Dimmung"), AstraJCodingModels.CodingValue("3", "Variante 3 - Skandinavien"), AstraJCodingModels.CodingValue("4", "Variante 4 - LED"), AstraJCodingModels.CodingValue("5", "Variante 5 - Voll")), subcategory = "Tagfahrlicht", riskLevel = 1, tags = listOf("tfl", "licht", "beleuchtung")),
        AstraJCodingModels.CodingOption("drl_with_parking", AstraJCodingModels.Module.UEC, "DRL with Parking Light", "DRL mit Standlicht", "Tagfahrlicht zusammen mit Standlicht", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Tagfahrlicht", riskLevel = 1, tags = listOf("tfl", "standlicht")),
        AstraJCodingModels.CodingOption("coming_home", AstraJCodingModels.Module.UEC, "Coming Home", "Coming Home", "Licht nach dem Abschliessen", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "10 Sekunden"), AstraJCodingModels.CodingValue("2", "20 Sekunden"), AstraJCodingModels.CodingValue("3", "30 Sekunden"), AstraJCodingModels.CodingValue("4", "60 Sekunden"), AstraJCodingModels.CodingValue("5", "90 Sekunden")), subcategory = "Coming/Leaving", riskLevel = 1, tags = listOf("coming", "home", "komfort")),
        AstraJCodingModels.CodingOption("leaving_home", AstraJCodingModels.Module.UEC, "Leaving Home", "Leaving Home", "Licht beim Entriegeln", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Coming/Leaving", riskLevel = 1, tags = listOf("leaving", "home", "komfort")),
        AstraJCodingModels.CodingOption("emergency_brake", AstraJCodingModels.Module.REC, "Emergency Brake Light", "Adaptives Bremslicht", "Bremslicht blinkt bei Notbremsung", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Bremslicht", riskLevel = 1, tags = listOf("bremslicht", "sicherheit", "notbremsung")),
        AstraJCodingModels.CodingOption("fog_lights", AstraJCodingModels.Module.UEC, "Fog Lamps Front", "Nebelscheinwerfer", "Nebelscheinwerfer aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Nebelscheinwerfer", riskLevel = 1, tags = listOf("nebel", "beleuchtung")),
        AstraJCodingModels.CodingOption("fog_as_drl", AstraJCodingModels.Module.UEC, "Fog as DRL", "Nebelscheinwerfer als TFL", "Nebelscheinwerfer als Tagfahrlicht nutzen", listOf(AstraJCodingModels.CodingValue("0", "Nein"), AstraJCodingModels.CodingValue("1", "Ja")), hardwareRequired = "Nebelscheinwerfer muss verbaut sein", subcategory = "Tagfahrlicht", riskLevel = 2, tags = listOf("tfl", "nebel")),
        AstraJCodingModels.CodingOption("check_control", AstraJCodingModels.Module.UEC, "Check Control", "Check Control Lampen", "Fehlerueberwachung fuer Lampen", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Check Control", riskLevel = 1, tags = listOf("check", "lampe", "fehler")),
        AstraJCodingModels.CodingOption("ambient_color_beleuchtung", AstraJCodingModels.Module.REC, "Ambient Light Color", "Ambientebeleuchtung Farbe", "Farbe der Ambientebeleuchtung", listOf(AstraJCodingModels.CodingValue("1", "Rot"), AstraJCodingModels.CodingValue("2", "Blau"), AstraJCodingModels.CodingValue("3", "Gruen"), AstraJCodingModels.CodingValue("4", "Lila"), AstraJCodingModels.CodingValue("5", "Cyan"), AstraJCodingModels.CodingValue("6", "Gelb")), hardwareRequired = "Ambientebeleuchtung muss verbaut sein", subcategory = "Ambiente", riskLevel = 1, tags = listOf("ambiente", "innenraum", "led")),
        AstraJCodingModels.CodingOption("interior_light_timeout", AstraJCodingModels.Module.BCM, "Interior Light Timeout", "Innenbeleuchtung Timeout", "Zeit bis Innenbeleuchtung erlischt", listOf(AstraJCodingModels.CodingValue("0", "Sofort aus"), AstraJCodingModels.CodingValue("1", "10 Sekunden"), AstraJCodingModels.CodingValue("2", "20 Sekunden"), AstraJCodingModels.CodingValue("3", "30 Sekunden"), AstraJCodingModels.CodingValue("4", "60 Sekunden"), AstraJCodingModels.CodingValue("5", "90 Sekunden"), AstraJCodingModels.CodingValue("6", "120 Sekunden"), AstraJCodingModels.CodingValue("7", "150 Sekunden")), subcategory = "Innenbeleuchtung", riskLevel = 1, tags = listOf("innenlicht", "innenraum", "timeout")),
        AstraJCodingModels.CodingOption("drl_mode", AstraJCodingModels.Module.UEC, "DRL Mode", "Tagfahrlicht-Modus", "Modus des Tagfahrlichts (DRL)", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Scheinwerfer"), AstraJCodingModels.CodingValue("2", "Separate LED-Leiste"), AstraJCodingModels.CodingValue("3", "Nebelscheinwerfer")), subcategory = "Tagfahrlicht", riskLevel = 2, tags = listOf("tfl", "modus", "beleuchtung")),
        AstraJCodingModels.CodingOption("parking_light_auto", AstraJCodingModels.Module.BCM, "Parking Light Auto", "Automatisches Parklicht", "Parklicht bei Daemmerung automatisch", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Lichtautomatik", riskLevel = 1, tags = listOf("parklicht", "automatik", "dimmung")),
        AstraJCodingModels.CodingOption("cornering_light_bcm", AstraJCodingModels.Module.BCM, "Cornering Light BCM", "Abbiegelicht (statisch)", "Statisches Abbiegelicht bei langsamer Fahrt", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Abbiegelicht", riskLevel = 1, tags = listOf("abbiegelicht", "beleuchtung")),
        AstraJCodingModels.CodingOption("rear_fog_light", AstraJCodingModels.Module.UEC, "Rear Fog Light", "Nebelschlussleuchte", "Nebelschlussleuchte aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Nebelschlussleuchte", riskLevel = 1, tags = listOf("nebel", "schlussleuchte", "beleuchtung")),
        AstraJCodingModels.CodingOption("follow_me_home", AstraJCodingModels.Module.UEC, "Follow Me Home", "Follow Me Home Dauer", "Lichtdauer nach Ausschalten", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "30 Sekunden"), AstraJCodingModels.CodingValue("2", "60 Sekunden"), AstraJCodingModels.CodingValue("3", "90 Sekunden")), subcategory = "Coming/Leaving", riskLevel = 1, tags = listOf("follow", "home", "licht"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("beleuchtung", "Beleuchtung", "Lightbulb", opts)
}

object AstraJKomfortCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("auto_lock_12", AstraJCodingModels.Module.UEC, "Speed Dependent Locking", "Auto-Verriegelung 12 km/h", "Tuern verriegeln automatisch bei 12 km/h", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Zentralverriegelung", riskLevel = 1, tags = listOf("zv", "autolock", "sicherheit")),
        AstraJCodingModels.CodingOption("single_unlock", AstraJCodingModels.Module.BCM, "Selective Door Unlock", "Einzelentriegelung", "1x druecken = Fahrertuer, 2x = alle Tuern", listOf(AstraJCodingModels.CodingValue("0", "Alle Tuern auf einmal"), AstraJCodingModels.CodingValue("1", "Einzelentriegelung")), subcategory = "Zentralverriegelung", riskLevel = 1, tags = listOf("zv", "einzel", "entriegelung")),
        AstraJCodingModels.CodingOption("comfort_close", AstraJCodingModels.Module.REC, "Windows Comfort Closing", "Komfortschliessen", "Fenster mit Fernbedienung schliessen", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Fensterheber", riskLevel = 1, tags = listOf("fenster", "komfort", "fernbedienung")),
        AstraJCodingModels.CodingOption("comfort_open", AstraJCodingModels.Module.REC, "Windows Comfort Opening", "Komfortoeffnen", "Fenster mit Fernbedienung oeffnen", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Fensterheber", riskLevel = 1, tags = listOf("fenster", "komfort", "fernbedienung")),
        AstraJCodingModels.CodingOption("crash_unlock", AstraJCodingModels.Module.UEC, "Crash Unlock Relay", "Crash-Entriegelung", "Tuern oeffnen bei Airbag-Ausloesung", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Zentralverriegelung", riskLevel = 1, tags = listOf("crash", "airbag", "sicherheit", "notfall")),
        AstraJCodingModels.CodingOption("mirror_fold", AstraJCodingModels.Module.BCM, "Power Folding Mirrors", "Spiegelanklappung", "Spiegel klappen bei Verriegelung ein", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), hardwareRequired = "Elektrisch anklappbare Spiegel", subcategory = "Spiegel", riskLevel = 1, tags = listOf("spiegel", "anklappung", "komfort")),
        AstraJCodingModels.CodingOption("mirror_unfold", AstraJCodingModels.Module.BCM, "Power Unfolding Mirrors", "Spiegelausklappung", "Spiegel klappen bei Entriegelung aus", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), hardwareRequired = "Elektrisch anklappbare Spiegel", subcategory = "Spiegel", riskLevel = 1, tags = listOf("spiegel", "ausklappung", "komfort")),
        AstraJCodingModels.CodingOption("rear_wiper_reverse", AstraJCodingModels.Module.REC, "Rear Wiper Reverse", "Heckwischer Rueckwaertsgang", "Heckwischer aktiv bei Rueckwaertsgang", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), hardwareRequired = "Heckscheibenwischer muss verbaut sein", subcategory = "Scheibenwischer", riskLevel = 1, tags = listOf("wischer", "heck", "rueckwaerts")),
        AstraJCodingModels.CodingOption("acoustic_lock", AstraJCodingModels.Module.CIM, "Acoustic Lock Confirmation", "Akustische Quittung", "Piepen bei Ver-/Entriegelung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Bei Verriegelung"), AstraJCodingModels.CodingValue("2", "Bei Entriegelung"), AstraJCodingModels.CodingValue("3", "Beides")), subcategory = "Akustik", riskLevel = 1, tags = listOf("akustik", "piep", "quittung", "zv")),
        AstraJCodingModels.CodingOption("auto_relock", AstraJCodingModels.Module.BCM, "Auto Relock", "Auto-Wiederverriegelung", "Auto verriegelt nach 3 Min wenn nicht geoeffnet", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Zentralverriegelung", riskLevel = 1, tags = listOf("zv", "autorelock", "sicherheit")),
        AstraJCodingModels.CodingOption("window_comfort", AstraJCodingModels.Module.BCM, "Window Comfort Close", "Fensterheber Komfort", "Komfortbedienung Fensterheber", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Fensterheber", riskLevel = 1, tags = listOf("fenster", "komfort", "heber")),
        AstraJCodingModels.CodingOption("auto_lock_temp", AstraJCodingModels.Module.BCM, "Auto Lock Temperature", "Auto-Verriegelung bei Temperatur", "Auto verriegelt bei niedriger Temperatur", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Unter 5 C"), AstraJCodingModels.CodingValue("2", "Unter 10 C")), subcategory = "Zentralverriegelung", riskLevel = 1, tags = listOf("zv", "temperatur", "kaelte")),
        AstraJCodingModels.CodingOption("mirror_auto_dim", AstraJCodingModels.Module.BCM, "Mirror Auto Dimming", "Spiegel Abdunkelung", "Automatische Spiegelabdunkelung", listOf(AstraJCodingModels.CodingValue("0", "Manuell"), AstraJCodingModels.CodingValue("1", "Automatisch")), subcategory = "Spiegel", riskLevel = 1, tags = listOf("spiegel", "dimmung", "innenraum")),
        AstraJCodingModels.CodingOption("rain_sensor", AstraJCodingModels.Module.BCM, "Rain Sensor", "Regensensor", "Automatischer Regensensor", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Scheibenwischer", riskLevel = 1, tags = listOf("regen", "sensor", "wischer", "automatik")),
        AstraJCodingModels.CodingOption("rain_sensitivity", AstraJCodingModels.Module.BCM, "Rain Sensor Sensitivity", "Regensensor Empfindlichkeit", "Empfindlichkeit des Regensensors", listOf(AstraJCodingModels.CodingValue("0", "Niedrig"), AstraJCodingModels.CodingValue("1", "Mittel"), AstraJCodingModels.CodingValue("2", "Hoch"), AstraJCodingModels.CodingValue("3", "Sehr hoch")), subcategory = "Scheibenwischer", riskLevel = 1, tags = listOf("regen", "sensor", "wischer", "empfindlichkeit")),
        AstraJCodingModels.CodingOption("wiper_interval", AstraJCodingModels.Module.BCM, "Wiper Interval", "Scheibenwischer Intervall", "Intervall der Scheibenwischer", listOf(AstraJCodingModels.CodingValue("0", "Kurz"), AstraJCodingModels.CodingValue("1", "Mittel"), AstraJCodingModels.CodingValue("2", "Lang"), AstraJCodingModels.CodingValue("3", "Automatik")), subcategory = "Scheibenwischer", riskLevel = 1, tags = listOf("wischer", "intervall", "scheibenwischer")),
        AstraJCodingModels.CodingOption("wiper_speed", AstraJCodingModels.Module.BCM, "Wiper Speed", "Scheibenwischer Geschwindigkeit", "Geschwindigkeit der Scheibenwischer", listOf(AstraJCodingModels.CodingValue("0", "Langsam"), AstraJCodingModels.CodingValue("1", "Mittel"), AstraJCodingModels.CodingValue("2", "Schnell")), subcategory = "Scheibenwischer", riskLevel = 1, tags = listOf("wischer", "geschwindigkeit")),
        AstraJCodingModels.CodingOption("rear_wiper_interval", AstraJCodingModels.Module.BCM, "Rear Wiper Interval", "Heckwischer Intervall", "Intervall des Heckwischers", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Veraengert")), subcategory = "Scheibenwischer", riskLevel = 1, tags = listOf("wischer", "heck", "intervall")),
        AstraJCodingModels.CodingOption("rear_wiper_speed", AstraJCodingModels.Module.BCM, "Rear Wiper Speed", "Heckwischer Geschwindigkeit", "Tempo des Heckwischers", listOf(AstraJCodingModels.CodingValue("0", "Langsam"), AstraJCodingModels.CodingValue("1", "Mittel"), AstraJCodingModels.CodingValue("2", "Schnell"), AstraJCodingModels.CodingValue("3", "Intervall langsam"), AstraJCodingModels.CodingValue("4", "Intervall schnell")), subcategory = "Scheibenwischer", riskLevel = 1, tags = listOf("wischer", "heck", "geschwindigkeit")),
        AstraJCodingModels.CodingOption("window_open_distance", AstraJCodingModels.Module.BCM, "Window Open Distance", "Fenster Oeffnen Distanz", "Distanz fuer schluesselloses Oeffnen", listOf(AstraJCodingModels.CodingValue("0", "Kurz"), AstraJCodingModels.CodingValue("1", "Mittel"), AstraJCodingModels.CodingValue("2", "Voll")), subcategory = "Fensterheber", riskLevel = 1, tags = listOf("fenster", "oeffnen", "komfort"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("komfort", "Komfort & ZV", "DirectionsCar", opts)
}

object AstraJMotorCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("start_stop", AstraJCodingModels.Module.BCM, "Start-Stop System", "Start-Stopp Automatik", "Start-Stopp System aktivieren/deaktivieren", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Start-Stopp", riskLevel = 2, tags = listOf("start", "stop", "spritsparen", "motor")),
        AstraJCodingModels.CodingOption("eco_mode", AstraJCodingModels.Module.ECU, "Eco Mode", "Eco-Modus", "ECO-Fahrmodus Parameter", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Eco Aktiviert"), AstraJCodingModels.CodingValue("2", "Sport Aktiviert")), subcategory = "Fahrmodus", riskLevel = 2, tags = listOf("eco", "fahrmodus", "motor")),
        AstraJCodingModels.CodingOption("cruise_control", AstraJCodingModels.Module.CIM, "Cruise Control", "Tempomat", "Tempomat aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Tempomat", riskLevel = 1, tags = listOf("tempomat", "cruise", "assistenz")),
        AstraJCodingModels.CodingOption("overspeed_warning_motor", AstraJCodingModels.Module.IPC, "Overspeed Warning Motor", "Geschwindigkeitswarnung", "Warnung bei Ueberschreitung", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Warnungen", riskLevel = 1, tags = listOf("geschwindigkeit", "warnung", "sicherheit")),
        AstraJCodingModels.CodingOption("throttle_response", AstraJCodingModels.Module.ECU, "Throttle Response", "Drosselklappen Ansprechverhalten", "Gaspedal Ansprechverhalten anpassen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Sport"), AstraJCodingModels.CodingValue("2", "Eco")), subcategory = "Fahrmodus", riskLevel = 2, tags = listOf("gas", "pedal", "ansprechen", "sport", "tuning")),
        AstraJCodingModels.CodingOption("idle_rpm", AstraJCodingModels.Module.ECU, "Idle RPM", "Leerlauf Drehzahl", "Leerlaufdrehzahl anpassen", listOf(AstraJCodingModels.CodingValue("0", "750 U/min"), AstraJCodingModels.CodingValue("1", "800 U/min"), AstraJCodingModels.CodingValue("2", "850 U/min"), AstraJCodingModels.CodingValue("3", "900 U/min")), subcategory = "Motor", riskLevel = 2, tags = listOf("leerlauf", "drehzahl", "motor", "tuning")),
        AstraJCodingModels.CodingOption("rpm_limiter_motor", AstraJCodingModels.Module.ECU, "RPM Limiter Engine", "Drehzahlbegrenzung Motor", "Motordrehzahlbegrenzung anpassen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "+200 U/min"), AstraJCodingModels.CodingValue("2", "+400 U/min")), subcategory = "Motor", riskLevel = 3, tags = listOf("drehzahl", "begrenzung", "motor", "tuning", "leistung")),
        AstraJCodingModels.CodingOption("fuel_injection_mode", AstraJCodingModels.Module.ECU, "Injection Mode", "Kraftstoff Einspritzung", "Einspritzverhalten des Motors", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Pilot"), AstraJCodingModels.CodingValue("2", "Main"), AstraJCodingModels.CodingValue("3", "Post")), subcategory = "Motor", riskLevel = 3, tags = listOf("einspritzung", "kraftstoff", "motor", "tuning")),
        AstraJCodingModels.CodingOption("max_fuel_pressure", AstraJCodingModels.Module.ECU, "Max Fuel Pressure", "Max Kraftstoffdruck", "Maximaler Kraftstoffdruck", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Erhoeht")), subcategory = "Motor", riskLevel = 3, tags = listOf("kraftstoff", "druck", "motor", "tuning")),
        AstraJCodingModels.CodingOption("engine_sound", AstraJCodingModels.Module.ECU, "Engine Sound Enhancement", "Fahrgeraeusch", "Motorgeraeusch im Innenraum", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein"), AstraJCodingModels.CodingValue("2", "Sport")), subcategory = "Sound", riskLevel = 1, tags = listOf("sound", "motor", "innenraum", "audio")),
        AstraJCodingModels.CodingOption("sound_generator", AstraJCodingModels.Module.ECU, "Active Sound Generator", "Sound Generator", "Aktiver Soundgenerator", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiviert"), AstraJCodingModels.CodingValue("2", "Aggressiv")), subcategory = "Sound", riskLevel = 1, tags = listOf("sound", "generator", "audio", "innenraum"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("motor", "Motor & Antrieb", "Engineering", opts)
}

object AstraJIPCCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("board_computer", AstraJCodingModels.Module.IPC, "Board Computer", "Bordcomputer", "Bordcomputer-Anzeigen freischalten", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Bordcomputer", riskLevel = 1, tags = listOf("bordcomputer", "display", "info")),
        AstraJCodingModels.CodingOption("dic_present", AstraJCodingModels.Module.IPC, "Driver Information Center", "Fahrerinfo-Center", "DIC Menues freischalten", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Fahrerinfo", riskLevel = 1, tags = listOf("dic", "display", "info")),
        AstraJCodingModels.CodingOption("instant_mpg", AstraJCodingModels.Module.IPC, "Instant MPG Display", "Momentanverbrauch", "Momentanverbrauch im Display", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Verbrauch", riskLevel = 1, tags = listOf("verbrauch", "momentan", "mpg", "liter")),
        AstraJCodingModels.CodingOption("avg_mpg", AstraJCodingModels.Module.IPC, "Average MPG Display", "Durchschnittsverbrauch", "Durchschnittsverbrauch anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Verbrauch", riskLevel = 1, tags = listOf("verbrauch", "durchschnitt", "mpg")),
        AstraJCodingModels.CodingOption("range_display", AstraJCodingModels.Module.IPC, "Fuel Range Display", "Reichweite", "Restreichweite anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Reichweite", riskLevel = 1, tags = listOf("reichweite", "tank", "display")),
        AstraJCodingModels.CodingOption("outside_temp", AstraJCodingModels.Module.IPC, "Outside Temperature", "Aussentemperatur", "Aussentemperatur anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Temperatur", riskLevel = 1, tags = listOf("aussentemperatur", "temp", "sensor")),
        AstraJCodingModels.CodingOption("oil_temp", AstraJCodingModels.Module.IPC, "Oil Temperature Display", "Oeltemperatur", "Oeltemperatur im Display (Mode 22)", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Temperatur", riskLevel = 1, tags = listOf("oel", "temperatur", "motor", "display")),
        AstraJCodingModels.CodingOption("eco_index", AstraJCodingModels.Module.IPC, "ECO Index Display", "ECO-Index", "ECO-Fahrindex anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "ECO", riskLevel = 1, tags = listOf("eco", "index", "fahreffizienz")),
        AstraJCodingModels.CodingOption("boost_gauge_ipc", AstraJCodingModels.Module.IPC, "Turbo Boost Gauge IPC", "Ladedruck-Anzeige", "Turbo-Ladedruck im Display", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Turbo", riskLevel = 1, tags = listOf("boost", "ladedruck", "turbo", "display")),
        AstraJCodingModels.CodingOption("compass_ipc", AstraJCodingModels.Module.IPC, "Compass Display IPC", "Kompass", "Kompass im Display", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Kompass", riskLevel = 1, tags = listOf("kompass", "display", "versteckt", "navigation")),
        AstraJCodingModels.CodingOption("language", AstraJCodingModels.Module.IPC, "Display Language", "Sprache", "Display-Sprache aendern", listOf(AstraJCodingModels.CodingValue("DE", "Deutsch"), AstraJCodingModels.CodingValue("EN", "Englisch"), AstraJCodingModels.CodingValue("FR", "Franzoesisch"), AstraJCodingModels.CodingValue("IT", "Italienisch"), AstraJCodingModels.CodingValue("ES", "Spanisch")), subcategory = "Einstellungen", riskLevel = 1, tags = listOf("sprache", "display", "einstellung")),
        AstraJCodingModels.CodingOption("units", AstraJCodingModels.Module.IPC, "Units", "Masseinheiten", "km/h oder mph anzeigen", listOf(AstraJCodingModels.CodingValue("0", "km/h, C, L/100km"), AstraJCodingModels.CodingValue("1", "mph, F, MPG")), subcategory = "Einstellungen", riskLevel = 1, tags = listOf("einheiten", "kmh", "mph", "display")),
        AstraJCodingModels.CodingOption("needle_sweep_ipc", AstraJCodingModels.Module.IPC, "Gauge Sweep IPC", "Nadelsweep (Baron Mode)", "Nadeln fahren beim Einschalten", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Anzeige", riskLevel = 1, tags = listOf("nadel", "sweep", "baron", "versteckt", "display")),
        AstraJCodingModels.CodingOption("speed_warning_ipc", AstraJCodingModels.Module.IPC, "Speed Warning IPC", "Geschwindigkeitswarnung", "Akustische Warnung bei Ueberschreitung", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "120 km/h"), AstraJCodingModels.CodingValue("2", "140 km/h"), AstraJCodingModels.CodingValue("3", "160 km/h"), AstraJCodingModels.CodingValue("4", "180 km/h"), AstraJCodingModels.CodingValue("5", "200 km/h"), AstraJCodingModels.CodingValue("6", "220 km/h"), AstraJCodingModels.CodingValue("7", "250 km/h")), subcategory = "Warnungen", riskLevel = 1, tags = listOf("geschwindigkeit", "warnung", "sicherheit", "kmh")),
        AstraJCodingModels.CodingOption("speed_sign_recognition", AstraJCodingModels.Module.IPC, "Speed Sign Recognition", "Verkehrsschilderkennung", "Verkehrsschilder im Display anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Assistenz", riskLevel = 1, tags = listOf("schild", "erkennung", "sicherheit", "versteckt")),
        AstraJCodingModels.CodingOption("gong_volume", AstraJCodingModels.Module.IPC, "Gong Volume Level", "Gongs Lautstaerke", "Lautstaerke der Warngongs", listOf(AstraJCodingModels.CodingValue("1", "Stufe 1"), AstraJCodingModels.CodingValue("2", "Stufe 2"), AstraJCodingModels.CodingValue("3", "Stufe 3"), AstraJCodingModels.CodingValue("4", "Stufe 4"), AstraJCodingModels.CodingValue("5", "Stufe 5")), subcategory = "Akustik", riskLevel = 1, tags = listOf("gong", "lautstaerke", "warnung", "display"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("ipc", "Bordcomputer & Display", "Dashboard", opts)
}

object AstraJInfotainmentCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("bluetooth_present", AstraJCodingModels.Module.ECU, "Bluetooth Module", "Bluetooth", "Bluetooth-Freisprecheinrichtung", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Bluetooth", riskLevel = 1, tags = listOf("bluetooth", "freisprechen", "audio")),
        AstraJCodingModels.CodingOption("usb_present", AstraJCodingModels.Module.ECU, "USB Module", "USB-Anschluss", "USB-Eingang aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "USB", riskLevel = 1, tags = listOf("usb", "anschluss", "audio", "media")),
        AstraJCodingModels.CodingOption("aux_present", AstraJCodingModels.Module.ECU, "AUX Input", "AUX-Eingang", "AUX-Eingang aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "AUX", riskLevel = 1, tags = listOf("aux", "audio", "media", "kabel")),
        AstraJCodingModels.CodingOption("video_motion", AstraJCodingModels.Module.ECU, "Video in Motion", "Video waehrend Fahrt", "Video-Wiedergabe waehrend der Fahrt", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), hardwareRequired = "Navi900/IntelliLink erforderlich", subcategory = "Video", riskLevel = 2, tags = listOf("video", "dvd", "navigation", "fahrt")),
        AstraJCodingModels.CodingOption("reverse_camera", AstraJCodingModels.Module.ECU, "Rear View Camera", "Rueckfahrkamera", "Rueckfahrkamera aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Kamera", riskLevel = 1, tags = listOf("kamera", "rueckfahrt", "parken")),
        AstraJCodingModels.CodingOption("navi_unlock", AstraJCodingModels.Module.ECU, "Navigation Unlock", "Navigation waehrend Fahrt", "Navi-Eingabe waehrend der Fahrt", listOf(AstraJCodingModels.CodingValue("0", "Gesperrt"), AstraJCodingModels.CodingValue("1", "Freigegeben")), subcategory = "Navigation", riskLevel = 2, tags = listOf("navi", "navigation", "fahrt", "eingabe")),
        AstraJCodingModels.CodingOption("rds_ta", AstraJCodingModels.Module.ECU, "RDS TA", "Verkehrsdurchsagen", "TP/TA Funktion aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Radio", riskLevel = 1, tags = listOf("rds", "traffic", "durchsage", "radio")),
        AstraJCodingModels.CodingOption("onstar_status", AstraJCodingModels.Module.ECU, "OnStar Status", "OnStar Status", "OnStar-Statusanzeige", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "OnStar", riskLevel = 1, tags = listOf("onstar", "internet", "app")),
        AstraJCodingModels.CodingOption("surround_view", AstraJCodingModels.Module.PAM, "Surround View Camera", "360 Grad Kamera", "Rundum-Kamera-System", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), hardwareRequired = "360 Grad Kamera-System erforderlich", subcategory = "Kamera", riskLevel = 1, tags = listOf("kamera", "360", "surround", "parken")),
        AstraJCodingModels.CodingOption("audio_eq", AstraJCodingModels.Module.DSP, "Audio EQ Mode", "Audio Equalizer", "Equalizer-Voreinstellung", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Bass"), AstraJCodingModels.CodingValue("2", "Pop"), AstraJCodingModels.CodingValue("3", "Rock"), AstraJCodingModels.CodingValue("4", "Klassik"), AstraJCodingModels.CodingValue("5", "Jazz")), subcategory = "Audio", riskLevel = 1, tags = listOf("audio", "eq", "equalizer", "sound")),
        AstraJCodingModels.CodingOption("surround_sound", AstraJCodingModels.Module.DSP, "Surround Sound", "Surround Sound", "Surround-Sound aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Audio", riskLevel = 1, tags = listOf("audio", "surround", "sound"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("infotainment", "Infotainment & Navi", "Radio", opts)
}

object AstraJFahrdynamikCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("esp_sport", AstraJCodingModels.Module.UEC, "Sport Mode ESP", "Sport-Modus ESP", "ESP im Sport-Modus deaktivieren", listOf(AstraJCodingModels.CodingValue("0", "Immer aktiv"), AstraJCodingModels.CodingValue("1", "Im Sport-Modus aus")), subcategory = "ESP", riskLevel = 3, tags = listOf("esp", "sport", "fahrdynamik", "stabilitaet")),
        AstraJCodingModels.CodingOption("tc_off", AstraJCodingModels.Module.UEC, "Traction Control", "Traktionskontrolle", "TC kann deaktiviert werden", listOf(AstraJCodingModels.CodingValue("0", "Immer aktiv"), AstraJCodingModels.CodingValue("1", "Deaktivierbar")), subcategory = "Traktion", riskLevel = 2, tags = listOf("tc", "traktion", "fahrdynamik")),
        AstraJCodingModels.CodingOption("hill_assist", AstraJCodingModels.Module.UEC, "Hill Start Assist", "Berganfahrassistent", "Berganfahrhilfe aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Assistenz", riskLevel = 1, tags = listOf("berg", "anfahrt", "assistent", "sicherheit")),
        AstraJCodingModels.CodingOption("flexride_sport", AstraJCodingModels.Module.BCM, "FlexRide Sport Mode", "FlexRide Sport-Modus", "Sport-Fahrwerk aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), hardwareRequired = "FlexRide Fahrwerk erforderlich", subcategory = "Fahrwerk", riskLevel = 2, tags = listOf("flexride", "fahrwerk", "sport", "komfort")),
        AstraJCodingModels.CodingOption("steering_weight", AstraJCodingModels.Module.BCM, "Steering Weight", "Lenkungsgewicht", "Lenkungsunterstuetzung anpassen", listOf(AstraJCodingModels.CodingValue("1", "Leicht"), AstraJCodingModels.CodingValue("2", "Normal"), AstraJCodingModels.CodingValue("3", "Sport")), subcategory = "Lenkung", riskLevel = 2, tags = listOf("lenkung", "gewicht", "sport", "komfort")),
        AstraJCodingModels.CodingOption("esp_sport_mode", AstraJCodingModels.Module.CIM, "ESP Sport Mode CIM", "ESP Sport-Modus (erweitert)", "ESP mit reduzierter Eingriffsschwelle", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Sport (reduziert)"), AstraJCodingModels.CodingValue("2", "ESC Off (nur fuer Profis)")), subcategory = "ESP", riskLevel = 3, tags = listOf("esp", "sport", "esc", "off", "fahrdynamik")),
        AstraJCodingModels.CodingOption("abs_sensitivity", AstraJCodingModels.Module.ABS, "ABS Sensitivity", "ABS Empfindlichkeit", "ABS Ansprechempfindlichkeit", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Verstaerkt"), AstraJCodingModels.CodingValue("2", "Dezent")), subcategory = "ABS", riskLevel = 3, tags = listOf("abs", "bremse", "empfindlichkeit", "sicherheit")),
        AstraJCodingModels.CodingOption("brake_assist", AstraJCodingModels.Module.ABS, "Brake Assist", "Bremsassistent", "Bremsassistent Empfindlichkeit", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Aggressiv"), AstraJCodingModels.CodingValue("2", "Dezent")), subcategory = "ABS", riskLevel = 2, tags = listOf("bremse", "assistent", "sicherheit")),
        AstraJCodingModels.CodingOption("brake_prefill", AstraJCodingModels.Module.ABS, "Brake Pre-fill", "Bremsevorspannung", "Bremse vorspannen bei Fuss auf Pedal", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "ABS", riskLevel = 2, tags = listOf("bremse", "vorspannung", "sicherheit")),
        AstraJCodingModels.CodingOption("dry_braking", AstraJCodingModels.Module.ABS, "Dry Braking Auto", "Trockenbremse", "Scheiben trockenbremsen bei Naesse", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "ABS", riskLevel = 1, tags = listOf("bremse", "trocken", "naesse", "sicherheit")),
        AstraJCodingModels.CodingOption("ebv_distribution", AstraJCodingModels.Module.ABS, "EBV Distribution", "EBV Verteilung", "Elektronische Bremskraftverteilung", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Verbessert")), subcategory = "ABS", riskLevel = 2, tags = listOf("bremse", "ebv", "verteilung", "sicherheit")),
        AstraJCodingModels.CodingOption("hill_descent_control", AstraJCodingModels.Module.ABS, "Hill Descent Control", "Bergabfahrassistent", "Bergabfahrkontrolle aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Assistenz", riskLevel = 2, tags = listOf("bergab", "fahrassistent", "gelaende", "sicherheit"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("fahrdynamik", "Fahrdynamik & ESP", "Speed", opts)
}

object AstraJSecurityCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("immobilizer_present", AstraJCodingModels.Module.BCM, "Immobilizer", "Wegfahrsperre", "Wegfahrsperren-Status", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Wegfahrsperre", riskLevel = 2, tags = listOf("wegfahrsperre", "immobilizer", "sicherheit", "diebstahl")),
        AstraJCodingModels.CodingOption("alarm_present", AstraJCodingModels.Module.BCM, "Alarm System", "Alarmsystem", "Diebstahlwarnanlage", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Alarmanlage", riskLevel = 2, tags = listOf("alarm", "alarmanlage", "sicherheit", "diebstahl")),
        AstraJCodingModels.CodingOption("interior_monitor", AstraJCodingModels.Module.BCM, "Interior Monitoring", "Innenraumueberwachung", "Innenraumsensor aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), hardwareRequired = "Innenraumsensor muss verbaut sein", subcategory = "Alarmanlage", riskLevel = 2, tags = listOf("innenraum", "sensor", "alarm", "sicherheit")),
        AstraJCodingModels.CodingOption("tilt_sensor", AstraJCodingModels.Module.BCM, "Tilt Sensor", "Neigungssensor", "Neigungssensor fuer Alarm", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Alarmanlage", riskLevel = 2, tags = listOf("neigung", "sensor", "alarm", "sicherheit", "diebstahl")),
        AstraJCodingModels.CodingOption("emergency_brake_flash_sec", AstraJCodingModels.Module.BCM, "Emergency Brake Flash Sec", "Notbremsblinken", "Bremslichter blinken bei Notbremsung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Bremslicht", riskLevel = 1, tags = listOf("bremslicht", "notbremsung", "blinken", "sicherheit"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("security", "Sicherheit & Alarm", "Security", opts)
}

object AstraJVersteckteFeaturesCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("needle_sweep_hidden", AstraJCodingModels.Module.IPC, "Gauge Sweep Hidden", "Nadelsweep (Baron Mode)", "Nadeln fahren beim Einschalten", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Display", riskLevel = 4, tags = listOf("versteckt", "nadel", "sweep", "baron", "display")),
        AstraJCodingModels.CodingOption("compass_hidden", AstraJCodingModels.Module.IPC, "Compass Display Hidden", "Kompass im Display", "Kompass-Anzeige im Tacho freischalten", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Display", riskLevel = 4, tags = listOf("versteckt", "kompass", "navigation", "display")),
        AstraJCodingModels.CodingOption("esp_sport_hidden", AstraJCodingModels.Module.CIM, "ESP Sport Mode Hidden", "ESP Sport-Modus (versteckt)", "ESP mit reduzierter Eingriffsschwelle", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Sport (reduziert)"), AstraJCodingModels.CodingValue("2", "ESC Off (nur fuer Profis)")), subcategory = "Fahrdynamik", riskLevel = 4, tags = listOf("versteckt", "esp", "sport", "esc", "off", "expert")),
        AstraJCodingModels.CodingOption("ambient_color_hidden", AstraJCodingModels.Module.BCM, "Ambient Lighting Color Hidden", "Ambientebeleuchtung Farbe", "Farbe der Innenbeleuchtung einstellen", listOf(AstraJCodingModels.CodingValue("0", "Blau (Standard)"), AstraJCodingModels.CodingValue("1", "Weiss"), AstraJCodingModels.CodingValue("2", "Rot"), AstraJCodingModels.CodingValue("3", "Gruen"), AstraJCodingModels.CodingValue("4", "Orange"), AstraJCodingModels.CodingValue("5", "Lila")), subcategory = "Ambiente", riskLevel = 4, tags = listOf("versteckt", "ambiente", "farbe", "innenlicht", "led")),
        AstraJCodingModels.CodingOption("auto_park_hidden", AstraJCodingModels.Module.PAM, "Auto Parking Hidden", "Automatisches Einparken", "Vollautomatisches Parken freischalten", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiviert")), hardwareRequired = "Parkassistent + Kamera erforderlich", subcategory = "Parken", riskLevel = 4, tags = listOf("versteckt", "parken", "auto", "pilot")),
        AstraJCodingModels.CodingOption("shift_light_hidden", AstraJCodingModels.Module.TCM, "Shift Light Hidden", "Schaltblitz im Tacho", "LED-Schaltblitz bei optimaler Drehzahl", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein"), AstraJCodingModels.CodingValue("2", "Bei RPM-Schwelle")), subcategory = "Getriebe", riskLevel = 4, tags = listOf("versteckt", "schaltblitz", "drehzahl", "tacho", "led")),
        AstraJCodingModels.CodingOption("launch_control_hidden", AstraJCodingModels.Module.ECU, "Launch Control Hidden", "Launch Control", "Launch Control fuer schnellen Start freischalten", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Motor", riskLevel = 4, tags = listOf("versteckt", "launch", "start", "performance", "expert")),
        AstraJCodingModels.CodingOption("overboost_hidden", AstraJCodingModels.Module.ECU, "Overboost Hidden", "Overboost", "Kurzzeitiger Ladedruck-Ueberboost", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Motor", riskLevel = 4, tags = listOf("versteckt", "overboost", "turbo", "boost", "performance", "expert")),
        AstraJCodingModels.CodingOption("anti_lag_hidden", AstraJCodingModels.Module.ECU, "Anti Lag System Hidden", "Anti-Lag", "Anti-Lag-System fuer Turbo-Fahrzeuge", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Motor", riskLevel = 4, tags = listOf("versteckt", "anti", "lag", "turbo", "expert")),
        AstraJCodingModels.CodingOption("max_boost_hidden", AstraJCodingModels.Module.ECU, "Max Boost Pressure Hidden", "Max Ladedruck erhoehen", "Maximalen Ladedruck ueber Serienwert erhoehen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "+0.1 bar"), AstraJCodingModels.CodingValue("2", "+0.2 bar")), subcategory = "Motor", riskLevel = 4, tags = listOf("versteckt", "turbo", "boost", "ladedruck", "tuning", "expert")),
        AstraJCodingModels.CodingOption("boost_gauge_hidden", AstraJCodingModels.Module.IPC, "Boost Gauge Hidden", "Ladedruck-Anzeige (versteckt)", "Turbo-Ladedruck im Tacho anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein"), AstraJCodingModels.CodingValue("2", "Detailliert")), subcategory = "Display", riskLevel = 4, tags = listOf("versteckt", "boost", "turbo", "ladedruck", "tacho", "display")),
        AstraJCodingModels.CodingOption("video_motion_hidden", AstraJCodingModels.Module.ECU, "Video in Motion Hidden", "Video waehrend Fahrt (versteckt)", "Video-Wiedergabe waehrend der Fahrt erlauben", listOf(AstraJCodingModels.CodingValue("0", "Deaktiviert"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Infotainment", riskLevel = 4, tags = listOf("versteckt", "video", "fahrt", "dvd", "navigation")),
        AstraJCodingModels.CodingOption("dpf_hidden", AstraJCodingModels.Module.ECU, "DPF Active Hidden", "DPF Software deaktivieren", "Dieselpartikelfilter per Software deaktivieren", listOf(AstraJCodingModels.CodingValue("0", "Aktiv"), AstraJCodingModels.CodingValue("1", "Deaktiviert")), subcategory = "Diesel", riskLevel = 4, tags = listOf("versteckt", "dpf", "partikelfilter", "diesel", "tuning", "expert")),
        AstraJCodingModels.CodingOption("egr_hidden", AstraJCodingModels.Module.ECU, "EGR Valve Hidden", "EGR deaktivieren", "Abgasrueckfuehrung per Software deaktivieren", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Reduziert"), AstraJCodingModels.CodingValue("2", "Deaktiviert")), subcategory = "Diesel", riskLevel = 4, tags = listOf("versteckt", "egr", "abgas", "rueckfuehrung", "diesel", "tuning", "expert")),
        AstraJCodingModels.CodingOption("service_menu_hidden", AstraJCodingModels.Module.ECU, "Diagnostic Menu Hidden", "Erweitertes Diagnose-Menue", "Zusaetzliche Diagnose-Optionen freischalten", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Erweitert")), subcategory = "Diagnose", riskLevel = 4, tags = listOf("versteckt", "diagnose", "service", "werkstatt", "menu")),
        AstraJCodingModels.CodingOption("can_trace_hidden", AstraJCodingModels.Module.ECU, "CAN Trace Hidden", "CAN-Bus Trace", "CAN-Bus Datenverkehr anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Diagnose", riskLevel = 4, tags = listOf("versteckt", "can", "bus", "trace", "diagnose", "expert")),
        AstraJCodingModels.CodingOption("extended_obd_hidden", AstraJCodingModels.Module.ECU, "Extended OBD Hidden", "Erweiterter OBD-Modus", "Erweiterte OBD-Daten freischalten", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Diagnose", riskLevel = 4, tags = listOf("versteckt", "obd", "extended", "diagnose", "daten"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("hidden_features", "Versteckte Features", "Build", opts)
}

object AstraJSitzkomfortCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("seat_heating_front", AstraJCodingModels.Module.HCM, "Seat Heating Level Front", "Sitzheizung vorne", "Sitzheizung Stufe vorne", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Stufe 1"), AstraJCodingModels.CodingValue("2", "Stufe 2"), AstraJCodingModels.CodingValue("3", "Stufe 3"), AstraJCodingModels.CodingValue("4", "Stufe 4"), AstraJCodingModels.CodingValue("5", "Stufe 5")), subcategory = "Sitzheizung", riskLevel = 1, tags = listOf("sitz", "heizung", "komfort", "sitzheizung")),
        AstraJCodingModels.CodingOption("seat_heating_rear", AstraJCodingModels.Module.HCM, "Seat Heating Level Rear", "Sitzheizung Heck", "Sitzheizung Stufe hinten", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Stufe 1"), AstraJCodingModels.CodingValue("2", "Stufe 2"), AstraJCodingModels.CodingValue("3", "Stufe 3")), subcategory = "Sitzheizung", riskLevel = 1, tags = listOf("sitz", "heizung", "heck", "komfort")),
        AstraJCodingModels.CodingOption("seat_ventilation", AstraJCodingModels.Module.HCM, "Seat Ventilation Front", "Sitzbeleftung", "Sitzbeleftung vorne", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Stufe 1"), AstraJCodingModels.CodingValue("2", "Stufe 2"), AstraJCodingModels.CodingValue("3", "Stufe 3")), hardwareRequired = "Beleftete Sitze erforderlich", subcategory = "Sitzbeleftung", riskLevel = 1, tags = listOf("sitz", "lueftung", "belueftung", "komfort")),
        AstraJCodingModels.CodingOption("seat_memory_driver", AstraJCodingModels.Module.BCM, "Seat Memory Driver", "Sitzposition Speicher Fahrer", "Fahrersitz-Speicherplaetze", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "1 Speicher"), AstraJCodingModels.CodingValue("2", "2 Speicher"), AstraJCodingModels.CodingValue("3", "3 Speicher")), subcategory = "Sitzmemory", riskLevel = 1, tags = listOf("sitz", "speicher", "memory", "komfort")),
        AstraJCodingModels.CodingOption("steering_heating", AstraJCodingModels.Module.HCM, "Steering Wheel Heating", "Sitzheizung Lenkrad", "Lenkradheizung", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Lenkrad", riskLevel = 1, tags = listOf("lenkrad", "heizung", "komfort")),
        AstraJCodingModels.CodingOption("massage_seat", AstraJCodingModels.Module.HCM, "Massage Seat Driver", "Massagesitz Fahrer", "Massagefunktion fuer Fahrersitz", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), hardwareRequired = "Massagesitze erforderlich", subcategory = "Massage", riskLevel = 1, tags = listOf("sitz", "massage", "komfort", "entspannung")),
        AstraJCodingModels.CodingOption("seat_heating_auto", AstraJCodingModels.Module.HCM, "Seat Heating Auto", "Sitzheizung Automatik", "Automatische Sitzheizung bei Kaelte", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Bei Kaltstart"), AstraJCodingModels.CodingValue("2", "Bei Bedarf")), subcategory = "Sitzheizung", riskLevel = 1, tags = listOf("sitz", "heizung", "automatik", "komfort"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("sitzkomfort", "Sitzkomfort", "AirlineSeatReclineNormal", opts)
}

object AstraJKlimaCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("climate_auto", AstraJCodingModels.Module.HCM, "Climate Auto Mode", "Klima Automatik", "Automatische Klimasteuerung", listOf(AstraJCodingModels.CodingValue("0", "Manuell"), AstraJCodingModels.CodingValue("1", "Auto"), AstraJCodingModels.CodingValue("2", "Semi-Auto")), subcategory = "Klimaautomatik", riskLevel = 1, tags = listOf("klima", "automatik", "heizung", "komfort")),
        AstraJCodingModels.CodingOption("climate_zones", AstraJCodingModels.Module.HCM, "Climate Zones", "Klimazonen", "Anzahl der Klimazonen", listOf(AstraJCodingModels.CodingValue("1", "1-Zone"), AstraJCodingModels.CodingValue("2", "2-Zonen"), AstraJCodingModels.CodingValue("3", "3-Zonen")), subcategory = "Klimazonen", riskLevel = 1, tags = listOf("klima", "zone", "heizung", "komfort")),
        AstraJCodingModels.CodingOption("rear_window_heating", AstraJCodingModels.Module.HCM, "Rear Window Heating", "Heckscheibenheizung", "Automatische Heckscheibenheizung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein"), AstraJCodingModels.CodingValue("2", "Automatisch")), subcategory = "Scheibenheizung", riskLevel = 1, tags = listOf("heizung", "heck", "scheibe", "frost")),
        AstraJCodingModels.CodingOption("activated_carbon", AstraJCodingModels.Module.HCM, "Activated Carbon Filter", "Luftfilter Aktivkohle", "Aktivkohle-Luftfilter aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Luftfilter", riskLevel = 1, tags = listOf("filter", "luft", "aktivkohle", "klima")),
        AstraJCodingModels.CodingOption("frost_protection", AstraJCodingModels.Module.HCM, "Frost Protection", "Frostschutz", "Automatischer Frostschutz", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein"), AstraJCodingModels.CodingValue("2", "Intensiv")), subcategory = "Frostschutz", riskLevel = 1, tags = listOf("frost", "schutz", "winter", "heizung")),
        AstraJCodingModels.CodingOption("windshield_heating", AstraJCodingModels.Module.HCM, "Windshield Heating", "Scheibenheizung frontal", "Frontscheibenheizung aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Scheibenheizung", riskLevel = 1, tags = listOf("scheibe", "heizung", "front", "frost")),
        AstraJCodingModels.CodingOption("remote_climate", AstraJCodingModels.Module.HCM, "Remote Climate Start", "Fernbedienung Klima", "Klimatisierung per Fernbedienung starten", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Fernsteuerung", riskLevel = 2, tags = listOf("fernbedienung", "klima", "start", "komfort")),
        AstraJCodingModels.CodingOption("humidity_control", AstraJCodingModels.Module.HCM, "Humidity Control", "Luftfeuchtigkeit", "Luftfeuchtigkeitsregelung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Klima", riskLevel = 1, tags = listOf("feuchtigkeit", "klima", "innenraum")),
        AstraJCodingModels.CodingOption("ionizer", AstraJCodingModels.Module.HCM, "Ionizer", "Ionisator", "Ionisator fuer Innenraumluft", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Luftqualitaet", riskLevel = 1, tags = listOf("ionisator", "luft", "qualitaet", "innenraum"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("klima", "Klima & Heizung", "Thermostat", opts)
}

object AstraJGetriebeCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("shift_mode", AstraJCodingModels.Module.TCM, "Shift Mode", "Schaltmodus", "Schaltmodus des Getriebes", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Sport"), AstraJCodingModels.CodingValue("2", "Eco"), AstraJCodingModels.CodingValue("3", "Winter")), subcategory = "Schaltmodus", riskLevel = 2, tags = listOf("getriebe", "schalten", "modus", "sport", "eco")),
        AstraJCodingModels.CodingOption("kickdown", AstraJCodingModels.Module.TCM, "Kickdown", "Kickdown", "Kickdown-Funktion", listOf(AstraJCodingModels.CodingValue("0", "Aktiv"), AstraJCodingModels.CodingValue("1", "Deaktiviert")), subcategory = "Kickdown", riskLevel = 2, tags = listOf("kickdown", "getriebe", "beschleunigung")),
        AstraJCodingModels.CodingOption("shift_points", AstraJCodingModels.Module.TCM, "Shift Points", "Schaltpunkt", "Schaltpunkt anpassen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Spaet"), AstraJCodingModels.CodingValue("2", "Frueh")), subcategory = "Schaltpunkt", riskLevel = 2, tags = listOf("schalten", "punkt", "drehzahl", "getriebe")),
        AstraJCodingModels.CodingOption("auto_lock_gearbox", AstraJCodingModels.Module.TCM, "Auto Lock Gearbox", "Autolock Getriebe", "Automatische Verriegelung bei Fahrt", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Verriegelung", riskLevel = 1, tags = listOf("autolock", "getriebe", "sicherheit")),
        AstraJCodingModels.CodingOption("gear_display", AstraJCodingModels.Module.TCM, "Gear Display", "Ganganzeige", "Aktuelle Gangstufe anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Anzeige", riskLevel = 1, tags = listOf("gang", "anzeige", "getriebe", "display")),
        AstraJCodingModels.CodingOption("winter_mode_tcm", AstraJCodingModels.Module.TCM, "Winter Mode TCM", "Winterrad", "Wintermodus fuer Getriebe", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Wintermodus", riskLevel = 1, tags = listOf("winter", "getriebe", "modus")),
        AstraJCodingModels.CodingOption("tiptronic_sensitivity", AstraJCodingModels.Module.TCM, "Tiptronic Sensitivity", "Tiptronic Empfindlichkeit", "Empfindlichkeit beim Schalten", listOf(AstraJCodingModels.CodingValue("0", "Normal"), AstraJCodingModels.CodingValue("1", "Veraenzoegert"), AstraJCodingModels.CodingValue("2", "Sofort")), subcategory = "Tiptronic", riskLevel = 2, tags = listOf("tiptronic", "schalten", "empfindlichkeit")),
        AstraJCodingModels.CodingOption("shift_light_tcm", AstraJCodingModels.Module.TCM, "Shift Light TCM", "Schaltblitz", "LED-Schaltblitz bei optimaler Drehzahl", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein"), AstraJCodingModels.CodingValue("2", "Bei RPM-Schwelle")), subcategory = "Schaltblitz", riskLevel = 1, tags = listOf("schaltblitz", "drehzahl", "led", "getriebe")),
        AstraJCodingModels.CodingOption("rpm_limiter_tcm", AstraJCodingModels.Module.TCM, "RPM Limiter TCM", "Drehzahlbegrenzung Getriebe", "Drehzahlbegrenzung anpassen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "+200 U/min"), AstraJCodingModels.CodingValue("2", "+500 U/min")), subcategory = "Begrenzung", riskLevel = 3, tags = listOf("drehzahl", "begrenzung", "getriebe", "tuning"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("getriebe", "Getriebe", "Settings", opts)
}

object AstraJParkenCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("park_assist_steering", AstraJCodingModels.Module.PAM, "Park Assist Steering", "Parkassistent Lenkung", "Automatisches Einparken mit Lenkung", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiv")), subcategory = "Parklenkung", riskLevel = 1, tags = listOf("parken", "lenkung", "assistent", "automatik")),
        AstraJCodingModels.CodingOption("front_sensors", AstraJCodingModels.Module.PAM, "Front Parking Sensors", "Parksensoren Vorne", "Parksensoren vorne aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "4-Sensoren"), AstraJCodingModels.CodingValue("2", "6-Sensoren")), subcategory = "Parksensoren", riskLevel = 1, tags = listOf("parken", "sensor", "vorne", "ultraschall")),
        AstraJCodingModels.CodingOption("rear_sensors", AstraJCodingModels.Module.PAM, "Rear Parking Sensors", "Parksensoren Heck", "Parksensoren hinten aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "4-Sensoren")), subcategory = "Parksensoren", riskLevel = 1, tags = listOf("parken", "sensor", "heck", "ultraschall")),
        AstraJCodingModels.CodingOption("parking_tone", AstraJCodingModels.Module.PAM, "Parking Tone", "Parkdistance Toene", "Tonmuster bei Parken", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Laut"), AstraJCodingModels.CodingValue("2", "Leise"), AstraJCodingModels.CodingValue("3", "Dynamisch")), subcategory = "Akustik", riskLevel = 1, tags = listOf("parken", "ton", "akustik", "sensor")),
        AstraJCodingModels.CodingOption("auto_parking", AstraJCodingModels.Module.PAM, "Auto Parking", "Automatisches Parken", "Vollautomatisches Einparken", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiv")), subcategory = "Autoparken", riskLevel = 2, tags = listOf("parken", "auto", "automatisch", "pilot")),
        AstraJCodingModels.CodingOption("cornering_light_park", AstraJCodingModels.Module.BCM, "Cornering Light Park", "Abbiegelicht", "Dynamisches Abbiegelicht bei langsamer Fahrt", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Abbiegelicht", riskLevel = 1, tags = listOf("abbiegelicht", "beleuchtung", "parken")),
        AstraJCodingModels.CodingOption("parking_light_mode", AstraJCodingModels.Module.BCM, "Parking Light Mode", "Parklicht Modus", "Parklicht bei langsamem Fahren", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Links"), AstraJCodingModels.CodingValue("2", "Rechts"), AstraJCodingModels.CodingValue("3", "Beide")), subcategory = "Parklicht", riskLevel = 1, tags = listOf("parklicht", "beleuchtung", "modus")),
        AstraJCodingModels.CodingOption("parking_tone_pattern", AstraJCodingModels.Module.PAM, "Parking Tone Pattern", "Parksignal Muster", "Signalton-Muster bei Parken", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Dynamisch"), AstraJCodingModels.CodingValue("2", "Progressiv")), subcategory = "Akustik", riskLevel = 1, tags = listOf("parken", "signal", "ton", "muster"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("parken", "Parkassistent", "Parking", opts)
}

object AstraJAnhaengerCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("trailer_mode", AstraJCodingModels.Module.TRC, "Trailer Mode", "Anhaengermodus", "Anhaengermodus aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiv")), subcategory = "Anhaenger", riskLevel = 2, tags = listOf("anhaenger", "modus", "trc")),
        AstraJCodingModels.CodingOption("trailer_esc", AstraJCodingModels.Module.TRC, "Trailer ESC", "Anhaenger ESC", "ESC-Anpassung fuer Anhaenger", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Empfindlich"), AstraJCodingModels.CodingValue("2", "Dezent")), subcategory = "ESC", riskLevel = 2, tags = listOf("anhaenger", "esc", "stabilitaet")),
        AstraJCodingModels.CodingOption("trailer_blinker_rate", AstraJCodingModels.Module.TRC, "Trailer Blinker Rate", "Blinker Frequenz Anhaenger", "Blinkfrequenz fuer LED-Anhaenger", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Schnell")), subcategory = "Blinker", riskLevel = 1, tags = listOf("anhaenger", "blinker", "frequenz", "led")),
        AstraJCodingModels.CodingOption("trailer_lights_check", AstraJCodingModels.Module.TRC, "Trailer Lights Check", "Anhaengerbeleuchtung Pruefung", "Automatische Pruefung der Anhaengerbeleuchtung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Beleuchtung", riskLevel = 1, tags = listOf("anhaenger", "beleuchtung", "pruefung")),
        AstraJCodingModels.CodingOption("trailer_stability", AstraJCodingModels.Module.TRC, "Trailer Stability", "Stabilitaetskontrolle Anhaenger", "Verstaerkte Stabilitaetskontrolle", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Normal"), AstraJCodingModels.CodingValue("2", "Intensiv")), subcategory = "Stabilitaet", riskLevel = 3, tags = listOf("anhaenger", "stabilitaet", "kontrolle", "sicherheit"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("anhaenger", "Anhaenger", "LocalShipping", opts)
}

object AstraJBremseCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("abs_sensitivity_brake", AstraJCodingModels.Module.ABS, "ABS Sensitivity Brake", "ABS Empfindlichkeit", "ABS Ansprechempfindlichkeit", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Verstaerkt"), AstraJCodingModels.CodingValue("2", "Dezent")), subcategory = "ABS", riskLevel = 3, tags = listOf("abs", "bremse", "empfindlichkeit", "sicherheit")),
        AstraJCodingModels.CodingOption("brake_assist_brake", AstraJCodingModels.Module.ABS, "Brake Assist Brake", "Bremsassistent", "Bremsassistent Empfindlichkeit", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Aggressiv"), AstraJCodingModels.CodingValue("2", "Dezent")), subcategory = "Bremsassistent", riskLevel = 2, tags = listOf("bremse", "assistent", "sicherheit")),
        AstraJCodingModels.CodingOption("brake_pad_monitor", AstraJCodingModels.Module.ABS, "Brake Pad Monitor", "Bremsbelagueberwachung", "Bremsbelag-Verschleissanzeige", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Verschleiss", riskLevel = 1, tags = listOf("bremse", "belag", "verschleiss", "monitor")),
        AstraJCodingModels.CodingOption("brake_prefill_brake", AstraJCodingModels.Module.ABS, "Brake Pre-fill Brake", "Bremsevorspannung", "Bremse vorspannen bei Fuss auf Pedal", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Vorspannung", riskLevel = 2, tags = listOf("bremse", "vorspannung", "sicherheit")),
        AstraJCodingModels.CodingOption("dry_braking_brake", AstraJCodingModels.Module.ABS, "Dry Braking Brake", "Trockenbremse", "Scheiben trockenbremsen bei Naesse", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Trockenbremse", riskLevel = 1, tags = listOf("bremse", "trocken", "naesse", "sicherheit")),
        AstraJCodingModels.CodingOption("emergency_brake_flash", AstraJCodingModels.Module.BCM, "Emergency Brake Flash", "Notbremsblinken", "Bremslichter blinken bei Notbremsung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Notbremsung", riskLevel = 1, tags = listOf("bremslicht", "notbremsung", "blinken", "sicherheit"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("bremse", "ABS & Bremse", "DiscFull", opts)
}

object AstraJDieselCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("dpf_regeneration", AstraJCodingModels.Module.ECU, "DPF Regeneration", "DPF Regeneration", "Dieselpartikelfilter Regeneration", listOf(AstraJCodingModels.CodingValue("0", "Automatisch"), AstraJCodingModels.CodingValue("1", "Manuell"), AstraJCodingModels.CodingValue("2", "Aus")), subcategory = "DPF", riskLevel = 3, tags = listOf("dpf", "partikelfilter", "regeneration", "diesel")),
        AstraJCodingModels.CodingOption("glow_plug_extended", AstraJCodingModels.Module.ECU, "Glow Plug Extended", "Gluehkerzen Veraengerung", "Veraengerte Gluehzeit bei Kaelte", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Gluehkerzen", riskLevel = 2, tags = listOf("gluehkerze", "diesel", "kaelte", "start")),
        AstraJCodingModels.CodingOption("adblue_consumption", AstraJCodingModels.Module.ECU, "AdBlue Consumption", "AdBlue Verbrauch", "AdBlue-Verbrauchsoptimierung", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Reduziert")), subcategory = "AdBlue", riskLevel = 2, tags = listOf("adblue", "verbrauch", "nox", "diesel")),
        AstraJCodingModels.CodingOption("dpf_active", AstraJCodingModels.Module.ECU, "DPF Active", "Dieselpartikelfilter", "DPF Status", listOf(AstraJCodingModels.CodingValue("0", "Aktiv"), AstraJCodingModels.CodingValue("1", "Deaktiviert")), subcategory = "DPF", riskLevel = 4, tags = listOf("dpf", "partikelfilter", "diesel", "tuning", "expert")),
        AstraJCodingModels.CodingOption("egr_valve", AstraJCodingModels.Module.ECU, "EGR Valve", "Abgasrecirculation (EGR)", "Abgasrueckfuehrung anpassen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Reduziert"), AstraJCodingModels.CodingValue("2", "Deaktiviert")), subcategory = "EGR", riskLevel = 3, tags = listOf("egr", "abgas", "rueckfuehrung", "diesel", "tuning")),
        AstraJCodingModels.CodingOption("fuel_injection_diesel", AstraJCodingModels.Module.ECU, "Injection Mode Diesel", "Kraftstoff Einspritzung", "Einspritzverhalten des Dieselmotors", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Pilot"), AstraJCodingModels.CodingValue("2", "Main"), AstraJCodingModels.CodingValue("3", "Post")), subcategory = "Einspritzung", riskLevel = 3, tags = listOf("einspritzung", "kraftstoff", "diesel", "tuning")),
        AstraJCodingModels.CodingOption("max_fuel_pressure_diesel", AstraJCodingModels.Module.ECU, "Max Fuel Pressure Diesel", "Max Kraftstoffdruck Diesel", "Maximaler Kraftstoffdruck", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Erhoeht")), subcategory = "Kraftstoff", riskLevel = 3, tags = listOf("kraftstoff", "druck", "diesel", "tuning")),
        AstraJCodingModels.CodingOption("idle_rpm_diesel", AstraJCodingModels.Module.ECU, "Idle RPM Diesel", "Leerlauf Drehzahl Diesel", "Leerlaufdrehzahl anpassen", listOf(AstraJCodingModels.CodingValue("0", "750 U/min"), AstraJCodingModels.CodingValue("1", "800 U/min"), AstraJCodingModels.CodingValue("2", "850 U/min"), AstraJCodingModels.CodingValue("3", "900 U/min")), subcategory = "Leerlauf", riskLevel = 2, tags = listOf("leerlauf", "drehzahl", "diesel", "tuning")),
        AstraJCodingModels.CodingOption("dpf_warning", AstraJCodingModels.Module.ECU, "DPF Warning", "DPF Warnung", "Warnung bei hohem Partikelfilter-Fuellstand", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "DPF", riskLevel = 1, tags = listOf("dpf", "warnung", "partikelfilter", "diesel"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("diesel", "Diesel-Spezifisch", "LocalGasStation", opts)
}

object AstraJTurboCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("boost_limit", AstraJCodingModels.Module.ECU, "Boost Limit", "Ladedruck Begrenzung", "Maximalen Ladedruck anpassen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "+0.1 bar"), AstraJCodingModels.CodingValue("2", "+0.2 bar")), subcategory = "Ladedruck", riskLevel = 3, tags = listOf("boost", "ladedruck", "turbo", "tuning", "leistung")),
        AstraJCodingModels.CodingOption("wastegate_control", AstraJCodingModels.Module.ECU, "Wastegate Control", "Waste Gate", "Wastegate-Ansteuerung", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Offen"), AstraJCodingModels.CodingValue("2", "Frueh")), subcategory = "Wastegate", riskLevel = 3, tags = listOf("wastegate", "turbo", "ladedruck", "tuning")),
        AstraJCodingModels.CodingOption("anti_lag", AstraJCodingModels.Module.ECU, "Anti Lag System", "Anti-Lag", "Anti-Lag-System freischalten", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Anti-Lag", riskLevel = 4, tags = listOf("anti", "lag", "turbo", "performance", "expert")),
        AstraJCodingModels.CodingOption("launch_control_turbo", AstraJCodingModels.Module.ECU, "Launch Control Turbo", "Launch Control", "Launch Control fuer schnellen Start", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Launch", riskLevel = 4, tags = listOf("launch", "start", "performance", "expert")),
        AstraJCodingModels.CodingOption("overboost_turbo", AstraJCodingModels.Module.ECU, "Overboost Turbo", "Overboost", "Kurzzeitiger Ladedruck-Ueberboost", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Overboost", riskLevel = 3, tags = listOf("overboost", "turbo", "boost", "performance")),
        AstraJCodingModels.CodingOption("turbo_spool_sound", AstraJCodingModels.Module.ECU, "Turbo Spool Sound", "Turbo Spool Sound", "Turbo-Sound bei Last", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Leicht"), AstraJCodingModels.CodingValue("2", "Deutlich")), subcategory = "Sound", riskLevel = 1, tags = listOf("turbo", "sound", "spool", "audio")),
        AstraJCodingModels.CodingOption("throttle_response_turbo", AstraJCodingModels.Module.ECU, "Throttle Response Turbo", "Drosselklappen Ansprechverhalten Turbo", "Gaspedal Ansprechverhalten anpassen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Sport"), AstraJCodingModels.CodingValue("2", "Eco")), subcategory = "Drosselklappe", riskLevel = 2, tags = listOf("gas", "pedal", "ansprechen", "sport", "tuning")),
        AstraJCodingModels.CodingOption("max_boost_turbo", AstraJCodingModels.Module.ECU, "Max Boost Pressure Turbo", "Max Ladedruck Turbo", "Maximalen Ladedruck erhoehen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "+0.1 bar"), AstraJCodingModels.CodingValue("2", "+0.2 bar")), subcategory = "Ladedruck", riskLevel = 4, tags = listOf("turbo", "boost", "ladedruck", "tuning", "expert")),
        AstraJCodingModels.CodingOption("boost_gauge_turbo", AstraJCodingModels.Module.IPC, "Boost Gauge Turbo", "Ladedruck-Anzeige Turbo", "Turbo-Ladedruck im Display", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein"), AstraJCodingModels.CodingValue("2", "Detailliert")), subcategory = "Anzeige", riskLevel = 1, tags = listOf("boost", "ladedruck", "turbo", "display")),
        AstraJCodingModels.CodingOption("rpm_limiter_turbo", AstraJCodingModels.Module.ECU, "RPM Limiter Turbo", "Drehzahlbegrenzung Turbo", "Drehzahlbegrenzung anpassen", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "+200 U/min"), AstraJCodingModels.CodingValue("2", "+400 U/min")), subcategory = "Begrenzung", riskLevel = 3, tags = listOf("drehzahl", "begrenzung", "turbo", "tuning", "leistung"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("turbo", "Turbo & Leistung", "Speed", opts)
}

object AstraJAFLCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("afl_adaptive", AstraJCodingModels.Module.AFL, "Adaptive Forward Lighting", "AFL Adaptive", "Adaptive Forward Lighting", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiv")), hardwareRequired = "AFL Scheinwerfer erforderlich", subcategory = "AFL", riskLevel = 2, tags = listOf("afl", "adaptive", "beleuchtung", "scheinwerfer")),
        AstraJCodingModels.CodingOption("afl_cornering", AstraJCodingModels.Module.AFL, "AFL Cornering Light", "Kurvenlicht AFL", "Dynamisches Kurvenlicht", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiv")), hardwareRequired = "AFL Scheinwerfer erforderlich", subcategory = "Kurvenlicht", riskLevel = 1, tags = listOf("afl", "kurvenlicht", "beleuchtung", "scheinwerfer")),
        AstraJCodingModels.CodingOption("high_beam_assist", AstraJCodingModels.Module.AFL, "High Beam Assist", "Fernlicht Assistent", "Automatischer Fernlicht-Assistent", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiv")), subcategory = "Fernlicht", riskLevel = 1, tags = listOf("fernlicht", "assistent", "automatik", "sicherheit")),
        AstraJCodingModels.CodingOption("afl_speed_adaptive", AstraJCodingModels.Module.AFL, "Speed Adaptive AFL", "AFL Geschwindigkeit", "Geschwindigkeitsabhaengige Lichtanpassung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "AFL", riskLevel = 1, tags = listOf("afl", "geschwindigkeit", "beleuchtung", "anpassung")),
        AstraJCodingModels.CodingOption("afl_light_distribution", AstraJCodingModels.Module.AFL, "Light Distribution Mode", "Lichtverteilung", "Lichtverteilung je nach Fahrsituation", listOf(AstraJCodingModels.CodingValue("0", "Stadt"), AstraJCodingModels.CodingValue("1", "Land"), AstraJCodingModels.CodingValue("2", "Autobahn")), subcategory = "Lichtverteilung", riskLevel = 1, tags = listOf("afl", "licht", "verteilung", "stadt", "land", "autobahn")),
        AstraJCodingModels.CodingOption("afl_sensitivity", AstraJCodingModels.Module.AFL, "AFL Sensitivity", "AFL Empfindlichkeit", "Empfindlichkeit des AFL-Systems", listOf(AstraJCodingModels.CodingValue("0", "Niedrig"), AstraJCodingModels.CodingValue("1", "Mittel"), AstraJCodingModels.CodingValue("2", "Hoch")), subcategory = "AFL", riskLevel = 1, tags = listOf("afl", "empfindlichkeit", "beleuchtung"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("afl", "AFL Scheinwerfer", "FlashlightOn", opts)
}

object AstraJEPBCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("epb_auto_release", AstraJCodingModels.Module.EPB, "Auto Release EPB", "Automatisch Loesen", "EPB automatisch beim Anfahren loesen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Auto Loesen", riskLevel = 1, tags = listOf("epb", "auto", "loesen", "komfort")),
        AstraJCodingModels.CodingOption("epb_emergency", AstraJCodingModels.Module.EPB, "EPB Emergency", "Notfallmodus", "EPB-Notfallmodus aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Notfall", riskLevel = 2, tags = listOf("epb", "notfall", "sicherheit")),
        AstraJCodingModels.CodingOption("epb_hill_hold", AstraJCodingModels.Module.EPB, "Hill Hold EPB", "Berganfahrhilfe EPB", "Berganfahrhilfe via EPB", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Berganfahr", riskLevel = 1, tags = listOf("epb", "berg", "anfahrt", "sicherheit")),
        AstraJCodingModels.CodingOption("epb_warning", AstraJCodingModels.Module.EPB, "EPB Warning Beep", "EPB Warnung", "Akustische Warnung bei EPB-Aktivitaet", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Warnung", riskLevel = 1, tags = listOf("epb", "warnung", "akustik")),
        AstraJCodingModels.CodingOption("auto_hold", AstraJCodingModels.Module.EPB, "Auto Hold", "Auto Hold", "Automatisch beim Halten loesen", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Aktiviert")), subcategory = "Auto Hold", riskLevel = 1, tags = listOf("auto", "hold", "komfort", "sicherheit"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("epb", "E-Parkbremse", "Lock", opts)
}

object AstraJReifenCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("tpms_warning_level", AstraJCodingModels.Module.TPM, "TPMS Warning Level", "TPMS Warnschwelle", "Warnschwelle fuer Reifendruck", listOf(AstraJCodingModels.CodingValue("0", "Niedrig"), AstraJCodingModels.CodingValue("1", "Mittel"), AstraJCodingModels.CodingValue("2", "Hoch")), subcategory = "TPMS", riskLevel = 1, tags = listOf("tpms", "warnung", "reifendruck", "sicherheit")),
        AstraJCodingModels.CodingOption("tpms_display_unit", AstraJCodingModels.Module.TPM, "TPMS Display Unit", "TPMS Anzeige", "Einheit fuer Reifendruckanzeige", listOf(AstraJCodingModels.CodingValue("0", "Bar"), AstraJCodingModels.CodingValue("1", "PSI"), AstraJCodingModels.CodingValue("2", "kPa")), subcategory = "Anzeige", riskLevel = 1, tags = listOf("tpms", "anzeige", "einheit", "reifendruck")),
        AstraJCodingModels.CodingOption("tire_calibration", AstraJCodingModels.Module.TPM, "Tire Calibration", "Reifendruck Kalibrierung", "Kalibrierung fuer Reifendruck", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Gelaende"), AstraJCodingModels.CodingValue("2", "Sport")), subcategory = "Kalibrierung", riskLevel = 1, tags = listOf("reifen", "kalibrierung", "gelaende", "sport")),
        AstraJCodingModels.CodingOption("temperature_monitor", AstraJCodingModels.Module.TPM, "Temperature Monitor", "Temperaturueberwachung", "Reifentemperatur-Ueberwachung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Temperatur", riskLevel = 1, tags = listOf("reifen", "temperatur", "sicherheit")),
        AstraJCodingModels.CodingOption("tire_size_display", AstraJCodingModels.Module.TPM, "Tire Size Display", "Reifengrößenanzeige", "Reifengröße im Display anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Anzeige", riskLevel = 1, tags = listOf("reifen", "groesse", "anzeige", "display")),
        AstraJCodingModels.CodingOption("tpms_reset", AstraJCodingModels.Module.TPM, "TPMS Reset", "Reifendruck Reset", "Reifendruck-Sensoren zuruecksetzen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Reset", riskLevel = 1, tags = listOf("tpms", "reset", "reifendruck", "sensor"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("reifen", "Reifendruck", "Circle", opts)
}

object AstraJDiagnoseCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("auto_dtc_clear", AstraJCodingModels.Module.ECU, "Auto DTC Clear", "DTC Automatisch Loeschen", "Fehlerspeicher automatisch loeschen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "DTC", riskLevel = 1, tags = listOf("dtc", "fehler", "loeschen", "diagnose")),
        AstraJCodingModels.CodingOption("diagnostic_menu_level", AstraJCodingModels.Module.ECU, "Diagnostic Menu Level", "Diagnose Menue", "Diagnose-Menue Ebene", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Erweitert")), subcategory = "Diagnose", riskLevel = 1, tags = listOf("diagnose", "menu", "werkstatt", "service")),
        AstraJCodingModels.CodingOption("dtc_export", AstraJCodingModels.Module.ECU, "DTC Export Enable", "Fehlerspeicher Export", "Fehlerspeicher Export aktivieren", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Export", riskLevel = 1, tags = listOf("dtc", "export", "diagnose", "daten")),
        AstraJCodingModels.CodingOption("sensor_logging", AstraJCodingModels.Module.ECU, "Sensor Data Logging", "Sensor Logging", "Sensor-Daten aufzeichnen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Logging", riskLevel = 1, tags = listOf("sensor", "logging", "daten", "aufzeichnung")),
        AstraJCodingModels.CodingOption("extended_obd", AstraJCodingModels.Module.ECU, "Extended OBD Mode", "OBD Erweitert", "Erweiterte OBD-Daten freischalten", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "OBD", riskLevel = 1, tags = listOf("obd", "extended", "diagnose", "daten")),
        AstraJCodingModels.CodingOption("can_trace", AstraJCodingModels.Module.ECU, "CAN Bus Trace", "CAN Trace", "CAN-Bus Datenverkehr anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "CAN", riskLevel = 1, tags = listOf("can", "bus", "trace", "diagnose")),
        AstraJCodingModels.CodingOption("live_data_rate", AstraJCodingModels.Module.ECU, "Live Data Rate", "Live Daten Rate", "Datenaktualisierungsrate", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Schnell"), AstraJCodingModels.CodingValue("2", "Sehr schnell")), subcategory = "Live Daten", riskLevel = 1, tags = listOf("live", "daten", "rate", "diagnose"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("diagnose", "Diagnose", "BugReport", opts)
}

object AstraJWartungCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("service_interval", AstraJCodingModels.Module.ECU, "Service Interval km", "Service Intervall", "Service-Intervall anpassen", listOf(AstraJCodingModels.CodingValue("0", "15000 km"), AstraJCodingModels.CodingValue("1", "20000 km"), AstraJCodingModels.CodingValue("2", "25000 km"), AstraJCodingModels.CodingValue("3", "30000 km")), subcategory = "Service", riskLevel = 1, tags = listOf("service", "intervall", "wartung")),
        AstraJCodingModels.CodingOption("oil_life_reset", AstraJCodingModels.Module.ECU, "Oil Life Reset", "Oelwechsel Ruecksetzung", "Oelwechsel-Anzeige zuruecksetzen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Oel", riskLevel = 1, tags = listOf("oel", "wechsel", "reset", "wartung")),
        AstraJCodingModels.CodingOption("brake_pad_reset", AstraJCodingModels.Module.ECU, "Brake Pad Life Reset", "Bremsbelag Ruecksetzung", "Bremsbelag-Anzeige zuruecksetzen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Bremse", riskLevel = 1, tags = listOf("bremse", "belag", "reset", "wartung")),
        AstraJCodingModels.CodingOption("service_reminder", AstraJCodingModels.Module.IPC, "Service Reminder", "Service Hinweis", "Service-Erinnerung anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Erinnerung", riskLevel = 1, tags = listOf("service", "erinnerung", "wartung")),
        AstraJCodingModels.CodingOption("level_sensor", AstraJCodingModels.Module.BCM, "Level Sensor", "Niveausensor", "Niveausensor fuer Federung", listOf(AstraJCodingModels.CodingValue("0", "Nicht vorhanden"), AstraJCodingModels.CodingValue("1", "Vorhanden")), subcategory = "Sensor", riskLevel = 1, tags = listOf("niveau", "sensor", "federung")),
        AstraJCodingModels.CodingOption("battery_registration", AstraJCodingModels.Module.ECU, "Battery Registration", "Batterie Registration", "Batterie-Management zuruecksetzen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Batterie", riskLevel = 2, tags = listOf("batterie", "registration", "reset", "bmg"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("wartung", "Wartung & Service", "Build", opts)
}

object AstraJCodingRepository {

    fun getAllCategories(): List<AstraJCodingModels.CodingCategory> = listOf(
        AstraJBleuchtungCoding.getCategory(),
        AstraJKomfortCoding.getCategory(),
        AstraJMotorCoding.getCategory(),
        AstraJIPCCoding.getCategory(),
        AstraJIPCExtendedCoding.getCategory(),
        AstraJInfotainmentCoding.getCategory(),
        AstraJInfotainmentExtendedCoding.getCategory(),
        AstraJFahrdynamikCoding.getCategory(),
        AstraJABSESPCoding.getCategory(),
        AstraJSecurityCoding.getCategory(),
        AstraJSitzkomfortCoding.getCategory(),
        AstraJKlimaCoding.getCategory(),
        AstraJGetriebeCoding.getCategory(),
        AstraJTCMCoding.getCategory(),
        AstraJParkenCoding.getCategory(),
        AstraJAnhaengerCoding.getCategory(),
        AstraJBremseCoding.getCategory(),
        AstraJDieselCoding.getCategory(),
        AstraJTurboCoding.getCategory(),
        AstraJAFLCoding.getCategory(),
        AstraJEPBCoding.getCategory(),
        AstraJReifenCoding.getCategory(),
        AstraJDiagnoseCoding.getCategory(),
        AstraJWartungCoding.getCategory(),
        AstraJMotorExtendedCoding.getCategory(),
        AstraJBCMExtendedCoding.getCategory(),
        AstraJAirbagCoding.getCategory(),
        AstraJVersteckteFeaturesCoding.getCategory()
    )

    fun getAllOptions(): List<AstraJCodingModels.CodingOption> =
        getAllCategories().flatMap { it.options }

    fun getCategoryById(id: String): AstraJCodingModels.CodingCategory? =
        getAllCategories().find { it.id == id }

    fun getOptionById(id: String): AstraJCodingModels.CodingOption? =
        getAllOptions().find { it.id == id }

    fun searchOptions(query: String): List<AstraJCodingModels.CodingOption> {
        if (query.isBlank()) return getAllOptions()
        val q = query.lowercase()
        return getAllOptions().filter { opt ->
            opt.displayName.lowercase().contains(q) ||
                opt.description.lowercase().contains(q) ||
                opt.id.lowercase().contains(q) ||
                opt.tags.any { it.lowercase().contains(q) }
        }
    }

    fun getFavoriteOptions(ids: List<String>): List<AstraJCodingModels.CodingOption> =
        ids.mapNotNull { getOptionById(it) }

    fun getProfiles(): List<AstraJCodingModels.CodingProfile> = listOf(
        AstraJCodingModels.CodingProfile("stock", "Werkseinstellung", "Alle Werte auf Werkseinstellung zuruecksetzen", emptyMap()),
        AstraJCodingModels.CodingProfile("comfort", "Komfort", "Maximaler Komfort mit allen Helfern", mapOf(
            "auto_lock_12" to "1", "single_unlock" to "1", "comfort_close" to "1", "comfort_open" to "1",
            "mirror_fold" to "1", "mirror_unfold" to "1", "acoustic_lock" to "3", "coming_home" to "3",
            "seat_heating_auto" to "1", "climate_auto" to "1"
        )),
        AstraJCodingModels.CodingProfile("sport", "Sport", "Sportliche Einstellung mit ESC-Off", mapOf(
            "esp_sport" to "1", "eco_mode" to "2", "single_unlock" to "0", "boost_gauge" to "1",
            "throttle_response" to "1", "shift_mode" to "1"
        )),
        AstraJCodingModels.CodingProfile("eco", "Eco", "Sparsame Einstellung mit Start-Stopp", mapOf(
            "start_stop" to "1", "eco_mode" to "1", "eco_index" to "1", "climate_auto" to "1"
        )),
        AstraJCodingModels.CodingProfile("winter", "Winter", "Optimierte Einstellungen fuer Winter", mapOf(
            "winter_mode_tcm" to "1", "frost_protection" to "2", "seat_heating_auto" to "1",
            "windshield_heating" to "1", "climate_auto" to "1", "brake_assist" to "1"
        )),
        AstraJCodingModels.CodingProfile("stadt", "Stadt", "Optimiert fuer den Stadtverkehr", mapOf(
            "front_sensors" to "2", "park_assist_steering" to "1", "cornering_light_park" to "1",
            "parking_light_mode" to "3", "wiper_interval" to "3", "rain_sensitivity" to "2"
        )),
        AstraJCodingModels.CodingProfile("tuning", "Tuning", "Leistungsorientierte Einstellungen", mapOf(
            "boost_limit" to "1", "launch_control_hidden" to "1", "overboost_hidden" to "1",
            "dpf_hidden" to "1", "engine_sound" to "2", "throttle_response" to "1",
            "egr_hidden" to "2", "rpm_limiter_turbo" to "1"
        )),
        AstraJCodingModels.CodingProfile("langstrecke", "Langstrecke", "Optimiert fuer lange Fahrten", mapOf(
            "cruise_control" to "1", "board_computer" to "1", "range_display" to "1",
            "avg_mpg" to "1", "gear_display" to "1", "auto_lock_12" to "1",
            "dry_braking" to "1"
        ))
    )
}

object AstraJIPCExtendedCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("temp_unit_celsius", AstraJCodingModels.Module.IPC, "Temperature Unit", "Temperatureinheit", "Celsius oder Fahrenheit anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Celsius (C)"), AstraJCodingModels.CodingValue("1", "Fahrenheit (F)")), subcategory = "Einheiten", riskLevel = 1, tags = listOf("temperatur", "einheit", "celsius", "fahrenheit")),
        AstraJCodingModels.CodingOption("speed_unit_kmh", AstraJCodingModels.Module.IPC, "Speed Unit", "Geschwindigkeitseinheit", "km/h oder mph anzeigen", listOf(AstraJCodingModels.CodingValue("0", "km/h"), AstraJCodingModels.CodingValue("1", "mph")), subcategory = "Einheiten", riskLevel = 1, tags = listOf("geschwindigkeit", "kmh", "mph", "einheit")),
        AstraJCodingModels.CodingOption("fuel_unit_liter", AstraJCodingModels.Module.IPC, "Fuel Unit", "Kraftstoffeinheit", "Liter oder Gallone anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Liter (L)"), AstraJCodingModels.CodingValue("1", "Gallone (US)")), subcategory = "Einheiten", riskLevel = 1, tags = listOf("kraftstoff", "liter", "gallone", "einheit")),
        AstraJCodingModels.CodingOption("pressure_unit_bar", AstraJCodingModels.Module.IPC, "Pressure Unit", "Druckeinheit", "Bar, PSI oder kPa anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Bar"), AstraJCodingModels.CodingValue("1", "PSI"), AstraJCodingModels.CodingValue("2", "kPa")), subcategory = "Einheiten", riskLevel = 1, tags = listOf("druck", "bar", "psi", "kpa", "einheit")),
        AstraJCodingModels.CodingOption("date_format", AstraJCodingModels.Module.IPC, "Date Format", "Datumsformat", "Datumsanzeige-Format", listOf(AstraJCodingModels.CodingValue("0", "TT.MM.JJJJ"), AstraJCodingModels.CodingValue("1", "MM/TT/JJJJ"), AstraJCodingModels.CodingValue("2", "JJJJ-MM-TT")), subcategory = "Einstellungen", riskLevel = 1, tags = listOf("datum", "format", "einstellung")),
        AstraJCodingModels.CodingOption("clock_format", AstraJCodingModels.Module.IPC, "Clock Format", "Uhrzeitformat", "12h oder 24h anzeigen", listOf(AstraJCodingModels.CodingValue("0", "24-Stunden"), AstraJCodingModels.CodingValue("1", "12-Stunden AM/PM")), subcategory = "Einstellungen", riskLevel = 1, tags = listOf("uhr", "zeit", "12h", "24h", "format")),
        AstraJCodingModels.CodingOption("oil_life_ipc", AstraJCodingModels.Module.IPC, "Oil Life IPC", "Oellebensdauer Anzeige", "Oellebensdauer im Display", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Service", riskLevel = 1, tags = listOf("oel", "lebensdauer", "service", "anzeige")),
        AstraJCodingModels.CodingOption("inspection_due", AstraJCodingModels.Module.IPC, "Inspection Due", "Inspektionsanzeige", "Nächste Inspektion anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Service", riskLevel = 1, tags = listOf("inspektion", "service", "anzeige")),
        AstraJCodingModels.CodingOption("odometer_display", AstraJCodingModels.Module.IPC, "Odometer Display", "Kilometerstandanzeige", "Gesamtkilometerstand anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Meilen"), AstraJCodingModels.CodingValue("1", "Kilometer")), subcategory = "Anzeige", riskLevel = 1, tags = listOf("kilometer", "tacho", "anzeige", "display")),
        AstraJCodingModels.CodingOption("illumination_day", AstraJCodingModels.Module.IPC, "Illumination Day", "Tacho-Beleuchtung Tag", "Skalenbeleuchtung bei Tag", listOf(AstraJCodingModels.CodingValue("0", "20%"), AstraJCodingModels.CodingValue("1", "40%"), AstraJCodingModels.CodingValue("2", "60%"), AstraJCodingModels.CodingValue("3", "80%"), AstraJCodingModels.CodingValue("4", "100%")), subcategory = "Beleuchtung", riskLevel = 1, tags = listOf("beleuchtung", "tacho", "tag", "display")),
        AstraJCodingModels.CodingOption("illumination_night", AstraJCodingModels.Module.IPC, "Illumination Night", "Tacho-Beleuchtung Nacht", "Skalenbeleuchtung bei Nacht", listOf(AstraJCodingModels.CodingValue("0", "20%"), AstraJCodingModels.CodingValue("1", "40%"), AstraJCodingModels.CodingValue("2", "60%"), AstraJCodingModels.CodingValue("3", "80%"), AstraJCodingModels.CodingValue("4", "100%")), subcategory = "Beleuchtung", riskLevel = 1, tags = listOf("beleuchtung", "tacho", "nacht", "display")),
        AstraJCodingModels.CodingOption("seatbelt_warning", AstraJCodingModels.Module.IPC, "Seatbelt Warning", "Gurtwarnung", "Akustische Gurtwarnung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Warnungen", riskLevel = 1, tags = listOf("gurt", "warnung", "sicherheit", "akustik")),
        AstraJCodingModels.CodingOption("fuel_reserve_warning", AstraJCodingModels.Module.IPC, "Fuel Reserve Warning", "Reservetankanzeige", "Warnung bei niedrigem Tankstand", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Warnungen", riskLevel = 1, tags = listOf("tank", "reserve", "warnung", "kraftstoff")),
        AstraJCodingModels.CodingOption("door_open_warning", AstraJCodingModels.Module.IPC, "Door Open Warning", "Tür-offen-Warnung", "Warnung bei offener Tür", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Warnungen", riskLevel = 1, tags = listOf("tuer", "warnung", "anzeige", "sicherheit")),
        AstraJCodingModels.CodingOption("coolant_temp_gauge", AstraJCodingModels.Module.IPC, "Coolant Temp Gauge", "Kühlmitteltemperaturanzeige", "Kühlmitteltemperatur im Tacho", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Anzeige", riskLevel = 1, tags = listOf("kuehlmittel", "temperatur", "tacho", "anzeige")),
        AstraJCodingModels.CodingOption("shift_up Indicator", AstraJCodingModels.Module.IPC, "Shift Up Indicator", "Schaltempfehlung", "Empfehlung zum Hochschalten", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Anzeige", riskLevel = 1, tags = listOf("schalten", "empfehlung", "gang", "anzeige")),
        AstraJCodingModels.CodingOption("auto_dimming_ipc", AstraJCodingModels.Module.IPC, "Auto Dimming IPC", "Automatische Abdunkelung", "Automatische Tacho-Abdunkelung", listOf(AstraJCodingModels.CodingValue("0", "Manuell"), AstraJCodingModels.CodingValue("1", "Automatisch")), subcategory = "Beleuchtung", riskLevel = 1, tags = listOf("dimmung", "automatisch", "tacho", "lichtsensor")),
        AstraJCodingModels.CodingOption("key_reminder", AstraJCodingModels.Module.IPC, "Key Reminder", "Schlüssel-Erinnerung", "Erinnerung bei vergessenem Schlüssel", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Erinnerungen", riskLevel = 1, tags = listOf("schluessel", "erinnerung", "anzeige"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("ipc_extended", "IPC & Anzeigen", "Dashboard", opts)
}

object AstraJABSESPCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("abs_operation", AstraJCodingModels.Module.ABS, "ABS Operation", "ABS Betrieb", "ABS-System Betriebsmodus", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Sport")), subcategory = "ABS", riskLevel = 2, tags = listOf("abs", "bremse", "betrieb")),
        AstraJCodingModels.CodingOption("ebd_operation", AstraJCodingModels.Module.ABS, "EBD Operation", "EBD Betrieb", "Elektronische Bremskraftverteilung", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Verbessert")), subcategory = "EBD", riskLevel = 2, tags = listOf("ebd", "bremse", "verteilung")),
        AstraJCodingModels.CodingOption("brake_assist_abs", AstraJCodingModels.Module.ABS, "Brake Assist ABS", "BA Betrieb", "Bremsassistent-Betrieb", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Normal"), AstraJCodingModels.CodingValue("2", "Sensibel")), subcategory = "Bremsassistent", riskLevel = 2, tags = listOf("ba", "bremsassistent", "sicherheit")),
        AstraJCodingModels.CodingOption("hdc_operation", AstraJCodingModels.Module.ABS, "HDC Operation", "HDC Betrieb", "Hill Descent Control Betrieb", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "HDC", riskLevel = 2, tags = listOf("hdc", "bergab", "fahrassistent")),
        AstraJCodingModels.CodingOption("tcs_operation", AstraJCodingModels.Module.ABS, "TCS Operation", "ASR Betrieb", "Antriebsschlupfregelung Betrieb", listOf(AstraJCodingModels.CodingValue("0", "Aktiv"), AstraJCodingModels.CodingValue("1", "Deaktivierbar")), subcategory = "ASR", riskLevel = 2, tags = listOf("tcs", "asr", "traktion", "schlupf")),
        AstraJCodingModels.CodingOption("ess_operation", AstraJCodingModels.Module.ABS, "ESS Operation", "ESS Betrieb", "Emergency Stop Signal Betrieb", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "ESS", riskLevel = 1, tags = listOf("ess", "notbremsung", "bremslicht", "sicherheit")),
        AstraJCodingModels.CodingOption("rsc_operation", AstraJCodingModels.Module.ABS, "RSC Operation", "RSC Betrieb", "Roll Stability Control Betrieb", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "RSC", riskLevel = 2, tags = listOf("rsc", "stabilitaet", "rollen")),
        AstraJCodingModels.CodingOption("baw_operation", AstraJCodingModels.Module.ABS, "BAW Operation", "BAW Betrieb", "Brake Assistant Warning Betrieb", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "BAW", riskLevel = 1, tags = listOf("baw", "bremsassistent", "warnung")),
        AstraJCodingModels.CodingOption("hsa_operation", AstraJCodingModels.Module.ABS, "HSA Operation", "HSA Betrieb", "Hill Start Assist Betrieb", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "HSA", riskLevel = 1, tags = listOf("hsa", "berganfahrt", "assistent")),
        AstraJCodingModels.CodingOption("sls_operation", AstraJCodingModels.Module.ABS, "SLS Operation", "SLS Betrieb", "Steering Line Lock Betrieb", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "SLS", riskLevel = 2, tags = listOf("sls", "lenkung", "sperre"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("abs_esp", "ABS & ESP Systeme", "DiscFull", opts)
}

object AstraJTCMCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("shift_schedule", AstraJCodingModels.Module.TCM, "Shift Schedule", "Schaltkennfeld", "Schaltkennfeld des Getriebes", listOf(AstraJCodingModels.CodingValue("0", "Komfort"), AstraJCodingModels.CodingValue("1", "Standard"), AstraJCodingModels.CodingValue("2", "Sport")), subcategory = "Schaltverhalten", riskLevel = 2, tags = listOf("schalten", "kennfeld", "getriebe")),
        AstraJCodingModels.CodingOption("clutch_adapt", AstraJCodingModels.Module.TCM, "Clutch Adaptation", "Kupplungsanpassung", "Kupplungsanpassungswerte", listOf(AstraJCodingModels.CodingValue("0", "Reset"), AstraJCodingModels.CodingValue("1", "Lernen")), subcategory = "Kupplung", riskLevel = 2, tags = listOf("kupplung", "anpassung", "m32", "lernen")),
        AstraJCodingModels.CodingOption("torque_convert_lock", AstraJCodingModels.Module.TCM, "Torque Converter Lock", "Drehmomentwandler-Schloss", "Drehmomentwandler-Schloss-Verhalten", listOf(AstraJCodingModels.CodingValue("0", "Früh schließen"), AstraJCodingModels.CodingValue("1", "Normal"), AstraJCodingModels.CodingValue("2", "Spät schließen")), subcategory = "Drehmomentwandler", riskLevel = 2, tags = listOf("wandler", "drehmoment", "lock", "schloss")),
        AstraJCodingModels.CodingOption("shift_rpm", AstraJCodingModels.Module.TCM, "Shift RPM", "Schaltrpm", "Drehzahl beim Schalten", listOf(AstraJCodingModels.CodingValue("0", "Niedrig"), AstraJCodingModels.CodingValue("1", "Mittel"), AstraJCodingModels.CodingValue("2", "Hoch")), subcategory = "Schaltverhalten", riskLevel = 2, tags = listOf("schalten", "drehzahl", "rpm", "getriebe")),
        AstraJCodingModels.CodingOption("launch_rpm", AstraJCodingModels.Module.TCM, "Launch RPM", "Anfahrdrehzahl", "Drehzahl beim Anfahren", listOf(AstraJCodingModels.CodingValue("0", "1500 rpm"), AstraJCodingModels.CodingValue("1", "2000 rpm"), AstraJCodingModels.CodingValue("2", "2500 rpm"), AstraJCodingModels.CodingValue("3", "3000 rpm")), subcategory = "Anfahren", riskLevel = 2, tags = listOf("anfahren", "drehzahl", "start", "rpm")),
        AstraJCodingModels.CodingOption("trans_oil_temp", AstraJCodingModels.Module.TCM, "Trans Oil Temp", "Getriebeöltemperatur", "Getriebeöltemperatur-Überwachung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Überwachung", riskLevel = 1, tags = listOf("getriebe", "oel", "temperatur", "anzeige")),
        AstraJCodingModels.CodingOption("adapt_reset", AstraJCodingModels.Module.TCM, "Adaptation Reset", "Adaptionswerte Reset", "Getriebeadaptionswerte zurücksetzen", listOf(AstraJCodingModels.CodingValue("0", "Nein"), AstraJCodingModels.CodingValue("1", "Ja")), subcategory = "Wartung", riskLevel = 2, tags = listOf("adapt", "reset", "getriebe", "lernen")),
        AstraJCodingModels.CodingOption("shift_pressure", AstraJCodingModels.Module.TCM, "Shift Pressure", "Schaltdruck", "Hydraulikdruck beim Schalten", listOf(AstraJCodingModels.CodingValue("0", "Niedrig"), AstraJCodingModels.CodingValue("1", "Mittel"), AstraJCodingModels.CodingValue("2", "Hoch")), subcategory = "Hydraulik", riskLevel = 3, tags = listOf("druck", "schalten", "hydraulik", "getriebe"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("tcm_extended", "Getriebe TCM", "Settings", opts)
}

object AstraJInfotainmentExtendedCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("display_brightness_auto", AstraJCodingModels.Module.ECU, "Display Brightness Auto", "Display-Helligkeit Auto", "Automatische Display-Helligkeit", listOf(AstraJCodingModels.CodingValue("0", "Manuell"), AstraJCodingModels.CodingValue("1", "Automatisch")), subcategory = "Display", riskLevel = 1, tags = listOf("display", "helligkeit", "automatisch")),
        AstraJCodingModels.CodingOption("display_night_mode", AstraJCodingModels.Module.ECU, "Display Night Mode", "Nachtmodus Display", "Automatischer Nachtmodus", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Display", riskLevel = 1, tags = listOf("nacht", "display", "dimmung")),
        AstraJCodingModels.CodingOption("speed_volume_comp", AstraJCodingModels.Module.ECU, "Speed Volume Compensation", "Geschwindigkeits-Lautstärke", "Automatische Lautstärkeanpassung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Niedrig"), AstraJCodingModels.CodingValue("2", "Mittel"), AstraJCodingModels.CodingValue("3", "Hoch"), AstraJCodingModels.CodingValue("4", "Max")), subcategory = "Audio", riskLevel = 1, tags = listOf("lautstaerke", "geschwindigkeit", "audio", "anpassung")),
        AstraJCodingModels.CodingOption("bass_level", AstraJCodingModels.Module.DSP, "Bass Level", "Bass", "Basspegel des Equalizers", listOf(AstraJCodingModels.CodingValue("0", "-7"), AstraJCodingModels.CodingValue("1", "-5"), AstraJCodingModels.CodingValue("2", "-3"), AstraJCodingModels.CodingValue("3", "0"), AstraJCodingModels.CodingValue("4", "+3"), AstraJCodingModels.CodingValue("5", "+5"), AstraJCodingModels.CodingValue("6", "+7")), subcategory = "Audio", riskLevel = 1, tags = listOf("bass", "equalizer", "audio")),
        AstraJCodingModels.CodingOption("treble_level", AstraJCodingModels.Module.DSP, "Treble Level", "Höhen", "Höhenpegel des Equalizers", listOf(AstraJCodingModels.CodingValue("0", "-7"), AstraJCodingModels.CodingValue("1", "-5"), AstraJCodingModels.CodingValue("2", "-3"), AstraJCodingModels.CodingValue("3", "0"), AstraJCodingModels.CodingValue("4", "+3"), AstraJCodingModels.CodingValue("5", "+5"), AstraJCodingModels.CodingValue("6", "+7")), subcategory = "Audio", riskLevel = 1, tags = listOf("treble", "hoheen", "equalizer", "audio")),
        AstraJCodingModels.CodingOption("fader_balance", AstraJCodingModels.Module.DSP, "Fader Balance", "Fader/Balance", "Verteilung vorne/hinten/l/r", listOf(AstraJCodingModels.CodingValue("0", "Vorne +5"), AstraJCodingModels.CodingValue("1", "Vorne +2"), AstraJCodingModels.CodingValue("2", "Mitte"), AstraJCodingModels.CodingValue("3", "Hinten +2"), AstraJCodingModels.CodingValue("4", "Hinten +5")), subcategory = "Audio", riskLevel = 1, tags = listOf("fader", "balance", "audio")),
        AstraJCodingModels.CodingOption("subwoofer_level", AstraJCodingModels.Module.DSP, "Subwoofer Level", "Subwoofer-Pegel", "Subwoofer-Lautstärke", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Niedrig"), AstraJCodingModels.CodingValue("2", "Mittel"), AstraJCodingModels.CodingValue("3", "Hoch")), subcategory = "Audio", riskLevel = 1, tags = listOf("subwoofer", "bass", "audio")),
        AstraJCodingModels.CodingOption("bt_autoconnect", AstraJCodingModels.Module.ECU, "BT Auto Connect", "BT Automatik-Verbindung", "Bluetooth automatisch verbinden", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Bluetooth", riskLevel = 1, tags = listOf("bluetooth", "auto", "verbindung")),
        AstraJCodingModels.CodingOption("bt_a2dp_streaming", AstraJCodingModels.Module.ECU, "BT A2DP Streaming", "BT Audio-Streaming", "Bluetooth A2DP Musikstreaming", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Bluetooth", riskLevel = 1, tags = listOf("bluetooth", "a2dp", "streaming", "audio")),
        AstraJCodingModels.CodingOption("usb_volume", AstraJCodingModels.Module.ECU, "USB Volume", "USB-Lautstärke", "Relative USB-Quell-Lautstärke", listOf(AstraJCodingModels.CodingValue("0", "-6"), AstraJCodingModels.CodingValue("1", "-3"), AstraJCodingModels.CodingValue("2", "0"), AstraJCodingModels.CodingValue("3", "+3"), AstraJCodingModels.CodingValue("4", "+6")), subcategory = "USB", riskLevel = 1, tags = listOf("usb", "lautstaerke", "audio")),
        AstraJCodingModels.CodingOption("aux_volume", AstraJCodingModels.Module.ECU, "AUX Volume", "AUX-Lautstärke", "Relative AUX-Quell-Lautstärke", listOf(AstraJCodingModels.CodingValue("0", "-6"), AstraJCodingModels.CodingValue("1", "-3"), AstraJCodingModels.CodingValue("2", "0"), AstraJCodingModels.CodingValue("3", "+3"), AstraJCodingModels.CodingValue("4", "+6")), subcategory = "AUX", riskLevel = 1, tags = listOf("aux", "lautstaerke", "audio"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("infotainment_extended", "Infotainment Erweitert", "Radio", opts)
}

object AstraJMotorExtendedCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("engine_sound_enhancement", AstraJCodingModels.Module.ECU, "Engine Sound Enhancement", "Motorsound", "Motorsound im Innenraum", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Normal"), AstraJCodingModels.CodingValue("2", "Sport")), subcategory = "Sound", riskLevel = 1, tags = listOf("sound", "motor", "innenraum")),
        AstraJCodingModels.CodingOption("active_sound_gen", AstraJCodingModels.Module.ECU, "Active Sound Generator", "Aktiver Soundgenerator", "Aktiver Soundgenerator", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Normal"), AstraJCodingModels.CodingValue("2", "Sport")), subcategory = "Sound", riskLevel = 1, tags = listOf("sound", "generator", "audio")),
        AstraJCodingModels.CodingOption("cylinder_deact", AstraJCodingModels.Module.ECU, "Cylinder Deactivation", "Zylinderabschaltung", "Zylinderabschaltung ( AFM )", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Motor", riskLevel = 2, tags = listOf("zylinder", "afs", "abschaltung", "sprit")),
        AstraJCodingModels.CodingOption("turbo_spool_tune", AstraJCodingModels.Module.ECU, "Turbo Spool Tuning", "Turbo Spool Tuning", "Turbolader-Ansprechverhalten", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Spontan"), AstraJCodingModels.CodingValue("2", "Sanft")), subcategory = "Turbo", riskLevel = 2, tags = listOf("turbo", "spool", "ladedruck", "ansprechen")),
        AstraJCodingModels.CodingOption("vvt_tuning", AstraJCodingModels.Module.ECU, "VVT Tuning", "VVT Tuning", "Nockenwellenverstellung", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Früh"), AstraJCodingModels.CodingValue("2", "Spät")), subcategory = "VVT", riskLevel = 2, tags = listOf("vvt", "nockenwelle", "steuerung")),
        AstraJCodingModels.CodingOption("rev_limit", AstraJCodingModels.Module.ECU, "Rev Limit", "Drehzahlbegrenzung", "Motordrehzahl-Begrenzung", listOf(AstraJCodingModels.CodingValue("0", "6500 rpm"), AstraJCodingModels.CodingValue("1", "6800 rpm"), AstraJCodingModels.CodingValue("2", "7000 rpm")), subcategory = "Begrenzung", riskLevel = 3, tags = listOf("drehzahl", "begrenzung", "rpm", "motor")),
        AstraJCodingModels.CodingOption("speed_limit", AstraJCodingModels.Module.ECU, "Speed Limit", "Geschwindigkeitsbegrenzung", "Fahrzeuggeschwindigkeit-Begrenzung", listOf(AstraJCodingModels.CodingValue("0", "Keine"), AstraJCodingModels.CodingValue("1", "180 km/h"), AstraJCodingModels.CodingValue("2", "200 km/h"), AstraJCodingModels.CodingValue("3", "220 km/h")), subcategory = "Begrenzung", riskLevel = 2, tags = listOf("geschwindigkeit", "begrenzung", "kmh")),
        AstraJCodingModels.CodingOption("fuel_cut_limit", AstraJCodingModels.Module.ECU, "Fuel Cut Limit", "Schubabschaltung", "Schubabschaltung Verhalten", listOf(AstraJCodingModels.CodingValue("0", "Standard"), AstraJCodingModels.CodingValue("1", "Früh"), AstraJCodingModels.CodingValue("2", "Spät")), subcategory = "Kraftstoff", riskLevel = 2, tags = listOf("schub", "abschaltung", "motor", "bremse"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("motor_extended", "Motor Erweitert", "Engineering", opts)
}

object AstraJBCMExtendedCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("lock_unlock_sound", AstraJCodingModels.Module.BCM, "Lock Unlock Sound", "Verriegelungston", "Akustische Rückmeldung Ver-/Entriegelung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Leise"), AstraJCodingModels.CodingValue("2", "Normal"), AstraJCodingModels.CodingValue("3", "Laut")), subcategory = "Akustik", riskLevel = 1, tags = listOf("akustik", "piep", "quittung", "zv")),
        AstraJCodingModels.CodingOption("unlock_sound_type", AstraJCodingModels.Module.BCM, "Unlock Sound Type", "Entriegelungston-Typ", "Art des Entriegelungstons", listOf(AstraJCodingModels.CodingValue("0", "Kurz"), AstraJCodingModels.CodingValue("1", "Doppelt"), AstraJCodingModels.CodingValue("2", "Lang")), subcategory = "Akustik", riskLevel = 1, tags = listOf("akustik", "ton", "entriegelung", "piep")),
        AstraJCodingModels.CodingOption("flash_alarm", AstraJCodingModels.Module.BCM, "Flash Alarm", "Blinken bei Alarm", "Lichter blinken bei Alarmauslösung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Alarmanlage", riskLevel = 1, tags = listOf("alarm", "blinken", "licht", "sicherheit")),
        AstraJCodingModels.CodingOption("panic_alarm", AstraJCodingModels.Module.BCM, "Panic Alarm", "Panik-Alarm", "Panik-Funktion der Fernbedienung", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Alarmanlage", riskLevel = 1, tags = listOf("panik", "alarm", "fernbedienung", "sicherheit")),
        AstraJCodingModels.CodingOption("passive_entry", AstraJCodingModels.Module.BCM, "Passive Entry", "Keyless Entry", "Keyless Entry Funktion", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Keyless", riskLevel = 1, tags = listOf("keyless", "entry", "komfort", "schluessel")),
        AstraJCodingModels.CodingOption("passive_start", AstraJCodingModels.Module.BCM, "Passive Start", "Keyless Start", "Keyless Start Funktion", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Keyless", riskLevel = 1, tags = listOf("keyless", "start", "komfort", "schluessel")),
        AstraJCodingModels.CodingOption("auto_unlock", AstraJCodingModels.Module.BCM, "Auto Unlock", "Auto-Entriegelung", "Automatische Entriegelung bei Motorstopp", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Bei Türe öffnen"), AstraJCodingModels.CodingValue("2", "Bei Zündung aus")), subcategory = "Zentralverriegelung", riskLevel = 1, tags = listOf("entriegelung", "auto", "komfort", "zv")),
        AstraJCodingModels.CodingOption("boot_unlock", AstraJCodingModels.Module.BCM, "Boot Unlock", "Kofferraum-Entriegelung", "separate Kofferraum-Entriegelung", listOf(AstraJCodingModels.CodingValue("0", "Mit ZV"), AstraJCodingModels.CodingValue("1", "Separate Taste")), subcategory = "Kofferraum", riskLevel = 1, tags = listOf("kofferraum", "entriegelung", "komfort"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("bcm_extended", "BCM Komfort Erweitert", "DirectionsCar", opts)
}

object AstraJAirbagCoding {
    private val opts = listOf(
        AstraJCodingModels.CodingOption("passenger_airbag_off", AstraJCodingModels.Module.ECU, "Passenger Airbag Off", "Beifahrer-Airbag Aus", "Beifahrer-Airbag deaktiviert", listOf(AstraJCodingModels.CodingValue("0", "Ein"), AstraJCodingModels.CodingValue("1", "Aus (Kindersitz)")), subcategory = "Airbag", riskLevel = 4, tags = listOf("airbag", "beifahrer", "kindersitz", "sicherheit"), hardwareRequired = "Nur für Kindersitze verwenden!"),
        AstraJCodingModels.CodingOption("side_airbag_off", AstraJCodingModels.Module.ECU, "Side Airbag Off", "Seiten-Airbag Aus", "Seiten-Airbags deaktiviert", listOf(AstraJCodingModels.CodingValue("0", "Ein"), AstraJCodingModels.CodingValue("1", "Aus")), subcategory = "Airbag", riskLevel = 4, tags = listOf("airbag", "seite", "sicherheit"), hardwareRequired = "Nur auf eigene Verantwortung!"),
        AstraJCodingModels.CodingOption("curtain_airbag_off", AstraJCodingModels.Module.ECU, "Curtain Airbag Off", "Vorhang-Airbag Aus", "Kopf-Airbags deaktiviert", listOf(AstraJCodingModels.CodingValue("0", "Ein"), AstraJCodingModels.CodingValue("1", "Aus")), subcategory = "Airbag", riskLevel = 4, tags = listOf("airbag", "vorhang", "kopf", "sicherheit"), hardwareRequired = "Nur auf eigene Verantwortung!"),
        AstraJCodingModels.CodingOption("airbag_warning", AstraJCodingModels.Module.IPC, "Airbag Warning", "Airbag-Warnung", "Airbag-Warnleuchte anzeigen", listOf(AstraJCodingModels.CodingValue("0", "Aus"), AstraJCodingModels.CodingValue("1", "Ein")), subcategory = "Airbag", riskLevel = 1, tags = listOf("airbag", "warnung", "leuchte", "anzeige"))
    )
    fun getCategory() = AstraJCodingModels.CodingCategory("airbag", "Airbag Systeme", "Security", opts)
}
