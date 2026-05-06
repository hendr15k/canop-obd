# MVVM Architektur mit Clean Architecture für Android OBD-Apps

## Inhaltsverzeichnis

1. [Einführung](#einführung)
2. [Architektur-Übersicht](#architektur-übersicht)
3. [Layer-Trennung (UI / Domain / Data)](#layer-trennung)
4. [Datenfluss-Diagramm](#datenfluss-diagramm)
5. [Empfohlene Package-Struktur](#package-struktur)
6. [MVVM Pattern mit Jetpack Compose](#mvvm-compose)
7. [Repository Pattern für OBD-Daten](#repository-pattern)
8. [Use Cases / Interactors](#use-cases)
9. [Dependency Injection (Hilt)](#dependency-injection)
10. [Single Activity Architecture](#single-activity)
11. [Unidirectional Data Flow](#unidirectional-data-flow)
12. [State Management Patterns](#state-management)
13. [Error Handling in Clean Architecture](#error-handling)
14. [Testing Strategy für MVVM](#testing)
15. [Code-Beispiele für OBD-Kontext](#code-beispiele)
16. [Quellen und Links](#quellen)

---

## 1. Einführung

Diese Dokumentation beschreibt die empfohlene Architektur für eine Android OBD-App (On-Board-Diagnose), basierend auf MVVM (Model-View-ViewModel) mit Clean Architecture Prinzipien. Die Architektur ist speziell zugeschnitten auf die Anforderungen von OBD-Apps wie Canopo OBD.

### Warum Clean Architecture für OBD-Apps?

OBD-Apps haben spezifische Herausforderungen:

- **Echtzeit-Datenströme**: Kontinuierliche PID-Abfragen (50ms - 2000ms Intervall)
- **Hardware-Kommunikation**: Bluetooth-Verbindung mit ELM327-Adaptern
- **Domänenlogik**: Turbolader-Analyse, Kraftstoffverbrauchsberechnung, Fehlerdiagnose
- **Persistenz**: Trip-Aufzeichnung, Wartungshistorie, Einstellungen
- **Komplexität**: Multiple Subsysteme (Motor, Getriebe, Abgassysteme)

Clean Architecture addressiert diese durch klare Trennung der Concerns und ermöglicht:
- Testbarkeit (Unit Tests für Business Logic)
- Wartbarkeit (Module austauschbar)
- Skalierbarkeit (neue Features einfach hinzufügen)

---

## 2. Architektur-Übersicht

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION LAYER                              │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐  │
│  │   Screens   │    │   Dialogs   │    │  Components │    │    HUD      │  │
│  │  (Compose) │    │  (Compose)  │    │  (Compose)  │    │   Mode      │  │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    └──────┬──────┘  │
│         │                   │                   │                   │         │
│         └───────────────────┴───────────────────┴───────────────────┘         │
│                                    │                                          │
│                           ┌────────▼────────┐                                │
│                           │   ViewModels    │                                 │
│                           │  (StateHoisting) │                                │
│                           └────────┬────────┘                                │
└────────────────────────────────────┼────────────────────────────────────────┘
                                     │
┌────────────────────────────────────┼────────────────────────────────────────┐
│                              DOMAIN LAYER                                    │
│                                    │                                          │
│  ┌────────────────────────────────┼────────────────────────────────┐      │
│  │                     USE CASES / INTERACTORS                       │      │
│  ├─────────────────┬─────────────────┬─────────────────┬────────────┤      │
│  │ GetOBDData      │ AnalyzeTurbo    │ ReadDTCs        │ ManageTrip │      │
│  │ UseCase         │ UseCase         │ UseCase         │ UseCase    │      │
│  └────────┬────────┴────────┬────────┴────────┬────────┴─────┬──────┘      │
│           │                 │                 │              │               │
│  ┌────────▼────────┐ ┌─────▼──────┐ ┌───────▼───────┐ ┌───▼───────┐      │
│  │    REPOSITORIES  │ │  ANALYZERS │ │    ANALYZERS   │ │ ANALYZERS │      │
│  │    (Interfaces)  │ │  (Domain)  │ │   (Domain)     │ │ (Domain)  │      │
│  └────────┬────────┘ └─────────────┘ └───────────────┘ └───────────┘      │
└───────────┼──────────────────────────────────────────────────────────────────┘
            │
┌───────────┼──────────────────────────────────────────────────────────────────┐
│           │                     DATA LAYER                                     │
│           │                                                                    │
│  ┌────────▼────────┐    ┌───────────────┐    ┌──────────────────────────┐     │
│  │   REPOSITORIES  │    │  DATA SOURCES │    │     DATA SOURCES         │     │
│  │  (Implementations) │  │   (Room DB)   │    │   (ELM327 Bluetooth)    │     │
│  └────────┬────────┘    └───────────────┘    └──────────────────────────┘     │
└───────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Layer-Trennung

### 3.1 UI Layer (Presentation)

**Verantwortlichkeiten:**
- UI-Darstellung mit Jetpack Compose
- User-Input-Verarbeitung
- State-Hoisting zu ViewModels
- Navigation zwischen Screens

**Komponenten:**
```
ui/
├── screens/           # Vollständige Screens
│   ├── dashboard/    # Hauptdashboard
│   ├── turbo/        # Turbo-Monitor
│   └── settings/     # Einstellungen
├── components/        # Wiederverwendbare UI-Komponenten
│   ├── Gauges.kt
│   ├── Cards.kt
│   └── Dialogs.kt
├── dialogs/          # Modal Dialogs
├── theme/           # Farben, Typografie, Theme
└── navigation/      # Navigation Graph
```

### 3.2 Domain Layer

**Verantwortlichkeiten:**
- Geschäftslogik und Regeln
- Domänenmodelle (keine Android-Abhängigkeiten)
- Use Cases (Anwendungsfälle)
- Repository-Interfaces

**Schlüsselprinzipien:**
- **Keine Android-Referenzen** im Domain Layer
- Geschäftslogik in Use Cases gekapselt
- Interfaces für alle Abhängigkeiten
- Domain Models sind Plain Kotlin Objects

```
domain/
├── model/           # Domänenmodelle
│   ├── OBDData.kt
│   ├── TurboAnalysis.kt
│   └── VehicleHealth.kt
├── repository/      # Repository Interfaces
│   ├── OBDRepository.kt
│   └── VehicleRepository.kt
├── usecase/        # Use Cases
│   ├── GetOBDDataUseCase.kt
│   ├── AnalyzeTurboHealthUseCase.kt
│   └── ReadDiagnosticTroubleCodesUseCase.kt
└── analyzer/       # Domain Analyzer (Business Logic)
    ├── BatteryHealthAnalyzer.kt
    ├── TurboAnalyzer.kt
    └── FuelTrimAnalyzer.kt
```

### 3.3 Data Layer

**Verantwortlichkeiten:**
- Repository-Implementierungen
- Data Sources (lokal und remote)
- Daten-Mapping zwischen Ebenen
- Caching-Strategien

```
data/
├── local/           # Room Datenbank
│   ├── database/
│   ├── dao/
│   └── entity/
├── remote/         # Bluetooth/ELM327
│   ├── bluetooth/
│   └── elm327/
├── repository/     # Repository Implementations
│   ├── OBDRepositoryImpl.kt
│   └── VehicleRepositoryImpl.kt
└── mapper/         # Data <-> Domain Mappers
```

---

## 4. Datenfluss-Diagramm

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           DATENFLUSS (UNIDIRECTIONAL)                         │
└──────────────────────────────────────────────────────────────────────────────┘

    ┌─────────────┐          ┌─────────────┐          ┌─────────────┐
    │  USER       │          │   UI        │          │  VIEWMODEL  │
    │  ACTION     │─────────▶│  (Compose)  │─────────▶│  (State)    │
    └─────────────┘          └─────────────┘          └──────┬──────┘
                                                              │
    ┌─────────────┐          ┌─────────────┐                 │
    │   DOMAIN    │◀─────────│   USE CASE  │◀────────────────┘
    │   MODEL     │          │             │          ┌─────────────┐
    └──────┬──────┘          └─────────────┘          │   REPOSITORY│
           │                                            │   INTERFACE │
    ┌──────┴──────┐          ┌─────────────┐          └──────┬──────┘
    │  ANALYZER   │          │  REPOSITORY │◀─────────────────┘
    │  (Business  │◀─────────│  CALL       │
    │   Logic)    │          │             │
    └─────────────┘          └──────┬──────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
              ┌─────▼──────┐              ┌────────▼────────┐
              │  LOCAL      │              │   REMOTE        │
              │  DATA       │              │   DATA SOURCE   │
              │  SOURCE     │              │   (ELM327 BT)   │
              │  (Room DB)  │              │                 │
              └─────────────┘              └─────────────────┘

================================================================================
                            TYPISCHER OBD-DATENFLUSS
================================================================================

    User drückt        ViewModel                    Repository
    "Connect" ──────▶ collectAsState() ◀──── StateFlow ◀────┐
                                                        │
    ┌─────────────────────────────────────────────────────┘
    │                                    │
    ▼                                    ▼
┌───────────┐                     ┌──────────────┐
│  UI       │                     │  ELM327      │
│  zeigt    │ ◀── uiState ────────│  Connection  │
│  "Verbunden"                     │              │
└───────────┘                     └──────────────┘
```

---

## 5. Package-Struktur

### 5.1 Empfohlene Struktur (Clean Architecture)

```
com.canopobd/
│
├── di/                         # Dependency Injection Module
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
│
├── domain/                     # DOMAIN LAYER (Keine Android Deps)
│   ├── model/
│   │   ├── OBDData.kt
│   │   ├── TurboData.kt
│   │   ├── VehicleHealth.kt
│   │   ├── DTC.kt
│   │   ├── FuelData.kt
│   │   └── TripData.kt
│   │
│   ├── repository/             # Repository Interfaces
│   │   ├── OBDRepositoryInterface.kt
│   │   ├── VehicleRepositoryInterface.kt
│   │   └── SettingsRepositoryInterface.kt
│   │
│   ├── usecase/
│   │   ├── obd/
│   │   │   ├── ConnectToAdapterUseCase.kt
│   │   │   ├── GetOBDDataStreamUseCase.kt
│   │   │   ├── ReadDTCsUseCase.kt
│   │   │   ├── ClearDTCsUseCase.kt
│   │   │   └── GetSupportedPIDsUseCase.kt
│   │   │
│   │   ├── analysis/
│   │   │   ├── AnalyzeTurboHealthUseCase.kt
│   │   │   ├── AnalyzeBatteryHealthUseCase.kt
│   │   │   ├── AnalyzeFuelTrimUseCase.kt
│   │   │   └── CalculateFuelEconomyUseCase.kt
│   │   │
│   │   └── trip/
│   │       ├── StartTripRecordingUseCase.kt
│   │       ├── StopTripRecordingUseCase.kt
│   │       └── ExportTripDataUseCase.kt
│   │
│   └── analyzer/               # Domain Logic (Pure Kotlin)
│       ├── BatteryHealthAnalyzer.kt
│       ├── TurboHealthAnalyzer.kt
│       ├── FuelTrimAnalyzer.kt
│       ├── EGTAnalyzer.kt
│       └── DriveStyleAnalyzer.kt
│
├── data/                      # DATA LAYER
│   ├── local/
│   │   ├── database/
│   │   │   └── CanopoDatabase.kt
│   │   ├── dao/
│   │   │   ├── MaintenanceDao.kt
│   │   │   ├── TripDao.kt
│   │   │   └── SettingsDao.kt
│   │   └── entity/
│   │       ├── MaintenanceEntity.kt
│   │       └── TripEntity.kt
│   │
│   ├── remote/
│   │   ├── bluetooth/
│   │   │   ├── ELM327Connection.kt
│   │   │   └── BluetoothDeviceManager.kt
│   │   └── protocol/
│   │       ├── OBDProtocol.kt
│   │       ├── PIDDecoder.kt
│   │       └── Mode22Decoder.kt
│   │
│   ├── repository/             # Repository Implementations
│   │   ├── OBDRepositoryImpl.kt
│   │   ├── VehicleRepositoryImpl.kt
│   │   └── SettingsRepositoryImpl.kt
│   │
│   └── mapper/
│       ├── OBDDataMapper.kt
│       └── EntityMapper.kt
│
├── ui/                        # UI LAYER (Presentation)
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   ├── navigation/
│   │   └── NavGraph.kt
│   │
│   ├── components/            # Wiederverwendbare Composables
│   │   ├── Gauge.kt
│   │   ├── GaugeArc.kt
│   │   ├── TrendGraph.kt
│   │   ├── StatusCard.kt
│   │   └── WarningOverlay.kt
│   │
│   ├── screens/
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt
│   │   │   └── DashboardViewModel.kt
│   │   │
│   │   ├── turbo/
│   │   │   ├── TurboScreen.kt
│   │   │   └── TurboViewModel.kt
│   │   │
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   │
│   └── dialogs/
│       ├── DTCDialog.kt
│       ├── ConnectionDialog.kt
│       └── AlertDialog.kt
│
├── bluetooth/                 # Plattform-spezifischer Code
│   ├── ELM327BTConnection.kt
│   └── RemoteBridge.kt
│
├── gps/                       # GPS Tracking
│   └── GPSTracker.kt
│
└── MainActivity.kt            # Single Entry Point
```

### 5.2 Alternative: Feature-Based Struktur

Für größere Apps mit vielen Features:

```
com.canopobd/
├── core/                      # Geteilter Code
│   ├── domain/               # Core Domain Models
│   ├── ui/                   # Geteilte UI Components
│   └── di/                   # Core DI Modules
│
├── feature/
│   ├── dashboard/            # Dashboard Feature
│   │   ├── data/
│   │   ├── domain/
│   │   └── ui/
│   │
│   ├── turbo/               # Turbo Feature
│   │   ├── data/
│   │   ├── domain/
│   │   └── ui/
│   │
│   └── diagnostics/         # Diagnostics Feature
│       ├── data/
│       ├── domain/
│       └── ui/
│
└── app/                      # App Module
    ├── MainActivity.kt
    └── CanopoApp.kt
```

---

## 6. MVVM Pattern mit Jetpack Compose

### 6.1 Grundprinzipien

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            MVVM PATTERN                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────┐         ┌─────────────┐         ┌─────────────┐         │
│   │   VIEW      │         │ VIEWMODEL   │         │   MODEL     │         │
│   │  (Compose)  │         │             │         │  (Data/Repo)│         │
│   └─────────────┘         └─────────────┘         └─────────────┘         │
│        │                        │                        │               │
│        │  1. User Interaction   │                        │               │
│        ├──────────────────────▶│                        │               │
│        │                        │                        │               │
│        │                        │  2. Use Case/Aktion    │               │
│        │                        ├───────────────────────▶│               │
│        │                        │                        │               │
│        │                        │  3. Daten-Updates      │               │
│        │  4. UI State Update    │◀───────────────────────┤               │
│        │◀───────────────────────┤                        │               │
│        │                        │                        │               │
│   ┌────┴─────────────┐         │                        │               │
│   │ StateFlow        │         │ StateFlow              │               │
│   │ collectAsState() │         │ _uiState               │               │
│   └──────────────────┘         └────────────────────────┘               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.2 ViewModel für OBD-Dashboard

```kotlin
// DashboardViewModel.kt
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getOBDDataUseCase: GetOBDDataStreamUseCase,
    private val connectUseCase: ConnectToAdapterUseCase,
    private val readDTCsUseCase: ReadDTCsUseCase
) : ViewModel() {

    // UI State als StateFlow
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeOBDData()
    }

    // User Actions (Events)
    fun onConnect(address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true) }
            connectUseCase(address)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            connectionState = ConnectionState.Connected,
                            isConnecting = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            connectionState = ConnectionState.Error(error.message ?: "Unknown"),
                            isConnecting = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun onDisconnect() {
        viewModelScope.launch {
            disconnectUseCase()
            _uiState.update {
                it.copy(connectionState = ConnectionState.Disconnected)
            }
        }
    }

    fun onReadDTCs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isReadingDTCs = true) }
            readDTCsUseCase()
                .onSuccess { dtcs ->
                    _uiState.update {
                        it.copy(dtcs = dtcs, isReadingDTCs = false)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message, isReadingDTCs = false)
                    }
                }
        }
    }

    private fun observeOBDData() {
        viewModelScope.launch {
            getOBDDataUseCase().collect { data ->
                _uiState.update { it.copy(obdData = data) }
            }
        }
    }
}

// UI State Data Class
data class DashboardUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val obdData: OBDData? = null,
    val dtcs: List<DiagnosticTroubleCode> = emptyList(),
    val isConnecting: Boolean = false,
    val isReadingDTCs: Boolean = false,
    val error: String? = null,
    val settings: UserSettings = UserSettings()
)

enum class ConnectionState {
    Disconnected, Connecting, Connected, Error
}
```

### 6.3 Compose Screen

```kotlin
// DashboardScreen.kt
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onConnect(lastDeviceAddress)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Connection Status Card
        ConnectionStatusCard(
            state = uiState.connectionState,
            onConnect = viewModel::onConnect,
            onDisconnect = viewModel::onDisconnect
        )

        // Fehleranzeige
        uiState.error?.let { error ->
            ErrorBanner(message = error)
        }

        // OBD Gauges
        uiState.obdData?.let { data ->
            GaugesRow(
                rpm = data.rpm,
                speed = data.speed,
                coolantTemp = data.coolantTemp
            )
        }

        // DTC Button
        Button(
            onClick = viewModel::onReadDTCs,
            enabled = !uiState.isReadingDTCs
        ) {
            Text(if (uiState.isReadingDTCs) "Lese Fehler..." else "Fehler auslesen")
        }
    }
}
```

---

## 7. Repository Pattern für OBD-Daten

### 7.1 Repository Interface (Domain Layer)

```kotlin
// OBDRepositoryInterface.kt (Domain)
interface OBDRepositoryInterface {
    // Connection State
    val connectionState: StateFlow<ConnectionState>

    // OBD Data Stream
    val obdDataStream: StateFlow<OBDData>

    // DTCs
    val dtcResponse: StateFlow<DTCResponse?>

    // Connection Actions
    suspend fun connect(deviceAddress: String): Result<Unit>
    suspend fun disconnect()

    // OBD Operations
    suspend fun readDTCs(): Result<List<DiagnosticTroubleCode>>
    suspend fun clearDTCs(): Result<Unit>
    suspend fun getSupportedPIDs(): Result<List<String>>

    // Data Recording
    suspend fun startRecording(): Result<Unit>
    suspend fun stopRecording(): Result<List<DataRecord>>

    // Settings
    suspend fun setPollRate(rateMs: Long)
    suspend fun setMeasurementUnit(unit: MeasurementUnit)
}
```

### 7.2 Repository Implementation (Data Layer)

```kotlin
// OBDRepositoryImpl.kt (Data)
class OBDRepositoryImpl @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val database: CanopoDatabase,
    private val preferences: SharedPreferences
) : OBDRepositoryInterface {

    private val connection = ELM327BTConnection(bluetoothAdapter)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _obdData = MutableStateFlow(OBDData())
    override val obdDataStream: StateFlow<OBDData> = _obdData.asStateFlow()

    private val pollPIDs = listOf(
        OBDPID.RPM, OBDPID.SPEED, OBDPID.COOLANT_TEMP,
        OBDPID.THROTTLE, OBDPID.ENGINE_LOAD, OBDPID.FUEL_LEVEL
    )

    override suspend fun connect(deviceAddress: String): Result<Unit> = suspendCoroutine { cont ->
        try {
            _connectionState.value = ConnectionState.Connecting
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            val socket = device.createRfcommSocketToServiceRecord(
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            )
            socket.connect()
            connection.initialize(socket)
            _connectionState.value = ConnectionState.Connected
            startPolling()
            cont.resume(Result.success(Unit))
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
            cont.resume(Result.failure(e))
        }
    }

    private var pollingJob: Job? = null

    private fun startPolling() {
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val results = connection.readMultiplePIDs(pollPIDs)
                    _obdData.value = OBDData(
                        rpm = results[OBDPID.RPM] ?: 0.0,
                        speed = results[OBDPID.SPEED] ?: 0.0,
                        coolantTemp = results[OBDPID.COOLANT_TEMP] ?: 0.0
                    )
                    delay(getPollRate())
                } catch (e: Exception) {
                    handleConnectionError(e)
                }
            }
        }
    }
}
```

---

## 8. Use Cases / Interactors

### 8.1 Was sind Use Cases?

Use Cases (auch Interactors genannt) kapseln einen einzelnen Anwendungsfall. Sie:
- Enthalten keine UI-Logik
- Haben typischerweise eine einzige Methode
- Nutzen Repositories für Datenzugriff
- Können andere Use Cases aufrufen

### 8.2 Use Case Beispiel: GetOBDDataStreamUseCase

```kotlin
// GetOBDDataStreamUseCase.kt
class GetOBDDataStreamUseCase @Inject constructor(
    private val repository: OBDRepositoryInterface
) {
    operator fun invoke(): Flow<OBDData> = repository.obdDataStream
}
```

### 8.3 Use Case Beispiel: AnalyzeTurboHealthUseCase

```kotlin
// AnalyzeTurboHealthUseCase.kt
class AnalyzeTurboHealthUseCase @Inject constructor(
    private val repository: OBDRepositoryInterface,
    private val turboAnalyzer: TurboHealthAnalyzer
) {
    operator fun invoke(): Flow<TurboHealthResult> = repository.obdDataStream
        .map { data -> turboAnalyzer.analyze(data) }
        .distinctUntilChanged()
}

class TurboHealthAnalyzer {
    data class TurboHealthInput(
        val boostPressure: Double,
        val wastegateDuty: Double,
        val turboRpm: Double,
        val coolantTemp: Double,
        val oilTemp: Double,
        val egt: Double
    )

    data class TurboHealthResult(
        val healthScore: Int,  // 0-100
        val status: TurboStatus,
        val issues: List<TurboIssue>,
        val recommendations: List<String>
    )

    enum class TurboStatus { HEALTHY, WARNING, CRITICAL, UNKNOWN }
    enum class TurboIssue { LOW_BOOST, OVERBOOST, HIGH_EGT, HIGH_OIL_TEMP }

    fun analyze(input: TurboHealthInput): TurboHealthResult {
        var score = 100
        val issues = mutableListOf<TurboIssue>()

        if (input.boostPressure < 0.5) {
            score -= 20
            issues.add(TurboIssue.LOW_BOOST)
        } else if (input.boostPressure > 1.3) {
            score -= 30
            issues.add(TurboIssue.OVERBOOST)
        }

        if (input.egt > 850) {
            score -= 25
            issues.add(TurboIssue.HIGH_EGT)
        }

        val status = when {
            score >= 80 -> TurboStatus.HEALTHY
            score >= 50 -> TurboStatus.WARNING
            else -> TurboStatus.CRITICAL
        }

        return TurboHealthResult(
            healthScore = score.coerceIn(0, 100),
            status = status,
            issues = issues,
            recommendations = issues.map { it.toRecommendation() }
        )
    }

    private fun TurboIssue.toRecommendation(): String = when (this) {
        TurboIssue.LOW_BOOST -> "Wastegate und Ladedrucksensor prüfen"
        TurboIssue.OVERBOOST -> "Ladedruckregelung prüfen"
        TurboIssue.HIGH_EGT -> "Last reduzieren"
        TurboIssue.HIGH_OIL_TEMP -> "Ölstand und Kühlung prüfen"
    }
}
```

---

## 9. Dependency Injection (Hilt)

### 9.1 Warum Dependency Injection?

DI ermöglicht:
- **Testbarkeit**: Abhängigkeiten leicht mockbar
- **Lose Kopplung**: Klassen kennen ihre Abhängigkeiten nicht
- **Lebenszyklus-Management**: Android-spezifische Lifecycle-Handling
- **Single Responsibility**: Keine Klasse erstellt ihre eigenen Abhängigkeiten

### 9.2 Hilt Modul Beispiel

```kotlin
// di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter? {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("canopo_prefs", Context.MODE_PRIVATE)
    }
}

// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideOBDRepository(
        bluetoothAdapter: BluetoothAdapter?,
        database: CanopoDatabase,
        preferences: SharedPreferences
    ): OBDRepositoryInterface {
        return OBDRepositoryImpl(bluetoothAdapter, database, preferences)
    }
}

// di/AnalyzerModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AnalyzerModule {

    @Provides
    @Singleton
    fun provideTurboHealthAnalyzer(): TurboHealthAnalyzer {
        return TurboHealthAnalyzer()
    }

    @Provides
    @Singleton
    fun provideBatteryHealthAnalyzer(): BatteryHealthAnalyzer {
        return BatteryHealthAnalyzer()
    }
}
```

### 9.3 Hilt Application

```kotlin
// CanopoApp.kt
@HiltAndroidApp
class CanopoApp : Application()

// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: DashboardViewModel by viewModels()
}

// DashboardViewModel.kt
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val connectUseCase: ConnectToAdapterUseCase,
    private val getOBDDataUseCase: GetOBDDataStreamUseCase
) : ViewModel() {
    // ...
}
```

---

## 10. Single Activity Architecture

### 10.1 Konzept

Single Activity Architecture bedeutet:
- Eine einzige `MainActivity` als Entry Point
- Navigation zwischen Screens via Jetpack Navigation Compose
- Keine Activity-Wechsel für neue Screens
- Dialoge und Bottom Sheets als separate Overlays

### 10.2 Navigation Graph

```kotlin
// Navigation.kt
@Composable
fun CanopoNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: DashboardViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToTurbo = { navController.navigate(Screen.Turbo.route) }
            )
        }

        composable(Screen.Turbo.route) {
            TurboScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Turbo : Screen("turbo")
    object Settings : Screen("settings")
}
```

---

## 11. Unidirectional Data Flow

### 11.1 Prinzip

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        UNIDIRECTIONAL DATA FLOW                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────┐      User       ┌─────────┐      Intent      ┌─────────────┐   │
│  │   UI    │◀───────────────│         │◀───────────────│   ViewModel │   │
│  │ (View)  │                │  State  │                 │   (Intent)  │   │
│  └─────────┘                │         │                 └──────┬──────┘   │
│       │                      │  Store  │                        │          │
│       │  State               │         │                        │ Action   │
│       │  Update              │         │                        │          │
│       │                     │         │                        ▼          │
│       │                     │         │                 ┌─────────────┐     │
│       │                     │         │                 │  Use Case  │     │
│       │                     │         │                 └──────┬─────┘     │
│       │                     │         │                        │          │
│       │                     │         │                        ▼          │
│       │                     │         │                 ┌─────────────┐     │
│       │                     │         │                 │ Repository │     │
│       │                     │         │                 └──────┬─────┘     │
│       │                     │         │                        │          │
│       │                     │         │                        ▼          │
│       │                     │         │                 ┌─────────────┐     │
│       │                     └─────────┘                 │ Data Source │     │
│       │                                                  └─────────────┘     │
│       ▼                                                                   │
│  ┌─────────┐      Render       ┌─────────┐                                 │
│  │   UI    │◀───────────────  │  New    │                                 │
│  │         │                   │  State  │                                 │
│  └─────────┘                   └─────────┘                                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 11.2 Implementation mit Intent Pattern

```kotlin
// Intent (User Action)
sealed class DashboardIntent {
    data class Connect(val deviceAddress: String) : DashboardIntent()
    object Disconnect : DashboardIntent()
    object ReadDTCs : DashboardIntent()
    data class SetPollRate(val rateMs: Long) : DashboardIntent()
}

