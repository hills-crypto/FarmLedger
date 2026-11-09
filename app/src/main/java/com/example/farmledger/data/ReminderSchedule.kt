package com.example.farmledger.data

// Note: This is an example reminder schedule. Always confirm vaccination and management dates with a local veterinarian or agricultural extension officer.

// Generates automated health, vaccination, and harvest reminders based on project type and start date.
object ReminderSchedule {
    private const val DAY_IN_MS = 24L * 60L * 60L * 1000L

    // Returns a list of pre-scheduled reminders calculated relative to the project start date.
    fun remindersFor(project: Project): List<Reminder> {
        val start = project.startDate
        val name = project.name

        return when (project.type) {
            ProjectType.POULTRY -> listOf(
                Reminder(
                    projectId = project.id,
                    title = "Newcastle (ND/IB) vaccine – $name",
                    dueDate = start + (7 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Gumboro vaccine – $name",
                    dueDate = start + (14 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Newcastle booster – $name",
                    dueDate = start + (21 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Expected selling date – $name",
                    dueDate = start + (35 * DAY_IN_MS)
                )
            )

            ProjectType.GOATS -> listOf(
                Reminder(
                    projectId = project.id,
                    title = "Deworming – $name",
                    dueDate = start + (14 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Vaccination check – $name",
                    dueDate = start + (30 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Repeat deworming – $name",
                    dueDate = start + (90 * DAY_IN_MS)
                )
            )

            ProjectType.GARDEN -> listOf(
                Reminder(
                    projectId = project.id,
                    title = "Spraying – $name",
                    dueDate = start + (14 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Expected harvest – $name",
                    dueDate = start + (75 * DAY_IN_MS)
                )
            )
        }
    }
}
