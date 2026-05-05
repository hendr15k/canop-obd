package com.canopobd.ui.drivescore

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.DriveScore
import com.canopobd.ui.theme.*

@Composable
fun DriveScoreDialog(
    score: DriveScore,
    sessionDuration: Long,
    harshAccels: Int,
    harshBrakes: Int,
    idleTimeSeconds: Long,
    avgRpm: Double,
    avgThrottle: Double,
    avgSpeed: Double,
    onDismiss: () -> Unit,
    onResetScore: () -> Unit
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
                        text = "Fahrstil-Analyse",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    Row {
                        IconButton(onClick = onResetScore) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = gaugeYellow, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .background(Color(score.color).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = score.grade,
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(score.color)
                                )
                                Text(
                                    text = "${score.score}/100",
                                    fontSize = 16.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ScoreMini(score.accelerationScore, "Beschleunigung", gaugeGreen)
                            ScoreMini(score.brakingScore, "Bremsen", gaugeRed)
                            ScoreMini(score.cruisingScore, "Schwung", gaugeCyan)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ScoreMini(score.idleScore, "Leerlauf", gaugeYellow)
                            ScoreMini(score.rpmScore, "Drehzahl", gaugeOrange)
                            ScoreMini(score.throttleScore, "Gas", gaugeCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = canopoDark
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Fahrtsitzung",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = canopoAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(label = "Dauer", value = formatDuration(sessionDuration))
                            StatItem(label = "Ø Drehzahl", value = "%.0f rpm".format(avgRpm))
                            StatItem(label = "Ø Gas", value = "%.0f%%".format(avgThrottle))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(label = "Ø Speed", value = "%.0f km/h".format(avgSpeed))
                            StatItem(label = "Rüde Beschl.", value = "$harshAccels")
                            StatItem(label = "Rüde Brems.", value = "$harshBrakes")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(label = "Leerlaufzeit", value = formatDuration(idleTimeSeconds))
                            StatItem(label = "Verbrauch", value = "--- L/100km")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreMini(score: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = textDim
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        Text(text = label, fontSize = 10.sp, color = textDim)
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, mins, secs)
    } else {
        "%d:%02d".format(mins, secs)
    }
}
