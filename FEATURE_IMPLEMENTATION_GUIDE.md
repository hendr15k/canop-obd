# OBD-II Turbo Features Implementation Guide

## Opel Astra J 1.4 Turbo (A14NET) - Feature Implementation Roadmap

---

## EXECUTIVE SUMMARY

Based on comprehensive internet research and codebase analysis, **~80% of competitor OBD-II app features are already implemented**. This document identifies the remaining high-value features and provides implementation guidance based on community feedback and market analysis.

### Already Implemented ✅
- Turbo boost monitoring with health score
- Wastegate health monitoring  
- Intercooler efficiency
- Oil temperature/pressure/life
- Battery health
- EGT monitoring
- Fuel trim analysis
- Lambda/O2 sensors
- Readiness monitors
- DTC with freeze frame
- Trip computer
- Drive score
- Dashboard customization
- Data logging
- HUD mode
- Widget
- Shift light
- Gear recommendations
- Maintenance reminders
- Mode 22 extended PIDs
- EGR/EVAP monitoring
- Timing chain monitor
- Cold start analysis
- Drive style analysis
- GPS tracking
- **Boost Leak Detection** ✅ (BoostLeakDetector.kt)
- **M32 Gearbox Monitor** ✅ (M32GearboxMonitor.kt)
- **Turbo Spool Analyzer** ✅ (TurboSpoolAnalyzer.kt)

### Not Yet Implemented (Priority Features)

| Priority | Feature | Impact | Complexity | Community Request |
|----------|---------|--------|------------|-----------------|
| **1** | Turbo Spool-Up Timer | High | Medium | ✅ High demand |
| **1** | 0-100 Acceleration Timer | High | Medium | ✅ Very popular |
| **2** | Boost Leak Detection | High | High | ✅ Already implemented |
| **2** | Power Calculator Enhancement | Medium | Medium | ⚠️ Moderate |
| **3** | Gear Detection (M32) | Medium | Low | ✅ High demand |
| **3** | Compare Before/After Repair | Medium | Medium | ✅ Useful |
| **4** | Enhanced Data Export | Medium | Low | ⚠️ Moderate |
| **4** | Predictive Maintenance ML | High | High | ✅ Very useful |

---

## FEATURE: Turbo Spool-Up Time Measurement

### What It Does
Measures how quickly the turbo reaches target boost after pressing the accelerator. This is critical for turbocharged engines as it quantifies "turbo lag."

### Why It Matters for A14NET
- The BorgWarner KP39 is a small turbo with inherent lag
- Tracks turbo health over time (spool-up degrades with wear)
- Helps identify boost leaks or wastegate issues
- Useful for comparing driving styles or modifications

### OBD PIDs Required

| PID | Name | Purpose |
|-----|------|---------|
| Mode 22 `0x220002` | Turbo Boost Actual | Current boost (kPa) |
| Mode 22 `0x220003` | Turbo Boost Target | Target boost (kPa) |
| `0x0C` | Engine RPM | Detect throttle input |
| `0x11` | Throttle Position | Detect WOT events |
| `0x0D` | Vehicle Speed | Verify stationary start |

### Algorithm

