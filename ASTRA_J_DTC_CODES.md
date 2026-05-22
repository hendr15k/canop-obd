# Opel Astra J 1.4 Turbo (A14NET) - DTC-Fehlercodes

> Vollständige Datenbank der Diagnose-Fehlercodes (DTC) mit deutschen Beschreibungen, Schweregraden und A14NET-spezifischen Hinweisen.

---

## Übersicht

| Kategorie | Codes | Beschreibung |
|-----------|-------|-------------|
| P01xx | Kraftstoff & Luft | Motormanagement - Gemischbildung |
| P02xx | Einspritzung | Kraftstoffeinspritzung & Zündung |
| P03xx | Zündung & Sensorik | Nockenwellen-, Kurbelwellensensor, Zündung |
| P04xx | Abgas | EGR, Katalysator, EVAP |
| P05xx | Geschwindigkeit & Leerlauf | Drehzahl, Geschwindigkeit, Leerlaufregelung |
| P06xx | Steuergerät | ECU-Fehler, Kommunikation |
| P07xx | Getriebe | Automatikgetriebe-Regelung |
| P1xxx | Herstellerspezifisch | GM/Opel-spezifische Codes |
| C0xxx | Fahrwerk | ABS, ESP, Raddrehzahlen |
| B0xxx | Karosserie | Airbag, Beleuchtung |
| U0xxx | Kommunikation | CAN-Bus, Modul-Kommunikation |

---

## Schweregrade

| Symbol | Schweregrad | Beschreibung |
|--------|------------|-------------|
| 🔴 | KRITISCH | Sofortige Handlung erforderlich. Weiterfahren kann Motorschaden verursachen. |
| 🟡 | WARNUNG | Baldige Reparatur erforderlich. Fahrzeug ist noch fahrbar. |
| 🔵 | INFO | Information. Kann vernachlässigt werden oder ist vorübergehend. |
| 🟠 | LEISTUNG | Leistungsminderung. Fahrzeug ist im Notlaufmodus (Limp Mode). |

---

## P01xx - Kraftstoff & Luft (Gemischbildung)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0100 | Luftmassenmesser (MAF) - Schaltfehler | 🔴 KRITISCH | Häufig bei A14NET. MAF-Sensor prüfen/reinigen. |
| P0101 | MAF - Bereich/Leistung | 🟡 WARNUNG | MAF-Sensor außerhalb des Normbereichs. |
| P0102 | MAF - Eingang zu niedrig | 🔴 KRITISCH | MAF-Verkabelung oder Sensor defekt. |
| P0103 | MAF - Eingang zu hoch | 🔴 KRITISCH | MAF-Sensor defekt oder Luftleck. |
| P0110 | Ansauglufttemperatur-Sensor (IAT) - Schaltfehler | 🟡 WARNUNG | IAT-Sensor prüfen. |
| P0111 | IAT - Bereich/Leistung | 🔵 INFO | Sensoraußenwerte prüfen. |
| P0112 | IAT - Eingang zu niedrig | 🟡 WARNUNG | Kurzschluss oder Sensor defekt. |
| P0113 | IAT - Eingang zu hoch | 🟡 WARNUNG | Unterbrechung oder Sensor defekt. |
| P0115 | Kühlmitteltemperatur-Sensor (ECT) - Schaltfehler | 🟡 WARNUNG | ECT-Sensor oder Verkabelung prüfen. |
| P0116 | ECT - Bereich/Leistung | 🟡 WARNUNG | Typisch 80.000–150.000 km beim A14NET. Sensor ersetzen. |
| P0117 | ECT - Eingang zu niedrig | 🟡 WARNung | Sensor defekt oder Kurzschluss. |
| P0118 | ECT - Eingang zu hoch | 🟡 WARNUNG | Sensor defekt oder Unterbrechung. |
| P0120 | Drosselklappenposition (TPS) - Schaltfehler | 🟡 WARNUNG | TPS-Sensor prüfen. |
| P0121 | TPS - Bereich/Leistung | 🟡 WARNUNG | TPS-Anpassung durchführen. |
| P0122 | TPS - Eingang zu niedrig | 🟡 WARNUNG | Verkabelung oder Sensor prüfen. |
| P0123 | TPS - Eingang zu hoch | 🟡 WARNUNG | Verkabelung oder Sensor prüfen. |

