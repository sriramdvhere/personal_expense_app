```markdown
# Personal Expense Manager - Android App Development Plan (v2)

> **Changelog from v1:** C1 → Excel export dropped, CSV only. C2 → Duplicate detection moved to import logic, DB unique constraint removed. C3 → Epoch conversion uses device timezone explicitly. D1 → `note` field added to expense. D2 → Future-date restriction removed from import. D3 → Export checkbox/date-range UX conflict resolved. D4 → Timestamp-based export filenames. AV1 → Hilt replaced with AppContainer. AV2 → Use case layer removed. AV3 → OpenCSV replaced with manual parser. AV4 → ExpenseMapper removed, entity used directly. M1 → Coroutines threading convention documented. M2 → Room migration strategy added. M3 → Android Auto Backup enabled. M4 → minSdk lowered to 29. M5 → Testing dependencies added.

## Table of Contents
1. [Project Overview](#project-overview)
2. [Technical Specifications](#technical-specifications)
3. [Predefined Categories](#predefined-categories)
4. [Project Structure](#project-structure)
5. [Database Schema](#database-schema)
6. [Screen Designs](#screen-designs)
7. [Dependencies](#dependencies)
8. [CSV Format Specification](#csv-format-specification)
9. [Feature Requirements](#feature-requirements)
10. [Implementation Checklist](#implementation-checklist)

---

## Project Overview

| Aspect | Details |
|--------|---------|
| **App Name** | Personal Expense Manager |
| **Purpose** | Personal expense tracking with import/export capabilities |
| **Platform** | Android |
| **Usage** | Personal use only |
| **Data Storage** | Local only (100% offline) |
| **Currency** | INR (₹) only |
| **Expected Data Volume** | ~100 entries per month maximum |

### Core Features Summary
1. Store personal expenses with date, amount, category, and optional note
2. Import expenses via CSV file
3. Add expenses manually via UI
4. Export data in CSV format
5. Dashboard with monthly summary and category breakdown
6. Filter expenses by date range
7. Edit and delete existing expenses

---

## Technical Specifications

| Specification | Value |
|---------------|-------|
| **Language** | Kotlin |
| **Min SDK** | API 29 (Android 10) |
| **Target SDK** | API 35 |
| **Architecture** | MVVM — ViewModel calls Repository directly (no use case layer) |
| **UI Framework** | Jetpack Compose |
| **Design System** | Material Design 3 |
| **Database** | Room (SQLite) |
| **DI** | Manual AppContainer (no Hilt) |
| **Async** | Kotlin Coroutines with `viewModelScope` |

---

## Predefined Categories

The app uses a fixed set of 15 system-defined categories. Users cannot add custom categories. Categories are case-insensitive during import matching.

| # | Category Name | Icon |
|---|---------------|------|
| 1 | Food & Dining | 🍔 |
| 2 | Transportation | 🚗 |
| 3 | Shopping | 🛒 |
| 4 | Bills & Utilities | 📱 |
| 5 | Entertainment | 🎬 |
| 6 | Health & Medical | 🏥 |
| 7 | Education | 📚 |
| 8 | Groceries | 🛍️ |
| 9 | Rent | 🏠 |
| 10 | Travel | ✈️ |
| 11 | Personal Care | 💅 |
| 12 | Gifts & Donations | 🎁 |
| 13 | Investments | 📈 |
| 14 | Insurance | 🛡️ |
| 15 | Others | 📌 |

**Import Matching Rule:** If category in CSV doesn't match any predefined category, default to "Others"

---

## Project Structure

```
📦 com.personal.expensemanager
│
├── 📂 data
│   ├── 📂 local
│   │   ├── 📂 database
│   │   │   ├── ExpenseDatabase.kt
│   │   │   └── Converters.kt
│   │   ├── 📂 dao
│   │   │   └── ExpenseDao.kt
│   │   └── 📂 entity
│   │       └── ExpenseEntity.kt        ← single model used throughout the app
│   └── 📂 repository
│       ├── ExpenseRepository.kt        ← interface (for testability)
│       └── ExpenseRepositoryImpl.kt
│
├── 📂 model                            ← lightweight data classes (not DB entities)
│   ├── Category.kt
│   ├── MonthlySummary.kt
│   └── CategorySummary.kt
│
├── 📂 presentation
│   ├── 📂 navigation
│   │   ├── NavGraph.kt
│   │   ├── Screen.kt
│   │   └── BottomNavBar.kt
│   ├── 📂 screens
│   │   ├── 📂 dashboard
│   │   │   ├── DashboardScreen.kt
│   │   │   ├── DashboardViewModel.kt
│   │   │   └── DashboardState.kt
│   │   ├── 📂 addexpense
│   │   │   ├── AddExpenseScreen.kt
│   │   │   ├── AddExpenseViewModel.kt
│   │   │   └── AddExpenseState.kt
│   │   ├── 📂 expenselist
│   │   │   ├── ExpenseListScreen.kt
│   │   │   ├── ExpenseListViewModel.kt
│   │   │   └── ExpenseListState.kt
│   │   └── 📂 importexport
│   │       ├── ImportExportScreen.kt
│   │       ├── ImportExportViewModel.kt
│   │       └── ImportExportState.kt
│   ├── 📂 components
│   │   ├── ExpenseCard.kt
│   │   ├── CategoryDropdown.kt
│   │   ├── DatePickerField.kt
│   │   ├── AmountTextField.kt
│   │   ├── SummaryCard.kt
│   │   ├── CategoryBreakdownItem.kt
│   │   ├── DateRangeFilter.kt
│   │   ├── LoadingIndicator.kt
│   │   ├── EmptyState.kt
│   │   └── ErrorMessage.kt
│   └── 📂 theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── 📂 util
│   ├── CsvHelper.kt                    ← manual 4-column parser (~40 lines, no library)
│   ├── DateUtils.kt
│   ├── CurrencyUtils.kt
│   ├── FileUtils.kt
│   └── Constants.kt
│
├── 📄 AppContainer.kt                  ← replaces Hilt; holds DB + repository singletons
├── 📄 MainActivity.kt
└── 📄 ExpenseManagerApp.kt
```

**Removed from v1:** `domain/usecase/`, `data/mapper/`, `di/`, `util/ExcelHelper.kt`

---

## Database Schema

### Table: expenses

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `date` | INTEGER | NOT NULL | Epoch millis at start of day in device timezone |
| `amount` | REAL | NOT NULL | Expense amount in INR |
| `category` | TEXT | NOT NULL | Category enum name (e.g., "FOOD_DINING") |
| `note` | TEXT | NULL | Optional free-text note (e.g., "Swiggy lunch") |
| `created_at` | INTEGER | NOT NULL | Record creation timestamp |
| `updated_at` | INTEGER | NOT NULL | Record last update timestamp |

### Constraints & Indexes

```sql
-- No UNIQUE constraint: two expenses with the same date/amount/category are
-- allowed (e.g., two coffees in a day). Deduplication is handled in import logic.

