package com.fleettrack.app.ui.driver.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideSummary
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import com.fleettrack.app.data.session.ShiftManager
import com.fleettrack.app.data.session.ShiftSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

data class DriverPlatformSummary(
    val uberFare: Double = 0.0,
    val uberRides: Int = 0,
    val rapidoFare: Double = 0.0,
    val rapidoRides: Int = 0,
    val olaFare: Double = 0.0,
    val olaRides: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DriverTodayViewModel @Inject constructor(
    private val rideRepository: RideRepository,
    private val driverRepository: DriverRepository,
    private val vehicleRepository: VehicleRepository,
    private val shiftManager: ShiftManager
) : ViewModel() {

    private val startOfDay: Instant = Clock.System.now() - 14.hours

    val shiftSession: StateFlow<ShiftSession> = shiftManager.currentShift
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ShiftSession()
        )

    val availableDrivers: StateFlow<List<DriverEntity>> = driverRepository.getAllDrivers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val availableVehicles: StateFlow<List<VehicleEntity>> = vehicleRepository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isTrackingActive = MutableStateFlow(true)
    val isTrackingActive: StateFlow<Boolean> = _isTrackingActive.asStateFlow()

    val todayRides: StateFlow<List<RideEntity>> = shiftSession.flatMapLatest { session ->
        if (session.isShiftActive && session.vehicleId != null) {
            rideRepository.getTodayRidesForVehicle(session.vehicleId, startOfDay)
        } else {
            rideRepository.getTodayRides(startOfDay)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todaySummary: StateFlow<RideSummary> = shiftSession.flatMapLatest { session ->
        if (session.isShiftActive && session.vehicleId != null) {
            rideRepository.getTodaySummaryForVehicle(session.vehicleId, startOfDay)
        } else {
            rideRepository.getTodaySummary(startOfDay)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RideSummary()
    )

    val platformBreakdown: StateFlow<DriverPlatformSummary> = todayRides.map { rides ->
        var uFare = 0.0
        var uCount = 0
        var rFare = 0.0
        var rCount = 0
        var oFare = 0.0
        var oCount = 0

        for (ride in rides) {
            val fare = ride.fare ?: 0.0
            when (ride.platform) {
                Platform.UBER -> {
                    uFare += fare
                    uCount++
                }
                Platform.RAPIDO -> {
                    rFare += fare
                    rCount++
                }
                Platform.OLA -> {
                    oFare += fare
                    oCount++
                }
                else -> {}
            }
        }
        DriverPlatformSummary(
            uberFare = uFare,
            uberRides = uCount,
            rapidoFare = rFare,
            rapidoRides = rCount,
            olaFare = oFare,
            olaRides = oCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriverPlatformSummary()
    )

    fun startShift(driver: DriverEntity, vehicle: VehicleEntity) {
        viewModelScope.launch {
            shiftManager.startShift(
                driverId = driver.id,
                driverName = driver.name,
                vehicleId = vehicle.id,
                vehicleRegNo = vehicle.regNo,
                vehicleNickname = vehicle.nickname
            )
        }
    }

    fun endShift() {
        viewModelScope.launch {
            shiftManager.endShift()
        }
    }
}
