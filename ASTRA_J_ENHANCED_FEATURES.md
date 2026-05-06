# Opel Astra J 1.4 Turbo (A14NEL) - Erweiterte OBD-Funktionen

## Technische Dokumentation für canop-obd Android App

**Fahrzeug:** Opel Astra J 2012 1.4L Turbo (A14NEL / LUJ)  
**Leistung:** 103 kW / 140 PS  
**Drehmoment:** 200 Nm  
**Getriebe:** Getrag M32 6-Gang Schaltgetriebe  
**Letztes Update:** Mai 2026

---

## 1. Übersicht

Dieses Dokument beschreibt alle erweiterten Funktionen, die für den Opel Astra J 1.4 Turbo in der canop-obd App implementiert wurden. Die neuen Module erweitern die Standard-OBD-II-Funktionalität um herstellerspezifische Diagnose- und Analysefunktionen.

### Implementierte Module

| Modul | Datei | Beschreibung |
|-------|-------|-------------|
| Sicherheitssysteme | `ui/safety/SafetySystemsDialog.kt` | ABS/ESP/TPMS/Airbag/Bremsen |
| ECO-Analyse | `ui/ecoscore/EcoScoreDialog.kt` | ECO-Score/Kosten/CO2-Analyse |
| Getriebeüberwachung | `ui/components/M32GearboxStatusCard.kt` | M32-Gangerkennung |
| Versteckte Features | `data/model/AstraJCodingModels.kt` | Werkscodierungen |

---

## 2. Sicherheitssysteme Modul

**Datei:** `ui/safety/SafetySystemsDialog.kt`  
**Datenmodelle:** `data/model/SafetyModels.kt`

### 2.1 Features

Das Sicherheitssystem-Modul bietet eine umfassende Überwachung aller sicherheitsrelevanten Systeme des Fahrzeugs:

#### Radgeschwindigkeiten (Tab 1)
- **Einzelradüberwachung:** Vorne Links (VL), Vorne Rechts (VR), Hinten Links (HL), Hinten Rechts (HR)
- **Achsdifferenz-Berechnung:** Vorne und Hinten separat
- **Maximale Differenz:** Gesamtgeschwindigkeitsdifferenz aller Räder
- **Farbcodierung:**
  - Grün: Differenz < 5 km/h (normal)
  - Gelb: Differenz 5-10 km/h (Warnung)
  - Rot: Differenz > 10 km/h (Kritisch)

#### ESP/ABS-Status (Tab 2)
- **ABS-Status:** Antiblockiersystem
- **ESP-Status:** Elektronische Stabilitätskontrolle
- **Traktionskontrolle:** Antriebsschlupfregelung
- **Hill Start Assist:** Berganfahrhilfe
- **Chassis-Sensoren:**
  - Gierrate (Yaw Rate) in °/s
  - Querbeschleunigung in g
  - Lenkwinkel in °
  - Bremsdruck in bar

#### Bremsenverschleiß (Tab 3)
- **Bremsbelag-Verschleiß:** Vorne Links/Rechts, Hinten Links/Rechts (0-100%)
- **Restkilometerschätzung:** Basierend auf aktuellem Verschleiß
- **Bremsflüssigkeitsstand:** Normal/Niedrig/Kritisch/Unbekannt
- **Farbanzeige:**
  - Grün: > 50% verbleibend
  - Gelb: 30-50% verbleibend
  - Rot: < 30% verbleibend

#### Airbag-Status (Tab 4)
- **Frontairbags:** Fahrer und Beifahrer
- **Seitenairbags:** Fahrer und Beifahrer
- **Kopfairbags (Gardinen):** Links und Rechts
- **Knieairbag:** Fahrer
- **Gurtstraffer:** Fahrer und Beifahrer
- **Systemstatus:** Bereit/Störung

### 2.2 Datenquellen

Die Sicherheitsdaten stammen aus folgenden Quellen:
- **OBD-II Mode 01:** Geschwindigkeitssensoren (PID 0D)
- **OBD-II Mode 09:** Fahrzeug-Identifikation
- **Herstellerspezifisch:** ABS/ESP-Steuergerät (proprietäre PIDs)
- **CAN-Bus:** Chassis-Sensoren (Gierrate, Beschleunigung)

### 2.3 Schwellenwerte (AstraJSafetyThresholds)

