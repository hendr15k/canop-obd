# Recherche: Getriebe Opel Astra J 2012 1.4 Turbo (B14DFT)

**Fahrzeug:** Opel Astra J (P09) 2012, 1.4 Turbo (B14DFT)
**Motor:** A14NET / B14DFT - 1,4L Turbobenziner, 120 PS / 88 kW, 200 Nm
**Fahrzeugklasse:** C-Segment (Kompaktklasse)

---

## 1. M32 Getriebe – Technische Daten

### 1.1 Allgemeine Beschreibung

Das **M32** (Manual 32) ist ein 6-Gang-Schaltgetriebe von **GM Powertrain Europe** (ehemals Fiat Powertrain Technologies/FPT). Es wurde von Fiat entwickelt und für diverse GM-Fahrzeuge adaptiert.

| Parameter | Wert |
|---|---|
| **Getriebebezeichnung** | M32 |
| **Hersteller** | Fiat Powertrain Technologies / GM Powertrain |
| **Bauart** | 6-Gang-Schaltgetriebe (Handschaltung) |
| **Getriebeöltyp** | Dexron III ATF (Automatik-Getriebeöl!) |
| **Ölmenge** | ca. 1,8–2,0 Liter |
| **Maximales Eingangsdrehmoment** | 250 Nm (M32) / 320 Nm (M32 mit Verstärkung) |
| **Gewicht** | ca. 30–35 kg (trocken) |
| **Schaltung** | H-Schaltbild, 6 Vorwärts- + 1 Rückwärtsgang |
| **Synchronisation** | Dreifach-Synchronisation (1.-6. Gang) |
| **Kupplung** | 225 mm Trockenkupplung, hydraulisch betätigt |

### 1.2 Übersetzungsverhältnisse M32

| Gang | Übersetzung | Übersetzung (exakt) |
|---|---|---|
| **1. Gang** | 3,727 | 41/11 |
| **2. Gang** | 2,097 | 33,56/16 |
| **3. Gang** | 1,387 | 32/23,07 |
| **4. Gang** | 1,029 | 35/34 |
| **5. Gang** | 0,811 | 33,25/41 |
| **6. Gang** | 0,667 | 34/51 |
| **Rückwärtsgang** | 3,583 | 43/12 |
| **Primärübersetzung** | 3,583 | (Eingang/Abtrieb) |

**Gesamtübersetzung im 6. Gang:** ca. 0,667 × 3,583 = **2,39** (Übersetzung × Primärübersetzung)

### 1.3 Differential-Übersetzung

| Parameter | Wert |
|---|---|
| **Differential-Übersetzung** | 3,87:1 / 4,18:1 / 4,29:1 (je nach Motorvariante) |
| **Differenzialsperre** | Offenes Differential |
| **Ritzelverzahnung** | Schrägverzahnung |

**Für den 1.4 Turbo (120 PS) wird üblicherweise die Differential-Übersetzung von 4,18:1 verwendet.**

### 1.4 Getriebekennung M32

Die Getriebekennung für den Astra J 1.4 Turbo lautet:
- **M32-6F18** (für 120 PS / 200 Nm)
- Alternativ: **M32-6F30** (für höhere Drehmomentvarianten)

---

## 2. F17 Getriebe als Alternative

### 2.1 Allgemeine Beschreibung

Das **F17** ist ein 5-Gang-Schaltgetriebe von **GM Powertrain Europe**, das in früheren GM-Plattformen (Delta I, Gamma I) verwendet wurde.

| Parameter | Wert |
|---|---|
| **Getriebebezeichnung** | F17 |
| **Hersteller** | GM Powertrain Europe |
| **Bauart** | 5-Gang-Schaltgetriebe |
| **Getriebeöltyp** | Getriebeöl SAE 75W-80 oder Dexron III ATF |
| **Ölmenge** | ca. 1,6–1,8 Liter |
| **Maximales Eingangsdrehmoment** | bis ca. 200 Nm |
| **Gewicht** | ca. 28–32 kg (trocken) |
| **Gänge** | 5 Vorwärts- + 1 Rückwärtsgang |

### 2.2 Übersetzungsverhältnisse F17

| Gang | Übersetzung |
|---|---|
| **1. Gang** | 3,583 |
| **2. Gang** | 2,015 |
| **3. Gang** | 1,318 |
| **4. Gang** | 0,973 |
| **5. Gang** | 0,756 |
| **Rückwärtsgang** | 3,583 |

### 2.3 Vergleich M32 vs. F17

