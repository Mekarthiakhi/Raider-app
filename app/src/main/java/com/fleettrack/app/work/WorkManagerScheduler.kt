package com.fleettrack.app.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    fun startPeriodicWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val connectedConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 1. Outbox Sync Worker (every 15 min when connected)
        val outboxRequest = PeriodicWorkRequestBuilder<OutboxSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(connectedConstraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            OutboxSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            outboxRequest
        )

        // 2. Device Heartbeat Worker (every 15 min)
        val heartbeatRequest = PeriodicWorkRequestBuilder<DeviceHeartbeatWorker>(15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DeviceHeartbeatWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            heartbeatRequest
        )

        Timber.i("WorkManager background workers scheduled (Outbox Sync + Heartbeat)")
    }

    fun triggerImmediateOutboxSync(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val connectedConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateRequest = OneTimeWorkRequestBuilder<OutboxSyncWorker>()
            .setConstraints(connectedConstraints)
            .build()

        workManager.enqueueUniqueWork(
            "immediate_outbox_sync",
            ExistingWorkPolicy.REPLACE,
            immediateRequest
        )
        Timber.d("Triggered immediate outbox sync")
    }
}
