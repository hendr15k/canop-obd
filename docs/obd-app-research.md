# Android OBD App Research: Opel/Vauxhall Features Analysis

## Executive Summary

This research documents features of leading Android OBD apps to guide development of **canop-obd** for Opel Astra J owners. Key findings:

- **Car Scanner ELM OBD2** is the most feature-rich free option with dedicated Opel/Vauxhall connection profiles
- **Torque Pro** offers deep customization but has been poorly maintained recently
- **OBD Auto Doctor** excels at diagnostics but lacks advanced customization
- **No true OP-COM Android alternatives exist** - Car Scanner comes closest with GM connection profiles
- Custom PID creation is critical for Opel engines (GM-specific sensors not in standard OBD-II)

---

## 1. Car Scanner ELM OBD2 (com.ovz.carscanner)

**Rating:** 4.7 stars | 333K reviews | 10M+ downloads

### Custom PID Creation
- **YES** - Full custom (extended) PID support
- Users can add manufacturer-specific PIDs hidden by car makers
- Built-in database of GM/Opel extended PIDs
- Formula support for calculated values
- PID testing interface to discover new PIDs

### Dashboard/Gauge Customization
- **Highly customizable dashboard** with drag-and-drop gauges
- Multiple gauge types: numerical, circular, bar, graph
- HUD mode for windshield projection
- Widget support for home screen
- Multiple dashboard profiles

### Data Logging
- **CSV and BRC (proprietary) export formats**
- Records only displayed data (smart resource usage)
- Automatic recording on connection
- Charts with zoom/scroll for review
- Share via email, messengers, cloud storage
- Rename and delete recordings

### DTC Scanning
- **Comprehensive DTC database** (thousands of manufacturer-specific codes)
- Reads Confirmed, Pending, and Permanent codes
- Clear DTCs / Reset MIL
- FREEZE FRAME data support
- Mode 06 for ECU self-monitoring tests
- Readiness monitors for emissions testing

### Trip/Fuel Logging
- Trip computer with fuel consumption statistics
- Real-time fuel economy monitoring
- Average speed tracking
- Distance traveled

### Performance Testing
- **Accurate acceleration measurements** (0-60, 0-100, etc.)
- Uses combined GPS+OBD for precision
- Braking distance measurement
- Multiple performance metrics

### Opel/Vauxhall Specific Features
- **GM/Opel/Vauxhall connection profile included**
- Knock retard monitoring (common GM PIDs)
- Specific sensor support for Opel engines
- Profile database updated regularly
- NOTE: Coding/service functions focus on VAG, Toyota, Renault - NOT Opel

### Connection Options
- Bluetooth, Bluetooth 4.0 (LE), Wi-Fi
- Recommended adapters: OBDLink, Kiwi 3, V-Gate, Carista, LELink, Veepeak
- Request optimization for CAN protocols (up to 6x faster)
- ATST command tuning for stability
- Warnings against cheap ELM327 clones

### Theme/UI
- Multiple color themes
- Dark/Light modes
- Customizable gauge colors

### Export/Sharing
- CSV export (open in Excel)
- Share to cloud storage
- Share via email/messengers
- BRC format for Car Scanner users

### Pricing
- **Mostly FREE** with ads
- Paid version removes ads, unlocks all features
- ~$4.99 USD

---

## 2. Torque Pro (org.prowl.torque)

**Rating:** 3.5 stars | 80.8K reviews | 1M+ downloads

### Custom PID Creation
- **YES** - Extended PID support
- **Java-like scripting language** (introduced v1.12.32)
- Full access to OBD, Sensor, Vehicle, Math classes
- Can create custom sensors, dialogs, push buttons
- Scripts run as CoreScripts (continuous) or PushButton scripts
- Example scripts available on GitHub

### Dashboard/Gauge Customization
- Customizable dashboard with widgets/gauges
- Multiple gauge types
- Theme support (different themes available)
- Profile support for different vehicles
- Widgets for Torque separate app available

