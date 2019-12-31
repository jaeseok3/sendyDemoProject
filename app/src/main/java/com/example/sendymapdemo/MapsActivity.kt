package com.example.sendymapdemo

import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView

import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var selectedMarker: Marker? = null
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var recyclerView : RecyclerView
    internal lateinit var db:LocationDB
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    lateinit var adapter:NavAdapter
    lateinit var layoutManager: LinearLayoutManager
    //마커리스트 생성
    val Markerlist = ArrayList<markerData>()



    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    ////스위치 상태 저장///
    override fun onResume() {
        super.onResume()
        restoreState()
    }
    override fun onPause() {
        super.onPause()
        saveState()
    }

    private fun restoreState(){ //onCreate, onResume에서 동작
        var pref : SharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        fab2.isChecked = pref.getBoolean("pref_bool",true)
    }

    private fun saveState(){ //onPause, onDestory에서 동작
        var pref : SharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        var editor : SharedPreferences.Editor = pref.edit()
            editor.putBoolean("pref_bool",fab2.isChecked)
        editor.commit()
    }
    ////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = LocationDB(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        recyclerView = findViewById(R.id.list)
        layoutManager = LinearLayoutManager(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        //navView.setNavigationItemSelectedListener(this.)
        //drawer 생성


        //어댑터 생성
        adapter = NavAdapter(Markerlist)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        //recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun placeMarkerOnMap(location: LatLng){
        val markerOptions = MarkerOptions().position(location).title("currentLocation")
        mMap.addMarker(markerOptions)
    }

    private fun setUpMap(){
        val fab: View = findViewById(R.id.fab)
        val fab2: Switch = findViewById(R.id.fab2)

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

            val newMarkerData = markerData(lastLocation.latitude, lastLocation.longitude, "NAME")
            Markerlist.add(newMarkerData)
            adapter.notifyDataSetChanged()
        }

        fab2.setOnCheckedChangeListener { switch, isChanged -> //지도 좌측 아래에 있는 Switch
            if(isChanged){ //켜졌을때 동작시키면 됨
                makeText(App.instance.Context(),"Switch is on", LENGTH_SHORT).show()
            }else{ //꺼졌을때 동작시키면 됨
                makeText(App.instance.Context(),"Switch is off", LENGTH_SHORT).show()
            }
        }



    }

    private val markerClickListener =
        GoogleMap.OnMarkerClickListener { marker ->

            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(marker?.position!!.latitude+0.005,marker?.position!!.longitude)))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            makeText(App.instance.Context(), "Marker in "+marker?.title, LENGTH_SHORT).show()
            val adapter = CustomInfoWindowAdapter(this)
            mMap.setInfoWindowAdapter(adapter)
            marker.showInfoWindow()
            true
        }

    private val mapClickListener =
        GoogleMap.OnMapClickListener { map ->
            var latLng = LatLng(map.latitude,map.longitude)

            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(map.latitude,map.longitude)))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            db.AddMarker(latLng,"hi")
            mMap.addMarker(MarkerOptions().position(latLng).title("hi"))
        }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled

        mMap.setOnMarkerClickListener(markerClickListener)
        val cityHallBus = LatLng(35.179792, 129.074997)

        /* Move Camera initially in Busan City Hall */
        /* must change to user's current position */
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityHallBus, 11.0f))
        setUpMap()
        mMap.setOnMapClickListener(mapClickListener)
        db.listMarker(mMap)
    }
}
