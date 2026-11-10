/**
 * SVM FLEET Operating System (Web Portal)
 * Authentication, Master Secret Key Management & Strict Role-Based Access Control (RBAC)
 * Zero-dependency browser module ensuring Hub Managers never view Net Profit margins.
 */

class SvmWebAuth {
    constructor() {
        this.SESSION_STORAGE_KEY = "SVM_WEB_SESSION_V1";
        this.OWNER_SECRET_STORAGE_KEY = "svm_owner_secret_key";
        this.DEFAULT_OWNER_SECRET_KEY = "7777";
        this.OWNER_PHONES = ["9391915013", "9959859347", "9876543210"];

        this.session = this.loadSession();

        if (typeof document !== "undefined") {
            if (document.body) {
                this.applyPermissionsToDOM();
            } else if (typeof document.addEventListener === "function") {
                document.addEventListener("DOMContentLoaded", () => {
                    this.applyPermissionsToDOM();
                });
            }
        }
    }

    /**
     * Safely retrieves browser localStorage when available.
     * @returns {Storage|null}
     */
    _getStorage() {
        try {
            if (typeof localStorage !== "undefined") {
                return localStorage;
            }
        } catch (e) {
            console.warn("localStorage is unavailable:", e);
        }
        return null;
    }

    /**
     * Normalizes a phone number to its trailing 10 digits.
     * @param {string|number} phone
     * @returns {string}
     */
    cleanPhone(phone) {
        if (phone === null || phone === undefined) return "";
        return String(phone).replace(/\D/g, "").slice(-10);
    }

    /**
     * Checks whether a phone number matches a pre-authorized Owner number.
     * @param {string|number} phone
     * @returns {boolean}
     */
    isAuthorizedOwnerPhone(phone) {
        const cleaned = this.cleanPhone(phone);
        return cleaned.length === 10 && this.OWNER_PHONES.includes(cleaned);
    }

    /**
     * Dynamically retrieves hubs/managers from DEMO_REPO with APP_CONFIG fallback.
     * @returns {Array<Object>}
     */
    getHubs() {
        const demoHubs =
            typeof window !== "undefined" &&
            window.DEMO_REPO &&
            typeof window.DEMO_REPO.getHubs === "function"
                ? window.DEMO_REPO.getHubs()
                : [];

        if (Array.isArray(demoHubs) && demoHubs.length > 0) {
            return demoHubs;
        }

        const configHubs =
            typeof window !== "undefined" &&
            window.APP_CONFIG &&
            Array.isArray(window.APP_CONFIG.hubs)
                ? window.APP_CONFIG.hubs
                : [];

        return configHubs;
    }

    /**
     * Validates a candidate session object and enforces strict RBAC invariants.
     * @param {any} candidate
     * @returns {Object|null}
     */
    _validateSession(candidate) {
        if (!candidate || typeof candidate !== "object") return null;
        if (candidate.isAuthenticated !== true) return null;

        const role = candidate.role;
        if (role !== "OWNER" && role !== "MANAGER") return null;

        if (role === "MANAGER") {
            const hubId = typeof candidate.hubId === "string" ? candidate.hubId.trim() : "";
            // A Manager session must ALWAYS be scoped to a concrete hub, never "ALL".
            if (!hubId || hubId === "ALL") return null;

            return {
                isAuthenticated: true,
                role: "MANAGER",
                hubId,
                userName: typeof candidate.userName === "string" ? candidate.userName : "Hub Manager",
                phone: typeof candidate.phone === "string" ? candidate.phone : "",
                isDemo: Boolean(candidate.isDemo),
                loginTime: typeof candidate.loginTime === "string" ? candidate.loginTime : ""
            };
        }

        const ownerHubId =
            typeof candidate.hubId === "string" && candidate.hubId.trim()
                ? candidate.hubId.trim()
                : "ALL";

        return {
            isAuthenticated: true,
            role: "OWNER",
            hubId: ownerHubId,
            userName:
                typeof candidate.userName === "string"
                    ? candidate.userName
                    : "Akhilesh (Fleet Owner)",
            phone:
                typeof candidate.phone === "string" && candidate.phone.trim()
                    ? candidate.phone
                    : this.OWNER_PHONES[0],
            isDemo: Boolean(candidate.isDemo),
            loginTime: typeof candidate.loginTime === "string" ? candidate.loginTime : ""
        };
    }

