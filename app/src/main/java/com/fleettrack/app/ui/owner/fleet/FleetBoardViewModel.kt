package com.fleettrack.app.ui.owner.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.DriverPerformance
import com.fleettrack.app.data.model.FleetOverview
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.VehicleStatus
import com.fleettrack.app.data.repository.DeviceRepository
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

enum class FleetBoardTab {
    VEHICLES,
    DRIVERS
}

@HiltViewModel
class FleetBoardViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val driverRepository: DriverRepository,
    private val rideRepository: RideRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val startOfDay: Instant = Clock.System.now() - 14.hours

    private val _selectedTab = MutableStateFlow(FleetBoardTab.VEHICLES)
    val selectedTab: StateFlow<FleetBoardTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: FleetBoardTab) {
        _selectedTab.value = tab
    }

    private val allVehicles = vehicleRepository.getAllVehicles()
    private val allDrivers = driverRepository.getAllDrivers()
    private val todayRides = rideRepository.getTodayRides(startOfDay)

    val overview: StateFlow<FleetOverview> = combine(
        todayRides,
        vehicleRepository.getActiveCount()
    ) { rides, activeCount ->
        var totalFare = 0.0
        var totalKm = 0.0
        var uFare = 0.0
        var uCount = 0
        var rFare = 0.0
        var rCount = 0

        for (ride in rides) {
            val fare = ride.fare ?: 0.0
            val km = ride.distanceKm ?: 0.0
            totalFare += fare
            totalKm += km

            when (ride.platform) {
                Platform.UBER -> {
                    uFare += fare
                    uCount++
                }
                Platform.RAPIDO -> {
                    rFare += fare
                    rCount++
                }
                else -> {}
            }
        }

        FleetOverview(
            totalEarningsToday = totalFare,
            totalRidesToday = rides.size,
            totalKmToday = totalKm,
            activeVehicles = activeCount,
            offlineVehicles = 30 - activeCount,
            onTripVehicles = 0,
            uberEarnings = uFare,
            uberRides = uCount,
            rapidoEarnings = rFare,
            rapidoRides = rCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FleetOverview()
    )

    val vehicles: StateFlow<List<VehicleStatus>> = combine(
        allVehicles,
        allDrivers,
        todayRides
    ) { vehicleList, driverList, rides ->
        val driverMap = driverList.associateBy { it.id }

        vehicleList.map { vehicle ->
            val vehicleRides = rides.filter { it.vehicleId == vehicle.id }
            val totalFare = vehicleRides.sumOf { it.fare ?: 0.0 }
            val totalKm = vehicleRides.sumOf { it.distanceKm ?: 0.0 }
            val latestRide = vehicleRides.maxByOrNull { it.acceptedAt }

            val assignedDriver = latestRide?.let { driverMap[it.driverId] }

            var uFare = 0.0
            var uCount = 0
            var rFare = 0.0
            var rCount = 0

            for (r in vehicleRides) {
                val f = r.fare ?: 0.0
                when (r.platform) {
                    Platform.UBER -> {
                        uFare += f
                        uCount++
                    }
                    Platform.RAPIDO -> {
                        rFare += f
                        rCount++
                    }
                    else -> {}
                }
            }

            VehicleStatus(
                vehicleId = vehicle.id,
                regNo = vehicle.regNo,
                nickname = vehicle.nickname,
                isOnline = vehicle.status == "active",
                currentRideStatus = latestRide?.status ?: RideStatus.IDLE,
                todayFare = totalFare,
                todayRides = vehicleRides.size,
                todayKm = totalKm,
                lastSeen = latestRide?.completedAt ?: latestRide?.acceptedAt,
                batteryLevel = 88,
                currentDriverName = assignedDriver?.name,
                currentDriverPhone = assignedDriver?.phone,
                uberFare = uFare,
                uberRides = uCount,
                rapidoFare = rFare,
                rapidoRides = rCount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val drivers: StateFlow<List<DriverPerformance>> = combine(
        allDrivers,
        allVehicles,
        todayRides
    ) { driverList, vehicleList, rides ->
        val vehicleMap = vehicleList.associateBy { it.id }

        driverList.map { driver ->
            val driverRides = rides.filter { it.driverId == driver.id }
            val totalFare = driverRides.sumOf { it.fare ?: 0.0 }
            val totalKm = driverRides.sumOf { it.distanceKm ?: 0.0 }
            val latestRide = driverRides.maxByOrNull { it.acceptedAt }
            val assignedVeh = latestRide?.let { vehicleMap[it.vehicleId] }

            var uFare = 0.0
            var uCount = 0
            var rFare = 0.0
            var rCount = 0

            for (r in driverRides) {
                val f = r.fare ?: 0.0
                when (r.platform) {
                    Platform.UBER -> {
                        uFare += f
                        uCount++
                    }
                    Platform.RAPIDO -> {
                        rFare += f
                        rCount++
                    }
                    else -> {}
                }
            }

            DriverPerformance(
                driverId = driver.id,
                driverName = driver.name,
                phone = driver.phone,
                assignedVehicleRegNo = assignedVeh?.regNo,
                assignedVehicleNickname = assignedVeh?.nickname,
                isShiftActive = driverRides.isNotEmpty(),
                todayFare = totalFare,
                todayRides = driverRides.size,
                todayKm = totalKm,
                uberFare = uFare,
                uberRides = uCount,
                rapidoFare = rFare,
                rapidoRides = rCount
            )
        }.sortedByDescending { it.todayFare }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addVehicle(regNo: String, model: String, fuelType: String) {
        viewModelScope.launch {
            vehicleRepository.insertVehicle(
                VehicleEntity(
                    fleetId = "fleet_hyd_01",
                    regNo = regNo.uppercase(),
                    nickname = "$model • $fuelType",
                    status = "active",
                    createdAt = Clock.System.now()
                )
            )
        }
    }

    fun addDriver(name: String, phone: String) {
        viewModelScope.launch {
            driverRepository.insertDriver(
                DriverEntity(
                    fleetId = "fleet_hyd_01",
                    name = name,
                    phone = phone,
                    consentAt = Clock.System.now()
                )
            )
        }
    }
}
