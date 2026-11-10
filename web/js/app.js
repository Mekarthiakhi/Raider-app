/**
 * SVM FLEET Operating System (Client Demo Environment)
 * Main Application Controller & View Orchestrator
 * Executive Dark Glassmorphism Edition
 * Integrated with SvmWebAuth for strict Owner vs Hub Manager RBAC.
 */

class SvmFleetApp {
    constructor() {
        this.auth = typeof window !== "undefined" ? window.SVM_AUTH : null;
        const session = this.auth && this.auth.isAuthenticated() ? this.auth.getSession() : null;

        this.currentRole = session ? session.role : null;
        this.currentHubId = session && this.auth ? this.auth.getAllowedHubId(session.hubId) : null;
        this.activeTab = "overview";
        this.selectedTimeframe = "day"; // "day" | "week" | "month"
        this.selectedChartTimeframe = "daily"; // "daily" | "weekly" | "monthly"
        this.mapController = null;
        this.fullMapController = null;
        this.searchQuery = "";
        this.shiftFilter = "ALL";
        this.selectedDateRange = "all"; // "all" | "today" | "week" | "month" | "custom"
        this.specificDateFilter = ""; // YYYY-MM-DD
        this.reportPeriod = "week"; // "today" | "week" | "month" | "all"
        this.registerViewMode = (typeof window !== "undefined" && window.innerWidth < 768) ? "cards" : "table";
        this.notifications = [
            { id: 1, hubId: "mgr_moosapet_01", title: "Shift Completed", text: "Suman Goud (TS10 EF 7745) completed 15 trips (₹3,750 gross). 35% commission tier unlocked!", time: "2m ago", unread: true, type: "success" },
            { id: 2, hubId: "mgr_moosapet_01", title: "Cash Handover", text: "Ravi Kumar (TS09 UB 3368) logged cash handover of ₹2,000 at Moosapet depot.", time: "8m ago", unread: true, type: "info" },
            { id: 3, hubId: "mgr_kukatpally_02", title: "Target Met", text: "Kukatpally Cab TS09 UB 2611 (Arun Teja) crossed 40% shift target (16 trips, ₹3,820 gross).", time: "14m ago", unread: true, type: "warning" },
            { id: 4, hubId: "mgr_madhapur_03", title: "Shift Verified", text: "Harish Naidu (TS09 UB 4043) completed 17 trips (₹3,950 gross) at Madhapur Hitec City Hub.", time: "25m ago", unread: true, type: "info" }
        ];
        this.activeGuideTab = "guide-quickstart";
        this._toastTimeout = null;
    }