    /**
     * Persists a validated session and synchronizes legacy role/hub storage keys.
     * @param {Object} session
     */
    _saveSession(session) {
        this.session = session;
        const storage = this._getStorage();
        if (!storage) return;

        try {
            storage.setItem(this.SESSION_STORAGE_KEY, JSON.stringify(session));
            storage.setItem("SVM_ROLE", session.role);
            storage.setItem("SVM_HUB_ID", session.hubId);
        } catch (e) {
            console.warn("Failed to persist SVM web session:", e);
        }
    }

    /**
     * Loads and validates the active session from localStorage.
     * @returns {Object|null}
     */
    loadSession() {
        const storage = this._getStorage();
        if (!storage) {
            this.session = null;
            return null;
        }

        try {
            const raw = storage.getItem(this.SESSION_STORAGE_KEY);
            if (!raw) {
                this.session = null;
                return null;
            }

            const parsed = JSON.parse(raw);
            const validated = this._validateSession(parsed);
            if (!validated) {
                storage.removeItem(this.SESSION_STORAGE_KEY);
                this.session = null;
                return null;
            }

            this.session = validated;
            // Keep legacy keys aligned with the authoritative session so reloads cannot escalate role.
            storage.setItem("SVM_ROLE", validated.role);
            storage.setItem("SVM_HUB_ID", validated.hubId);
            return this.session;
        } catch (e) {
            console.warn("Could not parse stored SVM auth session:", e);
            this.session = null;
            return null;
        }
    }

    /**
     * Returns a shallow copy of the current authenticated session, or null.
     * @returns {Object|null}
     */
    getSession() {
        return this.session ? { ...this.session } : null;
    }

    /**
     * Returns true when a valid Owner or Manager session is active.
     * @returns {boolean}
     */
    isAuthenticated() {
        return Boolean(
            this.session &&
            this.session.isAuthenticated === true &&
            (this.session.role === "OWNER" || this.session.role === "MANAGER")
        );
    }

    /**
     * Returns the current Owner Master Secret Key (defaults to "7777").
     * @returns {string}
     */
    getOwnerSecretKey() {
        const storage = this._getStorage();
        if (storage) {
            try {
                const stored = storage.getItem(this.OWNER_SECRET_STORAGE_KEY);
                if (stored && stored.trim().length > 0) {
                    return stored.trim();
                }
            } catch (e) {
                console.warn("Could not read owner secret key:", e);
            }
        }
        return this.DEFAULT_OWNER_SECRET_KEY;
    }

    /**
     * Rotates the Owner Master Secret Key. Strictly restricted to authenticated Fleet Owners.
     * @param {string} currentKey
     * @param {string} newKey
     * @returns {{ ok: boolean, error?: string }}
     */
    changeOwnerSecretKey(currentKey, newKey) {
        if (!this.isOwner()) {
            return {
                ok: false,
                error: "Access denied. Only the Fleet Owner can change the Master Secret Key."
            };
        }

        const cleanCurrent =
            currentKey === null || currentKey === undefined ? "" : String(currentKey).trim();
        const cleanNew = newKey === null || newKey === undefined ? "" : String(newKey).trim();

        if (cleanCurrent !== this.getOwnerSecretKey()) {
            return { ok: false, error: "Current Secret Key is incorrect." };
        }

        if (cleanNew.length < 4) {
            return { ok: false, error: "New Secret Key must be at least 4 characters long." };
        }

        const storage = this._getStorage();
        if (!storage) {
            return { ok: false, error: "Local storage is unavailable to save the new key." };
        }

        try {
            storage.setItem(this.OWNER_SECRET_STORAGE_KEY, cleanNew);
            return { ok: true };
        } catch (e) {
            return { ok: false, error: "Failed to save the new Owner Secret Key." };
        }
    }

