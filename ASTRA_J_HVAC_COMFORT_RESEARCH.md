# Opel Astra J 2012 1.4 Turbo (A14NEL) - HVAC & Comfort Features Research

## Executive Summary

This document provides comprehensive research on the climate control system and comfort features of the Opel Astra J 2012 1.4 Turbo (A14NEL) for implementing monitoring and coding features in an Android OBD app. The Astra J uses a sophisticated HVAC system controlled primarily through the Body Control Module (BCM) and HVAC control module, with limited data available through standard OBD-II protocols.

---

## 1. Climate Control System Architecture

### 1.1 System Overview

| Component | Description |
|-----------|-------------|
| **System Type** | Single-zone (standard) or Dual-zone (with automatic climate control) |
| **Control Module** | HVAC Control Module / Electronic Automatic Temperature Control (EATC) |
| **Main Bus** | GMLAN (High-Speed CAN) |
| **Access Method** | BCM via proprietary GM protocols, not standard OBD-II |

### 1.2 Zone Configuration by Trim Level

| Trim Level | Climate Control |
|------------|----------------|
| **Enjoy** | Manual single-zone |
| **Sport** | Manual single-zone |
| **Style** | Automatic dual-zone (optional) |
| **OPC/OPC Limited** | Automatic dual-zone (standard) |

### 1.3 Climate Control Sensors

The Astra J HVAC system utilizes multiple sensors for proper operation:

| Sensor | Location | Purpose |
|--------|----------|---------|
| **Interior Temperature Sensor** | Dashboard center, behind instrument cluster | Cabin temperature measurement |
| **Exterior Temperature Sensor** | Front bumper, driver side | Ambient air temperature |
| **Sunload Sensor** | Top of dashboard, near windshield | Solar radiation intensity |
| **Evaporator Temperature Sensor** | Evaporator core outlet | Frost protection |
| **Refrigerant Pressure Sensor** | High-pressure line near condenser | System pressure monitoring |
| **Mode Door Actuator Position** | HVAC case inside dashboard | Air distribution position |
| **Blend Door Actuator Position** | HVAC case inside dashboard | Temperature mixing |
| **Blower Motor Speed** | HVAC case | Fan speed control |
| **Compressor Clutch** | Engine compartment | A/C engagement |

---

## 2. HVAC Parameters via OBD Diagnostics

### 2.1 Standard OBD-II (Mode 01) - Limited HVAC Data

The following Mode 01 PIDs can provide some climate-related data:

| PID | Name | Unit | Description |
|-----|------|------|-------------|
| 0x05 | Engine Coolant Temperature | °C | Ambient temp proxy when engine off |
| 0x0F | Intake Air Temperature | °C | Under-hood air temp |
| 0x46 | Ambient Air Temperature | °C | Exterior temperature |
| 0x47 | Accelerator Pedal Position B | % | Not directly related |
| 0x5C | Engine Oil Temperature | °C | Related to engine heat |

### 2.2 Extended Mode 22 PIDs (Bosch ME17.9.22 ECU)

The engine ECU provides limited climate-related data:

| PID | Name | Unit | Description |
|-----|------|------|-------------|
| 0x223001 | Ambient Temperature | °C | Outside air temperature |
| 0x223002 | Engine Oil Temperature | °C | Engine temperature proxy |

**Note:** Evaporator temperature, refrigerant pressure, and HVAC actuator positions are NOT available through the engine ECU. These are handled by the separate HVAC control module on the GMLAN bus.

### 2.3 HVAC Control Module Data (GMLAN/Proprietary)

The actual HVAC data resides in the HVAC Control Module, accessible via:

| Method | Protocol | Accessibility |
|--------|----------|---------------|
| **OP-COM / VauxCom** | GMLAN | Full read/write via proprietary |
| **GM GDS2 / MDI2** | GMLAN | Dealer-grade diagnostics |
| **Standard OBD-II** | ISO 15765 | NOT accessible |
| **ELM327 Adapters** | OBD-II | NOT accessible |

### 2.4 HVAC Live Data Parameters (via OP-COM/GDS2)

| Parameter | Typical Values | Units |
|-----------|---------------|-------|
| Interior Temperature | -40 to +85 | °C |
| Exterior Temperature | -40 to +60 | °C |
| Sunload Intensity | 0 to 1200 | W/m² |
| Evaporator Temperature | -10 to +30 | °C |
| Refrigerant Pressure | 100 to 3500 | kPa |
| Blower Motor Speed | 0 to 100 | % |
| Compressor Clutch | On/Off | Status |
| Driver Blend Door | 0 to 100 | % (Cold to Hot) |
| Passenger Blend Door | 0 to 100 | % (Cold to Hot) |
| Mode Door Position | 0 to 7 | Position |
| AC Request Status | Active/Inactive | Status |
| Recirculation Mode | Fresh/Air Recirc | Status |
| Defrost Status | Active/Inactive | Status |

