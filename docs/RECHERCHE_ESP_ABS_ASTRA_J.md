# Recherche: ESP / ABS / Bremsensystem – Opel Astra J 2012 1.4 Turbo

> **Stand:** Mai 2026
> **Fahrzeug:** Opel Astra J (PFL/FL), Bj. 2012, 1.4 Turbo (B14NET), 120/140 PS
> **Plattform:** GM Delta II (Global Delta II)

---

## 1. ESP-/ESC-Version im Opel Astra J

### 1.1 Hersteller und Version

Der Opel Astra J使用t ein **Bosch ESP 9.0** (auch als **ESC – Electronic Stability Control** bezeichnet). Bei Opel/GM wird das System als **"ESP"** oder **"Elektronisches Stabilitätsprogramm"** vermarktet.

| Eigenschaft | Detail |
|---|---|
| **Hersteller** | Robert Bosch GmbH |
| **Bezeichnung** | Bosch ESP 9.0 (9. Generation) |
| **Baureihe** | Bosch ABS/ESP Gen 9 |
| **Bestellnummer (Steuergerät)** | 13 0800 431 (je nach Variant) |
| **Teilenummer GM/Opel** | 13504131 / 13581641 (variiert) |
| **Protokoll** | High-Speed CAN (500 kbit/s) |
| **Spannungsversorgung** | 12V Bordnetz |
| **Integration** | Integriertes ABS + ESP + EBD + TCS + VDC |

### 1.2 Funktionsweise

Das Bosch ESP 9.0 System des Astra J integriert folgende Funktionen in einem einzigen Steuergerät:

- **ABS** (Antiblockiersystem) – 4-Kanal, 4-Sensor
- **EBD** (Electronic Brakeforce Distribution – Elektronische Bremskraftverteilung)
- **TCS/ASR** (Traction Control System / Antriebsschlupfregelung)
- **ESP/ESC** (Elektronisches Stabilitätsprogramm)
- **HBA** (Hydraulic Brake Assist – Hydraulischer Bremsassistent)
- **HHC** (Hill Hold Control – Berganfahrhilfe)
- **CBC** (Cornering Brake Control – Kurvenbremsregelung)
- **RSC** (Roll Stability Control – Überschlagsstabilisierung)
- **BDW** (Brake Disc Wiping – Bremsscheibenwischer bei Nässe)

Das System vergleicht permanent (bis zu 150-mal pro Sekunde) die Lenkradwinkel-Eingabe des Fahrers mit dem tatsächlichen Fahrverhalten (Gierrate, Querbeschleunigung, Raddrehzahlen) und greift bei Abweichungen ein.

### 1.3 Vergleich mit anderen ESP-Versionen

| Version | Generation | Eingriffsrate | Besonderheiten |
|---|---|---|---|
| Bosch ESP 8.1 | 8. Gen. | ~50/s | Vorgängerversion |
| **Bosch ESP 9.0** | **9. Gen.** | **~150/s** | **Im Astra J verbaut** |
| Bosch ESP 9.3 | 9.3 Gen. | ~200/s | Kompaktere Bauform |
| Bosch ESP 10 | 10. Gen. | ~200/s | Neueste Generation |

---

## 2. ABS-Version und Hersteller

### 2.1 Technische Daten

| Eigenschaft | Detail |
|---|---|
| **Hersteller** | Robert Bosch GmbH |
| **Generation** | Bosch Gen 9 (integriert in ESP 9.0) |
| **Kanäle** | 4-Kanal, 4-Sensor |
| **Datenübertragung** | CAN-Bus (High-Speed) |
| **Hydraulikpumpe** | Elektromotorgetriebene Kolbenpumpe |
| **Ventile** | 8 Absperrventile (2 pro Rad) |
| **Eingriffsfrequenz** | Bis zu 15 Mal pro Sekunde pro Rad |

### 2.2 Komponenten

```
┌─────────────────────────────────────────────────┐
│              Bosch ESP 9.0 Modul                │
│  ┌─────────────┐  ┌──────────────────────────┐  │
│  │ Elektrische  │  │  Hydraulische            │  │
│  │ Steuereinheit│◄►│  Stufeinheit (HCU)       │  │
│  │ (ECU)        │  │  - 8 Absperrventile     │  │
│  │             │  │  - Elektropumpe           │  │
│  │             │  │  - Drucksensoren          │  │
│  └─────────────┘  └──────────────────────────┘  │
│        │                     │                  │
│   CAN-Bus             Hydraulische               │
│   (High-Speed)         Leitungen                 │
│                         zu allen 4               │
└─────────────────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
  Vorderrad-L   Vorderrad-R   Hinterrad-L   Hinterrad-R
  (Durchmesser-   (Durchmesser-  (Durchmesser-   (Durchmesser-
   Sensor +        Sensor +        Sensor +        Sensor +
   Zähnkr.)        Zähnkr.)        Zähnkr.)        Zähnkr.)
```

---

## 3. CAN-BUS-Adressen für ESP/ABS-Steuergerät

### 3.1 Netzwerkarchitektur Astra J

Der Opel Astra J verwendet ein **Dual-CAN-Bus-System**:

| CAN-Bus | Geschwindigkeit | Funktion |
|---|---|---|
| **High-Speed CAN** | 500 kbit/s | Antriebsstrang, Bremsen, Fahrwerk |
| **Low-Speed CAN** | 125 kbit/s | Komfort, Karosserie |

### 3.2 CAN-IDs des ABS/ESP-Steuergeräts

Die folgenden CAN-IDs gelten für den Astra J mit Bosch ESP 9.0:

#### Standard-IDs (High-Speed CAN)

