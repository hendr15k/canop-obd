# canop-obd

> OBD-II Android-App mit ELM327 Bluetooth-Unterstützung, Jetpack Compose & Material Design 3

![Build](https://github.com/hendr15k/canop-obd/actions/workflows/build.yml/badge.svg)
![Platform](https://img.shields.io/badge/platform-Android%2026%2B-brightgreen)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)

## Features

### Dashboard
- **Live-Tachometer** — RPM mit dynamischer Farbcodierung (grün → orange → rot)
- **Alle wichtigen Sensoren** — Geschwindigkeit, Kühlmitteltemperatur, Ansauglufttemperatur, Drosselklappe, Motorlast, Tankfüllstand
- **Batteriespannung** — Live-Überwachung mit Warnfarbe bei niedriger Spannung
- **Verbindungsqualität** — Dynamische Anzeige mit automatischer Polling-Anpassung

### Spezialmonitore
- **Turbo-Monitor** — Boost-Druck, Wastegate-Stellung, Ladelufttemperatur, Turbo-Drehzahl
- **Timing-Ketten-Monitor** — Verschleißindikator mit Zustandsbewertung
- **Turbo-Cooldown-Timer** — Manueller & automatischer Modus zum Schutz des Turboladers

### Fahrzeugprofile
- **CarProfile** — Vordefinierte Profile für verschiedene Fahrzeuge
- **Persistenz** — Letztes Profil wird automatisch gespeichert

### Verbindung
- **ELM327 Bluetooth** — Kompatibel mit allen gängigen Adaptern (ELM327, OBDLink, Vgate iCar Pro)
- **Adaptive Timing** — Automatische Polling-Anpassung basierend auf Verbindungsqualität
- **Auto-Reconnect** — Exponentieller Backoff bei Verbindungsproblemen

### UI/UX
- **Material Design 3** — Modernes Dark Theme für optimale Ablesbarkeit im Auto
- **Dynamische Farben** — Sensorwerte ändern die Farbe basierend auf Schwellwerten
- **Kritische Warnungen** — Hintergrundfarbe wechselt bei Überhitzung oder Motorgefährdung

## Unterstützte OBD-II PIDs

| PID | Name | Einheit |
|-----|------|---------|
| 010C | Motordrehzahl (RPM) | rpm |
| 010D | Fahrzeuggeschwindigkeit | km/h |
| 0105 | Kühlmitteltemperatur | °C |
| 0104 | Motorlast | % |
| 010F | Ansauglufttemperatur | °C |
| 0111 | Drosselklappenstellung | % |
| 012F | Kraftstofffüllstand | % |
| 0114 | Ladedruck | kPa |
| 010E | Zündzeitpunkt | ° |
| ATRV | Batteriespannung | V |

## Screenshots

> Coming soon

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
│   ├── model/
│   │   ├── OBDModels.kt      # OBDPID enum, OBDData, BluetoothDeviceInfo
│   │   └── CarProfile.kt     # Fahrzeugprofile & Sensordaten
│   └── repository/
│       └── OBDRepository.kt  # Single source of truth for OBD state
├── viewmodel/
│   └── DashboardViewModel.kt # MVVM ViewModel mit StateFlow
└── ui/
    ├── theme/                # Material 3 dark theme (canopo colors)
    ├── components/Gauges.kt   # CircularGauge composable
    ├── dashboard/
    │   └── DashboardScreen.kt # Main dashboard UI
    ├── turbo/                # Turbo-Monitor & Cooldown
    ├── timingchain/          # Timing-Ketten-Monitor
    └── carprofile/           # Fahrzeugprofil-Auswahl
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

## Contributing

1. Fork erstellen
2. Feature-Branch (`git checkout -b feature/amazing-feature`)
3. Commit (`git commit -m 'Add amazing feature'`)
4. Push (`git push origin feature/amazing-feature`)
5. Pull Request öffnen

## Lizenz

Apache 2.0 — siehe [LICENSE](LICENSE) für Details.
