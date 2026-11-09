# 🚕 SVM FLEET Operating System (Web Portal)

> **Hyderabad Commercial Fleet Management Platform**  
> Serving **Uber Driver**, **Rapido Captain**, and **Ola Driver** across **Moosapet**, **Kukatpally**, and **Madhapur** Hubs.

---

## 🌟 Overview & Capabilities

The **SVM FLEET Web Operating System** is a standalone, lightweight, zero-dependency web dashboard built specifically for the **Fleet Owner** and **Hub Fleet Managers** to:

1. **Track Vehicles Live on GPS Map:**
   - Real-time Hyderabad map (Leaflet + CARTO Dark tiles) showing active cabs, vehicle models, speed (km/h), fuel %, driver contact, and platform status.
   - Dedicated landmark hubs for **Moosapet Central Hub**, **Kukatpally Y-Junction Hub**, and **Madhapur Hitec City Hub**.
2. **Dual Revenue Entry System:**
   - **⚡ Automatic Telemetry Inflow:** Live stream of shifts captured via Android Accessibility + OCR from Uber Driver, Rapido Captain, and Ola Driver. Click **"1-Tap Auto-Aggregate"** to instantly verify and save the shift.
   - **📝 Manual Register Entry Form:** Pixel-perfect digital replica of the Moosapet paper logbook (`Image 2`). Fill in trips, gross earnings, cash collections, and expenditures, and the system **instantly calculates the official SVM commission tier (25%, 30%, 35%, 40%)** and net balance settlement!
3. **3-Tier Scoped Access Control:**
   - **👑 Fleet Owner (Master):** Full visibility across all 30 vehicles, all drivers, and all 3 hubs. Can transfer vehicles and drivers between hubs with 1 click.
   - **👔 Hub Managers (Moosapet / Kukatpally / Madhapur):** Scoped strictly to their assigned hub fleet (10–12 cars & drivers). Can manage daily shift entries and settle receipts for their assigned team.
4. **Export & Print Capabilities:**
   - **CSV Export:** One-click download of daily shift statements for accounting and audits.
   - **A4 Printable Receipts:** Formatted thermal/A4 settlement receipts for drivers with itemized credit, debit, and net balance.

---

## 🚀 How to Run the Website

### Option 1: Double-Click (No Server Required!)
Simply open `web/index.html` in Google Chrome, Microsoft Edge, Mozilla Firefox, or Safari. All styles and libraries are served via high-speed CDNs.

### Option 2: Local HTTP Server (Recommended)
You can launch a local web server using Python or Node:

```powershell
# Using Python
cd c:\Users\Dell\Desktop\raider-app\web
python -m http.server 3000
# Then open http://localhost:3000 in your browser
```

```powershell
# Using npx serve
cd c:\Users\Dell\Desktop\raider-app\web
npx serve .
```

---

## 📐 Official SVM Commission Logic

Implements the official rules from `SvmCommissionCalculator`:

### 12 Hours Shift (Minimum 10 Trips required):
- Gross Earnings **≥ ₹3,500** ➔ **40% Commission**
- Gross Earnings **≥ ₹2,500** ➔ **35% Commission**
- Gross Earnings **≥ ₹2,000** ➔ **30% Commission**
- Gross Earnings **≥ ₹1,500** ➔ **25% Commission**
- Under 10 trips or < ₹1,500 ➔ **0% Commission (Flat vehicle rent applies)**

### 24 Hours Shift (Minimum 18 Trips required):
- Gross Earnings **≥ ₹6,500** ➔ **40% Commission**
- Gross Earnings **≥ ₹5,000** ➔ **35% Commission**
- Gross Earnings **≥ ₹4,000** ➔ **30% Commission**
- Gross Earnings **≥ ₹2,500** ➔ **25% Commission**

### Settlement Formula:
$$\text{Net Balance} = (\text{Commission} + \text{Expenditure}) - (\text{Cash Collection} + \text{Advance} + \text{Drive Pass})$$
- **Positive Net Balance:** Company pays the driver.
- **Negative Net Balance:** Driver deposits cash collection balance to the office register.

---

## 📁 Directory Structure
```
web/
├── index.html          # Responsive Single Page Application
├── README.md           # Documentation and run instructions
├── css/
│   └── styles.css      # Dark-first Electric Purple & Glassmorphism design system
└── js/
    ├── config.js       # Hub definitions, Owner master profile & storage keys
    ├── calculator.js   # SVM Commission tier engine & INR currency formatter
    ├── data.js         # 30 vehicles, 30 drivers, Hyderabad GPS coordinates, seed logs
    ├── map.js          # Leaflet Hyderabad map controller with custom cab pins
    └── app.js          # View orchestrator, state management, modals, CSV export
```

