package com.canopobd.ui.power

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.canopobd.data.model.PowerCalculation
import com.canopobd.ui.theme.*

@Composable
fun PowerCalculatorDialog(
    calculation: PowerCalculation,
    rpm: Double,
    maf: Double,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
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
                        text = stringResource(R.string.power_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = canopoDark
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoChip(label = "RPM", value = "%.0f".format(rpm))
                        InfoChip(label = "MAF", value = "%.1f g/s".format(maf))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!calculation.isValid) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Speed, contentDescription = null, tint = textDim, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.power_start_engine),
                                fontSize = 16.sp,
                                color = textDim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.power_rpm_maf_required),
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PowerCard(
                            modifier = Modifier.weight(1f),
                            label = "PS",
                            value = "%.0f".format(calculation.horsepowerMetric),
                            subtitle = stringResource(R.string.power_metric),
                            color = gaugeGreen
                        )
                        PowerCard(
                            modifier = Modifier.weight(1f),
                            label = "HP",
                            value = "%.0f".format(calculation.horsepower),
                            subtitle = stringResource(R.string.power_us),
                            color = gaugeCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PowerCard(
                            modifier = Modifier.weight(1f),
                            label = "Nm",
                            value = "%.0f".format(calculation.torqueNm),
                            subtitle = stringResource(R.string.power_torque),
                            color = gaugeOrange
                        )
                        PowerCard(
                            modifier = Modifier.weight(1f),
                            label = "kW",
                            value = "%.0f".format(calculation.horsepower / 1.341),
                            subtitle = "Leistung",
                            color = gaugeCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = gaugeGreen.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = gaugeGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.power_estimated_maf),
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = canopoDark
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textDim
            )
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = canopoAccent)
        Text(text = label, fontSize = 11.sp, color = textDim)
    }
}