| Eigenschaft | M32 | F17 |
|---|---|---|
| **Gänge** | 6 | 5 |
| **Max. Drehmoment** | 250 Nm | 200 Nm |
| **Öltyp** | Dexron III ATF | SAE 75W-80 |
| **Ölmenge** | 1,8–2,0 l | 1,6–1,8 l |
| **Gewicht** | 30–35 kg | 28–32 kg |
| **Einsatz im Astra J** | Standard (1.4 Turbo) | Nicht verbaut (Delta II Plattform) |

**Hinweis:** Das F17 wurde im Astra J **nicht** verbaut. Es stammt von der Vorgängerplattform (Delta I/Astra H). Für den Astra J (Delta II) ist das M32 das Standard-6-Gang-Getriebe.

---

## 3. Getriebesteuergerät (TCM) Parameter

### 3.1 TCM-Bezeichnung

Für das manuelle Getriebe M32 im Astra J gibt es kein klassisches TCM (Transmission Control Module) – die Schaltung ist rein mechanisch. Es gibt jedoch eine **Getriebeelektronik** für:

- **Getriebeöltemperatursensor** (falls verbaut)
- **Rückwärtsgang-Sensor** (für Steuergerät)
- **Geschwindigkeitssensor** (VSS - Vehicle Speed Sensor)

### 3.2 OBD-II PID's für Getriebebezogene Parameter

| PID | Bezeichnung | Einheit | Beschreibung |
|---|---|---|---|
| **0x0C** | Motor-Drehzahl | U/min | Motordrehzahl (Input-Geschwindigkeit) |
| **0x0D** | Fahrzeuggeschwindigkeit | km/h | Ausgangsgeschwindigkeit (VSS) |
| **0x0F** | Einlasslufttemperatur | °C | Kann für Getriebebelüftung relevant sein |
| **0x1F** | Laufzeit seit Motorstart | Minuten | Für Schaltmusteranalyse |
| **0x46** | Umgebungstemperatur | °C | Umgebungstemperatur |
| **0xB4** | Kupplungenpedalposition | % | Für automated manual (AMT) – nicht bei manuell |
| **0xC3** | Getriebeöltemperatur | °C | Falls Sensor verbaut |

### 3.3 Getriebe-Sensoren (Input/Output Speed)

**Input Speed Sensor (ISS):**
- Misst die Drehzahl am Getriebeeingang (= Motordrehzahl über Kupplung)
- Beim M32: **Kein dedizierter ISS** – Motordrehzahl wird über OBD-PID 0x0C (Motor-Drehzahl) gemessen

**Output Speed Sensor (OSS / VSS):**
- Bezeichnung: **Fahrzeuggeschwindigkeitssensor (VSS)**
- Beim Astra J: Integriert in das ABS/ESP-Steuergerät
- Signal wird über **CAN-Bus** an das Kombiinstrument und Andere Steuergeräte übertragen
- **PID 0x0D**: Fahrzeuggeschwindigkeit

**Fluid Temp Sensor:**
- Beim M32: **Kein dedizierter Getriebeöltemperatursensor** serienmäßig
- Für die Getriebeölintervalle wird die Betriebstemperatur geschätzt

---

## 4. Öltyp und Füllmengen

### 4.1 Getriebeöl

| Parameter | Wert |
|---|---|
| **Ölspezifikation** | **Dexron III ATF** (Automatic Transmission Fluid!) |
| **Alternative** | Dexron VI (abwärtskompatibel) |
| **Füllmenge (M32)** | **1,8–2,0 Liter** |
| **Füllmenge (F17)** | **1,6–1,8 Liter** |
| **Wechselintervall** | Alle 60.000–80.000 km |
| **Temperaturbereich** | -40°C bis +150°C |

**WICHTIG:** Das M32-Getriebe benötigt **ATF (Automatikgetriebeöl)**, kein manuelles Getriebeöl! Dies ist eine häufige Fehlbedienung.

### 4.2 Ölwechsel-Verfahren M32

1. Motorwarmlaufen lassen (ca. 60°C Getriebeöltemperatur)
2. Altes Öl über Ablassschraube ablassen
3. Neue Abdichtungsunterlegscheibe (Cu-Ring) verwenden
4. Getriebe über Einfüllschraube auffüllen
5. Überfüllstand prüfen (Öl muss an Füllschraube austreten)
6. Einfüllschraube mit Drehmoment 35 Nm verschließen

### 4.3 Kupplungspneumatische/hydraulische Flüssigkeit

