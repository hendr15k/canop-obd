package com.canopobd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.protocol.CANMessage
import com.canopobd.data.protocol.CANMonitor
import com.canopobd.data.protocol.CANFilterMode
import com.canopobd.ui.theme.LocalAppColors
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CANMonitorDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    canMonitor: CANMonitor,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val colors = LocalAppColors.current
    var isMonitoring by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf("") }
    var showHex by remember { mutableStateOf(true) }
    var showAscii by remember { mutableStateOf(false) }
    var showTimestamp by remember { mutableStateOf(true) }
    var selectedFilterMode by remember { mutableStateOf(CANFilterMode.ALL) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showStatistics by remember { mutableStateOf(false) }

    val messages by canMonitor.messages.collectAsState()
    val filteredMessages = remember(messages, filterText, selectedFilterMode) {
        messages.filter { msg ->
            val matchesFilter = if (filterText.isNotBlank()) {
                msg.canId.contains(filterText, ignoreCase = true) ||
                        msg.hexData.contains(filterText, ignoreCase = true)
            } else true

            val matchesMode = when (selectedFilterMode) {
                CANFilterMode.STANDARD -> !msg.isExtended
                CANFilterMode.EXTENDED -> msg.isExtended
                CANFilterMode.ALL -> true
            }

            matchesFilter && matchesMode
        }
    }

    val messagesPerSecond by canMonitor.messagesPerSecond.collectAsState(initial = 0)
    val uniqueCanIds = remember(messages) { canMonitor.getUniqueCanIds() }
    val perIdStats = remember(messages) {
        messages.groupBy { it.canId }
            .mapValues { (id, msgs) ->
                PerIdStat(
                    canId = id,
                    count = msgs.size,
                    lastSeen = msgs.maxOfOrNull { it.timestamp } ?: 0L
                )
            }
            .values
            .sortedByDescending { it.count }
            .take(10)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            canMonitor.startMonitoring()
        } else {
            canMonitor.stopMonitoring()
        }
    }

    LaunchedEffect(Unit) {
        canMonitor.errorMessage.collectLatest { error ->
            errorMessage = error
        }
    }

    AlertDialog(
        onDismissRequest = {
            canMonitor.stopMonitoring()
            onDismiss()
        },
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
                        Icons.Filled.Cable,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAN-Bus Monitor",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = { canMonitor.clearMessages() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear", tint = colors.textSecondary)
                    }
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isMonitoring) "Aktiv" else "Gestoppt",
                                    color = if (isMonitoring) colors.gaugeGreen else colors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${filteredMessages.size} / ${messages.size} Nachrichten",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Button(
                                onClick = { isMonitoring = !isMonitoring },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMonitoring) colors.gaugeRed else colors.gaugeGreen
                                ),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = if (isMonitoring) "Stopp" else "Start",
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$messagesPerSecond",
                                    color = colors.accent,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "msg/s",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${messages.size}",
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Total",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${uniqueCanIds.size}",
                                    color = colors.gaugeYellow,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Unique IDs",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            TextButton(
                                onClick = { showStatistics = !showStatistics },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    if (showStatistics) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = "Toggle Stats",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Stats",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (showStatistics && perIdStats.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                color = colors.surface
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Top CAN-IDs:",
                                        color = colors.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    perIdStats.forEach { stat ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stat.canId,
                                                color = colors.gaugeGreen,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${stat.count}x",
                                                color = colors.textPrimary,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = formatTimestamp(stat.lastSeen),
                                                color = colors.textSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = filterText,
                                onValueChange = { newValue -> filterText = newValue.uppercase() },
                                label = { Text("Filter CAN-ID") },
                                modifier = Modifier.weight(1f).height(56.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accent,
                                    unfocusedBorderColor = colors.surface,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedLabelColor = colors.accent,
                                    unfocusedLabelColor = colors.textSecondary
                                )
                            )

                            FilterChip(
                                selected = selectedFilterMode == CANFilterMode.ALL,
                                onClick = { selectedFilterMode = CANFilterMode.ALL },
                                label = { Text("Alle", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.accent,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                            FilterChip(
                                selected = selectedFilterMode == CANFilterMode.STANDARD,
                                onClick = { selectedFilterMode = CANFilterMode.STANDARD },
                                label = { Text("11-Bit", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.accent,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                            FilterChip(
                                selected = selectedFilterMode == CANFilterMode.EXTENDED,
                                onClick = { selectedFilterMode = CANFilterMode.EXTENDED },
                                label = { Text("29-Bit", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.accent,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = showHex,
                                    onCheckedChange = { showHex = it },
                                    colors = CheckboxDefaults.colors(checkedColor = colors.accent),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("HEX", color = colors.textSecondary, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = showAscii,
                                    onCheckedChange = { showAscii = it },
                                    colors = CheckboxDefaults.colors(checkedColor = colors.accent),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("ASCII", color = colors.textSecondary, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = showTimestamp,
                                    onCheckedChange = { showTimestamp = it },
                                    colors = CheckboxDefaults.colors(checkedColor = colors.accent),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Zeit", color = colors.textSecondary, fontSize = 11.sp)
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
                    color = colors.surfaceVariant
                ) {
                    if (filteredMessages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isMonitoring) "Warte auf CAN-Nachrichten..." else "Monitoring gestoppt",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredMessages, key = { "${it.timestamp}_${it.canId}_${filteredMessages.indexOf(it)}" }) { msg ->
                                CANMessageRow(
                                    message = msg,
                                    showHex = showHex,
                                    showAscii = showAscii,
                                    showTimestamp = showTimestamp,
                                    colors = colors
                                )
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
                            text = "Haeufige CAN-IDs Astra J:",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CANMonitor.COMMON_IDS.entries.take(5).forEach { (id, name) ->
                                SuggestionChip(
                                    onClick = { filterText = id },
                                    label = {
                                        Text("${id.take(3)}: ${name.take(10)}", fontSize = 10.sp)
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = colors.surface,
                                        labelColor = colors.textSecondary
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                canMonitor.stopMonitoring()
                onDismiss()
            }) {
                Text("Schliessen", color = colors.accent)
            }
        }
    )
}

@Composable
private fun CANMessageRow(
    message: CANMessage,
    showHex: Boolean,
    showAscii: Boolean,
    showTimestamp: Boolean,
    colors: com.canopobd.ui.theme.AppColors
) {
    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault()) }
    val timestamp = remember(message.timestamp) { timeFormat.format(Instant.ofEpochMilli(message.timestamp)) }

    val canIdColor = when {
        message.canId.startsWith("7E") -> colors.accent
        message.canId.startsWith("1C") -> colors.gaugeYellow
        message.canId.startsWith("2C") -> colors.gaugeOrange
        else -> colors.gaugeGreen
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showTimestamp) {
            Text(
                text = timestamp,
                color = colors.textSecondary.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(80.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(4.dp),
            color = canIdColor.copy(alpha = 0.2f),
            modifier = Modifier.width(56.dp)
        ) {
            Text(
                text = message.canId,
                color = canIdColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (message.isExtended) {
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = colors.gaugeYellow.copy(alpha = 0.3f),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "EXT",
                    color = colors.gaugeYellow,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        if (showHex) {
            Text(
                text = message.hexData.take(23),
                color = colors.textPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        if (showAscii) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = message.asciiRepresentation.take(8),
                color = colors.textSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

data class PerIdStat(
    val canId: String,
    val count: Int,
    val lastSeen: Long
)

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 1000 -> "now"
        diff < 60000 -> "${diff / 1000}s"
        else -> timeFormatter.format(Instant.ofEpochMilli(timestamp))
    }
}