// ViewModel mit Intent Processing
class DashboardViewModel @Inject constructor(
    private val connectUseCase: ConnectToAdapterUseCase,
    private val readDTCsUseCase: ReadDTCsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun processIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.Connect -> connect(intent.deviceAddress)
            is DashboardIntent.Disconnect -> disconnect()
            is DashboardIntent.ReadDTCs -> readDTCs()
            is DashboardIntent.SetPollRate -> setPollRate(intent.rateMs)
        }
    }

    private fun connect(address: String) {
        viewModelScope.launch {
            _state.update { it.copy(isConnecting = true) }
            connectUseCase(address)
                .onSuccess {
                    _state.update { it.copy(isConnecting = false, connectionState = ConnectionState.Connected) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isConnecting = false, error = error.message) }
                }
        }
    }
}
```

---

## 12. State Management Patterns

### 12.1 State-Klassen Design

```kotlin
// Immutable UI State
data class DashboardState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val obdData: OBDData? = null,
    val dtcs: List<DTC> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val settings: UserSettings = UserSettings()
)

// Async State für Loading/Error
sealed class AsyncState<out T> {
    object Idle : AsyncState<Nothing>()
    object Loading : AsyncState<Nothing>()
    data class Success<T>(val data: T) : AsyncState<T>()
    data class Error(val message: String) : AsyncState<Nothing>()
}

