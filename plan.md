```markdown
# Personal Expense Manager - Android App Development Plan

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
1. Store personal expenses with date, amount, and category
2. Import expenses via CSV file
3. Add expenses manually via UI
4. Export data in CSV and XLSX formats
5. Dashboard with monthly summary and category breakdown
6. Filter expenses by date range
7. Edit and delete existing expenses

---

## Technical Specifications

| Specification | Value |
|---------------|-------|
| **Language** | Kotlin |
| **Min SDK** | API 35 (Android 15) |
| **Target SDK** | API 35 |
| **Architecture** | MVVM + Clean Architecture |
| **UI Framework** | Jetpack Compose |
| **Design System** | Material Design 3 |
| **Database** | Room (SQLite) |
| **DI Framework** | Hilt |

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
│   │       └── ExpenseEntity.kt
│   ├── 📂 repository
│   │   └── ExpenseRepositoryImpl.kt
│   └── 📂 mapper
│       └── ExpenseMapper.kt
│
├── 📂 domain
│   ├── 📂 model
│   │   ├── Expense.kt
│   │   ├── Category.kt
│   │   ├── MonthlySummary.kt
│   │   └── CategorySummary.kt
│   ├── 📂 repository
│   │   └── ExpenseRepository.kt
│   └── 📂 usecase
│       ├── AddExpenseUseCase.kt
│       ├── UpdateExpenseUseCase.kt
│       ├── DeleteExpenseUseCase.kt
│       ├── GetExpensesUseCase.kt
│       ├── GetExpensesByDateRangeUseCase.kt
│       ├── GetMonthlySummaryUseCase.kt
│       ├── GetCategoryBreakdownUseCase.kt
│       ├── ImportExpensesUseCase.kt
│       ├── ExportToCsvUseCase.kt
│       └── ExportToExcelUseCase.kt
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
├── 📂 di
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
│
├── 📂 util
│   ├── CsvHelper.kt
│   ├── ExcelHelper.kt
│   ├── DateUtils.kt
│   ├── CurrencyUtils.kt
│   ├── FileUtils.kt
│   └── Constants.kt
│
├── 📄 MainActivity.kt
└── 📄 ExpenseManagerApp.kt
```

---

## Database Schema

### Table: expenses

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `date` | INTEGER | NOT NULL | Date as timestamp (epoch millis at start of day) |
| `amount` | REAL | NOT NULL | Expense amount in INR |
| `category` | TEXT | NOT NULL | Category enum name (e.g., "FOOD_DINING") |
| `created_at` | INTEGER | NOT NULL | Record creation timestamp |
| `updated_at` | INTEGER | NOT NULL | Record last update timestamp |

### Constraints & Indexes

```sql
-- Unique constraint for duplicate detection during import
UNIQUE(date, amount, category)

-- Indexes for query performance
INDEX(date)
INDEX(category)
```

### Required DAO Queries

| Query | Purpose |
|-------|---------|
| Insert single expense | Add new expense (IGNORE on conflict for duplicates) |
| Insert multiple expenses | Bulk import (IGNORE on conflict for duplicates) |
| Update expense | Edit existing expense |
| Delete expense | Remove expense |
| Get all expenses | Ordered by date DESC |
| Get expenses by date range | For filtering |
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
- recentTransactions: List<Expense> (limit 5)
- isLoading: Boolean
- error: String?
```

**Features:**
- Month selector with previous/next navigation
- Total expense display for selected month
- Category-wise breakdown with percentage bars
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

**Behavior:**
- In edit mode, pre-populate fields with existing expense data
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
│  │                      [✏️] [🗑️] ││
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
- expenseToDelete: Expense?
```

**GroupedExpenses Model:**
```
- date: LocalDate
- expenses: List<Expense>
- dayTotal: Double
```

**Features:**
- Date range filter (start date, end date)
- Apply and Clear filter buttons
- Expenses grouped by date with day totals
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
│  │ Date,Amount,Category            ││
│  │ 25/03/2025,250.00,Food & Dining ││
│  │ 24/03/2025,150,Transportation   ││
│  │                                 ││
│  │ • First row must be header      ││
│  │ • Date format: DD/MM/YYYY       ││
│  │ • Duplicates will be skipped    ││
│  └─────────────────────────────────┘│
│                                     │
│  ═══════════════════════════════    │
│  EXPORT DATA                        │
│  ═══════════════════════════════    │
│                                     │
│  Export Range (Optional)            │
│  ┌──────────────┐ ┌──────────────┐  │
│  │ Start Date 📅│→│ End Date  📅 │  │
│  └──────────────┘ └──────────────┘  │
│  ☑️ Export all data                 │
│                                     │
│  ┌─────────────────────────────────┐│
│  │       📤 EXPORT AS CSV          ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │      📤 EXPORT AS EXCEL         ││
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

**ImportResult Model:**
```
- totalRecords: Int
- importedCount: Int
- skippedCount: Int (duplicates)
- errorCount: Int
- errors: List<String>
```

**ExportResult Model:**
```
- success: Boolean
- filePath: String?
- recordCount: Int
```

**Features:**
- File picker for CSV selection
- Import progress indicator
- Import summary dialog showing results
- Date range selection for export (optional)
- Checkbox to export all data
- Export as CSV button
- Export as Excel (.xlsx) button
- Share/save exported file

---

## Dependencies

### Gradle Dependencies (App Level)

```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
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
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // CSV Parsing (OpenCSV)
    implementation("com.opencsv:opencsv:5.9")
    
    // Excel Support (Apache POI)
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    
    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### Gradle Plugins Required

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}
```

---

## CSV Format Specification

### Import Format

**Requirements:**
- File extension: `.csv`
- Encoding: UTF-8
- Delimiter: Comma (,)
- First row: Header (mandatory)