INDEX(date)
INDEX(category)
```

### Database Migration Strategy

- **Schema version:** Start at version 1 with the full schema above (including `note`).
- **Safety net:** Add `fallbackToDestructiveMigration()` in `ExpenseDatabase` builder. For a personal app, the user can re-import from CSV if a migration fails.
- **Explicit migration (if upgrading from a v1 schema without `note`):**
  ```kotlin
  val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE expenses ADD COLUMN note TEXT")
      }
  }
  ```

### Required DAO Queries

| Query | Purpose |
|-------|---------|
| Insert single expense | Add new expense |
| Insert multiple expenses | Bulk import |
| Update expense | Edit existing expense |
| Delete expense | Remove expense |
| Get all expenses | Ordered by date DESC |
| Get expenses by date range | For filtering and export |
| Get total by date range | Monthly summary |
| Get category breakdown | Category-wise totals for date range |
| Get recent expenses (limit N) | Dashboard recent transactions |

---

## Screen Designs

### Screen 1: Dashboard (Home)

**Route:** `/dashboard`

**Layout:**
```
┌─────────────────────────────────────┐
│  Personal Expense Manager           │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────────┐│
│  │      This Month's Total         ││
│  │         ₹ 45,230.00             ││
│  │         March 2025              ││
│  │    [< Prev]       [Next >]      ││
│  └─────────────────────────────────┘│
│                                     │
│  Category Breakdown                 │
│  ┌─────────────────────────────────┐│
│  │ 🍔 Food & Dining      ₹8,500   ││
│  │ ━━━━━━━━━━━━━━━━ 18.8%         ││
│  │ 🚗 Transportation     ₹5,200   ││
│  │ ━━━━━━━━━━━━ 11.5%             ││
│  │ 🛒 Groceries         ₹12,000   ││
│  │ ━━━━━━━━━━━━━━━━━━━━ 26.5%     ││
│  │ (scrollable list)              ││
│  └─────────────────────────────────┘│
│                                     │
│  Recent Transactions                │
│  ┌─────────────────────────────────┐│
│  │ 25/03  Food & Dining    ₹250   ││
│  │ 24/03  Transportation   ₹150   ││
│  │ 24/03  Shopping       ₹1,200   ││
│  │ 23/03  Groceries      ₹2,500   ││
│  │ [View All →]                   ││
│  └─────────────────────────────────┘│
│                                     │
├─────────────────────────────────────┤
│  🏠        ➕        📋        📁   │
│ Home      Add      List    Import  │
└─────────────────────────────────────┘
```

**State:**
```
- selectedMonth: YearMonth (default: current month)
- monthlyTotal: Double
- categoryBreakdown: List<CategorySummary>
- recentTransactions: List<ExpenseEntity> (limit 5)
- isLoading: Boolean
- error: String?
```

**Features:**
- Month selector with previous/next navigation
- Total expense display for selected month
- Category-wise breakdown with percentage bars (guard against zero total to avoid division by zero)
- Last 5 recent transactions
- "View All" navigates to Expense List

---

### Screen 2: Add Expense

**Route:** `/add-expense` and `/edit-expense/{id}`

**Layout:**
```
┌─────────────────────────────────────┐
│  ← Add Expense                      │
├─────────────────────────────────────┤
│                                     │
│  Date *                             │
│  ┌─────────────────────────────────┐│
│  │ 25/03/2025                  📅  ││
│  └─────────────────────────────────┘│
│                                     │
│  Amount (₹) *                       │
│  ┌─────────────────────────────────┐│
│  │ ₹ 0.00                          ││
│  └─────────────────────────────────┘│
│                                     │
│  Category *                         │
│  ┌─────────────────────────────────┐│
│  │ Select Category              ▼  ││
│  └─────────────────────────────────┘│
│                                     │
│  Notes (Optional)                   │
│  ┌─────────────────────────────────┐│
│  │                                 ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │          SAVE EXPENSE           ││
│  └─────────────────────────────────┘│
│                                     │
└─────────────────────────────────────┘
```

**State:**
```
- expenseId: Long? (null for add, value for edit)
- date: LocalDate (default: today)
- amount: String
- selectedCategory: Category?
- note: String (default: empty)
- isLoading: Boolean
- isSaved: Boolean
- error: String?
- validationErrors: Map<Field, String>
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| Date | Required, cannot be future date |
| Amount | Required, positive number, max 2 decimal places |
| Category | Required, must select from dropdown |
| Notes | Optional, no validation |

