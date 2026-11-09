package com.fleettrack.app.data.repository

import com.fleettrack.app.data.local.dao.DriverDao
import com.fleettrack.app.data.local.dao.DutyLogDao
import com.fleettrack.app.data.local.dao.ManagerDao
import com.fleettrack.app.data.local.dao.RideDao
import com.fleettrack.app.data.local.dao.VehicleDao
import com.fleettrack.app.data.local.entity.ManagerEntity
import com.fleettrack.app.data.model.ManagerOverview
import com.fleettrack.app.data.remote.ManagerRemoteDataSource
import com.fleettrack.app.data.remote.SupabaseManagerDto
import com.fleettrack.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface ManagerRepository {
    fun getAllManagers(): Flow<List<ManagerEntity>>
    fun getActiveManagers(): Flow<List<ManagerEntity>>
    fun getManagerById(id: String): Flow<ManagerEntity?>
    suspend fun findManagerById(id: String): ManagerEntity?
    suspend fun findManagerByPhone(phone: String): ManagerEntity?
    suspend fun insertManager(manager: ManagerEntity)
    suspend fun insertManagers(managers: List<ManagerEntity>)
    suspend fun updateManager(manager: ManagerEntity)
    suspend fun deleteManager(managerId: String)
    suspend fun assignDriverToManager(driverId: Long, managerId: String?)
    suspend fun assignVehicleToManager(vehicleId: Long, managerId: String?)
    fun getManagerOverview(managerId: String): Flow<ManagerOverview?>
    suspend fun syncManagersFromSupabase()
}

@Singleton
class ManagerRepositoryImpl @Inject constructor(
    private val managerDao: ManagerDao,
    private val driverDao: DriverDao,
    private val vehicleDao: VehicleDao,
    private val rideDao: RideDao,
    private val dutyLogDao: DutyLogDao,
    private val managerRemoteDataSource: ManagerRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ManagerRepository {

    override fun getAllManagers(): Flow<List<ManagerEntity>> =
        managerDao.getAllManagers()

    override fun getActiveManagers(): Flow<List<ManagerEntity>> =
        managerDao.getActiveManagers()

    override fun getManagerById(id: String): Flow<ManagerEntity?> =
        managerDao.getManagerById(id)

    override suspend fun findManagerById(id: String): ManagerEntity? =
        withContext(ioDispatcher) {
            managerDao.findManagerById(id)
        }

    override suspend fun findManagerByPhone(phone: String): ManagerEntity? =
        withContext(ioDispatcher) {
            val cleanPhone = phone.filter { it.isDigit() }.takeLast(10)
            managerDao.findManagerByPhone(cleanPhone)
        }

    override suspend fun insertManager(manager: ManagerEntity) {
        withContext(ioDispatcher) {
            managerDao.insertManager(manager)
            managerRemoteDataSource.upsertManager(
                SupabaseManagerDto(
                    id = manager.id,
                    name = manager.name,
                    phone = manager.phone,
                    hubName = manager.hubName,
                    pinCode = manager.pinCode,
                    isActive = manager.isActive,
                    ownerId = manager.ownerId
                )
            )
        }
    }

    override suspend fun insertManagers(managers: List<ManagerEntity>) {
        withContext(ioDispatcher) {
            managerDao.insertManagers(managers)
            managers.forEach { manager ->
                managerRemoteDataSource.upsertManager(
                    SupabaseManagerDto(
                        id = manager.id,
                        name = manager.name,
                        phone = manager.phone,
                        hubName = manager.hubName,
                        pinCode = manager.pinCode,
                        isActive = manager.isActive,
                        ownerId = manager.ownerId
                    )
                )
            }
        }
    }

    override suspend fun updateManager(manager: ManagerEntity) {
        withContext(ioDispatcher) {
            managerDao.updateManager(manager)
            managerRemoteDataSource.upsertManager(
                SupabaseManagerDto(
                    id = manager.id,
                    name = manager.name,
                    phone = manager.phone,
                    hubName = manager.hubName,
                    pinCode = manager.pinCode,
                    isActive = manager.isActive,
                    ownerId = manager.ownerId
                )
            )
        }
    }

    override suspend fun deleteManager(managerId: String) {
        withContext(ioDispatcher) {
            managerDao.deleteManagerById(managerId)
            managerRemoteDataSource.deleteManager(managerId)
        }
    }

    override suspend fun assignDriverToManager(driverId: Long, managerId: String?) {
        withContext(ioDispatcher) {
            driverDao.assignDriverToManager(driverId, managerId)
        }
    }

    override suspend fun assignVehicleToManager(vehicleId: Long, managerId: String?) {
        withContext(ioDispatcher) {
            vehicleDao.assignVehicleToManager(vehicleId, managerId)
        }
    }

    override fun getManagerOverview(managerId: String): Flow<ManagerOverview?> {
        val managerFlow = managerDao.getManagerById(managerId)
        val driversFlow = driverDao.getDriversByManager(managerId)
        val vehiclesFlow = vehicleDao.getVehiclesByManager(managerId)
        val dutyLogsFlow = dutyLogDao.getDutyLogsByManager(managerId)

        return combine(managerFlow, driversFlow, vehiclesFlow, dutyLogsFlow) { manager, drivers, vehicles, logs ->
            if (manager == null) return@combine null

            val activeVehiclesCount = vehicles.count { it.status.equals("active", ignoreCase = true) }
            val totalTrips = logs.sumOf { it.trips }
            val totalEarnings = logs.sumOf { it.earnings }
            val totalCommission = logs.sumOf { it.commissionRent }

            ManagerOverview(
                managerId = manager.id,
                managerName = manager.name,
                phone = manager.phone,
                hubName = manager.hubName,
                totalDrivers = drivers.size,
                activeDrivers = drivers.count { it.consentAt != null },
                totalVehicles = vehicles.size,
                activeVehicles = activeVehiclesCount,
                todayEarnings = totalEarnings,
                todayTrips = totalTrips,
                monthlyEarnings = totalEarnings,
                commissionGenerated = totalCommission,
                isActive = manager.isActive
            )
        }
    }

    override suspend fun syncManagersFromSupabase() {
        withContext(ioDispatcher) {
            managerRemoteDataSource.fetchManagers().onSuccess { dtos ->
                val entities = dtos.map { dto ->
                    ManagerEntity(
                        id = dto.id ?: "mgr_${dto.phone.takeLast(4)}",
                        name = dto.name,
                        phone = dto.phone,
                        hubName = dto.hubName,
                        pinCode = dto.pinCode,
                        isActive = dto.isActive,
                        ownerId = dto.ownerId ?: "owner_akhilesh"
                    )
                }
                if (entities.isNotEmpty()) {
                    managerDao.insertManagers(entities)
                    Timber.d("Synced ${entities.size} managers from Supabase")
                }
            }.onFailure {
                Timber.w(it, "Failed to sync managers from Supabase, using local DB")
            }
        }
    }
}