## P013x - Lambdaregelung (O2-Sensoren)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0130 | Lambdasonde Bank 1 Sensor 1 - Schaltfehler | 🟡 WARNUNG | Lambda-Sensor prüfen. |
| P0131 | Lambda B1S1 - Spannung zu niedrig | 🟡 WARNUNG | Mager-Gemisch oder Sensor defekt. |
| P0132 | Lambda B1S1 - Spannung zu hoch | 🟡 WARNUNG | Fett-Gemisch oder Sensor defekt. |
| P0133 | Lambda B1S1 - Langsame Antwort | 🟡 WARNUNG | Lambda-Sensor verschlissen. |
| P0134 | Lambda B1S1 - Keine Aktivität | 🟡 WARNUNG | Sensor defekt, Heizung prüfen. |
| P0135 | Lambda B1S1 - Heizung - Schaltfehler | 🟡 WARNUNG | Lambda-Heizung defekt. |
| P0136 | Lambdasonde Bank 1 Sensor 2 - Schaltfehler | 🔵 INFO | Nachlambda-Sensor prüfen. |
| P0137 | Lambda B1S2 - Spannung zu niedrig | 🔵 INFO | Nachlambda defekt oder Auspundicht. |
| P0138 | Lambda B1S2 - Spannung zu hoch | 🔵 INFO | Nachlambda-Sensor prüfen. |

## P017x - Kraftstoffsystem

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0170 | Kraftstoffsystem zu fett (Bank 1) | 🟡 WARNUNG | Kraftstoffdruck, Injektoren oder Lambda prüfen. |
| P0171 | Kraftstoffsystem zu mager (Bank 1) | 🟡 WARNUNG | Häufig: Luftleck, MAF defekt, Kraftstoffdruck niedrig. |
| P0172 | Kraftstoffsystem zu fett (Bank 1) | 🟡 WARNUNG | Kraftstoffdruck hoch, Injektor undicht. |
| P0173 | Kraftstoffsystem zu fett (Bank 2) | 🟡 WARNung | Bank 2 gibt es beim A14NET nicht (R4). |
| P0174 | Kraftstoffsystem zu mager (Bank 2) | 🟡 WARNung | Bank 2 gibt es beim A14NET nicht (R4). |
| P0175 | Kraftstoffsystem zu fett (Bank 2) | 🟡 WARNung | Bank 2 gibt es beim A14NET nicht (R4). |

---

## P02xx - Einspritzung & Turbo

### Kraftstoffeinspritzung

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0200 | Einspritzventil-Schaltfehler | 🔴 KRITISCH | Injektor prüfen. |
| P0201 | Zylinder 1 - Einspritzventil-Schaltfehler | 🔴 KRITISCH | Injektor Zyl. 1 prüfen. |
| P0202 | Zylinder 2 - Einspritzventil-Schaltfehler | 🔴 KRITISCH | Injektor Zyl. 2 prüfen. |
| P0203 | Zylinder 3 - Einspritzventil-Schaltfehler | 🔴 KRITISCH | Injektor Zyl. 3 prüfen. |
| P0204 | Zylinder 4 - Einspritzventil-Schaltfehler | 🔴 KRITISCH | Injektor Zyl. 4 prüfen. |

### Turbo / Ladedruck

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0234 | Turbo Überladung (Overboost) | 🔴 KRITISCH | A14NET-typisch. Wastegate-Stellglied prüfen. |
| P0235 | Turbo-Ladedrucksensor - Schaltfehler | 🟡 WARNUNG | Boost-Sensor oder Verkabelung. |
| P0236 | Turbo Boost-Sensor - Bereich/Leistung | 🟡 WARNUNG | Sensor-Kalibrierung prüfen. |
| P0237 | Turbo Boost-Sensor - Eingang zu niedrig | 🟡 WARNUNG | Sensor oder Verkabelung defekt. |
| P0238 | Turbo Boost-Sensor - Eingang zu hoch | 🟡 WARNung | Sensor defekt oder Kurzschluss. |
| P0243 | Turbo Wastegate-Aktuator A - Schaltfehler | 🟡 WARNUNG | Wastegate-Stellglied prüfen. |
| P0245 | Turbo Wastegate-Aktuator A - Eingang zu niedrig | 🟡 WARNUNG | Unterdruckleck oder Aktuator. |
| P0246 | Turbo Wastegate-Aktuator A - Eingang zu hoch | 🟡 WARNUNG | Aktuator defekt. |
| P0298 | Motoröltemperatur zu niedrig | 🔵 INFO | Erst nach Kaltstart normal. |
| P0299 | Turbo Unterladung (Underboost) | 🟠 LEISTUNG | A14NET-typisch. Wastegate, Boost-Leitung oder Turbo prüfen. |