| CAN-ID (hex) | Beschreibung | Zyklus |
|---|---|---|
| **0x0A0** | Raddrehzahlsignale vorne links | 10 ms |
| **0x0A1** | Raddrehzahlsignale vorne rechts | 10 ms |
| **0x0A2** | Raddrehzahlsignale hinten links | 10 ms |
| **0x0A3** | Raddrehzahlsignale hinten rechts | 10 ms |
| **0x180** | ESP-/ABS-Status | 10 ms |
| **0x348** | Bremsendrucksensor | 20 ms |
| **0x0B4** | Bremsschalter-Status | 10 ms |
| **0x0C0** | Lenkwinkel-Geschwindigkeit | 10 ms |

#### Diagnose-IDs (UDS über CAN)

| CAN-ID (hex) | Beschreibung |
|---|---|
| **0x7E0** | ABS/ESP – Diagnose-Request (Functional) |
| **0x7E8** | ABS/ESP – Diagnose-Response |
| **0x7DF** | OBD-II – Broadcast-Request |
| **0x7E8** | OBD-II – ECU-Response (Abschnitt 1) |

### 3.3 Wichtige CAN-Signale im Detail

#### Raddrehzahlen (Beispiel: ID 0x0A0)

```
Byte 0-1: Vorderrad links – Raddrehzahl (16-bit, 0.01 U/s pro Bit)
Byte 2-3: Vorderrad rechts – Raddrehzahl
Byte 4-5: Bit-Status (ABS-Aktiv, ESP-Aktiv, etc.)
Byte 6-7: Checksumme / Reserve
```

#### ESP-Status (ID 0x180)

```
Byte 0, Bit 0: ABS-Lampe (ein/aus)
Byte 0, Bit 1: ESP-Lampe (ein/aus)
Byte 0, Bit 2: ESP-Eingriff (aktiv/inaktiv)
Byte 0, Bit 3: ASR-Eingriff (aktiv/inaktiv)
Byte 0, Bit 4: ABS-Fehler
Byte 0, Bit 5: ESP-Fehler
Byte 0, Bit 6: Handbremse (ein/aus)
Byte 0, Bit 7: Bremsflüssigkeitsstand (ok/niedrig)
Byte 1: Bremsbelag-Verschleiß (0-100%)
Byte 2-3: Diagnose-Codes
```

### 3.4 Andere relevante CAN-IDs im Astra J

| CAN-ID (hex) | Steuergerät | Beschreibung |
|---|---|---|
| 0x100 | Motor-ECM | Motordaten, Drehmoment |
| 0x140 | Getriebe-TCM | Getriebestatus (nur Automatik) |
| 0x200 | BCM | Karosserie |
| 0x240 | IPC | Instrumentencluster |
| 0x300 | BCM | Zentralverriegelung |
| 0x410 | BCM | Beleuchtung |
| 0x500 | DIM | Diagnose-Informationen |

---

## 4. Wheel Speed Sensoren (4-Kanal ABS)

### 4.1 Sensor-Technologie

| Eigenschaft | Detail |
|---|---|
| **Sensor-Typ** | Hall-Effekt-Sensor (aktiver Sensor) |
| **Zähnkränze** | 48 Zähne (Tonaufnehmer) |
| **Ausbau-Ø Zähnrad** | ca. 120 mm (vorne), ca. 100 mm (hinten) |
| **Luftspalt** | 0.3 – 1.5 mm |
| **Spannungsversorgung** | 5V (vom ESP-Steuergerät) |
| **Ausgangssignal** | Rechtecksignal (digital) |
| **Auflösung** | 48 Impulse pro Radumdrehung |
| **Max. Frequenz** | ca. 3 kHz |

### 4.2 Messprinzip

```
         Zähnkr Rad
        ┌──────────┐
        │ ▓░▓░▓░▓░ │  ← Zähne (48 Stk.)
        └──────────┘
              ↕  (Luftspalt 0.3-1.5mm)
        ┌──────────┐
        │  Sensor  │  ← Hall-Sensor mit Magnet
        │ (Fest)   │
        └──────────┘
              │
         Ausgangs-Signal:
         ┌┐  ┌┐  ┌┐  ┌┐  ┌┐
         ││  ││  ││  ││  ││  ──→ Rechtecksignal
         └┘  └┘  └┘  └┘  └┘
              │
         ESP-ECU:
         Frequenz ∝ Raddrehzahl
         ω = (Frequenz × 2π) / 48
```

### 4.3 Sensor-Positionen

```
          Vorne
     ┌───────────────┐
     │  FL ●─────● FR│
     │               │
     │               │
     │  HL ●─────● HR│
     └───────────────┘
          Hinten

FL = Vorn Links (Fahrerseite in DE)
FR = Vorn Rechts (Beifahrerseite in DE)
HL = Hinten Links
HR = Hinten Rechts
```

### 4.4 Datenformate

| Parameter | Formel | Einheit |
|---|---|---|
| Raddrehzahl | `ω = (f × 2π) / 48` | rad/s |
| Fahrzeuggeschwindigkeit | `v = ω × r_reifen` | m/s |
| Schlupf | `s = (v_rad - v_fahrzeug) / v_fahrzeug` | % |

### 4.5 Typische Sensordaten im Betrieb

| Fahrzustand | Vorderrad-links | Vorderrad-rechts | Hinterrad-links | Hinterrad-rechts |
|---|---|---|---|---|
| Geradeaus 100 km/h | ~27.8 rad/s | ~27.8 rad/s | ~27.8 rad/s | ~27.8 rad/s |
| Rechtskurve 50 km/h | ~13.9 rad/s | ~14.5 rad/s | ~13.2 rad/s | ~14.8 rad/s |
| ABS-Eingriff | Pulsierend | Pulsierend | Pulsierend | Pulsierend |

