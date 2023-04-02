package com.udacity.project4

import android.app.Application
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            single<DispatcherProvider> { DefaultDispatcherProvider() }
            viewModel {
                RemindersListViewModel(get(), get() as ReminderDataSource, get() as DispatcherProvider)
            }
            //Declare singleton definitions to be later injected using by inject()
            viewModel {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(this@MyApp) }

        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }
}

interface DispatcherProvider {
    fun default(): CoroutineDispatcher
    fun io(): CoroutineDispatcher
    fun main(): CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override fun default() = Dispatchers.Default
    override fun io() = Dispatchers.IO
    override fun main() = Dispatchers.Main
}

