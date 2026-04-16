# canop-obd

> OBD-II Android app mit ELM327 Bluetooth-Unterstützung, Jetpack Compose & Material Design 3

![Build](https://github.com/hendr15k/canop-obd/actions/workflows/build.yml/badge.svg)
![Platform](https://img.shields.io/badge/platform-Android%2026%2B-brightgreen)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)

## Features

- **ELM327 Bluetooth** — Verbindet sich mit jedem OBD-II Adapter (ELM327, OBDLink, Vgate iCar Pro etc.)
- **Live Dashboard** — RPM, Geschwindigkeit, Kühlmitteltemperatur, Drosselklappe, Motorlast, Tankfüllstand
- **Material Design 3** — Modernes Dark Theme für gute Ablesbarkeit im Auto
- **Kotlin + Jetpack Compose** — Moderne Android-Architektur mit MVVM
- **Auto Build** — GitHub Actions bauen die APK bei jedem Push automatisch

## Unterstützte OBD-II PIDs

| PID | Name | Einheit |
|-----|------|---------|
| 010C | Motordrehzahl (RPM) | rpm |
| 010D | Fahrzeuggeschwindigkeit | km/h |
| 0105 | Kühlmitteltemperatur | °C |
| 010F | Ansauglufttemperatur | °C |
| 0111 | Drosselklappenstellung | % |
| 0104 | Motorlast | % |
| 012F | Kraftstofffüllstand | % |
| ATRV | Batteriespannung | V |

## Setup

### Voraussetzungen
- Android Studio Hedgehog (2023.1.1) oder neuer
- JDK 17
- Android SDK 34
- Ein ELM327 Bluetooth OBD-II Adapter

### Build

```bash
git clone https://github.com/hendr15k/canop-obd.git
cd canop-obd
./gradlew assembleDebug
```

Die APK liegt in `app/build/outputs/apk/debug/app-debug.apk`.

### Installation

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Adapter koppeln

1. Android → Einstellungen → Bluetooth
2. ELM327 Adapter suchen und koppeln (PIN oft `1234` oder `0000`)
3. App öffnen → Bluetooth-Icon oben rechts → Gerät auswählen

## Architektur

```
app/src/main/java/com/canopobd/
├── MainActivity.kt           # Entry point
├── bluetooth/
│   └── ELM327BTConnection.kt # Bluetooth SPP connection + ELM327 AT commands
├── data/
│   ├── model/OBDModels.kt    # OBDPID enum, OBDData, BluetoothDeviceInfo
│   └── repository/OBDRepository.kt  # Single source of truth for OBD state
├── viewmodel/
│   └── DashboardViewModel.kt  # MVVM ViewModel
└── ui/
    ├── theme/                # Material 3 dark theme (canopo colors)
    ├── components/Gauges.kt  # CircularGauge composable
    └── dashboard/DashboardScreen.kt  # Main dashboard UI
```

## ELM327 AT Commands

Die App sendet folgende Initialisierungsbefehle:

| Befehl | Funktion |
|--------|----------|
| `ATZ` | Reset |
| `ATE0` | Echo aus |
| `ATL0` | Linefeed aus |
| `ATS0` | Spaces aus |
| `ATH0` | Headers aus |
| `ATSP0` | Automatische Protokollerkennung |
| `ATAT1` | Adaptive Timing an |

## Download

Debug APK von GitHub Actions:
**Artifact: `canop-obd-debug-apk`**
