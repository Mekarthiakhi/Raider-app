package com.fleettrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fleettrack.app.data.local.entity.RideEventEntity
import com.fleettrack.app.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RideEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: RideEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<RideEventEntity>)

    @Query("SELECT * FROM ride_events WHERE ride_id = :rideId ORDER BY occurred_at ASC")
    fun getByRideId(rideId: Long): Flow<List<RideEventEntity>>

    @Query("SELECT * FROM ride_events WHERE sync_status = 'PENDING' ORDER BY occurred_at ASC")
    suspend fun getPendingSync(): List<RideEventEntity>

    @Query("UPDATE ride_events SET sync_status = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, syncStatus: SyncStatus)
}

