package com.fleettrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fleettrack.app.data.local.entity.ManagerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ManagerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManager(manager: ManagerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManagers(managers: List<ManagerEntity>)

    @Update
    suspend fun updateManager(manager: ManagerEntity)

    @Delete
    suspend fun deleteManager(manager: ManagerEntity)

    @Query("DELETE FROM managers WHERE id = :managerId")
    suspend fun deleteManagerById(managerId: String)

    @Query("SELECT * FROM managers ORDER BY name ASC")
    fun getAllManagers(): Flow<List<ManagerEntity>>

    @Query("SELECT * FROM managers WHERE is_active = 1 ORDER BY name ASC")
    fun getActiveManagers(): Flow<List<ManagerEntity>>

    @Query("SELECT * FROM managers WHERE id = :id LIMIT 1")
    fun getManagerById(id: String): Flow<ManagerEntity?>

    @Query("SELECT * FROM managers WHERE id = :id LIMIT 1")
    suspend fun findManagerById(id: String): ManagerEntity?

    @Query("SELECT * FROM managers WHERE phone = :phone AND is_active = 1 LIMIT 1")
    suspend fun findManagerByPhone(phone: String): ManagerEntity?

    @Query("SELECT COUNT(*) FROM managers")
    suspend fun getManagerCount(): Int
}