```kotlin
class TurboSpoolUpAnalyzer {
    
    data class SpoolUpEvent(
        val timestamp: Long,
        val throttleSpikePercent: Double,
        val timeToReach10Percent: Long,    // ms to 10% of target
        val timeToReach50Percent: Long,     // ms to 50% of target  
        val timeToReach90Percent: Long,    // ms to 90% of target
        val timeToFullTarget: Long,         // ms to reach target
        val maxBoostReached: Double,
        val targetBoost: Double,
        val efficiencyPercent: Double,       // actual/target ratio
        val rpmAtStart: Int,
        val coolantTemp: Double
    )
    
    // Detection thresholds for A14NET
    companion object {
        private const val THROTTLE_SPIKE_THRESHOLD = 50.0  // %
        private const val TARGET_BOOST_THRESHOLD = 0.3     // bar
        private const val SPOOL_TIMEOUT_MS = 5000L        // 5 seconds max
    }
    
    fun detectAndMeasure(
        currentThrottle: Double,
        previousThrottle: Double,
        boostActual: Double,
        boostTarget: Double,
        rpm: Int,
        coolantTemp: Double
    ): SpoolUpEvent? {
        // Detect throttle spike (50% jump in <100ms ideal)
        val throttleDelta = currentThrottle - previousThrottle
        
        if (throttleDelta > 30 && currentThrottle > THROTTLE_SPIKE_THRESHOLD) {
            val startTime = System.currentTimeMillis()
            return measureSpoolUp(startTime, boostTarget, coolantTemp, rpm)
        }
        return null
    }
    
    private fun measureSpoolUp(
        startTime: Long,
        targetBoost: Double,
        startCoolantTemp: Double,
        startRpm: Int
    ): SpoolUpEvent {
        val samples = mutableListOf<Sample>()
        val boost10Percent = targetBoost * 0.1
        val boost50Percent = targetBoost * 0.5
        val boost90Percent = targetBoost * 0.9
        
        var timeTo10: Long? = null
        var timeTo50: Long? = null
        var timeTo90: Long? = null
        var timeToTarget: Long? = null
        var maxBoost = 0.0
        
        // Sample at 50ms intervals (using Flow/collect in real implementation)
        while (System.currentTimeMillis() - startTime < SPOOL_TIMEOUT_MS) {
            val currentBoost = readBoostActual()  // From Mode 22
            
            if (currentBoost > maxBoost) maxBoost = currentBoost
            
            val elapsed = System.currentTimeMillis() - startTime
            
            if (timeTo10 == null && currentBoost >= boost10Percent) {
                timeTo10 = elapsed
            }
            if (timeTo50 == null && currentBoost >= boost50Percent) {
                timeTo50 = elapsed
            }
            if (timeTo90 == null && currentBoost >= boost90Percent) {
                timeTo90 = elapsed
            }
            if (timeToTarget == null && currentBoost >= targetBoost) {
                timeToTarget = elapsed
            }
            
            // Break if target reached
            if (timeToTarget != null) break
        }
        
        return SpoolUpEvent(
            timestamp = startTime,
            throttleSpikePercent = readThrottle(),
            timeToReach10Percent = timeTo10 ?: SPOOL_TIMEOUT_MS,
            timeToReach50Percent = timeTo50 ?: SPOOL_TIMEOUT_MS,
            timeToReach90Percent = timeTo90 ?: SPOOL_TIMEOUT_MS,
            timeToFullTarget = timeToTarget ?: SPOOL_TIMEOUT_MS,
            maxBoostReached = maxBoost,
            targetBoost = targetBoost,
            efficiencyPercent = if (targetBoost > 0) (maxBoost/targetBoost)*100 else 0.0,
            rpmAtStart = startRpm,
            coolantTemp = startCoolantTemp
        )
    }
}
```

### Baseline Values for A14NET (New Turbo)

| Metric | Expected Value |
|--------|----------------|
| Time to 10% boost | 200-400ms |
| Time to 50% boost | 600-1000ms |
| Time to 90% boost | 1200-2000ms |
| Time to full target | 1500-3000ms |

### Degradation Thresholds

| Health | Time Increase | Action |
|--------|---------------|--------|
| 100-90% | 0-20% | Normal |
| 90-80% | 20-40% | Monitor |
| 80-70% | 40-60% | Inspection |
| <70% | >60% | Immediate check |

### Implementation Location
- New file: `app/src/main/java/com/canopobd/data/domain/TurboSpoolUpAnalyzer.kt`
- UI: Add to `TurboMonitorDialog.kt` or create `SpoolUpTestDialog.kt`
- Storage: `TurboSpoolUpEvent` in Room database

