package com.fleettrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "drivers",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["manager_id"])
    ]
)
data class DriverEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "fleet_id")
    val fleetId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "manager_id")
    val managerId: String? = null,

    @ColumnInfo(name = "consent_at")
    val consentAt: Instant? = null,

    @ColumnInfo(name = "supabase_id")
    val supabaseId: String? = null
)
