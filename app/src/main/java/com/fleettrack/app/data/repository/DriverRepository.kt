package com.fleettrack.app.data.repository

import com.fleettrack.app.data.local.dao.DriverDao
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.remote.SupabaseClientProvider
import com.fleettrack.app.di.IoDispatcher
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SupabaseDriverDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String,
    @SerialName("fleet_id") val fleetId: String? = null,
    @SerialName("manager_id") val managerId: String? = null
)

interface DriverRepository {
    fun getAllDrivers(): Flow<List<DriverEntity>>
    fun getDriversByFleet(fleetId: String): Flow<List<DriverEntity>>
    fun getDriversByManager(managerId: String): Flow<List<DriverEntity>>
    fun getUnassignedDrivers(): Flow<List<DriverEntity>>
    suspend fun getDriverCountByManager(managerId: String): Int
    suspend fun getDriversListByManager(managerId: String): List<DriverEntity>
    suspend fun assignDriverToManager(driverId: Long, managerId: String?)
    suspend fun getDriverById(id: Long): DriverEntity?
    suspend fun insertDriver(driver: DriverEntity): Long
    suspend fun insertAll(drivers: List<DriverEntity>)
    suspend fun updateDriver(driver: DriverEntity)
    suspend fun updateConsent(id: Long, consentAt: Instant?)
    suspend fun deleteDriver(driver: DriverEntity)
    suspend fun syncDriversFromSupabase(): List<DriverEntity>
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

    override fun getDriversByManager(managerId: String): Flow<List<DriverEntity>> =
        driverDao.getDriversByManager(managerId)

    override fun getUnassignedDrivers(): Flow<List<DriverEntity>> =
        driverDao.getUnassignedDrivers()

    override suspend fun getDriverCountByManager(managerId: String): Int =
        withContext(ioDispatcher) {
            driverDao.getDriverCountByManager(managerId)
        }

    override suspend fun getDriversListByManager(managerId: String): List<DriverEntity> =
        withContext(ioDispatcher) {
            driverDao.getDriversListByManager(managerId)
        }

    override suspend fun assignDriverToManager(driverId: Long, managerId: String?) =
        withContext(ioDispatcher) {
            driverDao.assignDriverToManager(driverId, managerId)
        }

    override suspend fun getDriverById(id: Long): DriverEntity? =
        withContext(ioDispatcher) {
            driverDao.getById(id)
        }

    override suspend fun insertDriver(driver: DriverEntity): Long =
        withContext(ioDispatcher) {
            val id = driverDao.insert(driver)
            // Push to Supabase cloud database
            try {
                val dto = SupabaseDriverDto(
                    name = driver.name,
                    phone = driver.phone,
                    fleetId = "a0000000-0000-0000-0000-000000000001",
                    managerId = driver.managerId
                )
                SupabaseClientProvider.client.from("drivers").insert(dto)
                Timber.d("Pushed new driver to Supabase: ${driver.name} (${driver.phone})")
            } catch (e: Exception) {
                Timber.w(e, "Supabase driver sync fallback for ${driver.name}")
            }
            id
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

    override suspend fun syncDriversFromSupabase(): List<DriverEntity> =
        withContext(ioDispatcher) {
            try {
                val remoteList = SupabaseClientProvider.client.from("drivers")
                    .select()
                    .decodeList<SupabaseDriverDto>()

                if (remoteList.isNotEmpty()) {
                    val currentLocal = driverDao.getAll().first()
                    val toInsert = mutableListOf<DriverEntity>()

                    for (remote in remoteList) {
                        val cleanRemotePhone = remote.phone.filter { it.isDigit() }.takeLast(10)
                        val existing = currentLocal.firstOrNull { local ->
                            val cleanLocalPhone = local.phone.filter { it.isDigit() }.takeLast(10)
                            cleanLocalPhone == cleanRemotePhone || (local.name.equals(remote.name, ignoreCase = true) && cleanRemotePhone.isNotEmpty())
                        }
                        if (existing != null) {
                            if (existing.supabaseId != remote.id) {
                                driverDao.update(existing.copy(supabaseId = remote.id))
                            }
                        } else {
                            toInsert.add(
                                DriverEntity(
                                    fleetId = remote.fleetId ?: "a0000000-0000-0000-0000-000000000001",
                                    name = remote.name,
                                    phone = remote.phone,
                                    supabaseId = remote.id,
                                    managerId = remote.managerId
                                )
                            )
                        }
                    }

                    if (toInsert.isNotEmpty()) {
                        driverDao.insertAll(toInsert)
                        Timber.d("Synced ${toInsert.size} new drivers from Supabase to local Room DB")
                    }
                }
                driverDao.getAll().first()
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync drivers from Supabase, using local DB")
                driverDao.getAll().first()
            }
        }
}