// Composable
@Composable
fun OBDDataDisplay(dataAsync: AsyncState<OBDData>) {
    when (val state = dataAsync) {
        is AsyncState.Loading -> CircularProgressIndicator()
        is AsyncState.Success -> GaugeRPM(value = state.data.rpm)
        is AsyncState.Error -> ErrorMessage(message = state.message)
        AsyncState.Idle -> {}
    }
}
```

### 12.2 Saved State Handle

```kotlin
// Für Activity-Rotation und Process-Death
class DashboardViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: OBDRepositoryInterface
) : ViewModel() {

    private val _selectedTab = savedStateHandle.getStateFlow(
        key = "selectedTab",
        initialValue = 0
    )
    val selectedTab: StateFlow<Int> = _selectedTab

    fun selectTab(index: Int) {
        savedStateHandle["selectedTab"] = index
    }
}
```

---

## 13. Error Handling in Clean Architecture

### 13.1 Fehler-Hierarchie

```kotlin
// domain/model/Errors.kt
sealed class OBDError(val message: String, val cause: Throwable? = null) {
    override fun toString() = message
}

// Connection Errors
sealed class ConnectionError(message: String, cause: Throwable? = null) : OBDError(message, cause) {
    class DeviceNotFound(address: String) : ConnectionError("Gerät nicht gefunden: $address")
    class BluetoothDisabled : ConnectionError("Bluetooth ist deaktiviert")
    class ConnectionTimeout : ConnectionError("Verbindung Timeout")
    class ConnectionLost(reason: String?) : ConnectionError("Verbindung verloren: $reason")
}

