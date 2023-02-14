package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    lateinit var app: Application
    lateinit var viewModel: RemindersListViewModel
    private val validReminders: MutableList<ReminderDTO> = mutableListOf(
        ReminderDataItem(
            "reminder",
            "This is a reminder",
            "Toronto",
            Random.nextDouble(0.0, 90.0),
            Random.nextDouble(0.0, 90.0),
        ),
        ReminderDataItem(
            "reminder",
            "This is a reminder",
            "Toronto",
            Random.nextDouble(0.0, 90.0),
            Random.nextDouble(0.0, 90.0),
        ),
        ReminderDataItem(
            "reminder",
            "This is a reminder",
            "Toronto",
            Random.nextDouble(0.0, 90.0),
            Random.nextDouble(0.0, 90.0),
        )
    ).toDTO()

    private fun MutableList<ReminderDataItem>.toDTO() = this.map {
        ReminderDTO(
            title = it.title,
            description = it.description,
            location = it.location,
            latitude = it.latitude,
            longitude = it.longitude
        )
    } as MutableList<ReminderDTO>

    private val testDispatcher = StandardTestDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext() as Application
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `when list values are present and loadReminders is invoked then reminder list should have the list values`() =
        runTest {
            viewModel = RemindersListViewModel(app, FakeDataSource(validReminders))
            assert(viewModel.remindersList.value == null)
            viewModel.loadReminders()
            (viewModel.remindersList.value as MutableList<ReminderDataItem>?)?.toDTO()
                ?.containsAll(validReminders)
                ?.let { assert(it) }
        }

    @Test
    fun `when list values are not present and loadReminders is invoked then reminder list should be null and showNoData MutableLiveData should be true`() =
        runTest {
            viewModel = RemindersListViewModel(app, FakeDataSource())
            assert(viewModel.remindersList.value == null)
            viewModel.loadReminders()
            assert( viewModel.shouldReturnError.value == true)
            assert((viewModel.remindersList.value as MutableList<ReminderDataItem>?)?.toDTO() == null)
            viewModel.showNoData.value?.let { assert(it) }
        }

    @Test
    fun `when the list is empty should return error`() =  runTest {


    }


    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        stopKoin()

        Dispatchers.resetMain()
    }


}