### Data Logging
- GPS tracker with OBD engine logging
- **Send logging to web or email as CSV/KML**
- Google Maps integration
- Excel/OpenOffice compatible exports

### DTC Scanning
- DTC/CEL/fault code reading
- **Massive fault code database** for multiple manufacturers
- Clear codes functionality

### Trip/Fuel Logging
- MPG tracking
- CO2 emissions readout
- GPS speedometer/tracking
- Real-time web upload capability

### Performance Testing
- **Dyno/Dynomometer - HP & Torque calculation**
- 0-60 speed timings (GPS-enhanced accuracy)
- Quarter mile, 1/8 mile times
- Braking distance measurement

### Opel/GM Specific Features
- **Knock Retard** - supported extended PID for GM vehicles
- Supports Vauxhall/Opel officially in description
- EGR system support
- Transmission temperature (vehicle dependent)

### Theme/UI
- Theme support (built-in themes)
- HUD mode for night driving
- Compass (GPS-based, no magnetic interference)

### Export/Sharing
- **Screenshot sharing** to Facebook, Twitter, Google+, Email
- CSV/KML export
- Web upload capability
- Video your journey with Track Recorder plugin

### Connection Options
- Bluetooth OBD2 adapter required
- Supports Scantool.net, OBDKey, PLX, OBDLink, ELM327
- NOT compatible with Garmin EcoRoute HD

### Concerns (from reviews)
- **Poor maintenance** - not updated frequently
- **Display issues** with new Android versions
- Limited module access (powertrain only)
- **No bi-directional controls**
- Users report weak compared to free alternatives

### Scripting Example
```
scriptTitle="Example Script";
scriptDescription="Description";
scriptPackage="org.unique.key";
scriptVersion=1;
scriptAuthor="Author";

onInit = function() {
    sensor = Sensor.createSensor("Name","Unit","S");
};

main = function() {
    while (!quit) {
        sensor.setValue(someCalculation);
        Time.sleep(1000);
    }
};

stop = function() {
    quit = true;
};
```

---

## 3. OBD Auto Doctor (com.obdautodoctor)

**Rating:** 4.3 stars | 27.5K reviews | 1M+ downloads

### Custom PID Creation
- NO - Limited to standard OBD-II PIDs
- Over 126 powertrain parameters supported
- No extended/custom PID support
- No scripting capability

### Dashboard/Gauge Customization
- **Select PIDs to display on dashboard**
- Live data view (numerical or graphical)
- Sensor Grid (4 sensors side-by-side)
- Sensor Histogram for distribution analysis
- **OBD Oscilloscope** (graph up to 6 sensors simultaneously)
- Min/Avg/Max values shown

### Data Logging
- **Export live sensor data to CSV**
- Open in Excel, Google Sheets
- Save graphs as image files
- Share with mechanics/forums

### DTC Scanning
- **Comprehensive DTC support**
  - Confirmed Codes (permanent malfunctions)
  - Pending Codes (temporary failures)
  - Permanent Codes (historic, auto-cleared)
- **18,000+ trouble codes** in database
- P/B/C/U code categories
- FREEZE FRAME data
- Clear DTCs / Reset MIL
- Export/save DTC info to text file

### Readiness Monitors
- **Full readiness monitor support**
- "Since DTCs cleared" status
- "This driving cycle" status
- Complete/Incomplete/Disabled states
- Emissions test readiness check
- 11 system tests support

### Bi-directional Controls
- **Mode 08 - Evaporative system leak test**
- **Mode 08 - Particulate Filter regeneration** (diesel)
- **Mode 08 - Inducement system reinitialization**
- In-Use Performance Tracking counters

### Multi-ECU Support
- Engine controller (always)
- **Transmission controller** (if supported)
- Other ECUs depending on vehicle

