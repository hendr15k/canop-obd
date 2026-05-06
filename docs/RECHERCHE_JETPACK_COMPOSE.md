# Jetpack Compose Best Practices für Android OBD-II Apps

Eine umfassende Sammlung von Best Practices für die Entwicklung einer Auto-Dashboard-App mit Jetpack Compose.

## Inhaltsverzeichnis

1. [Material 3 Design Guidelines für Auto-Dashboards](#1-material-3-design-guidelines-für-auto-dashboards)
2. [Compose Performance Optimierung für Echtzeit-Daten](#2-compose-performance-optimierung-für-echtzeit-daten)
3. [State Management mit StateFlow/SharedFlow](#3-state-management-mit-stateflowsharedflow)
4. [LazyColumn für dynamische Listen](#4-lazycolumn-für-dynamische-listen)
5. [Canvas/Zeichnung für animierte Tacho/Drehzahlmesser](#5-canvaszeichnungen-für-animierte-tachodrehzahlmesser)
6. [Navigation Compose Best Practices](#6-navigation-compose-best-practices)
7. [ViewModel SavedStateHandle](#7-viewmodel-savedstatehandle)
8. [Dependency Injection mit Hilt](#8-dependency-injection-mit-hilt)
9. [Compose Testing Best Practices](#9-compose-testing-best-practices)
10. [Animationsperformance für reibungslose UI](#10-animationsperformance-für-reibungslose-ui)

---

## 1. Material 3 Design Guidelines für Auto-Dashboards

### Grundprinzipien

Material 3 (Material You) bietet dynamische Farbschemata und adaptive Komponenten, die sich perfekt für Auto-Dashboards eignen:

**Quelle:** [Now in Android - Material 3 Case Study](https://github.com/android/nowinandroid)
**Quelle:** [Material 3 Guidelines](https://m3.material.io/)

### Theme-Setup für Auto-Dashboard

```kotlin
// ui/theme/Theme.kt
@Composable
fun ObdDashboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Spezielle Farbpalette für Auto-Dashboard

```kotlin
// Auto-spezifische Farben für Tacho/Drehzahlmesser
object DashboardColors {
    val SpeedNormal = Color(0xFF4CAF50)      // Grün - normale Geschwindigkeit
    val SpeedWarning = Color(0xFFFF9800)     // Orange - Warnung
    val SpeedDanger = Color(0xFFF44336)      // Rot - Gefahr
    val RpmNormal = Color(0xFF2196F3)         // Blau - normaler Drehzahlbereich
    val RpmHigh = Color(0xFFFF5722)          // Orange-Rot - hoher Drehzahlbereich
    val FuelLow = Color(0xFFF44336)          // Rot - niedriger Kraftstoffstand
    val FuelMedium = Color(0xFFFF9800)       // Orange - mittlerer Stand
    val FuelGood = Color(0xFF4CAF50)         // Grün - guter Stand
    val CoolantHot = Color(0xFFF44336)       // Rot - heiße Kühlflüssigkeit
    val CoolantNormal = Color(0xFF4CAF50)    // Grün - normale Temperatur
}
```

### Barrierefreiheit im Auto-Kontext

```kotlin
// Große Touch-Targets für Bedienung während der Fahrt
@Composable
fun DashboardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .minimumTouchTargetSize(56.dp)  // Minimum für Touch
            .heightIn(min = 56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = "Start Scan",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
```

**Performance-Tipps:**
- Verwende `dynamicColorScheme` nur wenn nötig (kann bei häufigen Updates teuer sein)
- Definiere konstante Farben für sicherheitsrelevante Elemente (Tacho-Ausschläge)
- Nutze `ColorScheme.copy()` für Varianten statt neue Farben zu erstellen

**Architektur-Empfehlungen:**
- Extrahiere Farblogik in ein zentrales `DashboardColors` Objekt
- Verwende Compose Preview für Theme-Validierung

**Quellen:**
- https://github.com/android/nowinandroid (Now in Android - Material 3 Case Study)
- https://m3.material.io/
- https://developer.android.com/jetpack/compose/material3

---

## 2. Compose Performance Optimierung für Echtzeit-Daten

### Das Problem bei OBD-II: Hohe Update-Frequenz

OBD-II Daten können mit 1-60 Hz aktualisiert werden. Unoptimiertes Compose kann zu Leistungsproblemen führen.

### Remember und RememberSaveable richtig nutzen

```kotlin
// SCHLECHT: Neue Instanz bei jedem Recompose
@Composable
fun Speedometer(data: ObdData) {
    val animator = remember { Animator() }  // Immer neue Instanz!
}

// GUT: Nur bei erstem Composition erstellt
@Composable
fun Speedometer(data: ObdData) {
    val animator = remember {
        Animator().also { /* Initialisierung */ }
    }
    
    // Für animationsrelevante Werte
    val animatedSpeed by animateFloatAsState(
        targetValue = data.speed,
        animationSpec = tween(durationMillis = 150),
        label = "speed"
    )
}
```

### Stable Klassen für Compose

```kotlin
// Markiere Klassen als @Stable für bessere Skipping-Logik
@Stable
interface ObdData {
    val speed: Float
    val rpm: Int
    val fuelLevel: Float
    val coolantTemp: Int
}

// Data Classes sind automatisch @Stable wenn alle Properties stable sind
@Stable
data class VehicleState(
    val speed: Float,
    val rpm: Int,
    val gear: Int,
    val timestamp: Long
)
```

### Lambda-Stabilität bei Callbacks

```kotlin
// SCHLECHT: Neue Lambda bei jedem Recompose
@Composable
fun Dashboard(onDataUpdate: (ObdData) -> Unit) {
    Button(onClick = { onDataUpdate(ObdData()) }) // Neue Lambda!
}

// GUT: Callback als remembered Lambda
@Composable
fun Dashboard(
    viewModel: DashboardViewModel,
    onNavigate: () -> Unit
) {
    val onButtonClick = remember(viewModel) {
        { viewModel.triggerScan() }
    }
    
    Button(onClick = onButtonClick) {
        Text("Scan")
    }
}
```

### Baseline Profiles für Startup-Performance

```kotlin
// baseline-prof.txt - muss für release builds generiert werden
H;com.example.obddashboard.MainActivity
H;com.example.obddashboard.ui.DashboardScreen
H;com.example.obddashboard.data.repository.ObdRepository
```

**Performance-Tipps:**
- Verwende `derivedStateOf` um Berechnungen zu cachen
- Nutze `snapshotFlow` für nicht-Compose Code der mit Compose interagiert
- Vermeide unnötige Recompositions mit `key {}` Blöcken
- Aktiviere Compose Compiler Metrics im Build:

```gradle
// build.gradle.kts
composeOptions {
    kotlinCompilerOptions {
        freeCompilerArgs.addAll(
            "-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=/build/compose-metrics"
        )
    }
}
```

**Architektur-Empfehlungen:**
- Folgende der "Now in Android" Architektur mit UI/Domain/Data Layer Trennung
- Nutze `remember` für teure Berechnungen
- Definiere stabile Interfaces für alle Datenmodelle

**Quellen:**
- https://github.com/android/compose-samples
- https://developer.android.com/jetpack/compose/stability
- https://medium.com/androiddevelopers/jetpack-compose-stability-explained-79c10db270c8

---

## 3. State Management mit StateFlow/SharedFlow

### StateFlow für UI-State

StateFlow ist ideal für UI-State, da es immer einen aktuellen Wert hat und lazy collectors unterstützt.

```kotlin
// ViewModel mit StateFlow
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val obdRepository: ObdRepository
) : ViewModel() {
    
    // Privater mutable StateFlow
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    // Für Fehler-Events
    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()
    
    init {
        observeObdData()
    }
    
    private fun observeObdData() {
        viewModelScope.launch {
            obdRepository.getLiveData().collect { data ->
                _uiState.update { state ->
                    state.copy(
                        speed = data.speed,
                        rpm = data.rpm,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun onScanButtonClicked() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.StartScanning)
        }
    }
}

// UI State Data Class
@Stable
data class DashboardUiState(
    val speed: Float = 0f,
    val rpm: Int = 0,
    val fuelLevel: Float = 0f,
    val coolantTemp: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

// Events für Side-Effects
sealed interface DashboardEvent {
    data object StartScanning : DashboardEvent
    data class ShowError(val message: String) : DashboardEvent
    data object NavigateToSettings : DashboardEvent
}
```

### SharedFlow für Events und One-Time-Effects

```kotlin
// In der Composable
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.StartScanning -> { /* Navigation/Action */ }
                is DashboardEvent.ShowError -> { /* Snackbar anzeigen */ }
                is DashboardEvent.NavigateToSettings -> { /* Navigation */ }
            }
        }
    }
    
    // UI Content...
}
```

### Conflation für hochfrequente Updates

```kotlin
// Für OBD-Daten mit hoher Frequenz:
// Option 1: Conflation aktivieren
private val _obdData = MutableStateFlow(ObdData())
val obdData: StateFlow<ObdData> = _obdData

fun updateSpeed(speed: Float) {
    // Schnelle Updates überspringen langsame Collector
    _obdData.value = _obdData.value.copy(speed = speed)
}

// Option 2: Debouncing mit channel
private val updateChannel = Channel<ObdData>(Channel.CONFLATED)
val obdData = updateChannel.receiveAsFlow().stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ObdData()
)

fun updateSpeed(speed: Float) {
    updateChannel.trySend(ObdData(speed = speed))
}
```

**Performance-Tipps:**
- Nutze `collectAsStateWithLifecycle()` statt `collectAsState()` für lifecycle-aware Collection
- Verwende `MutableStateFlow.update {}` für atomare Updates
- Konfiguriere `SharingStarted.WhileSubscribed(5000)` für schnelles Stoppen bei Inaktivität
- Conflate hochfrequente Daten (z.B. Geschwindigkeit) um UI-Überlastung zu vermeiden

**Architektur-Empfehlungen:**
- Trenne UI-State (StateFlow) von Events (SharedFlow)
- Verwende `sealed interface` für typsichere Events
- Stelle `initialValue` bei StateFlow immer bereit
- Nutze `stateIn` mit `WhileSubscribed` für effizientes State-Sharing

**Quellen:**
- https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/
- https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-shared-flow/
- https://developer.android.com/kotlin/coroutines/coroutines-best-practices

---

## 4. LazyColumn für dynamische Listen

### Grundlegendes LazyColumn mit OBD-Daten

```kotlin
@Composable
fun ObdParameterList(
    parameters: List<ObdParameter>,
    onParameterClick: (ObdParameter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = parameters,
            key = { it.pid }  // Stabiles Key für effizientes Update
        ) { parameter ->
            ObdParameterItem(
                parameter = parameter,
                onClick = { onParameterClick(parameter) }
            )
        }
    }
}

@Composable
fun ObdParameterItem(
    parameter: ObdParameter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = parameter.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = parameter.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = parameter.formattedValue,
                style = MaterialTheme.typography.headlineSmall,
                color = parameter.statusColor
            )
        }
    }
}
```

### LazyColumn mit Animationen

```kotlin
// Animiertes Hinzufügen/Entfernen von Items
LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(
        items = parameters,
        key = { it.pid },
        enterTransition = { fadeIn() + slideInVertically() },
        exitTransition = { fadeOut() + slideOutVertically() }
    ) { parameter ->
        ObdParameterItem(parameter = parameter)
    }
}
```

### Performance-Optimierung mit custom LazyListState

```kotlin
// Für große Listen mit vielen Updates
@Composable
fun OptimizedObdList(
    parameters: SnapshotStateList<ObdParameter>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Performance: Nur sichtbare Items werden recomposed
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        // Für große Listen: keys und contentType nutzen
        items(
            items = parameters,
            key = { it.pid },
            contentType = { it.category }  // Helps Compose reuse
        ) { parameter ->
            // Component wird nur neu gezeichnet wenn nötig
        }
    }
}
```

### Header und Sticky Items

```kotlin
LazyColumn(
    modifier = modifier.fillMaxSize(),
    stickyHeader = {
        Header(title = "Live Data")
    }
) {
    // Content...
}
```

**Performance-Tipps:**
- Verwende immer `key = { ... }` für Listen mit Updates
- Nutze `contentType` für besseres Recycling
- Vermeide Lambdas in `items()` wenn möglich - nutze `remember` für Callbacks
- Nutze `derivedStateOf` für scroll-bezogene Berechnungen
- Für Echtzeit-Updates: Überlege ob wirklich alle Items aktualisiert werden müssen

**Architektur-Empfehlungen:**
- Nutze `SnapshotStateList` statt `List` für reaktive Listen
- Extrahiere Item-Composables in eigene Dateien
- Definiere klare Trennung zwischen List-Container und List-Item

**Quellen:**
- https://developer.android.com/jetpack/compose/lists
- https://developer.android.com/jetpack/compose/lists/basics

---

## 5. Canvas/Zeichnungen für animierte Tacho/Drehzahlmesser

### Grundlegendes Speedometer mit Canvas

```kotlin
@Composable
fun Speedometer(
    speed: Float,
    maxSpeed: Float = 260f,
    modifier: Modifier = Modifier
) {
    val sweepAngle = 240f  // Grad im Bogen
    val startAngle = 150f // Startposition (unten links)
    
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val strokeWidth = size.minDimension * 0.08f
        val radius = (size.minDimension - strokeWidth) / 2
        
        // Hintergrund-Bogen
        drawArc(
            color = Color.Gray.copy(alpha = 0.3f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(radius * 2, radius * 2)
        )
        
        // Geschwindigkeits-Bogen
        val speedAngle = (speed / maxSpeed) * sweepAngle
        val color = when {
            speed > 200 -> DashboardColors.SpeedDanger
            speed > 180 -> DashboardColors.SpeedWarning
            else -> DashboardColors.SpeedNormal
        }
        
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = speedAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(radius * 2, radius * 2)
        )
    }
}
```

### Animierter Tacho mit AnimatedFloat

```kotlin
@Composable
fun AnimatedSpeedometer(
    targetSpeed: Float,
    maxSpeed: Float = 260f,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = targetSpeed,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "speed"
    )
    
    val speedPercent = (animatedSpeed / maxSpeed).coerceIn(0f, 1f)
    
    Canvas(modifier = modifier.aspectRatio(1f)) {
        // Zeichne Tacho...
        drawSpeedNeedle(animatedSpeed, maxSpeed)
    }
}

private fun DrawScope.drawSpeedNeedle(speed: Float, maxSpeed: Float) {
    val angle = 150f + (speed / maxSpeed) * 240f
    val angleRad = Math.toRadians(angle.toDouble())
    
    val centerX = size.width / 2
    val centerY = size.height / 2
    val needleLength = size.minDimension * 0.35f
    
    val endX = centerX + (needleLength * cos(angleRad)).toFloat()
    val endY = centerY + (needleLength * sin(angleRad)).toFloat()
    
    drawLine(
        color = Color.Red,
        start = Offset(centerX, centerY),
        end = Offset(endX, endY),
        strokeWidth = 8f,
        cap = StrokeCap.Round
    )
}
```

### Fortgeschritten: Drehzahlmesser mit Tick-Marks

```kotlin
@Composable
fun RpmGauge(
    rpm: Int,
    maxRpm: Int = 8000,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.85f
        val strokeWidth = size.minDimension * 0.06f
        
        // Tick-Marks zeichnen
        val tickCount = 16
        val anglePerTick = 240f / tickCount
        val startAngle = 150f
        
        for (i in 0..tickCount) {
            val angle = Math.toRadians((startAngle + i * anglePerTick).toDouble())
            val isMajor = i % 2 == 0
            val tickLength = if (isMajor) 30f else 15f
            
            val innerRadius = radius - tickLength
            val outerRadius = radius
            
            val startX = center.x + (innerRadius * cos(angle)).toFloat()
            val startY = center.y + (innerRadius * sin(angle)).toFloat()
            val endX = center.x + (outerRadius * cos(angle)).toFloat()
            val endY = center.y + (outerRadius * sin(angle)).toFloat()
            
            val tickColor = when {
                i >= 12 -> DashboardColors.RpmHigh
                else -> DashboardColors.RpmNormal
            }
            
            drawLine(
                color = tickColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isMajor) 4f else 2f,
                cap = StrokeCap.Round
            )
        }
        
        // Zeiger zeichnen
        val currentAngle = startAngle + (rpm.toFloat() / maxRpm) * 240f
        val currentRad = Math.toRadians(currentAngle.toDouble())
        val needleLength = radius * 0.7f
        
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(
                center.x + (needleLength * cos(currentRad)).toFloat(),
                center.y + (needleLength * sin(currentRad)).toFloat()
            ),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
        
        // Center Circle
        drawCircle(
            color = Color.White,
            radius = 15f,
            center = center
        )
    }
}
```

### Performance mit Canvas

```kotlin
@Composable
fun HighPerformanceGauge(
    rpm: Int,
    modifier: Modifier = Modifier
) {
    // Remember berechnet teure Werte nur einmal
    val path = remember { Path() }
    
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .drawWithCache {
                // Teure Berechnungen hier cachen
                onDrawBehind {
                    drawGaugeBackground()
                }
            }
    ) {
        // Schnelle Updates hier
        drawNeedle(rpm)
    }
}
```

**Performance-Tipps:**
- Nutze `remember` für teure Path-Berechnungen
- Verwende `drawWithCache` für statische Elemente
- Begrenzte Recomposition durch stabile Parameter
- Nutze `GraphicsLayer` für komplexe Transformationen
- Überlege ob du Canvas durch Semi-Transparent-Layer ersetzen kannst

**Architektur-Empfehlungen:**
- Extrahiere Gauge-spezifische Berechnungen in Utils
- Nutze sealed classes für verschiedene Gauge-Typen
- Definiere Default-Werte und Constraints zentral

**Quellen:**
- https://github.com/android/compose-samples (JetLagged - Custom Graphics)
- https://developer.android.com/jetpack/compose/graphics/draw/modifier

---

## 6. Navigation Compose Best Practices

### Type-Safe Navigation mit Navigation Compose

```kotlin
// Navigation Destinations
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object ParameterList : Screen("parameters")
    data object ParameterDetail : Screen("parameter/{pid}") {
        fun createRoute(pid: String) = "parameter/$pid"
    }
    data object Settings : Screen("settings")
    data object Diagnostics : Screen("diagnostics/{dtcCode}") {
        fun createRoute(dtcCode: String) = "diagnostics/$dtcCode"
    }
}

