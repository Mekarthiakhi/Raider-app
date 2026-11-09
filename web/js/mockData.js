/**
 * SVM FLEET Operating System (Client Demo Environment)
 * Dedicated Mock Data Layer - Isolated from Production Supabase
 * Realistic Datasets for 30 Vehicles, 30 Drivers, Shifts, Charts & Telemetry
 */

const DEMO_CONFIG = {
    isDemoMode: true,
    environmentName: "Client Demonstration Sandbox",
    lastUpdated: "Today, 09 Nov 2026",
    defaultCurrency: "INR",
    currencySymbol: "₹"
};

const DEMO_HUBS = [
    {
        id: "mgr_moosapet_01",
        name: "Moosapet Central Hub",
        shortName: "Moosapet",
        managerName: "Srinivas Rao",
        phone: "+91 98490 12345",
        pin: "1234",
        lat: 17.4652,
        lng: 78.4308,
        color: "#6655F5",
        cabsCount: 12
    },
    {
        id: "mgr_kukatpally_02",
        name: "Kukatpally Y-Junction Hub",
        shortName: "Kukatpally",
        managerName: "Venkatesh Naidu",
        phone: "+91 97012 34567",
        pin: "2345",
        lat: 17.4935,
        lng: 78.4012,
        color: "#3185F6",
        cabsCount: 10
    },
    {
        id: "mgr_madhapur_03",
        name: "Madhapur Hitec City Hub",
        shortName: "Madhapur",
        managerName: "Mohammed Farooq",
        phone: "+91 99890 56789",
        pin: "3456",
        lat: 17.4485,
        lng: 78.3752,
        color: "#10B981",
        cabsCount: 8
    }
];