---

## 3. Climate Control Coding

### 3.1 BCM Coding Channels for HVAC

Located in: `Body → BCM → Programming → Program Variant Configuration`

| Channel | Values | Description |
|---------|--------|-------------|
| **Auto Recirc Duration** | 0-10 | Auto recirculation timeout |
| **Defrost Auto Activation** | 0/1 | Auto defrost at certain conditions |
| **Defrost Timer** | 0-600 | Auto defrost shutoff time (seconds) |
| **Compressor Deactivation Speed** | 0-10 | Speed threshold to disengage AC |
| **Blower Speed Memory** | 0/1 | Remember last blower speed |
| **Temperature Unit** | C/F | Display units |
| **Auto Recirc Activation** | 0/1 | Enable auto recirculation |

### 3.2 HVAC Configuration Options

| Setting | Options | Default |
|---------|---------|---------|
| **Temperature Scale** | Celsius / Fahrenheit | Celsius |
| **Auto-Recirculation** | On/Off | On |
| **Defrost Timer** | 0-600 seconds | 300 |
| **AC Compressor Cutoff** | Speed-based | 180 km/h |
| **Blower Memory** | On/Off | Off |
| **Sync Mode** | On/Off | Off |

---

## 4. Heated Seats

### 4.1 System Configuration

| Feature | Availability | Module |
|---------|--------------|--------|
| **Heated Front Seats** | Optional (Style/OPC) | Seat Heating Module |
| **Heated Rear Seats** | Optional (Estate only) | Seat Heating Module |
| **Heated Steering Wheel** | Optional (Style+) | Steering Column Module |

### 4.2 Heating Levels

| Level | Temperature | Current Draw |
|-------|-------------|--------------|
| Off | 0% | 0A |
| Low (1) | ~30% | ~2A |
| Medium (2) | ~60% | ~4A |
| High (3) | 100% | ~6A |

### 4.3 Coding Options (Seat Heating Module)

| Channel | Values | Description |
|---------|--------|-------------|
| **Heated Seats Present** | 0/1 | Enable/disable module |
| **Auto High Seat Heating** | 0/1 | Auto boost to high initially |
| **Auto Seat Heating Timeout** | 0-60 | Minutes until auto-off |
| **Heated Steering Wheel Present** | 0/1 | Enable HSW module |

### 4.4 Diagnostic Data (via OP-COM)

| Parameter | Description |
|-----------|-------------|
| Driver Seat Heat Level | 0-3 |
| Passenger Seat Heat Level | 0-3 |
| Rear Left Seat Heat Level | 0-3 |
| Rear Right Seat Heat Level | 0-3 |
| Steering Wheel Heat Level | 0-3 |
| Seat Sensor Status | Occupied/Empty |

---

## 5. Heated Steering Wheel

### 5.1 Availability

| Trim Level | Heated Steering Wheel |
|------------|----------------------|
| Enjoy | No (optional accessory) |
| Sport | No |
| Style | Optional |
| OPC | Standard |

### 5.2 Operation

- **Activation:** Button on steering wheel or climate menu
- **Levels:** 3 (Low/Medium/High)
- **Auto-Off:** After 30 minutes or when engine off
- **Temperature:** ~35-40°C at high setting

### 5.3 Coding

| Channel | Values | Description |
|---------|--------|-------------|
| **Steering Wheel Heating** | 0/1 | Module present |
| **Auto Heat Activation** | 0/1 | Auto-enable on cold start |
| **Auto Timeout** | 0-60 min | Auto shutoff |

---

## 6. Auto Climate Control

### 6.1 How It Works

The automatic climate control system:

1. **Temperature Setpoint:** Driver and passenger set desired temperature (16-30°C)
2. **Interior Sensor:** Monitors actual cabin temperature
3. **Exterior Sensor:** Measures ambient temperature
4. **Sunload Sensor:** Detects solar radiation
5. **Blend Doors:** Mix hot/cold air to achieve setpoint
6. **Blower Speed:** Auto-adjusts based on difference between setpoint and actual
7. **Mode Doors:** Auto-select air distribution based on conditions

### 6.2 Control Logic

