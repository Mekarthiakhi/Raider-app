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
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

enum class ReportPeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class WeeklyPnLStatement(
    val weekId: String,
    val weekLabel: String,
    val dateRange: String,
    val uberGross: Double,
    val rapidoGross: Double,
    val totalGrossRevenue: Double,
    val driverPayouts: Double,
    val fuelExpenses: Double,
    val maintenanceExpenses: Double,
    val otherTolls: Double,
    val totalExpenses: Double,
    val netProfitOrLoss: Double,
    val isProfit: Boolean,
    val profitMarginPercent: Double,
    val topProfitableCar: String,
    val lossOrMaintenanceCar: String
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val rideRepository: RideRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.WEEKLY)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _selectedWeekIndex = MutableStateFlow(0) // 0 = Current Week
    val selectedWeekIndex: StateFlow<Int> = _selectedWeekIndex.asStateFlow()

    private val now = Clock.System.now()

    val dailySummary: StateFlow<RideSummary> = rideRepository.getTodaySummary(now - 1.days)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RideSummary())

    val weeklySummary: StateFlow<RideSummary> = rideRepository.getTodaySummary(now - 7.days)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RideSummary())

    val monthlySummary: StateFlow<RideSummary> = rideRepository.getTodaySummary(now - 30.days)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RideSummary())

    val weeklyPnLStatements: List<WeeklyPnLStatement> = listOf(
        WeeklyPnLStatement(
            weekId = "w4",
            weekLabel = "Week 4 (Current)",
            dateRange = "29 Oct - 04 Nov",
            uberGross = 76420.0,
            rapidoGross = 52030.0,
            totalGrossRevenue = 128450.0,
            driverPayouts = 64225.0, // 50% payout
            fuelExpenses = 28400.0,
            maintenanceExpenses = 8500.0,
            otherTolls = 3200.0,
            totalExpenses = 104325.0,
            netProfitOrLoss = 24125.0,
            isProfit = true,
            profitMarginPercent = 18.8,
            topProfitableCar = "TS09 AB 1234 (+₹8,420)",
            lossOrMaintenanceCar = "TS09 GH 3456 (-₹2,100 Service)"
        ),
        WeeklyPnLStatement(
            weekId = "w3",
            weekLabel = "Week 3",
            dateRange = "22 Oct - 28 Oct",
            uberGross = 84120.0,
            rapidoGross = 58240.0,
            totalGrossRevenue = 142360.0,
            driverPayouts = 71180.0,
            fuelExpenses = 31200.0,
            maintenanceExpenses = 12400.0,
            otherTolls = 3500.0,
            totalExpenses = 118280.0,
            netProfitOrLoss = 24080.0,
            isProfit = true,
            profitMarginPercent = 16.9,
            topProfitableCar = "TS09 CD 5678 (+₹9,120)",
            lossOrMaintenanceCar = "TS09 EF 9012 (-₹3,400 Brake)"
        ),
        WeeklyPnLStatement(
            weekId = "w2",
            weekLabel = "Week 2",
            dateRange = "15 Oct - 21 Oct",
            uberGross = 68400.0,
            rapidoGross = 44200.0,
            totalGrossRevenue = 112600.0,
            driverPayouts = 56300.0,
            fuelExpenses = 36800.0,
            maintenanceExpenses = 24500.0, // High repairs week
            otherTolls = 4100.0,
            totalExpenses = 121700.0,
            netProfitOrLoss = -9100.0, // Net Loss due to major engine service
            isProfit = false,
            profitMarginPercent = -8.1,
            topProfitableCar = "TS09 AB 1234 (+₹6,400)",
            lossOrMaintenanceCar = "TS09 IJ 7890 (-₹18,500 Engine overhaul)"
        ),
        WeeklyPnLStatement(
            weekId = "w1",
            weekLabel = "Week 1",
            dateRange = "08 Oct - 14 Oct",
            uberGross = 79800.0,
            rapidoGross = 54600.0,
            totalGrossRevenue = 134400.0,
            driverPayouts = 67200.0,
            fuelExpenses = 29400.0,
            maintenanceExpenses = 6200.0,
            otherTolls = 2800.0,
            totalExpenses = 105600.0,
            netProfitOrLoss = 28800.0,
            isProfit = true,
            profitMarginPercent = 21.4,
            topProfitableCar = "TS09 CD 5678 (+₹10,200)",
            lossOrMaintenanceCar = "TS09 GH 3456 (Idle 2 days)"
        )
    )

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun selectWeek(index: Int) {
        _selectedWeekIndex.value = index
    }
}
