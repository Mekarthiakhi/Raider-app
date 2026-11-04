package com.fleettrack.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.service.parser.DynamicSelectorEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class OutboxSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val rideRepository: RideRepository,
    private val dynamicSelectorEngine: DynamicSelectorEngine
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting OutboxSyncWorker...")

            // Refresh remote selectors from Supabase OTA
            try {
                dynamicSelectorEngine.syncSelectors()
            } catch (e: Exception) {
                Timber.w(e, "Minor warning: Failed to refresh remote selectors during outbox sync")
            }

            val syncResult = rideRepository.syncPendingRides()

            if (syncResult.isSuccess) {
                val syncedCount = syncResult.getOrDefault(0)
                Timber.i("OutboxSyncWorker completed successfully: %d rides uploaded", syncedCount)
                Result.success()
            } else {
                Timber.w(syncResult.exceptionOrNull(), "OutboxSyncWorker failed, retrying later")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during OutboxSyncWorker")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "outbox_sync_worker"
    }
}
