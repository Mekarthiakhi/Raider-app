package com.fleettrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "alerts",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicle_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["fleet_id", "is_read"]),
        Index(value = ["vehicle_id"])
    ]
)
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "fleet_id")
    val fleetId: String,

    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long? = null,

    @ColumnInfo(name = "alert_type")
    val alertType: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant
)

