package com.fleettrack.app.ui.owner.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.RideSummary
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val rideRepository: RideRepository
) : ViewModel() {

    val vehicleId: Long = savedStateHandle.get<String>("vehicleId")?.toLongOrNull() ?: 1L

    private val _vehicle = MutableStateFlow<VehicleEntity?>(null)
    val vehicle: StateFlow<VehicleEntity?> = _vehicle.asStateFlow()

    private val startOfDay: Instant = Clock.System.now() - 12.hours

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

    init {
        viewModelScope.launch {
            _vehicle.value = vehicleRepository.getVehicleById(vehicleId)
        }
    }
}

