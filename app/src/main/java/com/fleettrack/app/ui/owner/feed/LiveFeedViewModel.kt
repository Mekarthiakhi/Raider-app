package com.fleettrack.app.ui.owner.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LiveFeedViewModel @Inject constructor(
    private val rideRepository: RideRepository
) : ViewModel() {

    val recentRides: StateFlow<List<RideEntity>> = rideRepository.getRecentRides(limit = 60)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