### Sensor Parameters (Standard OBD-II)
- Comprehensive SAE J1979-DA parameter list
- Mode 06 for advanced diagnostics
- Oxygen sensor monitoring (Mode 05)
- Calculated fuel consumption
- Engine torque calculations
- Boost pressure from MAP sensor

### Trip/Fuel Logging
- **Real-time fuel economy monitoring**
- Multiple fuel types (gasoline/diesel)
- Per-trip and lifetime statistics

### Export/Sharing
- DTC/Freeze Frame export to text
- CSV sensor data export
- Graph image export
- Share via email

### Connection Options
- Bluetooth, BLE, Wi-Fi
- Requires ELM327 adapter
- Works with iPhone, Android, macOS, Windows

### Opel/GM Specific Features
- Generic OBD-II support (no special GM profiles)
- May work with Opel if standard PIDs supported
- **Premium required for advanced features**

### Pricing
- Free version available (limited)
- **Subscription model** ($5/month or $30/year reported)
- Premium required for misfire counters, full features
- Users report subscription frustration

---

## 4. DashCommand

**Status: DISCONTINUED** - No longer actively developed

### Historical Features (for reference)
- Gauges/dashboards
- Data logging
- Performance tests (0-60, etc.)
- DTC reading
- Trip computer

**Note:** Not recommended for new development reference.

---

## 5. InCarDoc (com.palads.inCardoc)

**Unable to verify current status** - May be discontinued or renamed

Based on historical data:
- Basic OBD-II scanning
- DTC reading/clearing
- Real-time data display
- Limited customization

---

## 6. OBD Fusion

**Unable to locate verified app**

Historical features:
- Custom PID support
- Dashboard customization
- Data logging to CSV
- Trip tracking
- Fuel economy

**Note:** App may have been rebranded or discontinued.

---

## 7. BlueDriver

**Status: iOS-focused, limited Android**

### Key Features
- **Enhanced OEM diagnostics** for many manufacturers
- Reads GM-specific codes (Opel/Vauxhall)
- Reads body, chassis, powertrain modules
- **Provides repair reports** with possible causes
- Stores DTC history
- Freeze frame data
- Mode 06 support
- Smog check readiness

### Limitations
- Requires **BlueDriver hardware** (proprietary)
- Android support limited
- Not ELM327 compatible
- Premium features require subscription

### Opel Support
- May support GM codes for Opel vehicles
- Module scanning depends on vehicle support
- Repair reports may not cover all Opel-specific codes

---

## 8. AUTOOL (OBDCheck)

**Status: Hardware-focused, limited Android app info**

Based on available information:
- Professional-grade diagnostics
- Vehicle-specific software
- May have specialized Opel/software
- Hardware adapter required

---

## 9. OP-COM Clone Apps

### Research Finding: **NO TRUE OP-COM ANDROID ALTERNATIVES EXIST**

OP-COM uses proprietary GM/Opel diagnostics protocols that:
- Are NOT standard OBD-II
- Require specific hardware (OP-COM v1.39, v2, etc.)
- Communicate via KWP2000 or proprietary CAN messages
- Access dealer-level functions not available via ELM327

### What CAN work:
- **Car Scanner ELM OBD2** with GM connection profile
  - Reads extended PIDs
  - Basic GM module access
  - NOT full OP-COM functionality
- **Torque Pro** with custom GM PIDs
  - Requires manual PID configuration
  - Limited to standard OBD-II + custom extended PIDs
- **OBD Auto Doctor**
  - Generic OBD-II only
  - No manufacturer-specific protocols

### OP-COM Specific Functions NOT Available via ELM327:
- Key programming
- IMMO adaptation
- Instrument cluster configuration
- BCM programming
- ABS module configuration
- Steering angle sensor calibration
- Airbag module access (full)
- Transmission programming
- BCM coding changes

---

## 10. EOBD Facile (org.eobdfacile.android)

**Rating:** 4.5 stars | Strong European presence