```
Target Temp = Driver Setpoint (or Sync setpoint)
Actual Temp = Interior Temperature Sensor
Delta = Target Temp - Actual Temp

if Delta > 5°C:
    Blower = High (80-100%)
    Blend = Full Heat
    Mode = Floor/Face blend
elif Delta > 2°C:
    Blower = Medium-High (50-80%)
    Blend = Moderate Heat
elif Delta > 0°C:
    Blower = Medium (30-50%)
    Blend = Slight Heat
elif Delta < -2°C:
    Blower = Medium (30-50%)
    Blend = Cooling
    AC = On
else:
    Blower = Low (10-30%)
    Blend = Maintain
```

### 6.3 Auto Recirculation

The system automatically activates recirculation when:
- **High Sunload:** Detected to reduce cabin heat gain
- **High Exterior Temp:** >30°C to improve cooling efficiency
- **Poor Air Quality:** Tunnel mode activation (if equipped)

### 6.4 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Auto AC Mode** | 0/1 | Enable auto climate |
| **Auto Recirc Threshold** | 0-10 | Sensitivity |
| **Sync Mode Default** | 0/1 | Auto-sync driver/passenger |

---

## 7. Recirculation System

### 7.1 Modes

| Mode | Description | Typical Use |
|------|-------------|-------------|
| **Fresh Air** | Outside air into cabin | Normal driving |
| **Recirculation** | Cabin air recirculated | Traffic, odors, hot weather |
| **Auto Recirc** | System decides based on sensors | Automatic optimal |

### 7.2 Auto Recirculation Logic

| Condition | Action |
|-----------|--------|
| Exterior Temp > 25°C | Activate recirculation |
| Sunload > 500 W/m² | Activate recirculation |
| Defrost active | Force fresh air |
| A/C off, Temp < 10°C | Force fresh air |

### 7.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Recirculation Present** | 0/1 | Module installed |
| **Auto Recirc Duration** | 0-10 | Timeout value |
| **Recirc Motor Present** | 0/1 | Actuator installed |

---

## 8. Rear Window Defogger

### 8.1 Operation

| Parameter | Value |
|-----------|-------|
| **Activation** | Button in climate panel |
| **Timer** | 10 minutes (auto-off) |
| **Heating Elements** | Grid pattern on rear glass |
| **Power Draw** | ~15A |

### 8.2 Automatic Activation

The rear defogger can auto-activate when:
- Rear window is wet/foggy (humidity sensor, if equipped)
- Outside temperature is low
- A/C is active and defrost mode selected

### 8.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Rear Defogger Present** | 0/1 | Installed |
| **Auto Rear Defogger** | 0/1 | Auto-activation enabled |
| **Rear Defogger Timer** | 0-15 min | Timeout duration |
| **Mirror Heating Linked** | 0/1 | Auto-enable with rear defog |

---

## 9. Mirror Heating

### 9.1 Configuration

| Feature | Availability |
|---------|--------------|
| **Heated Mirrors** | Standard on Style+ |
| **Separate Button** | No (linked to rear defogger) |
| **Power Draw** | ~3A per mirror |

### 9.2 Operation

- **Activation:** Linked to rear window defogger button
- **Timer:** Same as rear defogger (10 minutes)
- **Element Type:** Heating film on mirror glass

### 9.3 Coding

| Channel | Values | Description |
|---------|--------|-------------|
| **Mirror Heating Present** | 0/1 | Module installed |
| **Mirror Heating Linked** | 0/1 | Link to rear defogger |
| **Auto Mirror Heating** | 0/1 | Temperature-based activation |

---

## 10. Rain Sensor

### 10.1 System Type

| Parameter | Value |
|-----------|-------|
| **Type** | Optical/Infrared |
| **Location** | Windshield, behind mirror |
| **Manufacturer** | Bosch / Valeo (varies by VIN) |

### 10.2 Operation

The rain sensor works by:
1. Emitting infrared light at windshield
2. Measuring reflected light intensity
3. Detecting water droplets on glass
4. Adjusting wiper speed/intensity based on amount

### 10.3 Sensitivity Levels

| Level | Trigger Threshold | Wiper Speed |
|-------|------------------|-------------|
| 1 (Low) | Heavy rain | Slow intermittent |
| 2 (Medium) | Moderate rain | Medium intermittent |
| 3 (High) | Light rain/drizzle | Fast intermittent |
| 4 (Auto) | Adaptive | Variable speed |