// Protocol Errors
sealed class ProtocolError(message: String, cause: Throwable? = null) : OBDError(message, cause) {
    class PIDNotSupported(val pid: String) : ProtocolError("PID nicht unterstützt: $pid")
    class ResponseTimeout(val pid: String) : ProtocolError("Timeout für PID: $pid")
}

// Repository Errors
sealed class RepositoryError(message: String, cause: Throwable? = null) : OBDError(message, cause) {
    class DatabaseError(cause: Throwable) : RepositoryError("Datenbankfehler", cause)
}
```

### 13.2 Result-Typ in Use Cases

```kotlin
sealed class OBDResult<out T> {
    data class Success<T>(val data: T) : OBDResult<T>()
    data class Failure(val error: OBDError) : OBDResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): OBDResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    fun getOrNull(): T? = when (this) { is Success -> data; is Failure -> null }
}

class ReadDTCsUseCase @Inject constructor(private val repository: OBDRepositoryInterface) {
    suspend operator fun invoke(): OBDResult<List<DTC>> {
        return try {
            val dtcs = repository.readDTCs()
            OBDResult.Success(dtcs)
        } catch (e: ProtocolError) {
            OBDResult.Failure(e)
        } catch (e: Exception) {
            OBDResult.Failure(ProtocolError.ResponseTimeout("DTC"))
        }
    }
}
```

---

## 14. Testing Strategy für MVVM

### 14.1 Testpyramide

```
                    ┌───────────────────┐
                    │    E2E Tests      │
                    │   (Instrumented)   │
                    │   - Navigation     │
                    │   - Full Flows     │
                    └───────────────────┘
                           ▲
                           │
                    ┌──────┴──────┐
                    │  Integration│
                    │    Tests    │
                    │ - ViewModel  │
                    │ - Repository │
                    └──────┬──────┘
                           ▲
                           │
                    ┌──────┴──────┐
                    │  Unit Tests │
                    │ - Use Cases │
                    │ - Analyzers │
                    │ - Mappers   │
                    └─────────────┘