**Behavior:**
- In edit mode, pre-populate fields with existing expense data including note
- On successful save, navigate back
- Show validation errors inline

---

### Screen 3: Expense List

**Route:** `/expense-list`

**Layout:**
```
┌─────────────────────────────────────┐
│  ← All Expenses                     │
├─────────────────────────────────────┤
│  Filter by Date Range               │
│  ┌──────────────┐ ┌──────────────┐  │
│  │ 01/03/2025 📅│→│ 31/03/2025 📅│  │
│  └──────────────┘ └──────────────┘  │
│  [Apply Filter]      [Clear Filter] │
├─────────────────────────────────────┤
│                                     │
│  📅 25 March 2025           ₹400   │
│  ┌─────────────────────────────────┐│
│  │ Food & Dining           ₹250   ││
│  │ Swiggy lunch         [✏️] [🗑️] ││
│  ├─────────────────────────────────┤│
│  │ Transportation          ₹150   ││
│  │                      [✏️] [🗑️] ││
│  └─────────────────────────────────┘│
│                                     │
│  📅 24 March 2025         ₹3,700   │
│  ┌─────────────────────────────────┐│
│  │ Shopping              ₹1,200   ││
│  │                      [✏️] [🗑️] ││
│  ├─────────────────────────────────┤│
│  │ Groceries             ₹2,500   ││
│  │                      [✏️] [🗑️] ││
│  └─────────────────────────────────┘│
│                                     │
│  (Scrollable - grouped by date)     │
│                                     │
└─────────────────────────────────────┘
```