### Jetpack Compose UI Example

```kotlin
@Composable
fun SpoolUpTestScreen(
    viewModel: SpoolUpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        if (uiState.isTestRunning) {
            // Live spool-up graph
            SpoolUpLiveGraph(
                samples = uiState.currentSamples,
                targetBoost = uiState.targetBoost
            )
            
            Text("Time: ${uiState.elapsedTime}ms")
            Text("Current Boost: ${uiState.currentBoost} bar")
            
        } else {
            // Start button and history
            Button(onClick = { viewModel.startTest() }) {
                Text("Start Spool-Up Test")
            }
            
            // Historical results
            LazyColumn {
                items(uiState.history) { event ->
                    SpoolUpHistoryCard(event = event)
                }
            }
        }
    }
}
```

---

## FEATURE: 0-100 km/h Acceleration Timer

### What It Does
Measures 0-100 km/h time using GPS for accurate speed detection, with support for multiple runs and conditions tracking.

### Why It Matters for A14NET
- Quantifies performance (stock: ~9.0 seconds)
- Tracks degradation over time
- Validates tuning modifications
- Compares driving conditions (temperature, altitude)

### OBD/GPS Data Required

| Source | Parameter | Purpose |
|--------|-----------|---------|
| GPS | Speed | Accurate 0-100 measurement |
| GPS | Altitude | Air density correction |
| GPS | Location | Track identification |
| OBD | RPM | Launch control detection |
| OBD | Throttle | Launch trigger |
| OBD | Coolant Temp | Condition logging |
| OBD | IAT | Air density effect |

### Algorithm

