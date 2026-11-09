/**
 * SVM FLEET Driver Commission Calculator (Web Edition)
 * Moosapet, Hyderabad - 500018
 * Contacts: 9391915013, 9959859347
 *
 * Implements the official commission base tiers:
 * - 12 Hrs Shift: Minimum 10 Trips required
 * - 24 Hrs Shift: Minimum 18 Trips required
 */

const SvmCommissionCalculator = {
    SHIFT_TYPES: {
        SHIFT_12_HOURS: { label: "12 Hrs Shift", minTrips: 10, value: "12_HOURS" },
        SHIFT_24_HOURS: { label: "24 Hrs Shift", minTrips: 18, value: "24_HOURS" }
    },

    /**
     * Calculate commission and net balance based on SVM FLEET Moosapet rules.
     */
    calculate: function (shiftTypeKey, trips, grossEarnings, cashCollection = 0, expenditure = 0, drivePass = 0, advance = 0) {
        trips = parseInt(trips) || 0;
        grossEarnings = parseFloat(grossEarnings) || 0.0;
        cashCollection = parseFloat(cashCollection) || 0.0;
        expenditure = parseFloat(expenditure) || 0.0;
        drivePass = parseFloat(drivePass) || 0.0;
        advance = parseFloat(advance) || 0.0;

        const is12H = shiftTypeKey === "12_HOURS" || shiftTypeKey === "SHIFT_12_HOURS";
        const minTrips = is12H ? 10 : 18;
        const shiftLabel = is12H ? "12 Hrs Shift" : "24 Hrs Shift";
        const minTripsMet = trips >= minTrips;

        let percentage = 0.0;

        if (minTripsMet) {
            if (is12H) {
                if (grossEarnings >= 3500.0) percentage = 40.0;
                else if (grossEarnings >= 2500.0) percentage = 35.0;
                else if (grossEarnings >= 2000.0) percentage = 30.0;
                else if (grossEarnings >= 1500.0) percentage = 25.0;
                else percentage = 0.0;
            } else {
                if (grossEarnings >= 6500.0) percentage = 40.0;
                else if (grossEarnings >= 5000.0) percentage = 35.0;
                else if (grossEarnings >= 4000.0) percentage = 30.0;
                else if (grossEarnings >= 2500.0) percentage = 25.0;
                else percentage = 0.0;
            }
        }

        const commissionAmount = Math.round((grossEarnings * (percentage / 100.0)) * 100) / 100;

        // Net balance formula:
        // Driver is entitled to: Commission + Expenditure (fuel, repairs, tolls)
        // Driver must deduct: Cash collected from riders + Advance taken + Drive pass fee
        // Net Balance = (Commission + Expenditure) - (CashCollection + Advance + DrivePass)
        const driverCredit = commissionAmount + expenditure;
        const driverDebit = cashCollection + advance + drivePass;
        const netBalance = Math.round((driverCredit - driverDebit) * 100) / 100;

        let remarks = "";
        if (!minTripsMet) {
            remarks += `Trips (${trips}) below minimum required (${minTrips}). `;
        }
        if (percentage > 0) {
            remarks += `${percentage}% tier earned (₹${Math.round(commissionAmount)}). `;
        } else {
            remarks += "No commission applicable. ";
        }

        if (netBalance >= 0) {
            remarks += `Company to pay Driver: ₹${Math.round(netBalance)}`;
        } else {
            remarks += `Driver to deposit to Office: ₹${Math.round(Math.abs(netBalance))}`;
        }

        return {
            shiftType: shiftTypeKey,
            shiftLabel,
            trips,
            grossEarnings,
            minTripsMet,
            commissionPercentage: percentage,
            commissionAmount,
            cashCollection,
            expenditure,
            drivePass,
            advance,
            driverCredit,
            driverDebit,
            netBalance,
            remarks
        };
    },

    /**
     * Currency formatter for INR
     */
    formatINR: function (amount) {
        const val = Math.round(Number(amount) || 0);
        return "₹" + val.toLocaleString('en-IN');
    }
};

window.SvmCommissionCalculator = SvmCommissionCalculator;
