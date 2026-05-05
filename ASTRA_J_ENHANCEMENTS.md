# Opel Astra J 1.4 Turbo (A14NET) - Erweiterte Dokumentation & Feature-Integration

## Dokumentationsübersicht

| Dokument | Beschreibung |
|----------|-------------|
| [ASTRA_J_CALIBRATION.md](./ASTRA_J_CALIBRATION.md) | Vollständige Kalibrierungswerte, technische Daten, Wartungsintervalle, Live-Data-Referenzen |
| [ASTRA_J_DTC_CODES.md](./ASTRA_J_DTC_CODES.md) | 500+ DTC-Fehlercodes mit deutschen Beschreibungen, Schweregraden und A14NET-Hinweisen |
| [ASTRA_J_LIVE_DATA_REFERENCE.md](./ASTRA_J_LIVE_DATA_REFERENCE.md) | Live-Data-Referenzwerte für Leerlauf, Vollast und Autobahn |
| [ASTRA_J_TROUBLESHOOTING_GUIDE.md](./ASTRA_J_TROUBLESHOOTING_GUIDE.md) | Problemlösung, DTC-Interpretation, DIY-Diagnose |
| [KNOWN_ISSUES.md](./KNOWN_ISSUES.md) | Bekannte Probleme, Kosten, Prävention, Zeitlinien |
| **FEATURES_RESEARCH.md** | Marktanalyse: OBD2-App-Features, Tuning-Potenzial, Sensor-Integration |
| **FEATURE_IMPLEMENTATION_GUIDE.md** | Implementierungs-Roadmap für neue Features |

---

## Motor-Grunddaten (A14NET - ECOTEC Gen III)

| Parameter | Wert | Quelle |
|-----------|------|--------|
| **Motorkennung** | A14NET (Europa) / LUJ (US) | Opel Werkstattdaten |
| **Motor** | GM Family 1 Gen III, 1364cc R4 Turbo | Wikipedia: GM Family 1 engine |
| **Leistung** | 103 kW (140 PS) @ 4900–6000 rpm | Opel Spezifikation |
| **Drehmoment** | 200 Nm @ 1850–4900 rpm | Opel Spezifikation |
| **Overboost** | 220 Nm (max. 10 Sek.) | Opel/GM Technik |
| **Turbo** | BorgWarner KP39, Single-Scroll, Fixed-Geometry | BorgWarner |
| **Ladedruck** | 0,7 bar normal, 1,3 bar Overboost | Bosch ME17.9.22 |
| **ECU** | Bosch ME17.9.22 / Delco E78 | GM Diagnose-Tools |
| **Getriebe** | 6-Gang Schaltgetriebe (Getrag M32) | Opel |
| **Kraftstoff** | Benzin (95 ROZ minimum, 98 empfohlen) | Opel Handbuch |

---

## OBD2-Protokoll-Informationen

### Unterstützte Protokolle (ISO 15765-4 CAN)

| Protokoll | Geschwindigkeit | Pins | Verwendung |
|-----------|----------------|------|-----------|
| **ISO 15765-4 CAN (500kbit/s)** | 500 kbit/s | 6/14 (High/Low) | Motor-ECU (ECM) |
| **GMLAN High-Speed** | 500 kbit/s | Intern | Fahrzeug-Netzwerk |
| **KWP2000** | 10.4 kbit/s | Optional | Erweiterte Diagnose |
| **UDS (Unified Diagnostic Services)** | 500 kbit/s | 6/14 | Herstellerspezifisch |

### Steuergeräte (ECU-Architektur)

| Steuergerät | Funktion | OBD2-Zugriff |
|-------------|---------|--------------|
| **ECM/PCM** | Motorsteuerung (Bosch ME17.9.22) | ✅ Standard OBD2 |
| **TCM** | Getriebesteuerung (6-Gang) | ⚠️ Herstellerspezifisch |
| **ABS/ESP** | Bremsen & Stabilität (Bosch ESP 9) | ⚠️ Herstellerspezifisch |
| **SRS** | Airbag-Steuergerät | ❌ Getrennter Bus |
| **BCM** | Karosseriesteuergerät | ❌ Nicht über OBD2 |
| **IC** | Instrumentencluster | ⚠️ Herstellerspezifisch |
| **TPMS** | Reifendruckkontrolle | ⚠️ Herstellerspezifisch |

---

## Tuning-Stufen und Leistungsdaten

Basierend auf Recherche und Erfahrungsberichten (MOTOR-TALK, Chiptuningforum.com):

