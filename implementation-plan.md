# Implementation Plan — Personal Expense Manager

> Reference: `plan_v2.md`  
> Tasks are ordered so every dependency is complete before it is needed.

## Status Legend

| Mark | Meaning |
|------|---------|
| `[ ]` | Not started |
| `[~]` | In progress |
| `[x]` | Complete |

---

## Phase 1 — Project Setup

| # | Status | Task | Target File / Location |
|---|--------|------|------------------------|
| 1.1 | [x] | Create Android project with Empty Compose Activity | Android Studio wizard |
| 1.2 | [x] | Add all Gradle dependencies and plugins (see plan_v2.md § Dependencies) | `app/build.gradle.kts` |
| 1.3 | [x] | Set `minSdk = 29`, `targetSdk = 35`, `applicationId = "com.personal.expensemanager"` | `app/build.gradle.kts` |
| 1.4 | [x] | Create Auto Backup rules file | `res/xml/backup_rules.xml` |
| 1.5 | [x] | Create data extraction rules file (Android 12+) | `res/xml/data_extraction_rules.xml` |
| 1.6 | [x] | Add `allowBackup`, `fullBackupContent`, `dataExtractionRules` to manifest | `AndroidManifest.xml` |
| 1.7 | [x] | Register `ExpenseManagerApp` as `android:name` in manifest | `AndroidManifest.xml` |

---

## Phase 2 — Domain Models

> These are plain data classes with no Android dependencies — implement first so all later layers can reference them.

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 2.1 | [x] | Create `Category` enum with 15 entries and a `displayName` property | `model/Category.kt` |
| 2.2 | [x] | Create `MonthlySummary` data class (`yearMonth`, `total`) | `model/MonthlySummary.kt` |
| 2.3 | [x] | Create `CategorySummary` data class (`category`, `total`, `percentage`) | `model/CategorySummary.kt` |

---

## Phase 3 — Data Layer

> Build bottom-up: entity → DAO → converters → database → repository.

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 3.1 | [x] | Create `ExpenseEntity` Room entity with all columns (`id`, `date`, `amount`, `category`, `note`, `created_at`, `updated_at`) and `@Index` on `date` and `category` | `data/local/entity/ExpenseEntity.kt` |
| 3.2 | [x] | Create `ExpenseDao` with all 9 required `suspend fun` queries (see plan_v2.md § DAO Queries) | `data/local/dao/ExpenseDao.kt` |
| 3.3 | [x] | Create `Converters` for any Room type conversion needed (e.g. Category enum ↔ String) | `data/local/database/Converters.kt` |
| 3.4 | [x] | Create `ExpenseDatabase` Room database class with `fallbackToDestructiveMigration()` and schema version 1 | `data/local/database/ExpenseDatabase.kt` |
| 3.5 | [x] | Define `ExpenseRepository` interface with all data access methods | `data/repository/ExpenseRepository.kt` |
| 3.6 | [x] | Implement `ExpenseRepositoryImpl` delegating to `ExpenseDao` | `data/repository/ExpenseRepositoryImpl.kt` |

---

## Phase 4 — App Wiring (DI + Application)

> Depends on Phase 3 (needs `ExpenseDatabase` and `ExpenseRepository`).

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 4.1 | [x] | Implement `AppContainer` holding `ExpenseDatabase` and `ExpenseRepository` singletons | `AppContainer.kt` |
| 4.2 | [x] | Implement `ExpenseManagerApp` extending `Application`, initialising `AppContainer` in `onCreate` | `ExpenseManagerApp.kt` |

---

## Phase 5 — Utilities

> Depends on Phase 2 (`Category` needed by `CsvHelper`). No UI dependencies — safe to implement before any screen.

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 5.1 | [x] | Implement `DateUtils` with `LocalDate.toEpochMillis()` and `Long.toLocalDate()` using `ZoneId.systemDefault()`, plus `DD/MM/YYYY` formatter and parser | `util/DateUtils.kt` |
| 5.2 | [x] | Implement `CurrencyUtils` with INR formatter using `NumberFormat.getCurrencyInstance(Locale("en", "IN"))` | `util/CurrencyUtils.kt` |
| 5.3 | [x] | Implement `Constants` (e.g. recent transactions limit, CSV header string) | `util/Constants.kt` |
| 5.4 | [x] | Implement `CsvHelper` with manual 4-column `BufferedReader` parser (import) and writer (export); handle 3-column fallback for Notes | `util/CsvHelper.kt` |
| 5.5 | [x] | Implement `FileUtils` with `MediaStore.Downloads` save method (Android 10+) and share intent builder; timestamp-based filename `expenses_YYYYMMDD_HHmmss.csv` | `util/FileUtils.kt` |

