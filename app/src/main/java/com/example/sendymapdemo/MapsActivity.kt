package com.example.sendymapdemo

import android.location.Location
import android.os.Bundle
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var selectedMarker:Marker? = null
    internal lateinit var db:LocationDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        db= LocationDB(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

        val cityHallBus = LatLng(35.179792, 129.074997)

        val BuskDong = LatLng(35.205411, 129.077885)
        val BuskHae = LatLng(35.158713, 129.160248)
        val BuskPnu = LatLng(35.231028, 129.082287)
        val BuskGwang = LatLng(35.153028, 129.118666)

        mMap.addMarker(MarkerOptions().position(BuskDong).title("동래"))
        mMap.addMarker(MarkerOptions().position(BuskHae).title("해운대"))
        mMap.addMarker(MarkerOptions().position(BuskPnu).title("부산대"))
        mMap.addMarker(MarkerOptions().position(BuskGwang).title("광안리"))
        /* Move Camera initially in Busan City Hall */
        /* must change to user's current position */
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityHallBus, 11.0f))
        mMap.setOnMarkerClickListener(markerClickListener)
        mMap.setOnMapClickListener(mapClickListener)
        db.listMarker(mMap)
    }

}