---

## 5. ESP Sensoren

### 5.1 Lenkwinkelgeschwindigkeit (Steering Wheel Angle Sensor)

| Eigenschaft | Detail |
|---|---|
| **Sensor-Typ** | Kapazitiver Drehwinkelsensor (COD – Capacitive Angle Detector) |
| **Hersteller** | Bremse / Opel (Teil des ESP-Sensorbausteins) |
| **Messbereich** | -780° bis +780° (±2.15 Umdrehungen) |
| **Auflösung** | 0.1° |
| **Abtastrate** | 100 Hz (10 ms) |
| **Signaltyp** | Seriell (CAN oder LIN) |
| **Anbauort** | Lenksäule (unter dem Lenkrad) |
| **Spannungsversorgung** | 5V |

### 5.2 Gierratensensor (Yaw Rate Sensor / Drehratensensor)

| Eigenschaft | Detail |
|---|---|
| **Sensor-Typ** | MEMS-Gyroskop (mikromechanisch) |
| **Hersteller** | Bosch (integriert im ESP 9.0 Sensorik-Modul) |
| **Messbereich** | -100°/s bis +100°/s |
| **Auflösung** | 0.01°/s |
| **Abtastrate** | 100 Hz |
| **Fehlergrenze** | ±0.5°/s |
| **Anbauort** | Fahrzeug-Schwerpunkt (typisch unter Sitzbank oder in Mitteltunnel) |
| **Messachse** | Z-Achse (Gierachse) |

### 5.3 Querbeschleunigungssensor (Lateral Acceleration Sensor)

| Eigenschaft | Detail |
|---|---|
| **Sensor-Typ** | MEMS-Beschleunigungssensor |
| **Hersteller** | Bosch (integriert im ESP 9.0) |
| **Messbereich** | -2g bis +2g |
| **Auflösung** | 0.01g |
| **Abtastrate** | 100 Hz |
| **Anbauort** | Fahrzeug-Schwerpunkt (oft kombiniert mit Gierratensensor) |

### 5.4 Kombiniertes ESP-Sensor-Modul (IMU – Inertial Measurement Unit)

Im Bosch ESP 9.0 sind Gierrate und Querbeschleunigung oft in einem **kombinierten Sensor-Modul** integriert:

```
┌───────────────────────────────────────┐
│     Kombiniertes ESP-Sensor-Modul    │
│  ┌─────────────────┐                 │
│  │   MEMS-Gyro     │  Gierrate       │
│  │   (Yaw Rate)    │  ±100°/s        │
│  └─────────────────┘                 │
│  ┌─────────────────┐                 │
│  │   MEMS-Accel    │  Querbeschl.    │
│  │   (Lateral)     │  ±2g            │
│  └─────────────────┘                 │
│         │ CAN-Ausgang                │
└───────────────────────────────────────┘
```

### 5.5 Bremspedalsensor

| Eigenschaft | Detail |
|---|---|
| **Sensor-Typ** | Hall-Sensor oder Potentiometer |
| **Anzahl Sensoren** | 2 (Redundanz) |
| **Funktion** | Betätigung erkannt (an/aus) + Pedalweg |
| **Spannungsversorgung** | 5V |

### 5.6 Drucksensoren (im Hydraulikmodul)

| Sensor | Messbereich | Funktion |
|---|---|---|
| HAUPTDRUCK-SENSOR | 0 – 200 bar | Bremsdruck in der Hauptleitung |
| VORDEN-SENSOR | 0 – 200 bar | Vorderrad-Bremskreis |
| HINTEN-SENSOR | 0 – 200 bar | Hinterrad-Bremskreis |

---

## 6. Hill Start Assist (Berganfahrhilfe – HHC)

### 6.1 Funktion

Die **Berganfahrhilfe (Hill Hold Control – HHC)** ist im ESP-System des Opel Astra J integriert. Sie verhindert beim Anfahren auf Steigungen das Zurückrollen des Fahrzeugs.

### 6.2 Funktionsprinzip

1. **Erkennung:** Der Neigungssensor im ESP-Modul erkennt eine Steigung (typisch > 3%)
2. **Aktivierung:** Wenn das Bremspedal betätigt wird und das Fahrzeug stillsteht
3. **Halten:** Nach Lösen des Bremspedals wird der Bremsdruck für ca. **2-3 Sekunden** gehalten
4. **Lösen:** Wenn das Fahrzeug anfährt (Kupplung greift / Drehmoment steigt), wird die Bremse gelöst
5. **Deaktivierung:** Nach Ablauf der Zeit oder manuell durch Betätigung des Gaspedals

### 6.3 Voraussetzungen

- Fahrzeug steht still
- Neigung > ca. 3% (Steigung)
- Kupplungshaltepedal betätigt (Schaltgetriebe) oder D/P (Automatik)
- Lenkrad nicht eingeschlagen (bei manchen Varianten)

### 6.4 Technische Daten

| Parameter | Wert |
|---|---|
| **Aktivierungsschwelle** | > 3% Steigung |
| **Haltezeit** | ca. 2-3 Sekunden |
| **Neigungssensor** | MEMS-Accel (in IMU) |
| **Integration** | Im ESP-Steuergerät |

---

## 7. Sport-Modus für ESP (abschaltbar?)

### 7.1 ESP-Taste im Astra J

Der Opel Astra J (2012) verfügt über eine **ESP-Taste** am Armaturenbrett (links vom Lenkrad, unterhalb der Lüftungsdüse).

### 7.2 Modi