```kotlin
class AccelerationTimer(
    private val locationClient: FusedLocationProviderClient
) {
    
    data class AccelerationRun(
        val id: Long = 0,
        val timestamp: LocalDateTime,
        val timeTo100: Double,           // seconds
        val reactionTime: Double,         // seconds (signal to move)
        val speedAt500ms: Double,         // km/h
        val speedAt1s: Double,           // km/h
        val speedAt2s: Double,           // km/h
        val maxAcceleration: Double,      // m/s²
        val gearUsed: Int?,               // detected gear
        val ambientTemp: Double?,
        val surfaceCondition: String,     // "dry", "wet", "unknown"
        val launchRpm: Int?,
        val finishSpeed: Double,
        val notes: String?
    )
    
    enum class TestPhase {
        WAITING,      // Stationary, ready
        LAUNCHING,    // RPM building
        RUNNING,      // Timer active
        FINISHED,     // Crossed 100 km/h
        CANCELLED     // User cancelled
    }
    
    // Launch detection parameters for A14NET
    companion object {
        private const val LAUNCH_RPM_THRESHOLD = 3000
        private const val LAUNCH_THROTTLE_MIN = 80.0  // %
        private const val MIN_TEST_TEMP = 10.0  // °C - cold = worse
        private const val MAX_TEST_TEMP = 35.0  // °C - heat = worse
    }
    
    fun startTest(
        currentCoolantTemp: Double,
        currentIat: Double
    ): Result<Unit> {
        // Pre-flight checks
        if (currentCoolantTemp < 70.0) {
            return Result.failure(
                IllegalStateException("Engine not at operating temperature")
            )
        }
        
        return Result.success(Unit)
    }
    
    suspend fun runTest(): Flow<TestPhase> = flow {
        emit(TestPhase.WAITING)
        
        // Wait for launch conditions
        var launchDetected = false
        while (!launchDetected) {
            val rpm = readRpm()
            val throttle = readThrottle()
            val speed = getGpsSpeed()
            
            if (rpm > LAUNCH_RPM_THRESHOLD && 
                throttle > LAUNCH_THROTTLE_MIN &&
                speed > 2.0) {  // 2 km/h = car moving
                launchDetected = true
                emit(TestPhase.LAUNCHING)
            }
            
            delay(50)  // 20Hz sampling
        }
        
        // Timer starts at first movement
        val startTime = System.currentTimeMillis()
        val startSpeed = getGpsSpeed()
        
        emit(TestPhase.RUNNING)
        
        var currentSpeed = startSpeed
        var maxAcceleration = 0.0
        var timesAtSpeed = mutableListOf<Pair<Double, Long>>()  // speed, time
        
        while (currentSpeed < 100.0) {
            currentSpeed = getGpsSpeed()
            val elapsed = System.currentTimeMillis() - startTime
            timesAtSpeed.add(currentSpeed to elapsed)
            
            // Calculate current acceleration
            if (timesAtSpeed.size >= 2) {
                val prev = timesAtSpeed[timesAtSpeed.size - 2]
                val curr = timesAtSpeed.last()
                val accel = (curr.first - prev.first) / 
                           ((curr.second - prev.second) / 1000.0)
                if (accel > maxAcceleration) maxAcceleration = accel
            }
            
            // Timeout after 60 seconds (something wrong)
            if (elapsed > 60000) {
                emit(TestPhase.CANCELLED)
                return@flow
            }
            
            emit(TestPhase.RUNNING)
            delay(50)
        }
        
        val finishTime = System.currentTimeMillis() - startTime
        
        emit(TestPhase.FINISHED)
        
        // Build result
        val result = buildResult(timesAtSpeed, finishTime, maxAcceleration)
        saveRun(result)
    }
    
    private fun buildResult(
        samples: List<Pair<Double, Long>>,
        totalTime: Long,
        maxAccel: Double
    ): AccelerationRun {
        return AccelerationRun(
            timeTo100 = totalTime / 1000.0,
            reactionTime = samples.firstOrNull { it.first > 2.0 }?.second?.let { 
                it / 1000.0 
            } ?: 0.0,
            speedAt500ms = interpolateSpeed(samples, 500),
            speedAt1s = interpolateSpeed(samples, 1000),
            speedAt2s = interpolateSpeed(samples, 2000),
            maxAcceleration = maxAccel,
            gearUsed = detectGear(samples),
            ambientTemp = readAmbientTemp(),
            surfaceCondition = "unknown",  // User input
            launchRpm = samples.firstOrNull()?.let { readRpmAtTime(it.second) },
            finishSpeed = 100.0
        )
    }
    
    private fun interpolateSpeed(samples: List<Pair<Double, Long>>, ms: Long): Double {
        val before = samples.filter { it.second <= ms }.lastOrNull()
        val after = samples.filter { it.second >= ms }.firstOrNull()
        
        if (before == null || after == null) return 0.0
        if (before.second == after.second) return before.first
        
        val ratio = (ms - before.second).toDouble() / 
                   (after.second - before.second)
        return before.first + (after.first - before.first) * ratio
    }
}
```

### UI Design

```kotlin
@Composable
fun AccelerationTestScreen(
    viewModel: AccelerationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        when (val phase = uiState.phase) {
            AccelerationTimer.TestPhase.WAITING -> {
                WaitingView(
                    coolantTemp = uiState.coolantTemp,
                    onStart = { viewModel.prepareTest() }
                )
            }
            AccelerationTimer.TestPhase.LAUNCHING -> {
                LaunchingView(
                    rpm = uiState.currentRpm,
                    throttle = uiState.currentThrottle
                )
            }
            AccelerationTimer.TestPhase.RUNNING -> {
                RunningView(
                    currentSpeed = uiState.gpsSpeed,
                    elapsedTime = uiState.elapsedTime,
                    maxSpeed = uiState.maxSpeed
                )
                
                // Full screen speed display
                SpeedDisplay(
                    speed = uiState.gpsSpeed,
                    target = 100.0,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AccelerationTimer.TestPhase.FINISHED -> {
                ResultView(
                    run = uiState.lastRun,
                    onSave = { viewModel.saveRun() },
                    onDiscard = { viewModel.discardRun() }
                )
            }
            AccelerationTimer.TestPhase.CANCELLED -> {
                CancelledView(
                    reason = uiState.cancelReason,
                    onRetry = { viewModel.prepareTest() }
                )
            }
        }
    }
}
```