```kotlin
// Bremsenverschleiß
BRAKE_WEAR_WARNING = 30      // Prozent
BRAKE_WEAR_CRITICAL = 15     // Prozent

// Reifendruck (PSI)
TPMS_LOW_PRESSURE_PSI = 28.0
TPMS_CRITICAL_PSI = 24.0
TPMS_HIGH_PRESSURE_PSI = 42.0
TPMS_DEFAULT_PSI = 32.0

// Radgeschwindigkeiten
WHEEL_SPEED_DIFF_WARNING = 5.0    // km/h
WHEEL_SPEED_DIFF_CRITICAL = 10.0  // km/h

// Chassis
YAW_RATE_WARNING = 30.0           // °/s
LATERAL_ACCEL_WARNING = 0.8       // g
STEERING_ANGLE_MAX = 720.0        // °
```

### 2.4 DTC-Zuordnungen (Fehlercodes)

| System | Präfix | Beispiele |
|--------|--------|-----------|
| ABS | C- | C0035, C0040, C0110 |
| ESP | C- | C0235, C0236, C0241 |
| Airbag | B- | B0016, B0020, B0050 |
| TPMS | C- | C0750, C0755, C0760 |

---

## 3. ECO-Score Modul

**Datei:** `ui/ecoscore/EcoScoreDialog.kt`  
**Datenmodelle:** `data/model/EcoScoreModels.kt`

### 3.1 Features

#### ECO-Score (0-100)
- **Gesamtpunktzahl:** Animierte Kreisanzeige (0-100)
- **Teilwertungen:**
  - Effizienz
  - Sanftes Fahren
  - Gleitverhalten
  - Trägheit (Schubbetrieb)

#### CO2-Fußabdruck
- **Fahrt-CO2:** In kg und lb
- **Pro km:** Gramm CO2 pro Kilometer
- **Jährliche Schätzung:** Basierend auf Fahrprofil
- **Baum-Äquivalent:** Wieviele Bäume zur Kompensation nötig wären

#### Kraftstoffkosten
- **Konfigurierbarer Preis:** EUR/Liter
- **Fahrtkosten:** Aktuelle Fahrtkosten
- **Kosten pro km:** Durchschnittspreis
- **Monatlich/Jährlich:** Projizierte Kosten

#### Reichweite
- **Geschätzte Reichweite:** Basierend auf aktuellem Verbrauch
- **Best Case:** Minimaler Verbrauch angenommen
- **Worst Case:** Maximaler Verbrauch angenommen
- **Tankfüllstand:** In Prozent und Litern

#### Fahrstil-Analyse
- **Stil-Kategorien:**
  - Ökonomisch (grün)
  - Normal (gelb)
  - Aggressiv (orange)
  - Sportlich (rot)
- **Teilwertungen:**
  - Beschleunigen
  - Bremsen
  - Gleitverhalten
  - Antizipation

#### Optimierungstipps
- **Priorität:** HOCH/MITTEL/NIEDRIG
- **Auswirkung:** Kraftstoff/Reifen/Bremsen/Motor/Sicherheit
- **Sparpotenzial:** Geschätzte Ersparnis in Prozent

### 3.2 ECO-Score Berechnungsformel

```
Gesamtpunktzahl = (Effizienz × 0.35) + (Sanftes Fahren × 0.25) + 
                  (Gleitverhalten × 0.25) + (Trägheit × 0.15)
```

#### Notenvergabe

| Score | Note | Bedeutung |
|-------|------|-----------|
| 95-100 | A+ | Hervorragend |
| 90-94 | A | Ausgezeichnet |
| 85-89 | A- | Sehr gut |
| 80-84 | B+ | Gut |
| 75-79 | B | Befriedigend |
| 70-74 | B- | Akzeptabel |
| 65-69 | C+ | Durchschnittlich |
| 60-64 | C | Ausreichend |
| 55-59 | C- | Schwach |
| 50-54 | D+ | Mangelhaft |
| 45-49 | D | Mangelhaft |
| 40-44 | D- | Ungenügend |
| 0-39 | F | Unzureichend |

### 3.3 CO2-Berechnung

```
CO2_kg = Kraftstoffverbrauch_L × 2.31 kg/L

Baumäquivalent = CO2_kg_jährlich / 22 kg/Jahr
```

### 3.4 Schwellenwerte (AstraJEcoThresholds)