```

### 14.2 Unit Tests für Use Cases

```kotlin
// AnalyzeTurboHealthUseCaseTest.kt
class AnalyzeTurboHealthUseCaseTest {
    private lateinit var useCase: AnalyzeTurboHealthUseCase
    private lateinit var mockRepository: MockOBDRepository
    private lateinit var analyzer: TurboHealthAnalyzer

    @BeforeEach
    fun setup() {
        mockRepository = MockOBDRepository()
        analyzer = TurboHealthAnalyzer()
        useCase = AnalyzeTurboHealthUseCase(mockRepository, analyzer)
    }

    @Test
    fun `healthy turbo returns healthy status`() = runTest {
        mockRepository.emitOBDData(OBDData(
            boostPressure = 0.7,
            wastegateDuty = 45.0,
            turboRpm = 120000.0,
            coolantTemp = 90.0,
            oilTemp = 100.0,
            egt = 700.0
        ))

        val results = mutableListOf<TurboHealthResult>()
        useCase().collect { result -> results.add(result) }

        assertEquals(1, results.size)
        assertEquals(TurboStatus.HEALTHY, results[0].status)
        assertTrue(results[0].healthScore >= 80)
    }

    @Test
    fun `low boost returns warning status`() = runTest {
        mockRepository.emitOBDData(OBDData(
            boostPressure = 0.3,  // Niedrig
            wastegateDuty = 80.0,
            turboRpm = 80000.0,
            coolantTemp = 90.0,
            oilTemp = 100.0,
            egt = 700.0
        ))

        val result = useCase().first()

        assertEquals(TurboStatus.WARNING, result.status)
        assertTrue(result.issues.contains(TurboIssue.LOW_BOOST))
    }
}

