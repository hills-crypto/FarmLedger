package com.example.farmledger.data

// Based on common small-scale practice in Zambia; confirm with a vet or agricultural extension officer. Always follow the product label.

// Generates automated health, vaccination, and management reminders based on project type, variety, and start date.
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
                    title = "Deworm all goats (e.g. albendazole – follow the label dose) – $name",
                    dueDate = start + (3 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Start tick control – dip or spray (e.g. amitraz); repeat every 1–2 weeks in the rainy season – $name",
                    dueDate = start + (7 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Vaccinate – pulpy kidney (enterotoxaemia) + tetanus, 1st dose – $name",
                    dueDate = start + (14 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Check and trim hooves – $name",
                    dueDate = start + (30 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Vaccinate – pulpy kidney + tetanus booster – $name",
                    dueDate = start + (42 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Deworm (every 3 months) – $name",
                    dueDate = start + (93 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Deworm (every 3 months) – $name",
                    dueDate = start + (183 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Deworm (every 3 months) – $name",
                    dueDate = start + (273 * DAY_IN_MS)
                ),
                Reminder(
                    projectId = project.id,
                    title = "Yearly vaccine booster (pulpy kidney + tetanus; anthrax if your vet advises) – $name",
                    dueDate = start + (365 * DAY_IN_MS)
                )
            )

            ProjectType.GARDEN -> {
                when (project.variety.trim().lowercase()) {
                    "tomato" -> listOf(
                        Reminder(
                            projectId = project.id,
                            title = "Stake and tie the plants – $name",
                            dueDate = start + (10 * DAY_IN_MS)
                        ),
                        Reminder(
                            projectId = project.id,
                            title = "Start fungicide spray against blight (e.g. mancozeb); repeat every 7–14 days in wet weather – $name",
                            dueDate = start + (14 * DAY_IN_MS)
                        ),
                        Reminder(
                            projectId = project.id,
                            title = "Top-dress (e.g. CAN) – $name",
                            dueDate = start + (21 * DAY_IN_MS)
                        ),
                        Reminder(
                            projectId = project.id,
                            title = "Check for Tuta absoluta and whitefly – spray if seen (approved insecticide, follow the label) – $name",
                            dueDate = start + (30 * DAY_IN_MS)
                        ),
                        Reminder(
                            projectId = project.id,
                            title = "Expected first harvest – $name",
                            dueDate = start + (75 * DAY_IN_MS)
                        )
                    )

                    "cabbage" -> listOf(
                        Reminder(
                            projectId = project.id,
                            title = "Check for diamondback moth and aphids – spray if seen (e.g. cypermethrin) – $name",
                            dueDate = start + (14 * DAY_IN_MS)
                        ),
                        Reminder(
                            projectId = project.id,
                            title = "1st top-dress (Urea or Ammonium Nitrate) – $name",
                            dueDate = start + (21 * DAY_IN_MS)
                        ),
                        Reminder(
                            projectId = project.id,
                            title = "2nd top-dress – $name",
                            dueDate = start + (42 * DAY_IN_MS)
                        ),
                        Reminder(
                            projectId = project.id,
                            title = "Expected harvest – $name",
                            dueDate = start + (80 * DAY_IN_MS)
                        )
                    )

                    "other" -> listOf(
                        Reminder(
                            projectId = project.id,
                            title = "Expected harvest – add your own spray and fertiliser reminders in the Reminders screen – $name",
                            dueDate = start + (60 * DAY_IN_MS)
                        )
                    )

                    else -> listOf(
                        // Default to Rape schedule if variety is "Rape" or unspecified
                        Reminder(
                            projectId = project.id,
                            title = "Top-dress with Urea or Ammonium Nitrate – $name",
                            dueDate = start + (14 * DAY_IN_MS)
                        ),
                        Reminder(
                            projectId = project.id,
                            title = "Check for aphids and diamondback moth – spray if seen (e.g. cypermethrin; keep the pre-harvest days on the label) – $name",
                            dueDate = start + (21 * DAY_IN_MS)
                        ),
                        Reminder(
                            projectId = project.id,
                            title = "Start harvesting leaves – $name",
                            dueDate = start + (35 * DAY_IN_MS)
                        )
                    )
                }
            }
        }
    }
}