### 10.4 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Rain Sensor Present** | 0/1 | Installed |
| **Rain Sensor Sensitivity** | 1-4 | Sensitivity level |
| **Auto Wipers** | 0/1 | Enable auto function |
| **Wiper Sensitivity** | 0-10 | Adjustable sensitivity |

---

## 11. Light Sensor (Auto Headlights)

### 11.1 System Components

| Component | Description |
|-----------|-------------|
| **Light Sensor** | Photodiode/Phototransistor |
| **Location** | Top of dashboard |
| **Function** | Ambient light detection |

### 11.2 Operation

| Condition | Action |
|-----------|--------|
| Dark (< 300 lux) | Headlights ON |
| Twilight (300-500 lux) | Headlights ON |
| Daylight (> 500 lux) | Headlights OFF |
| Tunnel detected | Immediate ON |

### 11.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Auto Light Sensor** | 0/1 | Present |
| **Auto Light Sensitivity** | 1-10 | Brightness threshold |
| **Coming Home Sensitivity** | 1-10 | Darkness threshold |
| **Auto High Beam** | 0/1 | If equipped |
| **Daytime Running Light** | 0-5 | DRL mode (see Section 4.1) |

---

## 12. Parking Sensors

### 12.1 Configuration

| Position | Count | Frequency |
|----------|-------|-----------|
| **Front** | 4 (if equipped) | 40 kHz |
| **Rear** | 4 (standard on some trims) | 40 kHz |

### 12.2 Operation

- **Activation:** Reverse gear (rear), Low speed with obstacle (front)
- **Display:** Visual (parking pilot display) + Audible beeps
- **Beep Rate:** Increases as obstacle gets closer
- **Detection Range:** ~0.2m to 2.0m

### 12.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Front Parking Sensors** | 0/1 | Installed |
| **Rear Parking Sensors** | 0/1 | Installed |
| **Parking Sensor Volume** | 0-10 | Beep volume |
| **Parking Sensor Sensitivity** | 0-10 | Detection distance |
| **Parking Pilot Display** | 0/1 | Visual display |
| **Auto Front Parking** | 0/1 | Auto-activate at low speed |

---

## 13. Rear Camera

### 13.1 System (if equipped)

| Parameter | Specification |
|-----------|---------------|
| **Resolution** | 640x480 (VGA) typical |
| **Field of View** | ~130° horizontal |
| **Activation** | Reverse gear |
| **Display** | Radio/Navi screen |

### 13.2 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Rear Camera Present** | 0/1 | Installed |
| **Rear Camera Display** | 0/1 | Enable display |
| **Camera Guidelines** | 0/1 | Display parking lines |
| **Camera Mirror Mode** | 0/1 | Mirror image (if needed) |
| **Video in Motion** | 0/1 | Allow video while driving |

---

## 14. Seat Memory

### 14.1 System (if equipped)

| Feature | Specification |
|---------|---------------|
| **Memory Positions** | 2 per side (driver memory) |
| **Stored Settings** | Seat, mirrors, headrest |
| **Key Fob Integration** | Optional |

### 14.2 Memory Positions

| Position | Trigger |
|----------|---------|
| **Memory 1** | M1 button or Key Fob 1 |
| **Memory 2** | M2 button or Key Fob 2 |
| **Exit** | Easy exit position |

### 14.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Seat Memory Present** | 0/1 | Module installed |
| **Memory Positions** | 1-3 | Number of positions |
| **Seat Easy Exit** | 0/1 | Easy exit function |
| **Seat Mirror Tilt on Reverse** | 0/1 | Curb view assist |

---

## 15. Power Windows

### 15.1 Features

| Feature | Availability |
|---------|--------------|
| **One-Touch Up/Down** | Driver window (standard) |
| **One-Touch All Windows** | With comfort coding |
| **Anti-Pinch** | All windows (safety standard) |
| **Child Lock** | Rear switches |

### 15.2 Anti-Pinch Protection

| Parameter | Specification |
|-----------|---------------|
| **Detection** | Force sensor in motor |
| **Reversal Force** | < 100N |
| **Reversal Distance** | < 50mm |
| **Reset** | Auto after obstruction cleared |

### 15.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Windows Comfort Closing** | 0/1 | Close all with key |
| **Windows Comfort Opening** | 0/1 | Open all with key |
| **One-Touch Driver** | 0/1 | Enable one-touch |
| **One-Touch All** | 0/1 | One-touch for all windows |
| **Auto Window Reset** | 0/1 | Auto-learn after battery disconnect |

---

## 16. Central Locking

### 16.1 Auto-Lock Settings

