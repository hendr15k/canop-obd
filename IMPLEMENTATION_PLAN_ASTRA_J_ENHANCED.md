# Implementation Plan: Enhanced Astra J 1.4 Turbo Features

## Dateien - Gesamt: 14

### NEUE Dateien (6)

1. `app/src/main/java/com/canopobd/data/model/SafetyModels.kt`
2. `app/src/main/java/com/canopobd/data/model/EcoScoreModels.kt`
3. `app/src/main/java/com/canopobd/viewmodel/SafetyViewModel.kt`
4. `app/src/main/java/com/canopobd/viewmodel/EcoScoreViewModel.kt`
5. `app/src/main/java/com/canopobd/ui/safety/SafetySystemsDialog.kt`
6. `app/src/main/java/com/canopobd/ui/ecoscore/EcoScoreDialog.kt`

### ERWEITERTe bestehende Dateien (6)

7. `app/src/main/java/com/canopobd/data/model/OBDModels.kt` - +15 GM Mode 22 DIDs
8. `app/src/main/java/com/canopobd/data/model/AstraJCodingModels.kt` - +8 Hidden Features
9. `app/src/main/java/com/canopobd/ui/components/M32GearboxStatusCard.kt` - +Gear Detection
10. `app/src/main/java/com/canopobd/viewmodel/DashboardViewModel.kt` - +State+toggle
11. `app/src/main/java/com/canopobd/ui/dashboard/DashboardScreen.kt` - +Cards
12. `app/src/main/java/com/canopobd/MainActivity.kt` - +Dialog bindings

### NAVIGATION (1)

13. `app/src/main/java/com/canopobd/ui/navigation/NavRoutes.kt` - +2 Routes

### DOKUMENTATION (1)

14. `ASTRA_J_ENHANCED_FEATURES.md`

---

## Details pro Datei

### 1. SafetyModels.kt (NEU)
- `data class WheelSpeeds(fl, fr, rl, rr: Double)`
- `data class SafetySystemStatus(esp, abs, tpms, airbag, brakePadFL, brakePadFR, brakePadRL, brakePadRR)`
- `data class ChassisSensors(yawRate, lateralAccel, steeringAngle, brakePressure: Double)`
- `data class SafetyDtc(code: String, description: String, severity: DtcSeverity)`
- `enum class DtcSeverity { INFO, WARNING, CRITICAL }`
- `object AstraJSafetyThresholds` mit Referenzwerten

### 2. EcoScoreModels.kt (NEU)
- `data class EcoScoreData(overallScore, efficiencyScore, idleScore, momentumScore, cruiseScore)`
- `data class CO2Data(perTrip, perKm, cumulative)`
- `data class FuelCostData(pricePerLiter, tripCost, costPerKm, monthlyEstimate)`
- `data class RangeEstimation(estimatedKm, fuelLevelPercent, avgConsumption)`
- `data class DrivingStyleAnalysis(style: DrivingStyle, percentage: Float)`
- `enum class DrivingStyle { ECONOMICAL, NORMAL, AGGRESSIVE, SPORT }`

### 3. SafetyViewModel.kt (NEU)
- `wheelSpeeds: StateFlow<WheelSpeeds>`
- `safetyStatus: StateFlow<SafetySystemStatus>`
- `chassisSensors: StateFlow<ChassisSensors>`
- `safetyDTCs: StateFlow<List<SafetyDtc>>`
- `fun requestWheelSpeeds()` - PIDs 0x0C-0x0F via Mode 01
- `fun requestSafetyStatus()` - ABS/ESP/TMPS Status
- `fun requestChassisData()` - Yaw/Lateral/Steering

### 4. EcoScoreViewModel.kt (NEU)
- `ecoScore: StateFlow<EcoScoreData>`
- `co2Data: StateFlow<CO2Data>`
- `fuelCost: StateFlow<FuelCostData>`
- `rangeEstimation: StateFlow<RangeEstimation>`
- `drivingStyle: StateFlow<DrivingStyleAnalysis>`
- `fun updateFromOBD(maf, speed, rpm, load, coolantTemp)`
- `fun setFuelPrice(price: Double)`

### 5. SafetySystemsDialog.kt (NEU)
- Dialog mit 4 Tabs: Räder | ESP/ABS | Bremsen | Airbag
- Echtzeit-Radgeschwindigkeiten (4x数字 oder Kreisdiagramm)
- ESP/ABS Status-Anzeige (grün/rot)
- TPMS-Anzeige (Druck + Temperatur pro Reifen)
- Airbag-Status (alle Kammern)
- Bremsenverschleiß (Fortschrittsbalken pro Rad)
- Sicherheitsrelevante DTCs Liste

