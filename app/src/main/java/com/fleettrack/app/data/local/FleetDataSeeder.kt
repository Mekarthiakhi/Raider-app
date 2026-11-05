package com.fleettrack.app.data.local

import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.DutyLogEntity
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.SyncStatus
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.DutyLogRepository
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import com.fleettrack.app.util.SvmCommissionCalculator
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Seeder configured for SVM FLEET - Moosapet, Hyderabad (500018)
 * Contacts: 9391915013, 9959859347
 * Includes drivers and logs from official office paper register.
 */
@Singleton
class FleetDataSeeder @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val driverRepository: DriverRepository,
    private val rideRepository: RideRepository,
    private val dutyLogRepository: DutyLogRepository
) {
    private val carModels = listOf(
        "Hyundai Aura", "Maruti Dzire", "Honda Amaze", "Tata Tigor",
        "Maruti Tour S", "Hyundai Grand i10", "Toyota Etios"
    )

    companion object {
        // Drivers from the SVM FLEET office logbook
        val driverList = listOf(
            Pair("Suman", "+91 93919 15013"),
            Pair("Narendra", "+91 99598 59347"),
            Pair("Pavan", "+91 98451 22710"),
            Pair("Siva", "+91 97412 27330"),
            Pair("Prasad", "+91 96321 33200"),
            Pair("Baleru", "+91 98452 44550"),
            Pair("Ramesh Kumar", "+91 99001 55660"),
            Pair("Krishna Reddy", "+91 98450 66770"),
            Pair("Naresh G", "+91 97312 77880"),
            Pair("Mallesh Yadav", "+91 98860 88990"),
            Pair("Srinivas Rao", "+91 99450 11220"),
            Pair("Venkat Swamy", "+91 98456 22330"),
            Pair("Mahesh Goud", "+91 97410 33440"),
            Pair("Santosh V", "+91 96112 44550"),
            Pair("Arun Teja", "+91 98801 55660"),
            Pair("Raghavendra", "+91 99002 66770"),
            Pair("Dinesh Chary", "+91 98453 77880"),
            Pair("Chandrasekhar", "+91 97413 88990"),
            Pair("Shiva Shankar", "+91 96324 99000"),
            Pair("Anji Babu", "+91 98862 10100"),
            Pair("Naveen Kumar", "+91 99452 20200"),
            Pair("Pradeep Varma", "+91 98457 30300"),
            Pair("Harish Naidu", "+91 97314 40400"),
            Pair("Chetan Sai", "+91 96114 50500"),
            Pair("Ravinder Reddy", "+91 98803 60600"),
            Pair("Yadagiri", "+91 99004 70700"),
            Pair("Manoj K", "+91 98458 80800"),
            Pair("Giri Babu", "+91 97415 90900"),
            Pair("Bhanu Prasad", "+91 96326 12120"),
            Pair("Kiran Kumar", "+91 98864 13130")
        )
    }

    private val hyderabadLocations = listOf(
        "Moosapet Metro Station", "Kukatpally Y Junction", "Hitec City Cyber Towers",
        "Gachibowli DLF IT Park", "Madhapur Metro", "Jubilee Hills Road No. 36",
        "Banjara Hills Rd 12", "Secunderabad Railway Station", "Begumpet Airport Rd",
        "Ameerpet Circle", "Kondapur RTO", "Shamshabad Airport RGIA",
        "Miyapur Allwyn X Road", "Panjagutta Central Mall", "KPHB Colony Phase 3"
    )

    suspend fun seedIfEmpty() {
        val existingVehicles = vehicleRepository.getAllVehicles().first()
        if (existingVehicles.isNotEmpty()) return

        val now = Clock.System.now()
        val fleetId = "svm_fleet_hyderabad"

        // 1. Create 30 Drivers (SVM Fleet)
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

        // 2. Create 30 Vehicles (TS09 Hyderabad plates matching paper logbook)
        val specificVehicles = listOf("3368", "2271", "2733", "3320", "2950", "1840")
        val vehiclesToInsert = (1..30).map { index ->
            val number = if (index <= specificVehicles.size) {
                specificVehicles[index - 1]
            } else {
                "${1000 + (index * 179 % 8999)}"
            }
            val regNo = "TS09 UB $number"
            val model = carModels[(index - 1) % carModels.size]

            VehicleEntity(
                id = index.toLong(),
                fleetId = fleetId,
                regNo = regNo,
                nickname = model,
                status = if (index <= 28) "active" else "idle",
                supabaseId = "veh_$index",
                createdAt = now - 30.hours
            )
        }
        vehicleRepository.insertAll(vehiclesToInsert)

        // 3. Seed Office Paper Register Entries (Image 2 exact records)
        val officeDutyLogs = listOf(
            DutyLogEntity(
                fleetId = fleetId,
                shiftType = "12_HOURS",
                loginDate = "01/11/2026",
                loginTime = "06:00 AM",
                logoutDate = "01/11/2026",
                logoutTime = "08:15 PM",
                vehicleNo = "TS09 UB 3368",
                driverName = "Suman",
                driverPhone = "+91 93919 15013",
                trips = 16,
                earnings = 2950.0,
                cashCollection = 2503.0,
                expenditure = 200.0,
                commissionRent = 1032.5, // 35% tier (>= 2500)
                drivePass = 0.0,
                advance = 0.0,
                paymentCash = 0.0,
                paymentOnline = 278.0,
                netBalance = 0.0,
                status = "SETTLED",
                remarks = "Settled online via UPI. Good performance."
            ),
            DutyLogEntity(
                fleetId = fleetId,
                shiftType = "12_HOURS",
                loginDate = "01/11/2026",
                loginTime = "06:30 AM",
                logoutDate = "01/11/2026",
                logoutTime = "07:00 PM",
                vehicleNo = "TS09 UA 2271",
                driverName = "Narendra",
                driverPhone = "+91 99598 59347",
                trips = 7,
                earnings = 2271.0,
                cashCollection = 1850.0,
                expenditure = 150.0,
                commissionRent = 0.0, // Trips (7) < minimum 10
                drivePass = 0.0,
                advance = 0.0,
                paymentCash = 1700.0,
                paymentOnline = 0.0,
                netBalance = 0.0,
                status = "SETTLED",
                remarks = "Trips below 10 min threshold. Flat vehicle rent settled."
            ),
            DutyLogEntity(
                fleetId = fleetId,
                shiftType = "12_HOURS",
                loginDate = "01/11/2026",
                loginTime = "05:45 AM",
                logoutDate = "01/11/2026",
                logoutTime = "06:30 PM",
                vehicleNo = "TS09 UC 2733",
                driverName = "Pavan",
                driverPhone = "+91 98451 22710",
                trips = 11,
                earnings = 2733.0,
                cashCollection = 2100.0,
                expenditure = 100.0,
                commissionRent = 956.55, // 35% tier
                drivePass = 50.0,
                advance = 0.0,
                paymentCash = 0.0,
                paymentOnline = 450.0,
                netBalance = 0.0,
                status = "SETTLED",
                remarks = "35% commission applied. On time."
            ),
            DutyLogEntity(
                fleetId = fleetId,
                shiftType = "12_HOURS",
                loginDate = "01/11/2026",
                loginTime = "06:15 AM",
                logoutDate = "01/11/2026",
                logoutTime = "07:45 PM",
                vehicleNo = "TS09 UD 3320",
                driverName = "Siva",
                driverPhone = "+91 97412 27330",
                trips = 12,
                earnings = 3320.0,
                cashCollection = 2800.0,
                expenditure = 250.0,
                commissionRent = 1162.0, // 35% tier
                drivePass = 0.0,
                advance = 200.0,
                paymentCash = 0.0,
                paymentOnline = 350.0,
                netBalance = 0.0,
                status = "SETTLED",
                remarks = "High collection day."
            ),
            DutyLogEntity(
                fleetId = fleetId,
                shiftType = "12_HOURS",
                loginDate = "02/11/2026",
                loginTime = "06:00 AM",
                logoutDate = "02/11/2026",
                logoutTime = "06:00 PM",
                vehicleNo = "TS09 UE 2950",
                driverName = "Prasad",
                driverPhone = "+91 96321 33200",
                trips = 9,
                earnings = 1850.0,
                cashCollection = 1400.0,
                expenditure = 100.0,
                commissionRent = 0.0,
                drivePass = 0.0,
                advance = 0.0,
                paymentCash = 0.0,
                paymentOnline = 0.0,
                netBalance = -1300.0,
                status = "OPEN",
                remarks = "Active shift. Evening settlement pending."
            ),
            DutyLogEntity(
                fleetId = fleetId,
                shiftType = "24_HOURS",
                loginDate = "01/11/2026",
                loginTime = "08:00 AM",
                logoutDate = "02/11/2026",
                logoutTime = "08:00 AM",
                vehicleNo = "TS09 UF 1840",
                driverName = "Baleru",
                driverPhone = "+91 98452 44550",
                trips = 19,
                earnings = 6800.0,
                cashCollection = 5200.0,
                expenditure = 800.0,
                commissionRent = 2720.0, // 40% tier for >= 6500
                drivePass = 100.0,
                advance = 500.0,
                paymentCash = 0.0,
                paymentOnline = 1200.0,
                netBalance = 0.0,
                status = "SETTLED",
                remarks = "24hr shift completed. 40% highest commission tier earned."
            )
        )
        dutyLogRepository.insertAll(officeDutyLogs)

        // 4. Generate Realistic Today's Rides (Uber + Rapido in Hyderabad)
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

                val pickup = hyderabadLocations.random()
                var dropoff = hyderabadLocations.random()
                while (dropoff == pickup) {
                    dropoff = hyderabadLocations.random()
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
