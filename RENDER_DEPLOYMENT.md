# 🚀 Deploying SVM FLEET Web Operating System to Render

The **SVM FLEET Web Operating System** is completely connected to your live **Supabase Cloud Database**. Any manual or automated revenue entry made on the website is immediately saved to the cloud (`office_duty_logs` table) and will sync with the Android mobile app.

---

## 🌟 Step-by-Step Render Deployment (Takes 2 Minutes)

Render provides **free hosting** for static websites connected directly to your GitHub repository.

### Step 1: Open Render Dashboard
1. Go to **[dashboard.render.com](https://dashboard.render.com)**.
2. Sign in with your **GitHub account**.
3. In the top-right corner, click **"New +"** and choose **"Static Site"**.

---

### Step 2: Connect Your GitHub Repository
1. In the repository list, find and click on **`Raider-app`** (or enter `https://github.com/Mekarthiakhi/Raider-app`).
2. Fill in the deployment settings:
   - **Name:** `svm-fleet` (or any custom name)
   - **Branch:** `main`
   - **Root Directory:** `web`
   - **Build Command:** *(leave empty)*
   - **Publish Directory:** `.`

---

### Step 3: Click "Create Static Site"
1. Click the blue **"Create Static Site"** button at the bottom.
2. Render will deploy your site in ~30 seconds.
3. Once finished, Render gives you a live public URL:  
   👉 **`https://svm-fleet.onrender.com`**

*(You can open this URL on any mobile phone, tablet, laptop, or desktop!)*

---

## ☁️ How the Database Connection Works

- **Live Database:** Connected to Supabase (`https://nwcfwqiymqzzgrzgegty.supabase.co`).
- **Database Table:** `public.office_duty_logs`.
- **Offline-First with Realtime Sync:**
  1. **Immediate Local Cache:** When you hit "Save & Settle Shift Log", the dashboard updates with zero waiting or lag.
  2. **Cloud Sync:** The entry is automatically posted to Supabase via `@supabase/supabase-js`.
  3. **Live Indicator:** The top header displays a green pulsing **`Cloud DB Synced`** badge showing the connection status.
  4. **Supabase Realtime WebSockets:** If another hub manager or the Android app adds a new shift, the website receives the update in real-time without reloading the page!

---

## 📱 Features on the Live Website

1. **Dual Revenue Entry:**
   - **Manual Paper Register:** Select any driver to auto-fill their phone and vehicle reg number, enter trips and fare, and the system automatically calculates the official SVM commission tier (25%, 30%, 35%, 40%), fuel expenditure, and net balance!
   - **Automatic Telemetry Inflow:** 1-Tap aggregate incoming telemetry shifts from Uber, Rapido, and Ola.
2. **Mobile App Cards View & Desktop Table View:**
   - Switch with 1 click between the mobile app's `DutyLogCard` look and a high-density spreadsheet table.
3. **Hyderabad Live GPS Map:**
   - Live fleet monitoring across Moosapet, Kukatpally, and Madhapur Hubs.
4. **Export & Print:**
   - Download CSV reports and print A4 / thermal settlement receipts.