---

## Phase 6 — Theme

> No dependencies. Set up once so all Composables use the correct colours and typography.

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 6.1 | [x] | Define Material 3 colour scheme (light + dark) | `presentation/theme/Color.kt` |
| 6.2 | [x] | Define typography scale | `presentation/theme/Type.kt` |
| 6.3 | [x] | Create `ExpenseManagerTheme` composable wrapping `MaterialTheme` | `presentation/theme/Theme.kt` |

---

## Phase 7 — Shared UI Components

> Depends on Phase 5 (DateUtils, CurrencyUtils) and Phase 6 (Theme). Build all reusable components before assembling screens.

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 7.1 | [x] | `LoadingIndicator` — centred `CircularProgressIndicator` composable | `presentation/components/LoadingIndicator.kt` |
| 7.2 | [x] | `EmptyState` — icon + message composable for empty list states | `presentation/components/EmptyState.kt` |
| 7.3 | [x] | `ErrorMessage` — inline error text composable | `presentation/components/ErrorMessage.kt` |
| 7.4 | [x] | `AmountTextField` — numeric input with ₹ prefix, max 2 decimal validation | `presentation/components/AmountTextField.kt` |
| 7.5 | [x] | `DatePickerField` — tappable field that opens Material 3 date picker dialog | `presentation/components/DatePickerField.kt` |
| 7.6 | [x] | `CategoryDropdown` — `ExposedDropdownMenuBox` listing all 15 categories | `presentation/components/CategoryDropdown.kt` |
| 7.7 | [x] | `ExpenseCard` — single expense row with category, amount, optional note subtitle, edit and delete icon buttons | `presentation/components/ExpenseCard.kt` |
| 7.8 | [x] | `SummaryCard` — monthly total card with month label and prev/next navigation buttons | `presentation/components/SummaryCard.kt` |
| 7.9 | [x] | `CategoryBreakdownItem` — category row with total amount and linear progress bar percentage | `presentation/components/CategoryBreakdownItem.kt` |
| 7.10 | [x] | `DateRangeFilter` — two `DatePickerField`s (start, end) with Apply and Clear buttons | `presentation/components/DateRangeFilter.kt` |

---

## Phase 8 — Navigation Shell

> Depends on Phase 6. Create route definitions before building screens so each screen can reference correct routes.

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 8.1 | [x] | Create `Screen` sealed class with routes for Dashboard, AddExpense, EditExpense(id), ExpenseList, ImportExport | `presentation/navigation/Screen.kt` |
| 8.2 | [x] | Create `BottomNavBar` composable with 4 tabs (Home, Add, List, Import) | `presentation/navigation/BottomNavBar.kt` |

---

## Phase 9 — Screens

> Depends on Phases 3–8. Build each screen with its ViewModel and State class. Each ViewModel gets the repository from `AppContainer` via a `ViewModelProvider.Factory`.

### 9A — Dashboard Screen

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 9A.1 | [x] | Define `DashboardState` data class (`selectedMonth`, `monthlyTotal`, `categoryBreakdown`, `recentTransactions`, `isLoading`, `error`) | `presentation/screens/dashboard/DashboardState.kt` |
| 9A.2 | [x] | Implement `DashboardViewModel` with month navigation, monthly total query, category breakdown query (with zero-total guard), and recent 5 transactions query | `presentation/screens/dashboard/DashboardViewModel.kt` |
| 9A.3 | [x] | Implement `DashboardScreen` composable using `SummaryCard`, `CategoryBreakdownItem` list, `ExpenseCard` list, `BottomNavBar`, loading and error states | `presentation/screens/dashboard/DashboardScreen.kt` |

### 9B — Add / Edit Expense Screen

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 9B.1 | [x] | Define `AddExpenseState` data class (`expenseId`, `date`, `amount`, `selectedCategory`, `note`, `isLoading`, `isSaved`, `error`, `validationErrors`) | `presentation/screens/addexpense/AddExpenseState.kt` |
| 9B.2 | [x] | Implement `AddExpenseViewModel` with save (insert or update), load-by-id for edit mode, and validation logic (future date blocked, amount positive, category required) | `presentation/screens/addexpense/AddExpenseViewModel.kt` |
| 9B.3 | [x] | Implement `AddExpenseScreen` composable with `DatePickerField`, `AmountTextField`, `CategoryDropdown`, Notes `TextField`, save button, inline validation errors; pre-populate fields in edit mode | `presentation/screens/addexpense/AddExpenseScreen.kt` |

