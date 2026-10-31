package com.fleettrack.app.ui.owner.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlin.time.Duration.Companion.days

enum class ReportPeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val rideRepository: RideRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.DAILY)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val now = Clock.System.now()

    val dailySummary: StateFlow<RideSummary> = rideRepository.getTodaySummary(now - 1.days)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RideSummary())

    val weeklySummary: StateFlow<RideSummary> = rideRepository.getTodaySummary(now - 7.days)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RideSummary())

    val monthlySummary: StateFlow<RideSummary> = rideRepository.getTodaySummary(now - 30.days)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RideSummary())

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }
}