### Kraftstoffdruck

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0087 | Kraftstoffrail-Spannung zu niedrig | 🔴 KRITISCH | Kraftstoffpumpe oder Rail-Drucksensor. |
| P0088 | Kraftstoffrail-Spannung zu hoch | 🟡 WARNung | Kraftstoffdruckregler. |
| P0089 | Kraftstoffdruckregler - Leistung | 🟡 WARNUNG | Druckregler prüfen. |
| P0090 | Kraftstoffdruckregler - Steuerschaltkreis | 🟡 WARNUNG | Verkabelung prüfen. |
| P0091 | Kraftstoffdruckregler - Schaltkreis zu niedrig | 🟡 WARNUNG | Kurzschluss nach Masse. |
| P0092 | Kraftstoffdruckregler - Schaltkreis zu hoch | 🟡 WARNUNG | Kurzschluss nach Plus. |
| P0093 | Kraftstoffsystem - Große Undichtigkeit | 🔴 KRITISCH | Sofort Motor aus! Kraftstoffleck. |
| P0094 | Kraftstoffsystem - Kleine Undichtigkeit | 🟡 WARNUNG | Kraftstoffsystem auf Undichtigkeit prüfen. |

---

## P03xx - Zündung, Nockenwellen & Kurbelwellen

### Zündaussetzer

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0300 | Zufällige/mehrere Zylinder - Zündaussetzer | 🔴 KRITISCH | Zündkerzen, Zündspulen, Kompression prüfen. |
| P0301 | Zylinder 1 - Zündaussetzer | 🔴 KRITISCH | Zündkerze/Zündspule Zyl. 1. |
| P0302 | Zylinder 2 - Zündaussetzer | 🔴 KRITISCH | Zündkerze/Zündspule Zyl. 2. |
| P0303 | Zylinder 3 - Zündaussetzer | 🔴 KRITISCH | Zündkerze/Zündspule Zyl. 3. |
| P0304 | Zylinder 4 - Zündaussetzer | 🔴 KRITISCH | Zündkerze/Zündspule Zyl. 4. |

### Klopfsensor

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0325 | Klopf-Sensor 1 - Schaltfehler | 🟡 WARNUNG | Klopf-Sensor oder Verkabelung. |
| P0326 | Klopf-Sensor 1 - Bereich/Leistung | 🟡 WARNUNG | Sensor-Montage prüfen. |
| P0330 | Klopf-Sensor 2 - Schaltfehler | 🔵 INFO | Beim A14NET: nur ein Klopf-Sensor. |

### Nockenwellen & Kurbelwellenposition

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0335 | Kurbelwellenposition - Schaltfehler | 🔴 KRITISCH | CKP-Sensor prüfen. Motor startet evtl. nicht. |
| P0336 | Kurbelwellenposition - Bereich/Leistung | 🟡 WARNUNG | Sensor-Intervall prüfen. |
| P0340 | Nockenwellenposition - Schaltfehler (Bank 1) | 🔴 KRITISCH | **A14NET Hauptproblem!** Kettenspanner/Kette prüfen. |
| P0341 | Nockenwellenposition - Bereich/Leistung | 🔴 KRITISCH | **A14NET Hauptproblem!** Kettenspanner prüfen. |

---

## P04xx - Abgas & EGR

### EGR-System

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0400 | EGR-Ventil - Durchflussfehler | 🟡 WARNUNG | EGR-Ventil oder Ablagerungen. |
| P0401 | EGR - Unzureichender Durchfluss | 🟡 WARNUNG | EGR-Ventil verstopft oder klemmt. |
| P0402 | EGR - Übermäßiger Durchfluss | 🟡 WARNUNG | EGR-Ventil undicht. |
| P0403 | EGR - Steuerschaltkreis | 🟡 WARNUNG | Verkabelung prüfen. |
| P0404 | EGR - Bereich/Leistung | 🟡 WARNung | EGR-Positionssensor prüfen. |
| P0405 | EGR-Sensor A - Eingang zu niedrig | 🔵 INFO | Sensor-Spannung prüfen. |
| P0406 | EGR-Sensor A - Eingang zu hoch | 🔵 INFO | Sensor-Spannung prüfen. |

