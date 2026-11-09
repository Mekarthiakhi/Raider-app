/**
 * SVM FLEET Operating System (Client Demo Edition)
 * Hyderabad Fleet Live Map Controller (Leaflet JS + Free OpenStreetMap Dark Mode)
 * Supports Active (18), Idle (7), and Offline (5) Status Legend
 */

class HyderabadMapController {
    constructor(containerId = "hyderabad-map") {
        this.containerId = containerId;
        this.map = null;
        this.markers = new Map();
        this.hubMarkers = [];
    }

    init() {
        const el = document.getElementById(this.containerId);
        if (!el) return;
        if (this.map) return;

        try {
            // Centered on Hyderabad
            this.map = L.map(this.containerId, {
                center: [17.4420, 78.4250],
                zoom: 12,
                zoomControl: false,
                attributionControl: false
            });

            // Free OpenStreetMap with CSS dark inverted styling
            L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
                maxZoom: 19,
                subdomains: ['a', 'b', 'c']
            }).addTo(this.map);

            // Zoom controls bottom-right
            L.control.zoom({ position: 'bottomright' }).addTo(this.map);

            // Render 3 Operational Hubs
            this.renderHubs();

            // Initial load of demo vehicles
            const vehicles = window.DEMO_REPO ? window.DEMO_REPO.getVehicles() : [];
            this.updateVehicles(vehicles);
        } catch (e) {
            console.warn("Leaflet map initialization warning:", e);
        }
    }

    renderHubs() {
        if (!this.map) return;

        const hubs = (window.DEMO_REPO ? window.DEMO_REPO.getHubs() : null) || [
            { id: "mgr_moosapet_01", name: "Moosapet Central Hub", shortName: "Moosapet", lat: 17.4652, lng: 78.4308, color: "#6655F5", cabsCount: 12 },
            { id: "mgr_kukatpally_02", name: "Kukatpally Y-Junction Hub", shortName: "Kukatpally", lat: 17.4935, lng: 78.4012, color: "#3185F6", cabsCount: 10 },
            { id: "mgr_madhapur_03", name: "Madhapur Hitec City Hub", shortName: "Madhapur", lat: 17.4485, lng: 78.3752, color: "#10B981", cabsCount: 8 }
        ];

        hubs.forEach(hub => {
            // Radius circle around hub
            const circle = L.circle([hub.lat, hub.lng], {
                color: hub.color,
                fillColor: hub.color,
                fillOpacity: 0.12,
                radius: 1400,
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
                    <div style="font-size: 12px; color: #94A3B8;">Manager: <strong style="color: #F8FAFC;">${hub.managerName || 'Officer in Charge'}</strong></div>
                    <div style="font-size: 12px; color: #94A3B8;">Contact: <strong style="color: #F8FAFC;">${hub.phone || 'Depot'}</strong></div>
                    <div style="font-size: 12px; color: #94A3B8;">Fleet Assigned: <strong style="color: #10B981;">${hub.cabsCount} Cabs</strong></div>
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
            const statusLower = (vehicle.status || "active").toLowerCase();
            const markerClass = statusLower === "active" ? "active" : statusLower === "idle" ? "idle" : "offline";
            const iconSymbol = statusLower === "active" ? "🟢" : statusLower === "idle" ? "🟡" : "🔴";

            const cabIcon = L.divIcon({
                className: 'cab-pin-wrapper',
                html: `
                    <div class="cab-marker ${markerClass}" title="${vehicle.regNo} - ${vehicle.driverName || 'Unassigned'}">
                        <span style="font-size: 14px;">🚕</span>
                    </div>
                `,
                iconSize: [34, 34],
                iconAnchor: [17, 17]
            });

            const popupContent = `
                <div style="padding: 8px; font-family: inherit; min-width: 180px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                        <strong style="font-size: 13px; font-family: monospace; color: #F8FAFC;">${vehicle.regNo}</strong>
                        <span style="font-size: 10px; font-weight: bold; padding: 2px 6px; border-radius: 4px; background: #162038; color: #8B5CF6;">${vehicle.platform || 'Fleet'}</span>
                    </div>
                    <div style="font-size: 11px; color: #94A3B8;">Model: <span style="color: #F8FAFC;">${vehicle.model}</span></div>
                    <div style="font-size: 11px; color: #94A3B8;">Driver: <span style="color: #F8FAFC; font-weight: 600;">${vehicle.driverName || 'No Driver'}</span></div>
                    <div style="font-size: 11px; color: #94A3B8;">Location: <span style="color: #0EA5E9;">${vehicle.locationName || 'Hyderabad'}</span></div>
                    <div style="display: flex; justify-content: space-between; margin-top: 6px; padding-top: 6px; border-top: 1px solid #263149; font-size: 11px;">
                        <span>Speed: <strong style="color: #10B981;">${vehicle.speed || 0} km/h</strong></span>
                        <span>Fuel: <strong style="color: #F59E0B;">${vehicle.fuel || 80}%</strong></span>
                    </div>
                </div>
            `;

            if (this.markers.has(vehicle.id)) {
                const marker = this.markers.get(vehicle.id);
                marker.setLatLng([vehicle.lat, vehicle.lng]);
                marker.setIcon(cabIcon);
                marker.getPopup().setContent(popupContent);
            } else {
                const marker = L.marker([vehicle.lat, vehicle.lng], { icon: cabIcon }).addTo(this.map);
                marker.bindPopup(popupContent);
                this.markers.set(vehicle.id, marker);
            }
        });
    }

    focusHub(hubId) {
        if (!this.map) return;
        const hubs = window.DEMO_REPO ? window.DEMO_REPO.getHubs() : [];
        if (hubId === "ALL" || !hubId) {
            this.map.flyTo([17.4420, 78.4250], 12, { duration: 1.2 });
            return;
        }
        const hub = hubs.find(h => h.id === hubId);
        if (hub) {
            this.map.flyTo([hub.lat, hub.lng], 14, { duration: 1.2 });
        }
    }
}

window.HyderabadMapController = HyderabadMapController;