    init() {
        this.auth = typeof window !== "undefined" ? window.SVM_AUTH : this.auth;
        const session = this.auth && this.auth.isAuthenticated() ? this.auth.getSession() : null;

        if (session && this.auth) {
            this.currentRole = session.role;
            this.currentHubId = this.auth.getAllowedHubId(session.hubId);
            this.auth.applyPermissionsToDOM();
        } else {
            this.currentRole = null;
            this.currentHubId = null;
            if (this.auth) {
                this.auth.applyPermissionsToDOM();
            }
        }

        this.setupEventListeners();
        this.setupWheelLocks();
        this.populateLoginManagerHubs();
        this.syncHubSelectorOptions();
        this.renderRoleSelector();
        this.updateAuthUI();
        this.updateGreeting();
        this.setRegisterViewMode(this.registerViewMode);

        if (session && this.auth && this.auth.isAuthenticated()) {
            this.renderNotifications();

            if (typeof HyderabadMapController !== "undefined") {
                this.mapController = new HyderabadMapController("hyderabad-map");
                this.mapController.init();
            }

            this.refreshAll();
            this.renderReportsTab();
            this.renderTrendChart(this.selectedChartTimeframe || "daily");
            this.renderPlatformDonutChart();

            if (this.mapController && this.currentHubId && this.currentHubId !== "ALL") {
                this.mapController.focusHub(this.currentHubId);
            }
        } else {
            this._clearSensitiveOutputs();
        }

        window.addEventListener("resize", () => {
            if (window.innerWidth >= 1024) {
                this.closeMobileSidebar();
            }
            if (window.innerWidth < 768 && this.registerViewMode !== "cards") {
                this.setRegisterViewMode("cards");
            }
            if (this.mapController) this.mapController.invalidateSize();
            if (this.fullMapController) this.fullMapController.invalidateSize();
        });

        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    escapeHtml(value) {
        if (value === null || value === undefined) return "";
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    truncateName(value, maxLen = 16) {
        const str = value === null || value === undefined ? "" : String(value).trim();
        if (!str) return "";
        const limit = Math.max(1, Number(maxLen) || 16);
        return str.length > limit ? str.slice(0, limit).trimEnd() + "..." : str;
    }

    renderTruncatedNameHtml(value, maxLen = 16, extraClass = "", titleValue = null) {
        const displaySource = value === null || value === undefined ? "" : String(value).trim();
        const fullTitle = titleValue === null || titleValue === undefined ? displaySource : String(titleValue).trim();
        const cls = extraClass ? `truncate-name ${extraClass}` : "truncate-name";
        return `<span class="${this.escapeHtml(cls)}" title="${this.escapeHtml(fullTitle)}">${this.escapeHtml(this.truncateName(displaySource, maxLen))}</span>`;
    }

    _syncKpiGridColumns(gridEl, canSeeProfit, profitCardEl = null) {
        if (gridEl) {
            gridEl.classList.remove("grid-cols-1", "md:grid-cols-4", "md:grid-cols-5", "lg:grid-cols-5");
            gridEl.classList.add("grid-cols-2");
            gridEl.classList.toggle("sm:grid-cols-3", canSeeProfit);
            gridEl.classList.toggle("xl:grid-cols-5", canSeeProfit);
            gridEl.classList.toggle("sm:grid-cols-2", !canSeeProfit);
            gridEl.classList.toggle("lg:grid-cols-4", !canSeeProfit);
            gridEl.classList.toggle("xl:grid-cols-4", !canSeeProfit);
        }

        if (profitCardEl) {
            profitCardEl.classList.remove("md:col-span-1");
            profitCardEl.classList.add("col-span-2", "sm:col-span-1");
        }
    }

    formatCompactINR(val) {
        const n = Math.round(Number(val) || 0);
        if (n === 0) return "₹0";
        if (n >= 100000) {
            const lakhs = n / 100000;
            return "₹" + (Number.isInteger(lakhs) ? lakhs : lakhs.toFixed(1)) + "L";
        }
        if (n >= 1000) {
            return "₹" + Math.round(n / 1000) + "K";
        }
        return "₹" + n;
    }

    canViewNetProfit() {
        return Boolean(this.auth && typeof this.auth.canViewNetProfit === "function" && this.auth.canViewNetProfit());
    }

    getScopedHubId(requestedHubId = this.currentHubId) {
        if (!this.auth || !this.auth.isAuthenticated()) return null;
        return this.auth.getAllowedHubId(requestedHubId);
    }

    _clearSensitiveOutputs() {
        ["kpi-net-profit", "kpi-net-earnings", "report-kpi-profit", "report-kpi-margin"].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.innerText = "";
        });

        const profitCard = document.getElementById("kpi-card-net-profit");
        if (profitCard) {
            profitCard.classList.add("hidden");
            profitCard.setAttribute("aria-hidden", "true");
        }

        const reportProfitCard = document.getElementById("report-card-profit");
        if (reportProfitCard) {
            reportProfitCard.classList.add("hidden");
            reportProfitCard.setAttribute("aria-hidden", "true");
        }

        const trendLegend = document.getElementById("trend-legend-net-profit");
        if (trendLegend) {
            trendLegend.classList.add("hidden");
            trendLegend.setAttribute("aria-hidden", "true");
        }

        ["trend-chart-container", "overview-register-cards", "report-cards-container"].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.innerHTML = "";
        });
    }

    setupWheelLocks() {
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
        const topAddBtn = document.getElementById("btn-open-manual-entry");
        if (topAddBtn && !topAddBtn.hasAttribute("onclick")) {
            topAddBtn.addEventListener("click", () => this.openManualEntryModal());
        }

        document.querySelectorAll("[data-nav-tab]").forEach(btn => {
            if (!btn.hasAttribute("onclick")) {
                btn.addEventListener("click", (e) => {
                    const tab = e.currentTarget.getAttribute("data-nav-tab");
                    this.switchTab(tab);
                });
            }
        });

        const mobileMenuBtn = document.getElementById("mobile-menu-btn");
        if (mobileMenuBtn && !mobileMenuBtn.hasAttribute("onclick")) {
            mobileMenuBtn.addEventListener("click", () => this.toggleMobileSidebar());
        }

        const mobileCloseBtn = document.getElementById("mobile-sidebar-close-btn");
        if (mobileCloseBtn && !mobileCloseBtn.hasAttribute("onclick")) {
            mobileCloseBtn.addEventListener("click", () => this.closeMobileSidebar());
        }

        const mobileBackdrop = document.getElementById("mobile-sidebar-backdrop");
        if (mobileBackdrop && !mobileBackdrop.hasAttribute("onclick")) {
            mobileBackdrop.addEventListener("click", () => this.closeMobileSidebar());
        }

        const searchInput = document.getElementById("register-search");
        if (searchInput) {
            searchInput.addEventListener("input", (e) => {
                this.searchQuery = e.target.value.toLowerCase();
                this.renderDutyRegister();
            });
        }

        const globalSearch = document.getElementById("global-search-input");
        if (globalSearch) {
            globalSearch.addEventListener("input", (e) => {
                this.searchQuery = e.target.value.toLowerCase();
                const regSearch = document.getElementById("register-search");
                if (regSearch) regSearch.value = e.target.value;
                this.renderDutyRegister();
            });
        }

        window.addEventListener("keydown", (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "k") {
                e.preventDefault();
                const search = document.getElementById("global-search-input") || document.getElementById("register-search");
                if (search) search.focus();
            }
            if (e.key === "Escape") {
                this.closeAllModals();
                this.closeMobileSidebar();
            }
        });

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

        const editCalcInputs = [
            "edit-settle-trips", "edit-settle-gross", "edit-settle-cash", "edit-settle-fuel",
            "edit-settle-advance", "edit-settle-shifttype"
        ];
        editCalcInputs.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.addEventListener("input", () => this.updateEditModalCalculation());
                el.addEventListener("change", () => this.updateEditModalCalculation());
                el.addEventListener("wheel", (e) => e.preventDefault(), { passive: false });
            }
        });

        const modalIds = [
            "manual-entry-modal",
            "reassign-hub-modal",
            "add-driver-modal",
            "add-vehicle-modal",
            "add-manager-modal",
            "user-guide-modal",
            "edit-settlement-modal"
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

        document.addEventListener("click", (e) => {
            const notifBtn = document.getElementById("btn-notification-bell");
            const notifDropdown = document.getElementById("notification-dropdown");
            if (notifDropdown && notifBtn && !notifBtn.contains(e.target) && !notifDropdown.contains(e.target)) {
                notifDropdown.classList.remove("open");
            }

            const combobox = document.getElementById("driver-combobox-wrapper");
            const driverDropdown = document.getElementById("driver-search-dropdown");
            if (combobox && driverDropdown && !combobox.contains(e.target)) {
                this.closeDriverDropdown();
            }
        });
    }

    // ------------------------------------------------------------------------
    // Mobile Off-Canvas Sidebar Controls
    // ------------------------------------------------------------------------
    toggleMobileSidebar() {
        const sidebar = document.getElementById("app-sidebar");
        if (!sidebar) return;
        if (sidebar.classList.contains("mobile-open")) {
            this.closeMobileSidebar();
        } else {
            this.openMobileSidebar();
        }
    }

    openMobileSidebar() {
        const sidebar = document.getElementById("app-sidebar");
        const backdrop = document.getElementById("mobile-sidebar-backdrop");
        const menuBtn = document.getElementById("mobile-menu-btn");
        if (sidebar) sidebar.classList.add("mobile-open");
        if (backdrop) {
            backdrop.classList.add("open");
            backdrop.setAttribute("aria-hidden", "false");
        }
        if (menuBtn) menuBtn.setAttribute("aria-expanded", "true");
    }

    closeMobileSidebar() {
        const sidebar = document.getElementById("app-sidebar");
        const backdrop = document.getElementById("mobile-sidebar-backdrop");
        const menuBtn = document.getElementById("mobile-menu-btn");
        if (sidebar) sidebar.classList.remove("mobile-open");
        if (backdrop) {
            backdrop.classList.remove("open");
            backdrop.setAttribute("aria-hidden", "true");
        }
        if (menuBtn) menuBtn.setAttribute("aria-expanded", "false");
    }

    closeAllModals() {
        this.closeManualEntryModal();
        this.closeReassignModal();
        this.closeAddDriverModal();
        this.closeAddVehicleModal();
        this.closeAddManagerModal();
        this.closeUserGuideModal();
        this.closeEditSettlementModal();
    }

    // ------------------------------------------------------------------------
    // Authentication, Secret Key & Session Lifecycle
    // ------------------------------------------------------------------------
    populateLoginManagerHubs() {
        const select = document.getElementById("login-manager-hub");
        if (!select) return;

        const hubs = this.auth && typeof this.auth.getHubs === "function"
            ? this.auth.getHubs()
            : (window.DEMO_REPO ? window.DEMO_REPO.getHubs() : []);

        const prevVal = select.value;
        select.innerHTML = hubs.map(hub => `
            <option value="${this.escapeHtml(hub.id)}">${this.escapeHtml(hub.name)} (${this.escapeHtml(hub.managerName || "Manager")})</option>
        `).join("");

        if (prevVal && hubs.some(h => h.id === prevVal)) {
            select.value = prevVal;
        }
    }

    switchLoginRoleTab(role) {
        const target = String(role || "OWNER").toUpperCase() === "MANAGER" ? "MANAGER" : "OWNER";
        const ownerTab = document.getElementById("tab-login-owner");
        const managerTab = document.getElementById("tab-login-manager");
        const ownerPanel = document.getElementById("login-owner-panel");
        const managerPanel = document.getElementById("login-manager-panel");
        const errorBanner = document.getElementById("login-error-banner");

        if (ownerTab) {
            ownerTab.classList.toggle("active", target === "OWNER");
            ownerTab.setAttribute("aria-selected", String(target === "OWNER"));
        }
        if (managerTab) {
            managerTab.classList.toggle("active", target === "MANAGER");
            managerTab.setAttribute("aria-selected", String(target === "MANAGER"));
        }
        if (ownerPanel) {
            ownerPanel.classList.toggle("hidden", target !== "OWNER");
        }
        if (managerPanel) {
            managerPanel.classList.toggle("hidden", target !== "MANAGER");
        }
        if (errorBanner) {
            errorBanner.classList.add("hidden");
            const errorText = document.getElementById("login-error-text");
            if (errorText) errorText.textContent = "";
        }
    }

    _showLoginError(message) {
        const errorBanner = document.getElementById("login-error-banner");
        const errorText = document.getElementById("login-error-text");
        if (errorBanner) {
            if (errorText) {
                errorText.textContent = message;
            } else {
                errorBanner.textContent = message;
            }
            errorBanner.classList.remove("hidden");
        }
    }

    _clearLoginError() {
        const errorBanner = document.getElementById("login-error-banner");
        const errorText = document.getElementById("login-error-text");
        if (errorText) errorText.textContent = "";
        if (errorBanner) errorBanner.classList.add("hidden");
    }

    _completeLogin(res) {
        if (!res || !res.ok || !res.session) {
            const msg = (res && res.error) ? res.error : "Invalid credentials. Please verify and try again.";
            this._showLoginError(msg);
            return false;
        }

        this._clearLoginError();

        const session = res.session;
        this.currentRole = session.role;
        this.currentHubId = this.auth ? this.auth.getAllowedHubId(session.hubId) : session.hubId;

        if (this.auth) {
            this.auth.applyPermissionsToDOM();
        }

        this.populateLoginManagerHubs();
        this.syncHubSelectorOptions();
        this.renderRoleSelector();
        this.updateAuthUI();
        this.updateGreeting();
        this.renderNotifications();

        this.switchTab("overview");

        if (!this.mapController && typeof HyderabadMapController !== "undefined") {
            this.mapController = new HyderabadMapController("hyderabad-map");
            this.mapController.init();
        }

        this.refreshAll();
        this.renderReportsTab();
        this.renderTrendChart(this.selectedChartTimeframe || "daily");
        this.renderPlatformDonutChart();

        if (this.mapController) {
            this._refreshMapHubs(this.mapController);
            this.mapController.updateVehicles(this.getCurrentFilteredVehicles());
            this.mapController.focusHub(this.currentHubId);
            this.mapController.invalidateSize();
        }
        if (this.fullMapController) {
            this._refreshMapHubs(this.fullMapController);
            this.fullMapController.updateVehicles(this.getCurrentFilteredVehicles());
            this.fullMapController.focusHub(this.currentHubId);
            this.fullMapController.invalidateSize();
        }

        if (window.lucide) {
            window.lucide.createIcons();
        }

        this.showToast(`Signed in as ${session.userName}`, "success");
        return true;
    }

    handleOwnerLogin(e) {
        if (e && typeof e.preventDefault === "function") e.preventDefault();
        if (!this.auth) return false;

        const phoneEl = document.getElementById("login-owner-phone");
        const keyEl =
            document.getElementById("login-owner-secret") ||
            document.getElementById("login-owner-secret-key") ||
            document.getElementById("login-owner-key");

        const phone = phoneEl ? phoneEl.value.trim() : "";
        const secretKey = keyEl ? keyEl.value.trim() : "";

        const res = this.auth.loginAsOwner({ phone, secretKey, isDemo: false });
        if (res.ok && keyEl) {
            keyEl.value = "";
        }
        return this._completeLogin(res);
    }

    handleManagerLogin(e) {
        if (e && typeof e.preventDefault === "function") e.preventDefault();
        if (!this.auth) return false;

        const hubEl = document.getElementById("login-manager-hub");
        const phoneEl = document.getElementById("login-manager-phone");
        const pinEl = document.getElementById("login-manager-pin");

        const hubId = hubEl ? hubEl.value.trim() : "";
        const phone = phoneEl ? phoneEl.value.trim() : "";
        const pin = pinEl ? pinEl.value.trim() : "";

        const res = this.auth.loginAsManager({ hubId, phone, pin, isDemo: false });
        if (res.ok && pinEl) {
            pinEl.value = "";
        }
        return this._completeLogin(res);
    }

    handleDemoLogin(role = "OWNER") {
        if (!this.auth) return false;
        const normalized = String(role || "OWNER").toUpperCase();
        let res;

        if (normalized === "MANAGER") {
            const hubSelect = document.getElementById("login-manager-hub");
            const hubs = this.auth.getHubs();
            const hubId = (hubSelect && hubSelect.value) ? hubSelect.value : (hubs[0] ? hubs[0].id : "mgr_moosapet_01");
            res = this.auth.loginAsManager({ hubId, isDemo: true });
        } else {
            res = this.auth.loginAsOwner({ isDemo: true });
        }

        return this._completeLogin(res);
    }

    logout() {
        if (this.auth) {
            this.auth.logout();
        }

        this.currentRole = null;
        this.currentHubId = null;
        this.searchQuery = "";

        const regSearch = document.getElementById("register-search");
        const globalSearch = document.getElementById("global-search-input");
        if (regSearch) regSearch.value = "";
        if (globalSearch) globalSearch.value = "";

        const notifDropdown = document.getElementById("notification-dropdown");
        if (notifDropdown) notifDropdown.classList.remove("open");

        this.closeMobileSidebar();
        this.closeAllModals();
        this._clearSensitiveOutputs();
        this._clearLoginError();

        this.switchTab("overview");
        this.syncHubSelectorOptions();
        this.updateAuthUI();
        this.updateGreeting();

        if (window.lucide) {
            window.lucide.createIcons();
        }

        this.showToast("Signed out safely.", "info");
    }

    handleChangeSecretKey(e) {
        if (e && typeof e.preventDefault === "function") e.preventDefault();
        if (!this.auth || !this.auth.isOwner()) {
            this.showToast("Access denied. Only the Fleet Owner can change the Master Secret Key.", "alert");
            return false;
        }

        const currEl = document.getElementById("setting-current-secret-key");
        const newEl = document.getElementById("setting-new-secret-key");
        const confirmEl = document.getElementById("setting-confirm-secret-key");

        const curr = currEl ? currEl.value.trim() : "";
        const newKey = newEl ? newEl.value.trim() : "";
        const confirmKey = confirmEl ? confirmEl.value.trim() : "";

        if (!curr || !newKey || !confirmKey) {
            this.showToast("Please complete all Secret Key fields.", "alert");
            return false;
        }

        if (newKey !== confirmKey) {
            this.showToast("New Secret Key and confirmation do not match.", "alert");
            return false;
        }

        const res = this.auth.changeOwnerSecretKey(curr, newKey);
        if (!res || !res.ok) {
            this.showToast((res && res.error) ? res.error : "Failed to update Owner Secret Key.", "alert");
            return false;
        }

        if (currEl) currEl.value = "";
        if (newEl) newEl.value = "";
        if (confirmEl) confirmEl.value = "";

        this.updateAuthUI();
        this.showToast("Owner Master Secret Key updated successfully!", "success");
        return true;
    }

    updateAuthUI() {
        const session = this.auth && this.auth.isAuthenticated() ? this.auth.getSession() : null;
        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];

        const avatarEl = document.getElementById("header-user-avatar");
        const nameEl = document.getElementById("header-user-name");
        const roleEl = document.getElementById("header-user-role");
        const secretBadge = document.getElementById("setting-secret-key-badge");
        const addManagerBtn = document.getElementById("btn-add-manager");

        if (!session) {
            if (avatarEl) avatarEl.innerText = "SF";
            if (nameEl) {
                nameEl.innerText = "Locked Portal";
                nameEl.setAttribute("title", "Locked Portal");
            }
            if (roleEl) roleEl.innerText = "Sign In Required";
            if (secretBadge) secretBadge.innerText = "";
            if (addManagerBtn) addManagerBtn.classList.add("hidden");
            return;
        }

        if (session.role === "OWNER") {
            const ownerName = session.userName || "Akhilesh (Fleet Owner)";
            if (avatarEl) avatarEl.innerText = "AK";
            if (nameEl) {
                nameEl.innerText = this.truncateName(ownerName, 20);
                nameEl.setAttribute("title", ownerName);
            }
            if (roleEl) {
                const hub = hubs.find(h => h.id === this.currentHubId);
                roleEl.innerText = hub ? `Viewing: ${hub.shortName}` : "Master Admin • All Hubs";
            }
            if (secretBadge && this.auth) {
                const isDefault = this.auth.getOwnerSecretKey() === this.auth.DEFAULT_OWNER_SECRET_KEY;
                secretBadge.innerText = isDefault ? "Default Key Active" : "Custom Key Active ✓";
            }
            if (addManagerBtn) {
                addManagerBtn.classList.remove("hidden");
            }
        } else {
            const hub = hubs.find(h => h.id === session.hubId);
            const mgrName = hub && hub.managerName ? hub.managerName : "Hub Manager";
            const initials = mgrName.split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2) || "HM";
            if (avatarEl) avatarEl.innerText = initials;
            if (nameEl) {
                nameEl.innerText = this.truncateName(mgrName, 20);
                nameEl.setAttribute("title", mgrName);
            }
            if (roleEl) roleEl.innerText = `${hub ? hub.shortName : "Station"} Hub Manager`;
            if (secretBadge) secretBadge.innerText = "";
            if (addManagerBtn) {
                addManagerBtn.classList.add("hidden");
            }
        }
    }

    resetDemoState() {
        if (!this.auth || !this.auth.isAuthenticated()) {
            this.showToast("Please sign in to reset demo data.", "alert");
            return;
        }
        if (!this.auth.isOwner()) {
            this.showToast("Only the Fleet Owner can reset demo fleet data.", "alert");
            return;
        }

        if (window.DEMO_REPO && typeof window.DEMO_REPO.resetDemoData === "function") {
            window.DEMO_REPO.resetDemoData();
        }

        this.currentHubId = this.auth.getAllowedHubId("ALL");
        this.searchQuery = "";
        this.shiftFilter = "ALL";
        this.selectedDateRange = "all";
        this.specificDateFilter = "";

        const regSearch = document.getElementById("register-search");
        const globalSearch = document.getElementById("global-search-input");
        if (regSearch) regSearch.value = "";
        if (globalSearch) globalSearch.value = "";

        this.populateLoginManagerHubs();
        this.syncHubSelectorOptions();
        this.renderRoleSelector();
        this.updateAuthUI();
        this.updateGreeting();
        this.renderNotifications();

        if (this.mapController) {
            this._refreshMapHubs(this.mapController);
            this.mapController.updateVehicles(this.getCurrentFilteredVehicles());
            this.mapController.focusHub(this.currentHubId);
            this.mapController.invalidateSize();
        }
        if (this.fullMapController) {
            this._refreshMapHubs(this.fullMapController);
            this.fullMapController.updateVehicles(this.getCurrentFilteredVehicles());
            this.fullMapController.focusHub(this.currentHubId);
            this.fullMapController.invalidateSize();
        }

        this.refreshAll();
        this.renderReportsTab();
        this.renderTrendChart(this.selectedChartTimeframe || "daily");
        this.renderPlatformDonutChart();

        this.showToast("Demo fleet state restored to 30 cabs & 3 default hubs.", "success");
    }

    // ------------------------------------------------------------------------
    // Hub Selection, Scoping & Greetings
    // ------------------------------------------------------------------------
    syncHubSelectorOptions() {
        const selector = document.getElementById("top-branch-selector");
        if (!selector) return;

        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        const isAuthenticated = Boolean(this.auth && this.auth.isAuthenticated());
        const canSwitch = Boolean(this.auth && this.auth.canSwitchHubs());
        const allowedHubId = this.getScopedHubId(this.currentHubId);

        if (!isAuthenticated) {
            selector.innerHTML = `<option value="ALL">Hyderabad (All Hubs)</option>`;
            selector.value = "ALL";
            selector.disabled = true;
            return;
        }

        if (!canSwitch) {
            const hub = hubs.find(h => h.id === allowedHubId);
            const label = hub ? `${hub.name}` : (allowedHubId || "Assigned Hub");
            selector.innerHTML = `<option value="${this.escapeHtml(allowedHubId)}" selected>${this.escapeHtml(label)}</option>`;
            selector.value = allowedHubId;
            selector.disabled = true;
            return;
        }

        let optionsHtml = `<option value="ALL">Hyderabad (All Hubs)</option>`;
        hubs.forEach(hub => {
            optionsHtml += `<option value="${this.escapeHtml(hub.id)}">${this.escapeHtml(hub.name)}</option>`;
        });
        selector.innerHTML = optionsHtml;

        const targetVal = (allowedHubId === "ALL" || hubs.some(h => h.id === allowedHubId)) ? allowedHubId : "ALL";
        this.currentHubId = targetVal;
        selector.value = targetVal;
        selector.disabled = false;
    }

    populateHubSelectElement(selectEl, selectedHubId = "", lockToManager = true) {
        if (!selectEl) return;
        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        const isManager = Boolean(this.auth && this.auth.isManager());
        const allowedHubId = this.getScopedHubId(selectedHubId);

        if (isManager && lockToManager) {
            const hub = hubs.find(h => h.id === allowedHubId);
            const label = hub ? `${hub.name} (${hub.managerName || "Manager"})` : (allowedHubId || "Assigned Hub");
            selectEl.innerHTML = `<option value="${this.escapeHtml(allowedHubId)}" selected>${this.escapeHtml(label)}</option>`;
            selectEl.value = allowedHubId;
            selectEl.disabled = true;
            selectEl.classList.add("opacity-75", "cursor-not-allowed");
            return;
        }

        selectEl.innerHTML = hubs.map(h => `
            <option value="${this.escapeHtml(h.id)}">${this.escapeHtml(h.name)}</option>
        `).join("");
        selectEl.disabled = false;
        selectEl.classList.remove("opacity-75", "cursor-not-allowed");

        const fallbackId = hubs[0] ? hubs[0].id : "";
        const target = (selectedHubId && selectedHubId !== "ALL" && hubs.some(h => h.id === selectedHubId))
            ? selectedHubId
            : fallbackId;
        if (target) selectEl.value = target;
    }

    renderGreeting() {
        return this.updateGreeting();
    }

    updateGreeting() {
        const hour = new Date().getHours();
        let timeGreeting = "Good Evening";
        if (hour < 12) timeGreeting = "Good Morning";
        else if (hour < 17) timeGreeting = "Good Afternoon";

        const titleEl = document.getElementById("greeting-title");
        const subtitleEl = document.getElementById("greeting-subtitle");
        const session = this.auth && this.auth.isAuthenticated() ? this.auth.getSession() : null;
        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];

        if (!session) {
            if (titleEl) {
                titleEl.innerHTML = `<span>${timeGreeting}, SVM Fleet Portal</span> <span class="text-2xl">🚕</span>`;
            }
            if (subtitleEl) {
                subtitleEl.innerText = "Sign in to access live fleet operations.";
            }
            return;
        }

        const scopedHubId = this.getScopedHubId(this.currentHubId);
        const hub = hubs.find(h => h.id === scopedHubId);

        if (session.role === "MANAGER") {
            const fullManagerLabel = hub
                ? `${hub.managerName} (${hub.shortName} Manager)`
                : (session.userName || "Hub Manager");
            const rawMgrName = (hub && hub.managerName) ? hub.managerName : (session.userName || "Hub Manager");
            const cleanMgrName = rawMgrName.split("(")[0].trim() || rawMgrName;
            if (titleEl) {
                titleEl.innerHTML = `<span>${this.escapeHtml(timeGreeting)}, ${this.renderTruncatedNameHtml(cleanMgrName, 18, "", fullManagerLabel)}</span> <span class="text-2xl">👋</span>`;
            }
            if (subtitleEl) {
                subtitleEl.innerText = hub
                    ? `Here's your fleet performance for ${hub.name}.`
                    : `Here's your assigned hub performance for today.`;
            }
            return;
        }

        const rawOwner = session.userName || "Akhilesh (Fleet Owner)";
        const cleanOwner = rawOwner.split("(")[0].trim() || rawOwner;
        if (titleEl) {
            titleEl.innerHTML = `<span>${this.escapeHtml(timeGreeting)}, ${this.renderTruncatedNameHtml(cleanOwner, 18, "", rawOwner)}</span> <span class="text-2xl">👋</span>`;
        }
        if (subtitleEl) {
            subtitleEl.innerText = hub
                ? `Here's your fleet performance for ${hub.name}.`
                : `Here's your fleet performance across all Hyderabad hubs today.`;
        }
    }

    renderRoleSelector() {
        const selector = document.getElementById("role-selector");
        if (!selector) return;

        if (!this.auth || !this.auth.isAuthenticated() || !this.auth.canSwitchHubs()) {
            selector.innerHTML = "";
            return;
        }

        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        let options = `
            <option value="OWNER:ALL" ${this.currentHubId === "ALL" ? "selected" : ""}>
                👑 Fleet Owner (Master - All Hubs)
            </option>
        `;

        hubs.forEach(hub => {
            const isSelected = this.currentHubId === hub.id;
            options += `
                <option value="OWNER:${this.escapeHtml(hub.id)}" ${isSelected ? "selected" : ""}>
                    📍 Filter Hub: ${this.escapeHtml(hub.shortName)} (${this.escapeHtml(hub.managerName)})
                </option>
            `;
        });

        selector.innerHTML = options;
        selector.onchange = (e) => {
            const parts = String(e.target.value || "").split(":");
            this.setSession(parts[0], parts[1] || "ALL");
        };
    }

    setSession(role, hubId) {
        if (!this.auth || !this.auth.isAuthenticated()) {
            this.showToast("Authentication required.", "alert");
            return;
        }

        const session = this.auth.getSession();
        const allowedHubId = this.auth.getAllowedHubId(hubId);

        if (!this.auth.canSwitchHubs()) {
            this.currentRole = session.role;
            this.currentHubId = allowedHubId;
            this.syncHubSelectorOptions();
            this.showToast("Access denied: Hub Managers cannot switch roles or hubs.", "alert");
            return;
        }

        this.currentRole = session.role;
        this.currentHubId = allowedHubId || "ALL";
        try {
            localStorage.setItem("SVM_HUB_ID", this.currentHubId);
        } catch (err) {
            console.warn("Could not persist hub filter:", err);
        }

        this.syncHubSelectorOptions();
        this.updateAuthUI();
        this.updateGreeting();
        this.renderNotifications();
        this.refreshAll();
        this.renderReportsTab();
        this.renderTrendChart(this.selectedChartTimeframe || "daily");
        this.renderPlatformDonutChart();

        if (this.mapController) {
            this.mapController.focusHub(this.currentHubId);
        }
        if (this.fullMapController) {
            this.fullMapController.updateVehicles(this.getCurrentFilteredVehicles());
            this.fullMapController.focusHub(this.currentHubId);
        }

        this.showToast(`Switched view to ${this.currentHubId === "ALL" ? "All Hyderabad Hubs" : this.getActiveHubName() + " Hub"}`, "info");
    }

    filterHub(hubId) {
        if (!this.auth || !this.auth.isAuthenticated()) {
            this.showToast("Authentication required.", "alert");
            return;
        }

        const allowedHubId = this.auth.getAllowedHubId(hubId);
        if (!this.auth.canSwitchHubs() && hubId !== allowedHubId) {
            this.currentHubId = allowedHubId;
            this.syncHubSelectorOptions();
            this.showToast("Access locked to your assigned hub.", "alert");
            return;
        }

        this.currentHubId = allowedHubId || "ALL";
        if (this.auth.isOwner()) {
            try {
                localStorage.setItem("SVM_HUB_ID", this.currentHubId);
            } catch (err) {
                console.warn("Could not persist hub filter:", err);
            }
        }

        const topSelect = document.getElementById("top-branch-selector");
        if (topSelect) topSelect.value = this.currentHubId;

        this.updateAuthUI();
        this.updateGreeting();
        this.renderNotifications();
        this.refreshAll();
        this.renderReportsTab();
        this.renderTrendChart(this.selectedChartTimeframe || "daily");
        this.renderPlatformDonutChart();

        if (this.mapController) {
            this.mapController.focusHub(this.currentHubId);
        }
        if (this.fullMapController) {
            this.fullMapController.updateVehicles(this.getCurrentFilteredVehicles());
            this.fullMapController.focusHub(this.currentHubId);
        }
    }

    setTimeframe(tf) {
        this.selectedTimeframe = tf;

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
            btn.className = "shift-filter-btn px-3 py-1.5 rounded-full text-xs font-medium border border-[#263149] bg-[#080D1B] text-slate-400 hover:text-white shrink-0 whitespace-nowrap";
        });
        const activeMap = {
            "ALL": "filter-shift-all",
            "12_HOURS": "filter-shift-12",
            "24_HOURS": "filter-shift-24",
            "PENDING": "filter-shift-pending",
            "SETTLED": "filter-shift-settled"
        };
        const activeBtn = document.getElementById(activeMap[filter]);
        if (activeBtn) {
            activeBtn.className = "shift-filter-btn px-3 py-1.5 rounded-full text-xs font-bold border transition-all bg-[#6655F5] text-white border-[#6655F5] shrink-0 whitespace-nowrap";
        }
        this.renderDutyRegister();
    }

    formatInputDateToLog(dateStr) {
        if (!dateStr) return "";
        const parts = dateStr.split("-");
        if (parts.length !== 3) return dateStr;
        const [year, month, day] = parts;
        const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        const mIdx = parseInt(month, 10) - 1;
        return `${day.padStart(2, "0")} ${months[mIdx]} ${year}`;
    }

    setDateRangeFilter(range) {
        this.selectedDateRange = range;
        this.specificDateFilter = "";
        const picker = document.getElementById("register-date-picker");
        if (picker) picker.value = "";

        ["all", "today", "week", "month"].forEach(r => {
            const btn = document.getElementById(`btn-daterange-${r}`);
            if (btn) {
                if (r === range) {
                    btn.className = "date-range-btn px-3 py-1 rounded-lg text-xs font-bold bg-[#6655F5] text-white transition-all shrink-0 whitespace-nowrap";
                } else {
                    btn.className = "date-range-btn px-3 py-1 rounded-lg text-xs font-semibold text-[#94A3B8] hover:text-white transition-all shrink-0 whitespace-nowrap";
                }
            }
        });

        this.renderDutyRegister();
    }

    setSpecificDateFilter(dateStr) {
        if (!dateStr) {
            this.clearDateFilter();
            return;
        }
        this.specificDateFilter = dateStr;
        this.selectedDateRange = "custom";

        ["all", "today", "week", "month"].forEach(r => {
            const btn = document.getElementById(`btn-daterange-${r}`);
            if (btn) {
                btn.className = "date-range-btn px-3 py-1 rounded-lg text-xs font-semibold text-[#94A3B8] hover:text-white transition-all shrink-0 whitespace-nowrap";
            }
        });

        this.renderDutyRegister();
    }

    clearDateFilter() {
        this.specificDateFilter = "";
        this.setDateRangeFilter("all");
    }

    setRegisterViewMode(mode) {
        this.registerViewMode = mode === "cards" ? "cards" : "table";
        const cardsContainer = document.getElementById("register-cards-container");
        const tableContainer = document.getElementById("register-table-container");
        const cardsBtn = document.getElementById("view-mode-cards-btn");
        const tableBtn = document.getElementById("view-mode-table-btn");

        if (this.registerViewMode === "cards") {
            if (cardsContainer) cardsContainer.classList.remove("hidden");
            if (tableContainer) tableContainer.classList.add("hidden");
            if (cardsBtn) cardsBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white flex items-center gap-1 cursor-pointer shrink-0 whitespace-nowrap";
            if (tableBtn) tableBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-medium text-slate-400 hover:text-white flex items-center gap-1 cursor-pointer shrink-0 whitespace-nowrap";
        } else {
            if (cardsContainer) cardsContainer.classList.add("hidden");
            if (tableContainer) tableContainer.classList.remove("hidden");
            if (cardsBtn) cardsBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-medium text-slate-400 hover:text-white flex items-center gap-1 cursor-pointer shrink-0 whitespace-nowrap";
            if (tableBtn) tableBtn.className = "px-2.5 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white flex items-center gap-1 cursor-pointer shrink-0 whitespace-nowrap";
        }
        if (window.lucide) window.lucide.createIcons();
    }

    getHubLabel(hubId) {
        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        const hub = hubs.find(h => h.id === hubId);
        return hub ? hub.shortName : (hubId || "Fleet");
    }

    getActiveHubName() {
        const allowedHubId = this.getScopedHubId(this.currentHubId);
        if (!allowedHubId || allowedHubId === "ALL") return "All Hyderabad";
        return this.getHubLabel(allowedHubId);
    }

    getCurrentFilteredVehicles() {
        const allowedHubId = this.getScopedHubId(this.currentHubId);
        if (!allowedHubId || !window.DEMO_REPO) return [];
        return window.DEMO_REPO.getVehicles(allowedHubId);
    }

    getCurrentFilteredDrivers() {
        const allowedHubId = this.getScopedHubId(this.currentHubId);
        if (!allowedHubId || !window.DEMO_REPO) return [];
        return window.DEMO_REPO.getDrivers(allowedHubId);
    }

    getCurrentFilteredLogs() {
        const allowedHubId = this.getScopedHubId(this.currentHubId);
        if (!allowedHubId || !window.DEMO_REPO) return [];
        let logs = window.DEMO_REPO.getDutyLogs(allowedHubId);

        if (this.shiftFilter === "12_HOURS" || this.shiftFilter === "24_HOURS") {
            logs = logs.filter(l => l.shiftType === this.shiftFilter);
        } else if (this.shiftFilter === "PENDING" || this.shiftFilter === "OPEN") {
            logs = logs.filter(l => l.status === "PENDING" || l.status === "OPEN");
        } else if (this.shiftFilter === "SETTLED") {
            logs = logs.filter(l => l.status === "SETTLED");
        }

        if (this.specificDateFilter) {
            const formattedTarget = this.formatInputDateToLog(this.specificDateFilter);
            logs = logs.filter(l => l.loginDate === formattedTarget || (l.loginDate && l.loginDate.includes(this.specificDateFilter)));
        } else if (this.selectedDateRange === "today") {
            const todayStr = new Date().toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
            logs = logs.filter(l => (l.loginDate && l.loginDate.includes("09 Nov 2026")) || l.loginDate === todayStr);
        } else if (this.selectedDateRange === "week") {
            const weekDates = ["03 Nov", "04 Nov", "05 Nov", "06 Nov", "07 Nov", "08 Nov", "09 Nov"];
            logs = logs.filter(l => l.loginDate && weekDates.some(d => l.loginDate.includes(d)));
        } else if (this.selectedDateRange === "month") {
            logs = logs.filter(l => l.loginDate && l.loginDate.includes("Nov 2026"));
        }

        if (this.searchQuery) {
            logs = logs.filter(l =>
                (l.driverName && l.driverName.toLowerCase().includes(this.searchQuery)) ||
                (l.driverPhone && l.driverPhone.toLowerCase().includes(this.searchQuery)) ||
                (l.vehicleNo && l.vehicleNo.toLowerCase().includes(this.searchQuery)) ||
                (l.remarks && l.remarks.toLowerCase().includes(this.searchQuery)) ||
                (l.source && l.source.toLowerCase().includes(this.searchQuery))
            );
        }
        return logs;
    }

    _refreshMapHubs(mapCtrl) {
        if (!mapCtrl || !mapCtrl.map) return;
        if (Array.isArray(mapCtrl.hubMarkers)) {
            mapCtrl.hubMarkers.forEach(layer => {
                try {
                    mapCtrl.map.removeLayer(layer);
                } catch (err) {
                    console.warn("Map layer removal warning:", err);
                }
            });
            mapCtrl.hubMarkers = [];
        }
        if (typeof mapCtrl.renderHubs === "function") {
            mapCtrl.renderHubs();
        }
    }

    updateMapLegends(vehicles) {
        const list = Array.isArray(vehicles) ? vehicles : [];
        const activeCount = list.filter(v => String(v.status).toUpperCase() === "ACTIVE").length;
        const idleCount = list.filter(v => String(v.status).toUpperCase() === "IDLE").length;
        const offlineCount = list.filter(v => String(v.status).toUpperCase() === "OFFLINE").length;

        const overviewLegends = document.getElementById("overview-map-legends");
        if (overviewLegends) {
            const spans = Array.from(overviewLegends.children).filter(el => el.tagName === "SPAN");
            if (spans[0]) spans[0].innerHTML = `<span class="w-1.5 h-1.5 rounded-full bg-[#10B981]"></span> Active (${activeCount})`;
            if (spans[1]) spans[1].innerHTML = `<span class="w-1.5 h-1.5 rounded-full bg-[#F59E0B]"></span> Idle (${idleCount})`;
            if (spans[2]) spans[2].innerHTML = `<span class="w-1.5 h-1.5 rounded-full bg-[#EF4444]"></span> Offline (${offlineCount})`;
        }

        const fullLegends = document.getElementById("map-legend-counts");
        if (fullLegends) {
            const spans = Array.from(fullLegends.children).filter(el => el.tagName === "SPAN");
            if (spans[0]) spans[0].innerHTML = `<span class="w-3 h-3 rounded-full bg-[#10B981]"></span> Active (${activeCount})`;
            if (spans[1]) spans[1].innerHTML = `<span class="w-3 h-3 rounded-full bg-[#F59E0B]"></span> Idle (${idleCount})`;
            if (spans[2]) spans[2].innerHTML = `<span class="w-3 h-3 rounded-full bg-[#EF4444]"></span> Offline (${offlineCount})`;
        }
    }

    refreshAll() {
        if (!this.auth || !this.auth.isAuthenticated()) {
            this._clearSensitiveOutputs();
            return;
        }

        this.renderMetricCards();
        this.renderDutyRegister();
        this.renderDriversGrid();
        this.renderVehiclesGrid();
        this.renderPendingTelemetryList();

        const vehicles = this.getCurrentFilteredVehicles();
        this.updateMapLegends(vehicles);

        if (this.mapController) {
            this.mapController.updateVehicles(vehicles);
        }
        if (this.fullMapController) {
            this.fullMapController.updateVehicles(vehicles);
        }

        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    // ------------------------------------------------------------------------
    // KPI Metrics, Trend Chart & Platform Donut (Strict Profit RBAC)
    // ------------------------------------------------------------------------
    renderMetricCards() {
        const allowedHubId = this.getScopedHubId();
        const canSeeProfit = this.canViewNetProfit();

        const cabsEl = document.getElementById("kpi-total-cabs");
        const driversEl = document.getElementById("kpi-active-drivers");
        const tripsEl = document.getElementById("kpi-total-trips");
        const grossEl = document.getElementById("kpi-gross-earnings");
        const netEl = document.getElementById("kpi-net-profit") || document.getElementById("kpi-net-earnings");
        const profitCardEl = document.getElementById("kpi-card-net-profit") || (netEl ? netEl.closest(".kpi-card-emerald") : null);
        const kpiGridEl = document.getElementById("dashboard-kpi-grid") || (cabsEl ? cabsEl.closest(".grid") : null);

        this._syncKpiGridColumns(kpiGridEl, canSeeProfit, profitCardEl);

        if (profitCardEl) {
            profitCardEl.classList.toggle("hidden", !canSeeProfit);
            profitCardEl.setAttribute("aria-hidden", String(!canSeeProfit));
        }

        if (!allowedHubId) {
            if (cabsEl) cabsEl.innerText = "0";
            if (driversEl) driversEl.innerText = "0";
            if (tripsEl) tripsEl.innerText = "0";
            if (grossEl) grossEl.innerText = "₹0";
            if (netEl) netEl.innerText = "";
            return;
        }

        const vehicles = this.getCurrentFilteredVehicles();
        const drivers = this.getCurrentFilteredDrivers();
        const hubLogs = window.DEMO_REPO ? window.DEMO_REPO.getDutyLogs(allowedHubId) : [];

        const totalVehicles = vehicles.length;
        const activeDrivers = drivers.filter(d => d.status === "ON_DUTY").length;

        const vehTripsToday = vehicles.reduce((s, v) => s + (Number(v.tripsToday) || 0), 0);
        const vehGrossToday = vehicles.reduce((s, v) => s + (Number(v.todayEarnings) || 0), 0);

        const todayLabel = new Date().toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
        const todayLogs = hubLogs.filter(l => (l.loginDate && l.loginDate.includes("09 Nov 2026")) || l.loginDate === todayLabel);
        const addedTodayLogs = todayLogs.filter(l => !/^log_0\d\d_/.test(String(l.id)));

        const extraTripsToday = addedTodayLogs.reduce((s, l) => s + (Number(l.trips) || 0), 0);
        const extraGrossToday = addedTodayLogs.reduce((s, l) => s + (Number(l.earnings) || 0), 0);
        const allTodayLogsTrips = todayLogs.reduce((s, l) => s + (Number(l.trips) || 0), 0);
        const allTodayLogsGross = todayLogs.reduce((s, l) => s + (Number(l.earnings) || 0), 0);

        const dailyTrips = Math.max(vehTripsToday + extraTripsToday, allTodayLogsTrips);
        const dailyGross = Math.max(vehGrossToday + extraGrossToday, allTodayLogsGross);

        let displayTrips = dailyTrips;
        let displayGrossEarnings = dailyGross;

        if (this.selectedTimeframe === "week") {
            displayTrips = dailyTrips * 7;
            displayGrossEarnings = dailyGross * 7;
        } else if (this.selectedTimeframe === "month") {
            displayTrips = dailyTrips * 30;
            displayGrossEarnings = dailyGross * 30;
        }

        if (cabsEl) cabsEl.innerText = String(totalVehicles);
        if (driversEl) driversEl.innerText = String(activeDrivers);
        if (tripsEl) tripsEl.innerText = Math.round(displayTrips).toLocaleString("en-IN");
        if (grossEl) grossEl.innerText = "₹" + Math.round(displayGrossEarnings).toLocaleString("en-IN");

        if (!canSeeProfit) {
            if (netEl) netEl.innerText = "";
        } else {
            const displayNetProfit = Math.round(displayGrossEarnings * 0.3077);
            if (netEl) netEl.innerText = "₹" + displayNetProfit.toLocaleString("en-IN");
        }
    }

    renderTrendChart(timeframe = "daily") {
        this.selectedChartTimeframe = timeframe;
        const container = document.getElementById("trend-chart-container");
        if (!container) return;

        const allowedHubId = this.getScopedHubId();
        const canSeeProfit = this.canViewNetProfit();

        const titleEl = document.getElementById("trend-chart-title");
        const subtitleEl = document.getElementById("trend-chart-subtitle");
        const profitLegend = document.getElementById("trend-legend-net-profit");

        if (titleEl) {
            titleEl.innerText = canSeeProfit ? "Earnings & Profitability Trend" : "Earnings Performance Trend";
        }
        if (subtitleEl) {
            subtitleEl.innerText = canSeeProfit
                ? "Gross earnings, expenses and net profit"
                : "Gross fleet revenue trend";
        }
        if (profitLegend) {
            profitLegend.classList.toggle("hidden", !canSeeProfit);
            profitLegend.setAttribute("aria-hidden", String(!canSeeProfit));
        }

        const dailyBtn = document.getElementById("chart-tf-daily");
        const weeklyBtn = document.getElementById("chart-tf-weekly");
        const monthlyBtn = document.getElementById("chart-tf-monthly");

        [dailyBtn, weeklyBtn, monthlyBtn].forEach(b => {
            if (b) b.className = "px-2.5 py-1 rounded-md text-[11px] font-medium text-[#94A3B8] hover:text-white transition-all whitespace-nowrap";
        });
        if (timeframe === "daily" && dailyBtn) dailyBtn.className = "px-2.5 py-1 rounded-md text-[11px] font-bold bg-[#6655F5] text-white shadow-sm whitespace-nowrap";
        if (timeframe === "weekly" && weeklyBtn) weeklyBtn.className = "px-2.5 py-1 rounded-md text-[11px] font-bold bg-[#6655F5] text-white shadow-sm whitespace-nowrap";
        if (timeframe === "monthly" && monthlyBtn) monthlyBtn.className = "px-2.5 py-1 rounded-md text-[11px] font-bold bg-[#6655F5] text-white shadow-sm whitespace-nowrap";

        if (!allowedHubId) {
            container.innerHTML = "";
            return;
        }

        const trendData = window.DEMO_EARNINGS_TREND ? window.DEMO_EARNINGS_TREND[timeframe] : null;
        if (!trendData) return;

        const allVehicles = window.DEMO_REPO ? window.DEMO_REPO.getVehicles("ALL") : [];
        const scopedVehicles = this.getCurrentFilteredVehicles();
        const scaleFactor = (allowedHubId !== "ALL")
            ? (allVehicles.length > 0 ? scopedVehicles.length / allVehicles.length : 0)
            : 1;

        const labels = trendData.labels;
        const grossEarnings = trendData.grossEarnings.map(v => Math.round(v * scaleFactor));
        const netProfit = canSeeProfit ? trendData.netProfit.map(v => Math.round(v * scaleFactor)) : [];
        const n = labels.length;

        const svgW = 600;
        const svgH = 220;
        const padLeft = 45;
        const padRight = 20;
        const padTop = 15;
        const padBottom = 30;

        let maxVal;
        let yTicks;

        if (scaleFactor === 1) {
            maxVal = timeframe === "daily" ? 150000 : timeframe === "weekly" ? 800000 : 3000000;
            yTicks = timeframe === "daily"
                ? [{ v: 150000, l: "₹1.5L" }, { v: 120000, l: "₹1.2L" }, { v: 90000, l: "₹90K" }, { v: 60000, l: "₹60K" }, { v: 30000, l: "₹30K" }, { v: 0, l: "₹0" }]
                : timeframe === "weekly"
                ? [{ v: 800000, l: "₹8.0L" }, { v: 600000, l: "₹6.0L" }, { v: 400000, l: "₹4.0L" }, { v: 200000, l: "₹2.0L" }, { v: 0, l: "₹0" }]
                : [{ v: 3000000, l: "₹30L" }, { v: 2400000, l: "₹24L" }, { v: 1800000, l: "₹18L" }, { v: 1200000, l: "₹12L" }, { v: 600000, l: "₹6L" }, { v: 0, l: "₹0" }];
        } else {
            const peak = Math.max(...grossEarnings, 0);
            const rawMax = peak > 0 ? peak * 1.2 : 10000;
            const roughStep = rawMax / 5;
            const magnitude = Math.pow(10, Math.floor(Math.log10(Math.max(roughStep, 1))));
            const step = Math.ceil(roughStep / magnitude) * magnitude;
            maxVal = Math.max(step * 5, 5000);
            yTicks = [5, 4, 3, 2, 1, 0].map(mult => {
                const v = mult * step;
                return { v, l: this.formatCompactINR(v) };
            });
        }

        const chartW = svgW - padLeft - padRight;
        const chartH = svgH - padTop - padBottom;

        const getY = (val) => padTop + chartH - (Math.min(val, maxVal) / maxVal) * chartH;
        const getX = (i) => n <= 1 ? padLeft + chartW / 2 : padLeft + (i / (n - 1)) * chartW;

        let gridLinesHtml = "";
        yTicks.forEach(tick => {
            const y = getY(tick.v);
            gridLinesHtml += `
                <line x1="${padLeft}" y1="${y}" x2="${svgW - padRight}" y2="${y}" stroke="#1E273D" stroke-width="1" stroke-dasharray="3 4" />
                <text x="${padLeft - 8}" y="${y + 3}" fill="#64748B" font-size="9" font-family="monospace" text-anchor="end">${tick.l}</text>
            `;
        });

        let barsHtml = "";
        if (canSeeProfit) {
            const barWidth = Math.min(24, Math.floor(chartW / (n * 2.2)));
            for (let i = 0; i < n; i++) {
                const x = getX(i) - barWidth / 2;
                const barH = (netProfit[i] / maxVal) * chartH;
                const y = padTop + chartH - barH;
                barsHtml += `
                    <rect x="${x}" y="${y}" width="${barWidth}" height="${barH}" rx="4" fill="#10B981" fill-opacity="0.85">
                        <title>${this.escapeHtml(labels[i])} Net Profit: ₹${netProfit[i].toLocaleString("en-IN")}</title>
                    </rect>
                `;
            }
        }

        const linePoints = [];
        for (let i = 0; i < n; i++) {
            linePoints.push({ x: getX(i), y: getY(grossEarnings[i]) });
        }

        let pathD = `M ${linePoints[0].x} ${linePoints[0].y}`;
        for (let i = 0; i < linePoints.length - 1; i++) {
            const curr = linePoints[i];
            const next = linePoints[i + 1];
            const midX = (curr.x + next.x) / 2;
            pathD += ` C ${midX} ${curr.y}, ${midX} ${next.y}, ${next.x} ${next.y}`;
        }

        const bottomY = padTop + chartH;
        const areaD = `${pathD} L ${linePoints[linePoints.length - 1].x} ${bottomY} L ${linePoints[0].x} ${bottomY} Z`;

        let dotsHtml = "";
        let xLabelsHtml = "";
        for (let i = 0; i < n; i++) {
            const pt = linePoints[i];
            dotsHtml += `
                <circle cx="${pt.x}" cy="${pt.y}" r="4.5" fill="#6655F5" stroke="#FFFFFF" stroke-width="1.8" class="cursor-pointer">
                    <title>${this.escapeHtml(labels[i])} Gross: ₹${grossEarnings[i].toLocaleString("en-IN")}</title>
                </circle>
            `;
            xLabelsHtml += `
                <text x="${pt.x}" y="${svgH - 8}" fill="#94A3B8" font-size="10" font-weight="600" text-anchor="middle">${this.escapeHtml(labels[i])}</text>
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

    renderPlatformDonutChart() {
        const allowedHubId = this.getScopedHubId();
        const totalEl = document.getElementById("donut-gross-total");
        if (!allowedHubId) {
            if (totalEl) totalEl.innerText = "₹0";
            return;
        }

        const vehicles = this.getCurrentFilteredVehicles();
        const tfMult = this.selectedTimeframe === "week" ? 7 : this.selectedTimeframe === "month" ? 30 : 1;

        const earningsByPlatform = { Uber: 0, Rapido: 0, Ola: 0 };
        const cabsByPlatform = { Uber: 0, Rapido: 0, Ola: 0 };
        let totalGrossToday = 0;

        vehicles.forEach(v => {
            const amt = Number(v.todayEarnings) || 0;
            totalGrossToday += amt;
            if (v.platform === "Uber" || v.platform === "Rapido" || v.platform === "Ola") {
                earningsByPlatform[v.platform] += amt;
                if (v.status !== "OFFLINE") {
                    cabsByPlatform[v.platform] += 1;
                }
            }
        });

        const totalGross = Math.round(totalGrossToday * tfMult);
        const platformSum = earningsByPlatform.Uber + earningsByPlatform.Rapido + earningsByPlatform.Ola;

        const uberPct = platformSum > 0 ? Math.round((earningsByPlatform.Uber / platformSum) * 100) : 0;
        const rapidoPct = platformSum > 0 ? Math.round((earningsByPlatform.Rapido / platformSum) * 100) : 0;
        const olaPct = platformSum > 0 ? Math.max(0, 100 - uberPct - rapidoPct) : 0;

        const uberAmt = Math.round((totalGross * uberPct) / 100);
        const rapidoAmt = Math.round((totalGross * rapidoPct) / 100);
        const olaAmt = Math.max(0, totalGross - uberAmt - rapidoAmt);

        if (totalEl) {
            totalEl.innerText = "₹" + totalGross.toLocaleString("en-IN");
        }

        // Update SVG donut circles and legend rows inside the donut card
        const donutCard = totalEl ? totalEl.closest(".saas-card") : null;
        if (donutCard) {
            const cardHeading = donutCard.querySelector("h3 span");
            if (cardHeading) {
                const tfTitle = this.selectedTimeframe === "week" ? "This Week" : this.selectedTimeframe === "month" ? "This Month" : "Today";
                cardHeading.innerText = `Platform Earnings (${tfTitle})`;
            }

            const circles = donutCard.querySelectorAll("svg circle");
            const circumference = 238.8;
            const uberLen = ((uberPct / 100) * circumference).toFixed(1);
            const rapidoLen = ((rapidoPct / 100) * circumference).toFixed(1);
            const olaLen = ((olaPct / 100) * circumference).toFixed(1);

            if (circles[1]) {
                circles[1].setAttribute("stroke-dasharray", `${uberLen} ${circumference}`);
                circles[1].setAttribute("stroke-dashoffset", "0");
            }
            if (circles[2]) {
                circles[2].setAttribute("stroke-dasharray", `${rapidoLen} ${circumference}`);
                circles[2].setAttribute("stroke-dashoffset", `${(-Number(uberLen)).toFixed(1)}`);
            }
            if (circles[3]) {
                circles[3].setAttribute("stroke-dasharray", `${olaLen} ${circumference}`);
                circles[3].setAttribute("stroke-dashoffset", `${(-(Number(uberLen) + Number(rapidoLen))).toFixed(1)}`);
            }

            const legendRows = donutCard.querySelectorAll(".flex-col.gap-3 > .flex.items-center.justify-between");
            const stats = [
                { pct: uberPct, amt: uberAmt },
                { pct: rapidoPct, amt: rapidoAmt },
                { pct: olaPct, amt: olaAmt }
            ];
            legendRows.forEach((row, idx) => {
                if (!stats[idx]) return;
                const valueSpans = row.querySelectorAll(".text-right span");
                if (valueSpans[0]) valueSpans[0].innerText = `${stats[idx].pct}%`;
                if (valueSpans[1]) valueSpans[1].innerText = `₹${stats[idx].amt.toLocaleString("en-IN")}`;
            });
        }

        this.renderAnalyticsCards(
            { Uber: uberPct, Rapido: rapidoPct, Ola: olaPct },
            cabsByPlatform
        );
    }

    renderAnalyticsCards(pctMap, cabCountMap) {
        const analyticsTab = document.querySelector('[data-tab-content="analytics"]');
        if (!analyticsTab) return;

        const cards = analyticsTab.querySelectorAll(".grid > div");
        const platforms = [
            { name: "Uber", pct: pctMap.Uber, cabs: cabCountMap.Uber },
            { name: "Rapido", pct: pctMap.Rapido, cabs: cabCountMap.Rapido },
            { name: "Ola", pct: pctMap.Ola, cabs: cabCountMap.Ola }
        ];

        cards.forEach((card, idx) => {
            const item = platforms[idx];
            if (!item) return;
            const titleEl = card.querySelector("div:first-child");
            const barEl = card.querySelector(".h-full");
            const countEl = card.querySelector(".font-mono");
            if (titleEl) titleEl.innerText = `${item.name} Cabs (${item.pct}%)`;
            if (barEl) barEl.style.width = `${item.pct}%`;
            if (countEl) countEl.innerText = `${item.cabs} Active ${item.cabs === 1 ? "Cab" : "Cabs"}`;
        });
    }

    // ------------------------------------------------------------------------
    // Duty Register, Telemetry, Drivers & Vehicles Views
    // ------------------------------------------------------------------------
    _getVehicleModelMap() {
        const vehicles = this.getCurrentFilteredVehicles();
        const map = new Map();
        vehicles.forEach(v => {
            if (v && v.regNo && v.model) {
                map.set(String(v.regNo).trim().toUpperCase(), v.model);
            }
        });
        return map;
    }

    _renderMobileShiftCardHtml(log, { thirdLabel, thirdValue, thirdClass, showEditButton = false, vehicleModel = "" }) {
        const isSettled = log.status === "SETTLED";
        const safeLogId = this.escapeHtml(log.id);
        const modelHtml = vehicleModel
            ? `<div class="text-[11px] text-slate-400 mt-0.5">${this.renderTruncatedNameHtml(vehicleModel, 16)}</div>`
            : "";

        return `
            <div class="saas-card p-4 flex flex-col justify-between gap-3 hover:border-[#6655F5]/50 transition-all">
                <div>
                    <div class="flex justify-between items-start gap-2 mb-2.5">
                        <div class="min-w-0">
                            <div class="font-bold text-sm text-white">${this.renderTruncatedNameHtml(log.driverName, 16)}</div>
                            <div class="flex items-center gap-2 mt-0.5 flex-wrap">
                                <span class="font-mono text-xs font-bold text-slate-200">${this.escapeHtml(log.vehicleNo)}</span>
                                <span class="px-2 py-0.5 rounded bg-[#0B1021] border border-[#263149] text-[10px] font-mono text-slate-300">${this.escapeHtml(log.loginDate)}</span>
                            </div>
                            ${modelHtml}
                        </div>
                        <button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="shrink-0 text-[10px] font-bold px-2.5 py-1 rounded-full ${isSettled ? 'bg-[#10B981]/15 text-[#10B981] border border-[#10B981]/30 hover:bg-[#10B981]/25' : 'bg-[#F59E0B]/15 text-[#F59E0B] border border-[#F59E0B]/30 hover:bg-[#F59E0B]/25'} transition-colors cursor-pointer" title="Click to edit settlement">
                            ${isSettled ? '✓ Settled' : '⏳ Pending'}
                        </button>
                    </div>

                    <div class="grid grid-cols-4 bg-[#0B1021] border border-[#263149] rounded-xl p-2.5 gap-1.5 text-center">
                        <div>
                            <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Trips</div>
                            <div class="text-xs font-extrabold text-white mt-0.5 font-mono">${Number(log.trips) || 0}</div>
                        </div>
                        <div>
                            <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Gross</div>
                            <div class="text-xs font-extrabold text-[#10B981] mt-0.5 font-mono">₹${Math.round(log.earnings || 0).toLocaleString("en-IN")}</div>
                        </div>
                        <div>
                            <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">${this.escapeHtml(thirdLabel)}</div>
                            <div class="text-xs font-extrabold ${thirdClass} mt-0.5 font-mono">${this.escapeHtml(thirdValue)}</div>
                        </div>
                        <div>
                            <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Comm</div>
                            <div class="text-xs font-extrabold text-[#6655F5] mt-0.5 font-mono">₹${Math.round(log.commissionRent || 0).toLocaleString("en-IN")}</div>
                        </div>
                    </div>
                </div>

                <div class="pt-2.5 border-t border-[#263149] flex items-center justify-end gap-2">
                    ${showEditButton ? `
                        <button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="px-2.5 py-1.5 rounded-lg bg-[#6655F5]/20 hover:bg-[#6655F5]/30 text-[#8B5CF6] hover:text-white text-xs font-bold flex items-center gap-1 transition-colors cursor-pointer" title="Edit Shift & Settlement">
                            <i data-lucide="edit-3" class="w-3.5 h-3.5"></i> Edit
                        </button>
                    ` : ""}
                    <button type="button" onclick="window.SVM_APP.printReceipt('${safeLogId}')" class="px-2.5 py-1.5 rounded-lg bg-[#141B30] hover:bg-[#1E273D] text-slate-300 hover:text-white text-xs font-bold flex items-center gap-1 transition-colors cursor-pointer" title="Print Receipt">
                        <i data-lucide="printer" class="w-3.5 h-3.5"></i> Receipt
                    </button>
                </div>
            </div>
        `;
    }

    renderDutyRegister() {
        const tbodyOverview = document.getElementById("register-table-body");
        const tbodyFull = document.getElementById("register-table-body-full");
        const cardsContainer = document.getElementById("register-cards-container");
        const overviewCardsContainer = document.getElementById("overview-register-cards");
        const overviewTitle = document.getElementById("overview-register-title");

        const allowedHubId = this.getScopedHubId();
        if (overviewTitle) {
            overviewTitle.innerText = (!allowedHubId || allowedHubId === "ALL")
                ? "All Hubs Duty Register (Recent Entries)"
                : `${this.getActiveHubName()} Hub Duty Register (Recent Entries)`;
        }

        if (!allowedHubId) {
            if (tbodyOverview) tbodyOverview.innerHTML = "";
            if (tbodyFull) tbodyFull.innerHTML = "";
            if (cardsContainer) cardsContainer.innerHTML = "";
            if (overviewCardsContainer) overviewCardsContainer.innerHTML = "";
            return;
        }

        const logs = this.getCurrentFilteredLogs();
        const vehicleModelMap = this._getVehicleModelMap();
        const resolveVehicleModel = (log) => {
            if (log.vehicleModel || log.model) return log.vehicleModel || log.model;
            const regKey = String(log.vehicleNo || "").trim().toUpperCase();
            return vehicleModelMap.get(regKey) || "";
        };

        if (overviewCardsContainer) {
            const recentLogs = logs.slice(0, 6);
            if (recentLogs.length === 0) {
                overviewCardsContainer.innerHTML = `
                    <div class="col-span-full py-8 text-center text-xs text-slate-500 saas-card">
                        No duty logbook records match your filter.
                    </div>
                `;
            } else {
                overviewCardsContainer.innerHTML = recentLogs.map(log =>
                    this._renderMobileShiftCardHtml(log, {
                        thirdLabel: "Cash",
                        thirdValue: `₹${Math.round(log.cashCollection || 0).toLocaleString("en-IN")}`,
                        thirdClass: "text-[#F59E0B]",
                        showEditButton: true,
                        vehicleModel: resolveVehicleModel(log)
                    })
                ).join("");
            }
        }

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
                    const safeLogId = this.escapeHtml(log.id);
                    const vehModel = resolveVehicleModel(log);

                    return `
                        <div class="saas-card p-4 flex flex-col justify-between hover:border-[#6655F5]/50 transition-all">
                            <div>
                                <div class="flex justify-between items-start mb-3 gap-2">
                                    <div class="flex items-center gap-3 min-w-0">
                                        <div class="w-10 h-10 rounded-full bg-[#141B30] border border-[#263149] flex items-center justify-center text-[#6655F5] shrink-0">
                                            <i data-lucide="car" class="w-5 h-5"></i>
                                        </div>
                                        <div class="min-w-0">
                                            <div class="flex items-center gap-2 flex-wrap">
                                                <h4 class="font-extrabold text-base text-white font-mono">${this.escapeHtml(log.vehicleNo)}</h4>
                                                ${vehModel ? this.renderTruncatedNameHtml(vehModel, 16, "text-[11px] text-slate-400") : ""}
                                                <button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="text-[10px] font-bold px-2 py-0.5 rounded-full ${isSettled ? 'bg-[#10B981]/15 text-[#10B981] border border-[#10B981]/30 hover:bg-[#10B981]/25' : 'bg-[#F59E0B]/15 text-[#F59E0B] border border-[#F59E0B]/30 hover:bg-[#F59E0B]/25'} transition-colors cursor-pointer" title="Click to edit settlement">
                                                    ${isSettled ? '✓ Settled' : '⏳ Pending'}
                                                </button>
                                            </div>
                                            <div class="flex items-center gap-2 text-xs text-slate-300 mt-0.5 flex-wrap">
                                                ${this.renderTruncatedNameHtml(log.driverName, 16, "font-bold")}
                                                <span class="text-slate-500">•</span>
                                                <a href="tel:${this.escapeHtml(log.driverPhone || '')}" class="text-[#6655F5] hover:underline font-mono text-[11px] flex items-center gap-1">
                                                    <i data-lucide="phone" class="w-3 h-3"></i> ${this.escapeHtml(log.driverPhone || 'No Phone')}
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="text-right shrink-0">
                                        <span class="text-xs text-slate-400 font-semibold block">${is12H ? '12h Shift' : '24h Shift'}</span>
                                        <span class="text-[10px] font-bold px-2 py-0.5 rounded ${isAuto ? 'bg-[#6655F5]/20 text-[#8B5CF6]' : 'bg-slate-800 text-slate-400'}">
                                            ${this.escapeHtml(log.source || 'Manual')}
                                        </span>
                                    </div>
                                </div>

                                <div class="py-1.5 px-3 rounded-lg bg-[#0B1021] border border-[#263149] flex justify-between text-[11px] text-slate-400 mb-3">
                                    <span>Date: <strong class="text-slate-200">${this.escapeHtml(log.loginDate)}</strong></span>
                                    <span>Duty: <strong class="text-slate-200">${this.escapeHtml(log.loginTime || '06:00 AM')} - ${this.escapeHtml(log.logoutTime || '08:00 PM')}</strong></span>
                                </div>

                                <div class="grid grid-cols-4 bg-[#0B1021] border border-[#263149] rounded-xl p-2.5 gap-2 text-center mb-3">
                                    <div>
                                        <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Trips</div>
                                        <div class="text-sm font-extrabold text-white mt-0.5 font-mono">${Number(log.trips) || 0}</div>
                                    </div>
                                    <div>
                                        <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Gross</div>
                                        <div class="text-sm font-extrabold text-[#10B981] mt-0.5 font-mono">₹${Math.round(log.earnings || 0)}</div>
                                    </div>
                                    <div>
                                        <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Cash</div>
                                        <div class="text-sm font-extrabold text-[#F59E0B] mt-0.5 font-mono">₹${Math.round(log.cashCollection || 0)}</div>
                                    </div>
                                    <div>
                                        <div class="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Comm</div>
                                        <div class="text-sm font-extrabold text-[#6655F5] mt-0.5 font-mono">₹${Math.round(log.commissionRent || 0)}</div>
                                    </div>
                                </div>

                                <div class="text-xs text-slate-400 flex flex-wrap justify-between items-center py-2 border-t border-[#263149]">
                                    <span class="flex items-center gap-1">
                                        <i data-lucide="fuel" class="w-3.5 h-3.5 text-[#0EA5E9]"></i> Fuel/Exp: <strong class="text-[#0EA5E9]">₹${Number(log.expenditure) || 0}</strong>
                                    </span>
                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-bold ${tierBadgeClass}">
                                        ${commTier > 0 ? commTier + '% Tier' : 'Flat Rent'}
                                    </span>
                                </div>
                            </div>

                            <div class="mt-3 pt-3 border-t border-[#263149] flex justify-between items-center gap-2">
                                <div>
                                    <span class="text-[10px] text-slate-500 uppercase tracking-wider block font-bold">Net Balance</span>
                                    <span class="font-extrabold text-sm ${log.netBalance >= 0 ? 'text-[#10B981]' : 'text-[#F59E0B]'}">
                                        ${log.netBalance >= 0 ? 'Company Pays: ₹' + Math.round(log.netBalance) : 'Driver Handover: ₹' + Math.round(Math.abs(log.netBalance))}
                                    </span>
                                </div>
                                <div class="flex items-center gap-1.5">
                                    <button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="px-2.5 py-1.5 rounded-lg bg-[#6655F5]/20 hover:bg-[#6655F5]/30 text-[#8B5CF6] hover:text-white text-xs font-bold flex items-center gap-1 transition-colors cursor-pointer" title="Edit Shift & Commission">
                                        <i data-lucide="edit-3" class="w-3.5 h-3.5"></i> Edit
                                    </button>
                                    <button type="button" onclick="window.SVM_APP.printReceipt('${safeLogId}')" class="px-2.5 py-1.5 rounded-lg bg-[#141B30] hover:bg-[#1E273D] text-slate-300 hover:text-white text-xs font-bold flex items-center gap-1 transition-colors cursor-pointer" title="Print Receipt">
                                        <i data-lucide="printer" class="w-3.5 h-3.5"></i> Receipt
                                    </button>
                                </div>
                            </div>
                        </div>
                    `;
                }).join("");
            }
        }

        const tableRowsHtml = logs.map((log, idx) => {
            const isSettled = log.status === "SETTLED";
            const initials = (log.driverName || "DR").split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2) || "DR";
            const avatarColors = ["bg-[#6655F5]", "bg-[#0EA5E9]", "bg-[#8B5CF6]", "bg-[#10B981]", "bg-[#EC4899]"];
            const avatarColor = avatarColors[Math.abs((log.driverName || "D").charCodeAt(0)) % avatarColors.length];
            const safeLogId = this.escapeHtml(log.id);
            const vehModel = resolveVehicleModel(log);

            let sourceTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#162038] text-slate-200 border border-[#263149]">${this.escapeHtml(log.source || "Uber")}</span>`;
            if (log.source === "Rapido" || (log.remarks && log.remarks.toLowerCase().includes("rapido"))) {
                sourceTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#F59E0B]/20 text-[#F59E0B] border border-[#F59E0B]/30">Rapido</span>`;
            } else if (log.source === "Ola" || (log.remarks && log.remarks.toLowerCase().includes("ola"))) {
                sourceTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30">Ola</span>`;
            } else if (log.source === "Uber") {
                sourceTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#0B1021] text-white border border-[#334155]">Uber</span>`;
            }

            const statusTag = isSettled
                ? `<button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#10B981]/15 text-[#10B981] border border-[#10B981]/30 hover:bg-[#10B981]/25 transition-colors cursor-pointer" title="Click to Edit Settlement">✓ Settled</button>`
                : `<button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#F59E0B]/15 text-[#F59E0B] border border-[#F59E0B]/30 hover:bg-[#F59E0B]/25 transition-colors cursor-pointer" title="Click to Settle & Edit">⏳ Pending</button>`;

            return `
                <tr class="hover:bg-[#11182A]/70 transition-colors">
                    <td class="py-3 px-3 text-xs font-bold text-[#64748B] font-mono">${idx + 1}</td>
                    <td class="py-3 px-4">
                        <div class="flex items-center gap-3">
                            <div class="w-8 h-8 rounded-full ${avatarColor} flex items-center justify-center font-bold text-xs text-white shadow-sm shrink-0">
                                ${this.escapeHtml(initials)}
                            </div>
                            <div class="min-w-0">
                                <div class="font-bold text-sm text-white">${this.renderTruncatedNameHtml(log.driverName, 16)}</div>
                                <div class="text-xs text-[#64748B] font-mono">${this.escapeHtml(log.driverPhone || "")}</div>
                            </div>
                        </div>
                    </td>
                    <td class="py-3 px-4">
                        <div class="flex items-center gap-2">
                            <i data-lucide="car" class="w-4 h-4 text-[#64748B] shrink-0"></i>
                            <div>
                                <span class="font-mono text-xs font-bold text-slate-200 block">${this.escapeHtml(log.vehicleNo)}</span>
                                ${vehModel ? this.renderTruncatedNameHtml(vehModel, 16, "text-[10px] text-[#64748B] block") : ""}
                            </div>
                        </div>
                    </td>
                    <td class="py-3 px-3 text-center font-bold text-white text-sm font-mono">${Number(log.trips) || 0}</td>
                    <td class="py-3 px-4 text-right font-bold text-white font-mono">₹${Math.round(log.earnings || 0).toLocaleString("en-IN")}</td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">₹${(Number(log.expenditure) || 0).toFixed(2)}</td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">₹${Math.round(log.cashCollection || 0).toLocaleString("en-IN")}</td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">₹${Math.round(log.commissionRent || 0).toLocaleString("en-IN")}</td>
                    <td class="py-3 px-3 text-center">${sourceTag}</td>
                    <td class="py-3 px-3 text-center">${statusTag}</td>
                    <td class="py-3 px-3 text-right">
                        <div class="flex items-center justify-end gap-1">
                            <button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="p-1.5 rounded-lg hover:bg-[#1E273D] text-[#6655F5] hover:text-[#8B5CF6] transition-colors cursor-pointer" title="Edit Shift & Settlement">
                                <i data-lucide="edit-3" class="w-4 h-4"></i>
                            </button>
                            <button type="button" onclick="window.SVM_APP.printReceipt('${safeLogId}')" class="p-1.5 rounded-lg hover:bg-[#1E273D] text-[#94A3B8] hover:text-white transition-colors cursor-pointer" title="Print Driver Receipt">
                                <i data-lucide="printer" class="w-4 h-4"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join("");

        const tableRowsFullHtml = logs.map((log, idx) => {
            const isSettled = log.status === "SETTLED";
            const initials = (log.driverName || "DR").split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2) || "DR";
            const avatarColors = ["bg-[#6655F5]", "bg-[#0EA5E9]", "bg-[#8B5CF6]", "bg-[#10B981]", "bg-[#EC4899]"];
            const avatarColor = avatarColors[Math.abs((log.driverName || "D").charCodeAt(0)) % avatarColors.length];
            const hubName = this.getHubLabel(log.managerId);
            const safeLogId = this.escapeHtml(log.id);
            const vehModel = resolveVehicleModel(log);

            let platformTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#162038] text-slate-200 border border-[#263149]">${this.escapeHtml(log.source || "Uber")}</span>`;
            if (log.source === "Rapido" || (log.remarks && log.remarks.toLowerCase().includes("rapido"))) {
                platformTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#F59E0B]/20 text-[#F59E0B] border border-[#F59E0B]/30">Rapido</span>`;
            } else if (log.source === "Ola" || (log.remarks && log.remarks.toLowerCase().includes("ola"))) {
                platformTag = `<span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30">Ola</span>`;
            }

            const statusTag = isSettled
                ? `<button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#10B981]/15 text-[#10B981] border border-[#10B981]/30 hover:bg-[#10B981]/25 transition-colors cursor-pointer" title="Click to Edit Settlement">✓ Settled</button>`
                : `<button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#F59E0B]/15 text-[#F59E0B] border border-[#F59E0B]/30 hover:bg-[#F59E0B]/25 transition-colors cursor-pointer" title="Click to Settle & Edit">⏳ Pending</button>`;

            return `
                <tr class="hover:bg-[#11182A]/70 transition-colors">
                    <td class="py-3 px-3 text-xs font-bold text-[#64748B] font-mono">${idx + 1}</td>
                    <td class="py-3 px-4">
                        <div class="flex items-center gap-2.5">
                            <div class="w-7 h-7 rounded-full ${avatarColor} flex items-center justify-center font-bold text-xs text-white shadow-sm shrink-0">
                                ${this.escapeHtml(initials)}
                            </div>
                            ${this.renderTruncatedNameHtml(log.driverName, 16, "font-bold text-sm text-white")}
                        </div>
                    </td>
                    <td class="py-3 px-4 font-mono text-xs text-[#94A3B8]">${this.escapeHtml(log.driverPhone || "")}</td>
                    <td class="py-3 px-4">
                        <span class="font-mono text-xs font-bold text-slate-200 block">${this.escapeHtml(log.vehicleNo)}</span>
                        ${vehModel ? this.renderTruncatedNameHtml(vehModel, 16, "text-[10px] text-[#64748B] block") : ""}
                    </td>
                    <td class="py-3 px-3">
                        <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-[#141B30] text-[#8B5CF6] border border-[#263149] inline-block">${this.renderTruncatedNameHtml(hubName, 16)}</span>
                    </td>
                    <td class="py-3 px-3 text-xs text-slate-300 font-mono">${this.escapeHtml(log.loginDate)}</td>
                    <td class="py-3 px-3 text-center font-bold text-white text-sm font-mono">${Number(log.trips) || 0}</td>
                    <td class="py-3 px-4 text-right font-bold text-white font-mono">₹${Math.round(log.earnings || 0).toLocaleString("en-IN")}</td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">₹${(Number(log.expenditure) || 0).toFixed(2)}</td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">₹${Math.round(log.cashCollection || 0).toLocaleString("en-IN")}</td>
                    <td class="py-3 px-4 text-right text-slate-300 font-mono text-xs">₹${Math.round(log.commissionRent || 0).toLocaleString("en-IN")}</td>
                    <td class="py-3 px-3 text-center">${platformTag}</td>
                    <td class="py-3 px-3 text-center">${statusTag}</td>
                    <td class="py-3 px-3 text-right">
                        <div class="flex items-center justify-end gap-1">
                            <button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="p-1.5 rounded-lg hover:bg-[#1E273D] text-[#6655F5] hover:text-[#8B5CF6] transition-colors cursor-pointer" title="Edit Shift & Settlement">
                                <i data-lucide="edit-3" class="w-4 h-4"></i>
                            </button>
                            <button type="button" onclick="window.SVM_APP.printReceipt('${safeLogId}')" class="p-1.5 rounded-lg hover:bg-[#1E273D] text-[#94A3B8] hover:text-white transition-colors cursor-pointer" title="Print Driver Receipt">
                                <i data-lucide="printer" class="w-4 h-4"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join("");

        if (tbodyOverview) tbodyOverview.innerHTML = tableRowsHtml;
        if (tbodyFull) tbodyFull.innerHTML = tableRowsFullHtml;

        const countBadge = document.getElementById("register-records-count");
        if (countBadge) {
            const totalGross = logs.reduce((sum, l) => sum + (Number(l.earnings) || 0), 0);
            countBadge.innerText = `Showing ${logs.length} Shift Logs (₹${Math.round(totalGross).toLocaleString("en-IN")})`;
        }

        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    renderPendingTelemetryList() {
        const container = document.getElementById("telemetry-pending-list");
        if (!container) return;

        const allowedHubId = this.getScopedHubId();
        if (!allowedHubId) {
            container.innerHTML = "";
            return;
        }

        let items = [];
        if (allowedHubId === "ALL") {
            items = window.DEMO_REPO ? window.DEMO_REPO.getTelemetry() : [];
        } else {
            const hubShort = this.getActiveHubName();
            const hubVehicles = this.getCurrentFilteredVehicles().filter(v => v.status !== "OFFLINE");
            items = hubVehicles.slice(0, 4).map((v, idx) => ({
                driverName: v.driverName || "Hub Driver",
                vehicleNo: v.regNo,
                platform: v.platform && v.platform !== "Offline" ? v.platform : "Fleet",
                status: idx === 0 ? "Auto Aggregated" : idx === 1 ? "Shift Active" : idx === 2 ? "Trip Synced" : "Pending Review",
                statusColor: idx === 0
                    ? "bg-[#10B981]/15 text-[#10B981] border-[#10B981]/30"
                    : idx === 1
                    ? "bg-[#3185F6]/15 text-[#3185F6] border-[#3185F6]/30"
                    : idx === 2
                    ? "bg-[#6655F5]/15 text-[#8B5CF6] border-[#6655F5]/30"
                    : "bg-[#F59E0B]/15 text-[#F59E0B] border-[#F59E0B]/30",
                time: `${(idx + 1) * 3} min ago • ${hubShort}`
            }));
        }

        if (items.length === 0) {
            container.innerHTML = `
                <div class="p-4 text-center text-xs text-slate-500">
                    No active shift telemetry for ${this.escapeHtml(this.getActiveHubName())} yet.
                </div>
            `;
            return;
        }

        container.innerHTML = items.map(item => `
            <div class="p-3 rounded-xl bg-[#0B1021] border border-[#263149] hover:border-[#6655F5]/40 flex items-center justify-between gap-3 transition-colors">
                <div class="flex items-center gap-3 min-w-0">
                    <div class="w-8 h-8 rounded-lg bg-[#141B30] border border-[#263149] flex items-center justify-center text-[#94A3B8] shrink-0">
                        <i data-lucide="car" class="w-4 h-4"></i>
                    </div>
                    <div class="min-w-0">
                        <div class="text-xs font-bold text-white">${this.renderTruncatedNameHtml(item.driverName, 16)}</div>
                        <div class="text-[10px] text-[#64748B] font-mono flex items-center gap-1 mt-0.5 truncate">
                            <i data-lucide="car" class="w-3 h-3 text-[#64748B] shrink-0"></i> ${this.escapeHtml(item.vehicleNo)} • ${this.escapeHtml(item.platform)}
                        </div>
                    </div>
                </div>
                <div class="text-right shrink-0">
                    <span class="inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold border ${item.statusColor}">
                        ${this.escapeHtml(item.status)}
                    </span>
                    <div class="text-[10px] text-[#64748B] mt-0.5">${this.escapeHtml(item.time)}</div>
                </div>
            </div>
        `).join("");

        if (window.lucide) window.lucide.createIcons();
    }

    renderDriversGrid() {
        const container = document.getElementById("drivers-grid");
        if (!container) return;

        const allowedHubId = this.getScopedHubId();
        if (!allowedHubId || !window.DEMO_REPO) {
            container.innerHTML = "";
            return;
        }

        const drivers = this.getCurrentFilteredDrivers();
        const logs = window.DEMO_REPO.getDutyLogs(allowedHubId);
        const hubs = window.DEMO_REPO.getHubs();

        if (drivers.length === 0) {
            container.innerHTML = `
                <div class="col-span-full py-12 text-center text-slate-500 saas-card">
                    No drivers assigned to this hub yet.
                </div>
            `;
            return;
        }

        container.innerHTML = drivers.map(d => {
            const hub = hubs.find(h => h.id === d.managerId);
            const hubLabel = hub ? hub.shortName : (d.managerId || "Fleet");
            const driverLogs = logs.filter(l =>
                (l.driverName && d.name && l.driverName.toLowerCase() === d.name.toLowerCase()) ||
                (l.driverPhone && d.phone && l.driverPhone === d.phone)
            );
            const totalShiftsLogged = driverLogs.length;
            const isOnDuty = d.status === "ON_DUTY";

            return `
                <div class="saas-card p-4 flex flex-col justify-between hover:border-[#6655F5]/40 transition-all">
                    <div>
                        <div class="flex justify-between items-start mb-2.5 gap-2">
                            <div class="min-w-0">
                                <div class="flex items-center gap-2 flex-wrap">
                                    <h4 class="font-extrabold text-white text-base min-w-0">${this.renderTruncatedNameHtml(d.name, 16)}</h4>
                                    <span class="inline-flex items-center gap-1 px-2 py-0.5 text-[9px] font-bold rounded-full ${isOnDuty ? 'bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30' : 'bg-[#F59E0B]/20 text-[#F59E0B] border border-[#F59E0B]/30'}">
                                        <span class="w-1.5 h-1.5 rounded-full ${isOnDuty ? 'bg-[#10B981]' : 'bg-[#F59E0B]'}"></span>
                                        ${isOnDuty ? 'ON DUTY' : 'IDLE'}
                                    </span>
                                </div>
                                <a href="tel:${this.escapeHtml(d.phone || '')}" class="text-xs text-[#6655F5] hover:underline font-mono flex items-center gap-1 mt-1">
                                    <i data-lucide="phone" class="w-3 h-3"></i> ${this.escapeHtml(d.phone)}
                                </a>
                            </div>
                            <span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-[#141B30] text-[#8B5CF6] border border-[#263149] shrink-0">
                                ${this.renderTruncatedNameHtml(hubLabel, 16)}
                            </span>
                        </div>
                        <div class="grid grid-cols-2 gap-2 mt-3 p-2.5 rounded-xl bg-[#0B1021] border border-[#263149] text-xs">
                            <div>
                                <span class="text-[10px] text-slate-500 block font-semibold uppercase tracking-wider">Assigned Cab</span>
                                <span class="font-mono font-bold text-slate-200 mt-0.5 block">${this.escapeHtml(d.vehicleNo || 'Unassigned')}</span>
                            </div>
                            <div class="text-right">
                                <span class="text-[10px] text-slate-500 block font-semibold uppercase tracking-wider">Duty History</span>
                                <span class="font-mono font-bold text-[#6655F5] mt-0.5 block">${totalShiftsLogged} Logs</span>
                            </div>
                        </div>
                    </div>
                    <div class="mt-4 pt-3 border-t border-[#263149] flex justify-between items-center text-xs gap-2">
                        <span class="text-[#10B981] font-bold flex items-center gap-1">⭐ ${this.escapeHtml(d.rating)}</span>
                        <div class="flex items-center gap-1.5">
                            <button type="button" onclick="window.SVM_APP.filterDriverInRegister(${Number(d.id)})" class="px-2.5 py-1.5 rounded-lg bg-[#141B30] hover:bg-[#1E273D] text-slate-300 hover:text-white text-xs font-semibold flex items-center gap-1 cursor-pointer transition-colors" title="View Duty Register for this driver">
                                <i data-lucide="file-text" class="w-3 h-3"></i> Shifts
                            </button>
                            <button type="button" onclick="window.SVM_APP.openManualEntryForDriver(${Number(d.id)})" class="px-3 py-1.5 rounded-lg bg-[#6655F5] hover:bg-[#5443E8] text-white font-bold text-xs flex items-center gap-1 shadow-md shadow-[#6655F5]/20 cursor-pointer transition-colors">
                                <i data-lucide="plus" class="w-3 h-3"></i> Log Shift
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join("");

        if (window.lucide) window.lucide.createIcons();
    }

    filterDriverInRegister(driverOrId) {
        let driverName = "";
        if (typeof driverOrId === "number") {
            const d = this.getCurrentFilteredDrivers().find(item => item.id === driverOrId);
            driverName = d ? d.name : "";
        } else {
            driverName = String(driverOrId || "");
        }
        if (!driverName) return;

        this.switchTab("register");
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

        const allowedHubId = this.getScopedHubId();
        const titleEl = document.getElementById("vehicles-tab-title");

        if (!allowedHubId || !window.DEMO_REPO) {
            container.innerHTML = "";
            if (titleEl) titleEl.innerText = "SVM Fleet Vehicles (0 Commercial Cabs)";
            return;
        }

        const vehicles = this.getCurrentFilteredVehicles();
        const hubs = window.DEMO_REPO.getHubs();
        const canTransfer = Boolean(this.auth && this.auth.canManageHubs());

        if (titleEl) {
            const prefix = allowedHubId === "ALL" ? "SVM Fleet" : `${this.getActiveHubName()} Hub`;
            titleEl.innerText = `${prefix} Vehicles (${vehicles.length} Commercial ${vehicles.length === 1 ? "Cab" : "Cabs"})`;
        }

        if (vehicles.length === 0) {
            container.innerHTML = `
                <div class="col-span-full py-12 text-center text-slate-500 saas-card">
                    No vehicles assigned to this hub yet.
                </div>
            `;
            return;
        }

        container.innerHTML = vehicles.map(v => {
            const isActive = v.status === "ACTIVE";
            const hub = hubs.find(h => h.id === v.managerId);

            return `
                <div class="saas-card p-4 flex flex-col justify-between hover:border-[#6655F5]/40 transition-all">
                    <div>
                        <div class="flex justify-between items-start mb-2">
                            <div class="min-w-0">
                                <h4 class="font-bold text-white font-mono text-base">${this.escapeHtml(v.regNo)}</h4>
                                <p class="text-xs text-slate-400">${this.renderTruncatedNameHtml(v.model, 16)}</p>
                            </div>
                            <span class="px-2 py-0.5 text-[10px] font-bold rounded ${isActive ? 'bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30' : 'bg-slate-800 text-slate-400'}">
                                ${this.escapeHtml(v.status)}
                            </span>
                        </div>
                        <div class="mt-2 text-xs text-slate-300 flex items-center gap-1.5">
                            <i data-lucide="user" class="w-3.5 h-3.5 text-slate-500 shrink-0"></i> ${this.renderTruncatedNameHtml(v.driverName || "Unassigned", 16)}
                        </div>
                        <div class="mt-1 text-xs text-[#0EA5E9] flex items-center gap-1.5">
                            <i data-lucide="map-pin" class="w-3.5 h-3.5 text-slate-500 shrink-0"></i> ${this.renderTruncatedNameHtml(v.locationName || "Depot", 16)}
                        </div>
                    </div>
                    <div class="mt-4 pt-3 border-t border-[#263149] flex justify-between items-center text-xs">
                        <span class="text-slate-400">${this.renderTruncatedNameHtml(hub ? hub.shortName : "Fleet", 16)}</span>
                        ${canTransfer ? `
                            <button type="button" onclick="window.SVM_APP.openReassignModal(${Number(v.id)})" class="text-xs text-[#6655F5] hover:text-[#8B5CF6] font-semibold underline cursor-pointer">
                                Transfer Hub
                            </button>
                        ` : ""}
                    </div>
                </div>
            `;
        }).join("");

        if (window.lucide) window.lucide.createIcons();
    }

    // ------------------------------------------------------------------------
    // Searchable Driver Combobox & Manual Shift Entry Modal
    // ------------------------------------------------------------------------
    populateQuickDriverDropdown() {
        const select = document.getElementById("modal-quick-driver-select");
        const drivers = this.getCurrentFilteredDrivers();

        if (select) {
            let options = `<option value="">-- Choose Driver (or enter manually below) --</option>`;
            drivers.forEach(d => {
                options += `<option value="${Number(d.id)}">${this.escapeHtml(d.name)} (${this.escapeHtml(d.phone)}) - ${this.escapeHtml(d.vehicleNo || "No Cab")}</option>`;
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
                    No drivers found for this hub. Enter details manually below.
                </div>
            `;
            return;
        }

        dropdown.innerHTML = drivers.map(d => {
            const initials = (d.name || "DR").split(" ").map(n => n[0]).join("").substring(0, 2).toUpperCase();
            const hubName = this.getHubLabel(d.managerId);

            return `
                <div class="dropdown-option flex items-center justify-between p-2.5 hover:bg-[#6655F5]/20 cursor-pointer transition-colors"
                     onclick="window.SVM_APP.selectDriverOption(${Number(d.id)})">
                    <div class="flex items-center gap-2.5">
                        <div class="w-7 h-7 rounded-full bg-[#6655F5]/30 border border-[#6655F5]/40 text-[#8B5CF6] font-bold text-xs flex items-center justify-center shrink-0">
                            ${this.escapeHtml(initials)}
                        </div>
                        <div class="text-left min-w-0">
                            ${this.renderTruncatedNameHtml(d.name, 16, "font-bold text-xs text-white block")}
                            <span class="text-[10px] text-slate-400 font-mono">${this.escapeHtml(d.phone)}</span>
                        </div>
                    </div>
                    <div class="text-right shrink-0">
                        <span class="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-[#162038] text-[#10B981] border border-[#263149] inline-block">
                            ${this.escapeHtml(d.vehicleNo || "No Cab")}
                        </span>
                        ${this.renderTruncatedNameHtml(hubName, 16, "text-[9px] text-slate-400 block mt-0.5")}
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
        const drivers = this.getCurrentFilteredDrivers();
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
        const drivers = this.getCurrentFilteredDrivers();
        const d = drivers.find(item => item.id === parseInt(driverId, 10));
        if (d) {
            const searchInput = document.getElementById("driver-search-input");
            if (searchInput) {
                searchInput.value = `${d.name} (${d.vehicleNo || "No Cab"})`;
            }
            const quickSelect = document.getElementById("modal-quick-driver-select");
            if (quickSelect) quickSelect.value = driverId;
        }

        this.onQuickDriverSelected(driverId);
        this.closeDriverDropdown();
    }

    onQuickDriverSelected(driverId) {
        if (!driverId) return;
        const drivers = this.getCurrentFilteredDrivers();
        const d = drivers.find(item => item.id === parseInt(driverId, 10));
        if (!d) return;

        const nameEl = document.getElementById("modal-drivername");
        const phoneEl = document.getElementById("modal-driverphone");
        const regnoEl = document.getElementById("modal-regno");
        const hubEl = document.getElementById("modal-hub");

        if (nameEl) nameEl.value = d.name;
        if (phoneEl) phoneEl.value = d.phone;
        if (regnoEl) regnoEl.value = d.vehicleNo || "";
        if (hubEl && !hubEl.disabled) {
            const allowedHubId = this.getScopedHubId(d.managerId);
            if (allowedHubId && allowedHubId !== "ALL") {
                hubEl.value = allowedHubId;
            }
        }

        this.updateModalCalculation();
    }

    openManualEntryForDriver(driverId) {
        const d = this.getCurrentFilteredDrivers().find(item => item.id === parseInt(driverId, 10));
        if (!d) {
            this.showToast("Access denied or driver not in your hub.", "alert");
            return;
        }
        this.openManualEntryModal(d.vehicleNo || "", d.name, d.phone);
    }

    openManualEntryModal(prefillRegNo = "", prefillDriverName = "", prefillDriverPhone = "", prefillTrips = "", prefillGross = "") {
        const allowedHubId = this.getScopedHubId();
        if (!allowedHubId) {
            this.showToast("Please sign in to log a shift.", "alert");
            return;
        }

        const modal = document.getElementById("manual-entry-modal");
        if (!modal) return;

        try {
            this.populateQuickDriverDropdown();

            const searchInput = document.getElementById("driver-search-input");
            if (searchInput) {
                searchInput.value = prefillDriverName ? `${prefillDriverName} (${prefillRegNo || ""})` : "";
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
            if (remarksEl) remarksEl.value = `Manual register entry from ${this.getActiveHubName()} logbook.`;

            this.populateHubSelectElement(hubSelect, allowedHubId, true);

            modal.querySelectorAll('input[type="number"]').forEach(input => {
                input.addEventListener("wheel", (e) => e.preventDefault(), { passive: false });
            });

            this.updateModalCalculation();
        } catch (e) {
            console.warn("Error setting modal values:", e);
        }

        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeManualEntryModal() {
        const modal = document.getElementById("manual-entry-modal");
        if (modal) modal.classList.remove("open");
        this.closeDriverDropdown();
        document.body.classList.remove("modal-open");
    }

    updateModalCalculation() {
        try {
            const shiftType = document.getElementById("modal-shifttype")?.value || "12_HOURS";
            const trips = parseInt(document.getElementById("modal-trips")?.value, 10) || 0;
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
        if (!this.auth || !this.auth.isAuthenticated()) {
            this.showToast("Authentication required.", "alert");
            return;
        }

        const shiftType = document.getElementById("modal-shifttype").value;
        const driverName = document.getElementById("modal-drivername").value.trim();
        const driverPhone = document.getElementById("modal-driverphone").value.trim();
        const vehicleNo = document.getElementById("modal-regno").value.trim().toUpperCase();
        const requestedHub = document.getElementById("modal-hub").value;
        const hubId = this.auth.isManager() ? this.auth.getAllowedHubId() : (requestedHub || "mgr_moosapet_01");
        const trips = parseInt(document.getElementById("modal-trips").value, 10) || 0;
        const gross = parseFloat(document.getElementById("modal-earnings").value) || 0;
        const cash = parseFloat(document.getElementById("modal-cash").value) || 0;
        const exp = parseFloat(document.getElementById("modal-expenditure").value) || 0;
        const pass = parseFloat(document.getElementById("modal-drivepass").value) || 0;
        const advance = parseFloat(document.getElementById("modal-advance").value) || 0;
        const status = document.getElementById("modal-status").value;
        const remarks = document.getElementById("modal-remarks").value.trim();

        if (!driverName || !vehicleNo) {
            this.showToast("Please provide both Driver Name and Vehicle Number.", "alert");
            return;
        }

        const calc = SvmCommissionCalculator.calculate(shiftType, trips, gross, cash, exp, pass, advance);

        const newLog = {
            shiftType,
            loginDate: new Date().toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" }),
            loginTime: "06:00 AM",
            logoutDate: new Date().toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" }),
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
        this.renderReportsTab();
        this.showToast(`Manual Shift Log saved for ${driverName}!`, "success");
    }

    // ------------------------------------------------------------------------
    // Transfer Vehicle / Add Driver / Add Vehicle / Add Hub & Manager Modals
    // ------------------------------------------------------------------------
    openReassignModal(vehicleId) {
        if (!this.auth || !this.auth.canManageHubs()) {
            this.showToast("Access denied: Only the Fleet Owner can transfer vehicles between hubs.", "alert");
            return;
        }

        const vehicles = window.DEMO_REPO ? window.DEMO_REPO.getVehicles("ALL") : [];
        const v = vehicles.find(item => item.id === Number(vehicleId));
        if (!v) return;

        const modal = document.getElementById("reassign-hub-modal");
        if (!modal) return;

        const titleEl = document.getElementById("reassign-vehicle-title");
        const idEl = document.getElementById("reassign-vehicle-id");
        if (titleEl) titleEl.innerText = `${v.regNo} (${v.model})`;
        if (idEl) idEl.value = v.id;

        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        const hubSelect = document.getElementById("reassign-target-hub");
        if (hubSelect) {
            hubSelect.innerHTML = hubs.map(h => `
                <option value="${this.escapeHtml(h.id)}" ${v.managerId === h.id ? "selected" : ""}>${this.escapeHtml(h.name)} (${this.escapeHtml(h.managerName)})</option>
            `).join("");
        }

        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeReassignModal() {
        const modal = document.getElementById("reassign-hub-modal");
        if (modal) modal.classList.remove("open");
        document.body.classList.remove("modal-open");
    }

    submitReassignment(e) {
        e.preventDefault();
        if (!this.auth || !this.auth.canManageHubs()) {
            this.showToast("Access denied: Only the Fleet Owner can transfer vehicles between hubs.", "alert");
            return;
        }

        const vehicleId = document.getElementById("reassign-vehicle-id").value;
        const targetHubId = document.getElementById("reassign-target-hub").value;

        if (window.DEMO_REPO) {
            window.DEMO_REPO.reassignVehicle(vehicleId, targetHubId);
        }
        this.closeReassignModal();
        this.refreshAll();
        this.showToast("Vehicle & Driver transferred to new Hub!", "success");
    }

    openAddDriverModal() {
        const allowedHubId = this.getScopedHubId();
        if (!allowedHubId) {
            this.showToast("Please sign in first.", "alert");
            return;
        }

        const modal = document.getElementById("add-driver-modal");
        if (!modal) return;

        const hubSelect = document.getElementById("add-driver-hub");
        this.populateHubSelectElement(hubSelect, allowedHubId, true);

        const vehicleSelect = document.getElementById("add-driver-vehicle");
        if (vehicleSelect) {
            const vehicles = this.getCurrentFilteredVehicles();
            vehicleSelect.innerHTML = `<option value="">-- No Vehicle Assigned Yet --</option>` +
                vehicles.map(v => `<option value="${this.escapeHtml(v.regNo)}">${this.escapeHtml(v.regNo)} (${this.escapeHtml(v.model)}) - ${v.driverName ? "Driver: " + this.escapeHtml(v.driverName) : "Unassigned"}</option>`).join("");
        }

        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeAddDriverModal() {
        const modal = document.getElementById("add-driver-modal");
        if (modal) modal.classList.remove("open");
        document.body.classList.remove("modal-open");
    }

    async submitAddDriver(e) {
        e.preventDefault();
        if (!this.auth || !this.auth.isAuthenticated()) {
            this.showToast("Authentication required.", "alert");
            return;
        }

        const name = document.getElementById("add-driver-name")?.value.trim();
        const phone = document.getElementById("add-driver-phone")?.value.trim();
        const requestedHub = document.getElementById("add-driver-hub")?.value || "mgr_moosapet_01";
        const managerId = this.auth.isManager() ? this.auth.getAllowedHubId() : requestedHub;
        const vehicleNo = document.getElementById("add-driver-vehicle")?.value.trim().toUpperCase() || "";
        const rating = parseFloat(document.getElementById("add-driver-rating")?.value) || 4.85;
        const status = document.getElementById("add-driver-status")?.value || "ON_DUTY";

        if (!name || !phone) {
            this.showToast("Please provide both Driver Name and Mobile Number.", "alert");
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
        this.showToast(`Driver ${name} enrolled in ${this.getHubLabel(managerId)} Hub!`, "success");
    }

    openAddVehicleModal() {
        const allowedHubId = this.getScopedHubId();
        if (!allowedHubId) {
            this.showToast("Please sign in first.", "alert");
            return;
        }

        const modal = document.getElementById("add-vehicle-modal");
        if (!modal) return;

        const hubSelect = document.getElementById("add-veh-hub");
        this.populateHubSelectElement(hubSelect, allowedHubId, true);

        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeAddVehicleModal() {
        const modal = document.getElementById("add-vehicle-modal");
        if (modal) modal.classList.remove("open");
        document.body.classList.remove("modal-open");
    }

    async submitAddVehicle(e) {
        e.preventDefault();
        if (!this.auth || !this.auth.isAuthenticated()) {
            this.showToast("Authentication required.", "alert");
            return;
        }

        const regNo = document.getElementById("add-veh-reg")?.value.trim().toUpperCase();
        const model = document.getElementById("add-veh-model")?.value.trim();
        const requestedHub = document.getElementById("add-veh-hub")?.value || "mgr_moosapet_01";
        const managerId = this.auth.isManager() ? this.auth.getAllowedHubId() : requestedHub;
        const platform = document.getElementById("add-veh-platform")?.value || "Uber";

        if (!regNo || !model) {
            this.showToast("Please provide Registration Plate and Vehicle Model.", "alert");
            return;
        }

        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        const hub = hubs.find(h => h.id === managerId);

        const newVehicle = {
            regNo,
            model,
            managerId,
            platform,
            driverName: "Unassigned",
            locationName: `${hub ? hub.shortName : "Fleet"} Hub Yard`,
            lat: hub ? hub.lat : 17.4652,
            lng: hub ? hub.lng : 78.4308,
            status: "ACTIVE"
        };

        if (window.DEMO_REPO) {
            window.DEMO_REPO.addVehicle(newVehicle);
        }

        this.closeAddVehicleModal();
        this.refreshAll();
        this.renderPlatformDonutChart();
        this.showToast(`Vehicle ${regNo} added to ${this.getHubLabel(managerId)} Hub!`, "success");
    }

    openAddManagerModal() {
        if (!this.auth || !this.auth.canManageHubs()) {
            this.showToast("Access denied: Only the Fleet Owner can add new hubs & managers.", "alert");
            return;
        }
        const modal = document.getElementById("add-manager-modal");
        if (!modal) return;
        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeAddManagerModal() {
        const modal = document.getElementById("add-manager-modal");
        if (modal) modal.classList.remove("open");
        document.body.classList.remove("modal-open");
    }

    async submitAddManager(e) {
        e.preventDefault();
        if (!this.auth || !this.auth.canManageHubs()) {
            this.showToast("Access denied: Only the Fleet Owner can create hubs.", "alert");
            return;
        }

        const hubName = document.getElementById("add-hub-name")?.value.trim();
        const shortName = document.getElementById("add-hub-short")?.value.trim();
        const managerName = document.getElementById("add-hub-manager")?.value.trim();
        const phone = document.getElementById("add-hub-phone")?.value.trim();

        if (!hubName || !managerName || !phone) {
            this.showToast("Please fill in Hub Name, Manager Name, and Phone Number.", "alert");
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
            cabsCount: 0
        };

        if (window.DEMO_REPO) {
            window.DEMO_REPO.data.hubs.push(newHub);
            window.DEMO_REPO.saveState();
        }

        this.closeAddManagerModal();
        this.populateLoginManagerHubs();
        this.syncHubSelectorOptions();
        this.renderRoleSelector();
        this._refreshMapHubs(this.mapController);
        this._refreshMapHubs(this.fullMapController);
        this.refreshAll();
        this.showToast(`New Hub "${hubName}" & Manager ${managerName} created!`, "success");
    }

    // ------------------------------------------------------------------------
    // CSV Export & Receipt Printing (Strictly Hub-Scoped)
    // ------------------------------------------------------------------------
    exportCSV() {
        const logs = this.getCurrentFilteredLogs();
        if (logs.length === 0) {
            this.showToast("No logs available to export.", "alert");
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
            `"${String(l.driverName || "").replace(/"/g, '""')}"`,
            `"${String(l.driverPhone || "").replace(/"/g, '""')}"`,
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
            `"${String(l.remarks || "").replace(/"/g, '""')}"`
        ]);

        const csvContent = "data:text/csv;charset=utf-8," + [headers.join(","), ...rows.map(e => e.join(","))].join("\n");
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        const hubTag = this.getScopedHubId() || "hub";
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", `SVM_FLEET_Duty_Register_${hubTag}_${Date.now()}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        this.showToast("CSV statement exported successfully!", "success");
    }

    printReceipt(logId) {
        const allowedHubId = this.getScopedHubId();
        if (!allowedHubId || !window.DEMO_REPO) return;

        const logs = window.DEMO_REPO.getDutyLogs(allowedHubId);
        const log = logs.find(l => String(l.id) === String(logId));
        if (!log) {
            this.showToast("Access denied or shift record not found for your hub.", "alert");
            return;
        }

        const hubs = window.DEMO_REPO.getHubs();
        const hub = hubs.find(h => h.id === log.managerId);
        const hubLocation = hub ? `${hub.name}, Hyderabad` : "Hyderabad Operations";

        const printWin = window.open("", "_blank");
        if (!printWin) {
            this.showToast("Please allow popups to print receipt.", "alert");
            return;
        }
        printWin.document.write(`
            <html>
            <head>
                <title>SVM FLEET - Shift Settlement Receipt #${this.escapeHtml(log.id)}</title>
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
                    <p>${this.escapeHtml(hubLocation)} | Phone: 9391915013, 9959859347</p>
                    <h3>DRIVER DUTY SETTLEMENT RECEIPT (DEMO)</h3>
                </div>
                <div class="row"><span>Receipt ID:</span><span>#${this.escapeHtml(log.id)}</span></div>
                <div class="row"><span>Date & Time:</span><span>${this.escapeHtml(log.loginDate)} (${log.shiftType === "12_HOURS" ? "12h Shift" : "24h Shift"})</span></div>
                <div class="row"><span>Driver:</span><span><strong>${this.escapeHtml(log.driverName)}</strong> (${this.escapeHtml(log.driverPhone || "N/A")})</span></div>
                <div class="row"><span>Vehicle Reg No:</span><span><strong>${this.escapeHtml(log.vehicleNo)}</strong></span></div>
                <div class="row"><span>Trips Completed:</span><span><strong>${Number(log.trips) || 0}</strong></span></div>
                <hr style="border: 0; border-top: 1px dashed #aaa; margin: 10px 0;" />
                <div class="row"><span>Gross Earnings:</span><span>₹${Number(log.earnings) || 0}</span></div>
                <div class="row"><span>Commission Tier:</span><span>${Number(log.commissionPercentage) || 0}%</span></div>
                <div class="row"><span>Commission Amount:</span><span>₹${Number(log.commissionRent) || 0}</span></div>
                <div class="row"><span>Fuel & Daily Expenditure:</span><span>₹${Number(log.expenditure) || 0}</span></div>
                <div class="row"><span>Cash Collected from Riders:</span><span>₹${Number(log.cashCollection) || 0}</span></div>
                <div class="total row">
                    <span>NET BALANCE:</span>
                    <span>₹${log.netBalance >= 0 ? "+" : ""}${Number(log.netBalance) || 0} (${log.netBalance >= 0 ? "Company Pays Driver" : "Driver Deposited to Office"})</span>
                </div>
                <div class="footer">
                    <p>Remarks: ${this.escapeHtml(log.remarks || "Standard shift completion.")}</p>
                    <p>Verified by SVM FLEET Hub Officer (${this.escapeHtml(this.getHubLabel(log.managerId))})</p>
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

    // ------------------------------------------------------------------------
    // Tab Navigation & Reports (Strict RBAC + Mobile Drawer/Bottom Nav Sync)
    // ------------------------------------------------------------------------
    switchTab(tabId) {
        if (tabId === "settings" && (!this.auth || !this.auth.isOwner())) {
            this.showToast("Access denied: Settings is restricted to Fleet Owner.", "alert");
            return;
        }

        this.activeTab = tabId;
        this.closeMobileSidebar();

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

        document.querySelectorAll(".mobile-nav-btn").forEach(btn => {
            const btnTab = btn.getAttribute("data-nav-tab") || btn.getAttribute("data-mobile-tab");
            if (!btnTab) return;
            const isCurrent = btnTab === tabId;
            btn.classList.toggle("active", isCurrent);
            btn.setAttribute("aria-current", isCurrent ? "page" : "false");
        });

        document.querySelectorAll("[data-tab-content]").forEach(content => {
            content.classList.add("hidden");
        });
        const activeContent = document.querySelector(`[data-tab-content="${tabId}"]`);
        if (activeContent) {
            activeContent.classList.remove("hidden");
        }

        if (!this.auth || !this.auth.isAuthenticated()) {
            return;
        }

        const filteredVehicles = this.getCurrentFilteredVehicles();
        if (tabId === "map") {
            if (!this.fullMapController && typeof HyderabadMapController !== "undefined") {
                this.fullMapController = new HyderabadMapController("hyderabad-map-full");
                this.fullMapController.init();
            }
            if (this.fullMapController) {
                this.fullMapController.updateVehicles(filteredVehicles);
                this.fullMapController.focusHub(this.currentHubId);
                this.fullMapController.invalidateSize();
            }
            this.updateMapLegends(filteredVehicles);
        } else if (tabId === "overview") {
            if (this.mapController) {
                this.mapController.updateVehicles(filteredVehicles);
                this.mapController.invalidateSize();
            }
        } else if (tabId === "reports") {
            this.renderReportsTab();
        }
    }

    setReportPeriod(period) {
        this.reportPeriod = period;
        ["today", "week", "month", "all"].forEach(p => {
            const btn = document.getElementById(`btn-rep-${p}`);
            if (btn) {
                if (p === period) {
                    btn.className = "rep-period-btn px-3 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white transition-all cursor-pointer shrink-0 whitespace-nowrap";
                } else {
                    btn.className = "rep-period-btn px-3 py-1.5 rounded-lg text-xs font-semibold text-[#94A3B8] hover:text-white transition-all cursor-pointer shrink-0 whitespace-nowrap";
                }
            }
        });

        const badge = document.getElementById("report-period-badge");
        if (badge) {
            badge.innerText = period === "today" ? "Daily View (Today)"
                : period === "week" ? "Weekly Fleet Report"
                : period === "month" ? "Monthly Report (MTD)"
                : "All Time Audit";
        }

        this.renderReportsTab();
        this.showToast(`Reports updated for ${period.toUpperCase()} period`, "info");
    }

    renderReportsTab() {
        const allowedHubId = this.getScopedHubId();
        const canSeeProfit = this.canViewNetProfit();

        const grossEl = document.getElementById("report-kpi-gross");
        const commEl = document.getElementById("report-kpi-comm");
        const commRateEl = document.getElementById("report-kpi-comm-rate");
        const fuelEl = document.getElementById("report-kpi-fuel");
        const profitEl = document.getElementById("report-kpi-profit");
        const marginEl = document.getElementById("report-kpi-margin");
        const shiftsEl = document.getElementById("report-kpi-shifts");
        const tripsEl = document.getElementById("report-kpi-trips");
        const profitCard = document.getElementById("report-card-profit") || (profitEl ? profitEl.closest(".rounded-xl") : null);
        const kpiGrid = document.getElementById("reports-kpi-grid") || (grossEl ? grossEl.closest(".grid") : null);
        const subtitleEl = document.getElementById("report-subtitle");
        const tbody = document.getElementById("report-table-body");
        const reportCards = document.getElementById("report-cards-container");

        this._syncKpiGridColumns(kpiGrid, canSeeProfit, profitCard);

        if (profitCard) {
            profitCard.classList.toggle("hidden", !canSeeProfit);
            profitCard.setAttribute("aria-hidden", String(!canSeeProfit));
        }

        if (subtitleEl) {
            subtitleEl.innerText = canSeeProfit
                ? "Aggregated gross revenues, driver commission disbursements, fuel vouchers, and profit margins across Hyderabad depots."
                : `Aggregated gross revenues, driver commission disbursements, and fuel vouchers for ${this.getActiveHubName()} Hub.`;
        }

        if (!canSeeProfit) {
            if (profitEl) profitEl.innerText = "";
            if (marginEl) marginEl.innerText = "";
        }

        if (!allowedHubId || !window.DEMO_REPO) {
            if (tbody) tbody.innerHTML = "";
            if (reportCards) reportCards.innerHTML = "";
            return;
        }

        const allLogs = window.DEMO_REPO.getDutyLogs(allowedHubId);
        let periodLogs = allLogs;

        if (this.reportPeriod === "today") {
            const todayStr = new Date().toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
            periodLogs = allLogs.filter(l => (l.loginDate && l.loginDate.includes("09 Nov 2026")) || l.loginDate === todayStr);
        } else if (this.reportPeriod === "week") {
            const weekDates = ["03 Nov", "04 Nov", "05 Nov", "06 Nov", "07 Nov", "08 Nov", "09 Nov"];
            periodLogs = allLogs.filter(l => l.loginDate && weekDates.some(d => l.loginDate.includes(d)));
        } else if (this.reportPeriod === "month") {
            periodLogs = allLogs.filter(l => l.loginDate && l.loginDate.includes("Nov 2026"));
        }

        let totalGross = 0;
        let totalComm = 0;
        let totalFuel = 0;
        let totalTrips = 0;

        periodLogs.forEach(l => {
            totalGross += (Number(l.earnings) || 0);
            totalComm += (Number(l.commissionRent) || 0);
            totalFuel += (Number(l.expenditure) || 0);
            totalTrips += (Number(l.trips) || 0);
        });

        const avgCommRate = totalGross > 0 ? ((totalComm / totalGross) * 100).toFixed(1) : "0.0";

        if (grossEl) grossEl.innerText = `₹${Math.round(totalGross).toLocaleString("en-IN")}`;
        if (commEl) commEl.innerText = `₹${Math.round(totalComm).toLocaleString("en-IN")}`;
        if (commRateEl) commRateEl.innerText = `${avgCommRate}% Avg Slab`;
        if (fuelEl) fuelEl.innerText = `₹${Math.round(totalFuel).toLocaleString("en-IN")}`;
        if (shiftsEl) shiftsEl.innerText = `${periodLogs.length} Shifts`;
        if (tripsEl) tripsEl.innerText = `${totalTrips.toLocaleString("en-IN")} Completed Trips`;

        if (canSeeProfit) {
            const netProfit = totalGross - totalComm - totalFuel;
            const netMargin = totalGross > 0 ? ((netProfit / totalGross) * 100).toFixed(1) : "0.0";
            if (profitEl) profitEl.innerText = `₹${Math.round(netProfit).toLocaleString("en-IN")}`;
            if (marginEl) marginEl.innerText = `${netMargin}% Net Margin`;
        }

        const vehicleModelMap = this._getVehicleModelMap();
        const resolveVehicleModel = (log) => {
            if (log.vehicleModel || log.model) return log.vehicleModel || log.model;
            const regKey = String(log.vehicleNo || "").trim().toUpperCase();
            return vehicleModelMap.get(regKey) || "";
        };

        if (reportCards) {
            if (periodLogs.length === 0) {
                reportCards.innerHTML = `
                    <div class="col-span-full py-8 text-center text-xs text-slate-500 saas-card">
                        No shift records found for this period.
                    </div>
                `;
            } else {
                reportCards.innerHTML = periodLogs.map(log =>
                    this._renderMobileShiftCardHtml(log, {
                        thirdLabel: "Fuel",
                        thirdValue: `₹${Math.round(log.expenditure || 0).toLocaleString("en-IN")}`,
                        thirdClass: "text-[#0EA5E9]",
                        showEditButton: false,
                        vehicleModel: resolveVehicleModel(log)
                    })
                ).join("");
            }
        }

        if (tbody) {
            if (periodLogs.length === 0) {
                tbody.innerHTML = `<tr><td colspan="11" class="text-center py-6 text-slate-500 font-sans">No shift records found for this period.</td></tr>`;
            } else {
                tbody.innerHTML = periodLogs.map((log, idx) => {
                    const isSettled = log.status === "SETTLED";
                    const safeLogId = this.escapeHtml(log.id);
                    return `
                        <tr class="hover:bg-[#11182A]/70 transition-colors">
                            <td class="py-2.5 px-3 text-slate-500">${idx + 1}</td>
                            <td class="py-2.5 px-3 text-white font-mono">${this.escapeHtml(log.loginDate)}</td>
                            <td class="py-2.5 px-3">${this.renderTruncatedNameHtml(log.driverName, 16, "font-bold text-white")}</td>
                            <td class="py-2.5 px-3 text-slate-300 font-mono">${this.escapeHtml(log.vehicleNo)}</td>
                            <td class="py-2.5 px-3 text-center text-white font-bold">${Number(log.trips) || 0}</td>
                            <td class="py-2.5 px-3 text-right font-bold text-[#10B981]">₹${Math.round(log.earnings || 0).toLocaleString("en-IN")}</td>
                            <td class="py-2.5 px-3 text-right text-[#0EA5E9]">₹${Math.round(log.expenditure || 0).toLocaleString("en-IN")}</td>
                            <td class="py-2.5 px-3 text-right text-[#6655F5] font-bold">₹${Math.round(log.commissionRent || 0).toLocaleString("en-IN")}</td>
                            <td class="py-2.5 px-3 text-right text-[#F59E0B]">₹${Math.round(log.cashCollection || 0).toLocaleString("en-IN")}</td>
                            <td class="py-2.5 px-3 text-center">
                                <button type="button" onclick="window.SVM_APP.openEditSettlementModal('${safeLogId}')" class="px-2 py-0.5 rounded-full text-[10px] font-bold ${isSettled ? 'bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30' : 'bg-[#F59E0B]/20 text-[#F59E0B] border border-[#F59E0B]/30'} cursor-pointer" title="Click to edit settlement">
                                    ${isSettled ? '✓ Settled' : '⏳ Pending'}
                                </button>
                            </td>
                            <td class="py-2.5 px-3 text-right">
                                <button type="button" onclick="window.SVM_APP.printReceipt('${safeLogId}')" class="text-slate-400 hover:text-white p-1 rounded hover:bg-[#1E273D]" title="Print Receipt">
                                    <i data-lucide="printer" class="w-3.5 h-3.5"></i>
                                </button>
                            </td>
                        </tr>
                    `;
                }).join("");
            }
        }
        if (window.lucide) window.lucide.createIcons();
    }

    // ------------------------------------------------------------------------
    // Edit Shift Settlement Modal (Strictly Hub-Scoped)
    // ------------------------------------------------------------------------
    openEditSettlementModal(logId) {
        const allowedHubId = this.getScopedHubId();
        if (!allowedHubId || !window.DEMO_REPO) return;

        const logs = window.DEMO_REPO.getDutyLogs(allowedHubId);
        const log = logs.find(l => String(l.id) === String(logId));
        if (!log) {
            this.showToast("Access denied: Shift record belongs to another hub.", "alert");
            return;
        }

        const modal = document.getElementById("edit-settlement-modal");
        if (!modal) return;

        const idEl = document.getElementById("edit-settle-id");
        const driverEl = document.getElementById("edit-settle-driver-name");
        const vehEl = document.getElementById("edit-settle-veh-plate");
        const dateEl = document.getElementById("edit-settle-date-badge");
        const shiftTypeEl = document.getElementById("edit-settle-shifttype");
        const statusEl = document.getElementById("edit-settle-status");
        const tripsEl = document.getElementById("edit-settle-trips");
        const grossEl = document.getElementById("edit-settle-gross");
        const fuelEl = document.getElementById("edit-settle-fuel");
        const cashEl = document.getElementById("edit-settle-cash");
        const advEl = document.getElementById("edit-settle-advance");
        const remarksEl = document.getElementById("edit-settle-remarks");

        if (idEl) idEl.value = log.id;
        if (driverEl) driverEl.innerText = `${log.driverName} (${log.driverPhone || "N/A"})`;
        if (vehEl) vehEl.innerText = log.vehicleNo;
        if (dateEl) dateEl.innerText = `${log.loginDate} • ${log.source || "Uber"}`;

        if (shiftTypeEl) shiftTypeEl.value = log.shiftType || "12_HOURS";
        if (statusEl) statusEl.value = log.status || "SETTLED";
        if (tripsEl) tripsEl.value = log.trips ?? 12;
        if (grossEl) grossEl.value = log.earnings ?? 3000;
        if (fuelEl) fuelEl.value = log.expenditure ?? 200;
        if (cashEl) cashEl.value = log.cashCollection ?? 2000;
        if (advEl) advEl.value = log.advance ?? 0;
        if (remarksEl) remarksEl.value = log.remarks || "Reconciled at depot counter.";

        modal.querySelectorAll('input[type="number"]').forEach(input => {
            input.addEventListener("wheel", (e) => e.preventDefault(), { passive: false });
        });

        this.updateEditModalCalculation();

        modal.classList.add("open");
        modal.scrollTop = 0;
        document.body.classList.add("modal-open");
    }

    closeEditSettlementModal() {
        const modal = document.getElementById("edit-settlement-modal");
        if (modal) modal.classList.remove("open");
        document.body.classList.remove("modal-open");
    }

    updateEditModalCalculation() {
        try {
            const shiftType = document.getElementById("edit-settle-shifttype")?.value || "12_HOURS";
            const trips = parseInt(document.getElementById("edit-settle-trips")?.value, 10) || 0;
            const gross = parseFloat(document.getElementById("edit-settle-gross")?.value) || 0;
            const cash = parseFloat(document.getElementById("edit-settle-cash")?.value) || 0;
            const exp = parseFloat(document.getElementById("edit-settle-fuel")?.value) || 0;
            const advance = parseFloat(document.getElementById("edit-settle-advance")?.value) || 0;

            const result = SvmCommissionCalculator.calculate(shiftType, trips, gross, cash, exp, 0, advance);

            const badgeEl = document.getElementById("edit-calc-trips-badge");
            if (badgeEl) {
                badgeEl.innerText = result.minTripsMet ? `✓ Target Met (${trips} trips)` : `⚠ Below Minimum (${trips}/${shiftType === "12_HOURS" ? 10 : 18} trips)`;
                badgeEl.className = result.minTripsMet ? "text-[#10B981] font-bold text-xs" : "text-[#F59E0B] font-bold text-xs";
            }

            const tierEl = document.getElementById("edit-calc-tier");
            if (tierEl) tierEl.innerText = result.commissionPercentage > 0 ? `${result.commissionPercentage}% Commission Tier` : "0% (Flat Vehicle Rent)";

            const commAmtEl = document.getElementById("edit-calc-comm-amt");
            if (commAmtEl) commAmtEl.innerText = `₹${Math.round(result.commissionAmount)}`;

            const credEl = document.getElementById("edit-calc-credit");
            if (credEl) credEl.innerText = `₹${Math.round(result.driverCredit)} (Comm: ₹${Math.round(result.commissionAmount)} + Fuel: ₹${Math.round(exp)})`;

            const debEl = document.getElementById("edit-calc-debit");
            if (debEl) debEl.innerText = `₹${Math.round(result.driverDebit)} (Cash: ₹${Math.round(cash)} + Adv: ₹${Math.round(advance)})`;

            const netEl = document.getElementById("edit-calc-net");
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
            console.warn("Edit modal calculation preview error:", e);
        }
    }

    submitEditSettlementForm(e) {
        e.preventDefault();
        const allowedHubId = this.getScopedHubId();
        if (!allowedHubId || !window.DEMO_REPO) return;

        const logId = document.getElementById("edit-settle-id").value;
        const allowedLogs = window.DEMO_REPO.getDutyLogs(allowedHubId);
        if (!allowedLogs.some(l => String(l.id) === String(logId))) {
            this.showToast("Access denied: Cannot edit shifts from another hub.", "alert");
            return;
        }

        const shiftType = document.getElementById("edit-settle-shifttype").value;
        const status = document.getElementById("edit-settle-status").value;
        const trips = parseInt(document.getElementById("edit-settle-trips").value, 10) || 0;
        const gross = parseFloat(document.getElementById("edit-settle-gross").value) || 0;
        const exp = parseFloat(document.getElementById("edit-settle-fuel").value) || 0;
        const cash = parseFloat(document.getElementById("edit-settle-cash").value) || 0;
        const advance = parseFloat(document.getElementById("edit-settle-advance").value) || 0;
        const paymentMode = document.getElementById("edit-settle-payment-mode").value;
        const remarks = document.getElementById("edit-settle-remarks").value.trim();

        const calc = SvmCommissionCalculator.calculate(shiftType, trips, gross, cash, exp, 0, advance);

        const updatedFields = {
            shiftType,
            status,
            trips,
            earnings: gross,
            expenditure: exp,
            cashCollection: cash,
            advance,
            commissionRent: calc.commissionAmount,
            commissionPercentage: calc.commissionPercentage,
            netBalance: calc.netBalance,
            remarks: remarks || `${status === "SETTLED" ? "Settled via " + paymentMode : "Pending reconciliation"}`,
            paymentMode
        };

        window.DEMO_REPO.updateDutyLog(logId, updatedFields);

        this.closeEditSettlementModal();
        this.renderDutyRegister();
        this.renderReportsTab();
        this.renderMetricCards();
        this.showToast(`Shift settlement #${logId} updated to ${status}!`, "success");
    }

    // ------------------------------------------------------------------------
    // Scoped Notifications, User Guide & Mobile-Safe Toast
    // ------------------------------------------------------------------------
    getScopedNotifications() {
        const allowedHubId = this.getScopedHubId();
        if (!allowedHubId) return [];
        if (allowedHubId === "ALL") return this.notifications;
        return this.notifications.filter(n => n.hubId === allowedHubId);
    }

    renderNotifications() {
        const scoped = this.getScopedNotifications();
        const unreadCount = scoped.filter(n => n.unread).length;
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
            if (scoped.length === 0) {
                container.innerHTML = `
                    <div class="p-4 text-center text-xs text-slate-400">
                        No alerts for ${this.escapeHtml(this.getActiveHubName())} Hub.
                    </div>
                `;
            } else {
                container.innerHTML = scoped.map(n => `
                    <div class="p-3.5 hover:bg-[#161F33] transition-colors flex items-start gap-3 ${n.unread ? 'bg-[#0E1528]/80' : 'opacity-70'}">
                        <span class="w-2 h-2 mt-1.5 rounded-full shrink-0 ${n.unread ? (n.type === 'success' ? 'bg-[#10B981]' : n.type === 'warning' ? 'bg-[#F59E0B]' : 'bg-[#6655F5]') : 'bg-transparent'}"></span>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center justify-between gap-1 mb-0.5">
                                <span class="font-bold text-xs text-white truncate">${this.escapeHtml(n.title)}</span>
                                <span class="text-[10px] text-slate-500 font-mono shrink-0">${this.escapeHtml(n.time)}</span>
                            </div>
                            <p class="text-[11px] text-slate-300 leading-relaxed">${this.escapeHtml(n.text)}</p>
                        </div>
                    </div>
                `).join("");
            }
        }
    }

    toggleNotifications() {
        const dropdown = document.getElementById("notification-dropdown");
        if (dropdown) {
            dropdown.classList.toggle("open");
        }
    }

    markAllNotificationsRead() {
        const scoped = this.getScopedNotifications();
        scoped.forEach(n => { n.unread = false; });
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
        if (modal) modal.classList.remove("open");
        document.body.classList.remove("modal-open");
    }

    switchGuideTab(tabId) {
        this.activeGuideTab = tabId;
        document.querySelectorAll(".guide-tab-btn").forEach(btn => {
            btn.className = "guide-tab-btn px-3 py-1.5 rounded-lg text-xs font-semibold text-[#94A3B8] hover:text-white transition-all shrink-0 whitespace-nowrap";
        });
        const activeBtn = document.getElementById(`tab-btn-${tabId}`);
        if (activeBtn) {
            activeBtn.className = "guide-tab-btn px-3 py-1.5 rounded-lg text-xs font-bold bg-[#6655F5] text-white transition-all shrink-0 whitespace-nowrap";
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
        const existing = document.getElementById("svm-active-toast");
        if (existing) existing.remove();
        if (this._toastTimeout) clearTimeout(this._toastTimeout);

        const toast = document.createElement("div");
        toast.id = "svm-active-toast";
        const bgColor = type === "success" ? "bg-[#10B981]" : type === "alert" ? "bg-red-600" : "bg-[#6655F5]";
        toast.className = `fixed bottom-20 md:bottom-6 left-4 right-4 sm:left-auto sm:right-6 max-w-sm ${bgColor} text-white px-4 py-3 rounded-xl shadow-2xl z-[1000010] text-xs font-bold flex items-center gap-2 border border-white/20`;

        const iconSpan = document.createElement("span");
        iconSpan.textContent = type === "alert" ? "!" : "✓";
        const msgSpan = document.createElement("span");
        msgSpan.textContent = String(message || "");

        toast.appendChild(iconSpan);
        toast.appendChild(msgSpan);
        document.body.appendChild(toast);

        this._toastTimeout = setTimeout(() => {
            if (toast.parentNode) toast.remove();
        }, 3200);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    window.SVM_APP = new SvmFleetApp();
    window.SVM_APP.init();
});
