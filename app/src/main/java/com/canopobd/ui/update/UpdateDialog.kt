package com.canopobd.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.canopobd.R
import com.canopobd.data.model.AppUpdate
import com.canopobd.ui.theme.LocalAppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun UpdateDialog(
    update: AppUpdate,
    onDismiss: () -> Unit,
    onSkipVersion: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    @Suppress("DEPRECATION")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier
                        .size(32.dp)
                        .alpha(if (isDownloading) pulseAlpha else 1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.update_available),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "v${update.versionName}",
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                if (isDownloading) {
                    @Suppress("DEPRECATION")
                    LinearProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = colors.accent,
                        trackColor = colors.dark,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.update_downloading, (downloadProgress * 100).toInt()),
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else if (downloadError != null) {
                    Text(
                        text = stringResource(R.string.update_download_error, downloadError ?: ""),
                        fontSize = 13.sp,
                        color = colors.gaugeOrange
                    )
                } else if (downloadedFile != null) {
                    Text(
                        text = stringResource(R.string.update_ready_to_install),
                        fontSize = 14.sp,
                        color = colors.highlight
                    )
                } else {
                    if (update.releaseNotes.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.update_release_notes),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.dark.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = update.releaseNotes.take(1500),
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.update_new_version_hint, update.versionName),
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!isDownloading && downloadedFile == null) {
                    TextButton(onClick = {
                        onSkipVersion()
                        onDismiss()
                    }) {
                        Text(
                            stringResource(R.string.update_skip),
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (downloadedFile != null) {
                    Button(
                        onClick = {
                            installApk(context, downloadedFile!!)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.update_install),
                            color = colors.dark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else if (!isDownloading) {
                    Button(
                        onClick = {
                            isDownloading = true
                            downloadApk(
                                url = update.apkDownloadUrl,
                                context = context,
                                onProgress = { downloadProgress = it },
                                onComplete = { file ->
                                    isDownloading = false
                                    downloadedFile = file
                                },
                                onError = { error ->
                                    isDownloading = false
                                    downloadError = error
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.update_download),
                            color = colors.dark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        dismissButton = {}
    )
}

private fun downloadApk(
    url: String,
    context: Context,
    onProgress: (Float) -> Unit,
    onComplete: (File) -> Unit,
    onError: (String) -> Unit
) {
    Thread {
        try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            val fileLength = connection.contentLength
            val inputStream = connection.inputStream

            val file = File(context.cacheDir, "canop-obd-update.apk")
            val outputStream = file.outputStream()

            val buffer = ByteArray(8192)
            var totalRead = 0L
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                if (fileLength > 0) {
                    onProgress(totalRead.toFloat() / fileLength)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            onComplete(file)
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }.start()
}

private fun installApk(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: open download URL in browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hendr15k/canop-obd/releases/latest"))
        context.startActivity(intent)
    }
}
