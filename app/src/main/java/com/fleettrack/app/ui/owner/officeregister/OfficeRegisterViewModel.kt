package com.fleettrack.app.ui.owner.officeregister

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.entity.DutyLogEntity
import com.fleettrack.app.data.repository.DutyLogRepository
import com.fleettrack.app.util.SvmCommissionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OfficeRegisterUiState(
    val dutyLogs: List<DutyLogEntity> = emptyList(),
    val totalEarnings: Double = 0.0,
    val totalCashCollection: Double = 0.0,
    val totalTrips: Int = 0,
    val searchQuery: String = "",
    val selectedFilter: String = "ALL" // "ALL", "12_HOURS", "24_HOURS", "OPEN"
)

@HiltViewModel
class OfficeRegisterViewModel @Inject constructor(
    private val repository: DutyLogRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _managerFilter = MutableStateFlow<String?>(null)
    val managerFilter: StateFlow<String?> = _managerFilter.asStateFlow()

    private val allLogs = repository.getAllDutyLogs()

    val uiState: StateFlow<OfficeRegisterUiState> = combine(
        allLogs,
        _searchQuery,
        _selectedFilter,
        _managerFilter
    ) { logs, query, filter, mgrId ->
        val filtered = logs.filter { log ->
            val matchesManager = mgrId == null || log.managerId == mgrId

            val matchesQuery = query.isBlank() ||
                    log.vehicleNo.contains(query, ignoreCase = true) ||
                    log.driverName.contains(query, ignoreCase = true) ||
                    (log.remarks ?: "").contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "12_HOURS" -> log.shiftType == "12_HOURS"
                "24_HOURS" -> log.shiftType == "24_HOURS"
                "OPEN" -> log.status == "OPEN"
                else -> true
            }

            matchesManager && matchesQuery && matchesFilter
        }

        val totalEarnings = filtered.sumOf { it.earnings }
        val totalCash = filtered.sumOf { it.cashCollection }
        val totalTrips = filtered.sumOf { it.trips }

        OfficeRegisterUiState(
            dutyLogs = filtered,
            totalEarnings = totalEarnings,
            totalCashCollection = totalCash,
            totalTrips = totalTrips,
            searchQuery = query,
            selectedFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OfficeRegisterUiState()
    )

    fun setManagerFilter(managerId: String?) {
        _managerFilter.value = managerId
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }

    fun addNewDutyLog(
        shiftType: SvmCommissionCalculator.ShiftType,
        loginDate: String,
        loginTime: String,
        logoutDate: String,
        logoutTime: String,
        vehicleNo: String,
        driverName: String,
        driverPhone: String,
        trips: Int,
        earnings: Double,
        cashCollection: Double,
        expenditure: Double,
        drivePass: Double,
        advance: Double,
        paymentCash: Double,
        paymentOnline: Double,
        remarks: String
    ) {
        viewModelScope.launch {
            val calc = SvmCommissionCalculator.calculate(
                shiftType = shiftType,
                trips = trips,
                grossEarnings = earnings,
                cashCollection = cashCollection,
                expenditure = expenditure,
                drivePass = drivePass,
                advance = advance
            )

            val entity = DutyLogEntity(
                fleetId = "svm_fleet_hyderabad",
                shiftType = shiftType.name,
                loginDate = loginDate,
                loginTime = loginTime,
                logoutDate = logoutDate,
                logoutTime = logoutTime,
                vehicleNo = vehicleNo,
                driverName = driverName,
                driverPhone = driverPhone.ifBlank { null },
                trips = trips,
                earnings = earnings,
                cashCollection = cashCollection,
                expenditure = expenditure,
                commissionRent = calc.commissionAmount,
                drivePass = drivePass,
                advance = advance,
                paymentCash = paymentCash,
                paymentOnline = paymentOnline,
                netBalance = calc.netBalance,
                status = if (paymentCash > 0 || paymentOnline > 0 || calc.netBalance == 0.0) "SETTLED" else "OPEN",
                remarks = remarks.ifBlank { calc.remarks },
                managerId = _managerFilter.value ?: "mgr_moosapet_01",
                loggedByRole = "MANAGER"
            )

            repository.saveDutyLog(entity)
        }
    }

    fun deleteDutyLog(id: Long) {
        viewModelScope.launch {
            repository.deleteDutyLog(id)
        }
    }
}

