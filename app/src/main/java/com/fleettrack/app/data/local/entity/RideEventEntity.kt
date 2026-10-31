package com.fleettrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fleettrack.app.data.model.SyncStatus
import kotlinx.datetime.Instant

@Entity(
    tableName = "ride_events",
    foreignKeys = [
        ForeignKey(
            entity = RideEntity::class,
            parentColumns = ["id"],
            childColumns = ["ride_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ride_id"]),
        Index(value = ["sync_status"])
    ]
)
data class RideEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "ride_id")
    val rideId: Long,

    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "occurred_at")
    val occurredAt: Instant,

    @ColumnInfo(name = "raw_json")
    val rawJson: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