```kotlin
// Verbrauchsziele (L/100km)
TARGET_CITY = 8.0
TARGET_HIGHWAY = 5.5
TARGET_COMBINED = 6.5

// ECO-Score
ECO_EXCELLENT = 85
ECO_GOOD = 70
ECO_AVERAGE = 55

// Geschwindigkeit
OPTIMAL_CRUISE_SPEED_KMH = 90.0
MAX_ECO_SPEED_KMH = 120.0

// Drehzahl
OPTIMAL_RPM = 2000
ECO_UPSHIFT_RPM = 2500
```

---

## 4. GM Mode 22 Erweiterte PIDs

**Datei:** `data/model/OBDModels.kt`

Mode 22 (Service $22) ist ein herstellerspezifischer Diagnosemodus von General Motors/Opel, der erweiterte Datenidentifikatoren (DIDs) für fahrzeugspezifische Informationen bereitstellt.

### 4.1 Implementierte DIDs

| PID | Name | Format | Einheit | Formel |
|-----|------|--------|---------|--------|
| 221001 | ENGINE_TORQUE_MODE22 | 1 Byte | % | (byte - 128) |
| 221002 | REQUESTED_TORQUE_MODE22 | 1 Byte | % | (byte - 128) |
| 221008 | BOOST_PRESSURE_ACTUAL_MODE22 | 2 Bytes | kPa | (A×256 + B) |
| 221009 | BOOST_PRESSURE_TARGET_MODE22 | 2 Bytes | kPa | (A×256 + B) |
| 22100A | WASTEGATE_POSITION_MODE22 | 1 Byte | % | byte |
| 22100B | TURBO_RPM_MODE22 | 2 Bytes | rpm | (A×256 + B) |
| 22100C | OIL_TEMP_MODE22 | 1 Byte | °C | (byte - 40) |
| 22100D | COOLANT_TEMP_MODE22 | 1 Byte | °C | (byte - 40) |
| 22100E | INTAKE_AIR_TEMP_MODE22 | 1 Byte | °C | (byte - 40) |
| 22100F | FUEL_RAIL_PRESSURE_MODE22 | 2 Bytes | kPa | (A×256 + B) × 10 |
| 221010 | INJECTOR_PULSE_WIDTH | 2 Bytes | ms | (A×256 + B) / 100 |
| 221015 | VVT_INTAKE_MODE22 | 1 Byte | ° | (byte - 128) |
| 221016 | VVT_EXHAUST_MODE22 | 1 Byte | ° | (byte - 128) |
| 221018 | FUEL_CONSUMPTION_INSTANT | 2 Bytes | L/h | (A×256 + B) |
| 22101A | FUEL_CONSUMPTION_AVERAGE | 2 Bytes | L/100km | (A×256 + B) / 10 |
| 22101F | AFR_RATIO_MODE22 | 2 Bytes | λ | 2 × (A×256 + B) / 65536 |

### 4.2 Beispiel: Abfrage von Mode 22 DIDs

```
Anfrage: 22 10 08        (BOOST_PRESSURE_ACTUAL)
Antwort: 62 10 08 XX YY   (XX YY = Druckwerte)
```

### 4.3 Hinweise zur Nutzung

- Nicht alle ELM327-Adapter unterstützen Mode 22 vollständig
- Die ECU muss den jeweiligen DID unterstützen
- Einige DIDs erfordern eine aktive Motorlaufbedingung
- Antwortzeiten können länger sein als bei Standard-PIDs

---

## 5. Gangerkennung (Getrag M32)

**Datei:** `ui/components/M32GearboxStatusCard.kt`

### 5.1 Getriebedaten

Das Getrag M32 ist ein 6-Gang-Schaltgetriebe, das im Astra J 1.4 Turbo verbaut wird.

### 5.2 Gangübersetzung

| Gang | Untersetzung | Beschreibung |
|------|--------------|--------------|
| 1. Gang | 3.192 | Kriechgang |
| 2. Gang | 1.938 | Anfahrgang |
| 3. Gang | 1.357 | Stadtgang |
| 4. Gang | 1.034 | Überlandgang |
| 5. Gang | 0.825 | Schnellgang |
| 6. Gang | 0.693 | Schleichgang |
| Rückwärtsgang | ~3.250 | Rückwärtsgang |
| **Achsübersetzung** | **4.056** | Final Drive |

### 5.3 Reifendaten (205/55 R16)

```
Reifenumfang = 2 × π × (Felgendurchmesser/2 + Reifenhöhe)
            = 2 × π × (0.318m + 0.113m)
            ≈ 1.995 m
```

