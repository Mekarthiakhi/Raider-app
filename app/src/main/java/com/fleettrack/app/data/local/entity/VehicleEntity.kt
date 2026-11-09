package com.fleettrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "vehicles",
    indices = [
        Index(value = ["reg_no"], unique = true),
        Index(value = ["manager_id"])
    ]
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "fleet_id")
    val fleetId: String,

    @ColumnInfo(name = "reg_no")
    val regNo: String,

    @ColumnInfo(name = "nickname")
    val nickname: String? = null,

    @ColumnInfo(name = "status")
    val status: String = "active",

    @ColumnInfo(name = "manager_id")
    val managerId: String? = null,

    @ColumnInfo(name = "supabase_id")
    val supabaseId: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now()
)
