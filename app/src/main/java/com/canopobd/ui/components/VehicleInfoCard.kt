package com.canopobd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.*
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

@Composable
fun VehicleInfoCard(
    vehicleInfo: VehicleInfoData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.vehicle_info),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                if (vehicleInfo.supportedModes.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.accent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${vehicleInfo.supportedModes.size} ${stringResource(R.string.modes)}",
                            fontSize = 10.sp,
                            color = colors.accent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (vehicleInfo.vin.isNotEmpty()) {
                VINSection(vin = vehicleInfo.vin, colors = colors)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (vehicleInfo.calibrationId.isNotEmpty()) {
                    InfoSection(
                        label = stringResource(R.string.calibration_id),
                        value = vehicleInfo.calibrationId,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (vehicleInfo.cvn.isNotEmpty()) {
                    CVNSection(
                        cvn = vehicleInfo.cvn,
                        isValid = vehicleInfo.cvnValid,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (vehicleInfo.ecuName.isNotEmpty() || vehicleInfo.ecuVersion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                ECUSection(
                    name = vehicleInfo.ecuName,
                    version = vehicleInfo.ecuVersion,
                    colors = colors
                )
            }

            if (vehicleInfo.protocol.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ProtocolSection(protocol = vehicleInfo.protocol, colors = colors)
            }

            if (vehicleInfo.supportedModes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                SupportedModesRow(modes = vehicleInfo.supportedModes, colors = colors)
            }
        }
    }
}

@Composable
private fun VINSection(
    vin: String,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.vin),
                    fontSize = 10.sp,
                    color = colors.textDim
                )
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null,
                    tint = colors.textDim,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = vin,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                letterSpacing = 2.sp
            )

            if (vin.length == 17) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.manufacturer)}: ${vin.take(3)} | ${stringResource(R.string.model_year)}: ${getModelYear(vin)}",
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun InfoSection(
    label: String,
    value: String,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = colors.textDim
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CVNSection(
    cvn: String,
    isValid: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cvn),
                    fontSize = 10.sp,
                    color = colors.textDim
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isValid) colors.gaugeGreen else colors.gaugeOrange)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cvn,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun ECUSection(
    name: String,
    version: String,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Memory,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.ecu_label),
                        fontSize = 10.sp,
                        color = colors.textDim
                    )
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }

            if (version.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.version),
                        fontSize = 9.sp,
                        color = colors.textDim
                    )
                    Text(
                        text = version,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocolSection(
    protocol: String,
    colors: AppColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.SettingsEthernet,
            contentDescription = null,
            tint = colors.textDim,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.protocol) + ":",
            fontSize = 11.sp,
            color = colors.textDim
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = protocol,
            fontSize = 11.sp,
            color = colors.textSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SupportedModesRow(
    modes: List<String>,
    colors: AppColors
) {
    Column {
        Text(
            text = stringResource(R.string.supported_modes),
            fontSize = 10.sp,
            color = colors.textDim
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(modes) { mode ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colors.accent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = mode,
                        fontSize = 11.sp,
                        color = colors.accent,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun getModelYear(vin: String): String {
    return try {
        val yearCode = vin.getOrNull(9)?.uppercaseChar() ?: return "?"
        val year = when (yearCode) {
            'A' -> 2010
            'B' -> 2011
            'C' -> 2012
            'D' -> 2013
            'E' -> 2014
            'F' -> 2015
            'G' -> 2016
            'H' -> 2017
            'J' -> 2018
            'K' -> 2019
            'L' -> 2020
            'M' -> 2021
            'N' -> 2022
            'P' -> 2023
            'R' -> 2024
            else -> return "? ($yearCode)"
        }
        year.toString()
    } catch (e: Exception) {
        "?"
    }
}
