# 🚀 Deploying SVM FLEET Web Operating System to Render

You can deploy the **SVM FLEET Web Operating System** to **[Render.com](https://render.com)** in less than 2 minutes. Render provides free hosting for static websites and web applications connected directly to your GitHub repository.

---

## 🌟 Quick Deploy Steps (Recommended: Free Static Site)

### Step 1: Push Code to GitHub
Your repository is already connected and pushed to:
`https://github.com/Mekarthiakhi/Raider-app.git`

### Step 2: Open Render Dashboard
1. Go to **[dashboard.render.com](https://dashboard.render.com)** and log in (or sign up with your GitHub account).
2. Click the blue **"New +"** button at the top-right corner.
3. Select **"Static Site"**.

### Step 3: Connect Your GitHub Repository
1. In the repository list, find and select **`Raider-app`** (or paste `https://github.com/Mekarthiakhi/Raider-app`).
2. Configure the following 3 fields:
   - **Name:** `svm-fleet` (or any name you like)
   - **Branch:** `main`
   - **Root Directory:** `web` *(leave empty or enter `web`)*
   - **Build Command:** *(leave empty)*
   - **Publish Directory:** `.` *(if Root Directory is `web`) or `web` (if Root Directory is empty)*

### Step 4: Click "Create Static Site"
- Render will deploy your website in ~30 seconds!
- You will receive a live URL like:  
  **`https://svm-fleet.onrender.com`**

---

## ⚡ Alternative Option: Deploy as a Node.js Web Service

If you prefer deploying as a Web Service:
1. In Render Dashboard, click **"New +"** ➔ **"Web Service"**.
2. Select **`Raider-app`**.
3. Render will auto-detect the root `package.json` and `server.js`:
   - **Runtime:** `Node`
   - **Build Command:** `npm install` (or leave empty)
   - **Start Command:** `node server.js`
4. Click **"Deploy Web Service"**.

---

## 📋 Features Available on Your Deployed Website

Once live on Render, your operators in Moosapet, Kukatpally, and Madhapur can open the URL on any phone, tablet, or desktop:

1. **Live Hyderabad GPS Map:**
   - 30 commercial cabs active across Uber, Rapido, and Ola.
   - Hub bases for Moosapet, Kukatpally, and Madhapur.
2. **Manual Duty Register Entry (Matching Paper Logbook):**
   - Driver Name & Mobile Number (with one-tap calling).
   - Vehicle Reg Number (`TS09 UB 3368`, etc.).
   - Trips completed.
   - Gross earnings ("how much they earn").
   - Fuel & Daily Expenditure (Petrol / Diesel / CNG / Tolls).
   - Cash collected from passengers.
   - **Automatic SVM Commission Calculation (25%, 30%, 35%, 40% tiers)**.
   - **Net Balance Settlement** (Company pays driver vs Driver owes office).
3. **Mobile-App Card View & Dense Table View:**
   - Switch with 1 click between the mobile app's `DutyLogCard` look and a dense spreadsheet table.
4. **CSV Export & Printable A4 Receipts:**
   - Export all shift entries to CSV or print instant settlement slips.

