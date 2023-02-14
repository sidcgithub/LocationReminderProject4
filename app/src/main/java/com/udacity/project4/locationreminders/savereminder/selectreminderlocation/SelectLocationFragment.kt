package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {


    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
        private const val TAG = "SelectLocationFragment"
    }

    private var currentLocation: LatLng = LatLng(0.0,0.0)
    private var poi: PointOfInterest? = null
    private var latlng:LatLng? =null

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var location: Location? = null
   var  currentMapType: Int = 0

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
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
        binding.setLocation.setOnClickListener { onLocationSelected()
            requireActivity().onBackPressed()
        }

        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener { }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Do if the permission is granted
            getLocation()
            getCurrentLocation()
        } else {
            // Do otherwise
            Toast.makeText(
                requireContext(),
                "Please enable location permissions",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getCurrentLocation() {
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        currentLocation = LatLng(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
        mMap.addMarker(MarkerOptions().position(currentLocation).title("My current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
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

        }?: run {
            _viewModel.longitude.postValue(this.latlng?.longitude ?: 0.0)
            _viewModel.latitude.postValue(this.latlng?.latitude ?: 0.0)
            _viewModel.selectedPOI.postValue(null)
            _viewModel.reminderSelectedLocationStr.postValue("Unknown")
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
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
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.cool_style))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (getLocation()) {
            getCurrentLocation()
            setPoiClick(mMap)
            setLocationClick(mMap)
        }
    }


    fun getLocation(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return false
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            return false
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            return false
        }

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
            return true

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
