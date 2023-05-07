package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class SaveReminderFragment : BaseFragment() {

    companion object {
        const val ACTION_GEOFENCE_EVENT = "action-geofence-event"
        const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 200
        const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 201

        private const val FINE_LOCATION_PERMISSION_INDEX = 0
        private const val COARSE_LOCATION_PERMISSION_INDEX = 1
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 2
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 1234
    }

    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE )
        } else {
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
    lateinit var geofencingClient: GeofencingClient

    private var shouldGoToMap = false
    private var deviceLocationEnabled = false

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        return foregroundFineLocationApproved() && foregroundCoarseLocationApproved() && backgroundPermissionApproved()
    }
    fun foregroundFineLocationApproved() :Boolean = (
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION))
    fun foregroundCoarseLocationApproved() :Boolean = (
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION))

    fun  backgroundPermissionApproved() : Boolean =
        if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (
            grantResults.isEmpty() ||
            grantResults[FINE_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            grantResults[COARSE_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            _viewModel.showSnackBarInt.postValue(R.string.permission_denied_explanation)

        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this.requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(this.requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    deviceLocationEnabled = false
                }
            } else {
                deviceLocationEnabled = false
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                deviceLocationEnabled = true
                onSaveReminder()
            }
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
        } else {
            _viewModel.showSnackBarInt.postValue(R.string.permission_denied_explanation)
        }
    }

    private val requestBackgroundPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissions ->
        if ((permissions == true || !runningQOrLater)
        ) {
        } else {
            _viewModel.showSnackBarInt.postValue(R.string.permission_denied_explanation)
        }
    }


    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        val permissionsArray = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        requestPermissionLauncher.launch(permissionsArray.toTypedArray())

        if (runningQOrLater) {
            permissionsArray.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        // Launch the permission request using the launcher

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        checkDeviceLocationSettingsAndStartGeofence()

        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.postValue(NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()))

        }

        binding.saveReminder.setOnClickListener {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun onSaveReminder() {
        val reminderData = ReminderDataItem(
            _viewModel.reminderTitle.value,
            _viewModel.reminderDescription.value,
            _viewModel.reminderSelectedLocationStr.value,
            _viewModel.latitude.value,
            _viewModel.longitude.value,
            UUID.randomUUID().toString()
        )

        if (_viewModel.validateEnteredData(reminderData)) {
            if (_viewModel.ignorePermissionChecks || addGeofence(LatLng(_viewModel.latitude.value!!, _viewModel.longitude.value!!), reminderData.id)) {
                _viewModel.saveReminder(reminderData)
                _viewModel.navigationCommand.value = NavigationCommand.Back
                _viewModel.showToast.value = requireActivity().getString(R.string.reminder_saved)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addGeofence(latlng: LatLng, id: String): Boolean {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            requestForegroundAndBackgroundLocationPermissions()
            return false
        }
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(
                latlng.latitude,
                latlng.longitude, 200.0f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(1000)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL)
            .addGeofence(geofence)
            .build()
        return if (foregroundAndBackgroundLocationPermissionApproved()) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            true

        } else {
            _viewModel.showSnackBarInt.postValue(R.string.permission_denied_explanation)
            false
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }
}
