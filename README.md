# FarmLedger

FarmLedger helps small-scale farmers record farm expenses, sales and livestock health so they can track vaccination dates, collect unpaid balances and know the true profit of each project.

## Student details

| Full Name | Student Number |
|-----------|----------------|
| Mwanabaleya Lweendo Charles | 2300917 |

Mode: ODL · Programme: BSc Software Engineering · Course: BSE 3340 Mobile Application Development · Working alone.

## The problem

Small-scale farmers raising broilers, goats or vegetables often struggle to determine whether their farming operations make a true profit. Operational costs for feed, labor, and vaccines are incurred over several weeks, while sales occur in small amounts and frequently on credit. Because records are kept in memory or informal exercise books, vaccination dates are easily missed, and unpaid customer debts are forgotten over time.

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

## How to build and run

1. Clone the repository from GitHub.
2. Open the project root folder in Android Studio.
3. Allow Gradle to finish syncing project dependencies.
4. Connect an Android phone via USB/Wireless Debugging or launch an emulator running Android 7.0 (API level 24) or newer.
5. Click Run 'app' and allow notification permissions when prompted on Android 13+.

## Reminder schedule

Based on common small-scale practice in Zambia; confirm with a vet or agricultural extension officer. Always follow the product label.

### Poultry Schedule
| Day | Scheduled Task |
|---|---|
| Day 7 | Newcastle (ND/IB) vaccine |
| Day 14 | Gumboro vaccine |
| Day 21 | Newcastle booster |
| Day 35 | Expected selling date |

### Goats Schedule (Day 0 = arrival / project start)
| Day | Scheduled Task |
|---|---|
| Day 3 | Deworm all goats (e.g. albendazole – follow the label dose) |
| Day 7 | Start tick control – dip or spray (e.g. amitraz); repeat every 1–2 weeks in the rainy season |
| Day 14 | Vaccinate – pulpy kidney (enterotoxaemia) + tetanus, 1st dose |
| Day 30 | Check and trim hooves |
| Day 42 | Vaccinate – pulpy kidney + tetanus booster |
| Day 93 | Deworm (every 3 months) |
| Day 183 | Deworm (every 3 months) |
| Day 273 | Deworm (every 3 months) |
| Day 365 | Yearly vaccine booster (pulpy kidney + tetanus; anthrax if your vet advises) |

### Garden Schedule – Rape
| Day | Scheduled Task |
|---|---|
| Day 14 | Top-dress with Urea or Ammonium Nitrate |
| Day 21 | Check for aphids and diamondback moth – spray if seen (e.g. cypermethrin; keep the pre-harvest days on the label) |
| Day 35 | Start harvesting leaves |

### Garden Schedule – Tomato
| Day | Scheduled Task |
|---|---|
| Day 10 | Stake and tie the plants |
| Day 14 | Start fungicide spray against blight (e.g. mancozeb); repeat every 7–14 days in wet weather |
| Day 21 | Top-dress (e.g. CAN) |
| Day 30 | Check for Tuta absoluta and whitefly – spray if seen (approved insecticide, follow the label) |
| Day 75 | Expected first harvest |

### Garden Schedule – Cabbage
| Day | Scheduled Task |
|---|---|
| Day 14 | Check for diamondback moth and aphids – spray if seen (e.g. cypermethrin) |
| Day 21 | 1st top-dress (Urea or Ammonium Nitrate) |
| Day 42 | 2nd top-dress |
| Day 80 | Expected harvest |

### Garden Schedule – Other
| Day | Scheduled Task |
|---|---|
| Day 60 | Expected harvest – add your own spray and fertiliser reminders in the Reminders screen |

## Research Note

### 1. Local database with Room

#### What it is
Room is an official Android Jetpack persistence library that provides an abstraction layer over SQLite. It performs compile-time verification of SQL queries to ensure type safety and converts raw SQLite table rows directly into strongly-typed Kotlin objects.

#### Why my app needs it
Farmers operating in rural agricultural areas frequently lack stable cellular network coverage, making cloud-only data storage unreliable. FarmLedger requires an offline-first architecture where every project, ledger entry, debt balance, and reminder is saved locally on the device and persists through app restarts or device reboots. Crucial financial metrics, including project profit/loss, remaining livestock counts, and total money owed, are dynamically derived through reactive SQL queries.

