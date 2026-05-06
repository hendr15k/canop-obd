package com.canopobd.ui.coding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.model.AstraJCodingModels
import com.canopobd.data.model.AstraJCodingRepository

@Composable
fun AstraJCodingDialog(
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<AstraJCodingModels.CodingCategory?>(null) }
    var selectedOption by remember { mutableStateOf<AstraJCodingModels.CodingOption?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }

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
                AstraJCodingTopBar(
                    category = selectedCategory,
                    onBack = {
                        if (selectedOption != null) {
                            selectedOption = null
                        } else if (selectedCategory != null) {
                            selectedCategory = null
                        } else {
                            onDismiss()
                        }
                    },
                    option = selectedOption,
                    onDismiss = onDismiss
                )

                AnimatedContent(
                    targetState = Triple(selectedCategory, selectedOption, showProfileDialog),
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "content"
                ) { (category, option, showProfiles) ->
                    when {
                        showProfiles -> {
                            ProfileSelector(
                                onSelectProfile = { profile ->
                                    showProfileDialog = false
                                },
                                onDismiss = { showProfileDialog = false }
                            )
                        }
                        option != null -> {
                            CodingOptionDetail(
                                option = option,
                                onValueChange = { newValue ->
                                }
                            )
                        }
                        category != null -> {
                            CodingCategoryDetail(
                                category = category,
                                onOptionClick = { opt -> selectedOption = opt }
                            )
                        }
                        else -> {
                            CategorySelector(
                                categories = AstraJCodingRepository.getAllCategories(),
                                onCategoryClick = { cat -> selectedCategory = cat },
                                onProfilesClick = { showProfileDialog = true }
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
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when {
        option != null -> option.displayName
        category != null -> category.displayName
        else -> "Opel Astra J Codierung"
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = if (option != null || category != null) Icons.Default.ArrowBack else Icons.Default.Close,
                    contentDescription = "Zurück"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
private fun CategorySelector(
    categories: List<AstraJCodingModels.CodingCategory>,
    onCategoryClick: (AstraJCodingModels.CodingCategory) -> Unit,
    onProfilesClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ProfileCard(onClick = onProfilesClick)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(categories) { category ->
            CategoryCard(
                category = category,
                onClick = { onCategoryClick(category) }
            )
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
        )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                Text(
                    text = category.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${category.options.size} Codierungen verfügbar",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
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
private fun CodingCategoryDetail(
    category: AstraJCodingModels.CodingCategory,
    onOptionClick: (AstraJCodingModels.CodingOption) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Wähle eine Codierung:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(category.options) { option ->
            CodingOptionCard(
                option = option,
                onClick = { onOptionClick(option) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodingOptionCard(
    option: AstraJCodingModels.CodingOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Badge(
                    containerColor = if (option.currentValue != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = option.module.address,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = option.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kanal: ${option.channel}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary
            )
            if (option.hardwareRequired != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠ ${option.hardwareRequired}",
                    fontSize = 10.sp,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun CodingOptionDetail(
    option: AstraJCodingModels.CodingOption,
    onValueChange: (AstraJCodingModels.CodingValue) -> Unit
) {
    var selectedValue by remember { mutableStateOf(option.currentValue ?: option.values.firstOrNull()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = option.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = option.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
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
                    Spacer(modifier = Modifier.width(24.dp))
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
                }
                if (option.hardwareRequired != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option.hardwareRequired,
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
                if (option.requiresCarPass) {
                    Spacer(modifier = Modifier.height(4.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Verfügbare Werte:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(option.values) { value ->
                val isSelected = selectedValue == value
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedValue = value
                            onValueChange(value)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                selectedValue = value
                                onValueChange(value)
                            }
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedValue != null
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Codierung speichern")
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
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Profile ändern mehrere Einstellungen gleichzeitig.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
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
        )
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
                }
            }
        }
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
        else -> Icons.Default.Settings
    }
}