| Modus | Beschreibung | Taste |
|---|---|---|
| **ESP Normal (Ein)** | Vollständige Stabilitätskontrolle | Standard (Taste nicht gedrückt) |
| **ESP Sport** | Höhere Eingriffsschwelle | Taste 1x drücken (< 3 Sek.) |
| **ESP Aus** | ESP deaktiviert (nur ABS bleibt aktiv) | Taste > 3 Sek. gedrückt halten |

### 7.3 Details

#### ESP Normal
- Alle Funktionen aktiv (ABS, EBD, TCS, ESP, CBC)
- Frühzeitiges Eingreifen
- Maximaler Sicherheitskomfort

#### ESP Sport
- **Erhöhte Eingriffsschwelle** für Gierrate und Querbeschleunigung
- ESP greift später ein – erlaubt mehr Schieben/Driften
- ABS und EBD bleiben auf Normalniveau
- **Kein vollständiges Abschalten** – nur Verschiebung der Schwellwerte
- TCS kann ebenfalls höhere Schlupfwerte zulassen

#### ESP Aus
- ESP komplett deaktiviert
- **ABS bleibt IMMER aktiv** (Sicherheitsvorschrift)
- EBD bleibt aktiv
- TCS (ASR) wird ebenfalls deaktiviert
- **WARNLEUCHTE** leuchtet dauerhaft auf dem Armaturenbrett
- System reaktiviert sich automatisch bei:
  - Neustart des Motors
  - Überschreiten einer bestimmten Geschwindigkeit (ca. 80-100 km/h)
  - Betätigung des Bremspedals im Grenzbereich

### 7.4 Warnungen

```
ESP-Normal:     Keine Leuchte
ESP-Sport:      ESP-Leuchte blinkt (gelb/orange)
ESP-Aus:        ESP-Leuchte leuchtet dauernd (gelb/orange)
ESP-Eingriff:   ESP-Leuchte blinkt schnell (während des Eingriffs)
Fehler:         ESP-Leuchte + ABS-Leuchte (rot) gleichzeitig
```

---

## 8. Bremskraftverteilung (EBD – Electronic Brakeforce Distribution)

### 8.1 Funktion

Die **elektronische Bremskraftverteilung (EBD)** ist eine Untermenge des ESP-Systems und ersetzt die mechanische Druckbegrenzungsventile in der Bremskraftverteilung.

### 8.2 Funktionsprinzip

```
                    Bremskraft
                    (Verteilung)
         Vorne          Hinten
    ┌──────────┐    ┌──────────┐
    │ 70-80%   │    │ 20-30%   │  ← Normal (ungeremmte Fahrt)
    │          │    │          │
    │          │    │          │
    │          │    │          │
    └──────────┘    └──────────┘

Vollbremsung:
    ┌──────────┐    ┌──────────┐
    │ 85-90%   │    │ 10-15%   │  ← Höhere Vorderrad-Belastung
    │          │    │          │     durch Gewichtsverlagerung
    └──────────┘    └──────────┘
```

### 8.3 EBD im Detail

| Parameter | Wert |
|---|---|
| **Regelbereich** | 0 – 100% Vorder-/Hinterrad-Anteil |
| **Eingangssignale** | 4× Raddrehzahl, Bremsdruck, Querbeschleunigung |
| **Ausgangs-signale** | individuelle Ventilsteuerung pro Rad |
| **Reaktionszeit** | < 50 ms |

### 8.4 Vorteile gegenüber mechanischer Bremskraftverteilung

- **Ladenabhängige Anpassung:** Leer/Fracht wird automatisch kompensiert
- **Dynamische Lastverlagerung:** Berücksichtigt Gewichtsverschiebung beim Bremsen
- **Reibwertunterschiede:**passt sich an不同 Straßenzustände an (µ-split, µ-jump)
- **Keine mechanischen Verschleißteile**

---

## 9. TPMS (Reifendruckkontrollsystem)

### 9.1 Systemtyp im Opel Astra J

Der Opel Astra J (2012)使用t ein **indirektes TPMS (iTPMS)**, das in das ABS/ESP-Steuergerät integriert ist.

| Eigenschaft | Detail |
|---|---|
| **System-Typ** | Indirekt (iTPMS) – softwarebasiert |
| **Sensoren** | Keine separaten Drucksensoren in den Reifen! |
| **Nutzung** | Bestehende ABS-Raddrehzahlsensoren |
| **Hersteller-Bezeichnung** | TPMS / RDK |
| **Software-Anbieter** | NIRA Dynamics (typisch für GM/Opel) |

### 9.2 Funktionsprinzip

Das indirekte TPMS nutzt zwei physikalische Effekte:

#### Effekt 1: Abrollumfang
- Bei Druckabfall verringert sich der Außendurchmesser des Reifens
- Das Rad dreht sich schneller als die anderen
- Drehzahlanstieg wird als Druckabfall interpretiert

#### Effekt 2: Frequenzeffekt
- Der Reifen hat einen charakteristischen Schwingungsmodus
- Diese Schwingung ist druckabhängig
- Verschiebung der Schwingungsfrequenz → Druckabfall

### 9.3 Reset-Prozedur

Nach jedem Reifenwechsel oder Druckausgleich muss das System resetzt werden:

1. Alle Reifendrücke auf Sollwert einstellen
2. über Bordcomputer oder Menü: **TPMS-Reset** auswählen
3. System lernt ca. **20-60 Minuten** Fahrbetrieb
4. Nach Lernphase: System überwacht automatisch

### 9.4 Warnung

| Zustand | Anzeige |
|---|---|
| Druckabfall erkannt | Gelbe TPMS-Leuchte (Reifendruck-Symbol) |
| Systemfehler | Gelbe TPMS-Leuchte blinkt |
| System aktiv | Keine Leuchte |

