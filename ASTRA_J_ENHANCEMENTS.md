# Opel Astra J 1.4 Turbo (A14NET) - Feature-Übersicht

## Technische Daten (Wikipedia-basiert)

| Parameter | Wert |
|-----------|------|
| **Motor** | GM Family 0 Gen III, 1364cc R4 Turbo |
| **Leistung** | 103 kW (140 PS) @ 4900-6000 rpm |
| **Drehmoment** | 200 Nm @ 1850-4900 rpm |
| **Overboost** | 220 Nm (10 Sek.) |
| **Turbo** | BorgWarner KP39, Single-Scroll, Fixed-Geometry |
| **Ladedruck** | 0.7 bar normal, 1.3 bar Overboost |
| **ECU** | Bosch ME17.9.24 |
| **Getriebe** | 6-Gang Schaltgetriebe (Getrag M32) |

---

## Neue Features (Sprint 8)

### 1. OBD PIDs erweitert

| PID | Name | Einheit | Status |
|-----|------|---------|--------|
| 0167 | Turbo Oil Pressure | kPa | ✅ |
| 016A | Wastegate Position B | % | ✅ |
| 016B | Turbo Boost B | kPa | ✅ |
| 016C | VGT Position | % | ✅ (nicht vorhanden beim A14NET) |
| 016D | Turbo Water Cool | % | ✅ |
| 016E | Compressor Inlet Temp | °C | ✅ |
| 016F | Compressor Outlet Temp | °C | ✅ |
| 0176 | Turbine Inlet Temp (EGT) | °C | ✅ |
| 017A | Turbine Outlet Temp | °C | ✅ |

### 2. DTC-Datenbank (500+ Codes)

Enthält alle relevanten Fehlercodes für:
- **MAF-Sensor** (P0100-P0103, P1100-P1101)
- **Kettenspanner** (P0340-P0344, P0016-P0019, P1345)
- **Turbo/Ladedruck** (P0234-P0299, P1240-P1255, P2261-P2263)
- **Wastegate** (P0243-P0250, P1658-P1659)
- **Lambda/O2** (P0130-P0141, P2195-P2198)
- **Kraftstoff** (P0087-P0094, P0171-P0175)
- **Getriebe** (P0700-P0799, P2800-P2809)
- **ABS/ESP** (C0035-C0300)
- **Airbag** (B0001-B0031)

Alle Codes mit deutschen Beschreibungen.

### 3. Mode 22 OBD-II Support

| Mode 22 PID | Beschreibung |
|-------------|-------------|
| 22F190 | VIN auslesen |
| 22F151 | ECU Software Version |
| 22F156 | ECU Hardware Version |
| 220001-22000A | Turbo-Monitoring (Boost, Wastegate, Temps) |
| 221001-221005 | Kraftstoff-System |
| 225001-225003 | Lambda Wideband |
| 223001-223004 | Temperaturen |

### 4. UI-Komponenten

| Datei | Beschreibung |
|-------|-------------|
| `TurboHealthCard.kt` | Turbo-Gesundheitsanzeige mit Boost/Wastegate/Temps |
| `FuelTrimDialog.kt` | Kraftstoff-Trim-Analyse mit STFT/LTFT |
| `DriveModeIndicator.kt` | Fahrmodus-Anzeige (ECO/NORMAL/SPORT) |
| `ShiftRecommendation.kt` | Schaltpunkt-Empfehlung |
| `MaintenanceDialog.kt` | Erweiterte Wartungsverwaltung |

### 5. Domain-Logik

| Datei | Beschreibung |
|-------|-------------|
| `DriveModeDetector.kt` | Fahrmodus-Erkennung aus OBD-Daten |
| `FuelTrimAnalyzer.kt` | STFT/LTFT-Diagnose |
| `WastegateHealthAnalyzer.kt` | Wastegate-Gesundheitsdiagnose |
| `SensorValidator.kt` | Sensor-Validierung |
| `SensorHealthMonitor.kt` | Sensor-Gesundheitsüberwachung |
| `MaintenanceReminder.kt` | Wartungserinnerungen |
| `MaintenanceService.kt` | Wartungsverwaltung |

### 6. Kalibrierung (AstraJ14TurboCalibration)

| Parameter | Wert |
|-----------|------|
| Redline | 6500 RPM |
| RPM Warning | 5850 RPM |
| Leerlauf | 750 RPM |
| Max Torque | 200 Nm @ 3000 RPM |
| Overboost Torque | 220 Nm |
| Normal Boost | 0.7 bar |
| Overboost Max | 1.3 bar |
| Max EGT | 850°C |
| Max Oil Temp | 120°C |
| Optimal Oil Temp | 90-110°C |
| Optimal RPM | 1500-3000 |
| Power Band | 5000-5500 RPM |

---

## Bekannte Probleme (A14NET)

| Problem | DTCs | Typisch ab |
|---------|------|-----------|
| Kettenspanner | P0340, P0016, P1345 | 80.000-150.000 km |
| MAF-Sensor | P0100-P0103 | 60.000-120.000 km |
| Wastegate | P0234, P0299 | 80.000-150.000 km |
| PCV-Ventil | P1100, P1101 | 60.000-100.000 km |
| Kühlmittel-Sensor | P0116, P0117 | 80.000-150.000 km |
| Zündkerzen | P0300-P0304 | 30.000-60.000 km |

---

## Wartungsintervalle

| Bauteil | Intervall |
|---------|-----------|
| Ölwechsel (Dexos2 5W-30) | 15.000 km |
| Luftfilter | 30.000 km |
| Zündkerzen | 60.000 km |
| Kühlmittel | 80.000 km |
| Turbo-Inspektion | 60.000 km |
| Timing-Kette | 150.000 km |
