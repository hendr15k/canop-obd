package com.canopobd.ui.performance

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.AccelerationRun
import com.canopobd.data.model.PerformanceTestState
import com.canopobd.data.model.PerformanceTestType
import com.canopobd.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceTestDialog(
    testState: PerformanceTestState,
    gpsSpeedKmh: Double,
    accelerationRun: AccelerationRun?,
    onDismiss: () -> Unit,
    onStartTest: (PerformanceTestType) -> Unit,
    onStopTest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
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
                        text = stringResource(R.string.perf_test_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var selectedTest by remember { mutableStateOf(testState.currentTestType) }

                if (!testState.isRunning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PerformanceTestType.entries.forEach { test ->
                            FilterChip(
                                selected = selectedTest == test,
                                onClick = { selectedTest = test },
                                label = { Text(test.label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = canopoAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = canopoAccent
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (testState.isRunning) {
                        RunningGpsIndicator(
                            statusMessage = testState.statusMessage,
                            gpsSpeedKmh = gpsSpeedKmh,
                            progress = accelerationRun?.let {
                                val target = when (selectedTest) {
                                    PerformanceTestType.ZERO_100 -> 100.0
                                    PerformanceTestType.ZERO_200 -> 200.0
                                    PerformanceTestType.HUNDRED_200 -> 200.0
                                }
                                val start = when (selectedTest) {
                                    PerformanceTestType.HUNDRED_200 -> 100.0
                                    else -> 0.0
                                }
                                ((gpsSpeedKmh - start) / (target - start)).coerceIn(0.0, 1.0).toFloat()
                            } ?: 0f,
                            testType = selectedTest
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (accelerationRun != null && accelerationRun.valid) {
                                // Show rich result
                                Text(
                                    text = "%.2f s".format(accelerationRun.timeSeconds),
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = gaugeGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = accelerationRun.testType.label,
                                    fontSize = 16.sp,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Phase details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    if (accelerationRun.timeTo50Percent != null) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "%.1f s".format(accelerationRun.timeTo50Percent),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = canopoAccent
                                            )
                                            Text(
                                                text = "50%",
                                                fontSize = 11.sp,
                                                color = textDim
                                            )
                                        }
                                    }
                                    if (accelerationRun.timeTo90Percent != null) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "%.1f s".format(accelerationRun.timeTo90Percent),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = canopoAccent
                                            )
                                            Text(
                                                text = "90%",
                                                fontSize = 11.sp,
                                                color = textDim
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "%.1f m/s²".format(accelerationRun.maxAcceleration),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = canopoHighlight
                                        )
                                        Text(
                                            text = "Max Accel",
                                            fontSize = 11.sp,
                                            color = textDim
                                        )
                                    }
                                    if (accelerationRun.gearShifts.isNotEmpty()) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${accelerationRun.gearShifts.size}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = canopoSecondary
                                            )
                                            Text(
                                                text = "Schaltungen",
                                                fontSize = 11.sp,
                                                color = textDim
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            } else if (testState.lastResult != null && testState.lastResult.valid) {
                                Text(
                                    text = stringResource(R.string.perf_test_result, testState.lastResult.timeSeconds),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = gaugeGreen
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = testState.lastResult.testType.label,
                                    fontSize = 16.sp,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Start/Stop button
                            Button(
                                onClick = {
                                    if (testState.isRunning) {
                                        onStopTest()
                                    } else {
                                        onStartTest(selectedTest)
                                    }
                                },
                                modifier = Modifier.size(100.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (testState.isRunning) gaugeRed else gaugeGreen
                                )
                            ) {
                                Text(
                                    text = if (testState.isRunning) {
                                        stringResource(R.string.perf_test_stop)
                                    } else {
                                        stringResource(R.string.perf_test_start)
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (testState.isRunning) {
                                    stringResource(R.string.perf_test_running)
                                } else {
                                    stringResource(R.string.perf_test_ready)
                                },
                                fontSize = 12.sp,
                                color = textDim
                            )

                            // GPS indicator
                            if (!testState.isRunning) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.MyLocation,
                                        contentDescription = null,
                                        tint = if (gpsSpeedKmh > 0) gaugeGreen else textDim,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (gpsSpeedKmh > 0) "GPS: %.0f km/h".format(gpsSpeedKmh) else "GPS: Kein Signal",
                                        fontSize = 11.sp,
                                        color = if (gpsSpeedKmh > 0) gaugeGreen else textDim
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // History
                if (testState.history.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.perf_test_history),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp)
                    ) {
                        items(testState.history.take(5)) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = result.testType.label,
                                    fontSize = 12.sp,
                                    color = textDim
                                )
                                Text(
                                    text = if (result.valid) "%.2f s".format(result.timeSeconds) else stringResource(R.string.perf_test_no_result),
                                    fontSize = 12.sp,
                                    color = if (result.valid) gaugeGreen else gaugeRed
                                )
                            }
                            @Suppress("DEPRECATION")
                            Divider(color = canopoDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RunningGpsIndicator(
    statusMessage: String,
    gpsSpeedKmh: Double,
    progress: Float,
    testType: PerformanceTestType
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Large GPS speed display
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(gaugeGreen.copy(alpha = 0.15f))
                .border(3.dp, gaugeGreen.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.0f".format(gpsSpeedKmh),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = gaugeGreen
                )
                Text(
                    text = "km/h",
                    fontSize = 14.sp,
                    color = gaugeGreen.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(gaugeGreen.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(gaugeGreen)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = testType.label,
            fontSize = 12.sp,
            color = textDim
        )

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = gaugeGreen,
                textAlign = TextAlign.Center
            )
        }
    }
}
