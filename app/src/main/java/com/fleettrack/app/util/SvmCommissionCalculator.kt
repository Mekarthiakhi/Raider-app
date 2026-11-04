package com.fleettrack.app.util

/**
 * SVM FLEET Driver Commission Calculator
 * Moosapet, Hyderabad - 500018
 * Contacts: 9391915013, 9959859347
 *
 * Implements the official commission base tiers:
 * - 12 Hrs Shift: Minimum 10 Trips required
 * - 24 Hrs Shift: Minimum 18 Trips required
 */
object SvmCommissionCalculator {

    enum class ShiftType(val label: String, val minTrips: Int) {
        SHIFT_12_HOURS("12 Hrs Shift", 10),
        SHIFT_24_HOURS("24 Hrs Shift", 18)
    }

    data class CommissionResult(
        val shiftType: ShiftType,
        val trips: Int,
        val grossEarnings: Double,
        val minTripsMet: Boolean,
        val commissionPercentage: Double,
        val commissionAmount: Double,
        val cashCollection: Double,
        val expenditure: Double,
        val drivePass: Double,
        val advance: Double,
        val netBalance: Double, // Positive = Company pays driver; Negative = Driver owes company
        val remarks: String
    )

    /**
     * Calculate commission based on official SVM FLEET rules
     */
    fun calculate(
        shiftType: ShiftType,
        trips: Int,
        grossEarnings: Double,
        cashCollection: Double = 0.0,
        expenditure: Double = 0.0,
        drivePass: Double = 0.0,
        advance: Double = 0.0
    ): CommissionResult {
        val minTripsMet = trips >= shiftType.minTrips

        val percentage = if (!minTripsMet) {
            0.0
        } else {
            when (shiftType) {
                ShiftType.SHIFT_12_HOURS -> {
                    when {
                        grossEarnings >= 3500.0 -> 40.0
                        grossEarnings >= 2500.0 -> 35.0
                        grossEarnings >= 2000.0 -> 30.0
                        grossEarnings >= 1500.0 -> 25.0
                        else -> 0.0
                    }
                }
                ShiftType.SHIFT_24_HOURS -> {
                    when {
                        grossEarnings >= 6500.0 -> 40.0
                        grossEarnings >= 5000.0 -> 35.0
                        grossEarnings >= 4000.0 -> 30.0
                        grossEarnings >= 2500.0 -> 25.0
                        else -> 0.0
                    }
                }
            }
        }

        val commissionAmount = (grossEarnings * (percentage / 100.0))

        // Net balance formula:
        // Driver is entitled to: Commission + Expenditure (petrol/toll refund)
        // Driver must deduct: Cash collected from riders + Advance taken + Drive pass fee
        // Net Balance = (Commission + Expenditure) - (CashCollection + Advance + DrivePass)
        val driverCredit = commissionAmount + expenditure
        val driverDebit = cashCollection + advance + drivePass
        val netBalance = driverCredit - driverDebit

        val remarks = buildString {
            if (!minTripsMet) {
                append("Trips ($trips) below minimum required (${shiftType.minTrips}). ")
            }
            if (percentage > 0.0) {
                append("${percentage.toInt()}% tier earned (₹${commissionAmount.toInt()}). ")
            } else {
                append("No commission applicable. ")
            }
            if (netBalance >= 0) {
                append("Company to pay Driver: ₹${netBalance.toInt()}")
            } else {
                append("Driver to deposit to Office: ₹${(-netBalance).toInt()}")
            }
        }

        return CommissionResult(
            shiftType = shiftType,
            trips = trips,
            grossEarnings = grossEarnings,
            minTripsMet = minTripsMet,
            commissionPercentage = percentage,
            commissionAmount = commissionAmount,
            cashCollection = cashCollection,
            expenditure = expenditure,
            drivePass = drivePass,
            advance = advance,
            netBalance = netBalance,
            remarks = remarks
        )
    }
}
