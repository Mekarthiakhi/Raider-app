package com.fleettrack.app.ui.owner.drivers

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

data class CarPerformanceSummary(
    val vehicleId: Long,
    val regNo: String,
    val model: String,
    val ridesCount: Int,
    val totalFare: Double,
    val totalKm: Double,
    val uberRides: Int,
    val rapidoRides: Int
)

data class DriverTripItem(
    val id: Long,
    val rideKey: String,
    val platform: Platform,
    val vehicleRegNo: String,
    val vehicleModel: String,
    val fare: Double,
    val distanceKm: Double,
    val pickup: String,
    val dropoff: String,
    val acceptedAt: Instant,
    val driverPayout: Double,
    val ownerMargin: Double
)

data class DriverReportUiState(
    val driver: DriverEntity? = null,
    val currentCarRegNo: String = "Unassigned",
    val activeShift: String = "Day Shift (6 AM - 6 PM)",
    val todayEarnings: Double = 0.0,
    val weeklyEarnings: Double = 0.0,
    val monthlyEarnings: Double = 0.0,
    val totalRides: Int = 0,
    val totalKm: Double = 0.0,
    val uberEarnings: Double = 0.0,
    val uberRidesCount: Int = 0,
    val rapidoEarnings: Double = 0.0,
    val rapidoRidesCount: Int = 0,
    val driverPayout: Double = 0.0,
    val ownerNetProfit: Double = 0.0,
    val carBreakdown: List<CarPerformanceSummary> = emptyList(),
    val trips: List<DriverTripItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DriverDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val driverRepository: DriverRepository,
    private val vehicleRepository: VehicleRepository,
    private val rideRepository: RideRepository
) : ViewModel() {

    private val isDemoMode: Boolean
        get() = context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE)
            .getBoolean("is_demo_mode", false)

    private val driverId: Long = runCatching {
        savedStateHandle.get<String>("driverId")?.toLongOrNull()
    }.getOrNull()
        ?: runCatching { savedStateHandle.get<Long>("driverId") }.getOrNull()
        ?: 1L

    private val now = Clock.System.now()
    private val startOfDay = now - 14.hours
    private val startOfWeek = now - 7.days
    private val startOfMonth = now - 30.days

    val uiState: StateFlow<DriverReportUiState> = combine(
        driverRepository.getAllDrivers(),
        vehicleRepository.getAllVehicles(),
        rideRepository.getRidesByDriver(driverId)
    ) { drivers, vehicles, rides ->
        val driver = drivers.find { it.id == driverId }
        val vehicleMap = vehicles.associateBy { it.id }

        var todayFare = 0.0
        var weekFare = 0.0
        var monthFare = 0.0
        var totalKm = 0.0

        var uFare = 0.0
        var uCount = 0
        var rFare = 0.0
        var rCount = 0

        for (ride in rides) {
            val f = ride.fare ?: 0.0
            val km = ride.distanceKm ?: 0.0
            totalKm += km

            if (ride.acceptedAt >= startOfDay) todayFare += f
            if (ride.acceptedAt >= startOfWeek) weekFare += f
            if (ride.acceptedAt >= startOfMonth) monthFare += f

            when (ride.platform) {
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

        // Car breakdown (Which car did this driver drive, how many trips, how much earned)
        val carGroups = rides.groupBy { it.vehicleId }
        val carBreakdown = carGroups.map { (vId, vRides) ->
            val veh = vehicleMap[vId]
            val vFare = vRides.sumOf { it.fare ?: 0.0 }
            val vKm = vRides.sumOf { it.distanceKm ?: 0.0 }
            CarPerformanceSummary(
                vehicleId = vId,
                regNo = veh?.regNo ?: "TS09 AB 1234",
                model = veh?.nickname ?: "Hyundai Aura",
                ridesCount = vRides.size,
                totalFare = vFare,
                totalKm = vKm,
                uberRides = vRides.count { it.platform == Platform.UBER },
                rapidoRides = vRides.count { it.platform == Platform.RAPIDO }
            )
        }.sortedByDescending { it.totalFare }

        // Latest assigned vehicle
        val latestRide = rides.maxByOrNull { it.acceptedAt }
        val assignedVeh = latestRide?.let { vehicleMap[it.vehicleId] }

        // Map trips
        val tripItems = rides.map { r ->
            val veh = vehicleMap[r.vehicleId]
            val fare = r.fare ?: 0.0
            // Standard Indian fleet payout: 70% driver payout, 30% owner fleet gross
            val driverPayout = fare * 0.70
            val ownerMargin = fare * 0.30

            DriverTripItem(
                id = r.id,
                rideKey = r.rideKey,
                platform = r.platform,
                vehicleRegNo = veh?.regNo ?: "TS09 AB 1234",
                vehicleModel = veh?.nickname ?: "Hyundai Aura",
                fare = fare,
                distanceKm = r.distanceKm ?: 0.0,
                pickup = r.pickup ?: "Koramangala 5th Block",
                dropoff = r.dropoff ?: "Kempegowda Int'l Airport",
                acceptedAt = r.acceptedAt,
                driverPayout = driverPayout,
                ownerMargin = ownerMargin
            )
        }

        val totalGross = rides.sumOf { it.fare ?: 0.0 }
        val totalDriverPayout = totalGross * 0.70
        val totalOwnerMargin = totalGross * 0.30

        val showDemoDefaults = isDemoMode && rides.isEmpty()

        DriverReportUiState(
            driver = driver,
            currentCarRegNo = assignedVeh?.regNo ?: if (showDemoDefaults) "TS09 AB 1234" else "Unassigned",
            activeShift = if (rides.isNotEmpty()) "Day Shift (6 AM - 6 PM)" else if (showDemoDefaults) "Day Shift (6 AM - 6 PM)" else "Idle",
            todayEarnings = if (todayFare > 0) todayFare else if (showDemoDefaults) 2840.0 else 0.0,
            weeklyEarnings = if (weekFare > 0) weekFare else if (showDemoDefaults) 18450.0 else 0.0,
            monthlyEarnings = if (monthFare > 0) monthFare else if (showDemoDefaults) 74200.0 else 0.0,
            totalRides = if (rides.isNotEmpty()) rides.size else if (showDemoDefaults) 14 else 0,
            totalKm = if (totalKm > 0) totalKm else if (showDemoDefaults) 182.4 else 0.0,
            uberEarnings = if (uFare > 0) uFare else if (showDemoDefaults) 1640.0 else 0.0,
            uberRidesCount = if (uCount > 0) uCount else if (showDemoDefaults) 8 else 0,
            rapidoEarnings = if (rFare > 0) rFare else if (showDemoDefaults) 1200.0 else 0.0,
            rapidoRidesCount = if (rCount > 0) rCount else if (showDemoDefaults) 6 else 0,
            driverPayout = if (totalDriverPayout > 0) totalDriverPayout else if (showDemoDefaults) 1988.0 else 0.0,
            ownerNetProfit = if (totalOwnerMargin > 0) totalOwnerMargin else if (showDemoDefaults) 852.0 else 0.0,
            carBreakdown = if (carBreakdown.isNotEmpty()) carBreakdown else if (showDemoDefaults) listOf(
                CarPerformanceSummary(
                    vehicleId = 1L,
                    regNo = "TS09 AB 1234",
                    model = "Hyundai Aura • Petrol",
                    ridesCount = 14,
                    totalFare = 2840.0,
                    totalKm = 182.4,
                    uberRides = 8,
                    rapidoRides = 6
                )
            ) else emptyList(),
            trips = tripItems,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriverReportUiState(isLoading = true)
    )
}

