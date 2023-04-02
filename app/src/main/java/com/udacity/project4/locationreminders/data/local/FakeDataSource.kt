package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.delay

public class FakeDataSource(    var remindersList: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        delay(100)
        return if (shouldReturnError) {
            Result.Error("Failed to get reminders")
        } else {
            Result.Success(remindersList)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        delay(100)

        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        delay(100)

        if (shouldReturnError) {
            return Result.Error("Failed to get reminder")
        }

        val reminder = remindersList.find { it.id == id }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder not found")
        }
    }

    override suspend fun deleteAllReminders() {
        delay(100)

        remindersList.clear()

    }
}