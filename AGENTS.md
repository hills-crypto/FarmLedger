# FarmLedger – context for AI coding agents

Read this before helping with this project.

## Who and why
- Student: Charles, BSc Software Engineering (distance learning).
- Course: BSE 3340 Mobile Application Development. Tools required: Android Studio + Kotlin.
- **Submission deadline: Monday 9 November 2026 (today).** Speed and a working app matter more than polish.
- **The project is submitted through GitHub.** The repository must contain the full Android Studio
  project and a README.md (purpose statement, features, screens, tech stack, how to run, research
  topics: Room and WorkManager). Commit and push after every working step; never commit secrets,
  `local.properties`, `build/` folders or `.idea/` workspace files (the default Android `.gitignore` handles this).
- After submission there is an **oral interview**: Charles must explain every part of the code himself.
  So: keep code simple, explain what each change does in plain language, and do not introduce
  advanced patterns (Hilt, multi-module, Flow operators beyond the basics) unless asked.

## What the app does (one sentence)
FarmLedger helps small-scale farmers record farm expenses, sales and livestock health so they can
track vaccination dates, collect unpaid balances and know the true profit of each project.

## Version 1 scope (the definition of "done")
1. Create a farming project (e.g. "400 Broilers – Batch 3") and record its expenses and sales,
   including credit sales with the customer's name, saved in a local Room database.
2. Automatically calculate profit/loss per project, and list customers who still owe money,
   with "Record Payment".
3. Automatically create vaccination/selling reminders from animal type + start date, and send a
   phone notification on the due date using WorkManager.

Out of scope (version 2): login, multiple users/worker accounts, daily logs (mortality/feed/eggs),
PDF export, charts, cloud sync. Do not add these.

## Tech stack (do not change)
- Kotlin, Jetpack Compose, Material 3. Single activity (`MainActivity`).
- Architecture: Composable screen → ViewModel → Repository → Room DAO → SQLite.
  "Events go up, data comes down." ViewModels never hold a `Context`.
- Room **2.8.x with KSP** (NOT Room 3 / `androidx.room3`, NOT kapt). No `room-ktx`.
- WorkManager `work-runtime` (no `-ktx`). minSdk 24.
- Collect state with `collectAsStateWithLifecycle()`.
- Navigation: **hand-written back stack**, no navigation library. In `MainActivity.kt`:
  `sealed interface Screen` (Dashboard, AddProject, ProjectDetails(projectId), AddTransaction(projectId),
  Customers, Reminders), `mutableStateListOf<Screen>`, `BackHandler` for system back,
  `when (current)` picks the screen. Screens receive lambdas (e.g. `onOpenProject: (Long) -> Unit`),
  never the back stack.
- Testing on a physical phone via **wireless debugging** (USB port/cable is faulty).

## Six screens
1. Dashboard – active projects, total profit/loss, total money owed, next 3 reminders, "+ New Project".
2. Add/Edit Project – name, type (POULTRY/GOATS/GARDEN), quantity, start date, budget (Kwacha).
   Saving also generates reminders for that type.
3. Project Details – totals (expenses, sales, profit, animals remaining), transactions newest first,
   next reminder, "Add Transaction".
4. Add Transaction – the main validated form (rules below).
5. Customers & Debts – name, phone, balance, oldest unpaid date, total owed, Record Payment
   dialog, call button (`Intent.ACTION_DIAL`, no permission needed).
6. Reminders – sorted by date, overdue in red, mark Done (cancels its WorkManager job), add custom.

## Database (version 1)
| Entity | Fields |
|---|---|
| Project | id, name, type, quantity, startDate (Long epoch ms), budget |
| LedgerEntry (table "transactions") | id, projectId, kind (EXPENSE/SALE), category, amount, quantity, date, note, paymentType (CASH/CREDIT), customerId (nullable) |
| Customer | id, name, phone |
| Payment | id, customerId, amount, date |
| Reminder | id, projectId, title, dueDate, isDone, isCustom |

Naming: do NOT name the entity `Transaction` (clashes with Room's `@Transaction`).
Calculations (wrap every SUM in `COALESCE(..., 0)`):
- profit = SUM(sales) − SUM(expenses) per project
- animals remaining = quantity − SUM(quantity sold)
- customer balance = SUM(credit sales) − SUM(payments) (use two subqueries, not a JOIN)
- total owed = sum of positive customer balances
During development `fallbackToDestructiveMigration()` is acceptable.

## Add Transaction validation (done in the ViewModel, errors shown via `supportingText`)
- Amount: not empty, number > 0 → "Please enter a valid amount greater than zero."
- Category (expense): required → "Please choose an expense category."
- Date: not in the future, not before project start → "Date must be between the project start date and today."
- Customer name: required when Credit → "Enter the customer's name for a credit sale."
- Quantity sold: ≤ animals remaining → "You only have N birds left in this batch."
Add Project: name not empty; quantity and budget positive.

## Reminders
- One `OneTimeWorkRequest` per reminder, `setInitialDelay(dueDate - now)`,
  `enqueueUniqueWork("reminder-$id", ExistingWorkPolicy.REPLACE, ...)`; mark done → `cancelUniqueWork`.
- Create a notification channel at startup; request `POST_NOTIFICATIONS` on Android 13+.
- For testing, use a short delay (10–30 seconds).

## Branding
Colours: Leaf Green #2E6B3A, Deep Green #1B4224, Harvest Gold #E8A317, Soil Brown #7A5230,
Cream #FAF7EF, Alert Red #B3261E. Dynamic colour off.

## Build progress
- [x] Step 1: Navigation skeleton with six placeholder screens – runs on phone.
- [ ] Step 1b: Create GitHub repository and push the project (then push after every step).
- [x] Step 2: Gradle – Room (KSP), lifecycle-viewmodel-compose, lifecycle-runtime-compose, WorkManager.
- [x] Step 3: Entities, DAOs, database, repository, AppContainer.
- [ ] Step 4: Add Project + Dashboard (real).
- [ ] Step 5: Project Details + Add Transaction with validation.
- [ ] Step 6: Customers & Debts + Record Payment.
- [ ] Step 7: Reminders + WorkManager notifications.
- [ ] Step 8: Test the demo journey (Mr Banda: 50 birds, K7,500 credit, pays K5,000 → balance K2,500), write README.md, final push to GitHub, submit the repository link.

Update the checkboxes as steps are completed.
