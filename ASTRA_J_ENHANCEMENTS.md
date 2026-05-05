# Opel Astra J 1.4 Turbo (A14NET) - Dokumentation & Technische Referenz

## Dokumentationsübersicht

| Dokument | Beschreibung |
|----------|-------------|
| [ASTRA_J_CALIBRATION.md](./ASTRA_J_CALIBRATION.md) | Vollständige Kalibrierungswerte, technische Daten, Wartungsintervalle, Live-Data-Referenzen |
| [ASTRA_J_DTC_CODES.md](./ASTRA_J_DTC_CODES.md) | 500+ DTC-Fehlercodes mit deutschen Beschreibungen, Schweregraden und A14NET-Hinweisen |
| [ASTRA_J_LIVE_DATA_REFERENCE.md](./ASTRA_J_LIVE_DATA_REFERENCE.md) | Live-Data-Referenzwerte für Leerlauf, Vollast und Autobahn |
| [ASTRA_J_TROUBLESHOOTING_GUIDE.md](./ASTRA_J_TROUBLESHOOTING_GUIDE.md) | Problemlösung, DTC-Interpretation, DIY-Diagnose |
| [KNOWN_ISSUES.md](./KNOWN_ISSUES.md) | Bekannte Probleme, Kosten, Prävention, Zeitlinien |

---

## Motor-Grunddaten

| Parameter | Wert |
|-----------|------|
| **Motor** | GM Family 0 Gen III, 1364cc R4 Turbo |
| **Leistung** | 103 kW (140 PS) @ 4900–6000 rpm |
| **Drehmoment** | 200 Nm @ 1850–4900 rpm |
| **Overboost** | 220 Nm (max. 10 Sek.) |
| **Turbo** | BorgWarner KP39, Single-Scroll, Fixed-Geometry |
| **Ladedruck** | 0,7 bar normal, 1,3 bar Overboost |
| **ECU** | Bosch ME17.9.22 / Delco E78 |
| **Getriebe** | 6-Gang Schaltgetriebe (Getrag M32) |

---

## Wartungsintervalle

| Bauteil | Standard | Schwerlast | Autobahn |
|---------|----------|------------|----------|
| **Ölwechsel (Dexos2 5W-30)** | 15.000 km / 12 Mon. | 10.000 km / 8 Mon. | 18.000 km / 14 Mon. |
| **Luftfilter** | 30.000 km / 24 Mon. | 15.000 km / 12 Mon. | 40.000 km / 36 Mon. |
| **Zündkerzen** | 60.000 km / 48 Mon. | 30.000 km / 24 Mon. | 70.000 km / 60 Mon. |
| **Kühlmittel** | 80.000 km / 60 Mon. | 60.000 km / 48 Mon. | 80.000 km / 72 Mon. |
| **Turbo-Inspektion** | 60.000 km / 48 Mon. | 45.000 km / 36 Mon. | 80.000 km / 72 Mon. |
| **Timing-Kette Prüfung** | 75.000 km / 60 Mon. | 60.000 km / 48 Mon. | 100.000 km / 84 Mon. |

---

## Typische Probleme nach Kilometerstand

| Km-Bereich | Problem | DTCs | Schweregrad |
|------------|---------|------|-------------|
| **30.000–60.000** | Zündkerzen-Verschleiß | P0300–P0304 | 🟡 |
| **60.000–80.000** | MAF-Sensor | P0100–P0103 | 🟡 |
| **60.000–100.000** | PCV-Ventil | P1100/P1101 | 🟡 |
| **80.000–150.000** | Kettenspanner | P0340, P0341, P1345 | 🔴 |
| **80.000–150.000** | Kühlmittel-Sensor | P0116, P0117 | 🟡 |
| **80.000–150.000** | Wastegate-Stellglied | P0234, P0299 | 🔴 |
| **100.000+** | Turbo-Inspektion | P1241, P1253 | 🟠 |
| **120.000–150.000** | Timing-Kette | P0016–P0019 | 🔴 |

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

## Mode 22 PIDs (Bosch ME17)

| PID | Beschreibung |
|-----|-------------|
| 22F190 | VIN auslesen |
| 22F151 | ECU Software Version |
| 22F156 | ECU Hardware Version |
| 220001 | Motordrehmoment (Nm) |
| 220002 | Turbo Boost Ist (kPa) |
| 220003 | Turbo Boost Soll (kPa) |
| 220004 | Wastegate Duty-Cycle (%) |
| 220005 | Turbo-Drehzahl (RPM) |
| 220006–220008 | Turbo-/Ladelufttemperatur |
| 221001 | Kraftstoffrail-Druck |
| 225001 | Lambda Wideband (λ) |

---

## Schweregrade

| Symbol | Grad | Beschreibung |
|--------|------|-------------|
| 🔴 | KRITISCH | Sofortige Handlung - Motorschaden möglich |
| 🟠 | LEISTUNG | Limp Mode aktiv - eingeschränkte Leistung |
| 🟡 | WARNUNG | Baldige Reparatur erforderlich |
| 🔵 | INFO | Information - kann vernachlässigt werden |