### Katalysator

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0420 | Kat-Effizienz unter Schwellwert (Bank 1) | 🟡 WARNUNG | Katalysator Verschleiß. Lambdasonde prüfen. |
| P0421 | Kat-Aufwärmphase ineffektiv (Bank 1) | 🟡 WARNung | Kat-Temperatur prüfen. |
| P0422 | Kat-Hauptsystemeffizienz niedrig (Bank 1) | 🟡 WARNung | Katalysator ersetzen. |
| P0430 | Kat-Effizienz unter Schwellwert (Bank 2) | 🔵 INFO | Beim A14NET: nur Bank 1 vorhanden. |

### EVAP (Verdunstungsemission)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0440 | EVAP-System - Schaltfehler | 🟡 WARNUNG | Tankentlüftungssystem prüfen. |
| P0441 | EVAP - Spülvolumenfehler | 🟡 WARNUNG | Spü Ventil oder Leitungen. |
| P0442 | EVAP - Kleine Undichtigkeit | 🔵 INFO | Tankdeckel prüfen! |
| P0443 | EVAP-Spü Ventil - Schaltkreis | 🟡 WARNung | Ventil oder Verkabelung. |
| P0444 | EVAP-Spü Ventil - Eingang zu niedrig | 🟡 WARNUNG | Kurzschluss. |
| P0445 | EVAP-Spü Ventil - Eingang zu hoch | 🟡 WARNUNG | Kurzschluss. |
| P0446 | EVAP-Entlüftungsventil - Fehler | 🟡 WARNung | Ventil oder Leitung. |
| P0450 | EVAP-Drucksensor - Fehler | 🔵 INFO | Druckdaten prüfen. |
| P0452 | EVAP-Drucksensor - Eingang zu niedrig | 🔵 INFO | Sensor prüfen. |
| P0453 | EVAP-Drucksensor - Eingang zu hoch | 🔵 INFO | Sensor prüfen. |
| P0455 | EVAP - Große Undichtigkeit | 🟡 WARNUNG | Tankdeckel! Große Undichtigkeit. |
| P0456 | EVAP - Sehr kleine Undichtigkeit | 🔵 INFO | Tankdeckel nachziehen. |

---

## P05xx - Geschwindigkeit, Leerlauf & Geschwindigkeit

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0500 | Fahrzeuggeschwindigkeit - Schaltfehler | 🟡 WARNUNG | VSS-Sensor oder ABS-Steuergerät. |
| P0501 | Fahrzeuggeschwindigkeit - Bereich/Leistung | 🔵 INFO | Geschwindigkeitsdaten prüfen. |
| P0502 | Fahrzeuggeschwindigkeit - Eingang zu niedrig | 🟡 WARNUNG | Sensor oder Verkabelung. |
| P0503 | Fahrzeuggeschwindigkeit - Intermitierend | 🔵 INFO | Verbindungsprobleme prüfen. |
| P0505 | Leerlaufregelung - Schaltfehler | 🟡 WARNUNG | Leerlaufregelventil prüfen. |
| P0506 | Leerlaufregelung - RPM zu niedrig | 🟡 WARNUNG | Leerlaufdrehzahl einstellen. |
| P0507 | Leerlaufregelung - RPM zu hoch | 🟡 WARNung | Leerlaufdrehzahl einstellen. |
| P0508 | Leerlaufregelung - RPM viel zu niedrig | 🟠 LEISTUNG | Leerlaufventil klemmt. |
| P0509 | Leerlaufregelung - RPM viel zu hoch | 🟡 WARNung | Leerlaufventil klemmt. |

---

## P06xx - Steuergerät & Kommunikation

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0562 | Systemspannung zu niedrig | 🟡 WARNUNG | Batterie oder Lichtmaschine. |
| P0563 | Systemspannung zu hoch | 🟡 WARNung | Lichtmaschine überlädt. |
| P0600 | Serielle Kommunikation - Schaltfehler | 🟠 LEISTUNG | CAN-Bus prüfen. |
| P0601 | Steuergerät ROM - Fehler | 🔴 KRITISCH | ECU defekt oder fehlerhaft programmiert. |
| P0602 | Steuergerät Programmierfehler | 🔴 KRITISCH | ECU muss neu programmiert werden. |
| P0603 | Steuergerät KAM - Fehler | 🟡 WARNung | Steuergerät-Rückstsetzung durchführen. |
| P0604 | Steuergerät RAM - Fehler | 🔴 KRITISCH | ECU defekt. |
| P0605 | Steuergerät ROM - Fehler | 🔴 KRITISCH | ECU defekt. |
| P0606 | PCM/ECU Prozessorfehler | 🔴 KRITISCH | ECU defekt. |
| P0685 | ECU-Relais - Steuerschaltkreis | 🔴 KRITISCH | ECU-Relais oder Verkabelung. |

