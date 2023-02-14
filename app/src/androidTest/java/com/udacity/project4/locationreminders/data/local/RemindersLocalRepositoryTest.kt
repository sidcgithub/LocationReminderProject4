package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {


    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // TODO replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        val newReminder = ReminderDTO("title", "description", "Toronto",0.0,0.0)
        localDataSource.saveReminder(newReminder)

        val result = localDataSource.getReminder(newReminder.id)

        Assert.assertThat(result.succeeded, `is`(true))
        result as Result.Success
        Assert.assertThat(result.data.title, `is`("title"))
        Assert.assertThat(result.data.description, `is`("description"))
    }
}