### 6. EcoScoreDialog.kt (NEU)
- Großer ECO Score Kreis (0-100, farbcodiert)
- CO2-Fußabdruck heute/diese Woche/diesen Monat
- Kraftstoffkosten-Rechner mit konfigurierbarem Literpreis
- Tankreichweite-Anzeige
- Fahrstil-Donut-Chart
- Optimierungsvorschläge als Liste

### 7. OBDModels.kt (ERWEITERN)
Neue OBDPID-Einträge:
```
BOOST_TARGET_MODE22("221009", "Boost Target", "kPa", 2, ...)
BOOST_ACTUAL_MODE22("221008", "Boost Actual", "kPa", 2, ...)
WASTEGATE_POSITION("22100A", "Wastegate Position", "%", 1, ...)
TURBO_RPM_MODE22("22100B", "Turbo RPM", "rpm", 2, ...)
OIL_TEMP_MODE22("22100C", "Oil Temperature", "°C", 1, ...)
VVT_INTAKE("221015", "VVT Intake", "°", 1, ...)
VVT_EXHAUST("221016", "VVT Exhaust", "°", 1, ...)
FUEL_INSTANT("221018", "Fuel Instant", "L/h", 2, ...)
FUEL_AVERAGE("22101A", "Fuel Average", "L/100km", 2, ...)
AFR_RATIO("22101F", "Air/Fuel Ratio", "", 2, ...)
O2_LAMBDA("221021", "O2 Lambda", "", 2, ...)
CATALYST_TEMP("221024", "Catalyst Temp", "°C", 2, ...)
ENGINE_TORQUE_MODE22("221001", "Engine Torque", "%", 1, ...)
REQUESTED_TORQUE("221002", "Requested Torque", "%", 1, ...)
ABSOLUTE_LOAD("22101D", "Absolute Load", "%", 2, ...)
```

### 8. AstraJCodingModels.kt (ERWEITERN)
Neue versteckte Features in Beleuchtung/Komfort:
- Needle Sweep (Baron Mode)
- ESP Sport Mode
- Geschwindigkeitswarnung
- Tagfahrlicht-Modus
- Ambient Lighting Farbe
- Regensensor Empfindlichkeit
- Rückwischer Geschwindigkeit
- Klappenspiegel bei Verriegelung

### 9. M32GearboxStatusCard.kt (ERWEITERN)
- `currentGear: Int` Parameter hinzufügen
- `fun detectGear(rpm, speed): Int` Algorithmus
- Ganganzeige im Card (große数字)
- Shift-Light Indikator im Card
- RPM/Speed Verhältnis-Balkendiagramm

### 10. DashboardViewModel.kt (ERWEITERN)
- `_showSafetySystems` StateFlow
- `_showEcoScore` StateFlow
- `toggleSafetySystems()`, `toggleEcoScore()` Funktionen
- Safety + ECO State-Delegationen

### 11. DashboardScreen.kt (ERWEITERN)
- Safety Systems Card im Dashboard (wenn verbunden)
- ECO Score Card im Dashboard
- Toggle-Buttons für neue Dialoge

### 12. MainActivity.kt (ERWEITERN)
- State-Bindungen für showSafetySystems/showEcoScore
- Dialog-Aufrufe

### 13. NavRoutes.kt (ERWEITERN)
- `data object SafetySystems : NavRoute("safety_systems")`
- `data object EcoScore : NavRoute("eco_score")`

### 14. ASTRA_J_ENHANCED_FEATURES.md (NEU)
- Dokumentation aller neuen Features
- OBD PID Referenz
- Technische Algorithmen
- Usage-Hinweise

---

## Ausführungsreihenfolge (optimiert für parallele Subagenten)

### Phase 1 (parallel): Models
- Subagent A: SafetyModels.kt
- Subagent B: EcoScoreModels.kt

### Phase 2 (parallel): ViewModels
- Subagent C: SafetyViewModel.kt (nutzt SafetyModels)
- Subagent D: EcoScoreViewModel.kt (nutzt EcoScoreModels)

### Phase 3 (parallel): UI
- Subagent E: SafetySystemsDialog.kt
- Subagent F: EcoScoreDialog.kt

### Phase 4 (parallel): Enhancements
- Subagent G: OBDModels.kt +15 DIDs
- Subagent H: AstraJCodingModels.kt +8 Features
- Subagent I: M32GearboxStatusCard.kt +Gear Detection

### Phase 5 (sequential): Integration
- Subagent J: DashboardViewModel.kt +State
- Subagent K: DashboardScreen.kt +Cards
- Subagent L: MainActivity.kt +Bindings
- Subagent M: NavRoutes.kt +Routes

### Phase 6: Build + Doku
- Build-Test
- ASTRA_J_ENHANCED_FEATURES.md