---

## P07xx - Getriebe

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P0700 | Getriebesteuerung - Fehler | 🟠 LEISTUNG | Getriebe-ECU speichert eigene Codes. |
| P0703 | Wandlerüberbrückungskupplung - Schaltfehler | 🟡 WARNUNG | Automatikgetriebe (nicht M32 Schaltgetriebe). |
| P0705 | Getriebewahlschalter - Schaltfehler | 🟡 WARNung | Schaltgestänge prüfen. |
| P0707 | Getriebewahlschalter - Eingang zu niedrig | 🔵 INFO | Sensor oder Verkabelung. |
| P0708 | Getriebewahlschalter - Eingang zu hoch | 🔵 INFO | Sensor oder Verkabelung. |
| P0710 | Getriebeöltemperatur - Schaltfehler | 🔵 INFO | Getriebeölsensor. |
| P0715 | Eingangsdrehzahl (Turbine) - Schaltfehler | 🟡 WARNung | Getriebe-Sensor. |
| P0720 | Ausgangsdrehzahl - Schaltfehler | 🟡 WARNung | Getriebe-Sensor. |
| P0725 | Motordrehzahl - Eingangsschaltkreis | 🟡 WARNUNG | ECU-Daten prüfen. |
| P0730 | Falsches Getriebeübersetzungsverhältnis | 🟠 LEISTUNG | Getriebe mechanisch prüfen. |
| P0731–P0735 | Gang 1–5 - Falsches Übersetzungsverhältnis | 🟠 LEISTUNG | Getriebe-Synchronring oder Lager. |

---

## P1xxx - Herstellerspezifisch (GM/Opel)

### MAF-Sensor (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1100 | MAF-Sensor Intermitierend / instabil | 🟡 WARNUNG | **A14NET-typisch 60.000–120.000 km.** MAF-Sensor reinigen oder ersetzen. |
| P1101 | MAF-Sensor Außerhalb des Selbsttest-Bereichs | 🟡 WARNUNG | Luftfilter und Verbindungen prüfen. |
| P1105 | MAP-Sensor Referenzschaltkreis | 🟡 WARNUNG | Referenzspannung prüfen. |
| P1106 | MAP-Sensor - Bereich/Leistung | 🟡 WARNung | MAP-Sensor prüfen. |
| P1107 | MAP-Sensor - Eingang zu niedrig | 🟡 WARNung | Sensor oder Vakuumleitung. |
| P1108 | MAP-Sensor - Eingang zu hoch | 🟡 WARNUNG | Sensor defekt. |

### Ansauglufttemperatur (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1110 | IAT-Sensor - Schaltfehler | 🔵 INFO | Verkabelung prüfen. |
| P1111 | IAT-Sensor - Bereich/Leistung | 🔵 INFO | Sensor-Außenwerte prüfen. |
| P1112 | IAT-Sensor - Eingang zu niedrig | 🔵 INFO | Kurzschluss. |
| P1113 | IAT-Sensor - Eingang zu hoch | 🔵 INFO | Unterbrechung. |

### Gaspedalsensor (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1120 | Gaspedalsensor 1 - Schaltfehler | 🔴 KRITISCH | Fahrzeug in Notlauf! APP-Sensor prüfen. |
| P1121 | Gaspedalsensor 1 - Bereich/Leistung | 🟠 LEISTUNG | Sensor-Anpassung prüfen. |
| P1122 | Gaspedalsensor 1 - Eingang zu niedrig | 🔴 KRITISCH | Sensor oder Verkabelung. |
| P1123 | Gaspedalsensor 1 - Eingang zu hoch | 🔴 KRITISCH | Sensor oder Verkabelung. |
| P1125 | Gaspedalsensor 2 - Schaltfehler | 🔴 KRITISCH | Zweiter Sensor-Kanal prüfen. |
| P1126 | Gaspedalsensor 2 - Bereich/Leistung | 🟠 LEISTung | Sensor-Anpassung. |
| P1127 | Gaspedalsensor 2 - Eingang zu niedrig | 🔴 KRITISCH | Sensor oder Verkabelung. |
| P1128 | Gaspedalsensor 2 - Eingang zu hoch | 🔴 KRITISCH | Sensor oder Verkabelung. |