#### Where I learned it
I learned how to implement Room from the official Android developer documentation on [Save data in a local database using Room](https://developer.android.com/training/data-storage/room) and the Google Developer Codelab [Persist data with Room](https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room).

#### What went wrong and how I fixed it
- Combining six Room Flows in one combine() would not compile, because Kotlin's typed combine() takes at most five flows. I fixed it by nesting two combine() calls of three flows each.
- A SUM over zero rows returns NULL in SQL, which would crash a Double. I wrapped every SUM in COALESCE(..., 0).
- I could not name my entity "Transaction" (it clashes with Room's @Transaction annotation, and "transaction" is an SQL keyword), so I named it LedgerEntry, in a table called "transactions".
- Because my app has one activity and a hand-written back stack, every ViewModel was stored in the same place. Adding a transaction to a second project reused the first project's ViewModel and could save the sale to the wrong project, and old form values stayed filled in. I fixed it by giving each ViewModel a key with the project id ("add-tx-<id>") and resetting the form after saving.
- The date picker returns midnight UTC, but a project created today stores the exact time, so a transaction dated on the start day was wrongly rejected. I fixed it by comparing whole days instead of milliseconds, and added unit tests for it.

### 2. Scheduled notifications with WorkManager

#### What it is
WorkManager is the recommended Android Jetpack library for deferrable, guaranteed background work that must execute even if the application process exits or the device is rebooted.

#### Why my app needs it
Farmers risk significant financial loss when livestock health, vaccination, or deworming schedules are forgotten. When a farmer creates a project, FarmLedger calculates key management milestones based on the project type and start date, scheduling background notifications to alert the farmer on due dates.

#### Where I learned it
I learned WorkManager implementation from the official Android documentation on [Persistent background tasks with WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent), the [Schedule tasks with WorkManager](https://developer.android.com/codelabs/basic-android-kotlin-compose-workmanager) Codelab, and the [Notification permissions guide](https://developer.android.com/develop/ui/views/notifications/notification-permission).

#### What went wrong and how I fixed it
- Notifications are disabled by default on Android 13+ (API 33+), so I requested the POST_NOTIFICATIONS runtime permission on app startup; if the user refuses, background notifications are skipped gracefully while the in-app Reminders list still displays all reminders.
- Overdue reminders for an older project would all fire background notifications at once if scheduled with zero delay. I fixed it by checking the calendar day index before scheduling; past-day reminders are not enqueued in WorkManager and instead display in red with an "(Overdue)" label on screen.
- Deleting a project removes its reminder rows from SQLite due to foreign key CASCADE rules, but the scheduled WorkManager background jobs remained active in the system scheduler. I fixed it by explicitly calling cancelUniqueWork("reminder-$id") for each of the project's reminders prior to executing the database delete operation.

#### Why WorkManager and not AlarmManager
WorkManager automatically persists scheduled background work to its internal database and reschedules tasks after device reboots. In contrast, AlarmManager exact alarms require special system permissions, are cancelled on device shutdown, and can drain device battery. WorkManager's initial delay provides a minimum execution window rather than an exact-second alarm, which is ideal for "due today" agricultural reminders.

### Sources

- https://developer.android.com/training/data-storage/room
- https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room
- https://developer.android.com/develop/background-work/background-tasks/persistent
- https://developer.android.com/codelabs/basic-android-kotlin-compose-workmanager
- https://developer.android.com/develop/ui/views/notifications/notification-permission

## How I used AI tools

I wrote the app's specification myself in AGENTS.md: the problem, the scope, the database design, the validation rules and the reminder schedules from my own farming experience. I used Android Studio's AI agent to write code from that specification, one step at a time. After each step I reviewed the code, tested it on my phone, and found and fixed problems. For example, sales could be saved to the wrong project, a valid date was rejected, and credit sales were missing the number of birds sold. Every design decision is mine, and I can explain every file.

## Version 2 ideas

Login and worker accounts, daily logs (mortality, feed, eggs), PDF reports for funders, charts, cloud sync.
