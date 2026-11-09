/**
 * SVM FLEET Operating System (Web Portal)
 * Main Application Controller & View Orchestrator
 */

class SvmFleetApp {
    constructor() {
        this.currentRole = localStorage.getItem(APP_CONFIG.storageKeys.role) || "OWNER";
        this.currentHubId = localStorage.getItem(APP_CONFIG.storageKeys.hubId) || "ALL";
        this.activeTab = "overview";
        this.mapController = null;
        this.searchQuery = "";
        this.shiftFilter = "ALL";
        this.registerViewMode = "cards"; // "cards" or "table"
    }

    init() {
        this.setupEventListeners();
        this.renderRoleSelector();
        this.mapController = new HyderabadMapController("hyderabad-map");
        this.mapController.init();
        this.refreshAll();

        // Refresh icons
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    setupEventListeners() {
        // Tab switching
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
            }
        });
    }

    renderRoleSelector() {
        const selector = document.getElementById("role-selector");
        if (!selector) return;

        let options = `
            <option value="OWNER:ALL" ${this.currentRole === "OWNER" && this.currentHubId === "ALL" ? "selected" : ""}>
                👑 Fleet Owner (Master - All Hubs)
            </option>
        `;

        APP_CONFIG.hubs.forEach(hub => {
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
        localStorage.setItem(APP_CONFIG.storageKeys.role, role);
        localStorage.setItem(APP_CONFIG.storageKeys.hubId, hubId);

        this.renderHubPills();
        this.refreshAll();

        if (this.mapController) {
            this.mapController.focusHub(hubId);
        }

        this.showToast(`Switched view to ${role === "OWNER" ? "Fleet Owner (Master)" : this.getActiveHubName() + " Manager"}`, "info");
    }

    renderHubPills() {
        const container = document.getElementById("hub-filter-pills");
        if (!container) return;

        if (this.currentRole === "MANAGER") {
            const hub = APP_CONFIG.hubs.find(h => h.id === this.currentHubId);
            container.innerHTML = `
                <span class="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-purple-900/40 border border-purple-500/40 text-purple-300">
                    <span class="pulse-dot"></span> Scoped: ${hub ? hub.name : 'Assigned Hub'}
                </span>
            `;
            return;
        }

        // Owner: Show clickable pills for filtering
        let html = `
            <button onclick="window.SVM_APP.filterHub('ALL')" class="hub-pill px-3 py-1 text-xs font-semibold rounded-full border transition-all ${this.currentHubId === 'ALL' ? 'bg-[#5B36F5] text-white border-[#5B36F5]' : 'bg-[#181C26] text-gray-400 border-gray-700 hover:text-white'}">
                All Hubs (30 Cabs)
            </button>
        `;

        APP_CONFIG.hubs.forEach(hub => {
            const isSelected = this.currentHubId === hub.id;
            html += `
                <button onclick="window.SVM_APP.filterHub('${hub.id}')" class="hub-pill px-3 py-1 text-xs font-semibold rounded-full border transition-all ${isSelected ? 'bg-[#5B36F5] text-white border-[#5B36F5]' : 'bg-[#181C26] text-gray-400 border-gray-700 hover:text-white'}">
                    ${hub.shortName}
                </button>
            `;
        });

        container.innerHTML = html;
    }

    filterHub(hubId) {
        this.currentHubId = hubId;
        this.renderHubPills();
        this.refreshAll();
        if (this.mapController) {
            this.mapController.focusHub(hubId);
        }
    }

    setShiftFilter(filter) {
        this.shiftFilter = filter;
        document.querySelectorAll(".shift-filter-btn").forEach(btn => {
            btn.className = "shift-filter-btn px-3 py-1.5 rounded-full text-xs font-medium border border-gray-700 bg-gray-900 text-gray-400 hover:text-white";
        });
        const activeBtn = event ? event.currentTarget : null;
        if (activeBtn) {
            activeBtn.className = "shift-filter-btn px-3 py-1.5 rounded-full text-xs font-bold border transition-all bg-[#5B36F5] text-white border-[#5B36F5]";
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
            if (cardsBtn) cardsBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-bold bg-[#5B36F5] text-white flex items-center gap-1";
            if (tableBtn) tableBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-medium text-gray-400 hover:text-white flex items-center gap-1";
        } else {
            if (cardsContainer) cardsContainer.classList.add("hidden");
            if (tableContainer) tableContainer.classList.remove("hidden");
            if (cardsBtn) cardsBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-medium text-gray-400 hover:text-white flex items-center gap-1";
            if (tableBtn) tableBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-bold bg-[#5B36F5] text-white flex items-center gap-1";
        }
        if (window.lucide) window.lucide.createIcons();
    }

    getActiveHubName() {
        const hub = APP_CONFIG.hubs.find(h => h.id === this.currentHubId);
        return hub ? hub.shortName : "All Hyderabad";
    }

    getCurrentFilteredVehicles() {
        return FleetDataRepository.getVehicles(this.currentHubId);
    }

    getCurrentFilteredDrivers() {
        return FleetDataRepository.getDrivers(this.currentHubId);
    }

    getCurrentFilteredLogs() {
        let logs = FleetDataRepository.getDutyLogs(this.currentHubId);

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
                (l.remarks && l.remarks.toLowerCase().includes(this.searchQuery))
            );
        }
        return logs;
    }

    refreshAll() {
        this.renderHubPills();
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
        const logs = FleetDataRepository.getDutyLogs(this.currentHubId);

        const totalVehicles = vehicles.length;
        const activeVehicles = vehicles.filter(v => v.status === "ACTIVE").length;

        let totalGross = 0;
        let totalCash = 0;
        let totalFuelAndComm = 0;

        logs.forEach(l => {
            totalGross += (l.earnings || 0);
            totalCash += (l.cashCollection || 0);
            totalFuelAndComm += (l.commissionRent || 0) + (l.expenditure || 0);
        });

        document.getElementById("stat-total-cabs").innerText = totalVehicles;
        document.getElementById("stat-active-cabs").innerText = activeVehicles;
        document.getElementById("stat-gross-revenue").innerText = SvmCommissionCalculator.formatINR(totalGross);
        document.getElementById("stat-cash-collected").innerText = SvmCommissionCalculator.formatINR(totalCash);
        document.getElementById("stat-net-settled").innerText = SvmCommissionCalculator.formatINR(totalFuelAndComm);
    }

    renderDutyRegister() {
        const tbodyOverview = document.getElementById("register-table-body");
        const tbodyFull = document.getElementById("register-table-body-full");
        const cardsContainer = document.getElementById("register-cards-container");

        const logs = this.getCurrentFilteredLogs();

        // 1. Render Mobile-App Style DutyLogCards
        if (cardsContainer) {
            if (logs.length === 0) {
                cardsContainer.innerHTML = `
                    <div class="col-span-full py-12 text-center text-gray-500 glass-card">
                        <i data-lucide="inbox" class="w-8 h-8 mx-auto mb-2 opacity-50"></i>
                        No duty logbook records match your filter.
                    </div>
                `;
            } else {
                cardsContainer.innerHTML = logs.map(log => {
                    const isSettled = log.status === "SETTLED";
                    const is12H = log.shiftType === "12_HOURS";
                    const isAuto = log.source === "AUTOMATIC_TELEMETRY";
                    const commTier = log.commissionPercentage || 0;
                    const tierClass = commTier >= 40 ? "tier-40" : commTier >= 35 ? "tier-35" : commTier >= 30 ? "tier-30" : commTier >= 25 ? "tier-25" : "tier-none";

                    return `
                        <div class="glass-card p-4 flex flex-col justify-between hover:border-purple-500/50 transition-all">
                            <!-- Card Header (Mirroring DutyLogCard) -->
                            <div>
                                <div class="flex justify-between items-start mb-3">
                                    <div class="flex items-center gap-3">
                                        <div class="w-10 h-10 rounded-full bg-purple-900/40 border border-purple-500/30 flex items-center justify-center text-purple-300">
                                            <i data-lucide="car" class="w-5 h-5"></i>
                                        </div>
                                        <div>
                                            <div class="flex items-center gap-2">
                                                <h4 class="font-extrabold text-base text-white font-mono">${log.vehicleNo}</h4>
                                                <span class="text-[10px] font-bold px-2 py-0.5 rounded-full ${isSettled ? 'bg-emerald-900/40 text-emerald-300 border border-emerald-500/30' : 'bg-amber-900/40 text-amber-300 border border-amber-500/30'}">
                                                    ${log.status}
                                                </span>
                                            </div>
                                            <div class="flex items-center gap-2 text-xs text-gray-300 mt-0.5">
                                                <span class="font-bold">${log.driverName}</span>
                                                <span class="text-gray-500">•</span>
                                                <a href="tel:${log.driverPhone || ''}" class="text-blue-400 hover:underline font-mono text-[11px] flex items-center gap-1">
                                                    <i data-lucide="phone" class="w-3 h-3"></i> ${log.driverPhone || 'No Phone'}
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="text-right">
                                        <span class="text-xs text-gray-400 font-semibold block">${is12H ? '12h Shift' : '24h Shift'}</span>
                                        <span class="text-[10px] font-bold px-2 py-0.5 rounded ${isAuto ? 'bg-purple-900/50 text-purple-300' : 'bg-gray-800 text-gray-400'}">
                                            ${isAuto ? '⚡ Auto' : '📝 Manual'}
                                        </span>
                                    </div>
                                </div>

                                <!-- Date & Log Time Row -->
                                <div class="py-1.5 px-3 rounded-lg bg-[#12151C] border border-[#232836] flex justify-between text-[11px] text-gray-400 mb-3">
                                    <span>Date: <strong class="text-gray-200">${log.loginDate}</strong></span>
                                    <span>Time: <strong class="text-gray-200">${log.loginTime || '06:00 AM'} - ${log.logoutTime || '08:00 PM'}</strong></span>
                                </div>

                                <!-- 4-Metric Financial Matrix (TRIPS, EARNINGS, CASH COLL, COMMISSION) -->
                                <div class="metric-matrix mb-3">
                                    <div>
                                        <div class="metric-item-title">Trips</div>
                                        <div class="metric-item-value text-white">${log.trips}</div>
                                    </div>
                                    <div>
                                        <div class="metric-item-title">Gross Fare</div>
                                        <div class="metric-item-value text-emerald-400">₹${Math.round(log.earnings)}</div>
                                    </div>
                                    <div>
                                        <div class="metric-item-title">Cash Coll.</div>
                                        <div class="metric-item-value text-amber-400">₹${Math.round(log.cashCollection)}</div>
                                    </div>
                                    <div>
                                        <div class="metric-item-title">Commission</div>
                                        <div class="metric-item-value text-purple-400">₹${Math.round(log.commissionRent)}</div>
                                    </div>
                                </div>

                                <!-- Secondary Breakdown: Fuel, Pass, Advance & Tier -->
                                <div class="text-xs text-gray-400 flex flex-wrap justify-between items-center py-2 border-t border-[#232836]">
                                    <span class="flex items-center gap-1">
                                        <i data-lucide="fuel" class="w-3.5 h-3.5 text-blue-400"></i> Fuel/Exp: <strong class="text-blue-300">₹${log.expenditure || 0}</strong>
                                    </span>
                                    <span>Pass: <strong class="text-gray-300">₹${log.drivePass || 0}</strong></span>
                                    <span>Adv: <strong class="text-gray-300">₹${log.advance || 0}</strong></span>
                                    <span class="pill-badge ${tierClass}">
                                        ${commTier > 0 ? commTier + '% Tier' : 'No Tier'}
                                    </span>
                                </div>
                            </div>

                            <!-- Net Settlement Footer -->
                            <div class="mt-3 pt-3 border-t border-[#232836] flex justify-between items-center">
                                <div>
                                    <span class="text-[10px] text-gray-500 uppercase tracking-wider block font-bold">Net Balance Settlement</span>
                                    <span class="font-extrabold text-sm ${log.netBalance >= 0 ? 'text-emerald-400' : 'text-amber-400'}">
                                        ${log.netBalance >= 0 ? 'Company Pays: ₹' + Math.round(log.netBalance) : 'Driver Owes: ₹' + Math.round(Math.abs(log.netBalance))}
                                    </span>
                                </div>
                                <button onclick="window.SVM_APP.printReceipt('${log.id}')" class="px-3 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-300 hover:text-white text-xs font-bold flex items-center gap-1.5 transition-colors">
                                    <i data-lucide="printer" class="w-3.5 h-3.5"></i> Receipt
                                </button>
                            </div>
                        </div>
                    `;
                }).join("");
            }
        }

        // 2. Render Tables (Overview Table & Full Table)
        const tableRowsHtml = logs.map(log => {
            const isAuto = log.source === "AUTOMATIC_TELEMETRY";
            const badgeClass = isAuto ? "bg-purple-900/40 text-purple-300 border border-purple-500/30" : "bg-gray-800 text-gray-300 border border-gray-700";
            const sourceLabel = isAuto ? "⚡ Auto" : "📝 Manual";
            const isSettled = log.status === "SETTLED";
            const statusBadge = isSettled ? "bg-emerald-900/40 text-emerald-300 border border-emerald-500/30" : "bg-amber-900/40 text-amber-300 border border-amber-500/30";

            return `
                <tr class="border-b border-[#232836] hover:bg-[#181C26]/60 transition-colors">
                    <td class="py-3 px-4">
                        <div class="font-bold text-sm text-white">${log.driverName}</div>
                        <a href="tel:${log.driverPhone || ''}" class="text-xs text-blue-400 hover:underline font-mono">${log.driverPhone || ''}</a>
                    </td>
                    <td class="py-3 px-4">
                        <span class="inline-block px-2 py-0.5 rounded bg-gray-900 border border-gray-700 font-mono text-xs font-bold text-gray-200">
                            ${log.vehicleNo}
                        </span>
                        <div class="text-[10px] text-gray-500 mt-0.5">${log.shiftType === "12_HOURS" ? "12h Shift" : "24h Shift"}</div>
                    </td>
                    <td class="py-3 px-4 text-center font-bold text-white text-sm">
                        ${log.trips}
                    </td>
                    <td class="py-3 px-4 text-right font-bold text-emerald-400">
                        ₹${Math.round(log.earnings).toLocaleString('en-IN')}
                    </td>
                    <td class="py-3 px-4 text-right font-bold text-blue-400 font-mono">
                        ₹${Math.round(log.expenditure || 0).toLocaleString('en-IN')}
                    </td>
                    <td class="py-3 px-4 text-right text-amber-400 font-mono font-medium">
                        ₹${Math.round(log.cashCollection).toLocaleString('en-IN')}
                    </td>
                    <td class="py-3 px-4 text-right text-purple-400 font-bold">
                        ₹${Math.round(log.commissionRent).toLocaleString('en-IN')}
                        <span class="text-[10px] block text-gray-500 font-normal">(${log.commissionPercentage || 0}%)</span>
                    </td>
                    <td class="py-3 px-4 text-right font-mono text-xs ${log.netBalance >= 0 ? 'text-emerald-400' : 'text-amber-400'}">
                        ${log.netBalance >= 0 ? '+₹' + Math.round(log.netBalance) : '-₹' + Math.round(Math.abs(log.netBalance))}
                    </td>
                    <td class="py-3 px-4 text-center">
                        <span class="inline-block px-2 py-0.5 text-[10px] font-semibold rounded ${badgeClass}">
                            ${sourceLabel}
                        </span>
                    </td>
                    <td class="py-3 px-4 text-center">
                        <span class="inline-block px-2 py-0.5 text-[10px] font-semibold rounded ${statusBadge}">
                            ${log.status}
                        </span>
                    </td>
                    <td class="py-3 px-4 text-right">
                        <button onclick="window.SVM_APP.printReceipt('${log.id}')" class="p-1.5 rounded hover:bg-gray-700/60 text-gray-400 hover:text-white" title="Print Receipt">
                            <i data-lucide="printer" class="w-4 h-4"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join("");

        if (tbodyOverview) tbodyOverview.innerHTML = tableRowsHtml;
        if (tbodyFull) tbodyFull.innerHTML = tableRowsHtml;

        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    renderDriversGrid() {
        const container = document.getElementById("drivers-grid");
        if (!container) return;

        const drivers = this.getCurrentFilteredDrivers();

        container.innerHTML = drivers.map(d => {
            const hub = APP_CONFIG.hubs.find(h => h.id === d.managerId);
            const hubLabel = hub ? hub.shortName : "Hub";

            return `
                <div class="glass-card p-4 flex flex-col justify-between">
                    <div>
                        <div class="flex justify-between items-start mb-2">
                            <div>
                                <h4 class="font-bold text-white text-base">${d.name}</h4>
                                <a href="tel:${d.phone}" class="text-xs text-blue-400 hover:underline font-mono flex items-center gap-1 mt-0.5">
                                    <i data-lucide="phone" class="w-3 h-3"></i> ${d.phone}
                                </a>
                            </div>
                            <span class="px-2 py-0.5 text-[10px] font-bold rounded bg-purple-900/40 text-purple-300 border border-purple-500/30">
                                ${hubLabel}
                            </span>
                        </div>
                        <div class="mt-3 py-2 px-3 rounded-lg bg-[#12151C] border border-[#232836] flex justify-between text-xs">
                            <span class="text-gray-400">Assigned Car:</span>
                            <span class="font-mono font-bold text-gray-200">${d.vehicleNo}</span>
                        </div>
                    </div>
                    <div class="mt-4 pt-3 border-t border-[#232836] flex justify-between items-center text-xs">
                        <span class="text-emerald-400 font-semibold">⭐ ${d.rating} Rating</span>
                        <button onclick="window.SVM_APP.openManualEntryModal('${d.vehicleNo}', '${d.name}', '${d.phone}')" class="px-2.5 py-1 rounded bg-[#5B36F5] hover:bg-[#4a2ae0] text-white font-bold text-xs">
                            + Shift Entry
                        </button>
                    </div>
                </div>
            `;
        }).join("");
    }

    renderVehiclesGrid() {
        const container = document.getElementById("cars-grid");
        if (!container) return;

        const vehicles = this.getCurrentFilteredVehicles();

        container.innerHTML = vehicles.map(v => {
            const hub = APP_CONFIG.hubs.find(h => h.id === v.managerId);
            const isActive = v.status === "ACTIVE";

            return `
                <div class="glass-card p-4 flex flex-col justify-between">
                    <div>
                        <div class="flex justify-between items-start mb-2">
                            <div>
                                <h4 class="font-bold text-white font-mono text-base">${v.regNo}</h4>
                                <p class="text-xs text-gray-400">${v.model}</p>
                            </div>
                            <span class="px-2 py-0.5 text-[10px] font-bold rounded ${isActive ? 'bg-emerald-900/40 text-emerald-300 border border-emerald-500/30' : 'bg-gray-800 text-gray-400'}">
                                ${v.status}
                            </span>
                        </div>
                        <div class="mt-2 text-xs text-gray-300 flex items-center gap-1">
                            <i data-lucide="user" class="w-3.5 h-3.5 text-gray-500"></i> ${v.driverName}
                        </div>
                        <div class="mt-1 text-xs text-blue-400 flex items-center gap-1">
                            <i data-lucide="map-pin" class="w-3.5 h-3.5 text-gray-500"></i> ${v.locationName}
                        </div>
                    </div>
                    <div class="mt-4 pt-3 border-t border-[#232836] flex justify-between items-center text-xs">
                        <span class="text-gray-400">${hub ? hub.shortName : 'Fleet'}</span>
                        ${this.currentRole === "OWNER" ? `
                            <button onclick="window.SVM_APP.openReassignModal(${v.id})" class="text-xs text-purple-400 hover:text-purple-300 font-semibold underline">
                                Transfer Hub
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
        }).join("");
    }

    renderPendingTelemetryList() {
        const container = document.getElementById("telemetry-pending-list");
        if (!container) return;

        let pending = window.PENDING_TELEMETRY_SHIFTS;
        if (this.currentHubId !== "ALL") {
            pending = pending.filter(p => p.managerId === this.currentHubId);
        }

        if (pending.length === 0) {
            container.innerHTML = `
                <div class="p-4 rounded-xl bg-gray-900/40 border border-gray-800 text-center text-gray-500 text-xs">
                    No pending shifts waiting for sync. All telemetries logged.
                </div>
            `;
            return;
        }

        container.innerHTML = pending.map((shift, idx) => {
            const calc = SvmCommissionCalculator.calculate(shift.shiftType, shift.trips, shift.earnings, shift.cashCollection, shift.expenditure);

            return `
                <div class="p-3 rounded-xl bg-[#12151C] border border-[#232836] flex flex-col justify-between gap-2.5">
                    <div>
                        <div class="flex items-center gap-2">
                            <span class="pulse-dot"></span>
                            <strong class="text-white text-sm font-bold">${shift.driverName}</strong>
                            <span class="px-2 py-0.5 rounded bg-gray-800 text-xs font-mono text-gray-300">${shift.vehicleNo}</span>
                        </div>
                        <div class="text-[11px] text-gray-400 mt-1">
                            ${shift.capturedPlatforms.join(", ")}
                        </div>
                        <div class="grid grid-cols-3 gap-2 mt-2 pt-2 border-t border-[#232836] text-[11px]">
                            <div>Trips: <strong class="text-white">${shift.trips}</strong></div>
                            <div>Gross: <strong class="text-emerald-400">₹${shift.earnings}</strong></div>
                            <div>Fuel: <strong class="text-blue-400">₹${shift.expenditure}</strong></div>
                        </div>
                    </div>
                    <button onclick="window.SVM_APP.autoAggregateShift(${idx})" class="w-full mt-1 py-1.5 rounded-lg bg-[#5B36F5] hover:bg-[#4a2ae0] text-white font-bold text-xs flex items-center justify-center gap-1.5 shadow-lg shadow-purple-500/20">
                        <span>⚡ 1-Tap Auto-Aggregate</span>
                    </button>
                </div>
            `;
        }).join("");
    }

    autoAggregateShift(index) {
        const shift = window.PENDING_TELEMETRY_SHIFTS[index];
        if (!shift) return;

        const calc = SvmCommissionCalculator.calculate(shift.shiftType, shift.trips, shift.earnings, shift.cashCollection, shift.expenditure);

        const newLog = {
            id: "log_" + Date.now(),
            shiftType: shift.shiftType,
            loginDate: new Date().toLocaleDateString('en-GB'),
            loginTime: "06:00 AM",
            logoutDate: new Date().toLocaleDateString('en-GB'),
            logoutTime: "08:00 PM",
            vehicleNo: shift.vehicleNo,
            driverName: shift.driverName,
            driverPhone: shift.driverPhone,
            trips: shift.trips,
            earnings: shift.earnings,
            cashCollection: shift.cashCollection,
            expenditure: shift.expenditure,
            commissionRent: calc.commissionAmount,
            commissionPercentage: calc.commissionPercentage,
            drivePass: 0,
            advance: 0,
            paymentCash: 0,
            paymentOnline: Math.abs(calc.netBalance),
            netBalance: calc.netBalance,
            status: "SETTLED",
            remarks: `Auto-aggregated from ${shift.capturedPlatforms.join(", ")}. ${calc.commissionPercentage}% tier verified.`,
            managerId: shift.managerId,
            source: "AUTOMATIC_TELEMETRY"
        };

        FleetDataRepository.saveDutyLog(newLog);

        window.PENDING_TELEMETRY_SHIFTS.splice(index, 1);

        this.refreshAll();
        this.showToast(`Auto-aggregated shift for ${shift.driverName} into Master Register!`, "success");
    }

    populateQuickDriverDropdown() {
        const select = document.getElementById("modal-quick-driver-select");
        if (!select) return;

        const drivers = FleetDataRepository.getDrivers();
        let options = `<option value="">-- Choose Driver (or enter manually below) --</option>`;
        drivers.forEach(d => {
            options += `<option value="${d.id}">${d.name} (${d.phone}) — ${d.vehicleNo}</option>`;
        });
        select.innerHTML = options;
    }

    onQuickDriverSelected(driverId) {
        if (!driverId) return;
        const drivers = FleetDataRepository.getDrivers();
        const d = drivers.find(item => item.id === parseInt(driverId));
        if (!d) return;

        document.getElementById("modal-drivername").value = d.name;
        document.getElementById("modal-driverphone").value = d.phone;
        document.getElementById("modal-regno").value = d.vehicleNo;
        document.getElementById("modal-hub").value = d.managerId || "mgr_moosapet_01";
        this.updateModalCalculation();
    }

    openManualEntryModal(prefillRegNo = "", prefillDriverName = "", prefillDriverPhone = "", prefillTrips = "", prefillGross = "") {
        const modal = document.getElementById("manual-entry-modal");
        if (!modal) return;

        this.populateQuickDriverDropdown();

        document.getElementById("modal-regno").value = prefillRegNo;
        document.getElementById("modal-drivername").value = prefillDriverName;
        document.getElementById("modal-driverphone").value = prefillDriverPhone || "+91 93919 15013";
        document.getElementById("modal-trips").value = prefillTrips || 12;
        document.getElementById("modal-earnings").value = prefillGross || 3000;
        document.getElementById("modal-cash").value = 2000;
        document.getElementById("modal-expenditure").value = 200;
        document.getElementById("modal-drivepass").value = 0;
        document.getElementById("modal-advance").value = 0;
        document.getElementById("modal-remarks").value = "Manual register entry from Moosapet logbook.";

        const hubSelect = document.getElementById("modal-hub");
        if (hubSelect) {
            hubSelect.value = this.currentHubId !== "ALL" ? this.currentHubId : "mgr_moosapet_01";
        }

        this.updateModalCalculation();
        modal.classList.remove("hidden");
    }

    closeManualEntryModal() {
        const modal = document.getElementById("manual-entry-modal");
        if (modal) modal.classList.add("hidden");
    }

    updateModalCalculation() {
        const shiftType = document.getElementById("modal-shifttype").value;
        const trips = parseInt(document.getElementById("modal-trips").value) || 0;
        const gross = parseFloat(document.getElementById("modal-earnings").value) || 0;
        const cash = parseFloat(document.getElementById("modal-cash").value) || 0;
        const exp = parseFloat(document.getElementById("modal-expenditure").value) || 0;
        const pass = parseFloat(document.getElementById("modal-drivepass").value) || 0;
        const advance = parseFloat(document.getElementById("modal-advance").value) || 0;

        const result = SvmCommissionCalculator.calculate(shiftType, trips, gross, cash, exp, pass, advance);

        const badgeEl = document.getElementById("calc-preview-trips-badge");
        if (badgeEl) {
            badgeEl.innerText = result.minTripsMet ? `✓ Target Met (${trips} trips)` : `⚠ Below Minimum (${trips}/${shiftType === "12_HOURS" ? 10 : 18} trips)`;
            badgeEl.className = result.minTripsMet ? "text-emerald-400 font-bold text-xs" : "text-amber-400 font-bold text-xs";
        }

        document.getElementById("calc-preview-tier").innerText = result.commissionPercentage > 0 ? `${result.commissionPercentage}% Commission Tier` : "0% (No Tier Applied)";
        document.getElementById("calc-preview-comm-amt").innerText = `₹${Math.round(result.commissionAmount)}`;
        document.getElementById("calc-preview-credit").innerText = `₹${Math.round(result.driverCredit)} (Comm: ₹${Math.round(result.commissionAmount)} + Fuel: ₹${Math.round(exp)})`;
        document.getElementById("calc-preview-debit").innerText = `₹${Math.round(result.driverDebit)} (Cash: ₹${Math.round(cash)} + Adv: ₹${Math.round(advance)})`;

        const netEl = document.getElementById("calc-preview-net");
        if (result.netBalance >= 0) {
            netEl.innerText = `+₹${Math.round(result.netBalance)} (Company pays Driver)`;
            netEl.className = "text-emerald-400 font-extrabold text-base";
        } else {
            netEl.innerText = `-₹${Math.round(Math.abs(result.netBalance))} (Driver deposits cash to Office)`;
            netEl.className = "text-amber-400 font-extrabold text-base";
        }
    }

    submitManualEntryForm(e) {
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
            alert("Please provide both Driver Name and Vehicle Reg Number.");
            return;
        }

        const calc = SvmCommissionCalculator.calculate(shiftType, trips, gross, cash, exp, pass, advance);

        const newLog = {
            id: "log_" + Date.now(),
            shiftType,
            loginDate: new Date().toLocaleDateString('en-GB'),
            loginTime: "06:00 AM",
            logoutDate: new Date().toLocaleDateString('en-GB'),
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
            source: "MANUAL_REGISTER"
        };

        FleetDataRepository.saveDutyLog(newLog);
        this.closeManualEntryModal();
        this.refreshAll();
        this.showToast(`Manual Shift Log saved for ${driverName} (${vehicleNo})!`, "success");
    }

    openReassignModal(vehicleId) {
        const vehicles = FleetDataRepository.getVehicles();
        const v = vehicles.find(item => item.id === vehicleId);
        if (!v) return;

        const modal = document.getElementById("reassign-hub-modal");
        if (!modal) return;

        document.getElementById("reassign-vehicle-title").innerText = `${v.regNo} (${v.model})`;
        document.getElementById("reassign-vehicle-id").value = v.id;

        const hubSelect = document.getElementById("reassign-target-hub");
        hubSelect.innerHTML = APP_CONFIG.hubs.map(h => `
            <option value="${h.id}" ${v.managerId === h.id ? 'selected' : ''}>${h.name} (${h.managerName})</option>
        `).join("");

        modal.classList.remove("hidden");
    }

    closeReassignModal() {
        const modal = document.getElementById("reassign-hub-modal");
        if (modal) modal.classList.add("hidden");
    }

    submitReassignment(e) {
        e.preventDefault();
        const vehicleId = document.getElementById("reassign-vehicle-id").value;
        const targetHubId = document.getElementById("reassign-target-hub").value;

        FleetDataRepository.reassignVehicle(vehicleId, targetHubId);
        this.closeReassignModal();
        this.refreshAll();
        this.showToast("Vehicle & Driver transferred to new Hub!", "success");
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
            l.source,
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
        this.showToast("CSV statement downloaded successfully!", "success");
    }

    printReceipt(logId) {
        const logs = FleetDataRepository.getDutyLogs();
        const log = logs.find(l => l.id === logId);
        if (!log) return;

        const printWin = window.open("", "_blank");
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
                    <h3>DRIVER DUTY SETTLEMENT RECEIPT</h3>
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
                    <p>Verified by SVM FLEET Hub Officer</p>
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
        document.querySelectorAll("[data-nav-tab]").forEach(btn => {
            const active = btn.getAttribute("data-nav-tab") === tabId;
            btn.className = active
                ? "flex items-center gap-2 px-4 py-2 rounded-xl font-bold text-xs bg-[#5B36F5] text-white shadow-lg shadow-purple-500/20"
                : "flex items-center gap-2 px-4 py-2 rounded-xl font-medium text-xs text-gray-400 hover:text-white hover:bg-gray-800/50";
        });

        document.querySelectorAll("[data-tab-content]").forEach(content => {
            content.classList.add("hidden");
        });
        const activeContent = document.querySelector(`[data-tab-content="${tabId}"]`);
        if (activeContent) {
            activeContent.classList.remove("hidden");
        }

        if (tabId === "map" || tabId === "overview") {
            setTimeout(() => {
                if (this.mapController && this.mapController.map) {
                    this.mapController.map.invalidateSize();
                }
            }, 100);
        }
    }

    showToast(message, type = "info") {
        const toast = document.createElement("div");
        const bgColor = type === "success" ? "bg-emerald-600" : type === "alert" ? "bg-red-600" : "bg-[#5B36F5]";
        toast.className = `fixed bottom-6 right-6 ${bgColor} text-white px-4 py-3 rounded-xl shadow-2xl z-[9999] text-xs font-bold flex items-center gap-2 animate-bounce`;
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