// Mock Repository
class MockOBDRepository : OBDRepositoryInterface {
    private val _obdData = MutableStateFlow(OBDData())
    override val connectionState = MutableStateFlow(ConnectionState.Connected)
    override val obdDataStream = _obdData.asStateFlow()
    override val dtcResponse = MutableStateFlow<DTCResponse?>(null)

    fun emitOBDData(data: OBDData) { _obdData.value = data }

    override suspend fun connect(deviceAddress: String): Result<Unit> = Result.success(Unit)
    override suspend fun disconnect() {}
    override suspend fun readDTCs(): Result<List<DiagnosticTroubleCode>> = Result.success(emptyList())
    override suspend fun clearDTCs(): Result<Unit> = Result.success(Unit)
    override suspend fun getSupportedPIDs(): Result<List<String>> = Result.success(emptyList())
    override suspend fun startRecording(): Result<Unit> = Result.success(Unit)
    override suspend fun stopRecording(): Result<List<DataRecord>> = Result.success(emptyList())
    override suspend fun setPollRate(rateMs: Long) {}
    override suspend fun setMeasurementUnit(unit: MeasurementUnit) {}
}
```

### 14.3 ViewModel Tests

```kotlin
// DashboardViewModelTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private lateinit var viewModel: DashboardViewModel
    private lateinit var mockConnectUseCase: MockConnectUseCase
    private lateinit var mockGetOBDDataUseCase: MockGetOBDDataUseCase

    @BeforeEach
    fun setup() {
        mockConnectUseCase = MockConnectUseCase()
        mockGetOBDDataUseCase = MockGetOBDDataUseCase()
        viewModel = DashboardViewModel(mockConnectUseCase, mockGetOBDDataUseCase)
    }

    @Test
    fun `connect updates state to connected on success`() = runTest {
        mockConnectUseCase.shouldSucceed = true

        viewModel.processIntent(DashboardIntent.Connect("00:11:22:33:44:55"))

        val states = mutableListOf<DashboardState>()
        viewModel.state.take(2).collect { states.add(it) }

        assertEquals(ConnectionState.Connected, states.last().connectionState)
        assertFalse(states.last().isConnecting)
    }

    @Test
    fun `connect updates state with error on failure`() = runTest {
        mockConnectUseCase.shouldSucceed = false
        mockConnectUseCase.errorMessage = "Bluetooth ist deaktiviert"

        viewModel.processIntent(DashboardIntent.Connect("00:11:22:33:44:55"))

        val state = viewModel.state.first()
        assertEquals(ConnectionState.Error, state.connectionState)
        assertEquals("Bluetooth ist deaktiviert", state.error)
    }
}
```

### 14.4 Robolectric/JUnit4 Test Configuration

```kotlin
// build.gradle.kts (app module)
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.robolectric:robolectric:4.11.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## 15. Code-Beispiele für OBD-Kontext

### 15.1 Domain Model: OBDData

```kotlin
// OBDData.kt (Domain Layer)
data class OBDData(
    val rpm: Double = 0.0,
    val speed: Double = 0.0,
    val coolantTemp: Double = 0.0,
    val intakeTemp: Double = 0.0,
    val throttle: Double = 0.0,
    val engineLoad: Double = 0.0,
    val fuelLevel: Double = 0.0,
    val batteryVoltage: Double = 0.0,
    val timingAdvance: Double = 0.0,
    val mafRate: Double = 0.0,
    val intakePressure: Double = 0.0,
    val fuelRailPressure: Double = 0.0,
    val commandedEGR: Double = 0.0,
    val shortTermFuelTrimB1: Double = 0.0,
    val longTermFuelTrimB1: Double = 0.0,
    val boostPressure: Double = 0.0,
    val wastegateDuty: Double = 0.0,
    val turboRpm: Double = 0.0,
    val egt: Double = 0.0,
    val oilTemp: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
```

### 15.2 FuelTrimAnalyzer (Domain Logic)

```kotlin
// FuelTrimAnalyzer.kt (Domain Layer)
class FuelTrimAnalyzer {

    data class FuelTrimResult(
        val shortTermTrim: Double,
        val longTermTrim: Double,
        val totalTrim: Double,
        val status: FuelTrimStatus,
        val diagnosis: String
    )

    enum class FuelTrimStatus { NORMAL, LEAN, RICH, WARNING, CRITICAL }

    fun analyze(stft: Double, ltft: Double): FuelTrimResult {
        val totalTrim = stft + ltft
        val status = when {
            totalTrim > 15 || stft > 10 -> FuelTrimStatus.CRITICAL
            totalTrim > 10 || stft > 7 -> FuelTrimStatus.WARNING
            totalTrim > 5 || stft > 3 -> FuelTrimStatus.LEAN
            totalTrim < -15 || stft < -10 -> FuelTrimStatus.CRITICAL
            totalTrim < -10 || stft < -7 -> FuelTrimStatus.WARNING
            totalTrim < -5 || stft < -3 -> FuelTrimStatus.RICH
            else -> FuelTrimStatus.NORMAL
        }

        val diagnosis = when (status) {
            FuelTrimStatus.CRITICAL -> if (totalTrim > 0) 
                "System zu mager - MAF/Luftleck prüfen" 
            else 
                "System zu fett - Einspritzung prüfen"
            FuelTrimStatus.WARNING -> "Leichte Abweichung - Monitorieren"
            else -> "Kraftstoffgemisch im Normalbereich"
        }

        return FuelTrimResult(
            shortTermTrim = stft,
            longTermTrim = ltft,
            totalTrim = totalTrim,
            status = status,
            diagnosis = diagnosis
        )
    }
}
```

