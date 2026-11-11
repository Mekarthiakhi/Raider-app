package com.fleettrack.app.ui.managers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.ManagerEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.ManagerOverview
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.ManagerRepository
import com.fleettrack.app.data.repository.VehicleRepository
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ManagersViewModel @Inject constructor(
    private val managerRepository: ManagerRepository,
    private val driverRepository: DriverRepository,
    private val vehicleRepository: VehicleRepository,
    private val shiftManager: ShiftManager
) : ViewModel() {

    val managers: StateFlow<List<ManagerEntity>> = managerRepository.getAllManagers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unassignedDrivers: StateFlow<List<DriverEntity>> = driverRepository.getUnassignedDrivers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unassignedVehicles: StateFlow<List<VehicleEntity>> = vehicleRepository.getUnassignedVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedManagerId = MutableStateFlow<String?>(null)
    val selectedManagerId: StateFlow<String?> = _selectedManagerId.asStateFlow()

    val selectedManagerOverview: StateFlow<ManagerOverview?> = _selectedManagerId
        .flatMapLatest { id ->
            if (id != null) managerRepository.getManagerOverview(id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val selectedManagerDrivers: StateFlow<List<DriverEntity>> = _selectedManagerId
        .flatMapLatest { id ->
            if (id != null) driverRepository.getDriversByManager(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedManagerVehicles: StateFlow<List<VehicleEntity>> = _selectedManagerId
        .flatMapLatest { id ->
            if (id != null) vehicleRepository.getVehiclesByManager(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectManager(managerId: String) {
        _selectedManagerId.value = managerId
    }

    fun addManager(name: String, phone: String, hubName: String, pinCode: String = "1234") {
        viewModelScope.launch {
            val cleanPhone = phone.filter { it.isDigit() }.takeLast(10)
            val id = "mgr_${cleanPhone.takeLast(4)}_${System.currentTimeMillis() % 1000}"
            val entity = ManagerEntity(
                id = id,
                name = name.trim(),
                phone = cleanPhone,
                hubName = hubName.trim(),
                pinCode = if (pinCode.isBlank()) "1234" else pinCode.trim(),
                isActive = true
            )
            managerRepository.insertManager(entity)
        }
    }

    fun updateManager(manager: ManagerEntity) {
        viewModelScope.launch {
            managerRepository.updateManager(manager)
        }
    }

    fun deleteManager(managerId: String) {
        viewModelScope.launch {
            managerRepository.deleteManager(managerId)
        }
    }

    fun assignDriver(driverId: Long, managerId: String?) {
        viewModelScope.launch {
            managerRepository.assignDriverToManager(driverId, managerId)
        }
    }

    fun assignVehicle(vehicleId: Long, managerId: String?) {
        viewModelScope.launch {
            managerRepository.assignVehicleToManager(vehicleId, managerId)
        }
    }

    suspend fun enterManagerPortal(manager: ManagerEntity) {
        shiftManager.setManagerSession(manager.id, manager.name, manager.hubName)
    }
}

