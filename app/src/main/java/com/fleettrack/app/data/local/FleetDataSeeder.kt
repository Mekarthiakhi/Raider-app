package com.fleettrack.app.data.local

import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.SyncStatus
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Singleton
class FleetDataSeeder @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val driverRepository: DriverRepository,
    private val rideRepository: RideRepository
) {
    private val driverNames = listOf(
        "Ramesh Kumar", "Suresh Gowda", "Mohammad Imran", "Anil Reddy",
        "Vijay Patil", "Karthik Nayak", "Manjunath S.", "Praveen Rao",
        "Deepak Sharma", "Sunil Verma", "Rajesh Babu", "Ganesh Murthy",
        "Santosh Hegde", "Arun Prasad", "Raghuveer Singh", "Dinesh K.",
        "Mahesh Shetty", "Shiva Kumar", "Venkat Raman", "Naveen Raj",
        "Pradeep Joshi", "Santosh Nayak", "Harish Gowda", "Chetan Kumar",
        "Ravindra Nath", "Yogesh Gowda", "Manoj Tiwari", "Girish Kumar",
        "Sandip Das", "Kiran Hegde"
    )

    private val bangaloreLocations = listOf(
        "Koramangala 5th Block", "Indiranagar 100ft Rd", "HSR Layout Sector 2",
        "Whitefield ITPL Main Rd", "Electronic City Phase 1", "MG Road Metro",
        "Kempegowda Int'l Airport", "Jayanagar 4th Block", "Bellandur EcoSpace",
        "Marathahalli Bridge", "BTM Layout 2nd Stage", "Hebbal Flyover",
        "JP Nagar 6th Phase", "Yeshwantpur Railway Station", "Malleshwaram 8th Cross",
        "Bannerghatta Rd Dairy Circle", "Sarjapur Wipro Gate", "Domlur Flyover",
        "Kalyan Nagar HRBR Layout", "Majestic Bus Station"
    )

    suspend fun seedIfEmpty() {
        val existingVehicles = vehicleRepository.getAllVehicles().first()
        if (existingVehicles.isNotEmpty()) return

        val now = Clock.System.now()
        val fleetId = "fleet_blr_central"

        // 1. Create 30 Drivers
        val driversToInsert = driverNames.mapIndexed { index, name ->
            DriverEntity(
                id = (index + 1).toLong(),
                fleetId = fleetId,
                name = name,
                phone = "+91 9845${String.format("%06d", 100000 + index * 317)}",
                consentAt = now - 10.hours,
                supabaseId = "drv_${index + 1}"
            )
        }
        driverRepository.insertAll(driversToInsert)

        // 2. Create 30 Vehicles (Cab #01 to Cab #30)
        val vehiclesToInsert = (1..30).map { index ->
            val regSuffix = 1000 + index
            VehicleEntity(
                id = index.toLong(),
                fleetId = fleetId,
                regNo = "KA-01-MJ-$regSuffix",
                nickname = "Cab #${String.format("%02d", index)}",
                status = if (index <= 26) "active" else "idle",
                supabaseId = "veh_$index",
                createdAt = now - 30.hours
            )
        }
        vehicleRepository.insertAll(vehiclesToInsert)

        // 3. Generate Realistic Today's Rides (Uber + Rapido) for active cabs
        val ridesToInsert = mutableListOf<RideEntity>()
        var rideIdCounter = 1L

        for (cabIndex in 1..26) {
            val vehicleId = cabIndex.toLong()
            val driverId = cabIndex.toLong() // 1:1 assigned driver today

            // Each cab did between 4 to 9 trips today split across Uber and Rapido
            val tripCount = Random.nextInt(4, 9)

            for (t in 0 until tripCount) {
                // Alternating / random between Uber and Rapido
                val platform = if (t % 2 == 0) Platform.UBER else Platform.RAPIDO

                val tripMinutesAgo = Random.nextInt(30, 480)
                val acceptedAt = now - tripMinutesAgo.minutes
                val tripDuration = Random.nextInt(14, 55)
                val completedAt = acceptedAt + tripDuration.minutes

                // Realistic fares & distances for city cabs in India
                val distanceKm = (Random.nextDouble(3.5, 24.0) * 10).toInt() / 10.0
                val baseRate = if (platform == Platform.UBER) 18.0 else 16.5
                val fare = ((distanceKm * baseRate + Random.nextInt(30, 75)) / 10).toInt() * 10.0

                val pickup = bangaloreLocations.random()
                var dropoff = bangaloreLocations.random()
                while (dropoff == pickup) {
                    dropoff = bangaloreLocations.random()
                }

                val rideKey = "${platform.name.lowercase()}_${cabIndex}_${acceptedAt.toEpochMilliseconds()}"

                ridesToInsert.add(
                    RideEntity(
                        id = rideIdCounter++,
                        vehicleId = vehicleId,
                        driverId = driverId,
                        platform = platform,
                        rideKey = rideKey,
                        status = RideStatus.COMPLETED,
                        acceptedAt = acceptedAt,
                        completedAt = completedAt,
                        fare = fare,
                        distanceKm = distanceKm,
                        durationMin = tripDuration.toDouble(),
                        pickup = pickup,
                        dropoff = dropoff,
                        confidence = if (t == tripCount - 1 && cabIndex % 7 == 0) 0.65 else 1.0,
                        needsReview = (t == tripCount - 1 && cabIndex % 7 == 0),
                        reviewNote = if (t == tripCount - 1 && cabIndex % 7 == 0) "Verify toll charge" else null,
                        syncStatus = SyncStatus.SYNCED,
                        createdAt = completedAt
                    )
                )
            }
        }

        rideRepository.insertAllRides(ridesToInsert)
    }
}

