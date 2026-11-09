package com.example.farmledger

import com.example.farmledger.data.Project
import com.example.farmledger.data.ProjectType
import com.example.farmledger.data.ReminderSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Unit test suite verifying schedule generation logic for goats and crop garden projects.
class ReminderScheduleTest {

    private val now = System.currentTimeMillis()

    // Verifies that a Goats project generates exactly 9 pre-scheduled reminders.
    @Test
    fun goatsProject_generatesNineReminders() {
        val project = Project(
            id = 1L,
            name = "Goat Herd 1",
            type = ProjectType.GOATS,
            quantity = 20,
            startDate = now,
            budget = 5000.0
        )
        val reminders = ReminderSchedule.remindersFor(project)
        assertEquals(9, reminders.size)
    }

    // Verifies that a Tomato garden project generates reminders containing mancozeb fungicide.
    @Test
    fun tomatoGarden_containsMancozebReminder() {
        val project = Project(
            id = 2L,
            name = "Tomato Field 1",
            type = ProjectType.GARDEN,
            quantity = 100,
            startDate = now,
            budget = 3000.0,
            variety = "Tomato"
        )
        val reminders = ReminderSchedule.remindersFor(project)
        assertEquals(5, reminders.size)
        val hasMancozeb = reminders.any { it.title.contains("mancozeb", ignoreCase = true) }
        assertTrue(hasMancozeb)
    }

    // Verifies that an 'Other' crop garden project generates 1 harvest reminder.
    @Test
    fun otherGarden_generatesOneReminder() {
        val project = Project(
            id = 3L,
            name = "Onion Patch",
            type = ProjectType.GARDEN,
            quantity = 50,
            startDate = now,
            budget = 1500.0,
            variety = "Other"
        )
        val reminders = ReminderSchedule.remindersFor(project)
        assertEquals(1, reminders.size)
        assertTrue(reminders[0].title.contains("Expected harvest", ignoreCase = true))
    }
}
