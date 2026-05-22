package com.canopobd.ui.carprofile

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.CarProfile
import com.canopobd.data.model.VehicleProfile
import com.canopobd.ui.theme.LocalAppColors

@Composable
fun CarProfileDialog(
    currentProfile: CarProfile,
    onSelectProfile: (CarProfile) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.car_profile_title),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CarProfile.allWithVehicleProfile()) { (profile, vehicleProfile) ->
                    ProfileCard(
                        profile = profile,
                        vehicleProfile = vehicleProfile,
                        isSelected = profile == currentProfile,
                        onClick = { onSelectProfile(profile) },
                        colors = colors
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = colors.accent)
            }
        }
    )
}

@Composable
private fun ProfileCard(
    profile: CarProfile,
    vehicleProfile: VehicleProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.canopobd.ui.theme.AppColors
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) colors.accent.copy(alpha = 0.1f) else colors.surface.copy(alpha = 0.3f),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, colors.accent)
        } else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profile.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) colors.accent else colors.textPrimary
                )
                if (isSelected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    label = stringResource(R.string.power_short),
                    value = profile.power.split("@").firstOrNull()?.trim() ?: profile.power,
                    colors = colors
                )
                InfoChip(
                    label = stringResource(R.string.torque_short),
                    value = profile.torque.split("@").firstOrNull()?.trim() ?: profile.torque,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    label = stringResource(R.string.redline_short),
                    value = "${profile.redlineRpm} rpm",
                    colors = colors
                )
                InfoChip(
                    label = stringResource(R.string.boost_short),
                    value = "%.1f bar".format(profile.normalBoostBar),
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${profile.engineCode} • ${profile.displacement}",
                fontSize = 10.sp,
                color = colors.textSecondary
            )
            Text(
                text = "${profile.turboType}",
                fontSize = 10.sp,
                color = colors.textDim
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Less details" else "More details",
                    fontSize = 11.sp,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                VehicleProfileSection(vehicleProfile = vehicleProfile, colors = colors)
            }
    }
    }
}

@Composable
private fun VehicleProfileSection(
    vehicleProfile: VehicleProfile,
    colors: com.canopobd.ui.theme.AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SectionHeader(text = "Engine Specs", colors = colors)
        DetailRow(label = "Displacement", value = vehicleProfile.displacement, colors = colors)
        DetailRow(label = "Fuel Type", value = vehicleProfile.fuelType, colors = colors)
        DetailRow(label = "Recommended Octane", value = "${vehicleProfile.recommendedFuelOctane} RON", colors = colors)

        Spacer(modifier = Modifier.height(4.dp))
        SectionHeader(text = "ECU", colors = colors)
        DetailRow(label = "ECU Type", value = vehicleProfile.ecuType, colors = colors)
        DetailRow(label = "Transmission", value = vehicleProfile.transmission, colors = colors)

        vehicleProfile.calibration?.let { cal ->
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(text = "Calibration Highlights", colors = colors)
            DetailRow(label = "Normal Boost", value = "%.2f bar".format(cal.normalBoostTargetBar), colors = colors)
            DetailRow(label = "Overboost", value = "%.2f bar".format(cal.overboostBar), colors = colors)
            DetailRow(label = "Max Boost", value = "%.2f bar".format(cal.maxBoostBar), colors = colors)
        }

        Spacer(modifier = Modifier.height(4.dp))
        DetailRow(
            label = "Maintenance Items",
            value = "${vehicleProfile.maintenanceItems.size}",
            colors = colors
        )
        DetailRow(
            label = "Known Issues",
            value = "${vehicleProfile.knownIssues.size}",
            colors = colors
        )
    }
}

@Composable
private fun SectionHeader(text: String, colors: com.canopobd.ui.theme.AppColors) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.accent
    )
}

@Composable
private fun DetailRow(label: String, value: String, colors: com.canopobd.ui.theme.AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = colors.textDim
        )
        Text(
            text = value,
            fontSize = 10.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    colors: com.canopobd.ui.theme.AppColors
) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            color = colors.textDim
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary
        )
    }
}
