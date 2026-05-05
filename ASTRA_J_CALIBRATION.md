# Opel Astra J 1.4 Turbo (A14NET) - Kalibrierung & Technische Daten

> Umfassende Referenzdokumentation für den Bosch ME17.9.22 / Delco E78 kalibrierten 1.4L Turbo-Motor.

---

## 1. Motor-Grunddaten

| Parameter | Wert |
|-----------|------|
| **Motorcode (Opel)** | A14NET |
| **Motorcode (GM)** | LUJ |
| **Motorgenereration** | GM Family 0 Gen III |
| **Hubraum** | 1364 cm³ (1.4L) |
| **Zylinder** | 4 Reihen (R4) |
| **Bohrung x Hub** | 72,5 mm x 82,6 mm |
| **Verdichtung** | 9,5:1 |
| **Ventilsteuerung** | DOHC 16V, DCVCP Nockenwellen |
| **VVT-System** | DCVCP (Dual Continuous Variable Cam Phasing) |
| **Emissionsklasse** | Euro 5 |
| **Kraftstoff** | Benzin (mind. 95 ROZ / 98 ROZ empfohlen) |

---

## 2. Leistungsdaten

| Parameter | Wert |
|-----------|------|
| **Maximale Leistung** | 103 kW (140 PS) @ 4900–6000 U/min |
| **Maximales Drehmoment** | 200 Nm @ 1850–4900 U/min |
| **Overboost-Drehmoment** | 220 Nm (max. 10 Sek.) |
| **Overboost-Druck** | 1,3 bar max |
| **Drehzahlbegrenzung** | 6500 U/min |
| **Leerlaufdrehzahl** | 750 U/min |
| **Spitzenleistung bei** | 5500 U/min (Peak Power Band) |
| **Drehmomentplateau** | 1500–3000 U/min (optimaler Bereich) |
| **Leistungsbereich** | 5000–5500 U/min |
| **Max. Dauer-Drehzahl** | 6000 U/min |
| **Drehzahlwarnung** | 5850 U/min |

---

## 3. Turbolader: BorgWarner KP39

| Parameter | Wert |
|-----------|------|
| **Hersteller** | BorgWarner |
| **Modell** | KP39 |
| **Typ** | Single-Scroll, Fixed-Geometry |
| **Ladedruck-Regelung** | Pneumatisch (Wastegate) |
| **Normaler Ladedruck** | 0,7 bar (70 kPa) |
| **Overboost-Maximum** | 1,3 bar (130 kPa) |
| **Overboost-Dauer** | max. 10 Sekunden |
| **Max. Turbo-Drehzahl** | 200.000 U/min |
| **Ladedrück-Ziel** | 0,7 bar @ Normalbetrieb |
| **Wastegate-Stellglied** | Pneumatisch, Magnetventil-geregelt |
| **Wastegate-Ziel-Duty-Cycle** | 45% @ Normalbetrieb |
| **Intercooler-Effizienz-Ziel** | 85% |

### Ladedruck-Verhalten

| Zustand | Ladedruck | Wastegate Duty |
|---------|-----------|----------------|
| Leerlauf | 0 bar | ~80–95% (offen) |
| Normalbetrieb | 0,5–0,7 bar | 45–70% |
| Vollast | 0,7–1,0 bar | 30–50% |
| Overboost | 1,0–1,3 bar | 15–30% |
| Unterladung (Fehler) | < 0,3 bar | Variabel |
| Überladung (Fehler) | > 1,0 bar | < 5% |

---

## 4. Getriebe

| Parameter | Wert |
|-----------|------|
| **Getriebe** | 6-Gang Schaltgetriebe |
| **Hersteller** | Getrag |
| **Modell** | M32 |
| **Getriebeöl** | Dexron VI ATF |

### Schaltpunkt-Empfehlung (A14NET)

| Gang | Optimaler Schaltpunkt (RPM) | Max. RPM |
|------|-----------------------------|----------|
| 1→2 | 3500–4000 | 5000 |
| 2→3 | 3500–4000 | 5000 |
| 3→4 | 3500–4000 | 5500 |
| 4→5 | 3000–3500 | 5500 |
| 5→6 | 2800–3200 | 5000 |

