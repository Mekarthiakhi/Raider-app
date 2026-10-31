package com.fleettrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fleettrack.app.data.local.entity.DriverEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface DriverDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(driver: DriverEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drivers: List<DriverEntity>)

    @Update
    suspend fun update(driver: DriverEntity)

    @Delete
    suspend fun delete(driver: DriverEntity)

    @Query("SELECT * FROM drivers WHERE id = :id")
    suspend fun getById(id: Long): DriverEntity?

    @Query("SELECT * FROM drivers WHERE fleet_id = :fleetId ORDER BY name ASC")
    fun getByFleetId(fleetId: String): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers ORDER BY name ASC")
    fun getAll(): Flow<List<DriverEntity>>

    @Query("UPDATE drivers SET consent_at = :consentAt WHERE id = :id")
    suspend fun updateConsent(id: Long, consentAt: Instant?)
}

