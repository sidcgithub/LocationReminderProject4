package com.udacity.project4.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.utils.SingleLiveEvent

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {

    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()
    val showErrorMessage: MutableLiveData<String> = MutableLiveData()
    val showSnackBar: MutableLiveData<String> = MutableLiveData()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: MutableLiveData<String> = MutableLiveData()
    val showLoading: MutableLiveData<Boolean> = MutableLiveData()
    val showNoData: MutableLiveData<Boolean> = MutableLiveData()

}