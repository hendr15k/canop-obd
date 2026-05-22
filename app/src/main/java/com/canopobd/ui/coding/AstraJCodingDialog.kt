package com.canopobd.ui.coding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.model.AstraJCodingModels
import com.canopobd.data.model.AstraJCodingRepository
import com.canopobd.ui.theme.*

private enum class RiskLevel(val label: String, val color: Color, val level: Int) {
    SAFE("Sicher", gaugeGreen, 1),
    MODERATE("Moderat", gaugeOrange, 2),
    RISKY("Risikant", gaugeRed, 3),
    EXPERT_ONLY("Nur Experten", Color(0xFF991B1B), 4)
}

private data class CodingHistoryEntry(
    val optionId: String,
    val optionName: String,
    val oldValue: String,
    val newValue: String,
    val timestamp: Long = System.currentTimeMillis()
)

private fun getRiskLevel(option: AstraJCodingModels.CodingOption): RiskLevel {
    val highRiskIds = setOf(
        "esp_sport", "esp_sport_mode", "tc_off", "flexride_sport",
        "immobilizer_present", "steering_weight"
    )
    val moderateRiskIds = setOf(
        "start_stop", "cruise_control", "video_motion", "navi_unlock",
        "crash_unlock", "emergency_brake"
    )
    val expertIds = setOf(
        "alarm_present", "interior_monitor", "tilt_sensor"
    )
    return when {
        option.id in expertIds -> RiskLevel.EXPERT_ONLY
        option.id in highRiskIds -> RiskLevel.RISKY
        option.id in moderateRiskIds -> RiskLevel.MODERATE
        else -> RiskLevel.SAFE
    }
}

private fun getSubcategories(option: AstraJCodingModels.CodingOption): String {
    return when (option.module) {
        AstraJCodingModels.Module.UEC -> "Elektrik"
        AstraJCodingModels.Module.REC -> "Elektrik"
        AstraJCodingModels.Module.BCM -> "Karosserie"
        AstraJCodingModels.Module.IPC -> "Anzeige"
        AstraJCodingModels.Module.CIM -> "Lenkung"
        AstraJCodingModels.Module.ECU -> "Motor"
        AstraJCodingModels.Module.TCM -> "Getriebe"
        AstraJCodingModels.Module.HCM -> "Klima"
        AstraJCodingModels.Module.PAM -> "Parken"
        AstraJCodingModels.Module.ABS -> "Bremse"
        AstraJCodingModels.Module.TRC -> "Anhaenger"
        AstraJCodingModels.Module.AFL -> "Licht"
        AstraJCodingModels.Module.EPB -> "Parkbremse"
        AstraJCodingModels.Module.TPM -> "Reifen"
        AstraJCodingModels.Module.DSP -> "Audio"
    }
}

private fun getOptionTags(option: AstraJCodingModels.CodingOption): List<String> {
    val tags = mutableListOf<String>()
    tags.add(getSubcategories(option))
    if (option.hardwareRequired != null) tags.add("Hardware")
    if (option.requiresCarPass) tags.add("CarPass")
    if (option.displayName.lowercase().contains("licht")) tags.add("Beleuchtung")
    if (option.displayName.lowercase().contains("fenster") || option.displayName.lowercase().contains("spiegel")) tags.add("Komfort")
    if (option.displayName.lowercase().contains("motor") || option.displayName.lowercase().contains("start")) tags.add("Antrieb")
    if (option.displayName.lowercase().contains("sicherheit") || option.displayName.lowercase().contains("alarm")) tags.add("Sicherheit")
    return tags
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 60 -> "Gerade eben"
        minutes < 60 -> "Vor ${minutes} Min."
        hours < 24 -> "Vor ${hours} Std."
        days < 7 -> "Vor ${days} Tg."
        else -> "Vor >7 Tg."
    }
}

private fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Lightbulb" -> Icons.Default.Lightbulb
        "Car" -> Icons.Default.DirectionsCar
        "Engine" -> Icons.Default.Engineering
        "Dashboard" -> Icons.Default.Dashboard
        "Radio" -> Icons.Default.Radio
        "Speed" -> Icons.Default.Speed
        "Security" -> Icons.Default.Security
        "AirlineSeatReclineNormal" -> Icons.Default.AirlineSeatReclineNormal
        "Thermostat" -> Icons.Default.Thermostat
        "Settings" -> Icons.Default.Settings
        "Parking" -> Icons.Default.LocalParking
        "LocalShipping" -> Icons.Default.LocalShipping
        "DiscFull" -> Icons.Default.DiscFull
        "LocalGasStation" -> Icons.Default.LocalGasStation
        "FlashlightOn" -> Icons.Default.FlashlightOn
        "Lock" -> Icons.Default.Lock
        "Circle" -> Icons.Default.Circle
        "BugReport" -> Icons.Default.BugReport
        "VolumeUp" -> Icons.Default.VolumeUp
        "Crop" -> Icons.Default.Crop
        "Build" -> Icons.Default.Build
        else -> Icons.Default.Settings
    }
}

