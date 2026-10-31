package com.fleettrack.app.ui.owner.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewQueueViewModel @Inject constructor(
    private val rideRepository: RideRepository
) : ViewModel() {

    val reviewRides: StateFlow<List<RideEntity>> = rideRepository.getNeedsReviewRides()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun resolveRide(id: Long, fare: Double?, distanceKm: Double?, note: String?) {
        viewModelScope.launch {
            rideRepository.resolveReview(id, fare, distanceKm, note)
        }
    }
}

