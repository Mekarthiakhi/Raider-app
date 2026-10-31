package com.fleettrack.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("last_seen") val lastSeen: String
)

interface DeviceRemoteDataSource {
    suspend fun registerDevice(dto: DeviceRegistrationDto): Result<String>
    suspend fun sendHeartbeat(dto: HeartbeatDto): Result<Unit>
}

class DeviceRemoteDataSourceImpl : DeviceRemoteDataSource {
    override suspend fun registerDevice(dto: DeviceRegistrationDto): Result<String> {
        return Result.success("device_id_stub")
    }

    override suspend fun sendHeartbeat(dto: HeartbeatDto): Result<Unit> {
        return Result.success(Unit)
    }
}

