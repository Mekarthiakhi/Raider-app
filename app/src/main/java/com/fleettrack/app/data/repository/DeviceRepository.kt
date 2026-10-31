package com.fleettrack.app.data.repository

import com.fleettrack.app.data.local.dao.DeviceDao
import com.fleettrack.app.data.local.entity.DeviceEntity
import com.fleettrack.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface DeviceRepository {
    fun getAllDevices(): Flow<List<DeviceEntity>>
    fun getDeviceByVehicleId(vehicleId: Long): Flow<DeviceEntity?>
    suspend fun getDeviceByToken(token: String): DeviceEntity?
    suspend fun insertDevice(device: DeviceEntity): Long
    suspend fun updateDevice(device: DeviceEntity)
    suspend fun updateHeartbeat(
        token: String,
        now: Instant,
        batteryLevel: Int?,
        a11yEnabled: Boolean
    )
}

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val deviceDao: DeviceDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DeviceRepository {

    override fun getAllDevices(): Flow<List<DeviceEntity>> =
        deviceDao.getAll()

    override fun getDeviceByVehicleId(vehicleId: Long): Flow<DeviceEntity?> =
        deviceDao.getByVehicleId(vehicleId)

    override suspend fun getDeviceByToken(token: String): DeviceEntity? =
        withContext(ioDispatcher) {
            deviceDao.getByToken(token)
        }

    override suspend fun insertDevice(device: DeviceEntity): Long =
        withContext(ioDispatcher) {
            deviceDao.insert(device)
        }

    override suspend fun updateDevice(device: DeviceEntity) =
        withContext(ioDispatcher) {
            deviceDao.update(device)
        }

    override suspend fun updateHeartbeat(
        token: String,
        now: Instant,
        batteryLevel: Int?,
        a11yEnabled: Boolean
    ) = withContext(ioDispatcher) {
        deviceDao.updateHeartbeat(token, now, batteryLevel, a11yEnabled)
    }
}