| Parameter | Specification |
|-----------|---------------|
| **Lock Speed** | 12 km/h (standard) |
| **Unlock on Park** | Optional |
| **Selective Unlock** | 1x=Driver, 2x=All |

### 16.2 Acoustic Confirmation

| Setting | Sound |
|---------|-------|
| 0 | Off |
| 1 | Chirp on lock only |
| 2 | Chirp on unlock only |
| 3 | Chirp on both |

### 16.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Speed Dependent Locking** | 0/1 | Lock at 12 km/h |
| **Selective Door Unlock** | 0/1 | 1x/2x unlock |
| **Auto Relock** | 0/1 | Re-lock after 3 min |
| **Crash Unlock** | 0/1 | Unlock on airbag |
| **Acoustic Confirmation** | 0-3 | Chirp settings |

---

## 17. Mirror Folding

### 17.1 Configuration

| Feature | Availability |
|---------|--------------|
| **Power Folding** | Style/OPC (optional) |
| **Auto Fold on Lock** | With comfort coding |
| **Heated Mirrors** | See Section 9 |

### 17.2 Operation

- **Manual:** Button on door panel
- **Auto:** On lock/unlock (with coding)
- **Speed:** ~2 seconds to fold/unfold

### 17.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Power Folding Mirrors** | 0/1 | Present |
| **Mirror Fold on Lock** | 0/1 | Auto-fold enable |
| **Mirror Unfold on Unlock** | 0/1 | Auto-unfold enable |
| **Fold on Speed** | 0/1 | Fold above certain speed |

---

## 18. Wiper System

### 18.1 Front Wipers

| Feature | Specification |
|---------|---------------|
| **Speed Settings** | Low, High, Interval |
| **Rain Sensor** | If equipped (see Section 10) |
| **Intermittent** | Adjustable delay |

### 18.2 Rear Wiper

| Feature | Specification |
|---------|---------------|
| **Activation** | Reverse gear + front wipers on |
| **Interval** | Linked to front interval |
| **Auto Rear Wiper** | Rain detection (if equipped) |

### 18.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Rear Wiper Present** | 0/1 | Installed |
| **Rear Wiper Reverse** | 0/1 | Activate in reverse |
| **Auto Wipers** | 0/1 | Rain sensor enable |
| **Wiper Sensitivity** | 0-10 | Interval timing |
| **Front Wiper Sensitivity** | 0-10 | Rain sensor sensitivity |

---

## 19. Headlight Leveling

### 19.1 Manual System

| Feature | Specification |
|---------|---------------|
| **Control** | Rotary dial (driver side) |
| **Positions** | 0-3 (typically) |
| **Setting** | Based on load |

| Position | Load Condition |
|----------|----------------|
| 0 | Normal load |
| 1 | Partial load |
| 2 | Full load |
| 3 | Maximum load / Trailer |

### 19.2 Automatic Leveling (if equipped)

| Feature | Specification |
|---------|---------------|
| **System** | Auto-leveling motors |
| **Sensors** | Level sensor on suspension |
| **Adjustment** | Continuous |

### 19.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Manual Headlight Leveling** | 0/1 | Present |
| **Auto Headlight Leveling** | 0/1 | Automatic system |
| **AFL Present** | 0/1 | Adaptive Forward Lighting |

---

## 20. Ambient Lighting (Interior)

### 20.1 System (if equipped)

| Feature | Specification |
|---------|---------------|
| **Locations** | Door pockets, footwells, dashboard |
| **Colors** | Single or multi-color (varies) |
| **Control** | Via infotainment (if color) |

### 20.2 Available Colors

| Color Code | Color |
|------------|-------|
| 1 | Red |
| 2 | Blue |
| 3 | Green |
| 4 | Purple/Violet |
| 5 | Cyan |
| 6 | Yellow |
| 7 | White |

### 20.3 Coding Options

| Channel | Values | Description |
|---------|--------|-------------|
| **Ambient Lighting Present** | 0/1 | Installed |
| **Ambient Light Color** | 1-7 | Color selection |
| **Ambient Light Brightness** | 0-100 | Brightness level |
| **Ambient Sync with Cluster** | 0/1 | Match instrument lighting |

---

## 21. OBD-II Access Summary

### 21.1 What Can Be Accessed via Standard OBD-II

| Data | OBD Access | Notes |
|------|------------|-------|
| Exterior Temperature | Mode 01 PID 0x46 | Limited precision |
| Intake Air Temp | Mode 01 PID 0x0F | Under-hood temp |
| Engine Coolant Temp | Mode 01 PID 0x05 | Related to HVAC heat |