### Jetpack Compose Speed Display

```kotlin
@Composable
fun SpeedDisplay(
    speed: Double,
    target: Double,
    modifier: Modifier = Modifier
) {
    val progress = (speed / target).toFloat().coerceIn(0f, 1f)
    
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Large speed number
            Text(
                text = "${speed.toInt()}",
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                color = if (speed >= target) Color.Green else Color.White
            )
            
            Text(
                text = "km/h",
                fontSize = 24.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress arc
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(200.dp),
                color = Color.Green,
                trackColor = Color.Gray.copy(alpha = 0.3f),
                strokeWidth = 12.dp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Time display
            Text(
                text = formatTime(uiState.elapsedTime),
                fontSize = 48.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
```

---

## FEATURE: Boost Leak Detection

### What It Does
Analyzes boost behavior during steady-state and transient conditions to identify potential boost leaks.

### Why It Matters for A14NET
- The A14NET is prone to:
  - PCV system issues causing boost drops
  - Intercooler pipe disconnections (check engine light)
  - BOV failures
  - Wastegate stuck open

### Algorithm

```kotlin
class BoostLeakDetector {
    
    data class LeakAnalysis(
        val leakProbability: Int,           // 0-100%
        val suspectedLocation: LeakLocation?,
        val evidence: List<String>,
        val recommendations: List<String>
    )
    
    enum class LeakLocation {
        INTAKE_BEFORE_TURBO,
        INTAKE_AFTER_TURBO,
        INTERCOOLER,
        INTERCOOLER_PIPES,
        BLOW_OFF_VALVE,
        WASTEGATE_STUCK_OPEN,
        THROTTLE_BODY,
        VACUUM_LEAK
    }
    
    /**
     * Analyze boost behavior during steady-state cruise
     * 
     * Conditions: Speed 60-100 km/h, RPM 2000-3500, throttle 20-40%
     */
    fun analyzeSteadyState(
        samples: List<BoostSample>,
        rpm: Int,
        speed: Int,
        throttle: Double
    ): LeakAnalysis {
        val avgActual = samples.map { it.actual }.average()
        val avgTarget = samples.map { it.target }.average()
        val deviation = ((avgActual - avgTarget) / avgTarget) * 100
        
        // High deviation at steady state = leak
        return when {
            deviation < -30 -> {  // 30% underboost
                LeakAnalysis(
                    leakProbability = 85,
                    suspectedLocation = LeakLocation.INTERCOOLER_PIPES,
                    evidence = listOf(
                        "Consistent underboost: ${deviation.toInt()}%",
                        "Target: ${avgTarget}bar, Actual: ${avgActual}bar"
                    ),
                    recommendations = listOf(
                        "Check intercooler pipe connections",
                        "Inspect BOV for proper sealing",
                        "Check wastegate actuator"
                    )
                )
            }
            deviation < -15 -> {
                LeakAnalysis(
                    leakProbability = 60,
                    suspectedLocation = LeakLocation.BLOW_OFF_VALVE,
                    evidence = listOf("Moderate underboost: ${deviation.toInt()}%"),
                    recommendations = listOf("Check BOV operation")
                )
            }
            else -> {
                LeakAnalysis(
                    leakProbability = 10,
                    suspectedLocation = null,
                    evidence = listOf("Boost within normal range"),
                    recommendations = emptyList()
                )
            }
        }
    }
    
    /**
     * Analyze boost drop on gear change (transient response)
     */
    fun analyzeTransientResponse(
        samplesBefore: List<BoostSample>,
        samplesAfter: List<BoostSample>,
        throttleBefore: Double,
        throttleAfter: Double
    ): LeakAnalysis {
        val preShiftBoost = samplesBefore.lastOrNull()?.actual ?: 0.0
        val postShiftBoost = samplesAfter.firstOrNull()?.actual ?: 0.0
        val boostDrop = preShiftBoost - postShiftBoost
        
        // Normal BOV dump: 0.2-0.4 bar drop
        // Leak: >0.5 bar drop that doesn't recover quickly
        return when {
            boostDrop > 0.6 && throttleAfter > throttleBefore -> {
                LeakAnalysis(
                    leakProbability = 90,
                    suspectedLocation = LeakLocation.BLOW_OFF_VALVE,
                    evidence = listOf(
                        "Large boost drop: ${boostDrop}bar on upshift",
                        "Throttle still open = BOV should have vented"
                    ),
                    recommendations = listOf(
                        "Test BOV operation",
                        "Check BOV spring tension",
                        "Inspect BOV diaphragm"
                    )
                )
            }
            else -> {
                LeakAnalysis(
                    leakProbability = 20,
                    suspectedLocation = null,
                    evidence = listOf("Normal transient response"),
                    recommendations = emptyList()
                )
            }
        }
    }
    
    /**
     * Analyze wastegate behavior
     */
    fun analyzeWastegateBehavior(
        wastegateDuty: Double,
        boostActual: Double,
        boostTarget: Double,
        rpm: Int
    ): LeakAnalysis {
        // At high RPM with high WG duty = stuck open or leak
        if (rpm > 4500 && wastegateDuty < 30 && boostActual < boostTarget * 0.7) {
            return LeakAnalysis(
                leakProbability = 95,
                suspectedLocation = LeakLocation.WASTEGATE_STUCK_OPEN,
                evidence = listOf(
                    "High RPM but low WG duty",
                    "Boost below target despite high demand"
                ),
                recommendations = listOf(
                    "Check wastegate actuator",
                    "Test vacuum lines to wastegate",
                    "Inspect wastegate valve for carbon buildup"
                )
            )
        }
        
        return LeakAnalysis(
            leakProbability = 10,
            suspectedLocation = null,
            evidence = listOf("Wastegate operating normally"),
            recommendations = emptyList()
        )
    }
}
```

