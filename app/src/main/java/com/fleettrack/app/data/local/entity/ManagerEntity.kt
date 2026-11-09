package com.fleettrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "managers",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["hub_name"])
    ]
)
data class ManagerEntity(
    @PrimaryKey
    val id: String, // e.g., "mgr_moosapet_01" or UUID

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String, // 10-digit number e.g. "9849012345"

    @ColumnInfo(name = "hub_name")
    val hubName: String, // e.g. "Moosapet Central Hub"

    @ColumnInfo(name = "pin_code")
    val pinCode: String = "1234", // 4-digit quick access PIN

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "owner_id")
    val ownerId: String = "owner_akhilesh",

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now()
)