---

## 5. Abgas- und Verbrauchswerte

| Parameter | Wert |
|-----------|------|
| **Kraftstoffverbrauch (kombiniert)** | 6,0 L/100 km |
| **Kraftstoffverbrauch (Stadt)** | 7,8 L/100 km |
| **Kraftstoffverbrauch (Außerorts)** | 5,0 L/100 km |
| **Kraftstoffverbrauch (Sport)** | 8,5 L/100 km |
| **Kraftstoffverbrauch (Eco)** | 5,5 L/100 km |
| **CO₂-Emissionen** | 139 g/km |
| **Tankvolumen** | 56 L |
| **Höchstgeschwindigkeit** | 207 km/h |
| **0–100 km/h** | 9,9 Sekunden |

---

## 6. Kalibrierungswerte (Sensoren & Schwellen)

### 6.1 Temperatur-Schwellen

| Sensor | Optimal | Warnung | Kritisch | Max. gültig |
|--------|---------|---------|----------|-------------|
| **Kühlmittel** | 80–105 °C | ≥ 99,75 °C (95%) | ≥ 105 °C | 105 °C |
| **Öltemperatur** | 90–110 °C | ≥ 108 °C (90%) | ≥ 120 °C | 120 °C |
| **Ladelufttemperatur** | 20–45 °C | ≥ 58,5 °C (90%) | ≥ 65 °C | 65 °C |
| **Abgastemperatur (EGT)** | 400–700 °C | ≥ 765 °C (90%) | ≥ 850 °C | 850 °C |

### 6.2 Drehzahl-Schwellen

| Parameter | Wert |
|-----------|------|
| Leerlauf | 750 U/min |
| Optimaler Bereich | 1500–3000 U/min |
| RPM-Warnung | 5850 U/min |
| Rotmarkierung | 6000–6500 U/min |
| Drehzahlbegrenzung | 6500 U/min |

### 6.3 Ladedruck-Schwellen

| Parameter | Wert |
|-----------|------|
| Leerlauf (Wastegate offen) | 0 bar |
| Normaler Ladedruck | 0,7 bar |
| Warnschwelle (85% von max) | 0,85 bar |
| Overboost-Schwelle | 1,2 bar |
| Max. Ladedruck | 1,0 bar |
| Overboost-Maximum | 1,3 bar |

### 6.4 Ölpressure-Schwellen

| Zustand | Mindestpressure |
|---------|----------------|
| Leerlauf (< 1500 U/min) | ≥ 1,0 bar |
| Betrieb (> 1500 U/min) | ≥ 2,0 bar |
| Normal | 2,5 bar |

### 6.5 Kraftstoff-Trim

| Status | STFT + LTFT Gesamt |
|--------|-------------------|
| NORMAL | -10% bis +10% |
| WARNUNG | +10% bis +15% oder -10% bis -15% |
| MAGER (LEAN) | > +15% |
| FETT (RICH) | < -15% |

### 6.6 Luftmassenmesser (MAF)

| Zustand | MAF-Wert |
|---------|----------|
| Leerlauf (normal) | 2,0–5,0 g/s |
| Allgemein gültig | 2,0–90,0 g/s |
| Außerhalb → Fehler | < 2,0 g/s oder > 90,0 g/s |

### 6.7 Batterie & Elektrik

| Parameter | Wert |
|-----------|------|
| Batterie | 70 Ah |
| Ladespannung (normal) | 14,0 V |
| Niedrig-Spannungswarnung | 11,8 V |

---

## 7. Wartungsintervalle