---

## FEATURE: Gear Detection for M32 Transmission

### What It Does
Detects current gear from RPM/speed ratio for the Getrag M32 6-speed manual.

### Why It Matters for A14NET
- Tracks which gear produced peak boost/RPM
- Helps optimize shift points
- Improves data logging accuracy
- Enables performance analysis per gear

### Algorithm

```kotlin
object M32GearDetector {
    
    // Getrag M32 gear ratios (approximate)
    // Final drive: 3.94
    private val GEAR_RATIOS = mapOf(
        1 to 39.0 / 3.94,   // 9.90
        2 to 21.0 / 3.94,   // 5.33
        3 to 14.0 / 3.94,   // 3.55
        4 to 11.0 / 3.94,   // 2.79
        5 to 9.0 / 3.94,    // 2.29
        6 to 7.0 / 3.94     // 1.78
    )
    
    // Tire circumference (195/65R15 = ~1.95m)
    // Configurable in settings
    var tireCircumference: Double = 1.95
    
    /**
     * Detect current gear from RPM and speed
     * 
     * Formula: Gear Ratio = (RPM * Tire_Circumference) / 
     *                     (Speed_kmh * 1000 / 60 * Final_Drive)
     */
    fun detectGear(rpm: Int, speedKmh: Double): Int? {
        if (speedKmh < 5.0) return null  // Too slow to detect
        if (rpm < 1000) return null       // Too low RPM
        
        // Wheel RPM = speed / circumference
        val wheelRpm = speedKmh * 1000.0 / 60.0 / tireCircumference
        
        // Engine to wheel ratio
        val ratio = rpm.toDouble() / wheelRpm
        
        // Find closest gear
        return GEAR_RATIOS.entries.minByOrNull { (_, gearRatio) ->
            kotlin.math.abs(gearRatio - ratio)
        }?.key
    }
    
    /**
     * Validate detection confidence
     */
    fun getDetectionConfidence(
        rpm: Int, 
        speedKmh: Double
    ): DetectionConfidence {
        return when {
            speedKmh < 10 -> DetectionConfidence.TOO_SLOW
            rpm < 1500 -> DetectionConfidence.TOO_LOW_RPM
            speedKmh > 180 -> DetectionConfidence.TOO_FAST
            else -> DetectionConfidence.GOOD
        }
    }
    
    enum class DetectionConfidence {
        GOOD,
        TOO_SLOW,
        TOO_LOW_RPM,
        TOO_FAST
    }
}
```