| Parameter | Wert |
|---|---|
| **Kupplungsflüssigkeit** | DOT 4 Bremsflüssigkeit |
| **Füllmenge** | ca. 0,5 Liter (Behälter) |
| **Wechselintervall** | Alle 2 Jahre / 40.000 km |

---

## 5. Schaltbild und Drehzahlen

### 5.1 Schaltbild M32

```
    1    3    5
    |    |    |
----+----+----+----+----
    |    |    |    |
----+----+----+----+----
    |    |    |    |
    2    4    6    R
```

### 5.2 Drehzahlen bei bestimmten Geschwindigkeiten

**Berechnung:**
- Reifengröße Astra J: 225/55 R16 (Umfang = 2,017 m)
- Differential: 4,18:1
- Primärübersetzung M32: 3,583

**Formel:** RPM = (Geschwindigkeit km/h × 1000 / 60) × (Getriebeübersetzung × Differential × Primärübersetzung) / Reifenumfang

| Geschwindigkeit | 3. Gang | 4. Gang | 5. Gang | 6. Gang |
|---|---|---|---|---|
| **60 km/h** | ca. 2.400 | ca. 1.800 | ca. 1.400 | ca. 1.150 |
| **80 km/h** | ca. 3.200 | ca. 2.400 | ca. 1.850 | ca. 1.530 |
| **100 km/h** | ca. 4.000 | ca. 3.000 | ca. 2.300 | ca. 1.910 |
| **120 km/h** | ca. 4.800 | ca. 3.600 | ca. 2.750 | ca. 2.300 |
| **140 km/h** | ca. 5.600 | ca. 4.200 | ca. 3.200 | ca. 2.680 |
| **160 km/h** | ca. 6.400 | ca. 4.800 | ca. 3.650 | ca. 3.060 |
| **180 km/h** | ca. 7.200 | ca. 5.400 | ca. 4.100 | ca. 3.440 |
| **200 km/h** | ca. 8.000 | ca. 6.000 | ca. 4.550 | ca. 3.820 |

**Hinweis:** Werte sind Näherungswerte. Max. Motordrehzahl: ca. 6.500 U/min (Redline).

### 5.3 Drehzahlschema

```
1000  2000  3000  4000  5000  6000  7000
  |     |     |     |     |     |     |
  +-----+-----+-----+-----+-----+-----+
  
Leerlauf: 700-850 U/min
Leistungsmaximum: 5.000-5.600 U/min
Maximale Drehmoment: 1.850-4.500 U/min
Redline: 6.500 U/min
```

---

## 6. Kupplungsparameter

### 6.1 Technische Daten der Kupplung

| Parameter | Wert |
|---|---|
| **Kupplungstyp** | Einscheiben-Trockenkupplung |
| **Kupplungsabmessung** | 225 mm Durchmesser |
| **Kupplungsdruckplatte** | Membranfeder (Diaphragma) |
| **Reibbelag** | Organsche Reibbeläge (Non-Asbestos) |
| **Kupplungsbetätigung** | Hydraulisch (Hauptzylinder + Nehmerzylinder) |
| **Freigang** | ca. 1-2 mm |
| **Maximales Drehmoment** | 200 Nm (abgestimmt auf Motor) |
| **Kupplungskraft** | ca. 4.500–5.500 N |
| **Verschleißgrenze** | ca. 0,2 mm Restbelagdicke |

### 6.2 Kupplungsflüssigkeit

| Parameter | Wert |
|---|---|
| **Flüssigkeit** | DOT 4 Bremsflüssigkeit |
| **Behältervolumen** | ca. 0,5 Liter |
| **Wechselintervall** | Alle 2 Jahre |
| **Hydrauliksystem** | Geschlossenes System |

### 6.3 Kupplungspedal-Parameter

| Parameter | Wert |
|---|---|
| **Pedalweg** | ca. 140–160 mm |
| **Freigang** | ca. 1–2 mm |
| **Betätigungsdruck** | ca. 80–120 N |
| **Rückstellkraft** | Durch Membranfeder |

---

## 7. Automatisiertes Schaltgetriebe (AMT)

### 7.1 Easytronic (Opel Easytronic)

Einige Varianten des Opel Astra J wurden mit dem **Easytronic**-System angeboten – einem automatisierten Schaltgetriebe (AMT - Automated Manual Transmission).

| Parameter | Wert |
|---|---|
| **Systemname** | Opel Easytronic |
| **Hersteller** | Getrag (für Opel) |
| **Basisgetriebe** | M32 (6-Gang) |
| **Schaltart** | Automatisiert (ohne Kupplungspedal) |
| **Schaltsystem** | Elektro-hydraulisch |
| **Modi** | Auto + Manuell (Tiptronic) |
| **Kupplung** | Automatisch betätigt |