    /**
     * Authenticates a Fleet Owner using phone + Secret Key (or 1-Tap Demo mode).
     * @param {{ phone?: string, secretKey?: string, isDemo?: boolean }} credentials
     * @returns {{ ok: boolean, session?: Object, error?: string }}
     */
    loginAsOwner(credentials = {}) {
        const { phone = "", secretKey = "", isDemo = false } = credentials || {};
        const rawPhone = phone === null || phone === undefined ? "" : String(phone).trim();
        const cleanedPhone = this.cleanPhone(rawPhone);
        const cleanedKey =
            secretKey === null || secretKey === undefined ? "" : String(secretKey).trim();
        const demoMode = Boolean(isDemo);

        if (!demoMode && cleanedKey !== this.getOwnerSecretKey()) {
            return { ok: false, error: "Invalid Owner Secret Key." };
        }

        if (rawPhone.length > 0 && cleanedPhone.length < 10) {
            return { ok: false, error: "Please enter a valid 10-digit mobile number." };
        }

        const resolvedPhone = cleanedPhone || this.OWNER_PHONES[0];
        const session = {
            isAuthenticated: true,
            role: "OWNER",
            hubId: "ALL",
            userName: "Akhilesh (Fleet Owner)",
            phone: resolvedPhone,
            isDemo: demoMode,
            loginTime: new Date().toISOString()
        };

        this._saveSession(session);
        this.applyPermissionsToDOM();
        return { ok: true, session: this.getSession() };
    }

    /**
     * Authenticates a Hub Manager using hubId or 10-digit phone + PIN (or 1-Tap Demo mode).
     * @param {{ hubId?: string, phone?: string, pin?: string, isDemo?: boolean }} credentials
     * @returns {{ ok: boolean, session?: Object, error?: string }}
     */
    loginAsManager(credentials = {}) {
        const { hubId = "", phone = "", pin = "", isDemo = false } = credentials || {};
        const requestedHubId =
            hubId === null || hubId === undefined ? "" : String(hubId).trim();
        const rawPhone = phone === null || phone === undefined ? "" : String(phone).trim();
        const cleanedPhone = this.cleanPhone(rawPhone);
        const cleanedPin = pin === null || pin === undefined ? "" : String(pin).trim();
        const demoMode = Boolean(isDemo);

        const hubs = this.getHubs();
        if (!Array.isArray(hubs) || hubs.length === 0) {
            return { ok: false, error: "Invalid Manager credentials or PIN." };
        }

        if (rawPhone.length > 0 && cleanedPhone.length < 10) {
            return { ok: false, error: "Invalid Manager credentials or PIN." };
        }

        let matchedHub = null;
        if (requestedHubId && requestedHubId !== "ALL") {
            matchedHub = hubs.find(h => h && String(h.id) === requestedHubId) || null;
            // If both hubId and phone were provided, require them to refer to the same hub.
            if (
                matchedHub &&
                cleanedPhone.length === 10 &&
                this.cleanPhone(matchedHub.phone) !== cleanedPhone
            ) {
                matchedHub = null;
            }
        } else if (cleanedPhone.length === 10) {
            matchedHub = hubs.find(h => h && this.cleanPhone(h.phone) === cleanedPhone) || null;
        } else if (demoMode && !requestedHubId && rawPhone.length === 0) {
            matchedHub = hubs[0] || null;
        }

        if (!matchedHub || !matchedHub.id) {
            return { ok: false, error: "Invalid Manager credentials or PIN." };
        }

        const expectedPin = String(matchedHub.pin || "1234").trim();
        if (!demoMode && cleanedPin !== expectedPin) {
            return { ok: false, error: "Invalid Manager credentials or PIN." };
        }

        const shortLabel = matchedHub.shortName || matchedHub.name || matchedHub.id;
        const managerLabel = matchedHub.managerName || "Hub Manager";
        const session = {
            isAuthenticated: true,
            role: "MANAGER",
            hubId: String(matchedHub.id),
            userName: `${managerLabel} (${shortLabel} Manager)`,
            phone: String(matchedHub.phone || cleanedPhone || ""),
            isDemo: demoMode,
            loginTime: new Date().toISOString()
        };

        this._saveSession(session);
        this.applyPermissionsToDOM();
        return { ok: true, session: this.getSession() };
    }