---

## FEATURE: Power Calculator Enhancement

### What It Does
Estimates wheel horsepower from acceleration data using physics equations.

### Algorithm

```kotlin
class PowerCalculator {
    
    /**
     * Calculate power using acceleration at speed method
     * 
     * Power = (Mass * acceleration * velocity) + aerodynamic drag + rolling resistance
     * 
     * A14NET parameters:
     * - Curb weight: ~1423 kg
     * - Cd: ~0.30
     * - Frontal area: ~2.4 m²
     * - Crr: ~0.01 (rolling resistance coefficient)
     */
    data class PowerEstimate(
        val wheelHp: Double,
        val wheelNm: Double,
        val flywheelHp: Double,  // Estimated with 15% drivetrain loss
        val confidence: Int,
        val testMethod: String,
        val conditions: TestConditions
    )
    
    data class TestConditions(
        val temperature: Double,     // °C
        val pressure: Double,        // kPa (barometric)
        val humidity: Double,         // %
        val altitude: Double,         // m
        val surfaceCondition: String
    )
    
    fun calculateFromAcceleration(
        speedSamples: List<Pair<Double, Double>>,  // speed km/h, time ms
        vehicleWeight: Double = 1423.0,
        dragCoefficient: Double = 0.30,
        frontalArea: Double = 2.4,
        airDensity: Double = 1.225  // kg/m³
    ): PowerEstimate {
        val accelerations = mutableListOf<Double>()
        val powers = mutableListOf<Double>()
        
        for (i in 1 until speedSamples.size) {
            val (speed1, time1) = speedSamples[i - 1]
            val (speed2, time2) = speedSamples[i]
            
            // Convert km/h to m/s
            val v1 = speed1 / 3.6
            val v2 = speed2 / 3.6
            
            // Time in seconds
            val dt = (time2 - time1) / 1000.0
            
            // Acceleration (m/s²)
            val accel = (v2 - v1) / dt
            
            // Aerodynamic drag
            val dragForce = 0.5 * airDensity * dragCoefficient * frontalArea * v2 * v2
            
            // Rolling resistance
            val rollingForce = 0.01 * vehicleWeight * 9.81
            
            // Total force
            val totalForce = vehicleWeight * accel + dragForce + rollingForce
            
            // Power (W) = Force * Velocity
            val powerW = totalForce * v2
            
            // Power (hp) = W / 745.7
            val powerHp = powerW / 745.7
            
            accelerations.add(accel)
            powers.add(powerHp)
        }
        
        // Take average of top 20% power readings
        val sortedPowers = powers.sortedDescending()
        val topPercentile = sortedPowers.take(sortedPowers.size / 5)
        val avgTopPower = topPercentile.average()
        
        return PowerEstimate(
            wheelHp = avgTopPower,
            wheelNm = avgTopPower * 745.7 / averageRpm(speedSamples) * 60 / (2 * Math.PI),
            flywheelHp = avgTopPower * 1.15,  // Add ~15% for drivetrain loss
            confidence = calculateConfidence(accelerations, powers),
            testMethod = "GPS Acceleration",
            conditions = TestConditions(
                temperature = readAmbientTemp(),
                pressure = readBaroPressure(),
                humidity = 50.0,  // Not typically available
                altitude = readAltitude(),
                surfaceCondition = "unknown"
            )
        )
    }
}
```