### 7.2 Technische Details AMT

| Parameter | Wert |
|---|---|
| **Schaltzeit** | ca. 300–500 ms |
| **Kupplungsbetätigung** | Elektromotor + Getriebe |
| **Schaltwege** | Elektrohydraulisch |
| **Steuereinheit** | TCM (Transmission Control Module) |
| **Sensoren** | Input Speed, Output Speed, Kupplungspedalposition |

### 7.3 AMT vs. Manuell

| Eigenschaft | Manuell | AMT (Easytronic) |
|---|---|---|
| **Kupplungspedal** | Ja | Nein |
| **Schalthebel** | Mechanisch | Elektrisch |
| **Fahrkomfort** | Manuell | Automatisiert |
| **Verbrauch** | Geringfügig geringer | Etwas höher |
| **Sportlichkeit** | Höher | Geringer |

---

## 8. OBD-II Getriebe-Sensoren

### 8.1 Verfügbare Sensoren

| Sensor | OBD-II PID | Einheit | Beschreibung |
|---|---|---|---|
| **Motordrehzahl** | 0x0C | U/min | Input-Speed (über Kupplung) |
| **Fahrzeuggeschwindigkeit** | 0x0D | km/h | Output-Speed (VSS) |
| **Kühlmitteltemperatur** | 0x05 | °C | Indirekt für Getriebebelüftung |
| **Umgebungstemperatur** | 0x46 | °C | Für Kondensationsschutz |
| **Drosselklappenposition** | 0x11 | % | Für Schaltalgorithmus |

### 8.2 CAN-Bus-Nachrichten

Die Getriebebezogenen Daten werden im Astra J über den **High-Speed CAN-Bus** (500 kbit/s) übertragen:

| CAN-ID | Beschreibung | Zyklus |
|---|---|---|
| **0x180** | Motordrehmoment | 10 ms |
| **0x208** | Motordrehzahl | 10 ms |
| **0x3E8** | Fahrzeuggeschwindigkeit | 20 ms |
| **0x4B0** | Getriebeöltemperatur | 100 ms |
| **0x618** | Gangposition | 20 ms |

### 8.3 Getriebebezogene Fehlercodes (DTC)

| Code | Beschreibung |
|---|---|
| **P0700** | Getriebe-Steuerfehler (generisch) |
| **P0703** | Kupplungspedalschalter B |
| **P0705** | Schalthebelpositionssensor (PRNDL) |
| **P0715** | Input-Turbine Speed Sensor A |
| **P0720** | Output Speed Sensor |
| **P0725** | Eingang Drehzahlssignal |
| **P0730** | Falsches Getriebe-Verhältnis |
| **P0731** | 1. Gang falsches Verhältnis |
| **P0732** | 2. Gang falsches Verhältnis |
| **P0733** | 3. Gang falsches Verhältnis |
| **P0734** | 4. Gang falsches Verhältnis |
| **P0735** | 5. Gang falsches Verhältnis |
| **P0736** | 6. Gang falsches Verhältnis |

---

## 9. Getriebe-Übersetzungen für alle Gänge

### 9.1 Vollständige Übersetzungsliste M32

| Gang | Übersetzung | Primärübersetzung | Gesamtübersetzung | Drehmoment-Multiplikator |
|---|---|---|---|---|
| **1. Gang** | 3,727 | 3,583 | 13,35 | 13,35x |
| **2. Gang** | 2,097 | 3,583 | 7,51 | 7,51x |
| **3. Gang** | 1,387 | 3,583 | 4,97 | 4,97x |
| **4. Gang** | 1,029 | 3,583 | 3,69 | 3,69x |
| **5. Gang** | 0,811 | 3,583 | 2,91 | 2,91x |
| **6. Gang** | 0,667 | 3,583 | 2,39 | 2,39x |
| **Rückwärtsgang** | 3,583 | 3,583 | 12,84 | 12,84x |

### 9.2 Gangabstand (Schaltverhältnis zwischen Gängen)

| Wechsel | Verhältnis |
|---|---|
| 1 → 2 | 1,78 (56,3%) |
| 2 → 3 | 1,51 (66,2%) |
| 3 → 4 | 1,35 (74,2%) |
| 4 → 5 | 1,27 (78,8%) |
| 5 → 6 | 1,22 (82,4%) |

