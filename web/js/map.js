/**
 * SVM FLEET Operating System (Web Portal)
 * Hyderabad Live Map Controller (Leaflet JS + CARTO Dark)
 */

class HyderabadMapController {
    constructor(containerId = "hyderabad-map") {
        this.containerId = containerId;
        this.map = null;
        this.markers = new Map();
        this.hubMarkers = [];
        this.simulationTimer = null;
    }

    init() {
        if (!document.getElementById(this.containerId)) return;
        if (this.map) return;

        // Initialize Leaflet Map
        this.map = L.map(this.containerId, {
            center: APP_CONFIG.map.defaultCenter,
            zoom: APP_CONFIG.map.defaultZoom,
            zoomControl: false,
            attributionControl: false
        });

        // Add Dark Theme CartoDB tiles
        L.tileLayer(APP_CONFIG.map.tileLayerUrl, {
            maxZoom: 19,
            subdomains: 'abcd'
        }).addTo(this.map);

        // Add Zoom Control to bottom-right
        L.control.zoom({ position: 'bottomright' }).addTo(this.map);

        // Render Hub Bases (Moosapet, Kukatpally, Madhapur)
        this.renderHubs();

        // Start live GPS drift simulation
        this.startLiveSimulation();
    }

    renderHubs() {
        APP_CONFIG.hubs.forEach(hub => {
            // Pulse circle around hub
            const circle = L.circle([hub.lat, hub.lng], {
                color: hub.color,
                fillColor: hub.color,
                fillOpacity: 0.08,
                radius: 1200,
                weight: 1.5,
                dashArray: "4, 6"
            }).addTo(this.map);

            // Hub Landmark Marker
            const hubIcon = L.divIcon({
                className: 'hub-pin',
                html: `
                    <div style="background: ${hub.color}; color: #fff; padding: 4px 8px; border-radius: 8px; font-size: 11px; font-weight: 700; white-space: nowrap; box-shadow: 0 4px 12px rgba(0,0,0,0.5); border: 1px solid rgba(255,255,255,0.2); display: flex; align-items: center; gap: 4px;">
                        <span>🏢</span> ${hub.shortName} Hub
                    </div>
                `,
                iconSize: [100, 24],
                iconAnchor: [50, 12]
            });

            const marker = L.marker([hub.lat, hub.lng], { icon: hubIcon }).addTo(this.map);
            marker.bindPopup(`
                <div style="padding: 4px;">
                    <div style="font-weight: 700; font-size: 14px; color: ${hub.color}; margin-bottom: 4px;">${hub.name}</div>
                    <div style="font-size: 12px; color: #94A3B8;">Manager: <strong style="color: #F8FAFC;">${hub.managerName}</strong></div>
                    <div style="font-size: 12px; color: #94A3B8;">Phone: <strong style="color: #F8FAFC;">${hub.phone}</strong></div>
                </div>
            `);

            this.hubMarkers.push(circle, marker);
        });
    }