| Stufe | Leistung | Drehmoment | Boost | Methode | Kosten (ca.) |
|-------|----------|------------|-------|---------|--------------|
| **Serie** | 140 PS | 200 Nm (220 Overboost) | 0,7 bar | - | - |
| **Stage 1** | 165–175 PS | 240–260 Nm | 0,8–0,9 bar | ECU Remap, OBD-Tuning | 500–800 € |
| **Stage 2** | 180–195 PS | 270–290 Nm | 0,95–1,0 bar | + Ansaugung, Auspuff | 1.500–2.500 € |
| **Stage 3** | 200–210 PS | 300+ Nm | 1,2–1,3 bar | + Intercooler, Kraftstoff | 3.000–5.000 € |
| **OPC-Motor** | 280 PS | 400 Nm | - | 2.0L Turbo A20NFT Swap | 8.000+ € |

### OBD-Tuning (Stage 1) - Besonderheiten A14NET

- **Ladedruck-Anhebung:** +0,15–0,25 bar
- **Overboost-Aktivierung:** dauerhaft 220 Nm oder mehr
- **Zündwinkel-Optimierung:** je nach Kraftstoffqualität
- **Drehmomentbegrenzung:** Anhebung von 200 auf 250–300 Nm
- **Empfohlener Tuner:** Spezialisierte Werkstätten mit GM-Family-1-Erfahrung

---

## Wartungsintervalle (A14NET-spezifisch)

### Standard-Bedingungen (Stadt: 50%, Autobahn: 50%)
| Bauteil | Standard | Getunt (Stage 1+) | Höchstlast |
|---------|----------|---------|------------|
| **Ölwechsel (Dexos2 5W-30)** | 15.000 km / 12 Mon. | 10.000 km / 8 Mon. | 10.000 km / 8 Mon. |
| **Luftfilter** | 30.000 km / 24 Mon. | 20.000 km / 16 Mon. | 15.000 km / 12 Mon. |
| **Zündkerzen (NGK LZKR6AP-11G)** | 60.000 km / 48 Mon. | 40.000 km / 32 Mon. | 30.000 km / 24 Mon. |
| **Kühlmittel** | 80.000 km / 60 Mon. | 60.000 km / 48 Mon. | 60.000 km / 48 Mon. |
| **Turbo-Inspektion** | 60.000 km / 48 Mon. | 45.000 km / 36 Mon. | 40.000 km / 32 Mon. |
| **Timing-Kette** | 150.000 km / 120 Mon. | 100.000 km / 80 Mon. | 80.000 km / 60 Mon. |
| **PCV-Ventil** | 60.000 km | 40.000 km | 40.000 km |
| **Intercooler** | Prüfung @ 80.000 km | Prüfung @ 60.000 km | Prüfung @ 50.000 km |
| **M32 Getriebeöl (Dexron VI ATF)** | 60.000 km (empfohlen) | 60.000 km | 40.000 km |

### PCV-System (Crankcase-Ventilation) - A14NET Spezifisch

**Problem:** PCV-Ventil verschmutzt, führt zu:
- Erhöhter Druck im Motorkofferraum
- Ölverlust durch Ölabscheider
- Ladeluft-Unterdruck-Probleme

**Prüfung:**
- Ölstand prüfen (bei defektem PCV: Ölverbrauch +0,5–1,0 l/1000 km)
- PCV-Ventil prüfen (pfeift bei Defekt)
- Unterdruckschlauch auf Risse prüfen

### Tuning-Implikationen für Wartung

| Tuning-Stufe | Ölwechsel | PCV-Prüfung | Zündkerzen | Ölqualität |
|--------------|-----------|-------------|------------|------------|
| Serie | 15.000 km | 60.000 km | 60.000 km | Dexos2 5W-30 |
| Stage 1 | 10.000 km | 40.000 km | 40.000 km | Dexos2 5W-30 / 0W-40 |
| Stage 2 | 8.000 km | 30.000 km | 30.000 km | Dexos2 0W-40 |
| Stage 3 | 6.000 km | 20.000 km | 25.000 km | Rennöl 5W-50 |

---

## Typische Probleme nach Kilometerstand

### Hauptprobleme A14NET (Recherche: MOTOR-TALK, Astra-J-Foren)

