# Opel Astra J 1.4 Turbo (A14NET) - Live Data Referenzwerte

> Referenzwerte für die Interpretation von Live-OBD-Daten. Alle Werte gelten für den motorwarmen Zustand (Kühlmitteltemperatur ≥ 80 °C).

---

## 1. Leerlauf (Idle) - Motor warm, 750 U/min, stehend

| Parameter | Normaler Bereich | Grenzwert (Warnung) | Grenzwert (Kritisch) | Anmerkung |
|-----------|-----------------|--------------------|--------------------|-----------|
| **Motordrehzahl** | 700–800 U/min | < 600 oder > 900 U/min | < 500 oder > 1200 U/min | Schwankungen ±50 U/min normal |
| **Kühlmitteltemperatur** | 80–105 °C | > 100 °C | ≥ 105 °C | Ziel: ~90 °C nach Aufwärmen |
| **Öltemperatur** | 90–110 °C | > 115 °C | ≥ 120 °C | Erst nach 10–15 Min. stabil |
| **Ladedruck** | 0 bar (absolut: ~1,0 bar) | — | — | Wastegate steht offen |
| **Wastegate Duty** | 80–95% | < 10% oder > 98% | < 5% oder > 99% | Offen = hoher Duty |
| **Drosselklappenstellung** | 0–5% | > 10% | > 20% | Fast geschlossen |
| **Motorlast** | 15–25% | > 35% | > 50% | Leerlauf-Kompensation |
| **MAF (Luftmasse)** | 2,0–5,0 g/s | < 1,5 g/s oder > 6,0 g/s | < 1,0 g/s oder > 8,0 g/s | Typisch ~3 g/s |
| **IAT (Ansaugluft)** | 15–40 °C | > 50 °C | > 65 °C | Umgebungstemperatur-bezogen |
| **Zündzeitpunkt** | 5–15° | > 25° | > 35° | Variabel je nach Last |
| **STFT (Bank 1)** | -5% bis +5% | ±10% | ±15% | Kurzzeit-Kraftstofftrim |
| **LTFT (Bank 1)** | -5% bis +5% | ±10% | ±15% | Langzeit-Kraftstofftrim |
| **Batteriespannung** | 13,8–14,5 V | < 12,5 V | < 11,8 V | Ladespannung muss anliegen |
| **Lambda B1S1** | Schwingt 0,1–0,9 V | Nur 0,1–0,4 V | Keine Aktivität | Schwingung = Regelfunktion OK |
| **Öldruck** | ≥ 1,0 bar | < 0,8 bar | < 0,5 bar | Min. 1,0 bar @ Leerlauf |

### Idle-Checkliste

- [ ] Drehzahl stabil bei 750 ± 50 U/min
- [ ] Keine ungewöhnlichen Schwingungen
- [ ] Kühlmitteltemperatur im Zielbereich (80–105 °C)
- [ ] Ladedruck = 0 bar (kein Boost im Leerlauf)
- [ ] STFT/LTFT innerhalb ±10%
- [ ] Batteriespannung > 13,5 V
- [ ] Keine Auffälligkeiten in der Lambda-Regelung

---

## 2. Vollast (Full Load) - 100% Drosselklappe, 3000–5500 U/min

| Parameter | Normaler Bereich | Grenzwert (Warnung) | Grenzwert (Kritisch) | Anmerkung |
|-----------|-----------------|--------------------|--------------------|-----------|
| **Motordrehzahl** | 3000–5500 U/min | > 5850 U/min | ≥ 6500 U/min | Redline-Bereich |
| **Ladedruck (Soll)** | 0,7 bar | — | — | Zielwert der ECU |
| **Ladedruck (Ist)** | 0,5–0,7 bar | < 0,3 bar (Underboost) | > 1,0 bar (Overboost) | Soll-Ist-Vergleich |
| **Overboost-Ladedruck** | bis 1,3 bar | — | > 1,3 bar | Max. 10 Sek. erlaubt |
| **Wastegate Duty** | 30–50% | > 80% (fast offen) | < 5% (fast geschlossen) | Geregelt über Pulsweitenmodulation |
| **Drosselklappenstellung** | 90–100% | — | — | Voll geöffnet |
| **Motorlast** | 80–100% | — | — | Maximale Auslastung |
| **MAF (Luftmasse)** | 30–90 g/s | < 20 g/s | < 15 g/s | Abhängig von Drehzahl |
| **Kraftstoffrail-Druck** | 40–120 bar | < 30 bar | > 150 bar | Direkteinspritzung |
| **EGT (Bank 1)** | 600–800 °C | > 765 °C (90%) | ≥ 850 °C | Unter 850 °C halten! |
| **Ladelufttemperatur** | 30–55 °C | > 58 °C | > 65 °C | Intercooler-Effizienz prüfen |
| **Öltemperatur** | 95–115 °C | > 115 °C | ≥ 120 °C | Überhitzungsgefahr |
| **STFT (Bank 1)** | -5% bis +5% | ±10% | ±15% | Kraftstoffanpassung |
| **Zündzeitpunkt** | 15–25° | > 30° | — | Lastabhängig |
| **Lambda B1S1** | 0,85–0,95 V (fast konstant) | Schwingt stark | — | Schichtbetrieb OK |
| **Turbo-Drehzahl** | 50.000–150.000 U/min | > 180.000 U/min | ≥ 200.000 U/min | Max. 200.000 RPM |

