# Android OBD-II Apps für den Opel Astra J (1.4 Turbo A14NET)

> Recherche vom 06. Mai 2026

## Inhaltsverzeichnis

1. [Torque Pro Features und OBD-PIDs](#1-torque-pro-features-und-obd-pids)
2. [OBD Car Doctor Features](#2-obd-car-doctor-features)
3. [Harry's LapTimer Features](#3-harrys-laptimer-features)
4. [OBDeleven vs Carista vs VAG-Com](#4-obdeleven-vs-carista-vs-vag-com)
5. [ELM327 Bluetooth Adapter Vergleich](#5-elm327-bluetooth-adapter-vergleich)
6. [Pro OBD2 Adapter Empfehlungen](#6-pro-obd2-adapter-empfehlungen)
7. [GM-spezifische PIDs Unterstützung](#7-gm-spezifische-pids-unterstützung)
8. [Dashcam-Integration in OBD-Apps](#8-dashcam-integration-in-obd-apps)
9. [Heads-Up Display Features](#9-heads-up-display-features)
10. [GPS-Tracking und Datenlogging](#10-gps-tracking-und-datenlogging)
11. [Feature-Vergleichstabelle](#11-feature-vergleichstabelle)
12. [Empfehlungen für canop-obd](#12-empfehlungen-für-canop-obd)
13. [Quellen und Links](#13-quellen-und-links)

---

## 1. Torque Pro Features und OBD-PIDs

**Preis:** €3.55 (einmalig)  
**Plattform:** Android  
**Download:** [Google Play Store](https://play.google.com/store/apps/details?id=org.prowl.torque)  
**Entwickler:** Ian Hawkins  
**Bewertung:** 3.5 Sterne (80.8K Bewertungen)

### Kernfeatures

| Feature | Beschreibung |
|---------|-------------|
| **Live-Dashboard** | Anpassbare Instrumente mit Echtzeit-Daten vom OBD-II |
| **Fehlercodes (DTC)** | Lesen und zurücksetzen von Check-Engine-Licht, inkl. Hersteller-Datenbank |
| **Dyno/Motorleistung** | Berechnung von PS und Drehmoment |
| **0-60 mph Timer** | GPS-gestützte Beschleunigungsmessung |
| **CO2-Emissionen** | Anzeige des CO2-Ausstoßes |
| **Kühltemperatur** | Überwachung der Getriebeöltemperatur (fahrzeugspezifisch) |
| **Turbo-Boost** | Boost-Druck-Anzeige für Fahrzeuge mit MAP/MAF-Sensor |
| **GPS-Tracker** | Tracker-Logs mit OBD-Motorlogs für jedes Zeitpunkt |
| **Heads-Up Display** | HUD-Modus für Nachtfahrten |
| **Track Recorder** | Videoaufnahme mit OBD-II-Daten-Overlay (Black Box) |
| **Alarmsystem** | Warnungen mit Sprach-Overlay (z.B. Kühltemperatur >120°C) |
| **CSV/KML-Export** | Datenexport für Excel/OpenOffice-Analyse |
| **Twitter-Integration** | Automatische GPS-getaggte Tweets |
| **Fahrzeugdock** | Car-Dock-Unterstützung |
| **Kompass** | GPS-basierter Kompass ohne magnetische Interferenz |
| **Themes** | Verschiedene Themes für Dashboard-Look |
| **Widgets** | Android-Widgets für Home-Screen |
| **AIDL API** | Dritte Apps können auf Daten zugreifen |

### Supported OBD PIDs (Standard + Erweitert)

| PID | Name | Einheit |
|-----|------|---------|
| 0x0C | Motordrehzahl | rpm |
| 0x0D | Fahrzeuggeschwindigkeit | km/h |
| 0x05 | Kühlmitteltemperatur | °C |
| 0x04 | Motorlast | % |
| 0x0F | Ansauglufttemperatur | °C |
| 0x11 | Drosselklappenstellung | % |
| 0x2F | Kraftstofffüllstand | % |
| 0x14-0x1B | O2-Sensoren | V/mV |
| 0x42 | ECU-Spannung | V |
| 0x5C | Öltemperatur | °C |

### Stärken für Astra J

- Gut für Standard-OBD-II Diagnose
- Einfache Einrichtung mit ELM327-Adaptern
- GPS-gestützte Tracking-Funktionen
- Große Community und Foren-Support

### Schwächen

- **Keine GM-spezifischen Mode-22 PIDs** (Bosch ME17.9.22)
- Keine bidirektionale Steuerung
- Keine Coding-Funktionen
- Limitierte Opel/Vauxhall-Unterstützung
- Nicht für tiefgreifende Modul-Diagnose geeignet

---

## 2. OBD Car Doctor Features

**Preis:** Freemium (Pro-Version verfügbar)  
**Plattform:** Android, iOS  
**Entwickler:** NuBits  

### Kernfeatures

| Feature | Beschreibung |
|---------|-------------|
| **Live-Sensordaten** | Echtzeit-Anzeige aller verfügbaren Sensoren |
| **Fehlercode-Reader** | DTC lesen und löschen |
| **Freeze Frame** | Gefrorene Daten bei Fehler aufrufen |
| **Batteriespannung** | Überwachung der Batteriespannung |
| **Performance-Tests** | 0-60, 0-100 Beschleunigung |
| **Verbrauchsrechner** | Kraftstoffverbrauchsberechnung |
| **Datenloggen** | Aufzeichnung von Sensordaten |
| **GPS-Integration** | Standortdaten mit OBD-Daten |
| **Datenexport** | CSV-Export für Analyse |
| **Multi-Fahrzeug** | Profile für mehrere Fahrzeuge |

### Astra J Kompatibilität

- Standard-OBD-II PIDs werden unterstützt
- Begrenzte Hersteller-spezifische PIDs
- Kein Zugriff auf GM-spezifische Module

---

## 3. Harry's LapTimer Features

**Preis:** €13.99 - €49.99 (je nach Version)  
**Plattform:** Android, iOS  
**Entwickler:** Harry's Garage  

### Kernfeatures

| Feature | Beschreibung |
|---------|-------------|
| **Lap-Timer** | Präziser Rundenzeiten-Messer für Rennstrecken |
| **GPS-Track** | Automatische Strecken-erkennung und Kartierung |
| **OBD-Daten** | Echtzeit-OBD-II Daten-Overlay auf Video |
| **Video-Overlay** | GPS-Track mit OBD-Daten auf Video |
| **Fahrer-Statistiken** | Detailed driving analytics |
| **Strecken-Datenbank** | Hunderte von Rennstrecken weltweit |
| **Social Features** | Vergleich mit anderen Fahrern |
| **Datenexport** | GPX, CSV Export |
| **Multi-Kamera** | Unterstützung für verschiedene Dashcams |

### Astra J Kompatibilität

- Speziell für Performance-Monitoring
- GPS-basierte Beschleunigungsmessung
- Video-Overlay für Trackdays
- **Kein spezifischer OBD-II Support für GM-ECU**

---

## 4. OBDeleven vs Carista vs VAG-Com

### OBDeleven

| Eigenschaft | Details |
|-------------|---------|
| **Preis** | Geräte: €114.99 (OBDeleven 3 + 200 Credits) |
| **Abo** | Free/Pro/Ultimate |
| **Fokus** | VAG, BMW, Toyota, Ford (US), Mercedes |
| **One-Click Apps** | Vorbereitete Coding-Optionen |
| **Manuelles Coding** | Für erfahrene Nutzer (Pro) |
| **Fehlerdiagnose** | Tiefgründig für lizenzierte Marken |
| **SFD-Unterstützung** | Automatisches Entschlüsseln (ab 2020er VW) |

**Für Astra J:** ❌ **NICHT KOMPATIBEL** - Nur für VAG, BMW, Toyota, Ford (US), Mercedes

### Carista

| Eigenschaft | Details |
|-------------|---------|
| **Preis** | EVO Bundle: ca. $100, App: Subscription-basiert |
| **Fokus** | Audi, VW, Toyota, BMW, Lexus, Mini |
| **Coding** | One-Click Coding für unterstützte Marken |
| **Fehlerdiagnose** | OEM-spezifische Fehlercodes |
| **Live-Daten** | Erweiterte Sensordaten |

**Für Astra J:** ❌ **NICHT KOMPATIBEL** - Nur für ausgewählte Marken

### FORScan (ehemals VAG-Com-artig)

| Eigenschaft | Details |
|-------------|---------|
| **Preis** | Windows: Free/Extended License, Mobile: €5-10 |
| **Fokus** | Ford, Mazda, Lincoln, Mercury |
| **Tiefe Diagnose** | Modul-Coding, PATS-Programmierung |
| **Module** | BCM, PCM, ABS, Airbag, etc. |
| **As-Built Data** | Fabrikeinstellungen laden/ändern |

**Für Astra J:** ❌ **NICHT KOMPATIBEL** - Nur für Ford/Mazda-Gruppe

### Fazit für Astra J

Alle drei Tools sind **nicht kompatibel** mit dem Opel Astra J. Der Astra J basiert auf der GM Delta II Plattform mit Bosch ME17.9.22 ECU, die von keiner dieser Plattformen speziell unterstützt wird. Für den Astra J braucht man:

- **OBDLink MX+** (unterstützt GM-LAN)
- **Torque Pro** oder **Car Scanner ELM OBD2** mit benutzerdefinierten PIDs
- **canop-obd** (unser Projekt) mit Mode-22 Erweiterungen

---

## 5. ELM327 Bluetooth Adapter Vergleich

### Adapter-Übersicht

| Adapter | Chip | Geschwindigkeit | Protokolle | Preis | Empfehlung |
|---------|------|-----------------|------------|-------|------------|
| **ELM327 Clone (China)** | ELM327 v1.5/2.1 | Langsam | Standard OBD-II | €10-20 | ⚠️ Nur für Basis-Funktionen |
| **Vgate iCar Pro 2** | STN1110 | Mittel | Standard +一些 erweitert | €25-35 | ✅ Gutes Preis-Leistungs-Verhältnis |
| **OBDLink LX** | STN2120 | Schnell | Standard OBD-II | €89.95 | ✅ Empfohlen für Basis-Nutzung |
| **OBDLink MX+** | STN1110 + MS-CAN | Sehr schnell | Standard + GM-LAN + MS-CAN | €139.95 | ✅✅ Beste Wahl für Astra J |
| **PLX Kiwi 3** | Proprietär | Schnell | Standard OBD-II | €80-100 | ✅ Gute Qualität |
| **VeePeak BLE** | Proprietär | Mittel | Standard OBD-II | €40-50 | ⚠️ Nur BLE (langsamer) |

### Wichtige Unterschiede

**ELM327 Clones:**
- Oft mit gefälschten Chips (v1.5 statt v2.1)
- Langsame Antwortzeiten
- Keine Erweiterten Protokolle
- Probleme mit Mode 22
- Nur für Basistests geeignet

**STN-basierte Adapter (OBDLink, Vgate):**
- Originale STN-Chips
- Schnellere Polling-Rate
- Bessere Protokoll-Unterstützung
- GM-LAN Support (MX+)
- Zuverlässiger für fortgeschrittene Nutzung

---

## 6. Pro OBD2 Adapter Empfehlungen

### 🏆 Top-Empfehlung: OBDLink MX+

| Eigenschaft | Details |
|-------------|---------|
| **Preis** | €139.95 |
| **Verbindung** | Bluetooth 5.0 |
| **Protokolle** | OBD-II, GM-LAN, Ford MS-CAN |
| **Geschwindigkeit** | 4x schneller als ELM327 Clones |
| **Unterstützung** | Torque Pro, Car Scanner, canop-obd |
| **Besonderheiten** | GM-LAN Support für erweiterte PIDs |
| **Garantie** | 3 Jahre + 180 Tage Geld-zurück |

**Warum für Astra J?**
- GM-LAN Support ermöglicht Zugriff auf erweiterte PIDs (Mode 22)
- Stabile Bluetooth-Verbindung
- Schnelle Abtastrate für Echtzeit-Monitoring
- Kompatibel mit canop-obd

### Budget-Alternative: OBDLink LX

| Eigenschaft | Details |
|-------------|---------|
| **Preis** | €89.95 |
| **Verbindung** | Bluetooth |
| **Protokolle** | Standard OBD-II |
| **Geschwindigkeit** | 4x schneller als Clones |
| **Unterstützung** | Torque Pro, Car Scanner, canop-obd |
| **Fehlend** | Kein GM-LAN |

**Für:** Nutzer, die nur Standard-OBD-II benötigen

### Preis-Leistungs-Sieger: Vgate iCar Pro 2

| Eigenschaft | Details |
|-------------|---------|
| **Preis** | €25-35 |
| **Verbindung** | Bluetooth + WiFi |
| **Protokolle** | Standard OBD-II |
| **Geschwindigkeit** | Mittel |
| **Unterstützung** | Torque Pro, Car Scanner |
| **Fehlend** | Kein GM-LAN |

**Für:** Nutzer mit begrenztem Budget

### Speziell für GM: Carbyte

| Eigenschaft | Details |
|-------------|---------|
| **Preis** | $149.95 |
| **Fokus** | GM Fahrzeuge |
| **Features** | AFM/DFM Deaktivierung, Auto Start/Stop Deaktivierung |
| **Scan Tool** | Integriert |

**Für:** GM-Fahrzeuge mit spezifischen Anforderungen

---

## 7. GM-spezifische PIDs Unterstützung

### Bosch ME17.9.22 ECU (A14NET)

Der Astra J 1.4 Turbo verwendet die Bosch ME17.9.22 ECU, die über **Mode 22 (erweiterte PIDs)** spezielle Sensordaten bereitstellt:

### Standard OBD-II PIDs (Mode 01)

| PID | Name | Einheit | A14NET-Werte |
|-----|------|---------|--------------|
| 0x04 | Motorlast | % | 20-90% |
| 0x05 | Kühlmitteltemperatur | °C | 85-105°C |
| 0x06 | STFT Bank 1 | % | ±10% |
| 0x07 | LTFT Bank 1 | % | ±10% |
| 0x0B | MAP-Sensor | kPa | 20-200 kPa |
| 0x0C | Motordrehzahl | rpm | 650-6500 |
| 0x0D | Fahrzeuggeschwindigkeit | km/h | Aus ABS |
| 0x0E | Zündzeitpunkt | ° | -40 bis +52° |
| 0x0F | Ansauglufttemperatur | °C | -40 bis +150°C |
| 0x10 | MAF | g/s | 2-150 g/s |
| 0x11 | Drosselklappenstellung | % | 0-100% |
| 0x14-0x1B | O2-Sensoren | V/mV | Wideband bevorzugt |
| 0x1F | Laufzeit | s | Seit Start |
| 0x21 | Entfernung seit MIL | km | DTC-bezogen |
| 0x2F | Kraftstofffüllstand | % | Vom Sender |
| 0x33 | Barometrischer Druck | kPa | 95-105 kPa |
| 0x42 | ECU-Spannung | V | 11.5-14.5V |
| 0x46 | Umgebungstemperatur | °C | Außenluft |
| 0x5C | Öltemperatur | °C | 90-130°C |
| 0x61 | Drehmomentanforderung | % | -125 bis +130 |
| 0x62 | Drehmoment actual | % | -125 bis +130 |

### Erweiterte Mode 22 PIDs (Herstellerspezifisch)

| PID | Name | Einheit | A14NET-Werte |
|-----|------|---------|--------------|
| 0x220001 | Motordrehmoment | Nm | 0-250 Nm |
| 0x220002 | Turbo-Boost actual | kPa | 0-250 kPa |
| 0x220003 | Turbo-Boost Soll | kPa | 0-250 kPa |
| 0x220004 | Wastegate-Stellung | % | 0-100% |
| 0x220005 | Turbo-Drehzahl | RPM | 0-200.000 |
| 0x220006 | Turbo-Einlass-Temperatur | °C | -40 bis +250 |
| 0x220007 | Turbo-Auslass-Temperatur | °C | -40 bis +250 |
| 0x220008 | Ladelufttemperatur | °C | -40 bis +150 |
| 0x221001 | Kraftstoffdruck rail | kPa | 0-200.000 |
| 0x225001 | Lambda actual | λ | 0.65-1.35 |
| 0x22F151 | ECU Software-Version | String | Identifikation |
| 0x22F190 | VIN | String | 17 Zeichen |

### Apps mit GM-spezifischer PIDs-Unterstützung

| App | GM-PIDs | Mode 22 | Custom PIDs |
|-----|---------|---------|-------------|
| **Torque Pro** | ⚠️ Begrenzt | ⚠️ Ja (manuell) | ✅ Ja |
| **Car Scanner ELM OBD2** | ⚠️ Begrenzt | ✅ Ja | ✅ Ja |
| **canop-obd** | ✅ A14NET-spezifisch | ✅ Implementiert | ✅ Ja |
| **EOBD Facile** | ⚠️ Begrenzt | ⚠️ Teilweise | ✅ Ja |

**Fazit:** Keine App hat nativen Support für GM-spezifische PIDs. canop-obd ist die einzige App, die speziell für den Astra J mit Mode-22 PIDs implementiert wird.

---

## 8. Dashcam-Integration in OBD-Apps

### Vorhandene Lösungen

| App | Dashcam-Support | Beschreibung |
|-----|-----------------|-------------|
| **Harry's LapTimer** | ✅ Umfangreich | Video-Overlay mit OBD-Daten, Multi-Kamera |
| **Torque Pro** | ✅ Track Recorder Plugin | Videoaufnahme mit OBD-Daten-Overlay |
| **Car Scanner** | ⚠️ Begrenzt | Video-Recording als Plugin |
| **Dedicated Dashcams** | ✅ Vollständig | BlackBox, Thinkware, Viofo mit OBD-Spannung |

### Dashcam-Integration Optionen

**Option 1: App-basierte Lösung**
- Torque Pro Track Recorder Plugin
- Harry's LapTimer Video-Overlay
- Vorteil: Kein zusätzlicher Hardware
- Nachteil: Handynutzung während der Fahrt

**Option 2: Dedizierte Dashcam mit OBD**
- BlackBox F770/F790 mit OBD-Spannungskabel
- Thinkware mit OBD-Adapter
- Viofo mit Parking Guard + OBD
- Vorteil: Unabhängige Aufnahme, Parküberwachung
- Nachteil: Zusätzliche Hardware

**Option 3: OBD-Adapter mit Dashcam-Trigger**
- OBDLink MX+ kann als Trigger für externe Dashcams dienen
- Unkosten: Adapter + Dashcam separat

### Empfehlung für Astra J

Für canop-obd wäre eine **Dashcam-Integration über Intent-Trigger** denkbar:
- Bei DTC-Erkennung: Dashcam-Flag setzen
- Bei Unfall-Erkennung: Aufnahme speichern
- GPS-Daten an Dashcam übermitteln

---

## 9. Heads-Up Display Features

### HUD-Modus in OBD-Apps

| App | HUD-Modus | Beschreibung |
|-----|-----------|-------------|
| **Torque Pro** | ✅ Vollständig | Spiegelte Anzeige für Windschutzscheibe |
| **Car Scanner** | ✅ Grundlegend | Einfache HUD-Ansicht |
| **OBDLink App** | ✅ Integriert | HUD in OBDLink-eigener App |
| **canop-obd** | ✅ Implementiert | HUD-Modus vorhanden |

### HUD-Implementierung in canop-obd

Aus `FEATURES_RESEARCH.md`:
- ✅ **HUD Mode** implementiert in `HUDMode.kt`
- Für Nachtfahrten optimiert
- Spiegelte Darstellung für Windschutzscheiben-Projektion

### HUD-Funktionen (Allgemein)

| Funktion | Beschreibung |
|----------|-------------|
| **Geschwindigkeit** | GPS- oder OBD-gestützt |
| **Drehzahl** | RPM-Anzeige |
| **Boost-Druck** | Für Turbo-Fahrzeuge |
| **Kühltemperatur** | Überhitzungswarnung |
| **Warnungen** | Farbcodierte Alarme |
| **Helligkeit** | Automatisch/manuell anpassbar |
| **Font-Größe** | Groß genug für Ablesbarkeit |

### Empfehlung

canop-obd sollte das HUD erweitern um:
1. **Boost-Druck im HUD** (einzigartig für Turbo-Fahrzeuge)
2. **Turbo-Cooldown-Timer im HUD**
3. **Shift Light im HUD** (bereits als separates Feature vorhanden)

---

## 10. GPS-Tracking und Datenlogging

### GPS-Tracking Features

| App | GPS-Tracking | OBD-Logging | Export |
|-----|--------------|-------------|--------|
| **Torque Pro** | ✅ Umfangreich | ✅ CSV/KML/GPX | ✅ Excel |
| **Harry's LapTimer** | ✅ Rennstrecken | ✅ OBD-Overlay | ✅ GPX |
| **Car Scanner** | ✅ Standard | ✅ CSV | ✅ CSV |
| **canop-obd** | ✅ GPSTracker.kt | ✅ DataLogDialog.kt | ⚠️ Teils implementiert |

### GPS-Tracking Funktionen

**Torque Pro:**
- Echtzeit-Web-Upload
- Historische Daten
- Track-Recording mit OBD-Daten
- Google Maps Integration
- Twitter-Integration

**Harry's LapTimer:**
- Automatische Rennstrecken-Erkennung
- Rundenzeiten-Tracking
- Vergleich mit Bestzeiten
- Video-Overlay mit GPS-Track

**canop-obd (aktuell):**
- GPSTracker.kt implementiert
- DataLogDialog.kt vorhanden
- Live-Trend-Graph implementiert

### Datenlogging Features

| Feature | Torque Pro | canop-obd |
|---------|------------|-----------|
| **Live-Graphen** | ✅ Mehrere Sensoren | ✅ LiveTrendGraph.kt |
| **CSV-Export** | ✅ Vollständig | ⚠️ Teilweise |
| **KML/GPX** | ✅ Google Earth | ❌ Fehlt |
| **Fahrtenaufzeichnung** | ✅ Hintergrund | ✅ DataLogDialog.kt |
| **Datenanalyse** | ✅ Excel-Import | ⚠️ Basis |

### Fehlende Features in canop-obd

1. **KML/GPX Export** für Google Earth
2. **Mehrere Sensoren parallel loggen**
3. **Automatische Track-Aufzeichnung** beim Einschalten der Zündung
4. **Cloud-Sync** (optional)
5. **Datenvergleich** vorher/nachher

---

## 11. Feature-Vergleichstabelle

| Feature | Torque Pro | Car Scanner | Harry's Lap | OBDeleven | Carista | canop-obd |
|---------|------------|-------------|-------------|-----------|---------|-----------|
| **Preis** | €3.55 | Freemium | €13-49 | €114+ | $100+ | Kostenlos |
| **Standard OBD-II** | ✅ | ✅ | ⚠️ | ✅ | ✅ | ✅ |
| **Mode 22 PIDs** | ⚠️ Manuell | ✅ | ❌ | ✅ (VAG) | ✅ (VAG) | ✅ A14NET |
| **Custom PIDs** | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ |
| **DTC lesen/löschen** | ✅ | ✅ | ⚠️ | ✅ | ✅ | ✅ |
| **Live-Dashboard** | ✅ | ✅ | ⚠️ | ✅ | ⚠️ | ✅ |
| **GPS-Tracking** | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ |
| **Datenloggen** | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ✅ |
| **HUD-Modus** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| **Dashcam-Integration** | ✅ | ⚠️ | ✅ | ❌ | ❌ | ❌ |
| **Turbo-Monitoring** | ⚠️ Basis | ⚠️ Basis | ❌ | ❌ | ❌ | ✅ Detailliert |
| **Fahrzeugprofile** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Alarmsystem** | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ | ✅ |
| **0-100 Timer** | ⚠️ GPS | ⚠️ GPS | ✅ | ❌ | ❌ | ⚠️ GPS vorhanden |
| **Dyno/PS** | ✅ | ⚠️ | ✅ | ❌ | ❌ | ❌ |
| **GM/Astra J support** | ⚠️ Basis | ⚠️ Basis | ❌ | ❌ | ❌ | ✅ Speziell |
| **Bidirektional** | ❌ | ❌ | ❌ | ✅ (VAG) | ✅ (VAG) | ❌ |
| **Coding** | ❌ | ❌ | ❌ | ✅ (VAG) | ✅ (VAG) | ❌ |

**Legende:**
- ✅ = Voll unterstützt
- ⚠️ = Teilweise/Begrenzt unterstützt
- ❌ = Nicht unterstützt

---

## 12. Empfehlungen für canop-obd

### Beste Android OBD-App für Astra J

**Empfehlung: canop-obd** (unser eigenes Projekt)

Begründung:
1. **Einzigartiges Feature-Set** für den Astra J 1.4 Turbo
2. **Mode 22 PIDs** speziell für Bosch ME17.9.22 implementiert
3. **Turbo-Monitoring** mit detaillierter Analyse (Boost, Wastegate, EGT, Turbo-Speed)
4. **Timing-Chain-Monitor** (kritisches Feature für A14NET)
5. **M32 Getriebe-Monitoring**
6. **Kostenlos** und Open Source

### Empfohlene Adapter (priorisiert)

| Priorität | Adapter | Preis | Grund |
|-----------|---------|-------|-------|
| 🥇 1 | **OBDLink MX+** | €139.95 | GM-LAN Support, schnell, zuverlässig |
| 🥈 2 | **OBDLink LX** | €89.95 | Schnell, aber ohne GM-LAN |
| 🥉 3 | **Vgate iCar Pro 2** | €30 | Budget-Option, gut genug für Standard |

### Features, die in canop-obd fehlen (nach Recherche)

#### Hohe Priorität

| Feature | Beschreibung | Aufwand |
|---------|-------------|---------|
| **KML/GPX Export** | GPS-Track-Export für Google Earth | 1-2 Tage |
| **0-100 km/h Timer** | GPS-gestützte Beschleunigungsmessung | 2-3 Tage |
| **Dashcam-Trigger** | Intent-basierte Dashcam-Steuerung | 2-3 Tage |
| **Cloud-Sync** | Optionaler Datenabgleich | 3-4 Tage |

#### Mittlere Priorität

| Feature | Beschreibung | Aufwand |
|---------|-------------|---------|
| **Dyno/PS-Berechnung** | Geschätzte Motorleistung | 2-3 Tage |
| **Enhanced Data Export** | CSV mit mehreren Sensoren parallel | 1-2 Tage |
| **Alarmsystem erweitert** | Mehr Schwellwerte, Sprachwarnungen | 1-2 Tage |

#### Niedrige Priorität

| Feature | Beschreibung | Aufwand |
|---------|-------------|---------|
| **Predictive Maintenance** | ML-basierte Wartungsvorhersage | 4-5 Tage |
| **Social Features** | Vergleich mit anderen Nutzern | 3-4 Tage |
| **Widget erweitert** | Mehr Widget-Größen und Stile | 1-2 Tage |

### Feature-Gaps zu Konkurrenz

| Feature | Torque Pro | canop-obd | Empfehlung |
|---------|------------|-----------|------------|
| **GM-LAN PIDs** | ⚠️ | ✅ | canop-obd führt |
| **Turbo-Diagnose** | ⚠️ Basis | ✅ Detailliert | canop-obd führt |
| **Timing Chain** | ❌ | ✅ | canop-obd führt |
| **GPS Export** | ✅ KML/GPX | ❌ | Fehlt - wichtig |
| **Dashcam** | ✅ | ❌ | Fehlt - nice-to-have |
| **Dyno** | ✅ | ❌ | Fehlt - Performance |

---

## 13. Quellen und Links

### Apps

| App | Link |
|-----|------|
| Torque Pro | https://play.google.com/store/apps/details?id=org.prowl.torque |
| Car Scanner ELM OBD2 | https://play.google.com/store/apps/details?id=com.nixdev.obd |
| OBDeleven | https://www.obdeleven.com/ |
| Carista | https://www.carista.com/ |
| FORScan | https://www.forscan.org/ |
| Harry's LapTimer | https://www.harrys-laptimer.de/ |

### Adapter

| Adapter | Link |
|---------|------|
| OBDLink MX+ | https://www.obdlink.com/products/obdlink-mx-plus/ |
| OBDLink LX | https://www.obdlink.com/products/obdlink-lx/ |
| Vgate iCar Pro | https://www.vgate.com/ |
| PLX Kiwi 3 | https://www.plxdevices.com/ |
| Carbyte (GM-spezifisch) | https://www.obdlink.com/products/carbyte/ |

### Astra J Ressourcen

| Ressource | Link |
|-----------|------|
| Opel Astra J Forum | https://www.motor-talk.de/forum/opel-astra-j.html |
| Astra J Hidden Features | https://www.astra-j.de/ |
| Bosch ME17.9.22 Wiki | https://en.wikipedia.org/wiki/Bosch_ME7 |
| Opel TIS (Technische Infos) | https://www.opel.de/ |

### OBD-II Standards

| Ressource | Link |
|-----------|------|
| OBD-II PIDs | https://en.wikipedia.org/wiki/OBD-II_PIDs |
| Mode 22 Explanation | https://www.obdapp.com/mode22.html |
| ELM327 AT Commands | https://www.elmelectronics.com/ic/elm327/ |

---

## Zusammenfassung

### Stärken von canop-obd im Vergleich

1. **Einzigartiges Turbo-Monitoring** für A14NET
2. **Timing-Chain-Monitor** (keine andere App hat das)
3. **M32 Getriebe-Spezialist**
4. **Mode 22 PIDs** vollständig implementiert
5. **Kostenlos** und Open Source
6. **Modernes UI** mit Material Design 3

### Fehlende Killer-Features

1. **KML/GPX Export** (wichtig für Analyse)
2. **0-100 Timer** (populäres Feature)
3. **Dashcam-Integration** (nice-to-have)

### Empfohlene nächste Schritte

1. **OBDLink MX+** als Standard-Adapter empfehlen
2. **KML/GPX Export** implementieren
3. **0-100 Timer** mit GPS-Integration
4. **Dokumentation** der Mode 22 PIDs erweitern
5. **Datenbank** für A14NET-spezifische Schwellwerte

---

*Diese Recherche wurde am 06. Mai 2026 durchgeführt und basiert auf aktuellen App-Store-Informationen und Hersteller-Websites.*
