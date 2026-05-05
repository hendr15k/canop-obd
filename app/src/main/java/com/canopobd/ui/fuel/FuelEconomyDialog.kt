package com.canopobd.ui.fuel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.FuelEconomyData
import com.canopobd.ui.theme.*

@Composable
fun FuelEconomyDialog(
    fuelEconomyData: FuelEconomyData,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.fuel_economy_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (fuelEconomyData.currentL100km == 0.0 && fuelEconomyData.currentKmL == 0.0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.LocalGasStation, contentDescription = null, tint = textDim, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.fuel_economy_no_data),
                                fontSize = 16.sp,
                                color = textDim
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        item {
                            if (fuelEconomyData.estimatedFromMaf) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = canopoAccent.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Info, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.fuel_economy_from_maf),
                                            fontSize = 12.sp,
                                            color = canopoAccent
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        item {
                            Text(
                                text = stringResource(R.string.fuel_economy_current),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                EconomyCard(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.fuel_economy_l100km),
                                    value = "%.1f".format(fuelEconomyData.currentL100km),
                                    color = gaugeGreen
                                )
                                EconomyCard(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.fuel_economy_kml),
                                    value = "%.1f".format(fuelEconomyData.currentKmL),
                                    color = gaugeCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                EconomyCard(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.fuel_economy_mpg_us),
                                    value = "%.1f".format(fuelEconomyData.currentMpgUs),
                                    color = gaugeYellow
                                )
                                EconomyCard(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.fuel_economy_mpg_uk),
                                    value = "%.1f".format(fuelEconomyData.currentMpgUk),
                                    color = gaugeOrange
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(R.string.fuel_economy_avg),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                EconomyCard(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.fuel_economy_l100km),
                                    value = "%.1f".format(fuelEconomyData.avgL100km),
                                    color = gaugeGreen
                                )
                                EconomyCard(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.fuel_economy_kml),
                                    value = "%.1f".format(fuelEconomyData.avgKmL),
                                    color = gaugeCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                EconomyCard(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.fuel_economy_mpg_us),
                                    value = "%.1f".format(fuelEconomyData.avgMpgUs),
                                    color = gaugeYellow
                                )
                                EconomyCard(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.fuel_economy_mpg_uk),
                                    value = "%.1f".format(fuelEconomyData.avgMpgUk),
                                    color = gaugeOrange
                                )
                            }
                        }
                    }
                }
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = textSecondary
            )
        }
    }
}
