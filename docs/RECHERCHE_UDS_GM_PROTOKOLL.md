# UDS & GM-spezifische Diagnoseprotokolle - Opel Astra J 2012 1.4 Turbo

> Umfassende Recherche zu Unified Diagnostic Services (UDS), GM-spezifischen Protokollen und technischen Details für den Opel Astra J (A14NEL, 140 PS).

---

## INHALTSVERZEICHNIS

1. [Überblick & Protokollhierarchie](#1-überblick--protokollhierarchie)
2. [CAN-BUS Konfiguration für Astra J](#2-can-bus-konfiguration-für-astra-j)
3. [Diagnoseadressen und CAN-IDs](#3-diagnoseadressen-und-can-ids)
4. [UDS Sessions und Ablauf](#4-uds-sessions-und-ablauf)
5. [UDS Dienste (Services) im Detail](#5-uds-dienste-services-im-detail)
6. [Seed-Key Sicherheitsmechanismus](#6-seed-key-sicherheitsmechanismus)
7. [DTC Format (Diagnostic Trouble Codes)](#7-dtc-format-diagnostic-trouble-codes)
8. [GM-spezifische Protokolle (HOBD/MOPED)](#8-gm-spezifische-protokolle-hobdmoped)
9. [Steuergeräte-Adressen](#9-steuergeräte-adressen)
10. [Typische Diagnoseabläufe](#10-typische-diagnoseabläufe)
11. [Referenzen und Quellen](#11-referenzen-und-quellen)

---

## 1. Überblick & Protokollhierarchie

### Architektur

Der Opel Astra J (Delta B, 2009-2015) verwendet eine mehrschichtige Diagnosearchitektur:

```
ISO 14229 (UDS)          ← Anwendungsschicht (Layer 7)
    ↓
ISO 15765 (DoCAN)        ← Transportschicht (Layer 4)
    ↓
ISO 11898 (CAN)          ← Datenlinkschicht (Layer 2)
    ↓
Physikalisches CAN-Bus   ← Physische Schicht (Layer 1)
```

### Protokoll-Versionen

| Protokoll | Version | Verwendung |
|-----------|---------|------------|
| UDS | ISO 14229-1:2013 | Hauptspezifikation |
| DoCAN | ISO 15765-2 | CAN-Transportprotokoll |
| CAN | ISO 11898-1:2015 | Physikschicht |
| OBD-II | SAE J1979 | Emissionsdiagnose |
| KWP2000 | ISO 14230 | Legacy (nicht aktiv) |

### GM Delta Delta CAN Delta Protokoll

Der Opel Astra J nutzt das GM-eigene "Delta CAN" Protokoll, das auf ISO 15765 basiert aber GM-spezifische Erweiterungen enthält:

- **Transportprotokoll**: ISO-TP (ISO 15765-2) mit Single-Frame und Multi-Frame
- **Korrekturparameter**: CAN-FD wird **nicht** unterstützt (Classical CAN)
- **Tester-Adresse**: 0xF1 (Standard für Opel/GM)

---

## 2. CAN-BUS Konfiguration für Astra J

### Highspeed-CAN (Powertrain)

| Parameter | Wert |
|-----------|------|
| Geschwindigkeit | **500 kbit/s** |
| CAN-ID (Tester → ECU) | 0x7E0 (Motor) |
| CAN-ID (ECU → Tester) | 0x7E8 |
| CAN-ID (Tester → BCM) | 0x7C0 |
| CAN-ID (BCM → Tester) | 0x7C8 |
| Terminierung | 120Ω (beide Enden) |
| Bussenkung | Differential (CAN_H, CAN_L) |

### Lowspeed-CAN (Body/Komfort)

| Parameter | Wert |
|-----------|------|
| Geschwindigkeit | **125 kbit/s** |
| CAN-ID (Tester → BCM) | 0x7C0 |
| CAN-ID (BCM → Tester) | 0x7C8 |
| Terminierung | 100Ω |

### Diagnose-CAN (Diag)

| Parameter | Wert |
|-----------|------|
| Geschwindigkeit | **500 kbit/s** |
| Physical Request ID | 0x7E0 |
| Physical Response ID | 0x7E8 |
| Functional Request ID | 0x7DF (Broadcast) |
| Tester Address | 0xF1 |
| ECU Address | 0x10 |

### OBD-II Anschluss

| Pin | Signal |
|-----|--------|
| 4 | Chassis Ground |
| 5 | Signal Ground |
| 6 | CAN High (500 kbit/s) |
| 7 | K-Line (ISO 9141) - nicht aktiv |
| 14 | CAN Low (500 kbit/s) |
| 16 | Battery +12V |

---

## 3. Diagnoseadressen und CAN-IDs

### ECU-Adressen (Modul-Adressen)

| Modul | Name | CAN-ID Request | CAN-ID Response | Diag-Adresse |
|-------|------|----------------|-----------------|--------------|
| **ECM** | Engine Control Module | 0x7E0 | 0x7E8 | 0x01 |
| **TCM** | Transmission Control Module | 0x7E1 | 0x7E9 | 0x02 |
| **BCM** | Body Control Module | 0x7C0 | 0x7C8 | 0xFF |
| **IPC** | Instrument Panel Cluster | 0x7C3 | 0x7CB | 0x83 |
| **CIM** | Column Integration Module | 0x7C1 | 0x7C9 | 0x7E |
| **ABS** | ABS/ESP Module | 0x7C2 | 0x7CA | 0x09 |
| **UEC** | Underhood Electrical Center | 0x7C4 | 0x7CC | 0x09 |
| **REC** | Rear Electrical Center | 0x7C6 | 0x7CE | 0x2E |
| **SRS** | Airbag Module | 0x7C5 | 0x7CD | 0x10 |
| **ICM** | Infotainment Control Module | 0x7C8 | 0x7D0 | 0x06 |

### CAN-ID Matrix für Diagnose

```
Tester (0xF1) → ECU (0x10):
  Request:   0x7E0 + ECU-Adresse
  Response:  0x7E8 + ECU-Adresse

Beispiel ECM (Motor):
  Request:   0x7E0
  Response:  0x7E8

Beispiel BCM:
  Request:   0x7C0
  Response:  0x7C8
```

### Functional vs. Physical Request

| Typ | CAN-ID | Verwendung |
|-----|--------|------------|
| **Physical** | 0x7E0-0x7EF | Spezifisches ECU ansprechen |
| **Functional** | 0x7DF | Broadcast an alle ECUs |

---

## 4. UDS Sessions und Ablauf

### Session-Types

| Session | Sub-Function | Beschreibung |
|---------|--------------|--------------|
| **Default Session** | 0x01 | Standard-Modus, nur Lesen |
| **Extended Session** | 0x03 | Erweiterte Diagnose, Schreibzugriff |
| **Programming Session** | 0x02 | Programmierung/Flashing |
| **EOL Session** | 0x04 | End-of-Line (Werkseinstellung) |

### Session-Ablaufdiagramm

```
┌─────────────────┐
│  Default (0x01) │ ← Startzustand
└────────┬────────┘
         │ 0x10 0x03
         ▼
┌─────────────────┐
│  Extended (0x03) │
└────────┬────────┘
         │ 0x10 0x02
         ▼
┌─────────────────┐
│Programming (0x02)│
└─────────────────┘
```

### Session-Wechsel Befehle

```
Request:  10 03          (DiagnosticSessionControl → Extended)
Response: 50 03 [Timing] (Positive Response)

Request:  10 02          (DiagnosticSessionControl → Programming)
Response: 50 02 [Timing] (Positive Response)

Request:  10 01          (DiagnosticSessionControl → Default)
Response: 50 01 [Timing] (Positive Response)
```

### Timing-Parameter

| Parameter | Extended | Programming |
|-----------|----------|-------------|
| P2 (Server Response Time) | 50 ms | 50 ms |
| P2* (Time after Neg. Response) | 5000 ms | 5000 ms |
| S3 (Session Timeout) | 5000 ms | - |

---

## 5. UDS Dienste (Services) im Detail

### Service 0x10 - DiagnosticSessionControl

**Zweck:** Session wechseln

```
Request:  10 [Session-ID]
Response: 50 [Session-ID] [P2] [P2*]

Session-IDs:
  0x01 = Default Session
  0x02 = Programming Session
  0x03 = Extended Session
  0x04 = EOL Session
```

**Beispiel:**
```
Request:  10 03
Response: 50 03 00 19 01 F4
```

### Service 0x11 - ECUReset

**Zweck:** ECU neu starten

```
Request:  11 [Reset-Type]
Response: 51 [Reset-Type]

Reset-Typen:
  0x01 = Hard Reset
  0x02 = Key Off/On Reset
  0x03 = Soft Reset
```

**Beispiel (Hard Reset):**
```
Request:  11 01
Response: 51 01
```

### Service 0x14 - ClearDiagnosticInformation

**Zweck:** DTCs löschen

```
Request:  14 [DTC-High] [DTC-Mid] [DTC-Low]
Response: 54

DTC-Werte:
  00 00 00 = Alle DTCs löschen
  FFFF FF = Alle DTCs (spezifisch)
```

**Beispiel (Alle DTCs löschen):**
```
Request:  14 FF FF FF
Response: 54
```

### Service 0x19 - ReadDTCInformation

**Zweck:** DTC-Informationen lesen

```
Request:  19 [Sub-Function] [DTC-Mask]
Response: 59 [Sub-Function] [Status] [DTCs...]

Sub-Function:
  0x01 = reportNumberOfDTCByStatusMask
  0x02 = reportDTCByStatusMask
  0x03 = reportDTCSnapshotIdentification
  0x04 = reportDTCSnapshotRecordByDTCNumber
  0x06 = reportDTCExtDataRecordByDTCNumber
  0x0A = reportSupportedDTCs
```

**Beispiel (Alle DTCs lesen):**
```
Request:  19 02 FF
Response: 59 02 [Status] [DTC1] [Status1] [DTC2] [Status2] ...
```

### Service 0x22 - ReadDataByIdentifier

**Zweck:** DID-basierte Daten lesen

```
Request:  22 [DID-High] [DID-Low]
Response: 62 [DID-High] [DID-Low] [Data...]

Wichtige DIDs für Astra J:
  F1 80 = System Name/Engine Type
  F1 86 = Active Diagnostic Session
  F1 87 = Vehicle Manufacturer ECU Software Number
  F1 88 = Vehicle Manufacturer ECU Software Version
  F1 89 = System Supplier ECU Hardware Number
  F1 8A = System Supplier ECU Hardware Version
  F1 8B = System Supplier ECU Serial Number
  F1 8C = Vehicle Manufacturer ECU Serial Number
  F1 90 = VIN (Vehicle Identification Number)
  F1 93 = System Supplier ECU Software Number
  F1 95 = System Supplier ECU Software Version
  F1 9A = Application Software Identification
  F1 9B = Application Software Version
  01 01 = DTC Nummer
  01 02 = DTC Status
  01 03 = DTC Count
  01 80 = Read Memory By Address
  02 00 = Engine RPM
  02 01 = Kühlmitteltemperatur
  02 02 = Fahrzeuggeschwindigkeit
  02 03 = Kraftstoffdruck
  02 04 = Drosselklappenposition
```

**Beispiel (VIN lesen):**
```
Request:  22 F1 90
Response: 62 F1 90 [VIN 17 Bytes]
```

### Service 0x27 - SecurityAccess

**Zweck:** Sicherheitsfreigabe (Seed-Key)

```
Request:  27 [Sub-Function] [Seed/Key]
Response: 67 [Sub-Function] [Seed/Key]

Sub-Function:
  0x01 = Seed Request (Level 1)
  0x02 = Key Response (Level 1)
  0x03 = Seed Request (Level 2)
  0x04 = Key Response (Level 2)
```

**Ablauf:**
```
Tester:  27 01                (Seed anfordern)
ECU:     67 01 [4-Byte Seed]  (Seed senden)

Tester:  27 02 [4-Byte Key]   (Key berechnen und senden)
ECU:     67 02                (Freigabe)
```

**Negativer Response:**
```
ECU:  7F 27 35  (invalidKey)
ECU:  7F 27 36  (exceedNumberOfAttempts)
ECU:  7F 27 37  (requiredTimeDelayNotExpired)
```

### Service 0x2E - WriteDataByIdentifier

**Zweck:** DID-basierte Daten schreiben

```
Request:  2E [DID-High] [DID-Low] [Data...]
Response: 6E [DID-High] [DID-Low]
```

**Beispiel (Steuergeräte-Konfiguration):**
```
Request:  2E F1 90 [VIN-Daten]
Response: 6E F1 90
```

### Service 0x3E - TesterPresent

**Zweck:** Session aktiv halten

```
Request:  3E [Sub-Function]
Response: 7E [Sub-Function]

Sub-Function:
  0x00 = No Response (empfohlen)
  0x01 = Response erwartet
```

**Beispiel (alle 2-3 Sekunden senden):**
```
Request:  3E 00
Response: 7E 00
```

### Service 0x28 - CommunicationControl

**Zweck:** Kommunikation steuern

```
Request:  28 [Sub-Function] [Communication-Type]
Response: 68 [Sub-Function]

Sub-Function:
  0x00 = Enable Rx and Tx
  0x01 = Enable Rx, Disable Tx
  0x02 = Disable Rx, Enable Tx
  0x03 = Disable Rx and Tx

Communication-Type:
  0x01 = Normal Message
  0x02 = Normal + NM
  0x03 = NM Only
```

### Service 0x85 - ControlDTCSettings

**Zweck:** DTC-Einstellungen steuern

```
Request:  85 [Sub-Function]
Response: C5 [Sub-Function]

Sub-Function:
  0x01 = DTC Settings On
  0x02 = DTC Settings Off
```

### Service 0x31 - RoutineControl

**Zweck:** Routinen ausführen

```
Request:  31 [Sub-Function] [Routine-ID-High] [Routine-ID-Low] [Data...]
Response: 71 [Sub-Function] [Routine-ID-High] [Routine-ID-Low] [Data...]

Sub-Function:
  0x01 = Start Routine
  0x02 = Stop Routine
  0x03 = Request Routine Results
```

**Typische Routinen für Astra J:**
| Routine ID | Beschreibung |
|------------|--------------|
| 0xFF00 | Check Programming Precondition |
| 0xFF01 | Erase Memory |
| 0xFF02 | Check Programming Dependencies |
| 0x0202 | Reset ECU |
| 0x0301 | Clear DTC |
| 0x0302 | Read DTC |
| 0x0400 | Turbo Boost Test |

### Service 0x34/0x36/0x37 - Download

**Zweck:** Firmware-Updates

```
Request:  34 [Memory Size] [Address...]
Response: 74 [Block Length]

Request:  36 [Block Count] [Data...]
Response: 76 [Block Count]

Request:  37
Response: 77
```

### Service 0x87 - LinkControl

**Zweck:** Baudrate ändern

```
Request:  87 [Sub-Function] [Data...]
Response: C7 [Sub-Function]

Sub-Function:
  0x01 = Verify Baudrate
  0x02 = Transition Baudrate
  0x03 = Control Parameter
```

---

## 6. Seed-Key Sicherheitsmechanismus

### Allgemeiner Ablauf

```
┌─────────┐         ┌─────────┐
│  Tester  │         │   ECU   │
└────┬────┘         └────┬────┘
     │  27 01 (Seed Req) │
     │──────────────────>│
     │  67 01 [Seed]     │
     │<──────────────────│
     │                   │
     │ Berechne Key = f(Seed, Secret)
     │                   │
     │  27 02 [Key]      │
     │──────────────────>│
     │  67 02 (OK)       │
     │<──────────────────│
```

### Seed-Key Implementierung (Allgemeines Konzept)

**WICHTIG:** Die genauen Algorithmen sind GM-proprietär und werden nicht öffentlich geteilt. Der folgende Code zeigt das allgemeine Prinzip:

```python
# ALLGEMEINES KONZEPT - NICHT der exakte GM-Algorithmus!
# Jedes ECU kann einen anderen Algorithmus verwenden

def calculate_key(seed: bytes, level: int) -> bytes:
    """
    Berechnet den Key basierend auf dem Seed.
    
    Der Algorithmus variiert je nach:
    - ECU-Typ (ECM, BCM, TCM)
    - Sicherheitslevel (1, 2, 3)
    - Fahrzeugmodell und Baujahr
    - Software-Version des ECUs
    
    Bekannte GM-Ansätze:
    - XOR mit konstanten Werten
    - Bit-Shifting Operationen
    - Lookup-Tabellen
    - Mehrstufige Transformation
    """
    if level == 1:
        # Security Level 1 - Erweiterte Diagnose
        key = bytearray(4)
        for i in range(4):
            key[i] = seed[i] ^ SEED_KEY_CONSTANTS_LEVEL1[i]
        return bytes(key)
    elif level == 2:
        # Security Level 2 - Programmierung
        key = bytearray(4)
        for i in range(4):
            key[i] = (seed[i] << SHIFT_CONSTANTS[i]) & 0xFF
        return bytes(key)
    return b'\x00\x00\x00\x00'
```

### Bekannte GM Seed-Key Verfahren

| Level | Verwendung | Algorithmus-Typ |
|-------|------------|-----------------|
| **Level 1** | Erweiterte Diagnose | XOR + Bit-Manipulation |
| **Level 2** | ECU-Programmierung | Mehrstufige Transformation |
| **Level 3** | Sicherheitsrelevante Funktionen | Spezieller Algorithmus |

### Wichtige Hinweise

1. **Jedes ECU** hat seinen eigenen Seed-Key-Algorithmus
2. **Der Algorithmus** ist in der ECU-Firmware implementiert
3. **OP-COM** hat die notwendigen Algorithmen für die meisten ECUs
4. **GM MDI + GDS2** verwendet die offiziellen Algorithmen
5. **Fehlversuche:** Nach 3-5 Fehlversuchen wird das ECU gesperrt (Timeout)

### Beispiele für Seed-Key Abläufe

```
ECM (Motorsteuergerät):
  Security Access Level 1: Für erweiterte Diagnose
  Security Access Level 2: Für Programmierung

BCM (Karosseriesteuergerät):
  Security Access Level 1: Für Codierungen
  Security Access Level 2: Für Konfigurationsänderungen

TCM (Getriebesteuergerät):
  Security Access Level 1: Für Diagnose
  Security Access Level 2: Für Adaptationen
```

---

## 7. DTC Format (Diagnostic Trouble Codes)

### DTC Struktur

```
Byte 1: High Byte (P0xxx, P2xxx)
Byte 2: Low Byte (P0xxx, P2xxx)
Byte 3: Status Byte
```

### DTC Klassifikation (OBD-II)

| Präfix | Kategorie | Beschreibung |
|--------|-----------|--------------|
| P0xxx | Standard | SAE definiert (allgemein) |
| P1xxx | Herstellerspezifisch | GM/Opel-spezifisch |
| P2xxx | Reserviert | Zukunft/Hersteller |
| P3xxx | Reserviert | Zukunft/Hersteller |
| B0xxx | Karosserie | Aufbau |
| C0xxx | Fahrwerk | Chassis |
| U0xxx | Netzwerk | Kommunikation |

### DTC Status Byte

| Bit | Name | Beschreibung |
|-----|------|--------------|
| 0 | testFailed | Test fehlgeschlagen |
| 1 | testFailedThisOperationCycle | Test in diesem Zyklus fehlgeschlagen |
| 2 | pendingDTC | Ausstehender DTC |
| 3 | confirmedDTC | Bestätigter DTC |
| 4 | testNotCompletedSinceLastClear | Test seit Löschen nicht abgeschlossen |
| 5 | testFailedSinceLastClear | Test seit Löschen fehlgeschlagen |
| 6 | testNotCompletedThisOperationCycle | Test in diesem Zyklus nicht abgeschlossen |
| 7 | warningIndicatorRequested | Warnleuchte angefordert |

### Typische DTCs für Astra J 1.4 Turbo

| DTC | Beschreibung | System |
|-----|--------------|--------|
| P0010 | Nockenwellen-Position (Bank 1) | Motor |
| P0011 | Nockenwellen-Position (Bank 1) Timing |
| P0013 | Nockenwellen-Position (Bank 1) |
| P0014 | Nockenwellen-Position (Bank 1) Timing |
| P0030 | O2-Sensor Heater (Bank 1 Sensor 1) |
| P0031 | O2-Sensor Heater (Bank 1 Sensor 1) Low |
| P0033 | Turbo Bypass Valve Control |
| P0034 | Turbo Bypass Valve Control Low |
| P0100 | MAF Sensor |
| P0101 | MAF Sensor Range/Performance |
| P0105 | MAP Sensor |
| P0106 | MAP Sensor Range/Performance |
| P0107 | MAP Sensor Low |
| P0108 | MAP Sensor High |
| P0110 | IAT Sensor |
| P0112 | IAT Sensor Low |
| P0113 | IAT Sensor High |
| P0115 | Engine Coolant Temp Sensor |
| P0116 | ECT Sensor Range/Performance |
| P0117 | ECT Sensor Low |
| P0118 | ECT Sensor High |
| P0120 | Throttle Position Sensor |
| P0121 | TPS Range/Performance |
| P0122 | TPS Low |
| P0123 | TPS High |
| P0130 | O2 Sensor (Bank 1 Sensor 1) |
| P0131 | O2 Sensor Low (Bank 1 Sensor 1) |
| P0132 | O2 Sensor High (Bank 1 Sensor 1) |
| P0133 | O2 Sensor Slow Response (Bank 1 Sensor 1) |
| P0134 | O2 Sensor No Activity (Bank 1 Sensor 1) |
| P0135 | O2 Sensor Heater (Bank 1 Sensor 1) |
| P0136 | O2 Sensor (Bank 1 Sensor 2) |
| P0171 | System Too Lean (Bank 1) |
| P0172 | System Too Rich (Bank 1) |
| P0190 | Fuel Rail Pressure Sensor |
| P0201 | Injector Circuit (Cylinder 1) |
| P0202 | Injector Circuit (Cylinder 2) |
| P0203 | Injector Circuit (Cylinder 3) |
| P0204 | Injector Circuit (Cylinder 4) |
| P0220 | Throttle Position Sensor 2 |
| P0222 | TPS 2 Low |
| P0223 | TPS 2 High |
| P0230 | Fuel Pump Primary Circuit |
| P0231 | Fuel Pump Secondary Low |
| P0232 | Fuel Pump Secondary High |
| P0240 | Turbocharger Boost Sensor B |
| P0241 | Turbocharger Boost Sensor B Low |
| P0242 | Turbocharger Boost Sensor B High |
| P0243 | Turbocharger Wastegate Solenoid A |
| P0244 | Turbocharger Wastegate Solenoid A Range/Performance |
| P0245 | Turbocharger Wastegate Solenoid A Low |
| P0246 | Turbocharger Wastegate Solenoid A High |
| P0300 | Random/Multiple Cylinder Misfire |
| P0301 | Cylinder 1 Misfire |
| P0302 | Cylinder 2 Misfire |
| P0303 | Cylinder 3 Misfire |
| P0304 | Cylinder 4 Misfire |
| P0335 | Crankshaft Position Sensor A |
| P0336 | Crankshaft Position Sensor A Range/Performance |
| P0340 | Camshaft Position Sensor (Bank 1) |
| P0341 | Camshaft Position Sensor Range/Performance |
| P0400 | EGR Flow |
| P0401 | EGR Flow Insufficient |
| P0402 | EGR Flow Excessive |
| P0403 | EGR Control Circuit |
| P0404 | EGR Control Circuit Range/Performance |
| P0405 | EGR Sensor A Low |
| P0406 | EGR Sensor A High |
| P0420 | Catalyst Efficiency (Bank 1) |
| P0440 | EVAP System |
| P0441 | EVAP System Purge Flow |
| P0442 | EVAP System Leak (Small) |
| P0443 | EVAP Purge Valve Circuit |
| P0444 | EVAP Purge Valve Open |
| P0445 | EVAP Purge Valve Short |
| P0446 | EVAP Vent Control |
| P0451 | EVAP Pressure Sensor Range/Performance |
| P0452 | EVAP Pressure Sensor Low |
| P0453 | EVAP Pressure Sensor High |
| P0454 | EVAP Pressure Sensor Intermittent |
| P0455 | EVAP System Leak (Gross) |
| P0460 | Fuel Level Sensor |
| P0461 | Fuel Level Sensor Range/Performance |
| P0462 | Fuel Level Sensor Low |
| P0463 | Fuel Level Sensor High |
| P0500 | Vehicle Speed Sensor |
| P0501 | Vehicle Speed Sensor Range/Performance |
| P0504 | Brake Switch A/B Correlation |
| P0506 | Idle Control System RPM Lower Than Expected |
| P0507 | Idle Control System RPM Higher Than Expected |
| P0562 | System Voltage Low |
| P0563 | System Voltage High |
| P0571 | Brake Switch A Circuit |
| P0601 | Internal Control Module Memory Check Sum Error |
| P0602 | Control Module Programming Error |
| P0604 | Internal Control Module RAM Error |
| P0606 | PCM/ECM Processor Error |
| P0621 | Generator L-Terminal Circuit |
| P0622 | Generator F-Terminal Circuit |
| P0650 | Malfunction Indicator Lamp (MIL) Control Circuit |
| P0685 | ECM/PCM Power Relay Control Circuit |
| P0686 | ECM/PCM Power Relay Control Circuit Low |
| P0687 | ECM/PCM Power Relay Control Circuit High |
| P0688 | ECM/PCM Power Relay Sense Circuit Low |
| P0689 | ECM/PCM Power Relay Sense Circuit High |
| P0690 | ECM/PCM Power Relay Sense Circuit High |
| P0691 | Cooling Fan 1 Control Circuit Low |
| P0692 | Cooling Fan 1 Control Circuit High |
| P0693 | Cooling Fan 2 Control Circuit Low |
| P0694 | Cooling Fan 2 Control Circuit High |

---

## 8. GM-spezifische Protokolle (HOBD/MOPED)

### GM HOBD (Human Oriented Bus Diagnostics)

GM HOBD ist ein herstellerspezifisches Protokoll, das auf UDS basiert:

- **Zielgruppe:** Werkstätten und Entwickler
- **Beschreibung:** Erweiterte Diagnosefunktionen über Standard-OBD-II-Anschluss
- **Verwendung:** Flottenmanagement, Fern Diagnose

### GM MOPED (Mechanic Oriented Programming and Engineering Diagnostics)

GM MOPED ist ein proprietäres Protokoll für die ECU-Programmierung:

- **Zielgruppe:** Werkstätten und Entwickler
- **Beschreibung:** ECU-Programmierung und Kalibrierung
- **Verwendung:** Firmware-Updates, Konfigurationsänderungen

### Technische Details

| Eigenschaft | HOBD | MOPED |
|-------------|------|-------|
| Basis | UDS (ISO 14229) | UDS (ISO 14229) |
| Transport | ISO-TP (ISO 15765) | ISO-TP (ISO 15765) |
| Geschwindigkeit | 500 kbit/s | 500 kbit/s |
| Sicherheit | Standard | Erweitert |
| Zugriffsebene | Level 1 | Level 2+ |
| Verwendung | Diagnose | Programmierung |

### GM-spezifische CAN-IDs

| CAN-ID | Beschreibung |
|--------|--------------|
| 0x000 | Kraftfahrzeug (Vehicle Bus) |
| 0x100 | Motor-ECU (High Speed) |
| 0x200 | Getriebe-ECU |
| 0x300 | BCM (Body Control Module) |
| 0x400 | IPC (Instrument Cluster) |
| 0x500 | ABS/ESP |
| 0x600 | SRS (Airbag) |
| 0x700 | Diagnose |
| 0x7DF | Functional OBD |
| 0x7E0-0x7EF | Physical Diagnose |
| 0x7E8-0x7EF | Physical Response |

### TP2.0 (Transport Protocol 2.0)

GM verwendet das TP2.0-Protokoll für die Kommunikation:

```
┌─────────────────────────────────────────┐
│           TP2.0 Frame Format            │
├─────────────────────────────────────────┤
│ Byte 1: CAN ID                          │
│ Byte 2: Frame Type                      │
│   0x00 = Single Frame                   │
│   0x01 = First Frame                    │
│   0x02 = Consecutive Frame              │
│   0x03 = Flow Control                   │
│ Byte 3: Length (bei Single Frame)       │
│ Bytes 4-8: Daten                        │
└─────────────────────────────────────────┘
```

---

## 9. Steuergeräte-Adressen

### Opel Astra J Steuergeräte

| ECU | Name | CAN-ID Request | CAN-ID Response | Adresse |
|-----|------|----------------|-----------------|---------|
| ECM | Motorsteuergerät | 0x7E0 | 0x7E8 | 0x01 |
| TCM | Getriebesteuergerät | 0x7E1 | 0x7E9 | 0x02 |
| BCM | Karosseriesteuergerät | 0x7C0 | 0x7C8 | 0xFF |
| IPC | Kombiinstrument | 0x7C3 | 0x7CB | 0x83 |
| CIM | Lenksäulenmodul | 0x7C1 | 0x7C9 | 0x7E |
| ABS | ABS/ESP Steuergerät | 0x7C2 | 0x7CA | 0x09 |
| UEC | Motornahes Steuergerät | 0x7C4 | 0x7CC | 0x09 |
| REC | Heckelektronik | 0x7C6 | 0x7CE | 0x2E |
| SRS | Airbag Steuergerät | 0x7C5 | 0x7CD | 0x10 |
| ICM | Infotainment Steuergerät | 0x7C8 | 0x7D0 | 0x06 |
| EPS | Elektrische Servolenkung | 0x7C9 | 0x7D1 | 0x10 |
| EPB | Elektrische Handbremse | 0x7CA | 0x7D2 | 0x09 |
| CLM | Climate Control Module | 0x7CB | 0x7D3 | 0x06 |
| DDM | Fahrertürmodul | 0x7CC | 0x7D4 | 0x10 |
| PDM | Beifahrertürmodul | 0x7CD | 0x7D5 | 0x10 |

### Zugriffspfade

```
Motorsteuergerät (ECM):
  Tester (0xF1) → ECM (0x01)
  CAN-ID: 0x7E0 → 0x7E8

Karosseriesteuergerät (BCM):
  Tester (0xF1) → BCM (0xFF)
  CAN-ID: 0x7C0 → 0x7C8

Kombiinstrument (IPC):
  Tester (0xF1) → IPC (0x83)
  CAN-ID: 0x7C3 → 0x7CB
```

---

## 10. Typische Diagnoseabläufe

### 10.1 Verbindungsaufbau

```
Schritt 1: CAN-Bus aktivieren
  - Spannung am OBD-Stecker prüfen (Pin 16: +12V)
  - CAN High/Low prüfen (Pins 6, 14)
  - Terminierung prüfen (120Ω)

Schritt 2: Session starten
  Request: 10 03  (Extended Session)
  Response: 50 03

Schritt 3: Tester Present senden (alle 2-3 Sek.)
  Request: 3E 00
  Response: 7E 00

Schritt 4: ECU identifizieren
  Request: 22 F1 90  (VIN lesen)
  Response: 62 F1 90 [VIN]

  Request: 22 F1 88  (ECU-Version)
  Response: 62 F1 88 [Version]
```

### 10.2 DTCs auslesen

```
Schritt 1: Extended Session
  Request: 10 03
  Response: 50 03

Schritt 2: DTCs lesen
  Request: 19 02 FF
  Response: 59 02 [Status] [DTC1] [Status1] ...

Schritt 3: DTC-Details
  Request: 19 04 [DTC-High] [DTC-Low] FF
  Response: 59 04 [DTC] [Status] [Snapshot] ...
```

### 10.3 DTCs löschen

```
Schritt 1: Extended Session
  Request: 10 03
  Response: 50 03

Schritt 2: DTCs löschen
  Request: 14 FF FF FF
  Response: 54

Schritt 3: Verifizieren
  Request: 19 02 FF
  Response: 59 02 00  (Keine DTCs)
```

### 10.4 Live-Daten auslesen

```
Schritt 1: Default Session
  Request: 10 01
  Response: 50 01

Schritt 2: RPM auslesen
  Request: 22 02 00
  Response: 62 02 00 [RPM High] [RPM Low]
  
  Berechnung: RPM = (High * 256 + Low) / 4

Schritt 3: Temperatur auslesen
  Request: 22 02 01
  Response: 62 02 01 [Temp]
  
  Berechnung: Temp = Value - 40

Schritt 4: Drosselklappenposition
  Request: 22 02 04
  Response: 62 02 04 [Position]
  
  Berechnung: Position = Value * 100 / 255
```

### 10.5 ECU-Reset

```
Schritt 1: Extended Session
  Request: 10 03
  Response: 50 03

Schritt 2: ECU Reset
  Request: 11 01
  Response: 51 01

Schritt 3: Warten bis ECU wieder aktiv
  (ca. 2-5 Sekunden)
```

---

## 11. Referenzen und Quellen

### Standards und Spezifikationen

| Dokument | Beschreibung |
|----------|--------------|
| ISO 14229-1:2013 | Unified Diagnostic Services (UDS) - Applikationsschicht |
| ISO 15765-2:2016 | Diagnostic communication over CAN (DoCAN) |
| ISO 11898-1:2015 | CAN-Datenlinkschicht |
| SAE J1979 | OBD-II PIDs (Parameter IDs) |
| SAE J1962 | OBD-II Data Link Connector |
| SAE J1939 | Heavy Duty CAN |

### Online-Quellen

| Link | Beschreibung |
|------|--------------|
| [Wikipedia: Unified Diagnostic Services](https://en.wikipedia.org/wiki/Unified_Diagnostic_Services) | Allgemeine UDS-Übersicht |
| [Wikipedia: OBD-II PIDs](https://en.wikipedia.org/wiki/OBD-II_PIDs) | Standard-PIDs Referenz |
| [py-uds Documentation](https://uds.readthedocs.io/) | Python UDS Implementierung |
| [ISO 14229-1](https://www.iso.org/standard/72439.html) | Offizielle ISO-Spezifikation |
| [Peak PCAN-UDS](https://www.peak-system.com/) | PCAN-UDS API Dokumentation |

### Tools und Software

| Tool | Verwendung |
|------|------------|
| **OP-COM** | Opel-spezifische Diagnose, Codierung |
| **VauxCom** | Vauxhall/Opel Diagnose |
| **GM MDI + GDS2** | Offizielle GM/Opel Werkstattsoftware |
| **Torque Pro** | OBD-II Live-Daten (Android) |
| **Car Scanner** | OBD-II Diagnose (iOS/Android) |
| **py-uds** | Python UDS-Bibliothek |

### Weiterführende Dokumentation

| Datei | Inhalt |
|-------|--------|
| [ASTRA_J_CODING_GUIDE.md](../ASTRA_J_CODING_GUIDE.md) | Vollständige Codierungsanleitung |
| [ASTRA_J_DTC_CODES.md](../ASTRA_J_DTC_CODES.md) | DTC-Fehlercodes Referenz |
| [ASTRA_J_LIVE_DATA_REFERENCE.md](../ASTRA_J_LIVE_DATA_REFERENCE.md) | Live-Daten Referenz |

---

## Anhang: Quick Reference

### Wichtige Befehle (Kopierhilfe)

```
Session:
  Default:     10 01
  Extended:    10 03
  Programming: 10 02

Sicherheit:
  Seed Req:    27 01
  Key Send:    27 02 [Key]

DTCs:
  Lesen:       19 02 FF
  Löschen:     14 FF FF FF

VIN:
  Lesen:       22 F1 90

Reset:
  Hard:        11 01
  Soft:        11 03

Keep-Alive:
  Tester:      3E 00

Kommunikation:
  Disable:     28 03 01
  Enable:      28 00 01
```

### CAN-ID Quick Reference

```
OBD-II Standard:
  Request:  7DF
  Response: 7E8

Physical (Motor):
  Request:  7E0
  Response: 7E8

Physical (BCM):
  Request:  7C0
  Response: 7C8

Physical (IPC):
  Request:  7C3
  Response: 7CB
```

### Negative Response Codes (NRC)

| Code | Name | Beschreibung |
|------|------|--------------|
| 0x10 | generalReject | Allgemeiner Fehler |
| 0x11 | serviceNotSupported | Dienst nicht unterstützt |
| 0x12 | subFunctionNotSupported | Sub-Funktion nicht unterstützt |
| 0x13 | incorrectMessageLength | Fehlende Nachrichtenlänge |
| 0x14 | responseTooLong | Antwort zu lang |
| 0x21 | busyRepeatRequest | Wiederholung nötig |
| 0x22 | conditionsNotCorrect | Bedingungen nicht erfüllt |
| 0x24 | requestSequenceError | Sequenzfehler |
| 0x31 | requestOutOfRange | Anfrage außerhalb des Bereichs |
| 0x33 | securityAccessDenied | Sicherheitszugriff verweigert |
| 0x35 | invalidKey | Ungültiger Key |
| 0x36 | exceedNumberOfAttempts | Zu viele Fehlversuche |
| 0x37 | requiredTimeDelayNotExpired | Zeitverzörgung nicht abgelaufen |
| 0x70 | uploadDownloadNotAccepted | Upload/Download nicht akzeptiert |
| 0x71 | transferDataSuspended | Datenübertragung unterbrochen |
| 0x72 | generalProgrammingFailure | Allgemeiner Programmierfehler |
| 0x73 | wrongBlockSequenceCounter | Falsche Blocksequenz |
| 0x78 | requestCorrectlyReceivedResponsePending | Antwort ausstehend |
| 0x7E | subFunctionNotSupportedInActiveSession | Sub-Funktion nicht in aktiver Session |
| 0x7F | serviceNotSupportedInActiveSession | Dienst nicht in aktiver Session |

---

*Erstellt: Mai 2026*
*Fahrzeug: Opel Astra J 2012 1.4 Turbo (A14NEL, 140 PS)*
*Projekt: CAN-OP OBD Diagnose-Tool*
