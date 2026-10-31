package com.fleettrack.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fleettrack.app.data.local.dao.AlertDao
import com.fleettrack.app.data.local.dao.DeviceDao
import com.fleettrack.app.data.local.dao.DriverDao
import com.fleettrack.app.data.local.dao.RideDao
import com.fleettrack.app.data.local.dao.RideEventDao
import com.fleettrack.app.data.local.dao.VehicleDao
import com.fleettrack.app.data.local.entity.AlertEntity
import com.fleettrack.app.data.local.entity.DeviceEntity
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.RideEventEntity
import com.fleettrack.app.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        DriverEntity::class,
        DeviceEntity::class,
        RideEntity::class,
        RideEventEntity::class,
        AlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FleetDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun driverDao(): DriverDao
    abstract fun deviceDao(): DeviceDao
    abstract fun rideDao(): RideDao
    abstract fun rideEventDao(): RideEventDao
    abstract fun alertDao(): AlertDao
}