    updateVehicles(vehicles) {
        if (!this.map) return;

        // Track active vehicle IDs to clean up removed ones
        const currentIds = new Set(vehicles.map(v => v.id));

        // Remove old markers that aren't in current filter
        for (const [id, marker] of this.markers.entries()) {
            if (!currentIds.has(id)) {
                this.map.removeLayer(marker);
                this.markers.delete(id);
            }
        }

        // Add or update markers
        vehicles.forEach(vehicle => {
            const isActive = vehicle.status === "ACTIVE";
            const statusClass = isActive ? "active" : "idle";
            const platformBadge = vehicle.platform === "Uber" ? "bg-black text-white"
                : vehicle.platform === "Rapido" ? "bg-yellow-500 text-black"
                : vehicle.platform === "Ola" ? "bg-green-600 text-white" : "bg-gray-600 text-white";

            const customIcon = L.divIcon({
                className: 'custom-cab-pin',
                html: `
                    <div class="cab-marker ${statusClass}" title="${vehicle.regNo}">
                        <span style="font-size: 16px;">🚕</span>
                    </div>
                `,
                iconSize: [38, 38],
                iconAnchor: [19, 19]
            });

            const popupContent = `
                <div style="min-width: 210px; font-family: inherit;">
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #232836; padding-bottom: 6px; margin-bottom: 8px;">
                        <span style="font-weight: 800; font-size: 13px; color: #F8FAFC; letter-spacing: 0.5px;">${vehicle.regNo}</span>
                        <span style="font-size: 10px; padding: 2px 6px; border-radius: 4px; font-weight: 700;" class="${platformBadge}">
                            ${vehicle.platform}
                        </span>
                    </div>
                    <div style="font-size: 11px; color: #94A3B8; margin-bottom: 4px;">Model: <strong style="color: #F8FAFC;">${vehicle.model}</strong></div>
                    <div style="font-size: 11px; color: #94A3B8; margin-bottom: 4px;">Driver: <strong style="color: #F8FAFC;">${vehicle.driverName}</strong></div>
                    <div style="font-size: 11px; color: #94A3B8; margin-bottom: 4px;">Location: <strong style="color: #38BDF8;">${vehicle.locationName}</strong></div>
                    <div style="display: flex; justify-content: space-between; font-size: 11px; margin-top: 6px; background: rgba(255,255,255,0.05); padding: 4px 6px; border-radius: 6px;">
                        <span>Speed: <strong style="color: #2ED3A0;">${vehicle.speed} km/h</strong></span>
                        <span>Fuel: <strong style="color: #E9B949;">${vehicle.fuel}%</strong></span>
                    </div>
                    <div style="display: flex; justify-content: space-between; font-size: 11px; margin-top: 4px; padding: 2px 6px;">
                        <span>Trips: <strong>${vehicle.tripsToday}</strong></span>
                        <span>Earnings: <strong style="color: #2ED3A0;">₹${vehicle.todayEarnings}</strong></span>
                    </div>
                    <button onclick="window.SVM_APP.openManualEntryModal('${vehicle.regNo}', '${vehicle.driverName}', ${vehicle.tripsToday}, ${vehicle.todayEarnings})" 
                        style="width: 100%; margin-top: 10px; background: #5B36F5; color: #fff; font-size: 11px; font-weight: 700; padding: 6px 0; border: none; border-radius: 6px; cursor: pointer;">
                        📝 Enter Shift Log
                    </button>
                </div>
            `;

            if (this.markers.has(vehicle.id)) {
                const marker = this.markers.get(vehicle.id);
                marker.setLatLng([vehicle.lat, vehicle.lng]);
                marker.setIcon(customIcon);
                marker.setPopupContent(popupContent);
            } else {
                const marker = L.marker([vehicle.lat, vehicle.lng], { icon: customIcon }).addTo(this.map);
                marker.bindPopup(popupContent);
                this.markers.set(vehicle.id, marker);
            }
        });
    }

    focusHub(hubId) {
        if (!this.map) return;
        if (!hubId || hubId === "ALL") {
            this.map.flyTo(APP_CONFIG.map.defaultCenter, APP_CONFIG.map.defaultZoom, { duration: 1.2 });
            return;
        }
        const hub = APP_CONFIG.hubs.find(h => h.id === hubId);
        if (hub) {
            this.map.flyTo([hub.lat, hub.lng], 13.5, { duration: 1.2 });
        }
    }

    startLiveSimulation() {
        if (this.simulationTimer) clearInterval(this.simulationTimer);

        // Every 4 seconds, introduce subtle realistic GPS drift for active vehicles
        this.simulationTimer = setInterval(() => {
            const vehicles = FleetDataRepository.getVehicles();
            let changed = false;

            vehicles.forEach(v => {
                if (v.status === "ACTIVE" && v.speed > 0) {
                    // Small delta around 0.0003 deg (~30 meters)
                    const deltaLat = (Math.random() - 0.5) * 0.0006;
                    const deltaLng = (Math.random() - 0.5) * 0.0006;
                    v.lat = Math.round((v.lat + deltaLat) * 10000) / 10000;
                    v.lng = Math.round((v.lng + deltaLng) * 10000) / 10000;
                    // Fluctuations in speed
                    v.speed = Math.max(15, Math.min(65, Math.round(v.speed + (Math.random() * 6 - 3))));
                    changed = true;
                }
            });

            if (changed && window.SVM_APP) {
                // Update live without rewriting full storage constantly
                const activeVehicles = window.SVM_APP.getCurrentFilteredVehicles();
                this.updateVehicles(activeVehicles);
            }
        }, 4000);
    }
}

window.HyderabadMapController = HyderabadMapController;

