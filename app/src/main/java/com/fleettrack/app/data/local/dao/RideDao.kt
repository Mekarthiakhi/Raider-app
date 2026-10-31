package com.fleettrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.RideSummary
import com.fleettrack.app.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface RideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ride: RideEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(ride: RideEntity): Long

    @Update
    suspend fun update(ride: RideEntity)

    @Delete
    suspend fun delete(ride: RideEntity)

    @Query("SELECT * FROM rides WHERE id = :id")
    suspend fun getById(id: Long): RideEntity?

    @Query("SELECT * FROM rides WHERE ride_key = :rideKey LIMIT 1")
    suspend fun getByRideKey(rideKey: String): RideEntity?

    @Query("SELECT * FROM rides WHERE vehicle_id = :vehicleId ORDER BY accepted_at DESC")
    fun getByVehicleId(vehicleId: Long): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE driver_id = :driverId ORDER BY accepted_at DESC")
    fun getByDriverId(driverId: Long): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE accepted_at >= :start AND accepted_at <= :end ORDER BY accepted_at DESC")
    fun getByDateRange(start: Instant, end: Instant): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE sync_status = 'PENDING' ORDER BY accepted_at ASC")
    suspend fun getPendingSync(): List<RideEntity>

    @Query("SELECT * FROM rides WHERE needs_review = 1 ORDER BY accepted_at DESC")
    fun getNeedsReview(): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE accepted_at >= :startOfDay ORDER BY accepted_at DESC")
    fun getTodayRides(startOfDay: Instant): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides ORDER BY accepted_at DESC LIMIT :limit")
    fun getRecentRides(limit: Int = 50): Flow<List<RideEntity>>

    @Query("""
        SELECT 
            COUNT(*) as rideCount,
            COALESCE(SUM(fare), 0.0) as totalFare,
            COALESCE(SUM(distance_km), 0.0) as totalKm,
            CASE 
                WHEN COALESCE(SUM(distance_km), 0.0) > 0 
                THEN COALESCE(SUM(fare), 0.0) / SUM(distance_km) 
                ELSE 0.0 
            END as avgFarePerKm,
            COALESCE(SUM(duration_min), 0.0) as totalDuration,
            COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelledCount
        FROM rides 
        WHERE accepted_at >= :startOfDay
    """)
    fun getTodaySummary(startOfDay: Instant): Flow<RideSummary>

    @Query("""
        SELECT 
            COUNT(*) as rideCount,
            COALESCE(SUM(fare), 0.0) as totalFare,
            COALESCE(SUM(distance_km), 0.0) as totalKm,
            CASE 
                WHEN COALESCE(SUM(distance_km), 0.0) > 0 
                THEN COALESCE(SUM(fare), 0.0) / SUM(distance_km) 
                ELSE 0.0 
            END as avgFarePerKm,
            COALESCE(SUM(duration_min), 0.0) as totalDuration,
            COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelledCount
        FROM rides 
        WHERE vehicle_id = :vehicleId AND accepted_at >= :startOfDay
    """)
    fun getTodaySummaryForVehicle(vehicleId: Long, startOfDay: Instant): Flow<RideSummary>

    @Query("SELECT * FROM rides WHERE vehicle_id = :vehicleId ORDER BY accepted_at DESC LIMIT 1")
    fun getLatestRideForVehicle(vehicleId: Long): Flow<RideEntity?>

    @Query("UPDATE rides SET sync_status = :syncStatus, supabase_id = :supabaseId WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, syncStatus: SyncStatus, supabaseId: String?)

    @Query("UPDATE rides SET needs_review = 0, fare = :fare, distance_km = :distanceKm, review_note = :note WHERE id = :id")
    suspend fun resolveReview(id: Long, fare: Double?, distanceKm: Double?, note: String?)
}

