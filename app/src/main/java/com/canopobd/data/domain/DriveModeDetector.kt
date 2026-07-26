package com.canopobd.data.domain

/**
 * Erkannte Fahrmodi fuer Opel Fahrzeuge (Astra J 1.4 Turbo / A14NET)
 *
 * Basierend auf OBD-II Standard-PIDs:
 * - PID 0x04: Engine Load (berechnete Motorlast)
 * - PID 0x0C: Engine RPM (Motordrehzahl)
 * - PID 0x0D: Vehicle Speed (Fahrzeuggeschwindigkeit)
 * - PID 0x11: Throttle Position (Gaspedalposition)
 * - PID 0x49: Accelerator Pedal Position D
 * - PID 0x4C: Commanded Throttle Actuator
 *
 * Erkennungslogik basierend auf Fahrverhalten:
 * - ECO: Niedrige Last, sanftes Gasgeben, moderates RPM
 * - NORMAL: Ausgewogene Fahrweise, moderater Throttle-Response
 * - SPORT: Hohe Drehzahl, aggressives Gasgeben, direktes Ansprechverhalten
 */
enum class DriveMode {
    ECO, // Spritsparmodus - sanftes Fahren, optimale Verbrauchszone
    NORMAL, // Normalfahrt - ausgewogenes Fahrverhalten
    SPORT // Sportfahrt - hohe Last, aggressive Beschleunigung
}

object DriveModeDetector {

    fun detectMode(
        throttle: Double,
        rpm: Double,
        speed: Double,
        engineLoad: Double,
        acceleratorPedalD: Double,
        throttleActuator: Double
    ): DriveMode {
        // Berechne Throttle-Response-Ratio
        // Dies ist der Schluessel zur Erkennung von Fahrmodi:
        // - Ein reduziertes Verhaeltnis (throttleActuator / acceleratorPedalD) deutet auf ECO-Modus hin
        //   da die Fahrzeugelektronik die Gasansprache zurueckschraubt
        // - Ein 1:1-Verhaeltnis oder sogar Uebersteuerung deutet auf SPORT-Modus hin
        val throttleResponse = if (acceleratorPedalD > 1.0) {
            throttleActuator / acceleratorPedalD
        } else {
            1.0
        }

        return when {
            // ECO: Niedrige Last, sanftes Gasgeben
            // Opel A14NET typische ECO-Werte:
            // - RPM unter 3000 (Optimalbereich 1500-3000 fuer Verbrauch)
            // - Throttle unter 30% (sanftes Antreten)
            // - Engine Load unter 35% (keine hohe Leistungsabforderung)
            // - Gedrosselter Throttle-Response (< 0.7) - ECO drosselt aktiv die Ansprache
            speed > 30 && engineLoad < 35 && throttle < 25 && throttleResponse < 0.7
            -> DriveMode.ECO

            // SPORT: Hohe Drehzahl oder aggressive Throttle
            // Opel A14NET typische SPORT-Werte:
            // - RPM > 4000 (Leistungsbereich ueber dem optimalen ECO-Fenster)
            // - Kombination aus RPM > 3500 UND Throttle > 60% (sportliche Beschleunigung)
            // - Throttle-Response > 0.95 - Volle oder uebersteuerte Ansprache (Sportmodus)
            rpm > 4000 || (rpm > 3500 && throttle > 60) || throttleResponse > 0.95
            -> DriveMode.SPORT

            // NORMAL: Standardfall fuer alle anderen Faelle
            // Typische Werte: RPM 1500-3500, Throttle 25-60%, moderate Last
            else -> DriveMode.NORMAL
        }
    }

    /**
     * Prueft ob das Fahrzeug gerade rollt (Coasting / Treiben)
     */
    fun isCoasting(speed: Double, throttle: Double, rpm: Double): Boolean {
        return speed > 20 && throttle < 5 && rpm > 1000
    }
}
