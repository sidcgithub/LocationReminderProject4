package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()
    val shouldReturnError = MutableLiveData(false)
    init {
        invalidateShowNoData()
    }

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch (Dispatchers.IO) {
            //interacting with the dataSource has to be through a coroutine
            when (val result = dataSource.getReminders()) {
                is Result.Success<List<ReminderDTO>> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    })
                    remindersList.postValue(dataList)
                    invalidateShowNoData()
                    shouldReturnError.postValue(showNoData.value == true)
                    showLoading.postValue(false)

                }
                is Result.Error -> {
                    shouldReturnError.postValue(true)
                    showSnackBar.postValue(result.message)
                    showLoading.postValue(false)
                }
            }

        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.postValue(remindersList.value == null || remindersList.value!!.isEmpty())
    }
}