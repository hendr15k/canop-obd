package com.canopobd.ui.profile

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.canopobd.data.model.VehicleProfile
import com.canopobd.data.model.VehicleProfiles
import com.canopobd.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class SavedProfile(
    val id: String,
    val name: String,
    val vehicle: String,
    val timestamp: Long,
    val settings: Map<String, Any>
)

@Composable
fun VehicleProfileManagerDialog(
    onDismiss: () -> Unit,
    onLoadProfile: (SavedProfile) -> Unit,
    onExportProfile: (SavedProfile) -> Unit,
    currentProfile: VehicleProfile?
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var savedProfiles by remember { mutableStateOf<List<SavedProfile>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf<SavedProfile?>(null) }
    
    LaunchedEffect(Unit) {
        savedProfiles = loadProfilesFromPrefs(context)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
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
                        Icon(Icons.Filled.DirectionsCar, null, tint = colors.accent, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fahrzeug-Profile", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Schliessen", tint = colors.textSecondary)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Neues Profil erstellen")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (savedProfiles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.FolderOff, null, tint = colors.textDim, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Keine gespeicherten Profile", color = colors.textDim)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedProfiles) { profile ->
                            ProfileCard(
                                profile = profile,
                                isSelected = selectedProfile?.id == profile.id,
                                onSelect = { selectedProfile = profile },
                                onLoad = { onLoadProfile(profile) },
                                onExport = { onExportProfile(profile) },
                                onDelete = {
                                    deleteProfile(context, profile.id)
                                    savedProfiles = savedProfiles.filter { it.id != profile.id }
                                },
                                colors = colors
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateProfileDialog(
            currentProfile = currentProfile,
            onDismiss = { showCreateDialog = false },
            onSave = { profile ->
                saveProfileToPrefs(context, profile)
                savedProfiles = savedProfiles + profile
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: SavedProfile,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onLoad: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(profile.vehicle, color = colors.textSecondary, fontSize = 12.sp)
                Text(
                    java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(profile.timestamp)),
                    color = colors.textDim,
                    fontSize = 10.sp
                )
            }
            Row {
                IconButton(onClick = onLoad) {
                    Icon(Icons.Filled.Download, "Laden", tint = colors.accent)
                }
                IconButton(onClick = onExport) {
                    Icon(Icons.Filled.Share, "Exportieren", tint = colors.textSecondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "Loeschen", tint = colors.gaugeRed)
                }
            }
        }
    }
}

@Composable
private fun CreateProfileDialog(
    currentProfile: VehicleProfile?,
    onDismiss: () -> Unit,
    onSave: (SavedProfile) -> Unit
) {
    val colors = LocalAppColors.current
    var profileName by remember { mutableStateOf(currentProfile?.displayName ?: "") }
    var selectedVehicle by remember { mutableStateOf(currentProfile?.id ?: "astra_j_2012_14t") }
    
    val availableVehicles = listOf(
        "astra_j_2012_14t" to "Opel Astra J 1.4 Turbo (2012)",
        "astra_j_2010_16" to "Opel Astra J 1.6 (2010-2014)",
        "astra_j_2012_17cdti" to "Opel Astra J 1.7 CDTi (2012)",
        "insignia_a_2009" to "Opel Insignia A (2009-2017)",
        "custom" to "Benutzerdefiniert"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Profil erstellen", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profilname") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Fahrzeug:", color = colors.textSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                availableVehicles.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVehicle = id }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedVehicle == id,
                            onClick = { selectedVehicle = id }
                        )
                        Text(name, color = colors.textPrimary, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val vehicleName = availableVehicles.find { it.first == selectedVehicle }?.second ?: "Benutzerdefiniert"
                            val profile = SavedProfile(
                                id = java.util.UUID.randomUUID().toString(),
                                name = profileName.ifBlank { vehicleName },
                                vehicle = vehicleName,
                                timestamp = System.currentTimeMillis(),
                                settings = emptyMap()
                            )
                            onSave(profile)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                    ) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}

private fun loadProfilesFromPrefs(context: Context): List<SavedProfile> {
    val prefs = context.getSharedPreferences("vehicle_profiles", Context.MODE_PRIVATE)
    val json = prefs.getString("profiles", "[]") ?: "[]"
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            SavedProfile(
                id = obj.getString("id"),
                name = obj.getString("name"),
                vehicle = obj.getString("vehicle"),
                timestamp = obj.getLong("timestamp"),
                settings = emptyMap()
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun saveProfileToPrefs(context: Context, profile: SavedProfile) {
    val prefs = context.getSharedPreferences("vehicle_profiles", Context.MODE_PRIVATE)
    val current = loadProfilesFromPrefs(context).toMutableList()
    current.removeAll { it.id == profile.id }
    current.add(profile)
    
    val array = JSONArray()
    current.forEach { p ->
        val obj = JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("vehicle", p.vehicle)
            put("timestamp", p.timestamp)
        }
        array.put(obj)
    }
    
    prefs.edit().putString("profiles", array.toString()).apply()
}

private fun deleteProfile(context: Context, profileId: String) {
    val prefs = context.getSharedPreferences("vehicle_profiles", Context.MODE_PRIVATE)
    val current = loadProfilesFromPrefs(context).toMutableList()
    current.removeAll { it.id == profileId }
    
    val array = JSONArray()
    current.forEach { p ->
        val obj = JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("vehicle", p.vehicle)
            put("timestamp", p.timestamp)
        }
        array.put(obj)
    }
    
    prefs.edit().putString("profiles", array.toString()).apply()
}

fun exportProfileToJson(profile: SavedProfile): String {
    val obj = JSONObject().apply {
        put("id", profile.id)
        put("name", profile.name)
        put("vehicle", profile.vehicle)
        put("timestamp", profile.timestamp)
        put("settings", JSONObject(profile.settings as Map<*, *>))
    }
    return obj.toString(2)
}
