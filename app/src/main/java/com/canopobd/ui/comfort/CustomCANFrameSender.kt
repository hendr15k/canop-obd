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
    
    val presets = remember {
        listOf(
            // Diagnose Presets
            CANPreset("Extended Session", "7E0", "10 03", "Erweiterte Diagnose-Sitzung"),
            CANPreset("Readiness Monitor", "7E0", "01 01", "Readiness Tests abfragen"),
            CANPreset("VIN lesen", "7E0", "09 02", "Fahrzeug-Identifikationsnummer"),
            CANPreset("Supported PIDs", "7E0", "01 00", "Unterstuetzte PIDs scannen"),
            
            // BCM Presets
            CANPreset("BCM Extended Session", "7C0", "10 03", "BCM erweiterte Sitzung"),
            CANPreset("BCM VIN", "7C0", "22 F1 90", "BCM VIN auslesen"),
            CANPreset("BCM Part Number", "7C0", "22 F1 8C", "BCM Teilenummer"),
            
            // Zentralverriegelung
            CANPreset("Verriegeln", "752", "2E FF 01 1F", "Alle Tueren verriegeln"),
            CANPreset("Entriegeln", "752", "2E FF 01 0F", "Alle Tueren entriegeln"),
            CANPreset("Fahrertuer Entriegeln", "752", "2E FF 01 01", "Nur Fahrertuer"),
            
            // Fensterheber
            CANPreset("Fenster Fahrer Auf", "74B", "2E FF 02 01 00", "Fahrerfenster hoch"),
            CANPreset("Fenster Fahrer Zu", "74B", "2E FF 02 01 64", "Fahrerfenster runter"),
            CANPreset("Fenster Alle Auf", "74B", "2E FF 02 00 00", "Alle Fenster hoch"),
            CANPreset("Fenster Alle Zu", "74B", "2E FF 02 00 64", "Alle Fenster runter"),
            CANPreset("Fenster Stop", "74B", "2E FF 02 01 FF", "Fenster anhalten"),
            
            // Spiegel
            CANPreset("Spiegel Einklappen", "752", "2E FF 03 04", "Spiegel einklappen"),
            CANPreset("Spiegel Ausklappen", "752", "2E FF 03 05", "Spiegel ausklappen"),
            CANPreset("Spiegelheizung An", "752", "2E FF 03 08", "Spiegelheizung einschalten"),
            CANPreset("Spiegelheizung Aus", "752", "2E FF 03 00", "Spiegelheizung ausschalten"),
            
            // Beleuchtung
            CANPreset("Coming Home An", "752", "2E FF 04 20", "Coming Home aktivieren"),
            CANPreset("Coming Home Aus", "752", "2E FF 04 00", "Coming Home deaktivieren"),
            CANPreset("Leaving Home An", "752", "2E FF 04 40", "Leaving Home aktivieren"),
            CANPreset("Eckenlicht An", "752", "2E FF 04 10", "Eckenlicht einschalten"),
            CANPreset("Tagfahrlicht An", "752", "2E FF 04 02", "DRL einschalten"),
            CANPreset("Tagfahrlicht Aus", "752", "2E FF 04 04", "DRL ausschalten"),
            
            // Heizung
            CANPreset("Heckscheibenheizung An", "752", "2E FF 05 01", "Heckscheibenheizung"),
            CANPreset("Heckscheibenheizung Aus", "752", "2E FF 05 00", "Heckscheibenheizung aus"),
            CANPreset("Frontscheibenheizung An", "752", "2E FF 05 02", "Frontscheibenheizung"),
            CANPreset("Lenkradheizung Stufe 2", "752", "2E FF 05 08", "Lenkradheizung"),
            
            // Scheibenwischer
            CANPreset("Wischer Aus", "752", "2E FF 06 00", "Scheibenwischer aus"),
            CANPreset("Wischer Stufe 1", "752", "2E FF 06 01", "Scheibenwischer langsam"),
            CANPreset("Wischer Stufe 2", "752", "2E FF 06 02", "Scheibenwischer schnell"),
            CANPreset("Wischer Auto", "752", "2E FF 06 13", "Scheibenwischer Automatik"),
            CANPreset("Heckwischer An", "752", "2E FF 06 04", "Heckscheibenwischer"),
            
            // Sitzheizung
            CANPreset("Sitz FH Stufe 1", "752", "2E FF 09 01", "Fahrersitzheizung Stufe 1"),
            CANPreset("Sitz FH Stufe 2", "752", "2E FF 09 02", "Fahrersitzheizung Stufe 2"),
            CANPreset("Sitz FH Stufe 3", "752", "2E FF 09 03", "Fahrersitzheizung Stufe 3"),
            CANPreset("Sitz BF Stufe 1", "752", "2E FF 09 10", "Beifahrersitzheizung"),
            
            // Horn & Sunroof
            CANPreset("Hupe", "752", "2E FF 07 01", "Hupe kurz hupen"),
            CANPreset("Sunroof Oeffnen", "76B", "2E FF 08 64", "Schiebedach oeffnen"),
            CANPreset("Sunroof Schliessen", "76B", "2E FF 08 00", "Schiebedach schliessen"),
            CANPreset("Sunroof Stop", "76B", "2E FF 08 FF", "Schiebedach stoppen")
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
                        singleLine = true
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
                        singleLine = true
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isLoading = true
                                onSendFrame(canId, data)
                                response = "Befehl gesendet..."
                            },
                            modifier = Modifier.weight(1f),
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
                            color = Color(0xFF0D1117)
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