**State:**
```
- expenses: List<GroupedExpenses>
- startDate: LocalDate?
- endDate: LocalDate?
- isFilterApplied: Boolean
- isLoading: Boolean
- error: String?
- showDeleteConfirmation: Boolean
- expenseToDelete: ExpenseEntity?
```

**GroupedExpenses Model:**
```
- date: LocalDate
- expenses: List<ExpenseEntity>
- dayTotal: Double
```

**Features:**
- Date range filter (start date, end date)
- Apply and Clear filter buttons
- Expenses grouped by date with day totals
- Note shown as subtitle under category name when present
- Edit button → navigates to Add Expense in edit mode
- Delete button → shows confirmation dialog
- Empty state when no expenses

---

### Screen 4: Import/Export

**Route:** `/import-export`

**Layout:**
```
┌─────────────────────────────────────┐
│  ← Import / Export                  │
├─────────────────────────────────────┤
│                                     │
│  ═══════════════════════════════    │
│  IMPORT DATA                        │
│  ═══════════════════════════════    │
│                                     │
│  ┌─────────────────────────────────┐│
│  │      📥 SELECT CSV FILE         ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ ℹ️ Expected CSV Format:         ││
│  │                                 ││
│  │ Date,Amount,Category,Notes      ││
│  │ 25/03/2025,250.00,Food & Dining,Swiggy││
│  │ 24/03/2025,150,Transportation,  ││
│  │                                 ││
│  │ • First row must be header      ││
│  │ • Date format: DD/MM/YYYY       ││
│  │ • Notes column is optional      ││
│  │ • Exact duplicates are skipped  ││
│  └─────────────────────────────────┘│
│                                     │
│  ═══════════════════════════════    │
│  EXPORT DATA                        │
│  ═══════════════════════════════    │
│                                     │
│  ☑️ Export all data                 │
│  Export Range (enabled when unchecked above)
│  ┌──────────────┐ ┌──────────────┐  │
│  │ [disabled]  📅│→│[disabled]  📅│  │
│  └──────────────┘ └──────────────┘  │
│                                     │
│  ┌─────────────────────────────────┐│
│  │       📤 EXPORT AS CSV          ││
│  └─────────────────────────────────┘│
│                                     │
└─────────────────────────────────────┘
```

**State:**
```
- isImporting: Boolean
- isExporting: Boolean
- importResult: ImportResult?
- exportResult: ExportResult?
- exportStartDate: LocalDate?
- exportEndDate: LocalDate?
- exportAllData: Boolean (default: true)
- error: String?
```

