package com.fleettrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.SyncStatus
import kotlinx.datetime.Instant

@Entity(
    tableName = "rides",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicle_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DriverEntity::class,
            parentColumns = ["id"],
            childColumns = ["driver_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ride_key"], unique = true),
        Index(value = ["vehicle_id", "accepted_at"]),
        Index(value = ["driver_id"]),
        Index(value = ["platform"]),
        Index(value = ["sync_status"]),
        Index(value = ["needs_review"]),
        Index(value = ["manager_id"])
    ]
)
data class RideEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,

    @ColumnInfo(name = "driver_id")
    val driverId: Long,

    @ColumnInfo(name = "manager_id")
    val managerId: String? = null,

    @ColumnInfo(name = "device_id")
    val deviceId: Long? = null,

    @ColumnInfo(name = "platform")
    val platform: Platform,

    @ColumnInfo(name = "ride_key")
    val rideKey: String,

    @ColumnInfo(name = "status")
    val status: RideStatus,

    @ColumnInfo(name = "accepted_at")
    val acceptedAt: Instant,

    @ColumnInfo(name = "completed_at")
    val completedAt: Instant? = null,

    @ColumnInfo(name = "fare")
    val fare: Double? = null,

    @ColumnInfo(name = "distance_km")
    val distanceKm: Double? = null,

    @ColumnInfo(name = "duration_min")
    val durationMin: Double? = null,

    @ColumnInfo(name = "pickup")
    val pickup: String? = null,

    @ColumnInfo(name = "dropoff")
    val dropoff: String? = null,

    @ColumnInfo(name = "confidence")
    val confidence: Double = 1.0,

    @ColumnInfo(name = "needs_review")
    val needsReview: Boolean = false,

    @ColumnInfo(name = "review_note")
    val reviewNote: String? = null,

    @ColumnInfo(name = "raw_extracted")
    val rawExtracted: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @ColumnInfo(name = "supabase_id")
    val supabaseId: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant
)
