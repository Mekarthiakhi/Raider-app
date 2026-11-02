package com.fleettrack.app.data.repository

import com.fleettrack.app.data.local.dao.DriverDao
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface DriverRepository {
    fun getAllDrivers(): Flow<List<DriverEntity>>
    fun getDriversByFleet(fleetId: String): Flow<List<DriverEntity>>
    suspend fun getDriverById(id: Long): DriverEntity?
    suspend fun insertDriver(driver: DriverEntity): Long
    suspend fun insertAll(drivers: List<DriverEntity>)
    suspend fun updateDriver(driver: DriverEntity)
    suspend fun updateConsent(id: Long, consentAt: Instant?)
    suspend fun deleteDriver(driver: DriverEntity)
}

@Singleton
class DriverRepositoryImpl @Inject constructor(
    private val driverDao: DriverDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DriverRepository {

    override fun getAllDrivers(): Flow<List<DriverEntity>> =
        driverDao.getAll()

    override fun getDriversByFleet(fleetId: String): Flow<List<DriverEntity>> =
        driverDao.getByFleetId(fleetId)

    override suspend fun getDriverById(id: Long): DriverEntity? =
        withContext(ioDispatcher) {
            driverDao.getById(id)
        }

    override suspend fun insertDriver(driver: DriverEntity): Long =
        withContext(ioDispatcher) {
            driverDao.insert(driver)
        }

    override suspend fun insertAll(drivers: List<DriverEntity>) =
        withContext(ioDispatcher) {
            driverDao.insertAll(drivers)
        }

    override suspend fun updateDriver(driver: DriverEntity) =
        withContext(ioDispatcher) {
            driverDao.update(driver)
        }

    override suspend fun updateConsent(id: Long, consentAt: Instant?) =
        withContext(ioDispatcher) {
            driverDao.updateConsent(id, consentAt)
        }

    override suspend fun deleteDriver(driver: DriverEntity) =
        withContext(ioDispatcher) {
            driverDao.delete(driver)
        }
}
