package com.fleettrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fleettrack.app.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<AlertEntity>)

    @Query("SELECT * FROM alerts WHERE fleet_id = :fleetId ORDER BY created_at DESC")
    fun getByFleetId(fleetId: String): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE is_read = 0 ORDER BY created_at DESC")
    fun getUnreadAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts WHERE is_read = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("UPDATE alerts SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE alerts SET is_read = 1")
    suspend fun markAllAsRead()
}

