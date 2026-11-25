/**
 * SVM FLEET - Interactive On-Screen Guided Walkthrough Tour Engine
 * Directly guides users where to click, where to go, and how each feature works.
 */

(function () {
    'use strict';

    class SvmTourEngine {
        constructor() {
            this.steps = [];
            this.currentStep = 0;
            this.isActive = false;
            this.container = null;
            this.spotlightEl = null;
            this.pointerEl = null;
            this.popoverEl = null;
            this.repositionRaf = null;

            this.handleKeyDown = this.handleKeyDown.bind(this);
            this.handleReposition = this.handleReposition.bind(this);

            this.initSteps();
        }

        initSteps() {
            this.steps = [
                {
                    id: 'hub_switch',
                    tab: null,
                    selector: '#top-branch-selector',
                    mobileSelector: '#top-branch-selector',
                    badge: 'DEPOT DISPATCH',
                    title: '1. Switch Operating Depot / Hub',
                    text: 'Click here to switch between Moosapet Central Hub, Kukatpally Y-Junction, Madhapur Hitec City, or view all Hyderabad hubs combined. Every metric, map cab, and trip log filters to the selected hub.',
                    actionLabel: 'Try Moosapet Hub',
                    action: () => {
                        const sel = document.getElementById('top-branch-selector');
                        if (sel) {
                            sel.value = 'mgr_moosapet_01';
                            if (window.SVM_APP && typeof window.SVM_APP.filterHub === 'function') {
                                window.SVM_APP.filterHub('mgr_moosapet_01');
                            }
                        }
                    }
                },
                {
                    id: 'add_shift',
                    tab: 'register',
                    selector: '#register-add-shift-btn',
                    mobileSelector: '#register-add-shift-btn',
                    badge: 'SHIFT ENTRY',
                    title: '2. Record Driver Shift (+ Add Shift)',
                    text: 'Click "+ Add Shift" in the Duty Register whenever a driver finishes duty. Search the driver, pick their assigned vehicle from the daily rotation pool (30 cabs), enter trips completed, fuel spent, and Uber/Rapido earnings. The official commission engine auto-computes tier earnings and settlement vouchers.',
                    actionLabel: 'Open Shift Entry Form',
                    action: () => {
                        if (window.SVM_APP && typeof window.SVM_APP.openManualEntryModal === 'function') {
                            window.SVM_APP.openManualEntryModal();
                        }
                    }
                },
                {
                    id: 'kpi_telemetry',
                    tab: 'overview',
                    selector: '#dashboard-kpi-grid',
                    mobileSelector: '#dashboard-kpi-grid',
                    badge: 'LIVE METRICS',
                    title: '3. Real-Time Fleet KPIs',
                    text: 'Live operational summary: Total Fleet Vehicles (30 Cabs), Active Working Drivers, Total Trips Today, Gross Revenue, and Net Fleet Profit. Numbers update immediately when shifts are submitted.',
                    actionLabel: null,
                    action: null
                },
                {
                    id: 'nav_register',
                    tab: 'overview',
                    selector: '#sidebar-nav button[data-nav-tab="register"]',
                    mobileSelector: '#mobile-bottom-nav button[data-nav-tab="register"]',
                    badge: 'NAVIGATION',
                    title: '4. Go to Trips & Duty Register',
                    text: 'Click "Duty Register" here (or in the bottom navigation on mobile) to view every trip and shift logged across your fleet.',
                    actionLabel: 'Go to Duty Register Now',
                    action: () => {
                        if (window.SVM_APP && typeof window.SVM_APP.switchTab === 'function') {
                            window.SVM_APP.switchTab('register');
                        }
                    }
                },
                {
                    id: 'filter_vehicle',
                    tab: 'register',
                    selector: '#register-vehicle-filter',
                    mobileSelector: '#register-vehicle-filter',
                    badge: 'VEHICLE FILTER',
                    title: '5. Filter Trips by Vehicle / Cab',
                    text: 'Use this dropdown to filter trips for any specific cab (e.g. TS09UA1234). It instantly displays only that vehicle\'s shifts, driver assignments, and fuel costs.',
                    actionLabel: 'Select Cab TS09UA1234',
                    action: () => {
                        const sel = document.getElementById('register-vehicle-filter');
                        if (sel && sel.options.length > 1) {
                            sel.selectedIndex = 1;
                            if (window.SVM_APP && typeof window.SVM_APP.setRegisterVehicleFilter === 'function') {
                                window.SVM_APP.setRegisterVehicleFilter(sel.value);
                            }
                        }
                    }
                },
                {
                    id: 'filter_date',
                    tab: 'register',
                    selector: '#btn-daterange-week',
                    mobileSelector: '#btn-daterange-week',
                    badge: 'DATE FILTER',
                    title: '6. Filter Trips by Date Range',
                    text: 'Click "This Week", "This Month", or "Today" to filter trips by date. You can also pick exact custom dates using the "From" and "To" date pickers.',
                    actionLabel: 'Filter to This Week',
                    action: () => {
                        if (window.SVM_APP && typeof window.SVM_APP.setDateRangeFilter === 'function') {
                            window.SVM_APP.setDateRangeFilter('week');
                        }
                    }
                },
                {
                    id: 'table_view_mode',
                    tab: 'register',
                    selector: '#view-mode-cards-btn',
                    mobileSelector: '#view-mode-cards-btn',
                    badge: 'VIEW TOGGLE',
                    title: '7. Switch Table vs. Mobile Cards',
                    text: 'Switch between the 14-column desktop data table and mobile-optimized swipe cards for fast reading on phones and tablets.',
                    actionLabel: 'Switch to Cards View',
                    action: () => {
                        if (window.SVM_APP && typeof window.SVM_APP.setRegisterViewMode === 'function') {
                            window.SVM_APP.setRegisterViewMode('cards');
                        }
                    }
                },
                {
                    id: 'driver_receipt',
                    tab: 'register',
                    selector: '#register-table-container, #register-cards-container',
                    mobileSelector: '#register-cards-container, #register-table-container',
                    badge: 'RECEIPTS & PAYOUTS',
                    title: '8. Print Driver Settlement Slips',
                    text: 'On any trip entry, click the receipt icon to open and print an official Driver Settlement Voucher for cash handovers or driver WhatsApp sharing.',
                    actionLabel: 'Preview Sample Voucher',
                    action: () => {
                        if (window.SVM_APP && window.SVM_APP.state && window.SVM_APP.state.dutyRegister && window.SVM_APP.state.dutyRegister.length > 0) {
                            window.SVM_APP.openReceiptModal(window.SVM_APP.state.dutyRegister[0].id);
                        }
                    }
                },
                {
                    id: 'nav_map',
                    tab: null,
                    selector: '#sidebar-nav button[data-nav-tab="map"]',
                    mobileSelector: '#mobile-bottom-nav button[data-nav-tab="map"]',
                    badge: 'LIVE GPS MAP',
                    title: '9. Real-Time Vehicle GPS Map',
                    text: 'Click "Live Map" to monitor real-time cab coordinates across Hyderabad. Green markers = Active on duty, Yellow = Idle, Red = Offline / Parked in depot.',
                    actionLabel: 'View Live Map',
                    action: () => {
                        if (window.SVM_APP && typeof window.SVM_APP.switchTab === 'function') {
                            window.SVM_APP.switchTab('map');
                        }
                    }
                },
                {
                    id: 'theme_settings',
                    tab: null,
                    selector: '#theme-toggle-btn',
                    mobileSelector: '#theme-toggle-btn',
                    badge: 'SYSTEM UTILITIES',
                    title: '10. Day / Night Mode & Quick Search',
                    text: 'Click here to switch between Dark and Light mode for day or night depot operations. Press Ctrl + K anywhere to search drivers, cab plates, or shifts instantly.',
                    actionLabel: 'Toggle Theme',
                    action: () => {
                        if (window.SVM_APP && typeof window.SVM_APP.toggleTheme === 'function') {
                            window.SVM_APP.toggleTheme();
                        }
                    }
                }
            ];
        }

        start(startStep = 0) {
            // If already active, end existing
            if (this.isActive) this.end();

            // Close any open modals first so target elements are visible
            if (window.SVM_APP) {
                if (typeof window.SVM_APP.closeUserGuideModal === 'function') window.SVM_APP.closeUserGuideModal();
                if (typeof window.SVM_APP.closeManualEntryModal === 'function') window.SVM_APP.closeManualEntryModal();
                if (typeof window.SVM_APP.closeReceiptModal === 'function') window.SVM_APP.closeReceiptModal();
            }

            this.isActive = true;
            this.currentStep = Math.max(0, Math.min(startStep, this.steps.length - 1));

            this.buildDOMElements();
            this.bindEvents();
            this.renderStep(this.currentStep);
        }

        buildDOMElements() {
            // Remove any old container
            const existing = document.getElementById('svm-tour-root');
            if (existing) existing.remove();

            const root = document.createElement('div');
            root.id = 'svm-tour-root';
            root.className = 'svm-tour-root';
            root.innerHTML = `
                <div id="svm-tour-spotlight" class="svm-tour-spotlight" aria-hidden="true"></div>
                <div id="svm-tour-pointer" class="svm-tour-pointer" aria-hidden="true">
                    <div class="svm-tour-pointer-badge">
                        <span class="svm-tour-pulse-dot"></span>
                        <span class="svm-tour-pointer-text">CLICK HERE</span>
                    </div>
                </div>
                <div id="svm-tour-popover" class="svm-tour-popover" role="dialog" aria-modal="true" aria-label="Guided Tour">
                    <!-- Popover content rendered dynamically -->
                </div>
            `;
            document.body.appendChild(root);

            this.container = root;
            this.spotlightEl = document.getElementById('svm-tour-spotlight');
            this.pointerEl = document.getElementById('svm-tour-pointer');
            this.popoverEl = document.getElementById('svm-tour-popover');
        }

        bindEvents() {
            window.addEventListener('keydown', this.handleKeyDown);
            window.addEventListener('resize', this.handleReposition, { passive: true });
            window.addEventListener('scroll', this.handleReposition, { passive: true, capture: true });
        }

        unbindEvents() {
            window.removeEventListener('keydown', this.handleKeyDown);
            window.removeEventListener('resize', this.handleReposition);
            window.removeEventListener('scroll', this.handleReposition, true);
        }

        handleKeyDown(e) {
            if (!this.isActive) return;
            if (e.key === 'Escape') {
                e.preventDefault();
                this.end();
            } else if (e.key === 'ArrowRight') {
                e.preventDefault();
                this.next();
            } else if (e.key === 'ArrowLeft') {
                e.preventDefault();
                this.prev();
            }
        }

        handleReposition() {
            if (!this.isActive) return;
            if (this.repositionRaf) cancelAnimationFrame(this.repositionRaf);
            this.repositionRaf = requestAnimationFrame(() => {
                this.updatePositions();
            });
        }

        resolveTarget(step) {
            const isMobile = window.innerWidth < 1024;
            if (isMobile && step.mobileSelector) {
                const mobileTarget = document.querySelector(step.mobileSelector);
                if (mobileTarget && mobileTarget.offsetParent !== null) {
                    return mobileTarget;
                }
            }
            return document.querySelector(step.selector);
        }

        async renderStep(stepIndex) {
            if (!this.isActive || stepIndex < 0 || stepIndex >= this.steps.length) return;
            this.currentStep = stepIndex;
            const step = this.steps[stepIndex];

            // 1. If step requires a specific tab, switch to it first
            if (step.tab && window.SVM_APP && typeof window.SVM_APP.switchTab === 'function') {
                if (window.SVM_APP.activeTab !== step.tab) {
                    window.SVM_APP.switchTab(step.tab);
                    await new Promise(r => setTimeout(r, 120));
                }
            }

            // 2. Find target element
            let targetEl = this.resolveTarget(step);
            if (!targetEl) {
                // If not found, wait a tiny bit for dynamic render
                await new Promise(r => setTimeout(r, 100));
                targetEl = this.resolveTarget(step);
            }

            // 3. Scroll target into view
            if (targetEl) {
                targetEl.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'center' });
                await new Promise(r => setTimeout(r, 150));
            }

            // 4. Build popover HTML
            const totalSteps = this.steps.length;
            const progressPercent = Math.round(((stepIndex + 1) / totalSteps) * 100);

            const hasAction = Boolean(step.actionLabel && typeof step.action === 'function');
            const isFirst = stepIndex === 0;
            const isLast = stepIndex === totalSteps - 1;

            this.popoverEl.innerHTML = `
                <div class="svm-tour-card-header">
                    <div class="flex items-center gap-2">
                        <span class="svm-tour-badge">${step.badge || 'FEATURE'}</span>
                        <span class="svm-tour-step-count font-mono text-[11px] text-slate-400">Step ${stepIndex + 1} of ${totalSteps}</span>
                    </div>
                    <button type="button" id="svm-tour-close-btn" class="svm-tour-close-btn" aria-label="Close tour" title="Close tour (Esc)">
                        &times;
                    </button>
                </div>

                <div class="svm-tour-progress-bar-wrap">
                    <div class="svm-tour-progress-bar" style="width: ${progressPercent}%"></div>
                </div>

                <div class="svm-tour-card-body">
                    <h3 class="svm-tour-title">${step.title}</h3>
                    <p class="svm-tour-text">${step.text}</p>
                </div>

                ${hasAction ? `
                    <div class="svm-tour-action-row">
                        <button type="button" id="svm-tour-action-btn" class="svm-tour-action-btn">
                            <span class="svm-tour-btn-icon">&#9658;</span>
                            <span>${step.actionLabel}</span>
                        </button>
                    </div>
                ` : ''}

                <div class="svm-tour-card-footer">
                    <button type="button" id="svm-tour-prev-btn" class="svm-tour-btn-secondary" ${isFirst ? 'disabled' : ''}>
                        &larr; Back
                    </button>
                    <div class="flex items-center gap-2">
                        <button type="button" id="svm-tour-next-btn" class="svm-tour-btn-primary">
                            ${isLast ? 'Finish Tour &#10003;' : 'Next Step &rarr;'}
                        </button>
                    </div>
                </div>
            `;

            // Bind popover buttons
            const closeBtn = document.getElementById('svm-tour-close-btn');
            if (closeBtn) closeBtn.onclick = () => this.end();

            const prevBtn = document.getElementById('svm-tour-prev-btn');
            if (prevBtn) prevBtn.onclick = () => this.prev();

            const nextBtn = document.getElementById('svm-tour-next-btn');
            if (nextBtn) nextBtn.onclick = () => this.next();

            const actionBtn = document.getElementById('svm-tour-action-btn');
            if (actionBtn && step.action) {
                actionBtn.onclick = () => {
                    step.action();
                    // Keep tour positions fresh after action
                    setTimeout(() => this.updatePositions(), 200);
                };
            }

            // 5. Update positions
            this.updatePositions();
        }

        updatePositions() {
            if (!this.isActive) return;
            const step = this.steps[this.currentStep];
            if (!step) return;

            const targetEl = this.resolveTarget(step);
            const pad = 6;
            const vpW = window.innerWidth;
            const vpH = window.innerHeight;

            if (targetEl) {
                const rect = targetEl.getBoundingClientRect();
                const spotTop = Math.max(0, rect.top - pad);
                const spotLeft = Math.max(0, rect.left - pad);
                const spotWidth = Math.min(vpW - spotLeft, rect.width + pad * 2);
                const spotHeight = Math.min(vpH - spotTop, rect.height + pad * 2);

                // Update spotlight box
                this.spotlightEl.style.display = 'block';
                this.spotlightEl.style.top = `${Math.round(spotTop)}px`;
                this.spotlightEl.style.left = `${Math.round(spotLeft)}px`;
                this.spotlightEl.style.width = `${Math.round(spotWidth)}px`;
                this.spotlightEl.style.height = `${Math.round(spotHeight)}px`;

                // Update pointer badge position (prefer top-center of target, else bottom)
                this.pointerEl.style.display = 'flex';
                let pointerTop = spotTop - 34;
                let pointerLeft = spotLeft + (spotWidth / 2) - 46;

                // If clipped at top, place just below
                if (pointerTop < 60) {
                    pointerTop = spotTop + spotHeight + 8;
                }
                pointerLeft = Math.max(8, Math.min(vpW - 100, pointerLeft));

                this.pointerEl.style.top = `${Math.round(pointerTop)}px`;
                this.pointerEl.style.left = `${Math.round(pointerLeft)}px`;

                // Update popover card position
                this.positionPopover(rect);
            } else {
                // Target missing - center popover and hide spotlight
                this.spotlightEl.style.display = 'none';
                this.pointerEl.style.display = 'none';

                const popW = 340;
                const popH = 260;
                this.popoverEl.style.top = `${Math.max(20, Math.round((vpH - popH) / 2))}px`;
                this.popoverEl.style.left = `${Math.max(16, Math.round((vpW - popW) / 2))}px`;
            }
        }

        positionPopover(targetRect) {
            const margin = 14;
            const popRect = this.popoverEl.getBoundingClientRect();
            const popW = popRect.width || 340;
            const popH = popRect.height || 260;
            const vpW = window.innerWidth;
            const vpH = window.innerHeight;

            let top = 0;
            let left = 0;

            const spaceBelow = vpH - targetRect.bottom;
            const spaceAbove = targetRect.top;

            if (spaceBelow >= popH + margin + 8) {
                // Place below target
                top = targetRect.bottom + margin;
            } else if (spaceAbove >= popH + margin + 8) {
                // Place above target
                top = targetRect.top - popH - margin;
            } else {
                // Viewport too tight vertically: pin to bottom
                top = Math.max(margin, vpH - popH - margin);
            }

            // Align horizontal center with target, clamp within screen bounds
            left = targetRect.left + (targetRect.width / 2) - (popW / 2);
            if (left + popW > vpW - margin) {
                left = vpW - popW - margin;
            }
            if (left < margin) {
                left = margin;
            }

            this.popoverEl.style.top = `${Math.round(top)}px`;
            this.popoverEl.style.left = `${Math.round(left)}px`;
        }

        next() {
            if (this.currentStep < this.steps.length - 1) {
                this.renderStep(this.currentStep + 1);
            } else {
                this.end(true);
            }
        }

        prev() {
            if (this.currentStep > 0) {
                this.renderStep(this.currentStep - 1);
            }
        }

        end(completed = false) {
            this.isActive = false;
            this.unbindEvents();

            if (this.container) {
                this.container.remove();
                this.container = null;
            }

            if (window.SVM_APP && typeof window.SVM_APP.showToast === 'function') {
                if (completed) {
                    window.SVM_APP.showToast("Guided tour complete! You're ready to dispatch fleet operations.", "success");
                } else {
                    window.SVM_APP.showToast("Guided tour paused. You can restart it anytime from the User Guide.", "info");
                }
            }
        }
    }

    // Expose singleton instance globally
    window.SVM_TOUR = new SvmTourEngine();

    // Attach convenience alias on window.SVM_APP if present
    document.addEventListener('DOMContentLoaded', () => {
        if (window.SVM_APP) {
            window.SVM_APP.startInteractiveTour = () => window.SVM_TOUR.start();
        }
    });
})();
