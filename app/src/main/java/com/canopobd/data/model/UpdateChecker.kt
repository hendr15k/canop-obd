package com.canopobd.data.model

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

data class AppUpdate(
    val versionName: String,
    val versionCode: Int,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val publishedAt: String
)

object UpdateChecker {

    private const val TAG = "UpdateChecker"
    private const val GITHUB_REPO = "hendr15k/canop-obd"
    private const val PREFS_NAME = "canop_obd_update"
    private const val KEY_LAST_CHECK = "last_update_check"
    private const val KEY_SKIPPED_VERSION = "skipped_version"
    private const val CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L

    fun getCurrentVersionCode(context: Context): Int {
        return try {
            PackageInfoCompat.getLongVersionCode(
                context.packageManager.getPackageInfo(context.packageName, 0)
            ).toInt()
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    fun getCurrentVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "0.0.0"
        }
    }

    suspend fun checkForUpdate(context: Context): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$GITHUB_REPO/releases/latest")
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

            val response = connection.getInputStream().bufferedReader().readText()
            val json = org.json.JSONObject(response)

            val tagName = json.optString("tag_name", "")
            val versionName = tagName.removePrefix("v")
            val body = json.optString("body", "")
            val publishedAt = json.optString("published_at", "")

            val currentVersionName = getCurrentVersionName(context)
            val currentCode = getCurrentVersionCode(context)

            val assets = json.optJSONArray("assets") ?: JSONArray()
            var apkUrl = ""
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith(".apk")) {
                    apkUrl = asset.optString("browser_download_url", "")
                    break
                }
            }

            if (apkUrl.isEmpty()) return@withContext null

            val remoteCode = parseVersionCode(body, versionName)
            val isNewer = compareVersions(versionName, currentVersionName) > 0 || remoteCode > currentCode
            if (!isNewer) return@withContext null

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val skippedVersion = prefs.getString(KEY_SKIPPED_VERSION, "")
            if (skippedVersion == versionName) return@withContext null

            AppUpdate(
                versionName = versionName,
                versionCode = remoteCode,
                releaseNotes = body,
                apkDownloadUrl = apkUrl,
                publishedAt = publishedAt
            )
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed: ${e.message}")
            null
        }
    }

    private fun compareVersions(remote: String, local: String): Int {
        val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val localParts = local.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r != l) return r - l
        }
        return 0
    }

    fun shouldCheckForUpdate(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0)
        return System.currentTimeMillis() - lastCheck > CHECK_INTERVAL_MS
    }

    fun markChecked(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
    }

    fun skipVersion(context: Context, versionName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SKIPPED_VERSION, versionName).apply()
    }

    private fun parseVersionCode(body: String, versionName: String): Int {
        val codePattern = Regex("""versionCode[:\s]*(\d+)""", RegexOption.IGNORE_CASE)
        codePattern.find(body)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }

        val parts = versionName.split(".")
        if (parts.size >= 3) {
            val major = parts[0].toIntOrNull() ?: 0
            val minor = parts[1].toIntOrNull() ?: 0
            val patch = parts[2].toIntOrNull() ?: 0
            return major * 100 + minor * 10 + patch + 1
        }
        return 0
    }
}
