package com.canopobd.ui.fuel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.canopobd.R
import com.canopobd.data.model.FuelEconomyData
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*

@Composable
fun FuelEconomyDialog(
    fuelEconomyData: FuelEconomyData,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.fuel_economy_title),
        eyebrow = "Kraftstoffverbrauch",
        heightFraction = 0.85f
    ) {
        if (fuelEconomyData.currentL100km == 0.0 && fuelEconomyData.currentKmL == 0.0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(colors.surfaceRaised),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.LocalGasStation,
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.fuel_economy_no_data),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Fahrt erforderlich",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (fuelEconomyData.estimatedFromMaf) {
                    item {
                        GlassCard(
                            accentEdge = colors.primary,
                            padding = 10.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.fuel_economy_from_maf),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }

                item { SectionHeader(title = stringResource(R.string.fuel_economy_current), icon = Icons.Filled.Speed) }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EconomyCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.fuel_economy_l100km),
                            value = "%.1f".format(fuelEconomyData.currentL100km),
                            color = colors.success
                        )
                        EconomyCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.fuel_economy_kml),
                            value = "%.1f".format(fuelEconomyData.currentKmL),
                            color = colors.info
                        )
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EconomyCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.fuel_economy_mpg_us),
                            value = "%.1f".format(fuelEconomyData.currentMpgUs),
                            color = colors.warning
                        )
                        EconomyCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.fuel_economy_mpg_uk),
                            value = "%.1f".format(fuelEconomyData.currentMpgUk),
                            color = colors.warning
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(4.dp))
                    SectionHeader(title = stringResource(R.string.fuel_economy_avg), icon = Icons.Filled.TrendingFlat)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EconomyCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.fuel_economy_l100km),
                            value = "%.1f".format(fuelEconomyData.avgL100km),
                            color = colors.success
                        )
                        EconomyCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.fuel_economy_kml),
                            value = "%.1f".format(fuelEconomyData.avgKmL),
                            color = colors.info
                        )
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EconomyCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.fuel_economy_mpg_us),
                            value = "%.1f".format(fuelEconomyData.avgMpgUs),
                            color = colors.warning
                        )
                        EconomyCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.fuel_economy_mpg_uk),
                            value = "%.1f".format(fuelEconomyData.avgMpgUk),
                            color = colors.warning
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun EconomyCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(AppRadius.md))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