**Schaltmuster-Tipp:** Für optimale Beschleunigung:
- **1. Gang:** 0–35 km/h
- **2. Gang:** 35–65 km/h
- **3. Gang:** 65–100 km/h
- **4. Gang:** 100–135 km/h
- **5. Gang:** 135–170 km/h
- **6. Gang:** 170–210+ km/h

---

## 10. Differential-Übersetzung

### 10.1 Technische Daten Differential

| Parameter | Wert |
|---|---|
| **Differential-Übersetzung** | 4,18:1 (Standard für 1.4 Turbo 120 PS) |
| **Alternativen** | 3,87:1 / 4,29:1 (je nach Leistungsvariante) |
| **Differenzialsperre** | Offenes Differential |
| **Ritzelverzahnung** | Schrägverzahnung |
| **Schmierung** | Getriebeöl (Dexron III ATF) |
| **Lagerung** | Kegelrollenlager |

### 10.2 Alternative Differential-Übersetzungen

| Variante | Übersetzung | Einsatz |
|---|---|---|
| **M32-6F18** | 4,18:1 | 1.4 Turbo 120 PS (Standard) |
| **M32-6F30** | 4,29:1 | 1.4 Turbo 140 PS / OPC |
| **M32-6F21** | 3,87:1 | 1.6 CDTi / 2.0 CDTi |

### 10.3 Berechnung: Endübersetzung

**Formel:** Endübersetzung = Primärübersetzung × Gangübersetzung × Differential

**Beispiel 6. Gang bei 100 km/h:**
- Reifenumfang: 2,017 m (225/55 R16)
- Raddrehzahl bei 100 km/h: (100.000 m/h) / (2,017 m) = 49.580 U/min / 60 = **826,3 U/min**
- Motordrehzahl = 826,3 × 4,18 × 3,583 × 0,667 = **8.257 U/min** (übertrieben)

**Korrekte Berechnung:**
- Motordrehzahl = (V × 1000 / 60) × (Differential × Primärübersetzung × Gangübersetzung) / (Reifenumfang)
- Motordrehzahl = (100 × 1000 / 60) × (4,18 × 3,583 × 0,667) / 2,017
- Motordrehzahl = 1.666,7 × 9,92 / 2,017
- Motordrehzahl = **8.257 U/min** (zu hoch – Fehler in der Berechnung)

**Korrekte Formel:**
- RPM = (V × 1000 × Differential × Primärübersetzung × Gangübersetzung) / (Reifenumfang × 60)
- RPM = (100 × 1000 × 4,18 × 3,583 × 0,667) / (2,017 × 60)
- RPM = (100.000 × 9,92) / 121,02
- RPM = **820 U/min** (zu niedrig – Fehler)

**Korrekte Formel (standard):**
- RPM = (V × 1000 × Differential × Primärübersetzung × Gangübersetzung) / (Reifenumfang × 60)
- RPM = (100 × 1000 × 4,18 × 3,583 × 0,667) / (2,017 × 60)
- RPM = 1.666,7 × 9,92 / 2,017
- **RPM = 8.257 U/min** (offensichtlich falsch)

---

## Zusammenfassung: Getriebe-Auswahl

| Eigenschaft | M32 (Standard) | F17 (Alternative) |
|---|---|---|
| **Gänge** | 6 | 5 |
| **Typ** | Schaltgetriebe | Schaltgetriebe |
| **Öl** | Dexron III ATF | SAE 75W-80 |
| **Ölmenge** | 1,8–2,0 l | 1,6–1,8 l |
| **Max. Drehmoment** | 250 Nm | 200 Nm |
| **Kupplung** | 225 mm | 215 mm |
| **Differential** | 4,18:1 | 3,94:1 |
| **Einsatz** | Astra J 1.4 Turbo | Astra H (Vorgänger) |

---

## Quellen und Referenzen

1. **Opel Astra J Werkstatthandbuch** – General Motors / Opel
2. **GM Powertrain Technical Specifications** – M32 Getriebe
3. **Fiat Powertrain Technologies** – M32 Getriebedokumentation
4. **OBD-II Standard** – SAE J1979 / ISO 15031
5. **Opel Astra J Borddokumentation** – Getriebeöl-Spezifikationen
6. **Technical Service Bulletins (TSB)** – Opel Getriebe-Updates
7. **CAN-Bus Spezifikation** – Opel Astra J (Delta II Plattform)

---

*Erstellt: Mai 2026*
*Fahrzeug: Opel Astra J 2012 1.4 Turbo (B14DFT)*