### 9.5 Empfohlene Reifendrücke Astra J 1.4 Turbo

| Achse | Belastung | Reifendruck |
|---|---|---|
| Vorne | Normal (1-3 Pers.) | 2.3 bar (33 psi) |
| Vorne | Maximalbelastung | 2.5 bar (36 psi) |
| Hinten | Normal | 2.3 bar (33 psi) |
| Hinten | Maximalbelastung | 2.5 bar (36 psi) |
| Reserve | - | 4.2 bar (60 psi) |

---

## 10. Airbag-Steuergerät und Seitenairbag-Sensoren

### 10.1 Airbag-Steuergerät (SDM – Sensing and Diagnostic Module)

| Eigenschaft | Detail |
|---|---|
| **Hersteller** | Continental / Autoliv (je nach Lieferant) |
| **Bezeichnung** | SDM (Sensing and Diagnostic Module) |
| **Anbauort** | Mitteltunnel, unter der Mittelkonsole |
| **CAN-Bus** | High-Speed CAN (500 kbit/s) |
| **Spannungsversorgung** | 12V Bordnetz |
| **Backup-Kondensator** | Für Zündung bei Spannungsausfall |

### 10.2 Airbag-System Übersicht

```
                    ┌──────────────────┐
                    │   SDM            │
                    │  (Airbag-ECU)    │
                    │                  │
                    │ ┌──────────────┐ │
                    │ │ Auswertung   │ │
                    │ │ (Algorithmus)│ │
                    │ └──────────────┘ │
                    └──────┬───────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────┴────┐      ┌─────┴─────┐      ┌─────┴─────┐
   │Frontal- │      │ Seiten-   │      │ Vorhang-  │
   │Airbags  │      │ Airbags   │      │ Airbags   │
   └─────────┘      └───────────┘      └───────────┘
```

### 10.3 Sensoren im Airbag-System

| Sensor | Typ | Ort | Funktion |
|---|---|---|---|
| **Front-Crash-Sensoren (2×)** | Beschleunigung (MEMS) | Frontscheinwerferbereich | Frontalaufprall erkennen |
| **Seiten-Crash-Sensoren (2×)** | Drucksensor (in Tür) | Türen (Fahrer/Beifahrer) | Seitenaufprall erkennen |
| **Zentraler Beschleunigungssensor** | MEMS (3-Achsen) | SDM (Mitteltunnel) | Verzögerungsmessung |
| **Gierratensensor** | MEMS | SDM (Mitteltunnel) | Rotationsbewegung |
| **Sitzbesetzsensor** | Druckmatratze | Fahrersitz | Erkennt ob Fahrer vorhanden |
| **Gurtstraffer-Sensoren** | Hall-Sensor | Gurtschloss | Gurtanlege-Erkennung |
| **Kindersitzerkennung** | Gewicht / Kapazitiv | Beifahrersitz | Airbag deaktivierung bei Kind |

### 10.4 Aufprall-Phasen (Beispiel Frontalaufprall)

```
Zeitachse (ms):
  0      15      30      45      60      80     100
  │       │       │       │       │       │       │
  ├──Erkennung──┤       │       │       │       │
  │  Crash wird │       │       │       │       │
  │  erkannt    │       │       │       │       │
  │             ├──Airbag entfaltet──┤       │       │
  │             │  (ca. 30-40 ms)   │       │       │
  │             │                    │       │       │
  │             │       ├──Gurtstraffer──┤    │       │
  │             │       │  (Vorspannung) │    │       │
```

### 10.5 Fehlercodes (typische DTCs)

| DTC | Beschreibung |
|---|---|
| B0001 | Fahrer-Seitenairbag – Hoher Widerstand |
| B0002 | Beifahrer-Seitenairbag – Hoher Widerstand |
| B0003 | Fahrer-Vorhangairbag – Hoher Widerstand |
| B0004 | Beifahrer-Vorhangairbag – Hoher Widerstand |
| B0010 | Front-Crash-Sensor Fahrerseite – Fehler |
| B0011 | Front-Crash-Sensor Beifahrerseite – Fehler |
| B0012 | Zentraler Beschleunigungssensor – Fehler |
| B0014 | Gurtstraffer Fahrerseite – Fehler |
| B0015 | Gurtstraffer Beifahrerseite – Fehler |
| B0020 | Airbag-Stromkreis – Spannung zu niedrig |
| B0022 | Kindersitzerkennung – Fehler |
| B0081 | Airbag-Kontrollleuchte – Fehler |

---

## 11. OBD-II PIDs für Bremssystem

### 11.1 Standard-OBD-II PIDs

Über den OBD-II-Diagnosestecker (DLC) können folgende PIDs für das Bremssystem abgefragt werden:

| PID (hex) | PID (dec) | Beschreibung | Einheit |
|---|---|---|---|
| 0x0D | 13 | Fahrzeuggeschwindigkeit | km/h |
| 0x0C | 12 | Motordrehzahl | U/min |
| 0x04 | 4 | Berechnete Motorlast | % |
| 0x11 | 17 | Drosselklappenposition | % |
| 0x42 | 66 | Steuerspannung des Moduls | V |
| 0x01 | 1 | MIL-Status (Motorkontrollleuchte) | Bit |

### 11.2 Herstellerspezifische PIDs (Opel/GM)