### 21.2 What Requires Proprietary Access

| Data | Required Tool | Notes |
|------|---------------|-------|
| Interior Temperature | OP-COM / GDS2 | HVAC module |
| Evaporator Temperature | OP-COM / GDS2 | HVAC module |
| Refrigerant Pressure | OP-COM / GDS2 | HVAC module |
| Blower Speed | OP-COM / GDS2 | HVAC module |
| Blend Door Positions | OP-COM / GDS2 | HVAC module |
| Compressor Status | OP-COM / GDS2 | HVAC module |
| Seat Heating Status | OP-COM / GDS2 | Seat module |
| Heated Steering Wheel | OP-COM / GDS2 | SCCM module |
| Rain Sensor Data | OP-COM / GDS2 | BCM |
| Light Sensor Data | OP-COM / GDS2 | BCM |
| Parking Sensor Status | OP-COM / GDS2 | BCM |
| Mirror Heating Status | OP-COM / GDS2 | BCM |
| Window Status | OP-COM / GDS2 | BCM |

### 21.3 Module Addresses for Diagnostics

| Module | Address | Purpose |
|--------|---------|---------|
| BCM | 0xFF | Body control, comfort |
| HVAC | 0x10 (internal) | Climate control |
| IPC | 0x83 | Instrument cluster |
| SDM | 0x3C | Airbag/safety |
| ABS | 0x14 | Brakes |
| ECM | 0x01 | Engine |
| TCM | 0x16 | Transmission |
| Radio | 0x18 | Infotainment |
| Seat Memory | 0x48 | Power seats |
| Seat Heating | 0x4C | Heated seats |

---

## 22. Implementation Recommendations

### 22.1 For Android OBD App

Given the limitations of standard OBD-II for HVAC data, the following approaches are recommended:

#### Approach 1: Read-Only Display via BCM/OBD

Use OP-COM protocol or develop GMLAN bridge to read available data:

```
Implementation Steps:
1. Implement OP-COM protocol handler
2. Connect to BCM (0xFF) for comfort data
3. Connect to IPC (0x83) for temperature display
4. Parse proprietary GM HVAC data
```

#### Approach 2: Coding Support

Provide UI for BCM coding via standardized interface:

```
Supported Coding Categories:
1. Comfort (windows, mirrors, locks)
2. Lighting (DRL, coming home)
3. Climate (auto-recirc, defrost timer)
4. Sensors (rain/light sensitivity)
```

### 22.2 Data Models

```kotlin
// HVAC Climate Data
data class HVACData(
    val interiorTempCelsius: Double?,
    val exteriorTempCelsius: Double?,
    val sunloadWattsPerM2: Int?,
    val evaporatorTempCelsius: Double?,
    val refrigerantPressureKpa: Int?,
    val blowerSpeedPercent: Int?,
    val compressorClutchActive: Boolean?,
    val driverBlendDoorPercent: Int?,
    val passengerBlendDoorPercent: Int?,
    val modeDoorPosition: Int?,
    val recirculationMode: RecircMode,
    val defrostActive: Boolean
)

// Comfort Data
data class ComfortData(
    val heatedSeats: HeatedSeatData,
    val heatedSteeringWheel: Int, // 0-3
    val mirrorHeatingActive: Boolean,
    val rearDefoggerActive: Boolean,
    val rainSensorSensitivity: Int,
    val lightSensorSensitivity: Int
)

// Seat Heating
data class HeatedSeatData(
    val driverLevel: Int, // 0-3
    val passengerLevel: Int, // 0-3
    val rearLeftLevel: Int?, // 0-3
    val rearRightLevel: Int? // 0-3
)
```

---

## 23. Coding Repository Structure

Based on the existing `AstraJCodingModels.kt`, extend with:

