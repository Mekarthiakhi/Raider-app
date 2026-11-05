package com.fleettrack.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.FleetDataSeeder
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.repository.DriverRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginRole {
    DRIVER,
    OWNER
}

sealed class AuthStatus {
    object Idle : AuthStatus()
    object IncompletePhone : AuthStatus()
    data class OwnerAuthorized(val phone: String) : AuthStatus()
    data class DriverAuthorized(val driverName: String, val phone: String, val driverId: Long) : AuthStatus()
    data class Unauthorized(val phone: String) : AuthStatus()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val driverRepository: DriverRepository
) : ViewModel() {

    // Authorized Fleet Owner Numbers (Moosapet Hyderabad Office & Demo Test Numbers)
    val authorizedOwnerNumbers = listOf("9391915013", "9959859347", "9876543210", "9999999999", "1111111111", "0000000000")

    val registeredDrivers: StateFlow<List<DriverEntity>> = driverRepository.getAllDrivers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    init {
        // Automatically sync latest registered drivers from Supabase cloud database
        viewModelScope.launch {
            try {
                driverRepository.syncDriversFromSupabase()
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            try {
                driverRepository.syncDriversFromSupabase()
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }

    fun evaluatePhone(phoneInput: String, role: LoginRole = LoginRole.DRIVER): AuthStatus {
        val clean = phoneInput.filter { it.isDigit() }
        if (clean.length < 10) return AuthStatus.IncompletePhone
        val last10 = clean.takeLast(10)

        if (role == LoginRole.OWNER) {
            // Check Owner Numbers
            if (authorizedOwnerNumbers.any { it.endsWith(last10) }) {
                return AuthStatus.OwnerAuthorized(last10)
            }
            return AuthStatus.Unauthorized(last10)
        } else {
            // Check Driver Numbers (Both Database and Seeded Drivers)
            val inDb = registeredDrivers.value.firstOrNull { d ->
                val p = d.phone.filter { it.isDigit() }
                p.endsWith(last10) || last10.endsWith(p.takeLast(10))
            }
            if (inDb != null) {
                return AuthStatus.DriverAuthorized(
                    driverName = inDb.name,
                    phone = last10,
                    driverId = inDb.id
                )
            }

            // Fallback check on seeded drivers list
            val seedIndex = FleetDataSeeder.driverList.indexOfFirst { pair ->
                val p = pair.second.filter { it.isDigit() }
                p.endsWith(last10) || last10.endsWith(p.takeLast(10))
            }
            if (seedIndex >= 0) {
                val pair = FleetDataSeeder.driverList[seedIndex]
                return AuthStatus.DriverAuthorized(
                    driverName = pair.first,
                    phone = last10,
                    driverId = (seedIndex + 1).toLong()
                )
            }

            return AuthStatus.Unauthorized(last10)
        }
    }
}