// NavHost Definition
@Composable
fun ObdNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToParameters = {
                    navController.navigate(Screen.ParameterList.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.ParameterList.route) {
            ParameterListScreen(
                onParameterClick = { pid ->
                    navController.navigate(Screen.ParameterDetail.createRoute(pid))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.ParameterDetail.route,
            arguments = listOf(
                navArgument("pid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pid = backStackEntry.arguments?.getString("pid") ?: ""
            ParameterDetailScreen(
                pid = pid,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
```

### Bottom Navigation mit Navigation

```kotlin
@Composable
fun ObdApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") },
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Parameter") },
                    selected = currentRoute == Screen.ParameterList.route,
                    onClick = {
                        navController.navigate(Screen.ParameterList.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        ObdNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
```

### Navigation mit SharedFlow für Events

```kotlin
// In DashboardViewModel
sealed class DashboardEvent {
    data class NavigateTo(val route: String) : DashboardEvent()
}

// In DashboardScreen
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is DashboardEvent.NavigateTo -> {
                    navController.navigate(event.route)
                }
            }
        }
    }
}
```

**Performance-Tipps:**
- Nutze `popUpTo(navController.graph.startDestinationId)` um Backstack zu bereinigen
- Setze `launchSingleTop = true` um keine Duplikate zu erzeugen
- Nutze `restoreState = true` für bessere UX bei Navigation
- Vermeide zu tiefen Backstack für Performance

**Architektur-Empfehlungen:**
- Definiere alle Routes zentral als sealed class
- Nutze separate NavHost composables pro Feature
- Implementiere Deep Linking für wichtige Screens
- Nutze SavedStateHandle für Navigation-State

**Quellen:**
- https://developer.android.com/jetpack/compose/navigation
- https://developer.android.com/guide/navigation/compose/nav-in-compose

---

## 7. ViewModel SavedStateHandle

### SavedStateHandle für Process-Death Recovery

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val obdRepository: ObdRepository
) : ViewModel() {
    
    // Speed aus SavedState wiederherstellen
    private val _speed = MutableStateFlow(
        savedStateHandle.get<Float>("speed") ?: 0f
    )
    val speed: StateFlow<Float> = _speed.asStateFlow()
    
    // RPM speichern
    fun updateRpm(rpm: Int) {
        _rpm.update { rpm }
        savedStateHandle["rpm"] = rpm
    }
    
    // Für Listen
    private val _errorCodes = MutableStateFlow<List<String>>(
        savedStateHandle.get<List<String>>("errorCodes") ?: emptyList()
    )
    
    // Mit SavedStateHandle und Flow
    private val _selectedPid = MutableStateFlow(
        savedStateHandle.getStateFlow("selectedPid", "")
    )
}
```

### Type-Safe SavedStateHandle mit WriteableNavArgs

```kotlin
// Navigation mit SavedState
data class ParameterDetailArgs(
    val pid: String,
    val mode: String = "live"
) {
    fun toSavedStateHandle(): SavedStateHandle {
        return savedStateOf {
            set("pid", pid)
            set("mode", mode)
        }
    }
    
    companion object {
        fun fromSavedStateHandle(handle: SavedStateHandle): ParameterDetailArgs {
            return ParameterDetailArgs(
                pid = handle.get<String>("pid") ?: "",
                mode = handle.get<String>("mode") ?: "live"
            )
        }
    }
}

@HiltViewModel
class ParameterDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val savedStateRegistryOwner: SavedStateRegistryOwner
) : ViewModel() {
    
    private val args = ParameterDetailArgs.fromSavedStateHandle(savedStateHandle)
    
    init {
        loadParameter(args.pid)
    }
}
```

### Automatisches Speichern mit @SaveableForSnapshot

```kotlin
@Stable
@SaveableForSnapshot
data class ObdParameterState(
    val pid: String,
    val value: Float,
    val unit: String,
    val timestamp: Long
)

@HiltViewModel
class ParametersViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // Diese Liste überlebt Process-Death automatisch
    var parameterStates by rememberSavedInstanceStateOf(
        initialValue = listOf<ObdParameterState>()
    ) { mutableStateListOf<ObdParameterState>() }
}
```

**Performance-Tipps:**
- Speichere nur notwendige Daten (keine großen Objekte)
- Nutze `getStateFlow()` für reaktive SavedState-Werte
- Batch-Updates um zu häufiges Speichern zu vermeiden
- Nutze `rememberSavedInstanceStateOf` für Compose-spezifisches State

**Architektur-Empfehlungen:**
- Definiere SavedStateKeys als Konstante für Typsicherheit
- Nutze `produceSaveableState` für komplexe Serialisierung
- Kombiniere mit Repository Pattern für persistente Daten
- Überlege ob DataStore für große/persistente Daten besser geeignet ist

**Quellen:**
- https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
- https://dagger.dev/hilt/view-model

---

## 8. Dependency Injection mit Hilt

### Hilt Setup für OBD-App

```kotlin
// Application Class
@HiltAndroidApp
class ObdApplication : Application()

// Activity Entry Point
@AndroidEntryPoint
class MainActivity : ComponentActivity()

// Fragment Entry Point (falls benötigt)
@AndroidEntryPoint
class DashboardFragment : Fragment()
```

### Repository und Data-Layer DI

```kotlin
// Repository Interface
interface ObdRepository {
    fun getLiveData(): Flow<ObdData>
    suspend fun connect(): Result<Unit>
    suspend fun disconnect()
}

// Repository Implementation
@Singleton
class ObdRepositoryImpl @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val obdConnectionFactory: ObdConnectionFactory
) : ObdRepository {
    
    private var connection: ObdConnection? = null
    
    override fun getLiveData(): Flow<ObdData> = flow {
        // Emitiert kontinuierlich OBD-Daten
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )
}

// Hilt Module
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideObdRepository(
        bluetoothManager: BluetoothManager,
        obdConnectionFactory: ObdConnectionFactory
    ): ObdRepository {
        return ObdRepositoryImpl(bluetoothManager, obdConnectionFactory)
    }
    
    @Provides
    @Singleton
    fun provideBluetoothManager(
        @ApplicationContext context: Context
    ): BluetoothManager {
        return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
}
```

### ViewModel Injection

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val obdRepository: ObdRepository,
    private val savedStateHandle: SavedStateHandle,
    @Assisted private val carId: String  // Assisted Injection
) : ViewModel() {
    
    val uiState: StateFlow<DashboardUiState> = obdRepository.getLiveData()
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )
}
```

### Test-Doubles mit Hilt

```kotlin
// Test Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ObdRepositoryModule::class]
)
abstract class FakeObdRepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindObdRepository(
        fakeRepository: FakeObdRepository
    ): ObdRepository
}

// Fake Implementation
@Singleton
class FakeObdRepository @Inject constructor() : ObdRepository {
    
    private val _data = MutableStateFlow(ObdData())
    
    override fun getLiveData(): Flow<ObdData> = _data.asFlow()
    
    fun updateSpeed(speed: Float) {
        _data.update { it.copy(speed = speed) }
    }
}

// In Test
@HiltAndroidTest
class DashboardViewModelTest {
    
    @Inject
    lateinit var fakeRepository: FakeObdRepository
    
    @Test
    fun `speed updates are reflected in UI state`() = runTest {
        fakeRepository.updateSpeed(120f)
        
        // Verify ViewModel state
    }
}
```

**Performance-Tipps:**
- Nutze `@Singleton` sparsam - teure Dependencies nur wenn nötig
- `@ViewModelScoped` für Dependencies pro ViewModel
- Vermeide zirkuläre Dependencies durch klare Architektur
- Lazy Injection für schwere Dependencies

**Architektur-Empfehlungen:**
- Programmiere gegen Interfaces (Repository Pattern)
- Nutze `@Binds` statt `@Provides` wo möglich (effizienter)
- Strukturiere Module nach Feature/Schicht
- Nutze `@EntryPoint` für nicht-Hilt Klassen

**Quellen:**
- https://dagger.dev/hilt/
- https://dagger.dev/hilt/view-model
- https://developer.android.com/training/dependency-injection/hilt-android

---

## 9. Compose Testing Best Practices

### Unit Tests für ViewModel

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val obdRepository: ObdRepository
) : ViewModel() {
    // ...
}

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class DashboardViewModelTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    @Named("test")
    lateinit var fakeRepository: FakeObdRepository
    
    @Test
    fun `initial state is loading`() = runTest {
        val viewModel = DashboardViewModel(obdRepository)
        
        val initialState = viewModel.uiState.value
        
        assertTrue(initialState.isLoading)
    }
    
    @Test
    fun `speed updates correctly`() = runTest {
        val viewModel = DashboardViewModel(obdRepository)
        
        fakeRepository.updateSpeed(100f)
        
        assertEquals(100f, viewModel.uiState.value.speed)
    }
}
```

### Compose UI Tests

```kotlin
@ComposeUiTest
@Composable
fun SpeedometerDisplaysSpeed() {
    val speed = 120f
    
    composeTestRule.setContent {
        ObdDashboardTheme {
            Speedometer(
                speed = speed,
                maxSpeed = 260f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    composeTestRule
        .onNodeWithText("120")
        .assertIsDisplayed()
}

@ComposeUiTest
fun DashboardShowsLoadingState() {
    composeTestRule.setContent {
        ObdDashboardTheme {
            DashboardScreen(
                viewModel = FakeDashboardViewModel(isLoading = true)
            )
        }
    }
    
    composeTestRule
        .onNodeWithText("Laden...")
        .assertIsDisplayed()
}
```

### Screenshot Tests mit Roborazzi

```kotlin
// In nowinandroid (siehe github.com/android/nowinandroid)
@ScreenshotTest
@RunWith(AndroidJUnit4::class)
class DashboardScreenshotTests {
    
    @Test
    fun dashboard_light_mode() {
        composeTestRule.takeScreenshot(
            comparisonFileName = "dashboard_light"
        )
    }
    
    @Test
    fun dashboard_dark_mode() {
        composeTestRule.takeScreenshot(
            comparisonFileName = "dashboard_dark"
        )
    }
}
```

### Integration Tests

```kotlin
@HiltAndroidTest
class ObdConnectionIntegrationTest {
    
    @Inject
    lateinit var repository: ObdRepository
    
    @Test
    fun `repository connects and emits data`() = runTest {
        val connectionResult = repository.connect()
        
        assertTrue(connectionResult.isSuccess)
        
        repository.getLiveData()
            .take(5)
            .collect { data ->
                assertTrue(data.speed >= 0)
            }
        
        repository.disconnect()
    }
}
```

**Performance-Tipps:**
- Nutze `fakeRepository` statt Mockito für weniger fragile Tests
- Führe UI-Tests auf physischen Geräten oder emuliertem ARM aus
- Nutze `@SmallTest`, `@MediumTest`, `@LargeTest` für Test-Kategorisierung
- Parallele Test-Ausführung wo möglich

**Architektur-Empfehlungen:**
- Teste ViewModels isoliert mit Fake Repositories
- Schreibe UI-Tests für kritische User Flows
- Nutze Screenshot-Tests für Theme-/Design-Regressionen
- Dokumentiere Test-Strategie im Projekt

**Quellen:**
- https://developer.android.com/jetpack/compose/testing
- https://github.com/android/nowinandroid (Testing section)
- https://developer.android.com/training/testing/integration-tests

---

## 10. Animationsperformance für reibungslose UI

### Grundlegende Animationen

```kotlin
@Composable
fun AnimatedSpeedIndicator(
    speed: Float,
    modifier: Modifier = Modifier
) {
    // Animierter Geschwindigkeitswert
    val animatedSpeed by animateFloatAsState(
        targetValue = speed,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "speed"
    )
    
    Text(
        text = animatedSpeed.toInt().toString(),
        style = MaterialTheme.typography.displayLarge
    )
}
```

###drawBehind für Canvas-Animationen

```kotlin
@Composable
fun AnimatedGauge(
    rpm: Int,
    modifier: Modifier = Modifier
) {
    val animatedRpm by animateFloatAsState(
        targetValue = rpm.toFloat(),
        animationSpec = tween(durationMillis = 100),
        label = "rpm"
    )
    
    Canvas(
        modifier = modifier.drawWithCache {
            val backgroundPath = Path().apply {
                addOval(ovalBounds)
            }
            
            onDrawWithContent {
                drawPath(backgroundPath, Color.Gray)
                drawNeedle(animatedRpm)
            }
        }
    ) {
        // Canvas content
    }
}
```

### Animations-Interceptor für Performance-Monitoring

```kotlin
// Debug: Recompositionen tracken
@Composable
fun DebugRecomposition(block: @Composable () -> Unit) {
    if (BuildConfig.DEBUG) {
        var recompositionCount by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                Log.d("ComposePerf", "Recompositions: $recompositionCount")
                recompositionCount = 0
            }
        }
        CompositionLocalProvider(LocalRecomposer countsAsInt {
            recompositionCount++
        }) {
            block()
        }
    } else {
        block()
    }
}
```

### Reibungslose Gauge-Animationen

```kotlin
@Composable
fun SmoothSpeedometer(
    speed: Float,
    modifier: Modifier = Modifier
) {
    // Nutze Float für smoothere Animation
    val animatedSpeed by animateFloatAsState(
        targetValue = speed,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "speed"
    )
    
    // Transform für Rotation
    val rotation by animateFloatAsState(
        targetValue = (animatedSpeed / 260f) * 240f,
        animationSpec = tween(150),
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {
        // Gauge content
    }
}
```

### Infinite Animations für "lebendige" Elemente

```kotlin
@Composable
fun PulsingConnectionIndicator(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    if (isConnected) {
        Box(
            modifier = modifier
                .size(12.dp)
                .graphicsLayer { this.alpha = alpha }
                .background(Color.Green, CircleShape)
        )
    }
}
```

**Performance-Tipps:**
- Nutze `animateFloatAsState` statt manueller Animation
- Bevorzuge `tween` über `spring` für wiederholte Updates (Tacho)
- Verwende `graphicsLayer` für GPU-beschleunigte Transformationen
- Nutze `drawWithCache` für statische Canvas-Elemente
- Begrenzung der Animationsfrequenz für OBD-Daten:
```kotlin
val debouncedSpeed by animateFloatAsState(
    targetValue = speed,
    animationSpec = tween(200), // Nicht zu schnell!
    label = "speed"
)
```

**Architektur-Empfehlungen:**
- Extrahiere Animations-Specs in ein zentrales Objekt
- Nutze `AnimationSpec` Konstanten für Konsistenz
- Definiere Animations-Duration als Ressource
- Teste Animationen auf schwächeren Geräten

**Quellen:**
- https://developer.android.com/jetpack/compose/animation
- https://developer.android.com/jetpack/compose/animation/features

---

## Zusammenfassung: Empfohlene Architektur für OBD-II App

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│  │  Dashboard   │  │  Parameter   │  │  Settings   │       │
│  │   Screen     │  │    List      │  │    Screen    │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                  │                  │               │
│  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼───────┐       │
│  │ ViewModel    │  │ ViewModel    │  │ ViewModel    │       │
│  │ (StateFlow)  │  │ (StateFlow)  │  │ (StateFlow)  │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
└─────────┼──────────────────┼──────────────────┼───────────────┘
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼───────────────┐
│                   Domain Layer                                │
│  ┌─────────────────────────────────────────────────────┐     │
│  │              Use Cases / Interactors                 │     │
│  │   - ConnectToVehicle                                  │     │
│  │   - ReadObdParameters                                 │     │
│  │   - ClearDtCodes                                      │     │
│  └─────────────────────────────────────────────────────┘     │
└────────────────────────────┬──────────────────────────────────┘
                             │
┌────────────────────────────▼──────────────────────────────────┐
│                     Data Layer                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │   OBD-II     │  │   Room      │  │  DataStore  │          │
│  │  Bluetooth   │  │  Database   │  │ Preferences │          │
│  │  Repository  │  │  Repository │  │  Repository │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└───────────────────────────────────────────────────────────────┘
```

### Wichtige Dateien und deren Verantwortung

| Datei/Ordner | Verantwortung |
|--------------|---------------|
| `ui/theme/` | Theme, Farben, Typografie |
| `ui/screens/dashboard/` | Dashboard UI und ViewModel |
| `ui/components/gauges/` | Canvas-basierte Gauge-Komponenten |
| `data/repository/` | Datenquellen abstrahieren |
| `data/bluetooth/` | Bluetooth/OBD-Kommunikation |
| `domain/usecase/` | Business-Logik |
| `di/` | Hilt Module |

### Wichtige Dependencies (Version 2024)

```kotlin
// build.gradle.kts (Module)
dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    
    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")
    
    // Activity & ViewModel
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

### Externe Ressourcen und Quellen

| Kategorie | Quelle |
|-----------|--------|
| Compose Samples | https://github.com/android/compose-samples |
| Now in Android | https://github.com/android/nowinandroid |
| Material 3 | https://m3.material.io/ |
| Hilt/Dagger | https://dagger.dev/hilt/ |
| Kotlin Coroutines | https://kotlinlang.org/docs/coroutines-overview.html |
| StateFlow API | https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/ |
| Jetpack Compose | https://developer.android.com/jetpack/compose |
| Compose Snippets | https://github.com/android/snippets |

---

## Lizenz

Diese Dokumentation basiert auf öffentlich verfügbaren Informationen von Google, JetBrains und der Android Open Source Community.
