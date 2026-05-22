# OBD-II PIDs & GM-spezifische Parameter für Opel Astra J 2012 1.4 Turbo (A14NEL)

> Umfassende Recherche zu OBD-II PIDs, GM Mode $22 DIDs und herstellerspezifischen Parametern für den Opel Astra J mit 1.4L Turbo 140PS Motor.

---

## Inhaltsverzeichnis

1. [Fahrzeuginformationen](#1-fahrzeuginformationen)
2. [Standard OBD-II Mode $01 PIDs](#2-standard-obd-ii-mode-01-pids)
3. [OBD-II Mode $02 Freeze Frame](#3-obd-ii-mode-02-freeze-frame)
4. [OBD-II Mode $03 DTCs](#4-obd-ii-mode-03-dtcs)
5. [OBD-II Mode $09 Fahrzeuginformationen](#5-obd-ii-mode-09-fahrzeuginformationen)
6. [GM Mode $22 Erweiterte Datenidentifikatoren (DIDs)](#6-gm-mode-22-erweiterte-datenidentifikatoren-dids)
7. [GM-spezifische PIDs für Turbomotoren](#7-gm-spezifische-pids-für-turbomotoren)
8. [GM Global Architecture spezifische PIDs](#8-gm-global-architecture-spezifische-pids)
9. [Bosch ME17.9.22 Motormanagement](#9-bosch-me17922-motormanagement)
10. [CAN-Bus Adressen und Protokoll](#10-can-bus-adressen-und-protokoll)
11. [Erwartete Wertebereiche für A14NEL](#11-erwartete-wertebereiche-für-a14nel)
12. [Quellen und Referenzen](#12-quellen-und-referenzen)

---

## 1. Fahrzeuginformationen

### 1.1 Motor: A14NEL / LUJ

| Eigenschaft | Wert |
|------------|------|
| Hubraum | 1.398 cm³ |
| Leistung | 103 kW (140 PS) bei 4.900-6.000 U/min |
| Drehmoment | 200 Nm bei 1.850-4.900 U/min |
| Aufladung | Abgasturbolader mit Wastegate |
| Kraftstoffeinspritzung | Direkteinspritzung (DI) |
| Motormanagement | Bosch ME17.9.22 |
| Bohrung × Hub | 72,5 × 84,2 mm |
| Verdichtung | 9,5:1 |
| Drehzahlbereich | 700 - 6.500 U/min |
| Ladedruck (max.) | 1,3 bar (Overboost) |
| Ladedruck (normal) | 0,7 bar |

### 1.2 Fahrzeugdetails

| Eigenschaft | Wert |
|------------|------|
| Modell | Opel Astra J 5-Türer |
| Baujahr | 2012 |
| Plattform | GM Delta II |
| Getriebe | Getrag M32 6-Gang |
| OBD-Standard | EOBD (Euro-5) |
| CAN-Bus Geschwindigkeit | 500 kbit/s |

---

## 2. Standard OBD-II Mode $01 PIDs

### 2.1 Übersicht der verfügbaren PIDs

Der Opel Astra J A14NEL unterstützt folgende Mode $01 PIDs (Dekodierung aus Wikipedia/SAE J1979):

| PID | Dezimal | Name | Bytes | Einheit | Formel | A14NEL Typisch |
|-----|---------|------|-------|---------|--------|----------------|
| **$00** | 0 | PIDs unterstützt [$01-$20] | 4 | Bit-Encoded | A7..D0 = PID $01..$20 | BE3FA813 |
| **$01** | 1 | Monitor Status | 4 | Bit-Encoded | Siehe Abschnitt 2.2 | 00000000 |
| **$03** | 3 | Kraftstoffsystem Status | 2 | Bit-Encoded | Siehe Abschnitt 2.3 | 0201 |
| **$04** | 4 | Motorlast | 1 | % | A × 100 / 255 | 20-90% |
| **$05** | 5 | Kühlmitteltemperatur | 1 | °C | A - 40 | 80-105°C |
| **$06** | 6 | STFT Bank 1 | 1 | % | (A / 1.28) - 100 | -10% bis +10% |
| **$07** | 7 | LTFT Bank 1 | 1 | % | (A / 1.28) - 100 | -10% bis +10% |
| **$08** | 8 | STFT Bank 2 | 1 | % | (A / 1.28) - 100 | -10% bis +10% |
| **$09** | 9 | LTFT Bank 2 | 1 | % | (A / 1.28) - 100 | -10% bis +10% |
| **$0B** | 11 | Ansaugluftdruck (MAP) | 1 | kPa | A | 25-100 kPa |
| **$0C** | 12 | Motordrehzahl | 2 | rpm | (A×256 + B) / 4 | 700-6000 |
| **$0D** | 13 | Fahrzeuggeschwindigkeit | 1 | km/h | A | 0-250 |
| **$0E** | 14 | Zündzeitpunkt | 1 | ° | A / 2 - 64 | 5-25° |
| **$0F** | 15 | Ansauglufttemperatur (IAT) | 1 | °C | A - 40 | 15-50°C |
| **$10** | 16 | MAF Luftmassensensor | 2 | g/s | (A×256 + B) / 100 | 2-150 g/s |
| **$11** | 17 | Drosselklappenstellung | 1 | % | A × 100 / 255 | 0-100% |
| **$1C** | 28 | OBD-Standard | 1 | Enum | Siehe Tabelle | 06 (EOBD) |
| **$1F** | 31 | Laufzeit seit Motorstart | 2 | s | A×256 + B | 0-65535 |
| **$21** | 33 | Distance MIL an | 2 | km | A×256 + B | 0-65535 |
| **$2C** | 44 | Bestellte EGR | 1 | % | A × 100 / 255 | 0-30% |
| **$2D** | 45 | EGR-Fehler | 1 | % | (A / 1.28) - 100 | -10% bis +10% |
| **$2E** | 46 | Bestellte Evap-Purge | 1 | % | A × 100 / 255 | 0-100% |
| **$2F** | 47 | Kraftstofftankfüllstand | 1 | % | A × 100 / 255 | 0-100% |
| **$30** | 48 | Warm-ups seit Codes gelöscht | 1 | count | A | 0-255 |
| **$31** | 49 | Distance seit Codes gelöscht | 2 | km | A×256 + B | 0-65535 |
| **$33** | 51 | Absoluter Luftdruck | 1 | kPa | A | 95-105 kPa |
| **$42** | 66 | Steuermodul-Spannung | 2 | V | (A×256 + B) / 1000 | 13.5-14.5V |
| **$43** | 67 | Absolute Last | 2 | % | 100 × (A×256 + B) / 255 | 10-100% |
| **$44** | 68 | Bestelltes Lambdaverhältnis | 2 | λ | 2 × (A×256 + B) / 65536 | 0.8-1.2 |
| **$45** | 69 | Relative Drosselklappe | 1 | % | A × 100 / 255 | 0-100% |
| **$46** | 70 | Umgebungstemperatur | 1 | °C | A - 40 | -40 bis +50°C |
| **$47** | 71 | Absolute Drosselklappe B | 1 | % | A × 100 / 255 | 0-100% |
| **$48** | 72 | Absolute Drosselklappe C | 1 | % | A × 100 / 255 | 0-100% |
| **$49** | 73 | Pedalstellung D | 1 | % | A × 100 / 255 | 0-100% |
| **$4A** | 74 | Pedalstellung E | 1 | % | A × 100 / 255 | 0-100% |
| **$4B** | 75 | Pedalstellung F | 1 | % | A × 100 / 255 | 0-100% |
| **$4C** | 76 | Bestellter Drosselsteller | 1 | % | A × 100 / 255 | 0-100% |
| **$51** | 81 | Kraftstofftyp | 1 | Enum | Siehe Tabelle | 01 (Benzin) |
| **$5C** | 92 | Motoröltemperatur | 1 | °C | A - 40 | 80-120°C |
| **$5D** | 93 | Kraftstoffeinspritzzeitpunkt | 2 | ° | (A×256 + B) / 128 - 210 | -210 bis +30° |
| **$5E** | 94 | Motor-Kraftstoffverbrauch | 2 | L/h | (A×256 + B) / 20 | 0.5-15 L/h |
| **$61** | 97 | Fahrer-Anforderung Drehmoment | 1 | % | A - 125 | -125 bis +40% |
| **$62** | 98 | Tatsächliches Drehmoment | 1 | % | A - 125 | -125 bis +40% |
| **$63** | 99 | Motordrehmoment-Referenz | 2 | Nm | A×256 + B | 200 Nm |

### 2.2 PID $01 - Monitor Status Dekodierung

```
Byte A:
  Bit 7 (A7): MIL Status (1 = AN, 0 = AUS)
  Bit 6-0: Anzahl DTCs

Byte B:
  Bit 7-5: Reserviert
  Bit 4: Spark Ignition (0) oder Compression (1)
  Bit 3: Komponententest verfügbar
  Bit 2: Kraftstoffsystem Test verfügbar
  Bit 1: Misfire Test verfügbar
  Bit 0: Zündung Test verfügbar

Bytes C-D: Readiness Monitors
```

### 2.3 PID $03 - Kraftstoffsystem Status

| Wert | Bedeutung |
|------|----------|
| 0 | Motor aus |
| 1 | Offene Schleife (Kraftstoffmangel) |
| 2 | Geschlossene Schleife (Lambda-Regelung aktiv) |
| 4 | Offene Schleife (Last/Decel) |
| 8 | Offene Schleife (Systemfehler) |
| 16 | Geschlossene Schleife mit Fehler |

### 2.4 PID $1C - OBD Standards

| Wert | Standard |
|------|----------|
| 01 | OBD-II (CARB) |
| 02 | OBD (EPA) |
| 03 | OBD + OBD-II |
| 04 | OBD-I |
| 06 | **EOBD (Europa)** |
| 07 | EOBD + OBD-II |

### 2.5 PID $51 - Kraftstofftyp

| Wert | Kraftstofftyp |
|------|---------------|
| 01 | Benzin |
| 02 | Methanol |
| 03 | Ethanol |
| 04 | Diesel |
| 05 | LPG |
| 06 | CNG |
| 09 | Benzin Bifuel |

---

## 3. OBD-II Mode $02 Freeze Frame

Mode $02 ermöglicht den Zugriff auf die gespeicherten Sensordaten zum Zeitpunkt der DTC-Setzung.

### Freeze Frame PIDs (identisch zu Mode $01)

| PID | Name | Einheit | Formel |
|-----|------|---------|--------|
| 02 | Freeze Frame DTC | - | Siehe DTC-Format |
| 03 | Kraftstoffsystem Status | - | Bit-Encoded |
| 04 | Motorlast | % | A × 100 / 255 |
| 05 | Kühlmitteltemperatur | °C | A - 40 |
| 06 | STFT Bank 1 | % | (A / 1.28) - 100 |
| 07 | LTFT Bank 1 | % | (A / 1.28) - 100 |
| 0B | MAP | kPa | A |
| 0C | Drehzahl | rpm | (A×256 + B) / 4 |
| 0D | Geschwindigkeit | km/h | A |
| 0E | Zündzeitpunkt | ° | A / 2 - 64 |
| 0F | IAT | °C | A - 40 |
| 10 | MAF | g/s | (A×256 + B) / 100 |
| 11 | Drosselklappe | % | A × 100 / 255 |
| 2F | Kraftstoffstand | % | A × 100 / 255 |
| 33 | Barometrischer Druck | kPa | A |
| 42 | Modulspannung | V | (A×256 + B) / 1000 |
| 44 | Lambdavenhältnis | λ | 2 × (A×256 + B) / 65536 |
| 45 | Relative Drosselklappe | % | A × 100 / 255 |
| 46 | Umgebungstemperatur | °C | A - 40 |
| 5D | Einspritzzeitpunkt | ° | (A×256 + B) / 128 - 210 |
| 5E | Kraftstoffverbrauch | L/h | (A×256 + B) / 20 |

---

## 4. OBD-II Mode $03 DTCs

Mode $03 liest gespeicherte Diagnosefehlercodes (DTCs).

### DTC Format

```
Byte 1-2: DTC Code (2 Bytes)
Byte 3: Status

DTC-Struktur:
  Bit 15-14: Kategorie
    00 = P (Powertrain)
    01 = C (Chassis)
    10 = B (Body)
    11 = U (Network)
  Bit 13-0: DTC Nummer
```

### Wichtige DTCs für A14NEL

| DTC | Beschreibung | System |
|-----|--------------|--------|
| P0010 | Nockenwellenposition Sensor A | VVT |
| P0011 | Nockenwellen-Timing | VVT |
| P0030 | O2 Sensor Heizung B1S1 | Abgas |
| P0033 | Turbo Bypass Ventil | Turbo |
| P0100 | MAF Sensor | Luftmasse |
| P0101 | MAF Bereich/Leistung | Luftmasse |
| P0106 | MAP Sensor Bereich | Ladedruck |
| P0107 | MAP Sensor Low | Ladedruck |
| P0108 | MAP Sensor High | Ladedruck |
| P0110 | IAT Sensor | Temperatur |
| P0115 | ECT Sensor | Temperatur |
| P0116 | ECT Bereich | Temperatur |
| P0117 | ECT Low | Temperatur |
| P0118 | ECT High | Temperatur |
| P0120 | Drosselklappen Sensor | Drossel |
| P0128 | Kühlmittelthermostat | Kühlung |
| P0130 | O2 Sensor B1S1 | Abgas |
| P0131 | O2 Sensor Low B1S1 | Abgas |
| P0132 | O2 Sensor High B1S1 | Abgas |
| P0133 | O2 Sensor Langsam B1S1 | Abgas |
| P0134 | O2 Sensor Keine Aktivität | Abgas |
| P0135 | O2 Sensor Heizung B1S1 | Abgas |
| P0171 | System Zu Mager B1 | Kraftstoff |
| P0172 | System Zu Fett B1 | Kraftstoff |
| P0230 | Kraftstoffpumpe | Kraftstoff |
| P0234 | Turbo Überladung | Turbo |
| P0236 | Turbo Boost Sensor | Turbo |
| P0237 | Turbo Boost Sensor Low | Turbo |
| P0238 | Turbo Boost Sensor High | Turbo |
| P0243 | Wastegate Solenoid A | Turbo |
| P0245 | Wastegate Solenoid Low | Turbo |
| P0246 | Wastegate Solenoid High | Turbo |
| P0299 | Turbo Unterladung | Turbo |
| P0300 | Zündaussetzer | Zündung |
| P0301-P0304 | Zylinder 1-4 Aussetzer | Zündung |
| P0335 | Kurbelwellenposition | Zündung |
| P0336 | Kurbelwellenbereich | Zündung |
| P0340 | Nockenwellenposition | VVT |
| P0341 | Nockenwellenbereich | VVT |
| P0400 | EGR Flow | Abgas |
| P0401 | EGR Unzureichend | Abgas |
| P0420 | Katalysatorwirkungsgrad | Abgas |
| P0440 | EVAP System | Verdampfung |
| P0442 | EVAP Klein Undicht | Verdampfung |
| P0443 | EVAP Purge Ventil | Verdampfung |
| P0446 | EVAP Ventilsteuerung | Verdampfung |
| P0455 | EVAP Groß Undicht | Verdampfung |
| P0500 | Fahrzeuggeschwindigkeit | Geschwindigkeit |
| P0506 | Drehzahl Niedriger | Leerlauf |
| P0507 | Drehzahl Höher | Leerlauf |
| P0562 | Systemspannung Low | Elektrik |
| P0563 | Systemspannung High | Elektrik |
| P0600 | Serielle Kommunikation | Netzwerk |
| P0601 | ECM Speicherfehler | ECM |
| P0602 | ECM Programmierfehler | ECM |
| P0606 | ECM Prozessorfehler | ECM |
| P0621 | Generator L-Terminal | Ladung |
| P0622 | Generator F-Terminal | Ladung |
| P0691-P0694 | Kühlungsgebläse | Kühlung |

---

## 5. OBD-II Mode $09 Fahrzeuginformationen

### 5.1 Verfügbare PIDs

| PID | Name | Bytes | Beschreibung |
|-----|------|-------|--------------|
| 00 | PIDs unterstützt [$01-$20] | 4 | Bit-Encoded |
| 01 | VIN Message Count | 1 | Anzahl VIN-Nachrichten |
| 02 | Vehicle Identification Number (VIN) | 17 | 17-Byte ASCII |
| 04 | Calibration ID | 16 | ECU-Kalibrierungs-ID |
| 06 | Calibration Verification Numbers | 4 | CVN |
| 0A | ECU Name | 20 | ASCII ECU Name |

### 5.2 VIN Struktur

```
Position | Inhalt | Beispiel
---------|--------|-------
1-3 | WMI (World Manufacturer Identifier) | W0L = Opel Germany
4-8 | VDS (Vehicle Descriptor Section) | 8N=1.4L Turbo
9 | Check Digit | 0-9 oder X
10 | Model Year | B = 2011, C = 2012
11 | Plant Code | 0-9
12-17 | Sequential Number | 000001-999999
```

### 5.3 Beispiel VIN

```
W0L000000123456789
     └─────────────┘
     Astra J A14NEL
```

---

## 6. GM Mode $22 Erweiterte Datenidentifikatoren (DIDs)

Mode $22 (Hex: $22) ist ein herstellerspezifischer Modus von GM/Opel für erweiterte Diagnosedaten.

### 6.1 Wichtige DIDs für A14NEL Motor

| DID | Name | Bytes | Einheit | Formel | Bereich | Beschreibung |
|-----|------|-------|---------|--------|---------|--------------|
| **221001** | ENGINE_TORQUE | 1 | % | (A - 128) | -125 bis +40% | Aktuelles Motordrehmoment |
| **221002** | REQUESTED_TORQUE | 1 | % | (A - 128) | -125 bis +40% | Angefordertes Drehmoment |
| **221003** | TORQUE_REDUCTION | 1 | % | (A - 128) | 0-100% | Drehmoment-Reduzierung |
| **221008** | BOOST_PRESSURE_ACTUAL | 2 | kPa | (A×256 + B) | 0-300 kPa | Tatsächlicher Ladedruck |
| **221009** | BOOST_PRESSURE_TARGET | 2 | kPa | (A×256 + B) | 0-250 kPa | Soll-Ladedruck |
| **22100A** | WASTEGATE_POSITION | 1 | % | A | 0-100% | Wastegate-Stellung |
| **22100B** | TURBO_RPM | 2 | rpm | (A×256 + B) | 0-200000 | Turbodrehzahl |
| **22100C** | OIL_TEMP | 1 | °C | (A - 40) | -40 bis +150 | Motoröltemperatur |
| **22100D** | COOLANT_TEMP | 1 | °C | (A - 40) | -40 bis +130 | Kühlmitteltemperatur |
| **22100E** | INTAKE_AIR_TEMP | 1 | °C | (A - 40) | -40 bis +120 | Ansauglufttemperatur |
| **22100F** | FUEL_RAIL_PRESSURE | 2 | kPa | (A×256 + B) × 10 | 0-655350 | Kraftstoffrail-Druck |
| **221010** | INJECTOR_PULSE_WIDTH | 2 | ms | (A×256 + B) / 100 | 0-1000 | Einspritzimpulsbreite |
| **221012** | SPARK_ADVANCE | 1 | ° | (A - 128) | -64 bis +63.5 | Zündzeitpunkt |
| **221013** | KNOCK_RETARD | 1 | ° | (A - 128) | 0-64 | Klopfereduzierung |
| **221015** | VVT_INTAKE | 1 | ° | (A - 128) | -64 bis +63.5 | Nockenwellenposition Einlass |
| **221016** | VVT_EXHAUST | 1 | ° | (A - 128) | -64 bis +63.5 | Nockenwellenposition Auslass |
| **221018** | FUEL_CONSUMPTION_INSTANT | 2 | L/h | (A×256 + B) | 0-65535 | Momentanverbrauch |
| **221019** | FUEL_CONSUMPTION_AVERAGE | 2 | L/100km | (A×256 + B) / 10 | 0-6553.5 | Durchschnittsverbrauch |
| **22101A** | THROTTLE_ACTUAL | 2 | % | (A×256 + B) / 10 | 0-100 | Tatsächliche Drosselklappe |
| **22101B** | THROTTLE_PEDAL | 2 | % | (A×256 + B) / 10 | 0-100 | Pedalstellung |
| **22101C** | ENGINE_RPM | 2 | rpm | (A×256 + B) / 4 | 0-16383.75 | Motordrehzahl |
| **22101D** | VEHICLE_SPEED | 2 | km/h | (A×256 + B) | 0-255 | Fahrzeuggeschwindigkeit |
| **22101F** | AFR_RATIO | 2 | λ | 2 × (A×256 + B) / 65536 | 0-2 | Luft-Kraftstoff-Verhältnis |
| **221020** | CAT_EFFICIENCY | 1 | % | A | 0-100 | Katalysatorwirkungsgrad |
| **221021** | O2_SENSOR_VOLTAGE | 2 | mV | (A×256 + B) | 0-9999 | Lambdasonde Spannung |

### 6.2 Mode $22 Anfrageformat

```
Anfrage (CAN Message):
  7E0 [Length] 22 [DID High] [DID Low] [Padding...]

Beispiel Boost Pressure:
  7E0 02 22 10 08  CC CC CC CC CC

Antwort (CAN Message):
  7E8 [Length] 62 [DID High] [DID Low] [Data Bytes] [Padding...]

Beispiel Antwort:
  7E8 04 62 10 08 01 8C  CC CC CC    (284 kPa = ~0.7 bar)
```

### 6.3 Hinweise zur Nutzung

1. **Nicht alle ELM327-Adapter** unterstützen Mode $22 vollständig
2. **Extended Session** (10 03) kann erforderlich sein
3. **Timing:** Mode $22 kann langsamer sein als Standard-PIDs
4. **ECU-Unterstützung:** Nicht alle DIDs sind auf allen Fahrzeugen verfügbar

---

## 7. GM-spezifische PIDs für Turbomotoren

### 7.1 Turbo-spezifische Parameter

Der A14NEL verwendet einen Abgasturbolader mit Wastegate-Dosierventil. Folgende spezifische Parameter sind relevant:

#### Ladedrucküberwachung

| PID | Quelle | Name | Einheit | Typischer Bereich |
|-----|--------|------|---------|-------------------|
| $0B | Mode $01 | MAP (Ansaugluftdruck) | kPa | 25-100 kPa |
| $0B | Mode $01 | Absoluter Ladedruck | kPa | 100-230 kPa |
| 221008 | Mode $22 | Ist-Ladedruck | kPa | 100-230 kPa |
| 221009 | Mode $22 | Soll-Ladedruck | kPa | 100-200 kPa |
| 22100A | Mode $22 | Wastegate-Stellung | % | 5-95% |

#### Ladelufttemperatur (Intercooler)

| PID | Quelle | Name | Einheit | Typischer Bereich |
|-----|--------|------|---------|-------------------|
| $0F | Mode $01 | IAT (Ansauglufttemp.) | °C | 20-60°C nach Ladeluftkühler |
| 22100E | Mode $22 | Ladelufttemperatur | °C | 25-65°C |

#### Turbodrehzahl

| PID | Quelle | Name | Einheit | Typischer Bereich |
|-----|--------|------|---------|-------------------|
| 22100B | Mode $22 | Turbodrehzahl | rpm | 30.000-180.000 rpm |

### 7.2 Berechnungsformeln für Turbo

```
Ladedruck (relativ):
  Boost_kPa = MAP_kPa - Barometer_kPa
  Boost_bar = Boost_kPa / 100

Wastegate-Stellung:
  Duty_Cycle = 100% - Wastegate_Öffnung
  (100% = WG geschlossen = max. Boost)

Turbo-Drehzahl (geschätzt):
  Turbo_RPM = (Abgasmassestrom × 60) / Turbinenkonstante
```

### 7.3 Turbodiagnose-Werte

| Parameter | Leerlauf | Teillast | Vollast | Overboost |
|-----------|----------|----------|---------|-----------|
| Ladedruck (kPa) | 100 | 120-160 | 170-200 | 230 |
| Ladedruck (bar) | 0 | 0,2-0,6 | 0,7-1,0 | 1,3 |
| Wastegate (%) | 85-95 | 50-70 | 30-50 | 10-20 |
| Ladelufttemp (°C) | 25-35 | 35-50 | 45-60 | 50-65 |
| Turbodrehzahl (RPM) | 0 | 50.000-100.000 | 100.000-150.000 | 150.000+ |

---

## 8. GM Global Architecture spezifische PIDs

### 8.1 GM Delta II Plattform Besonderheiten

Der Opel Astra J basiert auf der GM Delta II Plattform und nutzt folgende GM-spezifische Systeme:

#### Steuermodul-Adressen

| Modul | CAN-ID Request | CAN-ID Response | Adresse |
|-------|---------------|-----------------|---------|
| ECM (Motor) | 0x7E0 | 0x7E8 | 0x01 |
| TCM (Getriebe) | 0x7E1 | 0x7E9 | 0x02 |
| BCM (Karosserie) | 0x7C0 | 0x7C8 | 0xFF |
| IPC (Kombiinstrument) | 0x7C3 | 0x7CB | 0x83 |
| ABS/ESP | 0x7C2 | 0x7CA | 0x09 |
| Airbag | 0x7C5 | 0x7CD | 0x10 |

#### GM-spezifische DIDs (F1xx Bereich)

| DID | Name | Bytes | Beschreibung |
|-----|------|-------|--------------|
| F1 80 | System Name | 16 | Motortyp |
| F1 86 | Active Session | 1 | Aktuelle Diagnosesession |
| F1 87 | ECU Software Number | 16 | Softwarenummer |
| F1 88 | ECU Software Version | 8 | Softwareversion |
| F1 89 | ECU Hardware Number | 16 | Hardwarenummer |
| F1 8A | ECU Hardware Version | 8 | Hardwareversion |
| F1 90 | VIN | 17 | Fahrzeug-Identifikationsnummer |

### 8.2 GM-spezifische DTC-Formate

GM verwendet erweiterte DTC-Formate für die Delta II Plattform:

```
GM erweiterter DTC (3 Bytes):
  Byte 1: Kategorie + High Byte
  Byte 2: Low Byte
  Byte 3: Status

GM spezifische Präfixe:
  P1xxx - GM Powertrain
  C1xxx - GM Chassis
  B1xxx - GM Body
  U1xxx - GM Network
```

---

## 9. Bosch ME17.9.22 Motormanagement

### 9.1 Überblick

Das Motormanagement des A14NEL basiert auf der Bosch ME17.9.22 Steuerung.

| Eigenschaft | Wert |
|------------|------|
| Hersteller | Bosch |
| Typ | ME17.9.22 |
| Kraftstoffeinspritzung | Direkteinspritzung (DI) |
| Zündsystem | Individuelle Zündspulen |
| Lambdaregelung | Breitbandlambdasonde (LSU 4.9) |
| Turboregelung | Elektronisches Wastegate |

### 9.2 Verfügbare Sensordaten

#### Temperatursensoren

| Sensor | PID Mode $01 | PID Mode $22 | Bereich |
|--------|-------------|--------------|---------|
| Kühlmitteltemperatur | $05 | 22100D | -40 bis +130°C |
| Ansauglufttemperatur | $0F | 22100E | -40 bis +120°C |
| Motoröltemperatur | $5C | 22100C | -40 bis +150°C |
| Umgebungstemperatur | $46 | - | -40 bis +50°C |
| Ladelufttemperatur | - | (berechnet) | -40 bis +120°C |
| Kraftstofftemperatur | - | (nicht OBD) | -40 bis +120°C |

#### Drucksensoren

| Sensor | PID Mode $01 | PID Mode $22 | Bereich |
|--------|-------------|--------------|---------|
| MAP (Ansaugluftdruck) | $0B | - | 20-250 kPa |
| Barometrischer Druck | $33 | - | 80-110 kPa |
| Ladedruck (Ist) | - | 221008 | 0-300 kPa |
| Ladedruck (Soll) | - | 221009 | 0-250 kPa |
| Kraftstoffrail-Druck | - | 22100F | 0-200 bar |
| Kraftstoffdruck (Kraftstoffpumpe) | - | (nicht OBD) | 3-8 bar |

#### Positionssensoren

| Sensor | PID Mode $01 | PID Mode $22 | Bereich |
|--------|-------------|--------------|---------|
| Drosselklappe | $11, $45, $47 | 22101A | 0-100% |
| Pedalstellung | $49-$4B | 22101B | 0-100% |
| Nockenwelle Einlass | - | 221015 | -64 bis +63.5° |
| Nockenwelle Auslass | - | 221016 | -64 bis +63.5° |
| Kurbelwelle | $0C | 22101C | 0-16383 rpm |
| Wastegate | - | 22100A | 0-100% |

#### Lambdaregelung

| Sensor | PID Mode $01 | PID Mode $22 | Bereich |
|--------|-------------|--------------|---------|
| O2 Spannung | $14-$17 | 221021 | 0-1000 mV |
| O2 Strom | $34-$37 | - | -128 bis +128 mA |
| AFR (Breitband) | $24-$2B | 22101F | 0-2 (λ) |

### 9.3 Einspritzsystem

| Eigenschaft | Wert |
|------------|------|
| Einspritzventile | 4 × Magnetventil-Injektoren |
| Einspritzdruck | 50-200 bar (Hochdruckpumpe) |
| Einspritzstrategie | Mehrfacheinspritzung möglich |
| Injektor-ID-Codierung | Im ECM hinterlegt |

### 9.4 Zündsystem

| Eigenschaft | Wert |
|------------|------|
| Zündspulen | 4 × Einzelzündspulen (Pencil-Coil) |
| Zündkerzen | NGK ILZKBR7B8DG |
| Zündzeitpunkt | -64 bis +63.5° (variabel) |
| Klopfregelung | Piezo-Klopfsensoren |

---

## 10. CAN-Bus Adressen und Protokoll

### 10.1 CAN-Bus Konfiguration

| Parameter | Wert |
|-----------|------|
| Geschwindigkeit | 500 kbit/s |
| CAN-High Adresse | 0x7E0 (Request) |
| CAN-High Response | 0x7E8 (Response) |
| Terminierung | 120 Ohm |
| OBD-II Port | Pin 6 (CAN-H), Pin 14 (CAN-L) |

### 10.2 OBD-II CAN-Frame Format

```
Standard OBD-II Anfrage (11-bit ID):
  [ID: 7DF] [Length: 2] [Mode: 01/09] [PID: xx] [CC CC CC CC CC CC]

Standard OBD-II Antwort:
  [ID: 7E8] [Length] [Mode+40: 41/49] [PID: xx] [Data...] [Padding...]

Mode $22 (Erweiterte DIDs):
  [ID: 7E0] [Length: 2] [22] [DID High] [DID Low] [CC CC CC CC CC]

Mode $22 Antwort:
  [ID: 7E8] [Length: 3+] [62] [DID High] [DID Low] [Data...] [Padding...]
```

### 10.3 UDS-Dienste (ISO 14229)

| Service | Name | Verwendung |
|---------|------|------------|
| $10 | DiagnosticSessionControl | Session wechseln |
| $11 | ECUReset | ECU zurücksetzen |
| $14 | ClearDTC | DTCs löschen |
| $19 | ReadDTCInfo | DTCs lesen |
| **$22** | **ReadDataByIdentifier** | **Erweiterte Daten lesen** |
| $27 | SecurityAccess | Sicherheitsfreigabe |
| $2E | WriteDataByIdentifier | Daten schreiben |
| $3E | TesterPresent | Verbindung halten |

---

## 11. Erwartete Wertebereiche für A14NEL

### 11.1 Leerlauf-Bedingungen (Motor warm, 750 U/min)

| Parameter | Minimum | Typisch | Maximum | Einheit |
|-----------|---------|---------|---------|---------|
| Drehzahl | 700 | 750 | 800 | rpm |
| Kühlmitteltemp | 80 | 90 | 105 | °C |
| Motoröltemp | 85 | 95 | 110 | °C |
| Ladedruck | 100 | 100 | 101 | kPa |
| Motorlast | 15 | 20 | 25 | % |
| MAF | 2.0 | 3.0 | 4.5 | g/s |
| IAT | 20 | 30 | 40 | °C |
| Drosselklappe | 0 | 2 | 5 | % |
| Zündzeitpunkt | 5 | 10 | 15 | ° |
| STFT | -5 | 0 | +5 | % |
| LTFT | -5 | 0 | +5 | % |
| Batteriespannung | 13.5 | 14.0 | 14.5 | V |

### 11.2 Vollast-Bedingungen (WOT, 3000-5500 rpm)

| Parameter | Minimum | Typisch | Maximum | Einheit |
|-----------|---------|---------|---------|---------|
| Drehzahl | 3000 | 4500 | 5500 | rpm |
| Ladedruck | 180 | 200 | 230 | kPa |
| Motorlast | 80 | 90 | 100 | % |
| MAF | 50 | 80 | 120 | g/s |
| Kraftstoffverbrauch | 8 | 12 | 20 | L/h |
| Ladelufttemp | 35 | 50 | 65 | °C |
| Abgastemp | 600 | 750 | 850 | °C |
| Wastegate | 30 | 45 | 60 | % |

### 11.3 Teillast/Cruising (100 km/h, 6. Gang)

| Parameter | Minimum | Typisch | Maximum | Einheit |
|-----------|---------|---------|---------|---------|
| Drehzahl | 2500 | 2800 | 3200 | rpm |
| Geschwindigkeit | 95 | 100 | 105 | km/h |
| Ladedruck | 120 | 140 | 160 | kPa |
| Motorlast | 25 | 35 | 45 | % |
| MAF | 15 | 25 | 35 | g/s |
| Kraftstoffverbrauch | 5 | 6.5 | 8 | L/100km |
| Drosselklappe | 15 | 25 | 40 | % |

---

## 12. Quellen und Referenzen

### 12.1 Standards und Spezifikationen

| Quelle | Beschreibung | Link |
|--------|-------------|------|
| SAE J1979 | OBD-II PIDs Standard | https://en.wikipedia.org/wiki/OBD-II_PIDs |
| ISO 14229 | Unified Diagnostic Services (UDS) | https://en.wikipedia.org/wiki/Unified_Diagnostic_Services |
| ISO 15765 | CAN Transportprotokoll | ISO Standard |
| GM GMW8769 | GM Mode $22 Spezifikation | Proprietär (ETI-Mitgliedschaft erforderlich) |

### 12.2 Werkstattliteratur

| Quelle | Beschreibung |
|--------|-------------|
| Opel Astra J Werkstatthandbuch | Offizielle Service-Dokumentation |
| Bosch ME17.9.22 Technische Dokumentation | Motormanagement-Spezifikation |
| GM GDS2 | Offizielle GM-Diagnosedatenbank |

### 12.3 Online-Ressourcen

| Quelle | Beschreibung | Link |
|--------|-------------|------|
| Wikipedia OBD-II PIDs | Umfassende PID-Referenz | https://en.wikipedia.org/wiki/OBD-II_PIDs |
| Equipment and Tool Institute | Herstellerspezifische PIDs | https://www.etools.org/ (Mitgliedschaft) |

### 12.4 Hinweis zur Verfügbarkeit

**WICHTIG:** GM-spezifische Mode $22 DIDs und herstellerspezifische PIDs sind **proprietäre Informationen**, die von General Motors streng kontrolliert werden. Die vollständige Dokumentation ist nur über folgende Wege verfügbar:

1. **Equipment and Tool Institute (ETI)** - Mitgliedschaft erforderlich (Jahresgebühr)
2. **GM MDI + GDS2** - Offizielle GM-Werkstattausrüstung
3. **OP-COM / VauxCom** - Opel/Vauxhall-Diagnosetool mit GM-Zugriff

Die in diesem Dokument aufgeführten GM-spezifischen Werte basieren auf:
- Community-Recherche und Reverse Engineering
- Öffentlich zugängliche Foren und Projekte
- Kompatibilitätslisten von Drittanbieter-Tools

---

## Anhang A: Quick Reference - CAN-Befehle

### Standard OBD-II PIDs abfragen

```
ATZ              # Reset ELM327
ATE0             # Echo aus
ATL0             # Linefeed aus
ATS0             # Spaces aus
ATH0             # Headers aus
ATSP0            # Auto-Protokoll
ATAT1            # Adaptive Timing an

# Beispiel: Drehzahl lesen
01 0C            # Mode 01, PID 0C (RPM)
Antwort: 41 0C 0D F8    # (0D×256 + F8) / 4 = 3576 rpm

# Beispiel: Kühlmitteltemperatur
01 05            # Mode 01, PID 05 (ECT)
Antwort: 41 05 5A    # 5A - 40 = 90°C

# Beispiel: Ladedruck
01 0B            # Mode 01, PID 0B (MAP)
Antwort: 41 0B 64    # 100 kPa
```

### Mode $22 Erweiterte DIDs abfragen

```
# Extended Session starten (manchmal erforderlich)
10 03

# Boost-Druck lesen
22 10 08
Antwort: 62 10 08 01 8C    # 284 kPa

# Wastegate-Stellung
22 10 0A
Antwort: 62 10 0A 5A    # 90%

# Turbodrehzahl
22 10 0B
Antwort: 62 10 0B 1C 20    # 7200 rpm

# Motoröltemperatur
22 10 0C
Antwort: 62 10 0C 78    # 88°C

# Kraftstoffverbrauch
22 10 18
Antwort: 62 10 18 00 2C    # 44 L/h (0.44 L/h)
```

### DTCs lesen und löschen

```
# Alle DTCs lesen (Mode 03)
03
Antwort: 43 XX XX ...    # DTCs

# DTCs löschen (Mode 04)
04
Antwort: 44

# Mit Mode $19 (UDS)
19 02 FF         # Alle DTCs mit Status
19 14 01 00     # DTC P0100详细信息
```

---

## Anhang B: Glossar

| Abkürzung | Vollständiger Name |
|-----------|-------------------|
| A14NEL | Motorcode: 1.4L Turbo, GM Family 0 |
| AFR | Air-Fuel Ratio (Luft-Kraftstoff-Verhältnis) |
| BCM | Body Control Module (Karosseriesteuergerät) |
| CAN | Controller Area Network |
| CID | Calibration Identifier |
| DID | Data Identifier |
| DLC | Data Length Code |
| DTC | Diagnostic Trouble Code |
| ECT | Engine Coolant Temperature |
| ECM | Engine Control Module |
| EGR | Exhaust Gas Recirculation |
| EOBD | European On-Board Diagnostics |
| ICC | Instrument Cluster Cluster |
| IAT | Intake Air Temperature |
| LSU | Lambda Sonde Universal (Bosch Breitbandsonde) |
| MAF | Mass Air Flow |
| MAP | Manifold Absolute Pressure |
| MIL | Malfunction Indicator Light |
| OBD | On-Board Diagnostics |
| PID | Parameter ID |
| rpm | Revolutions Per Minute |
| TCM | Transmission Control Module |
| UDS | Unified Diagnostic Services |
| VVT | Variable Valve Timing |
| WMI | World Manufacturer Identifier |

---

*Erstellt: Mai 2026*
*Letzte Aktualisierung: Mai 2026*
*Fahrzeug: Opel Astra J 2012 1.4L Turbo (A14NEL) 140 PS*
*Projekt: CAN-OP OBD - Android OBD-II Diagnose-App*
