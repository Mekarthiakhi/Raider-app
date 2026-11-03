package com.fleettrack.app.data.repository

import com.fleettrack.app.data.local.dao.RideDao
import com.fleettrack.app.data.local.dao.RideEventDao
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.RideEventEntity
import com.fleettrack.app.data.model.RideSummary
import com.fleettrack.app.data.model.SyncStatus
import com.fleettrack.app.data.remote.RideRemoteDataSource
import com.fleettrack.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface RideRepository {
    fun getTodayRides(startOfDay: Instant): Flow<List<RideEntity>>
    fun getTodayRidesForVehicle(vehicleId: Long, startOfDay: Instant): Flow<List<RideEntity>>
    fun getTodayRidesForDriver(driverId: Long, startOfDay: Instant): Flow<List<RideEntity>>
    fun getRecentRides(limit: Int = 50): Flow<List<RideEntity>>
    fun getNeedsReviewRides(): Flow<List<RideEntity>>
    fun getTodaySummary(startOfDay: Instant): Flow<RideSummary>
    fun getTodaySummaryForVehicle(vehicleId: Long, startOfDay: Instant): Flow<RideSummary>
    fun getTodaySummaryForDriver(driverId: Long, startOfDay: Instant): Flow<RideSummary>
    fun getLatestRideForVehicle(vehicleId: Long): Flow<RideEntity?>
    fun getRidesByVehicle(vehicleId: Long): Flow<List<RideEntity>>
    fun getRidesByDriver(driverId: Long): Flow<List<RideEntity>>
    suspend fun getRideById(id: Long): RideEntity?
    suspend fun getRideByRideKey(rideKey: String): RideEntity?
    suspend fun insertRide(ride: RideEntity): Long
    suspend fun insertAllRides(rides: List<RideEntity>)
    suspend fun insertOrIgnoreRide(ride: RideEntity): Long
    suspend fun updateRide(ride: RideEntity)
    suspend fun recordEvent(event: RideEventEntity): Long
    suspend fun resolveReview(id: Long, fare: Double?, distanceKm: Double?, note: String?)
    suspend fun syncPendingRides(): Result<Int>
}

@Singleton
class RideRepositoryImpl @Inject constructor(
    private val rideDao: RideDao,
    private val rideEventDao: RideEventDao,
    private val rideRemoteDataSource: RideRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RideRepository {

    override fun getTodayRides(startOfDay: Instant): Flow<List<RideEntity>> =
        rideDao.getTodayRides(startOfDay)

    override fun getTodayRidesForVehicle(vehicleId: Long, startOfDay: Instant): Flow<List<RideEntity>> =
        rideDao.getTodayRidesForVehicle(vehicleId, startOfDay)

    override fun getTodayRidesForDriver(driverId: Long, startOfDay: Instant): Flow<List<RideEntity>> =
        rideDao.getTodayRidesForDriver(driverId, startOfDay)

    override fun getRecentRides(limit: Int): Flow<List<RideEntity>> =
        rideDao.getRecentRides(limit)

    override fun getNeedsReviewRides(): Flow<List<RideEntity>> =
        rideDao.getNeedsReview()

    override fun getTodaySummary(startOfDay: Instant): Flow<RideSummary> =
        rideDao.getTodaySummary(startOfDay)

    override fun getTodaySummaryForVehicle(vehicleId: Long, startOfDay: Instant): Flow<RideSummary> =
        rideDao.getTodaySummaryForVehicle(vehicleId, startOfDay)

    override fun getTodaySummaryForDriver(driverId: Long, startOfDay: Instant): Flow<RideSummary> =
        rideDao.getTodaySummaryForDriver(driverId, startOfDay)

    override fun getLatestRideForVehicle(vehicleId: Long): Flow<RideEntity?> =
        rideDao.getLatestRideForVehicle(vehicleId)

    override fun getRidesByVehicle(vehicleId: Long): Flow<List<RideEntity>> =
        rideDao.getByVehicleId(vehicleId)

    override fun getRidesByDriver(driverId: Long): Flow<List<RideEntity>> =
        rideDao.getByDriverId(driverId)

    override suspend fun getRideById(id: Long): RideEntity? =
        withContext(ioDispatcher) {
            rideDao.getById(id)
        }

    override suspend fun getRideByRideKey(rideKey: String): RideEntity? =
        withContext(ioDispatcher) {
            rideDao.getByRideKey(rideKey)
        }

    override suspend fun insertRide(ride: RideEntity): Long =
        withContext(ioDispatcher) {
            val id = rideDao.insert(ride)
            try {
                rideRemoteDataSource.uploadRide(ride.copy(id = id))
            } catch (_: Exception) {}
            id
        }

    override suspend fun insertAllRides(rides: List<RideEntity>) =
        withContext(ioDispatcher) {
            rideDao.insertAll(rides)
        }

    override suspend fun insertOrIgnoreRide(ride: RideEntity): Long =
        withContext(ioDispatcher) {
            rideDao.insertOrIgnore(ride)
        }

    override suspend fun updateRide(ride: RideEntity) =
        withContext(ioDispatcher) {
            rideDao.update(ride)
            try {
                rideRemoteDataSource.uploadRide(ride)
            } catch (_: Exception) {}
        }

    override suspend fun recordEvent(event: RideEventEntity): Long =
        withContext(ioDispatcher) {
            val id = rideEventDao.insert(event)
            try {
                rideRemoteDataSource.uploadRideEvent(event.copy(id = id))
            } catch (_: Exception) {}
            id
        }

    override suspend fun resolveReview(
        id: Long,
        fare: Double?,
        distanceKm: Double?,
        note: String?
    ) = withContext(ioDispatcher) {
        rideDao.resolveReview(id, fare, distanceKm, note)
    }

    override suspend fun syncPendingRides(): Result<Int> =
        withContext(ioDispatcher) {
            try {
                val pending = rideDao.getPendingSync()
                for (ride in pending) {
                    val res = rideRemoteDataSource.uploadRide(ride)
                    val supabaseId = res.getOrNull() ?: "synced_${ride.id}"
                    rideDao.updateSyncStatus(
                        id = ride.id,
                        syncStatus = SyncStatus.SYNCED,
                        supabaseId = supabaseId
                    )
                }
                Result.success(pending.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