### Lambdaregelung (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1130 | Lambda B1S1 - Anpassungsgrenze (mager) | 🟡 WARNung | Kraftstoffsystem prüfen. |
| P1131 | Lambda B1S1 - Zu wenig Schaltvorgänge (mager) | 🟡 WARNung | Lambda-Sensor defekt oder Luftleck. |
| P1132 | Lambda B1S1 - Zu wenig Schaltvorgänge (fett) | 🟡 WARNung | Kraftstoffdruck hoch, Injektor. |
| P1133 | Lambda B1S1 - Unzureichende Schaltvorgänge | 🟡 WARNUNG | Lambda-Sensor Verschleiß. |
| P1134 | Lambda B1S1 - Übergangszeit | 🟡 WARNung | Sensor langsamer werdend. |
| P1135 | Lambda B1S1 - Heizung (Bank 1) | 🟡 WARNUNG | Lambda-Heizung defekt. |
| P1136 | Lambda B1S2 - Heizung (Bank 1) | 🔵 INFO | Nachlambda-Heizung. |
| P1171 | Kraftstofftrim mager bei Vollast | 🟡 WARNUNG | Kraftstoffversorgung prüfen. |
| P1172 | Kraftstofftrim fett bei Vollast | 🟡 WARNUNG | Kraftstoffdruck oder Injektor. |

### Öltemperatur (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1187 | Motölsensor - Eingang zu niedrig | 🔵 INFO | Sensor oder Verkabelung. |
| P1188 | Motölsensor - Eingang zu hoch | 🔵 INFO | Sensor defekt. |
| P1189 | Motölsensor - Bereich/Leistung | 🔵 INFO | Sensor prüfen. |

### Turbo (Herstellerspezifisch)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1240 | Turbo-Boost-Sensor - Leistung | 🟡 WARNUNG | Boost-Sensor prüfen. |
| P1241 | Turbo-Boost-Druck zu niedrig | 🟠 LEISTUNG | Wastegate, Boost-Schlauch, Turbo. |
| P1242 | Turbo-Boost-Druck zu hoch (Overboost) | 🔴 KRITISCH | **Sofort Gas weg!** Wastegate klemmt. |
| P1243 | Turbo-Drehzahl-Sensor - Schaltkreis | 🟡 WARNUNG | Turbo-RPM-Sensor prüfen. |
| P1244 | Turbo-Drehzahl-Sensor - Eingang zu niedrig | 🟡 WARNUNG | Sensor oder Verkabelung. |
| P1245 | Turbo-Drehzahl-Sensor - Eingang zu hoch | 🟡 WARNUNG | Sensor defekt. |
| P1246 | Turbo-Drehzahl-Sensor - Bereich/Leistung | 🟡 WARNUNG | Sensor-Bereich prüfen. |
| P1247 | Turbo-Boost-Regelung - Leistung | 🟡 WARNUNG | Wastegate-Aktuator prüfen. |
| P1248 | Turbo-Boost-Regelung - Nicht erkannt | 🟡 WARNung | Regelung funktioniert nicht. |
| P1249 | Turbo-Boost-Aktuator A - Schaltkreis | 🟡 WARNUNG | Aktuator-Verkabelung. |
| P1250 | Turbo-Boost-Aktuator A - Eingang zu niedrig | 🟡 WARNUNG | Unterdruck-Aktuator. |
| P1251 | Turbo-Boost-Aktuator A - Eingang zu hoch | 🟡 WARNUNG | Aktuator defekt. |
| P1252 | Turbo-Boost-Aktuator A - Bereich/Leistung | 🟡 WARNUNG | Aktuator-Bereich prüfen. |
| P1253 | Turbo-Boost-Aktuator A - Festgefahren | 🔴 KRITISCH | **A14NET-typisch.** Wastegate-Stellglied ersetzen. |
| P1254 | Turbo-Boost-Aktuator A - Fest offen | 🔴 KRITISCH | Wastegate-Stellglied defekt. |
| P1255 | Turbo-Boost-Aktuator A - Fest geschlossen | 🔴 KRITISCH | **Gefahr!** Überladung möglich. |

### Drosselklappe (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1271 | Drosselklappenposition - Anpassung | 🟡 WARNung | Drosselklappen-Adaptation durchführen. |
| P1272 | Drosselklappen-Anpassung nicht erlernt | 🟡 WARNung | Adaptationsfahrt durchführen. |
| P1273 | Drosselklappenposition - Anpassungsbereich | 🟡 WARNUNG | Drosselklappe prüfen. |
| P1274 | Drosselklappenposition 1-2 - Korrelation | 🟡 WARNung | Doppelter TPS prüfen. |
| P1275 | Drosselklappensteuerung (elektronisch) | 🟠 LEISTUNG | Drosselklappenmodul defekt. |