// 30 Commercial Vehicles (18 Active, 7 Idle, 5 Offline)
const DEMO_VEHICLES = [
    // Moosapet (12)
    { id: 1, regNo: "TS09 UB 3368", model: "Hyundai Aura", managerId: "mgr_moosapet_01", driverId: 1, driverName: "Ravi Kumar", status: "ACTIVE", lat: 17.4645, lng: 78.4320, locationName: "Moosapet Metro", platform: "Uber", speed: 38, fuel: 82, tripsToday: 12, todayEarnings: 3000, monthlyEarnings: 84000, monthlyFuel: 14200 },
    { id: 2, regNo: "TS10 EF 7745", model: "Maruti Dzire", managerId: "mgr_moosapet_01", driverId: 2, driverName: "Suman Goud", status: "ACTIVE", lat: 17.4720, lng: 78.4410, locationName: "Kukatpally Y-Junction", platform: "Rapido", speed: 24, fuel: 65, tripsToday: 15, todayEarnings: 3750, monthlyEarnings: 92000, monthlyFuel: 16800 },
    { id: 3, regNo: "TS09 AC 9890", model: "Honda Amaze", managerId: "mgr_moosapet_01", driverId: 3, driverName: "Praveen M.", status: "ACTIVE", lat: 17.4510, lng: 78.4480, locationName: "Bharat Nagar Flyover", platform: "Ola", speed: 42, fuel: 74, tripsToday: 10, todayEarnings: 2400, monthlyEarnings: 76000, monthlyFuel: 13500 },
    { id: 4, regNo: "TS09 UD 3320", model: "Tata Tigor", managerId: "mgr_moosapet_01", driverId: 4, driverName: "Siva Reddy", status: "ACTIVE", lat: 17.4420, lng: 78.4550, locationName: "Ameerpet Circle", platform: "Uber", speed: 30, fuel: 58, tripsToday: 14, todayEarnings: 3320, monthlyEarnings: 88000, monthlyFuel: 15200 },
    { id: 5, regNo: "TS09 UE 2950", model: "Maruti Tour S", managerId: "mgr_moosapet_01", driverId: 5, driverName: "Prasad K.", status: "ACTIVE", lat: 17.4580, lng: 78.4210, locationName: "Sanathnagar Ind Area", platform: "Rapido", speed: 28, fuel: 89, tripsToday: 13, todayEarnings: 2950, monthlyEarnings: 81000, monthlyFuel: 14000 },
    { id: 6, regNo: "TS09 UF 1840", model: "Hyundai Grand i10", managerId: "mgr_moosapet_01", driverId: 6, driverName: "Baleru N.", status: "IDLE", lat: 17.4490, lng: 78.4680, locationName: "Begumpet Airport Rd", platform: "Uber", speed: 0, fuel: 70, tripsToday: 6, todayEarnings: 1480, monthlyEarnings: 65000, monthlyFuel: 11500 },
    { id: 7, regNo: "TS09 UB 1179", model: "Toyota Etios", managerId: "mgr_moosapet_01", driverId: 7, driverName: "Ramesh V.", status: "ACTIVE", lat: 17.4390, lng: 78.4410, locationName: "SR Nagar Main Rd", platform: "Ola", speed: 32, fuel: 55, tripsToday: 11, todayEarnings: 2640, monthlyEarnings: 79000, monthlyFuel: 13800 },
    { id: 8, regNo: "TS09 UB 1358", model: "Hyundai Aura", managerId: "mgr_moosapet_01", driverId: 8, driverName: "Krishna R.", status: "ACTIVE", lat: 17.4680, lng: 78.4350, locationName: "Balanagar X Roads", platform: "Uber", speed: 40, fuel: 91, tripsToday: 16, todayEarnings: 3500, monthlyEarnings: 94000, monthlyFuel: 17200 },
    { id: 9, regNo: "TS09 UB 1537", model: "Maruti Dzire", managerId: "mgr_moosapet_01", driverId: 9, driverName: "Naresh G.", status: "IDLE", lat: 17.4652, lng: 78.4308, locationName: "Moosapet Depot Yard", platform: "Offline", speed: 0, fuel: 45, tripsToday: 4, todayEarnings: 950, monthlyEarnings: 52000, monthlyFuel: 9200 },
    { id: 10, regNo: "TS09 UB 1716", model: "Honda Amaze", managerId: "mgr_moosapet_01", driverId: 10, driverName: "Mallesh Y.", status: "ACTIVE", lat: 17.4810, lng: 78.4280, locationName: "IDPL Colony", platform: "Uber", speed: 36, fuel: 78, tripsToday: 12, todayEarnings: 2850, monthlyEarnings: 82000, monthlyFuel: 14500 },
    { id: 11, regNo: "TS09 UB 1895", model: "Tata Tigor", managerId: "mgr_moosapet_01", driverId: 11, driverName: "Srinivas R.", status: "OFFLINE", lat: 17.4360, lng: 78.4490, locationName: "Punjagutta Central", platform: "Offline", speed: 0, fuel: 32, tripsToday: 0, todayEarnings: 0, monthlyEarnings: 48000, monthlyFuel: 8100 },
    { id: 12, regNo: "TS09 UB 2074", model: "Maruti Tour S", managerId: "mgr_moosapet_01", driverId: 12, driverName: "Venkat S.", status: "OFFLINE", lat: 17.4550, lng: 78.4390, locationName: "Erragadda Workshop", platform: "Offline", speed: 0, fuel: 20, tripsToday: 0, todayEarnings: 0, monthlyEarnings: 51000, monthlyFuel: 8600 },

    // Kukatpally (10)
    { id: 13, regNo: "TS09 UB 2253", model: "Hyundai Grand i10", managerId: "mgr_kukatpally_02", driverId: 13, driverName: "Mahesh G.", status: "ACTIVE", lat: 17.4930, lng: 78.4010, locationName: "Kukatpally Y-Junction", platform: "Uber", speed: 35, fuel: 77, tripsToday: 15, todayEarnings: 3420, monthlyEarnings: 91000, monthlyFuel: 16200 },
    { id: 14, regNo: "TS09 UB 2432", model: "Toyota Etios", managerId: "mgr_kukatpally_02", driverId: 14, driverName: "Santosh V.", status: "ACTIVE", lat: 17.4880, lng: 78.3910, locationName: "KPHB Colony Phase 3", platform: "Rapido", speed: 28, fuel: 69, tripsToday: 12, todayEarnings: 2750, monthlyEarnings: 80000, monthlyFuel: 14100 },
    { id: 15, regNo: "TS09 UB 2611", model: "Maruti Dzire", managerId: "mgr_kukatpally_02", driverId: 15, driverName: "Arun Teja", status: "ACTIVE", lat: 17.5010, lng: 78.4080, locationName: "JNTU Hyderabad Gate", platform: "Uber", speed: 44, fuel: 85, tripsToday: 16, todayEarnings: 3820, monthlyEarnings: 96000, monthlyFuel: 17500 },
    { id: 16, regNo: "TS09 UB 2790", model: "Tata Tigor", managerId: "mgr_kukatpally_02", driverId: 16, driverName: "Raghavendra", status: "IDLE", lat: 17.4780, lng: 78.3880, locationName: "Forum Sujana Mall", platform: "Ola", speed: 0, fuel: 52, tripsToday: 7, todayEarnings: 1680, monthlyEarnings: 68000, monthlyFuel: 12000 },
    { id: 17, regNo: "TS09 UB 2969", model: "Honda Amaze", managerId: "mgr_kukatpally_02", driverId: 17, driverName: "Dinesh C.", status: "ACTIVE", lat: 17.5120, lng: 78.3980, locationName: "Nizampet X Roads", platform: "Uber", speed: 38, fuel: 73, tripsToday: 13, todayEarnings: 3050, monthlyEarnings: 84000, monthlyFuel: 15000 },
    { id: 18, regNo: "TS09 UB 3148", model: "Hyundai Aura", managerId: "mgr_kukatpally_02", driverId: 18, driverName: "Chandrasekhar", status: "ACTIVE", lat: 17.4850, lng: 78.4120, locationName: "KPHB 6th Phase", platform: "Rapido", speed: 31, fuel: 88, tripsToday: 14, todayEarnings: 3280, monthlyEarnings: 89000, monthlyFuel: 15800 },
    { id: 19, regNo: "TS09 UB 3327", model: "Maruti Tour S", managerId: "mgr_kukatpally_02", driverId: 19, driverName: "Shiva Shankar", status: "ACTIVE", lat: 17.4950, lng: 78.4200, locationName: "Kukatpally Housing Board", platform: "Uber", speed: 26, fuel: 64, tripsToday: 11, todayEarnings: 2590, monthlyEarnings: 77000, monthlyFuel: 13600 },
    { id: 20, regNo: "TS09 UB 3506", model: "Toyota Etios", managerId: "mgr_kukatpally_02", driverId: 20, driverName: "Anji Babu", status: "IDLE", lat: 17.4935, lng: 78.4012, locationName: "Kukatpally Hub Standby", platform: "Offline", speed: 0, fuel: 50, tripsToday: 5, todayEarnings: 1200, monthlyEarnings: 59000, monthlyFuel: 10400 },
    { id: 21, regNo: "TS09 UB 3685", model: "Hyundai Grand i10", managerId: "mgr_kukatpally_02", driverId: 21, driverName: "Naveen Kumar", status: "OFFLINE", lat: 17.5050, lng: 78.3890, locationName: "Pragathi Nagar Shed", platform: "Offline", speed: 0, fuel: 35, tripsToday: 0, todayEarnings: 0, monthlyEarnings: 49000, monthlyFuel: 8300 },
    { id: 22, regNo: "TS09 UB 3864", model: "Maruti Dzire", managerId: "mgr_kukatpally_02", driverId: 22, driverName: "Pradeep V.", status: "OFFLINE", lat: 17.4890, lng: 78.4050, locationName: "Kukatpally Service Center", platform: "Offline", speed: 0, fuel: 15, tripsToday: 0, todayEarnings: 0, monthlyEarnings: 47000, monthlyFuel: 7900 },

    // Madhapur Hitec City (8)
    { id: 23, regNo: "TS09 UB 4043", model: "Hyundai Aura", managerId: "mgr_madhapur_03", driverId: 23, driverName: "Harish Naidu", status: "ACTIVE", lat: 17.4485, lng: 78.3752, locationName: "Cyber Towers Circle", platform: "Uber", speed: 45, fuel: 92, tripsToday: 17, todayEarnings: 3950, monthlyEarnings: 99000, monthlyFuel: 18200 },
    { id: 24, regNo: "TS09 UB 4222", model: "Maruti Dzire", managerId: "mgr_madhapur_03", driverId: 24, driverName: "Chetan Sai", status: "ACTIVE", lat: 17.4390, lng: 78.3820, locationName: "Inorbit Mall Durgam", platform: "Uber", speed: 33, fuel: 79, tripsToday: 13, todayEarnings: 3120, monthlyEarnings: 85000, monthlyFuel: 15300 },
    { id: 25, regNo: "TS09 UB 4401", model: "Honda Amaze", managerId: "mgr_madhapur_03", driverId: 25, driverName: "Ravinder R.", status: "ACTIVE", lat: 17.4420, lng: 78.3680, locationName: "Mindspace IT Park", platform: "Rapido", speed: 29, fuel: 68, tripsToday: 14, todayEarnings: 3340, monthlyEarnings: 87000, monthlyFuel: 15700 },
    { id: 26, regNo: "TS09 UB 4580", model: "Tata Tigor", managerId: "mgr_madhapur_03", driverId: 26, driverName: "Yadagiri P.", status: "ACTIVE", lat: 17.4520, lng: 78.3610, locationName: "Hitec City Station", platform: "Ola", speed: 37, fuel: 84, tripsToday: 12, todayEarnings: 2880, monthlyEarnings: 81000, monthlyFuel: 14600 },
    { id: 27, regNo: "TS09 UB 4759", model: "Toyota Etios", managerId: "mgr_madhapur_03", driverId: 27, driverName: "Manoj K.", status: "IDLE", lat: 17.4410, lng: 78.3900, locationName: "Madhapur Police Station", platform: "Uber", speed: 0, fuel: 61, tripsToday: 6, todayEarnings: 1550, monthlyEarnings: 67000, monthlyFuel: 11900 },
    { id: 28, regNo: "TS09 UB 4938", model: "Hyundai Grand i10", managerId: "mgr_madhapur_03", driverId: 28, driverName: "Giri Babu", status: "ACTIVE", lat: 17.4580, lng: 78.3720, locationName: "Kondapur Botanical Garden", platform: "Uber", speed: 41, fuel: 86, tripsToday: 15, todayEarnings: 3580, monthlyEarnings: 93000, monthlyFuel: 16900 },
    { id: 29, regNo: "TS09 UB 5117", model: "Maruti Tour S", managerId: "mgr_madhapur_03", driverId: 29, driverName: "Bhanu Prasad", status: "IDLE", lat: 17.4485, lng: 78.3752, locationName: "Madhapur Hub Yard", platform: "Offline", speed: 0, fuel: 48, tripsToday: 5, todayEarnings: 1250, monthlyEarnings: 58000, monthlyFuel: 10200 },
    { id: 30, regNo: "TS09 UB 5296", model: "Hyundai Aura", managerId: "mgr_madhapur_03", driverId: 30, driverName: "Kiran Kumar", status: "OFFLINE", lat: 17.4620, lng: 78.3580, locationName: "Gachibowli Stadium", platform: "Offline", speed: 0, fuel: 18, tripsToday: 0, todayEarnings: 0, monthlyEarnings: 46000, monthlyFuel: 7800 }
];

