package com.fleettrack.app.ui.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.DutyLogEntity
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.ManagerOverview
import com.fleettrack.app.data.model.UserRole
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.DutyLogRepository
import com.fleettrack.app.data.repository.ManagerRepository
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import com.fleettrack.app.data.session.ManagerSession
import com.fleettrack.app.data.session.ShiftManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ManagerDashboardViewModel @Inject constructor(
    private val shiftManager: ShiftManager,
    private val managerRepository: ManagerRepository,
    private val driverRepository: DriverRepository,
    private val vehicleRepository: VehicleRepository,
    private val rideRepository: RideRepository,
    private val dutyLogRepository: DutyLogRepository
) : ViewModel() {

    val managerSession: StateFlow<ManagerSession> = shiftManager.currentManager
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ManagerSession(
                managerId = "mgr_moosapet_01",
                managerName = "Srinivas Rao",
                hubName = "Moosapet Central Hub"
            )
        )

    val managerOverview: StateFlow<ManagerOverview?> = managerSession
        .flatMapLatest { session ->
            val id = session.managerId ?: "mgr_moosapet_01"
            managerRepository.getManagerOverview(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val assignedDrivers: StateFlow<List<DriverEntity>> = managerSession
        .flatMapLatest { session ->
            val id = session.managerId ?: "mgr_moosapet_01"
            driverRepository.getDriversByManager(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val assignedVehicles: StateFlow<List<VehicleEntity>> = managerSession
        .flatMapLatest { session ->
            val id = session.managerId ?: "mgr_moosapet_01"
            vehicleRepository.getVehiclesByManager(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val assignedDutyLogs: StateFlow<List<DutyLogEntity>> = managerSession
        .flatMapLatest { session ->
            val id = session.managerId ?: "mgr_moosapet_01"
            dutyLogRepository.getDutyLogsByManager(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayRides: StateFlow<List<RideEntity>> = managerSession
        .flatMapLatest { session ->
            val id = session.managerId ?: "mgr_moosapet_01"
            val now = Clock.System.now()
            val startOfDay = now - 24.hours
            rideRepository.getTodayRidesByManager(id, startOfDay)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onboardDriver(name: String, phone: String) {
        viewModelScope.launch {
            val activeMgrId = managerSession.value.managerId ?: "mgr_moosapet_01"
            val cleanPhone = phone.filter { it.isDigit() }.takeLast(10)
            val driver = DriverEntity(
                name = name.trim(),
                phone = "+91 $cleanPhone",
                consentAt = Clock.System.now(),
                fleetId = "svm_fleet_hyderabad",
                managerId = activeMgrId
            )
            driverRepository.insertDriver(driver)
        }
    }

    fun logout() {
        viewModelScope.launch {
            shiftManager.clearManagerSession()
            shiftManager.setUserRole(UserRole.OWNER)
        }
    }

    fun switchBackToOwner() = logout()
}

