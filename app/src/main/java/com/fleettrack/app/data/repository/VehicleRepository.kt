package com.fleettrack.app.data.repository

import com.fleettrack.app.data.local.dao.VehicleDao
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface VehicleRepository {
    fun getAllVehicles(): Flow<List<VehicleEntity>>
    fun getVehiclesByFleet(fleetId: String): Flow<List<VehicleEntity>>
    fun getActiveCount(): Flow<Int>
    suspend fun getVehicleById(id: Long): VehicleEntity?
    suspend fun getVehicleByRegNo(regNo: String): VehicleEntity?
    suspend fun insertVehicle(vehicle: VehicleEntity): Long
    suspend fun insertAll(vehicles: List<VehicleEntity>)
    suspend fun updateVehicle(vehicle: VehicleEntity)
    suspend fun deleteVehicle(vehicle: VehicleEntity)
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

    override fun getActiveCount(): Flow<Int> =
        vehicleDao.getActiveVehicleCount()

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
            vehicleDao.insert(vehicle)
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
}