### Features
- DTC reading with 15,000+ code database
- Freeze frame display
- Readiness monitors
- Real-time parameter display
- Graph view
- CSV export
- Multiple vehicle profiles
- **Good Opel/compatibility**
- Wi-Fi/Bluetooth support

### Limitations
- Less customization than Car Scanner
- Basic dashboard (no extensive customization)
- No scripting/custom PIDs

---

## Feature Comparison Matrix

| Feature | Car Scanner | Torque Pro | OBD Auto Doctor | BlueDriver | EOBD Facile |
|---------|-------------|------------|-----------------|------------|-------------|
| **Custom PIDs** | YES | YES (Scripting) | NO | NO | NO |
| **Free Version** | YES (mostly) | NO ($4.99) | YES (limited) | NO | YES (limited) |
| **Opel Profile** | YES (GM) | NO | NO | MAYBE | NO |
| **CSV Export** | YES | YES | YES | YES | YES |
| **Freeze Frame** | YES | YES | YES | YES | YES |
| **Mode 06** | YES | YES | YES | YES | YES |
| **Scripting** | NO | YES | NO | NO | NO |
| **Performance Tests** | YES | YES | NO | YES | YES |
| **Data Recording** | YES | YES | YES | YES | YES |
| **Dashboard Custom** | HIGH | MEDIUM | LOW | LOW | LOW |
| **Multi-ECU** | YES | YES | YES | YES | YES |
| **DTC Database** | HUGE | HUGE | HUGE | GOOD | GOOD |
| **Fuel Economy** | YES | YES | YES | YES | YES |
| **HUD Mode** | YES | YES | NO | NO | NO |
| **Actively Updated** | YES | NO | YES | YES | YES |

---

## User Requests & Complaints for Opel OBD Apps

### Most Wanted Features (from forums/reviews):

1. **Misfire detection per cylinder**
   - Critical for diagnosing rough idle
   - Premium features often required

2. **OP-COM level functionality**
   - Users want dealer-level access
   - BCM coding, key programming
   - Not possible with standard OBD-II

3. **Turbo/Boost monitoring**
   - A19let, A16LET, Z20LET, Z20LEH owners want boost pressure
   - Intake temperature monitoring
   - Turbo RPM where available

