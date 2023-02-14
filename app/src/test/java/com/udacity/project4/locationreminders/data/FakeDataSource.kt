package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.delay
import kotlin.random.Random

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(val remindersList: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        delay(1000)
        return Result.Success(remindersList)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        delay(500)
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        delay(500)
        return Result.Success(remindersList.first { it.id == id })
    }

    override suspend fun deleteAllReminders() {
        delay(500)
        remindersList.clear()

    }


}