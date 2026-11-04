package com.fleettrack.app.data.repository

import com.fleettrack.app.data.local.dao.DutyLogDao
import com.fleettrack.app.data.local.entity.DutyLogEntity
import com.fleettrack.app.data.remote.SupabaseClientProvider
import com.fleettrack.app.util.SvmCommissionCalculator
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SupabaseDutyLogDto(
    val id: String? = null,
    val fleet_id: String,
    val shift_type: String,
    val login_date: String,
    val login_time: String,
    val logout_date: String,
    val logout_time: String,
    val vehicle_no: String,
    val driver_name: String,
    val driver_phone: String? = null,
    val trips: Int,
    val earnings: Double,
    val cash_collection: Double,
    val expenditure: Double,
    val commission_rent: Double,
    val drive_pass: Double,
    val advance: Double,
    val payment_cash: Double,
    val payment_online: Double,
    val net_balance: Double,
    val status: String,
    val remarks: String? = null
)

@Singleton
class DutyLogRepository @Inject constructor(
    private val dutyLogDao: DutyLogDao
) {
    fun getAllDutyLogs(): Flow<List<DutyLogEntity>> = dutyLogDao.getAllDutyLogs()

    fun searchDutyLogs(query: String): Flow<List<DutyLogEntity>> = dutyLogDao.searchDutyLogs(query)

    suspend fun getDutyLogById(id: Long): DutyLogEntity? = dutyLogDao.getDutyLogById(id)

    fun getTotalEarnings(): Flow<Double> = dutyLogDao.getTotalEarnings()

    fun getTotalCashCollection(): Flow<Double> = dutyLogDao.getTotalCashCollection()

    fun getTotalTrips(): Flow<Int> = dutyLogDao.getTotalTrips()

    suspend fun saveDutyLog(entity: DutyLogEntity): Long {
        val id = dutyLogDao.insert(entity)
        // Attempt background push to Supabase if configured
        syncToSupabase(entity.copy(id = id))
        return id
    }

    suspend fun insertAll(list: List<DutyLogEntity>) {
        dutyLogDao.insertAll(list)
    }

    suspend fun updateDutyLog(entity: DutyLogEntity) {
        dutyLogDao.update(entity)
        syncToSupabase(entity)
    }

    suspend fun deleteDutyLog(id: Long) {
        dutyLogDao.deleteById(id)
    }

    private suspend fun syncToSupabase(log: DutyLogEntity) {
        try {
            val dto = SupabaseDutyLogDto(
                fleet_id = log.fleetId,
                shift_type = log.shiftType,
                login_date = log.loginDate,
                login_time = log.loginTime,
                logout_date = log.logoutDate,
                logout_time = log.logoutTime,
                vehicle_no = log.vehicleNo,
                driver_name = log.driverName,
                driver_phone = log.driverPhone,
                trips = log.trips,
                earnings = log.earnings,
                cash_collection = log.cashCollection,
                expenditure = log.expenditure,
                commission_rent = log.commissionRent,
                drive_pass = log.drivePass,
                advance = log.advance,
                payment_cash = log.paymentCash,
                payment_online = log.paymentOnline,
                net_balance = log.netBalance,
                status = log.status,
                remarks = log.remarks
            )
            SupabaseClientProvider.client.from("office_duty_logs").upsert(dto)
            Timber.d("Successfully synced duty log to Supabase: %s", log.driverName)
        } catch (e: Exception) {
            Timber.w(e, "Offline or Supabase sync skipped for duty log")
        }
    }
}
