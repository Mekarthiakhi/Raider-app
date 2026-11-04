package com.fleettrack.app.ui.owner.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class EnrichedTrip(
    val ride: RideEntity,
    val vehicleRegNo: String,
    val vehicleModel: String
)

@HiltViewModel
class LiveFeedViewModel @Inject constructor(
    private val rideRepository: RideRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    val recentRides: StateFlow<List<EnrichedTrip>> = combine(
        rideRepository.getRecentRides(limit = 60),
        vehicleRepository.getAllVehicles()
    ) { rides, vehicles ->
        val vehicleMap = vehicles.associateBy { it.id }
        rides.map { ride ->
            val v = vehicleMap[ride.vehicleId]
            EnrichedTrip(
                ride = ride,
                vehicleRegNo = v?.regNo ?: "TS09 AB 1234",
                vehicleModel = v?.nickname ?: "Hyundai Aura"
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