@Composable
fun AstraJCodingDialog(
    codingResult: AstraJCodingModels.CodingResult?,
    codingInProgress: Boolean,
    onDismiss: () -> Unit,
    onApplyOption: (AstraJCodingModels.CodingOption, AstraJCodingModels.CodingValue) -> Unit,
    onClearResult: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<AstraJCodingModels.CodingCategory?>(null) }
    var selectedOption by remember { mutableStateOf<AstraJCodingModels.CodingOption?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf<AstraJCodingModels.CodingProfile?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubcategory by remember { mutableStateOf("Alle") }
    val favorites = remember { mutableStateListOf<String>() }
    val history = remember { mutableStateListOf<CodingHistoryEntry>() }
    var showHistory by remember { mutableStateOf(false) }

    LaunchedEffect(codingResult) {
        if (codingResult != null && codingResult.success) {
            val existing = history.indexOfFirst { it.optionId == codingResult.option.id }
            if (existing >= 0) {
                history[existing] = CodingHistoryEntry(
                    optionId = codingResult.option.id,
                    optionName = codingResult.option.displayName,
                    oldValue = history[existing].newValue,
                    newValue = codingResult.newValue.displayName,
                    timestamp = codingResult.timestamp
                )
            } else {
                history.add(
                    0,
                    CodingHistoryEntry(
                        optionId = codingResult.option.id,
                        optionName = codingResult.option.displayName,
                        oldValue = codingResult.option.currentValue?.displayName ?: "Unbekannt",
                        newValue = codingResult.newValue.displayName,
                        timestamp = codingResult.timestamp
                    )
                )
            }
            if (history.size > 10) {
                while (history.size > 10) {
                    history.removeLast()
                }
            }
            kotlinx.coroutines.delay(3000)
            onClearResult()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val currentOption = selectedOption
                AstraJCodingTopBar(
                    category = selectedCategory,
                    option = currentOption,
                    isFavorite = currentOption?.let { favorites.contains(it.id) } == true,
                    onBack = {
                        if (selectedProfile != null) {
                            selectedProfile = null
                        } else if (selectedOption != null) {
                            selectedOption = null
                        } else if (showProfileDialog) {
                            showProfileDialog = false
                        } else if (selectedCategory != null) {
                            selectedCategory = null
                            selectedSubcategory = "Alle"
                        } else {
                            onDismiss()
                        }
                    },
                    onDismiss = onDismiss,
                    onToggleFavorite = {
                        selectedOption?.let { opt ->
                            if (favorites.contains(opt.id)) {
                                favorites.remove(opt.id)
                            } else {
                                favorites.add(opt.id)
                            }
                        }
                    }
                )

                AnimatedContent(
                    targetState = Triple(selectedCategory, selectedOption, showProfileDialog),
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "content"
                ) { (category, option, showProfiles) ->
                    when {
                        showProfiles && selectedProfile == null -> {
                            ProfileSelector(
                                onSelectProfile = { profile ->
                                    selectedProfile = profile
                                },
                                onDismiss = {
                                    showProfileDialog = false
                                }
                            )
                        }
                        selectedProfile != null -> {
                            ProfileApplyPreview(
                                profile = selectedProfile!!,
                                codingInProgress = codingInProgress,
                                onApplyAll = { profile, enabledOptions ->
                                    enabledOptions.forEach { (opt, value) ->
                                        onApplyOption(opt, value)
                                    }
                                    selectedProfile = null
                                    showProfileDialog = false
                                },
                                onBack = { selectedProfile = null }
                            )
                        }
                        option != null -> {
                            CodingOptionDetail(
                                option = option,
                                codingResult = codingResult,
                                codingInProgress = codingInProgress,
                                isFavorite = favorites.contains(option.id),
                                onToggleFavorite = {
                                    if (favorites.contains(option.id)) {
                                        favorites.remove(option.id)
                                    } else {
                                        favorites.add(option.id)
                                    }
                                },
                                onValueChange = { newValue ->
                                    onApplyOption(option, newValue)
                                }
                            )
                        }
                        category != null -> {
                            CodingCategoryDetail(
                                category = category,
                                selectedSubcategory = selectedSubcategory,
                                onSubcategoryChange = { selectedSubcategory = it },
                                favorites = favorites,
                                onOptionClick = { opt -> selectedOption = opt },
                                onToggleFavorite = { opt ->
                                    if (favorites.contains(opt.id)) {
                                        favorites.remove(opt.id)
                                    } else {
                                        favorites.add(opt.id)
                                    }
                                }
                            )
                        }
                        else -> {
                            CategorySelector(
                                categories = AstraJCodingRepository.getAllCategories(),
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                favorites = favorites,
                                history = history,
                                showHistory = showHistory,
                                onToggleHistory = { showHistory = !showHistory },
                                onCategoryClick = { cat ->
                                    selectedCategory = cat
                                    selectedSubcategory = "Alle"
                                },
                                onOptionClick = { opt -> selectedOption = opt },
                                onOptionFavorite = { opt ->
                                    if (favorites.contains(opt.id)) {
                                        favorites.remove(opt.id)
                                    } else {
                                        favorites.add(opt.id)
                                    }
                                },
                                onProfilesClick = { showProfileDialog = true },
                                onClearHistory = { history.clear() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AstraJCodingTopBar(
    category: AstraJCodingModels.CodingCategory?,
    option: AstraJCodingModels.CodingOption?,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val title = when {
        option != null -> option.displayName
        category != null -> category.displayName
        else -> "Opel Astra J Codierung"
    }

    val hasNavigation = option != null || category != null

    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = if (hasNavigation) Icons.Default.ArrowBack else Icons.Default.Close,
                    contentDescription = if (hasNavigation) "Zurück" else "Schließen"
                )
            }
        },
        actions = {
            if (option != null) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) "Favorit entfernen" else "Als Favorit hinzufügen",
                        tint = if (isFavorite) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Schließen"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                text = "Codierungen suchen...",
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Suche löschen",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    )
}

@Composable
private fun FavoritesChipRow(
    favorites: List<String>,
    onOptionClick: (AstraJCodingModels.CodingOption) -> Unit,
    modifier: Modifier = Modifier
) {
    if (favorites.isEmpty()) return

    val allOptions = remember {
        AstraJCodingRepository.getAllCategories().flatMap { it.options }
    }
    val favoriteOptions = remember(favorites) {
        favorites.mapNotNull { id -> allOptions.find { it.id == id } }
    }

    if (favoriteOptions.isEmpty()) return

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Favoriten",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favoriteOptions) { option ->
                AssistChip(
                    onClick = { onOptionClick(option) },
                    label = {
                        Text(
                            text = option.displayName,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(getRiskLevel(option).color)
                        )
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HistorySection(
    history: List<CodingHistoryEntry>,
    showHistory: Boolean,
    onToggle: () -> Unit,
    onClear: () -> Unit
) {
    if (history.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Verlauf",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${history.size}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (showHistory) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = showHistory,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                history.take(5).forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.optionName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row {
                                Text(
                                    text = entry.oldValue,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = " → ",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = entry.newValue,
                                    fontSize = 11.sp,
                                    color = gaugeGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            text = formatRelativeTime(entry.timestamp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                if (history.size > 5) {
                    Text(
                        text = "... und ${history.size - 5} weitere",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verlauf löschen",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsBanner(
    category: AstraJCodingModels.CodingCategory
) {
    val totalOptions = category.options.size
    val uniqueModules = category.options.map { it.module }.distinct().size
    val highRiskCount = category.options.count {
        getRiskLevel(it).level >= 3
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                value = "$totalOptions",
                label = "Codierungen"
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
            StatItem(
                value = "$uniqueModules",
                label = "Module"
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
            StatItem(
                value = "$highRiskCount",
                label = "Risiko: hoch",
                valueColor = if (highRiskCount > 0) gaugeRed else gaugeGreen
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CategorySelector(
    categories: List<AstraJCodingModels.CodingCategory>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    favorites: List<String>,
    history: List<CodingHistoryEntry>,
    showHistory: Boolean,
    onToggleHistory: () -> Unit,
    onCategoryClick: (AstraJCodingModels.CodingCategory) -> Unit,
    onOptionClick: (AstraJCodingModels.CodingOption) -> Unit,
    onOptionFavorite: (AstraJCodingModels.CodingOption) -> Unit,
    onProfilesClick: () -> Unit,
    onClearHistory: () -> Unit
) {
    val allOptions = remember {
        categories.flatMap { cat -> cat.options.map { it to cat } }
    }
    val filteredOptions = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val query = searchQuery.lowercase().trim()
            allOptions.filter { (option, _) ->
                option.displayName.lowercase().contains(query) ||
                        option.description.lowercase().contains(query) ||
                        getOptionTags(option).any { it.lowercase().contains(query) } ||
                        option.module.displayName.lowercase().contains(query)
            }.map { it.first }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )
        }

        if (searchQuery.isBlank()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                FavoritesChipRow(
                    favorites = favorites,
                    onOptionClick = onOptionClick
                )
            }

            item {
                ProfileCard(onClick = onProfilesClick)
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kategorien",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            items(categories) { category ->
                CategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }

            item {
                HistorySection(
                    history = history,
                    showHistory = showHistory,
                    onToggle = onToggleHistory,
                    onClear = onClearHistory
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${filteredOptions.size} Ergebnisse",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            if (filteredOptions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Keine Ergebnisse für \"$searchQuery\"",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(filteredOptions) { option ->
                    CodingOptionCard(
                        option = option,
                        isFavorite = favorites.contains(option.id),
                        showCategoryBadge = true,
                        onClick = { onOptionClick(option) },
                        onToggleFavorite = { onOptionFavorite(option) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Schnellprofile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Komplette Profile laden (Stock, Komfort, Sport, Eco)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: AstraJCodingModels.CodingCategory,
    onClick: () -> Unit
) {
    val icon = getCategoryIcon(category.icon)
    val highRiskCount = category.options.count { getRiskLevel(it).level >= 3 }
    val maxRisk = category.options.maxOfOrNull { getRiskLevel(it).level } ?: 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${category.options.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (highRiskCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (maxRisk) {
                                        4 -> Color(0xFF991B1B)
                                        3 -> gaugeRed
                                        2 -> gaugeOrange
                                        else -> gaugeGreen
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (highRiskCount > 1) "$highRiskCount risikante Codierungen" else "$highRiskCount risikante Codierung",
                            fontSize = 11.sp,
                            color = gaugeRed.copy(alpha = 0.8f)
                        )
                    } else {
                        Text(
                            text = "Alle sicher",
                            fontSize = 11.sp,
                            color = gaugeGreen.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubcategoryChips(
    subcategories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        items(subcategories) { sub ->
            val isSelected = sub == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(sub) },
                label = {
                    Text(
                        text = sub,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun CodingCategoryDetail(
    category: AstraJCodingModels.CodingCategory,
    selectedSubcategory: String,
    onSubcategoryChange: (String) -> Unit,
    favorites: List<String>,
    onOptionClick: (AstraJCodingModels.CodingOption) -> Unit,
    onToggleFavorite: (AstraJCodingModels.CodingOption) -> Unit
) {
    val subcategories = remember(category) {
        val subs = category.options.map { getSubcategories(it) }.distinct()
        listOf("Alle") + subs
    }

    val filteredOptions = remember(category, selectedSubcategory) {
        if (selectedSubcategory == "Alle") category.options
        else category.options.filter { getSubcategories(it) == selectedSubcategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            StatisticsBanner(category = category)
            Spacer(modifier = Modifier.height(12.dp))
            SubcategoryChips(
                subcategories = subcategories,
                selected = selectedSubcategory,
                onSelect = onSubcategoryChange
            )
        }

        items(filteredOptions) { option ->
            CodingOptionCard(
                option = option,
                isFavorite = favorites.contains(option.id),
                showCategoryBadge = false,
                onClick = { onOptionClick(option) },
                onToggleFavorite = { onToggleFavorite(option) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CodingOptionCard(
    option: AstraJCodingModels.CodingOption,
    isFavorite: Boolean,
    showCategoryBadge: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val risk = getRiskLevel(option)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(risk.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option.displayName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = risk.color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = risk.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = risk.color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isFavorite) "Favorit entfernen" else "Favorit hinzufügen",
                            tint = if (isFavorite) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = option.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showCategoryBadge) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = option.module.address,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = getSubcategories(option),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (option.hardwareRequired != null) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Hardware erforderlich",
                        tint = gaugeOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = option.channel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CodingOptionDetail(
    option: AstraJCodingModels.CodingOption,
    codingResult: AstraJCodingModels.CodingResult?,
    codingInProgress: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onValueChange: (AstraJCodingModels.CodingValue) -> Unit
) {
    var selectedValue by remember { mutableStateOf(option.currentValue ?: option.values.firstOrNull()) }
    var showDescription by remember { mutableStateOf(false) }
    val risk = getRiskLevel(option)

    val resultColor = when {
        codingResult == null -> Color.Unspecified
        codingResult.success -> gaugeGreen
        else -> gaugeRed
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (codingResult != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = resultColor.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (codingResult.success) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = resultColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (codingResult.success) "Erfolgreich gespeichert!" else "Fehler",
                                fontWeight = FontWeight.Bold,
                                color = resultColor
                            )
                            Text(
                                text = codingResult.error ?: codingResult.newValue.displayName,
                                fontSize = 12.sp,
                                color = resultColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = risk.color.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(risk.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = risk.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = risk.color
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = option.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "Modul",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                            Text(
                                text = option.module.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column {
                            Text(
                                text = "Kanal",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                            Text(
                                text = option.channel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column {
                            Text(
                                text = "Wert",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                            Text(
                                text = option.currentValue?.displayName ?: "Unbekannt",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (option.hardwareRequired != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = gaugeOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option.hardwareRequired,
                                fontSize = 12.sp,
                                color = gaugeOrange
                            )
                        }
                    }
                    if (option.requiresCarPass) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CarPass/Sicherheitscode erforderlich",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDescription = !showDescription }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Beschreibung",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (showDescription) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(
                visible = showDescription,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = option.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Verfügbare Werte:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        items(option.values) { value ->
            val isSelected = selectedValue == value
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedValue = value
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedValue = value }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = value.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        if (value.description.isNotEmpty()) {
                            Text(
                                text = value.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Text(
                        text = value.value,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    selectedValue?.let { onValueChange(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedValue != null && !codingInProgress,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (codingInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (codingInProgress) "Wird gespeichert..." else "Codierung speichern",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileSelector(
    onSelectProfile: (AstraJCodingModels.CodingProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val profiles = AstraJCodingRepository.getProfiles()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Schnellprofile laden",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Profile ändern mehrere Einstellungen gleichzeitig. Vorschau wird vor dem Anzeigen angezeigt.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(profiles) { profile ->
            ProfileCardItem(
                profile = profile,
                onClick = { onSelectProfile(profile) }
            )
        }
    }
}

@Composable
private fun ProfileCardItem(
    profile: AstraJCodingModels.CodingProfile,
    onClick: () -> Unit
) {
    val icon = when (profile.id) {
        "stock" -> Icons.Default.Settings
        "comfort" -> Icons.Default.Home
        "sport" -> Icons.Default.Sports
        "eco" -> Icons.Default.Eco
        else -> Icons.Default.Folder
    }

    val color = when (profile.id) {
        "stock" -> Color(0xFF607D8B)
        "comfort" -> Color(0xFF2196F3)
        "sport" -> Color(0xFFF44336)
        "eco" -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = profile.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                if (profile.options.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profile.options.size} Einstellungen",
                        fontSize = 10.sp,
                        color = color
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Werkseinstellungen",
                        fontSize = 10.sp,
                        color = color
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileApplyPreview(
    profile: AstraJCodingModels.CodingProfile,
    codingInProgress: Boolean,
    onApplyAll: (AstraJCodingModels.CodingProfile, Map<AstraJCodingModels.CodingOption, AstraJCodingModels.CodingValue>) -> Unit,
    onBack: () -> Unit
) {
    val allOptions = remember {
        AstraJCodingRepository.getAllCategories().flatMap { it.options }
    }

    val profileChanges = remember(profile, allOptions) {
        profile.options.mapNotNull { (optionId, targetValue) ->
            val option = allOptions.find { it.id == optionId } ?: return@mapNotNull null
            val target = option.values.find { it.value == targetValue } ?: return@mapNotNull null
            Triple(option, option.currentValue, target)
        }
    }

    val enabledChanges = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(profileChanges) {
        profileChanges.forEach { (option, _, _) ->
            if (!enabledChanges.containsKey(option.id)) {
                enabledChanges[option.id] = true
            }
        }
    }

    val enabledCount = enabledChanges.values.count { it }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = profile.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$enabledCount / ${profileChanges.size} Änderungen",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Änderungen:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        itemsIndexed(profileChanges) { index, (option, current, target) ->
            val isEnabled = enabledChanges[option.id] == true

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            enabledChanges[option.id] = checked
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = option.displayName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isEnabled)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = current?.displayName ?: "Unbekannt",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = target.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isEnabled) gaugeGreen else gaugeGreen.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val changesToApply = mutableMapOf<AstraJCodingModels.CodingOption, AstraJCodingModels.CodingValue>()
                    profileChanges.forEach { (option, _, target) ->
                        if (enabledChanges[option.id] == true) {
                            changesToApply[option] = target
                        }
                    }
                    if (changesToApply.isNotEmpty()) {
                        onApplyAll(profile, changesToApply)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabledCount > 0 && !codingInProgress,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (codingInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wird angewendet...")
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alle anwenden ($enabledCount)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