### 5.4 Gangerkennungs-Algorithmus

```kotlin
private fun detectM32Gear(rpm: Double, speedKmh: Double): Int {
    // Konstanten
    val tireCircumferenceM = 1.995f
    val tolerance = 0.15f  // 15% Toleranz
    val finalDrive = 4.056f
    val ratios = floatArrayOf(3.192f, 1.938f, 1.357f, 1.034f, 0.825f, 0.693f)
    
    // Mindestbedingungen
    if (speedKmh < 3.0 || rpm < 600) return 0  // Stand/Leerlauf
    
    // Geschwindigkeit in m/s
    val speedMs = speedKmh / 3.6f
    
    // Für jeden Gang: theoretische Drehzahl berechnen
    for (i in ratios.indices) {
        val overallRatio = ratios[i] * finalDrive
        val theoreticalRpm = speedMs * overallRatio * 60f / tireCircumferenceM
        val rpmDifference = abs(rpm - theoreticalRpm) / theoreticalRpm
        
        if (rpmDifference < tolerance) {
            return i + 1  // Gang gefunden
        }
    }
    
    return 0  // Kein Gang erkannt (Kupplung gedrückt)
}
```

### 5.5 Formel für theoretische Drehzahl

```
RPM_theoretisch = (Geschwindigkeit_m/s × Untersetzung × Achsübersetzung × 60) / Reifenumfang_m

Beispiel (4. Gang bei 100 km/h):
RPM = (27.78 m/s × 1.034 × 4.056 × 60) / 1.995 m
RPM = 3,637 rpm
```

### 5.6 Toleranzbereich

Die 15%-Toleranz berücksichtigt:
- Reifenabnutzung (< 2mm Differenz)
- Schlupf im Getriebe
- Messungenauigkeiten des Tachometers
- Reifendruckabweichungen

---

## 6. Versteckte Features Coding

**Datei:** `data/model/AstraJCodingModels.kt`  
**Objekt:** `AstraJVersteckteFeaturesCoding`

### 6.1 Übersicht

Dieses Modul enthält 8 versteckte Funktionen, die im Astra J über das IPC (Instrument Panel Cluster) oder BCM (Body Control Module) aktiviert werden können.

### 6.2 Implementierte Features

#### 1. Needle Sweep (Nadelsweep / Baron Mode)
- **Kanal:** Gauge Sweep
- **Modul:** IPC (0x83)
- **Beschreibung:** Beim Einschalten der Zündung fahren alle Nadeln über den gesamten Bereich und wieder zurück
- **Werte:** 0 = Deaktiviert, 1 = Aktiviert

#### 2. ESP Sport-Modus
- **Kanal:** ESP Sport Mode
- **Modul:** CIM (0x7E)
- **Beschreibung:** ESP mit reduzierter Eingriffsschwelle im Sport-Modus
- **Werte:**
  - 0 = Standard (immer aktiv)
  - 1 = Sport (reduzierte Eingriffsschwelle)
  - 2 = ESC Off (nur für Profis)

#### 3. Geschwindigkeitswarnung
- **Kanal:** Speed Warning
- **Modul:** IPC (0x83)
- **Beschreibung:** Akustische Warnung bei Überschreitung einer einstellbaren Geschwindigkeit
- **Werte:** 0 = Aus, 1 = 120 km/h, 2 = 140 km/h, 3 = 160 km/h, 4 = 180 km/h, 5 = 200 km/h, 6 = 220 km/h, 7 = 250 km/h

#### 4. Ambientebeleuchtung Farbe
- **Kanal:** Ambient Lighting Color
- **Modul:** BCM (0xFF)
- **Beschreibung:** Farbe der Innenbeleuchtung (Ambiente-Licht)
- **Werte:**
  - 0 = Blau (Standard)
  - 1 = Weiß
  - 2 = Rot
  - 3 = Grün
  - 4 = Orange
  - 5 = Lila
- **Hardware:** Ambientebeleuchtung muss verbaut sein

#### 5. Regensensor Empfindlichkeit
- **Kanal:** Rain Sensor Sensitivity
- **Modul:** BCM (0xFF)
- **Beschreibung:** Empfindlichkeit des Regensensors für automatische Scheibenwischer
- **Werte:** 0 = Niedrig, 1 = Mittel, 2 = Hoch, 3 = Sehr hoch

