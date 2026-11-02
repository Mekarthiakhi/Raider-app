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
    private val carModels = listOf(
        "Hyundai Aura", "Maruti Dzire", "Honda Amaze", "Tata Tigor",
        "Maruti Tour S", "Hyundai Grand i10", "Toyota Etios"
    )

    private val driverList = listOf(
        Pair("Ravi Kumar", "+91 98765 43210"),
        Pair("Suresh Reddy", "+91 98451 23456"),
        Pair("Imran Ali", "+91 97412 89012"),
        Pair("Mahesh B", "+91 96321 78901"),
        Pair("Anil Kumar", "+91 98452 34567"),
        Pair("Vijay Rao", "+91 99001 23489"),
        Pair("Karthik N", "+91 98450 99881"),
        Pair("Manjunath S", "+91 97312 44556"),
        Pair("Deepak Sharma", "+91 98860 11223"),
        Pair("Sunil Verma", "+91 99450 33445"),
        Pair("Rajesh Babu", "+91 98456 77889"),
        Pair("Ganesh Murthy", "+91 97410 55667"),
        Pair("Santosh Hegde", "+91 96112 33445"),
        Pair("Arun Prasad", "+91 98801 22334"),
        Pair("Raghuveer S", "+91 99002 88990"),
        Pair("Dinesh K", "+91 98453 66778"),
        Pair("Mahesh Shetty", "+91 97413 77889"),
        Pair("Shiva Kumar", "+91 96324 88990"),
        Pair("Venkat Raman", "+91 98862 44556"),
        Pair("Naveen Raj", "+91 99452 66778"),
        Pair("Pradeep Joshi", "+91 98457 11223"),
        Pair("Harish Gowda", "+91 97314 55667"),
        Pair("Chetan Kumar", "+91 96114 77889"),
        Pair("Ravindra Nath", "+91 98803 99001"),
        Pair("Yogesh Gowda", "+91 99004 11223"),
        Pair("Manoj Tiwari", "+91 98458 33445"),
        Pair("Girish Kumar", "+91 97415 55667"),
        Pair("Sandip Das", "+91 96326 77889"),
        Pair("Kiran Hegde", "+91 98864 99001"),
        Pair("Praveen Rao", "+91 99454 11223")
    )

    private val bangaloreLocations = listOf(
        "Koramangala 5th Block", "Indiranagar 100ft Rd", "HSR Layout Sector 2",
        "Whitefield ITPL Main Rd", "Electronic City Phase 1", "MG Road Metro",
        "Kempegowda Int'l Airport", "Jayanagar 4th Block", "Bellandur EcoSpace",
        "Marathahalli Bridge", "BTM Layout 2nd Stage", "Hebbal Flyover"
    )

    suspend fun seedIfEmpty() {
        val existingVehicles = vehicleRepository.getAllVehicles().first()
        if (existingVehicles.isNotEmpty()) return

        val now = Clock.System.now()
        val fleetId = "fleet_blr_central"

        // 1. Create 30 Drivers
        val driversToInsert = driverList.mapIndexed { index, pair ->
            DriverEntity(
                id = (index + 1).toLong(),
                fleetId = fleetId,
                name = pair.first,
                phone = pair.second,
                consentAt = now - 10.hours,
                supabaseId = "drv_${index + 1}"
            )
        }
        driverRepository.insertAll(driversToInsert)

        // 2. Create 30 Vehicles (matching TS09 AB 1234 pattern from mockup)
        val regPrefixes = listOf("TS09 AB", "TS09 CD", "TS09 EF", "TS09 GH", "KA01 MJ", "KA05 NB", "KA03 MH")
        val vehiclesToInsert = (1..30).map { index ->
            val prefix = regPrefixes[(index - 1) % regPrefixes.size]
            val number = 1000 + (index * 137 % 8999)
            val model = carModels[(index - 1) % carModels.size]

            VehicleEntity(
                id = index.toLong(),
                fleetId = fleetId,
                regNo = "$prefix $number",
                nickname = model,
                status = if (index <= 28) "active" else "idle",
                supabaseId = "veh_$index",
                createdAt = now - 30.hours
            )
        }
        vehicleRepository.insertAll(vehiclesToInsert)

        // 3. Generate Realistic Today's Rides (Uber + Rapido)
        val ridesToInsert = mutableListOf<RideEntity>()
        var rideIdCounter = 1L

        for (cabIndex in 1..28) {
            val vehicleId = cabIndex.toLong()
            val driverId = cabIndex.toLong()

            val tripCount = Random.nextInt(4, 9)

            for (t in 0 until tripCount) {
                val platform = if (t % 2 == 0) Platform.UBER else Platform.RAPIDO

                val tripMinutesAgo = Random.nextInt(20, 520)
                val acceptedAt = now - tripMinutesAgo.minutes
                val tripDuration = Random.nextInt(15, 50)
                val completedAt = acceptedAt + tripDuration.minutes

                val distanceKm = (Random.nextDouble(4.0, 22.0) * 10).toInt() / 10.0
                val baseRate = if (platform == Platform.UBER) 18.0 else 16.5
                val fare = ((distanceKm * baseRate + Random.nextInt(35, 75)) / 10).toInt() * 10.0

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
                        confidence = 1.0,
                        needsReview = false,
                        syncStatus = SyncStatus.SYNCED,
                        createdAt = completedAt
                    )
                )
            }
        }

        rideRepository.insertAllRides(ridesToInsert)
    }
}
