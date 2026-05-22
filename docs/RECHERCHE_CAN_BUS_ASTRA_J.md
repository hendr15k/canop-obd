# Opel Astra J 2012 - CAN-BUS Netzwerk-Kommunikation Recherche

**Fahrzeug:** Opel Astra J 1.4 Turbo (A14NEL/LUJ), Baujahr 2012
**Erstellt:** Mai 2025
**Letzte Aktualisierung:** Mai 2025

---

## Inhaltsverzeichnis

1. [CAN-BUS Architektur](#1-can-bus-architektur)
2. [OBD-II Diagnosestecker Pinbelegung](#2-obd-ii-diagnosestecker-pinbelegung)
3. [CAN-Bus Netzwerkübersicht](#3-can-bus-netzwerkübersicht)
4. [CAN-IDs für Steuergeräte (ECU, TCM, BCM, IPC)](#4-can-ids-für-steuergeräte)
5. [Infotainment CAN-BUS Messages](#5-infotainment-can-bus-messages)
6. [Tacho/Dashboard CAN-Kommunikation](#6-tachodashboard-can-kommunikation)
7. [Lenkradtasten CAN-Signale](#7-lenkradtasten-can-signale)
8. [Klimatisierung CAN-Nachrichten](#8-klimatisierung-can-nachrichten)
9. [OBD-II CAN-BUS Adressen für Astra J](#9-obd-ii-can-bus-adressen)
10. [GM Global B-CAN Spezifische Nachrichten](#10-gm-global-b-can-spezifische-nachrichten)
11. [CAN-Bus Nachrichten-Tabelle](#11-can-bus-nachrichten-tabelle)
12. [Quellen und Referenzen](#12-quellen-und-referenzen)

---

## 1. CAN-BUS Architektur

### 1.1 Überblick

Der Opel Astra J verwendet ein **Multi-Bus CAN-Architektur** mit drei separaten CAN-Netzwerken, die über das Gateway-Steuergerät miteinander verbunden sind:

| Bus-Typ | Geschwindigkeit | Hauptfunktionen |
|---------|----------------|----------------|
| **High-Speed CAN (HS-CAN)** | 500 kbps | Motormanagement, Getriebe, ABS/ESP |
| **Medium-Speed CAN (MS-CAN)** | 125 kbps | Karosserie, Infotainment, Klimatisierung |
| **GM-LAN Single Wire** | 33 kbps | Legacy-Module, einfache Körperfunktionen |

### 1.2 CAN 2.0 Spezifikationen

- **Protokoll:** CAN 2.0A (11-bit Identifier) und CAN 2.0B (29-bit Identifier)
- **Datenrate:** 125 kbps (MS-CAN), 500 kbps (HS-CAN)
- **Maximale Payload:** 8 Bytes pro Frame
- **Termination:** 120 Ohm an beiden Enden des Bus
- **Arbitration:** Prioritätsbasierte Bit-Arbitration (niedrigere ID = höhere Priorität)

### 1.3 Architekturdiagramm

```
                    ┌─────────────────────────────────────────────────┐
                    │                 GATEWAY / BCM                     │
                    │              (Zentrales Gateway)                  │
                    └───────────────┬─────────────┬─────────────────────┘
                                    │             │
          ┌─────────────────────────┤             ├──────────────────────────┐
          │                         │             │                          │
    ┌─────▼─────┐            ┌─────▼─────┐ ┌─────▼─────┐            ┌─────▼─────┐
    │   HS-CAN  │            │   MS-CAN  │ │ GM-LAN    │            │  LIN-BUS  │
    │  (500kbps)│            │  (125kbps)│ │  (33kbps) │            │  (20kbps) │
    └───────────┘            └───────────┘ └───────────┘            └───────────┘
         │                       │             │                       │
    ┌────┴────┐            ┌────┴────┐   ┌────┴────┐             ┌────┴────┐
    │ ECU/PCM │            │   IPC   │   │  TCM    │             │ Sensoren │
    │   (E)   │            │(Tacho)  │   │(Getriebe)│             │  (L)    │
    └─────────┘            └─────────┘   └─────────┘             └─────────┘
    ┌─────────┐            ┌─────────┐   ┌─────────┐
    │  ABS/   │            │  HVAC   │   │  BCM    │
    │   ESP   │            │(Klima)  │   │(Karosse) │
    └─────────┘            └─────────┘   └─────────┘
    ┌─────────┐            ┌─────────┐
    │  EPS    │            │  Audio  │
    │(Lenkung)│            │(Radio)  │
    └─────────┘            └─────────┘
```

---

## 2. OBD-II Diagnosestecker Pinbelegung

### 2.1 Standard OBD-II Stecker (SAE J1962)

| Pin | Funktion | Geschwindigkeit | Farbe |
|-----|----------|----------------|-------|
| 1 | GM_LAN Single Wire CAN-H | 33 kbps | - |
| 2 | (nicht verwendet) | - | - |
| 3 | **Medium Speed CAN-H** | 125 kbps | Gelb |
| 4 | Fahrzeug-Masse | - | Schwarz |
| 5 | Signal-Masse | - | Schwarz/Weiß |
| 6 | **High Speed CAN-H** | 500 kbps | Grün |
| 7 | (nicht verwendet) | - | - |
| 8 | (nicht verwendet) | - | - |
| 9 | (nicht verwendet) | - | - |
| 10 | (nicht verwendet) | - | - |
| 11 | **Medium Speed CAN-L** | 125 kbps | Braun |
| 12 | (nicht verwendet) | - | - |
| 13 | (nicht verwendet) | - | - |
| 14 | **High Speed CAN-L** | 500 kbps | Grau |
| 15 | (nicht verwendet) | - | - |
| 16 | **Batterie (+12V)** | - | Rot |

### 2.2 Zugriff auf verschiedene CAN-Busse

| Ziel-Bus | Pins | Adapter-Einstellung |
|----------|------|---------------------|
| High-Speed CAN (Motor/Getriebe) | Pin 6 (H), Pin 14 (L) | ELM327 Standard |
| Medium-Speed CAN (Karosserie/Infotainment) | Pin 3 (H), Pin 11 (L) | ELM327 mit MSPROT |
| GM-LAN Single Wire | Pin 1 | Spezieller Adapter |

---

## 3. CAN-Bus Netzwerkübersicht

### 3.1 Steuergeräte am High-Speed CAN (500 kbps)

| Steuergerät | Funktion | Typische CAN-ID Range |
|-------------|----------|----------------------|
| **ECM/PCM** | Motorsteuergerät | 0x100 - 0x2FF |
| **TCM** | Getriebesteuergerät | 0x200 - 0x2FF |
| **EBCM** | Elektronische Bremssteuerung | 0x300 - 0x3FF |
| **EPS** | Elektromechanische Servolenkung | 0x400 - 0x4FF |
| **ABS** | Antiblockiersystem | 0x300 - 0x3FF |

### 3.2 Steuergeräte am Medium-Speed CAN (125 kbps)

| Steuergerät | Funktion | Typische CAN-ID Range |
|-------------|----------|----------------------|
| **IPC** | Instrumentencluster | 0x180 - 0x1FF |
| **HVAC** | Heizung/Klima/Lüftung | 0x300 - 0x3FF |
| **BCM** | Karosseriesteuergerät | 0x400 - 0x4FF |
| **Radio/Audio** | Infotainment | 0x500 - 0x5FF |
| **DIS** | Driver Information System | 0x180 - 0x1FF |
| **SWC** | Steering Wheel Control | variabel |

### 3.3 Gateway-Funktion

Das Gateway-Steuergerät verbindet die verschiedenen CAN-Busse und übersetzt bei Bedarf Nachrichten zwischen den Netzwerken. Typische Gateway-Funktionen:

- Weiterleitung von Motordrehzahl (RPM) an IPC
- Weiterleitung von Fahrgeschwindigkeit an alle relevanten Module
- Verwaltung von Bus-Sleep/Awake-Zuständen
- Fehlerspeicher-Verwaltung

---

## 4. CAN-IDs für Steuergeräte

### 4.1 Motorsteuergerät (ECM/PCM) - High-Speed CAN

| CAN-ID (Hex) | Beschreibung | Zykluszeit | Bytes |
|--------------|--------------|-------------|-------|
| **0x201** | Motordrehzahl (RPM) | 20 ms | 8 |
| **0x208** | Motordaten 1 | 50 ms | 8 |
| **0x210** | Kühlmitteltemperatur | 100 ms | 8 |
| **0x2C0** | Kraftstoffeinspritzung | 20 ms | 8 |
| **0x2E0** | Drosselklappe | 50 ms | 8 |
| **0x300** | Fahrzeuggeschwindigkeit (VSS) | 20 ms | 8 |
| **0x308** | Gangposition (Auto) | 100 ms | 8 |

### 4.2 Instrumentencluster (IPC) - Medium-Speed CAN

| CAN-ID (Hex) | Beschreibung | Zykluszeit | Bytes |
|--------------|--------------|-------------|-------|
| **0x180** | Uhrzeit/Datum | 1 s | 8 |
| **0x188** | Tageskilometerstand | 1 s | 8 |
| **0x682** | Außentemperatur Sensor | 1 s | 8 |
| **0x683** | Außentemperatur Anzeige | 1 s | 8 |
| **0x68C** | Kraftstofffüllstand | 1 s | 8 |
| **0x4E8** | Fahrzeuggeschwindigkeit | 50 ms | 8 |

### 4.3 Body Control Module (BCM) - Medium-Speed CAN

| CAN-ID (Hex) | Beschreibung | Zykluszeit | Bytes |
|--------------|--------------|-------------|-------|
| **0x3C0** | Türstatus | 500 ms | 8 |
| **0x3C8** | Lichtstatus | 500 ms | 8 |
| **0x3D0** | Zentralverriegelung | Event | 8 |
| **0x400** | Klimastatus | 100 ms | 8 |
| **0x408** | Scheibenwischer | Event | 8 |
| **0x480** | Wegfahrsperre | Event | 8 |

### 4.4 Getriebesteuergerät (TCM) - High-Speed CAN

| CAN-ID (Hex) | Beschreibung | Zykluszeit | Bytes |
|--------------|--------------|-------------|-------|
| **0x202** | Getriebedaten | 50 ms | 8 |
| **0x210** | Gangposition | 100 ms | 8 |
| **0x220** | Drehmoment-Info | 50 ms | 8 |

---

## 5. Infotainment CAN-BUS Messages

### 5.1 Radio/Headunit CAN-Kommunikation

Das Astra J Infotainmentsystem (CD30/CD40/CD70 Navi) kommuniziert über den **MS-CAN (125 kbps)** mit anderen Steuergeräten.

#### Wichtige CAN-IDs für Infotainment:

| CAN-ID (Hex) | Quelle | Beschreibung | Zykluszeit |
|--------------|--------|--------------|-------------|
| **0x518** | Radio | Display-Text-Anfrage | Event |
| **0x520** | Radio | Audio-Quelle Status | 100 ms |
| **0x528** | Display | Anzeige-Acknowledge | Event |
| **0x530** | HVAC | Klimadaten an Display | 500 ms |
| **0x538** | IPC | Tachodaten an Display | 100 ms |

#### Display-Text Format (CID/GID/TID):

Das Display empfängt Textnachrichten über spezielle CAN-Frames. Die Textausgabe erfolgt über:
- **Artist Name**
- **Titel**
- **Album**

Text wird als 8-Byte Blöcke übertragen mit folgender Struktur:
```
Byte 0: Message Type (z.B. 0x01 für Track Info)
Byte 1: Position im Text-Buffer
Byte 2-7: Zeichen (max 6 Zeichen pro Frame)
```

### 5.2 Bluetooth-Audio Integration (EHU32 Projekt)

Das Open-Source Projekt EHU32 ermöglicht Bluetooth-Audio-Integration über den MS-CAN. Relevante Messages:

| CAN-ID (Hex) | Richtung | Funktion |
|--------------|----------|----------|
| **0x3B0** | → Display | Klimadaten anzeigen |
| **0x3B8** | → Display | AUX-Modus-Texte |
| **0x4E8** | ← Fahrzeug | Geschwindigkeit |
| **0x4EC** | ← Motor | Kühlmitteltemperatur |
| **0x0x340** | ← BCM | Batteriespannung |

### 5.3 Headunit Discovery Messages

Beim Start sendet das Radio Discovery-Nachrichten um angeschlossene Module zu identifizieren:

| CAN-ID (Hex) | Byte 0 | Beschreibung |
|--------------|--------|--------------|
| **0x500** | 0x01 | Headunit-Präsenz |
| **0x502** | 0x02 | Display-Anfrage |
| **0x504** | 0x03 | Klimamodul-Präsenz |

---

## 6. Tacho/Dashboard CAN-Kommunikation

### 6.1 Instrument Cluster (IPC) Messages

Das IPC empfängt und sendet folgende wichtige Nachrichten:

| CAN-ID (Hex) | Nachrichtenbeschreibung | Signalnamen | Bits | Zykluszeit |
|--------------|------------------------|-------------|------|------------|
| **0x180** | Echtzeituhr | Jahr, Monat, Tag, Stunde, Minute, Sekunde | 8 Bit je Feld | 1 s |
| **0x188** | Tageskilometer | Distance_Low, Distance_High | 16 Bit | 1 s |
| **0x4E8** | Fahrzeuggeschwindigkeit | Speed, Direction, Moving | 8 Bit, 2 Bit, 2 Bit | 50 ms |
| **0x4EC** | Motortemperatur | Engine_Temp = Byte2/2 - 40 | 8 Bit | 100 ms |
| **0x68C** | Kraftstoffstand | Fuel_Level = 94 - (Byte4/2) | 8 Bit | 1 s |

### 6.2 Geschwindigkeits-Dekodierung (CAN-ID 0x4E8)

```
Byte 0: 0x46 (Statischer Header)
Byte 1: 0x0F (Unbekannt)
Byte 2: RPM High (Motorordrehzahl)
Byte 3: RPM Low
Byte 4: Speed (Fahrgeschwindigkeit in km/h)
Byte 5: Status-Bits (Bewegung, Richtung)
Byte 6: Richtung (0x01 = Stehen, Bit2 = Bewegung, Bit3 = Rückwärts)
Byte 7: -
```

### 6.3 Kraftstoffstand-Dekodierung (CAN-ID 0x68C)

```
Byte 0: 0x46 (Statischer Header)
Byte 1: 0x01 (Unbekannt)
Byte 2: Fuel_Sensor
Byte 3: -
...
Kraftstoff (Liter) = 94 - (Byte4 / 2)

Hinweis: Die Berechnung kann je nach Sensor variieren, manchmal invertiert.
```

### 6.4 Tachometer-Datenfluss

```
Motor-ECU (0x201 RPM)
       │
       ▼
   GATEWAY
       │
       ├──────────────────────────► IPC (0x4E8)
       │                                    │
       │                              ┌─────▼─────┐
       │                              │  Tacho    │
       │                              │  Display  │
       │                              └───────────┘
       │
       └──────────────────────────► ABS/ESP (Radsensoren)
                                        │
                                        ▼
                                   Geschwindigkeit
```

---

## 7. Lenkradtasten CAN-Signale

### 7.1 Lenkradtasten-CAN-Frames

Die Lenkradtasten senden ihre Zustände über den **SWC (Steering Wheel Control)** Bus oder direkt auf dem MS-CAN.

| CAN-ID (Hex) | Beschreibung | Button-Codes |
|--------------|--------------|--------------|
| **0x3A0** | Lenkradtasten Status | Button ID + Status |
| **0x3A8** | Lenkradtasten Wiederholung | Wiederholungsrate |

### 7.2 Typische Button-Codes

| Taste | Byte-Wert | Funktion |
|-------|-----------|----------|
| VOL+ | 0x01 | Lauter |
| VOL- | 0x02 | Leiser |
| MUTE | 0x04 | Stumm |
| NEXT | 0x08 | Nächster Titel |
| PREV | 0x10 | Vorheriger Titel |
| PHONE | 0x20 | Telefon |
| VOICE | 0x40 | Sprachsteuerung |
| SRC | 0x80 | Quelle wechseln |

### 7.3 Lenkradtasten-Dekodierung (CAN-ID 0x3A0)

```
Byte 0: Button_ID (welche Taste)
Byte 1: Status (0x00 = Released, 0xFF = Pressed)
Byte 2: ?
Byte 3: ?
Byte 4: ?
Byte 5: ?
Byte 6: ?
Byte 7: ?
```

### 7.4 Integration mit EHU32 Projekt

Das EHU32-Projekt ermöglicht die Lenkradtasten-Auswertung für:
- **Play/Pause** Steuerung
- **Next/Previous Track** Navigation
- **Volume** Anpassung
- **Source** Wechsel

---

## 8. Klimatisierung CAN-Nachrichten

### 8.1 HVAC CAN-Frames

| CAN-ID (Hex) | Beschreibung | Zykluszeit | Bytes |
|--------------|--------------|-------------|-------|
| **0x340** | Klimastatus | 100 ms | 8 |
| **0x348** | Lüftergeschwindigkeit | 100 ms | 8 |
| **0x350** | Temperatur-Einstellung | Event | 8 |
| **0x358** | Klappen-Positionen | 500 ms | 8 |
| **0x360** | Kompressor-Status | 100 ms | 8 |

### 8.2 Klimadaten-Dekodierung (CAN-ID 0x340)

```
Byte 0: Klimamodus (Auto/Manuell)
Byte 1: Gebläsegeschwindigkeit (0-100%)
Byte 2: Fahrertemperatur-Sollwert
Byte 3: Beifahrertemperatur-Sollwert
Byte 4: Status-Bits (AC An/Aus, Recirc, etc.)
Byte 5: ?
Byte 6: ?
Byte 7: ?
```

### 8.3 Außentemperatur-Daten

Die Außentemperatur wird über den MS-CAN übertragen:

| CAN-ID (Hex) | Byte | Format | Einheit |
|--------------|------|--------|---------|
| **0x682** | Byte 2 | Raw / 2 - 40 | °C |
| **0x683** | Byte 2 | Raw / 2 - 40 | °C (Display) |

### 8.4 AC-Steuerung

| Byte | Bit | Funktion |
|------|-----|----------|
| Byte 4 | Bit 0 | Kompressor An/Aus |
| Byte 4 | Bit 1 | Umluft An/Aus |
| Byte 4 | Bit 2 | Defrost vorne |
| Byte 4 | Bit 3 | Defrost hinten |
| Byte 4 | Bit 4 | Sitzheizung Fahrer |
| Byte 4 | Bit 5 | Sitzheizung Beifahrer |

---

## 9. OBD-II CAN-BUS Adressen

### 9.1 Standard OBD-II Kommunikation (HS-CAN)

Der Opel Astra J verwendet für die OBD-II Diagnose den **High-Speed CAN (500 kbps)** auf Pin 6/14.

#### Wichtige Diagnose-CAN-IDs:

| CAN-ID (Hex) | Funktion | Beschreibung |
|--------------|----------|--------------|
| **0x7E8** | ECU Antwort (physikalisch) | Engine ECU Response |
| **0x7E9** | ECU Antwort (funktional) | Functional Address |
| **0x7EA** | ECU Antwort (erweitert) | Extended Response |
| **0x7E0** | Tester Anfrage (physikalisch) | Physical Request |
| **0x7DF** | Tester Anfrage (funktional) | Functional Request |

### 9.2 ISO 15765-4 Format

Die OBD-II Kommunikation folgt ISO 15765-4 mit folgenden Parametern:

- **Protokoll:** ISO 15765-4 (CAN)
- **Baudrate:** 500 kbps
- **CAN-IDs:** 11-bit (Standard)
- **Frames:** Multi-Frame (Consecutive Frames) für Daten > 7 Bytes
- **Timing:** 50 ms zwischen Frames

### 9.3 Diagnose-Services

| Mode | Service | Verwendungszweck |
|------|---------|------------------|
| $01 | Show Current Data | Live-Daten (PIDs 0x01-0x20) |
| $02 | Freeze Frame | Fehlerspeicher-Snapshot |
| $03 | Stored DTCs | Diagnose-Fehlercodes auslesen |
| $04 | Clear DTCs | Fehlercodes löschen |
| $06 | Test Results | Lambda/O2-Sensor Tests |
| $07 | Pending DTCs | Vorläufige Fehlercodes |
| $09 | Request Vehicle Info | VIN, Kalibrierungen |
| $22 | Read Data By Identifier | Herstellerspezifisch (GM) |

### 9.4 GM Mode 22 (Service $22) Extended PIDs

Der Astra J mit Bosch ME17.9.22 ECU unterstützt erweiterte Mode 22 PIDs:

| PID Code | Beschreibung | Bytes | Einheit | Formel |
|----------|--------------|-------|---------|--------|
| **221001** | Engine Torque | 1 | % | (A - 128) |
| **221002** | Requested Torque | 1 | % | (A - 128) |
| **221008** | Boost Pressure Actual | 2 | kPa | 256*A + B |
| **221009** | Boost Pressure Target | 2 | kPa | 256*A + B |
| **22100A** | Wastegate Position | 1 | % | A |
| **22100B** | Turbo RPM | 2 | rpm | 256*A + B |
| **22100C** | Oil Temperature | 1 | °C | A - 40 |
| **22100D** | Coolant Temperature | 1 | °C | A - 40 |
| **22100E** | Intake Air Temp | 1 | °C | A - 40 |
| **22100F** | Fuel Rail Pressure | 2 | kPa | (256*A + B) * 10 |
| **221010** | Injector Pulse Width | 2 | ms | (256*A + B) / 100 |
| **221015** | VVT Intake | 1 | ° | (A - 128) |
| **221016** | VVT Exhaust | 1 | ° | (A - 128) |
| **221018** | Fuel Consumption Instant | 2 | L/h | 256*A + B |
| **22101A** | Fuel Consumption Average | 2 | L/100km | (256*A + B) / 10 |
| **22101F** | AFR Ratio | 2 | λ | 2*(256*A + B) / 65536 |

---

## 10. GM Global B-CAN Spezifische Nachrichten

### 10.1 GM-LAN Protokoll Übersicht

GM-LAN ist GMs herstellerspezifische Implementierung des CAN-Bus Protokolls mit erweiterten Funktionen:

- **Erweiterte Nachrichtenformate**
- **Broadcast-Kommunikation**
- **Herstellerspezifische Zeitstempel**
- **Security-Zugriff für kodierte Funktionen**

### 10.2 GM Global B-CAN Nachrichtenformat

GM B-CAN Nachrichten verwenden ein erweitertes Format mit Time-Stamp und Message-Counter:

```
Byte 0: Message Counter
Byte 1: Time High
Byte 2: Time Low
Byte 3-N: Nutzdaten
```

### 10.3 GM-Spezifische Nachrichten-IDs

| CAN-ID (Hex) | Quelle/Ziel | Beschreibung |
|--------------|--------------|--------------|
| **0x100** | ECM | Basis-Motordaten |
| **0x1F0** | IPC | Tacho-Beleuchtung |
| **0x200** | BCM | Zentralverriegelung |
| **0x2F0** | TPM | Reifendruck |
| **0x300** | ABS | Bremsstatus |
| **0x3F0** | HVAC | Klimasteuerung |
| **0x4F0** | Cluster | Diagnose-Info |
| **0x5F0** | Radio | Audio-System |

### 10.4 GM Vehicle Information Messages

| CAN-ID (Hex) | Nachricht | Inhalt |
|--------------|-----------|--------|
| **0xF4** | VIN Request | Fahrzeug-Identifikation |
| **0xF5** | VIN Response | 17-stellige VIN |
| **0xF8** | Configuration | Fahrzeugkonfiguration |
| **0xFA** | Security Access | Zugriff auf kodierte Funktionen |

---

## 11. CAN-Bus Nachrichten-Tabelle

### 11.1 Zusammenfassung aller bekannten CAN-IDs

| CAN-ID (Hex) | Bus | Nachrichtenbeschreibung | Signalnamen und Bits | Zykluszeit | Quellen |
|--------------|-----|------------------------|---------------------|------------|---------|
| **0x180** | MS-CAN | Echtzeituhr | Byte2: Jahr, Byte3: Monat, Byte4: Tag/Stunde, Byte5: Stunde/Min, Byte6: Sek | 1000 ms | JJToB/Car-CAN-Message-DB |
| **0x188** | MS-CAN | Tageskilometerstand | Byte3-4: Distance*1.5748=cm, Byte5-6: Distance*1.5748=cm | 1000 ms | JJToB/Car-CAN-Message-DB |
| **0x201** | HS-CAN | Motordrehzahl | Byte0-1: RPM (Motor RPM) | 20 ms | OBD-II Standard |
| **0x202** | HS-CAN | Getriebedaten | Byte0: Gang, Byte1-2: Drehzahl | 50 ms | OBD-II Standard |
| **0x208** | HS-CAN | Motordaten 1 | Diverse Motordaten | 50 ms | OBD-II Standard |
| **0x210** | HS-CAN | Kühlmitteltemperatur | Byte2: Temp/2-40=°C | 100 ms | OBD-II Standard |
| **0x2C0** | HS-CAN | Kraftstoffeinspritzung | Byte0-1: Injectors | 20 ms | OBD-II Standard |
| **0x2E0** | HS-CAN | Drosselklappe | Byte0: Throttle Position | 50 ms | OBD-II Standard |
| **0x300** | HS-CAN | Fahrzeuggeschwindigkeit | Byte0-1: VSS (km/h) | 20 ms | OBD-II Standard |
| **0x340** | MS-CAN | Klimastatus | Byte0: Mode, Byte1: Fan, Byte4: AC Status | 100 ms | EHU32 Projekt |
| **0x348** | MS-CAN | Lüftergeschwindigkeit | Byte0: Fan Speed (0-100%) | 100 ms | EHU32 Projekt |
| **0x3A0** | MS-CAN | Lenkradtasten | Byte0: Button ID, Byte1: Status | Event | EHU32 Projekt |
| **0x3C0** | MS-CAN | Türstatus | Byte0: Door Status Bits | 500 ms | CAN_Hacking |
| **0x3C8** | MS-CAN | Lichtstatus | Byte0: Light Status | 500 ms | CAN_Hacking |
| **0x400** | MS-CAN | BCM Klimadaten | Diverse Klimadaten | 100 ms | Opel Workshop |
| **0x4E8** | MS-CAN | Fahrzeugbewegung | Byte2-3: RPM, Byte4: Speed, Byte5: Direction, Byte6: Moving | 50 ms | JJToB/Car-CAN-Message-DB |
| **0x4EC** | MS-CAN | Motortemperatur | Byte2: Engine Temp/2-40=°C | 100 ms | JJToB/Car-CAN-Message-DB |
| **0x4ED** | MS-CAN | Kraftstoffeinspritzung | Byte2-3: Injection Count | Event | JJToB/Car-CAN-Message-DB |
| **0x4EE** | MS-CAN | Reichweite | Byte2-3: Range*0.5=km | Event | JJToB/Car-CAN-Message-DB |
| **0x518** | MS-CAN | Display Text Anfrage | Byte0: Type, Byte1: Pos, Byte2-7: Text | Event | EHU32 Projekt |
| **0x520** | MS-CAN | Audio Quelle Status | Byte0: Source, Byte1: Status | 100 ms | EHU32 Projekt |
| **0x530** | MS-CAN | Klimadaten an Display | Diverse Klimawerte | 500 ms | EHU32 Projekt |
| **0x538** | MS-CAN | Display Acknowledge | Byte0: Status | Event | EHU32 Projekt |
| **0x682** | MS-CAN | Sensor Temperatur | Byte2: Temp/2-40=°C (Sensor) | 1000 ms | JJToB/Car-CAN-Message-DB |
| **0x683** | MS-CAN | Display Temperatur | Byte2: Temp/2-40=°C (Anzeige) | 1000 ms | JJToB/Car-CAN-Message-DB |
| **0x68C** | MS-CAN | Kraftstofffüllstand | Byte2: Fuel Sensor, Byte4: 94-(X/2)=Liter | 1000 ms | JJToB/Car-CAN-Message-DB |
| **0x7E8** | HS-CAN | OBD-II ECU Antwort | ISO 15765-4 Response | Event | OBD-II Standard |
| **0x7E0** | HS-CAN | OBD-II Tester Anfrage | ISO 15765-4 Request | Event | OBD-II Standard |
| **0x7DF** | HS-CAN | OBD-II Funktionale Anfrage | ISO 15765-4 Functional | Event | OBD-II Standard |

---

## 12. Quellen und Referenzen

### 12.1 GitHub Repositories

| Repository | Beschreibung | URL |
|------------|--------------|-----|
| **C-X1/CAN_Hacking** | Opel/Vauxhall Astra J CAN Message Reverse Engineering | https://github.com/C-X1/CAN_Hacking |
| **PNKP237/EHU32** | ESP32 Bluetooth Audio Integration für Opel/Vauxhall | https://github.com/PNKP237/EHU32 |
| **JJToB/Car-CAN-Message-DB** | Umfassende CAN-Bus Message Datenbank | https://github.com/JJToB/Car-CAN-Message-DB |
| **xymetox/OpelZafiraB_CanBus_dbc** | DBC-Dateien für Opel Zafira B CAN | https://github.com/xymetox/OpelZafiraB_CanBus_dbc |
| **sepp89117/Opel-Astra-H-opc-CAN-Gauge** | Arduino CAN Gauge für Astra H OPC | https://github.com/sepp89117/Opel-Astra-H-opc-CAN-Gauge |
| **ManuelW77/opel-astra-h-odb2-display** | OBD2 Display für Astra H | https://github.com/ManuelW77/opel-astra-h-odb2-display |

### 12.2 Technische Standards

| Standard | Beschreibung | Anwendung |
|----------|--------------|-----------|
| **SAE J1979** | OBD-II PID Definitionen | Standard Diagnose-PIDs |
| **SAE J2190** | Enhanced Diagnostic Services | Mode $22 Erweiterungen |
| **ISO 11898** | CAN Physical Layer | HS-CAN (500kbps) |
| **ISO 15765-4** | CAN Diagnostic Communication | OBD-II Protokoll |
| **SAE J1939** | Truck/Bus Diagnostic | Nutzfahrzeug-Diagnose |

### 12.3 Werkzeuge und Hardware

| Werkzeug | Verwendung | CAN-Unterstützung |
|----------|------------|-------------------|
| **OBDLink MX+** | Bluetooth OBD-II | HS-CAN, MS-CAN (konfigurierbar) |
| **ELM327 (v2.1+)** | Bluetooth/USB OBD-II | HS-CAN only (Standard) |
| **STN11xx/STN22xx** | Advanced CAN | HS, MS, LS CAN |
| **ESP32 + MCP2551** | Custom Development | Full flexibility |
| **OpCom** | Opel Diagnose | Proprietär |
| **Tech 2 / MDI2** | GM Werkstatt-Diagnose | Vollständig |

### 12.4 Foren und Communities

- **opel-power.at** - Opel-Enthusiasten-Forum (teilweise offline)
- **astra-j.de** - Deutsche Astra J Community
- **Motor-Talk.de** - Deutschsprachiges Auto-Forum
- **Vauxhall Owners Network** - UK Vauxhall-Forum
- **GitHub C-X1/CAN_Hacking** - CAN Reverse Engineering Community

---

## Anhang A: DBC-Datei Referenz

### Opel Astra J MS-CAN (125 kbps) Message Definitionen

```
VERSION=""
NS_ :
BS_:
BU_: ECU IPC BCM HVAC RADIO

BO_ 384 IPC_4E8: 8 IPC
 SG_ Speed : 4|8@1- (1,0) [0|255] "km/h"
 SG_ RPM : 2|16@1- (1,0) [0|65535] "rpm"
 SG_ Direction : 6|2@1- (1,0) [0|3] ""
 SG_ Moving : 6|2@1- (1,0) [0|3] ""

BO_ 899 IPC_683: 8 IPC
 SG_ Temp : 16|8@1- (0.5, -40) [-40|215] "°C"

BO_ 960 BCM_400: 8 BCM
 SG_ AC_Request : 32|1@1- (1,0) [0|1] ""
 SG_ Fan_Speed : 8|8@1- (1,0) [0|100] "%"
```

---

## Anhang B: Troubleshooting

### Häufige Probleme und Lösungen

| Problem | Ursache | Lösung |
|---------|---------|--------|
| Keine OBD-II Antwort | Falsche Baudrate | Auf 500 kbps einstellen |
| Nur Motor-IDs sichtbar | Auf falschem Bus | MS-CAN (125 kbps) für Karosserie |
| Lenkradtasten funktionieren nicht | Bus nicht verbunden | Direkte Verbindung zum Radio |
| Display zeigt keine Texte | CAN-ID Filter | 0x518 auf dem MS-CAN aktivieren |

---

## Haftungsausschluss

**WICHTIG:** Diese Dokumentation dient ausschließlich zu Informationszwecken und Forschungszwecken. Die hier enthaltenen Informationen wurden aus öffentlich zugänglichen Quellen zusammengestellt. Viele GM/Opel-spezifische Kodierungswerte und Verfahren erfordern:

- Händler-Level Diagnosegeräte
- Sicherheits-Zugriffscodes
- Spezifische Fahrzeugkonfigurationsdaten
- Direkten Foren-Zugang

Die Verwendung dieser Informationen zur Fahrzeugmodifikation erfolgt auf eigene Gefahr. Unsachgemäße BCM- oder ECU-Programmierung kann zu Fahrzeugfehlfunktionen oder -ausfällen führen.

---

**Dokumentversion:** 1.0
**Forschungsdatum:** Mai 2025
**Zielgruppe:** Opel Astra J CAN-Bus Hacker und OBD-App Entwickler
