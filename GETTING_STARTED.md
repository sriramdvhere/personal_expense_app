# Getting Started — Personal Expense Manager

This guide walks you through setting up, building, and using the Personal Expense Manager Android app. No prior Android knowledge is needed — if you have written a little Java before, you have enough background to follow along.

---

## What This App Does

Personal Expense Manager is an offline Android app that lets you:

- Record daily expenses (date, amount, category, optional note)
- View a monthly dashboard with category-wise spending breakdown
- Browse and filter your full expense history
- Import expenses in bulk from a CSV file
- Export your data as a CSV file

All data stays on your phone — nothing is sent to any server.

---

## Part 1 — Setting Up Your Machine

### Step 1: Install Android Studio

Android Studio is the official IDE (code editor + build tool) for Android apps. Think of it as IntelliJ IDEA or Eclipse, but purpose-built for Android. It includes everything you need: the Java/Kotlin compiler, the Android SDK, and an emulator.

1. Go to [developer.android.com/studio](https://developer.android.com/studio) and download the installer for your operating system.
2. Run the installer and follow the on-screen steps. Accept all defaults.
3. On first launch, Android Studio runs a **Setup Wizard**. Let it finish — it downloads the Android SDK (this takes a few minutes and requires an internet connection).

> **What is the Android SDK?**
> It is a collection of libraries and tools that let your code talk to Android hardware features (screen, storage, camera, etc.). Similar to the JDK for plain Java, but for Android.

---

### Step 2: Set Up an Emulator (Virtual Android Phone)

You can run the app on a virtual device (emulator) without needing a physical phone.

1. In Android Studio, open **Device Manager**:
   - Menu → **Tools** → **Device Manager**
2. Click **Create Device**.
3. Pick any phone profile (e.g. **Pixel 6**) and click **Next**.
4. Under **System Image**, choose a release with **API Level 29 or higher** (Android 10+). If none is downloaded yet, click the download arrow next to it and wait.
5. Click **Next** then **Finish**.

Your virtual device now appears in Device Manager. Click the green ▶ button to start it — it boots like a real phone.

> **Tip:** Keep the emulator running while you work. Cold-starting it every time is slow; leaving it running is fast.

---

## Part 2 — Opening the Project

### Step 3: Open the Project in Android Studio

1. Launch Android Studio.
2. On the welcome screen, click **Open**.
3. Navigate to the folder where the project lives:
   ```
   C:\Users\sriram_dhanaraj\my_projects\personal_expense_app
   ```
4. Click **OK**.

Android Studio opens the project and immediately starts **Gradle sync** in the background. You will see a progress bar at the bottom. This is Gradle downloading all the dependencies listed in `app/build.gradle.kts` (Room, Compose, Navigation, etc.).

> **What is Gradle?**
> Gradle is the build system for Android projects — similar to Maven in a standard Java project. It reads the `build.gradle.kts` files and compiles, packages, and assembles the app for you.

Wait until the sync finishes (the progress bar disappears and the bottom bar shows no tasks running). This may take a few minutes on first run as it downloads libraries.

---

### Step 4: Build and Run the App

1. Make sure your emulator is running (or a physical phone is connected via USB with USB Debugging enabled).
2. In the toolbar at the top of Android Studio, confirm the **target device** dropdown shows your emulator or phone.
3. Click the green **Run ▶** button (or press `Shift + F10`).

Android Studio compiles the code, assembles the APK (the installable app file), and deploys it to the device automatically. The app opens on the emulator/phone.

> **What is an APK?**
> An APK (Android Package) is the equivalent of a `.jar` or `.exe` — it is the compiled, packaged app that Android can install and run.

---

## Part 3 — Using the App

The app has four screens reachable via the bottom navigation bar.

---

### Screen 1: Dashboard (Home tab — 🏠)

The first screen you see after opening the app.

| Element | What it does |
|---------|-------------|
| **This Month's Total** card | Shows the total amount spent in the selected month |
| **< Prev / Next >** buttons | Navigate to the previous or next month |
| **Category Breakdown** | Lists each spending category with the amount and a percentage bar |
| **Recent Transactions** | Shows your 5 most recent expenses |
| **View All →** | Opens the full expense list |

---

### Screen 2: Add Expense (+ tab)

Use this screen to record a new expense manually.

1. Tap the **date field** — a calendar picker opens. Tap a date (future dates are disabled).
2. Tap the **amount field** and type the amount in rupees (e.g. `250.50`).
3. Tap **Select Category** and choose one of the 15 predefined categories.
4. Optionally type a short **note** (e.g. `Swiggy lunch`).
5. Tap **SAVE EXPENSE**. The app saves the entry and returns to the previous screen.

**Editing an existing expense:** From the Expense List screen, tap the ✏️ edit icon on any expense. The same form opens pre-filled with the existing values. Change what you need and tap **SAVE EXPENSE**.

---

### Screen 3: Expense List (📋 tab)

Shows all your recorded expenses, grouped by date.

**Filtering by date range:**
1. Tap the **Start Date** field and pick a date.
2. Tap the **End Date** field and pick a date.
3. Tap **Apply Filter** — the list updates to show only expenses in that range.
4. Tap **Clear Filter** to go back to the full list.

**Deleting an expense:**
1. Tap the 🗑️ delete icon on any expense row.
2. A confirmation dialog appears — tap **Delete** to confirm, or **Cancel** to dismiss.

---

### Screen 4: Import / Export (📁 tab)

#### Importing from CSV

Useful for adding many expenses at once from a spreadsheet.

1. Prepare a CSV file on your phone or computer with this format:

   ```
   Date,Amount,Category,Notes
   25/03/2025,250.00,Food & Dining,Swiggy lunch
   24/03/2025,150,Transportation,Ola cab
   23/03/2025,3000,Bills & Utilities,
   ```

   Rules:
   - First row must be the header `Date,Amount,Category,Notes`
   - Date format: **DD/MM/YYYY**
   - Category must match one of the 15 predefined names (case-insensitive). Unknown values become **Others**.
   - Notes column is optional — a 3-column file (without Notes) also works.
   - Future dates are allowed in imports.
   - Exact duplicates (same date + amount + category + note already in the app) are skipped automatically.

2. Tap **📥 SELECT CSV FILE** — the system file picker opens.
3. Navigate to and select your CSV file.
4. The app imports the file and shows a summary:
   - **Imported:** rows successfully added
   - **Skipped:** exact duplicates that were ignored
   - **Errors:** rows with invalid date or amount format

#### Exporting to CSV

1. By default, **Export all data** is checked — this exports every expense you have ever recorded.
2. To export only a date range: uncheck **Export all data**, then set a **Start Date** and **End Date**.
3. Tap **📤 EXPORT AS CSV**.
4. The app saves a file named `expenses_YYYYMMDD_HHmmss.csv` to your phone's **Downloads** folder and opens a share sheet so you can send it via email, WhatsApp, copy to your computer, etc.

---

## Part 4 — Predefined Categories

The app has 15 fixed categories. You cannot add custom ones.

| # | Category |
|---|----------|
| 1 | Food & Dining |
| 2 | Transportation |
| 3 | Shopping |
| 4 | Bills & Utilities |
| 5 | Entertainment |
| 6 | Health & Medical |
| 7 | Education |
| 8 | Groceries |
| 9 | Rent |
| 10 | Travel |
| 11 | Personal Care |
| 12 | Gifts & Donations |
| 13 | Investments |
| 14 | Insurance |
| 15 | Others |

---

## Part 5 — Backup and Data Safety

Your data lives only in the app's local database on the device. Two ways to protect it:

**Manual backup (recommended before switching phones):**
Go to Import / Export → Export all data as CSV. Copy the file to your computer or cloud storage. On the new phone, install the app and use the Import feature to restore your data.

**Automatic backup:**
The app supports Android's built-in Auto Backup. If you have a Google account signed in on your phone and backup is enabled in Android Settings → Google → Backup, your expense data is automatically backed up to Google Drive and restored when you sign in on a new device.

---

## Troubleshooting

| Problem | What to try |
|---------|-------------|
| Gradle sync fails with a network error | Check your internet connection and retry via **File → Sync Project with Gradle Files** |
| Emulator is very slow | In Device Manager, edit the device and increase RAM to 2048 MB; also enable hardware acceleration in BIOS if on Windows |
| App crashes immediately on launch | In Android Studio, open **Logcat** (bottom panel) — it shows the error message in red |
| CSV import shows many errors | Open the CSV in a text editor and confirm the date format is **DD/MM/YYYY** (not MM/DD/YYYY) and the first row is the header |
| Exported file not found | Open the **Files** app on the phone and look in the **Downloads** folder |
