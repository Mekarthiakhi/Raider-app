package com.fleettrack.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.fleettrack.app.data.local.FleetDatabase
import com.fleettrack.app.data.local.dao.AlertDao
import com.fleettrack.app.data.local.dao.DeviceDao
import com.fleettrack.app.data.local.dao.DriverDao
import com.fleettrack.app.data.local.dao.RideDao
import com.fleettrack.app.data.local.dao.RideEventDao
import com.fleettrack.app.data.local.dao.VehicleDao
import com.fleettrack.app.data.remote.DeviceRemoteDataSource
import com.fleettrack.app.data.remote.DeviceRemoteDataSourceImpl
import com.fleettrack.app.data.remote.RemoteSelectorDataSource
import com.fleettrack.app.data.remote.RemoteSelectorDataSourceImpl
import com.fleettrack.app.data.remote.RideRemoteDataSource
import com.fleettrack.app.data.remote.RideRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fleet_track_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFleetDatabase(
        @ApplicationContext context: Context
    ): FleetDatabase {
        return Room.databaseBuilder(
            context,
            FleetDatabase::class.java,
            "fleet_track_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    fun provideVehicleDao(db: FleetDatabase): VehicleDao = db.vehicleDao()

    @Provides
    fun provideDriverDao(db: FleetDatabase): DriverDao = db.driverDao()

    @Provides
    fun provideRideDao(db: FleetDatabase): RideDao = db.rideDao()

    @Provides
    fun provideRideEventDao(db: FleetDatabase): RideEventDao = db.rideEventDao()

    @Provides
    fun provideDeviceDao(db: FleetDatabase): DeviceDao = db.deviceDao()

    @Provides
    fun provideAlertDao(db: FleetDatabase): AlertDao = db.alertDao()

    @Provides
    fun provideDutyLogDao(db: FleetDatabase): com.fleettrack.app.data.local.dao.DutyLogDao = db.dutyLogDao()

    @Provides
    fun provideManagerDao(db: FleetDatabase): com.fleettrack.app.data.local.dao.ManagerDao = db.managerDao()

    @Provides
    @Singleton
    fun provideManagerRemoteDataSource(impl: com.fleettrack.app.data.remote.ManagerRemoteDataSourceImpl): com.fleettrack.app.data.remote.ManagerRemoteDataSource = impl

    @Provides
    @Singleton
    fun provideRideRemoteDataSource(impl: RideRemoteDataSourceImpl): RideRemoteDataSource = impl

    @Provides
    @Singleton
    fun provideDeviceRemoteDataSource(impl: DeviceRemoteDataSourceImpl): DeviceRemoteDataSource = impl

    @Provides
    @Singleton
    fun provideRemoteSelectorDataSource(impl: RemoteSelectorDataSourceImpl): RemoteSelectorDataSource = impl

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
