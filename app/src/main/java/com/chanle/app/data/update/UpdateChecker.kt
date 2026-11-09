package com.chanle.app.data.update

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateChecker(private val context: Context) {

    private val updateUrl = "https://raw.githubusercontent.com/LkkEternal/bbb/main/update.json"

    suspend fun checkForUpdate(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getCurrentVersion()
            val currentVersionCode = getCurrentVersionCode()

            val updateInfo = fetchUpdateInfo()
            if (updateInfo != null && updateInfo.versionCode > currentVersionCode) {
                UpdateResult.Available(
                    updateInfo = updateInfo,
                    currentVersion = currentVersion
                )
            } else {
                UpdateResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "检查更新失败")
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    private fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            1
        }
    }

    private suspend fun fetchUpdateInfo(): UpdateInfo? {
        return try {
            val client = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(updateUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    kotlinx.serialization.json.Json.decodeFromString<UpdateResponse>(body).latest
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

sealed class UpdateResult {
    data object UpToDate : UpdateResult()
    data class Available(
        val updateInfo: UpdateInfo,
        val currentVersion: String
    ) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}