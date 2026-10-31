package com.fleettrack.app.ui.driver.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.model.RideSummary
import com.fleettrack.app.data.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@HiltViewModel
class DriverTodayViewModel @Inject constructor(
    private val rideRepository: RideRepository
) : ViewModel() {

    private val startOfDay: Instant = Clock.System.now() - 12.hours

    private val _isTrackingActive = MutableStateFlow(true)
    val isTrackingActive: StateFlow<Boolean> = _isTrackingActive.asStateFlow()

    val todaySummary: StateFlow<RideSummary> = rideRepository.getTodaySummary(startOfDay)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RideSummary()
        )

    val todayRides: StateFlow<List<RideEntity>> = rideRepository.getTodayRides(startOfDay)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

