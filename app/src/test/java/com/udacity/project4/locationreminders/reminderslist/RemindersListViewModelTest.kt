package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.lang.Thread.sleep
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [29])
class RemindersListViewModelTest {



    //TODO: provide testing to the RemindersListViewModel and its live data objects
    lateinit var app: Application
    lateinit var viewModel: RemindersListViewModel
    lateinit var fakeDataSource: FakeDataSource
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

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext() as Application
         fakeDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(app, fakeDataSource)
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `when list values are present and loadReminders is invoked then reminder list should have the list values`() =
        runBlockingTest {
            fakeDataSource.remindersList = validReminders
            assert(viewModel.remindersList.value == null)
            viewModel.loadReminders()
//            assert(viewModel.showLoading.value == true)
            delay(100)
            (viewModel.remindersList.value as MutableList<ReminderDataItem>?)?.toDTO()
                ?.containsAll(validReminders)
                ?.let { assert(it) }


            assert( viewModel.shouldReturnError.value == false)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `when list values are not present and loadReminders is invoked then reminder list should be null and showNoData MutableLiveData should be true`() =
        runBlockingTest {
            assert(viewModel.remindersList.value == null)
            viewModel.loadReminders()
            assert(viewModel.showLoading.value == true)
            delay(100)
            assert((viewModel.remindersList.value as MutableList<ReminderDataItem>?)?.toDTO() == mutableListOf<ReminderDTO>())
            viewModel.showNoData.value?.let { assert(it) }

            assert( viewModel.shouldReturnError.value == true)
            assert(viewModel.showLoading.value == false)

        }

    @Test
    @ExperimentalCoroutinesApi
    fun `when there is an error should return error`() =  runBlockingTest {
        fakeDataSource.remindersList.addAll(validReminders)
        fakeDataSource.shouldReturnError = true
        viewModel.loadReminders()
        delay(100)
        assert(viewModel.remindersList.value == null)
        viewModel.showNoData.value?.let { assert(it) }
        assert( viewModel.shouldReturnError.value == true)

    }


    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        stopKoin()

        Dispatchers.resetMain()
    }


}