package com.fleettrack.app.data.remote

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class RemoteSelectorDto(
    @SerialName("id") val id: String? = null,
    @SerialName("platform") val platform: String,
    @SerialName("app_version") val appVersion: String? = null,
    @SerialName("screen_type") val screenType: String,
    @SerialName("selectors") val selectors: JsonObject? = null,
    @SerialName("priority") val priority: Int = 0,
    @SerialName("active") val active: Boolean = true
)

interface RemoteSelectorDataSource {
    suspend fun fetchSelectors(platform: String): Result<List<RemoteSelectorDto>>
}

@Singleton
class RemoteSelectorDataSourceImpl @Inject constructor() : RemoteSelectorDataSource {
    override suspend fun fetchSelectors(platform: String): Result<List<RemoteSelectorDto>> {
        return try {
            val list = SupabaseClientProvider.client.from("remote_selectors")
                .select {
                    filter {
                        eq("platform", platform.lowercase())
                        eq("active", true)
                    }
                }
                .decodeList<RemoteSelectorDto>()
            Result.success(list)
        } catch (e: Exception) {
            Timber.w(e, "Remote selectors fetch fallback for $platform")
            Result.success(emptyList())
        }
    }
}
