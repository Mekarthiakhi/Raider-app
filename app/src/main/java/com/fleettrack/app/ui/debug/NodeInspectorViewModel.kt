package com.fleettrack.app.ui.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.session.ShiftManager
import com.fleettrack.app.service.RideTrackerAccessibilityService
import com.fleettrack.app.service.parser.ParsedRideData
import com.fleettrack.app.service.statemachine.ActiveTripState
import com.fleettrack.app.service.statemachine.RideStateMachine
import com.fleettrack.app.service.statemachine.TelemetryLogEntry
import com.fleettrack.app.work.WorkManagerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class NodeInspectorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rideStateMachine: RideStateMachine,
    private val shiftManager: ShiftManager
) : ViewModel() {

    val activeTrip: StateFlow<ActiveTripState?> = rideStateMachine.activeTrip
    val telemetryLogs: StateFlow<List<TelemetryLogEntry>> = rideStateMachine.telemetryLogs

    private val _currentDumpText = MutableStateFlow("")
    val currentDumpText: StateFlow<String> = _currentDumpText.asStateFlow()

    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    init {
        refreshDump()
    }

    fun refreshDump() {
        _isServiceEnabled.value = RideTrackerAccessibilityService.isServiceRunning
        val dump = RideTrackerAccessibilityService.lastActiveWindowDump
        _currentDumpText.value = if (dump.isNotBlank()) dump else "No window dump captured yet.\n\nOpen Rapido Captain, Uber Driver, or Ola Driver and return here to inspect node hierarchies."
    }

    fun copyDumpToClipboard(): Boolean {
        val dump = _currentDumpText.value
        if (dump.isBlank()) return false
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ride Tracker Node Dump", dump)
        clipboard.setPrimaryClip(clip)
        return true
    }

    fun simulateRide(platform: Platform, fare: Double, distanceKm: Double) {
        viewModelScope.launch {
            val shift = shiftManager.currentShift.first()
            val vehicleId = shift.vehicleId ?: 1L
            val driverId = shift.driverId ?: 1L

            // 1. Simulate Accept
            rideStateMachine.processParsedScreen(
                ParsedRideData(
                    platform = platform,
                    status = RideStatus.ACCEPTED,
                    pickup = "Madhapur Metro Pillar #A42",
                    confidence = 0.95,
                    rawExtracted = "Simulated Pickup Screen"
                ),
                vehicleId = vehicleId,
                driverId = driverId
            )

            // 2. Simulate In Trip
            rideStateMachine.processParsedScreen(
                ParsedRideData(
                    platform = platform,
                    status = RideStatus.IN_TRIP,
                    distanceKm = distanceKm,
                    confidence = 0.95,
                    rawExtracted = "Simulated In-Trip Navigation"
                ),
                vehicleId = vehicleId,
                driverId = driverId
            )

            // 3. Simulate Complete
            val minuteWindow = Clock.System.now().toEpochMilliseconds() / 60000
            rideStateMachine.processParsedScreen(
                ParsedRideData(
                    platform = platform,
                    status = RideStatus.COMPLETED,
                    fare = fare,
                    distanceKm = distanceKm,
                    durationMin = 18.0,
                    rideKey = "sim_${platform.name.lowercase()}_${minuteWindow}_${fare.toInt()}",
                    confidence = 1.0,
                    rawExtracted = "Collect Cash ₹${fare.toInt()} • Distance $distanceKm km"
                ),
                vehicleId = vehicleId,
                driverId = driverId
            )

            // Trigger sync
            WorkManagerScheduler.triggerImmediateOutboxSync(context)
            refreshDump()
        }
    }

    fun triggerSync() {
        WorkManagerScheduler.triggerImmediateOutboxSync(context)
    }
}