| PID (hex) | Beschreibung | Einheit |
|---|---|---|
| 0x22 F0 00 | Bremsenverschleiß vorn | % |
| 0x22 F0 01 | Bremsenverschleiß hinten | % |
| 0x22 F0 02 | Bremsdruck (Hauptleitung) | bar |
| 0x22 F0 03 | ABS-Status | Codiert |
| 0x22 F0 04 | ESP-Status | Codiert |
| 0x22 F0 05 | Lenkwinkel | ° |
| 0x22 F0 06 | Gierrate | °/s |
| 0x22 F0 07 | Querbeschleunigung | g |

> **Hinweis:** Die genauen Opel/GM-spezifischen PIDs können je nach Diagnose-Software variieren. Für volle Zugriff wird **OPCOM**, **GDS2** oder **Tech2** empfohlen.

### 11.3 UDS-Diagnose (über 0x7E0 / 0x7E8)

| UDS Service | Service-ID | Funktion |
|---|---|---|
| DiagnosticSessionControl | 0x10 | Diagnose-Modus wechseln |
| ReadDTCInformation | 0x19 | Fehlercodes auslesen |
| ClearDiagnosticInformation | 0x14 | Fehlercodes löschen |
| ReadDataByIdentifier | 0x22 | Live-Daten abfragen |
| WriteDataByIdentifier | 0x2E | Kalibrierdaten schreiben |
| RoutineControl | 0x31 | Stellgliedtests durchführen |
| RequestDownload | 0x34 | Software-Update |

### 11.4 Typische Fehlercodes (DTCs) – ABS/ESP

| DTC | Beschreibung | Ursache |
|---|---|---|
| C0035 | Vorderrad-links Sensor | Sensor defekt, Verkabelung, Zähnkr. |
| C0040 | Vorderrad-rechts Sensor | Sensor defekt, Verkabelung, Zähnkr. |
| C0045 | Hinterrad-links Sensor | Sensor defekt, Verkabelung, Zähnkr. |
| C0050 | Hinterrad-rechts Sensor | Sensor defekt, Verkabelung, Zähnkr. |
| C0055 | Kein Raddrehzahlsignal | Mehrere Sensoren defekt |
| C0060 | Vorderrad-links Bremse | Ventil defekt, Hydraulik defekt |
| C0065 | Vorderrad-rechts Bremse | Ventil defekt, Hydraulik defekt |
| C0070 | Hinterrad-links Bremse | Ventil defekt, Hydraulik defekt |
| C0075 | Hinterrad-rechts Bremse | Ventil defekt, Hydraulik defekt |
| C0110 | ESP-Pumpe | Elektropumpe defekt |
| C0120 | ESP-Drucksensor | Sensor defekt |
| C0131 | Bremsdruck-Sensor | Sensor defekt |
| C0161 | Bremslichtschalter | Schalter defekt |
| C0186 | Querbeschleunigungssensor | Sensor defekt |
| C0196 | Gierratensensor | Sensor defekt |
| C0280 | Lenkwinkelsensor | Sensor defekt, Kalibrierung fehlt |
| C0550 | ECU-Speicherfehler | Steuergerät defekt |

---

## 12. Wartungshinweise

### 12.1 Regelmäßige Wartung

| Komponente | Intervall | Maßnahme |
|---|---|---|
| **Bremsbeläge vorn** | Alle 30.000-50.000 km | Verschleiß prüfen, ggf. tauschen |
| **Bremsbeläge hinten** | Alle 50.000-80.000 km | Verschleiß prüfen, ggf. tauschen |
| **Bremsscheiben vorn** | Alle 60.000-80.000 km | Dicke prüfen (Min. 22mm) |
| **Bremsscheiben hinten** | Alle 80.000-120.000 km | Dicke prüfen (Min. 10mm) |
| **Bremsflüssigkeit** | Alle 2 Jahre | DOT 4 oder DOT 4+, Taupunkt > 230°C |
| **ABS-Sensoren** | Bei Reifenwechsel prüfen | Luftspalt, Korrosion |
| **TPMS-Reset** | Nach jedem Reifenwechsel | Bordcomputer-Reset durchführen |
| **ESP-Steuergerät** | Bei Fehlerspeichereintrag | Diagnose, ggf. Kalibrierung |

### 12.2 Bremsflüssigkeit

| Eigenschaft | Wert |
|---|---|
| **Typ** | DOT 4 oder DOT 4+ |
| **Taupunkt** | > 230°C (DOT 4) |
| **Wechselintervall** | Alle 2 Jahre |
| **Füllmenge** | ca. 1.0 Liter (gesamt) |
| **Behälter** | Links im Motorraum, an der Spritzwand |

### 12.3 Nach Reifenwechsel

1. **Reifendrücke prüfen** und auf Sollwert einstellen (2.3 bar / 33 psi)
2. **TPMS-Reset** im Bordcomputer durchführen
3. **Fahrzeug ca. 30-60 Min. fahren** (Lernphase)
4. **Prüfen:** TPMS-Leuchte muss erloschen bleiben

### 12.4 Sensoren-Instandhaltung

- **ABS-Sensoren:** Bei Korrosion oder mechanischer Beschädigung austauschen
- **Luftspalt prüfen:** 0.3 – 1.5 mm
- **Zähnkränze:** Auf Beschädigungen und Verschmutzung prüfen
- **Verkabelung:** Auf Durchgang und Isolationswiderstand prüfen

---

## 13. Typische Fehler und deren Ursachen

### 13.1 Häufige ABS/ESP-Fehler im Astra J

#### 1. "ESP-Fehler" / "ABS-Fehler" auf dem Display

**Mögliche Ursachen:**
- Defekter Raddrehzahl-Sensor (häufigste Ursache!)
- Korrosion am Stecker des ABS-Sensors
- Beschädigter Zähnkranz
- Defektes ESP-Steuergerät
- Niedrige Batteriespannung
- Bremsflüssigkeitsstand zu niedrig