4. **Real-time fuel consumption**
   - Instant MPG vs average
   - Fuel flow rate (GM doesn't standard OBD-II)

5. **EGR monitoring and cleaning**
   - Common failure point on Astra J 1.7/2.0 CDTi
   - EGR position/feedback

6. **DPF status (diesel)**
   - Regeneration status
   - Soot load percentage
   - Ash load (requires Mode 22 or proprietary)

7. **Battery/Charging system**
   - Alternator output
   - Battery voltage
   - Battery health monitoring

8. **Transmission temperature**
   - ATF temperature for auto trans
   - Clutch wear (where available)

### Common Complaints:

1. **Poor Opel support**
   - Limited PIDs returned
   - No manufacturer-specific data
   - GM extended PIDs not well documented

2. **Connection instability**
   - Cheap ELM327 clones
   - Bluetooth interference
   - Vehicle ECU quirks

3. **Slow refresh rates**
   - Too many parameters requested
   - Cheap adapters
   - Protocol limitations

4. **Premium paywalls**
   - Basic functions behind subscription
   - Monthly fees annoying
   - One-time purchase preferred

5. **Display/UI issues**
   - Hard to read while driving
   - Small fonts
   - Poor sunlight visibility

---

## Best Practices for Custom PID Formulas

### GM/Opel Engine PIDs (Common Extended PIDs)

These PIDs may be available on GM/Opel ECUs but are NOT standard OBD-II:

#### Boost Pressure (Turbo)
```
PID: 22101B (Mode 22)
Formula: (A*256+B) / 4.0 - 409.6
Unit: kPa (convert to bar: /100)
```

#### Boost Pressure (Alternative)
```
PID: 221124
Formula: ((A*256)+B-32768)/100
Unit: kPa
```

#### Intake Air Temperature (IAT)
```
PID: 221171 (Mode 22)
Formula: A-40
Unit: °C
```

#### Fuel Rail Pressure (Diesel)
```
PID: 22115C
Formula: ((A*256)+B) * 0.1
Unit: bar
```

#### MAF Air Flow Rate
```
PID: 221110 (Mode 22)
Formula: ((A*256)+B) / 100
Unit: g/s
```

#### Knock Retard (GM Specific)
```
PID: 010C (Standard)
Note: Often available in Mode 22 for GM
```

#### Engine Coolant Temperature
```
PID: 0105 (Standard)
Formula: A-40
Unit: °C
```

#### Turbo RPM
```
PID: 22118E (Mode 22)
Formula: ((A*256)+B) / 4
Unit: RPM
```

#### DPF Temperature (If available)
```
PID: 221DA0 (Mode 22)
Formula: (A*256+B)/10 - 40
Unit: °C
```

### Formula Syntax Examples

**Torque Pro Scripting:**
```javascript
// Calculate absolute boost (MAP - Barometric)
var map = Sensor.getValue(0x0B);  // MAP PID
var baro = Sensor.getValue(0x33); // Baro PID
var absBoost = map - baro;

// Calculate fuel consumption (g/km)
var maf = Sensor.getValue(0x10); // MAF g/s
var speed = Sensor.getValue(0x0D); // km/h
var fuelPerKm = (maf / speed) * 0.0038; // Approximate

// Calculate turbo boost in bar
var boostKpa = ((rawHighByte * 256) + rawLowByte - 4096) / 4;
var boostBar = boostKpa / 100;
```

**Car Scanner Custom PID:**
```
Name: Turbo Boost
Short Name: Boost
PID: 221124
Equation: (A*256+B-32768)/100
Units: kPa
```

### Key GM Mode 22 PIDs (Common)

Mode 22 uses 16-bit data addresses. Common GM addresses:
- 0x1151: Intake Air Temp
- 0x115C: Fuel Rail Pressure
- 0x1166: Turbo Boost
- 0x1142: Engine Coolant Temp
- 0x1110: MAF Air Flow
- 0x11D8: Battery Voltage
- 0x118C: Turbo RPM
- 0x11A0: DPF Delta Pressure

---

## Gauge/UI Design Recommendations

### Best Practices for Real-Time Monitoring

#### Gauge Layout Principles
1. **Primary gauges at eye level** (center-top)
   - RPM (large, prominent)
   - Speed (if not using phone as speedometer)
   - Coolant temp
   - Boost (for turbo)

2. **Secondary gauges in peripheral vision** (sides)
   - Fuel level
   - Oil temp/pressure
   - Battery voltage

3. **Info density balance**
   - 4-6 gauges optimal for driving
   - 8-10 max for passengers
   - Avoid information overload

#### Color Coding
- **Normal:** Green/Blue
- **Warning:** Yellow/Orange (>90% or threshold)
- **Danger:** Red/Flashing (<10% or critical)
- **Cold:** Blue (coolant < 70°C)

#### Gauge Types by Function
| Parameter | Best Gauge Type | Reason |
|-----------|-----------------|--------|
| RPM | Arc/Needle | Traditional, fast scanning |
| Temperature | Linear with zones | Color-coded zones |
| Boost | Arc with center zero | Shows +/- clearly |
| Speed | Large number | Quick reading |
| Fuel | Bar/Level | At-a-glance capacity |
| MAF/Flow | Number/Bar | Precise value needed |

#### Dashboard Profiles Recommended
1. **Normal Driving** - Fuel economy, coolant, battery
2. **Sport Driving** - RPM, boost, temps, G-forces
3. **Diagnostic** - All sensors, O2, fuel trims
4. **Night Mode** - Dim, red-backlit gauges
5. **Parking** - Essential gauges only

---

## Recommended Features for canop-obd

Based on comprehensive research, prioritize:

### Phase 1 - Essential (MVP)
1. ✅ Standard OBD-II PID support (all mode 01 PIDs)
2. ✅ DTC reading/clearing with Opel-specific codes
3. ✅ Freeze frame data
4. ✅ Readiness monitors
5. ✅ Real-time gauge display (customizable)
6. ✅ Data logging to CSV
7. ✅ Fuel consumption tracking

### Phase 2 - Important
1. 🔧 **Pre-configured Opel Astra J profiles** (J04x engines)
   - Turbo boost pressure
   - Intake air temp
   - DPF status (where available)
   - EGR position/feedback
2. 🔧 Custom PID editor with GM Mode 22 support
3. 🔧 Multiple dashboard layouts
4. 🔧 Trip logging with GPS
5. 🔧 Graph/Chart views
6. 🔧 HUD mode

### Phase 3 - Differentiators
1. 🏆 **Astra J specific gauges** (pre-built)
   - Turbo boost (bar)
   - Air-fuel ratio (where available)
   - DPF soot load (if accessible)
   - EGR duty cycle
2. 🏆 Performance testing (0-100, etc.)
3. 🏆 Alarm/warnings system
4. 🏆 Share/export to cloud
5. 🏆 Widget support
6. 🏆 Opel community features (share configs)

### Technical Recommendations

#### ELM327 Optimization for Astra J
- Astra J uses ISO 15765-4 (CAN 11-bit)
- Enable "Optimize requests" for faster polling
- Recommended ATST: 32-48 for stability
- Watch for ECU timeout on some functions

#### Data Sampling
- Standard PIDs: 100-500ms
- Critical (RPM, Speed): 50-100ms
- Background logging: 1s minimum
- Export format: CSV with timestamp

#### Connection Stability
- Auto-reconnect on disconnect
- Handle Astra J's specific ECU behavior
- Warning for cheap adapters
- Recommend OBDLink or Veepeak adapters

---

## Key Sources & References

1. **Torque Wiki** - torque-bhp.com/wiki
2. **Car Scanner Documentation** - carscanner.info
3. **OBD Auto Doctor** - obdautodoctor.com
4. **Google Play Store** - App descriptions and reviews
5. **Vauxhall Owners Network Forum**
6. **Opel Owners Community**

---

## Appendix: Standard OBD-II PIDs (SAE J1979)

| PID | Name | Formula | Unit |
|-----|------|---------|------|
| 01 | Monitor Status | Bit-encoded | - |
| 02 | Freeze DTC | 2 bytes | - |
| 03 | Fuel System | Bit-encoded | - |
| 04 | Calculated Load | A * 100 / 255 | % |
| 05 | Coolant Temp | A - 40 | °C |
| 06 | Short Term FT Bank 1 | (A - 128) * 100 / 128 | % |
| 07 | Long Term FT Bank 1 | (A - 128) * 100 / 128 | % |
| 0B | Intake MAP | A | kPa |
| 0C | Engine RPM | (A * 256 + B) / 4 | rpm |
| 0D | Vehicle Speed | A | km/h |
| 0E | Timing Advance | A / 2 - 64 | ° |
| 0F | IAT | A - 40 | °C |
| 10 | MAF | (A * 256 + B) / 100 | g/s |
| 11 | Throttle Position | A * 100 / 255 | % |
| 1F | Run Time | A * 256 + B | sec |
| 21 | MIL Distance | A * 256 + B | km |
| 2F | Fuel Level | A * 100 / 255 | % |
| 33 | Barometric | A | kPa |
| 42 | ECU Voltage | (A * 256 + B) / 1000 | V |
| 5C | Oil Temp | A - 40 | °C |
| 5E | Fuel Rate | (A * 256 + B) / 20 | L/h |

---

*Research compiled: May 2026*
*For canop-obd Android app development - Opel Astra J*