### Vollast-Checkliste

- [ ] Ladedruck erreicht Zielwert (0,7 bar / Overboost 1,3 bar)
- [ ] Wastegate Duty im normalen Bereich (30–50%)
- [ ] EGT unter 850 °C
- [ ] Ladelufttemperatur unter 65 °C
- [ ] Keine Zündaussetzer (P0300–P0304)
- [ ] Kraftstoffrail-Druck stabil
- [ ] Keine Überladung (P0234) oder Unterladung (P0299)
- [ ] Öltemperatur unter 120 °C

---

## 3. Autobahn-Cruising (120 km/h, 6. Gang, ~2800 U/min)

| Parameter | Normaler Bereich | Grenzwert (Warnung) | Grenzwert (Kritisch) | Anmerkung |
|-----------|-----------------|--------------------|--------------------|-----------|
| **Motordrehzahl** | 2500–3200 U/min | > 4000 U/min | > 5000 U/min | 6. Gang bei 120 km/h |
| **Fahrzeuggeschwindigkeit** | 100–140 km/h | > 180 km/h | > 200 km/h | Richtgeschwindigkeit |
| **Ladedruck** | 0,3–0,5 bar | > 0,7 bar | > 1,0 bar | Teillast-Betrieb |
| **Drosselklappenstellung** | 20–40% | > 60% | > 80% | Je nach Steigung |
| **Motorlast** | 30–50% | > 70% | > 85% | Mittlere Auslastung |
| **Kühlmitteltemperatur** | 85–100 °C | > 103 °C | ≥ 105 °C | Stabil warm |
| **Öltemperatur** | 95–105 °C | > 110 °C | ≥ 120 °C | Im Autobahnbetrieb normal |
| **MAF (Luftmasse)** | 15–30 g/s | > 40 g/s | > 60 g/s | Abhängig von Last |
| **EGT (Bank 1)** | 500–650 °C | > 700 °C | > 800 °C | Teillast |
| **Ladelufttemperatur** | 25–45 °C | > 55 °C | > 65 °C | Umgebungstemperatur-bezogen |
| **Kraftstoffverbrauch (berechnet)** | 5,0–7,0 L/100 km | > 8,0 L/100 km | > 10 L/100 km | Realfahrzeug-Referenz |
| **STFT/LTFT** | -5% bis +5% | ±10% | ±15% | Kraftstoffanpassung stabil |

### Autobahn-Checkliste

- [ ] Motordrehzahl in der effizienten Zone (2500–3200 U/min)
- [ ] Ladedruck im Teillast-Bereich
- [ ] Alle Temperaturen stabil
- [ ] Keine Warnleuchten
- [ ] Kraftstoffverbrauch im erwarteten Bereich

---

## 4. Kaltstart-Referenzwerte

| Parameter | Start | Nach 30 Sek. | Nach 2 Min. | Nach 5 Min. (warm) |
|-----------|-------|-------------|-------------|-------------------|
| **Kühlmitteltemperatur** | Umgebung (z.B. 20 °C) | +5–10 °C | +15–25 °C | 80–90 °C |
| **Öltemperatur** | Umgebung | Umgebung | +5–10 °C | 70–90 °C |
| **Motordrehzahl** | 1200–1500 U/min | 1000–1200 U/min | 800–1000 U/min | 750 ± 50 U/min |
| **STFT** | -20% bis +20% (offene Schleife) | ±10% | ±8% | ±5% |
| **Lambda-Regelung** | OFF (offene Schleife) | OFF | ON (geschlossene Schleife) | ON stabil |
| **Ladedruck** | 0 bar | 0 bar | 0 bar | 0 bar |

---

## 5. Fehlerwert-Schwellen (Sensordiagnose)

### 5.1 Gültigkeitsbereiche

