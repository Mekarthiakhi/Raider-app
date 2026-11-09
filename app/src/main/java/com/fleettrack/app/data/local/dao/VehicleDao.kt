package com.fleettrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fleettrack.app.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: VehicleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehicles: List<VehicleEntity>)

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Delete
    suspend fun delete(vehicle: VehicleEntity)

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getById(id: Long): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE reg_no = :regNo LIMIT 1")
    suspend fun getByRegNo(regNo: String): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE fleet_id = :fleetId ORDER BY reg_no ASC")
    fun getByFleetId(fleetId: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles ORDER BY reg_no ASC")
    fun getAll(): Flow<List<VehicleEntity>>

    @Query("SELECT COUNT(*) FROM vehicles WHERE status = 'active'")
    fun getActiveVehicleCount(): Flow<Int>

    @Query("SELECT * FROM vehicles WHERE manager_id = :managerId ORDER BY reg_no ASC")
    fun getVehiclesByManager(managerId: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE manager_id = :managerId ORDER BY reg_no ASC")
    suspend fun getVehiclesListByManager(managerId: String): List<VehicleEntity>

    @Query("SELECT * FROM vehicles WHERE manager_id IS NULL OR manager_id = '' ORDER BY reg_no ASC")
    fun getUnassignedVehicles(): Flow<List<VehicleEntity>>

    @Query("UPDATE vehicles SET manager_id = :managerId WHERE id = :vehicleId")
    suspend fun assignVehicleToManager(vehicleId: Long, managerId: String?)

    @Query("SELECT COUNT(*) FROM vehicles WHERE manager_id = :managerId AND status = 'active'")
    fun getActiveVehicleCountByManager(managerId: String): Flow<Int>
}
