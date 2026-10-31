package com.fleettrack.app.ui.owner.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.dao.AlertDao
import com.fleettrack.app.data.local.entity.AlertEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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

