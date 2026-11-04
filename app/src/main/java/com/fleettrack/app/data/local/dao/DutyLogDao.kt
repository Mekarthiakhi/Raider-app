package com.fleettrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fleettrack.app.data.local.entity.DutyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DutyLogDao {

    @Query("SELECT * FROM duty_logs ORDER BY id DESC")
    fun getAllDutyLogs(): Flow<List<DutyLogEntity>>

    @Query("SELECT * FROM duty_logs WHERE id = :id LIMIT 1")
    suspend fun getDutyLogById(id: Long): DutyLogEntity?

    @Query("SELECT * FROM duty_logs WHERE vehicle_no LIKE '%' || :query || '%' OR driver_name LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchDutyLogs(query: String): Flow<List<DutyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dutyLog: DutyLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dutyLogs: List<DutyLogEntity>)

    @Update
    suspend fun update(dutyLog: DutyLogEntity)

    @Query("DELETE FROM duty_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM duty_logs")
    suspend fun getCount(): Int

    @Query("SELECT COALESCE(SUM(earnings), 0.0) FROM duty_logs")
    fun getTotalEarnings(): Flow<Double>

    @Query("SELECT COALESCE(SUM(cash_collection), 0.0) FROM duty_logs")
    fun getTotalCashCollection(): Flow<Double>

    @Query("SELECT COALESCE(SUM(trips), 0) FROM duty_logs")
    fun getTotalTrips(): Flow<Int>
}