| Sensor | Min. gültig | Max. gültig | Fehlerwert (zu niedrig) | Fehlerwert (zu hoch) |
|--------|------------|------------|------------------------|---------------------|
| Kühlmitteltemperatur | -40 °C | 130 °C | < -30 °C | > 130 °C |
| Ansauglufttemperatur | -40 °C | 120 °C | < -30 °C | > 120 °C |
| Öltemperatur | -40 °C | 150 °C | < -30 °C | > 150 °C |
| Ladedruck | 0 kPa | 300 kPa | < 10 kPa | > 250 kPa |
| MAF | 0 g/s | 250 g/s | < 0,5 g/s | > 200 g/s |
| Drosselklappe | 0% | 100% | < 0% | > 100% |
| EGT | 0 °C | 1000 °C | < 50 °C | > 1000 °C |
| Batteriespannung | 6 V | 18 V | < 9 V | > 16 V |

### 5.2 Sensor-Plausibilitätsprüfung

| Prüfung | Bedingung | Ergebnis |
|---------|-----------|----------|
| MAF vs. Drosselklappe | MAF > 50 g/s bei Drossel < 10% | Unplausibel |
| MAF vs. RPM | MAF < 1 g/s bei RPM > 3000 | Unplausibel |
| ECT vs. Laufzeit | ECT steigt nicht nach 5 Min. | Thermostat klemmt |
| Boost vs. Drossel | Boost > 0,5 bar bei Drossel < 5% | Undichtigkeit/Wastegate |
| Lambda vs. Kraftstofftrim | LTFT > 20% bei Lambda OK | Kraftstoffdruck-Problem |
| Öldruck vs. RPM | Öldruck < 0,5 bar bei RPM > 2000 | Ölstand oder -pumpe prüfen |

---

## 6. Diagnostische Interpretation

### 6.1 Kraftstoff-Trim-Analyse

| STFT | LTFT | Gesamt | Interpretation | Nächste Schritte |
|------|------|--------|---------------|-----------------|
| +2% | +3% | +5% | Normal | Keine Aktion |
| +8% | +12% | +20% | Mager-Tendenz | Luftleck, MAF prüfen |
| +15% | +15% | +30% | Stark mager | MAF, Kraftstoffdruck, Injektoren |
| -3% | -2% | -5% | Normal | Keine Aktion |
| -10% | -15% | -25% | Fett-Tendenz | Lambda-Sensor, Kraftstoffdruck |
| +20% | -5% | +15% | STFT instabil | Zündaussetzer, Lambda-Sensor |

### 6.2 Boost-Analyse

| Ist-Boost | Soll-Boost | Differenz | Interpretation |
|-----------|-----------|-----------|---------------|
| 0,7 bar | 0,7 bar | 0% | Optimal |
| 0,5 bar | 0,7 bar | -29% | Unterladung - Wastegate undicht |
| 0,3 bar | 0,7 bar | -57% | Starke Unterladung - Turbo-Problem |
| 1,0 bar | 0,7 bar | +43% | Überladung - Wastegate klemmt |
| 1,3 bar | 0,7 bar | +86% | Overboost (max. 10 Sek. erlaubt) |

### 6.3 Temperatur-Analyse

| Kühlmittel | Öl | EGT | Interpretation |
|------------|-----|-----|---------------|
| 90 °C | 95 °C | 600 °C | Normal, motorwarm |
| 105 °C | 110 °C | 750 °C | Grenzwert, beobachten |
| 110 °C | 120 °C | 850 °C | ⚠️ Überhitzung! Motor auskühlen lassen |
| 120 °C | 130 °C | 950 °C | 🔴 Kritisch! Motor sofort aus! |

### 6.4 Wastegate-Zustandsanalyse

| Duty-Cycle | Ladedruck | Interpretation |
|------------|-----------|---------------|
| 80–95% | 0 bar | Leerlauf - Wastegate offen (normal) |
| 45–70% | 0,5–0,7 bar | Normalbetrieb - WG geregelt |
| 30–50% | 0,7–1,0 bar | Vollast - WG schließt sich |
| < 5% | > 1,0 bar | ⚠️ WG klemmt geschlossen (Overboost-Gefahr) |
| > 95% | < 0,3 bar | ⚠️ WG klemmt offen (kein Boost) |

---

## 7. Warnleuchten-Referenz

| Leuchte | Bedeutung | Sofortige Aktion |
|---------|-----------|-----------------|
| 🔴 Motorkontrollleuchte (MIL) | DTC gespeichert | DTC auslesen, Schweregrad prüfen |
| 🔴 Öldruck-Warnleuchte | Öldruck < 0,5 bar | **Motor sofort aus!** Ölstand prüfen |
| 🔴 Temperatur-Warnleuchte | Kühlmittel > 110 °C | Motor auskühlen lassen, Kühlmittelstand prüfen |
| 🟡 Getriebe-Warnleuchte | Getriebe-DTC | Getriebe-ECU auslesen |
| 🟡 ESP/ABS-Warnleuchte | Fahrwerk-DTC | ABS-Sensoren prüfen |
| 🔵 Glühkerzen-Warnleuchte | DTC gespeichert (bei Benzin: ungewöhnlich) | ECU auslesen |