**Export UX Rules:**
- When `exportAllData = true`: date range fields are disabled and visually greyed out
- When either date field gets focus or is changed: `exportAllData` is automatically set to false and date range fields become enabled
- These two inputs are mutually exclusive; the checkbox takes priority

**ImportResult Model:**
```
- totalRecords: Int
- importedCount: Int
- skippedCount: Int (exact duplicates: same date + amount + category + note)
- errorCount: Int
- errors: List<String>
```

**ExportResult Model:**
```
- success: Boolean
- fileName: String?    (e.g., "expenses_20250325_143022.csv")
- filePath: String?
- recordCount: Int
```

**Features:**
- File picker for CSV selection
- Import progress indicator
- Import summary dialog showing results
- "Export all data" checkbox (default checked)
- Date range pickers enabled only when checkbox is unchecked
- Export as CSV button
- Exported file saved to Downloads with timestamp in filename
- Share intent after export so user can share or move the file

---

## Dependencies

### Gradle Dependencies (App Level)

```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Jetpack Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.9")

    // Instrumented Tests
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

**Removed from v1:** `opencsv`, `poi`, `poi-ooxml`, `hilt-android`, `hilt-compiler`, `hilt-navigation-compose`

### Gradle Plugins Required

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}
```

**Removed from v1:** `com.google.dagger.hilt.android`

---

## Dependency Injection: AppContainer

No Hilt. Use a hand-rolled container on the `Application` class.

```kotlin
// AppContainer.kt
class AppContainer(context: Context) {
    val database: ExpenseDatabase = ExpenseDatabase.create(context)
    val repository: ExpenseRepository = ExpenseRepositoryImpl(database.expenseDao())
}

// ExpenseManagerApp.kt
class ExpenseManagerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
```

ViewModels receive the repository via a `ViewModelProvider.Factory`:

```kotlin
class DashboardViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(klass: Class<T>): T =
        DashboardViewModel(repository) as T
}
```

In Composables, obtain the factory via `LocalContext`:
```kotlin
val app = LocalContext.current.applicationContext as ExpenseManagerApp
val viewModel: DashboardViewModel = viewModel(
    factory = DashboardViewModelFactory(app.container.repository)
)
```

---

## Threading Convention

All database access is `suspend` and runs on `Dispatchers.IO`. ViewModels launch coroutines with `viewModelScope`.

```kotlin
// ViewModel pattern
fun loadExpenses() {
    viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        try {
            val expenses = withContext(Dispatchers.IO) {
                repository.getAllExpenses()
            }
            _state.update { it.copy(expenses = expenses, isLoading = false) }
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }
}
```

All DAO functions are declared as `suspend fun`. UI state is exposed as `StateFlow<ScreenState>`.

---

## CSV Format Specification

### Import Format

**Requirements:**
- File extension: `.csv`
- Encoding: UTF-8
- Delimiter: Comma (,)
- First row: Header (mandatory)

**Header:**
```
Date,Amount,Category,Notes
```

The `Notes` column is optional for backward compatibility. A 3-column file (without Notes) is also accepted; the note field defaults to null.

**Sample Data:**
```csv
Date,Amount,Category,Notes
25/03/2025,250.00,Food & Dining,Swiggy lunch
24/03/2025,1200.50,Shopping,
24/03/2025,150,Transportation,Ola cab
23/03/2025,3000,Bills & Utilities,
```

**Column Specifications:**

| Column | Format | Required | Validation |
|--------|--------|----------|------------|
| Date | DD/MM/YYYY | Yes | Valid date (past or future dates both accepted) |
| Amount | Number | Yes | Positive, up to 2 decimals |
| Category | String | Yes | Match predefined or default to "Others" |
| Notes | String | No | Any text; trimmed; empty treated as null |

**Import Behavior:**
- Skip rows with invalid Date or Amount; continue processing remaining rows
- Duplicate detection: skip a row if an expense with the same `date + amount + category + note` already exists in the database (checked in-memory against a set built before bulk insert, not via DB unique constraint)
- Category matching is case-insensitive; unrecognised values default to "Others"
- Future-dated rows are accepted (no restriction on import)
- Report summary after import: imported / skipped / errors