| Bauteil | Standard | Schwerlast | Autobahn |
|---------|----------|------------|----------|
| **Ölwechsel** (Dexos2 5W-30, 4,5L) | 15.000 km / 12 Mon. | 10.000 km / 8 Mon. | 18.000 km / 14 Mon. |
| **Luftfilter** | 30.000 km / 24 Mon. | 15.000 km / 12 Mon. | 40.000 km / 36 Mon. |
| **Zündkerzen** (NGK LZKR6AP-11G) | 60.000 km / 48 Mon. | 30.000 km / 24 Mon. | 70.000 km / 60 Mon. |
| **Kühlmittel** (5,7L Dex-Cool) | 80.000 km / 60 Mon. | 60.000 km / 48 Mon. | 80.000 km / 72 Mon. |
| **Turbo-Inspektion** (KP39) | 60.000 km / 48 Mon. | 45.000 km / 36 Mon. | 80.000 km / 72 Mon. |
| **Timing-Kette Prüfung** | 75.000 km / 60 Mon. | 60.000 km / 48 Mon. | 100.000 km / 84 Mon. |
| **Getriebeöl** (Dexron VI ATF) | 80.000 km / 60 Mon. | 60.000 km / 48 Mon. | 100.000 km / 72 Mon. |
| **Bremsbeläge** (vorne/hinten) | 30.000 km / 24 Mon. | 20.000 km / 18 Mon. | 40.000 km / 36 Mon. |
| **Reifenrotation** | 15.000 km / 12 Mon. | 10.000 km / 8 Mon. | 20.000 km / 15 Mon. |
| **HU/AU (TÜV)** | 60.000 km / 24 Mon. | 60.000 km / 24 Mon. | 60.000 km / 24 Mon. |

### Zündkerzen-Details

| Parameter | Wert |
|-----------|------|
| Typ (Hersteller) | NGK LZKR6AP-11G |
| Alternativ | Bosch FR7HPP332 |
| Elektrodenabstand | 0,7 mm |
| Anzugsmoment | 20–25 Nm |

---

## 8. Problem-Zonen nach Kilometerstand

| Km-Bereich | Typische Probleme | Priorität |
|------------|-------------------|-----------|
| **0–30.000** | Normaler Verschleiß, keine typischen Probleme | — |
| **30.000–60.000** | Zündkerzen-Verschleiß (P0300–P0304) | Niedrig |
| **60.000–80.000** | MAF-Sensor (P0100–P0103), PCV-Ventil (P1100/P1101) | Mittel |
| **80.000–100.000** | Kettenspanner-Rattern (P0340/P0341/P1345), Kühlmittelsensor (P0116/P0117) | Hoch |
| **100.000–120.000** | Wastegate-Stellglied (P0234/P0299), Ladedruckverlust | Hoch |
| **120.000–150.000** | Timing-Kette Verschleiß (P0016–P0019), Turbo-Inspektion | Sehr hoch |
| **150.000+** | Kettentausch empfohlen, Generalüberholung prüfen | Kritisch |

---

## 9. Live-Data Referenzwerte

### 9.1 Leerlauf (Motor warm, 750 U/min, 0 km/h)

| Parameter | Normaler Bereich | Anmerkung |
|-----------|-----------------|-----------|
| Drehzahl | 750 ± 50 U/min | Leichtes Schwanken normal |
| Kühlmitteltemperatur | 80–105 °C | Ziel: 90 °C |
| Öltemperatur | 90–110 °C | Erst nach 10–15 Min. |
| Ladedruck | 0 bar (Atmosphärisch) | Wastegate offen |
| Wastegate Duty | 80–95% | WG steht offen |
| Drosselklappe | 0–5% | Fast geschlossen |
| Motorlast | 15–25% | Leerlauf-Kompensation |
| MAF | 2,0–5,0 g/s | Typisch ~3 g/s |
| Zündzeitpunkt | 5–15° | Variabel |
| Lambdawert (Bank 1) | 0,1–0,9 V (schwingt) | Sauerstoff-Sensor |
| Kraftstofftrim (STFT) | -5% bis +5% | Kurzzeit-Trim |
| Kraftstofftrim (LTFT) | -5% bis +5% | Langzeit-Trim |
| Batteriespannung | 13,8–14,5 V | Ladespannung |

### 9.2 Vollast (100% Drosselklappe, 3000–5500 U/min)

