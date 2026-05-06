package com.canopobd.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.canopobd.ui.theme.*

data class QuickAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: String,
    val action: () -> Unit
)

@Composable
fun QuickActionsDialog(
    onDismiss: () -> Unit,
    onExecuteAction: (String) -> Unit,
    onNavigateTo: (String) -> Unit
) {
    val colors = LocalAppColors.current
    
    val quickActions = remember {
        listOf(
            // Diagnostics
            QuickAction("dtc_read", "Fehler-Codes", "DTCs lesen", Icons.Filled.Warning, "Diagnose") { onNavigateTo("dtc") },
            QuickAction("dtc_clear", "Fehler loeschen", "DTCs loeschen", Icons.Filled.Delete, "Diagnose") { onExecuteAction("DTC_CLEAR") },
            QuickAction("readiness", "Readiness Status", "Pruefstatus abfragen", Icons.Filled.CheckCircle, "Diagnose") { onNavigateTo("readiness") },
            
            // Comfort
            QuickAction("unlock", "Entriegeln", "Fahrzeug entriegeln", Icons.Filled.LockOpen, "Komfort") { onExecuteAction("UNLOCK") },
            QuickAction("lock", "Verriegeln", "Fahrzeug verriegeln", Icons.Filled.Lock, "Komfort") { onExecuteAction("LOCK") },
            QuickAction("windows_down", "Fenster auf", "Alle Fenster oeffnen", Icons.Filled.Window, "Komfort") { onExecuteAction("WINDOW_ALL_DOWN") },
            QuickAction("windows_up", "Fenster zu", "Alle Fenster schliessen", Icons.Filled.Window, "Komfort") { onExecuteAction("WINDOW_ALL_UP") },
            QuickAction("sunroof_open", "Sunroof auf", "Schiebedach oeffnen", Icons.Filled.Roofing, "Komfort") { onExecuteAction("SUNROOF_OPEN") },
            QuickAction("mirror_fold", "Spiegel klappen", "Spiegel einklappen", Icons.Filled.SwapHoriz, "Komfort") { onExecuteAction("MIRROR_FOLD") },
            
            // Lighting
            QuickAction("lights_on", "Parklichter an", "Parklichter einschalten", Icons.Filled.LightMode, "Beleuchtung") { onExecuteAction("PARKING_LIGHTS_ON") },
            QuickAction("lights_off", "Parklichter aus", "Parklichter ausschalten", Icons.Filled.DarkMode, "Beleuchtung") { onExecuteAction("PARKING_LIGHTS_OFF") },
            QuickAction("coming_home", "Coming Home", "Coming Home aktivieren", Icons.Filled.Home, "Beleuchtung") { onExecuteAction("COMING_HOME_ON") },
            QuickAction("fog_on", "Nebelschluss", "Nebelschlussleuchte an", Icons.Filled.Highlight, "Beleuchtung") { onExecuteAction("FOG_LIGHTS_ON") },
            
            // Heating
            QuickAction("rear_heat", "Heckscheibe", "Heckscheibenheizung", Icons.Filled.ChevronLeft, "Heizung") { onExecuteAction("REAR_HEATING_ON") },
            QuickAction("seat_heat_1", "Sitzheizung", "Fahrersitz Stufe 1", Icons.Filled.Chair, "Heizung") { onExecuteAction("SEAT_DRIVER_HEAT_1") },
            QuickAction("steering_heat", "Lenkrad", "Lenkradheizung", Icons.Filled.PanTool, "Heizung") { onExecuteAction("STEERING_HEATING_1") },
            QuickAction("climate_defrost", "Defrost", "Alle Enteisungen", Icons.Filled.AcUnit, "Heizung") { onExecuteAction("CLIMATE_DEFROST_ALL") },
            
            // Service
            QuickAction("tpms_reset", "TPMS Reset", "Reifendruck zuruecksetzen", Icons.Filled.TireRepair, "Service") { onExecuteAction("TPMS_RESET") },
            QuickAction("oil_reset", "Oel Reset", "Oelwechsel zuruecksetzen", Icons.Filled.OilBarrel, "Service") { onExecuteAction("OIL_RESET") },
            QuickAction("wiper_test", "Wischer", "Testwischanlage", Icons.Filled.WaterDrop, "Service") { onExecuteAction("WIPER_LOW") },
            QuickAction("horn", "Hupe", "Testhupen", Icons.Filled.VolumeUp, "Service") { onExecuteAction("HORN") },
            
            // Data
            QuickAction("vin_read", "VIN lesen", "Fahrzeug-ID abfragen", Icons.Filled.Badge, "Daten") { onNavigateTo("vin") },
            QuickAction("protocol", "Protokoll", "CAN-Protokoll info", Icons.Filled.Memory, "Daten") { onNavigateTo("protocol") },
            QuickAction("supported_pids", "PIDs scannen", "Unterstuetzte PIDs", Icons.Filled.Search, "Daten") { onNavigateTo("pids") },
            
            // Quick Access
            QuickAction("dashboard", "Dashboard", "Hauptbildschirm", Icons.Filled.Dashboard, "Navigation") { onNavigateTo("dashboard") },
            QuickAction("settings", "Einstellungen", "App-Konfiguration", Icons.Filled.Settings, "Navigation") { onNavigateTo("settings") },
            QuickAction("maintenance", "Wartung", "Wartungsplan", Icons.Filled.Engineering, "Navigation") { onNavigateTo("maintenance") },
            QuickAction("vehicle_info", "Fahrzeug-Info", "Fahrzeugdetails", Icons.Filled.DirectionsCar, "Navigation") { onNavigateTo("vehicle_info") }
        )
    }
    
    val groupedActions = quickActions.groupBy { it.category }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FlashOn, null, tint = colors.accent, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quick Actions", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Schliessen", tint = colors.textSecondary)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    groupedActions.forEach { (category, actions) ->
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                            Text(
                                category,
                                color = colors.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(actions) { action ->
                            QuickActionCard(action = action, colors = colors)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(action: QuickAction, colors: AppColors) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { action.action() },
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                action.icon,
                contentDescription = action.title,
                tint = colors.accent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                action.title,
                color = colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                action.subtitle,
                color = colors.textDim,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