### Nockenwellen-Korrelation (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1345 | Nocken-Kurbelwellen-Korrelation (Bank 1) | 🔴 KRITISCH | **A14NET Hauptproblem!** Timing-Kette/Kettenspanner! |
| P1346 | Nockenwellenposition - Bereich/Leistung | 🔴 KRITISCH | Kette oder VVT-System prüfen. |
| P1347 | Nocken-Kurbelwellen-Korrelation (Bank 1) | 🔴 KRITISCH | Timing-Kette Verschleiß. |

### Zündspulen (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1351 | Zündspulen-Steuerkreis (Zyl. 1) | 🔴 KRITISCH | Zündspule oder Verkabelung. |
| P1352 | Zündspulen-Steuerkreis (Zyl. 2) | 🔴 KRITISCH | Zündspule oder Verkabelung. |
| P1353 | Zündspulen-Steuerkreis (Zyl. 3) | 🔴 KRITISCH | Zündspule oder Verkabelung. |
| P1354 | Zündspulen-Steuerkreis (Zyl. 4) | 🔴 KRITISCH | Zündspule oder Verkabelung. |
| P1355 | Zündspulen Primärkreis (alle Zylinder) | 🔴 KRITISCH | ECU-Zündausgang oder Verkabelung. |

### Kühlsystem (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1299 | Zylinderkopf-Übertemperatur | 🔴 KRITISCH | **Motor sofort aus!** Kühlmittelstand und -temperatur prüfen. |
| P1489 | Hochgeschwindigkeitsventilator - Schaltfehler | 🟡 WARNUNG | Ventilatorrelais oder Motor. |
| P1490 | Niedriggeschwindigkeitsventilator - Schaltfehler | 🟡 WARNUNG | Ventilatorstufe 1. |
| P1491 | Lüfteranlage - Leistung | 🟡 WARNung | Kühlsystem prüfen. |

### Drosselklappenventil (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1516 | Ansaugkrümmer-Verstellventil - Leistung | 🟡 WARNUNG | IMTV-Ventil prüfen. |
| P1517 | Ansaugkrümmer-Verstellventil - Schaltkreis | 🟡 WARNung | Ventil oder Verkabelung. |
| P1518 | Ansaugkrümmer-Verstellventil - Fest offen | 🟡 WARNung | Ventil klemmt. |
| P1519 | Ansaugkrümmer-Verstellventil - Fest geschlossen | 🟡 WARNung | Ventil klemmt. |
| P1520 | Ansaugkrümmer-Verstellventil - Schaltfehler | 🟡 WARNung | Steuerkreis prüfen. |

### Turbo-Boost (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1549 | Turbo-Boost-Regelventil - Schaltfehler | 🟡 WARNung | Wastegate-Ventil prüfen. |

### Bremsen (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1571 | Bremspedalschalter - Signalfehler | 🟡 WARNung | Bremslichtschalter prüfen. |
| P1572 | Bremsvakuumdrucksensor - Schaltkreis | 🟡 WARNung | Vakuumsensor oder Leitung. |
| P1573 | Motordrehmoment-Signal - Schaltfehler | 🟡 WARNung | ECU-Kommunikation prüfen. |

### Wastegate (Hersteller)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| P1658 | Wastegate-Ventil B - Schaltkreis | 🟡 WARNung | Wastegate-Verkabelung prüfen. |
| P1659 | Wastegate-Ventil B - Bereich/Leistung | 🟡 WARNung | Ventil-Bereich prüfen. |

---

## C0xxx - Fahrwerk (ABS/ESP)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| C0000 | TCS (Traktionskontrolle) - Schaltfehler | 🟡 WARNUNG | ABS/ESP-Steuergerät prüfen. |
| C0035 | Raddrehzahl links vorne - Sensorfehler | 🟡 WARNUNG | Radlager oder Sensor prüfen. |
| C0040 | Raddrehzahl rechts vorne - Sensorfehler | 🟡 WARNUNG | Radlager oder Sensor prüfen. |
| C0045 | Raddrehzahl links hinten - Sensorfehler | 🟡 WARNUNG | Radlager oder Sensor prüfen. |
| C0050 | Raddrehzahl rechts hinten - Sensorfehler | 🟡 WARNUNG | Radlager oder Sensor prüfen. |
| C0060 | ABS-Ventil links vorne - Fehler | 🟠 LEISTUNG | ABS-Ventilblock prüfen. |
| C0065 | ABS-Ventil rechts vorne - Fehler | 🟠 LEISTUNG | ABS-Ventilblock prüfen. |
| C0070 | ABS-Ventil links hinten - Fehler | 🟠 LEISTUNG | ABS-Ventilblock prüfen. |
| C0075 | ABS-Ventil rechts hinten - Fehler | 🟠 LEISTUNG | ABS-Ventilblock prüfen. |
| C0080 | ABS-Pumpenmotor - Fehler | 🔴 KRITISCH | ABS-Hydraulikblock defekt. |
| C0090 | Raddrehzahl links vorne - Signalfehler | 🟡 WARNUNG | Sensor oder Verkabelung. |