#### 2. ESP-Lampe blinkt dauerhaft

**Mögliche Ursachen:**
- System aktiv (normal bei schlingernder Fahrt)
- Defekter Gierratensensor
- Defekter Lenkwinkelsensor
- Kalibrierung verloren (Lenkwinkelsensor)

#### 3. TPMS-Warnung trotz korrektem Druck

**Mögliche Ursachen:**
- TPMS wurde nach Reifenwechsel nicht resettet
- Reifengröße geändert (System erkennt neue Größe nicht)
- Softwarefehler im TPMS-Algorithmus
- Temperaturbedingte Druckschwankungen (normal!)

#### 4. Berganfahrhilfe funktioniert nicht

**Mögliche Ursachen:**
- Neigungssensor defekt
- Kupplungssensor (Schaltgetriebe) defekt
- ESP-Steuergerät-Fehler
- Batteriespannung zu niedrig

### 13.2 Fehlerbehebung – Checkliste

```
1. DTC auslesen (OPCOM, GDS2 oder Tech2)
2. Batteriespannung prüfen (> 12.5V)
3. ABS-Sensoren auf Stecker und Verkabelung prüfen
4. Zähnkränze auf Verschmutzung/Beschädigung prüfen
5. Bremsflüssigkeitsstand prüfen
6. Bremsbeläge auf Verschleiß prüfen
7. Nach Reifenwechsel: TPMS-Reset durchführen
8. Bei ESP-Fehler: Lenkwinkelsensor kalibrieren
9. Bei anhaltendem Fehler: Steuergerät testen/tauschen
```

---

## 14. Sensordaten-Zusammenfassung

### 14.1 Alle Sensoren im Überblick

| Sensor | Messgröße | Bereich | Auflösung | Ort |
|---|---|---|---|---|
| Raddrehzahl (4×) | Umdrehungen/s | 0-1500 U/min | 48 Impulse/Rev. | An Radnabe |
| Lenkwinkel | Winkel | ±780° | 0.1° | Lenksäule |
| Gierrate | °/s | ±100°/s | 0.01°/s | Mitteltunnel |
| Querbeschleunigung | g | ±2g | 0.01g | Mitteltunnel |
| Längsbeschleunigung | g | ±1.5g | 0.01g | IMU (kombiniert) |
| Bremsdruck | bar | 0-200 bar | 1 bar | Hydraulikmodul |
| Bremspedal | an/aus + Weg | 2 Stufen | - | Pedal |
| Neigung (Pitch) | ° | ±15° | 0.1° | IMU (kombiniert) |

### 14.2 Sensordaten-CAN-Zuordnung

| Sensor | CAN-ID (hex) | Zyklus | Byte-Offset |
|---|---|---|---|
| Raddrehzahl FL | 0x0A0 | 10 ms | 0-1 |
| Raddrehzahl FR | 0x0A1 | 10 ms | 0-1 |
| Raddrehzahl HL | 0x0A2 | 10 ms | 0-1 |
| Raddrehzahl HR | 0x0A3 | 10 ms | 0-1 |
| Gierrate | 0x180 | 10 ms | 2-3 |
| Querbeschleunigung | 0x180 | 10 ms | 4-5 |
| Lenkwinkel | 0x0C0 | 10 ms | 0-1 |
| Bremsdruck | 0x348 | 20 ms | 0-1 |
| ESP-Status | 0x180 | 10 ms | 0-1 |

---

## 15. Technische Zeichnungen / Layout

### 15.1 Sensor-Positionen im Astra J

```
                    Astra J – Unterbodenansicht
                    
          Vorne (Motorraum)
    ┌────────────────────────────┐
    │                            │
    │  ● Front-Crash-Sensor L    │
    │  ● Front-Crash-Sensor R    │
    │                            │
    │     ┌──────────────┐       │
    │     │   Motor &    │       │
    │     │ Getriebe     │       │
    │     └──────────────┘       │
    │                            │
    │  ◉ ABS-Sensor FL    ◉ ABS │
    │       (Radnabe)      FR   │
    │                          │
    │  ┌────────────────────┐   │
    │  │                    │   │
    │  │   Mitteltunnel     │   │
    │  │  ┌──────────────┐  │   │
    │  │  │ ESP-Sensor   │  │   │
    │  │  │ (IMU/Gyro)   │  │   │
    │  │  └──────────────┘  │   │
    │  │  ┌──────────────┐  │   │
    │  │  │ SDM          │  │   │
    │  │  │ (Airbag-ECU) │  │   │
    │  │  └──────────────┘  │   │
    │  │                    │   │
    │  └────────────────────┘   │
    │                            │
    │  ◉ ABS-Sensor HL    ◉ ABS │
    │       (Radnabe)      HR   │
    │                            │
    │  ● Seitenaufprall-Sensor L │
    │  ● Seitenaufprall-Sensor R │
    │                            │
    │  ┌──────────────┐          │
    │  │  ESP-ECU      │          │
    │  │  (Hydraulik + │          │
    │  │   Steuerung)  │          │
    │  └──────────────┘          │
    │                            │
    └────────────────────────────┘
          Hinten (Heck)

Legende:
● = Crash-Sensor
◉ = ABS-Raddrehzahl-Sensor
┌─┐ = Steuergerät / Modul
```

### 15.2 ABS/ESP-Hydraulikmodul