| Parameter | Normaler Bereich | Anmerkung |
|-----------|-----------------|-----------|
| Drehzahl | 3000–5500 U/min | Je nach Gang |
| Ladedruck (soll) | 0,7 bar | Normaler Modus |
| Ladedruck (ist) | 0,5–0,7 bar | Abhängig von Last |
| Overboost-Ladedruck | bis 1,3 bar | Max. 10 Sek. |
| Wastegate Duty | 30–50% | WG schließt sich |
| Drosselklappe | 90–100% | Voll geöffnet |
| Motorlast | 80–100% | Max. Auslastung |
| MAF | 30–90 g/s | Je nach Drehzahl |
| Kraftstoffrail-Druck | 40–120 bar | Direkteinspritzung |
| EGT (Bank 1) | 600–800 °C | Unter 850 °C halten |
| Zündzeitpunkt | 15–25° | Lastabhängig |
| Lambdawert | 0,85–0,95 V (fast konstant) | Schichtbetrieb |

### 9.3 Autobahn (120 km/h, 6. Gang, ~2800 U/min)

| Parameter | Normaler Bereich | Anmerkung |
|-----------|-----------------|-----------|
| Drehzahl | 2500–3200 U/min | 6. Gang |
| Fahrzeuggeschwindigkeit | 100–140 km/h | Autobahn |
| Ladedruck | 0,3–0,5 bar | Teillast |
| Drosselklappe | 20–40% | Je nach Steigung |
| Motorlast | 30–50% | Mittlere Auslastung |
| Kühlmitteltemperatur | 85–100 °C | Stabil warm |
| Öltemperatur | 95–105 °C | Normal |
| Kraftstoffverbrauch | 5,0–7,0 L/100 km | Realfahrzeug |
| MAF | 15–30 g/s | Abhängig von Last |
| EGT | 500–650 °C | Teillast |

---

## 10. Fahrmodi

| Modus | Charakteristik | Kraftstoffverbrauch |
|-------|---------------|-------------------|
| **ECO** | Gedrosselte Drosselklappen-Ansprechzeit, frühes Schalten | 5,5 L/100 km |
| **NORMAL** | Standard-Kalibrierung | 6,0 L/100 km |
| **SPORT** | Volle Leistung, spätes Schalten | 8,5 L/100 km |

---

## 11. ECU-Informationen

| Parameter | Wert |
|-----------|------|
| **ECU-Typ** | Bosch ME17.9.22 / Delco E78 |
| **Kommunikation** | CAN-Bus (ISO 15765-4) |
| **OBD-II Protokoll** | ISO 15765-4 (CAN) |
| **Diagnosemodus** | Mode 01 (Standard) + Mode 22 (Herstellerspezifisch) |
| **Spannungsversorgung** | 12V (Batterie) |
| **Batterie** | 70 Ah |
| **Ladespannung** | 14,0 V |

---

## 12. Mode 22 PIDs (Herstellerspezifisch)

| PID | Beschreibung | Einheit |
|-----|-------------|---------|
| 22F190 | VIN (Fahrzeug-Ident.-Nr.) | ASCII |
| 22F151 | ECU Software-Version | ASCII |
| 22F156 | ECU Hardware-Version | ASCII |
| 220001 | Motordrehmoment | Nm |
| 220002 | Turbo-Ladedruck (Ist) | kPa |
| 220003 | Turbo-Ladedruck (Soll) | kPa |
| 220004 | Wastegate Duty-Cycle | % |
| 220005 | Turbo-Drehzahl | RPM |
| 220006 | Turbo-Einlauf-Temperatur | °C |
| 220007 | Turbo-Auslauf-Temperatur | °C |
| 220008 | Ladelufttemperatur | °C |
| 220009 | VGT-Stellung | % |
| 22000A | Turbo-Effizienz | % |
| 221001 | Kraftstoffrail-Druck | kPa |
| 221002 | Kraftstofftemperatur | °C |
| 221003 | Kraftstoffdruck | kPa |
| 221004 | Einspritzmenge | mg/hub |
| 221005 | Einspritzzeitpunkt | ° |
| 222001–222004 | Katalysatortemperaturen | °C |
| 223001 | Umgebungstemperatur | °C |
| 223002 | Motöoltemperatur | °C |
| 223003 | Motöldruck | kPa |
| 223004 | Getriebeöltemperatur | °C |
| 225001–225003 | Lambda (Wideband) | λ |

---

## 13. Kraftstoff-System

