package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import kotlin.random.Random

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersDao: RemindersDao
    private lateinit var database: RemindersDatabase
    private lateinit var remindersRepository: RemindersLocalRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        remindersDao = database.reminderDao()
        remindersRepository = RemindersLocalRepository(remindersDao, Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        stopKoin()
        database.close()
    }


    private val validReminders: MutableList<ReminderDTO> = mutableListOf(
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



    // TODO replace with runBlockingTest once issue is resolved
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveReminder_retrievesReminder() = runTest {
        val newReminder = validReminders[0]
        remindersRepository.saveReminder(newReminder)

        val result = remindersRepository.getReminder(newReminder.id)

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`(newReminder.title))
        assertThat(result.data.description, `is`(newReminder.description))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getReminders_returnsAllReminders() = runTest {
        val reminder1 = validReminders[0]
        val reminder2 = validReminders[1]
        val reminder3 = validReminders[2]

        remindersRepository.saveReminder(reminder1)
        remindersRepository.saveReminder(reminder2)
        remindersRepository.saveReminder(reminder3)

        val result = remindersRepository.getReminders()

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data, containsInAnyOrder(reminder1, reminder2, reminder3))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAllReminders_and_getReminders_returnsEmptyList() = runTest {
        val reminder1 = validReminders[0]
        val reminder2 = validReminders[1]
        val reminder3 = validReminders[2]

        remindersRepository.saveReminder(reminder1)
        remindersRepository.saveReminder(reminder2)
        remindersRepository.saveReminder(reminder3)

        remindersRepository.deleteAllReminders()

        val result = remindersRepository.getReminders()

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data, `is`(emptyList()))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getReminder_invalidId_returnsError() = runTest {
        val invalidId = "invalid_id"

        val result = remindersRepository.getReminder(invalidId)

        assertThat(result.succeeded, `is`(false))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }
}
