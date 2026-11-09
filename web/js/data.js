/**
 * SVM FLEET Operating System (Web Portal)
 * Master Fleet Data & Supabase Cloud Database Repository
 */

const DEFAULT_DRIVERS = [
    { id: 1, name: "Suman", phone: "+91 93919 15013", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 3368", status: "ON_DUTY", rating: 4.88, totalTrips: 412 },
    { id: 2, name: "Narendra", phone: "+91 99598 59347", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UA 2271", status: "ON_DUTY", rating: 4.75, totalTrips: 320 },
    { id: 3, name: "Pavan", phone: "+91 98451 22710", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UC 2733", status: "ON_DUTY", rating: 4.92, totalTrips: 580 },
    { id: 4, name: "Siva", phone: "+91 97412 27330", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UD 3320", status: "ON_DUTY", rating: 4.81, totalTrips: 290 },
    { id: 5, name: "Prasad", phone: "+91 96321 33200", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UE 2950", status: "ON_DUTY", rating: 4.70, totalTrips: 350 },
    { id: 6, name: "Baleru", phone: "+91 98452 44550", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UF 1840", status: "ON_DUTY", rating: 4.85, totalTrips: 420 },
    { id: 7, name: "Ramesh Kumar", phone: "+91 99001 55660", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1179", status: "ON_DUTY", rating: 4.65, totalTrips: 210 },
    { id: 8, name: "Krishna Reddy", phone: "+91 98450 66770", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1358", status: "ON_DUTY", rating: 4.90, totalTrips: 610 },
    { id: 9, name: "Naresh G", phone: "+91 97312 77880", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1537", status: "IDLE", rating: 4.60, totalTrips: 180 },
    { id: 10, name: "Mallesh Yadav", phone: "+91 98860 88990", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1716", status: "ON_DUTY", rating: 4.78, totalTrips: 340 },
    { id: 11, name: "Srinivas Rao", phone: "+91 99450 11220", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 1895", status: "ON_DUTY", rating: 4.82, totalTrips: 395 },
    { id: 12, name: "Venkat Swamy", phone: "+91 98456 22330", managerId: "mgr_moosapet_01", vehicleNo: "TS09 UB 2074", status: "ON_DUTY", rating: 4.71, totalTrips: 280 },

    // Kukatpally Hub (10 Drivers)
    { id: 13, name: "Mahesh Goud", phone: "+91 97410 33440", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2253", status: "ON_DUTY", rating: 4.89, totalTrips: 450 },
    { id: 14, name: "Santosh V", phone: "+91 96112 44550", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2432", status: "ON_DUTY", rating: 4.74, totalTrips: 310 },
    { id: 15, name: "Arun Teja", phone: "+91 98801 55660", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2611", status: "ON_DUTY", rating: 4.95, totalTrips: 520 },
    { id: 16, name: "Raghavendra", phone: "+91 99002 66770", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2790", status: "ON_DUTY", rating: 4.67, totalTrips: 220 },
    { id: 17, name: "Dinesh Chary", phone: "+91 98453 77880", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 2969", status: "ON_DUTY", rating: 4.80, totalTrips: 380 },
    { id: 18, name: "Chandrasekhar", phone: "+91 97413 88990", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3148", status: "ON_DUTY", rating: 4.86, totalTrips: 410 },
    { id: 19, name: "Shiva Shankar", phone: "+91 96324 99000", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3327", status: "ON_DUTY", rating: 4.72, totalTrips: 305 },
    { id: 20, name: "Anji Babu", phone: "+91 98862 10100", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3506", status: "ON_DUTY", rating: 4.79, totalTrips: 330 },
    { id: 21, name: "Naveen Kumar", phone: "+91 99452 20200", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3685", status: "ON_DUTY", rating: 4.83, totalTrips: 370 },
    { id: 22, name: "Pradeep Varma", phone: "+91 98457 30300", managerId: "mgr_kukatpally_02", vehicleNo: "TS09 UB 3864", status: "IDLE", rating: 4.62, totalTrips: 190 },

    // Madhapur Hitec City Hub (8 Drivers)
    { id: 23, name: "Harish Naidu", phone: "+91 97314 40400", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4043", status: "ON_DUTY", rating: 4.91, totalTrips: 560 },
    { id: 24, name: "Chetan Sai", phone: "+91 96114 50500", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4222", status: "ON_DUTY", rating: 4.85, totalTrips: 440 },
    { id: 25, name: "Ravinder Reddy", phone: "+91 98803 60600", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4401", status: "ON_DUTY", rating: 4.77, totalTrips: 350 },
    { id: 26, name: "Yadagiri", phone: "+91 99004 70700", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4580", status: "ON_DUTY", rating: 4.81, totalTrips: 390 },
    { id: 27, name: "Manoj K", phone: "+91 98458 80800", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4759", status: "ON_DUTY", rating: 4.69, totalTrips: 260 },
    { id: 28, name: "Giri Babu", phone: "+91 97415 90900", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 4938", status: "ON_DUTY", rating: 4.88, totalTrips: 480 },
    { id: 29, name: "Bhanu Prasad", phone: "+91 96326 12120", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 5117", status: "ON_DUTY", rating: 4.73, totalTrips: 310 },
    { id: 30, name: "Kiran Kumar", phone: "+91 98864 13130", managerId: "mgr_madhapur_03", vehicleNo: "TS09 UB 5296", status: "IDLE", rating: 4.58, totalTrips: 150 }
];

const DEFAULT_VEHICLES = [
    // Moosapet (12 vehicles)
    { id: 1, regNo: "TS09 UB 3368", model: "Hyundai Aura", managerId: "mgr_moosapet_01", driverId: 1, driverName: "Suman", status: "ACTIVE", lat: 17.4645, lng: 78.4320, locationName: "Moosapet Metro", platform: "Uber", speed: 38, fuel: 82, tripsToday: 16, todayEarnings: 2950 },
    { id: 2, regNo: "TS09 UA 2271", model: "Maruti Dzire", managerId: "mgr_moosapet_01", driverId: 2, driverName: "Narendra", status: "ACTIVE", lat: 17.4720, lng: 78.4410, locationName: "Kukatpally Y Junction", platform: "Rapido", speed: 24, fuel: 65, tripsToday: 7, todayEarnings: 2271 },
    { id: 3, regNo: "TS09 UC 2733", model: "Honda Amaze", managerId: "mgr_moosapet_01", driverId: 3, driverName: "Pavan", status: "ACTIVE", lat: 17.4510, lng: 78.4480, locationName: "Bharat Nagar Flyover", platform: "Ola", speed: 42, fuel: 74, tripsToday: 11, todayEarnings: 2733 },
    { id: 4, regNo: "TS09 UD 3320", model: "Tata Tigor", managerId: "mgr_moosapet_01", driverId: 4, driverName: "Siva", status: "ACTIVE", lat: 17.4420, lng: 78.4550, locationName: "Ameerpet Circle", platform: "Uber", speed: 30, fuel: 58, tripsToday: 12, todayEarnings: 3320 },
    { id: 5, regNo: "TS09 UE 2950", model: "Maruti Tour S", managerId: "mgr_moosapet_01", driverId: 5, driverName: "Prasad", status: "ACTIVE", lat: 17.4580, lng: 78.4210, locationName: "Sanathnagar Ind Area", platform: "Rapido", speed: 28, fuel: 89, tripsToday: 14, todayEarnings: 2950 },
    { id: 6, regNo: "TS09 UF 1840", model: "Hyundai Grand i10", managerId: "mgr_moosapet_01", driverId: 6, driverName: "Baleru", status: "ACTIVE", lat: 17.4490, lng: 78.4680, locationName: "Begumpet Airport Rd", platform: "Uber", speed: 45, fuel: 70, tripsToday: 10, todayEarnings: 2180 },
    { id: 7, regNo: "TS09 UB 1179", model: "Toyota Etios", managerId: "mgr_moosapet_01", driverId: 7, driverName: "Ramesh Kumar", status: "ACTIVE", lat: 17.4390, lng: 78.4410, locationName: "SR Nagar Main Rd", platform: "Ola", speed: 32, fuel: 55, tripsToday: 13, todayEarnings: 2640 },
    { id: 8, regNo: "TS09 UB 1358", model: "Hyundai Aura", managerId: "mgr_moosapet_01", driverId: 8, driverName: "Krishna Reddy", status: "ACTIVE", lat: 17.4680, lng: 78.4350, locationName: "Balanagar X Roads", platform: "Uber", speed: 40, fuel: 91, tripsToday: 15, todayEarnings: 3100 },
    { id: 9, regNo: "TS09 UB 1537", model: "Maruti Dzire", managerId: "mgr_moosapet_01", driverId: 9, driverName: "Naresh G", status: "IDLE", lat: 17.4652, lng: 78.4308, locationName: "Moosapet Hub Yard", platform: "Offline", speed: 0, fuel: 45, tripsToday: 4, todayEarnings: 820 },
    { id: 10, regNo: "TS09 UB 1716", model: "Honda Amaze", managerId: "mgr_moosapet_01", driverId: 10, driverName: "Mallesh Yadav", status: "ACTIVE", lat: 17.4810, lng: 78.4280, locationName: "IDPL Colony", platform: "Uber", speed: 36, fuel: 78, tripsToday: 11, todayEarnings: 2450 },
    { id: 11, regNo: "TS09 UB 1895", model: "Tata Tigor", managerId: "mgr_moosapet_01", driverId: 11, driverName: "Srinivas Rao", status: "ACTIVE", lat: 17.4360, lng: 78.4490, locationName: "Punjagutta Central", platform: "Rapido", speed: 20, fuel: 62, tripsToday: 14, todayEarnings: 2890 },
    { id: 12, regNo: "TS09 UB 2074", model: "Maruti Tour S", managerId: "mgr_moosapet_01", driverId: 12, driverName: "Venkat Swamy", status: "ACTIVE", lat: 17.4550, lng: 78.4390, locationName: "Erragadda Rythu Bazar", platform: "Ola", speed: 25, fuel: 84, tripsToday: 12, todayEarnings: 2580 },

    // Kukatpally (10 vehicles)
    { id: 13, regNo: "TS09 UB 2253", model: "Hyundai Grand i10", managerId: "mgr_kukatpally_02", driverId: 13, driverName: "Mahesh Goud", status: "ACTIVE", lat: 17.4930, lng: 78.4010, locationName: "Kukatpally Y-Junction", platform: "Uber", speed: 35, fuel: 77, tripsToday: 15, todayEarnings: 3420 },
    { id: 14, regNo: "TS09 UB 2432", model: "Toyota Etios", managerId: "mgr_kukatpally_02", driverId: 14, driverName: "Santosh V", status: "ACTIVE", lat: 17.4880, lng: 78.3910, locationName: "KPHB Colony Phase 3", platform: "Rapido", speed: 28, fuel: 69, tripsToday: 12, todayEarnings: 2750 },
    { id: 15, regNo: "TS09 UB 2611", model: "Hyundai Aura", managerId: "mgr_kukatpally_02", driverId: 15, driverName: "Arun Teja", status: "ACTIVE", lat: 17.4990, lng: 78.3840, locationName: "JNTU Metro Station", platform: "Uber", speed: 44, fuel: 93, tripsToday: 18, todayEarnings: 3880 },
    { id: 16, regNo: "TS09 UB 2790", model: "Maruti Dzire", managerId: "mgr_kukatpally_02", driverId: 16, driverName: "Raghavendra", status: "ACTIVE", lat: 17.5080, lng: 78.3670, locationName: "Miyapur Allwyn X Road", platform: "Ola", speed: 38, fuel: 51, tripsToday: 10, todayEarnings: 2190 },
    { id: 17, regNo: "TS09 UB 2969", model: "Honda Amaze", managerId: "mgr_kukatpally_02", driverId: 17, driverName: "Dinesh Chary", status: "ACTIVE", lat: 17.4820, lng: 78.3990, locationName: "Forum Sujana Mall", platform: "Uber", speed: 22, fuel: 88, tripsToday: 13, todayEarnings: 2840 },
    { id: 18, regNo: "TS09 UB 3148", model: "Tata Tigor", managerId: "mgr_kukatpally_02", driverId: 18, driverName: "Chandrasekhar", status: "ACTIVE", lat: 17.4760, lng: 78.3890, locationName: "Hitec City MMTS", platform: "Rapido", speed: 30, fuel: 63, tripsToday: 14, todayEarnings: 3050 },
    { id: 19, regNo: "TS09 UB 3327", model: "Maruti Tour S", managerId: "mgr_kukatpally_02", driverId: 19, driverName: "Shiva Shankar", status: "ACTIVE", lat: 17.4910, lng: 78.4110, locationName: "Vivekananda Nagar", platform: "Ola", speed: 27, fuel: 72, tripsToday: 11, todayEarnings: 2410 },
    { id: 20, regNo: "TS09 UB 3506", model: "Hyundai Grand i10", managerId: "mgr_kukatpally_02", driverId: 20, driverName: "Anji Babu", status: "ACTIVE", lat: 17.4840, lng: 78.4060, locationName: "Kukatpally Housing Board", platform: "Uber", speed: 41, fuel: 80, tripsToday: 12, todayEarnings: 2680 },
    { id: 21, regNo: "TS09 UB 3685", model: "Toyota Etios", managerId: "mgr_kukatpally_02", driverId: 21, driverName: "Naveen Kumar", status: "ACTIVE", lat: 17.4960, lng: 78.3950, locationName: "Manjeera Mall Rd", platform: "Uber", speed: 34, fuel: 66, tripsToday: 13, todayEarnings: 2810 },
    { id: 22, regNo: "TS09 UB 3864", model: "Hyundai Aura", managerId: "mgr_kukatpally_02", driverId: 22, driverName: "Pradeep Varma", status: "IDLE", lat: 17.4935, lng: 78.4012, locationName: "Kukatpally Hub Yard", platform: "Offline", speed: 0, fuel: 50, tripsToday: 3, todayEarnings: 650 },

    // Madhapur (8 vehicles)
    { id: 23, regNo: "TS09 UB 4043", model: "Maruti Dzire", managerId: "mgr_madhapur_03", driverId: 23, driverName: "Harish Naidu", status: "ACTIVE", lat: 17.4485, lng: 78.3752, locationName: "Cyber Towers Madhapur", platform: "Uber", speed: 39, fuel: 85, tripsToday: 17, todayEarnings: 3740 },
    { id: 24, regNo: "TS09 UB 4222", model: "Honda Amaze", managerId: "mgr_madhapur_03", driverId: 24, driverName: "Chetan Sai", status: "ACTIVE", lat: 17.4410, lng: 78.3810, locationName: "Inorbit Mall Durgam Cheruvu", platform: "Uber", speed: 25, fuel: 76, tripsToday: 15, todayEarnings: 3260 },
    { id: 25, regNo: "TS09 UB 4401", model: "Tata Tigor", managerId: "mgr_madhapur_03", driverId: 25, driverName: "Ravinder Reddy", status: "ACTIVE", lat: 17.4320, lng: 78.3680, locationName: "Knowledge City Silpa", platform: "Rapido", speed: 43, fuel: 90, tripsToday: 13, todayEarnings: 2900 },
    { id: 26, regNo: "TS09 UB 4580", model: "Maruti Tour S", managerId: "mgr_madhapur_03", driverId: 26, driverName: "Yadagiri", status: "ACTIVE", lat: 17.4430, lng: 78.3560, locationName: "Gachibowli DLF IT Park", platform: "Ola", speed: 36, fuel: 61, tripsToday: 14, todayEarnings: 3120 },
    { id: 27, regNo: "TS09 UB 4759", model: "Hyundai Grand i10", managerId: "mgr_madhapur_03", driverId: 27, driverName: "Manoj K", status: "ACTIVE", lat: 17.4570, lng: 78.3630, locationName: "Kondapur RTO Circle", platform: "Uber", speed: 31, fuel: 79, tripsToday: 11, todayEarnings: 2480 },
    { id: 28, regNo: "TS09 UB 4938", model: "Toyota Etios", managerId: "mgr_madhapur_03", driverId: 28, driverName: "Giri Babu", status: "ACTIVE", lat: 17.4330, lng: 78.4050, locationName: "Jubilee Hills Rd No 36", platform: "Uber", speed: 47, fuel: 82, tripsToday: 16, todayEarnings: 3600 },
    { id: 29, regNo: "TS09 UB 5117", model: "Hyundai Aura", managerId: "mgr_madhapur_03", driverId: 29, driverName: "Bhanu Prasad", status: "ACTIVE", lat: 17.4260, lng: 78.3420, locationName: "Financial District Nanakramguda", platform: "Rapido", speed: 52, fuel: 68, tripsToday: 12, todayEarnings: 2720 },
    { id: 30, regNo: "TS09 UB 5296", model: "Maruti Dzire", managerId: "mgr_madhapur_03", driverId: 30, driverName: "Kiran Kumar", status: "IDLE", lat: 17.4485, lng: 78.3752, locationName: "Madhapur Hub Yard", platform: "Offline", speed: 0, fuel: 40, tripsToday: 2, todayEarnings: 450 }
];

const DEFAULT_DUTY_LOGS = [
    {
        id: "log_001",
        shiftType: "12_HOURS",
        loginDate: "01/11/2026",
        loginTime: "06:00 AM",
        logoutDate: "01/11/2026",
        logoutTime: "08:15 PM",
        vehicleNo: "TS09 UB 3368",
        driverName: "Suman",
        driverPhone: "+91 93919 15013",
        trips: 16,
        earnings: 2950.0,
        cashCollection: 2503.0,
        expenditure: 200.0,
        commissionRent: 1032.5,
        commissionPercentage: 35.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 0.0,
        paymentOnline: 278.0,
        netBalance: 0.0,
        status: "SETTLED",
        remarks: "Settled online via UPI. 35% commission tier achieved.",
        managerId: "mgr_moosapet_01",
        source: "AUTOMATIC_TELEMETRY"
    },
    {
        id: "log_002",
        shiftType: "12_HOURS",
        loginDate: "01/11/2026",
        loginTime: "06:30 AM",
        logoutDate: "01/11/2026",
        logoutTime: "07:00 PM",
        vehicleNo: "TS09 UA 2271",
        driverName: "Narendra",
        driverPhone: "+91 99598 59347",
        trips: 7,
        earnings: 2271.0,
        cashCollection: 1850.0,
        expenditure: 150.0,
        commissionRent: 0.0,
        commissionPercentage: 0.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 1700.0,
        paymentOnline: 0.0,
        netBalance: 0.0,
        status: "SETTLED",
        remarks: "Trips (7) below 10 min threshold. Flat vehicle rent settled.",
        managerId: "mgr_moosapet_01",
        source: "MANUAL_REGISTER"
    },
    {
        id: "log_003",
        shiftType: "12_HOURS",
        loginDate: "01/11/2026",
        loginTime: "05:45 AM",
        logoutDate: "01/11/2026",
        logoutTime: "06:30 PM",
        vehicleNo: "TS09 UC 2733",
        driverName: "Pavan",
        driverPhone: "+91 98451 22710",
        trips: 11,
        earnings: 2733.0,
        cashCollection: 2100.0,
        expenditure: 100.0,
        commissionRent: 956.55,
        commissionPercentage: 35.0,
        drivePass: 50.0,
        advance: 0.0,
        paymentCash: 0.0,
        paymentOnline: 450.0,
        netBalance: 0.0,
        status: "SETTLED",
        remarks: "35% commission applied. On time check-out.",
        managerId: "mgr_moosapet_01",
        source: "AUTOMATIC_TELEMETRY"
    },
    {
        id: "log_004",
        shiftType: "12_HOURS",
        loginDate: "01/11/2026",
        loginTime: "06:15 AM",
        logoutDate: "01/11/2026",
        logoutTime: "07:45 PM",
        vehicleNo: "TS09 UD 3320",
        driverName: "Siva",
        driverPhone: "+91 97412 27330",
        trips: 12,
        earnings: 3320.0,
        cashCollection: 2800.0,
        expenditure: 250.0,
        commissionRent: 1162.0,
        commissionPercentage: 35.0,
        drivePass: 0.0,
        advance: 200.0,
        paymentCash: 0.0,
        paymentOnline: 350.0,
        netBalance: 0.0,
        status: "SETTLED",
        remarks: "High collection day. Settled via PhonePe.",
        managerId: "mgr_moosapet_01",
        source: "MANUAL_REGISTER"
    },
    {
        id: "log_005",
        shiftType: "12_HOURS",
        loginDate: "02/11/2026",
        loginTime: "05:30 AM",
        logoutDate: "02/11/2026",
        logoutTime: "06:45 PM",
        vehicleNo: "TS09 UB 2253",
        driverName: "Mahesh Goud",
        driverPhone: "+91 97410 33440",
        trips: 15,
        earnings: 3420.0,
        cashCollection: 2600.0,
        expenditure: 300.0,
        commissionRent: 1197.0,
        commissionPercentage: 35.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 0.0,
        paymentOnline: 510.0,
        netBalance: 0.0,
        status: "SETTLED",
        remarks: "Kukatpally hub register entry.",
        managerId: "mgr_kukatpally_02",
        source: "MANUAL_REGISTER"
    },
    {
        id: "log_006",
        shiftType: "12_HOURS",
        loginDate: "02/11/2026",
        loginTime: "06:00 AM",
        logoutDate: "02/11/2026",
        logoutTime: "08:00 PM",
        vehicleNo: "TS09 UB 4043",
        driverName: "Harish Naidu",
        driverPhone: "+91 97314 40400",
        trips: 17,
        earnings: 3740.0,
        cashCollection: 2900.0,
        expenditure: 250.0,
        commissionRent: 1496.0,
        commissionPercentage: 40.0,
        drivePass: 0.0,
        advance: 0.0,
        paymentCash: 0.0,
        paymentOnline: 620.0,
        netBalance: 0.0,
        status: "SETTLED",
        remarks: "Top earner: Achieved 40% Tier! Madhapur Hitec City routes.",
        managerId: "mgr_madhapur_03",
        source: "AUTOMATIC_TELEMETRY"
    }
];

// Live Telemetry Feed (Pending shifts that can be auto-aggregated)
const PENDING_TELEMETRY_SHIFTS = [
    {
        vehicleNo: "TS09 UE 2950",
        driverName: "Prasad",
        driverPhone: "+91 96321 33200",
        shiftType: "12_HOURS",
        trips: 14,
        earnings: 2950.0,
        cashCollection: 1980.0,
        expenditure: 220.0,
        managerId: "mgr_moosapet_01",
        capturedPlatforms: ["Uber Driver (9 trips)", "Rapido Captain (5 trips)"],
        lastSync: "3 mins ago"
    },
    {
        vehicleNo: "TS09 UB 2611",
        driverName: "Arun Teja",
        driverPhone: "+91 98801 55660",
        shiftType: "12_HOURS",
        trips: 18,
        earnings: 3880.0,
        cashCollection: 2750.0,
        expenditure: 300.0,
        managerId: "mgr_kukatpally_02",
        capturedPlatforms: ["Uber Driver (14 trips)", "Ola Driver (4 trips)"],
        lastSync: "Just now"
    },
    {
        vehicleNo: "TS09 UB 4222",
        driverName: "Chetan Sai",
        driverPhone: "+91 96114 50500",
        shiftType: "12_HOURS",
        trips: 15,
        earnings: 3260.0,
        cashCollection: 2400.0,
        expenditure: 180.0,
        managerId: "mgr_madhapur_03",
        capturedPlatforms: ["Uber Driver (15 trips)"],
        lastSync: "7 mins ago"
    }
];

// Master Data Repository Class with Supabase Cloud DB Connection
class FleetDataRepository {
    constructor() {
        this.supabase = null;
        this.isCloudConnected = false;
        this.init();
    }

    init() {
        if (!localStorage.getItem(APP_CONFIG.storageKeys.vehicles)) {
            localStorage.setItem(APP_CONFIG.storageKeys.vehicles, JSON.stringify(DEFAULT_VEHICLES));
        }
        if (!localStorage.getItem(APP_CONFIG.storageKeys.drivers)) {
            localStorage.setItem(APP_CONFIG.storageKeys.drivers, JSON.stringify(DEFAULT_DRIVERS));
        }
        if (!localStorage.getItem(APP_CONFIG.storageKeys.dutyLogs)) {
            localStorage.setItem(APP_CONFIG.storageKeys.dutyLogs, JSON.stringify(DEFAULT_DUTY_LOGS));
        }

        // Initialize Supabase client
        this.initSupabase();
    }

    initSupabase() {
        try {
            if (window.supabase && APP_CONFIG.supabase) {
                this.supabase = window.supabase.createClient(APP_CONFIG.supabase.url, APP_CONFIG.supabase.anonKey);
                // Mark connecting state; only mark live after successful query or subscription
                this.updateCloudStatusIndicator("CONNECTING");

                // Initial fetch from cloud DB
                this.syncWithSupabase();

                // Setup Realtime subscription
                this.setupRealtimeSubscription();
            } else {
                this.isCloudConnected = false;
                this.updateCloudStatusIndicator("OFFLINE");
            }
        } catch (e) {
            console.warn("Supabase initialization error:", e);
            this.isCloudConnected = false;
            this.updateCloudStatusIndicator("OFFLINE");
        }
    }

    updateCloudStatusIndicator(status) {
        // status: "ONLINE", "CONNECTING", "OFFLINE"
        const mobileEl = document.getElementById("db-cloud-indicator");
        const desktopEl = document.getElementById("db-cloud-indicator-desktop");
        
        const isOnline = status === "ONLINE" || status === true;
        const isConnecting = status === "CONNECTING";
        
        let label = "Cloud DB Live";
        let dotHtml = '<span class="pulse-dot"></span>';
        let colorClass = "bg-emerald-950/40 border-emerald-500/30 text-emerald-400";
        
        if (isConnecting) {
            label = "Connecting...";
            dotHtml = '<span class="w-2 h-2 rounded-full bg-indigo-400 animate-ping"></span>';
            colorClass = "bg-indigo-950/40 border-indigo-500/30 text-indigo-300";
        } else if (!isOnline) {
            label = "Offline Cache";
            dotHtml = '<span class="w-2 h-2 rounded-full bg-amber-400"></span>';
            colorClass = "bg-amber-950/40 border-amber-500/30 text-amber-400";
        }
        
        const content = `${dotHtml}<span class="text-[10px] font-bold uppercase tracking-wider">${label}</span>`;
        
        if (mobileEl) {
            mobileEl.innerHTML = content;
            mobileEl.className = `flex md:hidden items-center gap-1.5 px-2.5 py-1 rounded-full border shadow-sm ${colorClass}`;
        }
        if (desktopEl) {
            desktopEl.innerHTML = content;
            desktopEl.className = `hidden md:flex items-center gap-1.5 px-3 py-1.5 rounded-full border shadow-sm ${colorClass}`;
        }
    }

    async syncWithSupabase() {
        if (!this.supabase) {
            this.isCloudConnected = false;
            this.updateCloudStatusIndicator("OFFLINE");
            return;
        }

        try {
            const { data, error } = await this.supabase
                .from('office_duty_logs')
                .select('*')
                .order('created_at', { ascending: false });

            if (error) {
                console.warn("Supabase fetch duty logs error:", error);
                this.isCloudConnected = false;
                this.updateCloudStatusIndicator("OFFLINE");
                return;
            }

            // Connection succeeded!
            this.isCloudConnected = true;
            this.updateCloudStatusIndicator("ONLINE");

            if (data && data.length > 0) {
                // Map DB rows to client format
                const mappedLogs = data.map(row => {
                    const calc = SvmCommissionCalculator.calculate(
                        row.shift_type || '12_HOURS',
                        row.trips || 0,
                        row.earnings || 0,
                        row.cash_collection || 0,
                        row.expenditure || 0,
                        row.drive_pass || 0,
                        row.advance || 0
                    );

                    return {
                        id: row.id,
                        shiftType: row.shift_type || '12_HOURS',
                        loginDate: row.login_date,
                        loginTime: row.login_time,
                        logoutDate: row.logout_date,
                        logoutTime: row.logout_time,
                        vehicleNo: row.vehicle_no,
                        driverName: row.driver_name,
                        driverPhone: row.driver_phone || '+91 93919 15013',
                        trips: row.trips || 0,
                        earnings: parseFloat(row.earnings || 0),
                        cashCollection: parseFloat(row.cash_collection || 0),
                        expenditure: parseFloat(row.expenditure || 0),
                        commissionRent: parseFloat(row.commission_rent || calc.commissionAmount),
                        commissionPercentage: calc.commissionPercentage,
                        drivePass: parseFloat(row.drive_pass || 0),
                        advance: parseFloat(row.advance || 0),
                        paymentCash: parseFloat(row.payment_cash || 0),
                        paymentOnline: parseFloat(row.payment_online || 0),
                        netBalance: parseFloat(row.net_balance !== undefined ? row.net_balance : calc.netBalance),
                        status: row.status || 'SETTLED',
                        remarks: row.remarks || '',
                        managerId: row.manager_id || 'mgr_moosapet_01',
                        source: 'SUPABASE_CLOUD'
                    };
                });

                localStorage.setItem(APP_CONFIG.storageKeys.dutyLogs, JSON.stringify(mappedLogs));

                if (window.SVM_APP) {
                    window.SVM_APP.renderMetricCards();
                    window.SVM_APP.renderDutyRegister();
                }
            }
        } catch (err) {
            console.warn("Sync error:", err);
            this.isCloudConnected = false;
            this.updateCloudStatusIndicator("OFFLINE");
        }
    }

    setupRealtimeSubscription() {
        if (!this.supabase) return;

        try {
            this.supabase
                .channel('public:office_duty_logs')
                .on('postgres_changes', { event: '*', schema: 'public', table: 'office_duty_logs' }, payload => {
                    console.log('Realtime DB update received from Supabase:', payload);
                    this.syncWithSupabase();
                })
                .subscribe((status) => {
                    if (status === 'SUBSCRIBED') {
                        this.isCloudConnected = true;
                        this.updateCloudStatusIndicator("ONLINE");
                    } else if (status === 'CHANNEL_ERROR' || status === 'TIMED_OUT' || status === 'CLOSED') {
                        this.isCloudConnected = false;
                        this.updateCloudStatusIndicator("OFFLINE");
                    }
                });
        } catch (e) {
            console.warn("Realtime subscription error:", e);
            this.isCloudConnected = false;
            this.updateCloudStatusIndicator("OFFLINE");
        }
    }

    getVehicles(managerId = null) {
        const vehicles = JSON.parse(localStorage.getItem(APP_CONFIG.storageKeys.vehicles) || "[]");
        if (!managerId || managerId === "ALL") return vehicles;
        return vehicles.filter(v => v.managerId === managerId);
    }

    getDrivers(managerId = null) {
        const drivers = JSON.parse(localStorage.getItem(APP_CONFIG.storageKeys.drivers) || "[]");
        if (!managerId || managerId === "ALL") return drivers;
        return drivers.filter(d => d.managerId === managerId);
    }

    getDutyLogs(managerId = null) {
        const logs = JSON.parse(localStorage.getItem(APP_CONFIG.storageKeys.dutyLogs) || "[]");
        if (!managerId || managerId === "ALL") return logs;
        return logs.filter(l => l.managerId === managerId);
    }

    async saveDutyLog(logData) {
        const logs = this.getDutyLogs();
        if (!logData.id) {
            logData.id = "log_" + Date.now();
        }

        const existingIdx = logs.findIndex(l => l.id === logData.id);
        if (existingIdx >= 0) {
            logs[existingIdx] = logData;
        } else {
            logs.unshift(logData);
        }
        localStorage.setItem(APP_CONFIG.storageKeys.dutyLogs, JSON.stringify(logs));

        // Asynchronously persist to Supabase Cloud DB
        if (this.supabase) {
            try {
                const dbPayload = {
                    fleet_id: 'svm_fleet_hyderabad',
                    shift_type: logData.shiftType,
                    login_date: logData.loginDate,
                    login_time: logData.loginTime,
                    logout_date: logData.logoutDate,
                    logout_time: logData.logoutTime,
                    vehicle_no: logData.vehicleNo,
                    driver_name: logData.driverName,
                    driver_phone: logData.driverPhone,
                    trips: logData.trips,
                    earnings: logData.earnings,
                    cash_collection: logData.cashCollection,
                    expenditure: logData.expenditure,
                    commission_rent: logData.commissionRent,
                    drive_pass: logData.drivePass || 0,
                    advance: logData.advance || 0,
                    payment_cash: logData.paymentCash || 0,
                    payment_online: logData.paymentOnline || 0,
                    net_balance: logData.netBalance,
                    status: logData.status || 'SETTLED',
                    remarks: logData.remarks || '',
                    manager_id: logData.managerId
                };

                const { data, error } = await this.supabase
                    .from('office_duty_logs')
                    .insert(dbPayload)
                    .select();

                if (error) {
                    console.warn("Supabase insert error:", error);
                    this.isCloudConnected = false;
                    this.updateCloudStatusIndicator("OFFLINE");
                } else if (data && data[0]) {
                    this.isCloudConnected = true;
                    this.updateCloudStatusIndicator("ONLINE");
                    logData.id = data[0].id;
                    logData.source = 'SUPABASE_CLOUD';
                    localStorage.setItem(APP_CONFIG.storageKeys.dutyLogs, JSON.stringify(logs));
                }
            } catch (err) {
                console.warn("Supabase write error:", err);
                this.isCloudConnected = false;
                this.updateCloudStatusIndicator("OFFLINE");
            }
        }

        return logData;
    }

    async saveDriver(driverData) {
        const drivers = this.getDrivers();
        if (!driverData.id) {
            driverData.id = Date.now();
        }
        drivers.push(driverData);
        localStorage.setItem(APP_CONFIG.storageKeys.drivers, JSON.stringify(drivers));

        // If an existing vehicle was assigned, link it to this driver
        if (driverData.vehicleNo) {
            const vehicles = this.getVehicles();
            const v = vehicles.find(veh => veh.regNo.toUpperCase() === driverData.vehicleNo.toUpperCase());
            if (v) {
                v.driverId = driverData.id;
                v.driverName = driverData.name;
                v.managerId = driverData.managerId;
                localStorage.setItem(APP_CONFIG.storageKeys.vehicles, JSON.stringify(vehicles));
            }
        }

        return driverData;
    }

    saveHub(hubData) {
        if (!hubData.id) {
            const cleanShort = (hubData.shortName || "hub").toLowerCase().replace(/[^a-z0-9]/g, "_");
            hubData.id = "mgr_" + cleanShort + "_" + Date.now().toString().slice(-4);
        }
        if (!APP_CONFIG.hubs.some(h => h.id === hubData.id)) {
            APP_CONFIG.hubs.push(hubData);
        }
        const customHubs = JSON.parse(localStorage.getItem(APP_CONFIG.storageKeys.customHubs) || "[]");
        customHubs.push(hubData);
        localStorage.setItem(APP_CONFIG.storageKeys.customHubs, JSON.stringify(customHubs));
        return hubData;
    }

    reassignVehicle(vehicleId, newManagerId) {
        const vehicles = this.getVehicles();
        const v = vehicles.find(item => item.id === parseInt(vehicleId));
        if (v) {
            v.managerId = newManagerId;
            localStorage.setItem(APP_CONFIG.storageKeys.vehicles, JSON.stringify(vehicles));
            if (v.driverId) {
                const drivers = this.getDrivers();
                const d = drivers.find(item => item.id === v.driverId);
                if (d) {
                    d.managerId = newManagerId;
                    localStorage.setItem(APP_CONFIG.storageKeys.drivers, JSON.stringify(drivers));
                }
            }
            return true;
        }
        return false;
    }

    resetToDefaults() {
        localStorage.setItem(APP_CONFIG.storageKeys.vehicles, JSON.stringify(DEFAULT_VEHICLES));
        localStorage.setItem(APP_CONFIG.storageKeys.drivers, JSON.stringify(DEFAULT_DRIVERS));
        localStorage.setItem(APP_CONFIG.storageKeys.dutyLogs, JSON.stringify(DEFAULT_DUTY_LOGS));
    }
}

window.FleetDataRepository = new FleetDataRepository();
window.PENDING_TELEMETRY_SHIFTS = PENDING_TELEMETRY_SHIFTS;
