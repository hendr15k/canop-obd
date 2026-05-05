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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.PerformanceResult
import com.canopobd.data.model.PerformanceTestState
import com.canopobd.data.model.PerformanceTestType
import com.canopobd.ui.theme.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceTestDialog(
    testState: PerformanceTestState,
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
                        RunningIndicator(statusMessage = testState.statusMessage)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (testState.lastResult != null && testState.lastResult.valid) {
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
                                text = stringResource(R.string.perf_test_ready),
                                fontSize = 12.sp,
                                color = textDim
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
private fun RunningIndicator(statusMessage: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(gaugeGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(gaugeGreen.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Speed,
                    contentDescription = null,
                    tint = gaugeGreen,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.perf_test_running),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = gaugeGreen
        )
        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                fontSize = 14.sp,
                color = textSecondary
            )
        }
    }
}
