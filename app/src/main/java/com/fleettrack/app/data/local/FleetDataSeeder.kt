package com.fleettrack.app.data.local

import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.DutyLogEntity
import com.fleettrack.app.data.local.entity.ManagerEntity
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.SyncStatus
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.DutyLogRepository
import com.fleettrack.app.data.repository.ManagerRepository
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import com.fleettrack.app.util.SvmCommissionCalculator
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
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
    private val dutyLogRepository: DutyLogRepository,
    private val managerRepository: ManagerRepository
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

    private fun getManagerForIndex(index: Int): String {
        return when {
            index <= 12 -> "mgr_moosapet_01"
            index <= 22 -> "mgr_kukatpally_02"
            else -> "mgr_madhapur_03"
        }
    }

    suspend fun seedIfEmpty() {
        val now = Clock.System.now()
        val fleetId = "svm_fleet_hyderabad"

        // Seed Managers if empty
        val existingManagers = managerRepository.getAllManagers().first()
        if (existingManagers.isEmpty()) {
            val managersToInsert = listOf(
                ManagerEntity(
                    id = "mgr_moosapet_01",
                    name = "Srinivas Rao",
                    phone = "9849012345",
                    hubName = "Moosapet Central Hub",
                    pinCode = "1234",
                    isActive = true,
                    ownerId = "owner_akhilesh",
                    createdAt = now - 60.hours
                ),
                ManagerEntity(
                    id = "mgr_kukatpally_02",
                    name = "Venkatesh Naidu",
                    phone = "9701234567",
                    hubName = "Kukatpally Y-Junction Hub",
                    pinCode = "2345",
                    isActive = true,
                    ownerId = "owner_akhilesh",
                    createdAt = now - 60.hours
                ),
                ManagerEntity(
                    id = "mgr_madhapur_03",
                    name = "Mohammed Farooq",
                    phone = "9989056789",
                    hubName = "Madhapur / Hitec City Hub",
                    pinCode = "3456",
                    isActive = true,
                    ownerId = "owner_akhilesh",
                    createdAt = now - 60.hours
                )
            )
            managerRepository.insertManagers(managersToInsert)

            // If vehicles exist, retroactively assign managers to drivers & vehicles
            val existingVehicles = vehicleRepository.getAllVehicles().first()
            if (existingVehicles.isNotEmpty()) {
                val drivers = driverRepository.getAllDrivers().first()
                drivers.forEachIndexed { idx, d ->
                    if (d.managerId == null) {
                        driverRepository.assignDriverToManager(d.id, getManagerForIndex(idx + 1))
                    }
                }
                existingVehicles.forEachIndexed { idx, v ->
                    if (v.managerId == null) {
                        vehicleRepository.assignVehicleToManager(v.id, getManagerForIndex(idx + 1))
                    }
                }
            }
        }

        val existingVehicles = vehicleRepository.getAllVehicles().first()
        if (existingVehicles.isNotEmpty()) {
            ensureMultiDaySampleRides(existingVehicles, now)
            return
        }

        // 1. Create 30 Drivers (SVM Fleet)
        val driversToInsert = driverList.mapIndexed { index, pair ->
            DriverEntity(
                id = (index + 1).toLong(),
                fleetId = fleetId,
                name = pair.first,
                phone = pair.second,
                consentAt = now - 10.hours,
                supabaseId = "drv_${index + 1}",
                managerId = getManagerForIndex(index + 1)
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
                createdAt = now - 30.hours,
                managerId = getManagerForIndex(index)
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
                remarks = "Settled online via UPI. Good performance.",
                managerId = "mgr_moosapet_01"
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
                remarks = "Trips below 10 min threshold. Flat vehicle rent settled.",
                managerId = "mgr_moosapet_01"
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
                remarks = "35% commission applied. On time.",
                managerId = "mgr_moosapet_01"
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
                remarks = "High collection day.",
                managerId = "mgr_moosapet_01"
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
                remarks = "Active shift. Evening settlement pending.",
                managerId = "mgr_moosapet_01"
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
                remarks = "24hr shift completed. 40% highest commission tier earned.",
                managerId = "mgr_moosapet_01"
            )
        )
        dutyLogRepository.insertAll(officeDutyLogs)

        // 4. Generate Realistic Multi-Day Rides (Today, Yesterday, This Week, This Month) for All 30 Cabs
        val allVehiclesSeeded = (1..30).map { cabIndex ->
            Triple(cabIndex.toLong(), cabIndex.toLong(), getManagerForIndex(cabIndex))
        }
        val ridesToInsert = buildSampleRidesForVehicles(
            vehicleBindings = allVehiclesSeeded,
            now = now,
            seedTag = "init"
        )
        rideRepository.insertAllRides(ridesToInsert)
    }

    private suspend fun ensureMultiDaySampleRides(existingVehicles: List<VehicleEntity>, now: Instant) {
        val existingRides = rideRepository.getRecentRides(limit = 500).first()
        val tz = TimeZone.currentSystemDefault()
        val todayDate = now.toLocalDateTime(tz).date
        val yesterdayDate = (now - 1.days).toLocalDateTime(tz).date
        val hasToday = existingRides.count { it.acceptedAt.toLocalDateTime(tz).date == todayDate } >= 5
        val hasYesterday = existingRides.any { it.acceptedAt.toLocalDateTime(tz).date == yesterdayDate }
        if (hasToday && hasYesterday && existingRides.size >= 30) return

        val existingDrivers = driverRepository.getAllDrivers().first()
        if (existingDrivers.isEmpty()) return

        val bindings = existingVehicles.mapIndexed { idx, v ->
            val driverId = existingDrivers[idx % existingDrivers.size].id
            Triple(v.id, driverId, v.managerId ?: getManagerForIndex(idx + 1))
        }
        val backfillRides = buildSampleRidesForVehicles(
            vehicleBindings = bindings,
            now = now,
            seedTag = "md_${todayDate}"
        )
        backfillRides.forEach { ride ->
            rideRepository.insertOrIgnoreRide(ride)
        }
    }

    private fun buildSampleRidesForVehicles(
        vehicleBindings: List<Triple<Long, Long, String>>,
        now: Instant,
        seedTag: String
    ): List<RideEntity> {
        val rides = mutableListOf<RideEntity>()
        // Spread trips per cab across Today (0), Yesterday (1), This Week (2..5), and This Month (9..19)
        val dayBuckets = listOf(0, 0, 1, 2, 4, 9)

        vehicleBindings.forEachIndexed { idx, (vehicleId, driverId, managerId) ->
            val extraWeekOffset = 2 + (idx % 4)
            val extraMonthOffset = 10 + (idx % 10)
            val offsets = dayBuckets + listOf(extraWeekOffset, extraMonthOffset)

            offsets.forEachIndexed { tripIdx, dayOffset ->
                val platform = if ((idx + tripIdx) % 2 == 0) Platform.UBER else Platform.RAPIDO
                val minutesOffset = if (dayOffset == 0) {
                    25 + ((idx * 17 + tripIdx * 43) % 420)
                } else {
                    60 + ((idx * 29 + tripIdx * 61) % 600)
                }
                val acceptedAt = now - dayOffset.days - minutesOffset.minutes
                val tripDuration = 16 + ((idx + tripIdx) % 30)
                val completedAt = acceptedAt + tripDuration.minutes

                val distanceKm = ((5.0 + ((idx * 3 + tripIdx * 4) % 16)) * 10).toInt() / 10.0
                val baseRate = if (platform == Platform.UBER) 18.0 else 16.5
                val fare = ((distanceKm * baseRate + 40 + ((idx + tripIdx) * 7) % 35) / 10).toInt() * 10.0

                val pickup = hyderabadLocations[(idx + tripIdx) % hyderabadLocations.size]
                val dropoff = hyderabadLocations[(idx + tripIdx + 3) % hyderabadLocations.size]
                val rideKey = "${platform.name.lowercase()}_${seedTag}_${vehicleId}_${tripIdx}_${dayOffset}"

                rides.add(
                    RideEntity(
                        id = 0L,
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
                        createdAt = completedAt,
                        managerId = managerId
                    )
                )
            }
        }
        return rides
    }
}
