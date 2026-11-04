package com.fleettrack.app.data.remote

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class DeviceRegistrationDto(
    @SerialName("vehicle_id") val vehicleId: String? = null,
    @SerialName("driver_id") val driverId: String? = null,
    @SerialName("device_token") val deviceToken: String,
    @SerialName("device_model") val deviceModel: String,
    @SerialName("android_version") val androidVersion: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("battery_level") val batteryLevel: Int?,
    @SerialName("a11y_enabled") val a11yEnabled: Boolean
)

@Serializable
data class HeartbeatDto(
    @SerialName("device_token") val deviceToken: String,
    @SerialName("battery_level") val batteryLevel: Int?,
    @SerialName("a11y_enabled") val a11yEnabled: Boolean,
    @SerialName("is_online") val isOnline: Boolean = true,
    @SerialName("last_seen") val lastSeen: String
)

interface DeviceRemoteDataSource {
    suspend fun registerDevice(dto: DeviceRegistrationDto): Result<String>
    suspend fun sendHeartbeat(dto: HeartbeatDto): Result<Unit>
}

@Singleton
class DeviceRemoteDataSourceImpl @Inject constructor() : DeviceRemoteDataSource {
    override suspend fun registerDevice(dto: DeviceRegistrationDto): Result<String> {
        return try {
            SupabaseClientProvider.client.from("devices").upsert(dto)
            Result.success(dto.deviceToken)
        } catch (e: Exception) {
            Timber.w(e, "Device registration offline fallback")
            Result.success(dto.deviceToken)
        }
    }

    override suspend fun sendHeartbeat(dto: HeartbeatDto): Result<Unit> {
        return try {
            SupabaseClientProvider.client.from("devices").upsert(dto)
            Timber.d("Heartbeat sent to Supabase successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.w(e, "Heartbeat offline fallback")
            Result.success(Unit)
        }
    }
}
