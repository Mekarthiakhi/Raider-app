package com.fleettrack.app.ui.owner.detail

import androidx.lifecycle.SavedStateHandle
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

data class VehiclePlatformSplit(
    val uberFare: Double = 0.0,
    val uberRides: Int = 0,
    val rapidoFare: Double = 0.0,
    val rapidoRides: Int = 0
)

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val driverRepository: DriverRepository,
    private val rideRepository: RideRepository
) : ViewModel() {

    val vehicleId: Long = savedStateHandle.get<String>("vehicleId")?.toLongOrNull() ?: 1L

    private val _vehicle = MutableStateFlow<VehicleEntity?>(null)
    val vehicle: StateFlow<VehicleEntity?> = _vehicle.asStateFlow()

    private val _assignedDriver = MutableStateFlow<DriverEntity?>(null)
    val assignedDriver: StateFlow<DriverEntity?> = _assignedDriver.asStateFlow()

    private val startOfDay: Instant = Clock.System.now() - 14.hours

    val summary: StateFlow<RideSummary> = rideRepository.getTodaySummaryForVehicle(vehicleId, startOfDay)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RideSummary()
        )

    val rides: StateFlow<List<RideEntity>> = rideRepository.getRidesByVehicle(vehicleId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val platformSplit: StateFlow<VehiclePlatformSplit> = rides.map { rideList ->
        var uFare = 0.0
        var uCount = 0
        var rFare = 0.0
        var rCount = 0

        for (r in rideList) {
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
        VehiclePlatformSplit(
            uberFare = uFare,
            uberRides = uCount,
            rapidoFare = rFare,
            rapidoRides = rCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VehiclePlatformSplit()
    )

    init {
        viewModelScope.launch {
            _vehicle.value = vehicleRepository.getVehicleById(vehicleId)

            // Look up driver from latest ride
            rides.collect { rideList ->
                val latest = rideList.maxByOrNull { it.acceptedAt }
                if (latest != null) {
                    _assignedDriver.value = driverRepository.getDriverById(latest.driverId)
                }
            }
        }
    }
}
