package com.example.sendymapdemo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


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

    private val markerClickListener =
        GoogleMap.OnMarkerClickListener { marker ->
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker?.position))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            makeText(App.instance.Context(), marker?.title, LENGTH_SHORT).show()
            marker.showInfoWindow()
            true
        }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val cityHallBus = LatLng(35.179792, 129.074997)

        val BuskDong = LatLng(35.205411, 129.077885)
        val BuskHae = LatLng(35.158713, 129.160248)
        val BuskPnu = LatLng(35.231028, 129.082287)
        val BuskGwang = LatLng(35.153028, 129.118666)

        mMap.addMarker(MarkerOptions().position(BuskDong).title("Marker in 동래"))
        mMap.addMarker(MarkerOptions().position(BuskHae).title("Marker in 해운대"))
        mMap.addMarker(MarkerOptions().position(BuskPnu).title("Marker in 부산대"))
        mMap.addMarker(MarkerOptions().position(BuskGwang).title("Marker in 광안리"))

        /* Move Camera initially in Busan City Hall */
        /* must change to user's current position */
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityHallBus, 11.0f))
        mMap.setOnMarkerClickListener(markerClickListener)

        mMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            // Use default InfoWindow frame
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            // Defines the contents of the InfoWindow
            override fun getInfoContents(marker: Marker): View { // Getting view from the layout file info_window_layout
                val v: View = layoutInflater.inflate(R.layout.info_layout, null)
                // Getting the position from the marker
                val latLng = marker.position
                // Getting reference to the TextView to set latitude
                //val tvLat = v.findViewById<View>(R.id.tv_lat) as TextView
                // Getting reference to the TextView to set longitude
                //val tvLng = v.findViewById<View>(R.id.tv_lng) as TextView
                // Setting the latitude
                //tvLat.text = "Latitude:" + latLng.latitude
                // Setting the longitude
                //tvLng.text = "Longitude:" + latLng.longitude

                //val img = v.findViewById<View>(R.id.dongraeImg)
                // Returning the view containing InfoWindow contents
                return v
            }
        })
    }
}
