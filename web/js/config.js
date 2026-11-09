/**
 * SVM FLEET Operating System (Web Portal)
 * Global Configuration & Supabase Cloud Database Credentials
 */

const APP_CONFIG = {
    appName: "SVM FLEET Operating System",
    version: "v1.0.43 (Web Edition)",
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
            color: "#5B36F5",
            badgeClass: "badge-purple"
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
            color: "#38BDF8",
            badgeClass: "badge-blue"
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
            color: "#2ED3A0",
            badgeClass: "badge-green"
        }
    ],

    // Default Map Settings (Hyderabad Central)
    map: {
        defaultCenter: [17.4400, 78.4200],
        defaultZoom: 12,
        tileLayerUrl: "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png",
        attribution: '&copy; <a href="https://carto.com/">CARTO</a> | SVM FLEET Hyderabad'
    },

    // Storage Keys
    storageKeys: {
        role: "svm_active_role",
        hubId: "svm_active_hub_id",
        dutyLogs: "svm_duty_logs_v1",
        vehicles: "svm_vehicles_v1",
        drivers: "svm_drivers_v1"
    }
};

window.APP_CONFIG = APP_CONFIG;
