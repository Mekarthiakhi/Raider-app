/**
 * SVM FLEET Operating System (Web Portal)
 * Hyderabad Live Map Controller (Leaflet JS + Free OpenStreetMap Night Tiles)
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

        try {
            // Initialize Leaflet Map centered on Hyderabad
            this.map = L.map(this.containerId, {
                center: APP_CONFIG.map.defaultCenter,
                zoom: APP_CONFIG.map.defaultZoom,
                zoomControl: false,
                attributionControl: false
            });

            // Use Standard Free OpenStreetMap with CSS Dark Invert (Zero Watermark / No API Key!)
            L.tileLayer(APP_CONFIG.map.tileLayerUrl, {
                maxZoom: 19,
                subdomains: ['a', 'b', 'c']
            }).addTo(this.map);

            // Add Zoom Control to bottom-right
            L.control.zoom({ position: 'bottomright' }).addTo(this.map);

            // Render Hub Bases (Moosapet, Kukatpally, Madhapur)
            this.renderHubs();

            // Start live GPS drift simulation
            this.startLiveSimulation();
        } catch (e) {
            console.warn("Leaflet map initialization warning:", e);
        }
    }

    renderHubs() {
        if (!this.map) return;

        APP_CONFIG.hubs.forEach(hub => {
            // Pulse circle around hub
            const circle = L.circle([hub.lat, hub.lng], {
                color: hub.color,
                fillColor: hub.color,
                fillOpacity: 0.12,
                radius: 1200,
                weight: 1.5,
                dashArray: "4, 6"
            }).addTo(this.map);

            // Hub Landmark Marker
            const hubIcon = L.divIcon({
                className: 'hub-pin',
                html: `
                    <div style="background: ${hub.color}; color: #fff; padding: 4px 10px; border-radius: 8px; font-size: 11px; font-weight: 800; white-space: nowrap; box-shadow: 0 4px 16px rgba(0,0,0,0.6); border: 1px solid rgba(255,255,255,0.25); display: flex; align-items: center; gap: 5px;">
                        <span>🏢</span> ${hub.shortName} Hub
                    </div>
                `,
                iconSize: [110, 26],
                iconAnchor: [55, 13]
            });

            const marker = L.marker([hub.lat, hub.lng], { icon: hubIcon }).addTo(this.map);
            marker.bindPopup(`
                <div style="padding: 6px; font-family: inherit;">
                    <div style="font-weight: 800; font-size: 14px; color: ${hub.color}; margin-bottom: 4px;">${hub.name}</div>
                    <div style="font-size: 12px; color: #94A3B8;">Manager: <strong style="color: #F8FAFC;">${hub.managerName}</strong></div>
                    <div style="font-size: 12px; color: #94A3B8;">Contact: <strong style="color: #F8FAFC;">${hub.phone}</strong></div>
                    <div style="font-size: 12px; color: #94A3B8;">Fleet: <strong style="color: #10B981;">${hub.cabsCount} Cabs Active</strong></div>
                </div>
            `);

            this.hubMarkers.push(circle, marker);
        });
    }

    updateVehicles(vehicles) {
        if (!this.map) return;

        const currentIds = new Set(vehicles.map(v => v.id));

        for (const [id, marker] of this.markers.entries()) {
            if (!currentIds.has(id)) {
                this.map.removeLayer(marker);
                this.markers.delete(id);
            }
        }

        vehicles.forEach(vehicle => {
            const isActive = vehicle.status === "ACTIVE";
            const statusClass = isActive ? "active" : "idle";
            const platformBadge = vehicle.platform === "Uber" ? "background: #000; color: #fff;"
                : vehicle.platform === "Rapido" ? "background: #F59E0B; color: #000;"
                : vehicle.platform === "Ola" ? "background: #10B981; color: #fff;" : "background: #475569; color: #fff;";

            const customIcon = L.divIcon({
                className: 'custom-cab-pin',
                html: `
                    <div class="cab-marker ${statusClass}" title="${vehicle.regNo}">
                        <span style="font-size: 15px;">🚕</span>
                    </div>
                `,
                iconSize: [36, 36],
                iconAnchor: [18, 18]
            });

            const popupContent = `
                <div style="min-width: 220px; font-family: inherit;">
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 6px; margin-bottom: 8px;">
                        <span style="font-weight: 800; font-size: 13px; color: #F8FAFC; letter-spacing: 0.5px; font-family: monospace;">${vehicle.regNo}</span>
                        <span style="font-size: 10px; padding: 2px 8px; border-radius: 4px; font-weight: 800; ${platformBadge}">
                            ${vehicle.platform}
                        </span>
                    </div>
                    <div style="font-size: 12px; color: #94A3B8; margin-bottom: 3px;">Model: <strong style="color: #F8FAFC;">${vehicle.model}</strong></div>
                    <div style="font-size: 12px; color: #94A3B8; margin-bottom: 3px;">Driver: <strong style="color: #F8FAFC;">${vehicle.driverName}</strong></div>
                    <div style="font-size: 12px; color: #94A3B8; margin-bottom: 4px;">Zone: <strong style="color: #38BDF8;">${vehicle.locationName}</strong></div>
                    <div style="display: flex; justify-content: space-between; font-size: 11px; margin-top: 6px; background: rgba(255,255,255,0.06); padding: 5px 8px; border-radius: 6px;">
                        <span>Speed: <strong style="color: #10B981;">${vehicle.speed} km/h</strong></span>
                        <span>Fuel: <strong style="color: #F59E0B;">${vehicle.fuel}%</strong></span>
                    </div>
                    <div style="display: flex; justify-content: space-between; font-size: 11px; margin-top: 4px; padding: 2px 6px;">
                        <span>Today Trips: <strong>${vehicle.tripsToday}</strong></span>
                        <span>Gross: <strong style="color: #10B981;">₹${vehicle.todayEarnings}</strong></span>
                    </div>
                    <button onclick="window.SVM_APP.openManualEntryModal('${vehicle.regNo}', '${vehicle.driverName}')" 
                        style="width: 100%; margin-top: 10px; background: #6366F1; color: #fff; font-size: 11px; font-weight: 800; padding: 7px 0; border: none; border-radius: 8px; cursor: pointer;">
                        📝 Log Shift Entry
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

        this.simulationTimer = setInterval(() => {
            const vehicles = FleetDataRepository.getVehicles();
            let changed = false;

            vehicles.forEach(v => {
                if (v.status === "ACTIVE" && v.speed > 0) {
                    const deltaLat = (Math.random() - 0.5) * 0.0006;
                    const deltaLng = (Math.random() - 0.5) * 0.0006;
                    v.lat = Math.round((v.lat + deltaLat) * 10000) / 10000;
                    v.lng = Math.round((v.lng + deltaLng) * 10000) / 10000;
                    v.speed = Math.max(15, Math.min(65, Math.round(v.speed + (Math.random() * 6 - 3))));
                    changed = true;
                }
            });

            if (changed && window.SVM_APP) {
                const activeVehicles = window.SVM_APP.getCurrentFilteredVehicles();
                this.updateVehicles(activeVehicles);
            }
        }, 4000);
    }
}

window.HyderabadMapController = HyderabadMapController;
