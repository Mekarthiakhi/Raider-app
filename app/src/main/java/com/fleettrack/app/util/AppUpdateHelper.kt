package com.fleettrack.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.fleettrack.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class GitHubReleaseAsset(
    val name: String,
    val size: Long,
    val browser_download_url: String,
    val updated_at: String
)

@Serializable
data class GitHubReleaseResponse(
    val tag_name: String,
    val name: String? = null,
    val body: String? = null,
    val published_at: String? = null,
    val assets: List<GitHubReleaseAsset> = emptyList()
)

data class AppUpdateInfo(
    val versionName: String,
    val downloadUrl: String,
    val sizeMb: Double,
    val releaseNotes: String,
    val publishedAt: String = ""
)

object AppUpdateHelper {

    private const val LATEST_RELEASE_API =
        "https://api.github.com/repos/Mekarthiakhi/Raider-app/releases/tags/latest"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun dismissUpdate(context: Context, publishedAt: String) {
        if (publishedAt.isNotBlank()) {
            context.getSharedPreferences("svm_fleet_updates", Context.MODE_PRIVATE)
                .edit()
                .putString("dismissed_time", publishedAt)
                .apply()
        }
    }

    fun markUpdateInstalled(context: Context, publishedAt: String) {
        if (publishedAt.isNotBlank()) {
            context.getSharedPreferences("svm_fleet_updates", Context.MODE_PRIVATE)
                .edit()
                .putString("last_installed_time", publishedAt)
                .apply()
        }
    }

    private fun parseIsoToEpochMs(isoString: String?): Long {
        if (isoString.isNullOrBlank()) return 0L
        return try {
            java.time.Instant.parse(isoString).toEpochMilli()
        } catch (e: Exception) {
            try {
                kotlinx.datetime.Instant.parse(isoString).toEpochMilliseconds()
            } catch (e2: Exception) {
                0L
            }
        }
    }

    suspend fun checkForUpdate(context: Context): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(LATEST_RELEASE_API)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("User-Agent", "SVM-Fleet-App")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val release = json.decodeFromString<GitHubReleaseResponse>(responseText)

                val apkAsset = release.assets.firstOrNull { it.name == "app-release.apk" }
                    ?: release.assets.firstOrNull { it.name.endsWith(".apk") } ?: return@withContext null

                val assetVersionId = apkAsset.updated_at.ifBlank { release.published_at ?: release.tag_name }
                val prefs = context.getSharedPreferences("svm_fleet_updates", Context.MODE_PRIVATE)
                val lastInstalled = prefs.getString("last_installed_time", "")
                val dismissed = prefs.getString("dismissed_time", "")

                // If user dismissed this version or installed it
                if (assetVersionId.isNotBlank() && (assetVersionId == lastInstalled || assetVersionId == dismissed)) {
                    return@withContext null
                }

                val assetEpoch = parseIsoToEpochMs(apkAsset.updated_at).let {
                    if (it > 0L) it else parseIsoToEpochMs(release.published_at)
                }

                // 1. If GitHub asset was published within 45 mins of build time, it is part of this exact CI build!
                // A new update only exists if GitHub release was uploaded MORE than 45 mins after this app was built.
                if (BuildConfig.BUILD_TIME > 0L && assetEpoch > 0L) {
                    if (assetEpoch <= (BuildConfig.BUILD_TIME + 2_700_000L)) {
                        return@withContext null
                    }
                }

                // 2. If app was installed/updated within 30 mins of release, it is already up to date!
                val packageInfo = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }
                } catch (e: Exception) {
                    null
                }

                val installedAppTime = packageInfo?.lastUpdateTime ?: 0L
                if (assetEpoch > 0L && installedAppTime >= (assetEpoch - 1_800_000L)) {
                    return@withContext null
                }

                val sizeMb = (apkAsset.size / (1024.0 * 1024.0) * 10).toInt() / 10.0
                return@withContext AppUpdateInfo(
                    versionName = release.tag_name,
                    downloadUrl = apkAsset.browser_download_url,
                    sizeMb = sizeMb,
                    releaseNotes = release.body ?: "Latest performance updates and SVM FLEET fixes.",
                    publishedAt = assetVersionId
                )
            }
        } catch (e: Exception) {
            // Offline or network error
        }
        null
    }

    suspend fun downloadAndInstall(
        context: Context,
        downloadUrl: String,
        publishedAt: String = "",
        onProgress: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress("Starting download...")

            val destinationDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val outputFile = File(destinationDir, "SVM-FLEET-update.apk")
            if (outputFile.exists()) {
                outputFile.delete()
            }

            var currentUrl = downloadUrl
            var connection: HttpURLConnection
            var redirects = 0

            // Follow HTTP 302 redirects from GitHub to Azure / CDN
            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "SVM-Fleet-App")
                }

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == 307 || status == 308
                ) {
                    currentUrl = connection.getHeaderField("Location")
                    redirects++
                    if (redirects > 5) throw RuntimeException("Too many redirects")
                    continue
                }
                break
            }

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val percent = (downloadedBytes * 100 / totalBytes).toInt()
                            withContext(Dispatchers.Main) {
                                onProgress("Downloading: $percent%")
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                markUpdateInstalled(context, publishedAt)
                onProgress("Installing update...")
                launchInstallIntent(context, outputFile)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onProgress("Update failed: ${e.message}")
                Toast.makeText(context, "Direct download failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun launchInstallIntent(context: Context, apkFile: File) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val permissionIntent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(permissionIntent)
                Toast.makeText(context, "Please allow 'Install unknown apps' to complete SVM FLEET update", Toast.LENGTH_LONG).show()
                return
            }
        }

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        }

        context.startActivity(installIntent)
    }
}

