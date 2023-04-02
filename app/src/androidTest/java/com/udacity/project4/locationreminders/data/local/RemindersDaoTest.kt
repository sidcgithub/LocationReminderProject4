package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var remindersDao: RemindersDao
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RemindersDatabase::class.java).allowMainThreadQueries().build()
        remindersDao = database.reminderDao()
    }

    @After
    fun tearDown() {
        stopKoin()
        database.close()
    }

    val validReminders: MutableList<ReminderDTO> = mutableListOf(
        ReminderDataItem(
            "reminder",
            "This is a reminder",
            "Toronto",
            Random.nextDouble(0.0, 90.0),
            Random.nextDouble(0.0, 90.0),
            "0"
        ),
        ReminderDataItem(
            "reminder",
            "This is a reminder",
            "Toronto",
            Random.nextDouble(0.0, 90.0),
            Random.nextDouble(0.0, 90.0),
            "1"
        ),
        ReminderDataItem(
            "reminder",
            "This is a reminder",
            "Toronto",
            Random.nextDouble(0.0, 90.0),
            Random.nextDouble(0.0, 90.0),
            "2"
        )
    ).toDTO()


    fun List<ReminderDataItem>.toDTO() = this.map {
        ReminderDTO(
            id = it.id,
            title = it.title,
            description = it.description,
            location = it.location,
            latitude = it.latitude,
            longitude = it.longitude
        )
    } as MutableList<ReminderDTO>


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getReminderById_notFound_returnsNull() = runTest {
        val nonExistentReminderId = "non_existent_id"
        val result = remindersDao.getReminderById(nonExistentReminderId)
        assertThat(result).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun insertReminder_and_getReminderById() = runTest {

        val reminder = validReminders[0]

        remindersDao.saveReminder(reminder)

        val result = remindersDao.getReminderById(reminder.id)
        assertThat(result).isEqualTo(reminder)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun insertMultipleReminders_and_getReminders() = runTest {
        val reminder1 = validReminders[0]
        val reminder2 = validReminders[1]
        val reminder3 = validReminders[2]

        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)
        remindersDao.saveReminder(reminder3)

        val result = remindersDao.getReminders()
        assertThat(result).containsExactly(reminder1, reminder2, reminder3)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAllReminders_and_getReminders_returnsEmptyList() = runTest {
        val reminder1 = validReminders[0]
        val reminder2 = validReminders[1]
        val reminder3 = validReminders[2]

        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)
        remindersDao.saveReminder(reminder3)

        remindersDao.deleteAllReminders()

        val result = remindersDao.getReminders()
        assertThat(result).isEmpty()
    }
}