package xyz.ankitgrai.kaizen.data.update

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isUpdateAvailable: Boolean,
)

class UpdateChecker(private val httpClient: HttpClient) {

    companion object {
        private const val GITHUB_API_URL =
            "https://api.github.com/repos/grayankit/task-planner/releases/latest"
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Check GitHub for the latest release.
     * Returns UpdateInfo if successful, null on any error (network, parsing, etc.)
     */
    suspend fun checkForUpdate(currentVersion: String): UpdateInfo? {
        return try {
            val response: HttpResponse = httpClient.get(GITHUB_API_URL) {
                header("Accept", "application/vnd.github.v3+json")
            }
            val body = response.bodyAsText()
            val jsonObj = json.parseToJsonElement(body).jsonObject

            val tagName = jsonObj["tag_name"]?.jsonPrimitive?.content ?: return null
            // Strip leading 'v' if present: "v1.2.0" -> "1.2.0"
            val latestVersion = tagName.removePrefix("v")

            val releaseNotes = jsonObj["body"]?.jsonPrimitive?.content ?: ""

            // Find APK asset
            val assets = jsonObj["assets"]?.jsonArray ?: return null
            val apkAsset = assets.firstOrNull { asset ->
                val name = asset.jsonObject["name"]?.jsonPrimitive?.content ?: ""
                name.endsWith(".apk")
            }
            val downloadUrl = apkAsset?.jsonObject
                ?.get("browser_download_url")?.jsonPrimitive?.content ?: ""

            val isNewer = isVersionNewer(latestVersion, currentVersion)

            UpdateInfo(
                latestVersion = latestVersion,
                downloadUrl = downloadUrl,
                releaseNotes = releaseNotes,
                isUpdateAvailable = isNewer,
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Compare semver strings. Returns true if [latest] > [current].
     */
    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
