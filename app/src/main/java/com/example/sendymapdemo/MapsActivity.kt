package com.example.sendymapdemo

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    internal lateinit var db:LocationDB
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        db= LocationDB(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun placeMarkerOnMap(location: LatLng){
        val markerOptions = MarkerOptions().position(location).title("currentLocation")
        mMap.addMarker(markerOptions)
    }

    private fun setUpMap(){
        val fab: View = findViewById(R.id.fab)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){ location ->
            if (location != null){
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 11.0f))
            }
        }

        fab.setOnClickListener { view->
            Snackbar.make(view, "Add Marker Complete", Snackbar.LENGTH_SHORT)
                .setAction("Action", null)
                .show()
            val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            placeMarkerOnMap(latLng)
            db.AddMarker(latLng, "current Location")
        }
    }

    private val markerClickListener =
        GoogleMap.OnMarkerClickListener { marker ->
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker?.position))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            makeText(App.instance.Context(), "Marker in "+marker?.title, LENGTH_SHORT).show()
            val adapter = CustomInfoWindowAdapter(this)
            mMap.setInfoWindowAdapter(adapter)
            marker.showInfoWindow()
            true
        }

    private val mapClickListener =
        GoogleMap.OnMapClickListener { map ->
            var a = LatLng(map.latitude,map.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(a))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            db.AddMarker(a,"hi")
            mMap.addMarker(MarkerOptions().position(a).title("hi"))
        }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled

        mMap.setOnMarkerClickListener(markerClickListener)

        setUpMap()
        mMap.setOnMapClickListener(mapClickListener)
        db.listMarker(mMap)
    }
}