### Export Format

**CSV Structure:**
```csv
Date,Amount,Category,Notes
25/03/2025,250.00,Food & Dining,Swiggy lunch
24/03/2025,1200.50,Shopping,
```

**File Naming:**
```
expenses_YYYYMMDD_HHmmss.csv
```
Example: `expenses_20250325_143022.csv`

**Save location:** Android `Downloads` folder via `MediaStore` API (Android 10+). After saving, fire a share intent so the user can optionally move or share the file.

---

## Feature Requirements

### Dashboard Features

| Feature | Description |
|---------|-------------|
| Monthly Total | Sum of all expenses for selected month |
| Month Navigation | Previous/Next buttons to change month |
| Category Breakdown | List of categories with totals and percentages |
| Percentage Bar | Visual progress bar; guard against zero total (show 0% bars when month is empty) |
| Recent Transactions | Last 5 expenses across all categories |
| View All Link | Navigate to expense list |

### Add/Edit Expense Features

| Feature | Description |
|---------|-------------|
| Date Picker | Calendar dialog, default today, no future dates allowed |
| Amount Input | Numeric keyboard, INR formatting |
| Category Dropdown | List all 15 predefined categories |
| Notes Input | Optional single-line text field |
| Validation | Real-time validation with error messages |
| Save | Insert new or update existing |
| Auto-navigate | Go back on successful save |

### Expense List Features

| Feature | Description |
|---------|-------------|
| Date Range Filter | Start and end date pickers |
| Apply/Clear Filter | Buttons to control filter |
| Grouped Display | Expenses grouped by date |
| Day Totals | Sum shown for each date group |
| Note Display | Show note as subtitle when present |
| Edit Action | Navigate to edit screen |
| Delete Action | Confirmation dialog before delete |
| Empty State | Message when no expenses |

### Import Features

| Feature | Description |
|---------|-------------|
| File Picker | System file picker for CSV (SAF) |
| Parse CSV | Manual parser in CsvHelper.kt (~40 lines) |
| Validation | Validate each row, collect errors |
| Duplicate Skip | In-memory set comparison before bulk insert |
| Category Matching | Case-insensitive, default to "Others" |
| Import Summary | Dialog showing imported/skipped/errors |

### Export Features

| Feature | Description |
|---------|-------------|
| Date Range (Optional) | Filter data to export; disabled when "Export all" is checked |
| Export All Toggle | Checkbox (default checked); mutually exclusive with date range |
| CSV Export | Manual writer in CsvHelper.kt |
| File Save | Save to Downloads via MediaStore; timestamp-based filename |
| Share | Share intent after save |

---

## Android Manifest Additions

```xml
<!-- Auto Backup: covers the Room database file so data survives a phone swap -->
<application
    android:allowBackup="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules"
    ...>
```

```xml
<!-- res/xml/backup_rules.xml -->
<full-backup-content>
    <include domain="database" path="expenses.db" />
</full-backup-content>

<!-- res/xml/data_extraction_rules.xml (Android 12+) -->
<data-extraction-rules>
    <cloud-backup>
        <include domain="database" path="expenses.db" />
    </cloud-backup>
    <device-transfer>
        <include domain="database" path="expenses.db" />
    </device-transfer>
</data-extraction-rules>
```

---

## Implementation Checklist

### Setup
- [ ] Create Android project with Empty Compose Activity
- [ ] Configure Gradle with all dependencies (no Hilt plugin)
- [ ] Set `minSdk = 29`, `targetSdk = 35`
- [ ] Setup package structure
- [ ] Configure `ExpenseManagerApp` with `AppContainer`
- [ ] Setup Material 3 theme
- [ ] Add Auto Backup XML files and manifest entries