// 30 Drivers with Realistic Performance
const DEMO_DRIVERS = [
    { id: 1, name: "Ravi Kumar", phone: "+91 90123 45678", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 3368", status: "ON_DUTY", rating: 4.88, totalTrips: 412, platform: "Uber", earningsToday: 3000 },
    { id: 2, name: "Suman Goud", phone: "+91 98765 43210", managerId: "mgr_moosapet_01", vehicleNo: "TS10 EF 7745", status: "ON_DUTY", rating: 4.92, totalTrips: 520, platform: "Rapido", earningsToday: 3750 },
    { id: 3, name: "Praveen M.", phone: "+91 99887 66554", managerId: "mgr_moosapet_01", vehicleNo: "TS09 AC 9890", status: "ON_DUTY", rating: 4.75, totalTrips: 340, platform: "Ola", earningsToday: 2400 },
    { id: 4, name: "Siva Reddy", phone: "+91 97412 27330", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UD 3320", status: "ON_DUTY", rating: 4.81, totalTrips: 390, platform: "Uber", earningsToday: 3320 },
    { id: 5, name: "Prasad K.", phone: "+91 96321 33200", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UE 2950", status: "ON_DUTY", rating: 4.70, totalTrips: 360, platform: "Rapido", earningsToday: 2950 },
    { id: 6, name: "Baleru N.", phone: "+91 98452 44550", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UF 1840", status: "IDLE", rating: 4.65, totalTrips: 280, platform: "Uber", earningsToday: 1480 },
    { id: 7, name: "Ramesh V.", phone: "+91 99001 55660", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1179", status: "ON_DUTY", rating: 4.78, totalTrips: 350, platform: "Ola", earningsToday: 2640 },
    { id: 8, name: "Krishna R.", phone: "+91 98450 66770", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1358", status: "ON_DUTY", rating: 4.95, totalTrips: 610, platform: "Uber", earningsToday: 3500 },
    { id: 9, name: "Naresh G.", phone: "+91 97312 77880", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1537", status: "IDLE", rating: 4.60, totalTrips: 210, platform: "Offline", earningsToday: 950 },
    { id: 10, name: "Mallesh Y.", phone: "+91 98860 88990", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1716", status: "ON_DUTY", rating: 4.82, totalTrips: 420, platform: "Uber", earningsToday: 2850 },
    { id: 11, name: "Srinivas R.", phone: "+91 99450 11220", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1895", status: "IDLE", rating: 4.68, totalTrips: 190, platform: "Offline", earningsToday: 0 },
    { id: 12, name: "Venkat S.", phone: "+91 98456 22330", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 2074", status: "IDLE", rating: 4.71, totalTrips: 230, platform: "Offline", earningsToday: 0 },

    // Kukatpally (10)
    { id: 13, name: "Mahesh G.", phone: "+91 97410 33440", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2253", status: "ON_DUTY", rating: 4.89, totalTrips: 480, platform: "Uber", earningsToday: 3420 },
    { id: 14, name: "Santosh V.", phone: "+91 96112 44550", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2432", status: "ON_DUTY", rating: 4.74, totalTrips: 340, platform: "Rapido", earningsToday: 2750 },
    { id: 15, name: "Arun Teja", phone: "+91 98801 55660", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2611", status: "ON_DUTY", rating: 4.96, totalTrips: 580, platform: "Uber", earningsToday: 3820 },
    { id: 16, name: "Raghavendra", phone: "+91 99002 66770", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2790", status: "IDLE", rating: 4.67, totalTrips: 240, platform: "Ola", earningsToday: 1680 },
    { id: 17, name: "Dinesh C.", phone: "+91 98453 77880", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2969", status: "ON_DUTY", rating: 4.80, totalTrips: 410, platform: "Uber", earningsToday: 3050 },
    { id: 18, name: "Chandrasekhar", phone: "+91 97413 88990", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3148", status: "ON_DUTY", rating: 4.86, totalTrips: 430, platform: "Rapido", earningsToday: 3280 },
    { id: 19, name: "Shiva Shankar", phone: "+91 96324 99000", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3327", status: "ON_DUTY", rating: 4.73, totalTrips: 320, platform: "Uber", earningsToday: 2590 },
    { id: 20, name: "Anji Babu", phone: "+91 98862 10100", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3506", status: "IDLE", rating: 4.65, totalTrips: 270, platform: "Offline", earningsToday: 1200 },
    { id: 21, name: "Naveen Kumar", phone: "+91 99452 20200", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3685", status: "IDLE", rating: 4.62, totalTrips: 180, platform: "Offline", earningsToday: 0 },
    { id: 22, name: "Pradeep V.", phone: "+91 98457 30300", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3864", status: "IDLE", rating: 4.59, totalTrips: 160, platform: "Offline", earningsToday: 0 },

    // Madhapur (8)
    { id: 23, name: "Harish Naidu", phone: "+91 97314 40400", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4043", status: "ON_DUTY", rating: 4.93, totalTrips: 620, platform: "Uber", earningsToday: 3950 },
    { id: 24, name: "Chetan Sai", phone: "+91 96114 50500", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4222", status: "ON_DUTY", rating: 4.85, totalTrips: 450, platform: "Uber", earningsToday: 3120 },
    { id: 25, name: "Ravinder R.", phone: "+91 98803 60600", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4401", status: "ON_DUTY", rating: 4.77, totalTrips: 370, platform: "Rapido", earningsToday: 3340 },
    { id: 26, name: "Yadagiri P.", phone: "+91 99004 70700", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4580", status: "ON_DUTY", rating: 4.81, totalTrips: 400, platform: "Ola", earningsToday: 2880 },
    { id: 27, name: "Manoj K.", phone: "+91 98458 80800", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4759", status: "IDLE", rating: 4.69, totalTrips: 290, platform: "Uber", earningsToday: 1550 },
    { id: 28, name: "Giri Babu", phone: "+91 97415 90900", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4938", status: "ON_DUTY", rating: 4.88, totalTrips: 510, platform: "Uber", earningsToday: 3580 },
    { id: 29, name: "Bhanu Prasad", phone: "+91 96326 12120", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 5117", status: "IDLE", rating: 4.72, totalTrips: 310, platform: "Offline", earningsToday: 1250 },
    { id: 30, name: "Kiran Kumar", phone: "+91 98864 13130", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 5296", status: "IDLE", rating: 4.58, totalTrips: 150, platform: "Offline", earningsToday: 0 }
];

// Duty Register Logs (Exact match with reference image)
const DEMO_DUTY_LOGS = [
    {
        id: "log_001_rk",
        shiftType: "12_HOURS",
        loginDate: "09 Nov 2026",
        loginTime: "06:00 AM",
        logoutDate: "09 Nov 2026",
        logoutTime: "07:30 PM",
        vehicleNo: "TS09 UB 3368",
        driverName: "Ravi Kumar",
        driverPhone: "+91 90123 45678",
        trips: 12,
        earnings: 3000.0,
        expenditure: 201.50,
        cashCollection: 2000.0,
        commissionRent: 1050.0,
        commissionPercentage: 35.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 2000.0,
        paymentOnline: 0.0,
        netBalance: -748.5,
        status: "SETTLED",
        remarks: "Settled via UPI / Moosapet cash balance.",
        managerId: "mgr_moosapet_01",
        source: "Uber"
    },
    {
        id: "log_002_sg",
        shiftType: "12_HOURS",
        loginDate: "09 Nov 2026",
        loginTime: "06:15 AM",
        logoutDate: "09 Nov 2026",
        logoutTime: "07:45 PM",
        vehicleNo: "TS10 EF 7745",
        driverName: "Suman Goud",
        driverPhone: "+91 98765 43210",
        trips: 15,
        earnings: 3750.0,
        expenditure: 280.00,
        cashCollection: 2500.0,
        commissionRent: 1312.5,
        commissionPercentage: 35.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 0.0,
        paymentOnline: 0.0,
        netBalance: -907.5,
        status: "PENDING",
        remarks: "Shift completed, cash reconciliation pending at hub.",
        managerId: "mgr_moosapet_01",
        source: "Rapido"
    },
    {
        id: "log_003_pm",
        shiftType: "12_HOURS",
        loginDate: "09 Nov 2026",
        loginTime: "06:30 AM",
        logoutDate: "09 Nov 2026",
        logoutTime: "07:00 PM",
        vehicleNo: "TS09 AC 9890",
        driverName: "Praveen M.",
        driverPhone: "+91 99887 66554",
        trips: 10,
        earnings: 2400.0,
        expenditure: 190.00,
        cashCollection: 1600.0,
        commissionRent: 840.0,
        commissionPercentage: 35.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 1600.0,
        paymentOnline: 0.0,
        netBalance: -570.0,
        status: "SETTLED",
        remarks: "Standard 12h shift target achieved.",
        managerId: "mgr_moosapet_01",
        source: "Ola"
    },
    {
        id: "log_004_sr",
        shiftType: "12_HOURS",
        loginDate: "09 Nov 2026",
        loginTime: "06:00 AM",
        logoutDate: "09 Nov 2026",
        logoutTime: "07:15 PM",
        vehicleNo: "TS09 UD 3320",
        driverName: "Siva Reddy",
        driverPhone: "+91 97412 27330",
        trips: 14,
        earnings: 3320.0,
        expenditure: 240.00,
        cashCollection: 2200.0,
        commissionRent: 1162.0,
        commissionPercentage: 35.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 2200.0,
        paymentOnline: 0.0,
        netBalance: -798.0,
        status: "SETTLED",
        remarks: "Moosapet paper book entry verified.",
        managerId: "mgr_moosapet_01",
        source: "Uber"
    },
    {
        id: "log_005_pk",
        shiftType: "12_HOURS",
        loginDate: "09 Nov 2026",
        loginTime: "06:30 AM",
        logoutDate: "09 Nov 2026",
        logoutTime: "08:00 PM",
        vehicleNo: "TS09 UE 2950",
        driverName: "Prasad K.",
        driverPhone: "+91 96321 33200",
        trips: 13,
        earnings: 2950.0,
        expenditure: 210.00,
        cashCollection: 1900.0,
        commissionRent: 1032.5,
        commissionPercentage: 35.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 0.0,
        paymentOnline: 0.0,
        netBalance: -657.5,
        status: "PENDING",
        remarks: "Cash collection pending at evening check-in.",
        managerId: "mgr_moosapet_01",
        source: "Rapido"
    },
    {
        id: "log_006_mg",
        shiftType: "12_HOURS",
        loginDate: "09 Nov 2026",
        loginTime: "05:45 AM",
        logoutDate: "09 Nov 2026",
        logoutTime: "07:15 PM",
        vehicleNo: "TS09 UB 2253",
        driverName: "Mahesh G.",
        driverPhone: "+91 97410 33440",
        trips: 15,
        earnings: 3420.0,
        expenditure: 260.00,
        cashCollection: 2300.0,
        commissionRent: 1197.0,
        commissionPercentage: 35.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 2300.0,
        paymentOnline: 0.0,
        netBalance: -843.0,
        status: "SETTLED",
        remarks: "Kukatpally Y-Junction on time settlement.",
        managerId: "mgr_kukatpally_02",
        source: "Uber"
    },
    {
        id: "log_007_at",
        shiftType: "12_HOURS",
        loginDate: "09 Nov 2026",
        loginTime: "06:00 AM",
        logoutDate: "09 Nov 2026",
        logoutTime: "08:15 PM",
        vehicleNo: "TS09 UB 2611",
        driverName: "Arun Teja",
        driverPhone: "+91 98801 55660",
        trips: 16,
        earnings: 3820.0,
        expenditure: 290.00,
        cashCollection: 2600.0,
        commissionRent: 1528.0,
        commissionPercentage: 40.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 2600.0,
        paymentOnline: 0.0,
        netBalance: -782.0,
        status: "SETTLED",
        remarks: "Top performer: 40% Tier achieved! (₹3,820 Gross)",
        managerId: "mgr_kukatpally_02",
        source: "Uber"
    },
    {
        id: "log_008_hn",
        shiftType: "12_HOURS",
        loginDate: "09 Nov 2026",
        loginTime: "06:00 AM",
        logoutDate: "09 Nov 2026",
        logoutTime: "08:00 PM",
        vehicleNo: "TS09 UB 4043",
        driverName: "Harish Naidu",
        driverPhone: "+91 97314 40400",
        trips: 17,
        earnings: 3950.0,
        expenditure: 310.00,
        cashCollection: 2700.0,
        commissionRent: 1580.0,
        commissionPercentage: 40.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 2700.0,
        paymentOnline: 0.0,
        netBalance: -810.0,
        status: "SETTLED",
        remarks: "Hitec City tech park routes. 40% Tier achieved.",
        managerId: "mgr_madhapur_03",
        source: "Uber"
    }
];

// Telemetry stream matching screenshot Driver A, B, C, D
const DEMO_TELEMETRY = [
    {
        id: "tel_1",
        driverName: "Driver A (Demo)",
        vehicleNo: "TS09 XX 1234",
        platform: "Uber",
        status: "Auto Aggregated",
        statusColor: "bg-[#10B981]/15 text-[#10B981] border-[#10B981]/30",
        time: "2 min ago",
        trips: 12,
        gross: 3000,
        fuel: 201.50
    },
    {
        id: "tel_2",
        driverName: "Driver B (Demo)",
        vehicleNo: "TS10 XX 5678",
        platform: "Rapido",
        status: "Shift Started",
        statusColor: "bg-[#3185F6]/15 text-[#3185F6] border-[#3185F6]/30",
        time: "5 min ago",
        trips: 3,
        gross: 720,
        fuel: 80.00
    },
    {
        id: "tel_3",
        driverName: "Driver C (Demo)",
        vehicleNo: "TS09 XX 9012",
        platform: "Ola",
        status: "Trip Synced",
        statusColor: "bg-[#6655F5]/15 text-[#8B5CF6] border-[#6655F5]/30",
        time: "8 min ago",
        trips: 10,
        gross: 2400,
        fuel: 190.00
    },
    {
        id: "tel_4",
        driverName: "Driver D (Demo)",
        vehicleNo: "TS12 XX 3456",
        platform: "Uber",
        status: "Pending Review",
        statusColor: "bg-[#F59E0B]/15 text-[#F59E0B] border-[#F59E0B]/30",
        time: "12 min ago",
        trips: 9,
        gross: 2150,
        fuel: 160.00
    }
];

// Earnings and Profitability Trend Data (03 Nov to 09 Nov matching chart)
const DEMO_EARNINGS_TREND = {
    daily: {
        labels: ["03 Nov", "04 Nov", "05 Nov", "06 Nov", "07 Nov", "08 Nov", "09 Nov"],
        grossEarnings: [72000, 105000, 75000, 92000, 88000, 102000, 124860],
        netProfit: [24000, 34000, 22000, 29000, 27500, 32000, 38420]
    },
    weekly: {
        labels: ["Week 41", "Week 42", "Week 43", "Week 44", "Week 45"],
        grossEarnings: [480000, 520000, 560000, 610000, 680000],
        netProfit: [155000, 168000, 180000, 198000, 225000]
    },
    monthly: {
        labels: ["Jul", "Aug", "Sep", "Oct", "Nov (MTD)"],
        grossEarnings: [2150000, 2380000, 2560000, 2840000, 1248600],
        netProfit: [690000, 765000, 825000, 915000, 384200]
    }
};

// Platform Earnings Split (Today) matching screenshot
const DEMO_PLATFORM_EARNINGS = {
    totalGross: 124860,
    platforms: [
        { name: "Uber", percentage: 54, amount: 67424, color: "#6655F5", activeCabs: 16 },
        { name: "Rapido", percentage: 28, amount: 34961, color: "#F59E0B", activeCabs: 8 },
        { name: "Ola", percentage: 18, amount: 22475, color: "#10B981", activeCabs: 6 }
    ]
};

/**
 * Isolated Mock Data Repository for Client Demo
 * Operates purely in-memory and in localStorage with ZERO production database writes.
 */
class MockFleetRepository {
    constructor() {
        this.storageKey = "SVM_DEMO_STORE_V2";
        this.data = this.loadInitialData();
    }

    loadInitialData() {
        try {
            const raw = localStorage.getItem(this.storageKey);
            if (raw) {
                return JSON.parse(raw);
            }
        } catch (e) {
            console.warn("Could not load local demo store, using defaults:", e);
        }

        const initial = {
            vehicles: DEMO_VEHICLES,
            drivers: DEMO_DRIVERS,
            dutyLogs: DEMO_DUTY_LOGS,
            telemetry: DEMO_TELEMETRY,
            hubs: DEMO_HUBS
        };
        this.saveState(initial);
        return initial;
    }

    saveState(data = this.data) {
        try {
            localStorage.setItem(this.storageKey, JSON.stringify(data));
        } catch (e) {
            console.warn("Failed to persist demo state:", e);
        }
    }

    getVehicles(hubId = "ALL") {
        if (!hubId || hubId === "ALL") return this.data.vehicles;
        return this.data.vehicles.filter(v => v.managerId === hubId);
    }

    getDrivers(hubId = "ALL") {
        if (!hubId || hubId === "ALL") return this.data.drivers;
        return this.data.drivers.filter(d => d.managerId === hubId);
    }

    getDutyLogs(hubId = "ALL") {
        if (!hubId || hubId === "ALL") return this.data.dutyLogs;
        return this.data.dutyLogs.filter(l => l.managerId === hubId);
    }

    getHubs() {
        return this.data.hubs;
    }

    getTelemetry() {
        return this.data.telemetry;
    }

    addDutyLog(log) {
        const newLog = {
            id: "log_" + Date.now(),
            ...log
        };
        this.data.dutyLogs.unshift(newLog);
        this.saveState();
        return newLog;
    }

    addDriver(driver) {
        const newDriver = {
            id: Date.now(),
            totalTrips: 0,
            earningsToday: 0,
            ...driver
        };
        this.data.drivers.push(newDriver);
        this.saveState();
        return newDriver;
    }

    addVehicle(vehicle) {
        const newVehicle = {
            id: Date.now(),
            tripsToday: 0,
            todayEarnings: 0,
            monthlyEarnings: 0,
            monthlyFuel: 0,
            speed: 0,
            fuel: 100,
            status: "ACTIVE",
            ...vehicle
        };
        this.data.vehicles.push(newVehicle);
        this.saveState();
        return newVehicle;
    }

    reassignVehicle(vehicleId, newHubId) {
        const v = this.data.vehicles.find(item => item.id === parseInt(vehicleId));
        if (v) {
            v.managerId = newHubId;
            const d = this.data.drivers.find(d => d.vehicleNo === v.regNo);
            if (d) d.managerId = newHubId;
            this.saveState();
        }
        return v;
    }

    resetDemoData() {
        localStorage.removeItem(this.storageKey);
        this.data = this.loadInitialData();
        return this.data;
    }
}

// Global Demo Repository instance
window.DEMO_REPO = new MockFleetRepository();
window.DEMO_CONFIG = DEMO_CONFIG;
window.DEMO_HUBS = DEMO_HUBS;
window.DEMO_EARNINGS_TREND = DEMO_EARNINGS_TREND;
window.DEMO_PLATFORM_EARNINGS = DEMO_PLATFORM_EARNINGS;

// Global compatibility aliases
window.APP_CONFIG = {
    hubs: DEMO_HUBS,
    storageKeys: {
        role: "SVM_ROLE",
        hubId: "SVM_HUB_ID"
    }
};
window.FleetDataRepository = window.DEMO_REPO;

