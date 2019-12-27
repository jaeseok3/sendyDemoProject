package com.example.sendymapdemo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var selectedMarker:Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private val markerClickListener = object : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(marker: Marker?): Boolean {
            if (marker == selectedMarker) {
                selectedMarker = null
                // Return true to indicate we have consumed the event and that we do not
                // want the the default behavior to occur (which is for the camera to move
                // such that the marker is centered and for the marker's info window to open,
                // if it has one).
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedMarker, 15.0f))
                Log.e("d","d")
                return true
            }

            selectedMarker = marker
            // Return false to indicate that we have not consumed the event and that
            // we wish for the default behavior to occur.
            return false
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val BuskDong = LatLng(35.205411, 129.077885)
        val BuskHae = LatLng(35.158713, 129.160248)
        val BuskPnu = LatLng(35.231028, 129.082287)
        val BuskGwang = LatLng(35.153028, 129.118666)
        mMap.addMarker(MarkerOptions().position(BuskDong).title("Marker in 동래"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(BuskDong))
        mMap.addMarker(MarkerOptions().position(BuskHae).title("Marker in 해운대"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(BuskHae))
        mMap.addMarker(MarkerOptions().position(BuskPnu).title("Marker in 부산대"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(BuskPnu))
        mMap.addMarker(MarkerOptions().position(BuskGwang).title("Marker in 광안리"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(BuskGwang))

        mMap.setOnMapClickListener { markerClickListener }


    }
}
