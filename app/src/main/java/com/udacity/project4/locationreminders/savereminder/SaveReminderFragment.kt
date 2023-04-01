package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {

    companion object {
        const val ACTION_GEOFENCE_EVENT = "action-geofence-event"
        const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 200
        const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 201

        private const val FINE_LOCATION_PERMISSION_INDEX = 0
        private const val COARSE_LOCATION_PERMISSION_INDEX = 1
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 2
    }

    override val _viewModel: SaveReminderViewModel by inject()
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

    private fun checkDeviceLocationSettingsAndStartGeofence(): Boolean {
        val locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager

        return if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            shouldGoToMap = true
            // Location is enabled
            true
        } else {
            shouldGoToMap = false
            // Location is disabled
            false
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // Permissions granted, proceed with your logic
        } else {
            _viewModel.showSnackBarInt.postValue(R.string.permission_denied_explanation)
        }
    }

    private val requestBackgroundPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissions ->
        if ((permissions == true || !runningQOrLater)
        ) {
            // Permissions granted, proceed with your logic
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
            //            Navigate to another fragment to get the user location
            requestForegroundAndBackgroundLocationPermissions()
            if(foregroundFineLocationApproved() && shouldGoToMap) {
                _viewModel.navigationCommand.postValue(
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()))
            } else {
                _viewModel.showSnackBarInt.postValue(R.string.permission_denied_explanation)
            }
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            val id = UUID.randomUUID().toString()

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
            if(latitude!=  null && longitude!=null) {
                addGeofence(LatLng(latitude, longitude), id)
            }


//             2) save the reminder to the local db
            _viewModel.validateAndSaveReminder(ReminderDataItem(title,description,location, latitude, longitude, id)
            )


        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addGeofence(latlng: LatLng, id: String) {
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
        requestForegroundAndBackgroundLocationPermissions()
        checkDeviceLocationSettingsAndStartGeofence()
        if(backgroundPermissionApproved() && shouldGoToMap) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
        } else {
            _viewModel.showSnackBarInt.postValue(R.string.permission_denied_explanation)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
