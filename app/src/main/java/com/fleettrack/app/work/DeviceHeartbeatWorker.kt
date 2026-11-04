package com.fleettrack.app.work

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fleettrack.app.data.remote.DeviceRegistrationDto
import com.fleettrack.app.data.remote.DeviceRemoteDataSource
import com.fleettrack.app.data.remote.HeartbeatDto
import com.fleettrack.app.data.repository.DeviceRepository
import com.fleettrack.app.data.session.ShiftManager
import com.fleettrack.app.service.RideTrackerAccessibilityService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import timber.log.Timber
import java.util.UUID

@HiltWorker
class DeviceHeartbeatWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deviceRemoteDataSource: DeviceRemoteDataSource,
    private val deviceRepository: DeviceRepository,
    private val shiftManager: ShiftManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val batteryLevel = getBatteryPercentage(appContext)
            val isA11yRunning = RideTrackerAccessibilityService.isServiceRunning
            val now = Clock.System.now()

            val shift = shiftManager.currentShift.first()
            val vehicleId = shift.vehicleId
            val driverId = shift.driverId

            val deviceToken = getOrCreateDeviceToken(appContext)

            // Register device if not yet in database
            val registrationDto = DeviceRegistrationDto(
                vehicleId = vehicleId?.toString(),
                driverId = driverId?.toString(),
                deviceToken = deviceToken,
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                androidVersion = Build.VERSION.RELEASE,
                appVersion = "1.0.0",
                batteryLevel = batteryLevel,
                a11yEnabled = isA11yRunning
            )
            deviceRemoteDataSource.registerDevice(registrationDto)

            // Send periodic heartbeat
            val heartbeat = HeartbeatDto(
                deviceToken = deviceToken,
                batteryLevel = batteryLevel,
                a11yEnabled = isA11yRunning,
                isOnline = true,
                lastSeen = now.toString()
            )
            deviceRemoteDataSource.sendHeartbeat(heartbeat)

            // Update local Room device record
            deviceRepository.updateHeartbeat(
                token = deviceToken,
                now = now,
                batteryLevel = batteryLevel,
                a11yEnabled = isA11yRunning
            )

            Timber.d("Heartbeat sent: Battery %d%% | A11y: %b", batteryLevel, isA11yRunning)
            Result.success()
        } catch (e: Exception) {
            Timber.w(e, "DeviceHeartbeatWorker failed, retrying")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun getBatteryPercentage(context: Context): Int {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else 100
    }

    private fun getOrCreateDeviceToken(context: Context): String {
        val prefs = context.getSharedPreferences("device_telemetry_prefs", Context.MODE_PRIVATE)
        var token = prefs.getString("telemetry_device_token", null)
        if (token == null) {
            token = "dev_" + UUID.randomUUID().toString().take(16)
            prefs.edit().putString("telemetry_device_token", token).apply()
        }
        return token
    }

    companion object {
        const val WORK_NAME = "device_heartbeat_worker"
    }
}