```kotlin
object AstraJHVACCoding {
    
    val hvacOptions = listOf(
        // Climate Control
        CodingOption(
            id = "auto_recirc",
            module = Module.BCM,
            channel = "Auto Recirculation",
            displayName = "Auto Recirculation",
            description = "Automatisches Umluftsystem",
            values = listOf(
                CodingValue("0", "Deaktiviert"),
                CodingValue("1", "Aktiviert")
            )
        ),
        CodingOption(
            id = "defrost_timer",
            module = Module.BCM,
            channel = "Defrost Timer",
            displayName = "Heckscheibenheizung Timer",
            description = "Auto-Abschaltzeit Heckscheibenheizung",
            values = listOf(
                CodingValue("0", "5 Min"),
                CodingValue("1", "10 Min"),
                CodingValue("2", "15 Min")
            )
        ),
        // Seat Heating
        CodingOption(
            id = "heated_seats_present",
            module = Module.BCM,
            channel = "Heated Seats",
            displayName = "Sitzheizung",
            description = "Sitzheizung aktivieren",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            )
        ),
        CodingOption(
            id = "heated_steering_present",
            module = Module.BCM,
            channel = "Heated Steering Wheel",
            displayName = "Lenkradheizung",
            description = "Lenkradheizung aktivieren",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            )
        ),
        // Sensors
        CodingOption(
            id = "rain_sensor_sensitivity",
            module = Module.BCM,
            channel = "Rain Sensor Sensitivity",
            displayName = "Regensensor Empfindlichkeit",
            description = "Empfindlichkeit des Regensensors",
            values = (1..10).map { CodingValue("$it", "$it") }
        ),
        CodingOption(
            id = "light_sensor_sensitivity",
            module = Module.BCM,
            channel = "Light Sensor Sensitivity",
            displayName = "Lichtsensor Empfindlichkeit",
            description = "Empfindlichkeit des Lichtsensors",
            values = (1..10).map { CodingValue("$it", "$it") }
        ),
        // Windows
        CodingOption(
            id = "comfort_close",
            module = Module.REC,
            channel = "Windows Comfort Closing",
            displayName = "Komfortschließen",
            description = "Fenster mit Fernbedienung schließen",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            )
        ),
        CodingOption(
            id = "comfort_open",
            module = Module.REC,
            channel = "Windows Comfort Opening",
            displayName = "Komfortöffnen",
            description = "Fenster mit Fernbedienung öffnen",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            )
        ),
        CodingOption(
            id = "one_touch_all",
            module = Module.REC,
            channel = "One Touch All Windows",
            displayName = "One-Touch Alle Fenster",
            description = "One-Touch für alle Fenster",
            values = listOf(
                CodingValue("0", "Nur Fahrer"),
                CodingValue("1", "Alle Fenster")
            )
        ),
        // Mirrors
        CodingOption(
            id = "mirror_fold",
            module = Module.BCM,
            channel = "Power Folding Mirrors",
            displayName = "Spiegelanklappung",
            description = "Spiegel klappen bei Verriegelung ein",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Elektrisch anklappbare Spiegel"
        ),
        CodingOption(
            id = "mirror_unfold",
            module = Module.BCM,
            channel = "Power Unfolding Mirrors",
            displayName = "Spiegelausklappung",
            description = "Spiegel klappen bei Entriegelung aus",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Elektrisch anklappbare Spiegel"
        ),
        CodingOption(
            id = "mirror_heating_link",
            module = Module.BCM,
            channel = "Mirror Heating Linked",
            displayName = "Spiegelheizung verknüpft",
            description = "Spiegelheizung mit Heckscheibenheizung",
            values = listOf(
                CodingValue("0", "Unabhängig"),
                CodingValue("1", "Verknüpft")
            )
        ),
        // Parking Sensors
        CodingOption(
            id = "front_parking_sensors",
            module = Module.BCM,
            channel = "Front Parking Sensors",
            displayName = "Einparkhilfe vorne",
            description = "Sensoren vorne",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            )
        ),
        CodingOption(
            id = "rear_parking_sensors",
            module = Module.BCM,
            channel = "Rear Parking Sensors",
            displayName = "Einparkhilfe hinten",
            description = "Sensoren hinten",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            )
        ),
        CodingOption(
            id = "parking_sensor_sensitivity",
            module = Module.BCM,
            channel = "Parking Sensor Sensitivity",
            displayName = "Einparkhilfe Empfindlichkeit",
            description = "Empfindlichkeit der Sensoren",
            values = (1..10).map { CodingValue("$it", "Level $it") }
        ),
        // Rear Camera
        CodingOption(
            id = "rear_camera",
            module = Module.BCM,
            channel = "Rear View Camera",
            displayName = "Rückfahrkamera",
            description = "Rückfahrkamera aktivieren",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            )
        ),
        CodingOption(
            id = "camera_guidelines",
            module = Module.BCM,
            channel = "Camera Guidelines",
            displayName = "Einparkhilfe Linien",
            description = "Hilfslinien anzeigen",
            values = listOf(
                CodingValue("0", "Aus"),
                CodingValue("1", "Ein")
            )
        ),
        // Wipers
        CodingOption(
            id = "rear_wiper_reverse",
            module = Module.REC,
            channel = "Rear Wiper Reverse",
            displayName = "Heckwischer Rückwärtsgang",
            description = "Heckwischer bei Rückwärtsgang",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Heckscheibenwischer muss verbaut sein"
        ),
        CodingOption(
            id = "auto_wipers",
            module = Module.BCM,
            channel = "Auto Wipers",
            displayName = "Automatisches Wischen",
            description = "Regensensor für Scheibenwischer",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Regensensor muss verbaut sein"
        ),
        // Ambient Lighting
        CodingOption(
            id = "ambient_present",
            module = Module.REC,
            channel = "Ambient Lighting",
            displayName = "Ambientebeleuchtung",
            description = "Ambientebeleuchtung vorhanden",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Ambientebeleuchtung muss verbaut sein"
        ),
        CodingOption(
            id = "ambient_color",
            module = Module.REC,
            channel = "Ambient Light Color",
            displayName = "Ambientefarbe",
            description = "Farbe der Ambientebeleuchtung",
            values = listOf(
                CodingValue("1", "Rot"),
                CodingValue("2", "Blau"),
                CodingValue("3", "Grün"),
                CodingValue("4", "Lila"),
                CodingValue("5", "Cyan"),
                CodingValue("6", "Gelb")
            ),
            hardwareRequired = "Ambientebeleuchtung muss verbaut sein"
        ),
        // Seat Memory
        CodingOption(
            id = "seat_memory_present",
            module = Module.BCM,
            channel = "Seat Memory",
            displayName = "Sitzmemory",
            description = "Sitzpositionsspeicher",
            values = listOf(
                CodingValue("0", "Nicht vorhanden"),
                CodingValue("1", "Vorhanden")
            ),
            hardwareRequired = "Elektrisch verstellbare Sitze mit Memory"
        ),
        CodingOption(
            id = "easy_exit",
            module = Module.BCM,
            channel = "Easy Exit",
            displayName = "Komfort-Einstieg",
            description = "Sitze fahren bei Tür öffnen zurück",
            values = listOf(
                CodingValue("0", "Deaktiviert"),
                CodingValue("1", "Aktiviert")
            )
        ),
        // Interior Light Timeout
        CodingOption(
            id = "interior_light_timeout",
            module = Module.BCM,
            channel = "Interior Light Timeout",
            displayName = "Innenlicht Timeout",
            description = "Zeit bis Innenbeleuchtung erlischt",
            values = listOf(
                CodingValue("0", "Sofort"),
                CodingValue("1", "10 Sekunden"),
                CodingValue("2", "20 Sekunden"),
                CodingValue("3", "30 Sekunden"),
                CodingValue("4", "60 Sekunden"),
                CodingValue("5", "90 Sekunden"),
                CodingValue("6", "120 Sekunden"),
                CodingValue("7", "150 Sekunden")
            )
        )
    )
    
    fun getCategory() = CodingCategory(
        id = "hvac_comfort",
        displayName = "Klima & Komfort",
        icon = "Thermostat",
        options = hvacOptions
    )
}
```

