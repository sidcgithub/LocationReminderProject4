package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.DispatcherProvider
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.S])
class RemindersListViewModelTest() : KoinTest {



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
    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineScopeRule()
    lateinit var fakeDataSource: FakeDataSource


    @Before
    fun setup() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val testModule: Module = module {
            single<DispatcherProvider> { TestDispatcherProvider(mainCoroutineRule.testDispatcher) }
            viewModel { RemindersListViewModel(context as Application, fakeDataSource, get() as DispatcherProvider) }
        }
        startKoin {
            androidContext(context)
            modules(testModule)
        }
    }
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `when list values are present and loadReminders is invoked then reminder list should have the list values`() = mainCoroutineRule.testDispatcher.runBlockingTest {
        //TODO: provide testing to the RemindersListViewModel and its live data objects


        val viewModel: RemindersListViewModel = get()


         fakeDataSource.shouldReturnError = false

        fakeDataSource.remindersList = validReminders
        viewModel.executeLoadReminders()
        assert(viewModel.showLoading.value == true)
        advanceUntilIdle()
        val dataBool = viewModel.remindersList.value?.toDTO()?.containsAll(validReminders)  == true
        assert(dataBool)
        assert(viewModel.showLoading.value == false)

    }
    @Test
    fun `when there is an error should return error`() = mainCoroutineRule.testDispatcher.runBlockingTest {

        val viewModel: RemindersListViewModel = get()
        fakeDataSource.remindersList.addAll(validReminders)
        fakeDataSource.shouldReturnError = true

        viewModel.executeLoadReminders()
        assert(viewModel.remindersList.value == mutableListOf<ReminderDataItem>())
        advanceUntilIdle()

        assert(viewModel.shouldReturnError.value == true)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `when list values are not present and loadReminders is invoked then reminder list should be null and showNoData MutableLiveData should be true`() = mainCoroutineRule.testDispatcher.runBlockingTest {
        val viewModel: RemindersListViewModel = get()
        fakeDataSource.shouldReturnError = false
        fakeDataSource.remindersList = mutableListOf()

        viewModel.executeLoadReminders()
        assert(viewModel.showLoading.value == true)


        advanceUntilIdle()


        assert((viewModel.remindersList.value!!.toDTO() == mutableListOf<ReminderDTO>()))
        assert(viewModel.showNoData.value == true)
        assert(viewModel.showLoading.value == false)

    }


}


@ExperimentalCoroutinesApi
class MainCoroutineScopeRule(
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider(private val testDispatcher: TestCoroutineDispatcher) :
    DispatcherProvider {

    override fun default() = testDispatcher
    override fun io() = testDispatcher
    override fun main() = testDispatcher
}