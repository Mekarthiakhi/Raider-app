package com.fleettrack.app.data.repository

import com.fleettrack.app.data.local.dao.VehicleDao
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.remote.SupabaseClientProvider
import com.fleettrack.app.di.IoDispatcher
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SupabaseVehicleDto(
    @SerialName("id") val id: String? = null,
    @SerialName("reg_no") val regNo: String,
    @SerialName("nickname") val nickname: String? = null,
    @SerialName("status") val status: String = "active",
    @SerialName("fleet_id") val fleetId: String? = null,
    @SerialName("manager_id") val managerId: String? = null
)

interface VehicleRepository {
    fun getAllVehicles(): Flow<List<VehicleEntity>>
    fun getVehiclesByFleet(fleetId: String): Flow<List<VehicleEntity>>
    fun getVehiclesByManager(managerId: String): Flow<List<VehicleEntity>>
    fun getUnassignedVehicles(): Flow<List<VehicleEntity>>
    fun getActiveCount(): Flow<Int>
    fun getActiveVehicleCountByManager(managerId: String): Flow<Int>
    suspend fun getVehiclesListByManager(managerId: String): List<VehicleEntity>
    suspend fun assignVehicleToManager(vehicleId: Long, managerId: String?)
    suspend fun getVehicleById(id: Long): VehicleEntity?
    suspend fun getVehicleByRegNo(regNo: String): VehicleEntity?
    suspend fun insertVehicle(vehicle: VehicleEntity): Long
    suspend fun insertAll(vehicles: List<VehicleEntity>)
    suspend fun updateVehicle(vehicle: VehicleEntity)
    suspend fun deleteVehicle(vehicle: VehicleEntity)
    suspend fun syncVehiclesFromSupabase(): List<VehicleEntity>
}

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : VehicleRepository {

    override fun getAllVehicles(): Flow<List<VehicleEntity>> =
        vehicleDao.getAll()

    override fun getVehiclesByFleet(fleetId: String): Flow<List<VehicleEntity>> =
        vehicleDao.getByFleetId(fleetId)

    override fun getVehiclesByManager(managerId: String): Flow<List<VehicleEntity>> =
        vehicleDao.getVehiclesByManager(managerId)

    override fun getUnassignedVehicles(): Flow<List<VehicleEntity>> =
        vehicleDao.getUnassignedVehicles()

    override fun getActiveCount(): Flow<Int> =
        vehicleDao.getActiveVehicleCount()

    override fun getActiveVehicleCountByManager(managerId: String): Flow<Int> =
        vehicleDao.getActiveVehicleCountByManager(managerId)

    override suspend fun getVehiclesListByManager(managerId: String): List<VehicleEntity> =
        withContext(ioDispatcher) {
            vehicleDao.getVehiclesListByManager(managerId)
        }

    override suspend fun assignVehicleToManager(vehicleId: Long, managerId: String?) =
        withContext(ioDispatcher) {
            vehicleDao.assignVehicleToManager(vehicleId, managerId)
        }

    override suspend fun getVehicleById(id: Long): VehicleEntity? =
        withContext(ioDispatcher) {
            vehicleDao.getById(id)
        }

    override suspend fun getVehicleByRegNo(regNo: String): VehicleEntity? =
        withContext(ioDispatcher) {
            vehicleDao.getByRegNo(regNo)
        }

    override suspend fun insertVehicle(vehicle: VehicleEntity): Long =
        withContext(ioDispatcher) {
            val id = vehicleDao.insert(vehicle)
            try {
                val dto = SupabaseVehicleDto(
                    regNo = vehicle.regNo,
                    nickname = vehicle.nickname,
                    status = vehicle.status,
                    fleetId = "a0000000-0000-0000-0000-000000000001",
                    managerId = vehicle.managerId
                )
                SupabaseClientProvider.client.from("vehicles").upsert(dto)
                Timber.d("Pushed vehicle to Supabase: ${vehicle.regNo}")
            } catch (e: Exception) {
                Timber.w(e, "Supabase vehicle sync fallback for ${vehicle.regNo}")
            }
            id
        }

    override suspend fun insertAll(vehicles: List<VehicleEntity>) =
        withContext(ioDispatcher) {
            vehicleDao.insertAll(vehicles)
        }

    override suspend fun updateVehicle(vehicle: VehicleEntity) =
        withContext(ioDispatcher) {
            vehicleDao.update(vehicle)
        }

    override suspend fun deleteVehicle(vehicle: VehicleEntity) =
        withContext(ioDispatcher) {
            vehicleDao.delete(vehicle)
        }

    override suspend fun syncVehiclesFromSupabase(): List<VehicleEntity> =
        withContext(ioDispatcher) {
            try {
                val remoteList = SupabaseClientProvider.client.from("vehicles")
                    .select()
                    .decodeList<SupabaseVehicleDto>()

                if (remoteList.isNotEmpty()) {
                    val currentLocal = vehicleDao.getAll().first()
                    val toInsert = mutableListOf<VehicleEntity>()

                    for (remote in remoteList) {
                        val existing = currentLocal.firstOrNull { it.regNo.equals(remote.regNo, ignoreCase = true) }
                        if (existing != null) {
                            if (existing.supabaseId != remote.id) {
                                vehicleDao.update(existing.copy(supabaseId = remote.id))
                            }
                        } else {
                            toInsert.add(
                                VehicleEntity(
                                    fleetId = remote.fleetId ?: "a0000000-0000-0000-0000-000000000001",
                                    regNo = remote.regNo,
                                    nickname = remote.nickname ?: "Cab",
                                    status = remote.status,
                                    supabaseId = remote.id,
                                    managerId = remote.managerId
                                )
                            )
                        }
                    }

                    if (toInsert.isNotEmpty()) {
                        vehicleDao.insertAll(toInsert)
                        Timber.d("Synced ${toInsert.size} new vehicles from Supabase")
                    }
                }
                vehicleDao.getAll().first()
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync vehicles from Supabase")
                vehicleDao.getAll().first()
            }
        }
}
