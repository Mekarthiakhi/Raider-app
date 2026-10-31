package com.fleettrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fleettrack.app.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity): Long

    @Update
    suspend fun update(device: DeviceEntity)

    @Delete
    suspend fun delete(device: DeviceEntity)

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: Long): DeviceEntity?

    @Query("SELECT * FROM devices WHERE device_token = :token LIMIT 1")
    suspend fun getByToken(token: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE vehicle_id = :vehicleId LIMIT 1")
    fun getByVehicleId(vehicleId: Long): Flow<DeviceEntity?>

    @Query("SELECT * FROM devices ORDER BY last_seen DESC")
    fun getAll(): Flow<List<DeviceEntity>>

    @Query("""
        UPDATE devices 
        SET last_seen = :now, 
            battery_level = :batteryLevel, 
            a11y_enabled = :a11yEnabled 
        WHERE device_token = :token
    """)
    suspend fun updateHeartbeat(
        token: String,
        now: Instant,
        batteryLevel: Int?,
        a11yEnabled: Boolean
    )
}

