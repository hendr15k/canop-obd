package com.canopobd.ui.carprofile

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.CarProfile
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
                items(CarProfile.entries) { profile ->
                    ProfileCard(
                        profile = profile,
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
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.canopobd.ui.theme.AppColors
) {
    val borderColor = if (isSelected) colors.accent else colors.surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
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
        }
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
