/**
 * SVM FLEET Operating System (Client Demo Environment)
 * Main Application Controller & View Orchestrator
 * Executive Dark Glassmorphism Edition (Matching Client Mockup media_1794250116022)
 * Purely Mock-Data Driven - ZERO Writes to Production Supabase
 */

class SvmFleetApp {
    constructor() {
        this.currentRole = localStorage.getItem("SVM_ROLE") || "OWNER";
        this.currentHubId = localStorage.getItem("SVM_HUB_ID") || "ALL";
        this.activeTab = "overview";
        this.selectedTimeframe = "day"; // "day" | "week" | "month"
        this.selectedChartTimeframe = "daily"; // "daily" | "weekly" | "monthly"
        this.mapController = null;
        this.searchQuery = "";
        this.shiftFilter = "ALL";
        this.registerViewMode = "table"; // "table" or "cards"
        this.notifications = [
            { id: 1, title: "Shift Completed", text: "Suman Goud (TS10 EF 7745) completed 15 trips (₹3,750 gross). 35% commission tier unlocked!", time: "2m ago", unread: true, type: "success" },
            { id: 2, title: "Cash Handover", text: "Ravi Kumar (TS09 UB 3368) logged cash handover of ₹2,000 at Moosapet depot.", time: "8m ago", unread: true, type: "info" },
            { id: 3, title: "Target Met", text: "Kukatpally Cab TS09 AC 9890 crossed shift target (10 trips, ₹2,400 gross).", time: "14m ago", unread: true, type: "warning" },
            { id: 4, title: "Fleet Reassignment", text: "Vehicle TS11 QR 4411 assigned to Madhapur Hitec City Hub.", time: "25m ago", unread: true, type: "info" }
        ];
        this.activeGuideTab = "guide-quickstart";
    }

