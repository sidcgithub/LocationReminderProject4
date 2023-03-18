package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.DisableAnimationsRule
import com.udacity.project4.util.monitorActivity
import kotlinx.android.synthetic.main.activity_reminders.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import kotlin.random.Random


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    @get:Rule
    private val disableAnimationsRule = DisableAnimationsRule()

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    //    TODO: add End to End testing to the app
    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    private val validReminderItem = ReminderDataItem(
        "reminder",
        "This is a reminder",
        "Toronto",
        Random.nextDouble(0.0, 90.0),
        Random.nextDouble(0.0, 90.0),
    )

    @Test
    fun testSuccessFullFlowHappyPathTillSaveAndBack() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        var activity: RemindersActivity? = null
        activityScenario.onActivity {
            activity = it
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(typeText("Test Reminder Title"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Test Reminder Description"))


        val saveViewModel: SaveReminderViewModel = get()
        runOnUiThread {
            validReminderItem.apply {
                saveViewModel.latitude.value = latitude
                saveViewModel.longitude.value = longitude
                saveViewModel.reminderSelectedLocationStr.value = location
            }
        }
        closeSoftKeyboard()



        onView(withId(R.id.saveReminder)).perform(click())
        var toastResult: ViewInteraction? = null
        runOnUiThread {

            toastResult = onView(withText(R.string.reminder_saved)).inRoot(
                withDecorView(
                    not(
                        `is`(
                            activity?.window?.decorView
                        )
                    )
                )
            )

        }

        toastResult?.check(
            matches(
                isDisplayed()
            )
        )

        onView(withText(validReminderItem.location)).check(matches(isDisplayed()))
        closeSoftKeyboard()





        onView(withText("Test Reminder Title")).check(matches(isDisplayed()))
        activityScenario.close()

    }

    @Test
    fun testSuccessFullFlowEmptyTitleTillErrorSnackbar() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderDescription)).perform(typeText("Test Reminder Description"))


        val saveViewModel: SaveReminderViewModel = get()
        runOnUiThread {
            validReminderItem.apply {
                saveViewModel.latitude.value = latitude
                saveViewModel.longitude.value = longitude
                saveViewModel.reminderSelectedLocationStr.value = location
            }
        }
        closeSoftKeyboard()



        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))





        onView(withText(validReminderItem.location)).check(matches(isDisplayed()))
        closeSoftKeyboard()

        activityScenario.close()

    }

    @Test
    fun testSuccessFullFlowNoSelectedLocationTillErrorSnackbar() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(typeText("Test Reminder Title"))

        onView(withId(R.id.reminderDescription)).perform(typeText("Test Reminder Description"))


        closeSoftKeyboard()



        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))


        delay(1000)


        activityScenario.close()

    }

}