**Header (exact):**
```
Date,Amount,Category
```

**Sample Data:**
```csv
Date,Amount,Category
25/03/2025,250.00,Food & Dining
24/03/2025,1200.50,Shopping
24/03/2025,150,Transportation
23/03/2025,3000,Bills & Utilities
```

**Column Specifications:**

| Column | Format | Required | Validation |
|--------|--------|----------|------------|
| Date | DD/MM/YYYY | Yes | Valid date, not future |
| Amount | Number | Yes | Positive, up to 2 decimals |
| Category | String | Yes | Match predefined or default to "Others" |

**Import Behavior:**
- Skip rows with invalid data and continue processing
- Skip duplicate entries (same date + amount + category)
- Report summary after import (imported/skipped/errors)

### Export Format

**CSV Structure:**
```csv
Date,Amount,Category
25/03/2025,250.00,Food & Dining
24/03/2025,1200.50,Shopping
```

**Excel Structure (.xlsx):**
- Sheet name: "Expenses"
- Header row: Bold, gray background
- Columns: Date, Amount, Category
- Amount formatted as currency (₹)
- Date formatted as DD/MM/YYYY

---

## Feature Requirements

### Dashboard Features

| Feature | Description |
|---------|-------------|
| Monthly Total | Sum of all expenses for selected month |
| Month Navigation | Previous/Next buttons to change month |
| Category Breakdown | List of categories with totals and percentages |
| Percentage Bar | Visual progress bar for each category |
| Recent Transactions | Last 5 expenses across all categories |
| View All Link | Navigate to expense list |

### Add/Edit Expense Features

| Feature | Description |
|---------|-------------|
| Date Picker | Calendar dialog, default today, no future dates |
| Amount Input | Numeric keyboard, INR formatting |
| Category Dropdown | List all 15 predefined categories |
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
| Edit Action | Navigate to edit screen |
| Delete Action | Confirmation dialog before delete |
| Empty State | Message when no expenses |

### Import Features

| Feature | Description |
|---------|-------------|
| File Picker | System file picker for CSV |
| Parse CSV | Using OpenCSV library |
| Validation | Validate each row, collect errors |
| Duplicate Skip | Skip if same date+amount+category exists |
| Category Matching | Case-insensitive, default to "Others" |
| Import Summary | Dialog showing results |

### Export Features

| Feature | Description |
|---------|-------------|
| Date Range (Optional) | Filter data to export |
| Export All Toggle | Checkbox to export all data |
| CSV Export | Using OpenCSV library |
| Excel Export | Using Apache POI library |
| File Save | Save to Downloads or share |

---

## Implementation Checklist

### Setup
- [ ] Create Android project with Empty Compose Activity
- [ ] Configure Gradle with all dependencies
- [ ] Setup package structure
- [ ] Configure Hilt application class
- [ ] Setup Material 3 theme

### Domain Layer
- [ ] Create Category enum with all 15 categories
- [ ] Create Expense domain model
- [ ] Create MonthlySummary model
- [ ] Create CategorySummary model
- [ ] Create ExpenseRepository interface
- [ ] Create all UseCase classes

### Data Layer
- [ ] Create ExpenseEntity
- [ ] Create ExpenseDao with all queries
- [ ] Create ExpenseDatabase
- [ ] Create type converters
- [ ] Create ExpenseMapper
- [ ] Implement ExpenseRepositoryImpl
- [ ] Setup Hilt modules

### Utilities
- [ ] Create DateUtils (formatting, parsing, epoch conversion)
- [ ] Create CurrencyUtils (INR formatting)
- [ ] Create CsvHelper (parse and write CSV)
- [ ] Create ExcelHelper (write Excel)
- [ ] Create FileUtils (file operations)

### UI Components
- [ ] Create ExpenseCard
- [ ] Create CategoryDropdown
- [ ] Create DatePickerField
- [ ] Create AmountTextField
- [ ] Create SummaryCard
- [ ] Create CategoryBreakdownItem
- [ ] Create DateRangeFilter
- [ ] Create LoadingIndicator
- [ ] Create EmptyState
- [ ] Create ErrorMessage

### Screens
- [ ] Implement Dashboard screen with ViewModel
- [ ] Implement Add/Edit Expense screen with ViewModel
- [ ] Implement Expense List screen with ViewModel
- [ ] Implement Import/Export screen with ViewModel

### Navigation
- [ ] Create Screen sealed class
- [ ] Create NavGraph
- [ ] Create BottomNavBar
- [ ] Setup MainActivity

### Testing
- [ ] Test CRUD operations
- [ ] Test CSV import with valid/invalid data
- [ ] Test duplicate detection
- [ ] Test CSV export
- [ ] Test Excel export
- [ ] Test date range filtering
- [ ] Test edge cases

---

## Notes for Claude Code

1. **Date Handling:** Store dates as epoch milliseconds (Long) in database. Use `LocalDate` in domain/presentation layers.

2. **Duplicate Detection:** Use `OnConflictStrategy.IGNORE` in Room insert to skip duplicates automatically.

3. **Category Matching:** Implement case-insensitive matching using `lowercase()` comparison during CSV import.

4. **File Operations:** Use Android's Storage Access Framework (SAF) for file picking and saving.

5. **Error Handling:** Wrap all database and file operations in try-catch, propagate errors to UI via State.

6. **Currency Formatting:** Use `NumberFormat.getCurrencyInstance(Locale("en", "IN"))` for INR formatting.

7. **Month Calculations:** Use `YearMonth` class for month navigation and calculating month start/end dates.

8. **Proguard Rules:** Add keep rules for Apache POI and OpenCSV if using minification.
```