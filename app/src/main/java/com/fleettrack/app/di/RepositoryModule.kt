package com.fleettrack.app.di

import com.fleettrack.app.data.repository.DeviceRepository
import com.fleettrack.app.data.repository.DeviceRepositoryImpl
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.DriverRepositoryImpl
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.RideRepositoryImpl
import com.fleettrack.app.data.repository.VehicleRepository
import com.fleettrack.app.data.repository.VehicleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRideRepository(
        impl: RideRepositoryImpl
    ): RideRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        impl: VehicleRepositoryImpl
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindDriverRepository(
        impl: DriverRepositoryImpl
    ): DriverRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        impl: DeviceRepositoryImpl
    ): DeviceRepository
}
