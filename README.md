# Fleet Ride Tracker (30 Cars) 🚖

A complete, production-grade telemetry and accounting platform for fleet owners/captains managing 30+ vehicles across **Rapido**, **Uber**, and **Ola**.

---

## 🏗️ System Overview

The system operates across three interconnected layers:

1. **Driver Companion App (Android 11+, Target SDK 35)**:
   - Quiet background telemetry using Android `AccessibilityService`.
   - Reads driver app windows exclusively (`com.rapido.rider`, `com.ubercab.driver`, `com.olacabs.oladriver`).
   - Captures trip accepted, in-trip, and completed events with fare, distance (km), duration, and pickup/drop.
   - On-device ML Kit OCR fallback when view IDs are missing or obfuscated.
   - Idempotent local Room database queue with offline outbox sync.

2. **Cloud Backend (Supabase PostgreSQL & Realtime)**:
   - Multi-tenant architecture with Row Level Security (RLS) isolating fleet data by owner.
   - Granular device tokens restricting driver phones to insert only their own vehicle's trips.
   - Real-time ride streaming to captain dashboard.
   - Automated heartbeat monitoring (> 15m silence detection).
   - Remote selector packs table enabling over-the-air screen parser updates without APK reinstalls.
   - Weekly reconciliation against platform statement payouts.

3. **Fleet Captain / Owner App (Jetpack Compose, Material 3)**:
   - Live 30-car fleet board with status dots (On Trip / Idle / Offline).
   - Realtime ride feed with platform badges and confidence scores.
   - Per-vehicle daily timeline and earnings per kilometer analytics.
   - Captain review queue for approving low-confidence trips.
   - Daily, weekly, and monthly accounting reports with CSV and PDF export.

---

## 🎨 Design System

- **Dark-First Theme**: Background `#0B0D12`, Surface `#12151C`, Border `#232837`.
- **Accents**: Emerald `#2ED3A0` (Active/Online), Gold `#E9B949` (Earnings/Totals), Red `#FF5C6C` (Alerts/Needs Review).
- **Platform Chips**: Rapido Yellow, Uber White, Ola Lime.
- **Glassmorphic Cards**: Subtle 1px borders, smooth animated number counters, tabular numerals.

---

## 📂 Repository Structure

```
raider-app/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/fleettrack/app/
│   │       ├── FleetTrackApp.kt
│   │       ├── MainActivity.kt
│   │       ├── data/
│   │       │   ├── local/          # Room DB, Entities (6), DAOs (6), Converters
│   │       │   ├── model/          # Platform, RideStatus, Confidence, Summary
│   │       │   ├── remote/         # Supabase Client, Remote Data Sources
│   │       │   └── repository/     # Ride, Vehicle, Driver, Device Repositories
│   │       ├── di/                 # Hilt Dependency Injection Modules
│   │       └── ui/
│   │           ├── components/     # GlassCard, PlatformChip, EarningsText, StatusDot
│   │           ├── driver/         # Setup Wizard, Driver Today, DPDP Privacy
│   │           ├── navigation/     # NavHost, BottomNavBars, Screen routes
│   │           ├── owner/          # FleetBoard, LiveFeed, VehicleDetail, ReviewQueue, Reports, Alerts
│   │           └── theme/          # Color, Type, Shape, Theme
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          # Centralized version catalog
├── supabase/
│   └── migrations/
│       ├── 001_create_tables.sql
│       ├── 002_rls_policies.sql
│       ├── 003_views_and_functions.sql
│       └── 004_seed_remote_selectors.sql
├── settings.gradle.kts
├── build.gradle.kts
└── README.md
```

---

## 🚀 Quick Start

### 1. Database Setup
1. Create a project at [supabase.com](https://supabase.com).
2. Execute the migrations in `supabase/migrations/` in sequential order:
   - `001_create_tables.sql`
   - `002_rls_policies.sql`
   - `003_views_and_functions.sql`
   - `004_seed_remote_selectors.sql`

### 2. Android App Configuration
Add your Supabase project keys to `local.properties`:
```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

### 3. Build & Install
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛡️ Privacy & Compliance (DPDP Act 2023)

- **Ride Apps Only**: Telemetry runs exclusively when Rapido, Uber, or Ola driver packages are focused.
- **Strictly No Surveillance**: Zero access to SMS, contacts, personal chats, calls, camera, or personal gallery.
- **Consent Driven**: Drivers are shown an in-app transparency disclosure with consent timestamps recorded on the backend.

