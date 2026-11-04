package com.fleettrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Matches the SVM FLEET Office Duty Register / Log Sheet columns:
 * LOG IN (DATE, TIME), LOG OUT (DATE, TIME), VEHICLE NO, DRIVER NAME,
 * TRIPS, EARNING'S, CASH COLLECTION, EXPENDITURE, COMMISSION RENT,
 * DRIVE PASS, ADVANCE, PAYMENT (CASH, ONLINE), REMARKS
 */
@Entity(tableName = "duty_logs")
data class DutyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "fleet_id")
    val fleetId: String = "svm_fleet_hyderabad",

    @ColumnInfo(name = "shift_type")
    val shiftType: String = "12_HOURS", // "12_HOURS" or "24_HOURS"

    // 1. Log In
    @ColumnInfo(name = "login_date")
    val loginDate: String, // e.g. "01/11/2026"

    @ColumnInfo(name = "login_time")
    val loginTime: String, // e.g. "06:00 AM"

    // 2. Log Out
    @ColumnInfo(name = "logout_date")
    val logoutDate: String, // e.g. "01/11/2026"

    @ColumnInfo(name = "logout_time")
    val logoutTime: String, // e.g. "08:15 PM"

    // 3. Vehicle & Driver
    @ColumnInfo(name = "vehicle_no")
    val vehicleNo: String, // e.g. "3368" or "TS09 UB 3368"

    @ColumnInfo(name = "driver_name")
    val driverName: String, // e.g. "Suman", "Narendra", "Pavan", "Siva"

    @ColumnInfo(name = "driver_phone")
    val driverPhone: String? = null,

    // 4. Trips & Earnings
    @ColumnInfo(name = "trips")
    val trips: Int = 0,

    @ColumnInfo(name = "earnings")
    val earnings: Double = 0.0,

    // 5. Office Register Financial Columns
    @ColumnInfo(name = "cash_collection")
    val cashCollection: Double = 0.0,

    @ColumnInfo(name = "expenditure")
    val expenditure: Double = 0.0, // Petrol / CNG / maintenance / toll

    @ColumnInfo(name = "commission_rent")
    val commissionRent: Double = 0.0, // Calculated driver commission or car daily rent

    @ColumnInfo(name = "drive_pass")
    val drivePass: Double = 0.0,

    @ColumnInfo(name = "advance")
    val advance: Double = 0.0,

    // 6. Payments
    @ColumnInfo(name = "payment_cash")
    val paymentCash: Double = 0.0,

    @ColumnInfo(name = "payment_online")
    val paymentOnline: Double = 0.0, // UPI / GPay / NetBanking

    // 7. Settlement & Remarks
    @ColumnInfo(name = "net_balance")
    val netBalance: Double = 0.0,

    @ColumnInfo(name = "status")
    val status: String = "SETTLED", // "OPEN", "SETTLED", "PENDING_CASH"

    @ColumnInfo(name = "remarks")
    val remarks: String? = null,

    @ColumnInfo(name = "supabase_id")
    val supabaseId: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now()
)