| Km-Bereich | Problem | DTCs | Schweregrad | Kosten (ca.) |
|------------|---------|------|-------------|--------------|
| **30.000–60.000** | Zündkerzen-Verschleiß | P0300–P0304 | 🟡 | 80–150 € |
| **60.000–80.000** | MAF-Sensor verschmutzt | P0100–P0103 | 🟡 | 120–200 € |
| **60.000–100.000** | PCV-Ventil | P1100/P1101 | 🟡 | 50–100 € |
| **80.000–150.000** | Kettenspanner schwach | P0340, P0341, P1345 | 🔴 | 800–1500 € |
| **80.000–150.000** | Kühlmittel-Sensor | P0116, P0117 | 🟡 | 30–60 € |
| **80.000–150.000** | Wastegate-Stellglied | P0234, P0299 | 🔴 | 500–1200 € |
| **100.000+** | Turbo-Inspektion fällig | P1241, P1253 | 🟠 | variabel |
| **120.000–150.000** | Timing-Kette gestreckt | P0016–P0019 | 🔴 | 1500–3000 € |
| **150.000+** | Turbo-Überholung | P0234/P0299 | 🔴 | 2000–4000 € |

### Timing-Kette (A14NET Hauptproblem)

**Symptome:**
- Kaltstart-Rasseln (1–3 Sekunden)
- P0016, P0017, P0018, P0019 DTCs
- Leichte Leistungseinbuße

**Ursache:** Kettenspanner nutzt sich ab (öl Druck-betrieben)

**Prüfung:**
1. Kaltstart-Startgeräusch dokumentieren
2. Öldruck bei Leerlauf prüfen (>1.0 bar)
3. Mode 22 PID 0x220005 (Turbo-Drehzahl) analysieren

**Prävention:**
- Regelmäßige Ölwechsel (nie über 15.000 km)
- Hochwertiges Dexos2 5W-30 verwenden
- Öldruck-Sensor überwachen

---

## Sensorschwellen

| Parameter | Optimal | Warnung | Kritisch |
|-----------|---------|---------|----------|
| Kühlmittel | 80–105 °C | ≥ 99,75 °C | ≥ 105 °C |
| Öltemperatur | 90–110 °C | ≥ 108 °C | ≥ 120 °C |
| Ladedruck | 0,7 bar | 0,85 bar | 1,0 bar |
| EGT | 400–700 °C | ≥ 765 °C | ≥ 850 °C |
| Ladelufttemperatur | 20–45 °C | ≥ 58,5 °C | ≥ 65 °C |

---

## DTC-Kategorien

| Kategorie | Codes | Beschreibung |
|-----------|-------|-------------|
| P01xx | 50+ | Kraftstoff & Luft (MAF, Lambda, Temperatur) |
| P02xx | 80+ | Einspritzung & Turbo (Ladedruck, Wastegate) |
| P03xx | 40+ | Zündung & Sensorik (Nockenwelle, Kurbelwelle) |
| P04xx | 30+ | Abgas (EGR, Katalysator, EVAP) |
| P05xx | 20+ | Geschwindigkeit & Leerlauf |
| P06xx | 15+ | Steuergerät & Kommunikation |
| P07xx | 30+ | Getriebe |
| P1xxx | 150+ | Herstellerspezifisch (GM/Opel) |
| C0xxx | 20+ | ABS/ESP Fahrwerk |
| B0xxx | 15+ | Airbag & Beleuchtung |
| U0xxx | 10+ | CAN-Bus Kommunikation |

---

## Mode 22 PIDs (Bosch ME17.9.22)

### Identifikation & Fahrzeuginfo
| PID | Beschreibung | Anmerkung |
|-----|-------------|-----------|
| 22F190 | VIN auslesen | 17-stellig |
| 22F151 | ECU Software Version | CAL ID |
| 22F156 | ECU Hardware Version | Teilenummer |
| 22F15A | ECU Hardware Version | Alternative |

### Motor & Drehmoment
| PID | Beschreibung | Typischer Bereich |
|-----|-------------|------------------|
| 220001 | Motordrehmoment (Nm) | 0–250 Nm |
| 220002 | Turbo Boost Ist (kPa) | 0–250 kPa |
| 220003 | Turbo Boost Soll (kPa) | 0–250 kPa |
| 220004 | Wastegate Duty-Cycle (%) | 0–100% |
| 220005 | Turbo-Drehzahl (RPM) | 0–200.000 rpm |
| 220006 | Turbo Einlauf-Temperatur (°C) | -40–250 °C |
| 220007 | Turbo Auslauf-Temperatur (°C) | -40–250 °C |
| 220008 | Ladelufttemperatur (°C) | -40–150 °C |