| Parameter | Wert |
|-----------|------|
| **Einspritzung** | Direkteinspritzung (SIDI) |
| **Mind. Kraftstoffqualität** | 95 ROZ |
| **Empfohlene Kraftstoffqualität** | 98 ROZ |
| **Kraftstofffilter** | Integriert in Kraftstoffpumpe |
| **Rail-Druck (idle)** | 40–50 bar |
| **Rail-Druck (Last)** | 80–120 bar |

---

## 14. Kühlsystem

| Parameter | Wert |
|-----------|------|
| **Kühlmittelkapazität** | 5,7 L |
| **Kühlmittelspezifikation** | Dex-Cool (GM OAT) |
| **Thermostat öffnet bei** | ~82 °C |
| **Zieltemperatur** | 90 °C |
| **Ventilator-Stufe 1** | ~97 °C |
| **Ventilator-Stufe 2** | ~103 °C |
| **Max. Temperatur** | 105 °C |

---

## 15. Ölsystem

| Parameter | Wert |
|-----------|------|
| **Ölkapazität** | 4,5 Liter (inkl. Filter) |
| **Ölspezifikation** | Dexos2 5W-30 |
| **Alternative Öle** | ACEA C3 5W-30 / A3/B4 5W-40 |
| **Öldruck Leerlauf** | ≥ 1,0 bar |
| **Öldruck Betrieb** | ≥ 2,0 bar (typisch 2,5 bar) |
| **Zieltemperatur** | 90–110 °C |
| **Max. Temperatur** | 120 °C |

---

## 16. Zündsystem

| Parameter | Wert |
|-----------|------|
| **Zündkerzen-Typ** | NGK LZKR6AP-11G |
| **Alternative** | Bosch FR7HPP332 |
| **Elektrodenabstand** | 0,7 mm |
| **Anzugsmoment** | 20–25 Nm |
| **Zündfolge** | 1-3-4-2 |
| **Zündspulen** | Einzelzündspulen (COP) |

---

## 17. Fahrzeugdaten

| Parameter | Wert |
|-----------|------|
| **Fahrzeug** | Opel Astra J (2010–2015) |
| **Karosserievarianten** | Schrägheck, Kombi (Sports Tourer), Coupé (GTC) |
| **Antrieb** | Frontantrieb |
| **Radaufhängung vorn** | MacPherson-Federbein |
| **Radaufhängung hinten** | Verbundlenkerachse |
| **Lenkung** | Zahnstangenlenkung, elektrohydraulisch |

---

## 18. BCM/UEC/REC Codierungs-Referenz

### Steuergeräte-Module

| Modul | Adresse | Hauptfunktion |
|-------|---------|---------------|
| **UEC** | 0x09 | Underhood Electrical Center - Motornahes |
| **REC** | 0x2E | Rear Electrical Center - Heckelektronik |
| **BCM** | 0xFF | Body Control Module - Karosserieelektronik |
| **IPC** | 0x83 | Instrument Panel Cluster - Kombiinstrument |
| **CIM** | 0x7E | Column Integration Module - Lenksäule |

### BCM Kalibrierungs-Kanäle

| Kanal | Bereich | Standard | Beschreibung |
|-------|---------|---------|--------------|
| `Speed Dependent Locking` | 0-1 | 0 | Auto-Verriegelung 12km/h |
| `Selective Door Unlock` | 0-1 | 1 | Einzelentriegelung |
| `Windows Comfort Closing` | 0-1 | 0 | Komfortschließen |
| `Windows Comfort Opening` | 0-1 | 0 | Komfortöffnen |
| `Power Folding Mirrors` | 0-1 | 0 | Spiegelanklappung |
| `Crash Unlock Relay` | 0-1 | 1 | Crash-Entriegelung |
| `Acoustic Lock Confirmation` | 0-3 | 3 | Akustische Quittung |

### UEC Kalibrierungs-Kanäle

