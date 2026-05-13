package com.canopobd.ui.comfort

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.LocalAppColors

data class CANPreset(
    val name: String,
    val canId: String,
    val data: String,
    val description: String
)

@Composable
fun CustomCANFrameSenderDialog(
    onSendFrame: (canId: String, data: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    var canId by remember { mutableStateOf("") }
    var data by remember { mutableStateOf("") }
    var response by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val hexRegex = remember { Regex("^[0-9A-Fa-f]{1,2}( [0-9A-Fa-f]{1,2})*$") }
    val canIdValid = remember(canId) { canId.isNotEmpty() && canId.toIntOrNull(16) != null && canId.toInt(16) <= 0x7FF }
    val dataValid = remember(data) { data.isEmpty() || hexRegex.matches(data) }
    
    val presets = remember {
        listOf(
            // Diagnose Presets
            CANPreset("Extended Session", "7E0", "10 03", "Erweiterte Diagnose-Sitzung"),
            CANPreset("Readiness Monitor", "7E0", "01 01", "Readiness Tests abfragen"),
            CANPreset("VIN lesen", "7E0", "09 02", "Fahrzeug-Identifikationsnummer"),
            CANPreset("Supported PIDs", "7E0", "01 00", "Unterstuetzte PIDs scannen"),
            CANPreset("ECU Part Number", "7E0", "22 F1 90", "ECU Teilenummer auslesen"),
            CANPreset("ECU Hardware", "7E0", "22 F1 91", "ECU Hardware Version"),
            CANPreset("ECU Software", "7E0", "22 F1 92", "ECU Software Version"),
            
            // DTC Presets
            CANPreset("DTCs Lesen (Mode 03)", "7E0", "03", "Gespeicherte Fehlercodes lesen"),
            CANPreset("DTCs Pending (Mode 07)", "7E0", "07", "Pending Fehlercodes lesen"),
            CANPreset("DTCs Permanent (Mode 0A)", "7E0", "0A", "Permanente Fehlercodes lesen"),
            CANPreset("DTCs Loeschen (Mode 04)", "7E0", "04", "Alle Fehlercodes loeschen"),
            
            // BCM Presets
            CANPreset("BCM Extended Session", "7C0", "10 03", "BCM erweiterte Sitzung"),
            CANPreset("BCM VIN", "7C0", "22 F1 90", "BCM VIN auslesen"),
            CANPreset("BCM Part Number", "7C0", "22 F1 8C", "BCM Teilenummer"),
            
            // Service Reset Presets
            CANPreset("TPMS Reset", "7E0", "31 03 02", "Reifendruck-Reset durchfuehren"),
            CANPreset("Oel Reset", "7E0", "31 03 03", "Oelwechsel-Reset durchfuehren"),
            CANPreset("Inspektion Reset", "7E0", "31 03 04", "Inspektions-Reset durchfuehren"),
            CANPreset("Adaptives Zurueck", "7E0", "31 02 01", "Adaptionswerte zuruecksetzen"),
            
            // Zentralverriegelung
            CANPreset("Verriegeln", "752", "2E FF 01 1F", "Alle Tueren verriegeln"),
            CANPreset("Entriegeln", "752", "2E FF 01 0F", "Alle Tueren entriegeln"),
            CANPreset("Fahrertuer Entriegeln", "752", "2E FF 01 01", "Nur Fahrertuer"),
            CANPreset("Heckklappe Entriegeln", "752", "2E FF 01 20", "Heckklappe entriegeln"),
            CANPreset("Tankklappe Entriegeln", "752", "2E FF 01 40", "Tankklappe entriegeln"),
            
            // Fensterheber
            CANPreset("Fenster Fahrer Auf", "74B", "2E FF 02 01 00", "Fahrerfenster hoch"),
            CANPreset("Fenster Fahrer Zu", "74B", "2E FF 02 01 64", "Fahrerfenster runter"),
            CANPreset("Fenster Beifahrer Auf", "74B", "2E FF 02 02 00", "Beifahrerfenster hoch"),
            CANPreset("Fenster Beifahrer Zu", "74B", "2E FF 02 02 64", "Beifahrerfenster runter"),
            CANPreset("Fenster Hinten Links Auf", "74B", "2E FF 02 03 00", "Heckfenster links hoch"),
            CANPreset("Fenster Hinten Rechts Auf", "74B", "2E FF 02 04 00", "Heckfenster rechts hoch"),
            CANPreset("Fenster Alle Auf", "74B", "2E FF 02 00 00", "Alle Fenster hoch"),
            CANPreset("Fenster Alle Zu", "74B", "2E FF 02 00 64", "Alle Fenster runter"),
            CANPreset("Fenster Stop", "74B", "2E FF 02 01 FF", "Fenster anhalten"),
            
            // Spiegel
            CANPreset("Spiegel Einklappen", "752", "2E FF 03 04", "Spiegel einklappen"),
            CANPreset("Spiegel Ausklappen", "752", "2E FF 03 05", "Spiegel ausklappen"),
            CANPreset("Spiegelheizung An", "752", "2E FF 03 08", "Spiegelheizung einschalten"),
            CANPreset("Spiegelheizung Aus", "752", "2E FF 03 00", "Spiegelheizung ausschalten"),
            CANPreset("Spiegel Links Hoch", "752", "2E FF 03 01 01", "Linker Spiegel hoch"),
            CANPreset("Spiegel Links Runter", "752", "2E FF 03 01 02", "Linker Spiegel runter"),
            CANPreset("Spiegel Rechts Hoch", "752", "2E FF 03 02 01", "Rechter Spiegel hoch"),
            CANPreset("Spiegel Rechts Runter", "752", "2E FF 03 02 02", "Rechter Spiegel runter"),
            
            // Beleuchtung
            CANPreset("Coming Home An", "752", "2E FF 04 20", "Coming Home aktivieren"),
            CANPreset("Coming Home Aus", "752", "2E FF 04 00", "Coming Home deaktivieren"),
            CANPreset("Leaving Home An", "752", "2E FF 04 40", "Leaving Home aktivieren"),
            CANPreset("Leaving Home Aus", "752", "2E FF 04 00", "Leaving Home deaktivieren"),
            CANPreset("Eckenlicht An", "752", "2E FF 04 10", "Eckenlicht einschalten"),
            CANPreset("Tagfahrlicht An", "752", "2E FF 04 02", "DRL einschalten"),
            CANPreset("Tagfahrlicht Aus", "752", "2E FF 04 04", "DRL ausschalten"),
            CANPreset("Parklichter An", "752", "2E FF 04 01", "Parklichter einschalten"),
            CANPreset("Parklichter Aus", "752", "2E FF 04 00", "Parklichter ausschalten"),
            CANPreset("Nebelschlusslicht An", "752", "2E FF 04 80", "Nebelschlussleuchte"),
            
            // Klima/Climate
            CANPreset("Klima AC An", "752", "2E FF 11 01", "Klimakompressor einschalten"),
            CANPreset("Klima AC Aus", "752", "2E FF 11 00", "Klimakompressor ausschalten"),
            CANPreset("Defrost Front", "752", "2E FF 11 04", "Frontscheibenenteisung"),
            CANPreset("Defrost Heck", "752", "2E FF 11 08", "Heckscheibenenteisung"),
            CANPreset("Defrost Alle", "752", "2E FF 11 1C", "Alle Enteisungen"),
            CANPreset("Klima Auto", "752", "2E FF 11 02", "Automatikmodus"),
            CANPreset("Geblaese Stufe 1", "752", "2E FF 11 81", "Geblaese langsam"),
            CANPreset("Geblaese Stufe 4", "752", "2E FF 11 84", "Geblaese schnell"),
            CANPreset("Geblaese Stufe Max", "752", "2E FF 11 86", "Geblaese maximal"),
            
            // Heizung
            CANPreset("Heckscheibenheizung An", "752", "2E FF 05 01", "Heckscheibenheizung"),
            CANPreset("Heckscheibenheizung Aus", "752", "2E FF 05 00", "Heckscheibenheizung aus"),
            CANPreset("Frontscheibenheizung An", "752", "2E FF 05 02", "Frontscheibenheizung"),
            CANPreset("Lenkradheizung Stufe 1", "752", "2E FF 05 04", "Lenkradheizung Stufe 1"),
            CANPreset("Lenkradheizung Stufe 2", "752", "2E FF 05 08", "Lenkradheizung Stufe 2"),
            CANPreset("Lenkradheizung Stufe 3", "752", "2E FF 05 0C", "Lenkradheizung Stufe 3"),
            CANPreset("Lenkradheizung Aus", "752", "2E FF 05 00", "Lenkradheizung aus"),
            
            // Scheibenwischer
            CANPreset("Wischer Aus", "752", "2E FF 06 00", "Scheibenwischer aus"),
            CANPreset("Wischer Stufe 1", "752", "2E FF 06 01", "Scheibenwischer langsam"),
            CANPreset("Wischer Stufe 2", "752", "2E FF 06 02", "Scheibenwischer schnell"),
            CANPreset("Wischer Auto", "752", "2E FF 06 13", "Scheibenwischer Automatik"),
            CANPreset("Heckwischer An", "752", "2E FF 06 04", "Heckscheibenwischer"),
            CANPreset("Heckwischer Aus", "752", "2E FF 06 00", "Heckwischer aus"),
            CANPreset("Scheibenwaschanlage", "752", "2E FF 06 02", "Waschpumpe aktivieren"),
            
            // Sitzheizung
            CANPreset("Sitz FH Stufe 1", "752", "2E FF 09 01", "Fahrersitzheizung Stufe 1"),
            CANPreset("Sitz FH Stufe 2", "752", "2E FF 09 02", "Fahrersitzheizung Stufe 2"),
            CANPreset("Sitz FH Stufe 3", "752", "2E FF 09 03", "Fahrersitzheizung Stufe 3"),
            CANPreset("Sitz FH Aus", "752", "2E FF 09 00", "Fahrersitzheizung aus"),
            CANPreset("Sitz BF Stufe 1", "752", "2E FF 09 10", "Beifahrersitzheizung Stufe 1"),
            CANPreset("Sitz BF Stufe 2", "752", "2E FF 09 20", "Beifahrersitzheizung Stufe 2"),
            CANPreset("Sitz BF Stufe 3", "752", "2E FF 09 30", "Beifahrersitzheizung Stufe 3"),
            CANPreset("Sitz BF Aus", "752", "2E FF 09 00", "Beifahrersitzheizung aus"),
            
            // Horn & Sunroof
            CANPreset("Hupe", "752", "2E FF 07 01", "Hupe kurz hupen"),
            CANPreset("Sunroof Oeffnen", "76B", "2E FF 08 64", "Schiebedach oeffnen"),
            CANPreset("Sunroof Schliessen", "76B", "2E FF 08 00", "Schiebedach schliessen"),
            CANPreset("Sunroof Stop", "76B", "2E FF 08 FF", "Schiebedach stoppen"),
            CANPreset("Sunroof Luiftung", "76B", "2E FF 08 32", "Schiebedach luften"),
            
            // IPC/Cluster Presets
            CANPreset("IPC Odometer Lesen", "7C3", "22 C2 00", "Kilometerstand auslesen"),
            CANPreset("IPC Units Metrisch", "7C3", "2E C4 00 01", "Einheiten metrisch"),
            CANPreset("IPC Units Imperial", "7C3", "2E C4 00 02", "Einheiten imperial"),
            
            // UDS Session Presets
            CANPreset("UDS Default Session", "7E0", "10 01", "Standard Diagnose-Sitzung"),
            CANPreset("UDS Extended Session", "7E0", "10 03", "Erweiterte Diagnose-Sitzung"),
            CANPreset("UDS Programming", "7E0", "10 02", "Programmier-Sitzung"),
            CANPreset("UDS Tester Present", "7E0", "3E 00", "Verbindung halten"),
            CANPreset("UDS Session Reset", "7E0", "11 01", "ECU Reset (PowerOn)"),
            
            // Live Data PIDs
            CANPreset("PID Kuehlmittel", "7E0", "01 05", "Kuehlmitteltemperatur"),
            CANPreset("PID Drehzahl", "7E0", "01 0C", "Motor Drehzahl"),
            CANPreset("PID Geschwindigkeit", "7E0", "01 0D", "Fahrzeuggeschwindigkeit"),
            CANPreset("PID Drosselklappe", "7E0", "01 11", "Throttle Position"),
            CANPreset("PID Ladedruck", "7E0", "01 0B", "Intake Manifold Pressure"),
            CANPreset("PID Kraftstoff", "7E0", "01 2F", "Kraftstofftankpegel"),
            CANPreset("PID Batterie", "7E0", "ATRV", "Batteriespannung"),
            
            // Mode 22 Extended PIDs
            CANPreset("Mode22 Turbo Boost", "7E0", "22 00 02", "Turbo Ladedruck Ist"),
            CANPreset("Mode22 Turbo Target", "7E0", "22 00 03", "Turbo Ladedruck Soll"),
            CANPreset("Mode22 Wastegate", "7E0", "22 00 04", "Wastegate Duty"),
            CANPreset("Mode22 Oeltemperatur", "7E0", "22 30 02", "Motoroel-Temperatur"),
            CANPreset("Mode22 Ansaugtemperatur", "7E0", "22 00 08", "Ladelufttemperatur")
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Speed, null, tint = colors.accent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CAN Frame Sender", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Schliessen", tint = colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = colors.gaugeYellow.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Warning, null, tint = colors.gaugeYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Nur fuer Entwicklungszwecke!",
                                color = colors.gaugeYellow,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                item {
                    Text("CAN-ID", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = canId,
                        onValueChange = { canId = it.uppercase().filter { c -> c.isDigit() || c in 'A'..'F' }.take(3) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("z.B. 752") },
                        singleLine = true,
                        isError = canId.isNotEmpty() && !canIdValid
                    )
                }
                
                item {
                    Text("Daten (Hex)", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = data,
                        onValueChange = { data = it.uppercase().filter { c -> c.isDigit() || c in 'A'..'F' || c == ' ' }.take(50) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("z.B. 3F 01 01") },
                        singleLine = true,
                        isError = data.isNotEmpty() && !dataValid
                    )
                }

                validationError?.let { error ->
                    item {
                        Text(
                            text = error,
                            color = colors.gaugeRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!canIdValid) {
                                    validationError = "Ungueltige CAN-ID (1-7FF)"
                                    return@Button
                                }
                                if (data.isNotEmpty() && !dataValid) {
                                    validationError = "Ungueltige Hex-Daten (Format: XX XX XX)"
                                    return@Button
                                }
                                validationError = null
                                isLoading = true
                                onSendFrame(canId, data)
                                response = "Befehl gesendet..."
                            },
                            modifier = Modifier.weight(1f),
                            enabled = canIdValid && (data.isEmpty() || dataValid),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Icon(Icons.Filled.Send, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SENDEN")
                        }
                        OutlinedButton(
                            onClick = { canId = ""; data = ""; response = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Clear, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CLEAR")
                        }
                    }
                }
                
                response?.let { resp ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Response:", color = colors.textSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    resp,
                                    color = colors.gaugeGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Presets (PSA/Stellantis)", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                presets.forEach { preset ->
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    canId = preset.canId
                                    data = preset.data
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surfaceCard
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.name, color = colors.textPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                    Text(preset.description, color = colors.textDim, fontSize = 10.sp)
                                }
                                Text(
                                    "${preset.canId}: ${preset.data}",
                                    color = colors.accent,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = colors.surfaceCard
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("CAN-Bus IDs (Opel Astra J):", color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            listOf(
                                "752h" to "BMF (Body Module)",
                                "74Bh" to "PORTEC (Tuersen)",
                                "76Bh" to "BCM (Body Control)",
                                "1D0h" to "IC (Cluster)",
                                "420h" to "NAC/RCC (Radio)"
                            ).forEach { (id, name) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(id, color = colors.gaugeGreen, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                    Text(name, color = colors.textDim, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schliessen", color = colors.accent)
            }
        }
    )
}
