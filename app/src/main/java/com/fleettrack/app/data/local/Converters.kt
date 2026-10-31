package com.fleettrack.app.data.local

import androidx.room.TypeConverter
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.SyncStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun fromLocalDateString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromPlatform(platform: Platform?): String? {
        return platform?.name
    }

    @TypeConverter
    fun toPlatform(value: String?): Platform? {
        return value?.let { enumValueOf<Platform>(it) }
    }

    @TypeConverter
    fun fromRideStatus(status: RideStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toRideStatus(value: String?): RideStatus? {
        return value?.let { enumValueOf<RideStatus>(it) }
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? {
        return value?.let { enumValueOf<SyncStatus>(it) }
    }
}

