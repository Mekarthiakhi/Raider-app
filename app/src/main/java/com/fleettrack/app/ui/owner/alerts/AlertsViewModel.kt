package com.fleettrack.app.ui.owner.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.dao.AlertDao
import com.fleettrack.app.data.local.entity.AlertEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertDao: AlertDao
) : ViewModel() {

    val unreadAlerts: StateFlow<List<AlertEntity>> = alertDao.getUnreadAlerts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            val alerts = alertDao.getUnreadAlerts().first()
            if (alerts.isEmpty()) {
                seedInitialAlerts()
            }
        }
    }

    private suspend fun seedInitialAlerts() {
        val now = Clock.System.now()
        val defaultAlerts = listOf(
            AlertEntity(
                fleetId = "fleet_blr_central",
                vehicleId = 1L,
                alertType = "SERVICE",
                message = "Service Due: TS09 AB 1234 in 3 days",
                createdAt = now - 15.minutes,
                isRead = false
            ),
            AlertEntity(
                fleetId = "fleet_blr_central",
                vehicleId = 3L,
                alertType = "EXPIRY",
                message = "Insurance Expiry: TS09 EF 9012 in 18 days",
                createdAt = now - 2.hours,
                isRead = false
            ),
            AlertEntity(
                fleetId = "fleet_blr_central",
                vehicleId = 2L,
                alertType = "ACTIVITY",
                message = "High Fuel Expense: TS09 CD 5678 • ₹5,400 today",
                createdAt = now - 4.hours,
                isRead = false
            ),
            AlertEntity(
                fleetId = "fleet_blr_central",
                vehicleId = null,
                alertType = "ACTIVITY",
                message = "Earnings Update: Today's gross reached ₹18,420",
                createdAt = now - 6.hours,
                isRead = false
            ),
            AlertEntity(
                fleetId = "fleet_blr_central",
                vehicleId = 4L,
                alertType = "EXPIRY",
                message = "PUC Expiry: TS09 GH 3456 in 25 days",
                createdAt = now - 1.days,
                isRead = false
            )
        )
        alertDao.insertAll(defaultAlerts)
    }

    fun markRead(id: Long) {
        viewModelScope.launch {
            alertDao.markAsRead(id)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            alertDao.markAllAsRead()
        }
    }
}