#### 6. Heckwischer Geschwindigkeit
- **Kanal:** Rear Wiper Speed
- **Modul:** BCM (0xFF)
- **Beschreibung:** Intervall und Tempo des Heckwischers anpassen
- **Werte:**
  - 0 = Langsam
  - 1 = Mittel
  - 2 = Schnell
  - 3 = Intervall langsam
  - 4 = Intervall schnell

#### 7. Spiegel einklappen bei Verriegelung
- **Kanal:** Mirror Fold on Lock
- **Modul:** BCM (0xFF)
- **Beschreibung:** Seitenspiegel klappen automatisch ein, wenn das Fahrzeug verriegelt wird
- **Werte:** 0 = Deaktiviert, 1 = Aktiviert
- **Hardware:** Elektrisch anklappbare Spiegel erforderlich

#### 8. Tagfahrlicht-Modus (DRL)
- **Kanal:** DRL Mode
- **Modul:** UEC (0x09)
- **Beschreibung:** Modus des Tagfahrlichts
- **Werte:**
  - 0 = Deaktiviert
  - 1 = Scheinwerfer (niedrige Leistung)
  - 2 = Separate LED-Leiste
  - 3 = Nebelscheinwerfer

### 6.3 Coding-Kategorien

Alle Versteckten Features sind in `AstraJVersteckteFeaturesCoding` organisiert:

```kotlin
object AstraJVersteckteFeaturesCoding {
    fun getCategory() = CodingCategory(
        id = "hidden-features",
        displayName = "Versteckte Features",
        icon = "Build",
        options = hiddenOptions  // Liste der 8 Features
    )
}
```

---

## 7. Datenmodelle Übersicht

### 7.1 SafetyModels.kt

| Modell | Beschreibung |
|--------|--------------|
| `WheelSpeeds` | Geschwindigkeit aller 4 Räder |
| `SafetySystemStatus` | Status aller Sicherheitssysteme |
| `ChassisSensors` | Gierrate, Beschleunigung, Lenkwinkel |
| `BrakeWear` | Bremsbelag-Verschleiß |
| `TPMSData` | Reifendruck und -temperatur |
| `AirbagStatus` | Alle Airbag-Komponenten |
| `ESPState` | ESP-Aktivität und Modus |
| `SafetyDtc` | Fehlercodes mit Schweregrad |
| `SafetySummary` | Aggregierte Übersicht |

### 7.2 EcoScoreModels.kt

| Modell | Beschreibung |
|--------|--------------|
| `EcoScoreData` | ECO-Score mit Teilwertungen |
| `CO2Data` | CO2-Emissionen und Äquivalente |
| `FuelCostData` | Kraftstoffkosten |
| `RangeEstimation` | Reichweite |
| `FuelEfficiencyMetrics` | Verbrauchsmetriken |
| `DrivingStyleAnalysis` | Fahrverhalten |
| `EcoTip` | Optimierungsvorschläge |

---

## 8. Technische Architektur

### 8.1 ViewModel-Integration

```
DashboardViewModel
    ├── SafetyViewModel
    │   ├── SafetySummary
    │   ├── WheelSpeeds
    │   ├── SafetySystemStatus
    │   └── AirbagStatus
    └── EcoScoreViewModel
        ├── EcoScoreData
        ├── CO2Data
        ├── FuelCostData
        └── DrivingStyleAnalysis
```

### 8.2 Datenfluss

```
OBD-Adapter → ELM327 → Bluetooth → App
                                    │
                                    ▼
                            OBDRepository
                                    │
                                    ▼
                        ┌───────────┴───────────┐
                        ▼                       ▼
                SafetyDataSource         EcoDataSource
                        │                       │
                        ▼                       ▼
                SafetyViewModel         EcoScoreViewModel
                        │                       │
                        └───────────┬───────────┘
                                    ▼
                            SafetySystemsDialog
                            EcoScoreDialog
```

---

## 9. Nutzungsbeispiele

### 9.1 Sicherheitssysteme aufrufen

```kotlin
// Im Dashboard:
val safetySummary = remember { mutableStateOf(SafetySummary()) }

// Dialog anzeigen:
SafetySystemsDialog(
    safetySummary = safetySummary.value,
    onDismiss = { /* Dialog schließen */ }
)
```

### 9.2 ECO-Score abrufen

