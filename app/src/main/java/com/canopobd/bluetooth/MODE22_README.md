# Mode 22 OBD-II Support Documentation

## Overview

Mode 22 (also known as Service 22) is a manufacturer-specific diagnostic mode defined in SAE J2190 
that provides enhanced data not available through standard Mode 01 PIDs. This implementation 
adds Mode 22 support for GM/Opel vehicles equipped with Bosch ME17.x ECUs.

## Mode 22 Format

### Command Structure
```
22XXXX
```
- `22`: Mode 22 header (manufacturer-specific)
- `XXXX`: 4-digit hexadecimal PID code

### Response Structure
```
62XXXXYY...
```
- `62`: Positive response (0x22 + 0x40 = 0x62)
- `XXXX`: Echo of the requested PID
- `YY...`: Data bytes (variable length)

## Supported PIDs for GM/Opel Bosch ME17

### Vehicle Information PIDs
| PID Code | Description | Bytes | Unit |
|----------|-------------|-------|------|
| F190 | VIN (Vehicle Identification Number) | 17 | ASCII |
| F191 | Calibration ID | 16 | ASCII |
| F192 | CVN (Calibration Verification Number) | 4 | HEX |

### Turbo/Engine PIDs (Primary)
| PID Code | Description | Bytes | Unit |
|----------|-------------|-------|------|
| 220001 | Engine Torque | 2 | Nm |
| 220002 | Turbo Boost Actual | 2 | kPa |
| 220003 | Turbo Boost Target | 2 | kPa |
| 220004 | Wastegate Duty Cycle | 1 | % |
| 220005 | Turbo Speed | 2 | RPM |
| 220006 | Turbo Inlet Temperature | 1 | °C |
| 220007 | Turbo Outlet Temperature | 1 | °C |
| 220008 | Charge Air Temperature | 1 | °C |
| 220009 | VGT Position | 1 | % |
| 22000A | Turbo Efficiency | 1 | % |

### Fuel System PIDs
| PID Code | Description | Bytes | Unit |
|----------|-------------|-------|------|
| 221001 | Fuel Rail Pressure | 2 | kPa |
| 221002 | Fuel Temperature | 1 | °C |
| 221003 | Fuel Pressure | 1 | kPa |
| 221004 | Injection Quantity | 2 | mg/stroke |
| 221005 | Injection Timing | 2 | ° |

### Catalyst/Axhaust PIDs
| PID Code | Description | Bytes | Unit |
|----------|-------------|-------|------|
| 222001 | Catalyst Temp Bank 1 Sensor 1 | 2 | °C |
| 222002 | Catalyst Temp Bank 1 Sensor 2 | 2 | °C |
| 222003 | Catalyst Temp Bank 2 Sensor 1 | 2 | °C |
| 222004 | Catalyst Temp Bank 2 Sensor 2 | 2 | °C |

### Sensor PIDs
| PID Code | Description | Bytes | Unit |
|----------|-------------|-------|------|
| 223001 | Ambient Air Temperature | 1 | °C |
| 223002 | Engine Oil Temperature | 1 | °C |
| 223003 | Engine Oil Pressure | 1 | kPa |
| 223004 | Transmission Fluid Temperature | 1 | °C |

### Lambda/AFR PIDs
| PID Code | Description | Bytes | Unit |
|----------|-------------|-------|------|
| 225001 | Wideband Lambda Bank 1 | 2 | λ |
| 225002 | Wideband Lambda Bank 2 | 2 | λ |
| 225003 | Target Lambda | 1 | λ |

## Usage Examples

### Basic Mode 22 Request
```kotlin
val connection: ELM327BTConnection = ...

// Read single Mode 22 PID
val boost = connection.requestMode22PID("0002")

// Read multiple PIDs
val pids = connection.readMultipleMode22PIDs(listOf("0002", "0003", "0004"))
```

### Turbo Monitoring
```kotlin
// Read all turbo-related data
val turboData = connection.readTurboMonitoringData()

// Access the data
println("Boost: ${turboData.boostBar} bar")
println("Wastegate: ${turboData.wastegateDuty}%")
println("Turbo Speed: ${turboData.turboSpeed} RPM")
```

### VIN Retrieval
```kotlin
// Read VIN via Mode 22 (alternative to Mode 09)
val vin = connection.readVINMode22()
```

### Discover Supported PIDs
```kotlin
// Discover which Mode 22 PIDs the ECU supports
val supportedPids = connection.discoverMode22PIDs()
println("Supported: $supportedPids")
```

## Known Compatible Vehicles

This Mode 22 implementation has been designed for the following vehicles:

- **Opel Astra J (2010-2018)**: 1.4L Turbo (A14NET/LUJ) with Bosch ME17.9.22 ECU
- **Opel Insignia A (2008-2017)**: Various engines with Bosch ME17.x ECU
- **Vauxhall Astra H (2004-2010)**: 1.6L Turbo (Z16LET) with Bosch ME7.x ECU
- **Chevrolet Cruze (2008-2015)**: 1.4L/1.8L with Bosch ME17.x ECU

## Important Notes

1. **ECU Compatibility**: Not all ECUs support Mode 22. Use `discoverMode22PIDs()` 
   to check available PIDs before requesting data.

2. **Response Time**: Mode 22 responses may be slower than Mode 01. The implementation
   includes appropriate timeouts and retries.

3. **Error Handling**: If a PID returns ERROR, it may not be supported by the ECU.
   The implementation logs warnings but does not throw exceptions.

4. **Protocol**: Mode 22 works with both ISO 9141-2 and CAN protocols. The ELM327
   adapter handles protocol translation automatically.

## Troubleshooting

### Common Issues

1. **"ERROR" response for all Mode 22 PIDs**
   - The ECU may not support Mode 22
   - Try Mode 01 (standard PIDs) instead
   - Check vehicle compatibility

2. **Slow or intermittent responses**
   - Increase COMMAND_TIMEOUT_MS in ELM327BTConnection
   - Reduce batch size in readMultipleMode22PIDs()

3. **Incorrect values**
   - Verify PID formulas match your ECU
   - Some ECUs use different scaling factors

## References

- SAE J2190: Enhanced OBD-II (Mode 22)
- Bosch ME17.x Technical Documentation
- GM Global Diagnostic System (GDS) Protocol