---

## B0xxx - Karosserie (Airbag, Beleuchtung)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| B0001 | Fahrer-Airbag - Widerstand zu niedrig | 🔴 KRITISCH | Airbag-Verkabelung prüfen! |
| B0002 | Fahrer-Airbag - Widerstand zu hoch | 🔴 KRITISCH | Airbag-Steckverbindung. |
| B0003 | Fahrer-Airbag - Unterbrechung | 🔴 KRITISCH | Verkabelung oder Steckverbinder. |
| B0004 | Fahrer-Airbag - Kurzschluss nach Masse | 🔴 KRITISCH | Verkabelung prüfen. |
| B0005 | Fahrer-Airbag - Kurzschluss nach Plus | 🔴 KRITISCH | Verkabelung prüfen. |
| B0010 | Beifahrer-Airbag - Widerstand zu niedrig | 🔴 KRITISCH | Airbag-Verkabelung prüfen. |
| B0011 | Beifahrer-Airbag - Widerstand zu hoch | 🔴 KRITISCH | Steckverbindung prüfen. |
| B0100 | Innenbeleuchtung - Schaltfehler | 🔵 INFO | Relais oder Birne. |
| B0101 | Scheinwerferrelais - Schaltfehler | 🟡 WARNUNG | Relais prüfen. |

---

## U0xxx - Kommunikation (CAN-Bus)

| Code | Beschreibung (DE) | Severity | A14NET-Hinweis |
|------|-------------------|----------|----------------|
| U0001 | CAN-Bus Hochgeschwindigkeit - Fehler | 🔴 KRITISCH | CAN-Leitungen prüfen. Alle Module betroffen. |
| U0100 | Kommunikation mit ECM/PCM verloren | 🟠 LEISTUNG | ECU-Kommunikation unterbrochen. |
| U0101 | Kommunikation mit TCM verloren | 🟡 WARNUNG | Getriebe-ECU nicht erreichbar. |
| U0121 | Kommunikation mit ABS-Modul verloren | 🟡 WARNUNG | ABS-Steuergerät nicht erreichbar. |
| U0140 | Kommunikation mit BCM verloren | 🟡 WARNung | Karosserieelektrik-Steuergerät. |
| U0155 | Kommunikation mit Kombiinstrument verloren | 🔵 INFO | Schalttafel-Kommunikation. |

---

## Häufigste A14NET-spezifische Kombinationen

### Problem: Rattern bei Kaltstart
| Mögliche Codes | Ursache |
|---------------|---------|
| P0340 + P0341 | Nockenwellensensor - Timing-Kette |
| P1345 | Nocken-Kurbelwellen-Korrelation |
| P0016 | Kurbelwellen-Nockenwellen-Korrelation |

### Problem: Leistungsverlust
| Mögliche Codes | Ursache |
|---------------|---------|
| P0299 | Unterladung - Wastegate undicht |
| P0234 | Überladung - Wastegate klemmt |
| P1241 | Turbo-Boost zu niedrig |
| P1253 | Turbo-Aktuator festgefahren |

### Problem: Rauer Leerlauf
| Mögliche Codes | Ursache |
|---------------|---------|
| P0100–P0103 | MAF-Sensor defekt |
| P1100/P1101 | MAF-Sensor (Hersteller) |
| P0171 | Kraftstoffsystem zu mager |
| P0300–P0304 | Zündaussetzer |

### Problem: Temperatur-Warnungen
| Mögliche Codes | Ursache |
|---------------|---------|
| P0116/P0117 | Kühlmitteltemperatur-Sensor |
| P1187/P1188 | Öltemperatur-Sensor |
| P1299 | Zylinderkopf-Übertemperatur 🔴 |

---

## Quellen

- SAE J2012 (DTC Standard)
- Bosch ME17.9.22 Fehlercodedefinitionen
- GM/Opel Technische Service Bulletin (TSB)
- CANOPO-ODB App DTC-Datenbank (500+ Codes)
