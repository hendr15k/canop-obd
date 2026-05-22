# Kotlin Coroutines & Flow für Echtzeit-OBD-Datenverarbeitung

## Inhaltsverzeichnis

1. [StateFlow vs SharedFlow für UI State](#1-stateflow-vs-sharedflow-für-ui-state)
2. [ConflatedBroadcastChannel für OBD-Daten](#2-conflatedbroadcastchannel-für-obd-daten)
3. [Flow-Operatoren für Datenverarbeitung](#3-flow-operatoren-für-datenverarbeitung)
4. [Backpressure-Handling für schnelle OBD-Datenströme](#4-backpressure-handling-für-schnelle-obd-datenströme)
5. [Coroutine Dispatchers für Bluetooth-Kommunikation](#5-coroutine-dispatchers-für-bluetooth-kommunikation)
6. [Exception Handling in Flows](#6-exception-handling-in-flows)
7. [Testing von Coroutines und Flows](#7-testing-von-coroutines-und-flows)
8. [Structured Concurrency Best Practices](#8-structured-concurrency-best-practices)
9. [channelFlow vs flowOf für dynamische Streams](#9-channelflow-vs-flowof-für-dynamische-streams)
10. [Buffer- und Conflation-Optionen](#10-buffer--und-conflation-optionen)

---

## 1. StateFlow vs SharedFlow für UI State

### Grundlegende Unterschiede

| Eigenschaft | StateFlow | SharedFlow |
|------------|-----------|------------|
| **Initialwert** | Erforderlich | Optional (kann null sein) |
| **Replay** |Replayt den letzten Wert an neue Subscriber | Konfigurierbar (0, 1, alle) |
| **Use Case** | UI State | Events, Bus-Muster |
| **Null-Safety** | Nicht-nullbar (T) | Nullable (T?) |

### StateFlow - Der Standard für UI State

```kotlin
// Definition eines UI-State mit StateFlow
data class OBDDashboardState(
    val rpm: Int = 0,
    val speed: Int = 0,
    val fuelLevel: Float = 0f,
    val engineTemp: Int = 0,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class OBDDashboardViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(OBDDashboardState())
    val uiState: StateFlow<OBDDashboardState> = _uiState
    
    // Komfortable Update-Funktion
    fun updateRpm(newRpm: Int) {
        _uiState.update { currentState ->
            currentState.copy(rpm = newRpm)
        }
    }
    
    // Imperatives Update für komplexere Änderungen
    fun updateMultiple(newRpm: Int, newSpeed: Int) {
        _uiState.value = _uiState.value.copy(
            rpm = newRpm,
            speed = newSpeed
        )
    }
}
```

### SharedFlow - Für Events und One-Time-Nachrichten

```kotlin
// SharedFlow für UI Events (Navigation, Snackbar, etc.)
sealed class OBDEvent {
    data class ShowError(val message: String) : OBDEvent()
    data class NavigateTo(val route: String) : OBDEvent()
    object ShowConnectionSuccess : OBDEvent()
}

class OBDEventHandler {
    
    private val _events = MutableSharedFlow<OBDEvent>(
        replay = 0,           // Kein Replay für Events
        extraBufferCapacity = 64  // Puffer für Events
    )
    val events: SharedFlow<OBDEvent> = _events.asSharedFlow()
    
    suspend fun emitEvent(event: OBDEvent) {
        _events.emit(event)
    }
    
    // Für Fire-and-Forget Events
    fun tryEmitEvent(event: OBDEvent): Boolean {
        return _events.tryEmit(event)
    }
}
```

### Hybrid-Ansatz für OBD-Anwendung

```kotlin
class OBDSessionManager {
    
    // StateFlow für kontinuierlichen UI-State
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    // SharedFlow für OBD-Datenströme (können mehrere Consumer haben)
    private val _obdDataStream = MutableSharedFlow<OBDData>(
        replay = 1,  // Neuester Wert für neue Subscriber
        extraBufferCapacity = 100
    )
    val obdDataStream: SharedFlow<OBDData> = _obdDataStream.asSharedFlow()
    
    // SharedFlow für Events
    private val _oneTimeEvents = MutableSharedFlow<OneTimeEvent>()
    val oneTimeEvents: SharedFlow<OneTimeEvent> = _oneTimeEvents.asSharedFlow()
}
```

### Android Compose Integration

```kotlin
@Composable
fun OBDScreen(viewModel: OBDViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.oneTimeEvents.collect { event ->
            when (event) {
                is OneTimeEvent.ShowError -> showSnackbar(event.message)
                is OneTimeEvent.NavigateToSettings -> navController.navigate("settings")
            }
        }
    }
    
    // UI basierend auf State
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.errorMessage != null -> ErrorScreen(uiState.errorMessage!!)
        else -> Dashboard(uiState)
    }
}
```

---

## 2. ConflatedBroadcastChannel für OBD-Daten

### Historischer Kontext

`ConflatedBroadcastChannel` war die empfohlene Methode vor Kotlin 1.5 für Hot Flows. Es wurde durch `MutableSharedFlow` ersetzt und bietet identische Funktionalität mit besserem API-Design.

### Empfohlene Alternative: MutableSharedFlow mit Conflation

```kotlin
// Äquivalent zu ConflatedBroadcastChannel
class OBDDataSource {
    
    private val _obdDataChannel = MutableSharedFlow<OBDReading>(
        replay = 1,           // Conflation: nur neuester Wert
        extraBufferCapacity = 0,  // Kein extra Buffer
        onBufferOverflow = BufferOverflow.DROP_OLDEST  // Alte Werte verwerfen
    )
    val obdData: SharedFlow<OBDReading> = _obdDataChannel.asSharedFlow()
    
    suspend fun emitReading(reading: OBDReading) {
        _obdDataChannel.emit(reading)
    }
    
    // Fire-and-forget Emission
    fun tryEmit(reading: OBDReading): Boolean {
        return _obdDataChannel.tryEmit(reading)
    }
}
```

### OBD-spezifisches Beispiel

```kotlin
data class OBDReading(
    val pid: PID,
    val value: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val unit: String,
    val rawResponse: String
)

class OBDBluetoothManager {
    
    private val _liveReadings = MutableSharedFlow<OBDReading>(
        replay = 1,  // Neuester Wert wird replayed
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    
    val liveReadings: SharedFlow<OBDReading> = _liveReadings.asSharedFlow()
    
    // Polling-Loop für kontinuierliche OBD-Daten
    private fun startPollingLoop() {
        viewModelScope.launch {
            while (isActive) {
                val reading = readOBDCommand(currentPid)
                _liveReadings.emit(reading)  // Conflation passiert automatisch
                delay(pollInterval)  // Z.B. 100ms für schnelle PIDs
            }
        }
    }
}
```

### BufferOverflow-Strategien

```kotlin
// Option 1: DROP_OLDEST - Für Echtzeit-Daten wo nur Neuestes zählt
val rpmFlow = MutableSharedFlow<Int>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

// Option 2: DROP_LATEST - Verwerfe neueste wenn voll
val eventsFlow = MutableSharedFlow<Event>(
    replay = 0,
    onBufferOverflow = BufferOverflow.DROP_LATEST
)

// Option 3: SUSPEND - Standard, blockiert bis Platz
val defaultFlow = MutableSharedFlow<Int>(
    replay = 0,
    extraBufferCapacity = 64,
    onBufferOverflow = BufferOverflow.SUSPEND
)
```

---

## 3. Flow-Operatoren für Datenverarbeitung

### map - Transformation von OBD-Daten

```kotlin
// PID zu physikalischem Wert transformieren
fun Flow<String>.parseRPM(): Flow<Int> = map { response ->
    // OBD-Rohantwort: "41 0C 1A F8" -> RPM berechnen
    val bytes = response.split(" ").takeLast(2)
    val a = bytes[0].toInt(16)
    val b = bytes[1].toInt(16)
    ((256 * a) + b) / 4
}

// Temperatur von OBD zu Celsius
fun Flow<String>.parseTemperature(pid: PID): Flow<Float> = map { response ->
    val rawValue = response.split(" ").last().toInt(16)
    when (pid) {
        PID.ENGINE_COOLANT_TEMP -> rawValue - 40f  // Celsius
        PID.INTAKE_AIR_TEMP -> rawValue - 40f
        else -> rawValue.toFloat()
    }
}
```

### filter - OBD-Daten filtern

```kotlin
// Nur gültige Readings weiterleiten
fun Flow<OBDReading>.filterValid(): Flow<OBDReading> = filter { reading ->
    reading.value >= 0 && reading.value < MAX_REASONABLE_VALUE
}

// Fehlerhafte Daten filtern
fun Flow<OBDReading>.filterErrors(): Flow<OBDReading> = filter { reading ->
    reading.rawResponse.startsWith("41")  // Gültige OBD-Antworten
}

// Nur relevante PIDs
fun Flow<OBDReading>.filterPID(targetPids: Set<PID>): Flow<OBDReading> = 
    filter { it.pid in targetPids }
```

### debounce - UI-optimierung bei Benutzereingaben

```kotlin
// Suche wird erst nach 300ms Pause gesendet
val searchFlow = searchEditText.events()
    .debounce(300)
    .map { it.text }
    .filter { it.length >= 3 }
    .flatMapLatest { query -> fetchSearchResults(query) }

// Throttle für Performance-kritische Updates
val rpmUpdates = rpmFlow.throttle(16)  // ~60fps für UI
```

### Weitere wichtige Operatoren

```kotlin
// distinctUntilChanged - Nur Änderungen weiterleiten
fun Flow<OBDReading>.onlyChanges(): Flow<OBDReading> = 
    distinctUntilChanged { old, new -> 
        old.value.toInt() == new.value.toInt() 
    }

// sample - Regelmäßige Samples für Diagramme
fun Flow<OBDReading>.sampleForChart(intervalMs: Long = 100): Flow<OBDReading> = 
    sample(intervalMs)

// take - Nur bestimmte Anzahl
val first10Readings = obdFlow.take(10)

// catch - Fehler abfangen
fun <T> Flow<T>.withErrorHandling(): Flow<T> = 
    catch { e -> 
        emit(errorValue(e))
    }
```

### Kombinierte Datenverarbeitung

```kotlin
class OBDDataProcessor {
    
    fun createProcessedFlow(rawFlow: Flow<String>): Flow<ProcessedOBDData> {
        return rawFlow
            .filter { isValidOBDResponse(it) }
            .map { parseOBDResponse(it) }
            .filter { isReasonableValue(it) }
            .debounce(16)  // UI-Throttling
            .distinctUntilChanged { a, b -> 
                a.pid == b.pid && a.value.toInt() == b.value.toInt() 
            }
            .catch { e -> 
                emit(ProcessedOBDData.error(e))
            }
    }
    
    // Komplexere Verarbeitung mit mehreren Inputs
    fun combineSensorData(
        rpmFlow: Flow<Int>,
        speedFlow: Flow<Int>,
        tempFlow: Flow<Float>
    ): Flow<CombinedData> = combine(
        rpmFlow.distinctUntilChanged(),
        speedFlow.distinctUntilChanged(),
        tempFlow.distinctUntilChanged()
    ) { rpm, speed, temp ->
        CombinedData(rpm, speed, temp, calculateDerivedValues(rpm, speed))
    }
}
```

---

## 4. Backpressure-Handling für schnelle OBD-Datenströme

### Das Problem bei OBD-Daten

OBD-Schnittstellen können Daten mit 10-100Hz oder schneller senden. Android-UI kann nur ~60fps (16ms) verarbeiten. Ohne Backpressure-Handling führt dies zu:
- Memory Overflow
- UI-Lags
- dropped Frames

### Lösungsstrategien

```kotlin
// Strategie 1: Buffer mit Conflation
class BufferedOBDSource {
    private val _bufferedData = MutableSharedFlow<OBDReading>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
}

// Strategie 2: conflate() Operator
fun Flow<OBDReading>.conflateForUI(): Flow<OBDReading> = 
    conflate()  // Emitter läuft weiter, nur neuester Wert wird verarbeitet

// Strategie 3: collectLatest für Cancellation
viewModelScope.launch {
    obdSource.dataFlow
        .collectLatest { data ->  // Bricht vorherige Verarbeitung ab
            updateUI(data)
        }
}
```

### Praktisches Beispiel mit Backpressure

```kotlin
class OBDViewModel(
    private val obdRepository: OBDRepository
) {
    
    // Backpressure-konfigurierbarer DataStream
    private val _dataStreamConfig = MutableStateFlow(DataStreamConfig())
    
    val processedOBDData: Flow<OBDData> = obdRepository.rawDataFlow
        .conflate()  // Wichtig: UI nicht überfordern
        .transformWhile { data ->
            emit(processData(data))
            true  // Continue indefinitely
        }
        .retry { e ->
            if (e is RecoverableException) {
                delay(1000)
                true
            } else false
        }
        .catch { e -> 
            _errorState.emit(e)
        }
    
    // Konfigurierbare Sampling-Rate
    val sampledData: Flow<OBDData> = combine(
        processedOBDData,
        _dataStreamConfig
    ) { data, config ->
        data to config
    }.sample(config.sampleIntervalMs)
     .map { it.first }
}
```

### Buffer-Management

```kotlin
// Expliziter Buffer mit konfigurierbarer Größe
val bufferedFlow = rawFlow
    .buffer(
        capacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

// Async-Buffer für parallele Verarbeitung
val parallelProcessed = rawFlow
    .buffer(16)  // Buffer für Parallelisierung
    .map { data -> processExpensive(data) }
    .flowOn(Dispatchers.Default)

// Producer/Consumer mit unterschiedlichen Raten
fun createThrottledFlow(
    source: Flow<OBDReading>,
    targetRateHz: Float = 60f
): Flow<OBDReading> {
    val intervalMs = (1000f / targetRateHz).toLong()
    return source
        .conflate()
        .sample(intervalMs)
}
```

### Monitoring und Debugging

```kotlin
// Flow-Überwachung für Backpressure-Detection
class BackpressureMonitor {
    
    private var droppedCount = 0
    private var processedCount = 0
    
    fun monitor(flow: Flow<OBDReading>): Flow<OBDReading> = flow
        .onEach { processedCount++ }
        .onCompletion { cause ->
            if (cause != null) {
                logDroppedRatio()
            }
        }
    
    private fun logDroppedRatio() {
        val ratio = droppedCount.toFloat() / (processedCount + droppedCount)
        if (ratio > 0.1) {  // Warnung bei >10% Drop-Rate
            Log.w("Backpressure", "High drop rate: ${(ratio * 100).toInt()}%")
        }
    }
}
```

---

## 5. Coroutine Dispatchers für Bluetooth-Kommunikation

### Dispatcher-Übersicht

| Dispatcher | Thread-Pool | Use Case |
|------------|--------------|----------|
| `Dispatchers.Main` | Android Main Thread | UI-Updates |
| `Dispatchers.IO` | IO-optimiert (bis 64 Threads) | Bluetooth I/O |
| `Dispatchers.Default` | CPU-optimiert (bis Kerne) | Datenverarbeitung |
| `Dispatchers.Unconfined` | Kein fester Thread | Testing, spezielle Fälle |

### Bluetooth-spezifische Implementierung

```kotlin
object OBDDispatchers {
    
    // Bluetooth I/O - Blocking-Operationen
    val Bluetooth: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(4)
    
    // OBD-Protokoll-Parsing
    val Protocol: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(2)
    
    // UI-Updates (Main Thread)
    val UI: CoroutineDispatcher = Dispatchers.Main.immediate
}

// Bluetooth-Manager mit optimierten Dispatchers
class OBDBluetoothService(
    private val bluetoothAdapter: BluetoothAdapter
) {
    
    private val inputStream: InputStream = ...
    private val outputStream: OutputStream = ...
    
    // Lesen mit IO-Dispatcher
    suspend fun readResponse(timeoutMs: Long = 1000): String = 
        withContext(OBDDispatchers.Bluetooth) {
            val buffer = ByteArray(256)
            val bytesRead = withTimeoutOrNull(timeoutMs) {
                inputStream.read(buffer)
            } ?: throw TimeoutException("OBD Response timeout")
            
            String(buffer, 0, bytesRead ?: 0).trim()
        }
    
    // Schreiben mit IO-Dispatcher
    suspend fun sendCommand(command: String): Unit = 
        withContext(OBDDispatchers.Bluetooth) {
            val cmd = "$command\r".toByteArray()
            outputStream.write(cmd)
            outputStream.flush()
        }
    
    // Kombinierte Kommunikation
    suspend fun executeOBDCommand(command: String): OBDResponse = 
        withContext(OBDDispatchers.Bluetooth) {
            sendCommandInternal(command)
            val response = readResponse()
            parseResponse(response)
        }
}
```

### Flow-Erstellung mit Dispatcher-Wechsel

```kotlin
// Wichtig: flowOn für Emitter-Dispatcher
fun bluetoothDataFlow(): Flow<OBDReading> = flow {
    while (isActive) {
        val command = buildOBDCommand(currentPid)
        emit(executeOBDCommand(command))
        delay(pollInterval)
    }
}.flowOn(OBDDispatchers.Bluetooth)  // Emitter auf IO-Thread
   .map { parseOBDResponse(it) }
   .flowOn(OBDDispatchers.Protocol)  // Parsing auf Default
   .onEach { update -> /* UI-Updates */ }
   .flowOn(Dispatchers.Main)  // UI auf Main

// Alternative: channelFlow für bessere Kontrolle
fun bluetoothChannelFlow(): Flow<OBDReading> = channelFlow {
    withContext(OBDDispatchers.Bluetooth) {
        for (pid in activePIDs) {
            val reading = executeCommand(pid)
            send(reading)
        }
    }
}.flowOn(Dispatchers.Default)
```

### Praktische ViewModel-Integration

```kotlin
class OBDDashboardViewModel(
    private val bluetoothService: OBDBluetoothService,
    private val obdRepository: OBDRepository
) : ViewModel() {
    
    init {
        observeOBDData()
    }
    
    private fun observeOBDData() {
        viewModelScope.launch {
            obdRepository.obdReadings
                .flowOn(OBDDispatchers.Bluetooth)
                .map { reading -> transformToUIState(reading) }
                .flowOn(OBDDispatchers.Protocol)
                .collect { state ->
                    _uiState.update { state }  // Main Thread durch viewModelScope
                }
        }
    }
    
    fun sendCommand(command: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                bluetoothService.executeOBDCommand(command)
            } catch (e: Exception) {
                _error.emit(e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

---

## 6. Exception Handling in Flows

### Exception Transparency

Flows haben strenge Regeln für Exception Handling, bekannt als "Exception Transparency".

```kotlin
// FALSCH: Exception im Flow abfangen und emitten
fun badFlow(): Flow<Int> = flow {
    try {
        emit(1)
        throw RuntimeException("Error")
    } catch (e: Exception) {
        emit(-1)  // VERSTÖSST gegen Exception Transparency!
    }
}

// RICHTIG: catch-Operator verwenden
fun goodFlow(): Flow<Int> = flow {
    emit(1)
    throw RuntimeException("Error")
}.catch { e ->
    emit(-1)  // Im catch-Block erlaubt
    // oder: log(e); throw e
}
```

### Exception Handling Strategien

```kotlin
// Strategie 1: catch mit Emission
fun withFallback(): Flow<OBDReading> = obdFlow
    .catch { e ->
        emit(OBDReading.error(e))
    }

// Strategie 2: catch mit Retry
fun withRetry(): Flow<OBDReading> = obdFlow
    .retry(3) { e ->
        e is RecoverableException
    }

// Strategie 3: catch mit Supervision
fun withSupervision(): Flow<OBDReading> = flow {
    emitAll(supervisorScope {
        obdFlow
    })
}
.catch { e ->
    // Nur upstream Exceptions
    handleError(e)
}

// Strategie 4: OnEach + Catch für vollständige Abdeckung
fun declarativeHandling(): Flow<OBDReading> = obdFlow
    .onEach { data ->
        validate(data)
    }
    .catch { e ->
        log.error("Processing error", e)
    }
    .onCompletion { cause ->
        if (cause != null) {
            log.warn("Flow completed exceptionally")
        }
    }
```

### OBD-spezifisches Exception Handling

```kotlin
sealed class OBDException(message: String) : Exception(message) {
    class ConnectionLost : OBDException("Bluetooth connection lost")
    class NoData(val pid: PID) : OBDException("No data for PID: $pid")
    class InvalidResponse(val raw: String) : OBDException("Invalid response: $raw")
    class Timeout(val command: String) : OBDException("Timeout for command: $command")
}

class OBDDataSource {
    
    fun getReadings(): Flow<Result<OBDReading>> = flow {
        while (isActive) {
            try {
                val reading = fetchReading()
                emit(Result.success(reading))
            } catch (e: OBDException) {
                emit(Result.failure(e))
            }
            delay(pollInterval)
        }
    }
    .catch { e ->
        // Konvertiere zu Result
        emit(Result.failure(e))
    }
    
    // Mit Retry und Exponential Backoff
    fun robustReadings(): Flow<OBDReading> = flow {
        while (isActive) {
            emit(fetchReading())
        }
    }.retryWhen { e, attempt ->
        if (e is RecoverableOBDException && attempt < 3) {
            val delay = (1000L * (attempt + 1))
            delay(delay)  // 1s, 2s, 3s
            true
        } else {
            false
        }
    }.catch { e ->
        // Finaler Handler
        _connectionStatus.value = ConnectionStatus.ERROR
    }
}
```

### CoroutineExceptionHandler

```kotlin
class OBDApplication : Application() {
    
    private val exceptionHandler = CoroutineExceptionHandler { context, e ->
        when (e) {
            is OBDException -> handleOBDException(e)
            is CancellationException -> {
                // Erwartet bei normaler Cancellation
            }
            else -> {
                // Unerwartete Exceptions loggen
                Log.e("OBD", "Unhandled exception", e)
            }
        }
    }
    
    // Im ViewModel verwenden
    val viewModelScope = CoroutineScope(SupervisorJob() + exceptionHandler)
}
```

---

## 7. Testing von Coroutines und Flows

### Test-Dependencies

```kotlin
// build.gradle
dependencies {
    testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0"
}
```

### Grundlegendes Flow Testing

```kotlin
class OBDDataProcessorTest {
    
    @Test
    fun `map transforms raw string to reading`() = runTest {
        // Arrange
        val rawFlow = flowOf("41 0C 1A F8")  // Beispiel RPM-Response
        
        // Act
        val result = rawFlow
            .map { parseRPMResponse(it) }
            .first()
        
        // Assert
        assertEquals(6787, result)  // (26 * 256 + 248) / 4
    }
    
    @Test
    fun `filter removes invalid readings`() = runTest {
        // Arrange
        val readings = flowOf(
            OBDReading(PID.RPM, -1f, 0, "RPM"),
            OBDReading(PID.RPM, 500f, 0, "RPM"),
            OBDReading(PID.RPM, -50f, 0, "RPM")
        )
        
        // Act
        val valid = readings
            .filter { it.value >= 0 }
            .toList()
        
        // Assert
        assertEquals(1, valid.size)
        assertEquals(500f, valid[0].value)
    }
}
```

### TestScope und TestDispatcher

```kotlin
class OBDSessionTest {
    
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)
    private val testScope = TestScope(testDispatcher)
    
    @Test
    fun `session emits state changes`() = testScope.runTest {
        // Arrange
        val session = TestOBDSession(testDispatcher)
        
        // Act
        val results = mutableListOf<SessionState>()
        launch {
            session.state.collect { state ->
                results.add(state)
            }
        }
        
        // Advance virtual time
        testScheduler.advanceUntilIdle()
        
        // Assert
        assertEquals(3, results.size)
    }
    
    @Test
    fun `delays are skipped in test`() = runTest {
        val results = mutableListOf<Long>()
        
        launch {
            repeat(5) { i ->
                delay(1000)  // Wird übersprungen
                results.add(currentTime)
            }
        }
        
        testScheduler.advanceUntilIdle()
        
        // Virtual time ist 0, aber delays wurden übersprungen
        assertEquals(5, results.size)
    }
}
```

### Virtual Time Control

```kotlin
@Test
fun `throttled flow respects intervals`() = runTest {
    val flow = flow {
        repeat(10) { i ->
            emit(i)
            delay(100)
        }
    }.sample(300)
    
    val results = flow.toList()
    
    // Bei 300ms Intervall und 100ms Emission:
    // 0, 3, 6, 9 werden emitted (etwa 4 Werte)
    assertTrue(results.size <= 4)
}

@Test
fun `retry delays are controlled`() = runTest {
    var attempts = 0
    val flow = flow {
        attempts++
        if (attempts < 3) throw RuntimeException("Fail")
        emit("Success")
    }.retry(3) { e ->
        delay(1000)  // 1 Sekunde Wartezeit
        true
    }
    
    // Vor dem Fix: würde 2+ Sekunden dauern
    // Mit virtual time: instant
    val result = flow.first()
    assertEquals("Success", result)
}
```

### Mocking von Flows

```kotlin
class MockOBDDataSource : OBDDataSource {
    
    private val _readings = MutableSharedFlow<OBDReading>(
        replay = 1,
        extraBufferCapacity = 64
    )
    
    override val readings: Flow<OBDReading> = _readings.asSharedFlow()
    
    fun emitReading(reading: OBDReading) {
        _readings.tryEmit(reading)
    }
    
    fun complete() {
        _readings.emit(reading)
    }
}

class OBDViewModelTest {
    
    private lateinit var mockSource: MockOBDDataSource
    
    @Before
    fun setup() {
        mockSource = MockOBDDataSource()
    }
    
    @Test
    fun `viewmodel processes readings`() = runTest {
        // Arrange
        val viewModel = OBDViewModel(mockSource)
        
        // Act
        mockSource.emitReading(OBDReading(PID.RPM, 3000f, 0, "RPM"))
        testScheduler.runCurrent()
        
        // Assert
        assertEquals(3000, viewModel.uiState.value.rpm)
    }
}
```

---

## 8. Structured Concurrency Best Practices

### Grundprinzip: Scopes statt GlobalScope

```kotlin
// FALSCH: GlobalScope ist Anti-Pattern
class BadOBDManager {
    fun startPolling() {
        GlobalScope.launch(Dispatchers.IO) {  // VERMEIDEN
            while (isActive) {
                val data = readOBD()
                emit(data)
            }
        }
    }
}

// RICHTIG: ViewModelScope oder eigener CoroutineScope
class GoodOBDManager(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private var pollingJob: Job? = null
    
    fun startPolling() {
        pollingJob = scope.launch {
            while (isActive) {
                val data = readOBD()
                emit(data)
            }
        }
    }
    
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
    
    // Oder mit Lifecycle
    fun startPollingWithLifecycle(viewModel: ViewModel) {
        viewModel.viewModelScope.launch {
            while (isActive) {
                val data = readOBD()
                emit(data)
            }
        }
    }
}
```

### SupervisorScope für unabhängige Tasks

```kotlin
class OBDSessionManager(
    private val parentScope: CoroutineScope
) {
    
    private val supervisor = SupervisorJob()
    private val sessionScope = CoroutineScope(
        parentScope.coroutineContext + supervisor
    )
    
    fun startMultipleSensors() {
        // Diese Tasks sind unabhängig - einer darf den anderen nicht killen
        sessionScope.launch {
            pollRPM()
        }
        
        sessionScope.launch {
            pollSpeed()
        }
        
        sessionScope.launch {
            pollFuel()
        }
    }
    
    fun stopAll() {
        supervisor.cancel()  // Alle Kinder werden gecancelt
    }
}
```

### Exception Propagation verstehen

```kotlin
// Wenn ein Child-Coroutine fehlschlägt, werden alle Geschwister gecancelt
// (Standard-Job-Verhalten)

val parentJob = scope.launch {
    launch { throw RuntimeException("Child 1 failed") }
    launch { /* Child 2 - wird auch gecancelt */ }
}

// Mit Supervisor: Children können unabhängig fehlschlagen
val supervisorJob = SupervisorJob()
val supervisorScope = CoroutineScope(scope.coroutineContext + supervisorJob)

supervisorScope.launch { throw RuntimeException("Child 1 failed") }
supervisorScope.launch { /* Child 2 - läuft weiter */ }
```

### withContext für kurze Änderungen

```kotlin
suspend fun expensiveOperation(): Result<Data> = withContext(Dispatchers.IO) {
    try {
        Result.success(doWork())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Oder mit Timeout
suspend fun withTimeout(): Data? = withTimeoutOrNull(5000) {
    doWork()
}
```

### Job Lifecycle Management

```kotlin
class OBDPollingManager {
    
    private var pollingJob: Job? = null
    private val _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling.asStateFlow()
    
    fun startPolling(scope: CoroutineScope) {
        if (pollingJob?.isActive == true) return
        
        pollingJob = scope.launch {
            _isPolling.value = true
            try {
                while (isActive) {
                    pollOnce()
                    delay(POLL_INTERVAL)
                }
            } finally {
                _isPolling.value = false
            }
        }
    }
    
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
```

---

## 9. channelFlow vs flowOf für dynamische Streams

### flowOf - Für statische Streams

```kotlin
// Statische, unveränderliche Streams
val numbers = flowOf(1, 2, 3, 4, 5)

val readings = flowOf(
    OBDReading(PID.RPM, 3000f, time, "RPM"),
    OBDReading(PID.SPEED, 60f, time, "km/h")
)

// Transformation
flowOf("41 0C", "41 0D")
    .map { parseOBDResponse(it) }
```

### channelFlow - Für dynamische, konkurrierde Streams

```kotlin
// Bluetooth-Datenstrom - muss von mehreren Quellen füllen
fun bluetoothReadingsFlow(): Flow<OBDReading> = channelFlow {
    // Diese läuft in einem separaten Coroutine
    val readerJob = launch {
        while (isActive) {
            val reading = readFromBluetooth()
            send(reading)  // Nicht blockierend
        }
    }
    
    val monitorJob = launch {
        while (isActive) {
            send(getConnectionStatus())
            delay(1000)
        }
    }
    
    // Beide Jobs teilen den Channel
    awaitAll(readerJob, monitorJob)
}.flowOn(Dispatchers.IO)

// Parallele Producer
fun multiSensorFlow(): Flow<SensorData> = channelFlow {
    // Mehrere Sensoren werden parallel gelesen
    launch {
        while (isActive) {
            send(readAccelerometer())
        }
    }
    
    launch {
        while (isActive) {
            send(readGyroscope())
        }
    }
    
    launch {
        while (isActive) {
            send(readGPS())
        }
    }
}
```

### flow { } vs channelFlow { }

```kotlin
// flow: Sequential, kann nicht von mehreren Coroutinen füllen
fun sequentialFlow(): Flow<Int> = flow {
    repeat(5) {
        emit(it)  // Nur hier im selben Thread
    }
}

// channelFlow: Concurrent, mehrere Coroutinen können senden
fun concurrentFlow(): Flow<Int> = channelFlow {
    launch {
        repeat(3) { send(it) }
    }
    launch {
        repeat(3) { send(it + 100) }
    }
}
// Output: nicht-deterministisch, z.B. 0, 100, 1, 101, 2, 102
```

### Praktische Entscheidungsmatrix

| Scenario | Builder | Reason |
|----------|---------|--------|
| Festes Array | `flowOf()` | Einfach, performant |
| Berechnete Werte | `flow {}` | Sequential, einfach |
| Bluetooth-Stream | `channelFlow()` | Multi-producer |
| WebSocket | `channelFlow()` | Connection-Management |
| Room DB Changes | `flow {}` | Single-source |
| Multiple Sensors | `channelFlow()` | Concurrent reads |

### OBD-spezifisches Beispiel

```kotlin
// Flow für statische Konfiguration
val supportedPIDs = flowOf(PID.RPM, PID.SPEED, PID.THROTTLE, PID.ENGINE_LOAD)

// Flow für dynamischen Datenstrom
fun obdDataStream(): Flow<OBDReading> = channelFlow {
    // Connection-Handler
    val connectionJob = launch {
        observeConnection().collect { status ->
            send(OBDReading.system(status))
        }
    }
    
    // Polling-Loop
    val pollingJob = launch {
        for (pid in activePIDs) {
            try {
                val value = readOBD(pid)
                send(OBDReading.data(pid, value))
            } catch (e: Exception) {
                send(OBDReading.error(pid, e))
            }
            delay(getPollInterval(pid))
        }
    }
    
    awaitAll(connectionJob, pollingJob)
}.buffer(16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
```

---

## 10. Buffer- und Conflation-Optionen

### Buffer-Operator

```kotlin
// Buffer mit 64 Elementen
val bufferedFlow = sourceFlow.buffer(64)

// Buffer mit Conflation
val conflatedBuffer = sourceFlow.buffer(
    capacity = 64,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

// Buffer auf einem anderen Dispatcher
val parallelFlow = sourceFlow
    .buffer(16)  // Buffer für Parallelisierung
    .map { processExpensive(it) }  // Parallel verarbeitet
    .flowOn(Dispatchers.Default)
```

### Conflation

```kotlin
// conflate() - Äquivalent zu buffer mit DROP_OLDEST
val conflated = flow
    .conflate()  // Emitter läuft durch, nur neuester wird verarbeitet
    .collect { item ->
        // consumer
    }

// Unterschied zu buffer:
flow
    .buffer(0)  // DROP_OLDEST ohne extra Buffer
    .collect { }

flow
    .conflate()  // Identisch
    .collect { }
```

### collectLatest

```kotlin
// CollectLatest - Bricht laufende Verarbeitung ab
flow
    .collectLatest { item ->  // Cancel previous if still running
        process(item)  // Neue Items ersetzen alte
    }

// Praktisch für UI-Updates:
obdDataFlow
    .collectLatest { data ->
        // UI wird nur mit dem neuesten Update
        updateDashboard(data)
    }
```

### Benchmark-Vergleich

```kotlin
// Verschiedene Backpressure-Strategien
sealed class Strategy {
    object Suspend : Strategy()
    object Buffer64DropOldest : Strategy()
    object Buffer64DropLatest : Strategy()
    object Conflate : Strategy()
    object CollectLatest : Strategy()
}

fun benchmark(flow: Flow<Int>, strategy: Strategy): Flow<Int> = when (strategy) {
    Strategy.Suspend -> flow
    Strategy.Buffer64DropOldest -> flow.buffer(64, DROP_OLDEST)
    Strategy.Buffer64DropLatest -> flow.buffer(64, DROP_LATEST)
    Strategy.Conflate -> flow.conflate()
    Strategy.CollectLatest -> flow
}
```

### OBD-spezifische Buffer-Konfiguration

```kotlin
class OBDBufferConfig {
    
    // Buffer für verschiedene Datenraten
    enum class DataRate {
        SLOW,    // 1Hz - z.B. Fuel Level
        MEDIUM,  // 10Hz - z.B. RPM
        FAST     // 50Hz+ - z.B. Acceleration
    }
    
    fun getBufferSize(rate: DataRate): Int = when (rate) {
        DataRate.SLOW -> 8
        DataRate.MEDIUM -> 32
        DataRate.FAST -> 128
    }
    
    // Empfohlene Konfiguration für OBD
    companion object {
        const val DEFAULT_RPM_BUFFER = 64
        const val DEFAULT_SPEED_BUFFER = 32
        const val DEFAULT_DIAGNOSTIC_BUFFER = 8
        
        val DEFAULT_BUFFER_OVERFLOW = BufferOverflow.DROP_OLDEST
    }
}
```

---

## Praktische Beispiele für OBD-Anwendungen

### Beispiel 1: OBD-Data-Stream Verarbeitung

```kotlin
class OBDDataStreamProcessor(
    private val bluetoothService: BluetoothService
) {
    
    private val _sessionData = MutableStateFlow(SessionData())
    val sessionData: StateFlow<SessionData> = _sessionData.asStateFlow()
    
    val rpmFlow: Flow<Int> = bluetoothService.responses
        .filter { it.startsWith("41 0C") }
        .map { parseRPM(it) }
        .conflate()
        .distinctUntilChanged()
    
    val speedFlow: Flow<Int> = bluetoothService.responses
        .filter { it.startsWith("41 0D") }
        .map { parseSpeed(it) }
        .conflate()
    
    // Kombiniert alle relevanten OBD-Daten
    val combinedData: Flow<OBDDashboardData> = combine(
        rpmFlow,
        speedFlow,
        bluetoothService.connectionStatus
    ) { rpm, speed, status ->
        OBDDashboardData(rpm, speed, status)
    }
    
    private fun parseRPM(response: String): Int {
        val bytes = response.split(" ").takeLast(2)
        return ((bytes[0].toInt(16) * 256) + bytes[1].toInt(16)) / 4
    }
    
    private fun parseSpeed(response: String): Int {
        return response.split(" ").last().toInt(16)
    }
}
```

### Beispiel 2: UI-State Management mit StateFlow

```kotlin
data class DashboardUiState(
    val rpm: Int = 0,
    val speed: Int = 0,
    val gear: String = "N",
    val fuelLevel: Float = 0f,
    val coolantTemp: Int = 0,
    val isConnected: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val processor: OBDDataStreamProcessor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()
    
    init {
        observeOBDData()
    }
    
    private fun observeOBDData() {
        viewModelScope.launch {
            combine(
                processor.rpmFlow,
                processor.speedFlow,
                processor.sessionData
            ) { rpm, speed, session ->
                DashboardUiState(
                    rpm = rpm,
                    speed = speed,
                    gear = calculateGear(speed, rpm),
                    fuelLevel = session.fuelLevel,
                    coolantTemp = session.coolantTemp,
                    isConnected = session.isConnected
                )
            }.catch { e ->
                _events.emit(DashboardEvent.ShowError(e.message ?: "Unknown error"))
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    private fun calculateGear(speed: Int, rpm: Int): String {
        if (speed == 0) return "N"
        val ratio = (rpm.toFloat() / speed.coerceAtLeast(1))
        return when {
            ratio > 100 -> "1"
            ratio > 60 -> "2"
            ratio > 40 -> "3"
            ratio > 30 -> "4"
            ratio > 20 -> "5"
            ratio > 15 -> "6"
            else -> "?"
        }
    }
}
```

### Beispiel 3: Error Handling

```kotlin
sealed class OBDError {
    class ConnectionLost(val reason: String) : OBDError()
    class CommandTimeout(val command: String) : OBDError()
    class InvalidResponse(val raw: String) : OBDError()
    class ProtocolError(val code: Int) : OBDError()
}

class RobustOBDManager(
    private val bluetoothService: BluetoothService
) {
    
    private val _connectionState = MutableStateFlow<ConnectionState>(
        ConnectionState.Disconnected
    )
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    fun createResilientFlow(): Flow<Result<OBDReading>> = flow {
        while (isActive) {
            try {
                val response = bluetoothService.sendCommand(GET_RPM)
                val reading = parseResponse(response)
                emit(Result.success(reading))
            } catch (e: TimeoutException) {
                emit(Result.failure(OBDError.CommandTimeout(GET_RPM)))
            } catch (e: IOException) {
                emit(Result.failure(OBDError.ConnectionLost(e.message ?: "")))
                throw e  // Re-throw to trigger retry
            }
            delay(POLL_INTERVAL)
        }
    }.retry(3) { e ->
        if (e is IOException) {
            _connectionState.value = ConnectionState.Reconnecting
            delay(1000)
            true
        } else false
    }.catch { e ->
        _connectionState.value = ConnectionState.Error(e.message ?: "Unknown")
    }.retryWhen { _, attempt ->
        if (attempt < 3) {
            delay(1000L * (attempt + 1))  // Exponential backoff
            true
        } else false
    }
}
```

### Beispiel 4: Performance-Optimierung

```kotlin
class OptimizedOBDProcessor {
    
    private val scheduler = LazyThreadPoolScheduler(
        name = "OBD-IO",
        corePoolSize = 2,
        maxPoolSize = 4
    )
    
    private val ioDispatcher = scheduler.asCoroutineDispatcher()
    
    // Optimiert: Lazy Evaluation, Conflation
    fun createOptimizedFlow(rawData: Flow<String>): Flow<OBDData> = rawData
        .flowOn(ioDispatcher)  // I/O auf separatem Thread
        .map { parseResponse(it) }  // Parsing
        .filter { isValid(it) }
        .conflate()  // UI nicht überfordern
        .distinctUntilChanged { a, b -> 
            a.pid == b.pid && a.value.toInt() == b.value.toInt() 
        }
        .onEach { update ->  // Throttling
            // UI update happens on Main
        }
    
    // Caching für teure Berechnungen
    private val calculationCache = MutableSharedFlow<CalculatedData>(
        replay = 1,
        extraBufferCapacity = 1
    )
    
    val calculatedData: Flow<CalculatedData> = combine(
        _rpmFlow,
        _speedFlow
    ) { rpm, speed ->
        calculateAdvancedMetrics(rpm, speed)
    }.distinctUntilChanged()
     .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )
}
```

---

## Quellen und Links

### Offizielle Dokumentation

1. **Kotlin Flows Offizielle Dokumentation**
   - https://kotlinlang.org/docs/flow.html
   - Umfassende Referenz zu allen Flow-Konzepten

2. **Kotlin Channels**
   - https://kotlinlang.org/docs/channels.html
   - Für multi-producer Szenarien

3. **Kotlin Coroutines Exception Handling**
   - https://kotlinlang.org/docs/exception-handling.html
   - Exception Propagation und Handler

4. **Shared Mutable State and Concurrency**
   - https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html
   - Thread-Safety und Synchronisation

5. **Kotlinx Coroutines Test**
   - https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
   - Offizielle Testing-Bibliothek

### Android-spezifische Ressourcen

6. **Android Coroutines Best Practices**
   - https://developer.android.com/kotlin/coroutines/coroutines-best-practices
   - Empfehlungen von Google für Android

7. **StateFlow und SharedFlow**
   - https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
   - Android-spezifische Dokumentation

8. **UI-Layer State Management**
   - https://developer.android.com/topic/architecture/ui-layer/stateholders
   - StateFlow in MVVM-Architektur

### Lernressourcen

9. **Kotlin Academy**
   - https://kt.academy/
   - Articles und Tutorials von Marcin Moskała

10. **Kotlin Coroutines von Roman Elizarov**
    - https://elizarov.medium.com/
    - Blog des Kotlin Coroutines Lead Developers

### GitHub Repository

11. **Kotlinx Coroutines**
    - https://github.com/Kotlin/kotlinx.coroutines
    - Quellcode und aktuelle Updates

---

## Zusammenfassung: Best Practices für OBD-Anwendungen

### Empfohlene Architektur

```
┌─────────────────────────────────────────────────────────┐
│                    Bluetooth I/O                          │
│                 (Dispatchers.IO)                          │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│              channelFlow / flow                           │
│         mit flowOn(OBDDispatchers.IO)                    │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│         Buffer(64, DROP_OLDEST)                          │
│              Backpressure-Handling                        │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│     map / filter / distinctUntilChanged                  │
│         (Dispatchers.Default)                            │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│        conflate() / collectLatest()                      │
│            UI-optimierung                                │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│              StateFlow / SharedFlow                       │
│                (Dispatchers.Main)                         │
└─────────────────────────────────────────────────────────┘
```

### Checkliste

- [ ] StateFlow für UI-State verwenden
- [ ] SharedFlow für Events und Datenströme
- [ ] Buffer mit DROP_OLDEST für schnelle Producer
- [ ] conflate() für UI-Throttling
- [ ] Dispatchers.IO für Bluetooth-Operationen
- [ ] SupervisorScope für unabhängige Tasks
- [ ] Exception Handling mit catch() Operator
- [ ] Testing mit kotlinx-coroutines-test
- [ ] Structured Concurrency mit viewModelScope
- [ ] Flow-Operatoren kombinieren für komplexe Verarbeitung
