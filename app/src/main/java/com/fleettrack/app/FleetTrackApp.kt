package com.fleettrack.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.fleettrack.app.service.parser.DynamicSelectorEngine
import com.fleettrack.app.work.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class FleetTrackApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dynamicSelectorEngine: DynamicSelectorEngine

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("FleetTrackApp initialized")

        // Start background telemetry & outbox workers
        WorkManagerScheduler.startPeriodicWorkers(this)

        // Pre-fetch active remote selectors from Supabase OTA config
        dynamicSelectorEngine.syncSelectorsAsync()
    }
}