    /**
     * Logs out the current user, clears session & legacy role keys, and locks the UI.
     * @param {Function} [callback]
     */
    logout(callback) {
        this.session = null;
        const storage = this._getStorage();
        if (storage) {
            try {
                storage.removeItem(this.SESSION_STORAGE_KEY);
                storage.removeItem("SVM_ROLE");
                storage.removeItem("SVM_HUB_ID");
            } catch (e) {
                console.warn("Failed to clear SVM session from localStorage:", e);
            }
        }

        if (typeof document !== "undefined") {
            const branchSelector = document.getElementById("top-branch-selector");
            if (branchSelector) {
                branchSelector.value = "ALL";
            }
        }

        this.applyPermissionsToDOM();

        if (typeof callback === "function") {
            callback();
        }
    }

    /**
     * @returns {boolean}
     */
    isOwner() {
        return Boolean(
            this.session &&
            this.session.isAuthenticated &&
            this.session.role === "OWNER"
        );
    }

    /**
     * @returns {boolean}
     */
    isManager() {
        return Boolean(
            this.session &&
            this.session.isAuthenticated &&
            this.session.role === "MANAGER"
        );
    }

    /**
     * Strict RBAC check: Hub Managers and logged-out users NEVER see Net Profit margins.
     * @returns {boolean}
     */
    canViewNetProfit() {
        return this.isOwner();
    }

    /**
     * Only Fleet Owners can switch between hubs.
     * @returns {boolean}
     */
    canSwitchHubs() {
        return this.isOwner();
    }

    /**
     * Only Fleet Owners can add, edit, or reassign hubs.
     * @returns {boolean}
     */
    canManageHubs() {
        return this.isOwner();
    }

    /**
     * Enforces hub scoping: Managers are always locked to their assigned hubId.
     * @param {string} [requestedHubId]
     * @returns {string|null}
     */
    getAllowedHubId(requestedHubId) {
        if (this.isManager()) {
            return this.session.hubId;
        }
        if (this.isOwner()) {
            return requestedHubId || "ALL";
        }
        return null;
    }

    /**
     * Applies authentication lock state and strict RBAC visibility rules to the DOM.
     */
    applyPermissionsToDOM() {
        if (typeof document === "undefined" || !document.body) return;

        const authenticated = this.isAuthenticated();
        const owner = this.isOwner();
        const manager = this.isManager();
        const loginScreen = document.getElementById("auth-login-screen");

        if (!authenticated) {
            document.body.classList.add("auth-locked");
            document.body.classList.remove("role-owner", "role-manager");
            if (loginScreen) {
                loginScreen.classList.remove("hidden");
                loginScreen.setAttribute("aria-hidden", "false");
            }
        } else {
            document.body.classList.remove("auth-locked");
            document.body.classList.toggle("role-owner", owner);
            document.body.classList.toggle("role-manager", manager);
            if (loginScreen) {
                loginScreen.classList.add("hidden");
                loginScreen.setAttribute("aria-hidden", "true");
            }
        }

        const hideOwnerOnly = !owner;
        document.querySelectorAll("[data-owner-only]").forEach(el => {
            el.classList.toggle("hidden", hideOwnerOnly);
            el.setAttribute("aria-hidden", String(hideOwnerOnly));
        });

        ["dashboard-kpi-grid", "reports-kpi-grid"].forEach(gridId => {
            const grid = document.getElementById(gridId);
            if (!grid) return;
            grid.classList.toggle("lg:grid-cols-5", owner);
            grid.classList.toggle("lg:grid-cols-4", !owner);
        });

        const branchSelector = document.getElementById("top-branch-selector");
        if (branchSelector) {
            const canSwitch = this.canSwitchHubs();
            branchSelector.disabled = !canSwitch;
            branchSelector.classList.toggle("opacity-75", !canSwitch);
            branchSelector.classList.toggle("cursor-not-allowed", !canSwitch);
            branchSelector.classList.toggle("cursor-pointer", canSwitch);
            if (manager && this.session && this.session.hubId) {
                branchSelector.value = this.session.hubId;
            }
        }
    }
}

if (typeof window !== "undefined") {
    window.SvmWebAuth = SvmWebAuth;
    window.SVM_AUTH = new SvmWebAuth();
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = SvmWebAuth;
}
