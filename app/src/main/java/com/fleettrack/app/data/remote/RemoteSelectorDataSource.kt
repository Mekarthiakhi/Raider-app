package com.fleettrack.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteSelectorDto(
    @SerialName("id") val id: String,
    @SerialName("platform") val platform: String,
    @SerialName("app_version") val appVersion: String? = null,
    @SerialName("screen_type") val screenType: String,
    @SerialName("selectors") val selectors: String, // raw JSON string
    @SerialName("priority") val priority: Int = 0,
    @SerialName("active") val active: Boolean = true
)

interface RemoteSelectorDataSource {
    suspend fun fetchSelectors(platform: String): Result<List<RemoteSelectorDto>>
}

class RemoteSelectorDataSourceImpl : RemoteSelectorDataSource {
    override suspend fun fetchSelectors(platform: String): Result<List<RemoteSelectorDto>> {
        return Result.success(emptyList())
    }
}