### 9C — Expense List Screen

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 9C.1 | [x] | Define `ExpenseListState` data class (`groupedExpenses`, `startDate`, `endDate`, `isFilterApplied`, `isLoading`, `error`, `showDeleteConfirmation`, `expenseToDelete`) | `presentation/screens/expenselist/ExpenseListState.kt` |
| 9C.2 | [x] | Implement `ExpenseListViewModel` with load-all, apply/clear date range filter, delete with confirmation, and group-by-date + day-total logic | `presentation/screens/expenselist/ExpenseListViewModel.kt` |
| 9C.3 | [x] | Implement `ExpenseListScreen` composable with `DateRangeFilter`, `LazyColumn` of date-grouped `ExpenseCard`s with day totals, delete confirmation `AlertDialog`, empty state | `presentation/screens/expenselist/ExpenseListScreen.kt` |

### 9D — Import / Export Screen

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 9D.1 | [x] | Define `ImportExportState` data class (`isImporting`, `isExporting`, `importResult`, `exportResult`, `exportStartDate`, `exportEndDate`, `exportAllData`, `error`) | `presentation/screens/importexport/ImportExportState.kt` |
| 9D.2 | [x] | Implement `ImportExportViewModel` with: SAF file-picker result handler → `CsvHelper` parse → in-memory duplicate check → bulk insert → `ImportResult`; export query → `CsvHelper` write → `FileUtils` save → share intent | `presentation/screens/importexport/ImportExportViewModel.kt` |
| 9D.3 | [x] | Implement `ImportExportScreen` composable with CSV file picker button, format hint card, export-all checkbox (disables date pickers when checked), date range pickers, export CSV button, import result `AlertDialog` | `presentation/screens/importexport/ImportExportScreen.kt` |

---

## Phase 10 — Navigation Wiring

> Depends on all screens being built (Phase 9).

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 10.1 | [x] | Implement `NavGraph` composable wiring all 4 routes with arguments (edit expense id) and `BottomNavBar` | `presentation/navigation/NavGraph.kt` |
| 10.2 | [x] | Implement `MainActivity` setting content to `ExpenseManagerTheme { NavGraph(...) }` | `MainActivity.kt` |

---

## Phase 11 — Testing

> Unit tests run on JVM; instrumented tests require a device/emulator.

### Unit Tests (JVM)

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 11.1 | [x] | `CsvHelper`: valid 4-column row, valid 3-column row (note = null), invalid date, invalid amount, empty row skipped | `test/.../CsvHelperTest.kt` |
| 11.2 | [x] | `DateUtils`: `toEpochMillis` → `toLocalDate` round-trip stays on same date in device timezone | `test/.../DateUtilsTest.kt` |
| 11.3 | [x] | Category matching: exact match, case-insensitive match, unknown value defaults to OTHERS | `test/.../CategoryMatchingTest.kt` |
| 11.4 | [x] | Duplicate detection: set correctly skips existing key; allows same amount on different date | `test/.../DuplicateDetectionTest.kt` |

### Instrumented Tests (Device / Emulator)

| # | Status | Task | Target File |
|---|--------|------|-------------|
| 11.5 | [x] | DAO: insert, read, update, delete single expense | `androidTest/.../ExpenseDaoTest.kt` |
| 11.6 | [x] | DAO: date range query returns correct subset | `androidTest/.../ExpenseDaoTest.kt` |
| 11.7 | [x] | DAO: bulk insert allows two rows with same date/amount/category (no unique constraint) | `androidTest/.../ExpenseDaoTest.kt` |
| 11.8 | [x] | DAO: category breakdown totals and ordering | `androidTest/.../ExpenseDaoTest.kt` |

---

## Summary

| Phase | Tasks | Done |
|-------|-------|------|
| 1 — Project Setup | 7 | 7 |
| 2 — Domain Models | 3 | 3 |
| 3 — Data Layer | 6 | 6 |
| 4 — App Wiring | 2 | 2 |
| 5 — Utilities | 5 | 5 |
| 6 — Theme | 3 | 3 |
| 7 — UI Components | 10 | 10 |
| 8 — Navigation Shell | 2 | 2 |
| 9 — Screens | 12 | 12 |
| 10 — Navigation Wiring | 2 | 2 |
| 11 — Testing | 8 | 8 |
| **Total** | **60** | **60** |
