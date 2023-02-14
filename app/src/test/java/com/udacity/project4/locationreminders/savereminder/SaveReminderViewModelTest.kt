package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.util.*
import kotlin.random.Random

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    val validReminderItem = ReminderDataItem(
        "reminder",
        "This is a reminder",
        "Toronto",
        Random.nextDouble(0.0, 90.0),
        Random.nextDouble(0.0, 90.0),
    )

    val invalidReminderItem = ReminderDataItem(
        null,
        "Description",
        "Toronto",
        1000.0,
        1000.0,
    )

    lateinit var app: Application
    lateinit var viewModel: SaveReminderViewModel

    private val testDispatcher = StandardTestDispatcher()


    //TODO: provide testing to the SaveReminderView and its live data objects
    @Before
    fun setup() {
        app = getApplicationContext() as Application
        viewModel = SaveReminderViewModel(app, FakeDataSource())
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `when validateAndSaveReminder receives a valid reminder return saved is true`() = runTest {
        assert(viewModel.validateAndSaveReminder(validReminderItem))
    }

    @Test
    fun `when validateAndSaveReminder receives an invalid reminder return saved is false`() = runTest(){
        assert(!viewModel.validateAndSaveReminder(invalidReminderItem))
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }



}