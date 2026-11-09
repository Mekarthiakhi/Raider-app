package com.fleettrack.app.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fleettrack.app.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class ShiftSession(
    val isShiftActive: Boolean = false,
    val driverId: Long? = null,
    val driverName: String? = null,
    val vehicleId: Long? = null,
    val vehicleRegNo: String? = null,
    val vehicleNickname: String? = null,
    val shiftStartTime: Instant? = null
)

data class ManagerSession(
    val managerId: String? = null,
    val managerName: String? = null,
    val hubName: String? = null
)

@Singleton
class ShiftManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_SHIFT_ACTIVE = booleanPreferencesKey("shift_active")
        val KEY_DRIVER_ID = longPreferencesKey("shift_driver_id")
        val KEY_DRIVER_NAME = stringPreferencesKey("shift_driver_name")
        val KEY_VEHICLE_ID = longPreferencesKey("shift_vehicle_id")
        val KEY_VEHICLE_REG_NO = stringPreferencesKey("shift_vehicle_reg_no")
        val KEY_VEHICLE_NICKNAME = stringPreferencesKey("shift_vehicle_nickname")
        val KEY_SHIFT_START_TIME = longPreferencesKey("shift_start_time")

        // 3-Role Hierarchy Session Keys
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_MANAGER_ID = stringPreferencesKey("logged_in_manager_id")
        val KEY_MANAGER_NAME = stringPreferencesKey("logged_in_manager_name")
        val KEY_HUB_NAME = stringPreferencesKey("logged_in_hub_name")
    }

    val userRole: Flow<UserRole> = dataStore.data.map { prefs ->
        val roleStr = prefs[KEY_USER_ROLE] ?: UserRole.OWNER.name
        try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.OWNER
        }
    }

    val currentManager: Flow<ManagerSession> = dataStore.data.map { prefs ->
        ManagerSession(
            managerId = prefs[KEY_MANAGER_ID],
            managerName = prefs[KEY_MANAGER_NAME],
            hubName = prefs[KEY_HUB_NAME]
        )
    }

    suspend fun setUserRole(role: UserRole) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ROLE] = role.name
        }
    }

    suspend fun setManagerSession(managerId: String, managerName: String, hubName: String) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ROLE] = UserRole.MANAGER.name
            prefs[KEY_MANAGER_ID] = managerId
            prefs[KEY_MANAGER_NAME] = managerName
            prefs[KEY_HUB_NAME] = hubName
        }
    }

    suspend fun clearManagerSession() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_MANAGER_ID)
            prefs.remove(KEY_MANAGER_NAME)
            prefs.remove(KEY_HUB_NAME)
        }
    }

    val currentShift: Flow<ShiftSession> = dataStore.data.map { prefs ->
        val isActive = prefs[KEY_SHIFT_ACTIVE] ?: false
        val startTimeMillis = prefs[KEY_SHIFT_START_TIME]
        val startInstant = startTimeMillis?.let { Instant.fromEpochMilliseconds(it) }

        ShiftSession(
            isShiftActive = isActive,
            driverId = prefs[KEY_DRIVER_ID],
            driverName = prefs[KEY_DRIVER_NAME],
            vehicleId = prefs[KEY_VEHICLE_ID],
            vehicleRegNo = prefs[KEY_VEHICLE_REG_NO],
            vehicleNickname = prefs[KEY_VEHICLE_NICKNAME],
            shiftStartTime = startInstant
        )
    }

    suspend fun startShift(
        driverId: Long,
        driverName: String,
        vehicleId: Long,
        vehicleRegNo: String,
        vehicleNickname: String?
    ) {
        val nowMillis = Clock.System.now().toEpochMilliseconds()
        dataStore.edit { prefs ->
            prefs[KEY_SHIFT_ACTIVE] = true
            prefs[KEY_DRIVER_ID] = driverId
            prefs[KEY_DRIVER_NAME] = driverName
            prefs[KEY_VEHICLE_ID] = vehicleId
            prefs[KEY_VEHICLE_REG_NO] = vehicleRegNo
            prefs[KEY_VEHICLE_NICKNAME] = vehicleNickname ?: ""
            prefs[KEY_SHIFT_START_TIME] = nowMillis
        }
    }

    suspend fun endShift() {
        dataStore.edit { prefs ->
            prefs[KEY_SHIFT_ACTIVE] = false
            prefs.remove(KEY_DRIVER_ID)
            prefs.remove(KEY_DRIVER_NAME)
            prefs.remove(KEY_VEHICLE_ID)
            prefs.remove(KEY_VEHICLE_REG_NO)
            prefs.remove(KEY_VEHICLE_NICKNAME)
            prefs.remove(KEY_SHIFT_START_TIME)
        }
    }
}

