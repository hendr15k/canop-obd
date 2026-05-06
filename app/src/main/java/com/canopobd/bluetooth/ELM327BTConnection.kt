package com.canopobd.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.canopobd.data.model.DTCResponse
import com.canopobd.data.model.DiagnosticTroubleCode
import com.canopobd.data.model.FreezeFrame
import com.canopobd.data.model.OBDPID
import com.canopobd.data.model.ReadinessMonitor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class ELM327BTConnection(
    private val bluetoothAdapter: BluetoothAdapter
) {
    companion object {
        private const val TAG = "ELM327"
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val MAX_RETRIES = 3
        private const val COMMAND_TIMEOUT_MS = 3000L
        private const val CONNECT_TIMEOUT_MS = 15000L
        private const val INITIAL_RETRY_DELAY_MS = 100L
        private const val MAX_RETRY_DELAY_MS = 500L

        private val DTC_DESCRIPTIONS = mapOf(
            "P0100" to "Mass Air Flow Circuit Malfunction",
            "P0101" to "Mass Air Flow Circuit Range/Performance Problem",
            "P0102" to "Mass Air Flow Circuit Low Input",
            "P0103" to "Mass Air Flow Circuit High Input",
            "P0110" to "Intake Air Temperature Circuit Malfunction",
            "P0111" to "Intake Air Temperature Circuit Low Input",
            "P0112" to "Intake Air Temperature Circuit High Input",
            "P0113" to "Intake Air Temperature Circuit Intermittent",
            "P0115" to "Engine Coolant Temperature Circuit Malfunction",
            "P0116" to "Engine Coolant Temperature Range/Performance",
            "P0117" to "Engine Coolant Temperature Low Input",
            "P0118" to "Engine Coolant Temperature High Input",
            "P0120" to "Throttle Position Sensor Circuit Malfunction",
            "P0121" to "Throttle Position Sensor Range/Performance",
            "P0122" to "Throttle Position Sensor Circuit Low Input",
            "P0123" to "Throttle Position Sensor Circuit High Input",
            "P0130" to "O2 Sensor Circuit Malfunction (Bank 1 Sensor 1)",
            "P0131" to "O2 Sensor Circuit Low Voltage (Bank 1 Sensor 1)",
            "P0132" to "O2 Sensor Circuit High Voltage (Bank 1 Sensor 1)",
            "P0133" to "O2 Sensor Slow Response (Bank 1 Sensor 1)",
            "P0134" to "O2 Sensor No Activity Detected (Bank 1 Sensor 1)",
            "P0135" to "O2 Sensor Heater Circuit Malfunction (Bank 1 Sensor 1)",
            "P0136" to "O2 Sensor Circuit Malfunction (Bank 1 Sensor 2)",
            "P0137" to "O2 Sensor Circuit Low Voltage (Bank 1 Sensor 2)",
            "P0138" to "O2 Sensor Circuit High Voltage (Bank 1 Sensor 2)",
            "P0170" to "Fuel System Too Rich (Bank 1)",
            "P0171" to "Fuel System Too Lean (Bank 1)",
            "P0172" to "Fuel System Too Rich (Bank 1)",
            "P0173" to "Fuel System Too Rich (Bank 2)",
            "P0174" to "Fuel System Too Lean (Bank 2)",
            "P0175" to "Fuel System Too Rich (Bank 2)",
            "P0300" to "Random/Multiple Cylinder Misfire Detected",
            "P0301" to "Cylinder 1 Misfire Detected",
            "P0302" to "Cylinder 2 Misfire Detected",
            "P0303" to "Cylinder 3 Misfire Detected",
            "P0304" to "Cylinder 4 Misfire Detected",
            "P0305" to "Cylinder 5 Misfire Detected",
            "P0306" to "Cylinder 6 Misfire Detected",
            "P0307" to "Cylinder 7 Misfire Detected",
            "P0308" to "Cylinder 8 Misfire Detected",
            "P0325" to "Knock Sensor 1 Circuit Malfunction",
            "P0326" to "Knock Sensor 1 Range/Performance",
            "P0330" to "Knock Sensor 2 Circuit Malfunction",
            "P0335" to "Crankshaft Position Sensor Circuit Malfunction",
            "P0336" to "Crankshaft Position Sensor Range/Performance",
            "P0340" to "Camshaft Position Sensor Circuit Malfunction",
            "P0341" to "Camshaft Position Sensor Range/Performance",
            "P0400" to "EGR Flow Malfunction",
            "P0401" to "EGR Insufficient Flow Detected",
            "P0402" to "EGR Excessive Flow Detected",
            "P0403" to "EGR Control Circuit Malfunction",
            "P0404" to "EGR Control Range/Performance",
            "P0405" to "EGR Sensor A Circuit Low",
            "P0406" to "EGR Sensor A Circuit High",
            "P0407" to "EGR Sensor B Circuit Low",
            "P0408" to "EGR Sensor B Circuit High",
            "P0420" to "Catalyst System Efficiency Below Threshold (Bank 1)",
            "P0421" to "Catalyst System Efficiency Below Threshold (Bank 1)",
            "P0422" to "Catalyst System Efficiency Below Threshold (Bank 1)",
            "P0430" to "Catalyst System Efficiency Below Threshold (Bank 2)",
            "P0440" to "Evaporative Emission System Malfunction",
            "P0441" to "Evaporative Emission System Purge Flow Malfunction",
            "P0442" to "Evaporative Emission System Leak Detected (Small Leak)",
            "P0443" to "Evaporative Emission System Purge Control Circuit Malfunction",
            "P0444" to "Evaporative Emission System Purge Control Circuit Low",
            "P0445" to "Evaporative Emission System Purge Control Circuit High",
            "P0446" to "Evaporative Emission System Vent Control Malfunction",
            "P0447" to "Evaporative Emission System Vent Control Circuit Low",
            "P0448" to "Evaporative Emission System Vent Control Circuit High",
            "P0450" to "Evaporative Emission System Pressure Sensor Malfunction",
            "P0451" to "Evaporative Emission System Pressure Sensor Range/Performance",
            "P0452" to "Evaporative Emission System Pressure Sensor Low Input",
            "P0453" to "Evaporative Emission System Pressure Sensor High Input",
            "P0455" to "Evaporative Emission System Leak Detected (Gross Leak)",
            "P0456" to "Evaporative Emission System Leak Detected (Very Small Leak)",
            "P0500" to "Vehicle Speed Sensor Malfunction",
            "P0501" to "Vehicle Speed Sensor Range/Performance",
            "P0502" to "Vehicle Speed Sensor Low Input",
            "P0503" to "Vehicle Speed Sensor Intermittent/Bumpy",
            "P0505" to "Idle Control System Malfunction",
            "P0506" to "Idle Control System RPM Lower Than Expected",
            "P0507" to "Idle Control System RPM Higher Than Expected",
            "P0508" to "Idle Control System RPM Too Low",
            "P0509" to "Idle Control System RPM Too High",
            "P0510" to "Throttle Position Sensor Malfunction",
            "P0600" to "Serial Communication Link Malfunction",
            "P0601" to "Control Module Read Only Memory (ROM) Error",
            "P0602" to "Control Module Programming Error",
            "P0603" to "Control Module Keep Alive Memory (KAM) Error",
            "P0604" to "Control Module Random Access Memory (RAM) Error",
            "P0605" to "Control Module Read Only Memory (ROM) Error",
            "P0606" to "PCM Processor Fault",
            "P0700" to "Transmission Control System Malfunction",
            "P0703" to "Torque Converter Clutch Solenoid Circuit Malfunction",
            "P0705" to "Transmission Range Sensor Circuit Malfunction",
            "P0707" to "Transmission Range Sensor Low Input",
            "P0708" to "Transmission Range Sensor High Input",
            "P0710" to "Transmission Fluid Temperature Sensor Malfunction",
            "P0715" to "Input/Turbine Speed Sensor Circuit Malfunction",
            "P0717" to "Input/Turbine Speed Sensor No Signal",
            "P0720" to "Output Speed Sensor Circuit Malfunction",
            "P0722" to "Output Speed Sensor No Signal",
            "P0725" to "Engine Speed Input Circuit Malfunction",
            "P0730" to "Incorrect Gear Ratio",
            "P0731" to "Gear 1 Incorrect Ratio",
            "P0732" to "Gear 2 Incorrect Ratio",
            "P0733" to "Gear 3 Incorrect Ratio",
            "P0734" to "Gear 4 Incorrect Ratio",
            "P0735" to "Gear 5 Incorrect Ratio",
            "P0740" to "Torque Converter Clutch Solenoid Circuit Malfunction",
            "P0741" to "Torque Converter Clutch Solenoid Performance",
            "P0742" to "Torque Converter Clutch Solenoid Stuck On",
            "P0743" to "Torque Converter Clutch Solenoid Circuit Electrical",
            "P0750" to "Shift Solenoid A Malfunction",
            "P0751" to "Shift Solenoid A Performance/No Shift",
            "P0752" to "Shift Solenoid A Stuck On",
            "P0753" to "Shift Solenoid A Electrical",
            "P0755" to "Shift Solenoid B Malfunction",
            "P0756" to "Shift Solenoid B Performance/No Shift",
            "P0757" to "Shift Solenoid B Stuck On",
            "P0758" to "Shift Solenoid B Electrical",
            "C0000" to "TCS Malfunction",
            "C0035" to "Left Front Wheel Speed Sensor Malfunction",
            "C0040" to "Right Front Wheel Speed Sensor Malfunction",
            "C0045" to "Left Rear Wheel Speed Sensor Malfunction",
            "C0050" to "Right Rear Wheel Speed Sensor Malfunction",
            "C0060" to "Left Front ABS Solenoid Malfunction",
            "C0065" to "Right Front ABS Solenoid Malfunction",
            "C0070" to "Left Rear ABS Solenoid Malfunction",
            "C0075" to "Right Rear ABS Solenoid Malfunction",
            "C0080" to "ABS Pump Motor Malfunction",
            "C0085" to "ABS Pump Motor Speed Sensor Malfunction",
            "C0090" to "Left Front Wheel Speed Sensor Signal Malfunction",
            "B0001" to "Driver Airbag Circuit Resistance Low",
            "B0002" to "Driver Airbag Circuit Resistance High",
            "B0003" to "Driver Airbag Circuit Open",
            "B0004" to "Driver Airbag Circuit Short to Ground",
            "B0005" to "Driver Airbag Circuit Short to Power",
            "B0010" to "Passenger Airbag Circuit Resistance Low",
            "B0011" to "Passenger Airbag Circuit Resistance High",
            "B0100" to "Interior Lamp Circuit Malfunction",
            "B0101" to "Headlamp Relay Circuit Malfunction",
            "U0001" to "High Speed CAN Communication Bus Malfunction",
            "U0100" to "Lost Communication With ECM/PCM",
            "U0101" to "Lost Communication With TCM",
            "U0121" to "Lost Communication With ABS Module",
            "U0140" to "Lost Communication With BCM",
            "U0155" to "Lost Communication With Instrument Cluster",
            "P0100" to "Mass Air Flow Circuit Malfunction",
            "P0101" to "Mass Air Flow Circuit Range/Performance Problem",
            "P0102" to "Mass Air Flow Circuit Low Input",
            "P0103" to "Mass Air Flow Circuit High Input",
            "P0170" to "Fuel System Too Rich (Bank 1)",
            "P0171" to "Fuel System Too Lean (Bank 1)",
            "P0172" to "Fuel System Too Rich (Bank 1)",
            "P0173" to "Fuel System Too Rich (Bank 2)",
            "P0174" to "Fuel System Too Lean (Bank 2)",
            "P0234" to "Turbocharger/Supercharger Overboost Condition",
            "P0235" to "Turbocharger Boost Sensor Circuit Malfunction",
            "P0236" to "Turbocharger Boost Sensor Range/Performance",
            "P0237" to "Turbocharger Boost Sensor Low Input",
            "P0238" to "Turbocharger Boost Sensor High Input",
            "P0243" to "Turbocharger Wastegate Solenoid A Malfunction",
            "P0245" to "Turbocharger Wastegate Solenoid A Low Input",
            "P0246" to "Turbocharger Wastegate Solenoid A High Input",
            "P0298" to "Engine Oil Temperature Too Low",
            "P0299" to "Turbocharger/Supercharger Underboost Condition",
            "P0500" to "Vehicle Speed Sensor Malfunction",
            "P0506" to "Idle Control System RPM Lower Than Expected",
            "P0507" to "Idle Control System RPM Higher Than Expected",
            "P0562" to "System Voltage Low",
            "P0563" to "System Voltage High",
            "P0600" to "Serial Communication Link Malfunction",
            "P0601" to "Control Module Read Only Memory Error",
            "P0602" to "Control Module Programming Error",
            "P0606" to "ECM/PCM Processor Fault",
            "P0685" to "ECM Relay Control Circuit Malfunction",
            "P1100" to "MAF Sensor Intermittent/Erratic High",
            "P1101" to "MAF Sensor Out of Self-Test Range",
            "P1299" to "Cylinder Head Overtemperature Condition",
            "P1489" to "High Speed Fan Control Circuit Malfunction",
            "P1490" to "Low Speed Fan Control Circuit Malfunction",
            "P1491" to "Fan System Performance",
            "P1516" to "Intake Manifold Tuning Valve Performance",
            "P1517" to "Intake Manifold Tuning Valve Control Circuit",
            "P1518" to "Intake Manifold Tuning Valve Stuck Open",
            "P1519" to "Intake Manifold Tuning Valve Stuck Closed",
            "P1520" to "Intake Manifold Tuning Valve Circuit Malfunction",
            "P1523" to "Variable Load Control Solenoid A Circuit",
            "P1524" to "Variable Load Control Solenoid A Range/Performance",
            "P1549" to "Turbocharger Boost Control Valve Malfunction",
            "P1550" to "O2 Sensor Heater Circuit Malfunction (Bank 1)",
            "P1571" to "Brake Pedal Switch Signal Malfunction",
            "P1572" to "Brake Vacuum Pressure Sensor Circuit",
            "P1573" to "Engine Torque Signal Circuit Malfunction",
            "P1574" to "Engine Torque Signal Range/Performance",
            "P1593" to "Loss of Intake Air Flow Signal",
            "P1594" to "Throttle Position Sensor B Circuit",
            "P1595" to "Throttle Position Sensor B Range/Performance",
            "P1596" to "Throttle Position Sensor B Low Input",
            "P1597" to "Throttle Position Sensor B High Input",
            "P1600" to "Loss of Serial Communication",
            "P1604" to "Control Module RAM Error",
            "P1605" to "Control Module ROM Error",
            "P1606" to "ECM Control Relay Circuit Malfunction",
            "P1631" to "Throttle Position Sensor Performance",
            "P1633" to "Throttle Position Sensor Range/Performance",
            "P1639" to "Throttle Position Sensor B Performance",
            "P1640" to "TCM Control Relay Circuit",
            "P1655" to "Variable Load Control Solenoid B Circuit",
            "P1656" to "Variable Load Control Solenoid B Range/Performance",
            "P1657" to "Variable Load Control Solenoid B Malfunction",
            "P1658" to "Wastegate Solenoid B Circuit",
            "P1659" to "Wastegate Solenoid B Range/Performance",
            "P0075" to "Intake Valve Control Solenoid Circuit (Bank 1)",
            "P0076" to "Intake Valve Control Solenoid Circuit Low (Bank 1)",
            "P0077" to "Intake Valve Control Solenoid Circuit High (Bank 1)",
            "P0078" to "Exhaust Valve Control Solenoid Circuit (Bank 1)",
            "P0079" to "Exhaust Valve Control Solenoid Circuit Low (Bank 1)",
            "P007A" to "Intake Air Temperature Sensor Circuit Range/Performance",
            "P007B" to "Intake Air Temperature Sensor Circuit Range/Performance (Bank 2)",
            "P007C" to "Intake Air Temperature Sensor Circuit Low (Bank 1)",
            "P007D" to "Intake Air Temperature Sensor Circuit High (Bank 1)",
            "P0087" to "Fuel Rail Pressure Sensor Circuit Low",
            "P0088" to "Fuel Rail Pressure Sensor Circuit High",
            "P0089" to "Fuel Pressure Regulator Performance",
            "P0090" to "Fuel Pressure Regulator Control Circuit",
            "P0091" to "Fuel Pressure Regulator Control Circuit Low",
            "P0092" to "Fuel Pressure Regulator Control Circuit High",
            "P0093" to "Fuel System Leak Detected - Large Leak",
            "P0094" to "Fuel System Leak Detected - Small Leak",
            "P0095" to "Intake Air Temperature Sensor 2 Circuit Range/Performance",
            "P0096" to "Intake Air Temperature Sensor 2 Circuit Range/Performance",
            "P0097" to "Intake Air Temperature Sensor 2 Circuit Low",
            "P0098" to "Intake Air Temperature Sensor 2 Circuit High",
            "P0099" to "Intake Air Temperature Sensor 2 Circuit Intermittent",
            "P009A" to "Intake Air Temperature Sensor Circuit Range/Performance (Bank 2)",
            "P009B" to "Intake Air Temperature Sensor Circuit Low (Bank 2)",
            "P009C" to "Intake Air Temperature Sensor Circuit High (Bank 2)",
            "P009D" to "Intake Air Temperature Sensor Circuit Intermittent (Bank 2)",
            "P0298" to "Engine Oil Temperature Too Low",
            "P0299" to "Turbocharger/Supercharger Underboost Condition",
            "P1105" to "MAP Sensor Reference Circuit",
            "P1106" to "MAP Sensor Circuit Range/Performance",
            "P1107" to "MAP Sensor Circuit Low Input",
            "P1108" to "MAP Sensor Circuit High Input",
            "P1110" to "Intake Air Temperature Sensor Circuit",
            "P1111" to "Intake Air Temperature Sensor Circuit Range/Performance",
            "P1112" to "Intake Air Temperature Sensor Circuit Low",
            "P1113" to "Intake Air Temperature Sensor Circuit High",
            "P1114" to "Intake Air Temperature Sensor 2 Circuit Low",
            "P1115" to "Intake Air Temperature Sensor 2 Circuit High",
            "P1116" to "Intake Air Temperature Sensor Circuit Range/Performance",
            "P1117" to "Intake Air Temperature Sensor Circuit Intermittent",
            "P1120" to "Accelerator Pedal Position Sensor 1",
            "P1121" to "Accelerator Pedal Position Sensor 1 Range/Performance",
            "P1122" to "Accelerator Pedal Position Sensor 1 Low",
            "P1123" to "Accelerator Pedal Position Sensor 1 High",
            "P1125" to "Accelerator Pedal Position Sensor 2",
            "P1126" to "Accelerator Pedal Position Sensor 2 Range/Performance",
            "P1127" to "Accelerator Pedal Position Sensor 2 Low",
            "P1128" to "Accelerator Pedal Position Sensor 2 High",
            "P1130" to "O2 Sensor Adaptive Trim Lean Limit (Bank 1 Sensor 1)",
            "P1131" to "O2 Sensor 1 Insufficient Switching - Lean (Bank 1)",
            "P1132" to "O2 Sensor 1 Insufficient Switching - Rich (Bank 1)",
            "P1133" to "O2 Sensor 1 Insufficient Switching",
            "P1134" to "O2 Sensor 1 Transition Time",
            "P1135" to "O2 Sensor 1 Heater Circuit (Bank 1)",
            "P1136" to "O2 Sensor 2 Heater Circuit (Bank 1)",
            "P1137" to "O2 Sensor 2 Heater Circuit Low (Bank 1)",
            "P1138" to "O2 Sensor 2 Heater Circuit High (Bank 1)",
            "P1161" to "O2 Sensor Cross Counts (Bank 1 Sensor 1)",
            "P1162" to "O2 Sensor 1 Heater Performance (Bank 1)",
            "P1171" to "Fuel Trim Lean at WOT",
            "P1172" to "Fuel Trim Rich at WOT",
            "P1173" to "O2 Sensor 2 Heater Performance (Bank 1)",
            "P1187" to "Engine Oil Temperature Sensor Circuit Low",
            "P1188" to "Engine Oil Temperature Sensor Circuit High",
            "P1189" to "Engine Oil Temperature Sensor Circuit Range/Performance",
            "P1190" to "Engine Oil Temperature Sensor Circuit Intermittent",
            "P1240" to "Turbo Boost Sensor Performance",
            "P1241" to "Turbo Boost Pressure Low",
            "P1242" to "Turbo Boost Pressure High (Overboost)",
            "P1243" to "Turbocharger Speed Sensor Circuit",
            "P1244" to "Turbocharger Speed Sensor Low",
            "P1245" to "Turbocharger Speed Sensor High",
            "P1246" to "Turbocharger Speed Sensor Range/Performance",
            "P1247" to "Turbo Boost Pressure Control Performance",
            "P1248" to "Turbo Boost Pressure Control Not Detected",
            "P1249" to "Turbocharger Boost Pressure Actuator A Circuit",
            "P1250" to "Turbocharger Boost Pressure Actuator A Circuit Low",
            "P1251" to "Turbocharger Boost Pressure Actuator A Circuit High",
            "P1252" to "Turbocharger Boost Pressure Actuator A Range/Performance",
            "P1253" to "Turbocharger Boost Pressure Actuator A Stuck",
            "P1254" to "Turbocharger Boost Pressure Actuator A Stuck Open",
            "P1255" to "Turbocharger Boost Pressure Actuator A Stuck Closed",
            "P1271" to "Throttle Position Sensor (TPS) Adaptation",
            "P1272" to "Throttle Adaptation Not Learned",
            "P1273" to "Throttle Position Sensor Adaptation Range",
            "P1274" to "Throttle Position Sensor 1-2 Correlation",
            "P1275" to "Throttle Body Control (Electronic)",
            "P1276" to "Throttle Body Control Adaptation (2nd)",
            "P1277" to "Throttle Body Control Range/Performance",
            "P1278" to "Throttle Body Control Learned Position Not Set",
            "P1345" to "Camshaft-Crankshaft Correlation (Bank 1)",
            "P1346" to "Camshaft Position Sensor Circuit Range/Performance",
            "P1347" to "Crankshaft Position - Camshaft Position Correlation (Bank 1)",
            "P1351" to "Ignition Coil Control Circuit (Cylinder 1)",
            "P1352" to "Ignition Coil Control Circuit (Cylinder 2)",
            "P1353" to "Ignition Coil Control Circuit (Cylinder 3)",
            "P1354" to "Ignition Coil Control Circuit (Cylinder 4)",
            "P1355" to "Ignition Coil Primary Circuit (All Cylinders)",
            "P1356" to "Ignition Coil Secondary Circuit (Cylinder 1)",
            "P1357" to "Ignition Coil Secondary Circuit (Cylinder 2)",
            "P1358" to "Ignition Coil Secondary Circuit (Cylinder 3)",
            "P1359" to "Ignition Coil Secondary Circuit (Cylinder 4)",
            "P1361" to "Control Module Ignition Bypass Circuit",
            "P1362" to "Ignition Coil Control Circuit (Cylinder 1) - No Primary Voltage",
            "P1363" to "Ignition Coil Control Circuit (Cylinder 2) - No Primary Voltage",
            "P1364" to "Ignition Coil Control Circuit (Cylinder 3) - No Primary Voltage",
            "P1365" to "Ignition Coil Control Circuit (Cylinder 4) - No Primary Voltage",
            "P1366" to "Ignition Coil Control Circuit Low (Cylinder 1)",
            "P1367" to "Ignition Coil Control Circuit Low (Cylinder 2)",
            "P1368" to "Ignition Coil Control Circuit Low (Cylinder 3)",
            "P1369" to "Ignition Coil Control Circuit Low (Cylinder 4)",
            "P1370" to "Camshaft Position Sensor Circuit (Cylinder 1)",
            "P1371" to "Camshaft Position Sensor Range/Performance (Cylinder 1)",
            "P1372" to "Camshaft Position Sensor Circuit (Cylinder 2)",
            "P1373" to "Camshaft Position Sensor Range/Performance (Cylinder 2)",
            "P1374" to "Crankshaft Position Sensor Circuit (3X Signal)",
            "P1375" to "Crankshaft Position Sensor Circuit (24X Signal)",
            "P1376" to "Crankshaft Position Sensor Circuit Intermittent",
            "P1404" to "EGR Valve Position Sensor Circuit",
            "P1405" to "EGR Valve Position Sensor Circuit Range/Performance",
            "P1406" to "EGR Valve Position Sensor Circuit Intermittent",
            "P1407" to "EGR Valve Position Sensor Circuit Low",
            "P1408" to "EGR Valve Position Sensor Circuit High",
            "P1410" to "Exhaust Gas Recirculation Flow",
            "P1411" to "Exhaust Gas Recirculation Flow Insufficient",
            "P1412" to "Exhaust Gas Recirculation Flow Excessive",
            "P1413" to "Exhaust Gas Recirculation Control Circuit Low",
            "P1414" to "Exhaust Gas Recirculation Control Circuit High",
            "P1415" to "Secondary Air Injection System (Bank 1)",
            "P1416" to "Secondary Air Injection System (Bank 2)",
            "P1516" to "Intake Manifold Tuning Valve Performance",
            "P1517" to "Intake Manifold Tuning Valve Control Circuit",
            "P1518" to "Intake Manifold Tuning Valve Stuck Open",
            "P1519" to "Intake Manifold Tuning Valve Stuck Closed",
            "P1520" to "Intake Manifold Tuning Valve Circuit Malfunction",
            "P1521" to "Intake Manifold Tuning Valve Performance",
            "P1522" to "Intake Manifold Tuning Valve Control Circuit Range",
            "P1523" to "Throttle Body Control (Limp-Home Mode)",
            "P1524" to "Throttle Body Control Adaptation Not Learned",
            "P1526" to "Accelerator Pedal Position Sensor 1-2 Correlation",
            "P1527" to "Accelerator Pedal Position Sensor Range/Performance",
            "P1528" to "Accelerator Pedal Position Sensor 1 Low",
            "P1529" to "Accelerator Pedal Position Sensor 1 High",
            "P1530" to "Accelerator Pedal Position Sensor 2 Low",
            "P1531" to "Accelerator Pedal Position Sensor 2 High",
            "P1549" to "Turbocharger Boost Control Valve Malfunction",
            "P1550" to "O2 Sensor Heater Circuit Malfunction (Bank 1)",
            "P1554" to "O2 Sensor Heater Performance (Bank 1 Sensor 1)",
            "P1555" to "O2 Sensor Heater Performance (Bank 1 Sensor 2)",
            "P1571" to "Brake Pedal Switch Signal Malfunction",
            "P1572" to "Brake Vacuum Pressure Sensor Circuit",
            "P1573" to "Engine Torque Signal Circuit Malfunction",
            "P1574" to "Engine Torque Signal Range/Performance",
            "P1593" to "Loss of Intake Air Flow Signal",
            "P1594" to "Throttle Position Sensor B Circuit",
            "P1595" to "Throttle Position Sensor B Range/Performance",
            "P1596" to "Throttle Position Sensor B Low Input",
            "P1597" to "Throttle Position Sensor B High Input",
            "P1598" to "Accelerator Pedal Position Sensor 1-2 Voltage Correlation",
            "P1599" to "Accelerator Pedal Position Sensor 1-2 Signal Correlation",
            "P1600" to "Loss of Serial Communication",
            "P1601" to "Serial Communication Link Malfunction",
            "P1602" to "ECM/PCM Serial Communication Circuit",
            "P1603" to "ECM/PCM Internal Fault",
            "P1604" to "Control Module RAM Error",
            "P1605" to "Control Module ROM Error",
            "P1606" to "ECM Control Relay Circuit Malfunction",
            "P1607" to "ECM Control Relay Performance",
            "P1608" to "ECM Control Relay Circuit Low",
            "P1609" to "ECM Control Relay Circuit High",
            "P1621" to "Malfunction Indicator Lamp (MIL) Circuit",
            "P1622" to "Malfunction Indicator Lamp (MIL) Circuit Low",
            "P1623" to "Malfunction Indicator Lamp (MIL) Circuit High",
            "P1626" to "Theft Deterrent System Signal Missing",
            "P1627" to "Theft Deterrent System Performance",
            "P1628" to "Theft Deterrent System Communication",
            "P1629" to "Theft Deterrent System Starter Disable Circuit",
            "P1630" to "Theft Deterrent System Fuel Disable Circuit",
            "P1631" to "Throttle Position Sensor Performance",
            "P1632" to "Throttle Position Sensor Circuit - Signal Invalid",
            "P1633" to "Throttle Position Sensor Range/Performance",
            "P1634" to "Throttle Position Sensor 1 Circuit - Signal Low",
            "P1635" to "Throttle Position Sensor 1 Circuit - Signal High",
            "P1636" to "Throttle Position Sensor 2 Circuit - Signal Low",
            "P1637" to "Throttle Position Sensor 2 Circuit - Signal High",
            "P1638" to "Throttle Position Sensor 2 Circuit - Signal Invalid",
            "P1639" to "Throttle Position Sensor B Performance",
            "P1640" to "TCM Control Relay Circuit",
            "P1641" to "Throttle Actuator Control Motor Circuit",
            "P1642" to "Throttle Actuator Control Motor Circuit Range/Performance",
            "P1643" to "Throttle Actuator Control Motor Circuit Low",
            "P1644" to "Throttle Actuator Control Motor Circuit High",
            "P1645" to "Throttle Actuator Control Motor Circuit Open",
            "P1646" to "Throttle Actuator Control System - Idle Speed Low",
            "P1647" to "Throttle Actuator Control System - Idle Speed High",
            "P1648" to "Throttle Actuator Control System - Limp Home Mode",
            "P1655" to "Variable Load Control Solenoid B Circuit",
            "P1656" to "Variable Load Control Solenoid B Range/Performance",
            "P1657" to "Variable Load Control Solenoid B Malfunction",
            "P1658" to "Wastegate Solenoid B Circuit",
            "P1659" to "Wastegate Solenoid B Range/Performance",
            "P2100" to "Throttle Actuator Control Motor Circuit Open",
            "P2101" to "Throttle Actuator Control Motor Circuit Range/Performance",
            "P2102" to "Throttle Actuator Control Motor Circuit Low",
            "P2103" to "Throttle Actuator Control Motor Circuit High",
            "P2105" to "Throttle Actuator Control System - Forced Engine Shutdown",
            "P2106" to "Throttle Actuator Control System - Forced Limited Power",
            "P2107" to "Throttle Actuator Control System - Forced Idle",
            "P2108" to "Throttle Actuator Control System - Forced Engine Shutdown",
            "P2109" to "Throttle Position Sensor Minimum Stop Performance",
            "P2110" to "Throttle Actuator Control System - Forced Reduced Engine Power",
            "P2111" to "Throttle Actuator Control System - Failed Open",
            "P2112" to "Throttle Actuator Control System - Failed Closed",
            "P2113" to "Throttle Position Sensor C Performance",
            "P2114" to "Throttle Position Sensor C Range/Performance",
            "P2115" to "Throttle Position Sensor C Low",
            "P2116" to "Throttle Position Sensor C High",
            "P2117" to "Throttle Position Sensor C Intermittent",
            "P2118" to "Throttle Actuator Control Motor Current Range",
            "P2119" to "Throttle Actuator Control Body Range/Performance",
            "P2120" to "Throttle/Pedal Position Sensor D Circuit",
            "P2121" to "Throttle/Pedal Position Sensor D Range/Performance",
            "P2122" to "Throttle/Pedal Position Sensor D Circuit Low",
            "P2123" to "Throttle/Pedal Position Sensor D Circuit High",
            "P2124" to "Throttle/Pedal Position Sensor D Circuit Intermittent",
            "P2125" to "Throttle/Pedal Position Sensor E Circuit",
            "P2126" to "Throttle/Pedal Position Sensor E Range/Performance",
            "P2127" to "Throttle/Pedal Position Sensor E Circuit Low",
            "P2128" to "Throttle/Pedal Position Sensor E Circuit High",
            "P2129" to "Throttle/Pedal Position Sensor E Circuit Intermittent",
            "P2130" to "Throttle/Pedal Position Sensor F Circuit",
            "P2131" to "Throttle/Pedal Position Sensor F Range/Performance",
            "P2132" to "Throttle/Pedal Position Sensor F Circuit Low",
            "P2133" to "Throttle/Pedal Position Sensor F Circuit High",
            "P2134" to "Throttle/Pedal Position Sensor F Circuit Intermittent",
            "P2135" to "Throttle/Pedal Position Sensor Correlation (A/D)",
            "P2136" to "Throttle/Pedal Position Sensor Correlation (A/E)",
            "P2137" to "Throttle/Pedal Position Sensor Correlation (A/F)",
            "P2138" to "Accelerator Pedal Position Sensor Correlation (D/E)",
            "P2139" to "Accelerator Pedal Position Sensor Correlation (D/F)",
            "P2140" to "Accelerator Pedal Position Sensor Correlation (E/F)",
            "P2141" to "Accelerator Pedal Position Sensor Correlation (D/E/F)",
            "P2142" to "Throttle/Pedal Position Sensor G Circuit",
            "P2143" to "Throttle/Pedal Position Sensor G Circuit Range/Performance",
            "P2144" to "Throttle/Pedal Position Sensor G Circuit Low",
            "P2145" to "Throttle/Pedal Position Sensor G Circuit High",
            "P2146" to "Fuel Injector Group A Circuit",
            "P2147" to "Fuel Injector Group A Circuit Low",
            "P2148" to "Fuel Injector Group A Circuit High/Open",
            "P2149" to "Fuel Injector Group B Circuit",
            "P2150" to "Fuel Injector Group B Circuit Low",
            "P2151" to "Fuel Injector Group B Circuit High/Open",
            "P2152" to "Fuel Injector Group C Circuit",
            "P2153" to "Fuel Injector Group C Circuit Low",
            "P2154" to "Fuel Injector Group C Circuit High/Open",
            "P2155" to "Fuel Injector Group D Circuit",
            "P2156" to "Fuel Injector Group D Circuit Low",
            "P2157" to "Fuel Injector Group D Circuit High/Open",
            "P2158" to "Vehicle Speed Sensor Output Circuit",
            "P2159" to "Vehicle Speed Sensor Output Circuit Range/Performance",
            "P2160" to "Vehicle Speed Sensor Output Circuit Low",
            "P2161" to "Vehicle Speed Sensor Output Circuit High",
            "P2162" to "Vehicle Speed Sensor Output Circuit Intermittent",
            "P2163" to "Throttle/Pedal Position Sensor G Circuit High",
            "P2164" to "Throttle/Pedal Position Sensor H Circuit",
            "P2165" to "Throttle/Pedal Position Sensor H Circuit Range/Performance",
            "P2166" to "Throttle/Pedal Position Sensor H Circuit Low",
            "P2167" to "Throttle/Pedal Position Sensor H Circuit High",
            "P2168" to "Throttle/Pedal Position Sensor H Circuit Intermittent",
            "P2169" to "Exhaust Pressure Sensor - Regeneration Required",
            "P2170" to "Throttle Actuator Control System - Idle Speed Performance",
            "P2171" to "Throttle Actuator Control System - Forced Idle Speed Low",
            "P2172" to "Throttle Actuator Control System - Forced Idle Speed High",
            "P2173" to "Throttle Actuator Control System - Idle Speed High",
            "P2174" to "Throttle Actuator Control System - Idle Speed Low",
            "P2175" to "Throttle Actuator Control System - Idle Speed Not Reached",
            "P2176" to "Throttle Actuator Control System - Idle Not Learned",
            "P2177" to "System Too Rich Off Idle (Bank 1)",
            "P2178" to "System Too Lean Off Idle (Bank 1)",
            "P2179" to "System Too Rich Off Idle (Bank 2)",
            "P2180" to "System Too Lean Off Idle (Bank 2)",
            "P2181" to "Cooling System Performance",
            "P2182" to "Engine Coolant Temperature Sensor 2 Circuit Range/Performance",
            "P2183" to "Engine Coolant Temperature Sensor 2 Circuit Low",
            "P2184" to "Engine Coolant Temperature Sensor 2 Circuit High",
            "P2185" to "Engine Coolant Temperature Sensor 2 Circuit Intermittent",
            "P2186" to "Engine Coolant Temperature Sensor 2 Circuit Erratic",
            "P2187" to "System Too Lean at Idle (Bank 1)",
            "P2188" to "System Too Rich at Idle (Bank 1)",
            "P2189" to "System Too Lean at Idle (Bank 2)",
            "P2190" to "System Too Rich at Idle (Bank 2)",
            "P2191" to "System Too Lean at Higher Load (Bank 1)",
            "P2192" to "System Too Rich at Higher Load (Bank 1)",
            "P2193" to "System Too Lean at Higher Load (Bank 2)",
            "P2194" to "System Too Rich at Higher Load (Bank 2)",
            "P2195" to "O2 Sensor Signal Stuck Lean (Bank 1 Sensor 1)",
            "P2196" to "O2 Sensor Signal Stuck Rich (Bank 1 Sensor 1)",
            "P2197" to "O2 Sensor Signal Stuck Lean (Bank 2 Sensor 1)",
            "P2198" to "O2 Sensor Signal Stuck Rich (Bank 2 Sensor 1)",
            "P2199" to "O2 Sensor Signal Stuck Lean (Bank 1 Sensor 2)",
            "P21A0" to "O2 Sensor Signal Stuck Rich (Bank 1 Sensor 2)",
            "P21A1" to "O2 Sensor Signal Stuck Lean (Bank 2 Sensor 2)",
            "P21A2" to "O2 Sensor Signal Stuck Rich (Bank 2 Sensor 2)",
            "P2227" to "Barometric Pressure Sensor Range/Performance",
            "P2228" to "Barometric Pressure Sensor Circuit Low",
            "P2229" to "Barometric Pressure Sensor Circuit High",
            "P2230" to "Barometric Pressure Sensor Circuit Intermittent",
            "P2231" to "O2 Sensor Signal Circuit Shorted to Heater Circuit (Bank 1 Sensor 1)",
            "P2232" to "O2 Sensor Signal Circuit Shorted to Heater Circuit (Bank 1 Sensor 2)",
            "P2233" to "O2 Sensor Signal Circuit Shorted to Heater Circuit (Bank 2 Sensor 1)",
            "P2234" to "O2 Sensor Signal Circuit Shorted to Heater Circuit (Bank 2 Sensor 2)",
            "P2235" to "O2 Sensor Signal Circuit Shorted to Heater Circuit (Bank 3 Sensor 1)",
            "P2236" to "O2 Sensor Signal Circuit Shorted to Heater Circuit (Bank 3 Sensor 2)",
            "P2237" to "O2 Sensor Positive Current Control Circuit (Bank 1 Sensor 1)",
            "P2238" to "O2 Sensor Positive Current Control Circuit Low (Bank 1 Sensor 1)",
            "P2239" to "O2 Sensor Positive Current Control Circuit High (Bank 1 Sensor 1)",
            "P2240" to "O2 Sensor Positive Current Control Circuit (Bank 2 Sensor 1)",
            "P2241" to "O2 Sensor Positive Current Control Circuit Low (Bank 2 Sensor 1)",
            "P2242" to "O2 Sensor Positive Current Control Circuit High (Bank 2 Sensor 1)",
            "P2243" to "O2 Sensor Reference Voltage Circuit (Bank 1 Sensor 1)",
            "P2244" to "O2 Sensor Reference Voltage Circuit Range/Performance (Bank 1 Sensor 1)",
            "P2245" to "O2 Sensor Reference Voltage Circuit Low (Bank 1 Sensor 1)",
            "P2246" to "O2 Sensor Reference Voltage Circuit High (Bank 1 Sensor 1)",
            "P2247" to "O2 Sensor Reference Voltage Circuit (Bank 2 Sensor 1)",
            "P2248" to "O2 Sensor Reference Voltage Circuit Range/Performance (Bank 2 Sensor 1)",
            "P2249" to "O2 Sensor Reference Voltage Circuit Low (Bank 2 Sensor 1)",
            "P2250" to "O2 Sensor Reference Voltage Circuit High (Bank 2 Sensor 1)",
            "P2251" to "O2 Sensor Negative Current Control Circuit (Bank 1 Sensor 1)",
            "P2252" to "O2 Sensor Negative Current Control Circuit Low (Bank 1 Sensor 1)",
            "P2253" to "O2 Sensor Negative Current Control Circuit High (Bank 1 Sensor 1)",
            "P2254" to "O2 Sensor Negative Current Control Circuit (Bank 2 Sensor 1)",
            "P2255" to "O2 Sensor Negative Current Control Circuit Low (Bank 2 Sensor 1)",
            "P2256" to "O2 Sensor Negative Current Control Circuit High (Bank 2 Sensor 1)",
            "P2257" to "Secondary Air Injection System Circuit Low",
            "P2258" to "Secondary Air Injection System Circuit High",
            "P2259" to "Secondary Air Injection System Circuit Open",
            "P2260" to "Secondary Air Injection System Circuit Short",
            "P2261" to "Turbocharger/Supercharger Bypass Valve - Mechanical",
            "P2262" to "Turbo Boost Pressure Not Detected - Mechanical",
            "P2263" to "Turbocharger/Supercharger Boost System Performance",
            "P2264" to "Fuel Tank Pressure Sensor Circuit",
            "P2265" to "Fuel Tank Pressure Sensor Circuit Range/Performance",
            "P2266" to "Fuel Tank Pressure Sensor Circuit Low",
            "P2267" to "Fuel Tank Pressure Sensor Circuit High",
            "P2268" to "Fuel Tank Pressure Sensor Circuit Intermittent",
            "P2269" to "Water in Fuel Indicator Sensor Circuit",
            "P2270" to "O2 Sensor Signal Stuck Lean (Bank 1 Sensor 2)",
            "P2271" to "O2 Sensor Signal Stuck Rich (Bank 1 Sensor 2)",
            "P2272" to "O2 Sensor Signal Stuck Lean (Bank 2 Sensor 2)",
            "P2273" to "O2 Sensor Signal Stuck Rich (Bank 2 Sensor 2)",
            "P2274" to "O2 Sensor Signal Stuck Lean (Bank 1 Sensor 3)",
            "P2275" to "O2 Sensor Signal Stuck Rich (Bank 1 Sensor 3)",
            "P2276" to "O2 Sensor Signal Stuck Lean (Bank 2 Sensor 3)",
            "P2277" to "O2 Sensor Signal Stuck Rich (Bank 2 Sensor 3)",
            "P2278" to "O2 Sensor Signal Stuck Lean (Bank 1 Sensor 4)",
            "P2279" to "O2 Sensor Signal Stuck Rich (Bank 1 Sensor 4)",
            "P2280" to "O2 Sensor Signal Stuck Lean (Bank 2 Sensor 4)",
            "P2281" to "O2 Sensor Signal Stuck Rich (Bank 2 Sensor 4)",
            "P2282" to "Air/Fuel Ratio Sensor 1 Circuit Range/Performance (Bank 1)",
            "P2283" to "Air/Fuel Ratio Sensor 1 Circuit Range/Performance (Bank 2)",
            "P2284" to "Air/Fuel Ratio Sensor 1 Circuit Range/Performance (Bank 3)",
            "P2285" to "Air/Fuel Ratio Sensor 1 Circuit Low",
            "P2286" to "Air/Fuel Ratio Sensor 1 Circuit High",
            "P2287" to "Air/Fuel Ratio Sensor 1 Circuit Intermittent",
            "P2288" to "Air/Fuel Ratio Sensor 1 Circuit Open",
            "P2289" to "Air/Fuel Ratio Sensor 1 Circuit Short to Ground",
            "P2290" to "Air/Fuel Ratio Sensor 1 Circuit Short to Voltage",
            "P2291" to "Air/Fuel Ratio Sensor 2 Circuit Range/Performance (Bank 1)",
            "P2292" to "Air/Fuel Ratio Sensor 2 Circuit Range/Performance (Bank 2)",
            "P2293" to "Air/Fuel Ratio Sensor 2 Circuit Range/Performance (Bank 3)",
            "P2294" to "Air/Fuel Ratio Sensor 2 Circuit Low",
            "P2295" to "Air/Fuel Ratio Sensor 2 Circuit High",
            "P2296" to "Air/Fuel Ratio Sensor 2 Circuit Intermittent",
            "P2297" to "O2 Sensor Out of Range During Deceleration (Bank 1 Sensor 1)",
            "P2298" to "O2 Sensor Out of Range During Deceleration (Bank 2 Sensor 1)",
            "P2299" to "Brake Pedal Position/Accelerator Pedal Position Incompatible"
        )
    }

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun connect(device: BluetoothDevice): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            socket?.close()
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter.cancelDiscovery()

            withTimeout(15_000L) {
                socket?.connect()
            }
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream
            _isConnected.value = true

            if (!initELM327()) {
                throw Exception("ELM327 initialization failed")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            _isConnected.value = false
            Result.failure(e)
        }
    }

    private suspend fun initELM327(): Boolean {
        return try {
            sendCommand("ATZ")
            delay(1000)
            sendCommand("ATI")
            delay(200)
            sendCommand("ATE0")
            delay(100)
            sendCommand("ATL0")
            delay(100)
            sendCommand("ATS0")
            delay(100)
            sendCommand("ATH0")
            delay(100)
            sendCommand("ATSP0")
            delay(100)
            sendCommand("ATAT1")
            true
        } catch (e: Exception) {
            Log.e(TAG, "ELM327 init failed: ${e.message}")
            false
        }
    }

    suspend fun requestPID(pid: OBDPID): Double? = withContext(Dispatchers.IO) {
        var retryDelay = INITIAL_RETRY_DELAY_MS
        repeat(MAX_RETRIES) { attempt ->
            try {
                val response = sendCommandWithTimeout(pid.code)
                val result = parseResponse(response, pid)
                if (result != null) return@withContext result
            } catch (e: Exception) {
                Log.w(TAG, "PID ${pid.code} request failed (attempt ${attempt + 1}/$MAX_RETRIES): ${e.message}")
            }

            if (attempt < MAX_RETRIES - 1) {
                delay(retryDelay)
                retryDelay = (retryDelay * 1.5).toLong().coerceAtMost(MAX_RETRY_DELAY_MS)
            }
        }
        Log.d(TAG, "PID ${pid.code} unavailable after $MAX_RETRIES attempts")
        null
    }

    suspend fun readMultiplePIDs(pids: List<OBDPID>): Map<OBDPID, Double> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<OBDPID, Double>()
        pids.chunked(4).forEach { batch ->
            val deferreds = batch.map { pid ->
                async { pid to requestPID(pid) }
            }
            deferreds.awaitAll().forEach { (pid, value) ->
                value?.let { results[pid] = it }
            }
        }
        Log.v(TAG, "Read ${results.size}/${pids.size} PIDs")
        results
    }

    suspend fun readDTCs(): DTCResponse = withContext(Dispatchers.IO) {
        val codes = mutableListOf<DiagnosticTroubleCode>()
        val pendingCodes = mutableListOf<DiagnosticTroubleCode>()

        try {
            val response = sendCommandWithTimeout("03")
            val dtcs = parseDTCCodes(response, false)
            codes.addAll(dtcs)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read DTCs: ${e.message}")
        }

        try {
            val pendingResponse = sendCommandWithTimeout("07")
            val pending = parseDTCCodes(pendingResponse, true)
            pendingCodes.addAll(pending)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read pending DTCs: ${e.message}")
        }

        DTCResponse(codes, pendingCodes)
    }

    suspend fun clearDTCs(): Boolean = withContext(Dispatchers.IO) {
        try {
            sendCommandWithTimeout("04")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear DTCs: ${e.message}")
            false
        }
    }

    private fun parseDTCCodes(response: String, pending: Boolean): List<DiagnosticTroubleCode> {
        val codes = mutableListOf<DiagnosticTroubleCode>()
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        
        if (hex.contains("ERROR") || hex.isEmpty()) return codes
        
        val cleanHex = hex.drop(2)
        val chars = cleanHex.chunked(4)
        
        for (chunk in chars) {
            if (chunk.length == 4) {
                val firstChar = when (chunk[0]) {
                    '0' -> "P0"
                    '1' -> "P1"
                    '2' -> "P2"
                    '3' -> "P3"
                    '4' -> "C0"
                    '5' -> "C1"
                    '6' -> "C2"
                    '7' -> "C3"
                    '8' -> "B0"
                    '9' -> "B1"
                    'A', 'a' -> "B2"
                    'B', 'b' -> "B3"
                    'C', 'c' -> "U0"
                    'D', 'd' -> "U1"
                    'E', 'e' -> "U2"
                    'F', 'f' -> "U3"
                    else -> "P0"
                }
                val code = "$firstChar${chunk.substring(1)}"
                val description = DTC_DESCRIPTIONS[code] ?: "Unknown fault code"
                codes.add(DiagnosticTroubleCode(code, description, pending))
            }
        }
        return codes
    }

    suspend fun getBatteryVoltage(): Double? {
        return try {
            val response = sendCommandWithTimeout("ATRV")
            parseVoltageResponse(response)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read battery voltage: ${e.message}")
            null
        }
    }

    private fun parseVoltageResponse(response: String): Double? {
        val cleaned = response.replace("V", "").replace("v", "").trim()
        return cleaned.toDoubleOrNull()
    }

    private suspend fun sendCommand(cmd: String): String = withContext(Dispatchers.IO) {
        sendCommandWithTimeout(cmd)
    }

    suspend fun sendRawCommand(cmd: String): String = sendCommand(cmd)

    private suspend fun sendCommandWithTimeout(cmd: String): String = withContext(Dispatchers.IO) {
        val output = outputStream ?: throw IOException("Not connected")
        val input = inputStream ?: throw IOException("Not connected")

        val clearBuffer = ByteArray(64)
        try { while (input.available() > 0) input.read(clearBuffer) } catch (e: Exception) { Log.v(TAG, "Buffer clear warning: ${e.message}") }

        output.write("$cmd\r".toByteArray())
        output.flush()

        val deadline = System.currentTimeMillis() + COMMAND_TIMEOUT_MS
        val responseBuilder = StringBuilder()
        val readBuffer = ByteArray(256)

        while (System.currentTimeMillis() < deadline) {
            if (input.available() > 0) {
                val bytesRead = withContext(Dispatchers.IO) {
                    input.read(readBuffer)
                }
                if (bytesRead > 0) {
                    responseBuilder.append(String(readBuffer, 0, bytesRead, Charsets.US_ASCII))
                    if (responseBuilder.contains(">")) break
                }
            } else {
                delay(30L)
            }
        }

        cleanResponse(responseBuilder.toString())
    }

    private fun parseResponse(response: String, pid: OBDPID): Double? {
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        if (hex.contains("ERROR") || hex.isEmpty()) return null

        val dataHex = hex.drop(4)
        if (dataHex.length < pid.byteCount * 2) return null

        val bytes = ByteArray(pid.byteCount) { i ->
            dataHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }

        return pid.formula(bytes)
    }

    private fun cleanResponse(response: String): String {
        return response
            .replace("\r", " ")
            .replace("\n", " ")
            .replace(" ", "")
            .replace(">", "")
            .trim()
            .filter { it.isDigit() || it.isLetter() || it == ' ' || it == ':' }
            .trim()
    }

    suspend fun readVIN(): String = withContext(Dispatchers.IO) {
        try {
            val response = sendCommandWithTimeout("0902")
            parseVIN(response)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read VIN: ${e.message}")
            ""
        }
    }

    private fun parseVIN(response: String): String {
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        if (hex.contains("ERROR") || hex.isEmpty()) return ""
        val cleanHex = hex.drop(4)
        if (cleanHex.isEmpty()) return ""
        val chars = cleanHex.chunked(2).mapNotNull { byteStr ->
            if (byteStr.length == 2) {
                val intValue = byteStr.toInt(16)
                if (intValue in 0x20..0x7E) intValue.toChar() else null
            } else null
        }
        return chars.joinToString("")
    }

    suspend fun readFreezeFrames(): List<FreezeFrame> = withContext(Dispatchers.IO) {
        try {
            val response = sendCommandWithTimeout("02")
            if (response.contains("ERROR") || response.isBlank()) return@withContext emptyList()
            val dtcHex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
            val dtcChars = dtcHex.drop(4).chunked(4)
            val frames = mutableListOf<FreezeFrame>()
            for (chunk in dtcChars) {
                if (chunk.length == 4) {
                    val firstChar = when (chunk[0]) {
                        '0' -> "P0"; '1' -> "P1"; '2' -> "P2"; '3' -> "P3"
                        '4' -> "C0"; '5' -> "C1"; '6' -> "C2"; '7' -> "C3"
                        '8' -> "B0"; '9' -> "B1"; 'A', 'a' -> "B2"; 'B', 'b' -> "B3"
                        'C', 'c' -> "U0"; 'D', 'd' -> "U1"; 'E', 'e' -> "U2"; 'F', 'f' -> "U3"
                        else -> "P0"
                    }
                    val code = "$firstChar${chunk.substring(1)}"
                    val description = DTC_DESCRIPTIONS[code] ?: "Unknown fault code"
                    val data = mutableMapOf<String, Double>()
                    try {
                        val rpmResp = sendCommandWithTimeout("020C")
                        if (!rpmResp.contains("ERROR")) {
                            val rpmHex = rpmResp.replace(" ", "").drop(6)
                            if (rpmHex.length >= 4) {
                                data["RPM"] = ((rpmHex.substring(0, 2).toInt(16) * 256 + rpmHex.substring(2, 4).toInt(16)) / 4.0)
                            }
                        }
                        val speedResp = sendCommandWithTimeout("020D")
                        if (!speedResp.contains("ERROR")) {
                            val speedHex = speedResp.replace(" ", "").drop(6)
                            if (speedHex.length >= 2) {
                                data["Speed"] = speedHex.substring(0, 2).toInt(16).toDouble()
                            }
                        }
                        val coolResp = sendCommandWithTimeout("0205")
                        if (!coolResp.contains("ERROR")) {
                            val coolHex = coolResp.replace(" ", "").drop(6)
                            if (coolHex.length >= 2) {
                                data["Coolant"] = (coolHex.substring(0, 2).toInt(16) - 40).toDouble()
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to read freeze frame data: ${e.message}")
                    }
                    frames.add(FreezeFrame(DiagnosticTroubleCode(code, description), data))
                }
            }
            frames
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read freeze frames: ${e.message}")
            emptyList()
        }
    }

    suspend fun readReadinessMonitor(): ReadinessMonitor = withContext(Dispatchers.IO) {
        try {
            val response = sendCommandWithTimeout("0101")
            parseReadiness(response)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read readiness monitor: ${e.message}")
            ReadinessMonitor()
        }
    }

    private fun parseReadiness(response: String): ReadinessMonitor {
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        if (hex.contains("ERROR") || hex.isEmpty()) return ReadinessMonitor()
        val dataHex = hex.drop(4)
        if (dataHex.length < 8) return ReadinessMonitor()

        val byteC = dataHex.substring(4, 6).toInt(16)
        val byteD = dataHex.substring(6, 8).toInt(16)

        return ReadinessMonitor(
            misfire = (byteC and 0x01) != 0,
            fuelSystem = (byteC and 0x02) != 0,
            comprehensiveComponent = (byteC and 0x04) != 0,
            catalyst = (byteC and 0x08) != 0,
            heatedCatalyst = (byteC and 0x10) != 0,
            evapSystem = (byteC and 0x20) != 0,
            secondaryAirSystem = (byteC and 0x40) != 0,
            acSystemRefrigerant = (byteC and 0x80) != 0,
            oxygenSensor = (byteD and 0x01) != 0,
            oxygenSensorHeater = (byteD and 0x02) != 0,
            egrSystem = (byteD and 0x04) != 0
        )
    }

    suspend fun readProtocol(): String = withContext(Dispatchers.IO) {
        try {
            sendCommandWithTimeout("ATDP")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read protocol: ${e.message}")
            "Unknown"
        }
    }

    suspend fun scanSupportedPIDs(): List<String> = withContext(Dispatchers.IO) {
        val supported = mutableListOf<String>()
        try {
            val response = sendCommandWithTimeout("0100")
            val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
            if (hex.contains("ERROR") || hex.isEmpty()) return@withContext supported

            val dataHex = hex.drop(4)
            if (dataHex.length >= 8) {
                val bits = dataHex.chunked(2).map { it.toInt(16).toByte() }
                for ((byteIdx, byte) in bits.withIndex()) {
                    for (i in 7 downTo 0) {
                        if ((byte.toInt() and (1 shl i)) != 0) {
                            val pidNum = byteIdx * 8 + (7 - i) + 1
                            supported.add("01%02X".format(pidNum))
                        }
                    }
                }
            }

            val response2 = sendCommandWithTimeout("0120")
            val hex2 = response2.replace(" ", "").replace("\r", "").replace("\n", "").trim()
            if (!hex2.contains("ERROR") && hex2.isNotEmpty()) {
                val dataHex2 = hex2.drop(4)
                if (dataHex2.length >= 8) {
                    val bits2 = dataHex2.chunked(2).map { it.toInt(16).toByte() }
                    for ((byteIdx, byte) in bits2.withIndex()) {
                        for (i in 7 downTo 0) {
                            if ((byte.toInt() and (1 shl i)) != 0) {
                                val pidNum = 0x20 + byteIdx * 8 + (7 - i) + 1
                                if (pidNum <= 0x40) supported.add("01%02X".format(pidNum))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to scan supported PIDs: ${e.message}")
        }
        supported
    }

    fun disconnect() {
        _isConnected.value = false
        scope.cancel()
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.v(TAG, "Socket close warning: ${e.message}")
        }
        socket = null
        inputStream = null
        outputStream = null
        Log.i(TAG, "Disconnected")
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
    }


    // =========================================================================
    // MODE 22 EXTENDED PID SUPPORT (GM/Opel Bosch ME17)
    // =========================================================================
    
    /**
     * Send a Mode 22 command to the ECU.
     * 
     * Mode 22 is a manufacturer-specific diagnostic mode (SAE J2190) that provides
     * enhanced data not available through standard Mode 01 PIDs.
     * 
     * Command Format: 22XXXX
     *   - 22: Mode 22 (Read Data By Identifier)
     *   - XXXX: 4-digit hex PID code
     * 
     * Response Format: 62XXXX + data bytes
     *   - 62: Mode 22 positive response (0x22 + 0x40)
     *   - XXXX: Echo of the requested PID
     *   - data bytes: Response data
     * 
     * @param pidCode The 4-digit hex PID code (e.g., "F190" for VIN)
     * @return Raw response string from ELM327, or null if error
     */
    suspend fun sendMode22Command(pidCode: String): String? = withContext(Dispatchers.IO) {
        val command = "22$pidCode"
        try {
            Log.d(TAG, "Sending Mode 22 command: $command")
            val response = sendCommandWithTimeout(command)
            Log.d(TAG, "Mode 22 response for $pidCode: $response")
            
            if (response.contains("ERROR") || response.isEmpty()) {
                Log.w(TAG, "Mode 22 command $pidCode failed: $response")
                null
            } else {
                response
            }
        } catch (e: Exception) {
            Log.e(TAG, "Mode 22 command $pidCode exception: ${e.message}")
            null
        }
    }
    
    /**
     * Request a specific Mode 22 PID and return the parsed value.
     * 
     * @param pidCode The 4-digit hex PID code (e.g., "0002" for Turbo Boost Actual)
     * @return Parsed value from the response, or null if failed
     */
    suspend fun requestMode22PID(pidCode: String): Double? = withContext(Dispatchers.IO) {
        val response = sendMode22Command(pidCode) ?: return@withContext null
        parseMode22Response(response, pidCode)
    }
    
    /**
     * Read multiple Mode 22 PIDs in parallel.
     * 
     * @param pidCodes List of 4-digit hex PID codes to read
     * @return Map of PID code to parsed value (only successful reads)
     */
    suspend fun readMultipleMode22PIDs(pidCodes: List<String>): Map<String, Double> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, Double>()
        
        // Process in batches of 4 to avoid overwhelming the ECU
        pidCodes.chunked(4).forEach { batch ->
            val deferreds = batch.map { pidCode ->
                async { pidCode to requestMode22PID(pidCode) }
            }
            deferreds.awaitAll().forEach { (pidCode, value) ->
                value?.let { results[pidCode] = it }
            }
        }
        
        Log.d(TAG, "Mode 22 read: ${results.size}/${pidCodes.size} PIDs successful")
        results
    }
    
    /**
     * Read all turbo monitoring PIDs via Mode 22.
     * Returns a comprehensive set of turbo-related data.
     */
    suspend fun readTurboMonitoringData(): Mode22TurboData = withContext(Dispatchers.IO) {
        val results = readMultipleMode22PIDs(Mode22PIDs.TURBO_MONITORING_PIDS)
        
        Mode22TurboData(
            turboBoostActual = results[Mode22PIDs.TURBO_BOOST_ACTUAL] ?: 0.0,
            turboBoostTarget = results[Mode22PIDs.TURBO_BOOST_TARGET] ?: 0.0,
            wastegateDuty = results[Mode22PIDs.WASTEGATE_DUTY] ?: 0.0,
            turboSpeed = results[Mode22PIDs.TURBO_SPEED] ?: 0.0,
            chargeAirTemp = results[Mode22PIDs.CHARGE_AIR_TEMP] ?: 0.0,
            turboInletTemp = results[Mode22PIDs.TURBO_INLET_TEMP] ?: 0.0,
            turboOutletTemp = results[Mode22PIDs.TURBO_OUTLET_TEMP] ?: 0.0,
            engineTorque = results[Mode22PIDs.ENGINE_TORQUE] ?: 0.0,
            vgtPosition = results[Mode22PIDs.VGT_POSITION] ?: 0.0,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Parse a Mode 22 response and extract the value.
     * 
     * Response Format: 62XXXX YYYY...
     *   - 62: Positive response for Mode 22
     *   - XXXX: PID echo (4 chars)
     *   - YYYY: Data bytes (variable length)
     * 
     * @param response Raw response string
     * @param pidCode Requested PID code
     * @return Parsed value, or null if parsing fails
     */
    private fun parseMode22Response(response: String, pidCode: String): Double? {
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        
        if (hex.contains("ERROR") || hex.isEmpty()) {
            Log.w(TAG, "Mode 22 parse error - invalid response: $response")
            return null
        }
        
        // Check for positive response (0x62 = 0x22 + 0x40)
        if (!hex.startsWith("62")) {
            Log.w(TAG, "Mode 22 parse error - not positive response: $hex")
            return null
        }
        
        // Skip mode echo (2 chars: "62") + PID echo (4 chars: "XXXX")
        // Total: 6 hex chars
        if (hex.length < 8) {
            Log.w(TAG, "Mode 22 parse error - response too short: $hex")
            return null
        }
        
        val pidDef = Mode22PIDs.PID_DEFINITIONS["22$pidCode"]
        if (pidDef == null) {
            Log.w(TAG, "Mode 22 parse error - unknown PID: $pidCode")
            return null
        }
        
        // Extract data bytes (skip first 6 hex chars: "62" + PID)
        val dataHex = hex.substring(6)
        val expectedByteCount = pidDef.byteCount
        val data = ByteArray(expectedByteCount)
        
        for (i in 0 until expectedByteCount) {
            val start = i * 2
            if (start + 2 <= dataHex.length) {
                try {
                    data[i] = dataHex.substring(start, start + 2).toInt(16).toByte()
                } catch (e: NumberFormatException) {
                    Log.w(TAG, "Mode 22 parse error - invalid hex at position $start: ${dataHex.substring(start, start + 2)}")
                    return null
                }
            } else {
                Log.w(TAG, "Mode 22 parse error - insufficient data bytes: need $expectedByteCount, got ${dataHex.length / 2}")
                return null
            }
        }
        
        return try {
            val value = pidDef.formula(data)
            Log.v(TAG, "Mode 22 PID $pidCode = $value ${pidDef.unit}")
            value
        } catch (e: Exception) {
            Log.e(TAG, "Mode 22 formula error for $pidCode: ${e.message}")
            null
        }
    }
    
    /**
     * Read VIN using Mode 22 (alternative to Mode 09).
     * Some GM/Opel vehicles require Mode 22 for VIN retrieval.
     * 
     * @return VIN string (17 characters), or empty string if failed
     */
    suspend fun readVINMode22(): String = withContext(Dispatchers.IO) {
        try {
            val response = sendMode22Command("F190") ?: return@withContext ""
            parseVINMode22(response)
        } catch (e: Exception) {
            Log.w(TAG, "Mode 22 VIN read failed: ${e.message}")
            ""
        }
    }
    
    /**
     * Parse VIN from Mode 22 response.
     * Response: 62F190 + 17 bytes of VIN data
     */
    private fun parseVINMode22(response: String): String {
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        if (hex.contains("ERROR") || hex.isEmpty()) return ""
        
        // Skip "62F190" (6 hex chars)
        val dataHex = hex.drop(6)
        if (dataHex.isEmpty()) return ""
        
        // Convert hex bytes to ASCII characters
        return dataHex.chunked(2).mapNotNull { byteStr ->
            if (byteStr.length == 2) {
                val intValue = byteStr.toInt(16)
                if (intValue in 0x20..0x7E) intValue.toChar() else null
            } else null
        }.joinToString("")
    }
    
    /**
     * Read ECU Calibration ID using Mode 22.
     * 
     * @return Calibration ID string, or empty string if failed
     */
    suspend fun readCalibrationIdMode22(): String = withContext(Dispatchers.IO) {
        try {
            val response = sendMode22Command("F191") ?: return@withContext ""
            parseVINMode22(response) // Same parsing logic for ASCII data
        } catch (e: Exception) {
            Log.w(TAG, "Mode 22 Calibration ID read failed: ${e.message}")
            ""
        }
    }
    
    /**
     * Read ECU Calibration Verification Number (CVN) using Mode 22.
     * 
     * @return CVN string, or empty string if failed
     */
    suspend fun readCVNMode22(): String = withContext(Dispatchers.IO) {
        try {
            val response = sendMode22Command("F192") ?: return@withContext ""
            val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
            if (hex.contains("ERROR") || hex.isEmpty()) return@withContext ""
            
            // CVN is typically 4 bytes (8 hex chars) after "62F192"
            val dataHex = hex.drop(6)
            dataHex.take(8) // Return first 8 hex chars as CVN
        } catch (e: Exception) {
            Log.w(TAG, "Mode 22 CVN read failed: ${e.message}")
            ""
        }
    }
    
    /**
     * Discover which Mode 22 PIDs are supported by the ECU.
     * Sends a subset of common PIDs and checks which ones respond successfully.
     * 
     * @return List of supported 4-digit hex PID codes
     */
    suspend fun discoverMode22PIDs(): List<String> = withContext(Dispatchers.IO) {
        val discovered = mutableListOf<String>()
        
        // Test a representative set of Mode 22 PIDs
        val testPids = listOf(
            "0001",  // Engine Torque
            "0002",  // Turbo Boost Actual
            "0003",  // Turbo Boost Target
            "0004",  // Wastegate Duty
            "0005",  // Turbo Speed
            "0006",  // Turbo Inlet Temp
            "0007",  // Turbo Outlet Temp
            "0008",  // Charge Air Temp
            "0009",  // VGT Position
            "F190",  // VIN
            "F191",  // Calibration ID
            "1001",  // Fuel Rail Pressure
            "2001",  // Cat Temp B1S1
            "3002",  // Engine Oil Temp
            "5001",  // Wideband Lambda B1
        )
        
        for (pid in testPids) {
            val response = sendMode22Command(pid)
            if (response != null && !response.contains("ERROR")) {
                discovered.add(pid)
                Log.d(TAG, "Mode 22 PID $pid supported")
            }
        }
        
        Log.i(TAG, "Mode 22 discovery complete: ${discovered.size} PIDs supported")
        discovered
    }
}