### Kraftstoffsystem
| PID | Beschreibung | Typischer Bereich |
|-----|-------------|------------------|
| 221001 | Kraftstoffrail-Druck (kPa) | 0–200.000 kPa |
| 221002 | Kraftstofftemperatur (°C) | -40–150 °C |
| 221003 | Kraftstoffdruck (bar) | 3–12 bar |

### Lambda & Abgas
| PID | Beschreibung | Typischer Bereich |
|-----|-------------|------------------|
| 225001 | Lambda Wideband B1 (λ) | 0.65–1.35 |
| 225002 | Lambda Wideband B1 S2 (λ) | 0.65–1.35 |

### Öl & Kühlung
| PID | Beschreibung | Typischer Bereich |
|-----|-------------|------------------|
| 223001 | Umgebungstemperatur (°C) | -40–80 °C |
| 223002 | Motoröltemperatur (°C) | -40–150 °C |
| 223003 | Motoröldruck (kPa) | 0–600 kPa |

### Drehmoment-Management
| PID | Beschreibung | Anmerkung |
|-----|-------------|-----------|
| 226001 | Drehmoment-Verhältnis | -125 bis +130% |
| 226002 | Drehmoment-Differenz | Soll vs. Ist |
| 226003 | VVT Status | Nockenwellenstellung |

---

## Erweiterte OBD-Features für Astra J

### Bereits implementiert ✅
- Turbo Boost Monitoring mit Health Score
- Wastegate Health Monitoring
- Intercooler Efficiency
- Oil Temperature/Pressure/Life
- Battery Health
- EGT Monitoring (Mode 22)
- Fuel Trim Analysis
- Lambda/O2 Sensors
- Readiness Monitors
- DTC Reading with Freeze Frame
- Trip Computer
- Drive Score
- Dashboard Customization
- Data Logging
- HUD Mode
- Widget
- Shift Light
- Gear Recommendations
- Maintenance Reminders
- Mode 22 Extended PIDs
- EGR/EVAP Monitoring
- Timing Chain Monitor
- Cold Start Analysis
- Drive Style Analysis

### Noch nicht implementiert (Priorität)
| Priorität | Feature | Komplexität | Nutzen |
|-----------|---------|-------------|--------|
| **1** | Turbo Spool-Up Timer | Mittel | Hoch |
| **1** | 0-100 Acceleration Timer | Mittel | Hoch |
| **2** | Boost Leak Detection | Hoch | Hoch |
| **2** | Predictive Maintenance ML | Hoch | Hoch |
| **3** | Compare Before/After | Mittel | Mittel |
| **4** | Enhanced Data Export | Niedrig | Mittel |

---

## App-Empfehlungen (Marktanalyse)

Basierend auf Recherche der Konkurrenz-Apps (Torque Pro, Car Scanner, EOBD Facile):

### Top-Apps für Opel Astra J
| App | Plattform | Stärken |
|-----|----------|---------|
| **Torque Pro** | Android | Custom PIDs, Dashboard, Dyno |
| **Car Scanner ELM OBD2** | Android/iOS | Opel/Vauxhall Profile, Custom PIDs |
| **EOBD Facile** | Android/iOS | Fehlercode-Datenbank, Opel-Support |

### Empfohlene OBD2-Adapter
- **OBDLink MX+** (Bluetooth) - Schnellste CAN-Kommunikation
- **Vgate iCar Pro 4** (Bluetooth/WiFi) - Gutes Preis-Leistung
- **OBDLink LX** (Bluetooth) - Lange Lebensdauer

### Wichtige OBD-Parameter für Astra J
```
Empfohlene Dashboard-PIDs:
- Motordrehzahl (0x0C)
- Ladedruck (Mode 22 0x220002)
- Ladedruck Soll (Mode 22 0x220003)
- Wastegate Duty (Mode 22 0x220004)
- Kühlmitteltemperatur (0x05)
- Drosselklappe (0x11)
- MAF Rate (0x10)
- Lambda (Mode 22 0x225001)
- EGT (Mode 22 0x220006/0x220007)
- Öltemperatur (0x5C)
- Kraftstoffrail-Druck (Mode 22 0x221001)
```

---

## Schweregrade

| Symbol | Grad | Beschreibung |
|--------|------|-------------|
| 🔴 | KRITISCH | Sofortige Handlung - Motorschaden möglich |
| 🟠 | LEISTUNG | Limp Mode aktiv - eingeschränkte Leistung |
| 🟡 | WARNUNG | Baldige Reparatur erforderlich |
| 🔵 | INFO | Information - kann vernachlässigt werden |
