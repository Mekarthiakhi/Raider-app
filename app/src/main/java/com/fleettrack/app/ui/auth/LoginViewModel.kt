package com.fleettrack.app.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleettrack.app.data.local.FleetDataSeeder
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.repository.DriverRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val sessionPrefs = context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE)

    // Authorized Fleet Owner Numbers (Moosapet Hyderabad Office & Demo Test Numbers)
    val defaultOwnerNumbers = listOf("9391915013", "9959859347", "9876543210", "9999999999", "1111111111", "0000000000")

    fun getOwnerSecretKey(): String {
        return sessionPrefs.getString("owner_secret_key", "7777") ?: "7777"
    }

    fun setOwnerSecretKey(newKey: String) {
        sessionPrefs.edit().putString("owner_secret_key", newKey.trim()).apply()
    }

    fun getCustomOwnerNumbers(): Set<String> {
        return sessionPrefs.getStringSet("owner_custom_numbers", emptySet()) ?: emptySet()
    }

    fun addCustomOwnerNumber(phone: String) {
        val clean = phone.filter { it.isDigit() }.takeLast(10)
        if (clean.length == 10) {
            val current = getCustomOwnerNumbers().toMutableSet()
            current.add(clean)
            sessionPrefs.edit().putStringSet("owner_custom_numbers", current).apply()
        }
    }

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

    fun evaluatePhone(phoneInput: String, role: LoginRole = LoginRole.DRIVER, secretKeyInput: String = ""): AuthStatus {
        val clean = phoneInput.filter { it.isDigit() }
        if (clean.length < 10) return AuthStatus.IncompletePhone
        val last10 = clean.takeLast(10)

        val secretKey = getOwnerSecretKey()
        val customNumbers = getCustomOwnerNumbers()

        if (role == LoginRole.OWNER) {
            // 1. Secret Key Match: Allows Owner access from ANY mobile number
            if (secretKeyInput.isNotBlank() && secretKeyInput.trim() == secretKey) {
                addCustomOwnerNumber(last10)
                return AuthStatus.OwnerAuthorized(last10)
            }

            // 2. Pre-authorized Owner Numbers (Akhilesh & Office) or previously verified custom numbers
            if (defaultOwnerNumbers.any { it.endsWith(last10) } || customNumbers.any { it.endsWith(last10) }) {
                return AuthStatus.OwnerAuthorized(last10)
            }
            return AuthStatus.Unauthorized(last10)
        } else {
            // 1. If an authorized owner is logging in on Driver Tab, allow them to test Driver Mode!
            if (defaultOwnerNumbers.any { it.endsWith(last10) } || customNumbers.any { it.endsWith(last10) }) {
                val seedOwnerDriver = FleetDataSeeder.driverList.firstOrNull { pair ->
                    val p = pair.second.filter { it.isDigit() }
                    p.endsWith(last10) || last10.endsWith(p.takeLast(10))
                }
                val driverName = seedOwnerDriver?.first ?: "Fleet Owner (Driver Mode)"
                return AuthStatus.DriverAuthorized(
                    driverName = driverName,
                    phone = last10,
                    driverId = 1L
                )
            }

            // 2. Check Database Drivers (created by Owner via DriversScreen or Seed)
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

            // 3. Fallback check on seeded drivers list
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