```
    ┌─────────────────────────────────────────┐
    │         Bosch ESP 9.0 Hydraulikmodul    │
    │                                         │
    │  Bremskraftverstärker                   │
    │         │                               │
    │         ▼                               │
    │  ┌──────────────┐                       │
    │  │ Hauptzylinder │                       │
    │  │   (2-Kreis)  │                       │
    │  └──────┬───────┘                       │
    │         │                               │
    │    ┌────┴────┐                          │
    │    │ Vorderrad│  Hinterrad              │
    │    │ Kreis L  │  Kreis L                │
    │    │    │     │    │                    │
    │    │ [V] │    │ [V] │                   │
    │    │    │     │    │                    │
    │    │ [V] │    │ [V] │                   │
    │    └──┬──┘    └──┬──┘                   │
    │       │          │                      │
    │    ◉ Vord. L   ◉ Hinter L              │
    │                                         │
    │  Vorderrad Kreis R  Hinterrad Kreis R  │
    │    │     │    │    │     │    │        │
    │    [V] │    [V] │    [V] │    [V] │    │
    │    │          │    │          │        │
    │    ◉ Vord. R   ◉ Hinter R              │
    │                                         │
    │  [V] = Absperrventil (2 pro Rad)       │
    │  ◉   = Raddrehzahl-Sensor              │
    │                                         │
    │  Drucksensoren: 3× (Haupt, Vorder,     │
    │                         Hinter)         │
    │  Elektropumpe: 1×                        │
    └─────────────────────────────────────────┘
```

---

## 16. Quellen und Referenzen

### 16.1 Offizielle Dokumentation

| Quelle | URL |
|---|---|
| Wikipedia – Elektronisches Stabilitätsprogramm | https://de.wikipedia.org/wiki/Fahrdynamikregelung |
| Wikipedia – Antiblockiersystem | https://de.wikipedia.org/wiki/Antiblockiersystem |
| Wikipedia – Reifendruckkontrollsystem | https://de.wikipedia.org/wiki/Reifendruckkontrollsystem |
| Wikipedia – Berganfahrhilfe | https://de.wikipedia.org/wiki/Berganfahrhilfe |
| Wikipedia – Drehratensensor | https://de.wikipedia.org/wiki/Drehratensensor |
| Wikipedia – Electronic Stability Control | https://en.wikipedia.org/wiki/Electronic_stability_control |
| Wikipedia – Anti-lock Braking System | https://en.wikipedia.org/wiki/Anti-lock_braking_system |
| Wikipedia – Tire Pressure Monitoring System | https://en.wikipedia.org/wiki/Tire-pressure_monitoring_system |
| Wikipedia – OBD-II PIDs | https://en.wikipedia.org/wiki/OBD-II_PIDs |

### 16.2 Bosch Dokumentation

| Quelle | Beschreibung |
|---|---|
| Bosch Kraftfahrtechnisches Taschenbuch | Technische Daten zu ABS/ESP |
| Bosch ESP 9.0 Technische Dokumentation | Entwickler-Handbuch (nicht öffentlich) |
| Bosch Sensoren für Fahrzeugdynamik | Übersicht aller Sensoren |

### 16.3 Opel/GM Dokumentation

| Quelle | Beschreibung |
|---|---|
| Opel Astra J Werkstatthandbuch | Offizielles Service-Handbuch |
| GM TIS (Technical Information Service) | Technische Service-Informationen |
| OPCOM / Opel Diagnose | Spezifische Fehlercodes und Kalibrierungen |
| GDS2 (Global Diagnostic System 2) | GM-Diagnose-Software |

### 16.4 Forumsquellen

| Quelle | URL |
|---|---|
| Opel Astra J Forum (Deutsch) | https://www.opel-astra-j.de/ |
| Motor-Talk – Opel Astra J | https://www.motor-talk.de/forum/opel-astra-j.html |
| AstraForum.de | https://www.astraforum.de/ |

---

## 17. Wichtige Hinweise

### 17.1 Sicherheitshinweise

> **WARNUNG:** Arbeiten am Bremssystem erfordern Fachkenntnisse!
> - Bremsen nur von geschultem Personal warten lassen
> - Nach Every Bremsenwechsel: Einlaufphase beachten (30x vorsichtig bremsen)
> - Bremsflüssigkeit ist hygroskopisch – regelmäßig wechseln
> - Bei ESP-Fehlern: Fahrzeug nicht in Grenzbereich fahren
> - Bei ABS-Fehler: ABS-Tretemod beachten (pulsieren lassen, nicht pumpen)

### 17.2 Kalibrierung

Nach bestimmten Reparaturen ist eine **Kalibrierung** erforderlich:

| Reparatur | Kalibrierung |
|---|---|
| Lenkwinkelsensor aus-/eingebaut | Ja – per Diagnose-Tool |
| ESP-ECU ausgetauscht | Ja – Komplette Neukalibrierung |
| Fahrwerk verstellt (Spur, Sturz) | Empfohlen |
| Aufprall-Reparatur | Ja – vollständige Systemprüfung |
| Reifenwechsel (andere Größe) | TPMS-Reset |

### 17.3 Nützliche Diagnose-Tools

| Tool | Funktion | Preisbereich |
|---|---|---|
| **OPCOM** | Opel-spezifische Diagnose | 50-150 € |
| **GDS2** | GM-Diagnose (vollständig) | Ab 200 € |
| **Tech2** | Klassische GM-Diagnose | Ab 500 € |
| **Autel MaxiSys** | Multi-Make Diagnose | Ab 500 € |
| **ELM327 + App** | Grundlegende OBD-II-Diagnose | 10-30 € |

---

*Dieses Dokument dient nur zu Informationszwecken. Alle Angaben ohne Gewähr. Für verbindliche technische Daten wenden Sie sich an den Hersteller oder einen autorisierten Opel-Service.*

*Erstellt: Mai 2026*
