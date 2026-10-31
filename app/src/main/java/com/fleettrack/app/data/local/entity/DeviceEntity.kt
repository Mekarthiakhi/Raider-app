package com.fleettrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicle_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = DriverEntity::class,
            parentColumns = ["id"],
            childColumns = ["driver_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["vehicle_id"]),
        Index(value = ["driver_id"])
    ]
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long? = null,

    @ColumnInfo(name = "driver_id")
    val driverId: Long? = null,

    @ColumnInfo(name = "device_token")
    val deviceToken: String,

    @ColumnInfo(name = "model")
    val model: String,

    @ColumnInfo(name = "android_version")
    val androidVersion: String,

    @ColumnInfo(name = "app_version")
    val appVersion: String,

    @ColumnInfo(name = "last_seen")
    val lastSeen: Instant? = null,

    @ColumnInfo(name = "battery_level")
    val batteryLevel: Int? = null,

    @ColumnInfo(name = "a11y_enabled")
    val a11yEnabled: Boolean = false
)
