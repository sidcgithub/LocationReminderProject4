package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        val task = ReminderDTO("title", "description", "Toronto",0.0,0.0)
        database.reminderDao().saveReminder(task)

        val loaded = database.reminderDao().getReminderById(task.id)

        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(task.id))
        assertThat(loaded.title, `is`(task.title))
        assertThat(loaded.description, `is`(task.description))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        val task = ReminderDTO("title", "description", "Toronto",0.0,0.0)
        database.reminderDao().saveReminder(task)
        val loaded = database.reminderDao().getReminders()
        assertThat(loaded.size, `is`(1))

          database.reminderDao().deleteAllReminders()
        val cleared = database.reminderDao().getReminders()
        assertThat(cleared.size, `is`(0))
    }

}