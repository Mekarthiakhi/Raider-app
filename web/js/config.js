/**
 * SVM FLEET Operating System (Web Portal)
 * Global Configuration & Supabase Cloud Database Credentials
 */

const APP_CONFIG = {
    appName: "SVM FLEET Operating System",
    version: "v2.0 PRO (Executive Glassmorphism Edition)",
    city: "Hyderabad, Telangana",
    masterPin: "7777",
    
    // Supabase Cloud Backend Configuration
    supabase: {
        url: "https://nwcfwqiymqzzgrzgegty.supabase.co",
        anonKey: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im53Y2Z3cWl5bXF6emdyemdlZ3R5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTM0NjQwODksImV4cCI6MjEwOTA0MDA4OX0.1OFgMnay_qnHK8AZbStplhjh-1gJM1epRPEIs8_6uzA"
    },

    // Owner Master Account
    owner: {
        name: "Akhilesh (Fleet Owner)",
        phone: "9876543210",
        role: "OWNER",
        avatar: "👑"
    },

    // Hyderabad Hubs & Assigned Managers
    hubs: [
        {
            id: "mgr_moosapet_01",
            name: "Moosapet Central Hub",
            shortName: "Moosapet",
            managerName: "Srinivas Rao",
            phone: "9849012345",
            pin: "1234",
            lat: 17.4652,
            lng: 78.4308,
            color: "#6366F1",
            cabsCount: 12
        },
        {
            id: "mgr_kukatpally_02",
            name: "Kukatpally Y-Junction Hub",
            shortName: "Kukatpally",
            managerName: "Venkatesh Naidu",
            phone: "9701234567",
            pin: "2345",
            lat: 17.4935,
            lng: 78.4012,
            color: "#0EA5E9",
            cabsCount: 10
        },
        {
            id: "mgr_madhapur_03",
            name: "Madhapur Hitec City Hub",
            shortName: "Madhapur",
            managerName: "Mohammed Farooq",
            phone: "9989056789",
            pin: "3456",
            lat: 17.4485,
            lng: 78.3752,
            color: "#10B981",
            cabsCount: 8
        }
    ],

    // Default Map Settings (Free OpenStreetMap with Night Filter — No API Key Required)
    map: {
        defaultCenter: [17.4400, 78.4200],
        defaultZoom: 12,
        tileLayerUrl: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
        attribution: '&copy; OpenStreetMap contributors | SVM FLEET Hyderabad'
    },

    // Earnings Benchmark Defaults (Day, Week, Month-End)
    earningsBenchmarks: {
        day: {
            gross: 84250,
            driverComm: 31450,
            fuelExp: 9800,
            netProfit: 43000,
            activeShifts: 27,
            trips: 348,
            uberPct: 54,
            rapidoPct: 28,
            olaPct: 18
        },
        week: {
            gross: 589750,
            driverComm: 218500,
            fuelExp: 68200,
            netProfit: 303050,
            activeShifts: 182,
            trips: 2420,
            uberPct: 52,
            rapidoPct: 30,
            olaPct: 18
        },
        month: {
            gross: 2480000,
            driverComm: 915000,
            fuelExp: 285000,
            netProfit: 1280000,
            activeShifts: 780,
            trips: 10450,
            uberPct: 55,
            rapidoPct: 27,
            olaPct: 18
        }
    },

    // Storage Keys
    storageKeys: {
        role: "svm_active_role",
        hubId: "svm_active_hub_id",
        dutyLogs: "svm_duty_logs_v1",
        vehicles: "svm_vehicles_v1",
        drivers: "svm_drivers_v1",
        customHubs: "svm_custom_hubs"
    }
};

// Merge any custom hubs created by Fleet Owner from localStorage
try {
    const customHubsStr = localStorage.getItem("svm_custom_hubs");
    if (customHubsStr) {
        const customHubs = JSON.parse(customHubsStr);
        if (Array.isArray(customHubs)) {
            customHubs.forEach(ch => {
                if (!APP_CONFIG.hubs.some(h => h.id === ch.id)) {
                    APP_CONFIG.hubs.push(ch);
                }
            });
        }
    }
} catch (e) {
    console.warn("Could not load custom hubs:", e);
}

window.APP_CONFIG = APP_CONFIG;