### 15.3 OBD PID Definition

```kotlin
// OBDPID.kt
enum class OBDPID(
    val code: String,
    val displayName: String,
    val unit: String,
    val byteCount: Int,
    val formula: (ByteArray) -> Double
) {
    RPM("010C", "Engine RPM", "rpm", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 4.0 else 0.0
    }),
    SPEED("010D", "Vehicle Speed", "km/h", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    COOLANT_TEMP("0105", "Coolant Temperature", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    THROTTLE("0111", "Throttle Position", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    ENGINE_LOAD("0104", "Engine Load", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    FUEL_LEVEL("012F", "Fuel Tank Level", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    BATTERY_VOLTAGE("ATRV", "Battery Voltage", "V", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 10.0 else 0.0
    }),
    MAF_RATE("0110", "MAF Air Flow Rate", "g/s", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 100.0 else 0.0
    }),
    ENGINE_FUEL_RATE("015E", "Engine Fuel Rate", "L/h", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 20.0 else 0.0
    }),
    // ... weitere PIDs

    companion object {
        fun fromCode(code: String): OBDPID? = entries.find { it.code == code }
    }
}
```

---

## 16. Quellen und Links

### Offizielle Android Dokumentation

1. **Android Architecture Guide**
   - https://developer.android.com/topic/architecture
   - Der offizielle Leitfaden für Android-Architektur mit MVVM und Clean Architecture

2. **Architecture Components Samples**
   - https://github.com/android/architecture-components-samples
   -offizielle Beispiele von Google für Room, ViewModel, LiveData, Navigation

3. **Now in Android (NIA)**
   - https://github.com/android/nowinandroid
   - Vollständige Produktions-App mit moderner Architektur, Kotlin und Jetpack Compose

4. **Jetpack Compose**
   - https://developer.android.com/jetpack/compose
   - Offizielle Compose Dokumentation

5. **Hilt Dependency Injection**
   - https://developer.android.com/training/dependency-injection/hilt-android
   - Offizielle Hilt Dokumentation

6. **Room Database**
   - https://developer.android.com/topic/libraries/architecture/room
   - Offline-Datenbank mit Kotlin Coroutines Support

7. **Navigation Compose**
   - https://developer.android.com/jetpack/compose/navigation
   - Single Activity Architecture mit Navigation Compose

### Artikel und Tutorials

8. **Clean Architecture mit Android**
   - https://proandroiddev.com/clean-architecture-on-android-using-mvvm-kotlin-coroutines-and-repository-pattern-24428d5f8
   - Detaillierte Anleitung zur Implementierung

9. **MVVM mit Jetpack Compose**
   - https://developer.android.com/codelabs/basic-android-kotlin-training-mvvm-compose
   - Codelab für MVVM mit Compose

10. **State Management in Compose**
    - https://developer.android.com/jetpack/compose/state
    - Offizielle State Management Dokumentation

11. **Testing Android Apps**
    - https://developer.android.com/training/testing
    - Testing Best Practices

### Kotlin Coroutines

12. **Kotlin Coroutines Guide**
    - https://kotlinlang.org/docs/coroutines-overview.html
    - Offizielle Coroutines Dokumentation

13. **Flow API**
    - https://kotlinlang.org/docs/flow.html
    - Reactive Streams mit Kotlin

### Architektur Pattern

14. **Repository Pattern**
    - https://developer.android.com/topic/architecture/data-layer/repositories
    - Offizielle Repository Dokumentation

15. **Unidirectional Data Flow**
    - https://developer.android.com/topic/architecture/ui-layer/stateholders
    - State Management und UDF

---

## Anhang: Vergleich mit aktueller Canopo OBD Architektur

Die aktuelle Canopo OBD App verwendet bereits einige Clean Architecture Prinzipien:

**Vorhanden:**
- ✅ ViewModels für State Management
- ✅ StateFlow für reaktive Daten
- ✅ Repository Pattern (OBDRepository)
- ✅ Domain Analyzer (BatteryHealthAnalyzer, FuelTrimAnalyzer)
- ✅ Room Datenbank für Persistenz
- ✅ Jetpack Compose UI

**Empfehlungen für Verbesserungen:**

1. **Domain Layer Trennung**: Analyzer in eigenes `domain/` Package verschieben
2. **Use Cases**: Geschäftslogik in einzelne Use Cases kapseln
3. **Hilt Integration**: DI Module für Repository und Use Cases
4. **Fehler-Hierarchie**: Strukturierte Error-Klassen statt String
5. **Interface-Based Repositories**: Abstrakte Interfaces im Domain Layer

### Empfohlene Migration

```kotlin
// Vorher (aktuell)
class DashboardViewModel(application: Application) : ViewModel() {
    private val repository = OBDRepository(context, bluetoothAdapter)
    // Direkte Repository-Verwendung
}

// Nachher (Clean Architecture)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val connectUseCase: ConnectToAdapterUseCase,
    private val getOBDDataUseCase: GetOBDDataStreamUseCase
) : ViewModel() {
    // Use Cases über Constructor Injection
}
```

---

*Erstellt: Mai 2026*
*Version: 1.0*
