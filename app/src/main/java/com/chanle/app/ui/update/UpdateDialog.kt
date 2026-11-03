package com.chanle.app.ui.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.chanle.app.data.update.UpdateInfo

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    currentVersion: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var fileSize by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (downloadState is DownloadState.Idle) {
            checkFileSize(updateInfo.downloadUrl) { size ->
                fileSize = size
            }
            startDownload(context, updateInfo.downloadUrl, updateInfo.version) { state ->
                downloadState = state
            }
        }
    }

    Dialog(onDismissRequest = {
        if (downloadState !is DownloadState.Downloading && downloadState !is DownloadState.Completed) {
            onDismiss()
        }
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "发现新版本",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "v${updateInfo.version}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "  ($currentVersion → ${updateInfo.version})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (fileSize != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "安装包大小：$fileSize",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "更新内容",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = updateInfo.content,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (downloadState) {
                    is DownloadState.Idle, is DownloadState.Downloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "正在下载更新...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (downloadState is DownloadState.Downloading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${(downloadState as DownloadState.Downloading).progress}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    is DownloadState.Completed -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("取消")
                            }

                            Button(
                                onClick = {
                                    installApk(context, (downloadState as DownloadState.Completed).filePath)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("更新")
                            }
                        }
                    }

                    is DownloadState.Failed -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "下载失败",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("取消")
                                }
                                Button(
                                    onClick = {
                                        downloadState = DownloadState.Idle
                                        startDownload(context, updateInfo.downloadUrl, updateInfo.version) { state ->
                                            downloadState = state
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("重试")
                                }
                            }
                        }
                    }
                }

                if (updateInfo.isForce) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "此版本为强制更新，无法跳过",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data class Completed(val filePath: String) : DownloadState()
    data class Failed(val message: String) : DownloadState()
}

private fun startDownload(
    context: Context,
    downloadUrl: String,
    version: String,
    onStateChange: (DownloadState) -> Unit
) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("正在下载 v$version")
            setDescription("菜谱应用更新")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "chanle-recipe-v$version.apk"
            )
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val downloadId = downloadManager.enqueue(request)
        onStateChange(DownloadState.Downloading(0))

        Thread {
            var finished = false
            while (!finished) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor != null && cursor.moveToFirst()) {
                    val bytesDownloaded = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val bytesTotal = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )
                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    )

                    if (bytesTotal > 0) {
                        val progress = ((bytesDownloaded * 100) / bytesTotal).toInt()
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            onStateChange(DownloadState.Downloading(progress))
                        }
                    }

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                ?.absolutePath + "/chanle-recipe-v$version.apk"
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onStateChange(DownloadState.Completed(filePath))
                            }
                            finished = true
                        }
                        DownloadManager.STATUS_FAILED -> {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onStateChange(DownloadState.Failed("下载失败"))
                            }
                            finished = true
                        }
                    }
                }

                cursor?.close()
                Thread.sleep(500)
            }
        }.start()

    } catch (e: Exception) {
        onStateChange(DownloadState.Failed(e.message ?: "下载失败"))
    }
}

private fun installApk(context: Context, filePath: String) {
    try {
        val file = java.io.File(filePath)
        if (!file.exists()) {
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun checkFileSize(url: String, onResult: (String) -> Unit) {
    Thread {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connect()
            
            val fileLength = connection.contentLengthLong
            connection.disconnect()
            
            if (fileLength > 0) {
                val sizeStr = when {
                    fileLength >= 1024 * 1024 * 1024 -> String.format("%.2f GB", fileLength / (1024.0 * 1024.0 * 1024.0))
                    fileLength >= 1024 * 1024 -> String.format("%.2f MB", fileLength / (1024.0 * 1024.0))
                    fileLength >= 1024 -> String.format("%.2f KB", fileLength / 1024.0)
                    else -> "$fileLength B"
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onResult(sizeStr)
                }
            }
        } catch (e: Exception) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onResult("未知大小")
            }
        }
    }.start()
}