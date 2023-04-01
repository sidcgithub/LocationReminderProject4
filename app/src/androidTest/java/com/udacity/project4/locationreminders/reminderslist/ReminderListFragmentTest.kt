package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.DefaultDispatcherProvider
import com.udacity.project4.DispatcherProvider
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidTestRepository
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.


    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application



    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()

        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    repository,
                    get()
                )
            }

            single<DispatcherProvider> { DefaultDispatcherProvider() }

            single {
                SaveReminderViewModel(
                    appContext,
                    repository
                )
            }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }


        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }


    }

    @After
    fun cleanupDb() = runBlockingTest {
        repository.deleteAllReminders()
    }

    @Test
    fun clickAddReminderButton_navigateToAddEditFragment() = runBlockingTest{
        val reminder1 = ReminderDTO("TITLE1", "DESCRIPTION1", "Location 1", 0.0,longitude = 0.0)
        val reminder2 = ReminderDTO("TITLE2", "DESCRIPTION2", "Location 2", 90.0,longitude = 0.0)
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {

            Navigation.setViewNavController(it.requireView(), navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()

        )
    }



    @Test
    fun viewList_savedItemsDisplayed() = runBlockingTest {
        val reminder1 = ReminderDTO("TITLE1", "DESCRIPTION1", "Location 1", 0.0,longitude = 0.0)
        val reminder2 = ReminderDTO("TITLE2", "DESCRIPTION2", "Location 2", 90.0,longitude = 0.0)
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {

            Navigation.setViewNavController(it.requireView(), navController)
        }




        onView(ViewMatchers.withText(reminder1.title)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        onView(ViewMatchers.withText(reminder2.title)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        scenario.close()
    }

    @Test
    fun viewList_noItemError() = runBlockingTest {
        repository.deleteAllReminders()

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {

            Navigation.setViewNavController(it.requireView(), navController)
        }




        onView(ViewMatchers.withText("No reminders")).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        scenario.close()
    }
}