---

## 8. OBD-II Live Data PIDs

### Standard PIDs (Mode 01)

| PID | Name | Einheit | Formel |
|-----|------|---------|--------|
| 010C | Motordrehzahl | U/min | (A×256+B)/4 |
| 010D | Fahrzeuggeschwindigkeit | km/h | A×256+B |
| 0105 | Kühlmitteltemperatur | °C | A-40 |
| 010F | Ansauglufttemperatur | °C | A-40 |
| 0104 | Motorlast | % | A×100/255 |
| 0111 | Drosselklappenstellung | % | A×100/255 |
| 0114 | Ladedruck | kPa | (A×256+B)/100 |
| 010E | Zündzeitpunkt | ° | A/2-64 |
| 012F | Kraftstofffüllstand | % | A×100/255 |
| 0146 | Umgebungstemperatur | °C | A-40 |
| 015B | Kühlmitteltemperatur | °C | A-40 |
| 0106 | Korrigierter Motorlast | % | A×100/255 |
| 010B | Ansaugluftdruck | kPa | A |
| 0133 | Zeitspanne Lambdawechsel | s | A/200 |
| 0142 | Spannung Lambdasonde | V | A/200 |

### Hersteller PIDs (Mode 22)

| PID | Name | Einheit |
|-----|------|---------|
| 220001 | Motordrehmoment | Nm |
| 220002 | Turbo Boost (Ist) | kPa |
| 220003 | Turbo Boost (Soll) | kPa |
| 220004 | Wastegate Duty-Cycle | % |
| 220005 | Turbo-Drehzahl | RPM |
| 220006 | Turbo-Einlauf-Temp. | °C |
| 220007 | Turbo-Auslauf-Temp. | °C |
| 220008 | Ladelufttemperatur | °C |
| 220009 | VGT-Stellung | % |
| 22000A | Turbo-Effizienz | % |
| 221001 | Kraftstoffrail-Druck | kPa |
| 221002 | Kraftstofftemperatur | °C |
| 222001 | Kat-Temp. Bank 1 S1 | °C |
| 223001 | Umgebungstemperatur | °C |
| 223002 | Motölsensor | °C |
| 223003 | Motöldruck | kPa |
| 225001 | Lambda (Wideband) B1 | λ |

---

## 9. Z14XEL (1.4L N/A) – Neue Mode 22 PIDs

> Für den Opel Astra J mit Z14XEL-Motor (1.4L, 122 PS, 160 Nm,自然吸气).
> Der Z14XEL nutzt den gleichen Bosch ME17.9.2 ECU wie der A14NET, jedoch ohne Turbo.

### 9.1 Z14XEL-spezifische Mode 22 PIDs

| PID | Name | Einheit | Normalwert (Idle) |
|-----|------|---------|-------------------|
| 221012 | Drosselklappenstellung | % | 0–3% |
| 221011 | EGR-Stellung | % | 0–5% |
| 221013 | Ansaugkrümmer-Druck | kPa | 30–45 (Vakuum) |
| 221030 | Leerlauf-Luftregelung | % | 30–70% |
| 221031 | Klopfverstellung | ° | 0–5° |
| 221032 | Zündverweilzeit | ms | 1.0–2.0 |
| 221020 | Katalysator-Temperatur | °C | 50–200 (kalt) |
| 221027 | EVAP-Purge | % | 0–10% |

### 9.2 Unterschiede Z14XEL vs. A14NET

| Aspekt | Z14XEL (N/A) | A14NET (Turbo) |
|--------|--------------|----------------|
| Leistung | 90 kW (122 PS) | 103 kW (140 PS) |
| Drehmoment | 160 Nm @ 3800 | 200 Nm @ 1850–5500 |
| Max. Drehzahl | 6200 U/min | 6500 U/min |
| Ladedruck | — | bis 1.0 bar |
| Öldruck Idle | ≥ 1.0 bar | ≥ 1.0 bar |
| Kraftstoffrail-Druck | 350–450 kPa | 350–550 kPa |

---

## Quellen

- SAE J1979 (OBD-II Standard)
- Bosch ME17.9.22 Technische Dokumentation
- CANOPO-ODB App Live-Data Kalibrierung
- Opel Astra J Werkstatthandbuch