---

## 24. Conclusion

### Key Findings

1. **HVAC Data via OBD-II:** Standard OBD-II provides very limited HVAC data (ambient temp only). Full HVAC data requires proprietary GM protocols (OP-COM, GDS2).

2. **BCM Coding:** Most comfort features (windows, mirrors, locks, lights) are accessible via BCM coding through OP-COM.

3. **Climate Control:** The actual climate system operates on a separate module not accessible via standard OBD-II.

4. **Implementation Path:** For a complete HVAC monitoring app, either:
   - Implement OP-COM protocol support, OR
   - Partner with existing tools (Torque Pro with custom PIDs), OR
   - Focus on coding features which ARE accessible via BCM

### Recommended Implementation Priority

| Priority | Feature | OBD Access | Complexity |
|----------|---------|-------------|------------|
| 1 | Comfort Coding (windows, mirrors, locks) | BCM | Low |
| 2 | Lighting Coding (DRL, coming home) | BCM | Low |
| 3 | Sensor Coding (rain, light sensitivity) | BCM | Low |
| 4 | HVAC Basic Monitoring | Proprietary | High |
| 5 | Full Climate Dashboard | Proprietary | Very High |

---

*Document Version: 1.0*
*Last Updated: May 2026*
*Vehicle: Opel Astra J 2012 1.4 Turbo (A14NEL)*
*Sources: Opel Workshop Documentation, GM Tech2 Data, OP-COM Community Forums, Astra-J.de, MOTOR-TALK*
