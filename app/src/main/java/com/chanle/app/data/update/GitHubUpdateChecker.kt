package com.chanle.app.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("name") val name: String,
    @SerializedName("body") val body: String?,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("assets") val assets: List<ReleaseAsset>
)

data class ReleaseAsset(
    @SerializedName("name") val name: String,
    @SerializedName("browser_download_url") val downloadUrl: String,
    @SerializedName("content_type") val contentType: String?
)

class GitHubUpdateChecker(private val context: Context) {
    
    private val httpClient = OkHttpClient()
    private val gson = Gson()
    
    companion object {
        private const val OWNER = "LkkEternal"
        private const val REPO = "bbb"
        private const val CURRENT_VERSION = "0.1.0"
    }
    
    suspend fun checkForUpdate(): GitHubRelease? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val release = gson.fromJson(body, GitHubRelease::class.java)
                    val latestVersion = release.tagName.removePrefix("v")
                    
                    if (isNewerVersion(latestVersion, CURRENT_VERSION)) {
                        return@withContext release
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val latestPart = latestParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }
            
            when {
                latestPart > currentPart -> return true
                latestPart < currentPart -> return false
            }
        }
        return false
    }
    
    fun downloadAndInstall(release: GitHubRelease) {
        val apkAsset = release.assets.find { 
            it.name.endsWith(".apk") && it.contentType == "application/vnd.android.package-archive"
        }
        
        if (apkAsset != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(apkAsset.downloadUrl), "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "下载失败：${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "未找到APK文件", Toast.LENGTH_SHORT).show()
        }
    }
}