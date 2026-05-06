package com.canopobd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.canopobd.data.protocol.Mode22Client
import com.canopobd.data.protocol.Mode22DIDInfo
import com.canopobd.data.protocol.DIDCategory
import com.canopobd.ui.theme.LocalAppColors
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mode22BrowserDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    mode22Client: Mode22Client,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val colors = LocalAppColors.current
    var selectedCategory by remember { mutableStateOf<DIDCategory?>(null) }
    var selectedDid by remember { mutableStateOf<String?>(null) }
    var isDiscovering by remember { mutableStateOf(false) }
    var discoveredDIDs by remember { mutableStateOf<List<String>>(emptyList()) }
    var readValues by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isReading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val allDIDs = remember(selectedCategory) {
        val cat = selectedCategory
        if (cat != null) {
            mode22Client.getDIDsByCategory(cat)
        } else {
            mode22Client.getAllDIDCategories().flatMap { mode22Client.getDIDsByCategory(it) }
        }
    }

    LaunchedEffect(Unit) {
        mode22Client.lastError.collectLatest { error ->
            errorMessage = error
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
        containerColor = colors.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Explore,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mode 22 Browser",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "GM/Opel Erweiterte PIDs (Mode $22)",
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "SAE J2190 Erweiterte Diagnose",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = {
                                    isDiscovering = true
                                    discoveredDIDs = emptyList()
                                },
                                enabled = !isDiscovering && !isReading,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                if (isDiscovering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Scannen", fontSize = 12.sp)
                                }
                            }
                        }

                        if (discoveredDIDs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Gefundene DIDs: ${discoveredDIDs.size}",
                                color = colors.gaugeGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(
                                    rememberScrollState()
                                ),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                discoveredDIDs.take(10).forEach { did ->
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = colors.gaugeGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = did,
                                            color = colors.gaugeGreen,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Kategorie filtern:",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Alle", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.accent,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                            DIDCategory.entries.forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.displayName, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.accent,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                errorMessage?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = colors.gaugeRed.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = colors.gaugeRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(error, color = colors.gaugeRed, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0D1117)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allDIDs) { didInfo ->
                            DIDRow(
                                didInfo = didInfo,
                                value = readValues[didInfo.code],
                                isSelected = selectedDid == didInfo.code,
                                onSelect = {
                                    selectedDid = if (selectedDid == didInfo.code) null else didInfo.code
                                },
                                onRead = {
                                    isReading = true
                                    scope.launch {
                                        mode22Client.readDID(didInfo.code).collect { data ->
                                            if (data != null) {
                                                val value = mode22Client.getParsedValue(didInfo.code, data)
                                                readValues = readValues + (didInfo.code to value)
                                            }
                                            isReading = false
                                        }
                                    }
                                },
                                colors = colors
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isReading = true
                            readValues = emptyMap()
                            val didsToRead = allDIDs.take(10).map { it.code }
                            scope.launch {
                                mode22Client.readMultipleDIDs(didsToRead).collect { results ->
                                    results.forEach { (did, data) ->
                                        if (data != null) {
                                            val value = mode22Client.getParsedValue(did, data)
                                            readValues = readValues + (did to value)
                                        }
                                    }
                                    isReading = false
                                }
                            }
                        },
                        enabled = !isReading && !isDiscovering,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isReading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Alle lesen", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            readValues = emptyMap()
                            mode22Client.clearCache()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear", fontSize = 12.sp, color = colors.textSecondary)
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

@Composable
private fun DIDRow(
    didInfo: Mode22DIDInfo,
    value: Double?,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRead: () -> Unit,
    colors: com.canopobd.ui.theme.AppColors
) {
    val categoryColor = when (didInfo.category) {
        DIDCategory.ENGINE -> colors.gaugeYellow
        DIDCategory.TURBO -> colors.accent
        DIDCategory.FUEL -> colors.gaugeOrange
        DIDCategory.TEMPERATURE -> colors.gaugeRed
        DIDCategory.PRESSURE -> colors.gaugeGreen
        DIDCategory.ELECTRICAL -> colors.gaugeCyan
        DIDCategory.TRANSMISSION -> Color(0xFF9C27B0)
        else -> colors.textSecondary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        color = if (isSelected) colors.accent.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = didInfo.code,
                            color = categoryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = didInfo.category.displayName,
                        color = colors.textSecondary,
                        fontSize = 9.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = didInfo.name,
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (didInfo.description.isNotEmpty()) {
                    Text(
                        text = didInfo.description,
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (value != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = formatValue(didInfo, value),
                            color = colors.gaugeGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onRead,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Read",
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatValue(didInfo: Mode22DIDInfo, value: Double): String {
    return when (didInfo.unit) {
        "%" -> "%.1f%%".format(value)
        "°C" -> "%.1f°C".format(value)
        "Nm" -> "%.0f Nm".format(value)
        "kPa" -> "%.1f kPa".format(value)
        "V" -> "%.1f V".format(value)
        "rpm" -> "%.0f rpm".format(value)
        "L/h" -> "%.2f L/h".format(value)
        else -> "%.2f".format(value)
    }
}

@Composable
private fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()