| Kanal | Bereich | Standard | Beschreibung |
|-------|---------|---------|--------------|
| `Daytime Running Light` | 0-5 | 1 | Tagfahrlicht Variante |
| `DRL with Parking Light` | 0-1 | 0 | DRL mit Standlicht |
| `Coming Home` | 0-5 | 2 | Coming Home Zeit |
| `Leaving Home` | 0-1 | 0 | Leaving Home |
| `Fog Lamps Front` | 0-1 | 0 | Nebelscheinwerfer |
| `Fog as DRL` | 0-1 | 0 | Nebelscheinwerfer als TFL |
| `Check Control` | 0-1 | 1 | Check-Control |
| `Rain/Light Sensor` | 0-1 | 1 | Regen/Licht-Sensor |

### REC Kalibrierungs-Kanäle

| Kanal | Bereich | Standard | Beschreibung |
|-------|---------|---------|--------------|
| `Emergency Brake Light` | 0-1 | 0 | Adaptives Bremslicht |
| `Rear Wiper Reverse` | 0-1 | 1 | Heckwischer Rückwärtsgang |
| `Ambient Light Color` | 1-6 | 1 | Ambientefarbe |

### IPC Kalibrierungs-Kanäle

| Kanal | Bereich | Standard | Beschreibung |
|-------|---------|---------|--------------|
| `Driver Information Center` | 0-1 | 1 | DIC vorhanden |
| `Board Computer` | 0-1 | 1 | Bordcomputer |
| `Instant MPG Display` | 0-1 | 1 | Momentanverbrauch |
| `Average MPG Display` | 0-1 | 1 | Durchschnittsverbrauch |
| `Fuel Range Display` | 0-1 | 1 | Reichweite |
| `Outside Temperature` | 0-1 | 1 | Außentemperatur |
| `Oil Temperature Display` | 0-1 | 0 | Öltemperatur |
| `ECO Index Display` | 0-1 | 1 | ECO-Index |
| `Turbo Boost Gauge` | 0-1 | 0 | Ladedruck-Anzeige |
| `Overspeed Warning` | 0-1 | 1 | Geschwindigkeitswarnung |

---

## 19. Getriebe M32 - Schaltpunkte

### Gangübersetzung

| Gang | Übersetzung | Km/h bei 1000 U/min |
|------|-------------|---------------------|
| 1 | 39:11 (3.545) | 8.3 km/h |
| 2 | 21:13 (1.615) | 18.2 km/h |
| 3 | 14:11 (1.273) | 23.1 km/h |
| 4 | 11:11 (1.000) | 29.4 km/h |
| 5 | 9:11 (0.818) | 35.9 km/h |
| 6 | 7:11 (0.636) | 46.2 km/h |
| Rückwärts | 38:9 (4.222) | - |

### Optimaler Schaltbereich

| Gangwechsel | RPM-Bereich | Empfehlung |
|-------------|-------------|------------|
| 1 → 2 | 3000-4000 | 3500 RPM |
| 2 → 3 | 3000-4000 | 3500 RPM |
| 3 → 4 | 3000-4500 | 3500 RPM |
| 4 → 5 | 2800-3500 | 3000 RPM |
| 5 → 6 | 2500-3200 | 2800 RPM |

---

## 20. Sensor-Abgleichwerte (Idle)

### Leerlauf-Kalibrierung (Motor warm, 750 U/min)

| Parameter | Sollwert | Toleranz | Prüfbedingung |
|-----------|----------|----------|--------------|
| Drosselklappe | 2-5% | ±1% | Leerlauf |
| STFT | -5 bis +5% | ±2% | Leerlauf |
| LTFT | -5 bis +5% | ±2% | Leerlauf |
| Zündzeitpunkt | 6-12° | ±2° | Leerlauf |
| Lambdaspannung | 0.1-0.9V | - | Schwingend |
| Öldruck | 1.0-2.0 bar | ±0.3 | Leerlauf |
| Wastegate Duty | 85-95% | ±5% | Leerlauf |

---

## Quellen

- Bosch ME17.9.22 Technische Dokumentation
- GM/Opel Werkstatthandbuch Astra J
- SAE J1979 (OBD-II Standard)
- SAE J2190 (Mode 22 Herstellerdiagnose)
- CANOPO-ODB App Kalibrierungsdatenbank
- VXOC Forum - Astra J Coding
- Hidplanet.lv - OP-COM Coding Guide
- MOTOR-TALK.de - Opel Astra J Forum
