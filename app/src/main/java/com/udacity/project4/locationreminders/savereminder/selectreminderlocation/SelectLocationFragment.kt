package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {


    companion object {
        private const val TAG = "SelectLocationFragment"
    }

    private var currentLocation: LatLng = LatLng(37.4219999, -122.0840575)
    private var poi: PointOfInterest? = null
    private var latlng: LatLng? = null

    private lateinit var mMap: GoogleMap
    private lateinit var coolMapStyle: MapStyleOptions
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var location: Location? = null
    var currentMapType: Int = 0

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        if (savedInstanceState != null) {
            currentMapType = savedInstanceState.getInt("9b402f8640a3f0d1")
        }
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation DONE
//        TODO: zoom to the user location after taking his permission DONE
//        TODO: add style to the map DONE
//        TODO: put a marker to location that the user selected DONE


//        TODO: call this function after the user confirms on the selected location
        binding.setLocation.setOnClickListener {
            onLocationSelected()
            _viewModel.navigationCommand.postValue(NavigationCommand.Back)
        }

        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener =

            object: LocationListener {
                override fun onLocationChanged(location: Location) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    mMap.clear()
                    mMap.addMarker(
                        MarkerOptions().position(currentLocation).title("My current Location")
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                }

                override fun onProviderEnabled(provider: String) {
                }

                override fun onProviderDisabled(provider: String) {

                }

        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun enableMyLocation() {
        if (foregroundLocationApproved()) {
            mMap.isMyLocationEnabled = true
        } else {
            requestForegroundLocationPermission()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestForegroundLocationPermission()
    }

    private fun foregroundLocationApproved(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                getLocation()
                setLocationClick(mMap)
                setPoiClick(mMap)

            } else {
                _viewModel.showSnackBarInt.postValue(R.string.permission_denied_explanation)
            }
        }


    private fun requestForegroundLocationPermission() {
        if (foregroundLocationApproved()) {
            return
        }

        val permissionsArray = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        requestPermissionLauncher.launch(permissionsArray)
    }

    private fun checkLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        poi?.also {
            val pointOfInterest: PointOfInterest = it
            _viewModel.longitude.postValue(pointOfInterest.latLng.longitude)
            _viewModel.latitude.postValue(pointOfInterest.latLng.latitude)
            _viewModel.selectedPOI.postValue(pointOfInterest)
            _viewModel.reminderSelectedLocationStr.postValue(it.name)

        } ?: run {
            _viewModel.longitude.postValue(this.latlng?.longitude ?: 0.0)
            _viewModel.latitude.postValue(this.latlng?.latitude ?: 0.0)
            _viewModel.selectedPOI.postValue(null)
            _viewModel.reminderSelectedLocationStr.postValue("Unknown")
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mMap.setMapStyle(null)
        return when (item.itemId) {

            R.id.normal_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.hybrid_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.satellite_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.cool_map -> {
                mMap.setMapStyle(coolMapStyle)
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        coolMapStyle = MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.cool_style)
        getLocation()
        setPoiClick(mMap)
        setLocationClick(mMap)

        enableMyLocation()
    }


    private fun getLocation(): Boolean {
        return if (!checkLocationPermissions())
            false
        else {

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
            true
        }

    }


    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            this@SelectLocationFragment.poi = poi
            mMap.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }

    private fun setLocationClick(map: GoogleMap) {
        map.setOnMapClickListener { latlng ->
            this@SelectLocationFragment.poi = null
            mMap.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(latlng)
                    .title("Unknown")
            )
            this.latlng = latlng
            poiMarker?.showInfoWindow()
        }
    }


}
