# Opel Astra J 1.4 Turbo (A14NET) - Integration Guide

## Übersicht

Diese Dokumentation beschreibt die Integration und Optimierung für den Opel Astra J 2012 1.4 Turbo Benzin 140 PS (Motorcode: A14NET / LUJ).

## Wichtigste Integration-Features

### 1. Turbo-Überwachung (Overboost-Monitor)

**Spezifisch für A14NET:**
- **Normaler Ladedruck:** 0,7 bar (70 kPa)
- **Overboost-Limit:** 1,3 bar (130 kPa) - max. 10 Sekunden
- **Turbo-Drehzahl:** Bis 200.000 U/min
- **EGT-Grenze:** 850 °C kritisch

**Implementierung:**
```kotlin
data class TurboData(
    val overboostActive: Boolean = false,
    val overboostSecondsRemaining: Int = 0,
    val overboostMaxDuration: Int = 10,
    val currentTorqueNm: Double = 0.0,
    val maxTorqueNm: Double = 200.0,
    val overboostTorqueNm: Double = 220.0,
) {
    val isOverboost: Boolean get() = boostBar > 1.0
}
```

### 2. Wastegate-Monitoring

**Referenzwerte für BorgWarner KP39:**
| Zustand | Duty-Cycle | Ladedruck |
|---------|------------|-----------|
| Leerlauf (offen) | 80-95% | 0 bar |
| Normalbetrieb | 45-70% | 0,5-0,7 bar |
| Vollast | 30-50% | 0,7-1,0 bar |
| Kritisch (geklemmt) | < 10% | > 1,0 bar |

### 3. Temperatur-Überwachung

| Sensor | Optimal | Warnung | Kritisch |
|--------|---------|---------|----------|
| Kühlmittel | 80-105 °C | ≥ 99,75 °C | ≥ 105 °C |
| Öltemperatur | 90-110 °C | ≥ 108 °C | ≥ 120 °C |
| Ladeluft | 20-45 °C | ≥ 58,5 °C | ≥ 65 °C |
| EGT | 400-700 °C | ≥ 765 °C | ≥ 850 °C |

### 4. OBD-II PIDs (Mode 01)

| PID | Name | Einheit | Formel |
|-----|------|---------|--------|
| 010C | Motordrehzahl | rpm | (A×256+B)/4 |
| 010D | Geschwindigkeit | km/h | A×256+B |
| 0105 | Kühlmitteltemp | °C | A-40 |
| 0104 | Motorlast | % | A×100/255 |
| 0111 | Drosselklappung | % | A×100/255 |
| 0114 | Ladedruck | kPa | (A×256+B)/100 |
| 0170 | Boost Pressure | kPa | (A×256+B)/4 |
| 0174 | Turbo RPM | rpm | (A×256+B) |
| 0177 | CAC Temp | °C | A-40 |
| 0178 | EGT Bank 1 | °C | (A×256+B)/10-40 |

### 5. Mode 22 PIDs (Herstellerspezifisch)

| PID | Beschreibung | Einheit |
|-----|-------------|---------|
| 220001 | Motordrehmoment | Nm |
| 220002 | Turbo Boost Ist | kPa |
| 220003 | Turbo Boost Soll | kPa |
| 220004 | Wastegate Duty | % |
| 220005 | Turbo Drehzahl | RPM |
| 220006 | Turbo Einlauf-Temp | °C |
| 220007 | Turbo Auslauf-Temp | °C |
| 221001 | Kraftstoffrail-Druck | kPa |
| 225001 | Lambda Wideband | λ |

## Wartung & Probleme

### Wartungsintervalle

| Bauteil | Standard | Schwerlast |
|---------|----------|------------|
| Ölwechsel (Dexos2 5W-30) | 15.000 km | 10.000 km |
| Luftfilter | 30.000 km | 15.000 km |
| Zündkerzen (NGK LZKR6AP-11G) | 60.000 km | 30.000 km |
| Kühlmittel | 80.000 km | 60.000 km |
| Turbo-Inspektion | 60.000 km | 45.000 km |
| Timing-Kette | 150.000 km | 100.000 km |

### Typische Probleme nach km

| km-Bereich | Problem | DTCs |
|------------|---------|------|
| 30.000-60.000 | Zündkerzen-Verschleiß | P0300-P0304 |
| 60.000-80.000 | MAF-Sensor, PCV-Ventil | P0100-P0103, P1100/P1101 |
| 80.000-150.000 | Kettenspanner, Wastegate | P0340, P0234/P0299 |
| 100.000+ | Timing-Kette Verschleiß | P0016-P0019 |

## Schaltpunkt-Empfehlungen (M32 Getriebe)

| Gang | Optimal (RPM) | Max RPM |
|------|---------------|---------|
| 1→2 | 3500-4000 | 5000 |
| 2→3 | 3500-4000 | 5000 |
| 3→4 | 3500-4000 | 5500 |
| 4→5 | 3000-3500 | 5500 |
| 5→6 | 2800-3200 | 5000 |

## Tuning-Stufen

| Stage | Leistung | Torque | Boost | Anmerkung |
|-------|----------|--------|-------|-----------|
| Stage 1 | 170-185 PS | 260-280 Nm | 0,7-0,8 bar | ECU Remap, Luftfilter |
| Stage 2 | 195-210 PS | 290-310 Nm | 0,9-1,0 bar | Intercooler, Auspuff |
| Stage 3 | 230-260 PS | 320-350 Nm | 1,2-1,5 bar | Turbo-Tausch, Built Engine |

## Diagnose-Hilfen

### Boost-Analyse
- **Underboost (< 0,5 bar):** Wastegate undicht, Verschmutzung
- **Overboost (> 1,3 bar):** Wastegate klemmt, ECU-Problem

### Fuel-Trim-Analyse
- **STFT + LTFT > 15%:** Mager (Leck, MAF defekt)
- **STFT + LTFT < -15%:** Fett (Lambda-Sensor, Druck)

## Referenzmesswerte

### Leerlauf (warm)
- RPM: 750 ± 50
- Kühlmittel: 80-105 °C
- Ladedruck: 0 bar
- Wastegate: 80-95%
- MAF: 2,0-5,0 g/s

### Volllast
- Ladedruck: 0,5-0,7 bar
- Wastegate: 30-50%
- EGT: < 850 °C
- MAF: 30-90 g/s

## Quellen
- Bosch ME17.9.22 Technische Dokumentation
- GM/Opel Werkstatthandbuch Astra J
- SAE J1979 (OBD-II Standard)
- SAE J2190 (Mode 22 Herstellerdiagnose)
- CANOPO-OBD App Kalibrierungsdatenbank