```kotlin
// ECO-Daten:
val ecoScore = EcoScoreData(
    overallScore = 85,
    efficiencyScore = 90,
    smoothnessScore = 80,
    cruisingScore = 75,
    momentumScore = 95,
    grade = "B+"
)

// Dialog anzeigen:
EcoScoreDialog(
    ecoScore = ecoScore,
    co2Data = CO2Data(...),
    fuelCost = FuelCostData(...),
    rangeEstimation = RangeEstimation(...),
    efficiency = FuelEfficiencyMetrics(...),
    drivingStyle = DrivingStyleAnalysis(...),
    tips = listOf(...),
    onDismiss = { /* Dialog schließen */ },
    onSetFuelPrice = { price -> /* Preis aktualisieren */ }
)
```

### 9.3 Gangerkennung

```kotlin
M32GearboxStatusCard(
    currentRpm = 3500.0,
    vehicleSpeedKmh = 100.0,
    gearboxTempCelsius = 85.0,
    healthScore = 95,
    lastFluidChangeKm = 45000,
    currentKm = 52000
)
```

---

## 10. Einschränkungen

### 10.1 Sicherheitssysteme

- **DTCs (Fehlercodes):** Erfordern OP-COM oder GM-spezifischen Adapter für vollständigen Zugriff
- **Direkte TPMS-Daten:** Möglicherweise nicht auf allen Varianten verfügbar
- **Airbag-Zugriff:** Eingeschränkter Zugriff über Standard-OBD
- **Bremsbelag-Verschleiß:** Nur Schätzung basierend auf Fahrverhalten

### 10.2 Mode 22 DIDs

- **ELM327-Kompatibilität:** Nicht alle Adapter unterstützen Mode 22 vollständig
- **ECU-Unterstützung:** Alle DIDs müssen von der ECU unterstützt werden
- **Timing:** Mode 22 kann langsamer sein als Standard-PIDs
- **Proprietäre Daten:** Interpretation erfordert GM-spezifisches Wissen

### 10.3 Gangerkennung

- **Toleranz:** 15% Toleranz kann bei unterschiedlichen Reifengrößen zu Fehlern führen
- **Kupplungspunkt:** Erkennt nicht, ob Kupplung gedrückt ist (nur "unbekannter Gang")
- **Rückwärtsgang:** Nur Schätzung, nicht offiziell dokumentiert
- **Kaltstart:** Getriebeöl-Viskosität kann Erkennung beeinflussen

### 10.4 Versteckte Features

- **CarPass:** Einige Codierungen erfordern den Security Code (CarPass)
- **Hardware:** Bestimmte Features erfordern zusätzliche Hardware
- **Garantie:** Änderungen können die Garantie beeinflussen
- **Fehler:** Falsche Codierungen können zu Fehlfunktionen führen

---

## 11. Empfohlene Diagnosetools

| Tool | Verwendung |
|------|------------|
| OP-COM | Vollständiger GM/Opel-Diagnosezugang |
| Tech2 | Werkstattlevel-Diagnose |
| ELM327 (Original) | Basis-OBD-II mit Mode 22 |
| VX-Diag | Alternative für OP-COM |
| Canop-obd App | Erweiterte Überwachung und Analyse |

---

## Anhang A: Glossar

| Abkürzung | Vollständiger Name |
|-----------|-------------------|
| ABS | Antiblockiersystem |
| AFR | Air-Fuel Ratio (Luft-Kraftstoff-Verhältnis) |
| BCM | Body Control Module |
| CAN | Controller Area Network |
| CIM | Column Integration Module |
| DID | Data Identifier |
| DRL | Daytime Running Light |
| DTC | Diagnostic Trouble Code |
| ECU | Engine Control Unit |
| ESP | Elektronisches Stabilitätsprogramm |
| IPC | Instrument Panel Cluster |
| OBD | On-Board Diagnostics |
| PID | Parameter ID |
| TC | Traction Control |
| TPMS | Tire Pressure Monitoring System |
| VVT | Variable Valve Timing |
| WOT | Wide Open Throttle |

---

## Anhang B: Referenzen

- **GM GDS2:** Offizielle GM-Diagnosedatenbank
- **Opel Service Manual:** Werkstatthandbuch Astra J
- **OBD-II Spec:** SAE J1979 Standard
- **Mode 22 Spec:** GM GMW8769
- **Getrag M32 Tech Data:** Getrag Getriebedatenblatt

---

*Dieses Dokument wurde für canop-obd v1.0+ erstellt.*
*Stand: Mai 2026*