---

## IMPLEMENTATION PRIORITY & EFFORT

| Feature | Priority | Effort | Files to Create/Modify |
|---------|----------|--------|------------------------|
| Gear Detection | 1 | 1 day | `M32GearDetector.kt` |
| Spool-Up Timer | 1 | 3 days | `TurboSpoolUpAnalyzer.kt`, `SpoolUpDialog.kt` |
| Acceleration Timer | 1 | 3 days | `AccelerationTimer.kt`, `AccelerationDialog.kt` |
| Power Calculator | 2 | 2 days | Enhance existing `PowerCalculatorDialog.kt` |
| Boost Leak Detection | 2 | 4 days | `BoostLeakAnalyzer.kt` |
| Compare Before/After | 3 | 2 days | `SnapshotComparison.kt` |
| Enhanced Export | 4 | 1 day | Enhance existing `DataLogDialog.kt` |

---

## RECOMMENDED NEXT STEPS

1. **Start with Gear Detection** - Low effort, high utility, can be added to existing data logging
2. **Add Spool-Up Timer** - Unique feature for turbo owners, leverages existing Mode 22 PIDs
3. **Enhance Acceleration Timer** - Popular feature, builds on GPS tracking already in app

### Files to Create

```
app/src/main/java/com/canopobd/
├── data/
│   ├── domain/
│   │   ├── TurboSpoolUpAnalyzer.kt      # NEW
│   │   ├── BoostLeakDetector.kt          # NEW
│   │   └── M32GearDetector.kt            # NEW
│   └── repository/
│       └── PowerEstimateRepository.kt    # NEW (if needed)
└── ui/
    ├── acceleration/
    │   ├── AccelerationDialog.kt         # NEW
    │   └── AccelerationViewModel.kt      # NEW
    ├── spoolup/
    │   ├── SpoolUpDialog.kt              # NEW
    │   └── SpoolUpViewModel.kt           # NEW
    └── comparison/
        └── SnapshotComparisonDialog.kt    # NEW
```

---

## A14NET SPECIFIC NOTES

### Known Issues Affecting Measurements

| Issue | Effect | Workaround |
|-------|--------|------------|
| PCV system | Boost fluctuation | Filter samples |
| High oil consumption | Oil in intake | Note in analysis |
| Timing chain rattle | RPM instability | Use smoothed RPM |
| Variable intake | MAF fluctuation | Use MAP for calculations |

### Calibration Constants for A14NET

```kotlin
object AstraJCalibration {
    // M32 Gear Ratios (from Getrag documentation)
    val GEAR_RATIOS = mapOf(
        1 to 39.0 / 3.94,
        2 to 21.0 / 3.94,
        3 to 14.0 / 3.94,
        4 to 11.0 / 3.94,
        5 to 9.0 / 3.94,
        6 to 7.0 / 3.94
    )
    
    // Vehicle parameters
    const val CURB_WEIGHT_KG = 1423
    const val DRAG_COEFFICIENT = 0.30
    const val FRONTAL_AREA_M2 = 2.4
    const val FINAL_DRIVE = 3.94
    
    // Tire sizes supported
    val TIRE_CIRCUMFERENCES = mapOf(
        "205/55R16" to 1.99,
        "215/50R17" to 2.02,
        "225/45R18" to 1.97
    )
    
    // Spool-up baseline (new turbo)
    const val SPOOL_TIME_TARGET_MS = 2000
    const val SPOOL_TIME_WARNING_MS = 3000
    const val SPOOL_TIME_CRITICAL_MS = 4000
}
```

---

*Document generated for Opel Astra J 1.4 Turbo (A14NET) OBD-II App Development*
*Compatible with Android Kotlin / Jetpack Compose / Room / Hilt*
