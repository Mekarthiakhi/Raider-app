import os
import sys
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable, Preformatted
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_decorations(self, total_pages):
        self.saveState()
        # Suppress running header on page 1
        if self._pageNumber > 1:
            # Running Header
            self.setFont("Helvetica-Bold", 8)
            self.setFillColor(colors.HexColor("#5B36F5"))
            self.drawString(40, 810, "SVM FLEET (RAIDER APP)")
            self.setFont("Helvetica", 8)
            self.setFillColor(colors.HexColor("#6B7280"))
            self.drawString(160, 810, "•  System Architecture & Complete Technical Blueprint")
            self.setStrokeColor(colors.HexColor("#E5E7EB"))
            self.setLineWidth(0.5)
            self.line(40, 804, 555, 804)

        # Running Footer (All pages)
        self.setStrokeColor(colors.HexColor("#E5E7EB"))
        self.setLineWidth(0.5)
        self.line(40, 40, 555, 40)
        
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#6B7280"))
        self.drawString(40, 28, "Confidential • Moosapet Central Hub Operations • Version 1.0.43")
        
        page_str = f"Page {self._pageNumber} of {total_pages}"
        self.drawRightString(555, 28, page_str)
        self.restoreState()

def build_pdf(filename):
    doc = SimpleDocTemplate(
        filename,
        pagesize=A4,
        leftMargin=40,
        rightMargin=40,
        topMargin=50,
        bottomMargin=50
    )

    styles = getSampleStyleSheet()

    # Brand Colors
    PRIMARY = colors.HexColor("#5B36F5")      # Electric Purple
    PRIMARY_DARK = colors.HexColor("#130A3A") # Deep Night
    SECONDARY = colors.HexColor("#00C853")    # Emerald Active Green
    TEXT_DARK = colors.HexColor("#1A1D26")
    TEXT_MUTED = colors.HexColor("#5E6472")
    BG_LIGHT = colors.HexColor("#F7F8FC")
    BORDER_COLOR = colors.HexColor("#E2E5EE")

    # Custom Typography Styles
    h1_style = ParagraphStyle(
        'Header1',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=17,
        textColor=PRIMARY,
        spaceBefore=12,
        spaceAfter=5
    )

    h2_style = ParagraphStyle(
        'Header2',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10,
        leading=14,
        textColor=PRIMARY_DARK,
        spaceBefore=7,
        spaceAfter=3
    )

    body_style = ParagraphStyle(
        'BodyDark',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=12.5,
        textColor=TEXT_DARK
    )

    bullet_style = ParagraphStyle(
        'BulletDark',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8,
        leading=11.5,
        textColor=TEXT_DARK,
        leftIndent=10,
        spaceAfter=2
    )

    code_tree_style = ParagraphStyle(
        'CodeTree',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=6.5,
        leading=8.5,
        textColor=colors.HexColor("#0F172A")
    )

    story = []

    # =========================================================================
    # PAGE 1: COVER / HEADER BANNER & WHAT WE HAVE DONE
    # =========================================================================
    header_table_data = [
        [
            Paragraph("<b>SVM FLEET (RAIDER APP)</b>", ParagraphStyle('BTitle', fontName='Helvetica-Bold', fontSize=18, textColor=colors.white)),
            Paragraph("<b>VERSION 1.0.43</b><br/>HYDERABAD FLEET OS", ParagraphStyle('BSub', fontName='Helvetica-Bold', fontSize=8.5, textColor=colors.HexColor("#A78BFA"), alignment=2))
        ],
        [
            Paragraph("Comprehensive System Documentation, Implementation History & Technical Architecture", ParagraphStyle('BDesc', fontName='Helvetica', fontSize=9.5, textColor=colors.HexColor("#E2E8F0"))),
            Paragraph("Date: November 2026", ParagraphStyle('BDate', fontName='Helvetica', fontSize=8, textColor=colors.HexColor("#CBD5E1"), alignment=2))
        ]
    ]
    header_table = Table(header_table_data, colWidths=[380, 135])
    header_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), PRIMARY_DARK),
        ('PADDING', (0,0), (-1,-1), 10),
        ('BOTTOMPADDING', (0,1), (-1,1), 12),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ]))
    story.append(header_table)
    story.append(Spacer(1, 10))

    summary_box_data = [[
        Paragraph(
            "<b>Executive Overview:</b> SVM FLEET is an enterprise dual-interface fleet operating system purpose-built "
            "for commercial fleet operators in Hyderabad managing cabs across <b>Uber, Rapido, and Ola</b>. "
            "The system unites real-time background accessibility telemetry (zero driver typing), automated fare capture, "
            "dual-store data persistence (Room SQLite + Supabase Cloud PostgreSQL), live GPS fleet tracking, "
            "a digital Office Duty Register replacing physical logbooks, automated 70/30 commission & fuel accounting, "
            "and instant A4 PDF / CSV statements.",
            ParagraphStyle('SumBoxText', fontName='Helvetica', fontSize=8, leading=11.5, textColor=TEXT_DARK)
        )
    ]]
    summary_box = Table(summary_box_data, colWidths=[515])
    summary_box.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), BG_LIGHT),
        ('BOX', (0,0), (-1,-1), 1, BORDER_COLOR),
        ('PADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(summary_box)
    story.append(Spacer(1, 8))

    story.append(Paragraph("1. Complete Implementation Breakdown (What Was Built)", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=PRIMARY, spaceAfter=6))

    work_items_p1 = [
        ("A. Brand Identity & Intro Carousel (Figma Mockups Sheet 1 & 2)", [
            "<b>Electric Purple (#5B36F5) Adaptive App Icon:</b> Vector launcher foreground with centered crisp white vehicle emblem on electric purple background.",
            "<b>Intro Splash Screen Carousel:</b> Deep Night purple gradient (#130A3A), perspective animated road lines, pulsing vehicle emblem, 3-dot pagination slider (● ○ ○), and primary 'Enter Fleet Manager' CTA.",
            "<b>Unified Design System:</b> Custom theme palette (Electric Purple, Emerald Green #00C853, Inactive Red #E53935, Surface GlassCards, Pill-shaped controls)."
        ]),
        ("B. Authentication & Dual Access Gateway", [
            "<b>Dual-Mode Tabs:</b> Switch seamlessly between Fleet Owner Portal and Driver Shift Companion.",
            "<b>Owner Security Master PIN:</b> Authorized phone authentication + editable Owner Secret Key (Default: 7777).",
            "<b>In-App Secure OTP & Fast2SMS:</b> Instant in-app 4-digit code generator + Fast2SMS / WhatsApp gateway integration.",
            "<b>1-Tap Showcase Demo Mode:</b> Immediate presentation mode with 30 cars, 28 active, live metrics, and historical logs."
        ]),
        ("C. Driver Shift Companion & Automated Telemetry Engine", [
            "<b>3-Step Setup Wizard:</b> Guides driver through Battery Exemption (Unrestricted 24/7 run), Accessibility Service toggle, and DPDP Act 2023 Consent.",
            "<b>Silent Ride Ingestion (RideTrackerAccessibilityService):</b> Captures fares, distance (km), duration, and pickups/dropoffs from Uber Driver and Rapido Captain without driver manual entry.",
            "<b>DynamicSelectorEngine & RideStateMachine:</b> Self-healing dynamic UI tree parser with regex fallback for Uber, Rapido, and Ola screens, ensuring zero ride loss."
        ]),
        ("D. Dual-Storage & Cloud Realtime Architecture", [
            "<b>Offline-First Local SQLite (Room):</b> Tables for vehicles, drivers, rides, ride events, duty logs, and audit trails.",
            "<b>Supabase Realtime Cloud Sync:</b> Integrated postgrest-kt and realtime-kt upserting rides and duty logs directly to PostgreSQL with automatic reconnection and channel subscription ('fleet_telemetry_live')."
        ]),
        ("E. Office Duty Register & Commission Accounting", [
            "<b>Digital Logbook (OfficeRegisterScreen):</b> Replaces traditional Moosapet paper logbooks with digital shift logging.",
            "<b>SvmCommissionCalculator:</b> Automatic calculation of opening/closing km, fuel deduction, daily commission/rent, and driver net settlement.",
            "<b>Shift Rotation & Driver Handoff:</b> Car-level shift handover modal (Day Shift 06 AM - 06 PM vs Night Shift 06 PM - 06 AM) with starting odometer validation."
        ]),
        ("F. Live Fleet Tracking & Interactive Map", [
            "<b>Custom Canvas Map (LiveTrackingScreen):</b> Stylized Hyderabad street network (Banjara Hills, Jubilee Hills, Hitec City) with animated radar pulses, route trails, and active/idle car markers.",
            "<b>Floating Status Card:</b> Selected vehicle details (TS09 AB 1234 • On Trip • 8.4 km away), live speed (42 km/h), current fare, and one-tap driver dialer."
        ]),
        ("G. Document Vault & Native Statement Export", [
            "<b>Digital Document Vault (DocumentVaultScreen):</b> Digital compliance cards for RC, Commercial Insurance (ICICI Lombard), PUC, Fitness, and State Permits.",
            "<b>A4 Financial Statement Generator:</b> Programmatic PDF generator using Android PdfDocument drawing purple header banners, owner breakdown, and tabular car metrics.",
            "<b>RFC4180 CSV & FileProvider:</b> Full CSV export shared securely via Android FileProvider to WhatsApp, Drive, Gmail, or Excel."
        ])
    ]

    for title, bullets in work_items_p1:
        story.append(Paragraph(title, h2_style))
        for bullet in bullets:
            story.append(Paragraph(f"• {bullet}", bullet_style))
        story.append(Spacer(1, 2))

    story.append(PageBreak())

    # =========================================================================
    # PAGE 2: HOW IT WORKS (END-TO-END PIPELINE & ARCHITECTURE)
    # =========================================================================
    story.append(Paragraph("2. How It Works (End-to-End Pipeline & Architecture)", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=PRIMARY, spaceAfter=8))

    story.append(Paragraph(
        "The SVM FLEET platform operates through a coordinated sequence of distributed background services, "
        "local state machines, and real-time cloud channels. The system lifecycle is divided into 5 distinct operational stages:",
        body_style
    ))
    story.append(Spacer(1, 8))

    steps_data = [
        ["Stage", "Trigger / Process", "Underlying Component", "Result"],
        [
            "1. Driver Onboard",
            "Owner creates driver profile in Owner Portal with DL, phone, and car assignment.",
            "DriverRepository.kt\nVehicleRepository.kt",
            "Synced to Room DB & Supabase 'drivers' table. Phone is pre-authorized."
        ],
        [
            "2. Shift Start",
            "Driver logs in with OTP, selects assigned car, and taps 'Start Shift & Begin Tracking'.",
            "LoginViewModel.kt\nShiftManager.kt",
            "Shift state set to ACTIVE. Foreground service notifies OEM OS to prevent killing."
        ],
        [
            "3. Silent Ingestion",
            "Driver accepts and completes trips in Uber Driver or Rapido Captain app.",
            "RideTrackerAccessibilityService\nDynamicSelectorEngine.kt",
            "Accessibility tree parsed. Fares (₹), KM, platform, and dropoff extracted silently."
        ],
        [
            "4. Validation & Store",
            "State machine verifies ride transitions (OFFER → ACCEPTED → ON_TRIP → COMPLETED).",
            "RideStateMachine.kt\nRideDao.kt (Room)",
            "Ride stored in local SQLite cache. Immediate offline availability ensured."
        ],
        [
            "5. Cloud Sync & P&L",
            "Supabase real-time manager pushes trip to Supabase; updates Owner Board & Register.",
            "SupabaseRealtimeSyncManager\nDutyLogRepository.kt",
            "Owner Dashboard updates in real time; P&L split (70% Driver / 30% Owner) calculated."
        ]
    ]

    steps_table = Table(steps_data, colWidths=[70, 160, 135, 150])
    steps_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), PRIMARY_DARK),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('FONTSIZE', (0,0), (-1,0), 8),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('BACKGROUND', (0,1), (-1,1), colors.white),
        ('BACKGROUND', (0,2), (-1,2), BG_LIGHT),
        ('BACKGROUND', (0,3), (-1,3), colors.white),
        ('BACKGROUND', (0,4), (-1,4), BG_LIGHT),
        ('BACKGROUND', (0,5), (-1,5), colors.white),
        ('PADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(steps_table)
    story.append(Spacer(1, 10))

    story.append(Paragraph("Security, Fault-Tolerance & Regulatory Safeguards", h2_style))
    safeguards = [
        "<b>DPDP Act 2023 Compliance:</b> Telemetry is strictly restricted to commercial taxi platforms (com.ubercab.driver, com.rapido.captain, com.olacabs.driver). Personal messaging (WhatsApp), banking, and browser screens are mathematically ignored by package whitelist filters.",
        "<b>Zero-Loss Offline Caching:</b> If network connectivity drops in transit, trips and duty logs remain in Room SQLite. The WorkManager 'OutboxSyncWorker' automatically batches and flushes pending records upon network restoration.",
        "<b>Aggressive Task-Killer Defense:</b> Android 12–15 aggressive OEM power-savers are bypassed via `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` and sticky foreground service execution with Wakelocks.",
        "<b>Owner Master PIN Protection:</b> Direct in-app secret PIN management enables changing hub access credentials dynamically without re-compiling the APK."
    ]
    for s in safeguards:
        story.append(Paragraph(f"• {s}", bullet_style))

    story.append(PageBreak())

    # =========================================================================
    # PAGE 3: CODEBASE DIRECTORY TREE - PART 1
    # =========================================================================
    story.append(Paragraph("3. Codebase Architectural Tree — Part 1 (Data & Service Layer)", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=PRIMARY, spaceAfter=6))

    tree_part1 = """raider-app/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml                    # Permissions, Accessibility Service & FileProvider
│   │   └── java/com/fleettrack/app/
│   │       ├── FleetTrackApp.kt                   # Hilt Application entry point & WorkManager scheduler
│   │       ├── MainActivity.kt                    # Single-Activity host, Jetpack Compose navigation
│   │       │
│   │       ├── data/                              # Data Architecture (Offline Room + Remote Supabase)
│   │       │   ├── local/
│   │       │   │   ├── FleetDatabase.kt           # Room Database with RideDao, DriverDao, DutyLogDao
│   │       │   │   ├── FleetDataSeeder.kt         # Comprehensive 30-car fleet & historical demo seeder
│   │       │   │   ├── dao/
│   │       │   │   │   ├── DutyLogDao.kt          # Office logbook entries, cash, fuel, commission queries
│   │       │   │   │   ├── DriverDao.kt           # Driver records and shift assignment DAO
│   │       │   │   │   ├── RideDao.kt             # Captured trip records and sync status DAO
│   │       │   │   │   └── VehicleDao.kt          # Fleet vehicles and active status DAO
│   │       │   │   └── entity/
│   │       │   │       ├── DriverEntity.kt        # Driver phone, license, fleet ID schema
│   │       │   │       ├── DutyLogEntity.kt       # Opening/closing km, fuel, rent, cash balance schema
│   │       │   │       ├── RideEntity.kt          # Fare, platform, duration, coordinates, status schema
│   │       │   │       └── VehicleEntity.kt       # Reg number, nickname, compliance documents schema
│   │       │   ├── model/                         # Domain models: DriverPerformance, FleetOverview, VehicleStatus
│   │       │   ├── remote/                        # Cloud Sync (Supabase Postgrest & WebSockets)
│   │       │   │   ├── SupabaseClientProvider.kt  # Supabase client singleton (Realtime & Postgrest)
│   │       │   │   ├── SupabaseRealtimeSyncManager.kt # Realtime channel 'fleet_telemetry_live' listener
│   │       │   │   ├── RideRemoteDataSource.kt    # Postgrest ride upsert with offline fallback
│   │       │   │   └── DeviceRemoteDataSource.kt  # Heartbeat telemetry and battery reporting
│   │       │   ├── repository/                    # Clean Architecture Repositories
│   │       │   │   ├── DriverRepository.kt        # Driver CRUD, duplicate phone check, cloud sync
│   │       │   │   ├── DutyLogRepository.kt       # Office register duty logs & financial settlements
│   │       │   │   ├── RideRepository.kt          # Trip queries, aggregation, pending outbox sync
│   │       │   │   └── VehicleRepository.kt       # Vehicle status, fleet metrics, document vault
│   │       │   └── session/
│   │       │       └── ShiftManager.kt            # Active driver shift state & local preferences
│   │       │
│   │       ├── di/                                # Dependency Injection (Hilt / Dagger)
│   │       │   ├── AppModule.kt                   # Database, network, and application bindings
│   │       │   └── RepositoryModule.kt            # Repository interfaces to implementation bindings
│   │       │
│   │       ├── service/                           # Telemetry Engine & Screen Scraping
│   │       │   ├── RideTrackerAccessibilityService.kt # Android Accessibility Service for Uber/Rapido/Ola
│   │       │   ├── parser/
│   │       │   │   ├── DynamicSelectorEngine.kt   # Self-healing dynamic UI tree parser
│   │       │   │   ├── AccessibilityNodeUtils.kt  # Node hierarchy traversal & text extractors
│   │       │   │   ├── UberDriverParser.kt        # Uber fare, distance, pickup/dropoff regex parser
│   │       │   │   ├── RapidoCaptainParser.kt     # Rapido order fare & route extractor
│   │       │   │   └── OlaDriverParser.kt         # Ola driver ride card parser
│   │       │   └── statemachine/
│   │       │       └── RideStateMachine.kt        # State machine (OFFER -> ACCEPTED -> ON_TRIP -> COMPLETED)"""

    tree_box1 = Table([[Preformatted(tree_part1, code_tree_style)]], colWidths=[515])
    tree_box1.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#CBD5E1")),
        ('PADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(tree_box1)

    story.append(PageBreak())

    # =========================================================================
    # PAGE 4: CODEBASE DIRECTORY TREE - PART 2 & UTILITIES
    # =========================================================================
    story.append(Paragraph("3. Codebase Architectural Tree — Part 2 (UI, Utilities & Root)", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=PRIMARY, spaceAfter=6))

    tree_part2 = """│       ├── ui/                                # Jetpack Compose UI Presentation Layer
│       │   ├── auth/                          # LoginScreen (Dual Tab, OTP, Secret Key), LoginViewModel
│       │   ├── intro/                         # IntroSplashScreen (3-dot carousel, Deep Night gradient)
│       │   ├── navigation/                    # FleetNavigation, Screen routes, BottomNavBar
│       │   ├── driver/                        # Driver Shift Companion (Today, SetupWizard, Privacy)
│       │   ├── debug/                         # NodeInspectorScreen (Realtime accessibility tree viewer)
│       │   ├── owner/                         # Fleet Owner Portal Modules
│       │   │   ├── dashboard/                 # DashboardScreen (Fleet overview, earnings, Active 28)
│       │   │   ├── cars/                      # CarsListScreen (30 cars, filters, AddCarDialog)
│       │   │   ├── detail/                    # VehicleDetailScreen (Overview, Trips, Earnings, Handoff)
│       │   │   ├── drivers/                   # DriversScreen, DriverDetailScreen (P&L, shift rota)
│       │   │   ├── tracking/                  # LiveTrackingScreen (Hyderabad Canvas map & pins)
│       │   │   ├── officeregister/            # OfficeRegisterScreen (Digital duty book, cash, fuel)
│       │   │   ├── expenses/                  # ExpensesScreen (Donut chart), AddFuelExpenseScreen
│       │   │   ├── maintenance/               # MaintenanceScreen (Expiry alert), AddServiceScreen
│       │   │   ├── vault/                     # DocumentVaultScreen (Digital RC, Insurance, PUC cards)
│       │   │   ├── reports/                   # ReportsScreen (Revenue, expenses, driver payout analytics)
│       │   │   ├── alerts/                    # AlertsScreen (Service due, insurance expiry notifications)
│       │   │   ├── settings/                  # SettingsScreen (Owner Master PIN, battery, cloud sync)
│       │   │   └── more/                      # MoreScreen (Fleet operations menu directory)
│       │   ├── components/                    # Reusable UI (GlassCard, PlatformChip, ConfidenceBadge)
│       │   └── theme/                         # Theme.kt, Color.kt (#5B36F5), Shape.kt, Type.kt
│       │
│       ├── util/                              # Helpers & Utility Engines
│       │   ├── StatementExportHelper.kt       # Native A4 PDF & RFC4180 CSV export via FileProvider
│       │   ├── SvmCommissionCalculator.kt     # Commission base, fuel allowance, and settlement math
│       │   ├── Fast2SmsHelper.kt              # SMS gateway client for OTP dispatch
│       │   ├── WhatsAppHelper.kt              # WhatsApp dispatch for driver settlement slips
│       │   ├── AppUpdateHelper.kt             # Self-hosted GitHub APK release updater
│       │   └── BatteryOptimizationHelper.kt   # Unrestricted background run permission launcher
│       │
│       └── work/                              # Background Workers (AndroidX WorkManager)
│           ├── OutboxSyncWorker.kt            # Periodic background flush of offline rides
│           ├── DeviceHeartbeatWorker.kt       # Hourly device health, battery, and status heartbeat
│           └── WorkManagerScheduler.kt        # Worker lifecycle initializer
│
├── .github/workflows/build-apk.yml            # Automated GitHub CI pipeline to build debug APK
├── supabase/migrations/                       # SQL Schemas (005_svm_fleet_office_register.sql)
├── USER_MANUAL.md                             # Complete 200+ line field operational user guide
├── build.gradle.kts & settings.gradle.kts     # Kotlin DSL configuration (Gradle 8.11.1, Kotlin 2.0.20)
└── gradlew & gradlew.bat                      # Bundled Gradle wrapper executables"""

    tree_box2 = Table([[Preformatted(tree_part2, code_tree_style)]], colWidths=[515])
    tree_box2.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#CBD5E1")),
        ('PADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(tree_box2)

    story.append(PageBreak())

    # =========================================================================
    # PAGE 5: ROUTE DIRECTORY & TECHNICAL SPECIFICATIONS
    # =========================================================================
    story.append(Paragraph("4. Core Navigation Routes & UI Screen Directory", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=PRIMARY, spaceAfter=8))

    screen_data = [
        ["Screen / Route", "Role", "Primary Composable", "Key Features & Capabilities"],
        [
            "intro_splash",
            "Public",
            "IntroSplashScreen.kt",
            "Deep Night gradient (#130A3A), road perspective lines, 3-dot pagination carousel."
        ],
        [
            "auth_login",
            "Public",
            "LoginScreen.kt",
            "Owner phone + Secret Master PIN, Driver phone verification, Fast2SMS OTP, 1-Tap Demo."
        ],
        [
            "owner_dashboard",
            "Owner",
            "DashboardScreen.kt",
            "Total Cars (30), Active (28), Inactive (2), Today's ₹18,420 (+12%), weekly bar chart."
        ],
        [
            "owner_cars",
            "Owner",
            "CarsListScreen.kt",
            "Search, filter pills (All/Active/Idle), photo upload, AddCarDialog with RC/insurance fields."
        ],
        [
            "owner_vehicle_detail/{id}",
            "Owner",
            "VehicleDetailScreen.kt",
            "4 Tabs (Overview, Trips, Earnings, Service), Driver Shift Rotation modal, Document Vault."
        ],
        [
            "owner_live_tracking",
            "Owner",
            "LiveTrackingScreen.kt",
            "Hyderabad vector canvas map, animated car pins, radar pulses, live driver dialer."
        ],
        [
            "owner_officeregister",
            "Owner",
            "OfficeRegisterScreen.kt",
            "Digital Moosapet logbook, opening/closing km, fuel deduction, daily commission ledger."
        ],
        [
            "owner_expenses",
            "Owner",
            "ExpensesScreen.kt",
            "Interactive donut chart (Fuel 33%, Driver Payouts 54%, Maintenance 8%), fuel log."
        ],
        [
            "owner_add_fuel_expense",
            "Owner",
            "AddFuelExpenseScreen.kt",
            "Dedicated form: Car dropdown, fuel type chips, liters, amount ₹, odometer."
        ],
        [
            "owner_maintenance",
            "Owner",
            "MaintenanceScreen.kt",
            "Upcoming vs History service tabs, insurance/PUC expiry countdown badges."
        ],
        [
            "owner_add_service",
            "Owner",
            "AddServiceScreen.kt",
            "Dedicated form: Car dropdown, service type dropdown, date, amount ₹, multi-line notes."
        ],
        [
            "owner_document_vault",
            "Owner",
            "DocumentVaultScreen.kt",
            "Digital compliance cards (RC, Commercial Insurance, PUC, Fitness, Permit), file upload."
        ],
        [
            "owner_reports",
            "Owner",
            "ReportsScreen.kt",
            "Financial statements, car utilization, one-tap export to native A4 PDF & RFC4180 CSV."
        ],
        [
            "owner_settings",
            "Owner",
            "SettingsScreen.kt",
            "Owner Secret Master PIN dialog, 24/7 background exemption, live Supabase sync status."
        ],
        [
            "driver_today",
            "Driver",
            "DriverTodayScreen.kt",
            "Shift check-in/out, live shift earnings, aggregator ride counter, platform breakdown."
        ],
        [
            "driver_setup",
            "Driver",
            "SetupWizardScreen.kt",
            "3-step mandatory wizard: Battery exemption, Accessibility permission, DPDP consent."
        ]
    ]

    screen_table = Table(screen_data, colWidths=[95, 45, 125, 250])
    screen_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), PRIMARY_DARK),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('FONTSIZE', (0,0), (-1,0), 7.5),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
        ('PADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(screen_table)

    story.append(Spacer(1, 10))
    story.append(Paragraph("5. Production Readiness & Build Verification Summary", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=PRIMARY, spaceAfter=6))

    prod_summary = [
        "<b>Gradle Build Health:</b> Verified clean compilation using Gradle 8.11.1 and Kotlin 2.0.20 on Adoptium OpenJDK 17 with <b>BUILD SUCCESSFUL (zero errors)</b>.",
        "<b>Automated Continuous Integration:</b> GitHub Actions workflow (<code>.github/workflows/build-apk.yml</code>) builds universal debug APK on every push to <code>main</code>.",
        "<b>Play Store Readiness:</b> Architecture is modularized into feature components ready for release bundle generation (<code>.aab</code>) and DPDP compliant privacy declarations."
    ]
    for p in prod_summary:
        story.append(Paragraph(f"✓ {p}", bullet_style))

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Successfully generated {filename}")

if __name__ == "__main__":
    out_path = sys.argv[1] if len(sys.argv) > 1 else "SVM_FLEET_SYSTEM_DOCUMENTATION.pdf"
    build_pdf(out_path)