    init() {
        this.setupEventListeners();
        this.setupWheelLocks();
        this.renderRoleSelector();
        this.updateGreeting();
        this.renderNotifications();
        
        // Initialize Hyderabad Leaflet Map
        if (typeof HyderabadMapController !== "undefined") {
            this.mapController = new HyderabadMapController("hyderabad-map");
            this.mapController.init();
        }
        
        // Render all dashboard and sub-panel components
        this.refreshAll();

        // Render trend chart & donut chart
        this.renderTrendChart("daily");
        this.renderPlatformDonutChart();

        // Refresh Lucide icons
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    setupWheelLocks() {
        // Prevent mousewheel scroll from changing numeric values on all number inputs
        document.querySelectorAll('input[type="number"]').forEach(input => {
            input.addEventListener("wheel", (e) => e.preventDefault(), { passive: false });
        });

        window.addEventListener("wheel", () => {
            if (document.activeElement && document.activeElement.type === "number") {
                document.activeElement.blur();
            }
        }, { passive: false });
    }

    setupEventListeners() {
        // Direct click handler for top Add Shift / Manual Entry button
        const topAddBtn = document.getElementById("btn-open-manual-entry");
        if (topAddBtn) {
            topAddBtn.addEventListener("click", () => this.openManualEntryModal());
        }

        // Tab switching across sidebar links
        document.querySelectorAll("[data-nav-tab]").forEach(btn => {
            btn.addEventListener("click", (e) => {
                const tab = e.currentTarget.getAttribute("data-nav-tab");
                this.switchTab(tab);
            });
        });

        // Search in Duty Register
        const searchInput = document.getElementById("register-search");
        if (searchInput) {
            searchInput.addEventListener("input", (e) => {
                this.searchQuery = e.target.value.toLowerCase();
                this.renderDutyRegister();
            });
        }

        // Global top search bar
        const globalSearch = document.getElementById("global-search-input");
        if (globalSearch) {
            globalSearch.addEventListener("input", (e) => {
                this.searchQuery = e.target.value.toLowerCase();
                const regSearch = document.getElementById("register-search");
                if (regSearch) regSearch.value = e.target.value;
                this.renderDutyRegister();
            });
        }

        // Keyboard shortcuts
        window.addEventListener("keydown", (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "k") {
                e.preventDefault();
                const search = document.getElementById("global-search-input") || document.getElementById("register-search");
                if (search) search.focus();
            }
            if (e.key === "Escape") {
                this.closeAllModals();
            }
        });

        // Live calculation triggers in Manual Entry Modal
        const calcInputs = [
            "modal-trips", "modal-earnings", "modal-cash", "modal-expenditure",
            "modal-drivepass", "modal-advance", "modal-shifttype"
        ];
        calcInputs.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.addEventListener("input", () => this.updateModalCalculation());
                el.addEventListener("change", () => this.updateModalCalculation());
                el.addEventListener("wheel", (e) => e.preventDefault(), { passive: false });
            }
        });

        // Backdrop click dismissal for modals
        const modalIds = [
            "manual-entry-modal",
            "reassign-hub-modal",
            "add-driver-modal",
            "add-vehicle-modal",
            "add-manager-modal",
            "user-guide-modal"
        ];
        modalIds.forEach(id => {
            const modal = document.getElementById(id);
            if (modal) {
                modal.addEventListener("click", (e) => {
                    if (e.target === modal) {
                        this.closeAllModals();
                    }
                });
            }
        });

        // Document click handler to dismiss dropdowns when clicking outside
        document.addEventListener("click", (e) => {
            // Dismiss notification panel if clicked outside
            const notifBtn = document.getElementById("btn-notification-bell");
            const notifDropdown = document.getElementById("notification-dropdown");
            if (notifDropdown && notifBtn && !notifBtn.contains(e.target) && !notifDropdown.contains(e.target)) {
                notifDropdown.classList.remove("open");
            }

            // Dismiss driver combobox dropdown if clicked outside wrapper
            const combobox = document.getElementById("driver-combobox-wrapper");
            const driverDropdown = document.getElementById("driver-search-dropdown");
            if (combobox && driverDropdown && !combobox.contains(e.target)) {
                this.closeDriverDropdown();
            }
        });
    }

    closeAllModals() {
        this.closeManualEntryModal();
        this.closeReassignModal();
        this.closeAddDriverModal();
        this.closeAddVehicleModal();
        this.closeAddManagerModal();
        this.closeUserGuideModal();
    }

    updateGreeting() {
        const hour = new Date().getHours();
        let greeting = "Good Evening, Akhilesh";
        if (hour < 12) greeting = "Good Morning, Akhilesh";
        else if (hour < 17) greeting = "Good Afternoon, Akhilesh";

        const titleEl = document.getElementById("greeting-title");
        if (titleEl) {
            titleEl.innerHTML = `<span>${greeting}</span> <span class="text-2xl">👋</span>`;
        }

        const subtitleEl = document.getElementById("greeting-subtitle");
        if (subtitleEl) {
            const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
            const hub = hubs.find(h => h.id === this.currentHubId);
            subtitleEl.innerText = hub 
                ? `Here's your fleet performance for ${hub.name}` 
                : `Here's your fleet performance for today.`;
        }
    }

    renderRoleSelector() {
        const selector = document.getElementById("role-selector");
        if (!selector) return;

        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        let options = `
            <option value="OWNER:ALL" ${this.currentRole === "OWNER" && this.currentHubId === "ALL" ? "selected" : ""}>
                👑 Fleet Owner (Master - All 30 Cabs)
            </option>
        `;

        hubs.forEach(hub => {
            const isSelected = this.currentRole === "MANAGER" && this.currentHubId === hub.id;
            options += `
                <option value="MANAGER:${hub.id}" ${isSelected ? "selected" : ""}>
                    👔 ${hub.shortName} Manager (${hub.managerName})
                </option>
            `;
        });

        selector.innerHTML = options;
        selector.onchange = (e) => {
            const [role, hubId] = e.target.value.split(":");
            this.setSession(role, hubId);
        };
    }

    setSession(role, hubId) {
        this.currentRole = role;
        this.currentHubId = hubId;
        localStorage.setItem("SVM_ROLE", role);
        localStorage.setItem("SVM_HUB_ID", hubId);

        const topSelect = document.getElementById("top-branch-selector");
        if (topSelect) topSelect.value = hubId;

        this.updateGreeting();
        this.refreshAll();

        if (this.mapController) {
            this.mapController.focusHub(hubId);
        }

        this.showToast(`Switched view to ${role === "OWNER" ? "Fleet Owner (Master)" : this.getActiveHubName() + " Hub Manager"}`, "info");
    }

    filterHub(hubId) {
        this.currentHubId = hubId;
        localStorage.setItem("SVM_HUB_ID", hubId);
        const topSelect = document.getElementById("top-branch-selector");
        if (topSelect) topSelect.value = hubId;

        this.updateGreeting();
        this.refreshAll();
        if (this.mapController) {
            this.mapController.focusHub(hubId);
        }
    }

    setTimeframe(tf) {
        this.selectedTimeframe = tf;
        
        // Update button pills state
        const btnToday = document.getElementById("tf-btn-today");
        const btnWeek = document.getElementById("tf-btn-week");
        const btnMonth = document.getElementById("tf-btn-month");

        [btnToday, btnWeek, btnMonth].forEach(btn => {
            if (btn) {
                btn.className = "px-3 py-1.5 rounded-lg text-xs font-medium text-[#94A3B8] hover:text-white transition-all";
            }
        });

        if (tf === "day" && btnToday) {
            btnToday.className = "px-3 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white shadow-sm transition-all";
        } else if (tf === "week" && btnWeek) {
            btnWeek.className = "px-3 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white shadow-sm transition-all";
        } else if (tf === "month" && btnMonth) {
            btnMonth.className = "px-3 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white shadow-sm transition-all";
        }

        this.renderMetricCards();
        this.renderPlatformDonutChart();
    }

    setShiftFilter(filter) {
        this.shiftFilter = filter;
        document.querySelectorAll(".shift-filter-btn").forEach(btn => {
            btn.className = "shift-filter-btn px-3 py-1.5 rounded-full text-xs font-medium border border-[#263149] bg-[#0B1021] text-slate-400 hover:text-white";
        });
        if (window.event && window.event.currentTarget) {
            window.event.currentTarget.className = "shift-filter-btn px-3 py-1.5 rounded-full text-xs font-bold border transition-all bg-[#6655F5] text-white border-[#6655F5]";
        }
        this.renderDutyRegister();
    }

    setRegisterViewMode(mode) {
        this.registerViewMode = mode;
        const cardsContainer = document.getElementById("register-cards-container");
        const tableContainer = document.getElementById("register-table-container");
        const cardsBtn = document.getElementById("view-mode-cards-btn");
        const tableBtn = document.getElementById("view-mode-table-btn");

        if (mode === "cards") {
            if (cardsContainer) cardsContainer.classList.remove("hidden");
            if (tableContainer) tableContainer.classList.add("hidden");
            if (cardsBtn) cardsBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white flex items-center gap-1";
            if (tableBtn) tableBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-medium text-slate-400 hover:text-white flex items-center gap-1";
        } else {
            if (cardsContainer) cardsContainer.classList.add("hidden");
            if (tableContainer) tableContainer.classList.remove("hidden");
            if (cardsBtn) cardsBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-medium text-slate-400 hover:text-white flex items-center gap-1";
            if (tableBtn) tableBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white flex items-center gap-1";
        }
        if (window.lucide) window.lucide.createIcons();
    }

    getActiveHubName() {
        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        const hub = hubs.find(h => h.id === this.currentHubId);
        return hub ? hub.shortName : "All Hyderabad";
    }

    getCurrentFilteredVehicles() {
        if (!window.DEMO_REPO) return [];
        return window.DEMO_REPO.getVehicles(this.currentHubId);
    }

    getCurrentFilteredDrivers() {
        if (!window.DEMO_REPO) return [];
        return window.DEMO_REPO.getDrivers(this.currentHubId);
    }

    getCurrentFilteredLogs() {
        if (!window.DEMO_REPO) return [];
        let logs = window.DEMO_REPO.getDutyLogs(this.currentHubId);

        if (this.shiftFilter === "12_HOURS" || this.shiftFilter === "24_HOURS") {
            logs = logs.filter(l => l.shiftType === this.shiftFilter);
        } else if (this.shiftFilter === "OPEN") {
            logs = logs.filter(l => l.status === "OPEN" || l.status === "PENDING");
        }

        if (this.searchQuery) {
            logs = logs.filter(l =>
                l.driverName.toLowerCase().includes(this.searchQuery) ||
                (l.driverPhone && l.driverPhone.toLowerCase().includes(this.searchQuery)) ||
                l.vehicleNo.toLowerCase().includes(this.searchQuery) ||
                (l.remarks && l.remarks.toLowerCase().includes(this.searchQuery)) ||
                (l.source && l.source.toLowerCase().includes(this.searchQuery))
            );
        }
        return logs;
    }

    refreshAll() {
        this.renderMetricCards();
        this.renderDutyRegister();
        this.renderDriversGrid();
        this.renderVehiclesGrid();
        this.renderPendingTelemetryList();

        const vehicles = this.getCurrentFilteredVehicles();
        if (this.mapController) {
            this.mapController.updateVehicles(vehicles);
        }

        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    renderMetricCards() {
        const vehicles = this.getCurrentFilteredVehicles();
        const drivers = this.getCurrentFilteredDrivers();
        const logs = this.getCurrentFilteredLogs();

        const totalVehicles = vehicles.length || 30;
        const activeDrivers = drivers.filter(d => d.status === "ON_DUTY").length || 27;

        let calculatedTrips = 0;
        let calculatedGross = 0;

        logs.forEach(l => {
            calculatedTrips += (l.trips || 0);
            calculatedGross += (l.earnings || 0);
        });

        let displayTrips = 326;
        let displayGrossEarnings = 124860;
        let displayNetProfit = 38420;

        if (this.selectedTimeframe === "day") {
            displayTrips = calculatedTrips > 0 ? calculatedTrips : 326;
            displayGrossEarnings = calculatedGross > 0 ? calculatedGross : 124860;
            displayNetProfit = Math.round(displayGrossEarnings * 0.3077);
        } else if (this.selectedTimeframe === "week") {
            displayTrips = (calculatedTrips > 0 ? calculatedTrips * 7 : 2280);
            displayGrossEarnings = (calculatedGross > 0 ? calculatedGross * 7 : 874000);
            displayNetProfit = Math.round(displayGrossEarnings * 0.31);
        } else {
            displayTrips = (calculatedTrips > 0 ? calculatedTrips * 30 : 9780);
            displayGrossEarnings = (calculatedGross > 0 ? calculatedGross * 30 : 3745800);
            displayNetProfit = Math.round(displayGrossEarnings * 0.31);
        }

        const cabsEl = document.getElementById("kpi-total-cabs");
        const driversEl = document.getElementById("kpi-active-drivers");
        const tripsEl = document.getElementById("kpi-total-trips");
        const grossEl = document.getElementById("kpi-gross-earnings");
        const netEl = document.getElementById("kpi-net-profit") || document.getElementById("kpi-net-earnings");

        if (cabsEl) cabsEl.innerText = totalVehicles;
        if (driversEl) driversEl.innerText = activeDrivers;
        if (tripsEl) tripsEl.innerText = displayTrips.toLocaleString('en-IN');
        if (grossEl) grossEl.innerText = "₹" + Math.round(displayGrossEarnings).toLocaleString('en-IN');
        if (netEl) netEl.innerText = "₹" + Math.round(displayNetProfit).toLocaleString('en-IN');
    }

    /**
     * Render Interactive SVG Area & Column Chart (Earnings & Profitability Trend)
     * Matches exact visuals in media_1794250116022
     */
    renderTrendChart(timeframe = "daily") {
        this.selectedChartTimeframe = timeframe;
        const container = document.getElementById("trend-chart-container");
        if (!container) return;

        // Update switcher buttons
        const dailyBtn = document.getElementById("chart-tf-daily");
        const weeklyBtn = document.getElementById("chart-tf-weekly");
        const monthlyBtn = document.getElementById("chart-tf-monthly");

        [dailyBtn, weeklyBtn, monthlyBtn].forEach(b => {
            if (b) b.className = "px-2.5 py-1 rounded-md text-[11px] font-medium text-[#94A3B8] hover:text-white transition-all";
        });
        if (timeframe === "daily" && dailyBtn) dailyBtn.className = "px-2.5 py-1 rounded-md text-[11px] font-bold bg-[#6655F5] text-white shadow-sm";
        if (timeframe === "weekly" && weeklyBtn) weeklyBtn.className = "px-2.5 py-1 rounded-md text-[11px] font-bold bg-[#6655F5] text-white shadow-sm";
        if (timeframe === "monthly" && monthlyBtn) monthlyBtn.className = "px-2.5 py-1 rounded-md text-[11px] font-bold bg-[#6655F5] text-white shadow-sm";

        const trendData = window.DEMO_EARNINGS_TREND ? window.DEMO_EARNINGS_TREND[timeframe] : null;
        if (!trendData) return;

        const { labels, grossEarnings, netProfit } = trendData;
        const n = labels.length;

        // SVG dimensions & coordinate calculations
        const svgW = 600;
        const svgH = 220;
        const padLeft = 45;
        const padRight = 20;
        const padTop = 15;
        const padBottom = 30;

        const maxVal = timeframe === "daily" ? 150000 : timeframe === "weekly" ? 800000 : 3000000;
        const yTicks = timeframe === "daily" 
            ? [{ v: 150000, l: "₹1.5L" }, { v: 120000, l: "₹1.2L" }, { v: 90000, l: "₹90K" }, { v: 60000, l: "₹60K" }, { v: 30000, l: "₹30K" }, { v: 0, l: "₹0" }]
            : timeframe === "weekly"
            ? [{ v: 800000, l: "₹8.0L" }, { v: 600000, l: "₹6.0L" }, { v: 400000, l: "₹4.0L" }, { v: 200000, l: "₹2.0L" }, { v: 0, l: "₹0" }]
            : [{ v: 3000000, l: "₹30L" }, { v: 2400000, l: "₹24L" }, { v: 1800000, l: "₹18L" }, { v: 1200000, l: "₹12L" }, { v: 600000, l: "₹6L" }, { v: 0, l: "₹0" }];

        const chartW = svgW - padLeft - padRight;
        const chartH = svgH - padTop - padBottom;

        const getY = (val) => padTop + chartH - (val / maxVal) * chartH;
        const getX = (i) => padLeft + (i / (n - 1)) * chartW;

        // Generate Gridlines & Y-axis labels
        let gridLinesHtml = "";
        yTicks.forEach(tick => {
            const y = getY(tick.v);
            gridLinesHtml += `
                <line x1="${padLeft}" y1="${y}" x2="${svgW - padRight}" y2="${y}" stroke="#1E273D" stroke-width="1" stroke-dasharray="3 4" />
                <text x="${padLeft - 8}" y="${y + 3}" fill="#64748B" font-size="9" font-family="monospace" text-anchor="end">${tick.l}</text>
            `;
        });

        // Generate Net Profit Column Bars (Emerald Green #10B981)
        let barsHtml = "";
        const barWidth = Math.min(24, Math.floor(chartW / (n * 2.2)));
        for (let i = 0; i < n; i++) {
            const x = getX(i) - barWidth / 2;
            const barH = (netProfit[i] / maxVal) * chartH;
            const y = padTop + chartH - barH;
            barsHtml += `
                <rect x="${x}" y="${y}" width="${barWidth}" height="${barH}" rx="4" fill="#10B981" fill-opacity="0.85">
                    <title>${labels[i]} Net Profit: ₹${netProfit[i].toLocaleString('en-IN')}</title>
                </rect>
            `;
        }

        // Generate Smooth Area Curve & Line for Gross Earnings (Purple #6655F5)
        let linePoints = [];
        for (let i = 0; i < n; i++) {
            linePoints.push({ x: getX(i), y: getY(grossEarnings[i]) });
        }

        // Generate SVG Path with smooth bezier curves
        let pathD = `M ${linePoints[0].x} ${linePoints[0].y}`;
        for (let i = 0; i < linePoints.length - 1; i++) {
            const curr = linePoints[i];
            const next = linePoints[i + 1];
            const midX = (curr.x + next.x) / 2;
            pathD += ` C ${midX} ${curr.y}, ${midX} ${next.y}, ${next.x} ${next.y}`;
        }

        const bottomY = padTop + chartH;
        const areaD = `${pathD} L ${linePoints[linePoints.length - 1].x} ${bottomY} L ${linePoints[0].x} ${bottomY} Z`;

        // Dots & X-axis labels
        let dotsHtml = "";
        let xLabelsHtml = "";
        for (let i = 0; i < n; i++) {
            const pt = linePoints[i];
            dotsHtml += `
                <circle cx="${pt.x}" cy="${pt.y}" r="4.5" fill="#6655F5" stroke="#FFFFFF" stroke-width="1.8" class="cursor-pointer">
                    <title>${labels[i]} Gross: ₹${grossEarnings[i].toLocaleString('en-IN')}</title>
                </circle>
            `;
            xLabelsHtml += `
                <text x="${pt.x}" y="${svgH - 8}" fill="#94A3B8" font-size="10" font-weight="600" text-anchor="middle">${labels[i]}</text>
            `;
        }

        container.innerHTML = `
            <svg viewBox="0 0 ${svgW} ${svgH}" class="w-full h-full overflow-visible">
                <defs>
                    <linearGradient id="grossGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stop-color="#6655F5" stop-opacity="0.5"/>
                        <stop offset="100%" stop-color="#6655F5" stop-opacity="0.0"/>
                    </linearGradient>
                </defs>
                ${gridLinesHtml}
                ${barsHtml}
                <path d="${areaD}" fill="url(#grossGradient)" />
                <path d="${pathD}" fill="none" stroke="#6655F5" stroke-width="2.6" stroke-linecap="round" />
                ${dotsHtml}
                ${xLabelsHtml}
            </svg>
        `;
    }

    /**
     * Render Dynamic Platform Earnings Donut Chart
     */
    renderPlatformDonutChart() {
        const donut = window.DEMO_PLATFORM_EARNINGS || {
            totalGross: 124860,
            platforms: [
                { name: "Uber", percentage: 54, amount: 67424, color: "#6655F5" },
                { name: "Rapido", percentage: 28, amount: 34961, color: "#F59E0B" },
                { name: "Ola", percentage: 18, amount: 22475, color: "#10B981" }
            ]
        };

        const totalEl = document.getElementById("donut-gross-total");
        if (totalEl) {
            totalEl.innerText = "₹" + Math.round(donut.totalGross).toLocaleString('en-IN');
        }
    }

    renderDutyRegister() {
        const tbodyOverview = document.getElementById("register-table-body");
        const tbodyFull = document.getElementById("register-table-body-full");
        const cardsContainer = document.getElementById("register-cards-container");

        const logs = this.getCurrentFilteredLogs();

        // 1. Mobile-App Style Cards
        if (cardsContainer) {
            if (logs.length === 0) {
                cardsContainer.innerHTML = `
                    <div class="col-span-full py-12 text-center text-slate-500 saas-card">
                        <i data-lucide="inbox" class="w-8 h-8 mx-auto mb-2 opacity-50"></i>
                        No duty logbook records match your search or filter.
                    </div>
                `;
            } else {
                cardsContainer.innerHTML = logs.map(log => {
                    const isSettled = log.status === "SETTLED";
                    const is12H = log.shiftType === "12_HOURS";
                    const isAuto = log.source === "AUTOMATIC_TELEMETRY" || log.source === "Uber" || log.source === "Rapido" || log.source === "Ola";
                    const commTier = log.commissionPercentage || 0;
                    const tierBadgeClass = commTier >= 40 ? "bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/40"
                        : commTier >= 35 ? "bg-[#6655F5]/20 text-[#8B5CF6] border border-[#6655F5]/40"
                        : commTier >= 30 ? "bg-[#0EA5E9]/20 text-[#0EA5E9] border border-[#0EA5E9]/40"
                        : "bg-[#F59E0B]/20 text-[#F59E0B] border border-[#F59E0B]/40";

                    return `
                        <div class="saas-card p-4 flex flex-col justify-between hover:border-[#6655F5]/50 transition-all">
                            <div>
                                <div class="flex justify-between items-start mb-3">
                                    <div class="flex items-center gap-3">
                                        <div class="w-10 h-10 rounded-full bg-[#141B30] border border-[#263149] flex items-center justify-center text-[#6655F5]">
                                            <i data-lucide="car" class="w-5 h-5"></i>
                                        </div>
                                        <div>
                                            <div class="flex items-center gap-2">
                                                <h4 class="font-extrabold text-base text-white font-mono">${log.vehicleNo}</h4>
                                                <span class="text-[10px] font-bold px-2 py-0.5 rounded-full ${isSettled ? 'bg-[#10B981]/15 text-[#10B981] border border-[#10B981]/30' : 'bg-[#F59E0B]/15 text-[#F59E0B] border border-[#F59E0B]/30'}">
                                                    ${isSettled ? '✓ Settled' : '⏳ Pending'}
                                                </span>
                                            </div>
                                            <div class="flex items-center gap-2 text-xs text-slate-300 mt-0.5">
                                                <span class="font-bold">${log.driverName}</span>
                                                <span class="text-slate-500">•</span>
                                                <a href="tel:${log.driverPhone || ''}" class="text-[#6655F5] hover:underline font-mono text-[11px] flex items-center gap-1">
                                                    <i data-lucide="phone" class="w-3 h-3"></i> ${log.driverPhone || 'No Phone'}
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="text-right">
                                        <span class="text-xs text-slate-400 font-semibold block">${is12H ? '12h Shift' : '24h Shift'}</span>
                                        <span class="text-[10px] font-bold px-2 py-0.5 rounded ${isAuto ? 'bg-[#6655F5]/20 text-[#8B5CF6]' : 'bg-slate-800 text-slate-400'}">
                                            ${log.source || 'Manual'}
                                        </span>
                                    </div>
                                </div>

                                <div class="py-1.5 px-3 rounded-lg bg-[#0B1021] border border-[#263149] flex justify-between text-[11px] text-slate-400 mb-3">
                                    <span>Date: <strong class="text-slate-200">${log.loginDate}</strong></span>
                                    <span>Duty: <strong class="text-slate-200">${log.loginTime || '06:00 AM'} - ${log.logoutTime || '08:00 PM'}</strong></span>
                                </div>

                                <div class="grid grid-cols-4 bg-[#0B1021] border border-[#263149] rounded-xl p-2.5 gap-2 text-center mb-3">
                                    <div>
                                        <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Trips</div>
                                        <div class="text-sm font-extrabold text-white mt-0.5 font-mono">${log.trips}</div>
                                    </div>
                                    <div>
                                        <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Gross</div>
                                        <div class="text-sm font-extrabold text-[#10B981] mt-0.5 font-mono">₹${Math.round(log.earnings)}</div>
                                    </div>
                                    <div>
                                        <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Cash</div>
                                        <div class="text-sm font-extrabold text-[#F59E0B] mt-0.5 font-mono">₹${Math.round(log.cashCollection)}</div>
                                    </div>
                                    <div>
                                        <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Comm</div>
                                        <div class="text-sm font-extrabold text-[#6655F5] mt-0.5 font-mono">₹${Math.round(log.commissionRent)}</div>
                                    </div>
                                </div>

                                <div class="text-xs text-slate-400 flex flex-wrap justify-between items-center py-2 border-t border-[#263149]">
                                    <span class="flex items-center gap-1">
                                        <i data-lucide="fuel" class="w-3.5 h-3.5 text-[#0EA5E9]"></i> Fuel/Exp: <strong class="text-[#0EA5E9]">₹${log.expenditure || 0}</strong>
                                    </span>
                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-bold ${tierBadgeClass}">
                                        ${commTier > 0 ? commTier + '% Tier' : 'Flat Rent'}
                                    </span>
                                </div>
                            </div>

                            <div class="mt-3 pt-3 border-t border-[#263149] flex justify-between items-center">
                                <div>
                                    <span class="text-[10px] text-slate-500 uppercase tracking-wider block font-bold">Net Balance</span>
                                    <span class="font-extrabold text-sm ${log.netBalance >= 0 ? 'text-[#10B981]' : 'text-[#F59E0B]'}">
                                        ${log.netBalance >= 0 ? 'Company Pays: ₹' + Math.round(log.netBalance) : 'Driver Handover: ₹' + Math.round(Math.abs(log.netBalance))}
                                    </span>
                                </div>
                                <button onclick="window.SVM_APP.printReceipt('${log.id}')" class="px-3 py-1.5 rounded-lg bg-[#141B30] hover:bg-[#1E273D] text-slate-300 hover:text-white text-xs font-bold flex items-center gap-1.5 transition-colors cursor-pointer">
                                    <i data-lucide="printer" class="w-3.5 h-3.5"></i> Receipt
                                </button>
                            </div>
                        </div>
                    `;
                }).join("");
            }
        }

        // 2. Dense Tables (Exact match to reference UI)
        const tableRowsHtml = logs.map((log, idx) => {
            const isSettled = log.status === "SETTLED";
            const initials = log.driverName.split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2) || "DR";
            const avatarColors = ["bg-[#6655F5]", "bg-[#0EA5E9]", "bg-[#8B5CF6]", "bg-[#10B981]", "bg-[#EC4899]"];
            const avatarColor = avatarColors[Math.abs(log.driverName.charCodeAt(0)) % avatarColors.length];
            
            // Source Tag
            let sourceTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#162038] text-slate-200 border border-[#263149]">Uber</span>`;
            if (log.source === "Rapido" || (log.remarks && log.remarks.toLowerCase().includes("rapido"))) {
                sourceTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#F59E0B]/20 text-[#F59E0B] border border-[#F59E0B]/30">Rapido</span>`;
            } else if (log.source === "Ola" || (log.remarks && log.remarks.toLowerCase().includes("ola"))) {
                sourceTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30">Ola</span>`;
            } else if (log.source === "Uber") {
                sourceTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#0B1021] text-white border border-[#334155]">Uber</span>`;
            }

            // Status Tag matching screenshot
            const statusTag = isSettled
                ? `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#10B981]/15 text-[#10B981] border border-[#10B981]/30">✓ Settled</span>`
                : `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#F59E0B]/15 text-[#F59E0B] border border-[#F59E0B]/30">⏳ Pending</span>`;

            return `
                <tr class="hover:bg-[#11182A]/70 transition-colors">
                    <td class="py-3 px-3 text-xs font-bold text-[#64748B] font-mono">
                        ${idx + 1}
                    </td>
                    <td class="py-3 px-4">
                        <div class="flex items-center gap-3">
                            <div class="w-8 h-8 rounded-full ${avatarColor} flex items-center justify-center font-bold text-xs text-white shadow-sm shrink-0">
                                ${initials}
                            </div>
                            <div>
                                <div class="font-bold text-sm text-white">${log.driverName}</div>
                                <div class="text-xs text-[#64748B] font-mono">${log.driverPhone || ''}</div>
                            </div>
                        </div>
                    </td>
                    <td class="py-3 px-4">
                        <div class="flex items-center gap-2">
                            <i data-lucide="car" class="w-4 h-4 text-[#64748B]"></i>
                            <span class="font-mono text-xs font-bold text-slate-200">
                                ${log.vehicleNo}
                            </span>
                        </div>
                    </td>
                    <td class="py-3 px-3 text-center font-bold text-white text-sm font-mono">
                        ${log.trips}
                    </td>
                    <td class="py-3 px-4 text-right font-bold text-white font-mono">
                        ₹${Math.round(log.earnings).toLocaleString('en-IN')}
                    </td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">
                        ₹${(log.expenditure || 0).toFixed(2)}
                    </td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">
                        ₹${Math.round(log.cashCollection).toLocaleString('en-IN')}
                    </td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">
                        ₹${Math.round(log.commissionRent).toLocaleString('en-IN')}
                    </td>
                    <td class="py-3 px-3 text-center">
                        ${sourceTag}
                    </td>
                    <td class="py-3 px-3 text-center">
                        ${statusTag}
                    </td>
                    <td class="py-3 px-3 text-right">
                        <button onclick="window.SVM_APP.printReceipt('${log.id}')" class="p-1.5 rounded-lg hover:bg-[#1E273D] text-[#94A3B8] hover:text-white transition-colors cursor-pointer" title="Print Driver Receipt">
                            <i data-lucide="printer" class="w-4 h-4"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join("");

        // 3. 14-Column Master Table for Full Duty Register Tab
        const tableRowsFullHtml = logs.map((log, idx) => {
            const isSettled = log.status === "SETTLED";
            const initials = log.driverName.split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2) || "DR";
            const avatarColors = ["bg-[#6655F5]", "bg-[#0EA5E9]", "bg-[#8B5CF6]", "bg-[#10B981]", "bg-[#EC4899]"];
            const avatarColor = avatarColors[Math.abs(log.driverName.charCodeAt(0)) % avatarColors.length];
            
            // Hub label
            const hubName = log.managerId === "mgr_kukatpally_02" ? "Kukatpally" : log.managerId === "mgr_madhapur_03" ? "Madhapur" : "Moosapet";

            // Platform tag
            let platformTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#162038] text-slate-200 border border-[#263149]">Uber</span>`;
            if (log.source === "Rapido" || (log.remarks && log.remarks.toLowerCase().includes("rapido"))) {
                platformTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#F59E0B]/20 text-[#F59E0B] border border-[#F59E0B]/30">Rapido</span>`;
            } else if (log.source === "Ola" || (log.remarks && log.remarks.toLowerCase().includes("ola"))) {
                platformTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30">Ola</span>`;
            }

            const statusTag = isSettled
                ? `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#10B981]/15 text-[#10B981] border border-[#10B981]/30">✓ Settled</span>`
                : `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#F59E0B]/15 text-[#F59E0B] border border-[#F59E0B]/30">⏳ Pending</span>`;

            return `
                <tr class="hover:bg-[#11182A]/70 transition-colors">
                    <td class="py-3 px-3 text-xs font-bold text-[#64748B] font-mono">${idx + 1}</td>
                    <td class="py-3 px-4">
                        <div class="flex items-center gap-2.5">
                            <div class="w-7 h-7 rounded-full ${avatarColor} flex items-center justify-center font-bold text-xs text-white shadow-sm shrink-0">
                                ${initials}
                            </div>
                            <span class="font-bold text-sm text-white">${log.driverName}</span>
                        </div>
                    </td>
                    <td class="py-3 px-4 font-mono text-xs text-[#94A3B8]">${log.driverPhone || ''}</td>
                    <td class="py-3 px-4 font-mono text-xs font-bold text-slate-200">
                        ${log.vehicleNo}
                    </td>
                    <td class="py-3 px-3">
                        <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-[#141B30] text-[#8B5CF6] border border-[#263149]">${hubName}</span>
                    </td>
                    <td class="py-3 px-3 text-xs text-slate-300 font-mono">${log.loginDate}</td>
                    <td class="py-3 px-3 text-center font-bold text-white text-sm font-mono">${log.trips}</td>
                    <td class="py-3 px-4 text-right font-bold text-white font-mono">₹${Math.round(log.earnings).toLocaleString('en-IN')}</td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">₹${(log.expenditure || 0).toFixed(2)}</td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">₹${Math.round(log.cashCollection).toLocaleString('en-IN')}</td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">₹${Math.round(log.commissionRent).toLocaleString('en-IN')}</td>
                    <td class="py-3 px-3 text-center">${platformTag}</td>
                    <td class="py-3 px-3 text-center">${statusTag}</td>
                    <td class="py-3 px-3 text-right">
                        <button onclick="window.SVM_APP.printReceipt('${log.id}')" class="p-1.5 rounded-lg hover:bg-[#1E273D] text-[#94A3B8] hover:text-white transition-colors cursor-pointer" title="Print Driver Receipt">
                            <i data-lucide="printer" class="w-4 h-4"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join("");

        if (tbodyOverview) tbodyOverview.innerHTML = tableRowsHtml;
        if (tbodyFull) tbodyFull.innerHTML = tableRowsFullHtml;

        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    renderPendingTelemetryList() {
        const container = document.getElementById("telemetry-pending-list");
        if (!container) return;

        const telemetry = window.DEMO_REPO ? window.DEMO_REPO.getTelemetry() : [];
        if (telemetry.length === 0) return;

        container.innerHTML = telemetry.map(item => `
            <div class="p-3 rounded-xl bg-[#0B1021] border border-[#263149] hover:border-[#6655F5]/40 flex items-center justify-between gap-3 transition-colors">
                <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-lg bg-[#141B30] border border-[#263149] flex items-center justify-center text-[#94A3B8]">
                        <i data-lucide="car" class="w-4 h-4"></i>
                    </div>
                    <div>
                        <div class="text-xs font-bold text-white">${item.driverName}</div>
                        <div class="text-[10px] text-[#64748B] font-mono flex items-center gap-1 mt-0.5">
                            <i data-lucide="car" class="w-3 h-3 text-[#64748B]"></i> ${item.vehicleNo} • ${item.platform}
                        </div>
                    </div>
                </div>
                <div class="text-right">
                    <span class="inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold border ${item.statusColor}">
                        ${item.status}
                    </span>
                    <div class="text-[10px] text-[#64748B] mt-0.5">${item.time}</div>
                </div>
            </div>
        `).join("");

        if (window.lucide) window.lucide.createIcons();
    }

    renderDriversGrid() {
        const container = document.getElementById("drivers-grid");
        if (!container) return;

        const drivers = this.getCurrentFilteredDrivers();
        const logs = window.DEMO_REPO ? window.DEMO_REPO.getDutyLogs() : [];
        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];

        container.innerHTML = drivers.map(d => {
            const hub = hubs.find(h => h.id === d.managerId);
            const hubLabel = hub ? hub.shortName : "Moosapet";
            const driverLogs = logs.filter(l => l.driverName.toLowerCase() === d.name.toLowerCase() || (l.driverPhone && l.driverPhone === d.phone));
            const totalShiftsLogged = driverLogs.length;
            const isOnDuty = d.status === "ON_DUTY";

            return `
                <div class="saas-card p-4 flex flex-col justify-between hover:border-[#6655F5]/40 transition-all">
                    <div>
                        <div class="flex justify-between items-start mb-2.5">
                            <div>
                                <div class="flex items-center gap-2">
                                    <h4 class="font-extrabold text-white text-base">${d.name}</h4>
                                    <span class="inline-flex items-center gap-1 px-2 py-0.5 text-[9px] font-bold rounded-full ${isOnDuty ? 'bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30' : 'bg-[#F59E0B]/20 text-[#F59E0B] border border-[#F59E0B]/30'}">
                                        <span class="w-1.5 h-1.5 rounded-full ${isOnDuty ? 'bg-[#10B981]' : 'bg-[#F59E0B]'}"></span>
                                        ${isOnDuty ? 'ON DUTY' : 'IDLE'}
                                    </span>
                                </div>
                                <a href="tel:${d.phone}" class="text-xs text-[#6655F5] hover:underline font-mono flex items-center gap-1 mt-1">
                                    <i data-lucide="phone" class="w-3 h-3"></i> ${d.phone}
                                </a>
                            </div>
                            <span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#141B30] text-[#8B5CF6] border border-[#263149]">
                                ${hubLabel}
                            </span>
                        </div>
                        <div class="grid grid-cols-2 gap-2 mt-3 p-2.5 rounded-xl bg-[#0B1021] border border-[#263149] text-xs">
                            <div>
                                <span class="text-[10px] text-slate-500 block font-semibold uppercase tracking-wider">Assigned Cab</span>
                                <span class="font-mono font-bold text-slate-200 mt-0.5 block">${d.vehicleNo || 'Unassigned'}</span>
                            </div>
                            <div class="text-right">
                                <span class="text-[10px] text-slate-500 block font-semibold uppercase tracking-wider">Duty History</span>
                                <span class="font-mono font-bold text-[#6655F5] mt-0.5 block">${totalShiftsLogged} Logs</span>
                            </div>
                        </div>
                    </div>
                    <div class="mt-4 pt-3 border-t border-[#263149] flex justify-between items-center text-xs">
                        <span class="text-[#10B981] font-bold flex items-center gap-1">⭐ ${d.rating}</span>
                        <div class="flex items-center gap-1.5">
                            <button onclick="window.SVM_APP.filterDriverInRegister('${d.name}')" class="px-2.5 py-1.5 rounded-lg bg-[#141B30] hover:bg-[#1E273D] text-slate-300 hover:text-white text-xs font-semibold flex items-center gap-1 cursor-pointer transition-colors" title="View Duty Register for this driver">
                                <i data-lucide="file-text" class="w-3 h-3"></i> Shifts
                            </button>
                            <button onclick="window.SVM_APP.openManualEntryModal('${d.vehicleNo || ''}', '${d.name}', '${d.phone}')" class="px-3 py-1.5 rounded-lg bg-[#6655F5] hover:bg-[#5443E8] text-white font-bold text-xs flex items-center gap-1 shadow-md shadow-[#6655F5]/20 cursor-pointer transition-colors">
                                <i data-lucide="plus" class="w-3 h-3"></i> Log Shift
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join("");

        if (window.lucide) window.lucide.createIcons();
    }

    filterDriverInRegister(driverName) {
        this.switchTab('register');
        const searchInput = document.getElementById("register-search");
        if (searchInput) {
            searchInput.value = driverName;
            this.searchQuery = driverName.toLowerCase();
            this.renderDutyRegister();
        }
    }

    renderVehiclesGrid() {
        const container = document.getElementById("cars-grid");
        if (!container) return;

        const vehicles = this.getCurrentFilteredVehicles();
        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];

        container.innerHTML = vehicles.map(v => {
            const isActive = v.status === "ACTIVE";
            const hub = hubs.find(h => h.id === v.managerId);

            return `
                <div class="saas-card p-4 flex flex-col justify-between hover:border-[#6655F5]/40 transition-all">
                    <div>
                        <div class="flex justify-between items-start mb-2">
                            <div>
                                <h4 class="font-bold text-white font-mono text-base">${v.regNo}</h4>
                                <p class="text-xs text-slate-400">${v.model}</p>
                            </div>
                            <span class="px-2 py-0.5 text-[10px] font-bold rounded ${isActive ? 'bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30' : 'bg-slate-800 text-slate-400'}">
                                ${v.status}
                            </span>
                        </div>
                        <div class="mt-2 text-xs text-slate-300 flex items-center gap-1.5">
                            <i data-lucide="user" class="w-3.5 h-3.5 text-slate-500"></i> ${v.driverName || 'Unassigned'}
                        </div>
                        <div class="mt-1 text-xs text-[#0EA5E9] flex items-center gap-1.5">
                            <i data-lucide="map-pin" class="w-3.5 h-3.5 text-slate-500"></i> ${v.locationName}
                        </div>
                    </div>
                    <div class="mt-4 pt-3 border-t border-[#263149] flex justify-between items-center text-xs">
                        <span class="text-slate-400">${hub ? hub.shortName : 'Fleet'}</span>
                        ${this.currentRole === "OWNER" ? `
                            <button onclick="window.SVM_APP.openReassignModal(${v.id})" class="text-xs text-[#6655F5] hover:text-[#8B5CF6] font-semibold underline cursor-pointer">
                                Transfer Hub
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
        }).join("");

        if (window.lucide) window.lucide.createIcons();
    }

    populateQuickDriverDropdown() {
        const select = document.getElementById("modal-quick-driver-select");
        const drivers = window.DEMO_REPO ? window.DEMO_REPO.getDrivers(this.currentHubId) : [];

        if (select) {
            let options = `<option value="">-- Choose Driver (or enter manually below) --</option>`;
            drivers.forEach(d => {
                options += `<option value="${d.id}">${d.name} (${d.phone}) - ${d.vehicleNo || 'No Cab'}</option>`;
            });
            select.innerHTML = options;
        }

        this.renderDriverDropdownOptions(drivers);
    }

    renderDriverDropdownOptions(drivers) {
        const dropdown = document.getElementById("driver-search-dropdown");
        if (!dropdown) return;

        if (!drivers || drivers.length === 0) {
            dropdown.innerHTML = `
                <div class="p-3 text-center text-xs text-[#64748B]">
                    No drivers found. Enter details manually below.
                </div>
            `;
            return;
        }

        dropdown.innerHTML = drivers.map(d => {
            const initials = d.name.split(" ").map(n => n[0]).join("").substring(0, 2).toUpperCase();
            const hubName = d.managerId === "mgr_moosapet_01" ? "Moosapet" : d.managerId === "mgr_kukatpally_02" ? "Kukatpally" : "Madhapur";

            return `
                <div class="dropdown-option flex items-center justify-between p-2.5 hover:bg-[#6655F5]/20 cursor-pointer transition-colors"
                     onclick="window.SVM_APP.selectDriverOption(${d.id})">
                    <div class="flex items-center gap-2.5">
                        <div class="w-7 h-7 rounded-full bg-[#6655F5]/30 border border-[#6655F5]/40 text-[#8B5CF6] font-bold text-xs flex items-center justify-center shrink-0">
                            ${initials}
                        </div>
                        <div class="text-left">
                            <span class="font-bold text-xs text-white block">${d.name}</span>
                            <span class="text-[10px] text-slate-400 font-mono">${d.phone}</span>
                        </div>
                    </div>
                    <div class="text-right shrink-0">
                        <span class="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-[#162038] text-[#10B981] border border-[#263149] inline-block">
                            ${d.vehicleNo || 'No Cab'}
                        </span>
                        <span class="text-[9px] text-slate-400 block mt-0.5">${hubName}</span>
                    </div>
                </div>
            `;
        }).join("");
    }

    toggleDriverDropdown() {
        const dropdown = document.getElementById("driver-search-dropdown");
        if (!dropdown) return;
        if (dropdown.classList.contains("hidden")) {
            this.openDriverDropdown();
        } else {
            this.closeDriverDropdown();
        }
    }

    openDriverDropdown() {
        const dropdown = document.getElementById("driver-search-dropdown");
        const chevron = document.getElementById("driver-search-chevron");
        const input = document.getElementById("driver-search-input");
        if (dropdown) dropdown.classList.remove("hidden");
        if (chevron) chevron.classList.add("rotate-180");
        if (input) input.focus();
    }

    closeDriverDropdown() {
        const dropdown = document.getElementById("driver-search-dropdown");
        const chevron = document.getElementById("driver-search-chevron");
        if (dropdown) dropdown.classList.add("hidden");
        if (chevron) chevron.classList.remove("rotate-180");
    }

    filterDriverDropdown(query) {
        this.openDriverDropdown();
        const drivers = window.DEMO_REPO ? window.DEMO_REPO.getDrivers(this.currentHubId) : [];
        const q = (query || "").toLowerCase().trim();
        if (!q) {
            this.renderDriverDropdownOptions(drivers);
            return;
        }

        const filtered = drivers.filter(d => 
            (d.name && d.name.toLowerCase().includes(q)) ||
            (d.phone && d.phone.toLowerCase().includes(q)) ||
            (d.vehicleNo && d.vehicleNo.toLowerCase().includes(q))
        );

        this.renderDriverDropdownOptions(filtered);
    }

    selectDriverOption(driverId) {
        const drivers = window.DEMO_REPO ? window.DEMO_REPO.getDrivers() : [];
        const d = drivers.find(item => item.id === parseInt(driverId));
        if (d) {
            const searchInput = document.getElementById("driver-search-input");
            if (searchInput) {
                searchInput.value = `${d.name} (${d.vehicleNo || 'No Cab'})`;
            }
            const quickSelect = document.getElementById("modal-quick-driver-select");
            if (quickSelect) quickSelect.value = driverId;
        }

        this.onQuickDriverSelected(driverId);
        this.closeDriverDropdown();
    }

    onQuickDriverSelected(driverId) {
        if (!driverId) return;
        const drivers = window.DEMO_REPO ? window.DEMO_REPO.getDrivers() : [];
        const d = drivers.find(item => item.id === parseInt(driverId));
        if (!d) return;

        const nameEl = document.getElementById("modal-drivername");
        const phoneEl = document.getElementById("modal-driverphone");
        const regnoEl = document.getElementById("modal-regno");
        const hubEl = document.getElementById("modal-hub");

        if (nameEl) nameEl.value = d.name;
        if (phoneEl) phoneEl.value = d.phone;
        if (regnoEl) regnoEl.value = d.vehicleNo || "";
        if (hubEl) hubEl.value = d.managerId || "mgr_moosapet_01";
        
        this.updateModalCalculation();
    }

    openManualEntryModal(prefillRegNo = "", prefillDriverName = "", prefillDriverPhone = "", prefillTrips = "", prefillGross = "") {
        const modal = document.getElementById("manual-entry-modal");
        if (!modal) {
            console.error("Modal element #manual-entry-modal not found!");
            return;
        }

        try {
            this.populateQuickDriverDropdown();

            const searchInput = document.getElementById("driver-search-input");
            if (searchInput) {
                searchInput.value = prefillDriverName ? `${prefillDriverName} (${prefillRegNo || ''})` : "";
            }
            this.closeDriverDropdown();

            const regnoEl = document.getElementById("modal-regno");
            const nameEl = document.getElementById("modal-drivername");
            const phoneEl = document.getElementById("modal-driverphone");
            const tripsEl = document.getElementById("modal-trips");
            const grossEl = document.getElementById("modal-earnings");
            const cashEl = document.getElementById("modal-cash");
            const expEl = document.getElementById("modal-expenditure");
            const passEl = document.getElementById("modal-drivepass");
            const advEl = document.getElementById("modal-advance");
            const remarksEl = document.getElementById("modal-remarks");
            const hubSelect = document.getElementById("modal-hub");

            if (regnoEl) regnoEl.value = prefillRegNo;
            if (nameEl) nameEl.value = prefillDriverName;
            if (phoneEl) phoneEl.value = prefillDriverPhone || "+91 93919 15013";
            if (tripsEl) tripsEl.value = prefillTrips || 12;
            if (grossEl) grossEl.value = prefillGross || 3000;
            if (cashEl) cashEl.value = 2000;
            if (expEl) expEl.value = 200;
            if (passEl) passEl.value = 0;
            if (advEl) advEl.value = 0;
            if (remarksEl) remarksEl.value = "Manual register entry from Moosapet logbook.";

            if (hubSelect) {
                hubSelect.value = this.currentHubId !== "ALL" ? this.currentHubId : "mgr_moosapet_01";
            }

            // Prevent wheel events on modal inputs
            modal.querySelectorAll('input[type="number"]').forEach(input => {
                input.addEventListener("wheel", (e) => e.preventDefault(), { passive: false });
            });

            this.updateModalCalculation();
        } catch (e) {
            console.warn("Error setting modal values:", e);
        }

        // Rely strictly on class state (.open)
        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeManualEntryModal() {
        const modal = document.getElementById("manual-entry-modal");
        if (modal) {
            modal.classList.remove("open");
        }
        this.closeDriverDropdown();
        document.body.classList.remove("modal-open");
    }

    updateModalCalculation() {
        try {
            const shiftType = document.getElementById("modal-shifttype")?.value || "12_HOURS";
            const trips = parseInt(document.getElementById("modal-trips")?.value) || 0;
            const gross = parseFloat(document.getElementById("modal-earnings")?.value) || 0;
            const cash = parseFloat(document.getElementById("modal-cash")?.value) || 0;
            const exp = parseFloat(document.getElementById("modal-expenditure")?.value) || 0;
            const pass = parseFloat(document.getElementById("modal-drivepass")?.value) || 0;
            const advance = parseFloat(document.getElementById("modal-advance")?.value) || 0;

            const result = SvmCommissionCalculator.calculate(shiftType, trips, gross, cash, exp, pass, advance);

            const badgeEl = document.getElementById("calc-preview-trips-badge");
            if (badgeEl) {
                badgeEl.innerText = result.minTripsMet ? `✓ Target Met (${trips} trips)` : `⚠ Below Minimum (${trips}/${shiftType === "12_HOURS" ? 10 : 18} trips)`;
                badgeEl.className = result.minTripsMet ? "text-[#10B981] font-bold text-xs" : "text-[#F59E0B] font-bold text-xs";
            }

            const tierEl = document.getElementById("calc-preview-tier");
            if (tierEl) tierEl.innerText = result.commissionPercentage > 0 ? `${result.commissionPercentage}% Commission Tier` : "0% (Flat Vehicle Rent)";

            const commAmtEl = document.getElementById("calc-preview-comm-amt");
            if (commAmtEl) commAmtEl.innerText = `₹${Math.round(result.commissionAmount)}`;

            const credEl = document.getElementById("calc-preview-credit");
            if (credEl) credEl.innerText = `₹${Math.round(result.driverCredit)} (Comm: ₹${Math.round(result.commissionAmount)} + Fuel: ₹${Math.round(exp)})`;

            const debEl = document.getElementById("calc-preview-debit");
            if (debEl) debEl.innerText = `₹${Math.round(result.driverDebit)} (Cash: ₹${Math.round(cash)} + Adv: ₹${Math.round(advance)})`;

            const netEl = document.getElementById("calc-preview-net");
            if (netEl) {
                if (result.netBalance >= 0) {
                    netEl.innerText = `+₹${Math.round(result.netBalance)} (Company pays Driver)`;
                    netEl.className = "text-[#10B981] font-extrabold text-base";
                } else {
                    netEl.innerText = `-₹${Math.round(Math.abs(result.netBalance))} (Driver deposits cash to Office)`;
                    netEl.className = "text-[#F59E0B] font-extrabold text-base";
                }
            }
        } catch (e) {
            console.warn("Calculation preview error:", e);
        }
    }

    async submitManualEntryForm(e) {
        e.preventDefault();

        const shiftType = document.getElementById("modal-shifttype").value;
        const driverName = document.getElementById("modal-drivername").value.trim();
        const driverPhone = document.getElementById("modal-driverphone").value.trim();
        const vehicleNo = document.getElementById("modal-regno").value.trim().toUpperCase();
        const hubId = document.getElementById("modal-hub").value;
        const trips = parseInt(document.getElementById("modal-trips").value) || 0;
        const gross = parseFloat(document.getElementById("modal-earnings").value) || 0;
        const cash = parseFloat(document.getElementById("modal-cash").value) || 0;
        const exp = parseFloat(document.getElementById("modal-expenditure").value) || 0;
        const pass = parseFloat(document.getElementById("modal-drivepass").value) || 0;
        const advance = parseFloat(document.getElementById("modal-advance").value) || 0;
        const status = document.getElementById("modal-status").value;
        const remarks = document.getElementById("modal-remarks").value.trim();

        if (!driverName || !vehicleNo) {
            alert("Please provide both Driver Name and Vehicle Number.");
            return;
        }

        const calc = SvmCommissionCalculator.calculate(shiftType, trips, gross, cash, exp, pass, advance);

        const newLog = {
            shiftType,
            loginDate: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }),
            loginTime: "06:00 AM",
            logoutDate: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }),
            logoutTime: "07:30 PM",
            vehicleNo,
            driverName,
            driverPhone: driverPhone || "+91 93919 15013",
            trips,
            earnings: gross,
            cashCollection: cash,
            expenditure: exp,
            commissionRent: calc.commissionAmount,
            commissionPercentage: calc.commissionPercentage,
            drivePass: pass,
            advance,
            paymentCash: cash > 0 ? cash : 0,
            paymentOnline: 0,
            netBalance: calc.netBalance,
            status: status || "SETTLED",
            remarks: remarks || calc.remarks,
            managerId: hubId,
            source: "Manual"
        };

        if (window.DEMO_REPO) {
            window.DEMO_REPO.addDutyLog(newLog);
        }

        this.closeManualEntryModal();
        this.refreshAll();
        this.showToast(`Manual Shift Log saved for ${driverName}! (Demo Sandbox)`, "success");
    }

    openReassignModal(vehicleId) {
        const vehicles = window.DEMO_REPO ? window.DEMO_REPO.getVehicles() : [];
        const v = vehicles.find(item => item.id === vehicleId);
        if (!v) return;

        const modal = document.getElementById("reassign-hub-modal");
        if (!modal) return;

        document.getElementById("reassign-vehicle-title").innerText = `${v.regNo} (${v.model})`;
        document.getElementById("reassign-vehicle-id").value = v.id;

        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        const hubSelect = document.getElementById("reassign-target-hub");
        if (hubSelect) {
            hubSelect.innerHTML = hubs.map(h => `
                <option value="${h.id}" ${v.managerId === h.id ? 'selected' : ''}>${h.name} (${h.managerName})</option>
            `).join("");
        }

        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeReassignModal() {
        const modal = document.getElementById("reassign-hub-modal");
        if (modal) {
            modal.classList.remove("open");
        }
        document.body.classList.remove("modal-open");
    }

    submitReassignment(e) {
        e.preventDefault();
        const vehicleId = document.getElementById("reassign-vehicle-id").value;
        const targetHubId = document.getElementById("reassign-target-hub").value;

        if (window.DEMO_REPO) {
            window.DEMO_REPO.reassignVehicle(vehicleId, targetHubId);
        }
        this.closeReassignModal();
        this.refreshAll();
        this.showToast("Vehicle & Driver transferred to new Hub! (Demo Sandbox)", "success");
    }

    openAddDriverModal() {
        const modal = document.getElementById("add-driver-modal");
        if (!modal) return;

        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        const hubSelect = document.getElementById("add-driver-hub");
        if (hubSelect) {
            hubSelect.innerHTML = hubs.map(h => `<option value="${h.id}">${h.name}</option>`).join("");
        }

        const vehicleSelect = document.getElementById("add-driver-vehicle");
        if (vehicleSelect) {
            const vehicles = window.DEMO_REPO ? window.DEMO_REPO.getVehicles() : [];
            vehicleSelect.innerHTML = `<option value="">-- No Vehicle Assigned Yet --</option>` +
                vehicles.map(v => `<option value="${v.regNo}">${v.regNo} (${v.model}) - ${v.driverName ? 'Driver: ' + v.driverName : 'Unassigned'}</option>`).join("");
        }

        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeAddDriverModal() {
        const modal = document.getElementById("add-driver-modal");
        if (modal) {
            modal.classList.remove("open");
        }
        document.body.classList.remove("modal-open");
    }

    async submitAddDriver(e) {
        e.preventDefault();
        const name = document.getElementById("add-driver-name")?.value.trim();
        const phone = document.getElementById("add-driver-phone")?.value.trim();
        const managerId = document.getElementById("add-driver-hub")?.value || "mgr_moosapet_01";
        const vehicleNo = document.getElementById("add-driver-vehicle")?.value.trim().toUpperCase() || "";
        const rating = parseFloat(document.getElementById("add-driver-rating")?.value) || 4.85;
        const status = document.getElementById("add-driver-status")?.value || "ON_DUTY";

        if (!name || !phone) {
            alert("Please provide both Driver Name and Mobile Number.");
            return;
        }

        const newDriver = {
            name,
            phone,
            managerId,
            vehicleNo,
            status,
            rating,
            totalTrips: 0
        };

        if (window.DEMO_REPO) {
            window.DEMO_REPO.addDriver(newDriver);
        }

        this.closeAddDriverModal();
        this.refreshAll();
        this.showToast(`Driver ${name} successfully enrolled! (Demo Sandbox)`, "success");
    }

    openAddVehicleModal() {
        const modal = document.getElementById("add-vehicle-modal");
        if (!modal) return;
        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeAddVehicleModal() {
        const modal = document.getElementById("add-vehicle-modal");
        if (modal) {
            modal.classList.remove("open");
        }
        document.body.classList.remove("modal-open");
    }

    async submitAddVehicle(e) {
        e.preventDefault();
        const regNo = document.getElementById("add-veh-reg")?.value.trim().toUpperCase();
        const model = document.getElementById("add-veh-model")?.value.trim();
        const managerId = document.getElementById("add-veh-hub")?.value || "mgr_moosapet_01";
        const platform = document.getElementById("add-veh-platform")?.value || "Uber";

        if (!regNo || !model) {
            alert("Please provide Registration Plate and Vehicle Model.");
            return;
        }

        const newVehicle = {
            regNo,
            model,
            managerId,
            platform,
            driverName: "Unassigned",
            locationName: "Moosapet Yard",
            status: "ACTIVE"
        };

        if (window.DEMO_REPO) {
            window.DEMO_REPO.addVehicle(newVehicle);
        }

        this.closeAddVehicleModal();
        this.refreshAll();
        this.showToast(`Vehicle ${regNo} added to Fleet! (Demo Sandbox)`, "success");
    }

    openAddManagerModal() {
        const modal = document.getElementById("add-manager-modal");
        if (!modal) return;
        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeAddManagerModal() {
        const modal = document.getElementById("add-manager-modal");
        if (modal) {
            modal.classList.remove("open");
        }
        document.body.classList.remove("modal-open");
    }

    async submitAddManager(e) {
        e.preventDefault();
        const hubName = document.getElementById("add-hub-name")?.value.trim();
        const shortName = document.getElementById("add-hub-short")?.value.trim();
        const managerName = document.getElementById("add-hub-manager")?.value.trim();
        const phone = document.getElementById("add-hub-phone")?.value.trim();

        if (!hubName || !managerName || !phone) {
            alert("Please fill in Hub Name, Manager Name, and Phone Number.");
            return;
        }

        const newHub = {
            id: "mgr_" + (shortName || hubName).toLowerCase().replace(/[^a-z0-9]/g, "_") + "_" + Date.now().toString().slice(-4),
            name: hubName,
            shortName: shortName || hubName,
            managerName,
            phone,
            pin: "1234",
            lat: 17.4400,
            lng: 78.3489,
            color: "#6655F5",
            cabsCount: 6
        };

        if (window.DEMO_REPO) {
            window.DEMO_REPO.data.hubs.push(newHub);
            window.DEMO_REPO.saveState();
        }

        this.closeAddManagerModal();
        this.renderRoleSelector();
        if (this.mapController) {
            this.mapController.renderHubs();
        }
        this.refreshAll();
        this.showToast(`New Hub "${hubName}" & Manager ${managerName} created! (Demo Sandbox)`, "success");
    }

    exportCSV() {
        const logs = this.getCurrentFilteredLogs();
        if (logs.length === 0) {
            alert("No logs to export.");
            return;
        }

        const headers = [
            "Log ID", "Shift Type", "Date", "Driver Name", "Driver Phone",
            "Vehicle No", "Trips", "Gross Earned (INR)", "Fuel & Exp (INR)",
            "Cash Collected (INR)", "Commission Tier", "Commission Amount (INR)",
            "Net Balance (INR)", "Status", "Source", "Remarks"
        ];
        const rows = logs.map(l => [
            l.id,
            l.shiftType,
            l.loginDate,
            `"${l.driverName}"`,
            `"${l.driverPhone || ''}"`,
            l.vehicleNo,
            l.trips,
            l.earnings,
            l.expenditure || 0,
            l.cashCollection,
            `${l.commissionPercentage || 0}%`,
            l.commissionRent,
            l.netBalance,
            l.status,
            l.source || "Uber",
            `"${l.remarks || ''}"`
        ]);

        const csvContent = "data:text/csv;charset=utf-8," + [headers.join(","), ...rows.map(e => e.join(","))].join("\n");
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", `SVM_FLEET_Duty_Register_${this.currentHubId}_${Date.now()}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        this.showToast("CSV statement exported successfully! (Demo)", "success");
    }

    printReceipt(logId) {
        const logs = window.DEMO_REPO ? window.DEMO_REPO.getDutyLogs() : [];
        const log = logs.find(l => l.id === logId);
        if (!log) return;

        const printWin = window.open("", "_blank");
        if (!printWin) {
            alert("Please allow popups to print receipt.");
            return;
        }
        printWin.document.write(`
            <html>
            <head>
                <title>SVM FLEET - Shift Settlement Receipt #${log.id}</title>
                <style>
                    body { font-family: monospace; padding: 24px; color: #111; max-width: 600px; margin: 0 auto; }
                    .header { text-align: center; border-bottom: 2px dashed #444; padding-bottom: 12px; margin-bottom: 16px; }
                    .row { display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 14px; }
                    .total { border-top: 2px dashed #444; margin-top: 12px; padding-top: 12px; font-weight: bold; font-size: 16px; }
                    .footer { text-align: center; margin-top: 24px; font-size: 12px; color: #666; border-top: 1px solid #ddd; padding-top: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2>SVM FLEET OPERATING SYSTEM</h2>
                    <p>Moosapet, Hyderabad - 500018 | Phone: 9391915013, 9959859347</p>
                    <h3>DRIVER DUTY SETTLEMENT RECEIPT (DEMO)</h3>
                </div>
                <div class="row"><span>Receipt ID:</span><span>#${log.id}</span></div>
                <div class="row"><span>Date & Time:</span><span>${log.loginDate} (${log.shiftType === '12_HOURS' ? '12h Shift' : '24h Shift'})</span></div>
                <div class="row"><span>Driver:</span><span><strong>${log.driverName}</strong> (${log.driverPhone || 'N/A'})</span></div>
                <div class="row"><span>Vehicle Reg No:</span><span><strong>${log.vehicleNo}</strong></span></div>
                <div class="row"><span>Trips Completed:</span><span><strong>${log.trips}</strong></span></div>
                <hr style="border: 0; border-top: 1px dashed #aaa; margin: 10px 0;" />
                <div class="row"><span>Gross Earnings:</span><span>₹${log.earnings}</span></div>
                <div class="row"><span>Commission Tier:</span><span>${log.commissionPercentage || 0}%</span></div>
                <div class="row"><span>Commission Amount:</span><span>₹${log.commissionRent}</span></div>
                <div class="row"><span>Fuel & Daily Expenditure:</span><span>₹${log.expenditure || 0}</span></div>
                <div class="row"><span>Cash Collected from Riders:</span><span>₹${log.cashCollection}</span></div>
                <div class="total row">
                    <span>NET BALANCE:</span>
                    <span>₹${log.netBalance >= 0 ? '+' : ''}${log.netBalance} (${log.netBalance >= 0 ? 'Company Pays Driver' : 'Driver Deposited to Office'})</span>
                </div>
                <div class="footer">
                    <p>Remarks: ${log.remarks || 'Standard shift completion.'}</p>
                    <p>Verified by SVM FLEET Hub Officer (Demo)</p>
                </div>
            </body>
            </html>
        `);
        printWin.document.close();
        printWin.focus();
        setTimeout(() => {
            printWin.print();
        }, 300);
    }

    switchTab(tabId) {
        this.activeTab = tabId;

        // Update sidebar links active class
        document.querySelectorAll("#sidebar-nav button, [data-nav-tab]").forEach(btn => {
            const isCurrent = btn.getAttribute("data-nav-tab") === tabId;
            if (isCurrent) {
                btn.classList.add("active");
                const icon = btn.querySelector("i, svg");
                if (icon) {
                    icon.classList.remove("text-[#94A3B8]");
                    icon.classList.add("text-white");
                }
            } else {
                btn.classList.remove("active");
                const icon = btn.querySelector("i, svg");
                if (icon) {
                    icon.classList.remove("text-white");
                    icon.classList.add("text-[#94A3B8]");
                }
            }
        });

        // Toggle tab content panels
        document.querySelectorAll("[data-tab-content]").forEach(content => {
            content.classList.add("hidden");
        });
        const activeContent = document.querySelector(`[data-tab-content="${tabId}"]`);
        if (activeContent) {
            activeContent.classList.remove("hidden");
        }

        // Invalidate map size on map tab switch
        if (tabId === "map" || tabId === "overview") {
            setTimeout(() => {
                if (this.mapController && this.mapController.map) {
                    this.mapController.map.invalidateSize();
                }
            }, 100);
        }
    }

    renderNotifications() {
        const unreadCount = this.notifications.filter(n => n.unread).length;
        const badge = document.getElementById("notif-badge");
        if (badge) {
            if (unreadCount > 0) {
                badge.textContent = unreadCount;
                badge.classList.remove("hidden");
            } else {
                badge.classList.add("hidden");
            }
        }

        const pill = document.getElementById("notif-count-pill");
        if (pill) {
            pill.textContent = unreadCount > 0 ? `${unreadCount} New` : "All caught up";
        }

        const container = document.getElementById("notification-items-container");
        if (container) {
            container.innerHTML = this.notifications.map(n => `
                <div class="p-3.5 hover:bg-[#161F33] transition-colors flex items-start gap-3 ${n.unread ? 'bg-[#0E1528]/80' : 'opacity-70'}">
                    <span class="w-2 h-2 mt-1.5 rounded-full shrink-0 ${n.unread ? (n.type === 'success' ? 'bg-[#10B981]' : n.type === 'warning' ? 'bg-[#F59E0B]' : 'bg-[#6655F5]') : 'bg-transparent'}"></span>
                    <div class="flex-1 min-w-0">
                        <div class="flex items-center justify-between gap-1 mb-0.5">
                            <span class="font-bold text-xs text-white truncate">${n.title}</span>
                            <span class="text-[10px] text-slate-500 font-mono shrink-0">${n.time}</span>
                        </div>
                        <p class="text-[11px] text-slate-300 leading-relaxed">${n.text}</p>
                    </div>
                </div>
            `).join("");
        }
    }

    toggleNotifications() {
        const dropdown = document.getElementById("notification-dropdown");
        if (dropdown) {
            dropdown.classList.toggle("open");
        }
    }

    markAllNotificationsRead() {
        this.notifications.forEach(n => { n.unread = false; });
        this.renderNotifications();
        this.showToast("All telemetry alerts marked as read.", "info");
    }

    openUserGuideModal() {
        const modal = document.getElementById("user-guide-modal");
        if (modal) {
            modal.classList.add("open");
            modal.scrollTop = 0;
            document.body.classList.add("modal-open");
            this.switchGuideTab("guide-quickstart");
        }
    }

    closeUserGuideModal() {
        const modal = document.getElementById("user-guide-modal");
        if (modal) {
            modal.classList.remove("open");
        }
        document.body.classList.remove("modal-open");
    }

    switchGuideTab(tabId) {
        this.activeGuideTab = tabId;
        document.querySelectorAll(".guide-tab-btn").forEach(btn => {
            btn.className = "guide-tab-btn px-3 py-1.5 rounded-lg text-xs font-semibold text-[#94A3B8] hover:text-white transition-all";
        });
        const activeBtn = document.getElementById(`tab-btn-${tabId}`);
        if (activeBtn) {
            activeBtn.className = "guide-tab-btn px-3 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white transition-all";
        }

        document.querySelectorAll(".guide-content-panel").forEach(panel => {
            panel.classList.add("hidden");
        });
        const activePanel = document.getElementById(tabId);
        if (activePanel) {
            activePanel.classList.remove("hidden");
        }

        if (window.lucide) window.lucide.createIcons();
    }

    showToast(message, type = "info") {
        const toast = document.createElement("div");
        const bgColor = type === "success" ? "bg-[#10B981]" : type === "alert" ? "bg-red-600" : "bg-[#6655F5]";
        toast.className = `fixed bottom-6 right-6 ${bgColor} text-white px-4 py-3 rounded-xl shadow-2xl z-[999999] text-xs font-bold flex items-center gap-2 animate-bounce border border-white/20`;
        toast.innerHTML = `<span>✓</span> <span>${message}</span>`;
        document.body.appendChild(toast);
        setTimeout(() => {
            toast.remove();
        }, 3200);
    }
}

// Global bootstrap
document.addEventListener("DOMContentLoaded", () => {
    window.SVM_APP = new SvmFleetApp();
    window.SVM_APP.init();
});