### Data Layer
- [ ] Create `Category` enum with all 15 categories
- [ ] Create `MonthlySummary` and `CategorySummary` data classes
- [ ] Create `ExpenseEntity` with `note` nullable column (schema v1)
- [ ] Create `ExpenseDao` with all required queries (all as `suspend fun`)
- [ ] Create `ExpenseDatabase` with `fallbackToDestructiveMigration()`
- [ ] Create type converters if needed
- [ ] Define `ExpenseRepository` interface
- [ ] Implement `ExpenseRepositoryImpl`

### AppContainer & ViewModelFactories
- [ ] Implement `AppContainer`
- [ ] Create `ViewModelProvider.Factory` for each ViewModel

### Utilities
- [ ] `DateUtils` — formatting, parsing, epoch conversion using `ZoneId.systemDefault()`
- [ ] `CurrencyUtils` — INR formatting
- [ ] `CsvHelper` — manual 4-column parser (import) and writer (export)
- [ ] `FileUtils` — MediaStore save + share intent

### UI Components
- [ ] `ExpenseCard` (shows note as subtitle when present)
- [ ] `CategoryDropdown`
- [ ] `DatePickerField`
- [ ] `AmountTextField`
- [ ] `SummaryCard`
- [ ] `CategoryBreakdownItem`
- [ ] `DateRangeFilter`
- [ ] `LoadingIndicator`
- [ ] `EmptyState`
- [ ] `ErrorMessage`

### Screens
- [ ] Dashboard screen + ViewModel (guard zero total in percentage calc)
- [ ] Add/Edit Expense screen + ViewModel (notes field included)
- [ ] Expense List screen + ViewModel
- [ ] Import/Export screen + ViewModel (export checkbox/date range UX)

### Navigation
- [ ] `Screen` sealed class
- [ ] `NavGraph`
- [ ] `BottomNavBar`
- [ ] `MainActivity`

### Testing
- [ ] Unit tests: `CsvHelper` — valid rows, missing notes column, invalid date, invalid amount, duplicate set logic
- [ ] Unit tests: `DateUtils` — epoch conversion round-trips in device timezone
- [ ] Unit tests: category matching — case-insensitive, unknown defaults to Others
- [ ] Instrumented tests: DAO CRUD operations against in-memory Room database
- [ ] Instrumented tests: date range query correctness
- [ ] Instrumented tests: bulk insert (no unique constraint — confirm duplicates allowed)

---

## Implementation Notes

1. **Date Handling:** Store dates as epoch milliseconds using the **device timezone**:
   ```kotlin
   fun LocalDate.toEpochMillis(): Long =
       atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

   fun Long.toLocalDate(): LocalDate =
       Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
   ```

2. **Duplicate Detection on Import:** Before the bulk insert, query all existing expenses in one call and build an in-memory `HashSet<String>` of `"$date|$amount|$category|$note"` keys. Skip any CSV row whose key is already in the set.

3. **Category Matching:** Case-insensitive comparison:
   ```kotlin
   Category.entries.firstOrNull { it.displayName.lowercase() == csvValue.lowercase() }
       ?: Category.OTHERS
   ```

4. **Manual CSV Parser:** Use `BufferedReader` with `split(",", limit = 4)` for a 4-column parse. Trim each field. If only 3 columns are present, treat note as null.

5. **File Operations:** Use `MediaStore.Downloads` for saving (Android 10+). After insert, fire:
   ```kotlin
   Intent(Intent.ACTION_VIEW).apply {
       setDataAndType(exportedUri, "text/csv")
       addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
   }
   ```

6. **Currency Formatting:** `NumberFormat.getCurrencyInstance(Locale("en", "IN"))` for INR.

7. **Month Calculations:** `YearMonth` class for month navigation and start/end date derivation.

8. **Zero Total Guard:** In category breakdown percentage calculation:
   ```kotlin
   val pct = if (monthlyTotal > 0) (categoryTotal / monthlyTotal * 100) else 0.0
   ```

9. **ProGuard:** No special rules needed — POI and OpenCSV are removed.
```
