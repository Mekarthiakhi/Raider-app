package com.fleettrack.app.ui.owner.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.FleetOverview
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.VehicleStatus
import com.fleettrack.app.data.repository.DeviceRepository
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@HiltViewModel
class FleetBoardViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val rideRepository: RideRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    // Start of current day (midnight approx based on local time window)
    private val startOfDay: Instant = Clock.System.now() - 12.hours

    val overview: StateFlow<FleetOverview> = rideRepository.getTodaySummary(startOfDay)
        .combine(vehicleRepository.getActiveCount()) { summary, activeCount ->
            FleetOverview(
                totalEarningsToday = summary.totalFare,
                totalRidesToday = summary.rideCount,
                totalKmToday = summary.totalKm,
                activeVehicles = activeCount,
                offlineVehicles = 0,
                onTripVehicles = 0
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FleetOverview()
        )

    val vehicles: StateFlow<List<VehicleStatus>> = vehicleRepository.getAllVehicles()
        .combine(rideRepository.getTodayRides(startOfDay)) { vehicleList, todayRides ->
            vehicleList.map { vehicle ->
                val vehicleRides = todayRides.filter { it.vehicleId == vehicle.id }
                val totalFare = vehicleRides.sumOf { it.fare ?: 0.0 }
                val totalKm = vehicleRides.sumOf { it.distanceKm ?: 0.0 }
                val latestRide = vehicleRides.maxByOrNull { it.acceptedAt }

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
                    batteryLevel = 85 // Mock or populated from device
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

