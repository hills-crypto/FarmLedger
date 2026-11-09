# FarmLedger

FarmLedger helps small-scale farmers record farm expenses, sales and livestock health so they can track vaccination dates, collect unpaid balances and know the true profit of each project.

## Student details

| Full name | Student number | Mode | Programme | Course |
|---|---|---|---|---|
| Mwanabaleya Lweendo Charles | 2300917 | ODL | BSc Software Engineering | BSE 3340 Mobile Application Development |

## The problem

Small-scale farmers raising broilers, goats or vegetables often struggle to know whether their projects make a true profit. Operational costs for feed, vaccines and labor are spread over many weeks, while sales are small and frequently made on credit. When financial records are kept in memory or informal exercise books, vaccination dates are easily missed, and unpaid customer debts are forgotten over time.

## Features (version 1)

- Create farming projects (Poultry, Goats, Garden) with start date, quantity and budget
- Record expenses and sales, including credit sales with the customer's name
- Automatic profit/loss and animals remaining per project
- Customers & Debts: who owes money, record payments, call the customer
- Automatic reminders from the project type and start date, with phone notifications
- Works fully offline (all data on the phone)

## Screens

| Screen | Description |
|---|---|
| **Dashboard** | View active farming projects, overall profit/loss, total money owed, and the next three upcoming reminders. |
| **Add Project** | Enter new project details including name, category (Poultry/Goats/Garden), batch size, start date, and budget. |
| **Project Details** | Monitor total sales, expenses, calculated profit, remaining animal count, next reminder, and transaction history. |
| **Add Transaction** | Record individual expense categories or sales transactions with cash/credit payment type and customer details. |
| **Customers & Debts** | Review customers with outstanding credit balances, record cash payments, and initiate phone calls directly. |
| **Reminders** | Track scheduled vaccination, deworming, or harvest tasks with overdue status highlights and manual completion toggles. |

Screenshots: see /screenshots (added after final testing).

## Demo video

YouTube (unlisted): LINK TO BE ADDED

## Tech stack

Kotlin, Jetpack Compose, Material 3, single activity with a hand-written back stack, MVVM (Screen → ViewModel → Repository → Room), Room 2.8 with KSP, WorkManager, JUnit tests for the form validation. minSdk 24.

## How to run

1. Clone the repository.
2. Open the folder in Android Studio.
3. Let Gradle sync.
4. Run on a phone or emulator with Android 7.0 (API 24) or newer.
5. On Android 13+, allow notifications when asked.

## Research Note

### 1. Local database with Room

- **What it is**: An Android Jetpack library that sits on top of SQLite. It checks SQL queries at compile time when the app is built and provides typed Kotlin data objects instead of raw database cursors.
- **Why my app needs it**: Farmers in rural areas often have unreliable or no internet connection, so every expense, sale, debt and reminder must be saved locally on the phone and survive the app closing or the phone restarting. Key calculations like project profit, total money owed, and remaining animals are derived directly via SQL queries.
- **How I used it**: I created five database entities (Project, LedgerEntry in table "transactions", Customer, Payment, Reminder), DAOs with `@Query` annotations, foreign keys with `CASCADE` delete rules, and reactive queries returning `Flow` so screens update automatically when data changes. Every aggregate `SUM` is wrapped in `COALESCE(..., 0)`. The customer balance is calculated as credit sales minus payments using two subqueries rather than a `JOIN` to avoid double-counting.
- **What I learned**: I named the entity `LedgerEntry` rather than `Transaction` because `Transaction` clashes with Room's `@Transaction` annotation. I used `fallbackToDestructiveMigration(true)` during development and learned that proper migration scripts would be required before deploying to real farmers in production.

### 2. Scheduled notifications with WorkManager

- **What it is**: The official Android library for persistent background work that must run reliably even if the app is closed or the device restarts.
- **Why my app needs it**: Farmers risk losing livestock when they forget crucial vaccination or deworming dates. When a project is saved, the app automatically generates scheduled reminders based on animal or crop type and start date, using WorkManager to trigger a phone notification on the due date.
- **How I used it**: I created a `OneTimeWorkRequest` for each reminder using `setInitialDelay(dueDate - now)` and enqueued it with a unique name `"reminder-<id>"` to prevent duplicate jobs. Marking a reminder as done cancels its scheduled background work. A notification channel is initialized at app startup, and the `POST_NOTIFICATIONS` runtime permission is requested on Android 13+.
- **What I learned**: WorkManager is better suited than `AlarmManager` for this use case because it persists scheduled work and automatically reschedules background tasks after phone reboots. The initial delay acts as a minimum execution window rather than an exact-minute alarm, which is sufficient for daily vaccination reminders. Even if a user denies notification permissions, scheduled reminders remain visible inside the app's Reminders screen since they are stored permanently in Room.

### Sources

- https://developer.android.com/training/data-storage/room
- https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room
- https://developer.android.com/develop/background-work/background-tasks/persistent
- https://developer.android.com/codelabs/basic-android-kotlin-compose-workmanager
- https://developer.android.com/develop/ui/views/notifications/notification-permission

## Version 2 ideas

Login and worker accounts, daily logs (mortality, feed, eggs), PDF reports for funders, charts, cloud sync.
